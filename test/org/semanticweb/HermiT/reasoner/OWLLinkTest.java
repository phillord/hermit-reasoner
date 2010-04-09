package org.semanticweb.HermiT.reasoner;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

public class OWLLinkTest extends AbstractReasonerTest {

    public static final String NS="http://example.com/owl/families/";

    public OWLLinkTest(String name) {
        super(name);
    }

    public void testInverses() throws Exception {
        loadReasonerFromResource("res/primer.owl");
        m_reasoner.getDisjointObjectProperties(m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(NS+"hasParent")),false);
    }

    public void testObjectProperties() throws Exception {
        m_ontologyManager.addAxiom(m_ontology,m_dataFactory.getOWLDeclarationAxiom(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent"))));
        createReasoner();
        m_reasoner.getDisjointObjectProperties(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent")),false);
    }

    public void testSuccessiveCalls() throws Exception {
        loadReasonerFromResource("res/primer.owl");
        try {
            m_reasoner.getDisjointObjectProperties(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent")),false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        m_reasoner.getDisjointObjectProperties(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent")),false);
    }

    public void testTooManyProperties() throws Exception {
        String[] ontologies=new String[] { "agent-inst.owl","test.owl","situation-inst.owl","situation.owl","space.owl","agent.owl","time.owl" };
        String base="http://www.iyouit.eu/";
        String mainOntology=base+"agent-inst.owl";
        for (String ont : ontologies) {
            IRI physicalIRI=IRI.create(getClass().getResource("res/OWLLink/"+ont).toURI());
            IRI logicalIRI=IRI.create(base+ont);
            m_ontologyManager.addIRIMapper(new SimpleIRIMapper(logicalIRI,physicalIRI));
        }
        m_ontology=m_ontologyManager.loadOntology(IRI.create(mainOntology));
        createReasoner();
        OWLNamedIndividual e1079=m_dataFactory.getOWLNamedIndividual(IRI.create(mainOntology+"#1079"));
        OWLObjectProperty colleague=m_dataFactory.getOWLObjectProperty(IRI.create(mainOntology+"#colleague"));
        int[] expected= { 1086,1127,1098,1126,1096,1084,1079,1183 };
        Set<OWLNamedIndividual> expectedValues=new HashSet<OWLNamedIndividual>();
        for (int i=0;i<expected.length;i++) {
            expectedValues.add(m_dataFactory.getOWLNamedIndividual(IRI.create(mainOntology+"#"+expected[i])));
        }
        // m_reasoner.prepareReasoner();
        NodeSet<OWLNamedIndividual> peers=m_reasoner.getObjectPropertyValues(e1079,colleague);
        assertTrue(expected.length==peers.getFlattened().size());
        for (Node<OWLNamedIndividual> i : peers.getNodes())
            for (OWLNamedIndividual ni : i.getEntities()) {
                assertTrue(expectedValues.contains(ni));
            }
    }
}
