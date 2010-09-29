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
package org.seasar.tramina.transaction.impl;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.seasar.tramina.resource.jdbc.impl.LastXAResourceImpl;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * 
 * 
 * @author koichik
 */
public class ToplevelTransactionImplTest {

    JdbcDataSource ds;

    @Before
    public void before() throws Exception {
        ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:db1");
    }

    @Test
    public void testCommit0() throws Exception {
        TraminaTransactionManagerImpl tm =
            new TraminaTransactionManagerImpl().initialize();
        ToplevelTransactionImpl tx = new ToplevelTransactionImpl(tm);
        tx.begin();
        tx.commit();
    }

    @Test
    public void testCommit1pc() throws Exception {
        TraminaTransactionManagerImpl tm =
            new TraminaTransactionManagerImpl().initialize();
        ToplevelTransactionImpl tx = new ToplevelTransactionImpl(tm);
        tx.begin();
        assertThat(tx.participantResources.isEmpty(), is(true));

        XAConnection con1 = ds.getXAConnection();
        XAResource xa1 = con1.getXAResource();
        tx.enlistResource(xa1);
        assertThat(tx.participantResources.isEmpty(), is(false));
        assertThat(tx.participantResources.size(), is(equalTo(1)));
        assertThat(tx.participantResources.canOnePhaseCommit(), is(true));

        tx.commit();
    }

    @Test
    public void testCommit2pc() throws Exception {
        TraminaTransactionManagerImpl tm =
            new TraminaTransactionManagerImpl().initialize();
        ToplevelTransactionImpl tx = new ToplevelTransactionImpl(tm);
        tx.begin();
        assertThat(tx.participantResources.isEmpty(), is(true));

        XAConnection con1 = ds.getXAConnection();
        XAResource xa1 = con1.getXAResource();
        tx.enlistResource(xa1);
        assertThat(tx.participantResources.isEmpty(), is(false));
        assertThat(tx.participantResources.size(), is(equalTo(1)));

        XAConnection con2 = ds.getXAConnection();
        XAResource xa2 = con2.getXAResource();
        tx.enlistResource(xa2);
        assertThat(tx.participantResources.isEmpty(), is(false));
        assertThat(tx.participantResources.size(), is(equalTo(2)));
        assertThat(tx.participantResources.canOnePhaseCommit(), is(false));

        tx.commit();
    }

    @Test
    public void testCommit2pcWithLastResource() throws Exception {
        TraminaTransactionManagerImpl tm =
            new TraminaTransactionManagerImpl().initialize();
        ToplevelTransactionImpl tx = new ToplevelTransactionImpl(tm);
        tx.begin();
        assertThat(tx.participantResources.isEmpty(), is(true));

        XAConnection con1 = ds.getXAConnection();
        XAResource xa1 = new LastXAResourceImpl(con1.getConnection());
        tx.enlistResource(xa1);
        assertThat(tx.participantResources.isEmpty(), is(false));
        assertThat(tx.participantResources.size(), is(1));
        assertThat(tx.participantResources.hasLastResource(), is(true));

        XAConnection con2 = ds.getXAConnection();
        XAResource xa2 = con2.getXAResource();
        tx.enlistResource(xa2);
        assertThat(tx.participantResources.isEmpty(), is(false));
        assertThat(tx.participantResources.size(), is(equalTo(2)));
        assertThat(tx.participantResources.canOnePhaseCommit(), is(false));

        tx.commit();
    }

}
