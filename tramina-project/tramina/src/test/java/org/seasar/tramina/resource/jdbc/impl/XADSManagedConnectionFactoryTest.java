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

import org.h2.jdbcx.JdbcDataSource;
import org.h2.jdbcx.JdbcXAConnection;
import org.junit.Before;
import org.junit.Test;
import org.seasar.tramina.resource.ManagedConnection;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * 
 * 
 * @author koichik
 */
public class XADSManagedConnectionFactoryTest {

    JdbcDataSource ds;

    @Before
    public void before() throws Exception {
        ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:db1");
    }

    @Test
    public void test() throws Exception {
        XADSManagedConnectionFactory cf =
            new XADSManagedConnectionFactory().setXADataSource(ds);
        ManagedConnection<Connection, SQLException> mc =
            cf.createManagedConnection();
        assertThat(mc.getXAResource(), is(instanceOf(JdbcXAConnection.class)));
    }

    @Test
    public void testLastResource() throws Exception {
        XADSManagedConnectionFactory cf =
            new XADSManagedConnectionFactory()
                .setXADataSource(ds)
                .setLastResource(true);
        ManagedConnection<Connection, SQLException> mc =
            cf.createManagedConnection();
        assertThat(
            mc.getXAResource(),
            is(instanceOf(PseudoXAResourceImpl.class)));
    }

}
