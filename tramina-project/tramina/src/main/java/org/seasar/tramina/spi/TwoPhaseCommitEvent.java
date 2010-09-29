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
package org.seasar.tramina.spi;

import java.util.EventObject;

import javax.transaction.xa.Xid;

/**
 * 
 * 
 * @author koichik
 */
public class TwoPhaseCommitEvent extends EventObject {

    protected final Xid xid;

    protected final TransactionStatusType status;

    /**
     * @param source
     */
    public TwoPhaseCommitEvent(final TraminaTransaction source, final Xid xid,
            final TransactionStatusType status) {
        super(source);
        this.xid = xid;
        this.status = status;
    }

    public TraminaTransaction getTransaction() {
        return (TraminaTransaction) getSource();
    }

    /**
     * @return the xid
     */
    public Xid getXid() {
        return xid;
    }

    /**
     * @return the status
     */
    public TransactionStatusType getStatus() {
        return status;
    }

}
