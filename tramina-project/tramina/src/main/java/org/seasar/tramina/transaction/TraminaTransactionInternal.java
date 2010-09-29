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
package org.seasar.tramina.transaction;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.seasar.tramina.spi.Subtransaction;
import org.seasar.tramina.spi.TraminaTransaction;

/**
 * 
 * 
 * @author koichik
 */
public interface TraminaTransactionInternal extends TraminaTransaction {

    void begin() throws SystemException;

    void suspend() throws SystemException;

    void resume() throws IllegalStateException, SystemException;

    void onSuspended() throws SystemException;

    void onResumeed() throws SystemException;

    TraminaTransactionInternal createSubtransaction() throws SystemException;

    void onSubtransactionBegan(Subtransaction subtransaction)
            throws SystemException;

    void onSubtransactionCommitted(Subtransaction subtransaction,
            SameResources[] takeOverResources,
            Synchronization[] takeOverSynchronizations,
            Synchronization[] takeOverInterposedSynchronizations)
            throws SystemException;

    void onSubtransactionCommitted(Subtransaction subtransaction)
            throws SystemException;

    void onSubtransactionRolledBack(Subtransaction subtransaction)
            throws SystemException;

}
