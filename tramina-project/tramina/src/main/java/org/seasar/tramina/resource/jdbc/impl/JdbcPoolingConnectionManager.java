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

import org.seasar.tramina.resource.ManagedConnection;
import org.seasar.tramina.resource.impl.AbstractPoolingConnectionManager;
import org.seasar.tramina.resource.jdbc.JdbcManagedConnection;
import org.seasar.tramina.resource.jdbc.exception.TimeoutWaitingManagedConnectionException;

/**
 * 
 * 
 * @author koichik
 */
public class JdbcPoolingConnectionManager
        extends
        AbstractPoolingConnectionManager<JdbcPoolingConnectionManager, Connection, SQLException> {

    // /////////////////////////////////////////////////////////////////
    // instance methods from AbstractPoolingConnectionManager
    //
    @Override
    protected boolean validate(
            final ManagedConnection<Connection, SQLException> managedConnection)
            throws SQLException {
        return ((JdbcManagedConnection) managedConnection)
            .getPhysicalConnection()
            .isValid(validationTimeoutSeconds);
    }

    @Override
    protected SQLException newTimeoutException() {
        return new TimeoutWaitingManagedConnectionException();
    }

}
