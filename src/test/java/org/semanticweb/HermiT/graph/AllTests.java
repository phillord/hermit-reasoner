package org.semanticweb.HermiT.graph;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite=new TestSuite("Unit tests for the graph library");
        // $JUnit-BEGIN$
        suite.addTestSuite(GraphTest.class);
        // $JUnit-END$
        return suite;
    }

}
