package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FailingWGTestDebug {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Non-Rejected Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED || wgTestDescriptor.status==WGTestDescriptor.Status.PROPOSED || wgTestDescriptor.status==null) {
                if (wgTestDescriptor.identifier.startsWith("string-integer-clash")
                ) {
//                    wgTestDescriptor.addTestsToSuite(suite);
                }
                if (
//                		wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-202")
//                wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-203")
//                || wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-204")
//                || wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-206")
//                || wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-662")
//                || wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-663")
//                || wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-664")
//                wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-903")// large cardinalities
//                 wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-904")// large cardinalities
//                 wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-906")// large cardinalities - extra credit
//                || wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-910")// large cardinalities - extra credit
//                 wgTestDescriptor.identifier.startsWith("WebOnt-miscellaneous-010")
//                || wgTestDescriptor.identifier.startsWith("WebOnt-miscellaneous-011")
//                 wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-001")
                 wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-002")
//                || wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-003")
//                || wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-004")
//                || wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-005")
//                || wgTestDescriptor.identifier.startsWith("WebOnt-I5.8-012")
//                // out of memory
//                || wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-907")
//                || wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-909")
               ) {
                   wgTestDescriptor.addTestsToSuite(suite);
               }
            }
        return suite;
    }
}
