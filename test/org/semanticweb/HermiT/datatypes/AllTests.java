package org.semanticweb.HermiT.datatypes;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("Tests the internals of the datatype classes.");
        // $JUnit-BEGIN$
        suite.addTestSuite(DatatypesTestFin.class);
        // $JUnit-END$
        return suite;
    }

}
