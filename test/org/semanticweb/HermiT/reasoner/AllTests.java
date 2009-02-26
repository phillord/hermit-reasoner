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
        suite.addTestSuite(IndividualReuseTest.class);
        suite.addTestSuite(ComplexConceptTest.class);
        // $JUnit-END$
        return suite;
    }

}
