package org.semanticweb.HermiT.reasoner;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.vocab.OWLFacet;


public class ReasonerTest extends AbstractReasonerTest {

    public ReasonerTest(String name) {
        super(name);
    }
    // actually this test should cause a parsing error since xsd:minInclusive for restricting byte is supposed to use 
    // only values from the value space of byte, which \"4.5\"^^xsd:decimal isn't 
    public void testDataTypeRestriction() throws Exception {
        String axioms = "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:byte xsd:minInclusive \"4.5\"^^xsd:decimal xsd:maxInclusive \"7\"^^xsd:short)))"
            + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:decimal xsd:minInclusive \"6.0\"^^xsd:decimal xsd:maxInclusive \"6.8\"^^xsd:decimal)))"
            + "SubClassOf(:A DataSomeValuesFrom(:dp owl:real))"
            + "ClassAssertion(:A :a)"
            + "NegativeDataPropertyAssertion(:dp :a \"6\"^^xsd:unsignedInt)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testAnonymousIndividualConstraints() throws Exception {
        String axioms = "SameIndividual(:a _:anon1)";
        boolean exceptionThrown=false;
        try {
            loadReasonerWithAxioms(axioms);
        } catch (Exception e) {
            exceptionThrown=true;
        }
        assertTrue(exceptionThrown);
    }
    
    public void testAnonymousIndividualConstraints2() throws Exception {
        String axioms = "DifferentIndividuals(:a _:anon1)";
        boolean exceptionThrown=false;
        try {
            loadReasonerWithAxioms(axioms);
        } catch (Exception e) {
            exceptionThrown=true;
        }
        assertTrue(exceptionThrown);
    }
    
    public void testAnonymousIndividualConstraints3() throws Exception {
        String axioms = "NegativeObjectPropertyAssertion(:r :a _:anon1)";
        boolean exceptionThrown=false;
        try {
            loadReasonerWithAxioms(axioms);
        } catch (Exception e) {
            exceptionThrown=true;
        }
        assertTrue(exceptionThrown);
    }

    public void testAnonymousIndividualConstraints4() throws Exception {
        String axioms = "NegativeDataPropertyAssertion(:r _:anon1 \"test\")";
        boolean exceptionThrown=false;
        try {
            loadReasonerWithAxioms(axioms);
        } catch (Exception e) {
            exceptionThrown=true;
        }
        assertTrue(exceptionThrown);
    }
    
