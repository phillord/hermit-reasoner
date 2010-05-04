package org.semanticweb.HermiT.reasoner;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;

public class OWLLinkTest extends AbstractReasonerTest {

    public static final String NS="http://example.com/owl/families/";

    public OWLLinkTest(String name) {
        super(name);
    }

//    public void testInverses() throws Exception {
//        loadReasonerFromResource("res/primer.owl");
//        m_reasoner.getDisjointObjectProperties(m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(NS+"hasParent")),false);
//    }
//
//    public void testObjectProperties() throws Exception {
//        m_ontology=m_ontologyManager.createOntology();
//        m_ontologyManager.addAxiom(m_ontology,m_dataFactory.getOWLDeclarationAxiom(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent"))));
//        createReasoner();
//        m_reasoner.getDisjointObjectProperties(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent")),false);
//    }
//
//    public void testSuccessiveCalls() throws Exception {
//        loadReasonerFromResource("res/primer.owl");
//        try {
//            m_reasoner.getDisjointObjectProperties(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent")),false);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        m_reasoner.getDisjointObjectProperties(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent")),false);
//    }
//    // below are all the tests from the paper
//    // "Who the heck is the father of Bob"
//    public void testBobTestAandB() throws Exception {
//        String[] ontologies=new String[] { "agent-inst.owl","test.owl","situation-inst.owl","situation.owl","space.owl","agent.owl","time.owl" };
//        String base="http://www.iyouit.eu/";
//        String mainOntology=base+"agent.owl";
//        for (String ont : ontologies) {
//            IRI physicalIRI=IRI.create(getClass().getResource("res/OWLLink/"+ont).toURI());
//            IRI logicalIRI=IRI.create(base+ont);
//            m_ontologyManager.addIRIMapper(new SimpleIRIMapper(logicalIRI,physicalIRI));
//        }
//        m_ontology=m_ontologyManager.loadOntology(IRI.create(mainOntology));
//        createReasoner();
//        OWLObjectProperty knows=m_dataFactory.getOWLObjectProperty(IRI.create(mainOntology+"#knows"));
//        NodeSet<OWLObjectProperty> peers=m_reasoner.getSubObjectProperties(knows, true);
//        assertTrue(peers.getFlattened().size()==10); // Test A from the Bob paper
//        peers=m_reasoner.getSubObjectProperties(knows, false);
//        assertTrue(peers.getFlattened().size()==51); // Test B from the Bob paper
//    }
//    
//    public void testBobTestC() throws Exception {
//        String[] ontologies=new String[] { "agent-inst.owl","test.owl","situation-inst.owl","situation.owl","space.owl","agent.owl","time.owl" };
//        String base="http://www.iyouit.eu/";
//        String mainOntology=base+"agent-inst.owl";
//        for (String ont : ontologies) {
//            IRI physicalIRI=IRI.create(getClass().getResource("res/OWLLink/"+ont).toURI());
//            IRI logicalIRI=IRI.create(base+ont);
//            m_ontologyManager.addIRIMapper(new SimpleIRIMapper(logicalIRI,physicalIRI));
//        }
//        m_ontology=m_ontologyManager.loadOntology(IRI.create(mainOntology));
//        createReasoner();
//        OWLNamedIndividual e1079=m_dataFactory.getOWLNamedIndividual(IRI.create(mainOntology+"#1079"));
//        OWLObjectProperty colleague=m_dataFactory.getOWLObjectProperty(IRI.create(mainOntology+"#colleague"));
//        int[] expected= { 1086,1127,1098,1126,1096,1084,1079,1183 };
//        Set<OWLNamedIndividual> expectedValues=new HashSet<OWLNamedIndividual>();
//        for (int i=0;i<expected.length;i++) {
//            expectedValues.add(m_dataFactory.getOWLNamedIndividual(IRI.create(mainOntology+"#"+expected[i])));
//        }
//        // m_reasoner.prepareReasoner();
//        NodeSet<OWLNamedIndividual> peers=m_reasoner.getObjectPropertyValues(e1079,colleague);
//        assertTrue(expected.length==peers.getFlattened().size());
//        for (Node<OWLNamedIndividual> i : peers.getNodes())
//            for (OWLNamedIndividual ni : i.getEntities()) {
//                assertTrue(expectedValues.contains(ni));
//            }
//    }
    public void testDisjointProperties() throws Exception {
        loadReasonerFromResource("res/primer.owl");
        OWLObjectProperty hasParent=m_dataFactory.getOWLObjectProperty(IRI.create("http://example.com/owl/families/hasParent"));
        OWLObjectProperty hasSpouse=m_dataFactory.getOWLObjectProperty(IRI.create("http://example.com/owl/families/hasSpouse"));
        assertTrue(m_reasoner.isEntailed(m_dataFactory.getOWLDisjointObjectPropertiesAxiom(hasParent,hasSpouse)));
        
        NodeSet<OWLObjectProperty> result=m_reasoner.getDisjointObjectProperties(m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(NS+"hasSpouse")),false);
        Set<Node<OWLObjectProperty>> expectedSet=new HashSet<Node<OWLObjectProperty>>();
        String[][] disjoints=new String[][]{
                new String[]{"http://example.com/owl/families/hasFather"}, 
                new String[] {"http://example.com/owl/families/hasParent"}, 
                new String[] {"http://example.com/owl/families/hasChild", "http://example.org/otherOntologies/families/child"}
        };
        OWLObjectProperty op;
        Node<OWLObjectProperty> node;
        for (String[] s : disjoints) {
            Set<OWLObjectProperty> opSet=new HashSet<OWLObjectProperty>();
            for (String ops : s) {
                op=m_dataFactory.getOWLObjectProperty(IRI.create(ops));
                opSet.add(op);
            }
            node=new OWLObjectPropertyNode(opSet);
            expectedSet.add(node);
        }
        node=new OWLObjectPropertyNode(m_dataFactory.getOWLBottomObjectProperty());
        expectedSet.add(node);
        NodeSet<OWLObjectProperty> expected=new OWLObjectPropertyNodeSet(expectedSet);
        assertTrue(expected.getFlattened().size()==result.getFlattened().size());
        assertTrue(expected.getNodes().size()==result.getNodes().size());
        for (OWLObjectProperty o : expected.getFlattened())
            assertTrue(result.containsEntity(o));
        
