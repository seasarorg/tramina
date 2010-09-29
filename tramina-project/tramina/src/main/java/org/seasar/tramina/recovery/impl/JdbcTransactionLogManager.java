/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.tramina.recovery.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;

import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.xa.Xid;

import org.seasar.tramina.activity.Component;
import org.seasar.tramina.activity.impl.AbstractActiveComponent;
import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.recovery.JdbcTransactionLogManagerDialect;
import org.seasar.tramina.recovery.TransacstionResultType;
import org.seasar.tramina.recovery.TransactionLogManager;
import org.seasar.tramina.recovery.exception.AcquireConnectionFailedException;
import org.seasar.tramina.recovery.exception.AcquireTransactionResultFailedException;
import org.seasar.tramina.recovery.exception.InsertTransactionLogFailedException;
import org.seasar.tramina.recovery.exception.TransactionLogTableInitializationFailedException;
import org.seasar.tramina.spi.TwoPhaseCommitEvent;
import org.seasar.tramina.spi.WorkManager;
import org.seasar.tramina.util.JdbcUtil.ConnectionProcessor;
import org.seasar.tramina.util.JdbcUtil.PreparedStatementProcessor;
import org.seasar.tramina.util.JdbcUtil.ResultSetProcessor;

import static java.util.Arrays.*;
import static org.seasar.tramina.activity.impl.ActivityAssertions.*;
import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.recovery.RecoveryMessages.*;
import static org.seasar.tramina.recovery.TransacstionResultType.*;
import static org.seasar.tramina.util.JdbcUtil.*;

/**
 * 
 * 
 * @author koichik
 */
