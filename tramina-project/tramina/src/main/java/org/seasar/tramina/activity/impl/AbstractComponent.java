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
package org.seasar.tramina.activity.impl;

import org.seasar.tramina.activity.ActivityStatus;
import org.seasar.tramina.activity.Component;
import org.seasar.tramina.activity.Disposable;
import org.seasar.tramina.activity.Initializable;
import org.seasar.tramina.activity.exception.AlreadyDisposedException;
import org.seasar.tramina.activity.exception.AlreadyInitializedException;
import org.seasar.tramina.activity.exception.NotInitializedException;

import static org.seasar.tramina.activity.ActivityStatus.*;
import static org.seasar.tramina.activity.impl.ActivityAssertions.*;

/**
 * 
 * 
 * @author koichik
 */
public abstract class AbstractComponent<T extends AbstractComponent<T>>
        implements Component, Initializable<T>, Disposable<T> {

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    /** アクティビティステータス */
    protected volatile ActivityStatus activityStatus = CONSTRUCTED;

    // /////////////////////////////////////////////////////////////////
    // instance methods from Component
    //
    public ActivityStatus getActivityStatus() {
        return activityStatus;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Configurable
    //
    @Override
    public T initialize() throws AlreadyInitializedException {
        assertBeforeInitialized(this);
        doInitialize();
        activityStatus = INITIALIZED;
        @SuppressWarnings("unchecked")
        final T self = (T) this;
        return self;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Disposable
    //
    @Override
    public T dispose() throws NotInitializedException, AlreadyDisposedException {
        assertInitialized(this);
        assertBeforeDisposed(this);
        doDispose();
        activityStatus = DISPOSED;
        @SuppressWarnings("unchecked")
        final T self = (T) this;
        return self;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for internal
    //

    protected abstract void doInitialize();

    protected abstract void doDispose();

}
