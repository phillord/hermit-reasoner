package org.semanticweb.HermiT.owl_wg_tests;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import junit.framework.TestSuite;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

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
    public TestSuite createSuite(Set<WGTestDescriptor.Status> includeStatus,boolean includeStatusless) throws Exception {
        TestSuite suite=new TestSuite("OWL WG Tests");
        for (WGTestDescriptor wgTestDescriptor : m_testDescriptors) {
            if (((wgTestDescriptor.status==null && includeStatusless) || (wgTestDescriptor.status!=null && includeStatus.contains(wgTestDescriptor.status))) && wgTestDescriptor.isDLTest()) {
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
}
