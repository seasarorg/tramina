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
package org.seasar.tramina.resource.jdbc.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.seasar.tramina.resource.jdbc.exception.CommitResourceFailedException;
import org.seasar.tramina.resource.jdbc.exception.IllegalFlagsException;
import org.seasar.tramina.resource.jdbc.exception.IllegalXidException;
import org.seasar.tramina.resource.jdbc.exception.ResourceAlreadyStartedException;
import org.seasar.tramina.resource.jdbc.exception.ResourceNotEndedException;
import org.seasar.tramina.resource.jdbc.exception.ResourceNotPreparedException;
import org.seasar.tramina.resource.jdbc.exception.ResourceNotStartedException;
import org.seasar.tramina.resource.jdbc.exception.ResourceNotSuspendedException;
import org.seasar.tramina.resource.jdbc.exception.RollbackResourceFailedException;
import org.seasar.tramina.resource.jdbc.exception.SetAutoCommitFailedException;
import org.seasar.tramina.resource.jdbc.exception.StartResourceFailedException;

import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.resource.jdbc.impl.PseudoXAResourceImpl.StatusType.*;
import static org.seasar.tramina.spi.TmFlagType.*;
import static org.seasar.tramina.spi.VoteType.*;

/**
 * 
 * 
 * @author koichik
 */
public class PseudoXAResourceImpl implements XAResource {

    // /////////////////////////////////////////////////////////////////
    // member types
    //
    protected enum StatusType {
        NO_TRANSACTION,
        STARTED,
        SUSPENDED,
        ENDED,
        PREPARED,
        UNKNOWN;
    }

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final Connection connection;

    protected StatusType status = NO_TRANSACTION;

    protected Xid xid;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param connection
     */
    public PseudoXAResourceImpl(final Connection connection) {
        assertParameterNotNull("connection", connection);
        this.connection = connection;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from XAResource
    //
    @Override
    public boolean isSameRM(final XAResource xares) throws XAException {
        assertParameterNotNull("xares", xares);
        return false;
    }

    @Override
    public void start(final Xid xid, final int flags) throws XAException {
        assertParameterNotNull("xid", xid);
        if (NO_FLAGS.equals(flags)) {
            assertNoTransaction();
        } else if (RESUME.equals(flags)) {
            assertSuspended();
        } else {
            throw new IllegalFlagsException(this, flags);
        }
        try {
            connection.setAutoCommit(false);
        } catch (final SQLException e) {
            throw new StartResourceFailedException(this, e);
        }
        this.xid = xid;
        status = STARTED;
    }

    @Override
    public void end(final Xid xid, final int flags) throws XAException {
        assertParameterNotNull("xid", xid);
        assertStarted();
        assertXid(xid);
        if (SUSPEND.equals(flags)) {
            status = SUSPENDED;
        } else {
            status = ENDED;
        }
    }

    @Override
    public int prepare(final Xid xid) throws XAException {
        assertParameterNotNull("xid", xid);
        assertEnded();
        assertXid(xid);
        status = PREPARED;
        return OK.intValue();
    }

    @Override
    public void commit(final Xid xid, final boolean onePhase)
            throws XAException {
        assertParameterNotNull("xid", xid);
        if (onePhase) {
            assertEnded();
        } else {
            assertPrepared();
        }
        assertXid(xid);
        try {
            connection.commit();
        } catch (final SQLException e) {
            status = UNKNOWN;
            throw new CommitResourceFailedException(this, e);
        }
        try {
            connection.setAutoCommit(true);
        } catch (final SQLException e) {
            status = UNKNOWN;
            throw new SetAutoCommitFailedException(this, e);
        }
        status = NO_TRANSACTION;
        this.xid = null;
    }

    @Override
    public void rollback(final Xid xid) throws XAException {
        assertParameterNotNull("xid", xid);
        assertEndedOrPrepared();
        assertXid(xid);
        try {
            connection.rollback();
        } catch (final SQLException e) {
            status = UNKNOWN;
            throw new RollbackResourceFailedException(this, e);
        }
        try {
            connection.setAutoCommit(true);
        } catch (final SQLException e) {
            status = UNKNOWN;
            throw new SetAutoCommitFailedException(this, e);
        }
        status = NO_TRANSACTION;
        this.xid = null;
    }

    @Override
    public void forget(final Xid xid) throws XAException {
    }

    @Override
    public Xid[] recover(final int flag) throws XAException {
        return new Xid[0];
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    @Override
    public boolean setTransactionTimeout(final int seconds) throws XAException {
        return false;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public String toString() {
        return super.toString() + "[status : <" + status + ">, xid : <" + xid
            + ">, connection : <" + connection + ">]";
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for internal
    //
    protected void assertNoTransaction() throws XAException {
        if (status == NO_TRANSACTION) {
            return;
        }
        throw new ResourceAlreadyStartedException(this);
    }

    protected void assertStarted() throws XAException {
        if (status == STARTED) {
            return;
        }
        throw new ResourceNotStartedException(this);
    }

    protected void assertSuspended() throws XAException {
        if (status == SUSPENDED) {
            return;
        }
        throw new ResourceNotSuspendedException(this);
    }

    protected void assertEnded() throws XAException {
        if (status == ENDED) {
            return;
        }
        throw new ResourceNotEndedException(this);
    }

    protected void assertPrepared() throws XAException {
        if (status == PREPARED) {
            return;
        }
        throw new ResourceNotPreparedException(this);
    }

    protected void assertEndedOrPrepared() throws XAException {
        if (status == ENDED || status == PREPARED) {
            return;
        }
        throw new ResourceNotEndedException(this);
    }

    protected void assertXid(Xid xid) throws XAException {
        if (xid.equals(this.xid)) {
            return;
        }
        throw new IllegalXidException(this, xid);
    }

}
