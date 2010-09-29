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

import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import org.seasar.tramina.spi.Subtransaction;
import org.seasar.tramina.spi.TmFlagType;

/**
 * 同じリソースを表す {@link XAResource} を一つにまとめて扱うオブジェクトです．
 * 
 * @author koichik
 * @see XAResource#isSameRM(XAResource)
 */
public interface SameResources {

    boolean isLastResource();

    boolean isSameResource(XAResource otherResource) throws SystemException;

    XAResource getXAResource();

    void enlist(XAResource newResource) throws SystemException;

    boolean delist(XAResource xaResource, int flag) throws SystemException;

    void suspend() throws SystemException;

    void resume() throws SystemException;

    void end(TmFlagType flag) throws SystemException;

    void prepare() throws SystemException;

    void commitOnePhase() throws SystemException;

    void commit() throws SystemException;

    void rollback() throws SystemException;

    void beginSubtransaction(Subtransaction subtransaction)
            throws SystemException;

    void commitSubtransaction(Subtransaction subtransaction)
            throws SystemException;

    void rollbackSubtransaction(Subtransaction subtransaction)
            throws SystemException;

}
