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

import org.seasar.tramina.activity.Component;
import org.seasar.tramina.activity.exception.AlreadyDisposedException;
import org.seasar.tramina.activity.exception.AlreadyInitializedException;
import org.seasar.tramina.activity.exception.AlreadyStartedException;
import org.seasar.tramina.activity.exception.AlreadyStoppedException;
import org.seasar.tramina.activity.exception.NotDisposedException;
import org.seasar.tramina.activity.exception.NotInitializedException;
import org.seasar.tramina.activity.exception.NotStartedException;
import org.seasar.tramina.activity.exception.NotStoppedException;

/**
 * 
 * 
 * @author koichik
 */
public class ActivityAssertions {

    // /////////////////////////////////////////////////////////////////
    // static methods
    //
    public static void assertBeforeInitialized(final Component component) {
        switch (component.getActivityStatus()) {
        case CONSTRUCTED:
            break;
        default:
            throw new AlreadyInitializedException(component);
        }
    }

    public static void assertInitialized(final Component component) {
        switch (component.getActivityStatus()) {
        case CONSTRUCTED:
            throw new NotInitializedException(component);
        case DISPOSED:
            throw new AlreadyDisposedException(component);
        default:
            break;
        }
    }

    public static void assertBeforeStarted(final Component component) {
        switch (component.getActivityStatus()) {
        case CONSTRUCTED:
        case INITIALIZED:
            break;
        default:
            throw new AlreadyStartedException(component);
        }
    }

    public static void assertStarted(final Component component) {
        switch (component.getActivityStatus()) {
        case CONSTRUCTED:
        case INITIALIZED:
            throw new NotStartedException(component);
        case STARTED:
            break;
        case STOPPED:
            throw new AlreadyStoppedException(component);
        default:
            break;
        }
    }

    public static void assertStopped(final Component component) {
        switch (component.getActivityStatus()) {
        case CONSTRUCTED:
        case INITIALIZED:
            throw new NotStartedException(component);
        case STARTED:
            throw new NotStoppedException(component);
        default:
            break;
        }
    }

    public static void assertBeforeDisposed(final Component component) {
        switch (component.getActivityStatus()) {
        case DISPOSED:
            throw new AlreadyDisposedException(component);
        default:
            break;
        }
    }

    public static void assertDisposed(final Component component) {
        switch (component.getActivityStatus()) {
        case DISPOSED:
            break;
        default:
            throw new NotDisposedException(component);
        }
    }

}
