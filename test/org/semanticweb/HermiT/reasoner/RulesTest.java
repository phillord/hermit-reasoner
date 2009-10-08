package org.semanticweb.HermiT.reasoner;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLIndividualVariable;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLLiteralVariable;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.vocab.OWLFacet;


public class RulesTest extends AbstractReasonerTest {

    public RulesTest(String name) {
        super(name);
    }

    protected Configuration getConfiguration() {
        Configuration c=new Configuration();
//        c.tableauMonitorType=TableauMonitorType.DEBUGGER_HISTORY_ON;
//        c.checkClauses=false;
        return c;
    }

    public void testSimpleRule() throws Exception {
        String axioms = "SubClassOf(:A :B)"
            + "ClassAssertion(:A :a)"
            + "ClassAssertion(:D :b)";
        loadOntologyWithAxioms(axioms);
//      + "Rule(Body(ClassAtom(:B IndividualVariable(:x))) Head(ClassAtom(:C IndividualVariable(:x))))";
        // B(x) -> C(x)
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        
        SWRLAtom body=m_dataFactory.getSWRLClassAtom(B, x);
        SWRLAtom head=m_dataFactory.getSWRLClassAtom(C, x);
        SWRLRule rule=m_dataFactory.getSWRLRule(Collections.singleton(body), Collections.singleton(head));
        m_ontologyManager.addAxiom(m_ontology, rule);
        
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
    }
    
    public void testRuleWithConstants() throws Exception {
        String axioms = "SubClassOf(:A :B)"
            + "ClassAssertion(:A :a)"
            + "ClassAssertion(:D :b)";
        loadOntologyWithAxioms(axioms);
//      + "Rule(Body(ClassAtom(:B IndividualVariable(:x))) Head(ClassAtom(:C :a)))";
        // B(x) -> C(a)
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLIndividualArgument aarg = m_dataFactory.getSWRLIndividualArgument(a);
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        
        SWRLAtom body =m_dataFactory.getSWRLClassAtom(B, x);
        SWRLAtom head=m_dataFactory.getSWRLClassAtom(C, aarg);
        SWRLRule rule=m_dataFactory.getSWRLRule(Collections.singleton(body), Collections.singleton(head));
        m_ontologyManager.addAxiom(m_ontology, rule);
        
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
    }
    
