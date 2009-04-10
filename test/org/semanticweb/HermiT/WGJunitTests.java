package org.semanticweb.HermiT;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.semanticweb.HermiT.owl_wg_tests.TestLoader;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;


public class WGJunitTests extends junit.framework.TestCase {
    
    public static long TIMEOUT = 6000;
    public static boolean USE_ONLY_APPROVED_TESTS = false;
    public static String URI_BASE = "http://www.w3.org/2007/OWL/testOntology#";
    public static Logger log = Logger.getLogger(WGJunitTests.class.getName());
    
    public static junit.framework.TestSuite suite() throws OWLOntologyCreationException, URISyntaxException {
        TestSuite suite = new TestSuite("OWL WG Tests");
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        URI physicalURI = WGJunitTests.class.getResource("owl_wg_tests/ontologies/test-ontology.owl").toURI();
        m.loadOntologyFromPhysicalURI(physicalURI);
        
        //physicalURI = URI.create("http://wiki.webont.org/exports/all.rdf");
        physicalURI = WGJunitTests.class.getResource("owl_wg_tests/ontologies/all.rdf").toURI();
        //physicalURI = URI.create("file:/Users/bglimm/Documents/ontologies/WebOnt-description-logic-207.rdf");        
        loadTests(physicalURI, m, suite);
        
        return suite;
    }
    
    private static void loadTests(URI physicalURI, OWLOntologyManager m, TestSuite suite) {
        try {
            TestSuite tests;
            TestCase test;
            OWLClass testClass;
            
            OWLOntology o = m.loadOntologyFromPhysicalURI(physicalURI);
            
            tests = new TestSuite("OWL WG Inconsistency Tests");
            testClass = m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE + "InconsistencyTest")); 
            for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) {
                test = new TestLoader(m, o, ax.getIndividual()).loadConsistencyTest(false);
                if (test != null) tests.addTest(test);
            }
            if (tests.testCount() > 0) suite.addTest(tests);
            
            tests = new TestSuite("OWL WG Consistency Tests");
            testClass = m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE + "ConsistencyTest")); 
            for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) { 
                test = new TestLoader(m, o, ax.getIndividual()).loadConsistencyTest(true);
                if (test != null) tests.addTest(test);
            }
            if (tests.testCount() > 0) suite.addTest(tests);
            
            tests = new TestSuite("OWL WG Positive Entailment Tests");
            testClass = m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE + "PositiveEntailmentTest")); 
            for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) {
                Set<TestCase> ts = new TestLoader(m, o, ax.getIndividual()).loadEntailmentTest(true);
                if (ts != null) {
                    for (TestCase t : ts) {
                        tests.addTest(t);
                    }
                }
            }
            if (tests.testCount() > 0) suite.addTest(tests);
            
            tests = new TestSuite("OWL WG Negative Entailment Tests");
            testClass = m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE + "NegativeEntailmentTest")); 
            for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) {
                Set<TestCase> ts = new TestLoader(m, o, ax.getIndividual()).loadEntailmentTest(false);
                if (ts != null) {
                    for (TestCase t : ts) {
                        tests.addTest(t);
                    }
                }
            }
            if (tests.testCount() > 0) suite.addTest(tests);
        } catch (Exception e) {
            log.severe("Ontology could not be loaded from physical URI " + physicalURI);
            log.severe("Error message: " + e.getMessage());
            log.severe("Stack trace: " + e.getStackTrace());
        }
    }
}
