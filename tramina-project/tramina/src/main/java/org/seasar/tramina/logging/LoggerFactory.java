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
package org.seasar.tramina.logging;

import org.seasar.tramina.logging.impl.JulLoggerFactory;
import org.seasar.tramina.logging.impl.Slf4jLoggerFactory;

import static org.seasar.tramina.assertion.impl.BasicAssertions.*;

/**
 * 
 * 
 * @author koichik
 */
public class LoggerFactory {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    protected static final LoggerFactoryInternal loggerFactory =
        getLoggerFactory();

    // /////////////////////////////////////////////////////////////////
    // static methods
    //
    public static Logger getLogger(final Class<?> clazz) {
        assertParameterNotNull("clazz", clazz);
        return loggerFactory.getLogger(clazz);
    }

    // /////////////////////////////////////////////////////////////////
    // static methods for internal
    //
    protected static LoggerFactoryInternal getLoggerFactory() {
        try {
            Class.forName("org.slf4j.Logger");
            return new Slf4jLoggerFactory();
        } catch (final Exception ignore) {
        }
        return new JulLoggerFactory();
    }

}
