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

import java.nio.ByteBuffer;

import javax.transaction.xa.Xid;

/**
 * 
 * 
 * @author koichik
 */
public class XidUtil {

    // /////////////////////////////////////////////////////////////////
    // static methods
    //
    public static String toString(final Xid xid) {
        final StringBuilder buf =
            new StringBuilder(512).append("[").append(
                Integer.toHexString(xid.getFormatId())).append("]-[");
        ByteBuffer byteBuffer = ByteBuffer.wrap(xid.getGlobalTransactionId());
        for (int i = 0; i < Xid.MAXGTRIDSIZE / 8; ++i) {
            buf.append(Long.toHexString(byteBuffer.getLong())).append(":");
        }
        buf.setLength(buf.length() - 1);
        buf.append("]-[");
        byteBuffer = ByteBuffer.wrap(xid.getBranchQualifier());
        for (int i = 0; i < Xid.MAXBQUALSIZE / 8; ++i) {
            buf.append(Long.toHexString(byteBuffer.getLong())).append(":");
        }
        buf.setLength(buf.length() - 1);
        buf.append("]");
        return new String(buf);
    }

}
