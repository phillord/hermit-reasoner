// An update for the tests (all.rdf) should regularly be downloaded to the ontologies folder from http://wiki.webont.org/exports/
package org.semanticweb.HermiT.owl_wg_tests;

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
    public static String URI_BASE="http://www.w3.org/2007/OWL/testOntology#";
    public static String TEST_ID_PREFIX="http://km.aifb.uni-karlsruhe.de/projects/owltests/index.php/Special:URIResolver/";

    protected final OWLOntologyManager m_ontologyManager;
    protected final OWLOntology m_testContainer;
    protected final List<WGTestDescriptor> m_testDescriptors;
    protected final Map<String,WGTestDescriptor> m_testDescriptorsByID;
    
    public WGTestRegistry() throws Exception {
        m_ontologyManager=OWLManager.createOWLOntologyManager();
        m_ontologyManager.loadOntologyFromPhysicalURI(WGTestRegistry.class.getResource("ontologies/test-ontology.owl").toURI());
        m_testContainer=m_ontologyManager.loadOntologyFromPhysicalURI(WGTestRegistry.class.getResource("ontologies/all.rdf").toURI());
        m_testDescriptors=new ArrayList<WGTestDescriptor>();
        m_testDescriptorsByID=new HashMap<String,WGTestDescriptor>();
        OWLClass testCaseClass=m_ontologyManager.getOWLDataFactory().getOWLClass(URI.create(URI_BASE+"TestCase"));
        for (OWLClassAssertionAxiom ax : m_testContainer.getClassAssertionAxioms(testCaseClass)) {
            WGTestDescriptor wgTestDescriptor=new WGTestDescriptor(m_ontologyManager,m_testContainer,ax.getIndividual());
            m_testDescriptors.add(wgTestDescriptor);
            m_testDescriptorsByID.put(wgTestDescriptor.testID,wgTestDescriptor);
        }
    }
    public List<WGTestDescriptor> getTestDescriptors() {
        return m_testDescriptors;
    }
    public WGTestDescriptor getDescriptor(String testID) throws Exception {
        return m_testDescriptorsByID.get(testID);
    }
}
