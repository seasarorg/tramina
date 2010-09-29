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
import java.sql.Savepoint;

import javax.transaction.xa.Xid;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seasar.tramina.transaction.impl.TraminaXidImpl;
import org.seasar.tramina.unit.EasyMockRunner;
import org.seasar.tramina.unit.EasyMock;
import org.seasar.tramina.unit.MockType;
import org.seasar.tramina.unit.Record;

import static javax.transaction.xa.XAResource.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.seasar.tramina.resource.jdbc.impl.PseudoXAResourceImpl.StatusType.*;

/**
 * 
 * 
 * @author koichik
 */
@RunWith(EasyMockRunner.class)
public class SubtransactionAwareXAResourceImplTest {

    Xid xid = new TraminaXidImpl(0L);

    @EasyMock(MockType.STRICT)
    Connection con;

    @EasyMock(MockType.STRICT)
    Savepoint savepoint1;

    @EasyMock(MockType.STRICT)
    Savepoint savepoint2;

    @Record
    public void recordCommitAllSubtransactions() throws Exception {
        con.setAutoCommit(false);
        expect(con.setSavepoint()).andReturn(savepoint1);
        expect(con.setSavepoint()).andReturn(savepoint2);
        con.commit();
        con.setAutoCommit(true);
    }

    @Test
    public void testCommitAllSubtransactions() throws Exception {
        SubtransactionAwareXAResourceImpl xaResource =
            new SubtransactionAwareXAResourceImpl(con);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
        // enlist resources
        xaResource.start(xid, TMNOFLAGS);
        assertThat(xaResource.status, is(equalTo(STARTED)));
        // begin subtransaction 1
        xaResource.beginSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(1)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint1)));
        // begin subtransaction 2
        xaResource.beginSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(2)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint2)));
        // commit subtransaction 2
        xaResource.commitSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(1)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint1)));
        // commit subtranaction 1
        xaResource.commitSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.isEmpty(), is(true));
        // delist resource
        xaResource.end(xid, TMSUCCESS);
        assertThat(xaResource.status, is(equalTo(ENDED)));
        // commit toplevel
        xaResource.commit(xid, true);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
    }

    @Record
    public void recordRollbackParentSubtransactions() throws Exception {
        con.setAutoCommit(false);
        expect(con.setSavepoint()).andReturn(savepoint1);
        expect(con.setSavepoint()).andReturn(savepoint2);
        con.rollback(savepoint1);
        con.commit();
        con.setAutoCommit(true);
    }

    @Test
    public void testRollbackParentSubtransactions() throws Exception {
        SubtransactionAwareXAResourceImpl xaResource =
            new SubtransactionAwareXAResourceImpl(con);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
        Xid xid = new TraminaXidImpl(0L);
        // enlist resource
        xaResource.start(xid, TMNOFLAGS);
        assertThat(xaResource.status, is(equalTo(STARTED)));
        // begin subtransaction 1
        xaResource.beginSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(1)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint1)));
        // begin subtransaction 2
        xaResource.beginSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(2)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint2)));
        // commit subtransaction 2
        xaResource.commitSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(1)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint1)));
        // rollback subtransaction 1
        xaResource.rollbackSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.isEmpty(), is(true));
        // delist resource
        xaResource.end(xid, TMSUCCESS);
        assertThat(xaResource.status, is(equalTo(ENDED)));
        // commit toplevel
        xaResource.commit(xid, true);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
    }

    @Record
    public void recordRollbackChildSubtransactions() throws Exception {
        con.setAutoCommit(false);
        expect(con.setSavepoint()).andReturn(savepoint1);
        expect(con.setSavepoint()).andReturn(savepoint2);
        con.rollback(savepoint2);
        con.commit();
        con.setAutoCommit(true);
    }

    @Test
    public void testRollbackChildSubtransactions() throws Exception {
        SubtransactionAwareXAResourceImpl xaResource =
            new SubtransactionAwareXAResourceImpl(con);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
        Xid xid = new TraminaXidImpl(0L);
        // enlist resource
        xaResource.start(xid, TMNOFLAGS);
        assertThat(xaResource.status, is(equalTo(STARTED)));
        // begin subtransaction 1
        xaResource.beginSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(1)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint1)));
        // begin subtransaction 2
        xaResource.beginSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(2)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint2)));
        // rollback subtransaction 2
        xaResource.rollbackSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(1)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint1)));
        // commit subtransaction 1
        xaResource.commitSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.isEmpty(), is(true));
        // delist resource
        xaResource.end(xid, TMSUCCESS);
        assertThat(xaResource.status, is(equalTo(ENDED)));
        // commit toplevel
        xaResource.commit(xid, true);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
    }

    @Record
    public void recordRollbackAllSubtransactions() throws Exception {
        con.setAutoCommit(false);
        expect(con.setSavepoint()).andReturn(savepoint1);
        expect(con.setSavepoint()).andReturn(savepoint2);
        con.rollback(savepoint2);
        con.rollback(savepoint1);
        con.commit();
        con.setAutoCommit(true);
    }

    @Test
    public void testRollbackAllSubtransactions() throws Exception {
        SubtransactionAwareXAResourceImpl xaResource =
            new SubtransactionAwareXAResourceImpl(con);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
        Xid xid = new TraminaXidImpl(0L);
        // enlist resource
        xaResource.start(xid, TMNOFLAGS);
        assertThat(xaResource.status, is(equalTo(STARTED)));
        // begin subtransaction 1
        xaResource.beginSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(1)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint1)));
        // begin subtransaction 2
        xaResource.beginSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(2)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint2)));
        // rollback subtransaction 2
        xaResource.rollbackSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.size(), is(equalTo(1)));
        assertThat(
            xaResource.savepoints.getLast(),
            is(sameInstance(savepoint1)));
        // rollback subtransaction 1
        xaResource.rollbackSubtransaction();
        assertThat(xaResource.status, is(equalTo(STARTED)));
        assertThat(xaResource.savepoints.isEmpty(), is(true));
        // delist resource
        xaResource.end(xid, TMSUCCESS);
        assertThat(xaResource.status, is(equalTo(ENDED)));
        // commit toplevel
        xaResource.commit(xid, true);
        assertThat(xaResource.status, is(equalTo(NO_TRANSACTION)));
    }

}
