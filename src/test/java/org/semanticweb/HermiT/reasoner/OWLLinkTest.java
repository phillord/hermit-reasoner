package org.semanticweb.HermiT.reasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

public class OWLLinkTest extends AbstractReasonerTest {

    public static final String NS="http://example.com/owl/families/";
    public static final String LB=System.getProperty("line.separator");

    public OWLLinkTest(String name) {
        super(name);
    }

    protected void registerMappingToResource(String ontologyIRI,String physicalResource) throws Exception {
        IRI physicalIRI=IRI.create(getClass().getResource(physicalResource).toURI());
        IRI logicalIRI=IRI.create(ontologyIRI);
        m_ontologyManager.addIRIMapper(new SimpleIRIMapper(logicalIRI,physicalIRI));
    }
    
    // below are all the tests from the paper
    // "Who the heck is the father of Bob"
    public void testBobTestAandB() throws Exception {
        String[] ontologies=new String[] { "agent-inst.owl","test.owl","situation-inst.owl","situation.owl","space.owl","agent.owl","time.owl" };
        String base="http://www.iyouit.eu/";
        String mainOntology=base+"agent.owl";
        for (String ont : ontologies)
            registerMappingToResource(base+ont,"res/OWLLink/"+ont);
        m_ontology=m_ontologyManager.loadOntology(IRI.create(mainOntology));
        createReasoner();
        OWLObjectProperty knows=m_dataFactory.getOWLObjectProperty(IRI.create(mainOntology+"#knows"));
        NodeSet<OWLObjectPropertyExpression> peers=m_reasoner.getSubObjectProperties(knows, true);
        assertTrue(peers.getFlattened().size()==20); // Test A from the Bob paper
        peers=m_reasoner.getSubObjectProperties(knows, false);
        assertTrue(peers.getFlattened().size()==101); // Test B from the Bob paper
    }

