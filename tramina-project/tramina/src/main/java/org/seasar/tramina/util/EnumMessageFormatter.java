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
package org.seasar.tramina.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 列挙に定義された {@link MessageFormat} のパターンを使用してメッセージを組み立てるクラスです．
 * <p>
 * パターンを定義した列挙は {@link EnumMessage} を実装していなければなりません．
 * </p>
 * 
 * @author koichik
 */
public class EnumMessageFormatter {

    // /////////////////////////////////////////////////////////////////
    // static methods
    //
    /**
     * 列挙に定義されたデフォルトロケールのパターンに引数を適用してフォーマットした文字列を返します．
     * 
     * @param enumMessage
     *            列挙
     * @param args
     *            パターンに適用する引数の並び
     * @return 列挙に定義されたパターン
     * @param <T>
     *            パターンを定義した列挙の型
     */
    public static <T extends Enum<T> & EnumMessage> String format(
            final T enumMessage, final Object... args) {
        final ResourceBundle bundle =
            EnumMessageResourceBundle.getBundle(enumMessage);
        final String pattern = bundle.getString(enumMessage.name());
        return MessageFormat.format(pattern, args);
    }

    /**
     * 列挙に定義された指定されたロケールのパターンに引数を適用してフォーマットした文字列を返します．
     * 
     * @param enumMessage
     *            列挙
     * @param locale
     *            ロケール
     * @param args
     *            パターンに適用する引数の並び
     * @return 列挙に定義されたパターン
     * @param <T>
     *            パターンを定義した列挙の型
     */
    public static <T extends Enum<T> & EnumMessage> String format(
            final T enumMessage, final Locale locale, final Object... args) {
        final ResourceBundle bundle =
            EnumMessageResourceBundle.getBundle(enumMessage, locale);
        final String pattern = bundle.getString(enumMessage.name());
        return MessageFormat.format(pattern, args);
    }

}
