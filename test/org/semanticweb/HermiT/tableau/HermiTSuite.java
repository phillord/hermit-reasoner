package org.semanticweb.HermiT.tableau;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.semanticweb.HermiT.reasoner.ReasonerTest;

public class HermiTSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("HermiT Tests");
        suite.addTestSuite(NormalizationTest.class);
        suite.addTestSuite(ClausificationTest.class);
        suite.addTestSuite(DependencySetTest.class);
        suite.addTestSuite(TupleIndexTest.class);
        suite.addTestSuite(DLClauseEvaluationTest.class);
        suite.addTestSuite(MergeTest.class);
        suite.addTestSuite(NIRuleTest.class);
        suite.addTestSuite(TupleTableFullIndexTest.class);
        suite.addTestSuite(GraphTest.class);
        suite.addTestSuite(ReasonerTest.class);
        suite.addTestSuite(IndividualReuseTest.class);
        return suite;
    }

    public static void main(String... args) throws Throwable {
        junit.textui.TestRunner.run(suite());
        System.exit(0);
    }
}
