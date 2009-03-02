package org.semanticweb.HermiT.deprecated;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("Tests for HermiT that rely on the KAON2 parser.");
        // $JUnit-BEGIN$
        suite.addTestSuite(GraphTest.class);
        // $JUnit-END$
        return suite;
    }

}
