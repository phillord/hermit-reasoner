package org.semanticweb.HermiT.owl_wg_tests;

import java.io.PrintWriter;

public class ConsistencyTest extends AbstractTest {
    protected final boolean m_positive;

    public ConsistencyTest(WGTestDescriptor wgTestDescriptor,boolean positive,PrintWriter output) {
        super(wgTestDescriptor.identifier+(positive ? "-consistency" : "-inconsistency"),wgTestDescriptor,output);
        m_positive=positive;
    }
    protected void doTest() {
        assertEquals(m_positive,m_reasoner.isConsistent());
    }
    protected String getTestType() {
        return m_positive ? "consistency" : "inconsistency";
    }
    protected String reportTestType() {
        return (m_positive?"Consistency":"Inconsistency")+"Run";
    }
}
