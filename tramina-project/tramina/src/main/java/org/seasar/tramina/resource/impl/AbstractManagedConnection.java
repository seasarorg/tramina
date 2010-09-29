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
package org.seasar.tramina.resource.impl;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.resource.ManagedConnection;
import org.seasar.tramina.resource.ManagedConnectionEvent;
import org.seasar.tramina.resource.ManagedConnectionEventListener;

import static org.seasar.tramina.resource.ResourceMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public abstract class AbstractManagedConnection<C, E extends Exception>
        implements ManagedConnection<C, E> {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(AbstractManagedConnection.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected Set<ManagedConnectionEventListener<C, E>> listeners =
        new CopyOnWriteArraySet<ManagedConnectionEventListener<C, E>>();

    // /////////////////////////////////////////////////////////////////
    // instance methods from ManagedConnection
    //
    @Override
    public void addManagedConnectionEventListener(
            final ManagedConnectionEventListener<C, E> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeManagedConnectionEventListener(
            final ManagedConnectionEventListener<C, E> listener) {
        listeners.remove(listener);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for subclasses
    //
    protected void fireLogicalConnectionClosed(
            final ManagedConnectionEvent<C, E> event) {
        for (final ManagedConnectionEventListener<C, E> listener : listeners) {
            try {
                listener.logicalConnectionClosed(event);
            } catch (final Exception e) {
                logger.error(MANAGED_CONNECTION_EVENT_LISTENER_RAISED_EXCEPTION
                    .format(this, listener), e);
            }
        }
    }

    protected void fireConnectionErrorOccurred(
            final ManagedConnectionEvent<C, E> event) {
        for (final ManagedConnectionEventListener<C, E> listener : listeners) {
            try {
                listener.connectionErrorOccurred(event);
            } catch (final Exception e) {
                logger.error(MANAGED_CONNECTION_EVENT_LISTENER_RAISED_EXCEPTION
                    .format(this, listener), e);
            }
        }
    }

}
