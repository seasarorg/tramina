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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.spi.LastXAResource;
import org.seasar.tramina.spi.Subtransaction;
import org.seasar.tramina.spi.ToplevelTransaction;
import org.seasar.tramina.spi.TraminaTransaction;
import org.seasar.tramina.spi.TraminaXid;
import org.seasar.tramina.spi.TwoPhaseCommitEvent;
import org.seasar.tramina.transaction.SameResources;
import org.seasar.tramina.transaction.TraminaTransactionManagerInternal;
import org.seasar.tramina.transaction.exception.ToplevelTransactionRolledbackException;

import static java.util.Arrays.*;
import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.spi.TmFlagType.*;
import static org.seasar.tramina.spi.TransactionStatusType.*;
import static org.seasar.tramina.transaction.TransactionMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public class ToplevelTransactionImpl extends AbstractTraminaTransaction
        implements ToplevelTransaction {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(ToplevelTransactionImpl.class);

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param transctionManager
     */
    public ToplevelTransactionImpl(
            final TraminaTransactionManagerInternal transctionManager) {
        super(transctionManager, new TraminaXidImpl(transctionManager
            .getDomainId()));
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from ToplevelTransaction
    //
    @Override
    public TraminaXid getXid() {
        return xid;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from TraminaTransactionInternal
    //
    public void begin() {
        assertNoTransacstion();
        status = ACTIVE;
        transctionManager.associate(this);
        if (logger.isDebugEnabled()) {
            logger.debug(TOPLEVEL_TRANSACTION_BEGAN.format(this));
        }
    }

    @Override
    public void onSuspended() throws SystemException {
        assertTransactionActiveOrMarkedRollback();
        assertTransactionNotSuspended();
        participantResources.suspend();
        suspended = true;
        if (logger.isDebugEnabled()) {
            logger.debug(TOPLEVEL_TRANSACTION_SUSPENDED.format(this));
        }
    }

    @Override
    public void onResumeed() throws SystemException {
        assertTransactionActiveOrMarkedRollback();
        assertTransactionSuspended();
        participantResources.resume();
        suspended = false;
        if (logger.isDebugEnabled()) {
            logger.debug(TOPLEVEL_TRANSACTION_RESUMED.format(this));
        }
    }

    @Override
    public void onSubtransactionBegan(final Subtransaction subtransaction)
            throws SystemException {
        assertTransactionActive();
        participantResources.beginNestedTransaction(subtransaction);
    }

    @Override
    public void onSubtransactionCommitted(final Subtransaction subtransaction,
            final SameResources[] takeOverResources,
            final Synchronization[] takeOverSynchronizations,
            final Synchronization[] takeOverInterposedSynchronizations)
            throws SystemException {
        assertTransactionActive();
        assertChild(subtransaction);
        onSubtransactionCommitted(subtransaction);
        participantResources.takeOver(takeOverResources);
        synchronizations.addAll(asList(takeOverSynchronizations));
        interposedSynchronizations
            .addAll(asList(takeOverInterposedSynchronizations));
        child = null;
    }

    @Override
    public void onSubtransactionCommitted(final Subtransaction subtransaction)
            throws SystemException {
        assertTransactionActive();
        participantResources.commitSubtransaction(subtransaction);
    }

    @Override
    public void onSubtransactionRolledBack(final Subtransaction subtransaction)
            throws SystemException {
        assertTransactionActiveOrMarkedRollback();
        participantResources.rollbackSubtransaction(subtransaction);
        if (subtransaction == child) {
            child = null;
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from TraminaTransaction
    //
    @Override
    public boolean isToplevel() {
        return true;
    }

    @Override
    public TraminaTransaction getParent() {
        return null;
    }

    @Override
    public ToplevelTransaction getToplevelTransaction() {
        return this;
    }

    @Override
    public void registerInterposedSynchronization(final Synchronization sync)
            throws RollbackException, IllegalStateException, SystemException {
        assertParameterNotNull("sync", sync);
        assertTransactionNotMarkedRollbackOnly();
        assertTransactionActive();
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
            if (xaResource instanceof LastXAResource) {
                return participantResources.enlistLastResource(xaResource);
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
        doBeforeCompletion();
        doEnd();
        if (status == MARKED_ROLLBACK) {
            doRollback();
        } else if (participantResources.isEmpty()) {
            doCommitNoResource();
        } else if (participantResources.canOnePhaseCommit()) {
            doOnePhaseCommit();
        } else {
            doTwoPhaseCommit();
        }
        try {
            if (status == COMMITTED) {
                if (logger.isDebugEnabled()) {
                    logger.debug(TOPLEVEL_TRANSACTION_COMMITTED.format(this));
                }
            } else if (status == ROLLEDBACK) {
                if (logger.isDebugEnabled()) {
                    logger.debug(TOPLEVEL_TRANSACTION_ROLLEDBACK.format(this));
                }
                throw new ToplevelTransactionRolledbackException(this);
            } else {
                throw new SystemException();
            }
        } finally {
            transctionManager.dissociate();
            doAfterCompletion();
        }
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
        assertTransactionActiveOrMarkedRollback();
        if (child != null) {
            child.rollback();
        }
        status = MARKED_ROLLBACK;
        doEnd();
        doRollback();
        if (logger.isDebugEnabled()) {
            logger.debug(TOPLEVEL_TRANSACTION_ROLLEDBACK.format(this));
        }
        transctionManager.dissociate();
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
    protected void doCommitNoResource() {
        status = COMMITTED;
    }

    protected void doOnePhaseCommit() {
        try {
            status = COMMITTING;
            participantResources.commitOnePhase();
            status = COMMITTED;
        } catch (final Exception e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED_IN_COMMIT_PROCESS
                .format(this), e);
            doRollback();
        }
    }

    protected void doTwoPhaseCommit() {
        try {
            if (!beginTwoPhaseCommit()) {
                return;
            }
            if (!doPrepare()) {
                return;
            }
            if (!doLastResourceCommit()) {
                return;
            }
            doRestResourcesCommit();
        } finally {
            endTwoPhaseCommit();
        }
    }

    protected boolean beginTwoPhaseCommit() {
        try {
            transctionManager
                .fireBeforeTwoPhaseCommit(createTwoPhaseCommitEvent());
            return true;
        } catch (final Exception e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED_IN_COMMIT_PROCESS
                .format(this), e);
            doRollback();
            return false;
        }
    }

    protected boolean doPrepare() {
        try {
            status = PREPARING;
            participantResources.prepare();
            status = PREPARED;
            return true;
        } catch (final Exception e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED_IN_COMMIT_PROCESS
                .format(this), e);
            doRollback();
            return false;
        }
    }

    protected boolean doLastResourceCommit() {
        try {
            status = COMMITTING;
            transctionManager.fireBeforeLastCommit(createTwoPhaseCommitEvent());
            participantResources.commitLastResource();
            transctionManager.fireAfterLastCommit(createTwoPhaseCommitEvent());
            return true;
        } catch (final Exception e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED_IN_COMMIT_PROCESS
                .format(this), e);
            doRollback();
            return false;
        }
    }

    protected void doRestResourcesCommit() {
        try {
            participantResources.commit();
            status = COMMITTED;
        } catch (final Exception e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED_IN_COMMIT_PROCESS
                .format(this), e);
            status = UNKNOWN;
        }
    }

    protected void endTwoPhaseCommit() {
        try {
            transctionManager
                .fireAfterTwoPhaseCommit(createTwoPhaseCommitEvent());
        } catch (final Exception e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED_IN_COMMIT_PROCESS
                .format(this), e);
            status = UNKNOWN;
        }
    }

    protected void doRollback() {
        status = ROLLING_BACK;
        try {
            participantResources.rollback();
        } catch (final Exception e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED_IN_ROLLBACK_PROCESS
                .format(this), e);
        }
        status = ROLLEDBACK;
    }

    protected void doEnd() {
        try {
            participantResources.end(status == ACTIVE ? SUCCESS : FAIL);
        } catch (final Exception e) {
            logger.error(UNEXPECTED_EXCEPTION_OCCURRED.format(this), e);
            status = MARKED_ROLLBACK;
        }
    }

    protected TwoPhaseCommitEvent createTwoPhaseCommitEvent() {
        return new TwoPhaseCommitEvent(this, xid, status);
    }
}
