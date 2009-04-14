package org.semanticweb.HermiT.owl_wg_tests;

public class ConsistencyTest extends AbstractTest {
    protected final boolean m_positive;

    public ConsistencyTest(WGTestDescriptor wgTestDescriptor,boolean positive) {
        super(wgTestDescriptor.identifier+(positive ? "-consistenty" : "-inconsistency"),wgTestDescriptor);
        m_positive=positive;
    }
    protected void doTest() {
        assertEquals(m_positive,m_reasoner.isConsistent());
    }
}
