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
package org.seasar.tramina.resource.jdbc;

import java.util.Locale;

import org.seasar.tramina.util.EnumMessage;
import org.seasar.tramina.util.EnumMessageFormatter;

/**
 * 
 * 
 * @author koichik
 */
public enum JdbcResourceMessages implements EnumMessage {

    // /////////////////////////////////////////////////////////////////
    // enum constants
    //
    UNSUPPORTED_GET_CONNECTION_WITH_USER(
            "",
            "ユーザ・パスワードを指定した getConnection() はサポートされていません．method=getConnection(String, String)"),
    UNSUPPORTED_OPERATION("", "指定の操作はサポートされていません．method={0}"),
    INTERFACE_NOT_IMPLEMENTED("",
            "リソースマネージャは指定のインタフェースを実装していません．resourceManager={0}, interface={1}"),

    LOGICAL_CONNECTION_OPENED(
            "Logical connection was opened. managedConnection={0}",
            "論理コネクションをオープンしました．managedConnection={0}"),
    LOGICAL_CONNECTION_CLOSED(
            "Logical connection was closed. managedConnection={0}",
            "論理コネクションをクローズしました．managedConnection={0}"),
    PHYSICAL_CONNECTION_CLOSED(
            "Physical connection was closed. logicalConnection={0}",
            "物理コネクションはクローズ済みです．logicalConnection={0}"),
    METHOD_NOT_FOUND("Method was not found. class={0}, method={1}",
            "メソッドが見つかりませんでした．class={0}, method={1}"),

    TIMEOUT_WAITING_MANAGED_CONNECTION(
            "Timeout waiting for managed connection from connection pool.",
            "コネクションプールからマネージドコネクションの取得中にタイムアウトしました．"),

    MANAGED_CONNECTION_PURGED(
            "Managed connection was purged. managedConnection={0}",
            "マネージドコネクションを破棄しました．managedConnection={0}"),

    TWO_PHASE_COMMIT_NOT_SUPPORTED("",
            "ラストリソースでは2フェーズコミットはサポートされていません．xaResource={0}, method={1}"),

    SET_SAVEPOINT_FAILED("", "セーブポイントの設定で例外が発生しました．xaResource={0}"),
    ROLLBACK_SAVEPOINT_FAILED("", "セーブポイントのロールバックで例外が発生しました．xaResource={0}"),
    START_RESOURCE_FAILED("", "トランザクションの開始で例外が発生しました．xaResource={0}"),
    COMMIT_RESOURCE_FAILED("", "トランザクションのコミットで例外が発生しました．xaResource={0}"),
    ROLLBACK_RESOURCE_FAILED("", "トランザクションのロールバックで例外が発生しました．xaResource={0}"),

    RESOURCE_ALREADY_STARTED("", "リソースは既に開始されています．xaResource={0}"),
    RESOURCE_NOT_STARTED("", "リソースが開始されていません．xaResource={0}"),
    RESOURCE_NOT_SUSPENDED("", "リソースが中断されていません．xaResource={0}"),
    RESOURCE_NOT_ENDED("", "リソースが終了されていません．xaResource={0}"),
    RESOURCE_NOT_PREPARED("", "リソースがコミット準備されていません．xaResource={0}"),
    ILLEGAL_FLAGS("", "不正なフラグです．xaResource={0}, flags={1}"),
    ILLEGAL_XID("", "不正なXidです．xaResource={0}, xid={1}"),
    SET_AUTO_COMMIT_FAILED("", "コネクションを自動コミットモードに設定できませんでした．xaResource={0}"),
    SUBTRANSACTION_NOT_STARTED("", "サブトランザクションが開始されていません．xaResource={0}"),
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
    private JdbcResourceMessages(final String... messagePatterns) {
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
