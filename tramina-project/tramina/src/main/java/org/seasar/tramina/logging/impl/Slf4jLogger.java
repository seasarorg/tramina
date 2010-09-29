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
package org.seasar.tramina.logging.impl;

import org.seasar.tramina.logging.Logger;

/**
 * 
 * 
 * @author koichik
 */
public class Slf4jLogger implements Logger {

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final String sourceClass;

    protected final org.slf4j.Logger logger;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param logger
     */
    public Slf4jLogger(final Class<?> clazz) {
        sourceClass = clazz.getName();
        logger = org.slf4j.LoggerFactory.getLogger(clazz);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Logger
    //
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(final String message) {
        logger.error(message);
    }

    @Override
    public void error(final String message, final Throwable t) {
        logger.error(message, t);
    }

    @Override
    public boolean isWarningEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warning(final String message) {
        logger.warn(message);
    }

    @Override
    public void warning(final String message, final Throwable t) {
        logger.warn(message, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(final String message) {
        logger.info(message);
    }

    @Override
    public void info(final String message, final Throwable t) {
        logger.info(message, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(final String message) {
        logger.debug(message);
    }

    @Override
    public void debug(final String message, final Throwable t) {
        logger.debug(message, t);
    }

}
