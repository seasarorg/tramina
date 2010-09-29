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
package org.seasar.tramina.resource.jdbc;

import java.sql.Connection;

/**
 * 
 * 
 * @author koichik
 */
public enum TransactionIsolationType {

    // /////////////////////////////////////////////////////////////////
    // enum constants
    //
    /** */
    TRANSACTION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    /** */
    TRANSACTION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    /** */
    TRANSACTION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    /** */
    TRANSACTION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    private int intValue;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param intValue
     */
    private TransactionIsolationType(final int intValue) {
        this.intValue = intValue;
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