package org.semanticweb.HermiT.owl_wg_tests;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;

public class EntailmentTest extends AbstractTest {
    protected final boolean m_positive;
    protected OWLOntology m_conclusion;

    public EntailmentTest(WGTestDescriptor wgTestDescriptor,boolean positive) {
        super(wgTestDescriptor.identifier+(positive ? "-entailment" : "-nonentailment"),wgTestDescriptor);
        m_positive=positive;
    }
    protected void setUp() throws Exception {
        super.setUp();
        m_conclusion=m_positive ? m_wgTestDescriptor.getConclusionOntology(m_ontologyManager) : m_wgTestDescriptor.getNonConclusionOntology(m_ontologyManager);
    }
    protected void tearDown() {
        super.tearDown();
        m_conclusion=null;
    }
    protected void doTest() throws Exception {
        for (OWLAxiom axiom : m_conclusion.getLogicalAxioms()) {
            EntailmentChecker checker=new EntailmentChecker(m_reasoner,m_ontologyManager.getOWLDataFactory());
            boolean isEntailed=checker.isEntailed(axiom);
            if (m_positive) {
                assertTrue("Axiom "+axiom.toString()+" should be entailed.",isEntailed);
            }
            else {
                if (!isEntailed)
                    return;
            }
        }
        if (!m_positive)
            fail("At least one axiom should not be entailed by the premise ontology.");
    }
}
