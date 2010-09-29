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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.transaction.xa.Xid;

import org.seasar.tramina.spi.TraminaXid;
import org.seasar.tramina.util.XidUtil;

/**
 * 
 * 
 * @author koichik
 */
public class TraminaXidImpl implements TraminaXid {

    // /////////////////////////////////////////////////////////////////
    // constants
    //
    protected static final int FORMAT_ID =
        't' << 24 | 'r' << 16 | 'a' << 8 | 'm';

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    protected static final UUID uuid = UUID.randomUUID();

    protected static final AtomicLong sequenceGenerator = new AtomicLong();

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final long domainId;

    protected final long globalTransactionId;

    protected final long branchQualifier;

    protected final byte[] globalTransactionIdBytes;

    protected final byte[] branchQualifierBytes;

    protected long nextBranchQualifier;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * 
     */
    public TraminaXidImpl(final long domainId) {
        this(domainId, sequenceGenerator.incrementAndGet(), 0L);
    }

    /**
     * @param globalTransactionId
     * @param branchQualifier
     */
    protected TraminaXidImpl(final long domainId,
            final long globalTransactionId, final long branchQualifier) {
        this.domainId = domainId;
        this.globalTransactionId = globalTransactionId;
        this.branchQualifier = branchQualifier;
        globalTransactionIdBytes = createGlobalTransactionIdBytes();
        branchQualifierBytes = createBranchQualifierBytes();
    }

    // /////////////////////////////////////////////////////////////////
    // static methods
    //
    public static boolean isTraminaXid(final Xid xid) {
        return xid.getFormatId() == FORMAT_ID;
    }

    public static long getDomainId(final Xid xid) {
        final ByteBuffer buffer = ByteBuffer.wrap(xid.getGlobalTransactionId());
        return buffer.getLong();
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from TraminaXid
    //
    public TraminaXidImpl createNewBranch() {
        return new TraminaXidImpl(
            domainId,
            globalTransactionId,
            ++nextBranchQualifier);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Xid
    //
    @Override
    public int getFormatId() {
        return FORMAT_ID;
    }

    @Override
    public byte[] getGlobalTransactionId() {
        return globalTransactionIdBytes;
    }

    @Override
    public byte[] getBranchQualifier() {
        return branchQualifierBytes;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public int hashCode() {
        return (int) globalTransactionId;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Xid)) {
            return false;
        }
        final Xid xid = (Xid) obj;
        if (FORMAT_ID != xid.getFormatId()) {
            return false;
        }
        if (!Arrays.equals(globalTransactionIdBytes, xid
            .getGlobalTransactionId())) {
            return false;
        }
        if (!Arrays.equals(branchQualifierBytes, xid.getBranchQualifier())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return XidUtil.toString(this);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for internal
    //
    protected byte[] createGlobalTransactionIdBytes() {
        final byte[] bytes = new byte[MAXGTRIDSIZE];
        final ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.putLong(domainId);
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
        buf.putLong(MAXGTRIDSIZE - 8, globalTransactionId);
        return bytes;
    }

    protected byte[] createBranchQualifierBytes() {
        final byte[] bytes = new byte[MAXBQUALSIZE];
        final ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.putLong(MAXBQUALSIZE - 8, branchQualifier);
        return bytes;
    }

}
