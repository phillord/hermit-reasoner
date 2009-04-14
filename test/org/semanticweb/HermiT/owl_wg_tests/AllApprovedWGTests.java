package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllApprovedWGTests {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Approved Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED && wgTestDescriptor.isDLTest())
                wgTestDescriptor.addTestsToSuite(suite);
        return suite;
    }
}
