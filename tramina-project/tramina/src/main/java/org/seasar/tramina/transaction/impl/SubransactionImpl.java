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

import java.util.List;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.spi.Subtransaction;
import org.seasar.tramina.spi.SubtransactionAwareXAResource;
import org.seasar.tramina.spi.ToplevelTransaction;
import org.seasar.tramina.spi.TraminaTransaction;
import org.seasar.tramina.transaction.SameResources;
import org.seasar.tramina.transaction.TraminaTransactionInternal;
import org.seasar.tramina.transaction.TraminaTransactionManagerInternal;
import org.seasar.tramina.transaction.exception.BeginSubtransactionFailedException;
import org.seasar.tramina.transaction.exception.SubtransactionRolledbackException;

import static java.util.Arrays.*;
import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.spi.TransactionStatusType.*;
import static org.seasar.tramina.transaction.TransactionMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public class SubransactionImpl extends AbstractTraminaTransaction implements
        Subtransaction {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(SubransactionImpl.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final TraminaTransactionInternal parent;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param parent
     * @param name
     */
    public SubransactionImpl(
            final TraminaTransactionManagerInternal transctionManager,
            final TraminaTransactionInternal parent) {
        super(transctionManager, parent.getToplevelTransaction().getXid());
        assertParameterNotNull("parent", parent);
        this.parent = parent;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from TraminaTransactionInternal
    //
    @Override
    public void begin() throws SystemException {
        assertNoTransacstion();
        status = ACTIVE;
        try {
            parent.onSubtransactionBegan(this);
        } catch (SystemException e) {
            status = UNKNOWN;
            logger.error(PARENT_MARK_ROLLBACK_ONLY.format(this, parent), e);
            parent.setRollbackOnly();
            throw e;
        }
        transctionManager.associate(this);
        if (logger.isDebugEnabled()) {
            logger.debug(SUBTRANSACTION_BEGAN.format(this, parent));
        }
    }

    @Override
    public void onSuspended() throws SystemException {
        assertTransactionActiveOrMarkedRollback();
        participantResources.suspend();
        suspended = true;
        if (logger.isDebugEnabled()) {
            logger.debug(SUBTRANSACTION_SUSPENDED.format(this));
        }
        parent.onSuspended();
    }

    @Override
    public void onResumeed() throws SystemException {
        assertTransactionActiveOrMarkedRollback();
        assertTransactionSuspended();
        parent.onResumeed();
        participantResources.resume();
        suspended = false;
        if (logger.isDebugEnabled()) {
            logger.debug(SUBTRANSACTION_RESUMED.format(this));
        }
    }

    @Override
    public void onSubtransactionBegan(final Subtransaction subtransaction)
            throws SystemException {
        assertTransactionActive();
        parent.onSubtransactionBegan(subtransaction);
    }

    @Override
    public void onSubtransactionCommitted(final Subtransaction subtransaction,
            final SameResources[] takeOverResources,
            final Synchronization[] takeOverSynchronizations,
            final Synchronization[] takeOverInterposedSynchronizations)
            throws SystemException {
        assertTransactionActive();
        assertChild(subtransaction);
        participantResources.takeOver(takeOverResources);
        synchronizations.addAll(asList(takeOverSynchronizations));
        interposedSynchronizations
            .addAll(asList(takeOverInterposedSynchronizations));
        parent.onSubtransactionCommitted(subtransaction);
        child = null;
    }

    @Override
    public void onSubtransactionCommitted(final Subtransaction subtransaction)
            throws SystemException {
        assertTransactionActive();
        parent.onSubtransactionCommitted(subtransaction);
    }

    @Override
    public void onSubtransactionRolledBack(final Subtransaction subtransaction)
            throws SystemException {
        assertTransactionActiveOrMarkedRollback();
        parent.onSubtransactionRolledBack(subtransaction);
        if (subtransaction == child) {
            child = null;
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from TraminaTransaction
    //
    @Override
    public boolean isToplevel() {
        return false;
    }

    @Override
    public TraminaTransaction getParent() {
        return parent;
    }

    @Override
    public ToplevelTransaction getToplevelTransaction() {
        return parent.getToplevelTransaction();
    }

    @Override
    public void registerInterposedSynchronization(final Synchronization sync)
            throws RollbackException, IllegalStateException, SystemException {
        interposedSynchronizations.add(sync);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Transaction
    //
    @Override
    public boolean enlistResource(final XAResource xaResource)
            throws RollbackException, IllegalStateException, SystemException {
        assertParameterNotNull("xaResource", xaResource);
        assertTransactionNotMarkedRollbackOnly();
        assertTransactionActive();
        try {
            if (xaResource instanceof SubtransactionAwareXAResource) {
                parent.enlistResource(xaResource);
                try {
                    ((SubtransactionAwareXAResource) xaResource)
                        .beginSubtransaction();
                    return true;
                } catch (final XAException e) {
                    throw new BeginSubtransactionFailedException(
                        this,
                        xaResource,
                        e);
                }
            }
            return participantResources.enlist(xaResource);
        } catch (final SystemException e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED.format(this), e);
            setRollbackOnly();
            throw e;
        } catch (final RuntimeException e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED.format(this), e);
            setRollbackOnly();
            throw e;
        }
    }

    @Override
    public boolean delistResource(final XAResource xaResource, final int flag)
            throws IllegalStateException, SystemException {
        assertParameterNotNull("xaResource", xaResource);
        assertTransactionActiveOrMarkedRollback();
        try {
            if (xaResource instanceof SubtransactionAwareXAResource) {
                return parent.delistResource(xaResource, flag);
            }
            return participantResources.delist(xaResource, flag);
        } catch (final SystemException e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED.format(this), e);
            setRollbackOnly();
            throw e;
        } catch (final RuntimeException e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED.format(this), e);
            setRollbackOnly();
            throw e;
        }
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SecurityException,
            IllegalStateException, SystemException {
        assertTransactionActive();
        assertSubtransactionNotActive();
        try {
            status = COMMITTING;
            parent.onSubtransactionCommitted(
                this,
                participantResources.getCommitTargetResourcesAsArray(),
                toArray(synchronizations),
                toArray(interposedSynchronizations));
            status = COMMITTED;
            if (logger.isDebugEnabled()) {
                logger.debug(SUBTRANSACTION_COMMITTED.format(this));
            }
        } catch (final SystemException e) {
            status = ROLLEDBACK;
            if (logger.isDebugEnabled()) {
                logger.debug(SUBTRANSACTION_ROLLEDBACK.format(this));
            }
            throw new SubtransactionRolledbackException(this, e);
        } catch (final RuntimeException e) {
            status = ROLLEDBACK;
            if (logger.isDebugEnabled()) {
                logger.debug(SUBTRANSACTION_ROLLEDBACK.format(this));
            }
            throw new SubtransactionRolledbackException(this, e);
        } finally {
            transctionManager.associate(parent);
        }
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
        assertTransactionActiveOrMarkedRollback();
        if (child != null) {
            child.rollback();
        }
        status = ROLLING_BACK;
        try {
            participantResources.rollback();
            parent.onSubtransactionRolledBack(this);
            status = ROLLEDBACK;
            if (logger.isDebugEnabled()) {
                logger.debug(SUBTRANSACTION_ROLLEDBACK.format(this));
            }
        } catch (final Exception e) {
            status = UNKNOWN;
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED_IN_ROLLBACK_PROCESS
                .format(this), e);
        }
        transctionManager.associate(parent);
        doAfterCompletion();
    }

    @Override
    public void registerSynchronization(final Synchronization sync)
            throws RollbackException, IllegalStateException, SystemException {
        assertParameterNotNull("sync", sync);
        assertTransactionNotMarkedRollbackOnly();
        assertTransactionActive();
        synchronizations.add(sync);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for internal
    //
    protected Synchronization[] toArray(
            final List<Synchronization> synchronizations) {
        assertParameterNotNull("synchronizations", synchronizations);
        return synchronizations.toArray(new Synchronization[synchronizations
            .size()]);
    }

}
