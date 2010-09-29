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
package org.seasar.tramina.transaction;

import java.util.Locale;

import org.seasar.tramina.util.EnumMessage;
import org.seasar.tramina.util.EnumMessageFormatter;

/**
 * 
 * 
 * @author koichik
 */
public enum TransactionMessages implements EnumMessage {

    // /////////////////////////////////////////////////////////////////
    // enum constants
    //
    /** */
    TOPLEVEL_TRANSACTION_BEGAN("", "トップレベルトランザクションを開始しました．transaction={0}"),
    /** */
    TOPLEVEL_TRANSACTION_COMMITTED("",
            "トップレベルトランザクションをコミットしました．transaction={0}"),
    /** */
    TOPLEVEL_TRANSACTION_ROLLEDBACK("",
            "トップレベルトランザクションをロールバックしました．transaction={0}"),
    /** */
    TOPLEVEL_TRANSACTION_SUSPENDED("", "トップレベルトランザクションを中断しました．transaction={0}"),
    /** */
    TOPLEVEL_TRANSACTION_RESUMED("", "トップレベルトランザクションを再開しました．transaction={0}"),
    /** */
    TOPLEVEL_TRANSACTION_MARKED_ROLLBACK("",
            "トップレベルトランザクションがロールバックするようにマークされました．transaction={0}"),

    /** */
    SUBTRANSACTION_BEGAN("", "サブトランザクションを開始しました．transaction={0}, parent={1}"),
    /** */
    SUBTRANSACTION_COMMITTED("", "サブトランザクションをコミットしました．transaction={0}"),
    /** */
    SUBTRANSACTION_ROLLEDBACK("", "サブトランザクションをロールバックしました．transaction={0}"),
    /** */
    SUBTRANSACTION_SUSPENDED("", "サブトランザクションを中断しました．transaction={0}"),
    /** */
    SUBTRANSACTION_RESUMED("", "サブトランザクションを再開しました．transaction={0}"),
    /** */
    SUBTRANSACTION_MARKED_ROLLBACK("",
            "サブトランザクションがロールバックするようにマークされました．transaction={0}"),
    /** */
    PARENT_MARK_ROLLBACK_ONLY("",
            "サブトランザクションを開始できなかったので親トランザクションをロールバックするようにマークします．transaction={0}, parent={1}"),

    /** */
    TRANSACTION_NOT_YET_ASSOCIATED("", "現在のスレッドでトランザクションが開始されていません．"),
    /** */
    TRANSACTION_ALREADY_ASSOCIATED("", "現在のスレッドでトランザクションが開始済みです．"),
    /** */
    INVALID_TRAMINA_TRANSACTION("",
            "Traminaのトランザクション実装クラスではありません．transaction={0}"),

    /** */
    TRANSACTION_NOT_ACTIVE("", "トランザクションはアクティブではありません．transaction={0}"),
    /** */
    TRANSACTION_ALREADY_BEGAN("", "トランザクションは既に開始済みです．transaction={0}"),
    /** */
    TRANSACTION_NOT_SUSPEENDED("", "トランザクションは中断中ではありません．transaction={0}"),
    /** */
    TRANSACTION_SUSPEENDED("", "トランザクションは中断中です．transaction={0}"),
    /** */
    TRANSACTION_MARKED_ROLLBACK("",
            "トランザクションはロールバックするようにマークされています．transaction={0}"),
    /** */
    SUBTRANSACTION_ACTIVE("",
            "サブトランザクションがアクティブです．transaction={0}, subtransaction={1}"),
    /** */
    INVALID_SUBTRANSACTION("",
            "サブトランザクションはこのトランザクションの直接の子ではありません．transaction={0}, subtransaction={1}"),
    /** */
    NOT_CURRENT_TRANSACTION("",
            "現在のスレッドに関連づけられたトランザクションではありません．transaction={0}, currentTransaction={1}"),

    /** */
    SYNCHRONIZATION_RAISED_EXCEPTION("",
            "トランザクションシンクロナイゼーションが例外をスローしました．syncronization={0}"),
    /** */
    TWO_PHASE_LISTENER_RAISED_EXCEPTION("",
            "2フェーズコミットイベントリスナが例外をスローしました．listener={0}"),

