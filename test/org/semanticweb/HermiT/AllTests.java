package org.semanticweb.HermiT;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.semanticweb.HermiT");
        // $JUnit-BEGIN$
        // Tests the normalization and clausification, when loading via the
        // KAON2 API.
//        suite.addTest(org.semanticweb.HermiT.kaon2.AllTests.suite());
        // Tests the normalization and clausification, when loading via the
        // OWLAPI.
//        suite.addTest(org.semanticweb.HermiT.owlapi.AllTests.suite());
        // Tests Hermit as a blackbox, i.e., by only using the official
        // interface.
        suite.addTest(org.semanticweb.HermiT.reasoner.AllUnitTests.suite());
        // Tests for the internals of the reasoner.
        // These tests still rely on the KAON2 API in some places and need to be
        // adapted and properly categorized.
        suite.addTest(org.semanticweb.HermiT.tableau.AllTests.suite());
        // $JUnit-END$
        return suite;
    }
    
    public static void main(String... args) throws Throwable {
        junit.textui.TestRunner.run(suite());
        System.exit(0);
    }

}
