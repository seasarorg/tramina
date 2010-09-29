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

import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seasar.tramina.resource.jdbc.LogicalConnection;
import org.seasar.tramina.resource.jdbc.impl.dialect.StandardManagedConnectionFactoryDialect;
import org.seasar.tramina.unit.EasyMock;
import org.seasar.tramina.unit.EasyMockRunner;
import org.seasar.tramina.unit.MockType;
import org.seasar.tramina.unit.Record;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * 
 * 
 * @author koichik
 */
@RunWith(EasyMockRunner.class)
public class PooledJdbcManagedConnectionTest {

    @EasyMock(MockType.STRICT)
    XAConnection xaConnection;

    @EasyMock(MockType.STRICT)
    Connection connection;

    @EasyMock(MockType.STRICT)
    XAResource xaResource;

    @Record
    public void record() throws Exception {
        xaConnection
            .addConnectionEventListener(isA(ConnectionEventListener.class));
        // mc.getLogicalConnection()
        expect(xaConnection.getConnection()).andReturn(connection);
        expect(xaConnection.getXAResource()).andReturn(xaResource);
        expect(connection.isClosed()).andReturn(false);
        // mc.cleanup()
        expect(connection.isClosed()).andReturn(false);
        connection.setAutoCommit(true);
        connection.close();
        // mc.destroy()
        xaConnection
            .removeConnectionEventListener(isA(ConnectionEventListener.class));
        xaConnection.close();
    }

    @Test
    public void test() throws Exception {
        PooledJdbcManagedConnection mc =
            new PooledJdbcManagedConnection(new XAResourceFactoryImpl(
                false,
                false,
                new StandardManagedConnectionFactoryDialect()), xaConnection);
        assertThat(
            mc.pooledConnection,
            is(sameInstance((PooledConnection) xaConnection)));
        assertThat(mc.physicalConnection, is(nullValue()));
        assertThat(mc.logicalConnection, is(nullValue()));

        assertThat(mc.getXAResource(), is(sameInstance(xaResource)));

        Connection con = mc.getLogicalConnection();
        assertThat(con, is(not(nullValue())));
        assertThat(con, is(not(sameInstance(connection))));
        assertThat(con.isWrapperFor(LogicalConnection.class), is(true));
        assertThat(con.unwrap(LogicalConnection.class), is(sameInstance(con)));

        con.close();
        assertThat(con.isClosed(), is(false));

        mc.cleanup();
        assertThat(mc.logicalConnection, is(nullValue()));
        assertThat(mc.physicalConnection, is(nullValue()));
        assertThat(mc.xaResource, is(nullValue()));

        mc.destroy();
        assertThat(mc.pooledConnection, is(nullValue()));
    }

}
