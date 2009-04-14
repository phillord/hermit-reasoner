package org.semanticweb.HermiT.owl_wg_tests;

import java.net.URI;

import junit.framework.TestSuite;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class WGTestRegistry extends junit.framework.TestCase {
    public static String URI_BASE="http://www.w3.org/2007/OWL/testOntology#";

    public static TestSuite createSuite(boolean onlyApprovedTests) throws Exception {
        TestSuite suite=new TestSuite("OWL WG Tests");
        TestSuite tests;
        OWLOntologyManager m=OWLManager.createOWLOntologyManager();
        URI physicalURI = WGTestRegistry.class.getResource("ontologies/test-ontology.owl").toURI();
        m.loadOntologyFromPhysicalURI(physicalURI);
        OWLOntology o;
        OWLClass testClass;


        physicalURI=WGTestRegistry.class.getResource("ontologies/type-inconsistency20090408.rdf").toURI();
        o=m.loadOntologyFromPhysicalURI(physicalURI);
        tests=new TestSuite("OWL WG Inconsistency Tests");
        testClass=m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE+"InconsistencyTest"));
        for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) {
            WGTestDescriptor wgTestDescriptor=new WGTestDescriptor(m,o,ax.getIndividual());
            if ((!onlyApprovedTests || wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED) && wgTestDescriptor.isDLTest())
                tests.addTest(new ConsistencyTest(wgTestDescriptor,false));
        }
        suite.addTest(tests);

        physicalURI=WGTestRegistry.class.getResource("ontologies/type-consistency20090408.rdf").toURI();
        o=m.loadOntologyFromPhysicalURI(physicalURI);
        tests=new TestSuite("OWL WG Consistency Tests");
        testClass=m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE+"ConsistencyTest"));
        for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) {
            WGTestDescriptor wgTestDescriptor=new WGTestDescriptor(m,o,ax.getIndividual());
            if ((!onlyApprovedTests || wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED) && wgTestDescriptor.isDLTest())
                tests.addTest(new ConsistencyTest(wgTestDescriptor,true));
        }
        suite.addTest(tests);

        physicalURI=WGTestRegistry.class.getResource("ontologies/type-positive-entailment20090409.rdf").toURI();
        o=m.loadOntologyFromPhysicalURI(physicalURI);
        tests=new TestSuite("OWL WG Positive Entailment Tests");
        testClass=m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE+"PositiveEntailmentTest"));
        for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) {
            WGTestDescriptor wgTestDescriptor=new WGTestDescriptor(m,o,ax.getIndividual());
            if ((!onlyApprovedTests || wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED) && wgTestDescriptor.isDLTest())
                tests.addTest(new EntailmentTest(wgTestDescriptor,true));
        }
        suite.addTest(tests);

        physicalURI=WGTestRegistry.class.getResource("ontologies/type-negative-entailment20090409.rdf").toURI();
        o=m.loadOntologyFromPhysicalURI(physicalURI);
        tests=new TestSuite("OWL WG Negative Entailment Tests");
        testClass=m.getOWLDataFactory().getOWLClass(URI.create(URI_BASE+"NegativeEntailmentTest"));
        for (OWLClassAssertionAxiom ax : o.getClassAssertionAxioms(testClass)) {
            WGTestDescriptor wgTestDescriptor=new WGTestDescriptor(m,o,ax.getIndividual());
            if ((!onlyApprovedTests || wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED) && wgTestDescriptor.isDLTest())
                tests.addTest(new EntailmentTest(wgTestDescriptor,false));
        }
        suite.addTest(tests);

        return suite;
    }
}
