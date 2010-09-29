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
package org.seasar.tramina.resource.impl;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.resource.ManagedConnection;
import org.seasar.tramina.spi.ToplevelTransaction;
import org.seasar.tramina.spi.TraminaTransaction;
import org.seasar.tramina.spi.TraminaTransactionManager;

import static org.seasar.tramina.activity.impl.ActivityAssertions.*;
import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.resource.ResourceMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public abstract class AbstractTxBoundConnectionManager<CM extends AbstractTxBoundConnectionManager<CM, C, E>, C, E extends Exception>
        extends AbstractConnectionManager<CM, C, E> {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(AbstractConnectionManager.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected ConcurrentMap<Transaction, ManagedConnection<C, E>> managedConnections =
        new ConcurrentHashMap<Transaction, ManagedConnection<C, E>>();

    protected TraminaTransactionManager transactionManager;

    // /////////////////////////////////////////////////////////////////
    // instance methods from ConnectionManager
    //
    @Override
    public ManagedConnection<C, E> getManagedConnection() throws E {
        assertInitialized(this);
        final TraminaTransaction currentTransaction = getTransaction();
        if (currentTransaction == null) {
            return super.getManagedConnection();
        }
        final ToplevelTransaction toplevelTransaction =
            currentTransaction.getToplevelTransaction();
        if (managedConnections.containsKey(toplevelTransaction)) {
            return managedConnections.get(toplevelTransaction);
        }
        final ManagedConnection<C, E> managedConnection =
            super.getManagedConnection();
        enlistResource(managedConnection, currentTransaction);
        registerSynchronization(
            managedConnection,
            currentTransaction,
            toplevelTransaction);
        managedConnections.put(toplevelTransaction, managedConnection);
        if (logger.isDebugEnabled()) {
            logger.debug(MANAGED_CONNECTION_BOUND_TRANSACTION.format(
                managedConnection,
                currentTransaction));
        }
        return managedConnection;
    }

    @Override
    public void logicalConnectionClosed(
            final ManagedConnection<C, E> managedConnection) throws E {
        assertInitialized(this);
        if (!managedConnections.containsValue(managedConnection)) {
            super.logicalConnectionClosed(managedConnection);
        }
    }

    @Override
    public void physicalConnectionErrorOccurred(
            final ManagedConnection<C, E> managedConnection, final E cause)
            throws E {
        assertInitialized(this);
        for (final Entry<Transaction, ManagedConnection<C, E>> entry : managedConnections
            .entrySet()) {
            if (entry.getValue() == managedConnection) {
                final Transaction transaction = entry.getKey();
                managedConnections.remove(transaction);
                if (logger.isDebugEnabled()) {
                    logger.debug(MANAGED_CONNECTION_UNBOUND_TRANSACTION.format(
                        managedConnection,
                        transaction));
                }
                break;
            }
        }
        super.physicalConnectionErrorOccurred(managedConnection, cause);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from AbstractComponent
    //
    @Override
    protected void doInitialize() {
        assertPropertyNotNull(this, "transactionManager", transactionManager);
    }

    @Override
    protected void doDispose() {
        for (final Entry<Transaction, ManagedConnection<C, E>> entry : managedConnections
            .entrySet()) {
            final ManagedConnection<C, E> managedConnection = entry.getValue();
            try {
                super.logicalConnectionClosed(managedConnection);
            } catch (final Exception e) {
                logger.error(CLOSE_LOGICAL_CONNECTION_FAILED
                    .format(managedConnection), e);
            }
        }
        managedConnections.clear();
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public String toString() {
        return super.toString() + "[transactionManager : <"
            + transactionManager + ">, resourceManager : <" + resourceManager
            + ">]";
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for properties
    //
    /**
     * @param transactionManager
     *            the transactionManager to set
     */
    public CM setTransactionManager(
            final TraminaTransactionManager transactionManager) {
        assertBeforeInitialized(this);
        assertParameterNotNull("transactionManager", transactionManager);
        this.transactionManager = transactionManager;
        @SuppressWarnings("unchecked")
        final CM self = (CM) this;
        return self;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for subclasses
    //
    protected abstract E newTransactionUnavailableException(Exception cause);

    protected abstract E newEnlistResourceFailedException(
            ManagedConnection<C, E> managedConnection,
            TraminaTransaction transaction, Exception cause);

    protected abstract E newRegisterSynchronizationFailedException(
            ManagedConnection<C, E> managedConnection,
            TraminaTransaction transaction, Exception cause);

    // /////////////////////////////////////////////////////////////////
    // instance methods for internal
    //
    protected TraminaTransaction getTransaction() throws E {
        try {
            return transactionManager.getTransaction();
        } catch (final SystemException e) {
            throw newTransactionUnavailableException(e);
        }
    }

    protected void enlistResource(
            final ManagedConnection<C, E> managedConnection,
            final TraminaTransaction transaction) throws E {
        try {
            transaction.enlistResource(managedConnection.getXAResource());
        } catch (final Exception e) {
            throw newEnlistResourceFailedException(
                managedConnection,
                transaction,
                e);
        }
    }

    protected void registerSynchronization(
            final ManagedConnection<C, E> managedConnection,
            final TraminaTransaction currentTransaction,
            final ToplevelTransaction toplevelTransaction) throws E {
        try {
            currentTransaction.registerSynchronization(new SynchronizationImpl(
                toplevelTransaction));
        } catch (final Exception e) {
            throw newRegisterSynchronizationFailedException(
                managedConnection,
                currentTransaction,
                e);
        }
    }

    protected void unbound(final ToplevelTransaction toplevelTransaction) {
        final ManagedConnection<C, E> managedConnection =
            managedConnections.remove(toplevelTransaction);
        if (managedConnection != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(MANAGED_CONNECTION_UNBOUND_TRANSACTION.format(
                    managedConnection,
                    toplevelTransaction));
            }
            try {
                super.logicalConnectionClosed(managedConnection);
            } catch (final Exception e) {
                logger.error(CLOSE_LOGICAL_CONNECTION_FAILED
                    .format(managedConnection), e);
            }
        }

    }

    // /////////////////////////////////////////////////////////////////
    // member types
    //
    /**
     * 
     * 
     * @author koichik
     */
    protected class SynchronizationImpl implements Synchronization {

        protected final ToplevelTransaction toplevelTransaction;

        /**
         * @param toplevelTransaction
         */
        private SynchronizationImpl(
                final ToplevelTransaction toplevelTransaction) {
            this.toplevelTransaction = toplevelTransaction;
        }

        @Override
        public void beforeCompletion() {
        }

        @Override
        public void afterCompletion(final int status) {
            unbound(toplevelTransaction);
        }
    }

}
