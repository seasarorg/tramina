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
package org.seasar.tramina.recovery.exception;

import javax.transaction.SystemException;

import org.seasar.tramina.recovery.TransactionLogManager;
import org.seasar.tramina.spi.TraminaTransaction;

import static org.seasar.tramina.recovery.RecoveryMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public class AcquireConnectionFailedException extends SystemException {

    /**
     * @param message
     * @param cause
     */
    public AcquireConnectionFailedException(
            final TransactionLogManager transactionLogManager,
            final TraminaTransaction transaction, final Throwable cause) {
        super(ACQUIRE_CONNECTION_FAILED.format(
            transactionLogManager,
            transaction));
        initCause(cause);
    }

}
