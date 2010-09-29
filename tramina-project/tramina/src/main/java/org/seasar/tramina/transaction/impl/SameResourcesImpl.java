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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.spi.LastXAResource;
import org.seasar.tramina.spi.Subtransaction;
import org.seasar.tramina.spi.SubtransactionAwareXAResource;
import org.seasar.tramina.spi.TmFlagType;
import org.seasar.tramina.spi.VoteType;
import org.seasar.tramina.transaction.SameResources;
import org.seasar.tramina.transaction.exception.BeginSubtransactionFailedException;
import org.seasar.tramina.transaction.exception.CommitSubtransactionFailedException;
import org.seasar.tramina.transaction.exception.CommitTransactionFailedException;
import org.seasar.tramina.transaction.exception.EndTransactionFailedException;
import org.seasar.tramina.transaction.exception.IsSameResourceFailedException;
import org.seasar.tramina.transaction.exception.JoinTransactionFailedException;
import org.seasar.tramina.transaction.exception.PrepareTransactionFailedException;
import org.seasar.tramina.transaction.exception.ResumeTransactionFailedException;
import org.seasar.tramina.transaction.exception.RollbackSubtransactionFailedException;
import org.seasar.tramina.transaction.exception.RollbackTransactionFailedException;
import org.seasar.tramina.transaction.exception.StartTransactionFailedException;
import org.seasar.tramina.transaction.exception.SuspendTransactionFailedException;

