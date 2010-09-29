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

import javax.transaction.SystemException;

import org.seasar.tramina.spi.TraminaTransactionManager;
import org.seasar.tramina.spi.TwoPhaseCommitEvent;

/**
 * 
 * 
 * @author koichik
 */
public interface TraminaTransactionManagerInternal extends
        TraminaTransactionManager {

    long getDomainId();

    void associate(TraminaTransactionInternal tx);

    void dissociate();

    void fireBeforeTwoPhaseCommit(TwoPhaseCommitEvent event)
            throws SystemException;

    void fireBeforeLastCommit(TwoPhaseCommitEvent event) throws SystemException;

    void fireAfterLastCommit(TwoPhaseCommitEvent event) throws SystemException;

    void fireAfterTwoPhaseCommit(TwoPhaseCommitEvent event)
            throws SystemException;

}
