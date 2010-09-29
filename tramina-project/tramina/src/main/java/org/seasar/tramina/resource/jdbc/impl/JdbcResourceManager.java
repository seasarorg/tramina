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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.seasar.tramina.resource.ResourceManager;
import org.seasar.tramina.resource.impl.AbstractResourceManager;
import org.seasar.tramina.resource.jdbc.exception.InterfaceNotImplementedException;
import org.seasar.tramina.resource.jdbc.exception.TimeoutWaitingManagedConnectionException;
import org.seasar.tramina.resource.jdbc.exception.UnsupportedOperationSQLException;

/**
 * 
 * 
 * @author koichik
 */
public class JdbcResourceManager extends
        AbstractResourceManager<JdbcResourceManager, Connection, SQLException>
        implements DataSource {

    // /////////////////////////////////////////////////////////////////
    // instance methods from DataSource
    //
    @Override
    public Connection getConnection() throws SQLException {
        return getLogicalConnection();
    }

    @Override
    public Connection getConnection(final String username, final String password)
            throws SQLException {
        throw new TimeoutWaitingManagedConnectionException();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationSQLException("getLogWriter()");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationSQLException("getLoginTimeout()");
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        throw new UnsupportedOperationSQLException("setLogWriter(PrintWriter");
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        throw new UnsupportedOperationSQLException("setLoginTimeout(int)");
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface == ResourceManager.class;
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (iface == ResourceManager.class) {
            @SuppressWarnings("unchecked")
            final T result = (T) this;
            return result;
        }
        throw new InterfaceNotImplementedException(this, iface);
    }

}
