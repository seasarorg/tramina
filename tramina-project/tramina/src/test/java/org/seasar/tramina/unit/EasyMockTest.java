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

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * 
 * 
 * @author koichik
 */
@RunWith(EasyMockRunner.class)
public class EasyMockTest {

    @EasyMock
    DataSource ds;

    @EasyMock
    Connection con;

    @Record
    public void record() throws Exception {
        expect(ds.getConnection("hoge", "moge")).andReturn(con);
        con.close();
    }

    @Test
    public void test() throws Exception {
        Connection con = ds.getConnection("hoge", "moge");
        assertThat(con, is(sameInstance(this.con)));
        con.close();
    }

    @Test
    public void testNoRecord() throws Exception {
        assertThat(ds, is(not(nullValue())));
        assertThat(con, is(not(nullValue())));
    }

}
