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
package org.seasar.tramina.assertion;

import java.util.Locale;

import org.seasar.tramina.util.EnumMessage;
import org.seasar.tramina.util.EnumMessageFormatter;

/**
 * 
 * 
 * @author koichik
 */
public enum AssertionMessages implements EnumMessage {

    // /////////////////////////////////////////////////////////////////
    // enum constants
    //
    /** */
    PARAMETER_MUST_NOT_NULL("Parameter {0} must not be null.",
            "パラメータ {0} は null であってはいけません．"),
    /** */
    PARAMETER_MUST_NOT_EMPTY_STRING("Parameter {0} must not be empty string.",
            "パラメータ {0} は空の文字列であってはいけません．"),
    /** */
    PARAMETER_MUST_NOT_EMPTY_ARRAY("Paramter {0} must not be empty array.",
            "パラメータ {0} は空の配列であってはいけません．"),
    /** */
    PARAMETER_MUST_NOT_EMPTY_COLLECTION(
            "Paramter {0} must not be empty Collection.",
            "パラメータ {0} は空のコレクションであってはいけません．"),
    /** */
    PARAMETER_MUST_NOT_EMPTY_MAP("Paramter {0} must not be empty Map.",
            "パラメータ {0} は空のマップであってはいけません．"),

    /** */
    PROPERTY_MUST_NOT_NULL("Property {1} must not be null. component={0}",
            "プロパティ {1} は null であってはいけません．component={0}"),
    /** */
    PROPERTY_MUST_NOT_EMPTY_STRING(
            "Property {1} must not be empty string. component={0}",
            "プロパティ {1} は空の文字列であってはいけません．component={0}"),
    /** */
    PROPERTY_MUST_NOT_EMPTY_ARRAY(
            "Property {1} must not be empty array. component={0}",
            "プロパティ {1} は空の配列であってはいけません．component={0}"),
    /** */
    PROPERTY_MUST_NOT_EMPTY_COLLECTION(
            "Property {1} must not be empty Collection. component={0}",
            "プロパティ {1} は空のコレクションであってはいけません．component={0}"),
    /** */
    PROPERTY_MUST_NOT_EMPTY_MAP(
            "Property {1} must not be empty Map. component={0}",
            "プロパティ {1} は空のマップであってはいけません．component={0}"),
    /** */
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
    private AssertionMessages(final String... messagePatterns) {
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
