// Copyright 2009 by Oxford University; see license.txt for details
// An update for the tests (all.rdf) should regularly be downloaded to the 
// ontologies folder from http://wiki.webont.org/exports/
package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FailingWGTestDebug {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Debugging Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            //if (wgTestDescriptor.isDLTest() && (wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED || wgTestDescriptor.status==WGTestDescriptor.Status.PROPOSED || wgTestDescriptor.status==null)) {
                if (wgTestDescriptor.identifier.startsWith("WebOnt-allValuesFrom-002")
                ) {
                    wgTestDescriptor.addTestsToSuite(suite);
                }
            //}
        return suite;
    }
}
