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

import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.seasar.tramina.resource.jdbc.ManagedConnectionFactoryDialect;
import org.seasar.tramina.resource.jdbc.XAResourceFactory;

import static org.seasar.tramina.assertion.impl.BasicAssertions.*;

/**
 * 
 * 
 * @author koichik
 */
public class XAResourceFactoryImpl implements XAResourceFactory {

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final boolean lastResource;

    protected final boolean subtransactionAwareResource;

    protected final ManagedConnectionFactoryDialect dialect;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param lastResource
     * @param subtransactionAwareResource
     * @param dialect
     */
    protected XAResourceFactoryImpl(final boolean lastResource,
            final boolean subtransactionAwareResource,
            final ManagedConnectionFactoryDialect dialect) {
        assertParameterNotNull("dialect", dialect);
        this.lastResource = lastResource;
        this.subtransactionAwareResource = subtransactionAwareResource;
        this.dialect = dialect;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from XAResourceFactory
    //
    @Override
    public XAResource createXAResource(final PooledConnection pooledConnection,
            final Connection physicalConnection) throws SQLException {
        assertParameterNotNull("pooledConnection", pooledConnection);
        assertParameterNotNull("physicalConnection", physicalConnection);
        if (lastResource && subtransactionAwareResource) {
            return new SubtransactionAwareLastXAResourceImpl(physicalConnection);
        }
        if (subtransactionAwareResource) {
            return new SubtransactionAwareXAResourceImpl(physicalConnection);
        }
        if (lastResource) {
            return new LastXAResourceImpl(physicalConnection);
        }
        if (pooledConnection instanceof XAConnection) {
            return dialect.getXAResource((XAConnection) pooledConnection);
        }
        return new PseudoXAResourceImpl(physicalConnection);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public String toString() {
        return super.toString() + "[lastResource : <" + lastResource
            + ">, subtransactionAwareResource : <"
            + subtransactionAwareResource + ">, dialect : <" + dialect + ">";
    }

}
