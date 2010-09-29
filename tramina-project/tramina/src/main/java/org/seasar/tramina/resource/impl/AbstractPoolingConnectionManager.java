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

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.resource.ManagedConnection;
import org.seasar.tramina.resource.exception.InterruptedRuntimeException;
import org.seasar.tramina.resource.exception.ScheduleExpireTaskFailedException;
import org.seasar.tramina.spi.WorkManager;

import static org.seasar.tramina.activity.impl.ActivityAssertions.*;
import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.resource.ResourceMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public abstract class AbstractPoolingConnectionManager<CM extends AbstractPoolingConnectionManager<CM, C, E>, C, E extends Exception>
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
    protected final Deque<AllocationRequest> allocationRequests =
        new LinkedList<AllocationRequest>();

    protected final Deque<IdleConnection> idleConnections =
        new LinkedList<IdleConnection>();

    protected final Set<ManagedConnection<C, E>> activeConnections =
        new HashSet<ManagedConnection<C, E>>();

    protected int awaitingConnections;

    protected WorkManager workManager;

    protected int maxActiveConnections = 10;

    protected int maxIdleConnections = 10;

    protected int minIdleConnections = 0;

    protected long maxIdleSeconds = 600;

    protected long maxWaitMillis = -1;

    protected boolean testOnBorrow = true;

    protected int minTestOnBorrowIdleSeconds = 60;

    protected boolean testOnReturn = false;

    protected int validationTimeoutSeconds;

    // /////////////////////////////////////////////////////////////////
    // instance methods from ConnectionManager
    //
    @Override
    public ManagedConnection<C, E> getManagedConnection() throws E {
        assertInitialized(this);
        ManagedConnection<C, E> managedConnection =
            getManagedConnectionFromPool();
        if (managedConnection != null) {
            return managedConnection;
        }
        try {
            managedConnection = super.getManagedConnection();
            return managedConnection;
        } finally {
            synchronized (this) {
                --awaitingConnections;
                if (managedConnection != null) {
                    activeConnections.add(managedConnection);
                } else {
                    allocate();
                }
            }
        }
    }

    @Override
    public void logicalConnectionClosed(
            final ManagedConnection<C, E> managedConnection) throws E {
        assertInitialized(this);
        synchronized (this) {
            if (activeConnections.remove(managedConnection)) {
                if (idleConnections.size() < maxIdleConnections) {
                    try {
                        managedConnection.cleanup();
                        if (testOnReturn) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(VALIDATING_MANAGED_CONNECTION
                                    .format(managedConnection, this));
                            }
                            if (!validate(managedConnection)) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(INVALID_MANAGED_CONNECTION
                                        .format(managedConnection, this));
                                }
                            }
                        }
                        if (!allocationRequests.isEmpty()) {
                            allocate(managedConnection);
                            return;
                        }
                        if (idleConnections.size() < maxIdleConnections) {
                            idle(managedConnection);
                            return;
                        }
                    } catch (final Exception e) {
                        logger.error(CLOSE_LOGICAL_CONNECTION_FAILED
                            .format(managedConnection), e);
                    }
                }
            }
        }
        super.logicalConnectionClosed(managedConnection);
    }

    @Override
    public void physicalConnectionErrorOccurred(
            final ManagedConnection<C, E> managedConnection, final E cause)
            throws E {
        assertInitialized(this);
        synchronized (this) {
            for (final Iterator<IdleConnection> it = idleConnections.iterator(); it
                .hasNext();) {
                final IdleConnection idleConnection = it.next();
                if (idleConnection.getManagedConnection() == managedConnection) {
                    it.remove();
                    break;
                }
            }
            if (activeConnections.remove(managedConnection)) {
                allocate();
            }
        }
        super.physicalConnectionErrorOccurred(managedConnection, cause);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from AbstractComponent
    //
    @Override
    protected void doInitialize() {
        assertPropertyNotNull(this, "workManager", workManager);
    }

    @Override
    protected void doDispose() {
        synchronized (this) {
            for (final IdleConnection idleConnection : idleConnections) {
                idleConnection.cancel();
                final ManagedConnection<C, E> managedConnection =
                    idleConnection.getManagedConnection();
                try {
                    super.logicalConnectionClosed(managedConnection);
                } catch (final Exception e) {
                    logger.error(CLOSE_LOGICAL_CONNECTION_FAILED
                        .format(managedConnection), e);
                }
            }
            idleConnections.clear();
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from AbstractComponent
    //
    @Override
    public String toString() {
        synchronized (this) {
            return super.toString() + "[idleConnections : <"
                + idleConnections.size() + ">, activeConnections : <"
                + activeConnections.size() + ">, activatingConnections : <"
                + awaitingConnections + ">, maxActiveConnections : <"
                + maxActiveConnections + ">, maxIdleConnections : <"
                + maxIdleConnections + ">, minIdleConnections : <"
                + minIdleConnections + ">, maxIdleSeconds : <" + maxIdleSeconds
                + ">, maxWaitMillis : <" + maxWaitMillis
                + ">, testOnBorrow : <" + testOnBorrow
                + ">, minTestOnBorrowIdleSeconds : <"
                + minTestOnBorrowIdleSeconds + ">, testOnReturn : <"
                + testOnReturn + ">, validationTimeoutSeconds : <"
                + validationTimeoutSeconds + ">, resourceManager : <"
                + resourceManager + ">]";
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for properties
    //
    /**
     * @param workManager
     *            the workManager to set
     */
    public CM setWorkManager(final WorkManager workManager) {
        assertBeforeInitialized(this);
        assertParameterNotNull("workManager", workManager);
        this.workManager = workManager;
        @SuppressWarnings("unchecked")
        final CM self = (CM) this;
        return self;
    }

    /**
     * @param maxActiveConnections
     *            the maxPoolSize to set
     */
    public CM setMaxActiveConnections(final int maxActiveConnections) {
        assertBeforeInitialized(this);
        this.maxActiveConnections = maxActiveConnections;
        @SuppressWarnings("unchecked")
        final CM self = (CM) this;
        return self;
    }

    /**
     * @param maxIdleConnections
     *            the maxPoolSize to set
     */
    public CM setMaxIdleConnections(final int maxIdleConnections) {
        assertBeforeInitialized(this);
        this.maxIdleConnections = maxIdleConnections;
        @SuppressWarnings("unchecked")
        final CM self = (CM) this;
        return self;
    }

    /**
     * @param minIdleConnections
     *            the maxPoolSize to set
     */
    public CM setMinIdleConnections(final int minIdleConnections) {
        assertBeforeInitialized(this);
        this.minIdleConnections = minIdleConnections;
        @SuppressWarnings("unchecked")
        final CM self = (CM) this;
        return self;
    }

    /**
     * @param maxIdleSeconds
     *            the timeoutSeconds to set
     */
    public CM setMaxIdleSeconds(final long maxIdleSeconds) {
        assertBeforeInitialized(this);
        this.maxIdleSeconds = maxIdleSeconds;
        @SuppressWarnings("unchecked")
        final CM self = (CM) this;
        return self;
    }

    /**
     * @param maxWaitMillis
     *            the timeoutSeconds to set
     */
    public CM setMaxWaitMillis(final long maxWaitMillis) {
        assertBeforeInitialized(this);
        this.maxWaitMillis = maxWaitMillis;
        @SuppressWarnings("unchecked")
        final CM self = (CM) this;
        return self;
    }

    /**
     * @param minTestOnBorrowIdleSeconds
     *            the minTestOnBorrowIdleSeconds to set
     */
    public CM setMinTestOnBorrowIdleSeconds(final int minTestOnBorrowIdleSeconds) {
        assertBeforeInitialized(this);
        this.minTestOnBorrowIdleSeconds = minTestOnBorrowIdleSeconds;
        @SuppressWarnings("unchecked")
        final CM self = (CM) this;
        return self;
    }

    /**
     * @param testOnBorrow
     *            the testOnBorrow to set
     */
    public CM setTestOnBorrow(final boolean testOnBorrow) {
        assertBeforeInitialized(this);
        this.testOnBorrow = testOnBorrow;
        @SuppressWarnings("unchecked")
        final CM self = (CM) this;
        return self;
    }

    /**
     * @param testOnReturn
     *            the testOnReturn to set
     */
    public CM setTestOnReturn(final boolean testOnReturn) {
        assertBeforeInitialized(this);
        this.testOnReturn = testOnReturn;
        @SuppressWarnings("unchecked")
        final CM self = (CM) this;
        return self;
    }

    /**
     * @param validationTimeoutSeconds
     *            the validationTimeoutSeconds to set
     */
    public CM setValidationTimeoutSeconds(final int validationTimeoutSeconds) {
        assertBeforeInitialized(this);
        this.validationTimeoutSeconds = validationTimeoutSeconds;
        @SuppressWarnings("unchecked")
        final CM self = (CM) this;
        return self;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for subclasses
    //
    protected abstract boolean validate(
            ManagedConnection<C, E> managedConnection) throws E;

    protected abstract E newTimeoutException();

    // /////////////////////////////////////////////////////////////////
    // instance methods for internal
    //
    protected ManagedConnection<C, E> getManagedConnectionFromPool() throws E {
        for (;;) {
            final AllocationRequest allocationRequest = new AllocationRequest();
            synchronized (this) {
                allocationRequests.addLast(allocationRequest);
                allocate();
            }

            if (allocationRequest.await()) {
                final ManagedConnection<C, E> managedConnection =
                    allocationRequest.getManagedConnection();
                if (logger.isDebugEnabled()) {
                    logger.debug(OBTAIN_MANAGED_CONNECTION_FROM_POOL.format(
                        managedConnection,
                        this));
                }
                if (testOnBorrow
                    && allocationRequest.getIdleTimeMillis() > minTestOnBorrowIdleSeconds * 1000) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(VALIDATING_MANAGED_CONNECTION.format(
                            managedConnection,
                            this));
                    }
                    if (!validate(managedConnection)) {
                        logger.debug(INVALID_MANAGED_CONNECTION.format(
                            managedConnection,
                            this));
                        super.logicalConnectionClosed(managedConnection);
                        continue;
                    }
                }
                return managedConnection;
            }
            return null;
        }
    }

    protected synchronized void allocate() {
        while (!idleConnections.isEmpty() && !allocationRequests.isEmpty()) {
            final IdleConnection idleConnection = idleConnections.removeLast();
            idleConnection.cancel();
            final AllocationRequest allocationRequest =
                allocationRequests.removeFirst();
            final ManagedConnection<C, E> managedConnection =
                idleConnection.getManagedConnection();
            activeConnections.add(managedConnection);
            allocationRequest.setIdleTimeMillis(System.currentTimeMillis()
                - idleConnection.getIdledTime());
            allocationRequest.notify(managedConnection);
        }

        while (activeConnections.size() + awaitingConnections < maxActiveConnections
            && !allocationRequests.isEmpty()) {
            final AllocationRequest allocationRequest =
                allocationRequests.removeFirst();
            allocationRequest.notify(null);
            ++awaitingConnections;
        }
    }

    /**
     * @param managedConnection
     */
    protected synchronized void allocate(
            final ManagedConnection<C, E> managedConnection) {
        if (logger.isDebugEnabled()) {
            logger.debug(RETURN_MANAGED_CONNECTION_TO_POOL.format(
                managedConnection,
                this));
        }
        activeConnections.add(managedConnection);
        final AllocationRequest allocationRequest =
            allocationRequests.removeFirst();
        allocationRequest.notify(managedConnection);
    }

    /**
     * @param managedConnection
     */
    protected synchronized void idle(
            final ManagedConnection<C, E> managedConnection) {
        final IdleConnection idleConnection =
            new IdleConnection(managedConnection);
        if (maxIdleSeconds > 0) {
            idleConnection
                .setScheduledFuture(scheduleExpireTask(managedConnection));
        }
        idleConnections.addLast(idleConnection);
        if (logger.isDebugEnabled()) {
            logger.debug(RETURN_MANAGED_CONNECTION_TO_POOL.format(
                managedConnection,
                this));
        }
    }

    protected ScheduledFuture<Void> scheduleExpireTask(
            final ManagedConnection<C, E> managedConnection) {
        try {
            final ExpireTask expireTask = new ExpireTask();
            return workManager.schedule(expireTask, maxIdleSeconds);
        } catch (final RejectedExecutionException e) {
            throw new ScheduleExpireTaskFailedException(managedConnection, e);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // member types
    //
    protected class AllocationRequest {

        // /////////////////////////////////////////////////////////////////
        // instance fields
        //
        protected final CountDownLatch latch = new CountDownLatch(1);

        protected ManagedConnection<C, E> managedConnection;

        protected long idleTimeMillis;

        // /////////////////////////////////////////////////////////////////
        // instance methods
        //
        public boolean await() throws E {
            if (maxWaitMillis == 0L) {
                throw newTimeoutException();
            }
            try {
                if (latch.await(maxWaitMillis, TimeUnit.MILLISECONDS)) {
                    return managedConnection != null;
                }
                throw newTimeoutException();
            } catch (final InterruptedException e) {
                throw new InterruptedRuntimeException(e);
            }
        }

        public void notify(final ManagedConnection<C, E> managedConnection) {
            this.managedConnection = managedConnection;
            latch.countDown();
        }

        /**
         * @return the managedConnection
         */
        public ManagedConnection<C, E> getManagedConnection() {
            return managedConnection;
        }

        /**
         * @return the idleTimeMillis
         */
        public long getIdleTimeMillis() {
            return idleTimeMillis;
        }

        /**
         * @param idleTimeMillis
         *            the idleTimeMillis to set
         */
        public void setIdleTimeMillis(final long idleTimeMillis) {
            this.idleTimeMillis = idleTimeMillis;
        }

    }

    protected class IdleConnection {

        // /////////////////////////////////////////////////////////////////
        // instance fields
        //
        protected final ManagedConnection<C, E> managedConnection;

        protected final long idledTime = System.currentTimeMillis();

        protected ScheduledFuture<Void> scheduledFuture;

        // /////////////////////////////////////////////////////////////////
        // constructors
        //
        /**
         * @param managedConnection
         */
        public IdleConnection(final ManagedConnection<C, E> managedConnection) {
            this.managedConnection = managedConnection;
        }

        // /////////////////////////////////////////////////////////////////
        // instance methods
        //
        /**
         * @param scheduledFuture
         *            the scheduledFuture to set
         */
        public void setScheduledFuture(
                final ScheduledFuture<Void> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        public boolean cancel() {
            if (scheduledFuture != null) {
                return scheduledFuture.cancel(false);
            }
            return true;
        }

        public boolean isExpired() {
            return idledTime + maxIdleSeconds * 1000 < System
                .currentTimeMillis();
        }

        /**
         * @return the managedConnection
         */
        public ManagedConnection<C, E> getManagedConnection() {
            return managedConnection;
        }

        /**
         * @return the idledTime
         */
        public long getIdledTime() {
            return idledTime;
        }

    }

    protected class ExpireTask implements Callable<Void> {

        // /////////////////////////////////////////////////////////////////
        // instance methods from Callable
        //
        @Override
        public Void call() throws Exception {
            final List<ManagedConnection<C, E>> expiredConnections =
                new ArrayList<ManagedConnection<C, E>>(maxIdleConnections);
            synchronized (AbstractPoolingConnectionManager.this) {
                while (idleConnections.size() > minIdleConnections) {
                    final IdleConnection idleConnection =
                        idleConnections.getFirst();
                    if (idleConnection.isExpired()) {
                        expiredConnections.add(idleConnections
                            .removeFirst()
                            .getManagedConnection());
                    } else {
                        break;
                    }
                }
            }
            for (final ManagedConnection<C, E> managedConnection : expiredConnections) {
                AbstractPoolingConnectionManager.super
                    .logicalConnectionClosed(managedConnection);
            }
            return null;
        }
    }

}
