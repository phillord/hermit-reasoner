package org.semanticweb.HermiT;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllQuickTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All HermiT tests");
        // $JUnit-BEGIN$
        // Tests the internals of the datatype classes.
        suite.addTest(org.semanticweb.HermiT.datatypes.AllTests.suite());
        // Tests the normalization and clausification, when loading via the OWLAPI.
        suite.addTest(org.semanticweb.HermiT.structural.AllTests.suite());
        // Tests Hermit as a blackbox, i.e., by only using the official interface.
        suite.addTest(org.semanticweb.HermiT.reasoner.AllQuickTests.suite());
        // Tests for the internals of the reasoner.
        suite.addTest(org.semanticweb.HermiT.tableau.AllTests.suite());
        // Tests the graph library.
        suite.addTest(org.semanticweb.HermiT.graph.AllTests.suite());
        // $JUnit-END$
        return suite;
    }
    
    public static void main(String... args) throws Throwable {
        junit.textui.TestRunner.run(suite());
        System.exit(0);
    }

}
