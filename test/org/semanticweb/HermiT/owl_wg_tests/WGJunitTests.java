package org.semanticweb.HermiT.owl_wg_tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import junit.framework.TestSuite;

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
        URI physicalURI = WGJunitTests.class.getResource("ontologies/test-ontology.owl").toURI();
        m.loadOntologyFromPhysicalURI(physicalURI);
        OWLOntology o;
        TestSuite tests;
        OWLClass testClass;
        
        //physicalURI = URI.create("http://wiki.webont.org/exports/type-inconsistency.rdf");
        physicalURI = WGJunitTests.class.getResource("ontologies/type-inconsistency20090408.rdf").toURI();
        o = m.loadOntologyFromPhysicalURI(physicalURI);
        tests = new TestSuite("OWL WG Inconsistency Tests");
        testClass = m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE + "InconsistencyTest")); 
        for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) {
            new ConsistencyTest(m, o, ax.getIndividual(), tests, false); 
        }
        suite.addTest(tests);
        
        //physicalURI = URI.create("http://wiki.webont.org/exports/type-consistency.rdf");
        physicalURI = WGJunitTests.class.getResource("ontologies/type-consistency20090408.rdf").toURI();
        o = m.loadOntologyFromPhysicalURI(physicalURI);
        tests = new TestSuite("OWL WG Consistency Tests");
        testClass = m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE + "ConsistencyTest")); 
        for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) {
            new ConsistencyTest(m, o, ax.getIndividual(), tests, true); 
        }
        suite.addTest(tests);
        
        //physicalURI = URI.create("http://wiki.webont.org/exports/type-positive-entailment.rdf");
        physicalURI = WGJunitTests.class.getResource("ontologies/type-positive-entailment20090409.rdf").toURI();
        o = m.loadOntologyFromPhysicalURI(physicalURI);
        tests = new TestSuite("OWL WG Positive Entailment Tests");
        testClass = m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE + "PositiveEntailmentTest")); 
        for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) {
            new EntailmentTest(m, o, ax.getIndividual(), tests, true); 
        }
        suite.addTest(tests);
        
        //physicalURI = URI.create("http://wiki.webont.org/exports/type-negative-entailment.rdf");
        physicalURI = WGJunitTests.class.getResource("ontologies/type-negative-entailment20090409.rdf").toURI();
        o = m.loadOntologyFromPhysicalURI(physicalURI);
        tests = new TestSuite("OWL WG Negative Entailment Tests");
        testClass = m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE + "NegativeEntailmentTest")); 
        for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) {
            new EntailmentTest(m, o, ax.getIndividual(), tests, false); 
        }
        suite.addTest(tests);
        
        return suite;
    }
}