    public void testUpdatesBuffered() throws Exception {
        String axioms="SubClassOf(:A :B)"+
            "SubClassOf(:B :C)";
        loadOntologyWithAxioms(axioms);

        OWLClass a=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"A"));
        OWLClass b=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"B"));
        OWLClass d=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"D"));
        OWLClass e=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"E"));
        OWLClass f=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"F"));
        OWLClass g=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"G"));

        createOWLReasoner();

        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        changes.add(new AddAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(d, f)));
        changes.add(new RemoveAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(d, f)));
        changes.add(new RemoveAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(a, b)));
        changes.add(new AddAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(d, e)));
        changes.add(new AddAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(e, f)));
        changes.add(new AddAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(e, f)));
        changes.add(new RemoveAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(f, g)));

        // apply changes
        m_ontologyManager.applyChanges(changes);

        assertHierarchies("res/OWLLink/updateHierarchy.txt");
        m_reasoner.flush();
        assertHierarchies("res/OWLLink/updateHierarchyFlushed.txt");
    }
    public void testUpdatesNonBuffered() throws Exception {
        String axioms="SubClassOf(:A :B)"+
            "SubClassOf(:B :C)";
        loadOntologyWithAxioms(axioms);

        OWLClass a=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"A"));
        OWLClass b=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"B"));
        OWLClass d=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"D"));
        OWLClass e=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"E"));
        OWLClass f=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"F"));
        OWLClass g=m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS+"G"));

        Configuration c=getConfiguration();
        c.bufferChanges=false;
        createOWLReasoner(c);

        assertHierarchies("res/OWLLink/updateHierarchy.txt");

        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        changes.add(new AddAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(d, f)));
        changes.add(new RemoveAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(d, f)));
        changes.add(new RemoveAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(a, b)));
        changes.add(new AddAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(d, e)));
        changes.add(new AddAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(e, f)));
        changes.add(new AddAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(e, f)));
        changes.add(new RemoveAxiom(m_ontology, m_dataFactory.getOWLSubClassOfAxiom(f, g)));

        // apply changes
        m_ontologyManager.applyChanges(changes);
        assertHierarchies("res/OWLLink/updateHierarchyFlushed.txt");
    }
    public void testInverses() throws Exception {
        registerMappingToResource("http://www.owllink.org/ontologies/families","res/families.owl");
        loadReasonerFromResource("res/primer.owl");
        m_reasoner.getDisjointObjectProperties(m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(NS+"hasParent")));
    }

    public void testObjectProperties() throws Exception {
        m_ontology=m_ontologyManager.createOntology();
        m_ontologyManager.addAxiom(m_ontology,m_dataFactory.getOWLDeclarationAxiom(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent"))));
        createReasoner();
        m_reasoner.getDisjointObjectProperties(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent")));
    }

    public void testSuccessiveCalls() throws Exception {
        registerMappingToResource("http://www.owllink.org/ontologies/families","res/families.owl");
        loadReasonerFromResource("res/primer.owl");
        try {
            m_reasoner.getDisjointObjectProperties(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent")));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        m_reasoner.getDisjointObjectProperties(m_dataFactory.getOWLObjectProperty(IRI.create(NS+"hasParent")));
    }

    public void testBobTestC() throws Exception {
        String[] ontologies=new String[] { "agent-inst.owl","test.owl","situation-inst.owl","situation.owl","space.owl","agent.owl","time.owl" };
        String base="http://www.iyouit.eu/";
        String mainOntology=base+"agent-inst.owl";
        for (String ont : ontologies)
            registerMappingToResource(base+ont,"res/OWLLink/"+ont);
        m_ontology=m_ontologyManager.loadOntology(IRI.create(mainOntology));
        createReasoner();
        m_reasoner.getPrefixes().declareDefaultPrefix("http://www.iyouit.eu/agent-inst.owl#");
        OWLNamedIndividual e1079=m_dataFactory.getOWLNamedIndividual(IRI.create(mainOntology+"#1079"));
        OWLObjectProperty colleague=m_dataFactory.getOWLObjectProperty(IRI.create(mainOntology+"#colleague"));
        int[] expected= { 1079,1084,1086,1096,1098,1126,1127,1183 };
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
    public void testDisjointProperties() throws Exception {
        registerMappingToResource("http://www.owllink.org/ontologies/families","res/families.owl");
        loadReasonerFromResource("res/primer.owl");
        OWLObjectProperty hasParent=m_dataFactory.getOWLObjectProperty(IRI.create("http://example.com/owl/families/hasParent"));
        OWLObjectProperty hasSpouse=m_dataFactory.getOWLObjectProperty(IRI.create("http://example.com/owl/families/hasSpouse"));
        assertTrue(m_reasoner.isEntailed(m_dataFactory.getOWLDisjointObjectPropertiesAxiom(hasParent,hasSpouse)));

        NodeSet<OWLObjectPropertyExpression> result=m_reasoner.getDisjointObjectProperties(m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(NS+"hasSpouse")));
        Set<Node<OWLObjectPropertyExpression>> expectedSet=new HashSet<Node<OWLObjectPropertyExpression>>();
        String[][] disjoints=new String[][]{
                new String[]{"http://example.com/owl/families/hasFather"},
                new String[] {"http://example.com/owl/families/hasParent", "InverseOf(http://example.org/otherOntologies/families/child)", "InverseOf(http://example.com/owl/families/hasChild)"},
                new String[] {"http://example.com/owl/families/hasChild", "http://example.org/otherOntologies/families/child", "InverseOf(http://example.com/owl/families/hasParent)"},
                new String[] {"InverseOf(http://example.com/owl/families/hasFather)"}
        };
        OWLObjectPropertyExpression ope;
        Node<OWLObjectPropertyExpression> node;
        for (String[] disjointStrings : disjoints) {
            Set<OWLObjectPropertyExpression> opeSet=new HashSet<OWLObjectPropertyExpression>();
            for (String opeString : disjointStrings) {
                if (opeString.startsWith("InverseOf(")) {
                    String opString=opeString.substring(10,opeString.length()-1);
                    OWLObjectProperty op=m_dataFactory.getOWLObjectProperty(IRI.create(opString));
                    ope=m_dataFactory.getOWLObjectInverseOf(op);
                }
                else
                    ope=m_dataFactory.getOWLObjectProperty(IRI.create(opeString));
                opeSet.add(ope);
            }
            node=new OWLObjectPropertyNode(opeSet);
            expectedSet.add(node);
        }
        node=new OWLObjectPropertyNode(m_dataFactory.getOWLBottomObjectProperty());
        expectedSet.add(node);
        NodeSet<OWLObjectPropertyExpression> expected=new OWLObjectPropertyNodeSet(expectedSet);
        assertTrue(expected.getFlattened().size()==result.getFlattened().size());
        assertTrue(expected.getNodes().size()==result.getNodes().size());
        for (OWLObjectPropertyExpression o : expected.getFlattened())
            assertTrue(result.containsEntity(o));

        result=m_reasoner.getDisjointObjectProperties(m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(NS+"hasParent")));
        expectedSet=new HashSet<Node<OWLObjectPropertyExpression>>();
        disjoints=new String[][]{
                new String[] {"http://example.com/owl/families/hasSpouse", "InverseOf(http://example.com/owl/families/hasSpouse)"},
                new String[] {"http://example.com/owl/families/hasWife"},
                new String[] {"InverseOf(http://example.com/owl/families/hasWife)"},
                new String[] {"http://example.com/owl/families/hasChild", "http://example.org/otherOntologies/families/child", "InverseOf(http://example.com/owl/families/hasParent)"},
                new String[] {"InverseOf(http://example.com/owl/families/hasFather)"}
        };
        for (String[] disjointStrings : disjoints) {
            Set<OWLObjectPropertyExpression> opeSet=new HashSet<OWLObjectPropertyExpression>();
            for (String opeString : disjointStrings) {
                if (opeString.startsWith("InverseOf(")) {
                    String opString=opeString.substring(10,opeString.length()-1);
                    OWLObjectProperty op=m_dataFactory.getOWLObjectProperty(IRI.create(opString));
                    ope=m_dataFactory.getOWLObjectInverseOf(op);
                }
                else
                    ope=m_dataFactory.getOWLObjectProperty(IRI.create(opeString));
                opeSet.add(ope);
            }
            node=new OWLObjectPropertyNode(opeSet);
            expectedSet.add(node);
        }
        node=new OWLObjectPropertyNode(m_dataFactory.getOWLBottomObjectProperty());
        expectedSet.add(node);
        expected=new OWLObjectPropertyNodeSet(expectedSet);
        assertTrue(expected.getFlattened().size()==result.getFlattened().size());
        assertTrue(expected.getNodes().size()==result.getNodes().size());
        for (OWLObjectPropertyExpression o : expected.getFlattened())
            assertTrue(result.containsEntity(o));
    }
    public void testDisjointClasses() throws Exception {
        registerMappingToResource("http://www.owllink.org/ontologies/families","res/families.owl");
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
        NodeSet<OWLClass> result=m_reasoner.getDisjointClasses(families);
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
        try {
            createReasoner();
            fail();
        }
        catch (IllegalArgumentException e) {
            if (!e.getMessage().contains("Non-simple property"))
                fail(e.getMessage());
        }
    }
}
