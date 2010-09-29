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
import java.util.Map;
import java.util.Properties;

import org.seasar.tramina.resource.ManagedConnection;
import org.seasar.tramina.resource.impl.AbstractConnectionManager;
import org.seasar.tramina.resource.jdbc.HoldabilityType;
import org.seasar.tramina.resource.jdbc.TransactionIsolationType;

import static org.seasar.tramina.activity.impl.ActivityAssertions.*;
import static org.seasar.tramina.assertion.impl.BasicAssertions.*;

/**
 * 
 * 
 * @author koichik
 */
public class JdbcSettingsConnectionManager
        extends
        AbstractConnectionManager<JdbcSettingsConnectionManager, Connection, SQLException> {

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected Boolean autoCommit;

    protected String catalog;

    protected Properties clientInfo;

    protected HoldabilityType holdability;

    protected Boolean readOnly;

    protected TransactionIsolationType transactionIsolation;

    protected Map<String, Class<?>> typeMap;

    // /////////////////////////////////////////////////////////////////
    // instance methods from AbstractComponent
    //
    @Override
    public ManagedConnection<Connection, SQLException> getManagedConnection()
            throws SQLException {
        assertInitialized(this);
        final ManagedConnection<Connection, SQLException> managedConnection =
            super.getManagedConnection();
        final Connection logicalConnection =
            managedConnection.getLogicalConnection();
        if (autoCommit != null) {
            logicalConnection.setAutoCommit(autoCommit.booleanValue());
        }
        if (catalog != null) {
            logicalConnection.setCatalog(catalog);
        }
        if (clientInfo != null) {
            logicalConnection.setClientInfo(clientInfo);
        }
        if (holdability != null) {
            logicalConnection.setHoldability(holdability.intValue());
        }
        if (readOnly != null) {
            logicalConnection.setReadOnly(readOnly.booleanValue());
        }
        if (transactionIsolation != null) {
            logicalConnection.setTransactionIsolation(transactionIsolation
                .intValue());
        }
        if (typeMap != null) {
            logicalConnection.setTypeMap(typeMap);
        }
        return managedConnection;
    }

    @Override
    protected void doInitialize() {
    }

    @Override
    protected void doDispose() {
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for properties
    //
    /**
     * @param autoCommit
     *            the autoCommit to set
     */
    public JdbcSettingsConnectionManager setAutoCommit(final boolean autoCommit) {
        assertBeforeInitialized(this);
        this.autoCommit = autoCommit;
        return this;
    }

    /**
     * @param catalog
     *            the catalog to set
     */
    public JdbcSettingsConnectionManager setCatalog(final String catalog) {
        assertBeforeInitialized(this);
        assertParameterNotEmptyString("catalog", catalog);
        this.catalog = catalog;
        return this;
    }

    /**
     * @param clientInfo
     *            the clientInfo to set
     */
    public JdbcSettingsConnectionManager setClientInfo(
            final Properties clientInfo) {
        assertBeforeInitialized(this);
        assertParameterNotEmptyMap("clientInfo", clientInfo);
        this.clientInfo = clientInfo;
        return this;
    }

    /**
     * @param holdability
     *            the holdability to set
     */
    public JdbcSettingsConnectionManager setHoldability(
            final HoldabilityType holdability) {
        assertBeforeInitialized(this);
        assertParameterNotNull("holdability", holdability);
        this.holdability = holdability;
        return this;
    }

    /**
     * @param readOnly
     *            the readOnly to set
     */
    public JdbcSettingsConnectionManager setReadOnly(final boolean readOnly) {
        assertBeforeInitialized(this);
        this.readOnly = readOnly;
        return this;
    }

    /**
     * @param transactionIsolation
     *            the transactionIsolation to set
     */
    public JdbcSettingsConnectionManager setTransactionIsolation(
            final TransactionIsolationType transactionIsolation) {
        assertBeforeInitialized(this);
        assertParameterNotNull("transactionIsolation", transactionIsolation);
        this.transactionIsolation = transactionIsolation;
        return this;
    }

    /**
     * @param typeMap
     *            the typeMap to set
     */
    public JdbcSettingsConnectionManager setTypeMap(
            final Map<String, Class<?>> typeMap) {
        assertBeforeInitialized(this);
        assertParameterNotEmptyMap("typeMap", typeMap);
        this.typeMap = typeMap;
        return this;
    }

}
