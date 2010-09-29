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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * 
 * 
 * @author koichik
 */
public class JdbcUtil {

    public static <T> T process(final DataSource dataSource,
            final ConnectionProcessor<T> processor) throws SQLException {
        final Connection con = dataSource.getConnection();
        try {
            return processor.process(con);
        } finally {
            con.close();
        }
    }

    public static <T> T process(final DataSource dataSource, final String sql,
            final PreparedStatementProcessor<T> processor) throws SQLException {
        return process(dataSource, new ConnectionProcessor<T>() {
            @Override
            public T process(final Connection con) throws SQLException {
                final PreparedStatement ps = con.prepareStatement(sql);
                try {
                    return processor.process(ps);
                } finally {
                    ps.close();
                }
            }
        });
    }

    public static <T> T process(final DataSource dataSource, final String sql,
            final ResultSetProcessor<T> processor) throws SQLException {
        return process(dataSource, sql, new PreparedStatementProcessor<T>() {
            @Override
            public T process(final PreparedStatement ps) throws SQLException {
                final ResultSet rs = ps.executeQuery();
                try {
                    return processor.process(rs);
                } finally {
                    rs.close();
                }
            }
        });
    }

    // /////////////////////////////////////////////////////////////////
    // member types
    //
    public interface ConnectionProcessor<T> {
        public T process(Connection con) throws SQLException;
    }

    public interface PreparedStatementProcessor<T> {
        public T process(PreparedStatement ps) throws SQLException;
    }

    public interface ResultSetProcessor<T> {
        public T process(ResultSet rs) throws SQLException;
    }

}
