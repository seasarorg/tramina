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
package org.seasar.tramina.recovery.impl.dialect;

/**
 * 
 * 
 * @author koichik
 */
public class H2Dialect extends AbstractJdbcTransactionLogManagerDialect {

    // /////////////////////////////////////////////////////////////////
    // instance initializer
    //
    {
        createTableSql =
            new String[] { "CREATE TABLE TRAN_LOG ("
                + "ID INTEGER NOT NULL IDENTITY PRIMARY KEY, "
                + "FORMAT_ID INTEGER NOT NULL, "
                + "GLOBAL_ID BINARY(64) NOT NULL, "
                + "BRANCH_ID BINARY(64) NOT NULL, "
                + "COMMIT_TIMESTAMP TIMESTAMP NOT NULL)" };

        selectSql = "SELECT FORMAT_ID, GLOBAL_ID, BRANCH_ID FROM TRAN_LOG";

        insertSql =
            "INSERT INTO TRAN_LOG "
                + "(FORMAT_ID, GLOBAL_ID, BRANCH_ID, COMMIT_TIMESTAMP) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        deleteSql =
            "DELETE FROM TRAN_LOG WHERE DATEADD('MINUTE', 30, COMMIT_TIMESTAMP) < CURRENT_TIMESTAMP";
    }

}
