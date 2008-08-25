package org.semanticweb.HermiT.kaon2;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for the normalization and clasification code used with the KAON2 API. ");
        // $JUnit-BEGIN$
        suite.addTestSuite(NormalizationTest.class);
        suite.addTestSuite(ClausificationTest.class);
        // $JUnit-END$
        return suite;
    }

}
