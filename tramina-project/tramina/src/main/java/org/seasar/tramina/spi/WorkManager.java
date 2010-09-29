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
package org.seasar.tramina.spi;

import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * 作業をバックグラウンドで実行するサービスを提供するコンポーネントです．
 * 
 * @author koichik
 */
public interface WorkManager {

    /**
     * 指定された遅延後に有効になる {@link ScheduledFuture} を作成して実行します．
     * 
     * @param <V>
     *            戻り値の型
     * @param callable
     *            実行する関数
     * @param delaySeconds
     *            現在から遅延実行までの時間 (秒単位)
     * @return 結果を抽出または取り消すために使用できる {@link ScheduledFuture}
     * @throws RejectedExecutionException
     *             タスクの実行をスケジュールできない場合
     * @see ScheduledExecutorService#schedule(Callable, long,
     *      java.util.concurrent.TimeUnit)
     */
    <V> ScheduledFuture<V> schedule(Callable<V> callable, long delaySeconds)
            throws RejectedExecutionException;

    /**
     * 指定された初期遅延の経過後にはじめて有効になり，その後は指定された期間ごとに有効になる定期的なアクションを作成して実行します．
     * 
     * @param command
     *            実行するタスク
     * @param initialDelay
     *            最初の遅延実行までの時間 (秒単位)
     * @param period
     *            連続する実行の間隔 (秒単位)
     * @return タスクの保留状態の完了を表す {@link ScheduledFuture}．その
     *         {@link ScheduledFuture#get()} メソッドは取り消し時に例外をスローする
     * @see ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long,
     *      java.util.concurrent.TimeUnit)
     */
    ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay,
            long period);
}
