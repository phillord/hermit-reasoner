package org.semanticweb.HermiT.owl_wg_tests;

import java.net.URISyntaxException;

import junit.framework.TestSuite;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class ConsistencyTest extends AbstractTest {

    public ConsistencyTest(OWLOntologyManager m, OWLOntology o, OWLIndividual ind, TestSuite ts, boolean positive) throws OWLOntologyCreationException, URISyntaxException {
        super(m, o, ind);
        if (!parsingError & isApplicable) {
            final String type = positive ? "Consistency" : "Inconsistency";
            final OWLOntologyManager manager = m;
            junit.framework.TestCase test1 = new junit.framework.TestCase("test-" + identifier) {
                public void runTest() {
                    boolean result;
                    RunnableHermiT t = new RunnableHermiT() {  
                        public void run() {
                            Reasoner hermit = new Reasoner(new Configuration(), manager, premiseOntology);
                            consistent = hermit.isConsistent();
                        }
                    };
                    t.start();
                    try {
                        t.join(WGJunitTests.TIMEOUT);
                        if (!t.isAlive()) {
                            result = t.getConsistent();
                            if (type.equals("Consistency")) {
                                String message = "Failure: The ontology " + premiseOntology.getURI() + " should be consistent!";
                                assertTrue(message, result);
                            } else {
                                String message = "Failure: The ontology " + premiseOntology.getURI() + " should be inconsistent!";
                                assertFalse(message, result);
                            }
                        } else {
                            throw new RuntimeException("Timeout: HermiT couldn't decide whether the ontology " + premiseOntology.getURI() + " is consistent within " + WGJunitTests.TIMEOUT + "ms");
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Timeout: HermiT couldn't decide whether the ontology " + premiseOntology.getURI() + " is consistent within " + WGJunitTests.TIMEOUT + "ms");
                    }
                }
            };
            ts.addTest(test1);
        }
    }
}