    /** */
    UNEXPECTED_EXCEPTION_OCCURRED_IN_COMMIT_PROCESS("",
            "トランザクションのコミット処理中に例外が発生しました．transaction={0}"),
    /** */
    UNEXPECTED_EXCEPTION_OCCURRED_IN_ROLLBACK_PROCESS("",
            "トランザクションのロールバック処理中に例外が発生しました．transaction={0}"),

    /** */
    START_TRANSACTION_FAILED("",
            "XAリソースがトランザクションを開始できませんでした．xid={0}, xaResource={1}"),
    /** */
    JOIN_TRANSACTION_FAILED("",
            "XAリソースがトランザクションに合流できませんでした．xid={0}, xaResource={1}"),
    /** */
    RESUME_TRANSACTION_FAILED("",
            "XAリソースがトランザクションを再開できませんでした．xid={0}, xaResource={1}"),
    /** */
    END_TRANSACTION_FAILED("",
            "XAリソースがトランザクションを終了できませんでした．xid={0}, xaResource={1}"),
    /** */
    SUSPEND_TRANSACTION_FAILED("",
            "XAリソースがトランザクションを中断できませんでした．xid={0}, xaResource={1}"),
    /** */
    PREPARE_TRANSACTION_FAILED("",
            "XAリソースがトランザクションをコミット準備できませんでした．xid={0}, xaResource={1}"),
    /** */
    COMMIT_TRANSACTION_FAILED("",
            "XAリソースがトランザクションをコミットできませんでした．xid={0}, xaResource={1}"),
    /** */
    ROLLBACK_TRANSACTION_FAILED("",
            "XAリソースがトランザクションをロールバックできませんでした．xid={0}, xaResource={1}"),
    /** */
    CANNOT_COMMIT_BECOME_INDOUBT_TRANSACTION(
            "",
            "2フェーズコミットの準備フェーズでコミット可能を返したリソースをコミットできませんでした．インダウトトランザクションになった可能性があります．xid={0}, xaResource={1}"),
    /** */
    CANNOT_ROLLBACK_BECOME_INDOUBT_TRANSACTION(
            "",
            "2フェーズコミットの準備フェーズでコミット可能を返したリソースをロールバックできませんでした．インダウトトランザクションになった可能性があります．xid={0}, xaResource={1}"),

    /** */
    BEGIN_SUBTRANSACTION_FAILED("",
            "XAリソースがサブトランザクションを開始できませんでした．transaction={0}, xaResource={1}"),
    /** */
    COMMIT_SUBTRANSACTION_FAILED("",
            "XAリソースがサブトランザクションをコミットできませんでした．transaction={0}, xaResource={1}"),
    /** */
    ROLLBACK_SUBTRANSACTION_FAILED("",
            "XAリソースがサブトランザクションをロールバックできませんでした．transaction={0}, xaResource={1}"),

    /** */
    LAST_RESOURCE_ALREADY_ENLISTED(
            "",
            "別のラストリソースが既にトランザクションに参加しています．トランザクションに参加できるラストリソースリソースは一つだけです．transaction={0}, xaResource={1}, lastResource={2}"),

    /** */
    IS_SAME_RESOURCE_FAILED("",
            "XAResource が同一リソースか比較できませんでした．xaResource1={0}, xaResource2={1}"),

    /** */
    UNEXPECTED_EXCEPTION_OCCURRED("", "予期しない例外が発生しました．transaction={0}"),
    /* */
    ;

    // /////////////////////////////////////////////////////////////////
    // static fields
    //
    /** サポートするロケールの配列 */
    public static final Locale[] SUPPORTED_LOCALES =
        new Locale[] { Locale.ROOT, Locale.JAPANESE };

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    private final String[] messagePatterns;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * インスタンスを構築します．
     * 
     * @param messagePatterns
     *            {@link #SUPPORTED_LOCALES}配列のロケールに応じたメッセージパターンの配列
     */
    private TransactionMessages(final String... messagePatterns) {
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
