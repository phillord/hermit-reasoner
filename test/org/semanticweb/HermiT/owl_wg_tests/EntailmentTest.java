package org.semanticweb.HermiT.owl_wg_tests;

import java.io.File;
import java.io.PrintWriter;

import org.semanticweb.HermiT.EntailmentChecker;
import org.semanticweb.owlapi.model.OWLOntology;

public class EntailmentTest extends AbstractTest {
    protected final boolean m_positive;
    protected OWLOntology m_conclusionOntology;

    public EntailmentTest(WGTestDescriptor wgTestDescriptor,boolean positive,PrintWriter output) {
        super(wgTestDescriptor.identifier+(positive ? "-entailment" : "-nonentailment"),wgTestDescriptor,output);
        m_positive=positive;
    }
    protected void setUp() throws Exception {
        super.setUp();
        m_conclusionOntology=m_wgTestDescriptor.getConclusionOntology(m_ontologyManager,m_positive);
    }
    protected void tearDown() {
        super.tearDown();
        m_conclusionOntology=null;
    }
    protected void doTest() throws Exception {
        EntailmentChecker checker=new EntailmentChecker(m_reasoner,m_ontologyManager.getOWLDataFactory());
        boolean isEntailed=checker.entails(m_conclusionOntology.getLogicalAxioms());
        if (m_positive) {
            assertTrue("Axioms should be entailed.",isEntailed);
        } else {
            assertTrue("At least one axiom should not be entailed by the premise ontology.",!isEntailed);
        }
    }
    protected String getTestType() {
        return m_positive ? "entailment" : "nonentailment";
    }
    protected void dumpFailureData() throws Exception {
        super.dumpFailureData();
        saveOntology(m_ontologyManager,m_conclusionOntology,new File(getFailureRoot(),m_positive ? "conclusion.owl" : "nonconclusion.owl"));
    }
    protected String reportTestType() {
        return (m_positive?"Positive":"Negative")+"EntailmentRun";
    }
}
