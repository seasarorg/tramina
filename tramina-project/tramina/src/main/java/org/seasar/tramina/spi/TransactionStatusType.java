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

import static javax.transaction.Status.*;

/**
 * 
 * 
 * @author koichik
 */
public enum TransactionStatusType {

    // /////////////////////////////////////////////////////////////////
    // enum constants
    //
    /** */
    ACTIVE(STATUS_ACTIVE),
    /** */
    MARKED_ROLLBACK(STATUS_MARKED_ROLLBACK),
    /** */
    PREPARED(STATUS_PREPARED),
    /** */
    COMMITTED(STATUS_COMMITTED),
    /** */
    ROLLEDBACK(STATUS_ROLLEDBACK),
    /** */
    UNKNOWN(STATUS_UNKNOWN),
    /** */
    NO_TRANSACTION(STATUS_NO_TRANSACTION),
    /** */
    PREPARING(STATUS_PREPARING),
    /** */
    COMMITTING(STATUS_COMMITTING),
    /** */
    ROLLING_BACK(STATUS_ROLLING_BACK);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    private final int intValue;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param intValue
     */
    private TransactionStatusType(final int intValue) {
        this.intValue = intValue;
    }

    // /////////////////////////////////////////////////////////////////
    // static methods
    //
    public static TransactionStatusType getStatusType(final int status) {
        return TransactionStatusType.class.getEnumConstants()[status];
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods
    //
    public int intValue() {
        return intValue;
    }

    public boolean equals(final int intValue) {
        return this.intValue == intValue;
    }

}
