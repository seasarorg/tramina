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
package org.seasar.tramina.resource.jdbc.exception;

import java.sql.SQLException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import static org.seasar.tramina.resource.jdbc.JdbcResourceMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public class StartResourceFailedException extends XAException {

    /**
     * @param configurable
     */
    public StartResourceFailedException(final XAResource xaResource,
            final SQLException cause) {
        super(START_RESOURCE_FAILED.format(xaResource));
        errorCode = XAER_RMFAIL;
        initCause(cause);
    }

}