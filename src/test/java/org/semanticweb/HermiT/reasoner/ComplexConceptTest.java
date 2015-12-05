package org.semanticweb.HermiT.reasoner;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class ComplexConceptTest extends AbstractReasonerTest {

    public ComplexConceptTest(String name) {
        super(name);
    }
    
    public void testConceptWithDatatypes() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Declaration(NamedIndividual(:a))");
        buffer.append("Declaration(Class(:A))");
        buffer.append("Declaration(Class(:B))");
        buffer.append("Declaration(Class(:C))");
        buffer.append("Declaration(ObjectProperty(:f))");
        buffer.append("Declaration(DataProperty(:dp))");
        
        buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:f :B))");
        buffer.append("SubClassOf(:A ObjectSomeValuesFrom(:f :C))");
        buffer.append("SubClassOf(:B DataSomeValuesFrom(:dp DataOneOf( \"abc\"^^xsd:string \"def\"^^xsd:string )))");
        buffer.append("SubClassOf(:C DataHasValue(:dp \"abc@\"^^rdf:PlainLiteral))");
        buffer.append("FunctionalObjectProperty(:f)");
        buffer.append("ClassAssertion(:A :a)");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLNamedIndividual a = df.getOWLNamedIndividual(IRI.create("file:/c/test.owl#a"));
        OWLObjectProperty f = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#f"));
        OWLDataProperty dp = df.getOWLDataProperty(IRI.create("file:/c/test.owl#dp"));
        
        OWLClassExpression desc = df.getOWLObjectSomeValuesFrom(f, df.getOWLDataSomeValuesFrom(dp, df.getOWLDataOneOf(PL("abc",""))));
        assertInstanceOf(desc, a, true);
   }
 
    public void testConceptWithDatatypes2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Declaration(NamedIndividual(:a))");
        buffer.append("Declaration(Class(:A))");
        buffer.append("Declaration(DataProperty(:dp))");
        
        buffer.append("SubClassOf(:A DataAllValuesFrom(:dp DataComplementOf(rdfs:Literal)))");
        buffer.append("ClassAssertion(:A :a)");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLNamedIndividual a = df.getOWLNamedIndividual(IRI.create("file:/c/test.owl#a"));
        OWLDataProperty dp = df.getOWLDataProperty(IRI.create("file:/c/test.owl#dp"));
        
        OWLClassExpression desc = df.getOWLDataSomeValuesFrom(dp, df.getTopDatatype());
        assertInstanceOf(desc, a, false);
   }
    
    public void testConceptWithNominals() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Declaration(NamedIndividual(:a))");
        buffer.append("Declaration(NamedIndividual(:b))");
        buffer.append("Declaration(NamedIndividual(:o))");
        buffer.append("Declaration(Class(:A))");
        buffer.append("Declaration(Class(:B))");
        buffer.append("Declaration(ObjectProperty(:f1))");
        buffer.append("Declaration(ObjectProperty(:f2))");
        buffer.append("Declaration(DataProperty(:dp))");
        
        buffer.append("ClassAssertion(ObjectSomeValuesFrom(:f1 ObjectSomeValuesFrom(:f2 ObjectOneOf(:o))) :a)");
        buffer.append("ClassAssertion(ObjectSomeValuesFrom(:f1 ObjectSomeValuesFrom(:f2 ObjectOneOf(:o))) :b)");
        buffer.append("InverseFunctionalObjectProperty(:f1)");
        buffer.append("InverseFunctionalObjectProperty(:f2)");
        buffer.append("ClassAssertion(ObjectAllValuesFrom(:f1 :A) :a)");
        buffer.append("ClassAssertion(ObjectAllValuesFrom(:f1 :B) :b)");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLNamedIndividual o = df.getOWLNamedIndividual(IRI.create("file:/c/test.owl#o"));
        OWLObjectProperty f2 = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#f2"));
        OWLObjectPropertyExpression invf2 = df.getOWLObjectInverseOf(f2);
        OWLClass A = df.getOWLClass(IRI.create("file:/c/test.owl#A"));
        OWLClass B = df.getOWLClass(IRI.create("file:/c/test.owl#B"));
        
        OWLClassExpression desc = df.getOWLObjectAllValuesFrom(invf2, df.getOWLObjectIntersectionOf(A, B));
        assertInstanceOf(desc, o, true);
    }
    
    public void testConceptWithNominals2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Declaration(NamedIndividual(:a))");
        buffer.append("Declaration(NamedIndividual(:b))");
        buffer.append("Declaration(NamedIndividual(:o))");
        buffer.append("Declaration(Class(:A))");
        buffer.append("Declaration(Class(:B))");
        buffer.append("Declaration(ObjectProperty(:f1))");
        buffer.append("Declaration(ObjectProperty(:f2))");
        
        buffer.append("ClassAssertion(ObjectSomeValuesFrom(:f1 ObjectSomeValuesFrom(:f2 ObjectOneOf(:o))) :a)");
        buffer.append("ClassAssertion(ObjectSomeValuesFrom(:f1 ObjectSomeValuesFrom(:f2 ObjectOneOf(:o))) :b)");
        buffer.append("InverseFunctionalObjectProperty(:f1)");
        buffer.append("InverseFunctionalObjectProperty(:f2)");
        buffer.append("ClassAssertion(ObjectAllValuesFrom(:f1 :A) :a)");
        buffer.append("ClassAssertion(ObjectAllValuesFrom(:f1 :B) :b)");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLNamedIndividual a = df.getOWLNamedIndividual(IRI.create("file:/c/test.owl#a"));
        OWLNamedIndividual b = df.getOWLNamedIndividual(IRI.create("file:/c/test.owl#b"));
        
        OWLClassExpression desc = df.getOWLObjectIntersectionOf(df.getOWLObjectOneOf(a), df.getOWLObjectOneOf(b));
        assertInstanceOf(desc, a, true);
        assertInstanceOf(desc, b, true);
    }
    
    public void testConceptWithNominals3() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Declaration(NamedIndividual(:a))");
        buffer.append("Declaration(NamedIndividual(:b))");
        buffer.append("Declaration(NamedIndividual(:o))");
        buffer.append("Declaration(Class(:A))");
        buffer.append("Declaration(Class(:B))");
        buffer.append("Declaration(ObjectProperty(:f1))");
        buffer.append("Declaration(ObjectProperty(:f2))");
        
        buffer.append("DisjointClasses(:A :B)");
        buffer.append("ClassAssertion(ObjectSomeValuesFrom(:f1 ObjectSomeValuesFrom(:f2 ObjectOneOf(:o))) :a)");
        buffer.append("ClassAssertion(ObjectSomeValuesFrom(:f1 ObjectSomeValuesFrom(:f2 ObjectOneOf(:o))) :b)");
        buffer.append("InverseFunctionalObjectProperty(:f1)");
        buffer.append("InverseFunctionalObjectProperty(:f2)");
        buffer.append("ClassAssertion(ObjectAllValuesFrom(:f1 :A) :a)");
        buffer.append("ClassAssertion(ObjectAllValuesFrom(:f1 :B) :b)");
        loadReasonerWithAxioms(buffer.toString());
        
        assertABoxSatisfiable(false);
    }
   
    
    public void testConceptWithNominals4() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Declaration(NamedIndividual(:a))");
        buffer.append("Declaration(NamedIndividual(:b))");
        buffer.append("Declaration(NamedIndividual(:o))");
        buffer.append("Declaration(ObjectProperty(:f1))");
        buffer.append("Declaration(ObjectProperty(:f2))");
        
        buffer.append("DisjointClasses(ObjectOneOf(:a) ObjectOneOf(:b))");
        buffer.append("ClassAssertion(ObjectSomeValuesFrom(:f1 ObjectSomeValuesFrom(:f2 ObjectOneOf(:o))) :a)");
        buffer.append("ClassAssertion(ObjectSomeValuesFrom(:f1 ObjectSomeValuesFrom(:f2 ObjectOneOf(:o))) :b)");
        buffer.append("InverseFunctionalObjectProperty(:f1)");
        buffer.append("InverseFunctionalObjectProperty(:f2)");
        loadReasonerWithAxioms(buffer.toString());
        
        assertABoxSatisfiable(false);
    }
    
    public void testConceptWithNominals5() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Declaration(NamedIndividual(:a))");
        buffer.append("Declaration(NamedIndividual(:b))");
        buffer.append("Declaration(Class(:B))");
        buffer.append("Declaration(ObjectProperty(:f))");
        buffer.append("Declaration(DataProperty(:dp))");
        
        buffer.append("ClassAssertion(ObjectSomeValuesFrom(:f :B) :a)");
        buffer.append("ObjectPropertyAssertion(:f :a :b)");
        buffer.append("FunctionalObjectProperty(:f)");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLIndividual b = df.getOWLNamedIndividual(IRI.create("file:/c/test.owl#b"));
        OWLClass B = df.getOWLClass(IRI.create("file:/c/test.owl#B"));
        
        assertSubsumedBy(df.getOWLObjectOneOf(b), B, true);
    }
    
    public void testJustifications() throws Exception {
        // test for Matthew's justifications that HermiT originally didn't answer correctly
        StringBuffer buffer = new StringBuffer();
        buffer.append("Declaration(NamedIndividual(:Matt))");
        buffer.append("Declaration(NamedIndividual(:Gemma))");
        buffer.append("Declaration(Class(:Person))");
        buffer.append("Declaration(Class(:Sibling))");
        buffer.append("Declaration(ObjectProperty(:hasSibling))");
        buffer.append("Declaration(ObjectProperty(:f2))");
        buffer.append("Declaration(DataProperty(:dp))");
        
        buffer.append("ClassAssertion(:Person :Matt)");
        buffer.append("ClassAssertion(:Person :Gemma)");
        buffer.append("ObjectPropertyAssertion(:hasSibling :Matt :Gemma)");
        buffer.append("SubClassOf(ObjectIntersectionOf(:Person ObjectSomeValuesFrom(:hasSibling :Person)) :Sibling)");
        buffer.append("SubClassOf(:Sibling ObjectIntersectionOf(:Person ObjectSomeValuesFrom(:hasSibling :Person)))");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLIndividual matt = df.getOWLNamedIndividual(IRI.create("file:/c/test.owl#Matt"));
        OWLClass sibling = df.getOWLClass(IRI.create("file:/c/test.owl#Sibling"));
        OWLClassExpression desc = df.getOWLObjectIntersectionOf(
                df.getOWLObjectOneOf(matt),
                df.getOWLObjectComplementOf(sibling));
        
        assertSatisfiable(desc, false);
    }
    
}
