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

import java.util.ArrayList;
import java.util.List;

import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.spi.Subtransaction;
import org.seasar.tramina.spi.TraminaTransaction;
import org.seasar.tramina.spi.TraminaXid;
import org.seasar.tramina.spi.TransactionStatusType;
import org.seasar.tramina.transaction.ParticipantResources;
import org.seasar.tramina.transaction.TraminaTransactionInternal;
import org.seasar.tramina.transaction.TraminaTransactionManagerInternal;
import org.seasar.tramina.transaction.exception.InvalidSubtransactionException;
import org.seasar.tramina.transaction.exception.NotCurrentTtransactionException;
import org.seasar.tramina.transaction.exception.SubtransactionActiveException;
import org.seasar.tramina.transaction.exception.TransactionAlreadyBeganException;
import org.seasar.tramina.transaction.exception.TransactionMarkedRollbackException;
import org.seasar.tramina.transaction.exception.TransactionNotActiveException;
import org.seasar.tramina.transaction.exception.TransactionNotSuspendedException;
import org.seasar.tramina.transaction.exception.TransactionSuspendedException;

import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.spi.TransactionStatusType.*;
import static org.seasar.tramina.transaction.TransactionMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public abstract class AbstractTraminaTransaction implements
        TraminaTransactionInternal {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(AbstractTraminaTransaction.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final TraminaTransactionManagerInternal transctionManager;

    protected final TraminaXid xid;

    protected final ParticipantResources participantResources;

    protected final List<Synchronization> synchronizations =
        new ArrayList<Synchronization>();

    protected final List<Synchronization> interposedSynchronizations =
        new ArrayList<Synchronization>();

    protected TransactionStatusType status = NO_TRANSACTION;

    protected boolean suspended;

    protected boolean rollbackOnly;

    protected TraminaTransactionInternal child;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param transctionManager
     * @param xid
     */
    public AbstractTraminaTransaction(
            final TraminaTransactionManagerInternal transctionManager,
            final TraminaXid xid) {
        assertParameterNotNull("transctionManager", transctionManager);
        assertParameterNotNull("xid", xid);
        this.transctionManager = transctionManager;
        this.xid = xid;
        participantResources = new ParticipantResourcesImpl(this, xid);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods form TraminaTransactionInternal
    //
    @Override
    public void suspend() throws IllegalStateException, SystemException {
        if (child != null) {
            child.suspend();
        } else {
            onSuspended();
            transctionManager.dissociate();
        }
    }

    @Override
    public void resume() throws IllegalStateException, SystemException {
        if (child != null) {
            child.resume();
        } else {
            onResumeed();
            transctionManager.associate(this);
        }
    }

    @Override
    public TraminaTransactionInternal createSubtransaction()
            throws SystemException {
        assertTransactionActive();
        assertSubtransactionNotActive();
        child = new SubransactionImpl(transctionManager, this);
        return child;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods form TraminaTransaction
    //
    @Override
    public TransactionStatusType getStatusType() {
        return status;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods form Transaction
    //
    @Override
    public int getStatus() {
        return status.intValue();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        assertTransactionActiveOrMarkedRollback();
        status = MARKED_ROLLBACK;
        if (logger.isDebugEnabled()) {
            if (isToplevel()) {
                logger.debug(TOPLEVEL_TRANSACTION_MARKED_ROLLBACK.format(this));
            } else {
                logger.debug(SUBTRANSACTION_MARKED_ROLLBACK.format(this));
            }
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public String toString() {
        return super.toString() + "[status : <" + status + ">, suspended : <"
            + suspended + ">, rollbackOnly : <" + rollbackOnly + ">, xid : <"
            + xid + ">, child : " + child + ">]";
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for subclasses
    //
    protected void doBeforeCompletion() {
        assertTransactionActive();
        for (final Synchronization sync : synchronizations) {
            try {
                sync.beforeCompletion();
            } catch (final Exception e) {
                status = MARKED_ROLLBACK;
                logger.error(SYNCHRONIZATION_RAISED_EXCEPTION.format(sync), e);
                return;
            }
        }
        for (final Synchronization sync : interposedSynchronizations) {
            try {
                sync.beforeCompletion();
            } catch (final Exception e) {
                status = MARKED_ROLLBACK;
                logger.error(SYNCHRONIZATION_RAISED_EXCEPTION.format(sync), e);
                return;
            }
        }
    }

    protected void doAfterCompletion() {
        for (final Synchronization sync : interposedSynchronizations) {
            try {
                sync.afterCompletion(status.intValue());
            } catch (final Exception e) {
                logger.error(SYNCHRONIZATION_RAISED_EXCEPTION.format(sync), e);
            }
        }
        for (final Synchronization sync : synchronizations) {
            try {
                sync.afterCompletion(status.intValue());
            } catch (final Exception e) {
                logger.error(SYNCHRONIZATION_RAISED_EXCEPTION.format(sync), e);
            }
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for assertions
    //
    protected void assertNoTransacstion() {
        switch (status) {
        case NO_TRANSACTION:
            break;
        default:
            throw new TransactionAlreadyBeganException(this);
        }
    }

    protected void assertTransactionActive() {
        switch (status) {
        case ACTIVE:
            break;
        default:
            throw new TransactionNotActiveException(this);
        }
    }

    protected void assertTransactionActiveOrMarkedRollback() {
        switch (status) {
        case ACTIVE:
        case MARKED_ROLLBACK:
            break;
        default:
            throw new TransactionNotActiveException(this);
        }
    }

    protected void assertTransactionNotSuspended() {
        if (suspended) {
            throw new TransactionSuspendedException(this);
        }
    }

    protected void assertTransactionSuspended() {
        if (!suspended) {
            throw new TransactionNotSuspendedException(this);
        }
    }

    protected void assertTransactionNotMarkedRollbackOnly()
            throws RollbackException {
        if (rollbackOnly) {
            throw new TransactionMarkedRollbackException(this);
        }
    }

    protected void assertSubtransactionNotActive() {
        if (child != null) {
            throw new SubtransactionActiveException(this, child);
        }
    }

    protected void assertChild(final Subtransaction subtransaction) {
        if (subtransaction != child) {
            throw new InvalidSubtransactionException(this, child);
        }
    }

    protected void assertCurrentTransaction() throws SystemException {
        assertCurrentTransaction(this);
    }

    protected void assertCurrentTransaction(
            AbstractTraminaTransaction transaction) throws SystemException {
        final TraminaTransaction current = transctionManager.getTransaction();
        if (current != transaction) {
            throw new NotCurrentTtransactionException(transaction, current);
        }
    }
}
