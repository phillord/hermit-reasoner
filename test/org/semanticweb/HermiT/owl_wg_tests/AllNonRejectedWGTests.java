package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllNonRejectedWGTests {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Non-Rejected Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED || wgTestDescriptor.status==WGTestDescriptor.Status.PROPOSED || wgTestDescriptor.status==null) {
//                if (wgTestDescriptor.identifier.startsWith("WebOnt-miscellaneous-010")
//                        || wgTestDescriptor.identifier.startsWith("TestCase:WebOnt-miscellaneous-010")
//                        || wgTestDescriptor.identifier.startsWith("Somevaluesfrom2bnode")
//                        || wgTestDescriptor.identifier.startsWith("Bnode2somevaluesfrom")
//                        || wgTestDescriptor.identifier.startsWith("TestCase:WebOnt-allValuesFrom-002")
//                        || wgTestDescriptor.identifier.startsWith("WebOnt-allValuesFrom-002")) {
                    wgTestDescriptor.addTestsToSuite(suite);
//                }
            }
        return suite;
    }
}
