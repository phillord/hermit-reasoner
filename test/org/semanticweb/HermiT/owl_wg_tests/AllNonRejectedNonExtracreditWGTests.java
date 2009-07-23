package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllNonRejectedNonExtracreditWGTests {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Non-Rejected Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED || wgTestDescriptor.status==WGTestDescriptor.Status.PROPOSED || wgTestDescriptor.status==null) {
                if (!wgTestDescriptor.identifier.startsWith("Consistent-but-all-unsat") // parsing error, Mike is working on it, some wiki problem
                         && !wgTestDescriptor.identifier.startsWith("FS2RDF-literals-ar")) // contains #plainLiteral with lowercase p
                    wgTestDescriptor.addTestsToSuite(suite);
           }
        return suite;
    }
}
