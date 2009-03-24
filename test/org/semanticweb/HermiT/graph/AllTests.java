package org.semanticweb.HermiT.graph;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Unit tests for utility functions");
        //$JUnit-BEGIN$
        suite.addTestSuite(GraphUtilsTest.class);
        //$JUnit-END$
        return suite;
    }

}