    public void testRuleWithConstants2() throws Exception {
        String axioms = "ClassAssertion(ObjectSomeValuesFrom(:r owl:Thing) :a)"
            + "ObjectPropertyAssertion(:r :a :b)";
        loadOntologyWithAxioms(axioms);
        // r(x, y) -> s(x, y)
        // r(x, b) -> sb(x, b)
        // s(a, x) -> sa(a, b)
        // r(a, b) -> q(a, b)
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "r"));
        OWLObjectProperty s = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "s"));
        OWLObjectProperty sa = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "sa"));
        OWLObjectProperty sb = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "sb"));
        OWLObjectProperty q = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "q"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLIndividualVariable y = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "y"));
        SWRLIndividualArgument aarg = m_dataFactory.getSWRLIndividualArgument(a);
        SWRLIndividualArgument barg = m_dataFactory.getSWRLIndividualArgument(b);

        SWRLAtom body =m_dataFactory.getSWRLObjectPropertyAtom(r, x, y);
        SWRLAtom head=m_dataFactory.getSWRLObjectPropertyAtom(s, x, y);
        SWRLRule rule=m_dataFactory.getSWRLRule(Collections.singleton(body), Collections.singleton(head));
        m_ontologyManager.addAxiom(m_ontology, rule);
        body=m_dataFactory.getSWRLObjectPropertyAtom(r, x, barg);
        head=m_dataFactory.getSWRLObjectPropertyAtom(sb, x, barg);
        rule=m_dataFactory.getSWRLRule(Collections.singleton(body), Collections.singleton(head));
        m_ontologyManager.addAxiom(m_ontology, rule);
        body=m_dataFactory.getSWRLObjectPropertyAtom(s, aarg, x);
        head=m_dataFactory.getSWRLObjectPropertyAtom(sa, aarg, barg);
        rule=m_dataFactory.getSWRLRule(Collections.singleton(body), Collections.singleton(head));
        m_ontologyManager.addAxiom(m_ontology, rule);
        body=m_dataFactory.getSWRLObjectPropertyAtom(r, aarg, barg);
        head=m_dataFactory.getSWRLObjectPropertyAtom(q, aarg, barg);
        rule=m_dataFactory.getSWRLRule(Collections.singleton(body), Collections.singleton(head));
        m_ontologyManager.addAxiom(m_ontology, rule);
        
        createReasoner();
        Set<OWLNamedIndividual> result=m_reasoner.getRelatedIndividuals(a, r);
        assertTrue(result.size()==1 && result.contains(b));
        result=m_reasoner.getRelatedIndividuals(a, s);
        assertTrue(result.size()==1 && result.contains(b));
        result=m_reasoner.getRelatedIndividuals(a, sa);
        assertTrue(result.size()==1 && result.contains(b));
        result=m_reasoner.getRelatedIndividuals(a, sb);
        assertTrue(result.size()==1 && result.contains(b));
        result=m_reasoner.getRelatedIndividuals(a, q);
        assertTrue(result.size()==1 && result.contains(b));
    }
    
    public void testRuleWithDatatypes() throws Exception {
        String axioms = "DataPropertyAssertion(:dp :a \"18\"^^xsd:short)"
            + "DataPropertyAssertion(:dp :b \"17\"^^xsd:short)";
        loadOntologyWithAxioms(axioms);
        // dp(x, "18"^^xsd:integer) -> C(x)
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLLiteralArgument lit = m_dataFactory.getSWRLLiteralArgument(m_dataFactory.getOWLTypedLiteral(18));
        OWLDataProperty dp = m_dataFactory.getOWLDataProperty(IRI.create(AbstractReasonerTest.NS + "dp"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        
        SWRLAtom body =m_dataFactory.getSWRLDataPropertyAtom(dp, x, lit);
        SWRLAtom head=m_dataFactory.getSWRLClassAtom(C, x);
        SWRLRule rule=m_dataFactory.getSWRLRule(Collections.singleton(body), Collections.singleton(head));
        m_ontologyManager.addAxiom(m_ontology, rule);
        
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
    }
    
    
    public void testRuleWithDatatypes2() throws Exception {
        String axioms = "ClassAssertion(DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"10\"^^xsd:integer)) :a)"
            + "DataPropertyAssertion(:dp :b \"10\"^^xsd:short)"
            + "DataPropertyAssertion(:dp :c \"25\"^^xsd:integer)"
            + "ClassAssertion(ObjectComplementOf(:C) :a)";
        loadOntologyWithAxioms(axioms);
        // dp(x, y) /\ DatatypeRestriction(xsd:int xsd:minInclusive "15"^^xsd:int)(y) -> C(x)
        // dp(x, y) /\ DataComplementOf(DatatypeRestriction(xsd:int xsd:minInclusive "15"^^xsd:int))(y) -> D(x)
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLNamedIndividual c = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "c"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLLiteralVariable y = m_dataFactory.getSWRLLiteralVariable(IRI.create(AbstractReasonerTest.NS + "y"));
        OWLDataProperty dp = m_dataFactory.getOWLDataProperty(IRI.create(AbstractReasonerTest.NS + "dp"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        OWLClass D = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "D"));
        
        Set<SWRLAtom> bodies1=new HashSet<SWRLAtom>();
        SWRLAtom bodyDP=m_dataFactory.getSWRLDataPropertyAtom(dp, x, y);
        bodies1.add(bodyDP);
        bodies1.add(m_dataFactory.getSWRLDataRangeAtom(m_dataFactory.getOWLDatatypeRestriction(m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#int")), m_dataFactory.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, 15)), y));
        SWRLRule rule1=m_dataFactory.getSWRLRule(bodies1, Collections.singleton(m_dataFactory.getSWRLClassAtom(C, x)));
        m_ontologyManager.addAxiom(m_ontology, rule1);
        Set<SWRLAtom> bodies2=new HashSet<SWRLAtom>();
        bodies2.add(bodyDP);
        bodies2.add(m_dataFactory.getSWRLDataRangeAtom(m_dataFactory.getOWLDataComplementOf(m_dataFactory.getOWLDatatypeRestriction(m_dataFactory.getOWLDatatype(IRI.create("http://www.w3.org/2001/XMLSchema#int")), m_dataFactory.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, 15))), y));
        SWRLRule rule2=m_dataFactory.getSWRLRule(bodies2, Collections.singleton(m_dataFactory.getSWRLClassAtom(D, x)));
        m_ontologyManager.addAxiom(m_ontology, rule2);
        
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(c));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(D, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(D, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(D, false).contains(c));
    }
    
    public void testRuleWithFreshIndividuals() throws Exception {
        String axioms = "ClassAssertion(:A :a)";
        loadOntologyWithAxioms(axioms);
        // A(x) -> B(b)
        // B(x) -> C(x)
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "A"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLIndividualArgument barg = m_dataFactory.getSWRLIndividualArgument(b);
        
        SWRLAtom body =m_dataFactory.getSWRLClassAtom(A, x);
        SWRLAtom head=m_dataFactory.getSWRLClassAtom(B, barg);
        SWRLRule rule=m_dataFactory.getSWRLRule(Collections.singleton(body), Collections.singleton(head));
        m_ontologyManager.addAxiom(m_ontology, rule);
        body =m_dataFactory.getSWRLClassAtom(B, x);
        head=m_dataFactory.getSWRLClassAtom(C, x);
        rule=m_dataFactory.getSWRLRule(Collections.singleton(body), Collections.singleton(head));
        m_ontologyManager.addAxiom(m_ontology, rule);
        
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(B, false).contains(b));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(B, false).contains(a));
    }
    
    public void testAddingFactsByRules() throws Exception {
        String axioms = "ClassAssertion(:A :a)";
        loadOntologyWithAxioms(axioms);
        // -> B(a)
        // -> B(b)
        // B(x) -> C(x)
        // B(x) /\ A(x) -> D(x)
        // B(x) /\ D(y) -> E(e)
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLNamedIndividual e = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "e"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "A"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        OWLClass D = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "D"));
        OWLClass E = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "E"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLIndividualVariable y = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "y"));
        SWRLIndividualArgument aArg = m_dataFactory.getSWRLIndividualArgument(a);
        SWRLIndividualArgument bArg = m_dataFactory.getSWRLIndividualArgument(b);
        SWRLIndividualArgument eArg = m_dataFactory.getSWRLIndividualArgument(e);
        
        SWRLAtom Ba =m_dataFactory.getSWRLClassAtom(B, aArg);
        SWRLAtom Bb =m_dataFactory.getSWRLClassAtom(B, bArg);
        SWRLAtom Bx =m_dataFactory.getSWRLClassAtom(B, x);
        SWRLAtom Cx =m_dataFactory.getSWRLClassAtom(C, x);
        SWRLAtom Ax =m_dataFactory.getSWRLClassAtom(A, x);
        SWRLAtom Dx =m_dataFactory.getSWRLClassAtom(D, x);
        SWRLAtom Dy =m_dataFactory.getSWRLClassAtom(D, y);
        SWRLAtom Ee =m_dataFactory.getSWRLClassAtom(E, eArg);
        
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(new HashSet<SWRLAtom>(), Collections.singleton(Ba)));
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(new HashSet<SWRLAtom>(), Collections.singleton(Bb)));
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(Collections.singleton(Bx), Collections.singleton(Cx)));
        Set<SWRLAtom> body1=new HashSet<SWRLAtom>();
        body1.add(Bx);
        body1.add(Ax);
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(body1, Collections.singleton(Dx)));
        Set<SWRLAtom> body2=new HashSet<SWRLAtom>();
        body2.add(Bx);
        body2.add(Dy);
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(body2, Collections.singleton(Ee)));
        
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(A, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(A, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(B, false).contains(a));
        assertTrue(m_reasoner.getIndividuals(B, false).contains(b));
        assertTrue(!m_reasoner.getIndividuals(B, false).contains(e));
        assertTrue(m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(m_reasoner.getIndividuals(C, false).contains(b));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(e));
        assertTrue(m_reasoner.getIndividuals(D, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(D, false).contains(b));
        assertTrue(!m_reasoner.getIndividuals(D, false).contains(e));
        assertTrue(!m_reasoner.getIndividuals(E, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(E, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(E, false).contains(e));
    }
    
    public void testLloydTopor() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
            + "ClassAssertion(:B :b)";
        loadOntologyWithAxioms(axioms);
        // A(x) -> B(x) /\ C(x)
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "A"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLAtom Ax =m_dataFactory.getSWRLClassAtom(A, x);
        SWRLAtom Bx =m_dataFactory.getSWRLClassAtom(B, x);
        SWRLAtom Cx =m_dataFactory.getSWRLClassAtom(C, x);
        
        Set<SWRLAtom> head1=new HashSet<SWRLAtom>();
        head1.add(Bx);
        head1.add(Cx);
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(Collections.singleton(Ax), head1));
        
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(A, false).contains(a));
        assertTrue(m_reasoner.getIndividuals(B, false).contains(a));
        assertTrue(m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(A, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(B, false).contains(b));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
    }
    
    public void testDRInHead() throws Exception {
        String axioms = "ClassAssertion(:A :a)";
        loadOntologyWithAxioms(axioms);
        // -> xsd:short("15"^^xsd:int)
        SWRLLiteralArgument byte15 = m_dataFactory.getSWRLLiteralArgument(m_dataFactory.getOWLTypedLiteral(15));
        OWLDatatype dt=m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#short"));
        SWRLDataRangeAtom dra=m_dataFactory.getSWRLDataRangeAtom(dt, byte15);
        
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(new HashSet<SWRLAtom>(), Collections.singleton(dra)));
        
        createReasoner();
        assertTrue(m_reasoner.isConsistent());
    }
    
    public void testDRInHead2() throws Exception {
        String axioms = "";
        loadOntologyWithAxioms(axioms);
        // -> xsd:short("15"^^xsd:int)
        SWRLLiteralArgument byte15 = m_dataFactory.getSWRLLiteralArgument(m_dataFactory.getOWLTypedLiteral(15));
        OWLDatatype dt=m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#short"));
        SWRLDataRangeAtom dra=m_dataFactory.getSWRLDataRangeAtom(dt, byte15);
        
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(new HashSet<SWRLAtom>(), Collections.singleton(dra)));
        
        createReasoner();
        assertTrue(m_reasoner.isConsistent());
    }
    
    public void testDRInHead3() throws Exception {
        String axioms = "";
        loadOntologyWithAxioms(axioms);
        // -> xsd:byte("10000"^^xsd:integer)
        SWRLLiteralArgument integer10000 = m_dataFactory.getSWRLLiteralArgument(m_dataFactory.getOWLTypedLiteral(10000));
        OWLDatatype dt=m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#byte"));
        SWRLDataRangeAtom dra=m_dataFactory.getSWRLDataRangeAtom(dt, integer10000);
        
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(new HashSet<SWRLAtom>(), Collections.singleton(dra)));
        
        createReasoner();
        assertTrue(!m_reasoner.isConsistent());
    }
    
    public void testDRSafety() throws Exception {
        String axioms = "ClassAssertion(:A :a)";
        loadOntologyWithAxioms(axioms);
        // A(x) /\ xsd:integer(y) -> dp(x, y)
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLLiteralVariable y = m_dataFactory.getSWRLLiteralVariable(IRI.create(AbstractReasonerTest.NS + "y"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "A"));
        SWRLAtom Ax =m_dataFactory.getSWRLClassAtom(A, x);
        OWLDatatype dt=m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#integer"));
        SWRLDataRangeAtom integery=m_dataFactory.getSWRLDataRangeAtom(dt, y);
        OWLDataProperty dp = m_dataFactory.getOWLDataProperty(IRI.create(AbstractReasonerTest.NS + "dp"));
        SWRLDataPropertyAtom dpxy=m_dataFactory.getSWRLDataPropertyAtom(dp, x, y);
        
        Set<SWRLAtom> body1=new HashSet<SWRLAtom>();
        body1.add(Ax);
        body1.add(integery);
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(body1, Collections.singleton(dpxy)));
        
        boolean catched=false;
        try {
            createReasoner();
        } catch (IllegalArgumentException e) {
            catched=true;
        }
        assertTrue(catched);
    }
    
    public void testNormalSafety() throws Exception {
        String axioms = "ClassAssertion(:A :a)";
        loadOntologyWithAxioms(axioms);
        // A(x) -> r(x, y)
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLIndividualVariable y = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "y"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "A"));
        SWRLAtom Ax =m_dataFactory.getSWRLClassAtom(A, x);
        OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "r"));
        SWRLObjectPropertyAtom rxy=m_dataFactory.getSWRLObjectPropertyAtom(r, x, y);
        
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(Collections.singleton(Ax), Collections.singleton(rxy)));
        
        boolean catched=false;
        try {
            createReasoner();
        } catch (IllegalArgumentException e) {
            catched=true;
        }
        assertTrue(catched);
    }
    
    public void testSeveralVars() throws Exception {
        String axioms = "ClassAssertion(:A :a) ClassAssertion(:B :b) ClassAssertion(:C :c) ClassAssertion(:D :d) ClassAssertion(:E :e)"
            +"ObjectPropertyAssertion(:rab :a :b) ObjectPropertyAssertion(:rac :a :c) ObjectPropertyAssertion(:rcd :c :d)";
        loadOntologyWithAxioms(axioms);
        // A(xa) /\ B(b) /\ rab(xa, xb) /\ rac(xa, xc) /\ rcd(xc, xd) /\ E(xe) -> Ap(xa) /\ Bp(xb) /\ Cp(c) /\ Dp(xd) /\ Ep(xe) /\ rae(xa, xe)
        SWRLIndividualVariable xa = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "xa"));
        SWRLIndividualVariable xb = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "xb"));
        SWRLIndividualVariable xc = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "xc"));
        SWRLIndividualVariable xd = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "xd"));
        SWRLIndividualVariable xe = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "xe"));
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLNamedIndividual c = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "c"));
        OWLNamedIndividual d = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "d"));
        OWLNamedIndividual e = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "e"));
        SWRLIndividualArgument bArg = m_dataFactory.getSWRLIndividualArgument(b);
        SWRLIndividualArgument cArg = m_dataFactory.getSWRLIndividualArgument(c);
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "A"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLClass E = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "E"));
        OWLClass Ap = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "Ap"));
        OWLClass Bp = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "Bp"));
        OWLClass Cp = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "Cp"));
        OWLClass Dp = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "Dp"));
        OWLClass Ep = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "Ep"));
        OWLObjectProperty rab = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "rab"));
        OWLObjectProperty rac = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "rac"));
        OWLObjectProperty rcd = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "rcd"));
        OWLObjectProperty rae = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "rae"));
        SWRLAtom Axa =m_dataFactory.getSWRLClassAtom(A, xa);
        SWRLAtom Bb =m_dataFactory.getSWRLClassAtom(B, bArg);
        SWRLObjectPropertyAtom rabxaxb=m_dataFactory.getSWRLObjectPropertyAtom(rab, xa, xb);
        SWRLObjectPropertyAtom racxaxc=m_dataFactory.getSWRLObjectPropertyAtom(rac, xa, xc);
        SWRLObjectPropertyAtom rcdxcxd=m_dataFactory.getSWRLObjectPropertyAtom(rcd, xc, xd);
        SWRLAtom Exe =m_dataFactory.getSWRLClassAtom(E, xe);
        SWRLAtom Apxa =m_dataFactory.getSWRLClassAtom(Ap, xa);
        SWRLAtom Bpxb =m_dataFactory.getSWRLClassAtom(Bp, xb);
        SWRLAtom Cpc =m_dataFactory.getSWRLClassAtom(Cp, cArg);
        SWRLAtom Dpxd =m_dataFactory.getSWRLClassAtom(Dp, xd);
        SWRLAtom Epxe =m_dataFactory.getSWRLClassAtom(Ep, xe);
        SWRLObjectPropertyAtom raexaxe=m_dataFactory.getSWRLObjectPropertyAtom(rae, xa, xe);
        
        Set<SWRLAtom> body=new HashSet<SWRLAtom>();
        body.add(Axa);
        body.add(Bb);
        body.add(rabxaxb);
        body.add(racxaxc);
        body.add(rcdxcxd);
        body.add(Exe);
        
        Set<SWRLAtom> head=new HashSet<SWRLAtom>();
        head.add(Apxa);
        head.add(Bpxb);
        head.add(Cpc);
        head.add(Dpxd);
        head.add(Epxe);
        head.add(raexaxe);
        
        m_ontologyManager.addAxiom(m_ontology, m_dataFactory.getSWRLRule(body, head));
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(Ap, false).contains(a));
        assertTrue(m_reasoner.getIndividuals(Bp, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(Cp, false).contains(c));
        assertTrue(m_reasoner.getIndividuals(Dp, false).contains(d));
        assertTrue(m_reasoner.getIndividuals(Ep, false).contains(e));
        assertTrue(m_reasoner.getIndividuals(A, false).contains(a));
        assertTrue(m_reasoner.getIndividuals(A, false).contains(a));
        assertTrue(m_reasoner.getIndividuals(B, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(E, false).contains(e));
        Set<OWLNamedIndividual> result=m_reasoner.getRelatedIndividuals(c, rcd);
        assertTrue(result.contains(d));
        result=m_reasoner.getRelatedIndividuals(a, rae);
        assertTrue(result.contains(e));
        assertTrue(result.size()==1);
    }
    
    public void testPositiveBodyDataRange() throws Exception {
        String axioms = "ClassAssertion(:A :a) SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"6\"^^xsd:integer xsd:maxInclusive \"9\"^^xsd:integer)))";
        loadOntologyWithAxioms(axioms);
        // dp(x, y) /\ ((xsd:integer >= 5) and (xsd:decimal <= 10))(y) /\ (xsd:int <= 9)(y) -> B(x)
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLLiteralVariable y = m_dataFactory.getSWRLLiteralVariable(IRI.create(AbstractReasonerTest.NS + "y"));
        OWLDataProperty dp = m_dataFactory.getOWLDataProperty(IRI.create(AbstractReasonerTest.NS + "dp"));
        OWLDatatype xsdinteger=m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#integer"));
        OWLDatatype xsdint=m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#int"));
        OWLDatatype xsddecimal=m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#decimal"));
        OWLDatatypeRestriction dtr5=m_dataFactory.getOWLDatatypeRestriction(xsdinteger, m_dataFactory.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, 5));
        OWLDatatypeRestriction dtr10=m_dataFactory.getOWLDatatypeRestriction(xsddecimal, m_dataFactory.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, 10));
        OWLDataRange dr5_10=m_dataFactory.getOWLDataIntersectionOf(dtr5, dtr10);
        OWLDatatypeRestriction dtr9=m_dataFactory.getOWLDatatypeRestriction(xsdint, m_dataFactory.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, 9));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        
        SWRLDataPropertyAtom dpxy=m_dataFactory.getSWRLDataPropertyAtom(dp, x, y);
        SWRLDataRangeAtom dr1y=m_dataFactory.getSWRLDataRangeAtom(dr5_10, y);
        SWRLDataRangeAtom dr2y=m_dataFactory.getSWRLDataRangeAtom(dtr9, y);
        SWRLClassAtom Bx=m_dataFactory.getSWRLClassAtom(B, x);
        
        Set<SWRLAtom> body=new HashSet<SWRLAtom>();
        body.add(dpxy);
        body.add(dr1y);
        body.add(dr2y);
        SWRLRule rule=m_dataFactory.getSWRLRule(body, Collections.singleton(Bx));
        m_ontologyManager.addAxiom(m_ontology, rule);
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(B, false).contains(a));
    }
    
    public void testNegativeBodyDataRange() throws Exception {
        String axioms = "ClassAssertion(:A :a) ClassAssertion(:B :b)"
            + "SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"6\"^^xsd:integer xsd:maxInclusive \"9\"^^xsd:integer)))"
            + "SubClassOf(:B DataHasValue(:dp \"abc\"))";
        loadOntologyWithAxioms(axioms);
        // dp(x, y) /\ not((xsd:integer >= 5) and (xsd:decimal <= 10))(y) -> C(x)
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLLiteralVariable y = m_dataFactory.getSWRLLiteralVariable(IRI.create(AbstractReasonerTest.NS + "y"));
        OWLDataProperty dp = m_dataFactory.getOWLDataProperty(IRI.create(AbstractReasonerTest.NS + "dp"));
        OWLDatatype xsdinteger=m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#integer"));
        OWLDatatype xsddecimal=m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#decimal"));
        OWLDatatypeRestriction dtr5=m_dataFactory.getOWLDatatypeRestriction(xsdinteger, m_dataFactory.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, 5));
        OWLDatatypeRestriction dtr10=m_dataFactory.getOWLDatatypeRestriction(xsddecimal, m_dataFactory.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, 10));
        OWLDataRange dr5_10=m_dataFactory.getOWLDataIntersectionOf(dtr5, dtr10);
        OWLDataRange notdr5_10=m_dataFactory.getOWLDataComplementOf(dr5_10);
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        
        SWRLDataPropertyAtom dpxy=m_dataFactory.getSWRLDataPropertyAtom(dp, x, y);
        SWRLDataRangeAtom dr1y=m_dataFactory.getSWRLDataRangeAtom(notdr5_10, y);
        SWRLClassAtom Cx=m_dataFactory.getSWRLClassAtom(C, x);
        
        Set<SWRLAtom> body=new HashSet<SWRLAtom>();
        body.add(dpxy);
        body.add(dr1y);
        SWRLRule rule=m_dataFactory.getSWRLRule(body, Collections.singleton(Cx));
        m_ontologyManager.addAxiom(m_ontology, rule);
        createReasoner();
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(m_reasoner.getIndividuals(C, false).contains(b));
    }

    public void testNegDRInHead() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
            + "SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"6\"^^xsd:integer xsd:maxInclusive \"9\"^^xsd:integer)))";
        loadOntologyWithAxioms(axioms);
        // dp(x, y) -> not(xsd:short >= 1)(y)
        OWLDataProperty dp = m_dataFactory.getOWLDataProperty(IRI.create(AbstractReasonerTest.NS + "dp"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLLiteralVariable y = m_dataFactory.getSWRLLiteralVariable(IRI.create(AbstractReasonerTest.NS + "y"));
        OWLDatatype xsdshort=m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#short"));
        OWLDatatypeRestriction int_geq_1=m_dataFactory.getOWLDatatypeRestriction(xsdshort, m_dataFactory.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, 1));
        OWLDataRange not_geq_1=m_dataFactory.getOWLDataComplementOf(int_geq_1);
        
        SWRLDataPropertyAtom dpxy=m_dataFactory.getSWRLDataPropertyAtom(dp, x, y);
        SWRLDataRangeAtom dr1y=m_dataFactory.getSWRLDataRangeAtom(not_geq_1, y);
        SWRLRule rule=m_dataFactory.getSWRLRule(Collections.singleton(dpxy), Collections.singleton(dr1y));
        m_ontologyManager.addAxiom(m_ontology, rule);
        
        createReasoner();
        assertTrue(!m_reasoner.isConsistent());
    }
    
    public void testSameAs() throws Exception {
        String axioms = "ClassAssertion(:A :a) ClassAssertion(:B :b) DisjointClasses(:A :B) ObjectPropertyAssertion(:r :a :b)";
        loadOntologyWithAxioms(axioms);
        // r(x, y) -> SameAs(x, y)
        OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "r"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLIndividualVariable y = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "y"));
        
        SWRLObjectPropertyAtom rxy=m_dataFactory.getSWRLObjectPropertyAtom(r, x, y);
        SWRLSameIndividualAtom samexy=m_dataFactory.getSWRLSameIndividualAtom(x, y);
        SWRLRule rule=m_dataFactory.getSWRLRule(Collections.singleton(rxy), Collections.singleton(samexy));
        m_ontologyManager.addAxiom(m_ontology, rule);
        
        createReasoner();
        assertTrue(!m_reasoner.isConsistent());
    }
    
    public void testDifferentFrom() throws Exception {
        String axioms = "ObjectPropertyAssertion(:f :a :b) ObjectPropertyAssertion(:f :a :c) FunctionalObjectProperty(:f)";
        loadOntologyWithAxioms(axioms);
        // f(x, y) /\ f(x, z) -> DifferentFrom(y, z)
        OWLObjectProperty f = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "f"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLIndividualVariable y = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "y"));
        SWRLIndividualVariable z = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "z"));
        
        SWRLObjectPropertyAtom fxy=m_dataFactory.getSWRLObjectPropertyAtom(f, x, y);
        SWRLObjectPropertyAtom fxz=m_dataFactory.getSWRLObjectPropertyAtom(f, x, z);
        SWRLDifferentIndividualsAtom differentyz=m_dataFactory.getSWRLDifferentIndividualsAtom(y, z);
        
        Set<SWRLAtom> body=new HashSet<SWRLAtom>();
        body.add(fxy);
        body.add(fxz);
        SWRLRule rule=m_dataFactory.getSWRLRule(body, Collections.singleton(differentyz));
        m_ontologyManager.addAxiom(m_ontology, rule);
        
        createReasoner();
        assertTrue(!m_reasoner.isConsistent());
    }
    
    public void testDiffrentFrom2() throws Exception {
        String axioms = "ClassAssertion(:A :a) ClassAssertion(:B :b) DisjointClasses(:A :B) ObjectPropertyAssertion(:r :a :b)";
        loadOntologyWithAxioms(axioms);
        // r(x, y) /\ DifferentFrom(x, y)-> C(x)
        OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "r"));
        SWRLIndividualVariable x = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "x"));
        SWRLIndividualVariable y = m_dataFactory.getSWRLIndividualVariable(IRI.create(AbstractReasonerTest.NS + "y"));
        SWRLObjectPropertyAtom rxy=m_dataFactory.getSWRLObjectPropertyAtom(r, x, y);
        SWRLDifferentIndividualsAtom diffxy=m_dataFactory.getSWRLDifferentIndividualsAtom(x, y);
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        SWRLAtom Cx =m_dataFactory.getSWRLClassAtom(C, x);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        
        Set<SWRLAtom> body=new HashSet<SWRLAtom>();
        body.add(rxy);
        body.add(diffxy);
        SWRLRule rule=m_dataFactory.getSWRLRule(body, Collections.singleton(Cx));
        m_ontologyManager.addAxiom(m_ontology, rule);
        
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
    }
}
