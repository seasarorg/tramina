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
package org.seasar.tramina;

import java.sql.Connection;

import javax.transaction.xa.Xid;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.tramina.recovery.TransacstionResultType;
import org.seasar.tramina.recovery.impl.JdbcTransactionLogManager;
import org.seasar.tramina.recovery.impl.RecoveryManagerImpl;
import org.seasar.tramina.recovery.impl.dialect.H2Dialect;
import org.seasar.tramina.resource.jdbc.impl.JdbcPoolingConnectionManager;
import org.seasar.tramina.resource.jdbc.impl.JdbcResourceManager;
import org.seasar.tramina.resource.jdbc.impl.JdbcTxBoundConnectionManager;
import org.seasar.tramina.resource.jdbc.impl.XADSManagedConnectionFactory;
import org.seasar.tramina.spi.RecoveryManager;
import org.seasar.tramina.spi.ToplevelTransaction;
import org.seasar.tramina.spi.TraminaTransaction;
import org.seasar.tramina.spi.TraminaTransactionManager;
import org.seasar.tramina.transaction.impl.TraminaTransactionManagerImpl;
import org.seasar.tramina.work.impl.WorkManagerImpl;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * 
 * 
 * @author koichik
 */
public class NestedTransactionTest {

    JdbcDataSource ds1;

    JdbcDataSource ds2;

    Connection con1;

    Connection con2;

    WorkManagerImpl wm;

    TraminaTransactionManager tm;

    JdbcTransactionLogManager logm;

    RecoveryManager recm;

    JdbcResourceManager rm1;

    JdbcResourceManager rm2;

    @Before
    public void before() throws Exception {
        ds1 = new JdbcDataSource();
        ds1.setURL("jdbc:h2:mem:last");
        con1 = ds1.getConnection();

        ds2 = new JdbcDataSource();
        ds2.setURL("jdbc:h2:mem:first");
        con2 = ds2.getConnection();

        wm = new WorkManagerImpl().initialize().start();

        tm = new TraminaTransactionManagerImpl().setDomainId(100).initialize();

        rm1 =
            new JdbcResourceManager().addConnectionManager(
                new JdbcTxBoundConnectionManager()
                    .setTransactionManager(tm)
                    .initialize()).addConnectionManager(
                new JdbcPoolingConnectionManager()
                    .setWorkManager(wm)
                    .setMaxIdleConnections(2)
                    .setMaxIdleSeconds(0)
                    .initialize()).setManagedConnectionFactory(
                new XADSManagedConnectionFactory()
                    .setXADataSource(ds1)
                    .setLastResource(true)
                    .setSubtransactionAwareResource(true)
                    .initialize()).initialize();

        logm =
            new JdbcTransactionLogManager().setWorkManager(wm).setDataSource(
                rm1).setDialect(new H2Dialect()).initialize().start();
        recm =
            new RecoveryManagerImpl()
                .setTransactionManager(tm)
                .setTransactionLogManager(logm)
                .initialize();

        rm2 =
            new JdbcResourceManager().addConnectionManager(
                new JdbcTxBoundConnectionManager()
                    .setTransactionManager(tm)
                    .initialize()).addConnectionManager(
                new JdbcPoolingConnectionManager()
                    .setWorkManager(wm)
                    .setMaxIdleConnections(2)
                    .setMaxIdleSeconds(0)
                    .initialize()).setManagedConnectionFactory(
                new XADSManagedConnectionFactory()
                    .setXADataSource(ds2)
                    .initialize()).setRecoveryManager(recm).initialize();
    }

    @After
    public void after() throws Exception {
        con1.close();
        con2.close();
    }

