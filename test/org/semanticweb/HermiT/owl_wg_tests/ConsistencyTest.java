/* Copyright 2009 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.owl_wg_tests;

import java.io.File;

import java.util.Collection;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;


@RunWith(Parameterized.class)
public class ConsistencyTest extends AbstractTest {
    protected final boolean m_positive;

    public ConsistencyTest(WGTestDescriptor wgTestDescriptor,boolean positive,File dumpTestDataDirectory,boolean useDisjunctionLearning) {
        super(wgTestDescriptor.identifier+(positive ? "-consistency" : "-inconsistency"),wgTestDescriptor,dumpTestDataDirectory,useDisjunctionLearning);
        m_positive=positive;
    }
    protected void doTest() {
        assertEquals(m_positive,m_reasoner.isConsistent());
    }


    @Test
    public void test() throws Throwable{
        runTest();
    }
    
    @Before
    public void before() throws Exception{
        setUp();
    }
    
    @After
    public void after() throws Exception{
        tearDown();
    }
    @Parameters
    public static Collection<Object[]> params() throws Exception{
        return TstDescriptorForMaven.getConsistencyTestParams();
    }

}
