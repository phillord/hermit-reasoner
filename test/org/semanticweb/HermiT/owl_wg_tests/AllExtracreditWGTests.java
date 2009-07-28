package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllExtracreditWGTests {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Non-Rejected Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.isDLTest() && wgTestDescriptor.status==WGTestDescriptor.Status.EXTRACREDIT) {
                    wgTestDescriptor.addTestsToSuite(suite);
            }
        return suite;
    }
}
