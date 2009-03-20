package org.semanticweb.HermiT.reasoner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("Tests for HermiT as a blackbox.");
        // $JUnit-BEGIN$
        suite.addTest(AllQuickTests.suite());
        suite.addTestSuite(ClassificationTest.class);
        suite.addTestSuite(ClassificationIndividualReuseTest.class);
        // $JUnit-END$
        return suite;
    }

}
