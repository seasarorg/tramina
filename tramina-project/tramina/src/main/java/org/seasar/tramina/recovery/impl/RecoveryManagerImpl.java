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
package org.seasar.tramina.recovery.impl;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.seasar.tramina.activity.exception.AlreadyDisposedException;
import org.seasar.tramina.activity.exception.AlreadyInitializedException;
import org.seasar.tramina.activity.exception.NotInitializedException;
import org.seasar.tramina.activity.impl.AbstractComponent;
import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.recovery.TransacstionResultType;
import org.seasar.tramina.recovery.TransactionLogManager;
import org.seasar.tramina.recovery.exception.AcquireIndoubtTransactionFailedException;
import org.seasar.tramina.spi.RecoveryManager;
import org.seasar.tramina.spi.TraminaTransactionManager;
import org.seasar.tramina.spi.TwoPhaseCommitEvent;
import org.seasar.tramina.spi.TwoPhaseCommitEventListener;

import static org.seasar.tramina.activity.impl.ActivityAssertions.*;
import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.recovery.RecoveryMessages.*;
import static org.seasar.tramina.spi.TmFlagType.*;

/**
 * 
 * 
 * @author koichik
 */
public class RecoveryManagerImpl extends AbstractComponent<RecoveryManagerImpl>
        implements RecoveryManager, TwoPhaseCommitEventListener {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(RecoveryManagerImpl.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected TraminaTransactionManager transactionManager;

    protected TransactionLogManager transactionLogManager;

    // /////////////////////////////////////////////////////////////////
    // instance methods from RecoveryManager
    //
    public void recover(final XAResource xaResource) throws SystemException {
        final Xid[] indoubtXids = getIndoubtXids(xaResource);
        if (indoubtXids.length == 0) {
            return;
        }
        final Xid[] managedXids = getManagedXids(indoubtXids);
        final TransacstionResultType[] results =
            transactionLogManager.getTransactionResult(managedXids);

        for (int i = 0; i < managedXids.length; ++i) {
            final Xid xid = managedXids[i];
            switch (results[i]) {
            case COMMITTED:
                commit(xaResource, xid);
                break;
            case ROLLED_BACK:
                rollback(xaResource, xid);
                break;
            default:
                logger.warning(UNKNOWN_INDOUBT_TRANSACTION.format(xid));
                break;
            }
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from TwoPhaseCommitEventListener
    //
    @Override
    public void beforeTwoPahseCommit(final TwoPhaseCommitEvent event)
            throws SystemException {
        assertInitialized(this);
        assertParameterNotNull("event", event);
        transactionLogManager.open(event);
    }

    @Override
    public void beforeLastCommit(final TwoPhaseCommitEvent event)
            throws SystemException {
        assertInitialized(this);
        assertParameterNotNull("event", event);
        transactionLogManager.writeBeforeCommitLog(event);
    }

    @Override
    public void afterTwoPhaseCommit(final TwoPhaseCommitEvent event)
            throws SystemException {
        assertInitialized(this);
        assertParameterNotNull("event", event);
        transactionLogManager.writeAfterCommitLog(event);
    }

    @Override
    public void afterLastCommit(final TwoPhaseCommitEvent event)
            throws SystemException {
        assertInitialized(this);
        assertParameterNotNull("event", event);
        transactionLogManager.close(event);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from AbstractComponent
    //

    @Override
    protected void doInitialize() throws AlreadyInitializedException {
        assertPropertyNotNull(this, "transactionManager", transactionManager);
        assertPropertyNotNull(
            this,
            "transactionLogManager",
            transactionLogManager);
    }

    @Override
    protected void doDispose() throws NotInitializedException,
            AlreadyDisposedException {
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public String toString() {
        return super.toString() + "[activityStatus : <" + activityStatus
            + ">, transactionManager : <" + transactionManager
            + ">, transactionLogManager : <" + transactionLogManager + ">]";
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for properties
    //
    /**
     * @param transactionManager
     *            the transactionManager to set
     */
    public RecoveryManagerImpl setTransactionManager(
            final TraminaTransactionManager transactionManager) {
        assertBeforeInitialized(this);
        assertParameterNotNull("transactionManager", transactionManager);
        this.transactionManager = transactionManager;
        transactionManager.addTwoPhaseCommitEventListener(this);
        return this;
    }

    /**
     * @param transactionLogManager
     *            the transactionLogger to set
     */
    public RecoveryManagerImpl setTransactionLogManager(
            final TransactionLogManager transactionLogManager) {
        assertBeforeInitialized(this);
        assertParameterNotNull("transactionLogManager", transactionLogManager);
        this.transactionLogManager = transactionLogManager;
        return this;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for internal
    //
    protected Xid[] getIndoubtXids(final XAResource xaResource)
            throws SystemException {
        try {
            return xaResource.recover(START_RSCAN.getIntValue());
        } catch (final XAException e) {
            throw new AcquireIndoubtTransactionFailedException(this, e);
        }
    }

    protected Xid[] getManagedXids(final Xid[] indoubtXids) {
        final List<Xid> managedXids = new ArrayList<Xid>(indoubtXids.length);
        for (final Xid xid : indoubtXids) {
            if (transactionManager.isManagedXid(xid)) {
                managedXids.add(xid);
            } else {
                logger.warning(NOT_MANAGED_XID.format(
                    transactionLogManager,
                    xid));
            }
        }
        return managedXids.toArray(new Xid[managedXids.size()]);
    }

    protected void commit(final XAResource xaResource, final Xid xid) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info(COMMITTING_INDOUBT_TRANSACTION.format(xid));
            }
            xaResource.commit(xid, false);
            if (logger.isInfoEnabled()) {
                logger.info(INDOUBT_TRANSACTION_COMMITTED.format(xid));
            }
        } catch (final XAException e) {
            logger.error(COMMIT_INDOUBT_TRANSACTION_FAILED.format(xid), e);
            switch (e.errorCode) {
            case XAException.XA_HEURRB:
            case XAException.XA_HEURMIX:
            case XAException.XA_HEURHAZ:
                logger.error(INDOUBT_TRANSACTION_HEURISTIC_ROLLEDBACK
                    .format(xid));
            }
            forgetSilently(xaResource, xid);
        }
    }

    protected void rollback(final XAResource xaResource, final Xid xid) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info(ROLLINGBACK_INDOUBT_TRANSACTION.format(xid));
            }
            xaResource.rollback(xid);
            if (logger.isInfoEnabled()) {
                logger.info(INDOUBT_TRANSACTION_ROLLEDBACK.format(xid));
            }
        } catch (final XAException e) {
            logger.error(ROLLBACK_INDOUBT_TRANSACTION_FAILED.format(xid), e);
            switch (e.errorCode) {
            case XAException.XA_HEURCOM:
            case XAException.XA_HEURMIX:
            case XAException.XA_HEURHAZ:
                logger.error(INDOUBT_TRANSACTION_HEURISTIC_COMMITTED
                    .format(xid));
            }
            forgetSilently(xaResource, xid);
        }
    }

    protected void forgetSilently(final XAResource xaResource, final Xid xid) {
        try {
            xaResource.forget(xid);
        } catch (final XAException ignore) {
        }
    }
}
