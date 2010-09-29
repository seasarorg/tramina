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

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seasar.tramina.resource.jdbc.impl.dialect.StandardManagedConnectionFactoryDialect;
import org.seasar.tramina.spi.LastXAResource;
import org.seasar.tramina.spi.SubtransactionAwareXAResource;
import org.seasar.tramina.unit.EasyMock;
import org.seasar.tramina.unit.EasyMockRunner;
import org.seasar.tramina.unit.MockType;
import org.seasar.tramina.unit.Record;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * 
 * 
 * @author koichik
 */
@RunWith(EasyMockRunner.class)
public class XAResourceFactoryImplTest {

    @EasyMock(MockType.STRICT)
    XAConnection xaConnection;

    @EasyMock(MockType.STRICT)
    Connection physicalConnection;

    @EasyMock(MockType.STRICT)
    XAResource xaResource;

    @Record
    public void recordNormalResource() throws Exception {
        expect(xaConnection.getXAResource()).andReturn(xaResource);
    }

    @Test
    public void testNormalResource() throws Exception {
        XAResourceFactoryImpl factory =
            new XAResourceFactoryImpl(
                false,
                false,
                new StandardManagedConnectionFactoryDialect());
        XAResource xaRes =
            factory.createXAResource(xaConnection, physicalConnection);
        assertThat(xaRes, is(sameInstance(xaResource)));
        assertThat(xaRes instanceof LastXAResource, is(false));
        assertThat(xaRes instanceof SubtransactionAwareXAResource, is(false));
    }

    @Record( { "testLastResource", "testSubtransactionAwareResource",
        "testSubtransactionAwareLastResource" })
    public void recordOtherResource() throws Exception {
    }

    @Test
    public void testLastResource() throws Exception {
        XAResourceFactoryImpl factory =
            new XAResourceFactoryImpl(
                true,
                false,
                new StandardManagedConnectionFactoryDialect());
        XAResource xaRes =
            factory.createXAResource(xaConnection, physicalConnection);
        assertThat(xaRes, is(not(sameInstance(xaResource))));
        assertThat(xaRes instanceof LastXAResource, is(true));
        assertThat(xaRes instanceof SubtransactionAwareXAResource, is(false));
    }

    @Test
    public void testSubtransactionAwareResource() throws Exception {
        XAResourceFactoryImpl factory =
            new XAResourceFactoryImpl(
                false,
                true,
                new StandardManagedConnectionFactoryDialect());
        XAResource xaRes =
            factory.createXAResource(xaConnection, physicalConnection);
        assertThat(xaRes, is(not(sameInstance(xaResource))));
        assertThat(xaRes instanceof LastXAResource, is(false));
        assertThat(xaRes instanceof SubtransactionAwareXAResource, is(true));
    }

    @Test
    public void testSubtransactionAwareLastResource() throws Exception {
        XAResourceFactoryImpl factory =
            new XAResourceFactoryImpl(
                true,
                true,
                new StandardManagedConnectionFactoryDialect());
        XAResource xaRes =
            factory.createXAResource(xaConnection, physicalConnection);
        assertThat(xaRes, is(not(sameInstance(xaResource))));
        assertThat(xaRes instanceof LastXAResource, is(true));
        assertThat(xaRes instanceof SubtransactionAwareXAResource, is(true));
    }

}
