package org.semanticweb.HermiT.reasoner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllHeavyTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("Unit tests for HermiT as a blackbox -- the heavy subset.");
        // $JUnit-BEGIN$
        suite.addTestSuite(ClassificationTest.class);
        suite.addTestSuite(ClassificationIndividualReuseTest.class);
        suite.addTestSuite(OWLLinkTest.class);
        // $JUnit-END$
        return suite;
    }

}
