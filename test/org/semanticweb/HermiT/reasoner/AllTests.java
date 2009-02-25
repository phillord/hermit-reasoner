package org.semanticweb.HermiT.reasoner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("Tests for HermiT as a blackbox.");
        // $JUnit-BEGIN$
        suite.addTestSuite(HeavyReasonerTest.class);
        suite.addTestSuite(ReasonerTest.class);
        suite.addTestSuite(DatatypesTest.class);
        suite.addTestSuite(ReasonerTestKAON2.class);
        suite.addTestSuite(ReasonerTestKAON2Heavy.class);
        // $JUnit-END$
        return suite;
    }

}
