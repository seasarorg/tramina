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
package org.seasar.tramina.transaction.impl;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.Xid;

import org.seasar.tramina.activity.exception.AlreadyInitializedException;
import org.seasar.tramina.activity.exception.NotInitializedException;
import org.seasar.tramina.activity.impl.AbstractComponent;
import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.spi.ToplevelTransaction;
import org.seasar.tramina.spi.TraminaTransaction;
import org.seasar.tramina.spi.TwoPhaseCommitEvent;
import org.seasar.tramina.spi.TwoPhaseCommitEventListener;
import org.seasar.tramina.transaction.TraminaTransactionInternal;
import org.seasar.tramina.transaction.TraminaTransactionManagerInternal;
import org.seasar.tramina.transaction.exception.InvalidTraminaTransactionException;
import org.seasar.tramina.transaction.exception.TransactionAlreadyAssociatedException;
import org.seasar.tramina.transaction.exception.TransactionNotActiveException;
import org.seasar.tramina.transaction.exception.TransactionNotAssociatedException;

import static org.seasar.tramina.activity.impl.ActivityAssertions.*;
import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.transaction.TransactionMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public class TraminaTransactionManagerImpl extends
        AbstractComponent<TraminaTransactionManagerImpl> implements
        TraminaTransactionManagerInternal {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(TraminaTransactionManagerImpl.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    /** 現在のスレッドに関連づけられたトランザクション */
    protected final ThreadLocal<TraminaTransactionInternal> currentTransactions =
        new ThreadLocal<TraminaTransactionInternal>();

    /** このトランザクションマネージャのドメインID */
    protected long domainId;

    /** 2PC イベントを受け取るリスナー */
    protected final Set<TwoPhaseCommitEventListener> listeners =
        new CopyOnWriteArraySet<TwoPhaseCommitEventListener>();

    // /////////////////////////////////////////////////////////////////
    // instance methods from TransactionManager
    //
    @Override
    public int getStatus() throws SystemException {
        assertInitialized(this);
        final TraminaTransaction current = currentTransactions.get();
        if (current == null) {
            return Status.STATUS_NO_TRANSACTION;
        }
        return current.getStatus();
    }

    @Override
    public void begin() throws NotSupportedException, SystemException,
            NotInitializedException {
        assertInitialized(this);
        final TraminaTransactionInternal parent = currentTransactions.get();
        if (parent == null) {
            new ToplevelTransactionImpl(this).begin();
        } else {
            parent.createSubtransaction().begin();
        }
    }

    @Override
    public TraminaTransaction suspend() throws SystemException {
        assertInitialized(this);
        assertTransacstionActive();
        final TraminaTransactionInternal current = getTransaction();
        current.suspend();
        return current;
    }

    @Override
    public void resume(final Transaction transaction)
            throws InvalidTransactionException, IllegalStateException,
            SystemException {
        assertInitialized(this);
        assertParameterNotNull("transaction", transaction);
        assertTransactionNotAssociated();
        assertTraminaTransaction(transaction);
        ((TraminaTransactionInternal) transaction).resume();
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SecurityException,
            IllegalStateException, SystemException {
        assertInitialized(this);
        assertTransacstionActive();
        getTransaction().commit();
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException,
            SystemException {
        assertInitialized(this);
        assertTransacstionActiveOrMarkedRollback();
        getTransaction().rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        assertInitialized(this);
        assertTransacstionActiveOrMarkedRollback();
    }

    @Override
    public void setTransactionTimeout(final int seconds) throws SystemException {
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from TraminaTransactionManager
    //
    @Override
    public TraminaTransactionInternal getTransaction() {
        assertInitialized(this);
        return currentTransactions.get();
    }

    @Override
    public ToplevelTransaction getToplevelTransaction() {
        assertInitialized(this);
        final TraminaTransaction current = getTransaction();
        if (current == null) {
            return null;
        }
        return current.getToplevelTransaction();
    }

    @Override
    public int getToplevelTransactionStatus() {
        assertInitialized(this);
        final TraminaTransaction toplevel = getToplevelTransaction();
        if (toplevel == null) {
            return Status.STATUS_NO_TRANSACTION;
        }
        return toplevel.getStatusType().intValue();
    }

    @Override
    public void addTwoPhaseCommitEventListener(
            final TwoPhaseCommitEventListener listener) {
        assertBeforeDisposed(this);
        assertParameterNotNull("listener", listener);
        listeners.add(listener);
    }

    @Override
    public void removeTwoPhaseCommitEventListener(
            final TwoPhaseCommitEventListener listener) {
        assertBeforeDisposed(this);
        assertParameterNotNull("listener", listener);
        listeners.remove(listener);
    }

    @Override
    public boolean isManagedXid(final Xid xid) {
        assertInitialized(this);
        assertParameterNotNull("xid", xid);
        if (!TraminaXidImpl.isTraminaXid(xid)) {
            return false;
        }
        return TraminaXidImpl.getDomainId(xid) == domainId;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from TraminaTransactionManagerInternal
    //
    /**
     * @return the domainId
     */
    public long getDomainId() {
        assertInitialized(this);
        return domainId;
    }

    @Override
    public void associate(final TraminaTransactionInternal transaction) {
        assertInitialized(this);
        assertParameterNotNull("transaction", transaction);
        currentTransactions.set(transaction);
    }

    @Override
    public void dissociate() {
        assertInitialized(this);
        currentTransactions.set(null);
    }

    @Override
    public void fireBeforeTwoPhaseCommit(final TwoPhaseCommitEvent event)
            throws SystemException {
        assertInitialized(this);
        assertParameterNotNull("event", event);
        SystemException unexpectedException = null;
        for (final TwoPhaseCommitEventListener listener : listeners) {
            try {
                listener.beforeTwoPahseCommit(event);
            } catch (final SystemException e) {
                unexpectedException = e;
                logger.error(TWO_PHASE_LISTENER_RAISED_EXCEPTION
                    .format(listener), e);
            }
        }
        if (unexpectedException != null) {
            throw unexpectedException;
        }
    }

    @Override
    public void fireBeforeLastCommit(final TwoPhaseCommitEvent event)
            throws SystemException {
        assertInitialized(this);
        assertParameterNotNull("event", event);
        SystemException unexpectedException = null;
        for (final TwoPhaseCommitEventListener listener : listeners) {
            try {
                listener.beforeLastCommit(event);
            } catch (final SystemException e) {
                unexpectedException = e;
                logger.error(TWO_PHASE_LISTENER_RAISED_EXCEPTION
                    .format(listener), e);
            }
        }
        if (unexpectedException != null) {
            throw unexpectedException;
        }
    }

    @Override
    public void fireAfterLastCommit(final TwoPhaseCommitEvent event)
            throws SystemException {
        assertInitialized(this);
        assertParameterNotNull("event", event);
        SystemException unexpectedException = null;
        for (final TwoPhaseCommitEventListener listener : listeners) {
            try {
                listener.afterLastCommit(event);
            } catch (final SystemException e) {
                unexpectedException = e;
                logger.error(TWO_PHASE_LISTENER_RAISED_EXCEPTION
                    .format(listener), e);
            }
        }
        if (unexpectedException != null) {
            throw unexpectedException;
        }
    }

    @Override
    public void fireAfterTwoPhaseCommit(final TwoPhaseCommitEvent event)
            throws SystemException {
        assertInitialized(this);
        assertParameterNotNull("event", event);
        SystemException unexpectedException = null;
        for (final TwoPhaseCommitEventListener listener : listeners) {
            try {
                listener.afterTwoPhaseCommit(event);
            } catch (final SystemException e) {
                unexpectedException = e;
                logger.error(TWO_PHASE_LISTENER_RAISED_EXCEPTION
                    .format(listener), e);
            }
        }
        if (unexpectedException != null) {
            throw unexpectedException;
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from AbstractComponent
    //
    @Override
    protected void doInitialize() {
    }

    @Override
    protected void doDispose() {
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public String toString() {
        return super.toString() + "[activityStatus : <" + activityStatus
            + ">, domainId : <" + domainId + ">]";
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for properties
    //
    /**
     * ドメインIDを設定します．
     * 
     * @param domainId
     *            ドメインID
     * @return このインスタンス自身
     * @throws AlreadyInitializedException
     */
    public TraminaTransactionManagerImpl setDomainId(final long domainId)
            throws AlreadyInitializedException {
        assertBeforeInitialized(this);
        this.domainId = domainId;
        return this;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for assertions
    //
    protected void assertTransactionAssociated() {
        final TraminaTransaction tx = getTransaction();
        if (tx == null) {
            throw new TransactionNotAssociatedException();
        }
    }

    protected void assertTransactionNotAssociated() {
        final TraminaTransaction tx = getTransaction();
        if (tx != null) {
            throw new TransactionAlreadyAssociatedException();
        }
    }

    protected void assertTransacstionActive() {
        assertTransactionAssociated();
        final TraminaTransaction tx = getTransaction();
        switch (getTransaction().getStatusType()) {
        case ACTIVE:
            break;
        default:
            throw new TransactionNotActiveException(tx);
        }
    }

    protected void assertTransacstionActiveOrMarkedRollback() {
        assertTransactionAssociated();
        final TraminaTransaction tx = getTransaction();
        switch (getTransaction().getStatusType()) {
        case ACTIVE:
        case MARKED_ROLLBACK:
            break;
        default:
            throw new TransactionNotActiveException(tx);
        }
    }

    protected void assertTraminaTransaction(final Transaction tx)
            throws InvalidTraminaTransactionException {
        if (tx instanceof TraminaTransactionInternal) {
            return;
        }
        throw new InvalidTraminaTransactionException(tx);
    }

}
