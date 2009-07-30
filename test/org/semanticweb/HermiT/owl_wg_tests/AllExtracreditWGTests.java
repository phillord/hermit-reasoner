// Copyright 2009 by Oxford University; see license.txt for details
// An update for the tests (all.rdf) should regularly be downloaded to the 
// ontologies folder from http://wiki.webont.org/exports/
package org.semanticweb.HermiT.owl_wg_tests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllExtracreditWGTests {
    public static Test suite() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String today=dateFormat.format(new Date());
        System.setProperty(WGTestRegistry.RESULTS_FILE_PATH, "/Users/bglimm/Documents/workspace/HermiT/test/org/semanticweb/HermiT/owl_wg_tests/ontologies/results-extracredit-"+today+".owl");
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Extracredit Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.isDLTest() && wgTestDescriptor.status==WGTestDescriptor.Status.EXTRACREDIT) {
                    wgTestDescriptor.addTestsToSuite(suite);
            }
        return suite;
    }
}
