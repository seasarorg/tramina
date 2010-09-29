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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static java.util.Collections.*;
import static org.seasar.tramina.util.EnumMessage.*;

/**
 * メッセージを定義した列挙を使用するリソースバンドルです．
 * <p>
 * フォーマット文字列を定義した列挙は {@link EnumMessage} を実装していなければなりません．
 * </p>
 * 
 * @author koichik
 * @param <T>
 *            メッセージを定義した列挙の型
 */
public class EnumMessageResourceBundle<T extends Enum<T> & EnumMessage> extends
        ResourceBundle {

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    /** メッセージを定義した列挙の型に対応する{@link ResourceBundle}のマップ */
    protected static final Map<Class<?>, ResourceBundle> bundles =
        new HashMap<Class<?>, ResourceBundle>();

    /** メッセージを定義した列挙の型 */
    protected final Class<T> enumClass;

    /** ロケール */
    protected final int locale;

    // /////////////////////////////////////////////////////////////////
    // constructors
    //
    /**
     * インスタンスを構築します．
     * 
     * @param enumClass
     *            メッセージを定義した列挙の型
     * @param locale
     *            ロケール
     */
    public EnumMessageResourceBundle(final Class<T> enumClass, final int locale) {
        this.enumClass = enumClass;
        this.locale = locale;
    }

    // /////////////////////////////////////////////////////////////////
    // static methods
    //
    /**
     * メッセージを定義した列挙を使用するリソースバンドルを返します．
     * 
     * @param <T>
     *            メッセージのパターンを定義した列挙の型
     * @param enumMessage
     *            メッセージのパターンを定義した列挙の型
     * @return リソースバンドル
     * @throws IllegalStateException
     *             列挙に {@literal SUPPORTED_LOCALES} が定義されていない場合
     * @see EnumMessage
     */
    public static <T extends Enum<T> & EnumMessage> ResourceBundle getBundle(
            final T enumMessage) throws IllegalStateException {
        @SuppressWarnings("unchecked")
        final Class<T> enumClass = (Class<T>) enumMessage.getClass();
        if (bundles.containsKey(enumClass)) {
            return bundles.get(enumClass);
        }
        final ResourceBundle resourceBundle =
            ResourceBundle.getBundle(
                enumClass.getName(),
                new EnumMessageResourceBundleControl<T>(enumClass));
        bundles.put(enumClass, resourceBundle);
        return resourceBundle;
    }

    /**
     * メッセージを定義した列挙を使用するリソースバンドルを返します．
     * 
     * @param <T>
     *            メッセージのパターンを定義した列挙の型
     * @param enumMessage
     *            メッセージのパターンを定義した列挙の型
     * @param locale
     *            ロケール
     * @return リソースバンドル
     * @throws IllegalStateException
     *             列挙に {@code SUPPORTED_LOCALES} が定義されていない場合
     * @see EnumMessage
     */
    public static <T extends Enum<T> & EnumMessage> ResourceBundle getBundle(
            final T enumMessage, final Locale locale)
            throws IllegalStateException {
        @SuppressWarnings("unchecked")
        final Class<T> enumClass = (Class<T>) enumMessage.getClass();
        return ResourceBundle.getBundle(
            enumClass.getName(),
            locale,
            new EnumMessageResourceBundleControl<T>(enumClass));
    }

    /**
     * メッセージを定義した列挙を使用するリソースバンドルを返します．
     * <p>
     * フォールバックロケールは使用しません．
     * </p>
     * 
     * @param <T>
     *            メッセージを定義した列挙の型
     * @param enumClass
     *            メッセージを定義した列挙の型
     * @return リソースバンドル
     * @see java.util.ResourceBundle.Control#getNoFallbackControl(List)
     */
    public static <T extends Enum<T> & EnumMessage> ResourceBundle getBundleNoFallback(
            final Class<T> enumClass) {
        return ResourceBundle.getBundle(
            enumClass.getName(),
            new EnumMessageResourceBundleControl<T>(enumClass, false));
    }

    /**
     * メッセージを定義した列挙を使用するリソースバンドルを返します．
     * <p>
     * フォールバックロケールは使用しません．
     * </p>
     * 
     * @param <T>
     *            メッセージを定義した列挙の型
     * @param enumClass
     *            メッセージを定義した列挙の型
     * @param locale
     *            ロケール
     * @return リソースバンドル
     * @see java.util.ResourceBundle.Control#getNoFallbackControl(List)
     */
    public static <T extends Enum<T> & EnumMessage> ResourceBundle getBundleNoFallback(
            final Class<T> enumClass, final Locale locale) {
        return ResourceBundle.getBundle(
            enumClass.getName(),
            locale,
            new EnumMessageResourceBundleControl<T>(enumClass, false));
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods from ResourceBundle
    //
    @Override
    public Enumeration<String> getKeys() {
        final Set<String> keys = handleKeySet();
        if (parent != null) {
            for (final Enumeration<String> it = parent.getKeys(); it
                .hasMoreElements();) {
                final String key = it.nextElement();
                keys.add(key);
            }
        }
        return enumeration(keys);
    }

    @Override
    protected Set<String> handleKeySet() {
        final T[] codes = enumClass.getEnumConstants();
        final Set<String> keys = new HashSet<String>(codes.length);
        for (final T code : codes) {
            if (code.getMessagePattern(locale) != null) {
                keys.add(code.name());
            }
        }
        return keys;
    }

    @Override
    protected Object handleGetObject(final String key) {
        try {
            final T code = Enum.valueOf(enumClass, key);
            return code.getMessagePattern(locale);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    // /////////////////////////////////////////////////////////////////
    // member types
    //
    /**
     * {@link EnumMessageResourceBundle} を作成する
     * {@link java.util.ResourceBundle.Control} です．
     * 
     * @author koichik
     * @param <T>
     *            メッセージを定義した列挙の型
     */
    static class EnumMessageResourceBundleControl<T extends Enum<T> & EnumMessage>
            extends Control {

        // /////////////////////////////////////////////////////////////////
        // instance fields
        //
        /** メッセージコードを定義した列挙の型 */
        protected final Class<T> enumClass;

        /**
         * フォールバックしない場合は {@literal true}
         * 
         * @see java.util.ResourceBundle.Control#getFallbackLocale(String,
         *      Locale)
         */
        protected final boolean noFallback;

        protected final Locale[] supportedLocales;

        // /////////////////////////////////////////////////////////////////
        // constructors
        //
        /**
         * インスタンスを構築します．
         * 
         * @param enumClass
         *            メッセージを定義した列挙の型
         */
        public EnumMessageResourceBundleControl(final Class<T> enumClass) {
            this(enumClass, false);
        }

        /**
         * インスタンスを構築します．
         * 
         * @param enumClass
         *            メッセージを定義した列挙の型
         * @param noFallback
         *            フォールバックしない場合は {@literal true}
         * @see java.util.ResourceBundle.Control#getFallbackLocale(String,
         *      Locale)
         */
        public EnumMessageResourceBundleControl(final Class<T> enumClass,
                final boolean noFallback) {
            this.enumClass = enumClass;
            this.noFallback = noFallback;
            this.supportedLocales = getSupportedLocales(enumClass);
        }

        // /////////////////////////////////////////////////////////////////
        // instance methods from Control
        //
        @Override
        public List<String> getFormats(final String baseName) {
            return FORMAT_CLASS;
        }

        @Override
        public ResourceBundle newBundle(final String baseName,
                final Locale locale, final String format,
                final ClassLoader loader, final boolean reload)
                throws IllegalAccessException, InstantiationException,
                IOException {
            for (int i = 0; i < supportedLocales.length; ++i) {
                if (supportedLocales[i].equals(locale)) {
                    return new EnumMessageResourceBundle<T>(enumClass, i);
                }
            }
            return null;
        }

        @Override
        public Locale getFallbackLocale(final String baseName,
                final Locale locale) {
            return null;
        }

        // /////////////////////////////////////////////////////////////////
        // instance methods for internal
        //
        protected Locale[] getSupportedLocales(final Class<T> enumClass) {
            try {
                final Field field =
                    enumClass.getDeclaredField(SUPPORTED_LOCALES_NAME);
                field.setAccessible(true);
                if (field != null) {
                    return (Locale[]) field.get(null);
                }
            } catch (final Exception ignore) {
            }
            throw new IllegalStateException(enumClass.getName() + " must have "
                + SUPPORTED_LOCALES_NAME + " field.");
        }

    }

}
