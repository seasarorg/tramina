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

import org.seasar.tramina.activity.impl.AbstractComponent;
import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.resource.ConnectionManager;
import org.seasar.tramina.resource.ManagedConnection;
import org.seasar.tramina.resource.ResourceManager;

import static org.seasar.tramina.activity.impl.ActivityAssertions.*;
import static org.seasar.tramina.assertion.impl.BasicAssertions.*;

/**
 * 
 * 
 * @author koichik
 */
public abstract class AbstractConnectionManager<CM extends AbstractConnectionManager<CM, C, E>, C, E extends Exception>
        extends AbstractComponent<CM> implements ConnectionManager<C, E> {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(AbstractConnectionManager.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected ResourceManager<?, E> resourceManager;

    protected ConnectionManager<C, E> next;

    // /////////////////////////////////////////////////////////////////
    // instance methods from ConnectionManager
    //
    @Override
    public void setResourceManager(ResourceManager<?, E> resourceManager) {
        assertInitialized(this);
        assertParameterNotNull("resourceManager", resourceManager);
        this.resourceManager = resourceManager;
    }

    @Override
    public void setNextConnectionManager(final ConnectionManager<C, E> next) {
        assertInitialized(this);
        assertParameterNotNull("next", next);
        this.next = next;
    }

    @Override
    public ManagedConnection<C, E> getManagedConnection() throws E {
        assertInitialized(this);
        return next.getManagedConnection();
    }

    @Override
    public void logicalConnectionClosed(
            final ManagedConnection<C, E> managedConnection) throws E {
        assertInitialized(this);
        assertParameterNotNull("managedConnection", managedConnection);
        next.logicalConnectionClosed(managedConnection);
    }

    @Override
    public void physicalConnectionErrorOccurred(
            final ManagedConnection<C, E> managedConnection, final E cause)
            throws E {
        assertInitialized(this);
        assertParameterNotNull("managedConnection", managedConnection);
        assertParameterNotNull("cause", cause);
        next.physicalConnectionErrorOccurred(managedConnection, cause);
    }

    @Override
    public void destroy() throws E {
        dispose();
        next.destroy();
    }

}
