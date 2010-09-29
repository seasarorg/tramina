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
package org.seasar.tramina.activity;

import java.util.Locale;

import org.seasar.tramina.util.EnumMessage;
import org.seasar.tramina.util.EnumMessageFormatter;

/**
 * 
 * 
 * @author koichik
 */
public enum ActivityMessages implements EnumMessage {

    // /////////////////////////////////////////////////////////////////
    // enum constants
    //
    /** */
    NOT_INITIALIZED(
            "Component is not yet initialized, call initialized() method. component={0}",
            "コンポーネントはまだ初期化されていません．initialize()メソッドを呼び出してください．component={0}"),
    /** */
    ALREADY_CONFIGURED(
            "Component is already configured, could not change configuration. component={0}",
            "コンポーネントは既に構成済みです．設定を変更することはできません．component={0}"),
    /** */
    NOT_STARTED("Component is not yet started. component={0}",
            "コンポーネントはまだ開始されていません．component={0}"),
    /** */
    ALREADY_STARTED("Component is already started. component={0}",
            "コンポーネントは既に開始済みです．component={0}"),
    /** */
    NOT_STOPPED("Component is not yet stopped. component={0}",
            "コンポーネントは停止されていません．component={0}"),
    /** */
    ALREADY_STOPPED("Component is already stopped. component={0}",
            "コンポーネントは既に停止済みです．component={0}"),
    /** */
    NOT_DISPOSED("Component is not yet disposed. component={0}",
            "コンポーネントはまだ破棄されていません．component={0}"),
    /** */
    ALREADY_DISPOSED("Component is already disposed. component={0}",
            "コンポーネントは既に破棄済みです．component={0}"),
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
    private ActivityMessages(final String... messagePatterns) {
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
