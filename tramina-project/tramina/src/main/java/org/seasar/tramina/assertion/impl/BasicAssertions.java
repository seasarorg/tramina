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
package org.seasar.tramina.assertion.impl;

import java.util.Collection;
import java.util.Map;

import org.seasar.tramina.assertion.exception.ParameterMustNotEmptyArrayException;
import org.seasar.tramina.assertion.exception.ParameterMustNotEmptyCollectionException;
import org.seasar.tramina.assertion.exception.ParameterMustNotEmptyMapException;
import org.seasar.tramina.assertion.exception.ParameterMustNotEmptyStringException;
import org.seasar.tramina.assertion.exception.ParameterMustNotNullException;
import org.seasar.tramina.assertion.exception.PropertyMustNotEmptyArrayException;
import org.seasar.tramina.assertion.exception.PropertyMustNotEmptyCollectionException;
import org.seasar.tramina.assertion.exception.PropertyMustNotEmptyMapException;
import org.seasar.tramina.assertion.exception.PropertyMustNotEmptyStringException;
import org.seasar.tramina.assertion.exception.PropertyMustNotNullException;

/**
 * 
 * 
 * @author koichik
 */
public class BasicAssertions {

    // /////////////////////////////////////////////////////////////////
    // static methods
    //
    public static void assertParameterNotNull(final String parameterName,
            final Object parameter) {
        if (parameter == null) {
            throw new ParameterMustNotNullException(parameterName);
        }
    }

    public static void assertParameterNotEmptyString(
            final String parameterName, final String parameter) {
        assertParameterNotNull(parameterName, parameter);
        if (parameter.isEmpty()) {
            throw new ParameterMustNotEmptyStringException(parameterName);
        }
    }

    public static void assertParameterNotEmptyArray(final String parameterName,
            final Object[] parameter) {
        assertParameterNotNull(parameterName, parameter);
        if (parameter.length == 0) {
            throw new ParameterMustNotEmptyArrayException(parameterName);
        }
    }

    public static void assertParameterNotEmptyCollection(
            final String parameterName, final Collection<?> parameter) {
        assertParameterNotNull(parameterName, parameter);
        if (parameter.size() == 0) {
            throw new ParameterMustNotEmptyCollectionException(parameterName);
        }
    }

    public static void assertParameterNotEmptyMap(final String parameterName,
            final Map<?, ?> parameter) {
        assertParameterNotNull(parameterName, parameter);
        if (parameter.size() == 0) {
            throw new ParameterMustNotEmptyMapException(parameterName);
        }
    }

    public static void assertPropertyNotNull(final Object component,
            final String propertyName, final Object property) {
        if (property == null) {
            throw new PropertyMustNotNullException(component, propertyName);
        }
    }

    public static void assertPropertyNotEmptyString(final Object component,
            final String propertyName, final String property) {
        assertPropertyNotNull(component, propertyName, property);
        if (property.isEmpty()) {
            throw new PropertyMustNotEmptyStringException(
                component,
                propertyName);
        }
    }

    public static void assertPropertyNotEmptyArray(final Object component,
            final String propertyName, final Object[] property) {
        assertPropertyNotNull(component, propertyName, property);
        if (property.length == 0) {
            throw new PropertyMustNotEmptyArrayException(
                component,
                propertyName);
        }
    }

    public static void assertPropertyNotEmptyCollection(final Object component,
            final String propertyName, final Collection<?> property) {
        assertPropertyNotNull(component, propertyName, property);
        if (property.size() == 0) {
            throw new PropertyMustNotEmptyCollectionException(
                component,
                propertyName);
        }
    }

    public static void assertPropertyNotEmptyMap(final Object component,
            final String propertyName, final Map<?, ?> property) {
        assertPropertyNotNull(component, propertyName, property);
        if (property.size() == 0) {
            throw new PropertyMustNotEmptyMapException(component, propertyName);
        }
    }

}