public class JdbcTransactionLogManager extends
        AbstractActiveComponent<JdbcTransactionLogManager> implements
        TransactionLogManager, Component {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(JdbcTransactionLogManager.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected WorkManager workManager;

    protected DataSource dataSource;

    protected JdbcTransactionLogManagerDialect dialect;

    protected long intervalSeconds = 60;

    protected ScheduledFuture<?> scheduledFuture;

    // /////////////////////////////////////////////////////////////////
    // instance methods from TransactionLogManager
    //
    @Override
    public void open(final TwoPhaseCommitEvent event) throws SystemException {
        assertInitialized(this);
        assertParameterNotNull("event", event);
        try {
            final Connection con = dataSource.getConnection();
            con.close();
        } catch (final SQLException e) {
            throw new AcquireConnectionFailedException(this, event
                .getTransaction(), e);
        }
    }

    @Override
    public void writeBeforeCommitLog(final TwoPhaseCommitEvent event)
            throws SystemException {
        assertInitialized(this);
        assertParameterNotNull("event", event);
        try {
            process(
                dataSource,
                dialect.getInsertSql(),
                new PreparedStatementProcessor<Integer>() {
                    @Override
                    public Integer process(final PreparedStatement ps)
                            throws SQLException {
                        dialect.bindParameterToInsertSql(ps, event);
                        return ps.executeUpdate();
                    }
                });
            if (logger.isDebugEnabled()) {
                logger.debug(TRANSACTION_LOG_INSERTED.format(this, event
                    .getTransaction()));
            }
        } catch (final SQLException e) {
            throw new InsertTransactionLogFailedException(this, event
                .getTransaction(), e);
        }
    }

    @Override
    public void writeAfterCommitLog(final TwoPhaseCommitEvent event)
            throws SystemException {
    }

    @Override
    public void close(final TwoPhaseCommitEvent event) throws SystemException {
    }

    @Override
    public TransacstionResultType[] getTransactionResult(final Xid[] indoubtXids)
            throws SystemException {
        assertInitialized(this);
        final TransacstionResultType[] result =
            new TransacstionResultType[indoubtXids.length];
        fill(result, ROLLED_BACK);
        try {
            process(
                dataSource,
                dialect.getSelectSql(),
                new ResultSetProcessor<Void>() {
                    @Override
                    public Void process(final ResultSet rs) throws SQLException {
                        while (rs.next()) {
                            final Xid xid = dialect.createXid(rs);
                            for (int i = 0; i < indoubtXids.length; ++i) {
                                if (xid.equals(indoubtXids[i])) {
                                    result[i] = COMMITTED;
                                }
                            }
                        }
                        return null;
                    }
                });
        } catch (final SQLException e) {
            throw new AcquireTransactionResultFailedException(this, e);
        }
        return result;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from AbstractActiveComponent
    //
    @Override
    protected void doInitialize() {
        assertPropertyNotNull(this, "workManager", workManager);
        assertPropertyNotNull(this, "dataSource", dataSource);
        assertPropertyNotNull(this, "dialect", dialect);
        try {
            process(dataSource, new ConnectionProcessor<Void>() {
                @Override
                public Void process(final Connection con) throws SQLException {
                    if (!isLogTableExist(con)) {
                        createLogTable(con);
                    }
                    return null;
                }
            });
        } catch (final SQLException e) {
            throw new TransactionLogTableInitializationFailedException(this, e);
        }
    }

    @Override
    protected void doStart() {
        scheduledFuture = workManager.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    deleteExpiredRecord();
                } catch (final Exception e) {
                    logger.error(DELETE_EXPIRED_TRANSACTION_LOG_FAILED
                        .format(this), e);
                }
            }
        }, intervalSeconds, intervalSeconds);
        if (logger.isDebugEnabled()) {
            logger.debug(TRANSACTION_LOG_MANAGER_STARTED.format(this));
        }
    }

    @Override
    protected void doStop() {
        scheduledFuture.cancel(false);
        if (logger.isDebugEnabled()) {
            logger.debug(TRANSACTION_LOG_MANAGER_STOPPED.format(this));
        }
    }

    @Override
    protected void doDispose() {
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods
    //
    @Override
    public String toString() {
        return super.toString() + "[activityStatus : <" + activityStatus
            + ">, intervalSeconds : <" + intervalSeconds + ">]";
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods
    //
    public void deleteExpiredRecord() throws SQLException {
        assertInitialized(this);
        final int rows =
            process(
                dataSource,
                dialect.getDeleteSql(),
                new PreparedStatementProcessor<Integer>() {
                    @Override
                    public Integer process(final PreparedStatement ps)
                            throws SQLException {
                        return ps.executeUpdate();
                    }
                });
        if (rows > 0 && logger.isDebugEnabled()) {
            logger.debug(EXPIRED_TRANSACTION_LOG_DELETED.format(this, rows));
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for properties
    //
    /**
     * @param workManager
     *            the workManager to set
     */
    public JdbcTransactionLogManager setWorkManager(
            final WorkManager workManager) {
        assertBeforeInitialized(this);
        assertParameterNotNull("workManager", workManager);
        this.workManager = workManager;
        return this;
    }

    /**
     * @param dataSource
     *            the dataSource to set
     */
    public JdbcTransactionLogManager setDataSource(final DataSource dataSource) {
        assertBeforeInitialized(this);
        assertParameterNotNull("dataSource", dataSource);
        this.dataSource = dataSource;
        return this;
    }

    /**
     * @param dialect
     *            the dialect to set
     */
    public JdbcTransactionLogManager setDialect(
            final JdbcTransactionLogManagerDialect dialect) {
        assertBeforeInitialized(this);
        assertParameterNotNull("dialect", dialect);
        this.dialect = dialect;
        return this;
    }

    /**
     * @param intervalSeconds
     *            the intervalSeconds to set
     */
    public JdbcTransactionLogManager setIntervalSeconds(
            final long intervalSeconds) {
        assertBeforeInitialized(this);
        assertParameterNotNull("intervalSeconds", intervalSeconds);
        this.intervalSeconds = intervalSeconds;
        return this;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for internal
    //
    protected boolean isLogTableExist(final Connection con) throws SQLException {
        final DatabaseMetaData dbmd = con.getMetaData();
        final ResultSet rs =
            dbmd.getTables(
                dialect.getCatalogName(),
                dialect.getSchemaName(),
                dialect.getTableName(),
                new String[] { "TABLE" });
        try {
            return rs.next();
        } finally {
            rs.close();
        }
    }

    protected void createLogTable(final Connection con) throws SQLException {
        final boolean autoCommit = con.getAutoCommit();
        con.setAutoCommit(true);
        try {
            for (final String sql : dialect.getCreateTableSql()) {
                final PreparedStatement st = con.prepareStatement(sql);
                try {
                    st.execute();
                } finally {
                    st.close();
                }
            }
        } finally {
            con.setAutoCommit(autoCommit);
        }
    }

}
