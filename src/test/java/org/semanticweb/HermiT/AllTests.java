package org.semanticweb.HermiT;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("All HermiT tests");
        // $JUnit-BEGIN$
        // Performs all quick tests.
        suite.addTest(org.semanticweb.HermiT.AllQuickTests.suite());
        // Performs the heavy reasoner tests.
        suite.addTest(org.semanticweb.HermiT.reasoner.AllHeavyTests.suite());
        // $JUnit-END$
        return suite;
    }
    public static void main(String... args) throws Throwable {
        junit.textui.TestRunner.run(suite());
        System.exit(0);
    }

}
