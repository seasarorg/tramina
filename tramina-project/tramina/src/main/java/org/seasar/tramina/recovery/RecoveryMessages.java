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
package org.seasar.tramina.recovery;

import java.util.Locale;

import org.seasar.tramina.util.EnumMessage;
import org.seasar.tramina.util.EnumMessageFormatter;

/**
 * 
 * 
 * @author koichik
 */
public enum RecoveryMessages implements EnumMessage {

    // /////////////////////////////////////////////////////////////////
    // enum constants
    //
    ACQUIRE_INDOUBT_TRANSACTION_FAILED("",
            "インダウトトランザクションを取得できませんでした．recoveryManager={0}"),
    NOT_MANAGED_XID("",
            "Xidはこのトランザクションマネージャの対象外のためスキップします．transactionManager={0}, xid={1}"),
    COMMITTING_INDOUBT_TRANSACTION("", "インダウトトランザクションをコミットします．xid={0}"),
    INDOUBT_TRANSACTION_COMMITTED("", "インダウトトランザクションをコミットしました．xid={0}"),
    COMMIT_INDOUBT_TRANSACTION_FAILED("", "インダウトトランザクションをコミットできませんでした．xid={0}"),
    INDOUBT_TRANSACTION_HEURISTIC_ROLLEDBACK(
            "",
            "トランザクションマネージャはコミットを決定しましたが，インダウトトランザクションの一部または全部がヒューリスティックにロールバックされた可能性があります．xid={0}"),
    ROLLINGBACK_INDOUBT_TRANSACTION("", "インダウトトランザクションをロールバックします．xid={0}"),
    INDOUBT_TRANSACTION_ROLLEDBACK("", "インダウトトランザクションをロールバックしました．xid={0}"),
    ROLLBACK_INDOUBT_TRANSACTION_FAILED("",
            "インダウトトランザクションをロールバックできませんでした．xid={0}"),
    INDOUBT_TRANSACTION_HEURISTIC_COMMITTED(
            "",
            "トランザクションマネージャはロールバックを決定しましたが，インダウトトランザクションの一部または全部がヒューリスティックにコミットされた可能性があります．xid={0}"),
    UNKNOWN_INDOUBT_TRANSACTION("", "インダウトトランザクションの結果が不明です．xid={0}"),

    TRANSACTION_LOG_MANAGER_STARTED(
            "TransactionLogManager is started. transactionLogManager={0}",
            "TransactionLogManagerを開始しました．transactionLogManager={0}"),
    TRANSACTION_LOG_MANAGER_STOPPED(
            "TransactionLogManager is stopped. transactionLogManager={0}",
            "TransactionLogManagerを終了しました．transactionLogManager={0}"),
    TRANSACTION_LOG_TABLE_INITIALIZATION_FAILED("",
            "トランザクションログテーブルを初期化できませんでした．transactionLogManager={0}"),
    ACQUIRE_CONNECTION_FAILED("",
            "トランザクションログを挿入するためのコネクションが取得できませんでした．transactionLogManager={0}"),
    TRANSACTION_LOG_INSERTED("",
            "トランザクションログレコードを挿入しました．transactionLogManager={0}, transaction={1}"),
    INSERT_TRANSACTION_LOG_FAILED("",
            "トランザクションログレコードを挿入できませんでした．transactionLogManager={0}, transaction={1}"),
    EXPIRED_TRANSACTION_LOG_DELETED("",
            "期限が過ぎたトランザクションログレコードを{1}件削除しました．transactionLogManager={0}"),
    DELETE_EXPIRED_TRANSACTION_LOG_FAILED("",
            "期限を過ぎたトランザクションログレコードの削除ができませんでした．transactionLogManager={0}"),
    ACQUIRE_TRANSACTION_RESULT_FAILED("",
            "トランザクションログレコードを読み込めませんでした．transactionLogManager={0}"),
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
    private RecoveryMessages(final String... messagePatterns) {
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