        result=m_reasoner.getDisjointObjectProperties(m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(NS+"hasParent")),false);
        expectedSet=new HashSet<Node<OWLObjectProperty>>();
        disjoints=new String[][]{
                new String[]{"http://example.com/owl/families/hasSpouse"}, 
                new String[] {"http://example.com/owl/families/hasWife"}, 
                new String[] {"http://example.com/owl/families/hasChild", "http://example.org/otherOntologies/families/child"}
        };
        for (String[] s : disjoints) {
            Set<OWLObjectProperty> opSet=new HashSet<OWLObjectProperty>();
            for (String ops : s) {
                op=m_dataFactory.getOWLObjectProperty(IRI.create(ops));
                opSet.add(op);
            }
            node=new OWLObjectPropertyNode(opSet);
            expectedSet.add(node);
        }
        node=new OWLObjectPropertyNode(m_dataFactory.getOWLBottomObjectProperty());
        expectedSet.add(node);
        expected=new OWLObjectPropertyNodeSet(expectedSet);
        assertTrue(expected.getFlattened().size()==result.getFlattened().size());
        assertTrue(expected.getNodes().size()==result.getNodes().size());
        for (OWLObjectProperty o : expected.getFlattened())
            assertTrue(result.containsEntity(o));
    }
    public void testDisjointClasses() throws Exception {
        loadReasonerFromResource("res/primer.owl");
        OWLClass families=m_dataFactory.getOWLClass(IRI.create("http://example.com/owl/families/Father"));
        Set<Node<OWLClass>> expectedSet=new HashSet<Node<OWLClass>>();
        String[] disjoints={"Mother", "YoungChild", "ChildlessPerson", "Woman"};
        OWLClass cls;
        Node<OWLClass> node;
        for (String s : disjoints) {
            cls=m_dataFactory.getOWLClass(IRI.create("http://example.com/owl/families/"+s));
            node=new OWLClassNode(cls);
            expectedSet.add(node);
        }
        node=new OWLClassNode(m_dataFactory.getOWLNothing());
        expectedSet.add(node);
        NodeSet<OWLClass> expected=new OWLClassNodeSet(expectedSet);
        NodeSet<OWLClass> result=m_reasoner.getDisjointClasses(families, false);
        assertTrue(expected.getFlattened().size()==result.getFlattened().size());
        assertTrue(expected.getNodes().size()==result.getNodes().size());
        for (OWLClass c : expected.getFlattened())
            assertTrue(result.containsEntity(c));
    }
    public void testBobTests() throws Exception {
        // Tests 1a to 3b check for cardinality merging abilities within different expressive language fragments. 
        // Tests 4 to 6 focus on blocking abilities with or without inverse properties.
        // Test 7 is for nominals. 
        // Test 8 tests the open world assumption., <rdf:Description about="&prem;I"> should have rdf:about (fixed locally)
        // Tests 9 to 10b test property filler merging. 
        // Test 11 is a combined syntax/special test case. It checks whether the systems handle empty 
        // unions, intersections, or enumerations logically correctly. 
        // Test 12 not tested in the Bob paper, I can't see why the entailment should follow 
        // Test 13 tests individual merging.  
        // Tests 14 is a nominal test with merging. 
        // Test 15 tests reasoning with inverse roles.
        // Test 16 is a nominal test. 
        // Test 18 tests A or not A equiv top.
        // Test 19 is a syntax check whether there are complex properties which are not allowed 
        // within at-most cardinality restrictions and a transitive property which cannot be a 
        // sub-property of a functional property.
        // Test 20 tests an infinite model. 
        // Test 21 is a datatype property test and builds up a datatype property hierarchy, 
        // assigns some fillers and checks whether the system assume that datatype properties are 
        // functional per se (which they are not). 
        // <rdf:Description about="&prem;i1"> should have rdf:about (fixed locally)
        // Test 22 defines an unsatisfiable class due to conflicting range restrictions of a datatype (>= 0 <= 1).
        // Test 23 is a simple partitioning test.
        // Test 25 is an open world nominal test, <rdf:Description about="&prem;a">  should have rdf:about (fixed locally)
        // Test 26 is a kind of nominal merging test.
        // Test 27 aims at an sub-property entailment with help of nominals. 
        // Test 28 is an and-branching test and checks efficient propagation of filler restrictions and non-determinism. 
        // The test 29a/b is again a cardinality merging problem (with non-determinism).
        
        // "1b", "2b" removed because HermiT has timeouts
        
        String[] tests=new String[] { "1a", "2a", "2c", "3a", "3b", "4", "5", "6", "7", "8", "9", 
              "10a", "10b", "11", "13", "14", "15", "16", "17", "18", 
              "20", "21", "22", "23", "24", "25", "26", "27", "28", "29a"};

        for (String testName : tests) {
            boolean testInconsistency=(testName.equals("17") ? true : false );
            IRI physicalIRI=IRI.create(getClass().getResource("res/OWLLink/"+testName+".owl").toURI());
            m_ontology=m_ontologyManager.loadOntology(physicalIRI);
            createReasoner();
            OWLOntology c=null;
            if (!testInconsistency) { 
                physicalIRI=IRI.create(getClass().getResource("res/OWLLink/"+testName+"-conclusion.owl").toURI());
                c=m_ontologyManager.loadOntology(physicalIRI); 
            }
            boolean result=testInconsistency ? !m_reasoner.isConsistent() : m_reasoner.isEntailed(c.getLogicalAxioms());
            assertTrue("Test "+testName+" failed! ",result);
        }
        
        // Test 19 is for non-simple properties in number restrictions
        IRI physicalIRI=IRI.create(getClass().getResource("res/OWLLink/19.owl").toURI());
        m_ontology=m_ontologyManager.loadOntology(physicalIRI);
        boolean errorSpotted=false;
        try {
            createReasoner();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Non-simple property"))
                errorSpotted=true;
            else
                throw new Exception(e.getMessage());
        }
        assertTrue(errorSpotted);
    }
}
