package org.semanticweb.HermiT.reasoner;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;


public class RulesTest extends AbstractReasonerTest {

    public RulesTest(String name) {
        super(name);
    }

//    protected Configuration getConfiguration() {
//        Configuration c=new Configuration();
//        return c;
//    }

    public void testSimpleRule() throws Exception {
        String axioms = "SubClassOf(:A :B)"
            + "ClassAssertion(:A :a)"
            + "ClassAssertion(:D :b)"
            // B(x) -> C(x)
            + "DLSafeRule(Body(ClassAtom(:B IndividualVariable(:x))) Head(ClassAtom(:C IndividualVariable(:x))))";
        loadOntologyWithAxioms(axioms);
        
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));

        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
    }
    
    public void testRuleWithConstants() throws Exception {
        String axioms = "SubClassOf(:A :B)"
            + "ClassAssertion(:A :a)"
            + "ClassAssertion(:D :b)"
            // B(x) -> C(a)
            + "DLSafeRule(Body(ClassAtom(:B IndividualVariable(:x))) Head(ClassAtom(:C :a)))";
        loadOntologyWithAxioms(axioms);
        
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
    }
    
    public void testRuleWithConstants2() throws Exception {
        String axioms = "ClassAssertion(ObjectSomeValuesFrom(:r owl:Thing) :a)"
            + "ObjectPropertyAssertion(:r :a :b)"
            // r(x, y) -> s(x, y)
            + "DLSafeRule(Body(ObjectPropertyAtom(:r IndividualVariable(:x) IndividualVariable(:y))) Head(ObjectPropertyAtom(:s IndividualVariable(:x) IndividualVariable(:y))))"
            // r(x, b) -> sb(x, b)
            + "DLSafeRule(Body(ObjectPropertyAtom(:r IndividualVariable(:x) :b)) Head(ObjectPropertyAtom(:sb IndividualVariable(:x) :b)))"
            // s(a, x) -> sa(a, b)
            + "DLSafeRule(Body(ObjectPropertyAtom(:s :a IndividualVariable(:x))) Head(ObjectPropertyAtom(:sa :a :b)))"
            // r(a, b) -> q(a, b)
            + "DLSafeRule(Body(ObjectPropertyAtom(:r :a :b)) Head(ObjectPropertyAtom(:q :a :b)))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "r"));
        OWLObjectProperty s = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "s"));
        OWLObjectProperty sa = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "sa"));
        OWLObjectProperty sb = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "sb"));
        OWLObjectProperty q = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "q"));
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
            + "DataPropertyAssertion(:dp :b \"17\"^^xsd:short)"
            // dp(x, "18"^^xsd:integer) -> C(x)
            + "DLSafeRule(Body(DataPropertyAtom(:dp IndividualVariable(:x) \"18\"^^xsd:integer)) Head(ClassAtom(:C IndividualVariable(:x))))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
    }
    
    
    public void testRuleWithDatatypes2() throws Exception {
        String axioms = "ClassAssertion(DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"10\"^^xsd:integer)) :a)"
            + "DataPropertyAssertion(:dp :b \"10\"^^xsd:short)"
            + "DataPropertyAssertion(:dp :c \"25\"^^xsd:integer)"
            + "ClassAssertion(ObjectComplementOf(:C) :a)"
            // dp(x, y) /\ DatatypeRestriction(xsd:int xsd:minInclusive "15"^^xsd:int)(y) -> C(x)
            + "DLSafeRule(Body(DataPropertyAtom(:dp IndividualVariable(:x) LiteralVariable(:y)) DataRangeAtom(DatatypeRestriction(xsd:int xsd:minInclusive \"15\"^^xsd:int) LiteralVariable(:y))) Head(ClassAtom(:C IndividualVariable(:x))))"
            // dp(x, y) /\ DataComplementOf(DatatypeRestriction(xsd:int xsd:minInclusive "15"^^xsd:int))(y) -> D(x)
            + "DLSafeRule(Body(DataPropertyAtom(:dp IndividualVariable(:x) LiteralVariable(:y)) DataRangeAtom(DataComplementOf(DatatypeRestriction(xsd:int xsd:minInclusive \"15\"^^xsd:int)) LiteralVariable(:y))) Head(ClassAtom(:D IndividualVariable(:x))))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLNamedIndividual c = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "c"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        OWLClass D = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "D"));
        
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(c));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(D, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(D, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(D, false).contains(c));
    }
    
    public void testRuleWithFreshIndividuals() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
            // A(x) -> B(b)
            + "DLSafeRule(Body(ClassAtom(:A IndividualVariable(:x))) Head(ClassAtom(:B :b)))"
            // B(x) -> C(x)
            + "DLSafeRule(Body(ClassAtom(:B IndividualVariable(:x))) Head(ClassAtom(:C IndividualVariable(:x))))";;
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(B, false).contains(b));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(B, false).contains(a));
    }
    
    public void testAddingFactsByRules() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
            // -> B(a)
            + "DLSafeRule(Body() Head(ClassAtom(:B :a)))"
            // -> B(b)
            + "DLSafeRule(Body() Head(ClassAtom(:B :b)))"
            // B(x) -> C(x)
            + "DLSafeRule(Body(ClassAtom(:B IndividualVariable(:x))) Head(ClassAtom(:C IndividualVariable(:x))))"
            // B(x) /\ A(x) -> D(x)
            + "DLSafeRule(Body(ClassAtom(:B IndividualVariable(:x)) ClassAtom(:A IndividualVariable(:x))) Head(ClassAtom(:D IndividualVariable(:x))))"
            // B(x) /\ D(y) -> E(e)
            + "DLSafeRule(Body(ClassAtom(:B IndividualVariable(:x)) ClassAtom(:D IndividualVariable(:y))) Head(ClassAtom(:E :e)))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLNamedIndividual e = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "e"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "A"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        OWLClass D = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "D"));
        OWLClass E = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "E"));
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
            + "ClassAssertion(:B :b)"
            // A(x) -> B(x) /\ C(x)
            + "DLSafeRule(Body(ClassAtom(:A IndividualVariable(:x))) Head(ClassAtom(:B IndividualVariable(:x)) ClassAtom(:C IndividualVariable(:x))))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "A"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(A, false).contains(a));
        assertTrue(m_reasoner.getIndividuals(B, false).contains(a));
        assertTrue(m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(A, false).contains(b));
        assertTrue(m_reasoner.getIndividuals(B, false).contains(b));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
    }
    
    public void testDRInHead() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
            // -> xsd:short("15"^^xsd:int)
            + "DLSafeRule(Body() Head(DataRangeAtom(xsd:short \"15\"^^xsd:int)))";
        loadOntologyWithAxioms(axioms);        
        createReasoner();
        assertTrue(m_reasoner.isConsistent());
    }
    
    public void testDRInHead2() throws Exception {
        // -> xsd:short("15"^^xsd:int)
        String axioms = "DLSafeRule(Body() Head(DataRangeAtom(xsd:short \"15\"^^xsd:int)))";
        loadOntologyWithAxioms(axioms);
        createReasoner();
        assertTrue(m_reasoner.isConsistent());
    }
    
    public void testDRInHead3() throws Exception {
        // -> xsd:byte("10000"^^xsd:integer)
        String axioms = "DLSafeRule(Body() Head(DataRangeAtom(xsd:byte \"10000\"^^xsd:integer)))";
        loadOntologyWithAxioms(axioms);
        createReasoner();
        assertTrue(!m_reasoner.isConsistent());
    }
    
    public void testDRSafety() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
            // A(x) /\ xsd:integer(y) -> dp(x, y)
            + "DLSafeRule(Body(ClassAtom(:A IndividualVariable(:x)) DataRangeAtom(xsd:integer LiteralVariable(:y))) Head(DataPropertyAtom(:dp IndividualVariable(:x) LiteralVariable(:y))))";
        loadOntologyWithAxioms(axioms);
        boolean caught=false;
        try {
            createReasoner();
        } catch (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue(caught);
    }
    
    public void testNormalSafety() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
            // A(x) -> r(x, y)            
            + "DLSafeRule(Body(ClassAtom(:A IndividualVariable(:x))) Head(ObjectPropertyAtom(:r IndividualVariable(:x) IndividualVariable(:y))))";
        loadOntologyWithAxioms(axioms);
        boolean caught=false;
        try {
            createReasoner();
        } catch (IllegalArgumentException e) {
            caught=true;
        }
        assertTrue(caught);
    }
    
    public void testSeveralVars() throws Exception {
        String axioms = "ClassAssertion(:A :a) ClassAssertion(:B :b) ClassAssertion(:C :c) ClassAssertion(:D :d) ClassAssertion(:E :e)"
            +"ObjectPropertyAssertion(:rab :a :b) ObjectPropertyAssertion(:rac :a :c) ObjectPropertyAssertion(:rcd :c :d)"
            // A(xa) /\ B(b) /\ rab(xa, xb) /\ rac(xa, xc) /\ rcd(xc, xd) /\ E(xe) -> Ap(xa) /\ Bp(xb) /\ Cp(c) /\ Dp(xd) /\ Ep(xe) /\ rae(xa, xe)
            + "DLSafeRule(Body(" +
            		"ClassAtom(:A IndividualVariable(:xa)) " +
            		"ClassAtom(:B :b) " +
            		"ObjectPropertyAtom(:rab IndividualVariable(:xa) IndividualVariable(:xb))" +
            		"ObjectPropertyAtom(:rac IndividualVariable(:xa) IndividualVariable(:xc))" +
            		"ObjectPropertyAtom(:rcd IndividualVariable(:xc) IndividualVariable(:xd))" +
            		"ClassAtom(:E IndividualVariable(:xe)) " +
            		") Head(" +
            		"ClassAtom(:Ap IndividualVariable(:xa)) " +
            		"ClassAtom(:Bp IndividualVariable(:xb)) " +
            		"ClassAtom(:Cp :c) " +
            		"ClassAtom(:Dp IndividualVariable(:xd)) " +
            		"ClassAtom(:Ep IndividualVariable(:xe)) " +
            		"ObjectPropertyAtom(:rae IndividualVariable(:xa) IndividualVariable(:xe))" +
            		"))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        OWLNamedIndividual c = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "c"));
        OWLNamedIndividual d = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "d"));
        OWLNamedIndividual e = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "e"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "A"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLClass E = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "E"));
        OWLClass Ap = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "Ap"));
        OWLClass Bp = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "Bp"));
        OWLClass Cp = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "Cp"));
        OWLClass Dp = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "Dp"));
        OWLClass Ep = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "Ep"));
        OWLObjectProperty rcd = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "rcd"));
        OWLObjectProperty rae = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS + "rae"));

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
        String axioms = "ClassAssertion(:A :a) SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"6\"^^xsd:integer xsd:maxInclusive \"9\"^^xsd:integer)))"
            // dp(x, y) /\ ((xsd:integer >= 5) and (xsd:decimal <= 10))(y) /\ (xsd:int <= 9)(y) -> B(x)
            + "DLSafeRule(Body(" +
            		"DataPropertyAtom(:dp IndividualVariable(:x) LiteralVariable(:y)) " +
            		"DataRangeAtom(DataIntersectionOf(DatatypeRestriction(xsd:integer xsd:minInclusive \"5\"^^xsd:int) DatatypeRestriction(xsd:decimal xsd:maxInclusive \"10\"^^xsd:int)) LiteralVariable(:y))" +
            		") Head(ClassAtom(:B IndividualVariable(:x))))";
        loadOntologyWithAxioms(axioms);
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(B, false).contains(a));
    }
    
    public void testNegativeBodyDataRange() throws Exception {
        String axioms = "ClassAssertion(:A :a) ClassAssertion(:B :b)"
            + "SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"6\"^^xsd:integer xsd:maxInclusive \"9\"^^xsd:integer)))"
            + "SubClassOf(:B DataHasValue(:dp \"abc\"))"
            // dp(x, y) /\ not((xsd:integer >= 5) and (xsd:decimal <= 10))(y) -> C(x)
            + "DLSafeRule(Body(" +
                    "DataPropertyAtom(:dp IndividualVariable(:x) LiteralVariable(:y)) " +
                    "DataRangeAtom(DataComplementOf(DataIntersectionOf(DatatypeRestriction(xsd:integer xsd:minInclusive \"5\"^^xsd:int) DatatypeRestriction(xsd:decimal xsd:maxInclusive \"10\"^^xsd:int))) LiteralVariable(:y))" +
                    ") Head(ClassAtom(:C IndividualVariable(:x))))";
        loadOntologyWithAxioms(axioms);
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "B"));
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));

        createReasoner();
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(m_reasoner.getIndividuals(C, false).contains(b));
    }

    public void testNegDRInHead() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
            + "SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"6\"^^xsd:integer xsd:maxInclusive \"9\"^^xsd:integer)))"
            // dp(x, y) -> not(xsd:short >= 1)(y)
            + "DLSafeRule(Body(" +
            "DataPropertyAtom(:dp IndividualVariable(:x) LiteralVariable(:y)) " +
            ") Head(" +
            "DataRangeAtom(DataComplementOf(DatatypeRestriction(xsd:short xsd:minInclusive \"1\"^^xsd:int)) LiteralVariable(:y))" +
            "))";
        loadOntologyWithAxioms(axioms);
        createReasoner();
        assertTrue(!m_reasoner.isConsistent());
    }
    
    public void testSameAs() throws Exception {
        String axioms = "ClassAssertion(:A :a) " 
		+ "ClassAssertion(:B :b) " 
		+ "DisjointClasses(:A :B) " 
		+ "ObjectPropertyAssertion(:r :a :b)"
		// r(x, y) -> SameAs(x, y)
		+ "DLSafeRule(Body(ObjectPropertyAtom(:r IndividualVariable(:x) IndividualVariable(:y))) Head(SameIndividualAtom(IndividualVariable(:x) IndividualVariable(:y))))";
        loadOntologyWithAxioms(axioms);        
        createReasoner();
        assertTrue(!m_reasoner.isConsistent());
    }
    
    public void testDifferentFrom() throws Exception {
        String axioms = "ObjectPropertyAssertion(:f :a :b)"
            + "ObjectPropertyAssertion(:f :a :c)" 
            + "FunctionalObjectProperty(:f)"
            // f(x, y) /\ f(x, z) -> DifferentFrom(y, z)
            + "DLSafeRule(Body(ObjectPropertyAtom(:f IndividualVariable(:x) IndividualVariable(:y)) ObjectPropertyAtom(:f IndividualVariable(:x) IndividualVariable(:z))) Head(DifferentIndividualsAtom(IndividualVariable(:y) IndividualVariable(:z))))";
        loadOntologyWithAxioms(axioms);
        createReasoner();
        assertTrue(!m_reasoner.isConsistent());
    }
    
    public void testDiffrentFrom2() throws Exception {
        String axioms = "ClassAssertion(:A :a) ClassAssertion(:B :b) DisjointClasses(:A :B) ObjectPropertyAssertion(:r :a :b)"
            // r(x, y) /\ DifferentFrom(x, y)-> C(x)
            + "DLSafeRule(Body(" +
            		"ObjectPropertyAtom(:r IndividualVariable(:x) IndividualVariable(:y)) " +
            		"DifferentIndividualsAtom(IndividualVariable(:x) IndividualVariable(:y))) Head(ClassAtom(:C IndividualVariable(:x))))";
        loadOntologyWithAxioms(axioms);

        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "C"));
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS + "b"));
        
        createReasoner();
        assertTrue(m_reasoner.getIndividuals(C, false).contains(a));
        assertTrue(!m_reasoner.getIndividuals(C, false).contains(b));
    }
}
