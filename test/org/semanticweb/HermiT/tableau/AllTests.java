package org.semanticweb.HermiT.tableau;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for org.semanticweb.HermiT.tableau");
        //$JUnit-BEGIN$
        suite.addTestSuite(TupleIndexTest.class);
        suite.addTestSuite(DLClauseEvaluationTest.class);
        suite.addTestSuite(TupleTableFullIndexTest.class);
        suite.addTestSuite(GraphTest.class);
        suite.addTestSuite(ReasonerTest.class);
        suite.addTestSuite(DependencySetTest.class);
        suite.addTestSuite(NIRuleTest.class);
        //$JUnit-END$
        return suite;
    }

}
