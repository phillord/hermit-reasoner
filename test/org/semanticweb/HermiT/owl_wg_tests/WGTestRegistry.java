// An update for the tests (all.rdf) should regularly be downloaded to the ontologies folder from http://wiki.webont.org/exports/
package org.semanticweb.HermiT.owl_wg_tests;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class WGTestRegistry {
    public static String URI_BASE = "http://www.w3.org/2007/OWL/testOntology#";
    public static String TEST_ID_PREFIX = "http://km.aifb.uni-karlsruhe.de/projects/owltests/index.php/Special:URIResolver/";
    public static final String RESULTS_FILE_PATH = "resultsFilePath";
    
    protected final OWLOntologyManager m_ontologyManager;
    protected final OWLOntology m_testContainer;
    protected final List<WGTestDescriptor> m_testDescriptors;
    protected final Map<String, WGTestDescriptor> m_testDescriptorsByID;

    public WGTestRegistry() throws Exception {
        PrintWriter output;
        String resultsFilePath = System.getProperty(WGTestRegistry.RESULTS_FILE_PATH);
        if (resultsFilePath != null) {
            File file=new File(resultsFilePath);
            output = new PrintWriter(file);
        } else {
            output=new PrintWriter(System.out);
        }
        printResultsHeader(output);
        
        m_ontologyManager = OWLManager.createOWLOntologyManager();
        m_ontologyManager.loadOntologyFromPhysicalURI(WGTestRegistry.class
                .getResource("ontologies/test-ontology.owl").toURI());
        m_testContainer = m_ontologyManager
                .loadOntologyFromPhysicalURI(WGTestRegistry.class.getResource(
                        "ontologies/all.rdf").toURI());
        m_testDescriptors = new ArrayList<WGTestDescriptor>();
        m_testDescriptorsByID = new HashMap<String, WGTestDescriptor>();
        OWLClass testCaseClass = m_ontologyManager.getOWLDataFactory()
                .getOWLClass(URI.create(URI_BASE + "TestCase"));
        for (OWLClassAssertionAxiom ax : m_testContainer
                .getClassAssertionAxioms(testCaseClass)) {
            WGTestDescriptor wgTestDescriptor = new WGTestDescriptor(
                    m_ontologyManager, m_testContainer, ax.getIndividual(),
                    output);
            m_testDescriptors.add(wgTestDescriptor);
            m_testDescriptorsByID
                    .put(wgTestDescriptor.testID, wgTestDescriptor);
        }
    }
    
    protected void printResultsHeader(PrintWriter output) {
        output.println("@prefix : <owlapi:ontology:ont1#> .");
        output.println("@prefix owl2xml: <http://www.w3.org/2006/12/owl2-xml#> .");
        output.println("@prefix testOntology: <http://www.w3.org/2007/OWL/testOntology#> .");
        output.println("@prefix p1: <> .");
        output.println("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .");
        output.println("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .");
        output.println("@prefix testResultOntology: <http://www.w3.org/2007/OWL/testResultOntology#> .");
        output.println("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
        output.println("@prefix owl: <http://www.w3.org/2002/07/owl#> .");
        output.println("@base <owlapi:ontology:ont1> .");
        output.println("");
        output.println("<owlapi:ontology:ont1> rdf:type owl:Ontology ;");
        output.println("  owl:imports <http://www.w3.org/2007/OWL/testResultOntology> .");
        output.println("");
        output.println("testResultOntology:details rdf:type owl:AnnotationProperty .");
        output.println("testResultOntology:runner rdf:type owl:ObjectProperty .");
        output.println("testResultOntology:test rdf:type owl:ObjectProperty .");
        output.println("testOntology:identifier rdf:type owl:DatatypeProperty .");
        output.println("testResultOntology:ConsistencyRun rdf:type owl:Class .");
        output.println("testResultOntology:FailingRun rdf:type owl:Class .");
        output.println("testResultOntology:IncompleteRun rdf:type owl:Class .");
        output.println("testResultOntology:InconsistencyRun rdf:type owl:Class .");
        output.println("testResultOntology:NegativeEntailmentRun rdf:type owl:Class .");
        output.println("testResultOntology:PassingRun rdf:type owl:Class .");
        output.println("testResultOntology:PositiveEntailmentRun rdf:type owl:Class .");
        output.println("testResultOntology:TestRun rdf:type owl:Class .");
        output.println("");
        output.println("testResultOntology:hermit");
        output.println("  a owl:Thing ;");
        output.println("  rdfs:label \"HermiT Reasoner (Version 1.0)\" .");
        output.println("");
        output.flush();
    }

    public List<WGTestDescriptor> getTestDescriptors() {
        return m_testDescriptors;
    }

    public WGTestDescriptor getDescriptor(String testID) throws Exception {
        return m_testDescriptorsByID.get(testID);
    }
}
