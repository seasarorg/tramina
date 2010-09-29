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

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.seasar.tramina.resource.jdbc.exception.TwoPhaseCommitNotSupportedException;
import org.seasar.tramina.spi.LastXAResource;

/**
 * 
 * 
 * @author koichik
 */
public class SubtransactionAwareLastXAResourceImpl extends
        SubtransactionAwareXAResourceImpl implements LastXAResource {

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param connection
     */
    public SubtransactionAwareLastXAResourceImpl(final Connection connection) {
        super(connection);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from XAResource
    //
    @Override
    public int prepare(final Xid xid) throws XAException {
        throw new TwoPhaseCommitNotSupportedException(this, "prepare");
    }

    @Override
    public void commit(final Xid xid, final boolean onePhase)
            throws XAException {
        if (!onePhase) {
            throw new TwoPhaseCommitNotSupportedException(this, "commit");
        }
        super.commit(xid, onePhase);
    }

}
