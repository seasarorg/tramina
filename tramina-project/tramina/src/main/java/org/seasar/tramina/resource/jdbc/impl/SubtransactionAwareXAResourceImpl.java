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
import java.sql.Savepoint;
import java.util.Deque;
import java.util.LinkedList;

import javax.transaction.xa.XAException;

import org.seasar.tramina.resource.jdbc.exception.RollbackSavePointFailedException;
import org.seasar.tramina.resource.jdbc.exception.SetSavePointFailedException;
import org.seasar.tramina.resource.jdbc.exception.SubtransactionNotStartedException;
import org.seasar.tramina.spi.SubtransactionAwareXAResource;

/**
 * 
 * 
 * @author koichik
 */
public class SubtransactionAwareXAResourceImpl extends PseudoXAResourceImpl
        implements SubtransactionAwareXAResource {

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final Deque<Savepoint> savepoints = new LinkedList<Savepoint>();

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param connection
     */
    public SubtransactionAwareXAResourceImpl(final Connection connection) {
        super(connection);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from SubtransactionAwareXAResource
    //
    @Override
    public void beginSubtransaction() throws XAException {
        assertStarted();
        try {
            savepoints.add(connection.setSavepoint());
        } catch (final SQLException e) {
            throw new SetSavePointFailedException(this, e);
        }
    }

    @Override
    public void commitSubtransaction() throws XAException {
        assertStarted();
        assertSubtransactionStarted();
        savepoints.removeLast();
    }

    @Override
    public void rollbackSubtransaction() throws XAException {
        assertStarted();
        assertSubtransactionStarted();
        try {
            final Savepoint savepoint = savepoints.removeLast();
            if (savepoint != null) {
                connection.rollback(savepoint);
            }
        } catch (final SQLException e) {
            throw new RollbackSavePointFailedException(this, e);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for internal
    //
    protected void assertSubtransactionStarted() throws XAException {
        if (!savepoints.isEmpty()) {
            return;
        }
        throw new SubtransactionNotStartedException(this);
    }

}
