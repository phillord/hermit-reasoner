package org.semanticweb.HermiT.reasoner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllUnitTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("Unit tests for HermiT as a blackbox.");
        // $JUnit-BEGIN$
        suite.addTestSuite(ReasonerTest.class);
        suite.addTestSuite(DatatypesTest.class);
        // $JUnit-END$
        return suite;
    }
}
