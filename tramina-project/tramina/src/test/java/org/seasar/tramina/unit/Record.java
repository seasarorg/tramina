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
package org.seasar.tramina.unit;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * 特定のテストメソッドが実行される前に呼び出されるメソッドを注釈します．
 * <p>
 * {@link #value()}要素を省略する場合は，注釈するメソッドの名前を{@literal before}で始めます． {@literal
 * before}を取り除いて先頭を小文字にした名前が一致するテストメソッドが実行される前に呼び出されます．<br/>
 * 以下の例では，{@code beforeHoge}メソッドは{@code hoge()}メソッドが実行される前に呼び出されます．
 * </p>
 * 
 * <pre>
 * &#x40;Before
 * public void beforeHoge() {...}
 * </pre>
 * 
 * <p>
 * {@link #value()}要素を指定した場合は，指定された名前と一致するテストメソッドが実行される前に呼び出されます．<br/>
 * 以下の例では，{@code foo}メソッドは{@code hoge()}メソッドまたは{@code moge()}
 * メソッドが実行される前に呼び出されます．
 * </p>
 * 
 * <pre>
 * &#x40;Before({"hoge", "moge"})
 * public void foo() {...}
 * </pre>
 * 
 * @author koichik
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Record {
    /**
     * このメソッドが対象とするメソッドの名前です．
     * 
     * @return このメソッドが対象とするメソッドの名前
     */
    String[] value() default {};
}
