package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllWGTestsWithoutStatus {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Tests without Status");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.status==null && wgTestDescriptor.isDLTest())
                wgTestDescriptor.addTestsToSuite(suite);
        return suite;
    }
}
