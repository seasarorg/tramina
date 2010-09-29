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

/**
 * メッセージパターンを定義した列挙が実装するインタフェースです．
 * <p>
 * メッセージパターンを定義した列挙は，それぞれの列挙定数にロケールごとのメッセージパターンを持ちます． メッセージパターンは，
 * {@link MessageFormat} で扱える形式の文字列です． サポートするメッセージのロケールは {@code
 * SUPORTED_LOCALES} という名前の {@code static} フィールドに {@link Locale} の配列として定義します．
 * </p>
 * 
 * <pre>
 * public static final Locale[] SUPORTED_LOCALES = new Locale[] { Locale.ROOT, Locale.JAPANESE };
 * </pre>
 * <p>
 * {@code SUPPORTED_LOCALES} 配列には {@link Locale#ROOT} を含めるべきです．
 * {@link #getMessagePattern(int)} の引数には， {@code SUPPORTED_LOCALES}
 * 配列のインデックスがロケールとして渡されます．
 * </p>
 * <p>
 * 典型的な列挙の定義は次のようになります．
 * </p>
 * 
 * <pre>
 * public enum TestMessages implements MessageEnum {
 *     // 第 2 引数はルートロケールのメッセージ，第 3 引数は日本語ロケールのメッセージ
 *     XXXX(Level.ERROR,   "Error",   "エラー"),
 *     YYYY(Level.WARNING, "Warning", "警告"),
 *     ...
 *     ;
 * 
 *     &#x2F;** サポートするロケールの配列 *&#x2F;
 *     public static final Locale[] SUPPORTED_LOCALES = new Locale[] {
 *             Locale.ROOT, Locale.JAPANESE };
 * 
 *     &#x2F;** メッセージパターンの配列 *&#x2F;
 *     private final String[] messagePatterns;
 * 
 *     &#x2F;**
 *      * インスタンスを構築します．
 *      * 配列の要素は SUPPORTED_LOCALES 配列のロケールに対応するメッセージパターンです．
 *      * 
 *      * &#x40;param messagePatterns
 *      *            メッセージパターンの配列
 *      *&#x2F;
 *     private TestMessages(final String... messagePatterns) {
 *         this.messagePatterns = messagePatterns;
 *     }
 * 
 *     &#x2F;**
 *      * 指定されたロケールのメッセージパターンを返します．
 *      * 
 *      * &#x40;param locale
 *      *            ロケール
 *      * &#x40;return 指定されたロケールのメッセージパターン
 *      *&#x2F;
 *     public String getMessagePattern(final int locale) {
 *         return messageFormats[locale];
 *     }
 * }
 * </pre>
 * 
 * @author koichik
 */
public interface EnumMessage {

    // /////////////////////////////////////////////////////////////////
    // constants
    //
    /** サポートするロケールの配列を定義した定数の名前 */
    String SUPPORTED_LOCALES_NAME = "SUPPORTED_LOCALES";

    // /////////////////////////////////////////////////////////////////
    // instance methods
    //
    /**
     * 指定されたロケールのメッセージパターンを返します．
     * 
     * @param locale
     *            ロケールのインデックス
     * @return 指定されたロケールのメッセージパターン
     */
    String getMessagePattern(int locale);

    String format(Object... args);

    String format(Locale locale, Object... args);

}
