package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllNonRejectedShouldPassWGTests {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Non-Rejected Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED || wgTestDescriptor.status==WGTestDescriptor.Status.PROPOSED || wgTestDescriptor.status==null) {
                if (
                            !wgTestDescriptor.identifier.startsWith("FS2RDF-negative-property-assertion-ar") // contains an object in the place of a data literal, reported will be fixed
                         && !wgTestDescriptor.identifier.startsWith("Consistent-but-all-unsat") // parsing error, Mike is working on it, some wiki problem
                         && !wgTestDescriptor.identifier.startsWith("FS2RDF-literals-ar") // contains invalid data values, will be fixed
                         // out of time
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-202")
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-203")
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-204")
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-206")
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-662")
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-663")
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-664")
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-903")// large cardinalities
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-904")// large cardinalities
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-906")// extra credit, large cardinalities
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-910")// extra credit, large cardinalitiest
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-miscellaneous-010") //extra credit
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-miscellaneous-011")
                         //&& !wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-001") // hacked, extra credit
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-002")
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-003") //extra credit
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-004") //extra credit
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-005")
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-012")
                         // out of memory
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-907")
                         && !wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-909")
                ) {
                    wgTestDescriptor.addTestsToSuite(suite);
                }
            }
        return suite;
    }
}
