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

import java.util.Arrays;

import javax.transaction.xa.Xid;

/**
 * 
 * 
 * @author koichik
 */
public class XidImpl implements Xid {

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final int formatId;

    protected final byte[] globalTransactionId;

    protected final byte[] branchQualifier;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param formatId
     * @param globalTransactionId
     * @param branchQualifier
     */
    public XidImpl(final int formatId, final byte[] globalTransactionId,
            final byte[] branchQualifier) {
        this.formatId = formatId;
        this.globalTransactionId = globalTransactionId;
        this.branchQualifier = branchQualifier;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Xid
    //
    @Override
    public byte[] getBranchQualifier() {
        return branchQualifier;
    }

    @Override
    public byte[] getGlobalTransactionId() {
        return globalTransactionId;
    }

    @Override
    public int getFormatId() {
        return formatId;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public int hashCode() {
        return formatId + Arrays.hashCode(globalTransactionId)
            + Arrays.hashCode(branchQualifier);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Xid)) {
            return false;
        }
        final Xid other = (Xid) obj;
        if (other.getFormatId() != formatId) {
            return false;
        }
        if (!Arrays.equals(other.getGlobalTransactionId(), globalTransactionId)) {
            return false;
        }
        if (!Arrays.equals(other.getBranchQualifier(), branchQualifier)) {
            return false;
        }
        return true;
    }

}