    public void testTransitivity() throws Exception {
        String axioms = "ObjectPropertyRange(:isSiblingOf :Person)"
            + "DisjointClasses(:Person :Sex)"
            + "SubClassOf(:Person ObjectSomeValuesFrom(:hasGender :Sex))"
            + "TransitiveObjectProperty(:isSiblingOf)"
            + "SymmetricObjectProperty(:isSiblingOf)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testSubAndSuperConcepts() throws Exception {
        String axioms = "SubClassOf(:C :D)"
            + "SubClassOf(:D :E)";
        loadReasonerWithAxioms(axioms);
        OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#C"));
        OWLClassExpression d = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#D"));
        OWLClassExpression e = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#E"));
        Set<OWLClassExpression> cEquiv=new HashSet<OWLClassExpression>();
        cEquiv.add(c);
        Set<OWLClassExpression> dEquiv=new HashSet<OWLClassExpression>();
        dEquiv.add(d);
        Set<OWLClassExpression> eEquiv=new HashSet<OWLClassExpression>();
        eEquiv.add(e);
        assertTrue(m_reasoner.getAncestorClasses(c).contains(cEquiv));
        assertTrue(m_reasoner.getAncestorClasses(c).contains(dEquiv));
        assertTrue(m_reasoner.getAncestorClasses(c).contains(eEquiv));
        assertTrue(m_reasoner.getAncestorClasses(d).contains(dEquiv));
        assertTrue(m_reasoner.getAncestorClasses(d).contains(eEquiv));
        assertTrue(!m_reasoner.getAncestorClasses(d).contains(cEquiv));
        assertTrue(m_reasoner.getAncestorClasses(e).contains(eEquiv));
        assertTrue(!m_reasoner.getAncestorClasses(e).contains(cEquiv));
        assertTrue(!m_reasoner.getAncestorClasses(e).contains(dEquiv));
        
        assertTrue(m_reasoner.getDescendantClasses(c).contains(cEquiv));
        assertTrue(!m_reasoner.getDescendantClasses(c).contains(dEquiv));
        assertTrue(!m_reasoner.getDescendantClasses(c).contains(eEquiv));
        assertTrue(m_reasoner.getDescendantClasses(d).contains(cEquiv));
        assertTrue(m_reasoner.getDescendantClasses(d).contains(dEquiv));
        assertTrue(!m_reasoner.getDescendantClasses(d).contains(eEquiv));
        assertTrue(m_reasoner.getDescendantClasses(e).contains(cEquiv));
        assertTrue(m_reasoner.getDescendantClasses(e).contains(dEquiv));
        assertTrue(m_reasoner.getDescendantClasses(e).contains(eEquiv));
        
        assertTrue(m_reasoner.isSubClassOf(c, c));
        assertTrue(m_reasoner.isSubClassOf(d, d));
        assertTrue(m_reasoner.isSubClassOf(e, e));
        assertTrue(m_reasoner.isSubClassOf(c, d));
        assertTrue(m_reasoner.isSubClassOf(c, e));
        assertTrue(m_reasoner.isSubClassOf(d, e));
        assertTrue(!m_reasoner.isSubClassOf(d, c));
        assertTrue(!m_reasoner.isSubClassOf(e, c));
        assertTrue(!m_reasoner.isSubClassOf(e, d));
    }
    public void testSubAndSuperRoles() throws Exception {
        String axioms = "SubObjectPropertyOf(:r :s)"
            + "SubObjectPropertyOf(:s :t)";
        loadReasonerWithAxioms(axioms);
        OWLObjectPropertyExpression r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
        OWLObjectPropertyExpression s = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#s"));
        OWLObjectPropertyExpression t = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#t"));
        Set<OWLObjectPropertyExpression> rEquiv=new HashSet<OWLObjectPropertyExpression>();
        rEquiv.add(r);
        Set<OWLObjectPropertyExpression> sEquiv=new HashSet<OWLObjectPropertyExpression>();
        sEquiv.add(s);
        Set<OWLObjectPropertyExpression> tEquiv=new HashSet<OWLObjectPropertyExpression>();
        tEquiv.add(t);
        assertTrue(m_reasoner.getAncestorProperties(r).contains(rEquiv));
        assertTrue(m_reasoner.getAncestorProperties(r).contains(sEquiv));
        assertTrue(m_reasoner.getAncestorProperties(r).contains(tEquiv));
        assertTrue(m_reasoner.getAncestorProperties(s).contains(sEquiv));
        assertTrue(m_reasoner.getAncestorProperties(s).contains(tEquiv));
        assertTrue(!m_reasoner.getAncestorProperties(s).contains(rEquiv));
        assertTrue(m_reasoner.getAncestorProperties(t).contains(tEquiv));
        assertTrue(!m_reasoner.getAncestorProperties(t).contains(rEquiv));
        assertTrue(!m_reasoner.getAncestorProperties(t).contains(sEquiv));
        
        assertTrue(m_reasoner.getDescendantProperties(r).contains(rEquiv));
        assertTrue(!m_reasoner.getDescendantProperties(r).contains(sEquiv));
        assertTrue(!m_reasoner.getDescendantProperties(r).contains(tEquiv));
        assertTrue(m_reasoner.getDescendantProperties(s).contains(rEquiv));
        assertTrue(m_reasoner.getDescendantProperties(s).contains(sEquiv));
        assertTrue(!m_reasoner.getDescendantProperties(s).contains(tEquiv));
        assertTrue(m_reasoner.getDescendantProperties(t).contains(rEquiv));
        assertTrue(m_reasoner.getDescendantProperties(t).contains(sEquiv));
        assertTrue(m_reasoner.getDescendantProperties(t).contains(tEquiv));
        
        assertTrue(m_reasoner.isSubPropertyOf(r, r));
        assertTrue(m_reasoner.isSubPropertyOf(s, s));
        assertTrue(m_reasoner.isSubPropertyOf(t, t));
        assertTrue(m_reasoner.isSubPropertyOf(r, s));
        assertTrue(m_reasoner.isSubPropertyOf(r, t));
        assertTrue(m_reasoner.isSubPropertyOf(s, t));
        assertTrue(!m_reasoner.isSubPropertyOf(s, r));
        assertTrue(!m_reasoner.isSubPropertyOf(t, r));
        assertTrue(!m_reasoner.isSubPropertyOf(t, s));
    }
    public void testSubRolesChain() throws Exception {
        String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:r) :s)";
        loadReasonerWithAxioms(axioms);
        OWLObjectPropertyExpression r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
        OWLObjectPropertyExpression s = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#s"));
        Set<OWLObjectPropertyExpression> requiv=new HashSet<OWLObjectPropertyExpression>();
        requiv.add(r);
        Set<OWLObjectPropertyExpression> sequiv=new HashSet<OWLObjectPropertyExpression>();
        sequiv.add(s);
        assertTrue(!m_reasoner.getSubProperties(s).contains(sequiv));
        assertTrue(m_reasoner.getSubProperties(s).contains(requiv));
    }
    public void testHasKeyEntailment() throws Exception {
        String axioms = "HasKey( :Person () ( :hasSSN ) )"
            + "SubClassOf( :Man :Person )";
        loadReasonerWithAxioms(axioms);
        OWLClass C=m_dataFactory.getOWLClass(IRI.create(ReasonerTest.NS + "Man"));
        OWLObjectProperty p=m_dataFactory.getOWLObjectProperty(IRI.create(ReasonerTest.NS + "hasSSN"));
        assertEntails(m_dataFactory.getOWLHasKeyAxiom(C, p), true);
    }
    public void testHasKeyNonEntailment() throws Exception {
        String axioms = "HasKey( :Person () ( :hasSSN ) )"
            + "SubClassOf( :Man :Person )"
            + "SubClassOf( :Person :Mammal )";
        loadReasonerWithAxioms(axioms);
        OWLClass C=m_dataFactory.getOWLClass(IRI.create(ReasonerTest.NS + "Mammal"));
        OWLObjectProperty p=m_dataFactory.getOWLObjectProperty(IRI.create(ReasonerTest.NS + "hasSSN"));
        assertEntails(m_dataFactory.getOWLHasKeyAxiom(C, p), false);
    }
    public void testDatatypeDefEntailment() throws Exception {
        String axioms = "DatatypeDefinition(:SSN DatatypeRestriction(xsd:string xsd:pattern \"[0-9]{3}-[0-9]{2}-[0-9]{4}\"))";
        loadReasonerWithAxioms(axioms);
        OWLDatatype dt=m_dataFactory.getOWLDatatype(IRI.create(ReasonerTest.NS + "SSN"));
        OWLFacetRestriction fr=m_dataFactory.getOWLFacetRestriction(OWLFacet.PATTERN, m_dataFactory.getOWLTypedLiteral("[0-9]{3}-[0-9]{2}-[0-9]{4}"));
        OWLDataRange dr=m_dataFactory.getOWLDatatypeRestriction(m_dataFactory.getOWLDatatype(URI.create("http://www.w3.org/2001/XMLSchema#string")), fr);
        OWLDatatypeDefinitionAxiom ddef=m_dataFactory.getOWLDatatypeDefinitionAxiom(dt, dr);
        assertEntails(ddef, true);
    }
    public void testDomainRange() throws Exception {
        String axioms = "Declaration(DataProperty(:p1))"
            + "Declaration(DataProperty(:p2))"
            + "Declaration(DataProperty(:p3))"
            + "Declaration(DataProperty(:p4))"
            + "Declaration(DataProperty(:p5))"
            + "Declaration(DataProperty(:p6))"
            + "Declaration(Class(:c2))"
            + "Declaration(Class(:c3))"
            + "Declaration(Class(:c4))"
            + "Declaration(Class(:c5))"
            + "Declaration(Class(:c6))"
            + "DataPropertyRange(:p1 rdf:PlainLiteral)"
            + "DataPropertyDomain(:p2 :c2)"
            + "ObjectPropertyRange(:p3 :c3)"
            + "ObjectPropertyDomain(:p4 :c4)"
            + "AnnotationPropertyDomain(:p5 :c5)"
            + "AnnotationPropertyRange(:p6 :c6)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    public void testDateTime2() throws Exception {
        String axioms = "SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:dateTime xsd:minInclusive \"2008-10-08T20:44:11.656+01:00\"^^xsd:dateTime)))" 
            + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:dateTime xsd:maxInclusive \"2008-10-08T20:44:11.656+01:00\"^^xsd:dateTime)))"
            + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    public void testChains4() throws Exception {
        String axioms = "SubObjectPropertyOf( ObjectPropertyChain( :t :t ) :t )"
            + "ObjectPropertyAssertion(:t :a :b)"
            + "ObjectPropertyAssertion(:t :b :c)"
            + "ClassAssertion(ObjectAllValuesFrom(:t :A) :a)"
            + "ClassAssertion(ObjectComplementOf(:A) :c)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    public void testChains1() throws Exception {
        String axioms = "TransitiveObjectProperty(:p)" +
        				"SubObjectPropertyOf(ObjectPropertyChain(:p1 :p :p2) :S) ";
        loadReasonerWithAxioms(axioms);
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLObjectProperty p = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#p"));
        OWLObjectProperty p1 = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#p1"));
        OWLObjectProperty p2 = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#p2"));
        OWLObjectProperty S = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#S"));
        List<OWLObjectPropertyExpression> chain = new ArrayList<OWLObjectPropertyExpression>();
        chain.add(p1);
        chain.add(p);
        chain.add(p);
        chain.add(p2);
        OWLSubPropertyChainOfAxiom ax = df.getOWLSubPropertyChainOfAxiom(chain, S);
        assertEntails(ax, true);
    }
    public void testChains3() throws Exception {
        String axioms = "TransitiveObjectProperty( :p)";
        loadReasonerWithAxioms(axioms);
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLObjectProperty p = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#p"));
        List<OWLObjectPropertyExpression> chain = new ArrayList<OWLObjectPropertyExpression>();
        chain.add(p);
        chain.add(p);
        OWLSubPropertyChainOfAxiom ax = df.getOWLSubPropertyChainOfAxiom(chain, p);
        assertEntails(ax, true);
    }
    public void testChains2() throws Exception {
        String axioms = "ObjectPropertyAssertion(:p :a :b)"
            + "ObjectPropertyAssertion(:q :b :c)"
            + "SubObjectPropertyOf(ObjectPropertyChain(:p :q) :p)"
            + "ObjectPropertyAssertion(:p :a :b)"
            + "ObjectPropertyAssertion(:q :b :c)"
            + "NegativeObjectPropertyAssertion(:p :a :c)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    public void testAnonymousIndiviuals1() throws Exception {
        String axioms = "SubClassOf(:r ObjectAllValuesFrom(:p :c))"
            + "ClassAssertion(:r :i)"
            + "ClassAssertion(ObjectComplementOf(:c) _:o)"
            + "ObjectPropertyAssertion( :p :i _:o )";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    public void testDateTime() throws Exception {
        String axioms = "SubClassOf(:A DataHasValue(:dp \"2007-10-08T20:44:11.656+01:00\"^^xsd:dateTime))" 
            + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:dateTime xsd:minInclusive \"2008-07-08T20:44:11.656+01:00\"^^xsd:dateTime xsd:maxInclusive \"2008-10-08T20:44:11.656+01:00\"^^xsd:dateTime)))" 
            + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    public void testDataRanges() throws Exception {
        String axioms = "SubClassOf(:A DataAllValuesFrom(:dp owl:real))"
            + "SubClassOf(:A DataSomeValuesFrom(:dp DataOneOf(\"-INF\"^^xsd:float \"-0\"^^xsd:integer)) )"
            + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    // needs proper implementation of BottomDataProperty
    public void testBottomDataProperty() throws Exception {
        String axioms = "ClassAssertion( DataSomeValuesFrom( owl:bottomDataProperty rdfs:Literal ) :i )";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }     
    public void testChains() throws Exception {
        String axioms = "SubObjectPropertyOf( ObjectPropertyChain( :hasMother :hasSister ) :hasAunt )"
            + "ObjectPropertyAssertion( :hasMother :Stewie :Lois )"
            + "ObjectPropertyAssertion( :hasSister :Lois :Carol )"
            + "NegativeObjectPropertyAssertion( :hasAunt :Stewie :Carol ) ";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    public void testDatatypeLiterals() throws Exception {
        loadReasonerFromResource("res/FS2RDF-literals-ar-consistent.f.owl");
        assertABoxSatisfiable(true);
    }
    public void testHasKeysOnlyNamed() throws Exception {
        String axioms = "Declaration( Class( :Person ) )"
            + "Declaration( Class( :Man ) )"
            + "Declaration( DataProperty( :hasSSN ) )"
            + "Declaration( ObjectProperty( :marriedTo ) )"
            + "HasKey( :Person () ( :hasSSN ) )"
            + "DataPropertyAssertion( :hasSSN :Peter \"123-45-6789\" )"
            + "ClassAssertion( :Person :Peter )"
            + "ClassAssertion( ObjectSomeValuesFrom( :marriedTo ObjectIntersectionOf( :Man DataHasValue( :hasSSN \"123-45-6789\"^^xsd:string ) ) ) :Lois )"
            + "SubClassOf( :Man :Person )"
            + "ClassAssertion( ObjectComplementOf( :Man ) :Peter )";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    public void testTopDataProperty() throws Exception {
        String axioms = "ClassAssertion(ObjectComplementOf(DataSomeValuesFrom( owl:topDataProperty rdfs:Literal)) :i)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    public void testNegativeDataPropertyAssertion() throws Exception {
        String axioms = "Declaration( DataProperty( :hasAge ) )"
            + "NegativeDataPropertyAssertion( :hasAge :Meg \"5\"^^xsd:integer )"
            + "DataPropertyAssertion( :hasAge :Meg \"5\"^^xsd:integer )";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    public void testKeys() throws Exception {
        String axioms = "Declaration(Class(:RegisteredPatient))"
            + "Declaration(DataProperty(:hasWaitingListN))"
            + "SubDataPropertyOf(:hasWaitingListN owl:topDataProperty)"
            + "Declaration(NamedIndividual(:TestPatient1))"
            + "ClassAssertion(:RegisteredPatient :TestPatient1)"
            + "DataPropertyAssertion(:hasWaitingListN :TestPatient1 \"123-45-6789\")"
            + "DataPropertyAssertion(:hasWaitingListN :TestPatient2 \"123-45-6789\")"
            + "Declaration(NamedIndividual(:TestPatient2))"
            + "ClassAssertion(:RegisteredPatient :TestPatient2)"
            + "ClassAssertion(:RegisteredPatient :TestPatient2)"
            + "HasKey(:RegisteredPatient () (:hasWaitingListN))"
            + "DifferentIndividuals(:TestPatient1 :TestPatient2)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testNonUnaryKeys() throws Exception {
        String axioms = "HasKey(:Car () (:licensePlate :nationality))" 
            + "ClassAssertion(:Car :myCar1)"
            + "ClassAssertion(:Car :myCar2)"
            + "ClassAssertion(:Car :myCar3)"
            + "DataPropertyAssertion(:licensePlate :myCar1 \"OD-SG-101\")"
            + "DataPropertyAssertion(:nationality :myCar1 \"German\")"
            + "DataPropertyAssertion(:licensePlate :myCar2 \"OD-SG-101\")"
            + "DataPropertyAssertion(:nationality :myCar2 \"German\")"
            + "DataPropertyAssertion(:licensePlate :myCar3 \"OD-SG-101\")"
            + "DataPropertyAssertion(:nationality :myCar3 \"British\")"
            + "DifferentIndividuals(:myCar1 :myCar2)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testNonUnaryKeys2() throws Exception {
        String axioms = "HasKey(:Car () (:licensePlate :nationality))" 
            + "ClassAssertion(:Car :myCar1)"
            + "ClassAssertion(:Car :myCar2)"
            + "ClassAssertion(:Car :myCar3)"
            + "DataPropertyAssertion(:licensePlate :myCar1 \"OD-SG-101\")"
            + "DataPropertyAssertion(:nationality :myCar1 \"German\")"
            + "DataPropertyAssertion(:licensePlate :myCar2 \"OD-SG-101\")"
            + "DataPropertyAssertion(:nationality :myCar2 \"German\")"
            + "DataPropertyAssertion(:licensePlate :myCar3 \"OD-SG-101\")"
            + "DataPropertyAssertion(:nationality :myCar3 \"British\")"
            + "DifferentIndividuals(:myCar1 :myCar3)"
            + "DifferentIndividuals(:myCar2 :myCar3)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testHierarchyPrinting1() throws Exception {
        String axioms =
            "SubClassOf( :A :B )"+
            "SubClassOf( :D :A )"+
            "SubClassOf( :C :B )"+
            "SubClassOf( :D :C )"+
            "SubClassOf( :F :D )"+
            "SubClassOf( :F :E )"+
            "SubClassOf( :H :E )"+
            "SubClassOf( :G owl:Nothing )"+
            "SubClassOf( owl:Thing :I )"+
            "SubClassOf( <http://my.com/test2#J> :D )"+
            "EquivalentClasses( :A <"+NS+"Class> )"+
            "EquivalentClasses( :A <"+NS+"s:local> )"+
            "EquivalentClasses( :C <http://my.com/test1#otherC> )"+
            "EquivalentClasses( :H <http://zzz.com/test3#K> )"+
            
            "InverseObjectProperties( :op3 :op3i ) "+
            "InverseObjectProperties( :op7 :op7i ) "+
            "InverseObjectProperties( :op8 :op8i ) "+
            "EquivalentObjectProperties( :op8 :op8i ) "+
            "SubObjectPropertyOf( owl:topObjectProperty :op6 ) "+
            "SubObjectPropertyOf( :op2 :op6 ) "+
            "SubObjectPropertyOf( :op3i :op6 ) "+
            "SubObjectPropertyOf( :op1 :op2 ) "+
            "SubObjectPropertyOf( :op1 :op3i ) "+
            "SubObjectPropertyOf( :op4 :op2 ) "+
            "SubObjectPropertyOf( :op7 :op2 ) "+
            "SubObjectPropertyOf( :op7i :op2 ) "+
            "SubObjectPropertyOf( :op8 :op1 ) "+
            "SubObjectPropertyOf( :op8 :op4 ) "+
            "SubObjectPropertyOf( :op5 owl:bottomObjectProperty ) "+

            "SubDataPropertyOf( :dp4 :dp2 ) "+
            "SubDataPropertyOf( :dp4 :dp3 ) "+
            "SubDataPropertyOf( :dp3 :dp5 ) "+
            "SubDataPropertyOf( :dp5 :dp3 ) "+
            "SubDataPropertyOf( :dp2 :dp1 ) "+
            "SubDataPropertyOf( :dp3 :dp1 ) "+
            "SubDataPropertyOf( :dp4 :dp1 ) "+
            "SubDataPropertyOf( :dp6 :dp2 ) "+
            "SubDataPropertyOf( :dp7 owl:bottomDataProperty ) ";
        loadReasonerWithAxioms(axioms);
        assertHierarchies("res/hierarchy-printing-1.txt");
    }

    public void testHierarchyPrinting2() throws Exception {
        String axioms =
            "SubClassOf( :A :B )"+
            "SubClassOf( :D :A )"+
            "SubClassOf( :C :B )"+
            "SubClassOf( :D :C )"+
            "SubClassOf( :F :D )"+
            "SubClassOf( :F :E )"+
            "SubClassOf( :H :E )"+
            "SubClassOf( <http://my.com/test2#J> :D )"+
            "EquivalentClasses( :A <"+NS+"Class> )"+
            "EquivalentClasses( :A <"+NS+"s:local> )"+
            "EquivalentClasses( :C <http://my.com/test1#otherC> )"+
            
            "InverseObjectProperties( :op3 :op3i ) "+
            "SubObjectPropertyOf( :op2 :op6 ) "+
            "SubObjectPropertyOf( :op3i :op6 ) "+
            "SubObjectPropertyOf( :op1 :op2 ) "+
            "SubObjectPropertyOf( :op1 :op3i ) "+
            "SubObjectPropertyOf( :op4 :op2 ) "+
    
            "SubDataPropertyOf( :dp4 :dp2 ) "+
            "SubDataPropertyOf( :dp4 :dp3 ) "+
            "SubDataPropertyOf( :dp3 :dp5 ) "+
            "SubDataPropertyOf( :dp5 :dp3 ) "+
            "SubDataPropertyOf( :dp2 :dp1 ) "+
            "SubDataPropertyOf( :dp3 :dp1 ) "+
            "SubDataPropertyOf( :dp4 :dp1 ) "+
            "SubDataPropertyOf( :dp6 :dp2 ) ";
        loadReasonerWithAxioms(axioms);
        assertHierarchies("res/hierarchy-printing-2.txt");
    }

    public void testHierarchyPrinting3() throws Exception {
        String axioms =
            "SubClassOf( :A :B )"+
            "SubClassOf( owl:Thing owl:Nothing )";            
        loadReasonerWithAxioms(axioms);
        assertHierarchies("res/hierarchy-printing-3.txt");
    }

    @SuppressWarnings("unchecked")
    public void testObjectPropertyHierarchy() throws Exception {
        String axioms =
            "InverseObjectProperties( :r1 :r1i ) "+
            "InverseObjectProperties( :r2 :r2i ) "+
            "InverseObjectProperties( :r3 :r3i ) "+
            "InverseObjectProperties( :r4 :r4i ) "+
            "SubObjectPropertyOf( :r4 :r2 ) "+
            "SubObjectPropertyOf( :r4 :r3 ) "+
            "SubObjectPropertyOf( :r3 :r5 ) "+
            "SubObjectPropertyOf( :r5 :r3 ) "+
            "SubObjectPropertyOf( :r2i :r1i ) "+
            "SubObjectPropertyOf( :r3i :r1i ) "+
            "SubObjectPropertyOf( :r4 :r1 ) "+
            "SubObjectPropertyOf( :r6 :r2 ) "+
            "SubObjectPropertyOf( :r6 :r2i ) ";
        loadReasonerWithAxioms(axioms);
        
        assertEquivalentObjectProperties("r1",IRIs("r1",INV("r1i")));
        assertEquivalentObjectProperties("r2",IRIs("r2",INV("r2i")));
        assertEquivalentObjectProperties("r3",IRIs("r3","r5",INV("r3i")));
        assertEquivalentObjectProperties("r4",IRIs("r4",INV("r4i")));
        assertEquivalentObjectProperties("r5",IRIs("r3","r5",INV("r3i")));
        assertEquivalentObjectProperties("r6",IRIs("r6"));
        
        assertSuperObjectProperties("r2",EQ("r1",INV("r1i")));
        assertSuperObjectProperties("r3",EQ("r1",INV("r1i")));
        assertSuperObjectProperties("r4",EQ("r2",INV("r2i")),EQ("r3","r5",INV("r3i")));
        assertSuperObjectProperties("r5",EQ("r1",INV("r1i")));
        assertSuperObjectProperties("r6",EQ("r2",INV("r2i")),EQ("r2i",INV("r2")));
    }

    public void testSemanticObjectPropertyClassification() throws Exception {
        String axioms =
            "ObjectPropertyRange( :r ObjectOneOf( :a ) ) "+
            "ObjectPropertyRange( :s ObjectOneOf( :a ) ) "+
            "EquivalentClasses( ObjectSomeValuesFrom( :r owl:Thing ) ObjectSomeValuesFrom( :s owl:Thing ) ) ";
        loadReasonerWithAxioms(axioms);
        
        assertTrue(m_reasoner.isEquivalentProperty(m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create("file:/c/test.owl#r")),m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create("file:/c/test.owl#s"))));
    }

    @SuppressWarnings("unchecked")
    public void testDataPropertyHierarchy() throws Exception {
        String axioms =
            "SubDataPropertyOf( :r4 :r2 ) "+
            "SubDataPropertyOf( :r4 :r3 ) "+
            "SubDataPropertyOf( :r3 :r5 ) "+
            "SubDataPropertyOf( :r5 :r3 ) "+
            "SubDataPropertyOf( :r2 :r1 ) "+
            "SubDataPropertyOf( :r3 :r1 ) "+
            "SubDataPropertyOf( :r4 :r1 ) "+
            "SubDataPropertyOf( :r6 :r2 ) ";
        loadReasonerWithAxioms(axioms);
        
        assertEquivalentDataProperties("r1",IRIs("r1"));
        assertEquivalentDataProperties("r2",IRIs("r2"));
        assertEquivalentDataProperties("r3",IRIs("r3","r5"));
        assertEquivalentDataProperties("r4",IRIs("r4"));
        assertEquivalentDataProperties("r5",IRIs("r3","r5"));
        assertEquivalentDataProperties("r6",IRIs("r6"));
        
        assertSuperDataProperties("r2",EQ("r1"));
        assertSuperDataProperties("r3",EQ("r1"));
        assertSuperDataProperties("r4",EQ("r2"),EQ("r3","r5"));
        assertSuperDataProperties("r5",EQ("r1"));
        assertSuperDataProperties("r6",EQ("r2"));
    }

    public void testSemanticDataPropertyClassification() throws Exception {
        String axioms =
            "DataPropertyRange( :r xsd:nonNegativeInteger ) "+
            "DataPropertyRange( :r xsd:nonPositiveInteger ) "+
            "DataPropertyRange( :s xsd:nonNegativeInteger ) "+
            "DataPropertyRange( :s xsd:nonPositiveInteger ) "+
            "EquivalentClasses( DataSomeValuesFrom( :r rdfs:Literal ) DataSomeValuesFrom( :s rdfs:Literal ) ) ";
        loadReasonerWithAxioms(axioms);
        
        assertTrue(m_reasoner.isEquivalentProperty(m_ontologyManager.getOWLDataFactory().getOWLDataProperty(IRI.create("file:/c/test.owl#r")),m_ontologyManager.getOWLDataFactory().getOWLDataProperty(IRI.create("file:/c/test.owl#s"))));
    }

    public void testComplexConceptInstanceRetrieval() throws Exception {
        String axioms =
            "EquivalentClasses(:a ObjectSomeValuesFrom(:r :b)) " +
            "EquivalentClasses(:c ObjectSomeValuesFrom(:r :d)) " +
            "SubClassOf(:b :e) " +
            "SubClassOf(:d :e) " +
            "ClassAssertion(:a :i1) "+
            "ClassAssertion(:c :i2) "+
            "ObjectPropertyAssertion(:r :i3 :i4) "+
            "ClassAssertion(:e :i4) ";
        loadReasonerWithAxioms(axioms);

        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
        OWLClass a = df.getOWLClass(IRI.create("file:/c/test.owl#a"));
        OWLClass b = df.getOWLClass(IRI.create("file:/c/test.owl#b"));
        OWLClass e = df.getOWLClass(IRI.create("file:/c/test.owl#e"));
        OWLClassExpression some_r_b = df.getOWLObjectSomeValuesFrom(r,b);
        OWLClassExpression some_r_e = df.getOWLObjectSomeValuesFrom(r,e);
        assertInstancesOf(a,false,IRIs("i1"));
        assertInstancesOf(some_r_b,false,IRIs("i1"));
        assertInstancesOf(some_r_e,false,IRIs("i1","i2","i3"));
        assertInstancesOf(some_r_e,true,IRIs("i3"));
    }
    
    public void testWidmann1() throws Exception {
        String axioms = "SubClassOf(owl:Thing ObjectSomeValuesFrom(:a :p)) "
                + "SubClassOf(owl:Thing ObjectSomeValuesFrom(:b ObjectAllValuesFrom(:a ObjectSomeValuesFrom(:a ObjectComplementOf(:p))))) "
                + "InverseObjectProperties(:a :a-)"
                + "InverseObjectProperties(:b :b-)"
                + "SubClassOf(owl:Thing ObjectAllValuesFrom(:a- ObjectAllValuesFrom(:a- ObjectAllValuesFrom(:b ObjectAllValuesFrom(:b- :p))))) ";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testWidmann2() throws Exception {
        // <r>q; 
        // <r->[r-][r][r][r]p 
        String axioms = "SubClassOf(owl:Thing ObjectSomeValuesFrom(:r :q)) "
            + "InverseObjectProperties(:r :r-)"
            + "SubClassOf(owl:Thing ObjectSomeValuesFrom(:r- ObjectAllValuesFrom(:r- ObjectAllValuesFrom(:r ObjectAllValuesFrom(:r ObjectAllValuesFrom(:r :p)))))) ";
        loadReasonerWithAxioms(axioms);
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLClassExpression p = df.getOWLClass(IRI.create("file:/c/test.owl#p"));
        OWLObjectProperty invr = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));

        OWLClassExpression desc = df.getOWLObjectSomeValuesFrom(invr, df.getOWLObjectComplementOf(p));
        assertSatisfiable(desc,false);
    }
    
    public void testWidmann3() throws Exception {
        // <r-><r>[r]<r->~p; 
        // <r-><r>p; 
        // <r->[r-]<r-><r->[r][r]p; 
        // [r]<r>[r-]<r>[r-][r]p  
        String axioms = "InverseObjectProperties(:r :r-)"
            + "SubClassOf(owl:Thing ObjectSomeValuesFrom(:r- ObjectSomeValuesFrom(:r ObjectAllValuesFrom(:r ObjectSomeValuesFrom(:r- ObjectComplementOf(:p)))))) "
            + "SubClassOf(owl:Thing ObjectSomeValuesFrom(:r- ObjectSomeValuesFrom(:r :p))) "
            + "SubClassOf(owl:Thing ObjectSomeValuesFrom(:r- ObjectAllValuesFrom(:r- ObjectSomeValuesFrom(:r- ObjectSomeValuesFrom(:r- ObjectAllValuesFrom(:r ObjectAllValuesFrom(:r :p))))))) "
            + "SubClassOf(owl:Thing ObjectAllValuesFrom(:r ObjectSomeValuesFrom(:r ObjectAllValuesFrom(:r- ObjectSomeValuesFrom(:r ObjectAllValuesFrom(:r- ObjectAllValuesFrom(:r :p))))))) ";
        loadReasonerWithAxioms(axioms);
        
        assertABoxSatisfiable(false);
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLClassExpression desc = df.getOWLThing();
        assertSatisfiable(desc,false);
    }

    public void testReflexivity() throws Exception {
        String axioms = "ReflexiveObjectProperty(:r) "
                + "ClassAssertion(ObjectAllValuesFrom(:r " + "owl:Nothing) :a) "
                + "ClassAssertion(owl:Thing :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testNegProperties() throws Exception {
        String axioms = "ObjectPropertyAssertion(:r :a :b) "
                + "ObjectPropertyAssertion(:r :b :c) "
                + "TransitiveObjectProperty(:r) "
                + "NegativeObjectPropertyAssertion(:r :a :c)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testIrreflexivity() throws Exception {
        String axioms = "IrreflexiveObjectProperty(:r) "
                + "ObjectPropertyAssertion(:r :a :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testRoleDisjointness() throws Exception {
        String axioms = "DisjointObjectProperties(:r :s :t) "
                + "ObjectPropertyAssertion(:r :a :b) "
                + "ObjectPropertyAssertion(:s :a :b)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
        axioms = "DisjointObjectProperties(:r :s :t) "
                + "ObjectPropertyAssertion(:r :a :b) "
                + "ObjectPropertyAssertion(:t :a :b)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testRoleDisjointness2() throws Exception {
        String axioms = "DisjointObjectProperties(:r :s) "
                + "ClassAssertion(ObjectSomeValuesFrom(:r owl:Thing) :a) "
                + "ClassAssertion(ObjectSomeValuesFrom(:s owl:Thing) :a) "
                + "ClassAssertion(:C :a) "
                + "SubClassOf(:C ObjectMaxCardinality(1 :f)) "
                + "SubObjectPropertyOf(:r :f) " + "SubObjectPropertyOf(:s :f)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testExistsSelf1() throws Exception {
        String axioms = "ClassAssertion(ObjectAllValuesFrom(:r "
                + "owl:Nothing) :a) " + "ClassAssertion(ObjectHasSelf(:r) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testExistsSelf2() throws Exception {
        String axioms = "SubClassOf(:B1 ObjectSomeValuesFrom(:r :C2)) "
                + "SubClassOf(:C2 ObjectSomeValuesFrom(:r :B2)) "
                + "SubClassOf(:B2 ObjectSomeValuesFrom(:r :C1)) "
                + "SubClassOf(:C1 ObjectSomeValuesFrom(:r :B1)) "
                + "ClassAssertion(:C1 :a) "
                + "ClassAssertion(ObjectAllValuesFrom(:r "
                + "ObjectHasSelf(:r)) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }

    public void testAsymmetry() throws Exception {
        String axioms = "AsymmetricObjectProperty(:as) "
                + "SubObjectPropertyOf(:r :as) "
                + "ObjectPropertyAssertion(:as :b :a) "
                + "ObjectPropertyAssertion(:r :a :b)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability1() throws Exception {
        String axioms = "ClassAssertion(:C :a) "
                + "ClassAssertion(ObjectComplementOf(:C) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability2() throws Exception {
        String axioms = "SubClassOf(owl:Thing :C) " + "SubClassOf(owl:Thing "
                + "ObjectComplementOf(:C))";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability3() throws Exception {
        String axioms = "SubClassOf(:Person "
                + "ObjectSomeValuesFrom(:hasParent :Person)) "
                + "SubClassOf(ObjectSomeValuesFrom(:hasParent "
                + "ObjectSomeValuesFrom(:hasParent :Person)) " + " :Grandchild) "
                + "ClassAssertion(:Person :peter) " + "ClassAssertion("
                + "ObjectComplementOf(:Grandchild) :peter)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability4() throws Exception {
        String axioms = "FunctionalObjectProperty(:R) "
                + "ObjectPropertyAssertion(:R :a :b) "
                + "SubClassOf(owl:Thing ObjectSomeValuesFrom(:R :C)) "
                + "ClassAssertion(ObjectComplementOf(:C) :b)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testChanges() throws Exception {
        String axioms = "SubClassOf(owl:Thing :C)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
        axioms = "SubClassOf(owl:Thing :C) "
                + "SubClassOf(owl:Thing ObjectComplementOf(:C))";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
        axioms = "SubClassOf(owl:Thing ObjectComplementOf(:C))";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }

    public void testSubsumption1() throws Exception {
        String axioms = "SubClassOf(:Person :Animal) "
                + "SubClassOf(:Student :Person) " + "SubClassOf(:Dog :Animal)";
        loadReasonerWithAxioms(axioms);
        assertSubsumedBy("Student", "Animal", true);
        assertSubsumedBy("Animal", "Student", false);
        assertSubsumedBy("Student", "Dog", false);
        assertSubsumedBy("Dog", "Student", false);
    }

    public void testSubsumption2() throws Exception {
        String axioms = "SubObjectPropertyOf(:R :S) "
                + "EquivalentClasses(:A ObjectSomeValuesFrom(:R :C)) "
                + "EquivalentClasses(:B ObjectSomeValuesFrom(:S :C))";
        loadReasonerWithAxioms(axioms);
        assertSubsumedBy("A", "B", true);
        assertSubsumedBy("B", "A", false);
    }

    public void testSubsumption3() throws Exception {
        String axioms = "EquivalentObjectProperties(:R :S) "
                + "EquivalentClasses(:A ObjectSomeValuesFrom(:R :C)) "
                + "EquivalentClasses(:B ObjectSomeValuesFrom(:S :C))";
        loadReasonerWithAxioms(axioms);
        assertSubsumedBy("A", "B", true);
        assertSubsumedBy("B", "A", true);
    }

    public void testHeinsohnTBox1() throws Exception {
        // Tests incoherency caused by disjoint concepts
        String axioms = "DisjointClasses(:c :d) SubClassOf(:e3 :c) "
                + "SubClassOf(:f :d) SubClassOf(:c1 :d1) "
                + "DisjointClasses(:c1 :d1) EquivalentClasses(:complex1 "
                + "ObjectIntersectionOf(:c :d)) EquivalentClasses(:complex2 "
                + "ObjectIntersectionOf(ObjectAllValuesFrom(:r "
                + "ObjectIntersectionOf(:c :d)) ObjectSomeValuesFrom(:r "
                + "owl:Thing))) EquivalentClasses(:complex3 "
                + "ObjectIntersectionOf(:e3 :f))";
        loadReasonerWithAxioms(axioms);
        assertSatisfiable("complex1", false);
        assertSatisfiable("complex2", false);
        assertSatisfiable("complex3", false);
        assertSatisfiable("c1", false);
    }

    public void testHeinsohnTBox2() throws Exception {
        // Tests incoherency caused by number restrictions
        String axioms = "DisjointClasses(:c :d)" + "EquivalentClasses(:complex1 "
                + "ObjectIntersectionOf(ObjectMinCardinality(2 :r) "
                + "ObjectMaxCardinality(1 :r)))" + "EquivalentClasses(:complex2 "
                + "ObjectIntersectionOf(ObjectMaxCardinality(1 :r) "
                + "ObjectSomeValuesFrom(:r :c) ObjectSomeValuesFrom(:r :d)))";
        loadReasonerWithAxioms(axioms);
        assertSatisfiable("complex1", false);
        assertSatisfiable("complex2", false);
    }

    public void testHeinsohnTBox3c() throws Exception {
        // Tests incoherency caused by the role hierarchy and number restrictions
        String axioms = "DisjointClasses(:c :d)"
                + "SubClassOf(:a ObjectIntersectionOf(:c :d))"
                + "SubObjectPropertyOf(:t1 :tc)" + "SubObjectPropertyOf(:t1 :td)"
                + "SubClassOf(owl:Thing ObjectAllValuesFrom(:tc :c))"
                + "SubClassOf(owl:Thing ObjectAllValuesFrom(:td :d))"
                + "SubObjectPropertyOf(:tc :r)" + "SubObjectPropertyOf(:td :s)"
                + "EquivalentClasses(:complex1 "
                + "ObjectIntersectionOf(ObjectAllValuesFrom(:t1 :a) "
                + "ObjectMinCardinality(3 :t1) " + "ObjectMaxCardinality(1 :r) "
                + "ObjectMaxCardinality(1 :s)))";
        loadReasonerWithAxioms(axioms);
        assertSatisfiable("complex1", false);
    }

    public void testHeinsohnTBox3cIrh() throws Exception {
        // Tests incoherency caused by number restrictions
        String axioms = "DisjointClasses(:c :d) "
                + "EquivalentClasses(:a ObjectUnionOf(:c :d))"
                + "EquivalentClasses(:complex1 ObjectIntersectionOf("
                + "ObjectAllValuesFrom(:tt :a)" + "ObjectMinCardinality(3 :tt)"
                + "ObjectMaxCardinality(1 :tt :c)"
                + "ObjectMaxCardinality(1 :tt :d)" + "))";
        loadReasonerWithAxioms(axioms);
        assertSatisfiable("complex1", false);
    }

    public void testHeinsohnTBox3() throws Exception {
        // Tests incoherency caused by number restrictions and role hierarchy
        StringBuffer buffer = new StringBuffer();
        buffer.append("DisjointClasses(:c :d :e)");
        buffer.append("SubClassOf(:a ObjectUnionOf(:c :d))");
        buffer.append("SubObjectPropertyOf(:r1 :r)");
        buffer.append("SubObjectPropertyOf(:r2 :r)");
        buffer.append("SubObjectPropertyOf(:r3 :r)");
        buffer.append("SubObjectPropertyOf(:t1 :tt)");
        buffer.append("SubObjectPropertyOf(:t2 :tt)");
        buffer.append("SubObjectPropertyOf(:t3 :tt)");
        buffer.append("EquivalentClasses(:complex1a ObjectIntersectionOf(");
        buffer.append("ObjectMinCardinality(1 :r)");
        buffer.append("ObjectSomeValuesFrom(:r :c)");
        buffer.append("ObjectSomeValuesFrom(:r :d)))");
        buffer.append("EquivalentClasses(:complex1b ");
        buffer.append("ObjectMinCardinality(2 :r))");
        buffer.append("EquivalentClasses(:complex2a ObjectIntersectionOf(");
        buffer.append("ObjectMaxCardinality(2 :r)");
        buffer.append("ObjectSomeValuesFrom(:r :c)");
        buffer.append("ObjectSomeValuesFrom(:r :d)");
        buffer.append("))");
        buffer.append("EquivalentClasses(:complex2b ObjectIntersectionOf(");
        buffer.append("ObjectMaxCardinality(1 :r :c)");
        buffer.append("ObjectMaxCardinality(1 :r :d)");
        buffer.append("))");
        buffer.append("EquivalentClasses(:complex3a ObjectIntersectionOf(");
        buffer.append("ObjectAllValuesFrom(:r :a)");
        buffer.append("ObjectMinCardinality(3 :r)");
        buffer.append("ObjectMaxCardinality(1 :r :c)");
        buffer.append("))");
        buffer.append("EquivalentClasses(:complex3b ");
        buffer.append("ObjectMinCardinality(2 :r :d))");
        buffer.append("EquivalentClasses(:complex4a ObjectIntersectionOf(");
        buffer.append("ObjectSomeValuesFrom(:r1 ");
        buffer.append("ObjectIntersectionOf(ObjectMaxCardinality(1 :tt) ");
        buffer.append("ObjectSomeValuesFrom(:t1 :c)))");
        buffer.append("ObjectSomeValuesFrom(:r2 ");
        buffer.append("ObjectIntersectionOf(ObjectMaxCardinality(1 :tt) ");
        buffer.append("ObjectSomeValuesFrom(:t2 :d)))");
        buffer.append("ObjectSomeValuesFrom(:r2 ");
        buffer.append("ObjectIntersectionOf(ObjectMaxCardinality(1 :tt) ");
        buffer.append("ObjectSomeValuesFrom(:t2 :d)))");
        buffer.append("ObjectSomeValuesFrom(:r3 ");
        buffer.append("ObjectIntersectionOf(ObjectMaxCardinality(1 :tt) ");
        buffer.append("ObjectSomeValuesFrom(:t3 :e)))");
        buffer.append("))");
        buffer.append("EquivalentClasses(:complex4b ");
        buffer.append("ObjectMinCardinality(2 :r))");
        loadReasonerWithAxioms(buffer.toString());
        assertSubsumedBy("complex1a", "complex1b", true);
        assertSubsumedBy("complex2a", "complex2b", true);
        assertSubsumedBy("complex3a", "complex3b", true);
        assertSubsumedBy("complex4a", "complex4b", true);
    }
    public void testHeinsohnTBox3Modified() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(:C :D)");
         buffer.append("SubClassOf(:A ObjectMaxCardinality(2 :r))");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:r :C))");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:r :D))");
         buffer.append("SubClassOf(owl:Thing ObjectUnionOf(ObjectMinCardinality(2 :r :C) ObjectMinCardinality(2 :r :D) :B))");
         loadReasonerWithAxioms(buffer.toString());
         assertSubsumedBy("A","B",true);
     }
     public void testHeinsohnTBox4a() throws Exception {
         // Tests role restrictions
         loadReasonerWithAxioms("");
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLClassExpression D = df.getOWLClass(IRI.create("file:/c/test.owl#D"));
         OWLClassExpression E = df.getOWLClass(IRI.create("file:/c/test.owl#E"));
         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         OWLClassExpression desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectAllValuesFrom(r, D), df.getOWLObjectAllValuesFrom(r, df.getOWLObjectUnionOf(df.getOWLObjectComplementOf(D), E)));
         OWLClassExpression desc2 = df.getOWLObjectAllValuesFrom(r, E);
         assertSubsumedBy(desc1,desc2,true);
     }

     public void testHeinsohnTBox4b() throws Exception {
         // Tests role restrictions
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(:C :D)");
         loadReasonerWithAxioms(buffer.toString());
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLClassExpression C = df.getOWLClass(IRI.create("file:/c/test.owl#C"));
         OWLClassExpression D = df.getOWLClass(IRI.create("file:/c/test.owl#D"));
         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         OWLObjectProperty s = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#s"));
         OWLClassExpression desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectAllValuesFrom(r, df.getOWLObjectUnionOf(df.getOWLObjectComplementOf(df.getOWLObjectMinCardinality(2, s)), C)), df.getOWLObjectAllValuesFrom(r, D));
         OWLClassExpression desc2 = df.getOWLObjectAllValuesFrom(r, df.getOWLObjectMaxCardinality(1, s));
         assertSubsumedBy(desc1,desc2,true);
     }
     
      public void testHeinsohnTBox7() throws Exception {
          // Tests inverse roles
          loadReasonerWithAxioms("");
          OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
          OWLClassExpression A = df.getOWLClass(IRI.create("file:/c/test.owl#A"));
          OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
          OWLObjectPropertyExpression invr = df.getOWLObjectInverseOf(r);
          OWLClassExpression desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectAllValuesFrom(r, df.getOWLObjectAllValuesFrom(invr, A)), df.getOWLObjectSomeValuesFrom(r, df.getOWLThing()));
          assertSubsumedBy(desc1,A,true);
     }
      
     public void testIanT1a() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:p1 ObjectComplementOf(ObjectUnionOf(:p2 :p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p2 ObjectComplementOf(ObjectUnionOf(:p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p3 ObjectComplementOf(ObjectUnionOf(:p4 :p5)))");
         buffer.append("SubClassOf(:p4 ObjectComplementOf(:p5))");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLClassExpression p1 = df.getOWLClass(IRI.create("file:/c/test.owl#p1"));
         OWLClassExpression p2 = df.getOWLClass(IRI.create("file:/c/test.owl#p2"));
         OWLClassExpression p3 = df.getOWLClass(IRI.create("file:/c/test.owl#p3"));
         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));

         OWLClassExpression desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectSomeValuesFrom(r, p1), df.getOWLObjectSomeValuesFrom(r, p2), df.getOWLObjectSomeValuesFrom(r, p3), df.getOWLObjectMaxCardinality(2, r));
         assertSatisfiable(desc1,false);
     }
     public void testIanT1b() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:p1 ObjectComplementOf(ObjectUnionOf(:p2 :p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p2 ObjectComplementOf(ObjectUnionOf(:p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p3 ObjectComplementOf(ObjectUnionOf(:p4 :p5)))");
         buffer.append("SubClassOf(:p4 ObjectComplementOf(:p5))");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLClassExpression p1 = df.getOWLClass(IRI.create("file:/c/test.owl#p1"));
         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         OWLObjectPropertyExpression invr = df.getOWLObjectInverseOf(r);
         
         OWLClassExpression desc1 = df.getOWLObjectSomeValuesFrom(invr, df.getOWLObjectIntersectionOf(df.getOWLObjectSomeValuesFrom(r, p1), df.getOWLObjectMaxCardinality(1, r, p1)));
         assertSatisfiable(desc1,true);
     }
     public void testIanT1c() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:p1 ObjectComplementOf(ObjectUnionOf(:p2 :p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p2 ObjectComplementOf(ObjectUnionOf(:p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p3 ObjectComplementOf(ObjectUnionOf(:p4 :p5)))");
         buffer.append("SubClassOf(:p4 ObjectComplementOf(:p5))");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLClassExpression p1 = df.getOWLClass(IRI.create("file:/c/test.owl#p1"));
         OWLClassExpression p2 = df.getOWLClass(IRI.create("file:/c/test.owl#p2"));
         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         OWLObjectPropertyExpression invr = df.getOWLObjectInverseOf(r);
         
         OWLClassExpression desc1 = df.getOWLObjectIntersectionOf(p2, df.getOWLObjectSomeValuesFrom(invr, df.getOWLObjectIntersectionOf(df.getOWLObjectSomeValuesFrom(r, p1), df.getOWLObjectMaxCardinality(1, r))));
         assertSatisfiable(desc1,false);
     }
     
     public void testIanT2() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubObjectPropertyOf(:r :f1)");
         buffer.append("SubObjectPropertyOf(:r :f2)");
         buffer.append("SubClassOf(:p1 ObjectComplementOf(:p2))");
         buffer.append("FunctionalObjectProperty(:f1)");
         buffer.append("FunctionalObjectProperty(:f2)");
         loadReasonerWithAxioms(buffer.toString());

         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLClassExpression p1 = df.getOWLClass(IRI.create("file:/c/test.owl#p1"));
         OWLClassExpression p2 = df.getOWLClass(IRI.create("file:/c/test.owl#p2"));
         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         OWLObjectProperty f1 = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#f1"));
         OWLObjectProperty f2 = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#f2"));
         
         OWLClassExpression desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectSomeValuesFrom(f1, p1), df.getOWLObjectSomeValuesFrom(f2, p2));
         assertSatisfiable(desc1,true);
         
         desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectSomeValuesFrom(f1, p1), df.getOWLObjectSomeValuesFrom(f2, p2), df.getOWLObjectSomeValuesFrom(r, df.getOWLThing()));
         assertSatisfiable(desc1, false);
     }
     
     public void testIanT3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:p1 ObjectComplementOf(ObjectUnionOf(:p2 :p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p2 ObjectComplementOf(ObjectUnionOf(:p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p3 ObjectComplementOf(ObjectUnionOf(:p4 :p5)))");
         buffer.append("SubClassOf(:p4 ObjectComplementOf(:p5))");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLClassExpression p = df.getOWLClass(IRI.create("file:/c/test.owl#p"));
         OWLClassExpression p1 = df.getOWLClass(IRI.create("file:/c/test.owl#p1"));
         OWLClassExpression p2 = df.getOWLClass(IRI.create("file:/c/test.owl#p2"));
         OWLClassExpression p3 = df.getOWLClass(IRI.create("file:/c/test.owl#p3"));
         OWLClassExpression p4 = df.getOWLClass(IRI.create("file:/c/test.owl#p4"));
         OWLClassExpression p5 = df.getOWLClass(IRI.create("file:/c/test.owl#p5"));
         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));

         OWLClassExpression desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeValuesFrom(r, p1), 
                 df.getOWLObjectSomeValuesFrom(r, p2), 
                 df.getOWLObjectSomeValuesFrom(r, p3),
                 df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p1, p)), 
                 df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p2, p)), 
                 df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p3, p)), 
                 df.getOWLObjectMaxCardinality(3, r));         
         assertSatisfiable(desc,true);
     
         desc = df.getOWLObjectIntersectionOf(
             df.getOWLObjectSomeValuesFrom(r, p1), 
             df.getOWLObjectSomeValuesFrom(r, p2), 
             df.getOWLObjectSomeValuesFrom(r, p3),
             df.getOWLObjectSomeValuesFrom(r, p4), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p1, p)), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p2, p)), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p3, p)), 
             df.getOWLObjectMaxCardinality(3, r)
         );
         assertSatisfiable(desc,false);
     
         desc = df.getOWLObjectIntersectionOf(
             df.getOWLObjectSomeValuesFrom(r, p1), 
             df.getOWLObjectSomeValuesFrom(r, p2), 
             df.getOWLObjectSomeValuesFrom(r, p3),
             df.getOWLObjectSomeValuesFrom(r, p4), 
             df.getOWLObjectMaxCardinality(3, r)
         );
         assertSatisfiable(desc,false);
     
         desc = df.getOWLObjectIntersectionOf(
             df.getOWLObjectSomeValuesFrom(r, p1), 
             df.getOWLObjectSomeValuesFrom(r, p2), 
             df.getOWLObjectSomeValuesFrom(r, p3),
             df.getOWLObjectSomeValuesFrom(r, p4), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p1, p)), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p2, p)), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p3, p)),
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p4, p)),
             df.getOWLObjectMaxCardinality(4, r));
         assertSatisfiable(desc,true);

         desc = df.getOWLObjectIntersectionOf(
             df.getOWLObjectSomeValuesFrom(r, p1), 
             df.getOWLObjectSomeValuesFrom(r, p2), 
             df.getOWLObjectSomeValuesFrom(r, p3),
             df.getOWLObjectSomeValuesFrom(r, p4),
             df.getOWLObjectSomeValuesFrom(r, p5), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p1, p)), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p2, p)), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p3, p)),
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p4, p)),
             df.getOWLObjectMaxCardinality(4, r));
         assertSatisfiable(desc,false);

         desc = df.getOWLObjectIntersectionOf(
             df.getOWLObjectSomeValuesFrom(r, p1), 
             df.getOWLObjectSomeValuesFrom(r, p2), 
             df.getOWLObjectSomeValuesFrom(r, p3),
             df.getOWLObjectSomeValuesFrom(r, p4),
             df.getOWLObjectSomeValuesFrom(r, p5), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p1, p)), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p2, p)), 
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p3, p)),
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p4, p)),
             df.getOWLObjectSomeValuesFrom(r, df.getOWLObjectIntersectionOf(p5, p)),
             df.getOWLObjectMaxCardinality(5, r));
         assertSatisfiable(desc,true);
     }
     
     public void testIanT4() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("TransitiveObjectProperty(:p)");
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("InverseObjectProperties(:p :p-)");
         buffer.append("InverseObjectProperties(:s :s-)");
         buffer.append("EquivalentClasses(:c ObjectAllValuesFrom(:r- ObjectAllValuesFrom(:p- ObjectAllValuesFrom(:s- ObjectComplementOf(:a)))))");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLClassExpression a = df.getOWLClass(IRI.create("file:/c/test.owl#a"));
         OWLClassExpression c = df.getOWLClass(IRI.create("file:/c/test.owl#c"));
         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         OWLObjectProperty s = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#s"));
         OWLObjectProperty p = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#p"));
         
         OWLClassExpression desc =
             df.getOWLObjectIntersectionOf(a, 
                 df.getOWLObjectSomeValuesFrom(s, 
                     df.getOWLObjectIntersectionOf(
                         df.getOWLObjectSomeValuesFrom(r, df.getOWLThing()), 
                         df.getOWLObjectSomeValuesFrom(p, df.getOWLThing()), 
                         df.getOWLObjectAllValuesFrom(r, c), 
                         df.getOWLObjectAllValuesFrom(p, df.getOWLObjectSomeValuesFrom(r, df.getOWLThing())), 
                         df.getOWLObjectAllValuesFrom(p, df.getOWLObjectSomeValuesFrom(p, df.getOWLThing())),
                         df.getOWLObjectAllValuesFrom(p, df.getOWLObjectAllValuesFrom(r, c)))));  
         assertSatisfiable(desc,false);
     }

     public void testIanT5() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("InverseObjectProperties(:f :f-)");
         buffer.append("TransitiveObjectProperty(:r)");
         buffer.append("SubObjectPropertyOf(:f :r)");
         buffer.append("FunctionalObjectProperty(:f)");
         loadReasonerWithAxioms(buffer.toString());
      
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLClassExpression a = df.getOWLClass(IRI.create("file:/c/test.owl#a"));
         OWLObjectProperty invr = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));
         OWLObjectProperty invf = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#f-"));
      
         OWLClassExpression desc = df.getOWLObjectIntersectionOf(
              df.getOWLObjectComplementOf(a), 
              df.getOWLObjectSomeValuesFrom(invf, a), 
              df.getOWLObjectAllValuesFrom(invr, df.getOWLObjectSomeValuesFrom(invf, a))
          );  
         assertSatisfiable(desc,true);
     }
     public void testIanT6() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("InverseObjectProperties(:f :f-)");
         buffer.append("TransitiveObjectProperty(:r)");
         buffer.append("SubObjectPropertyOf(:f :r)");
         buffer.append("FunctionalObjectProperty(:f)");
         buffer.append("EquivalentClasses(:d ObjectIntersectionOf(:c ObjectSomeValuesFrom(:f ObjectComplementOf(:c))))");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c"));
         OWLClassExpression d = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#d"));
         OWLObjectProperty invr = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));
         OWLObjectProperty invf = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f-"));
         
         OWLClassExpression desc = m_dataFactory.getOWLObjectIntersectionOf(
             m_dataFactory.getOWLObjectComplementOf(c), 
             m_dataFactory.getOWLObjectSomeValuesFrom(invf, d), 
             m_dataFactory.getOWLObjectAllValuesFrom(invr, m_dataFactory.getOWLObjectSomeValuesFrom(invf, d))
         );  
         assertSatisfiable(desc,false);
     }
     
     public void testIanT7a() throws Exception {
         StringBuffer buffer=new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("InverseObjectProperties(:f :f-)");
         buffer.append("TransitiveObjectProperty(:r)");
         buffer.append("FunctionalObjectProperty(:f)");
         loadReasonerWithAxioms(buffer.toString());

         OWLClassExpression p1=m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p1"));
         OWLObjectProperty r=m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         OWLObjectProperty invr=m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));

         OWLClassExpression desc=
             m_dataFactory.getOWLObjectIntersectionOf(
             p1,
             m_dataFactory.getOWLObjectSomeValuesFrom(r,
                     m_dataFactory.getOWLObjectSomeValuesFrom(r,
                         m_dataFactory.getOWLObjectIntersectionOf(
                         p1,
                         m_dataFactory.getOWLObjectAllValuesFrom(invr,
                                 m_dataFactory.getOWLObjectComplementOf(p1)
                         )
                     )
                 )
             )
         );
         // [and p1 [some r [some r [and p1 [all r- [not p1]]]]]]
         assertSatisfiable(desc,false);
     }
     
     public void testIanT7b() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("InverseObjectProperties(:f :f-)");
         buffer.append("TransitiveObjectProperty(:r)");
         buffer.append("FunctionalObjectProperty(:f)");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLClassExpression p1 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p1"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         OWLObjectProperty invr = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));

         OWLClassExpression desc;
         desc = m_dataFactory.getOWLObjectIntersectionOf(
                 p1, 
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, 
                         m_dataFactory.getOWLObjectSomeValuesFrom(
                                 r, 
                                 m_dataFactory.getOWLObjectIntersectionOf(
                                         p1, 
                                         m_dataFactory.getOWLObjectAllValuesFrom(invr, 
                                                 m_dataFactory.getOWLObjectUnionOf(
                                                         m_dataFactory.getOWLObjectComplementOf(p1), 
                                                         m_dataFactory.getOWLObjectAllValuesFrom(r, p1)
                                                 )
                                         )
                                 )
                         )
                 )
             ); 
         
         assertSatisfiable(desc,true);
     }
     
     public void testIanT7c() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("InverseObjectProperties(:f :f-)");
         buffer.append("TransitiveObjectProperty(:r)");
         buffer.append("FunctionalObjectProperty(:f)");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLClassExpression p1 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p1"));
         OWLObjectProperty f = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f"));
         OWLObjectProperty invf = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f-"));
         
         OWLClassExpression desc;
         desc = m_dataFactory.getOWLObjectSomeValuesFrom(f, 
                 m_dataFactory.getOWLObjectIntersectionOf(
                         p1,
                         m_dataFactory.getOWLObjectAllValuesFrom(invf, 
                                 m_dataFactory.getOWLObjectSomeValuesFrom(
                                         f,
                                         m_dataFactory.getOWLObjectComplementOf(p1)
                                 )
                         )
                 )
             ); 
         assertSatisfiable(desc,false);
     }
     
     public void testIanT8a() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         loadReasonerWithAxioms(buffer.toString());
         
         
         OWLClassExpression p = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         OWLObjectProperty invr = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));
         OWLObjectProperty r1 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r1"));
         
         OWLClassExpression desc;
         desc = m_dataFactory.getOWLObjectIntersectionOf(m_dataFactory.getOWLObjectSomeValuesFrom(r, 
                 m_dataFactory.getOWLObjectAllValuesFrom(invr, m_dataFactory.getOWLObjectAllValuesFrom(r1, p))), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectAllValuesFrom(invr, 
                         m_dataFactory.getOWLObjectAllValuesFrom(r1, m_dataFactory.getOWLObjectComplementOf(p)))));
         
         assertSatisfiable(desc,true);
     }
     
     public void testIanT8() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLClassExpression p = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         OWLObjectProperty invr = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));
         OWLObjectProperty r1 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r1"));
         
         OWLClassExpression desc;
         desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(r1, m_dataFactory.getOWLThing()), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectAllValuesFrom(invr, m_dataFactory.getOWLObjectAllValuesFrom(r1, p))), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectAllValuesFrom(invr, m_dataFactory.getOWLObjectAllValuesFrom(r1, m_dataFactory.getOWLObjectComplementOf(p)))));
         assertSatisfiable(desc,false);
     }
     
     public void testIanT9() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:successor :successor-)");
         buffer.append("TransitiveObjectProperty(:descendant)");
         buffer.append("SubObjectPropertyOf(:successor :descendant)");
         buffer.append("InverseFunctionalObjectProperty(:successor)");
         buffer.append("SubClassOf(:root ObjectComplementOf(ObjectSomeValuesFrom(:successor- owl:Thing)))");
         buffer.append("SubClassOf(:Infinite-Tree-Node ObjectIntersectionOf(:node ObjectSomeValuesFrom(:successor :Infinite-Tree-Node)))");
         buffer.append("SubClassOf(:Infinite-Tree-Root ObjectIntersectionOf(:Infinite-Tree-Node :root))");
         
         loadReasonerWithAxioms(buffer.toString());
         assertSatisfiable("file:/c/test.owl#Infinite-Tree-Root",true);
         
         OWLClassExpression itr = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#Infinite-Tree-Root"));
         OWLClassExpression root = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#root"));
         OWLObjectProperty descendant = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#descendant"));
         OWLObjectProperty invsuccessor = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#successor-"));
         
         // [and Infinite-Tree-Root [all descendant [some successor- root]]]
         OWLClassExpression desc =
             m_dataFactory.getOWLObjectIntersectionOf(
                 itr, 
                 m_dataFactory.getOWLObjectAllValuesFrom(descendant, 
                     m_dataFactory.getOWLObjectSomeValuesFrom(invsuccessor, root)
                 )
             );
         assertSatisfiable(desc,false);
     }
     public void testIanT10() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:s :s-)");
         buffer.append("InverseObjectProperties(:f :f-)");
         buffer.append("InverseObjectProperties(:f1 :f1-)");
         buffer.append("FunctionalObjectProperty(:f)");
         buffer.append("FunctionalObjectProperty(:f1)");
         buffer.append("SubObjectPropertyOf(:s :f)");
         buffer.append("SubObjectPropertyOf(:s :f1)");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLClassExpression p = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p"));
         OWLObjectProperty f = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f"));
         OWLObjectProperty invf = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f-"));
         OWLObjectProperty f1 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f1"));
         OWLObjectProperty invf1 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f1-"));
         OWLObjectProperty s= m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#s"));
         OWLObjectProperty invs= m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#s-"));
         
         OWLClassExpression desc;
         desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectComplementOf(p), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(f, 
                         m_dataFactory.getOWLObjectIntersectionOf(
                                 m_dataFactory.getOWLObjectAllValuesFrom(invs, p), 
                                 m_dataFactory.getOWLObjectAllValuesFrom(invf, m_dataFactory.getOWLObjectSomeValuesFrom(s, p))
                         )
                 )
         );
         
         assertSatisfiable(desc,false);
         
         desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectAllValuesFrom(s, 
                         m_dataFactory.getOWLObjectComplementOf(p)
                 ), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(s, 
                         m_dataFactory.getOWLObjectIntersectionOf(
                                 p, 
                                 m_dataFactory.getOWLObjectSomeValuesFrom(invs, p)
                         )
                 )
         );
         assertSatisfiable(desc,false);
         
         desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(f, p), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(f1, m_dataFactory.getOWLObjectComplementOf(p))
         );
         assertSatisfiable(desc,true);
         
         desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(f, p), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(s, m_dataFactory.getOWLThing()),
                 m_dataFactory.getOWLObjectSomeValuesFrom(f1, m_dataFactory.getOWLObjectComplementOf(p))
         );
         assertSatisfiable(desc,false);
         
         desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(f1, p), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(f1, 
                         m_dataFactory.getOWLObjectIntersectionOf(
                                 m_dataFactory.getOWLObjectComplementOf(p),
                                 m_dataFactory.getOWLObjectAllValuesFrom(invf1, 
                                         m_dataFactory.getOWLObjectSomeValuesFrom(s, m_dataFactory.getOWLThing())
                                 )
                         )
                 )
         );
         assertSatisfiable(desc,false);
     }
     
     public void testIanT11() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:s :s-)");
         buffer.append("SubObjectPropertyOf(:s :r)");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLClassExpression p = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p"));
         OWLObjectProperty s= m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#s"));
         OWLObjectProperty invs = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#s-"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));

         OWLClassExpression desc;
         desc = m_dataFactory.getOWLObjectIntersectionOf(
                     m_dataFactory.getOWLObjectComplementOf(p), 
                     m_dataFactory.getOWLObjectMaxCardinality(1, r), 
                     m_dataFactory.getOWLObjectSomeValuesFrom(r, 
                             m_dataFactory.getOWLObjectAllValuesFrom(invs, p)
                     ), 
                     m_dataFactory.getOWLObjectSomeValuesFrom(s, p)
         );
         
         assertSatisfiable(desc,false);
     }
     
     public void testIanT12() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLClassExpression p = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p"));
         OWLClassExpression q = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#q"));
         OWLObjectProperty s = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#s"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         OWLObjectProperty invr = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));
         
         OWLClassExpression desc;
         desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(s, 
                         m_dataFactory.getOWLObjectIntersectionOf(
                                 m_dataFactory.getOWLObjectComplementOf(p), 
                                 m_dataFactory.getOWLObjectComplementOf(q)
                         )
                 ), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, 
                         m_dataFactory.getOWLObjectIntersectionOf(
                                 m_dataFactory.getOWLObjectMaxCardinality(1, invr), 
                                 m_dataFactory.getOWLObjectSomeValuesFrom(invr, 
                                         m_dataFactory.getOWLObjectAllValuesFrom(s, p)
                                 )
                         )
                 )
         );
         
         assertSatisfiable(desc,false);
     }
     
     public void testIanT13() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:s :s-)");
         buffer.append("EquivalentClasses(:a1 ObjectSomeValuesFrom(:s ObjectAllValuesFrom(:s- ObjectAllValuesFrom(:r :c))))");
         buffer.append("EquivalentClasses(:a2 ObjectSomeValuesFrom(:s ObjectAllValuesFrom(:s- ObjectAllValuesFrom(:r ObjectComplementOf(:c)))))");
         buffer.append("EquivalentClasses(:a3a ObjectSomeValuesFrom(:s ObjectAllValuesFrom(:s- ObjectUnionOf(ObjectSomeValuesFrom(:r :d) ObjectSomeValuesFrom(:s :d)))))");
         buffer.append("EquivalentClasses(:a3b ObjectUnionOf(ObjectSomeValuesFrom(:r :d) ObjectSomeValuesFrom(:s :d)))");
         buffer.append("EquivalentClasses(:a3c ObjectUnionOf(ObjectSomeValuesFrom(:r :d) :d))");
         buffer.append("EquivalentClasses(:a3e ObjectSomeValuesFrom(:r :d))");
         loadReasonerWithAxioms(buffer.toString());
                  
         OWLClassExpression a = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a"));
         OWLClassExpression a1 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a1"));
         OWLClassExpression a2 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a2"));
         OWLClassExpression a3a = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a3a"));
         OWLClassExpression a3b = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a3b"));
         OWLClassExpression a3c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a3c"));
         OWLClassExpression a3e = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a3e"));

         OWLClassExpression desc = m_dataFactory.getOWLObjectIntersectionOf(a3a, a2, a1);
         assertSatisfiable(desc,true);
         desc = m_dataFactory.getOWLObjectIntersectionOf(a3b, a2, a1);
         assertSatisfiable(desc,true);
         desc = m_dataFactory.getOWLObjectIntersectionOf(a3c, a2, a1);
         assertSatisfiable(desc,true);
         desc = m_dataFactory.getOWLObjectIntersectionOf(a3e, a2, a1);
         assertSatisfiable(desc,false);
         desc = m_dataFactory.getOWLObjectIntersectionOf(a, a2, a1);
         assertSatisfiable(desc,true);
         desc = m_dataFactory.getOWLObjectIntersectionOf(m_dataFactory.getOWLObjectIntersectionOf(a3a, a2, a1), m_dataFactory.getOWLObjectComplementOf(m_dataFactory.getOWLObjectIntersectionOf(a3b, a2, a1)));
         assertSatisfiable(desc,false);
         desc = m_dataFactory.getOWLObjectIntersectionOf(m_dataFactory.getOWLObjectComplementOf(m_dataFactory.getOWLObjectIntersectionOf(a3a, a2, a1)), m_dataFactory.getOWLObjectIntersectionOf(a3b, a2, a1));
         assertSatisfiable(desc,false);
         desc = m_dataFactory.getOWLObjectIntersectionOf(m_dataFactory.getOWLObjectIntersectionOf(a3c, a2, a1), m_dataFactory.getOWLObjectComplementOf(m_dataFactory.getOWLObjectIntersectionOf(a3c, a2, a1)));
         assertSatisfiable(desc,false);
     }
     
     public void testIanFact1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(:a :b :c)");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLClassExpression a = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a"));
         OWLClassExpression b = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#b"));
         OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c"));

         OWLClassExpression desc = m_dataFactory.getOWLObjectUnionOf(
                 m_dataFactory.getOWLObjectIntersectionOf(a, b),
                 m_dataFactory.getOWLObjectIntersectionOf(a, c),
                 m_dataFactory.getOWLObjectIntersectionOf(b, c)
         );
         assertSatisfiable(desc,false);
     }
     
     public void testIanFact2() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:c ObjectAllValuesFrom(:r :c))");
         buffer.append("SubClassOf(ObjectAllValuesFrom(:r :c) :d)");
         loadReasonerWithAxioms(buffer.toString());
         
         assertSubsumedBy("c","d",true);
     }
     
     public void testIanFact3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("FunctionalObjectProperty(:f1)");
         buffer.append("FunctionalObjectProperty(:f2)");
         buffer.append("FunctionalObjectProperty(:f3)");
         buffer.append("SubObjectPropertyOf(:f3 :f1)");
         buffer.append("SubObjectPropertyOf(:f3 :f2)");
         loadReasonerWithAxioms(buffer.toString());
         
         
         OWLClassExpression p1 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p1"));
         OWLClassExpression p2 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p2"));
         OWLObjectProperty f1 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f1"));
         OWLObjectProperty f2 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f2"));
         OWLObjectProperty f3 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f3"));
         
         OWLClassExpression desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(f1, p1),
                 m_dataFactory.getOWLObjectSomeValuesFrom(f2, m_dataFactory.getOWLObjectComplementOf(p1)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(f3, p2) 
         );
         assertSatisfiable(desc,false);
     }
     
     public void testIanFact4() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("FunctionalObjectProperty(:rx)");
        buffer.append("FunctionalObjectProperty(:rx3)");
        buffer.append("SubObjectPropertyOf(:rx3 :rx)");
        buffer.append("SubObjectPropertyOf(:rx3 :rx1)");
        buffer.append("FunctionalObjectProperty(:rx4)");
        buffer.append("SubObjectPropertyOf(:rx4 :rx)");
        buffer.append("SubObjectPropertyOf(:rx4 :rx2)");
        buffer.append("FunctionalObjectProperty(:rx3a)");
        buffer.append("SubObjectPropertyOf(:rx3a :rxa)");
        buffer.append("SubObjectPropertyOf(:rx3a :rx1a)");
        buffer.append("FunctionalObjectProperty(:rx4a)");
        buffer.append("SubObjectPropertyOf(:rx4a :rxa)");
        buffer.append("SubObjectPropertyOf(:rx4a :rx2a)");

        loadReasonerWithAxioms(buffer.toString());

        OWLClassExpression c1 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c1"));
        OWLClassExpression c2 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c2"));
        OWLObjectProperty rx3 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#rx3"));
        OWLObjectProperty rx4 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#rx4"));
        OWLObjectProperty rx3a = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#rx3a"));
        OWLObjectProperty rx4a = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#rx4a"));

        OWLClassExpression desc1 = m_dataFactory.getOWLObjectIntersectionOf(
                m_dataFactory.getOWLObjectSomeValuesFrom(rx3, c1),
                m_dataFactory.getOWLObjectSomeValuesFrom(rx4, c2)
        );
        OWLClassExpression desc2 = m_dataFactory.getOWLObjectSomeValuesFrom(rx3, m_dataFactory.getOWLObjectIntersectionOf(c1, c2));
        assertSubsumedBy(desc1,desc2,true);
        desc1 = m_dataFactory.getOWLObjectIntersectionOf(
                m_dataFactory.getOWLObjectSomeValuesFrom(rx3a, c1),
                m_dataFactory.getOWLObjectSomeValuesFrom(rx4a, c2)
        );
        desc2 = m_dataFactory.getOWLObjectSomeValuesFrom(rx3a, m_dataFactory.getOWLObjectIntersectionOf(c1, c2));
        assertSubsumedBy(desc1,desc2,false);
    }
     public void testIanBug1b() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("EquivalentClasses(:c ObjectIntersectionOf(:a ObjectComplementOf(:b)))");
         buffer.append("SubClassOf(:a ObjectIntersectionOf(:d ObjectComplementOf(:c)))");
         loadReasonerWithAxioms(buffer.toString());
         
         
         OWLClassExpression a = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a"));
         OWLClassExpression b = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#b"));
         OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c"));
         OWLClassExpression d = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#d"));
         
         OWLClassExpression desc = m_dataFactory.getOWLObjectIntersectionOf(m_dataFactory.getOWLObjectComplementOf(c), a, m_dataFactory.getOWLObjectComplementOf(b), d);
         assertSatisfiable(desc,false);
     }
     public void testIanBug3() throws Exception {
         // slow, but works!
         loadReasonerWithAxioms("");
         
         OWLClassExpression a = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a"));
         OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c"));
         OWLClassExpression d = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#d"));
         OWLClassExpression e = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#e"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         
         OWLClassExpression desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, a), 
                 m_dataFactory.getOWLObjectMinCardinality(3, r, c), 
                 m_dataFactory.getOWLObjectMinCardinality(3, r, d),
                 m_dataFactory.getOWLObjectMinCardinality(2, r, m_dataFactory.getOWLObjectIntersectionOf(
                         e, 
                         m_dataFactory.getOWLObjectComplementOf(m_dataFactory.getOWLObjectIntersectionOf(c, d)))), 
                 m_dataFactory.getOWLObjectMaxCardinality(4, r), 
                 m_dataFactory.getOWLObjectMaxCardinality(2, r, m_dataFactory.getOWLObjectIntersectionOf(c, d)) 
         );
         assertSatisfiable(desc,true);
     }
     
     public void testIanBug4() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("SubObjectPropertyOf(:r :r-)");
         buffer.append("TransitiveObjectProperty(:r)");
         loadReasonerWithAxioms(buffer.toString());
         
         
         OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         
         // [and c [some r owl:Thing] [all r [not c]]]
         OWLClassExpression desc =
             m_dataFactory.getOWLObjectIntersectionOf(
                 c, 
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLThing()), 
                 m_dataFactory.getOWLObjectAllValuesFrom(r, m_dataFactory.getOWLObjectComplementOf(c))
             );
         assertSatisfiable(desc,false);
         
         // [and c [some r [some r c]] [all r [not c]]]
         desc = m_dataFactory.getOWLObjectIntersectionOf(
             c,
             m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectSomeValuesFrom(r, c)),
             m_dataFactory.getOWLObjectAllValuesFrom(r, m_dataFactory.getOWLObjectComplementOf(c))
         );
         assertSatisfiable(desc,false);
     }
     
     public void testIanBug5() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("TransitiveObjectProperty(:r1)");
         buffer.append("SubObjectPropertyOf(:r2 :r1)");
         buffer.append("TransitiveObjectProperty(:r2)");
         loadReasonerWithAxioms(buffer.toString());
         
         OWLClassExpression p = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p"));
         OWLObjectProperty r1 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r1"));
         OWLObjectProperty r2 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r2"));
         
         // [and [all r1 p] [some r2 [some r1 [not p]]]]
         OWLClassExpression desc =
             m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectAllValuesFrom(r1, p),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r2, m_dataFactory.getOWLObjectSomeValuesFrom(r1, m_dataFactory.getOWLObjectComplementOf(p)))
             );
         assertSatisfiable(desc,false);
     }
     
     public void testIanBug6() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubObjectPropertyOf(:S1 :R)");
         buffer.append("TransitiveObjectProperty(:S1)");
         buffer.append("SubObjectPropertyOf(:S2 :R)");
         buffer.append("TransitiveObjectProperty(:S2)");
         buffer.append("SubObjectPropertyOf(:P :S1)");
         buffer.append("SubObjectPropertyOf(:P :S2)");
         loadReasonerWithAxioms(buffer.toString());
         
         
         OWLClassExpression C = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#C"));
         OWLObjectProperty R = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#R"));
         OWLObjectProperty P = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#P"));
         OWLObjectProperty S1 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#S1"));
         OWLObjectProperty S2 = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#S2"));
         
         // [and [all R C] [some P [some S1 [not C]]]]
         OWLClassExpression desc =
             m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectAllValuesFrom(R, C), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(P, m_dataFactory.getOWLObjectSomeValuesFrom(S1, m_dataFactory.getOWLObjectComplementOf(C)))
             );
         assertSatisfiable(desc,false);
         
         // [and [all R C] [some P [some S2 [not C]]]]
         desc = m_dataFactory.getOWLObjectIntersectionOf(
             m_dataFactory.getOWLObjectAllValuesFrom(R, C), 
             m_dataFactory.getOWLObjectSomeValuesFrom(P, m_dataFactory.getOWLObjectSomeValuesFrom(S2, m_dataFactory.getOWLObjectComplementOf(C)))
         );
         assertSatisfiable(desc,false);
     }
     
     public void testIanBug7() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:A ObjectComplementOf(:B))");
         loadReasonerWithAxioms(buffer.toString());
         
         
         OWLClassExpression A = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#A"));
         OWLClassExpression B = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#B"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         
         // [and [some r A] [atMost 1 r A] [some r B] [atMost 1 r B]]
         OWLClassExpression desc =
             m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, A), 
                 m_dataFactory.getOWLObjectMaxCardinality(1, r, A),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, B),
                 m_dataFactory.getOWLObjectMaxCardinality(1, r, B)
             );
         assertSatisfiable(desc,true);
     }
     public void testIanBug8() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:X ObjectComplementOf(:Y))");
         buffer.append("SubClassOf(:A ObjectIntersectionOf(ObjectMinCardinality(1 :r :X) ObjectMaxCardinality(1 :r :X)))");
         buffer.append("SubClassOf(:A ObjectIntersectionOf(ObjectMinCardinality(1 :r :Y) ObjectMaxCardinality(1 :r :Y)))");
         loadReasonerWithAxioms(buffer.toString());
         assertSatisfiable("file:/c/test.owl#A",true);
     }
     
     public void testIanMergeTest1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("SubClassOf(:c ObjectSomeValuesFrom(:r ObjectAllValuesFrom(:r- ObjectComplementOf(:d))))");
         loadReasonerWithAxioms(buffer.toString());

         
         OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c"));
         OWLClassExpression c1 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c1"));
         OWLClassExpression c2 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c2"));
         OWLClassExpression c3 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c3"));
         OWLClassExpression c4 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c4"));
         OWLClassExpression c5 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c5"));
         OWLClassExpression c6 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c6"));
         OWLClassExpression c7 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c7"));
         OWLClassExpression c8 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c8"));
         OWLClassExpression c9 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c9"));
         OWLClassExpression c10 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c10"));
         OWLClassExpression c11 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c11"));
         OWLClassExpression c12 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c12"));
         OWLClassExpression c13 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c13"));
         OWLClassExpression c14 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c14"));
         OWLClassExpression c15 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c15"));
         OWLClassExpression c16 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c16"));
         OWLClassExpression c17 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c17"));
         OWLClassExpression c18 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c18"));
         OWLClassExpression d = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#d"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         
         OWLClassExpression desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, c1), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c2)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c3)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c4)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c5)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c6)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c7)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c8)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c9)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c10)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c11)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c12)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c13)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c14)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c15)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c16)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c17)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, c18),
                 m_dataFactory.getOWLObjectMaxCardinality(1, r, d)
         );
         assertSatisfiable(desc,true);
     }
     public void testIanMergeTest2() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("SubClassOf(:c ObjectSomeValuesFrom(:r ObjectAllValuesFrom(:r- :d)))");
         loadReasonerWithAxioms(buffer.toString());
         
         
         OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c"));
         OWLClassExpression c1 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c1"));
         OWLClassExpression c2 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c2"));
         OWLClassExpression c3 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c3"));
         OWLClassExpression c4 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c4"));
         OWLClassExpression c5 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c5"));
         OWLClassExpression c6 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c6"));
         OWLClassExpression c7 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c7"));
         OWLClassExpression c8 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c8"));
         OWLClassExpression c9 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c9"));
         OWLClassExpression c10 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c10"));
         OWLClassExpression c11 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c11"));
         OWLClassExpression c12 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c12"));
         OWLClassExpression c13 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c13"));
         OWLClassExpression c14 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c14"));
         OWLClassExpression c15 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c15"));
         OWLClassExpression c16 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c16"));
         OWLClassExpression c17 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c17"));
         OWLClassExpression c18 = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c18"));
         OWLClassExpression d = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#d"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         
         OWLClassExpression desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, c1), 
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c2)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c3)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c4)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c5)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c6)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c7)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c8)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c9)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c10)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c11)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c12)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c13)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c14)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c15)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c16)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, m_dataFactory.getOWLObjectIntersectionOf(c, c17)),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, c18),
                 m_dataFactory.getOWLObjectMaxCardinality(1, r, d)
         );
         
         assertSatisfiable(desc,true);
     }
     
     public void testIanQNRTest() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubObjectPropertyOf(:son :child)");
         buffer.append("SubObjectPropertyOf(:daughter :child)");
         buffer.append("EquivalentClasses(:A ObjectIntersectionOf(ObjectMinCardinality(2 :son :male) ObjectMinCardinality(2 :daughter ObjectComplementOf(:male))))");
         buffer.append("EquivalentClasses(:B ObjectMinCardinality(4 :child))");
         loadReasonerWithAxioms(buffer.toString());
         assertSubsumedBy("file:/c/test.owl#A","file:/c/test.owl#B",true);
     }
     
     public void testIanRecursiveDefinitionTest1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:A ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :B) ObjectSomeValuesFrom(:R1 :B) ObjectSomeValuesFrom(:R2 :B) ObjectSomeValuesFrom(:R3 :B) ObjectSomeValuesFrom(:R4 :B) ObjectSomeValuesFrom(:R5 :B) ObjectSomeValuesFrom(:R6 :B) ObjectSomeValuesFrom(:R7 :B) ObjectSomeValuesFrom(:R8 :B) ObjectSomeValuesFrom(:R9 :B)))");
         buffer.append("SubClassOf(:B ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :A) ObjectSomeValuesFrom(:R1 :A) ObjectSomeValuesFrom(:R2 :A) ObjectSomeValuesFrom(:R3 :A) ObjectSomeValuesFrom(:R4 :A) ObjectSomeValuesFrom(:R5 :A) ObjectSomeValuesFrom(:R6 :A) ObjectSomeValuesFrom(:R7 :A) ObjectSomeValuesFrom(:R8 :A) ObjectSomeValuesFrom(:R9 :A)))");
         loadReasonerWithAxioms(buffer.toString());
         assertSatisfiable("file:/c/test.owl#A",true);
     }

      public void testIanRecursiveDefinitionTest2() throws Exception {
          StringBuffer buffer = new StringBuffer();
          buffer.append("SubClassOf(:A ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :B) ObjectSomeValuesFrom(:R1 :B) ObjectSomeValuesFrom(:R2 :B) ObjectSomeValuesFrom(:R3 :B) ObjectSomeValuesFrom(:R4 :B) ObjectSomeValuesFrom(:R5 :B) ObjectSomeValuesFrom(:R6 :B) ObjectSomeValuesFrom(:R7 :B) ObjectSomeValuesFrom(:R8 :B) ObjectSomeValuesFrom(:R9 :B)))");
          buffer.append("SubClassOf(:B ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :C) ObjectSomeValuesFrom(:R1 :C) ObjectSomeValuesFrom(:R2 :C) ObjectSomeValuesFrom(:R3 :C) ObjectSomeValuesFrom(:R4 :C) ObjectSomeValuesFrom(:R5 :C) ObjectSomeValuesFrom(:R6 :C) ObjectSomeValuesFrom(:R7 :C) ObjectSomeValuesFrom(:R8 :C) ObjectSomeValuesFrom(:R9 :C)))");
          buffer.append("SubClassOf(:C ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :A) ObjectSomeValuesFrom(:R1 :A) ObjectSomeValuesFrom(:R2 :A) ObjectSomeValuesFrom(:R3 :A) ObjectSomeValuesFrom(:R4 :A) ObjectSomeValuesFrom(:R5 :A) ObjectSomeValuesFrom(:R6 :A) ObjectSomeValuesFrom(:R7 :A) ObjectSomeValuesFrom(:R8 :A) ObjectSomeValuesFrom(:R9 :A)))");
          loadReasonerWithAxioms(buffer.toString());
          assertSatisfiable("file:/c/test.owl#A",true);
      }
     public void testIanRecursiveDefinitionTest3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:A ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :B) ObjectSomeValuesFrom(:R1 :B) ObjectSomeValuesFrom(:R2 :B) ObjectSomeValuesFrom(:R3 :B) ObjectSomeValuesFrom(:R4 :B) ObjectSomeValuesFrom(:R5 :B) ObjectSomeValuesFrom(:R6 :B) ObjectSomeValuesFrom(:R7 :B) ObjectSomeValuesFrom(:R8 :B) ObjectSomeValuesFrom(:R9 :B)))");
         buffer.append("SubClassOf(:B ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :C) ObjectSomeValuesFrom(:R1 :C) ObjectSomeValuesFrom(:R2 :C) ObjectSomeValuesFrom(:R3 :C) ObjectSomeValuesFrom(:R4 :C) ObjectSomeValuesFrom(:R5 :C) ObjectSomeValuesFrom(:R6 :C) ObjectSomeValuesFrom(:R7 :C) ObjectSomeValuesFrom(:R8 :C) ObjectSomeValuesFrom(:R9 :C)))");
         buffer.append("SubClassOf(:C ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :D) ObjectSomeValuesFrom(:R1 :D) ObjectSomeValuesFrom(:R2 :D) ObjectSomeValuesFrom(:R3 :D) ObjectSomeValuesFrom(:R4 :D) ObjectSomeValuesFrom(:R5 :D) ObjectSomeValuesFrom(:R6 :D) ObjectSomeValuesFrom(:R7 :D) ObjectSomeValuesFrom(:R8 :D) ObjectSomeValuesFrom(:R9 :D)))");
         buffer.append("SubClassOf(:D ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :A) ObjectSomeValuesFrom(:R1 :A) ObjectSomeValuesFrom(:R2 :A) ObjectSomeValuesFrom(:R3 :A) ObjectSomeValuesFrom(:R4 :A) ObjectSomeValuesFrom(:R5 :A) ObjectSomeValuesFrom(:R6 :A) ObjectSomeValuesFrom(:R7 :A) ObjectSomeValuesFrom(:R8 :A) ObjectSomeValuesFrom(:R9 :A)))");
         loadReasonerWithAxioms(buffer.toString());
         assertSatisfiable("file:/c/test.owl#A",true);
     }
     public void testIanBackjumping1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:C1 ObjectIntersectionOf(" +
                         "ObjectUnionOf(:A0 :B0) " +
                         "ObjectUnionOf(:A1 :B1)" +
                         "ObjectUnionOf(:A2 :B2)" +
                         "ObjectUnionOf(:A3 :B3)" +
                         "ObjectUnionOf(:A4 :B4)" +
                         "ObjectUnionOf(:A5 :B5)" +
                         "ObjectUnionOf(:A6 :B6)" +
                         "ObjectUnionOf(:A7 :B7)" +
                         "ObjectUnionOf(:A8 :B8)" +
                         "ObjectUnionOf(:A9 :B9)" +
                         "ObjectUnionOf(:A10 :B10)" +
                         "ObjectUnionOf(:A11 :B11)" +
                         "ObjectUnionOf(:A12 :B12)" +
                         "ObjectUnionOf(:A13 :B13)" +
                         "ObjectUnionOf(:A14 :B14)" +
                         "ObjectUnionOf(:A15 :B15)" +
                         "ObjectUnionOf(:A16 :B16)" +
                         "ObjectUnionOf(:A17 :B17)" +
                         "ObjectUnionOf(:A18 :B18)" +
                         "ObjectUnionOf(:A19 :B19)" +
                         "ObjectUnionOf(:A20 :B20)" +
                         "ObjectUnionOf(:A21 :B21)" +
                         "ObjectUnionOf(:A22 :B22)" +
                         "ObjectUnionOf(:A23 :B23)" +
                         "ObjectUnionOf(:A24 :B24)" +
                         "ObjectUnionOf(:A25 :B25)" +
                         "ObjectUnionOf(:A26 :B26)" +
                         "ObjectUnionOf(:A27 :B27)" +
                         "ObjectUnionOf(:A28 :B28)" +
                         "ObjectUnionOf(:A29 :B29)" +
                         "ObjectUnionOf(:A30 :B30)" +
                         "ObjectUnionOf(:A31 :B31)" +
         		"))");
         buffer.append("SubClassOf(:C2 ObjectIntersectionOf(ObjectUnionOf(:A :B) ObjectUnionOf(:A ObjectComplementOf(:B))))");
         buffer.append("SubClassOf(:C3 ObjectIntersectionOf(ObjectUnionOf(ObjectComplementOf(:A) :B) ObjectUnionOf(ObjectComplementOf(:A) ObjectComplementOf(:B))))");
         buffer.append("SubClassOf(:C4 ObjectSomeValuesFrom(:R :C2))");
         buffer.append("SubClassOf(:C5 ObjectAllValuesFrom(:R :C3))");
         buffer.append("SubClassOf(:test ObjectIntersectionOf(:C1 :C4 :C5))");
         loadReasonerWithAxioms(buffer.toString());
         assertSatisfiable("file:/c/test.owl#test",false);
     }
     
     public void testIanBackjumping2() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:C2 ObjectIntersectionOf(ObjectUnionOf(:A :B) ObjectUnionOf(:A ObjectComplementOf(:B))))");
         buffer.append("SubClassOf(:C3 ObjectIntersectionOf(ObjectUnionOf(ObjectComplementOf(:A) :B) ObjectUnionOf(ObjectComplementOf(:A) ObjectComplementOf(:B))))");
         buffer.append("SubClassOf(:C4 ObjectSomeValuesFrom(:R ObjectIntersectionOf(:C2 :C8)))");
         buffer.append("SubClassOf(:C5 ObjectAllValuesFrom(:R ObjectIntersectionOf(:C3 :C9)))");
         buffer.append("SubClassOf(:C6 ObjectSomeValuesFrom(:R ObjectIntersectionOf(:C2 :C10)))");
         buffer.append("SubClassOf(:C7 ObjectAllValuesFrom(:R ObjectIntersectionOf(:C3 :C11)))");
         buffer.append("SubClassOf(:test ObjectIntersectionOf(" +
                         "ObjectUnionOf(:A0 :B0) " +
                         "ObjectUnionOf(:A1 :B1)" +
                         "ObjectUnionOf(:A2 :B2)" +
                         "ObjectUnionOf(:A3 :B3)" +
                         "ObjectUnionOf(:A4 :B4)" +
                         "ObjectUnionOf(:A5 :B5)" +
                         "ObjectUnionOf(:A6 :B6)" +
                         "ObjectUnionOf(:A7 :B7)" +
                         "ObjectUnionOf(:A8 :B8)" +
                         "ObjectUnionOf(:A9 :B9)" +
                         "ObjectUnionOf(:A10 :B10)" +
                         "ObjectUnionOf(:A11 :B11)" +
                         "ObjectUnionOf(:A12 :B12)" +
                         "ObjectUnionOf(:A13 :B13)" +
                         "ObjectUnionOf(:A14 :B14)" +
                         "ObjectUnionOf(:A15 :B15)" +
                         "ObjectUnionOf(:A16 :B16)" +
                         "ObjectUnionOf(:A17 :B17)" +
                         "ObjectUnionOf(:A18 :B18)" +
                         "ObjectUnionOf(:A19 :B19)" +
                         "ObjectUnionOf(:A20 :B20)" +
                         "ObjectUnionOf(:A21 :B21)" +
                         "ObjectUnionOf(:A22 :B22)" +
                         "ObjectUnionOf(:A23 :B23)" +
                         "ObjectUnionOf(:A24 :B24)" +
                         "ObjectUnionOf(:A25 :B25)" +
                         "ObjectUnionOf(:A26 :B26)" +
                         "ObjectUnionOf(:A27 :B27)" +
                         "ObjectUnionOf(:A28 :B28)" +
                         "ObjectUnionOf(:A29 :B29)" +
                         "ObjectUnionOf(:A30 :B30)" +
                         "ObjectUnionOf(:A31 :B31)" +
         		"))");
         loadReasonerWithAxioms(buffer.toString());
         assertSatisfiable("file:/c/test.owl#test",true);
     }
     public void testIanBackjumping3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:C2 ObjectIntersectionOf(ObjectUnionOf(:A :B) ObjectUnionOf(:A ObjectComplementOf(:B))))");
         buffer.append("SubClassOf(:C3 ObjectIntersectionOf(ObjectUnionOf(ObjectComplementOf(:A) :B) ObjectUnionOf(ObjectComplementOf(:A) ObjectComplementOf(:B))))");
         buffer.append("SubClassOf(:C4 ObjectSomeValuesFrom(:R ObjectIntersectionOf(:C2 :C8)))");
         buffer.append("SubClassOf(:C5 ObjectAllValuesFrom(:R ObjectIntersectionOf(:C3 :C9)))");
         buffer.append("SubClassOf(:C6 ObjectSomeValuesFrom(:R ObjectIntersectionOf(:C2 :C10)))");
         buffer.append("SubClassOf(:C7 ObjectAllValuesFrom(:R ObjectIntersectionOf(:C3 :C11)))");
         buffer.append("SubClassOf(:test ObjectIntersectionOf(" +
                        "ObjectUnionOf(:A0 :B0) " +
                        "ObjectUnionOf(:A1 :B1)" +
                        "ObjectUnionOf(:A2 :B2)" +
                        "ObjectUnionOf(:A3 :B3)" +
                        "ObjectUnionOf(:A4 :B4)" +
                        "ObjectUnionOf(:A5 :B5)" +
                        "ObjectUnionOf(:A6 :B6)" +
                        "ObjectUnionOf(:A7 :B7)" +
                        "ObjectUnionOf(:A8 :B8)" +
                        "ObjectUnionOf(:A9 :B9)" +
                        "ObjectUnionOf(:A10 :B10)" +
                        "ObjectUnionOf(:A11 :B11)" +
                        "ObjectUnionOf(:A12 :B12)" +
                        "ObjectUnionOf(:A13 :B13)" +
                        "ObjectUnionOf(:A14 :B14)" +
                        "ObjectUnionOf(:A15 :B15)" +
                        "ObjectUnionOf(:A16 :B16)" +
                        "ObjectUnionOf(:A17 :B17)" +
                        "ObjectUnionOf(:A18 :B18)" +
                        "ObjectUnionOf(:A19 :B19)" +
                        "ObjectUnionOf(:A20 :B20)" +
                        "ObjectUnionOf(:A21 :B21)" +
                        "ObjectUnionOf(:A22 :B22)" +
                        "ObjectUnionOf(:A23 :B23)" +
                        "ObjectUnionOf(:A24 :B24)" +
                        "ObjectUnionOf(:A25 :B25)" +
                        "ObjectUnionOf(:A26 :B26)" +
                        "ObjectUnionOf(:A27 :B27)" +
                        "ObjectUnionOf(:A28 :B28)" +
                        "ObjectUnionOf(:A29 :B29)" +
                        "ObjectUnionOf(:A30 :B30)" +
                        "ObjectUnionOf(:A31 :B31)" +
                        "ObjectUnionOf(:C4 :C6)" +
                        "ObjectUnionOf(:C5 :C7)" +
                        "))"); 
         loadReasonerWithAxioms(buffer.toString());
         assertSatisfiable("file:/c/test.owl#test",false);
     }
     
     public void testNominals1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("ClassAssertion(:A :a) ");
         buffer.append("ClassAssertion(:A :b) ");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:R :A)) ");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:S ObjectOneOf(:n))) ");
         loadReasonerWithAxioms(buffer.toString());
         assertABoxSatisfiable(true);
     }
     
     public void testNominals2() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("ClassAssertion(:A :a) ");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:R :A)) ");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:S ObjectOneOf(:n))) ");
         buffer.append("ClassAssertion(:B :b) ");
         buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:R :B)) ");
         buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:S ObjectOneOf(:n))) ");
         buffer.append("DisjointClasses(:A :B) ");
         buffer.append("ClassAssertion(ObjectMaxCardinality(5 ObjectInverseOf(:S)) :n) ");
         loadReasonerWithAxioms(buffer.toString());
         assertABoxSatisfiable(true);
     }
     
     public void testNominals3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:R :A)) ");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:S ObjectOneOf(:n))) ");
         buffer.append("ClassAssertion(ObjectSomeValuesFrom(:R :A) :a) ");
         buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:R :B)) ");
         buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:S ObjectOneOf(:n))) ");
         buffer.append("ClassAssertion(ObjectSomeValuesFrom(:R :B) :b) ");
         buffer.append("ClassAssertion(ObjectMaxCardinality(1 ObjectInverseOf(:S)) :n)");
         loadOntologyWithAxioms(buffer.toString());

         OWLClassExpression A = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#A"));
         OWLClassExpression B = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#B"));
         OWLObjectProperty S = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#S"));
         OWLObjectPropertyExpression invS = m_dataFactory.getOWLObjectInverseOf(S);
         OWLObjectProperty R = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#R"));
         OWLNamedIndividual n = m_dataFactory.getOWLNamedIndividual(IRI.create("file:/c/test.owl#n"));

         createReasoner();
                           
         // [some [inv S] [and A B [some R [and A B]]]]
         OWLClassExpression desc =
             m_dataFactory.getOWLObjectSomeValuesFrom(invS, 
                 m_dataFactory.getOWLObjectIntersectionOf(
                     A,
                     B, 
                     m_dataFactory.getOWLObjectSomeValuesFrom(R,
                         m_dataFactory.getOWLObjectIntersectionOf(
                             A,
                             B
                         )
                     )
                 )
             );
         
         assertInstanceOf(desc,n,true);
     }
     
     public void testNominals4() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(:A :B) ");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:R :A))");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:S ObjectOneOf(:n)))");
         buffer.append("ClassAssertion(ObjectSomeValuesFrom(:R :A) :a) ");
         buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:R :B)) ");
         buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:S ObjectOneOf(:n))) ");
         buffer.append("ClassAssertion(ObjectSomeValuesFrom(:R :B) :b) ");
         loadOntologyWithAxioms(buffer.toString());
     
         
         OWLClassExpression A = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#A"));
         OWLClassExpression B = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#B"));
         OWLObjectProperty S = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#S"));
         OWLObjectPropertyExpression invS = m_dataFactory.getOWLObjectInverseOf(S);
         OWLObjectProperty R = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#R"));
         OWLNamedIndividual n = m_dataFactory.getOWLNamedIndividual(IRI.create("file:/c/test.owl#n"));

         // OWL API has an error: axiom
         //     ClassAssertion(n ObjectMaxCardinality(2 InverseObjectProperty(S)))
         // gets loaded as
         //     ClassAssertion(n ObjectMaxCardinality(2 S))
         // Therefore, we add this axiom manually.         
         OWLObjectMaxCardinality atMostTwoInvS = m_dataFactory.getOWLObjectMaxCardinality(2, S.getInverseProperty());
         OWLClassAssertionAxiom nOfAtMostTwoInvS = m_dataFactory.getOWLClassAssertionAxiom(atMostTwoInvS,n);
         m_ontologyManager.addAxiom(m_ontology, nOfAtMostTwoInvS);
         
         createReasoner();
         
         // [some [inv S] [and A [some R A]]]
         OWLClassExpression desc =
             m_dataFactory.getOWLObjectSomeValuesFrom(invS, 
                 m_dataFactory.getOWLObjectIntersectionOf(
                     A, 
                     m_dataFactory.getOWLObjectSomeValuesFrom(R, A)
                 )
              );
         assertInstanceOf(desc, n, true);

         // [some [inv S] [and B [some R B]]]
         desc =
             m_dataFactory.getOWLObjectSomeValuesFrom(invS, 
                 m_dataFactory.getOWLObjectIntersectionOf(
                     B, 
                     m_dataFactory.getOWLObjectSomeValuesFrom(R, B)
                 )
             );
         assertInstanceOf(desc, n, true);
     }
     
     public void testNominals5() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(:A :B) ");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:R :A))");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:S ObjectOneOf(:n)))");
         buffer.append("ClassAssertion(ObjectSomeValuesFrom(:R :A) :a) ");
         buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:R :B)) ");
         buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:S ObjectOneOf(:n))) ");
         buffer.append("ClassAssertion(ObjectSomeValuesFrom(:R :B) :b) ");
         loadOntologyWithAxioms(buffer.toString());
         
         
         OWLClassExpression A = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#A"));
         OWLClassExpression B = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#B"));
         OWLObjectProperty S = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#S"));
         OWLObjectPropertyExpression invS = m_dataFactory.getOWLObjectInverseOf(S);
         OWLNamedIndividual n = m_dataFactory.getOWLNamedIndividual(IRI.create("file:/c/test.owl#n"));

         // OWL API has an error: axiom
         //     ClassAssertion(n ObjectMaxCardinality(2 InverseObjectProperty(S)))
         // gets loaded as
         //     ClassAssertion(n ObjectMaxCardinality(2 S))
         // Therefore, we add this axiom manually.         
         OWLObjectMaxCardinality atMostTwoInvS = m_dataFactory.getOWLObjectMaxCardinality(2, S.getInverseProperty());
         OWLClassAssertionAxiom nOfAtMostTwoInvS = m_dataFactory.getOWLClassAssertionAxiom(atMostTwoInvS, n);
         m_ontologyManager.addAxiom(m_ontology, nOfAtMostTwoInvS);
         
         createReasoner();

         // [atLeast 2 [inv S] [or A B]]
         OWLClassExpression desc = m_dataFactory.getOWLObjectMinCardinality(2, invS, m_dataFactory.getOWLObjectUnionOf(A, B));
         assertInstanceOf(desc, n, true);
     }
     public void testNominals6() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(:A :B) ");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:R :A))");
         buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:S ObjectOneOf(:n)))");
         buffer.append("ClassAssertion(ObjectSomeValuesFrom(:R :A) :a) ");
         buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:R :B)) ");
         buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:S ObjectOneOf(:n))) ");
         buffer.append("ClassAssertion(ObjectSomeValuesFrom(:R :B) :b) ");
         loadOntologyWithAxioms(buffer.toString());
         
         
         OWLClassExpression A = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#A"));
         OWLObjectProperty S = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#S"));
         OWLObjectPropertyExpression invS = m_dataFactory.getOWLObjectInverseOf(S);
         OWLNamedIndividual n = m_dataFactory.getOWLNamedIndividual(IRI.create("file:/c/test.owl#n"));

         // OWL API has an error: axiom
         //     ClassAssertion(n ObjectMaxCardinality(2 InverseObjectProperty(S)))
         // gets loaded as
         //     ClassAssertion(n ObjectMaxCardinality(2 S))
         // Therefore, we add this axiom manually.         
         OWLObjectMaxCardinality atMostTwoInvS = m_dataFactory.getOWLObjectMaxCardinality(2, S.getInverseProperty());
         OWLClassAssertionAxiom nOfAtMostTwoInvS = m_dataFactory.getOWLClassAssertionAxiom(atMostTwoInvS, n);
         m_ontologyManager.addAxiom(m_ontology, nOfAtMostTwoInvS);
         
         createReasoner();

         // [atLeast 1 [inv S] [not A]]
         OWLClassExpression desc = m_dataFactory.getOWLObjectMinCardinality(1, invS, m_dataFactory.getOWLObjectComplementOf(A));
         assertInstanceOf(desc, n, true);

         // [atLeast 2 [inv S] [not A]]
         desc = m_dataFactory.getOWLObjectMinCardinality(2, invS, m_dataFactory.getOWLObjectComplementOf(A));
         assertInstanceOf(desc, n, false);
     }
     
    public void testDependencyDisjunctionMergingBug() throws Exception {
        loadReasonerFromResource("res/dependency-disjuntion-merging-bug.xml");
        assertSubsumedBy(
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Anjou",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#FullBodiedWine",
                false);
    }
    
    public void testNovelNominals() throws Exception {
        
        String axioms = "ClassAssertion(:C :a)";
        loadReasonerWithAxioms(axioms);
        OWLIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create("file:/c/test.owl#a"));
        OWLClass c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#C"));
        OWLClassExpression desc = m_ontologyManager.getOWLDataFactory().getOWLObjectIntersectionOf(
            m_dataFactory.getOWLObjectOneOf(a),
            m_dataFactory.getOWLObjectComplementOf(c));

        assertFalse(m_reasoner.isSatisfiable(desc));
    }

    public void testKeys1() throws Exception {
        String axioms = "DataPropertyAssertion(:hasSSN :Peter \"123-45-6789\") " +
                        "ClassAssertion(:Person :Peter) " +
                        "DataPropertyAssertion(:hasSSN :Peter_Griffin \"123-45-6789\") " +
                        "ClassAssertion(:Person :Peter_Griffin) " +
                        "DifferentIndividuals(:Peter :Peter_Griffin)" + 
                        "HasKey(:Person () (:hasSSN))";
        loadOntologyWithAxioms(axioms);
        createReasoner(getConfiguration(),null);
        assertABoxSatisfiable(false);
    }

    public void testKeys2() throws Exception {
        String axioms = "DataPropertyAssertion(:hasSSN :Peter \"123-45-6789\") " +
                "ClassAssertion(:Person :Peter) " +
                "ClassAssertion(ObjectSomeValuesFrom(:marriedTo ObjectIntersectionOf(:Man DataHasValue(:hasSSN \"123-45-6789\"^^xsd:string))) :Lois) " +
                "SubClassOf(:Man ObjectComplementOf(:Person))" + 
                "HasKey(:Person () (:hasSSN))";
        loadOntologyWithAxioms(axioms);
        createReasoner(getConfiguration(),null);
        assertABoxSatisfiable(true);
    }

    public void testNominalMerging() throws Exception {
        // This is the example from Section 3.2.5 from the SHOIQ+ paper.
        StringBuffer buffer = new StringBuffer();
        buffer.append("ObjectPropertyAssertion(:S :a :a)");
        buffer.append("ClassAssertion(ObjectSomeValuesFrom(:R :B) :a)");
        buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:R :C))");
        buffer.append("SubClassOf(:C ObjectSomeValuesFrom(:S :D))");
        buffer.append("SubClassOf(:D ObjectOneOf(:a))");
        buffer.append("InverseFunctionalObjectProperty(:S)");
        loadReasonerWithAxioms(buffer.toString());
        assertABoxSatisfiable(true);
    }
    
    public void testNIRuleBlockingWithUnraveling() throws Exception {
        // This is the example from Section 3.2.6 of the SHOIQ+ paper.
        StringBuffer buffer = new StringBuffer();
        buffer.append("ClassAssertion(:A :a)");
        buffer.append("ClassAssertion(ObjectSomeValuesFrom(:R :B) :a)");
        buffer.append("SubClassOf(ObjectSomeValuesFrom(:R :A) owl:Nothing)");
        buffer.append("SubClassOf(:B ObjectSomeValuesFrom(:R :B))");
        buffer.append("SubClassOf(:B ObjectHasValue(:S :a))");
        buffer.append("InverseFunctionalObjectProperty(:R)");
        buffer.append("InverseObjectProperties(:S :Si)");
        buffer.append("SubClassOf( owl:Thing ObjectMaxCardinality(3 :Si owl:Thing))");
        loadReasonerWithAxioms(buffer.toString());
        assertABoxSatisfiable(false);
    }
    
    public void testPunning() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Declaration(Class(:Person))");
        buffer.append("ClassAssertion(:Service :s1)");
        buffer.append("ObjectPropertyAssertion(:hasInput :s1 :Person)");
        loadReasonerWithAxioms(buffer.toString());
        assertABoxSatisfiable(true);
    } 
    
    public void testPunning2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Declaration( Class( :Deprecated_Properties ) )");
        buffer.append("Declaration( ObjectProperty( :is_located_in ) )");
        buffer.append("ClassAssertion( :Deprecated_Properties :is_located_in )");
        loadReasonerWithAxioms(buffer.toString());
        assertABoxSatisfiable(true);
    } 
    
    public void testPunning3() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Declaration( Class( :Person ) ) Declaration( Class( :Company ) )");
        buffer.append("SubClassOf( :PersonCompany :Association )");
        buffer.append("ObjectPropertyDomain( :PersonCompany :Person ) ObjectPropertyRange( :PersonCompany :Company )");
        loadReasonerWithAxioms(buffer.toString());
        assertABoxSatisfiable(true);
    }

    public void testInverses2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("InverseObjectProperties( :hasPart :partOf ) ObjectPropertyAssertion(:hasPart :a :b) NegativeObjectPropertyAssertion(:partOf :b :a)");
        loadReasonerWithAxioms(buffer.toString());
        assertABoxSatisfiable(false);
    }
    
    public void testMissingCBug() throws Exception {
        String axioms = "EquivalentClasses(:C ObjectMinCardinality(0 :p owl:Nothing))";
        loadReasonerWithAxioms(axioms);
        m_reasoner.classify();
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter output=new PrintWriter(buffer);
        m_reasoner.printHierarchies(output,true,true,true);
        output.flush();
        m_reasoner.classify();
        CharArrayWriter buffer2=new CharArrayWriter();
        PrintWriter output2=new PrintWriter(buffer2);
        m_reasoner.printHierarchies(output2,true,true,true);
        output2.flush();
        assertTrue(buffer.toString().equals(buffer2.toString()));
    }
    
    public void testInverses() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("EquivalentObjectProperties( :hasPart ObjectInverseOf( :partOf ) ) ObjectPropertyAssertion(:hasPart :a :b) NegativeObjectPropertyAssertion(:partOf :b :a)");
        loadReasonerWithAxioms(buffer.toString());
        assertABoxSatisfiable(false);
    }
    public void testAnonymousIndiviuals2() throws Exception {
        String axioms = "ObjectPropertyAssertion( :city _:a1 :Paris )";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testAnonymousIndiviuals3() throws Exception {
        String axioms = "ObjectPropertyAssertion( a:livesAt a:Peter _:a1 )"
            + "ObjectPropertyAssertion( a:city _:a1 a:Quahog )"
            + "ObjectPropertyAssertion( a:state _:a1 a:RI )";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    public void testSatisfiabilityWithRIAs1() throws Exception {
        String axioms = "ObjectPropertyAssertion( :R1 :a :b )" +
						"ObjectPropertyAssertion( :R1 :b :c )" +
						"ObjectPropertyAssertion( :R2 :c :d )" +
						"ObjectPropertyAssertion( :R2 :d :e )" +
						"ObjectPropertyAssertion( :R3 :e :f )" +
						"SubObjectPropertyOf(:R1 :dumm) " +
						"SubObjectPropertyOf(:R2 :dumm) " +
						"SubObjectPropertyOf(:dumm :R) " +
        				"ClassAssertion(ObjectAllValuesFrom(:R :C) :f) " +
        				"ClassAssertion(ObjectComplementOf(:C) :a) " +
        				"TransitiveObjectProperty(:R1) " +
        				"TransitiveObjectProperty(:R2) " +
        				"TransitiveObjectProperty(:dumm) " +
        				"SubObjectPropertyOf(ObjectPropertyChain(:dumm :R3) ObjectInverseOf(:R)) ";
                		
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    public void testSatisfiabilityWithRIAs2() throws Exception {
        String axioms = "ObjectPropertyAssertion( :R1 :a :b )" +
						"ObjectPropertyAssertion( :S3- :b :c )" +
						"ObjectPropertyAssertion( :S2- :c :d )" +
						"ObjectPropertyAssertion( :S1- :d :e )" +
        				"ObjectPropertyAssertion( :R2 :e :f )" +
        				"InverseObjectProperties(:S :S-) " +
        				"InverseObjectProperties(:S1 :S1-) " +
        				"InverseObjectProperties(:S2 :S2-) " +
        				"InverseObjectProperties(:S3 :S3-) " +
        				"ClassAssertion(ObjectComplementOf(:C) :a) " +
        				"ClassAssertion(ObjectAllValuesFrom(:R :C) :f) " +
        				"SubObjectPropertyOf(ObjectPropertyChain(:S1 :S2 :S3) :S) " +
        				"SubObjectPropertyOf(ObjectPropertyChain(:R1 :S- :R2) ObjectInverseOf(:R)) ";

        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    public void testSatisfiabilityWithRIAs3() throws Exception {
        String axioms = "ObjectPropertyAssertion( :R1 :a :b )" +
						"ObjectPropertyAssertion( :R2 :b :c )" +
        				"InverseObjectProperties(:R :R-) " +
        				"ClassAssertion(ObjectComplementOf(:C) :a) " +
        				"ClassAssertion(ObjectAllValuesFrom(:R :C) :c) " +
        				"SubObjectPropertyOf(ObjectPropertyChain(:R1 :R2) :R-) " +
        				"SubObjectPropertyOf(ObjectPropertyChain(:R3 :R4) :R)";

        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
	 public void testSatisfiabilityWithRIAs4() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R2 :R3) :R) " +
		 				 "EquivalentObjectProperties( :R :R2 )" +
		 				 "EquivalentObjectProperties( :R :R3 )" +
		 				 "ObjectPropertyAssertion( :R2 :a :b )" +
		 				 "ObjectPropertyAssertion( :R3 :b :c )" +
		 				 "ClassAssertion(ObjectComplementOf(:C) :c) " +
		 				 "ClassAssertion(ObjectAllValuesFrom(:R :C) :a) ";
		 loadReasonerWithAxioms(axioms);
	     assertABoxSatisfiable(false);
	 }
}
