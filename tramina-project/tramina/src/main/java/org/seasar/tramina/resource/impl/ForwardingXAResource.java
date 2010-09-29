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

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * 
 * 
 * @author koichik
 */
public class ForwardingXAResource implements XAResource {

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final XAResource delegate;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param delegate
     */
    public ForwardingXAResource(final XAResource delegate) {
        this.delegate = delegate;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from XAResource
    //
    @Override
    public void commit(final Xid xid, final boolean onePhase)
            throws XAException {
        delegate.commit(xid, onePhase);
    }

    @Override
    public void end(final Xid xid, final int flags) throws XAException {
        delegate.end(xid, flags);
    }

    @Override
    public void forget(final Xid xid) throws XAException {
        delegate.forget(xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return delegate.getTransactionTimeout();
    }

    @Override
    public boolean isSameRM(final XAResource xares) throws XAException {
        return delegate.isSameRM(xares);
    }

    @Override
    public int prepare(final Xid xid) throws XAException {
        return delegate.prepare(xid);
    }

    @Override
    public Xid[] recover(final int flag) throws XAException {
        return delegate.recover(flag);
    }

    @Override
    public void rollback(final Xid xid) throws XAException {
        delegate.rollback(xid);
    }

    @Override
    public boolean setTransactionTimeout(final int seconds) throws XAException {
        return delegate.setTransactionTimeout(seconds);
    }

    @Override
    public void start(final Xid xid, final int flags) throws XAException {
        delegate.start(xid, flags);
    }

}
