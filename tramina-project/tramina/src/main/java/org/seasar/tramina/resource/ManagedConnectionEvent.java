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
package org.seasar.tramina.resource;

import java.util.EventObject;

/**
 * 
 * 
 * @author koichik
 */
public class ManagedConnectionEvent<C, E extends Exception> extends EventObject {

    protected final E cause;

    /**
     * @param source
     */
    public ManagedConnectionEvent(final ManagedConnection<C, E> source) {
        this(source, null);
    }

    public ManagedConnectionEvent(final ManagedConnection<C, E> source,
            final E cause) {
        super(source);
        this.cause = cause;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ManagedConnection<C, E> getSource() {
        return (ManagedConnection<C, E>) super.getSource();
    }

    /**
     * @return the cause
     */
    public E getCause() {
        return cause;
    }

}