import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.spi.TmFlagType.*;
import static org.seasar.tramina.transaction.TransactionMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public class SameResourcesImpl implements SameResources {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(SameResourcesImpl.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final Xid xid;

    protected final XAResource xaResource;

    protected final Set<XAResource> enlistedResources =
        new HashSet<XAResource>();

    protected final Set<XAResource> suspendedResources =
        new HashSet<XAResource>();

    protected boolean subtransactionAware;

    protected VoteType vote;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param xid
     * @param xaResource
     */
    public SameResourcesImpl(final Xid xid, final XAResource xaResource)
            throws SystemException {
        assertParameterNotNull("xid", xid);
        assertParameterNotNull("xaResource", xaResource);
        try {
            xaResource.start(xid, NO_FLAGS.getIntValue());
        } catch (final XAException e) {
            throw new StartTransactionFailedException(xid, xaResource, e);
        }
        this.xid = xid;
        this.xaResource = xaResource;
        subtransactionAware =
            xaResource instanceof SubtransactionAwareXAResource;
        enlistedResources.add(xaResource);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from SameResource
    //
    @Override
    public boolean isLastResource() {
        return xaResource instanceof LastXAResource;
    }

    @Override
    public boolean isSameResource(final XAResource otherResource)
            throws SystemException {
        try {
            return xaResource.isSameRM(otherResource);
        } catch (final XAException e) {
            throw new IsSameResourceFailedException(
                xaResource,
                otherResource,
                e);
        }
    }

    @Override
    public XAResource getXAResource() {
        return xaResource;
    }

    @Override
    public void enlist(final XAResource enlistResource) throws SystemException {
        if (!suspendedResources.contains(enlistResource)) {
            try {
                enlistResource.start(xid, JOIN.getIntValue());
            } catch (final XAException e) {
                throw new JoinTransactionFailedException(xid, enlistResource, e);
            }
        } else {
            suspendedResources.remove(enlistResource);
            try {
                enlistResource.start(xid, RESUME.getIntValue());
            } catch (final XAException e) {
                throw new ResumeTransactionFailedException(
                    xid,
                    enlistResource,
                    e);
            }
        }
        enlistedResources.add(enlistResource);
    }

    @Override
    public boolean delist(final XAResource delistResource, final int flag)
            throws SystemException {
        if (!enlistedResources.contains(delistResource)) {
            return false;
        }
        try {
            delistResource.end(xid, flag);
        } catch (final XAException e) {
            if (SUSPEND.equals(flag)) {
                throw new SuspendTransactionFailedException(
                    xid,
                    delistResource,
                    e);
            }
            throw new EndTransactionFailedException(xid, delistResource, e);
        }
        enlistedResources.remove(delistResource);
        if (SUSPEND.equals(flag)) {
            suspendedResources.add(delistResource);
        }
        return true;
    }

    @Override
    public void suspend() throws SystemException {
        SystemException systemException = null;
        for (final Iterator<XAResource> it = enlistedResources.iterator(); it
            .hasNext();) {
            final XAResource enlistedResource = it.next();
            it.remove();
            try {
                enlistedResource.end(xid, SUSPEND.getIntValue());
            } catch (final XAException e) {
                logger.error(SUSPEND_TRANSACTION_FAILED.format(
                    xid,
                    enlistedResource), e);
                systemException =
                    new SuspendTransactionFailedException(
                        xid,
                        enlistedResource,
                        e);
            }
            suspendedResources.add(enlistedResource);
        }
        if (systemException != null) {
            throw systemException;
        }
    }

    @Override
    public void resume() throws SystemException {
        SystemException systemException = null;
        for (final Iterator<XAResource> it = suspendedResources.iterator(); it
            .hasNext();) {
            final XAResource suspendedResource = it.next();
            it.remove();
            try {
                suspendedResource.start(xid, RESUME.getIntValue());
            } catch (final XAException e) {
                logger.error(RESUME_TRANSACTION_FAILED.format(
                    xid,
                    suspendedResource), e);
                systemException =
                    new ResumeTransactionFailedException(
                        xid,
                        suspendedResource,
                        e);
            }
            enlistedResources.add(suspendedResource);
        }
        if (systemException != null) {
            throw systemException;
        }
    }

    @Override
    public void end(final TmFlagType flag) throws SystemException {
        SystemException systemException = null;
        for (final XAResource xaResource : enlistedResources) {
            try {
                xaResource.end(xid, flag.getIntValue());
            } catch (final XAException e) {
                logger.error(END_TRANSACTION_FAILED.format(xid, xaResource), e);
                systemException =
                    new EndTransactionFailedException(xid, xaResource, e);
            }
        }
        if (systemException != null) {
            throw systemException;
        }
    }

    @Override
    public void prepare() throws SystemException {
        try {
            vote = VoteType.getVoteType(xaResource.prepare(xid));
        } catch (final XAException e) {
            throw new PrepareTransactionFailedException(xid, xaResource, e);
        }
    }

    @Override
    public void commitOnePhase() throws SystemException {
        try {
            xaResource.commit(xid, true);
        } catch (final XAException e) {
            throw new CommitTransactionFailedException(xid, xaResource, e);
        }
    }

    @Override
    public void commit() throws SystemException {
        if (vote != VoteType.OK) {
            return;
        }
        try {
            xaResource.commit(xid, false);
        } catch (final XAException e) {
            logger.error(CANNOT_COMMIT_BECOME_INDOUBT_TRANSACTION.format(
                xid,
                xaResource), e);
            throw new CommitTransactionFailedException(xid, xaResource, e);
        }
    }

    @Override
    public void rollback() throws SystemException {
        if (vote == VoteType.RDONLY) {
            return;
        }
        try {
            xaResource.rollback(xid);
        } catch (final XAException e) {
            if (vote == VoteType.OK) {
                logger.error(CANNOT_ROLLBACK_BECOME_INDOUBT_TRANSACTION.format(
                    xid,
                    xaResource), e);
            }
            throw new RollbackTransactionFailedException(xid, xaResource, e);
        }
    }

    @Override
    public void beginSubtransaction(final Subtransaction subtransaction)
            throws SystemException {
        if (!subtransactionAware) {
            return;
        }
        try {
            ((SubtransactionAwareXAResource) xaResource).beginSubtransaction();
        } catch (final XAException e) {
            throw new BeginSubtransactionFailedException(
                subtransaction,
                xaResource,
                e);
        }
    }

    @Override
    public void commitSubtransaction(final Subtransaction subtransaction)
            throws SystemException {
        if (!subtransactionAware) {
            return;
        }
        try {
            ((SubtransactionAwareXAResource) xaResource)
                .commitSubtransaction();
        } catch (final XAException e) {
            throw new CommitSubtransactionFailedException(
                subtransaction,
                xaResource,
                e);
        }
    }

    @Override
    public void rollbackSubtransaction(final Subtransaction subtransaction)
            throws SystemException {
        if (!subtransactionAware) {
            return;
        }
        try {
            ((SubtransactionAwareXAResource) xaResource)
                .rollbackSubtransaction();
        } catch (final XAException e) {
            throw new RollbackSubtransactionFailedException(
                subtransaction,
                xaResource,
                e);
        }
    }

}
