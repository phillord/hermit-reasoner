package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllNonRejectedNonExtracreditWGTests {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Non-Rejected Non-Extracredit Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.isDLTest() && (wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED || wgTestDescriptor.status==WGTestDescriptor.Status.PROPOSED)) {
                //if (!wgTestDescriptor.identifier.startsWith("Consistent-but-all-unsat")) // contains hashes in class names where there should be none, reported 
                    wgTestDescriptor.addTestsToSuite(suite);
           }
        return suite;
    }
}
