package org.semanticweb.HermiT.tableau;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for the HermiT internals (tableau)");
        //$JUnit-BEGIN$
        suite.addTestSuite(TupleIndexTest.class);
        suite.addTestSuite(TupleTableFullIndexTest.class);
        suite.addTestSuite(DLClauseEvaluationTest.class);
        suite.addTestSuite(DependencySetTest.class);
        suite.addTestSuite(NIRuleTest.class);
        suite.addTestSuite(MergeTest.class);
        suite.addTestSuite(GraphTest.class);
        //$JUnit-END$
        return suite;
    }

    public static void main(String... args) throws Throwable {
        junit.textui.TestRunner.run(suite());
        System.exit(0);
    }

}
