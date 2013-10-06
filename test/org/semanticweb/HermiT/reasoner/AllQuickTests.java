package org.semanticweb.HermiT.reasoner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllQuickTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("Unit tests for HermiT as a blackbox -- the quick subset.");
        // $JUnit-BEGIN$
        suite.addTestSuite(DatatypesTest.class);
        suite.addTestSuite(NumericsTest.class);
        suite.addTestSuite(RDFPlainLiteralTest.class);
        suite.addTestSuite(AnyURITest.class);
        suite.addTestSuite(FloatDoubleTest.class);
        suite.addTestSuite(DateTimeTest.class);
        suite.addTestSuite(BinaryDataTest.class);
        suite.addTestSuite(XMLLiteralTest.class);
        suite.addTestSuite(ReasonerTest.class);
        suite.addTestSuite(ReasonerIndividualReuseTest.class);
        suite.addTestSuite(ReasonerCoreBlockingTest.class);
        suite.addTestSuite(ComplexConceptTest.class);
        suite.addTestSuite(EntailmentTest.class);
        suite.addTestSuite(RIATest.class);
        suite.addTestSuite(SimpleRolesTest.class);
        suite.addTestSuite(RulesTest.class);
        suite.addTestSuite(OWLReasonerTest.class);
        suite.addTestSuite(DatalogEngineTest.class);
        // $JUnit-END$
        return suite;
    }
}
