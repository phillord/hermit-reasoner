package org.semanticweb.HermiT.reasoner;

import java.net.URI;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;

public class ComplexConceptTest extends AbstractReasonerTest {

    public ComplexConceptTest(String name) {
        super(name);
    }
    
    public void testConceptWithDatatypes() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("SubClassOf(A ObjectSomeValuesFrom(f B))");
        buffer.append("SubClassOf(A ObjectSomeValuesFrom(f C))");
        buffer.append("SubClassOf(B DataSomeValuesFrom(dp DataOneOf( \"abc\"^^xsd:string \"def\"^^xsd:string )))");
        buffer.append("SubClassOf(C DataHasValue(dp \"abc@\"^^rdf:text))");
        buffer.append("FunctionalObjectProperty(f)");
        buffer.append("ClassAssertion(a A)");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLIndividual a = df.getOWLIndividual(URI.create("file:/c/test.owl#a"));
        OWLObjectProperty f = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f"));
        OWLDataProperty dp = df.getOWLDataProperty(URI.create("file:/c/test.owl#dp"));
        
        OWLDescription desc = df.getOWLObjectSomeRestriction(f, df.getOWLDataSomeRestriction(dp, df.getOWLDataOneOf(df.getOWLUntypedConstant("abc"))));
        assertInstanceOf(desc, a, true);
   }
 
    public void testConceptWithDatatypes2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("SubClassOf(A DataAllValuesFrom(dp DataComplementOf(rdfs:Literal)))");
        buffer.append("ClassAssertion(a A)");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLIndividual a = df.getOWLIndividual(URI.create("file:/c/test.owl#a"));
        OWLDataProperty dp = df.getOWLDataProperty(URI.create("file:/c/test.owl#dp"));
        
        OWLDescription desc = df.getOWLDataSomeRestriction(dp, df.getTopDataType());
        assertInstanceOf(desc, a, false);
   }
    
    public void testConceptWithNominals() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ClassAssertion(a ObjectSomeValuesFrom(f1 ObjectSomeValuesFrom(f2 ObjectOneOf(o))))");
        buffer.append("ClassAssertion(b ObjectSomeValuesFrom(f1 ObjectSomeValuesFrom(f2 ObjectOneOf(o))))");
        buffer.append("InverseFunctionalObjectProperty(f1)");
        buffer.append("InverseFunctionalObjectProperty(f2)");
        buffer.append("ClassAssertion(a ObjectAllValuesFrom(f1 A))");
        buffer.append("ClassAssertion(b ObjectAllValuesFrom(f1 B))");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLIndividual o = df.getOWLIndividual(URI.create("file:/c/test.owl#o"));
        OWLObjectProperty f2 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f2"));
        OWLObjectPropertyExpression invf2 = df.getOWLObjectPropertyInverse(f2);
        OWLClass A = df.getOWLClass(URI.create("file:/c/test.owl#A"));
        OWLClass B = df.getOWLClass(URI.create("file:/c/test.owl#B"));
        
        OWLDescription desc = df.getOWLObjectAllRestriction(invf2, df.getOWLObjectIntersectionOf(A, B));
        assertInstanceOf(desc, o, true);
    }
    
    public void testConceptWithNominals2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ClassAssertion(a ObjectSomeValuesFrom(f1 ObjectSomeValuesFrom(f2 ObjectOneOf(o))))");
        buffer.append("ClassAssertion(b ObjectSomeValuesFrom(f1 ObjectSomeValuesFrom(f2 ObjectOneOf(o))))");
        buffer.append("InverseFunctionalObjectProperty(f1)");
        buffer.append("InverseFunctionalObjectProperty(f2)");
        buffer.append("ClassAssertion(a ObjectAllValuesFrom(f1 A))");
        buffer.append("ClassAssertion(b ObjectAllValuesFrom(f1 B))");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLIndividual a = df.getOWLIndividual(URI.create("file:/c/test.owl#a"));
        OWLIndividual b = df.getOWLIndividual(URI.create("file:/c/test.owl#b"));
        
        OWLDescription desc = df.getOWLObjectIntersectionOf(df.getOWLObjectOneOf(a), df.getOWLObjectOneOf(b));
        assertInstanceOf(desc, a, true);
        assertInstanceOf(desc, b, true);
    }
    
    public void testConceptWithNominals3() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DisjointClasses(A B)");
        buffer.append("ClassAssertion(a ObjectSomeValuesFrom(f1 ObjectSomeValuesFrom(f2 ObjectOneOf(o))))");
        buffer.append("ClassAssertion(b ObjectSomeValuesFrom(f1 ObjectSomeValuesFrom(f2 ObjectOneOf(o))))");
        buffer.append("InverseFunctionalObjectProperty(f1)");
        buffer.append("InverseFunctionalObjectProperty(f2)");
        buffer.append("ClassAssertion(a ObjectAllValuesFrom(f1 A))");
        buffer.append("ClassAssertion(b ObjectAllValuesFrom(f1 B))");
        loadReasonerWithAxioms(buffer.toString());
        
        assertABoxSatisfiable(false);
    }
   
    
    public void testConceptWithNominals4() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DisjointClasses(ObjectOneOf(a) ObjectOneOf(b))");
        buffer.append("ClassAssertion(a ObjectSomeValuesFrom(f1 ObjectSomeValuesFrom(f2 ObjectOneOf(o))))");
        buffer.append("ClassAssertion(b ObjectSomeValuesFrom(f1 ObjectSomeValuesFrom(f2 ObjectOneOf(o))))");
        buffer.append("InverseFunctionalObjectProperty(f1)");
        buffer.append("InverseFunctionalObjectProperty(f2)");
        loadReasonerWithAxioms(buffer.toString());
        
        assertABoxSatisfiable(false);
    }
    
    public void testConceptWithNominals5() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ClassAssertion(a ObjectSomeValuesFrom(f B))");
        buffer.append("ObjectPropertyAssertion(f a b)");
        buffer.append("FunctionalObjectProperty(f)");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLIndividual b = df.getOWLIndividual(URI.create("file:/c/test.owl#b"));
        OWLClass B = df.getOWLClass(URI.create("file:/c/test.owl#B"));
        
        assertSubsumedBy(df.getOWLObjectOneOf(b), B, true);
    }
    
    public void testJustifications() throws Exception {
        // test for Matthew's justifications that HermiT originally didn't answer correctly
        StringBuffer buffer = new StringBuffer();
        buffer.append("ClassAssertion(Matt Person)");
        buffer.append("ClassAssertion(Gemma Person)");
        buffer.append("ObjectPropertyAssertion(hasSibling Matt Gemma)");
        buffer.append("SubClassOf(ObjectIntersectionOf(Person ObjectSomeValuesFrom(hasSibling Person)) Sibling)");
        buffer.append("SubClassOf(Sibling ObjectIntersectionOf(Person ObjectSomeValuesFrom(hasSibling Person)))");
        loadReasonerWithAxioms(buffer.toString());
        
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLIndividual matt = df.getOWLIndividual(URI.create("file:/c/test.owl#Matt"));
        OWLClass sibling = df.getOWLClass(URI.create("file:/c/test.owl#Sibling"));
        OWLDescription desc = df.getOWLObjectIntersectionOf(
                df.getOWLObjectOneOf(matt),
                df.getOWLObjectComplementOf(sibling));
        
        assertSatisfiable(desc, false);
    }
    
}
