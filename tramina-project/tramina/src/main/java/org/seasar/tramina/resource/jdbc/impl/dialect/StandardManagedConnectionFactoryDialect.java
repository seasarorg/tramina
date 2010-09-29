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
package org.seasar.tramina.resource.jdbc.impl.dialect;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.seasar.tramina.resource.jdbc.ManagedConnectionFactoryDialect;

import static org.seasar.tramina.assertion.impl.BasicAssertions.*;

/**
 * 
 * 
 * @author koichik
 */
public class StandardManagedConnectionFactoryDialect implements
        ManagedConnectionFactoryDialect {

    // /////////////////////////////////////////////////////////////////
    // instance methods from ManagedConnectionFactoryDialect
    //
    @Override
    public XAResource getXAResource(final XAConnection xaConnection)
            throws SQLException {
        assertParameterNotNull("xaConnection", xaConnection);
        final XAResource xaResource = xaConnection.getXAResource();
        final Class<?> clazz = xaResource.getClass();
        if (clazz.getSimpleName().equals("OracleXAResource")) {
            return new OracleXAResourceWrapper(xaResource);
        }
        return xaResource;
    }

}
