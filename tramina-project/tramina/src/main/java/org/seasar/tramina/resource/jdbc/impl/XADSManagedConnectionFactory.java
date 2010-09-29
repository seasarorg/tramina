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

import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.seasar.tramina.activity.impl.AbstractComponent;
import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.resource.ManagedConnection;
import org.seasar.tramina.resource.ManagedConnectionFactory;
import org.seasar.tramina.resource.impl.AbstractConnectionManager;
import org.seasar.tramina.resource.jdbc.ManagedConnectionFactoryDialect;
import org.seasar.tramina.resource.jdbc.impl.dialect.StandardManagedConnectionFactoryDialect;

import static org.seasar.tramina.activity.impl.ActivityAssertions.*;
import static org.seasar.tramina.assertion.impl.BasicAssertions.*;

/**
 * 
 * 
 * @author koichik
 */
public class XADSManagedConnectionFactory extends
        AbstractComponent<XADSManagedConnectionFactory> implements
        ManagedConnectionFactory<Connection, SQLException> {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(AbstractConnectionManager.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected XADataSource xaDataSource;

    protected boolean lastResource;

    protected boolean subtransactionAwareResource;

    protected ManagedConnectionFactoryDialect dialect =
        new StandardManagedConnectionFactoryDialect();

    // /////////////////////////////////////////////////////////////////
    // instance methods from ManagedConnectionFactory
    //
    @Override
    public boolean isLastResource() {
        return lastResource;
    }

    @Override
    public boolean isSubtransactionAwareResource() {
        return subtransactionAwareResource;
    }

    @Override
    public ManagedConnection<Connection, SQLException> createManagedConnection()
            throws SQLException {
        final XAConnection xaConnection = xaDataSource.getXAConnection();
        return new PooledJdbcManagedConnection(new XAResourceFactoryImpl(
            lastResource,
            subtransactionAwareResource,
            dialect), xaConnection);
    }

    @Override
    public void destroy() throws SQLException {
        dispose();
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from AbstractComponent
    //
    @Override
    protected void doInitialize() {
        assertPropertyNotNull(this, "xaDataSource", xaDataSource);
    }

    @Override
    protected void doDispose() {
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public String toString() {
        return super.toString() + "[xaDataSource : <" + xaDataSource
            + ">, lastResource : <" + lastResource
            + ">, subtransactionAwareResource : <"
            + subtransactionAwareResource + ">, dialect : <" + dialect + ">]";
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for properties
    //
    /**
     * @param xaDataSource
     *            the xaDataSource to set
     */
    public XADSManagedConnectionFactory setXADataSource(
            final XADataSource xaDataSource) {
        assertBeforeInitialized(this);
        assertParameterNotNull("xaDataSource", xaDataSource);
        this.xaDataSource = xaDataSource;
        return this;
    }

    /**
     * @param lastResource
     *            the lastResource to set
     */
    public XADSManagedConnectionFactory setLastResource(
            final boolean lastResource) {
        assertBeforeInitialized(this);
        this.lastResource = lastResource;
        return this;
    }

    /**
     * @param subtransactionAwareResource
     *            the subtransactionAware to set
     */
    public XADSManagedConnectionFactory setSubtransactionAwareResource(
            final boolean subtransactionAwareResource) {
        assertBeforeInitialized(this);
        this.subtransactionAwareResource = subtransactionAwareResource;
        return this;
    }

    /**
     * @param dialect
     *            the dialect to set
     */
    public XADSManagedConnectionFactory setDialect(
            final ManagedConnectionFactoryDialect dialect) {
        assertBeforeInitialized(this);
        assertParameterNotNull("dialect", dialect);
        this.dialect = dialect;
        return this;
    }

}
