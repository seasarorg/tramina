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

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.seasar.tramina.spi.TraminaTransactionManager;
import org.seasar.tramina.transaction.impl.TraminaTransactionManagerImpl;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * 
 * 
 * @author koichik
 */
public class JdbcTxBoundConnectionManagerTest {

    @Test
    public void test0() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:");

        TraminaTransactionManager tm =
            new TraminaTransactionManagerImpl().initialize();
        tm.begin();

        JdbcResourceManager rm =
            new JdbcResourceManager().addConnectionManager(
                new JdbcTxBoundConnectionManager()
                    .setTransactionManager(tm)
                    .initialize()).setManagedConnectionFactory(
                new XADSManagedConnectionFactory()
                    .setXADataSource(ds)
                    .initialize()).initialize();
        Connection con = rm.getConnection();
        assertThat(con, is(notNullValue()));
        assertThat(con.isClosed(), is(false));
        assertThat(rm.getConnection(), is(sameInstance(con)));

        con.close();
        assertThat(con.isClosed(), is(false));
        assertThat(rm.getConnection(), is(sameInstance(con)));

        tm.commit();
        assertThat(con.isClosed(), is(true));

        rm.dispose();
    }

}
