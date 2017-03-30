package org.semanticweb.HermiT.structural;

import org.junit.Ignore;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
@Ignore
@SuppressWarnings("javadoc")
public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for the normalization and clasification code used with the OWLAPI. ");
        // $JUnit-BEGIN$
        suite.addTestSuite(NormalizationTest.class);
        suite.addTestSuite(ClausificationTest.class);
        suite.addTestSuite(ClausificationDatatypesTest.class);
        // $JUnit-END$
        return suite;
    }

}
