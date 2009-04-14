package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllWGTests {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG All Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.isDLTest())
                wgTestDescriptor.addTestsToSuite(suite);
        return suite;
    }
}
