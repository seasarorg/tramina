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
package org.seasar.tramina.work.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.seasar.tramina.activity.impl.AbstractActiveComponent;
import org.seasar.tramina.logging.Logger;
import org.seasar.tramina.logging.LoggerFactory;
import org.seasar.tramina.spi.WorkManager;

import static org.seasar.tramina.activity.impl.ActivityAssertions.*;
import static org.seasar.tramina.work.WorkMessages.*;

/**
 * 作業をバックグラウンドで実行するサービスを提供するコンポーネントの実装です．
 * 
 * @author koichik
 */
public class WorkManagerImpl extends AbstractActiveComponent<WorkManagerImpl>
        implements WorkManager {

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** ロガー */
    private static final Logger logger =
        LoggerFactory.getLogger(WorkManagerImpl.class);

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    /** アイドルであってもスレッドプール内に維持されるスレッドの数 */
    protected int corePoolSize = 1;

    /** スレッドプール */
    protected ScheduledExecutorService service;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * インスタンスを構築します．
     */
    public WorkManagerImpl() {
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from WorkManager
    //
    @Override
    public <V> ScheduledFuture<V> schedule(final Callable<V> callable,
            final long delaySeconds) throws RejectedExecutionException {
        assertStarted(this);
        return service.schedule(callable, delaySeconds, TimeUnit.SECONDS);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command,
            final long initialDelay, final long period) {
        return service.scheduleAtFixedRate(
            command,
            initialDelay,
            period,
            TimeUnit.SECONDS);
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from AbstractActiveComponent
    //
    @Override
    protected void doInitialize() {
    }

    @Override
    protected void doStart() {
        service = Executors.newScheduledThreadPool(corePoolSize);
        if (logger.isDebugEnabled()) {
            logger.debug(WORK_MANAGER_STARTED.format(this));
        }
    }

    @Override
    protected void doStop() {
        service.shutdown();
        if (logger.isDebugEnabled()) {
            logger.debug(WORK_MANAGER_STOPPED.format(this));
        }
    }

    @Override
    protected void doDispose() {
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from Object
    //
    @Override
    public String toString() {
        return super.toString() + "[corePoolSize : <" + corePoolSize
            + ">, service : <" + service + ">]";
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for properties
    //
    /**
     * アイドルであってもスレッドプール内に維持されるスレッドの数を設定します．
     * 
     * @param corePoolSize
     *            アイドルであってもスレッドプール内に維持されるスレッドの数
     * @return このインスタンス自身
     */
    public WorkManagerImpl setCorePoolSize(final int corePoolSize) {
        assertBeforeInitialized(this);
        this.corePoolSize = corePoolSize;
        return this;
    }

}
