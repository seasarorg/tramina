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
package org.seasar.tramina.resource.jdbc.impl;

import java.sql.Connection;

import javax.transaction.xa.Xid;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seasar.tramina.resource.jdbc.exception.ResourceNotEndedException;
import org.seasar.tramina.resource.jdbc.exception.ResourceNotPreparedException;
import org.seasar.tramina.resource.jdbc.exception.ResourceNotStartedException;
import org.seasar.tramina.transaction.impl.TraminaXidImpl;
import org.seasar.tramina.unit.EasyMock;
import org.seasar.tramina.unit.EasyMockRunner;
import org.seasar.tramina.unit.MockType;
import org.seasar.tramina.unit.Record;

import static javax.transaction.xa.XAResource.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.seasar.tramina.resource.jdbc.impl.PseudoXAResourceImpl.StatusType.*;

/**
 * 
 * 
 * @author koichik
 */
@RunWith(EasyMockRunner.class)
public class PseudoXAResourceImplTest {

    Xid xid = new TraminaXidImpl(0L);

    @EasyMock(MockType.STRICT)
    Connection con;

    @Record
    public void recordOnePhaseCommit() throws Exception {
        con.setAutoCommit(false);
        con.commit();
        con.setAutoCommit(true);
    }

    @Test
    public void testOnePhaseCommit() throws Exception {
        PseudoXAResourceImpl xaResource = new PseudoXAResourceImpl(con);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
        // enlist resouces
        xaResource.start(xid, TMNOFLAGS);
        assertThat(xaResource.status, is(equalTo(STARTED)));
        // delist resouces
        xaResource.end(xid, TMSUCCESS);
        assertThat(xaResource.status, is(equalTo(ENDED)));
        // commit
        xaResource.commit(xid, true);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
    }

    @Record
    public void recordTwoPhaseCommit() throws Exception {
        con.setAutoCommit(false);
        con.commit();
        con.setAutoCommit(true);
    }

    @Test
    public void testTwoPhaseCommit() throws Exception {
        PseudoXAResourceImpl xaResource = new PseudoXAResourceImpl(con);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
        // enlist resource
        xaResource.start(xid, TMNOFLAGS);
        assertThat(xaResource.status, is(equalTo(STARTED)));
        // delist resource
        xaResource.end(xid, TMSUCCESS);
        assertThat(xaResource.status, is(equalTo(ENDED)));
        // prepare
        xaResource.prepare(xid);
        assertThat(xaResource.status, is(equalTo(PREPARED)));
        // commit
        xaResource.commit(xid, false);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
    }

    @Record
    public void recordRollback1() throws Exception {
        con.setAutoCommit(false);
        con.rollback();
        con.setAutoCommit(true);
    }

    @Test
    public void testRollback1() throws Exception {
        PseudoXAResourceImpl xaResource = new PseudoXAResourceImpl(con);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
        // enlist resource
        xaResource.start(xid, TMNOFLAGS);
        assertThat(xaResource.status, is(equalTo(STARTED)));
        // delist resource
        xaResource.end(xid, TMSUCCESS);
        assertThat(xaResource.status, is(equalTo(ENDED)));
        // rollback
        xaResource.rollback(xid);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
    }

    @Record
    public void recordRollback2() throws Exception {
        con.setAutoCommit(false);
        con.rollback();
        con.setAutoCommit(true);
    }

    @Test
    public void testRollback2() throws Exception {
        PseudoXAResourceImpl xaResource = new PseudoXAResourceImpl(con);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
        // enlist resource
        xaResource.start(xid, TMNOFLAGS);
        assertThat(xaResource.status, is(equalTo(STARTED)));
        // delist resource
        xaResource.end(xid, TMSUCCESS);
        assertThat(xaResource.status, is(equalTo(ENDED)));
        // prepare
        xaResource.prepare(xid);
        assertThat(xaResource.status, is(equalTo(PREPARED)));
        // rollback
        xaResource.rollback(xid);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
    }

    @Test(expected = ResourceNotStartedException.class)
    public void testNotStarted() throws Exception {
        PseudoXAResourceImpl xaResource = new PseudoXAResourceImpl(con);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
        xaResource.end(xid, TMSUCCESS);
    }

    @Record
    public void recordNotEnded() throws Exception {
        con.setAutoCommit(false);
    }

    @Test(expected = ResourceNotEndedException.class)
    public void testNotEnded() throws Exception {
        PseudoXAResourceImpl xaResource = new PseudoXAResourceImpl(con);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
        xaResource.start(xid, TMNOFLAGS);
        assertThat(xaResource.status, is(equalTo(STARTED)));
        xaResource.prepare(xid);
    }

    @Record
    public void recordNotPrepared() throws Exception {
        con.setAutoCommit(false);
    }

    @Test(expected = ResourceNotPreparedException.class)
    public void testNotPrepared() throws Exception {
        PseudoXAResourceImpl xaResource = new PseudoXAResourceImpl(con);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
        xaResource.start(xid, TMNOFLAGS);
        assertThat(xaResource.status, is(equalTo(STARTED)));
        xaResource.end(xid, TMSUCCESS);
        assertThat(xaResource.status, is(equalTo(ENDED)));
        xaResource.commit(xid, false);
    }

}
