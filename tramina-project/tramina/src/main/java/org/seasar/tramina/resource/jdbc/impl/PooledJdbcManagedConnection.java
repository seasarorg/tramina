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
package org.seasar.tramina.resource.jdbc.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.transaction.xa.XAResource;

import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.resource.ManagedConnectionEvent;
import org.seasar.tramina.resource.jdbc.XAResourceFactory;

import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.resource.jdbc.JdbcResourceMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public class PooledJdbcManagedConnection extends AbstractJdbcManagedConnection
        implements ConnectionEventListener {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(PooledJdbcManagedConnection.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected PooledConnection pooledConnection;

    protected Connection physicalConnection;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param xaResource
     * @param pooledConnection
     */
    public PooledJdbcManagedConnection(
            final XAResourceFactory xaResourceFactory,
            final PooledConnection pooledConnection) {
        super(xaResourceFactory);
        assertParameterNotNull("pooledConnection", pooledConnection);
        this.pooledConnection = pooledConnection;
        pooledConnection.addConnectionEventListener(this);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from ManagedConnection
    //
    @Override
    public XAResource getXAResource() throws SQLException {
        if (xaResource == null) {
            xaResource =
                xaResourceFactory.createXAResource(
                    pooledConnection,
                    getPhysicalConnection());
        }
        return xaResource;
    }

    @Override
    public void cleanup() throws SQLException {
        try {
            if (logicalConnection != null) {
                logicalConnection.cleanup();
                if (logger.isDebugEnabled()) {
                    logger.debug(LOGICAL_CONNECTION_CLOSED.format(this));
                }
            }
            if (physicalConnection != null && !physicalConnection.isClosed()) {
                physicalConnection.setAutoCommit(true);
                physicalConnection.close();
            }
        } finally {
            logicalConnection = null;
            physicalConnection = null;
            xaResource = null;
        }
    }

    @Override
    public void destroy() throws SQLException {
        try {
            cleanup();
            if (pooledConnection != null) {
                pooledConnection.removeConnectionEventListener(this);
                pooledConnection.close();
            }
        } finally {
            pooledConnection = null;
        }
    }

    @Override
    public boolean isDestroyed() {
        return pooledConnection == null;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from ConnectionEventListener
    //
    @Override
    public void connectionClosed(final ConnectionEvent event) {
    }

    @Override
    public void connectionErrorOccurred(final ConnectionEvent event) {
        assertParameterNotNull("event", event);
        fireConnectionErrorOccurred(new ManagedConnectionEvent<Connection, SQLException>(
            this,
            event.getSQLException()));
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from JdbcManagedConnection
    //
    @Override
    public Connection getPhysicalConnection() throws SQLException {
        if (physicalConnection == null) {
            physicalConnection = pooledConnection.getConnection();
        }
        return physicalConnection;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public String toString() {
        return super.toString() + "[pooledConnection : <" + pooledConnection
            + ">, physicalConnection : <" + physicalConnection
            + ">, logicalConnection : <" + logicalConnection
            + ">, xaResource : <" + xaResource + ">]";
    }

}
