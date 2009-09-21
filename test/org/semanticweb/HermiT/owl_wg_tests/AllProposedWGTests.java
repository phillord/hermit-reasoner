package org.semanticweb.HermiT.owl_wg_tests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllProposedWGTests {
    public static Test suite() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String today=dateFormat.format(new Date());
        System.setProperty(WGTestRegistry.RESULTS_FILE_PATH, "/Users/bglimm/Documents/workspace/HermiT/test/org/semanticweb/HermiT/owl_wg_tests/ontologies/results-proposed-"+today+".owl");
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Proposed Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.isDLTest() && wgTestDescriptor.status==WGTestDescriptor.Status.PROPOSED)
                wgTestDescriptor.addTestsToSuite(suite);
        return suite;
    }
}
