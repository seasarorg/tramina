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
package org.seasar.tramina.transaction.exception;

import javax.transaction.RollbackException;

import org.seasar.tramina.spi.Subtransaction;

import static org.seasar.tramina.transaction.TransactionMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public class SubtransactionRolledbackException extends RollbackException {

    /**
     * @param configurable
     */
    public SubtransactionRolledbackException(final Subtransaction tx,
            final Throwable cause) {
        super(SUBTRANSACTION_ROLLEDBACK.format(tx));
        initCause(cause);
    }

}
