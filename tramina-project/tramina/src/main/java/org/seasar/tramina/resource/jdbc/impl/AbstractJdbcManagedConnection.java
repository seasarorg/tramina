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
package org.seasar.tramina.resource.jdbc.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.xa.XAResource;

import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.resource.ManagedConnectionEvent;
import org.seasar.tramina.resource.impl.AbstractManagedConnection;
import org.seasar.tramina.resource.jdbc.JdbcManagedConnection;
import org.seasar.tramina.resource.jdbc.LogicalConnection;
import org.seasar.tramina.resource.jdbc.XAResourceFactory;
import org.seasar.tramina.resource.jdbc.exception.NoSuchMethodRuntimeException;
import org.seasar.tramina.resource.jdbc.exception.PhysicalConnectionClosedException;

import static org.seasar.tramina.assertion.impl.BasicAssertions.*;
import static org.seasar.tramina.resource.jdbc.JdbcResourceMessages.*;

/**
 * 
 * 
 * @author koichik
 */
public abstract class AbstractJdbcManagedConnection extends
        AbstractManagedConnection<Connection, SQLException> implements
        JdbcManagedConnection {

    // /////////////////////////////////////////////////////////////////
    // constants
    //
    protected static final Method CLEANUP =
        getMethod(LogicalConnection.class, "cleanup");

    protected static final Method CLOSE = getMethod(Connection.class, "close");

    protected static final Method IS_CLOSED =
        getMethod(Connection.class, "isClosed");

    protected static final Method IS_WRAPPER_FOR =
        getMethod(Connection.class, "isWrapperFor", Class.class);

    protected static final Method UNWRAP =
        getMethod(Connection.class, "unwrap", Class.class);

    protected static final Method TO_STRING =
        getMethod(Object.class, "toString");

    protected static final Method HASH_CODE =
        getMethod(Object.class, "hashCode");

    protected static final Method EQUALS =
        getMethod(Object.class, "equals", Object.class);

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(AbstractJdbcManagedConnection.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected final XAResourceFactory xaResourceFactory;

    protected LogicalConnection logicalConnection;

    protected XAResource xaResource;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * @param xaResource
     */
    protected AbstractJdbcManagedConnection(
            final XAResourceFactory xaResourceFactory) {
        assertParameterNotNull("xaResourceFactory", xaResourceFactory);
        this.xaResourceFactory = xaResourceFactory;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from ManagedConnection
    //
    @Override
    public Connection getLogicalConnection() throws SQLException {
        if (logicalConnection == null) {
            logicalConnection = createLogicalConnection();
            if (logger.isDebugEnabled()) {
                logger.debug(LOGICAL_CONNECTION_OPENED.format(this));
            }
        }
        return logicalConnection;
    }

    // /////////////////////////////////////////////////////////////////
    // static methods for internal
    //
    protected static Method getMethod(final Class<?> clazz, final String name,
            final Class<?>... paramTypes) {
        try {
            return clazz.getMethod(name, paramTypes);
        } catch (final Exception e) {
            throw new NoSuchMethodRuntimeException(clazz, name, e);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for internaal
    //
    protected LogicalConnection createLogicalConnection() throws SQLException {
        return (LogicalConnection) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] { LogicalConnection.class },
            new LogicalConnectionInvocationHandler(getPhysicalConnection()));
    }

    // /////////////////////////////////////////////////////////////////
    // member types
    //
    protected class LogicalConnectionInvocationHandler implements
            InvocationHandler {

        // /////////////////////////////////////////////////////////////////
        // instance fields
        //
        protected Connection physicalConnection;

        protected final Map<Method, InvocationHandler> handlers =
            new HashMap<Method, InvocationHandler>();
        {
            handlers.put(CLEANUP, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method,
                        final Object[] args) throws Throwable {
                    physicalConnection = null;
                    return null;
                }
            });
            handlers.put(CLOSE, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method,
                        final Object[] args) throws Throwable {
                    if (physicalConnection != null) {
                        fireLogicalConnectionClosed(new ManagedConnectionEvent<Connection, SQLException>(
                            AbstractJdbcManagedConnection.this));
                    }
                    return null;
                }
            });
            handlers.put(IS_CLOSED, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method,
                        final Object[] args) throws Throwable {
                    if (physicalConnection == null) {
                        return Boolean.TRUE;
                    }
                    return method.invoke(physicalConnection, args);
                }
            });
            handlers.put(IS_WRAPPER_FOR, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method,
                        final Object[] args) throws Throwable {
                    if (args[0] == LogicalConnection.class) {
                        return Boolean.TRUE;
                    } else if (((Class<?>) args[0])
                        .isInstance(physicalConnection)) {
                        return Boolean.TRUE;
                    }
                    return method.invoke(physicalConnection, args);
                }
            });
            handlers.put(UNWRAP, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method,
                        final Object[] args) throws Throwable {
                    if (args[0] == LogicalConnection.class) {
                        return proxy;
                    } else if (((Class<?>) args[0])
                        .isInstance(physicalConnection)) {
                        return physicalConnection;
                    }
                    return method.invoke(physicalConnection, args);
                }
            });
            handlers.put(TO_STRING, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method,
                        final Object[] args) throws Throwable {
                    return LogicalConnectionInvocationHandler.this.toString();
                }
            });
            handlers.put(HASH_CODE, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method,
                        final Object[] args) throws Throwable {
                    return super.hashCode();
                }
            });
            handlers.put(EQUALS, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method,
                        final Object[] args) throws Throwable {
                    return super.equals(args[0]);
                }
            });
        }

        // /////////////////////////////////////////////////////////////////
        // constructors
        //
        /**
         * @param physicalConnection
         */
        public LogicalConnectionInvocationHandler(
                final Connection physicalConnection) {
            this.physicalConnection = physicalConnection;
        }

        // /////////////////////////////////////////////////////////////////
        // instance methods from InvocationHandler
        //
        @Override
        public Object invoke(final Object proxy, final Method method,
                final Object[] args) throws Throwable {
            final InvocationHandler handler = handlers.get(method);
            if (handler != null) {
                return handler.invoke(proxy, method, args);
            }
            if (physicalConnection == null) {
                throw new PhysicalConnectionClosedException(this);
            }
            return method.invoke(physicalConnection, args);
        }

        // /////////////////////////////////////////////////////////////////
        // instance methods from Object
        //
        @Override
        public String toString() {
            return super.toString() + "[closed : <"
                + (physicalConnection == null) + ">]";
        }

    }

}
