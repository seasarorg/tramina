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

import javax.transaction.xa.XAResource;

import org.seasar.tramina.activity.Component;
import org.seasar.tramina.activity.Disposable;
import org.seasar.tramina.activity.Initializable;
import org.seasar.tramina.activity.impl.AbstractComponent;
import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.resource.ConnectionManager;
import org.seasar.tramina.resource.ManagedConnection;
import org.seasar.tramina.resource.ManagedConnectionEvent;
import org.seasar.tramina.resource.ManagedConnectionEventListener;
import org.seasar.tramina.resource.ManagedConnectionFactory;
import org.seasar.tramina.resource.ResourceManager;
import org.seasar.tramina.resource.exception.RecoveryIndoubtTransactionFailedException;
import org.seasar.tramina.spi.RecoveryManager;

import static org.seasar.tramina.activity.impl.ActivityAssertions.*;
import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.resource.ResourceMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public abstract class AbstractResourceManager<RM extends AbstractResourceManager<RM, C, E>, C, E extends Exception>
        extends AbstractComponent<RM> implements ResourceManager<RM, E>,
        Component, Initializable<RM>, Disposable<RM> {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(AbstractResourceManager.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected LastConnectionManager lastConnectionManager =
        new LastConnectionManager();

    protected ConnectionManager<C, E> firstConnectionManager =
        lastConnectionManager;

    protected ManagedConnectionFactory<C, E> managedConnectionFactory;

    protected RecoveryManager recoveryManager;

    // /////////////////////////////////////////////////////////////////
    // instance methods from ResourceManager
    //
    @Override
    public boolean isLastResource() {
        assertInitialized(this);
        return managedConnectionFactory == null ? false
            : managedConnectionFactory.isLastResource();
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from AbstractComponent
    //
    @Override
    protected void doInitialize() {
        assertPropertyNotNull(
            this,
            "managedConnectionFactory",
            managedConnectionFactory);
        if (recoveryManager != null) {
            try {
                final ManagedConnection<C, E> managedConnection =
                    firstConnectionManager.getManagedConnection();
                try {
                    final XAResource xaResource =
                        managedConnection.getXAResource();
                    recoveryManager.recover(xaResource);
                } finally {
                    firstConnectionManager
                        .logicalConnectionClosed(managedConnection);
                }
            } catch (final Exception e) {
                throw new RecoveryIndoubtTransactionFailedException(this, e);
            }
        }
    }

    @Override
    protected void doDispose() {
        try {
            firstConnectionManager.destroy();
        } catch (final Exception e) {
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public String toString() {
        return super.toString() + "[activityStatus : <" + activityStatus + ">]";
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for properties
    //
    /**
     * @param connectionManagers
     *            the connectionManagers to set
     */
    public RM addConnectionManager(
            final ConnectionManager<C, E> connectionManager) {
        assertBeforeInitialized(this);
        assertParameterNotNull("connectionManager", connectionManager);
        connectionManager.setResourceManager(this);
        lastConnectionManager
            .insertPreviousConnectionManager(connectionManager);
        @SuppressWarnings("unchecked")
        final RM self = (RM) this;
        return self;
    }

    /**
     * @param managedConnectionFactory
     *            the managedConnectionFactory to set
     */
    public RM setManagedConnectionFactory(
            final ManagedConnectionFactory<C, E> managedConnectionFactory) {
        assertBeforeInitialized(this);
        assertParameterNotNull(
            "managedConnectionFactory",
            managedConnectionFactory);
        this.managedConnectionFactory = managedConnectionFactory;
        @SuppressWarnings("unchecked")
        final RM self = (RM) this;
        return self;
    }

    /**
     * @param recoveryManager
     *            the recoveryManager to set
     */
    public RM setRecoveryManager(final RecoveryManager recoveryManager) {
        assertBeforeInitialized(this);
        assertParameterNotNull("recoveryManager", recoveryManager);
        this.recoveryManager = recoveryManager;
        @SuppressWarnings("unchecked")
        final RM self = (RM) this;
        return self;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for internal
    //
    protected C getLogicalConnection() throws E {
        assertInitialized(this);
        return firstConnectionManager
            .getManagedConnection()
            .getLogicalConnection();
    }

    // /////////////////////////////////////////////////////////////////
    // member types
    //
    protected class LastConnectionManager implements ConnectionManager<C, E> {

        // /////////////////////////////////////////////////////////////////
        // instance fields
        //
        protected ConnectionManager<C, E> previous;

        // /////////////////////////////////////////////////////////////////
        // instance methods
        //
        public void insertPreviousConnectionManager(
                final ConnectionManager<C, E> previous) {
            assertParameterNotNull("previous", previous);
            if (this.previous == null) {
                firstConnectionManager = previous;
            } else {
                this.previous.setNextConnectionManager(previous);
            }
            previous.setNextConnectionManager(this);
            this.previous = previous;
        }

        // /////////////////////////////////////////////////////////////////
        // instance methods from ConnectionManager
        //
        @Override
        public void setResourceManager(ResourceManager<?, E> resourceManager) {
        }

        @Override
        public void setNextConnectionManager(final ConnectionManager<C, E> next) {
        }

        @Override
        public ManagedConnection<C, E> getManagedConnection() throws E {
            final ManagedConnection<C, E> managedConnection =
                managedConnectionFactory.createManagedConnection();
            managedConnection
                .addManagedConnectionEventListener(new ManagedConnectionEventListenerImpl());
            if (logger.isDebugEnabled()) {
                logger.debug(PHYSICAL_CONNECTION_OPENED.format(
                    managedConnection,
                    AbstractResourceManager.this));
            }
            return managedConnection;
        }

        @Override
        public void logicalConnectionClosed(
                final ManagedConnection<C, E> managedConnection) throws E {
            try {
                managedConnection.cleanup();
                if (logger.isDebugEnabled()) {
                    logger.debug(PHYSICAL_CONNECTION_CLOSED.format(
                        managedConnection,
                        AbstractResourceManager.this));
                }
            } catch (final Exception e) {
                logger.error(CLEANUP_MANAGED_CONNECTION_FAILED.format(
                    managedConnection,
                    AbstractResourceManager.this), e);
            }
            try {
                managedConnection.destroy();
            } catch (final Exception e) {
                logger.error(DESTROY_MANAGED_CONNECTION_FAILED.format(
                    managedConnection,
                    AbstractResourceManager.this), e);
            }
        }

        @Override
        public void physicalConnectionErrorOccurred(
                final ManagedConnection<C, E> managedConnection, final E cause)
                throws E {
            try {
                managedConnection.destroy();
            } catch (final Exception e) {
                logger.error(DESTROY_MANAGED_CONNECTION_FAILED.format(
                    this,
                    managedConnection), e);
            }
        }

        @Override
        public void destroy() throws E {
            managedConnectionFactory.destroy();
        }

    }

    /**
     * 
     * 
     * @author koichik
     */
    protected class ManagedConnectionEventListenerImpl implements
            ManagedConnectionEventListener<C, E> {

        // /////////////////////////////////////////////////////////////////
        // instance methods from ManagedConnectionEventListener
        //
        @Override
        public void logicalConnectionClosed(
                final ManagedConnectionEvent<C, E> event) throws E {
            firstConnectionManager.logicalConnectionClosed(event.getSource());
        }

        @Override
        public void connectionErrorOccurred(
                final ManagedConnectionEvent<C, E> event) throws E {
            firstConnectionManager.physicalConnectionErrorOccurred(event
                .getSource(), event.getCause());
        }

    }

}
