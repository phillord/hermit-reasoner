package org.semanticweb.HermiT.owl_wg_tests;

import java.net.URISyntaxException;

import junit.framework.TestSuite;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class EntailmentTest extends AbstractTest {

    public EntailmentTest(OWLOntologyManager m, OWLOntology o, OWLIndividual ind, TestSuite ts, boolean positive) throws OWLOntologyCreationException, URISyntaxException {
        super(m, o, ind);
        if (!parsingError && isApplicable) {
            if (positive && conclusionOntology == null) {
                WGJunitTests.log.warning("The test " + identifier + " is an entailment test, but has no conclusion and will be ignored. ");
            } else if (!positive && nonConclusionOntology == null) {
                WGJunitTests.log.warning("The test " + identifier + " is a nonentailment test, but has no nonconclusion and will be ignored. ");
            } else {
                final boolean entailment = positive;
                final OWLOntology conclusions = positive ? conclusionOntology : nonConclusionOntology;
                int testNumber = 0;
                final OWLOntologyManager manager = m;
                for (final OWLAxiom axiom : conclusions.getLogicalAxioms()) {
                    testNumber++;
                    junit.framework.TestCase test1 = new junit.framework.TestCase("test-" + identifier + "-No-" + testNumber) {
                        public void runTest() {
                            boolean result;
                            RunnableHermiT t = new RunnableHermiT() {  
                                public void run() {
                                    final Reasoner hermit = new Reasoner(new Configuration(), manager, premiseOntology);
                                    final EntailmentChecker checker = new EntailmentChecker(hermit, manager.getOWLDataFactory());
                                    try {
                                        consistent = checker.isEntailed(axiom);
                                    } catch (OWLReasonerException e) {
                                        throw new RuntimeException("The ontology " + premiseOntology.getURI() + " caused a reasoning exception!");
                                    }
                                }
                            };
                            t.start();
                            try {
                                t.join(WGJunitTests.TIMEOUT);
                                if (!t.isAlive()) {
                                    result = t.getConsistent();
                                    if (entailment) {
                                        String message = "Failure: Axiom " + axiom + " should be entailed by the ontology " + premiseOntology.getURI() + "!";
                                        assertTrue(message, result);
                                    } else {
                                        String message = "Failure: Axiom " + axiom + " should not be entailed by the ontology " + premiseOntology.getURI() + "!";
                                        assertFalse(message, result);
                                    }
                                } else {
                                    throw new RuntimeException("Timeout: HermiT couldn't decide whether " + axiom + " is entailed by the ontology " + premiseOntology.getURI() + " within " + WGJunitTests.TIMEOUT + "ms");
                                }
                            } catch (InterruptedException e) {
                                throw new RuntimeException("Timeout: HermiT couldn't decide whether " + axiom + " is entailed by the ontology " + premiseOntology.getURI() + " within " + WGJunitTests.TIMEOUT + "ms");
                            }
                        }
                    };
                    ts.addTest(test1);
                }
            }
        }
    }
}
