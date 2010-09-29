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
package org.seasar.tramina.transaction.impl;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seasar.tramina.spi.LastXAResource;
import org.seasar.tramina.unit.EasyMock;
import org.seasar.tramina.unit.EasyMockRunner;
import org.seasar.tramina.unit.MockType;
import org.seasar.tramina.unit.Record;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.seasar.tramina.spi.TmFlagType.*;

/**
 * 
 * 
 * @author koichik
 */
@RunWith(EasyMockRunner.class)
public class SameResourcesImplTest {

    @EasyMock(MockType.STRICT)
    Xid xid;

    @EasyMock(MockType.STRICT)
    XAResource xaResource;

    @EasyMock(MockType.STRICT)
    LastXAResource lastXAResource;

    @EasyMock(MockType.STRICT)
    XAResource xaResource2;

    @EasyMock(MockType.STRICT)
    XAResource xaResource3;

    @Record
    public void record() throws Exception {
        xaResource.start(xid, NO_FLAGS.getIntValue());
        expect(xaResource.isSameRM(xaResource2)).andReturn(true);
        xaResource2.start(xid, JOIN.getIntValue());
        expect(xaResource.isSameRM(xaResource3)).andReturn(false);
    }

    @Test
    public void test() throws Exception {
        SameResourcesImpl sameResources =
            new SameResourcesImpl(xid, xaResource);
        assertThat(sameResources.isLastResource(), is(false));
        assertThat(sameResources.enlistedResources.isEmpty(), is(false));
        assertThat(sameResources.enlistedResources.size(), is(equalTo(1)));

        assertThat(sameResources.isSameResource(xaResource2), is(true));
        sameResources.enlist(xaResource2);
        assertThat(sameResources.enlistedResources.isEmpty(), is(false));
        assertThat(sameResources.enlistedResources.size(), is(equalTo(2)));

        assertThat(sameResources.isSameResource(xaResource3), is(false));
    }

    @Record
    public void recordLastResource() throws Exception {
        lastXAResource.start(xid, NO_FLAGS.getIntValue());
    }

    @Test
    public void testLastResource() throws Exception {
        SameResourcesImpl sameResources =
            new SameResourcesImpl(xid, lastXAResource);
        assertThat(sameResources.isLastResource(), is(true));
    }

}
