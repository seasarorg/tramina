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

import org.h2.jdbc.JdbcConnection;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.seasar.tramina.resource.ManagedConnection;
import org.seasar.tramina.resource.impl.AbstractConnectionManager;
import org.seasar.tramina.resource.jdbc.LogicalConnection;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * 
 * 
 * @author koichik
 */
public class JdbcResourceManagerTest {
    StringBuilder buf = new StringBuilder();

    @Test
    public void test0Cm() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:");
        JdbcResourceManager rm =
            new JdbcResourceManager().setManagedConnectionFactory(
                new XADSManagedConnectionFactory()
                    .setXADataSource(ds)
                    .initialize()).initialize();
        Connection con = rm.getConnection();
        assertThat(con, is(notNullValue()));
        assertThat(con.isClosed(), is(false));
        assertThat(con.isWrapperFor(LogicalConnection.class), is(true));
        assertThat(con.unwrap(LogicalConnection.class), is(sameInstance(con)));
        assertThat(con.isWrapperFor(JdbcConnection.class), is(true));
        assertThat(buf.toString(), is(equalTo("")));

        con.close();
        assertThat(con.isClosed(), is(true));
        assertThat(buf.toString(), is(equalTo("")));

        rm.dispose();
        assertThat(buf.toString(), is(equalTo("")));
    }

    @Test
    public void test1Cm() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:");
        JdbcResourceManager rm =
            new JdbcResourceManager()
                .addConnectionManager(
                    new ConnectionManagerImpl("cm1").initialize())
                .setManagedConnectionFactory(
                    new XADSManagedConnectionFactory()
                        .setXADataSource(ds)
                        .initialize())
                .initialize();
        Connection con = rm.getConnection();
        assertThat(con, is(notNullValue()));
        assertThat(con.isClosed(), is(false));
        assertThat(buf.toString(), is(equalTo("cm1Created:")));

        con.close();
        assertThat(con.isClosed(), is(true));
        assertThat(buf.toString(), is(equalTo("cm1Created:cm1Closed:")));

        rm.dispose();
        assertThat(
            buf.toString(),
            is(equalTo("cm1Created:cm1Closed:cm1Destroyed:")));
    }

    @Test
    public void test2Cm() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:");
        JdbcResourceManager rm =
            new JdbcResourceManager()
                .addConnectionManager(
                    new ConnectionManagerImpl("cm1").initialize())
                .addConnectionManager(
                    new ConnectionManagerImpl("cm2").initialize())
                .setManagedConnectionFactory(
                    new XADSManagedConnectionFactory()
                        .setXADataSource(ds)
                        .initialize())
                .initialize();
        Connection con = rm.getConnection();
        assertThat(con, is(notNullValue()));
        assertThat(con.isClosed(), is(false));
        assertThat(buf.toString(), is(equalTo("cm1Created:cm2Created:")));

        con.close();
        assertThat(con.isClosed(), is(true));
        assertThat(
            buf.toString(),
            is(equalTo("cm1Created:cm2Created:cm1Closed:cm2Closed:")));

        rm.dispose();
        assertThat(
            buf.toString(),
            is(equalTo("cm1Created:cm2Created:cm1Closed:cm2Closed:cm1Destroyed:cm2Destroyed:")));
    }

    public class ConnectionManagerImpl
            extends
            AbstractConnectionManager<ConnectionManagerImpl, Connection, SQLException> {

        protected String name;

        public ConnectionManagerImpl(String name) {
            this.name = name;
        }

        @Override
        public ManagedConnection<Connection, SQLException> getManagedConnection()
                throws SQLException {
            buf.append(name).append("Created:");
            return super.getManagedConnection();
        }

        @Override
        public void logicalConnectionClosed(
                ManagedConnection<Connection, SQLException> managedConnection)
                throws SQLException {
            buf.append(name).append("Closed:");
            super.logicalConnectionClosed(managedConnection);
        }

        public void physicalConnectionErrorOccurred(
                ManagedConnection<Connection, SQLException> managedConnection,
                SQLException cause) throws SQLException {
            buf.append(name).append("Errored:");
            super.physicalConnectionErrorOccurred(managedConnection, cause);
        }

        @Override
        public void destroy() throws SQLException {
            buf.append(name).append("Destroyed:");
            super.destroy();
        }

        @Override
        protected void doInitialize() {
        }

        @Override
        protected void doDispose() {
        }

    }

}
