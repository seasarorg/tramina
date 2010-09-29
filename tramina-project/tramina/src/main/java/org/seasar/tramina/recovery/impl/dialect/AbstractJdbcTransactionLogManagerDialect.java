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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.transaction.xa.Xid;

import org.seasar.tramina.recovery.JdbcTransactionLogManagerDialect;
import org.seasar.tramina.recovery.impl.XidImpl;
import org.seasar.tramina.spi.TwoPhaseCommitEvent;

/**
 * 
 * 
 * @author koichik
 */
public abstract class AbstractJdbcTransactionLogManagerDialect implements
        JdbcTransactionLogManagerDialect {

    // /////////////////////////////////////////////////////////////////
    // instance fields
    //
    protected String catalogName;

    protected String schemaName;

    protected String tableName = "TRAN_LOG";

    protected String[] createTableSql;

    protected String selectSql;

    protected String insertSql;

    protected String deleteSql;

    // /////////////////////////////////////////////////////////////////
    // instance methods from JdbcTransactionLogManagerDialect
    //
    @Override
    public String getCatalogName() {
        return catalogName;
    }

    @Override
    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public String[] getCreateTableSql() {
        return createTableSql;
    }

    @Override
    public String getSelectSql() {
        return selectSql;
    }

    @Override
    public String getInsertSql() {
        return insertSql;
    }

    @Override
    public String getDeleteSql() {
        return deleteSql;
    }

    @Override
    public void bindParameterToInsertSql(final PreparedStatement ps,
            final TwoPhaseCommitEvent event) throws SQLException {
        final Xid xid = event.getXid();
        ps.setInt(1, xid.getFormatId());
        ps.setBytes(2, xid.getGlobalTransactionId());
        ps.setBytes(3, xid.getBranchQualifier());
    }

    @Override
    public Xid createXid(final ResultSet rs) throws SQLException {
        return new XidImpl(rs.getInt(1), rs.getBytes(2), rs.getBytes(3));
    }

    // /////////////////////////////////////////////////////////////////
    // instance methods for properties
    //
    /**
     * @param catalogName
     *            the catalogName to set
     */
    public void setCatalogName(final String catalogName) {
        this.catalogName = catalogName;
    }

    /**
     * @param schemaName
     *            the schemaName to set
     */
    public void setSchemaName(final String schemaName) {
        this.schemaName = schemaName;
    }

    /**
     * @param tableName
     *            the tableName to set
     */
    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    /**
     * @param createTableSql
     *            the createTableSql to set
     */
    public void setCreateTableSql(final String[] createTableSql) {
        this.createTableSql = createTableSql;
    }

    /**
     * @param insertSql
     *            the insertSql to set
     */
    public void setInsertSql(final String insertSql) {
        this.insertSql = insertSql;
    }

    /**
     * @param selectSql
     *            the selectSql to set
     */
    public void setSelectSql(final String selectSql) {
        this.selectSql = selectSql;
    }

    /**
     * @param deleteSql
     *            the deleteSql to set
     */
    public void setDeleteSql(final String deleteSql) {
        this.deleteSql = deleteSql;
    }

}
