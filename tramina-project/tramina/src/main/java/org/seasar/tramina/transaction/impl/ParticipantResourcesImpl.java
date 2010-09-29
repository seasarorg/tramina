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

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.spi.Subtransaction;
import org.seasar.tramina.spi.TmFlagType;
import org.seasar.tramina.spi.TraminaTransaction;
import org.seasar.tramina.spi.TraminaXid;
import org.seasar.tramina.transaction.SameResources;
import org.seasar.tramina.transaction.ParticipantResources;
import org.seasar.tramina.transaction.exception.LastResourceAlreadyEnlistedException;

import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.transaction.TransactionMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public class ParticipantResourcesImpl implements ParticipantResources {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(ParticipantResourcesImpl.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final TraminaTransaction transaction;

    protected final TraminaXid xid;

    protected final Deque<SameResources> resources =
        new LinkedList<SameResources>();

    protected SameResources lastResource;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param xid
     */
    public ParticipantResourcesImpl(final TraminaTransaction transaction,
            final TraminaXid xid) {
        assertParameterNotNull("transaction", transaction);
        assertParameterNotNull("xid", xid);
        this.transaction = transaction;
        this.xid = xid;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from ParticipantResources
    //
    @Override
    public boolean isEmpty() {
        return resources.isEmpty() && lastResource == null;
    }

    @Override
    public int size() {
        return resources.size() + (hasLastResource() ? 1 : 0);
    }

    @Override
    public boolean hasLastResource() {
        return lastResource != null;
    }

    @Override
    public SameResources[] getCommitTargetResourcesAsArray() {
        if (!hasLastResource()) {
            return resources
                .toArray(new SameResources[resources.size()]);
        }
        final SameResources[] result =
            resources.toArray(new SameResources[resources.size() + 1]);
        result[resources.size()] = lastResource;
        return result;
    }

    @Override
    public boolean enlist(final XAResource xaResource) throws SystemException {
        for (final SameResources resource : resources) {
            if (resource.isSameResource(xaResource)) {
                resource.enlist(xaResource);
                return true;
            }
        }
        resources.addLast(new SameResourcesImpl(
            xid.createNewBranch(),
            xaResource));
        return true;
    }

    @Override
    public boolean enlistLastResource(final XAResource xaResource)
            throws SystemException {
        if (hasLastResource()) {
            if (lastResource.isSameResource(xaResource)) {
                lastResource.enlist(xaResource);
                return true;
            }
            throw new LastResourceAlreadyEnlistedException(
                transaction,
                xaResource,
                lastResource.getXAResource());
        }
        lastResource =
            new SameResourcesImpl(xid.createNewBranch(), xaResource);
        return true;
    }

    @Override
    public boolean delist(final XAResource xaResource, final int flag)
            throws SystemException {
        for (final SameResources resource : getAllResources()) {
            if (resource.delist(xaResource, flag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void takeOver(final SameResources[] takeOverResources)
            throws SystemException {
        for (final SameResources resource : takeOverResources) {
            if (resource.isLastResource()) {
                if (hasLastResource()) {
                    throw new LastResourceAlreadyEnlistedException(
                        transaction,
                        resource.getXAResource(),
                        lastResource.getXAResource());
                }
                lastResource = resource;
            } else {
                resources.addLast(resource);
            }
        }
    }

    @Override
    public void suspend() throws SystemException {
        SystemException systemException = null;
        for (final SameResources resource : getAllResources()) {
            try {
                resource.suspend();
            } catch (final SystemException e) {
                systemException = e;
            }
        }
        if (systemException != null) {
            throw systemException;
        }
    }

    @Override
    public void resume() throws SystemException {
        SystemException systemException = null;
        for (final SameResources resource : getAllResources()) {
            try {
                resource.resume();
            } catch (final SystemException e) {
                systemException = e;
            }
        }
        if (systemException != null) {
            throw systemException;
        }
    }

    @Override
    public void end(final TmFlagType flag) throws SystemException {
        SystemException systemException = null;
        for (final SameResources resource : getAllResources()) {
            try {
                resource.end(flag);
            } catch (final SystemException e) {
                systemException = e;
            }
        }
        if (systemException != null) {
            throw systemException;
        }
    }

    @Override
    public boolean canOnePhaseCommit() {
        if (hasLastResource()) {
            return resources.isEmpty();
        }
        return resources.size() == 1;
    }

    @Override
    public void commitOnePhase() throws SystemException {
        if (hasLastResource()) {
            lastResource.commitOnePhase();
        } else {
            resources.getLast().commitOnePhase();
        }
    }

    @Override
    public void prepare() throws SystemException {
        if (!hasLastResource()) {
            lastResource = resources.removeLast();
        }
        for (final SameResources resource : resources) {
            resource.prepare();
        }
    }

    @Override
    public void commitLastResource() throws SystemException {
        lastResource.commitOnePhase();
    }

    @Override
    public void commit() throws SystemException {
        SystemException systemException = null;
        for (final SameResources resource : resources) {
            try {
                resource.commit();
            } catch (final SystemException e) {
                systemException = e;
            }
        }
        if (systemException != null) {
            throw systemException;
        }
    }

    @Override
    public void rollback() throws SystemException {
        SystemException systemException = null;
        for (final SameResources resource : getAllResources()) {
            try {
                resource.rollback();
            } catch (final SystemException e) {
                systemException = e;
            }
        }
        if (systemException != null) {
            throw systemException;
        }
    }

    @Override
    public void beginNestedTransaction(final Subtransaction subtransaction)
            throws SystemException {
        final Deque<SameResources> beganResources =
            new LinkedList<SameResources>();
        SystemException systemException = null;
        for (final SameResources resource : getAllResources()) {
            try {
                resource.beginSubtransaction(subtransaction);
                beganResources.addLast(resource);
            } catch (final SystemException e) {
                systemException = e;
                break;
            }
        }
        if (systemException != null) {
            for (final SameResources resource : beganResources) {
                try {
                    resource.rollbackSubtransaction(subtransaction);
                } catch (final SystemException e) {
                    logger.error(ROLLBACK_SUBTRANSACTION_FAILED.format(
                        transaction,
                        resource.getXAResource()), e);
                }
            }
            throw systemException;
        }
    }

    @Override
    public void commitSubtransaction(final Subtransaction subtransaction)
            throws SystemException {
        SystemException systemException = null;
        for (final SameResources resource : getAllResources()) {
            try {
                resource.commitSubtransaction(subtransaction);
            } catch (final SystemException e) {
                systemException = e;
            }
        }
        if (systemException != null) {
            throw systemException;
        }
    }

    @Override
    public void rollbackSubtransaction(final Subtransaction subtransaction)
            throws SystemException {
        SystemException systemException = null;
        for (final SameResources resource : getAllResources()) {
            try {
                resource.rollbackSubtransaction(subtransaction);
            } catch (final SystemException e) {
                systemException = e;
            }
        }
        if (systemException != null) {
            throw systemException;
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for internal
    //
    protected Iterable<SameResources> getAllResources() {
        if (!hasLastResource()) {
            return resources;
        }
        final Iterator<SameResources> it = resources.iterator();
        return new Iterable<SameResources>() {
            @Override
            public Iterator<SameResources> iterator() {
                return new Iterator<SameResources>() {
                    protected boolean processedLastResource;

                    @Override
                    public boolean hasNext() {
                        return it.hasNext() || !processedLastResource;
                    }

                    @Override
                    public SameResources next() {
                        if (it.hasNext()) {
                            return it.next();
                        }
                        if (!processedLastResource) {
                            processedLastResource = true;
                            return lastResource;
                        }
                        throw new NoSuchElementException();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("remove");
                    }
                };
            }
        };
    }

}
