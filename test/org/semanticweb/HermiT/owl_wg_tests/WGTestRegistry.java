package org.semanticweb.HermiT.owl_wg_tests;

import java.net.URI;

import junit.framework.TestSuite;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLIndividual;

public class WGTestRegistry extends junit.framework.TestCase {
    public static String URI_BASE="http://www.w3.org/2007/OWL/testOntology#";
    public static String TEST_ID_PREFIX="http://km.aifb.uni-karlsruhe.de/projects/owltests/index.php/Special:URIResolver/";

    public static TestSuite createSuite(boolean onlyApprovedTests) throws Exception {
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLOntology testContainer=loadTestDatabase(ontologyManager);

        TestSuite suite=new TestSuite("OWL WG Tests");
        OWLClass testCaseClass=ontologyManager.getOWLDataFactory().getOWLClass(URI.create(URI_BASE+"TestCase"));
        for (OWLClassAssertionAxiom ax : testContainer.getClassAssertionAxioms(testCaseClass)) {
            WGTestDescriptor wgTestDescriptor=new WGTestDescriptor(ontologyManager,testContainer,ax.getIndividual());
            if ((!onlyApprovedTests || wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED) && wgTestDescriptor.isDLTest()) {
                if (wgTestDescriptor.testTypes.contains(WGTestDescriptor.TestType.CONSISTENCY))
                    suite.addTest(new ConsistencyTest(wgTestDescriptor,true));
                if (wgTestDescriptor.testTypes.contains(WGTestDescriptor.TestType.INCONSISTENCY))
                    suite.addTest(new ConsistencyTest(wgTestDescriptor,false));
                if (wgTestDescriptor.testTypes.contains(WGTestDescriptor.TestType.POSITIVE_ENTAILMENT))
                    suite.addTest(new EntailmentTest(wgTestDescriptor,true));
                if (wgTestDescriptor.testTypes.contains(WGTestDescriptor.TestType.NEGATIVE_ENTAILMENT))
                    suite.addTest(new EntailmentTest(wgTestDescriptor,false));
            }
        }

        return suite;
    }
    public WGTestDescriptor getDescriptor(String testName) throws Exception {
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLOntology testContainer=loadTestDatabase(ontologyManager);
        String testID;
        if (testName.endsWith("-consistency"))
            testID=testName.substring(0,testName.length()-"-consistency".length());
        else if (testName.endsWith("-inconsistency"))
            testID=testName.substring(0,testName.length()-"-inconsistency".length());
        else if (testName.endsWith("-entailment"))
            testID=testName.substring(0,testName.length()-"-entailment".length());
        else if (testName.endsWith("-nonentailment"))
            testID=testName.substring(0,testName.length()-"-nonentailment".length());
        else
            throw new IllegalArgumentException("Unknown test '"+testName+"'.");
       
        OWLIndividual testIndividual=ontologyManager.getOWLDataFactory().getOWLIndividual(URI.create(TEST_ID_PREFIX+testID));
        return new WGTestDescriptor(ontologyManager,testContainer,testIndividual);
    }
    protected static OWLOntology loadTestDatabase(OWLOntologyManager ontologyManager) throws Exception {
        ontologyManager.loadOntologyFromPhysicalURI(WGTestRegistry.class.getResource("ontologies/test-ontology.owl").toURI());
        return ontologyManager.loadOntologyFromPhysicalURI(WGTestRegistry.class.getResource("ontologies/all.rdf").toURI());
    }
}
