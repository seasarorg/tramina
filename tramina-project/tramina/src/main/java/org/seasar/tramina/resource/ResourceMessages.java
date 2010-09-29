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

import java.util.Locale;

import org.seasar.tramina.util.EnumMessage;
import org.seasar.tramina.util.EnumMessageFormatter;

/**
 * 
 * 
 * @author koichik
 */
public enum ResourceMessages implements EnumMessage {

    // /////////////////////////////////////////////////////////////////
    // enum constants
    //
    PHYSICAL_CONNECTION_OPENED(
            "Physical connection was opened. managedConnection={0}, resourceManager={1}",
            "物理コネクションをオープンしました．managedConnection={0}, resourceManager={1}"),
    PHYSICAL_CONNECTION_CLOSED(
            "Physical connection was closed. managedConnection={0}, resourceManager={1}",
            "物理コネクションをクローズしました．managedConnection={0}, resourceManager={1}"),
    CLEANUP_MANAGED_CONNECTION_FAILED("",
            "論理コネクションのクリーンアップで例外が発生しました．managedConnection={0}, resourceManager={1}"),
    DESTROY_MANAGED_CONNECTION_FAILED("",
            "物理コネクションの破棄で例外が発生しました．managedConnection={0}, resourceManager={1}"),
    RECOVERY_INDOUBT_TRANSACTION_FAILED("",
            "インダウトトランザクションを回復できませんでした．resourceManager={0}"),

    MANAGED_CONNECTION_EVENT_LISTENER_RAISED_EXCEPTION("",
            "マネージドコネクションのイベントリスナが例外をスローしました．managedConnection={0}, listener={1}"),

    CLOSE_LOGICAL_CONNECTION_FAILED("",
            "論理コネクションのクローズで例外が発生しました．managedConnection={0}"),

    OBTAIN_MANAGED_CONNECTION_FROM_POOL("",
            "物理コネクションをコネクションプールから取り出しました．managedConnection={0}, connectionPool={1}"),
    RETURN_MANAGED_CONNECTION_TO_POOL("",
            "物理コネクションをコネクションプールに入れました．managedConnection={0}, connectionPool={1}"),
    INTERRUPTED("", "空きコネクションを待機中に割り込みが発生しました．"),
    SCHEDULE_MANAGED_CONNECTION_FAILED("",
            "コネクションのタイムアウト監視で例外が発生しました．managedConnection={0}"),

    VALIDATING_MANAGED_CONNECTION("",
            "物理コネクションを検証します．managedConnection={0}, connectionPool={1}"),
    INVALID_MANAGED_CONNECTION("",
            "物理コネクションが無効なので破棄します．managedConnection={0}, connectionPool={1}"),

    MANAGED_CONNECTION_BOUND_TRANSACTION("",
            "コネクションをトランザクションに関連づけました．managedConnection={0}, transaction={1}"),
    MANAGED_CONNECTION_UNBOUND_TRANSACTION("",
            "コネクションをトランザクションから切り離しました．managedConnection={0}, transaction={1}"),
    TRANSACTION_UNAVAILABLE("",
            "現在のトランザクションを取得する際に例外が発生しました．transactionManager={0}"),
    ENLIST_RESOURCE_FAILED("",
            "トランザクションにXAResourceを登録する際に例外が発生しました．managedConnection={0}, transaction={1}"),
    REGISTER_SYNCHRONIZATION_FAILED(
            "",
            "トランザクションにSynchronizationを登録する際に例外が発生しました．managedConnection={0}, transaction={1}"),
    /* */
    ;

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    public static final Locale[] SUPPORTED_LOCALES =
        new Locale[] { Locale.ROOT, Locale.JAPANESE };

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    private final String[] messagePatterns;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    private ResourceMessages(final String... messagePatterns) {
        this.messagePatterns = messagePatterns;
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from EnumMessage
    //
    @Override
    public String getMessagePattern(final int locale) {
        return messagePatterns[locale];
    }

    @Override
    public String format(final Object... args) {
        return EnumMessageFormatter.format(this, args);
    }

    @Override
    public String format(final Locale locale, final Object... args) {
        return EnumMessageFormatter.format(this, locale, args);
    }

}