    @Test
    public void testCommit() throws Exception {
        tm.begin(); // tx1
        TraminaTransaction tx1 = tm.getTransaction();
        assertThat(tx1.isToplevel(), is(true));
        assertThat(tx1.getParent(), is(nullValue()));

        Connection con1 = rm1.getConnection();
        con1.close();

        tm.begin(); // tx1-1
        TraminaTransaction tx1_1 = tm.getTransaction();
        assertThat(tx1_1.isToplevel(), is(false));
        assertThat(tx1_1.getParent(), is(sameInstance(tx1)));

        Connection con2 = rm2.getConnection();
        con2.close();

        tm.begin(); // tx1-1-1
        tm.begin(); // tx1-1-1-1

        tx1_1.rollback(); // tx1-1
        assertThat(tm.getTransaction(), is(sameInstance(tx1)));
        assertThat(con1.isClosed(), is(false));
        assertThat(con2.isClosed(), is(true));

        tm.begin(); // tx1-2
        Connection con3 = rm2.getConnection();
        assertThat(con3, is(not(sameInstance(con2))));

        tm.commit(); // / tx1-2
        assertThat(con1.isClosed(), is(false));
        assertThat(con3.isClosed(), is(false));

        tm.commit(); // tx1
        assertThat(con1.isClosed(), is(true));
        assertThat(con3.isClosed(), is(true));

        TransacstionResultType[] result =
            logm.getTransactionResult(new Xid[] { ((ToplevelTransaction) tx1)
                .getXid() });
        assertThat(result, is(not(nullValue())));
        assertThat(result.length, is(1));
        assertThat(result[0], is(TransacstionResultType.COMMITTED));

        logm.stop().dispose();
        wm.stop().dispose();
    }

    @Test
    public void testRollback() throws Exception {
        tm.begin(); // tx1
        TraminaTransaction tx1 = tm.getTransaction();
        assertThat(tx1.isToplevel(), is(true));
        assertThat(tx1.getParent(), is(nullValue()));

        Connection con1 = rm1.getConnection();
        con1.close();

        tm.begin(); // tx1-1
        TraminaTransaction tx1_1 = tm.getTransaction();
        assertThat(tx1_1.isToplevel(), is(false));
        assertThat(tx1_1.getParent(), is(sameInstance(tx1)));

        Connection con2 = rm2.getConnection();
        con2.close();

        tx1.rollback(); // tx1
        assertThat(tm.getTransaction(), is(nullValue()));
        assertThat(con1.isClosed(), is(true));
        assertThat(con2.isClosed(), is(true));

        TransacstionResultType[] result =
            logm.getTransactionResult(new Xid[] { ((ToplevelTransaction) tx1)
                .getXid() });
        assertThat(result, is(not(nullValue())));
        assertThat(result.length, is(1));
        assertThat(result[0], is(TransacstionResultType.ROLLED_BACK));

        logm.stop().dispose();
        wm.stop().dispose();
    }

    @Test
    public void testSuspendResume() throws Exception {
        tm.begin(); // tx1
        TraminaTransaction tx1 = tm.getTransaction();
        assertThat(tx1.isToplevel(), is(true));
        assertThat(tx1.getParent(), is(nullValue()));

        Connection con1 = rm1.getConnection();
        con1.close();

        tm.begin(); // tx1-1
        TraminaTransaction tx1_1 = tm.getTransaction();
        assertThat(tx1_1.isToplevel(), is(false));
        assertThat(tx1_1.getParent(), is(sameInstance(tx1)));

        Connection con2 = rm2.getConnection();
        con2.close();

        tm.begin(); // tx1-1-1
        TraminaTransaction tx1_1_1 = tm.getTransaction();

        TraminaTransaction suspended = tm.suspend();
        assertThat(suspended, is(sameInstance(tx1_1_1)));
        assertThat(tm.getTransaction(), is(nullValue()));
        tm.begin(); // tx2
        ToplevelTransaction tx2 = tm.getToplevelTransaction();
        rm1.getConnection().close();
        rm2.getConnection().close();
        tm.commit(); // tx2
        tm.resume(suspended);
        assertThat(tm.getTransaction(), is(sameInstance(suspended)));

        tm.commit(); // / tx1-1-1
        tm.commit(); // / tx1-1
        assertThat(con1.isClosed(), is(false));
        assertThat(con2.isClosed(), is(false));

        tm.commit(); // tx1
        assertThat(con1.isClosed(), is(true));
        assertThat(con2.isClosed(), is(true));

        TransacstionResultType[] result =
            logm.getTransactionResult(new Xid[] {
                ((ToplevelTransaction) tx1).getXid(), tx2.getXid() });
        assertThat(result, is(not(nullValue())));
        assertThat(result.length, is(2));
        assertThat(result[0], is(TransacstionResultType.COMMITTED));
        assertThat(result[1], is(TransacstionResultType.COMMITTED));

        logm.stop().dispose();
        wm.stop().dispose();
    }

}
