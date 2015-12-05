package org.semanticweb.HermiT;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllQuickTests extends TestCase {

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("All HermiT tests");
        // $JUnit-BEGIN$
        
        // not running because the tests fail so often due to different concept definitions introduced for complex concepts depending on the parse order
        // Tests the normalization and clausification, when loading via the OWLAPI.
        //suite.addTest(org.semanticweb.HermiT.structural.AllTests.suite());
        
        // Tests Hermit as a blackbox, i.e., by only using the official interface.
        suite.addTest(org.semanticweb.HermiT.reasoner.AllQuickTests.suite());
        // Tests for the internals of the reasoner.
        suite.addTest(org.semanticweb.HermiT.tableau.AllTests.suite());
        // Tests the graph library.
        suite.addTest(org.semanticweb.HermiT.graph.AllTests.suite());
        // Rungs the WG tests.
        suite.addTest(org.semanticweb.HermiT.owl_wg_tests.AllNonRejectedNonExtracreditWGTests.suite());
        // $JUnit-END$
        return suite;
    }
    
    public static void main(String... args) throws Throwable {
        junit.textui.TestRunner.run(suite());
        System.exit(0);
    }

}
