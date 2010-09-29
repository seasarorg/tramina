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
package org.seasar.tramina.unit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * 
 * 
 * @author koichik
 */
public class EasyMockRunner extends BlockJUnit4ClassRunner {

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    /** モックのリスト */
    protected final List<Object> mocks = new ArrayList<Object>();

    /**
     * @param clazz
     * @throws InitializationError
     */
    public EasyMockRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected Statement methodInvoker(final FrameworkMethod method,
            final Object test) {
        Statement statement = super.methodInvoker(method, test);
        statement = new ReplayAndVerify(statement, test);
        statement = recordMethodInvoker(statement, method, test);
        return new CreateMock(statement, test);
    }

    protected Statement recordMethodInvoker(final Statement next,
            final FrameworkMethod method, final Object test) {
        final String testMethodName = method.getName();
        for (final FrameworkMethod recordMethod : getTestClass()
            .getAnnotatedMethods(Record.class)) {
            for (final String targetMethodName : recordMethod.getAnnotation(
                Record.class).value()) {
                if (targetMethodName.equals(testMethodName)) {
                    return new InvokeRecordMethod(next, recordMethod, test);
                }
            }
            final String recordMethodName;
            if (testMethodName.startsWith("test")) {
                recordMethodName =
                    "record" + testMethodName.substring("test".length());
            } else {
                recordMethodName = "record" + capitalize(testMethodName);
            }
            if (recordMethod.getName().equals(recordMethodName)) {
                return new InvokeRecordMethod(next, recordMethod, test);
            }
        }
        return next;
    }

    protected <T> T createMock(final Class<T> clazz) {
        final T mock = org.easymock.classextension.EasyMock.createMock(clazz);
        mocks.add(mock);
        return mock;
    }

    protected <T> T createNiceMock(final Class<T> clazz) {
        final T mock =
            org.easymock.classextension.EasyMock.createNiceMock(clazz);
        mocks.add(mock);
        return mock;
    }

    protected <T> T createStrictMock(final Class<T> clazz) {
        final T mock =
            org.easymock.classextension.EasyMock.createStrictMock(clazz);
        mocks.add(mock);
        return mock;
    }

    protected void replay() {
        for (final Object mock : mocks) {
            org.easymock.classextension.EasyMock.replay(mock);
        }
    }

    protected void verify() {
        for (final Object mock : mocks) {
            org.easymock.classextension.EasyMock.verify(mock);
        }
    }

    protected void reset() {
        for (final Object mock : mocks) {
            org.easymock.classextension.EasyMock.reset(mock);
        }
    }

    /**
     * 文字列をキャピタライズして返します．
     * 
     * @param s
     *            文字列
     * @return キャピタライズした文字列
     */
    protected static String capitalize(final String s) {
        final int first = s.codePointAt(0);
        if (Character.isLowerCase(first)) {
            return new String(new StringBuilder(s.length()).appendCodePoint(
                Character.toUpperCase(first)).append(
                s.substring(Character.toChars(first).length)));
        }
        return s;
    }

    public class CreateMock extends Statement {

        protected final Statement next;

        protected final Object target;

        /**
         * @param next
         * @param target
         */
        private CreateMock(final Statement next, final Object target) {
            this.next = next;
            this.target = target;
        }

        @Override
        public void evaluate() throws Throwable {
            bindMockFields();
            next.evaluate();
        }

        protected void bindMockFields() throws Throwable {
            for (Class<?> clazz = target.getClass(); clazz != Object.class; clazz =
                clazz.getSuperclass()) {
                for (final Field field : clazz.getDeclaredFields()) {
                    bindMockField(field);
                }
            }
        }

        protected void bindMockField(final Field field) throws Throwable {
            final EasyMock annotation = field.getAnnotation(EasyMock.class);
            if (annotation == null) {
                return;
            }
            field.setAccessible(true);
            if (field.get(target) != null) {
                return;
            }
            switch (annotation.value()) {
            case STRICT:
                field.set(target, createStrictMock(field.getType()));
                break;
            case NICE:
                field.set(target, createNiceMock(field.getType()));
                break;
            default:
                field.set(target, createMock(field.getType()));
                break;
            }
        }

    }

    public class InvokeRecordMethod extends Statement {

        protected final Statement next;

        protected final FrameworkMethod recordMethod;

        protected final Object target;

        /**
         * @param next
         * @param recordMethod
         * @param target
         */
        private InvokeRecordMethod(final Statement next,
                final FrameworkMethod recordMethod, final Object target) {
            this.next = next;
            this.recordMethod = recordMethod;
            this.target = target;
        }

        @Override
        public void evaluate() throws Throwable {
            recordMethod.invokeExplosively(target);
            next.evaluate();
        }

    }

    public class ReplayAndVerify extends Statement {

        protected final Statement next;

        protected final Object target;

        /**
         * @param next
         * @param target
         */
        private ReplayAndVerify(final Statement next, final Object target) {
            this.next = next;
            this.target = target;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                replay();
                next.evaluate();
                verify();
            } finally {
                reset();
            }
        }

    }

}
