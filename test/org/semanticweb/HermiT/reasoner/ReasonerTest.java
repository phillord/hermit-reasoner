package org.semanticweb.HermiT.reasoner;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.owlapi.structural.OWLHasKeyDummy;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;

public class ReasonerTest extends AbstractReasonerTest {

    public ReasonerTest(String name) {
        super(name);
    }
    
    //    keys are not yet supported by the OWL API, but should pass the following tests once implemented

    public void testKeys() throws Exception {
        String axioms = "DataPropertyAssertion(hasSSN Peter \"123-45-6789\") " +
                        "ClassAssertion(Peter Person) " +
                        "DataPropertyAssertion(hasSSN Peter_Griffin \"123-45-6789\") " +
                        "ClassAssertion(Peter_Griffin Person) " +
                        "DifferentIndividuals(Peter Peter_Griffin)";
        //HasKey(Person hasSSN)
        OWLHasKeyDummy key = new OWLHasKeyDummy();
        OWLDataFactory f = OWLManager.createOWLOntologyManager().getOWLDataFactory(); 
        OWLClass person = f.getOWLClass(new URI("file:/c/test.owl#Person"));
        key.setClassExpression(person);
        
        Set<OWLDataPropertyExpression> dprops = new HashSet<OWLDataPropertyExpression>();
        dprops.add(f.getOWLDataProperty(new URI("file:/c/test.owl#hasSSN")));
        key.setDataProperties(dprops);
        
        key.setClassExpression(person);
        key.setDataProperties(dprops);
        Set<OWLHasKeyDummy> keys = new HashSet<OWLHasKeyDummy>();
        keys.add(key);
        loadOntologyWithAxiomsAndKeys(axioms, null, keys);
        assertABoxSatisfiable(false);
    }

    public void testKeys2() throws Exception {
        String axioms = "DataPropertyAssertion(hasSSN Peter \"123-45-6789\") " +
                "ClassAssertion(Peter Person) " +
                "ClassAssertion(Lois ObjectSomeValuesFrom(marriedTo " +
                "ObjectIntersectionOf(Man " +
                "DataHasValue(hasSSN \"123-45-6789\"^^xsd:string)))) " +
                "SubClassOf(Man ObjectComplementOf(Person))";
        //HasKey(Person hasSSN)
        OWLHasKeyDummy key = new OWLHasKeyDummy();
        OWLDataFactory f = OWLManager.createOWLOntologyManager().getOWLDataFactory(); 
        OWLClass person = f.getOWLClass(new URI("file:/c/test.owl#Person"));
        key.setClassExpression(person);
        Set<OWLDataPropertyExpression> dprops = new HashSet<OWLDataPropertyExpression>();
        dprops.add(f.getOWLDataProperty(new URI("file:/c/test.owl#hasSSN")));
        key.setDataProperties(dprops);
        key.setClassExpression(person);
        key.setDataProperties(dprops);
        Set<OWLHasKeyDummy> keys = new HashSet<OWLHasKeyDummy>();
        keys.add(key);
        loadOntologyWithAxiomsAndKeys(axioms, null, keys);
        assertABoxSatisfiable(true);
    }
    
    public void testReflexivity() throws Exception {
        String axioms = "ReflexiveObjectProperty(r) "
                + "ClassAssertion(a ObjectAllValuesFrom(r " + "owl:Nothing)) "
                + "ClassAssertion(a owl:Thing)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testNegProperties() throws Exception {
        String axioms = "ObjectPropertyAssertion(r a b) "
                + "ObjectPropertyAssertion(r b c) "
                + "TransitiveObjectProperty(r) "
                + "NegativeObjectPropertyAssertion(r a c)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testIrreflexivity() throws Exception {
        String axioms = "IrreflexiveObjectProperty(r) "
                + "ObjectPropertyAssertion(r a a)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testRoleDisjointness() throws Exception {
        String axioms = "DisjointObjectProperties(r s t) "
                + "ObjectPropertyAssertion(r a b) "
                + "ObjectPropertyAssertion(s a b)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
        axioms = "DisjointObjectProperties(r s t) "
                + "ObjectPropertyAssertion(r a b) "
                + "ObjectPropertyAssertion(t a b)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testRoleDisjointness2() throws Exception {
        String axioms = "DisjointObjectProperties(r s) "
                + "ClassAssertion(a ObjectSomeValuesFrom(r owl:Thing)) "
                + "ClassAssertion(a ObjectSomeValuesFrom(s owl:Thing)) "
                + "ClassAssertion(a C) "
                + "SubClassOf(C ObjectMaxCardinality(1 f)) "
                + "SubObjectPropertyOf(r f) " + "SubObjectPropertyOf(s f)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testExistsSelf1() throws Exception {
        String axioms = "ClassAssertion(a ObjectAllValuesFrom(r "
                + "owl:Nothing)) " + "ClassAssertion(a ObjectExistsSelf(r))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testExistsSelf2() throws Exception {
        String axioms = "SubClassOf(B1 ObjectSomeValuesFrom(r C2)) "
                + "SubClassOf(C2 ObjectSomeValuesFrom(r B2)) "
                + "SubClassOf(B2 ObjectSomeValuesFrom(r C1)) "
                + "SubClassOf(C1 ObjectSomeValuesFrom(r B1)) "
                + "ClassAssertion(a C1) "
                + "ClassAssertion(a ObjectAllValuesFrom(r "
                + "ObjectExistsSelf(r)))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(true);
    }

    public void testAsymmetry() throws Exception {
        String axioms = "AntiSymmetricObjectProperty(as) "
                + "SubObjectPropertyOf(r as) "
                + "ObjectPropertyAssertion(as b a) "
                + "ObjectPropertyAssertion(r a b)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability1() throws Exception {
        String axioms = "ClassAssertion(a C) "
                + "ClassAssertion(a ObjectComplementOf(C))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability2() throws Exception {
        String axioms = "SubClassOf(owl:Thing C) " + "SubClassOf(owl:Thing "
                + "ObjectComplementOf(C))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability3() throws Exception {
        String axioms = "SubClassOf(Person "
                + "ObjectSomeValuesFrom(hasParent Person)) "
                + "SubClassOf(ObjectSomeValuesFrom(hasParent "
                + "ObjectSomeValuesFrom(hasParent Person)) " + "Grandchild) "
                + "ClassAssertion(peter Person) " + "ClassAssertion(peter "
                + "ObjectComplementOf(Grandchild))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability4() throws Exception {
        String axioms = "FunctionalObjectProperty(R) "
                + "ObjectPropertyAssertion(R a b) "
                + "SubClassOf(owl:Thing ObjectSomeValuesFrom(R C)) "
                + "ClassAssertion(b ObjectComplementOf(C))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
    }

    public void testChanges() throws Exception {
        String axioms = "SubClassOf(owl:Thing C)";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(true);
        axioms = "SubClassOf(owl:Thing C) "
                + "SubClassOf(owl:Thing ObjectComplementOf(C))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(false);
        axioms = "SubClassOf(owl:Thing ObjectComplementOf(C))";
        loadOntologyWithAxioms(axioms, null);
        assertABoxSatisfiable(true);
    }

    public void testSubsumption1() throws Exception {
        String axioms = "SubClassOf(Person Animal) "
                + "SubClassOf(Student Person) " + "SubClassOf(Dog Animal)";
        loadOntologyWithAxioms(axioms, null);
        assertSubsumedBy("Student", "Animal", true);
        assertSubsumedBy("Animal", "Student", false);
        assertSubsumedBy("Student", "Dog", false);
        assertSubsumedBy("Dog", "Student", false);
    }

    public void testSubsumption2() throws Exception {
        String axioms = "SubObjectPropertyOf(R S) "
                + "EquivalentClasses(A ObjectSomeValuesFrom(R C)) "
                + "EquivalentClasses(B ObjectSomeValuesFrom(S C))";
        loadOntologyWithAxioms(axioms, null);
        assertSubsumedBy("A", "B", true);
        assertSubsumedBy("B", "A", false);
    }

    public void testSubsumption3() throws Exception {
        String axioms = "EquivalentObjectProperties(R S) "
                + "EquivalentClasses(A ObjectSomeValuesFrom(R C)) "
                + "EquivalentClasses(B ObjectSomeValuesFrom(S C))";
        loadOntologyWithAxioms(axioms, null);
        assertSubsumedBy("A", "B", true);
        assertSubsumedBy("B", "A", true);
    }

    public void testHeinsohnTBox1() throws Exception {
        // Tests incoherency caused by disjoint concepts
        String axioms = "DisjointClasses(c d) SubClassOf(e3 c) "
                + "SubClassOf(f d) SubClassOf(c1 d1) "
                + "DisjointClasses(c1 d1) EquivalentClasses(complex1 "
                + "ObjectIntersectionOf(c d)) EquivalentClasses(complex2 "
                + "ObjectIntersectionOf(ObjectAllValuesFrom(r "
                + "ObjectIntersectionOf(c d)) ObjectSomeValuesFrom(r "
                + "owl:Thing))) EquivalentClasses(complex3 "
                + "ObjectIntersectionOf(e3 f))";
        loadOntologyWithAxioms(axioms, null);
        assertSatisfiable("complex1", false);
        assertSatisfiable("complex2", false);
        assertSatisfiable("complex3", false);
        assertSatisfiable("c1", false);
    }

    public void testHeinsohnTBox2() throws Exception {
        // Tests incoherency caused by number restrictions
        String axioms = "DisjointClasses(c d)" + "EquivalentClasses(complex1 "
                + "ObjectIntersectionOf(ObjectMinCardinality(2 r) "
                + "ObjectMaxCardinality(1 r)))" + "EquivalentClasses(complex2 "
                + "ObjectIntersectionOf(ObjectMaxCardinality(1 r) "
                + "ObjectSomeValuesFrom(r c) ObjectSomeValuesFrom(r d)))";
        loadOntologyWithAxioms(axioms, null);
        assertSatisfiable("complex1", false);
        assertSatisfiable("complex2", false);
    }

    public void testHeinsohnTBox3c() throws Exception {
        // Tests incoherency caused by the role hierarchy and number
        // restrictions
        String axioms = "DisjointClasses(c d)"
                + "SubClassOf(a ObjectIntersectionOf(c d))"
                + "SubObjectPropertyOf(t1 tc)" + "SubObjectPropertyOf(t1 td)"
                + "SubClassOf(owl:Thing ObjectAllValuesFrom(tc c))"
                + "SubClassOf(owl:Thing ObjectAllValuesFrom(td d))"
                + "SubObjectPropertyOf(tc r)" + "SubObjectPropertyOf(td s)"
                + "EquivalentClasses(complex1 "
                + "ObjectIntersectionOf(ObjectAllValuesFrom(t1 a) "
                + "ObjectMinCardinality(3 t1) " + "ObjectMaxCardinality(1 r) "
                + "ObjectMaxCardinality(1 s)))";
        loadOntologyWithAxioms(axioms, null);
        assertSatisfiable("complex1", false);
    }

    public void testHeinsohnTBox3cIrh() throws Exception {
        // Tests incoherency caused by number restrictions
        String axioms = "DisjointClasses(c d) "
                + "EquivalentClasses(a ObjectUnionOf(c d))"
                + "EquivalentClasses(complex1 ObjectIntersectionOf("
                + "ObjectAllValuesFrom(tt a)" + "ObjectMinCardinality(3 tt)"
                + "ObjectMaxCardinality(1 tt c)"
                + "ObjectMaxCardinality(1 tt d)" + "))";
        loadOntologyWithAxioms(axioms, null);
        assertSatisfiable("complex1", false);
    }

    public void testHeinsohnTBox3() throws Exception {
        // Tests incoherency caused by number restrictions and role hierarchy
        StringBuffer buffer = new StringBuffer();
        buffer.append("DisjointClasses(c d e)");
        buffer.append("SubClassOf(a ObjectUnionOf(c d))");
        buffer.append("SubObjectPropertyOf(r1 r)");
        buffer.append("SubObjectPropertyOf(r2 r)");
        buffer.append("SubObjectPropertyOf(r3 r)");
        buffer.append("SubObjectPropertyOf(t1 tt)");
        buffer.append("SubObjectPropertyOf(t2 tt)");
        buffer.append("SubObjectPropertyOf(t3 tt)");
        buffer.append("EquivalentClasses(complex1a ObjectIntersectionOf(");
        buffer.append("ObjectMinCardinality(1 r)");
        buffer.append("ObjectSomeValuesFrom(r c)");
        buffer.append("ObjectSomeValuesFrom(r d)))");
        buffer.append("EquivalentClasses(complex1b ");
        buffer.append("ObjectMinCardinality(2 r))");
        buffer.append("EquivalentClasses(complex2a ObjectIntersectionOf(");
        buffer.append("ObjectMaxCardinality(2 r)");
        buffer.append("ObjectSomeValuesFrom(r c)");
        buffer.append("ObjectSomeValuesFrom(r d)");
        buffer.append("))");
        buffer.append("EquivalentClasses(complex2b ObjectIntersectionOf(");
        buffer.append("ObjectMaxCardinality(1 r c)");
        buffer.append("ObjectMaxCardinality(1 r d)");
        buffer.append("))");
        buffer.append("EquivalentClasses(complex3a ObjectIntersectionOf(");
        buffer.append("ObjectAllValuesFrom(r a)");
        buffer.append("ObjectMinCardinality(3 r)");
        buffer.append("ObjectMaxCardinality(1 r c)");
        buffer.append("))");
        buffer.append("EquivalentClasses(complex3b ");
        buffer.append("ObjectMinCardinality(2 r d))");
        buffer.append("EquivalentClasses(complex4a ObjectIntersectionOf(");
        buffer.append("ObjectSomeValuesFrom(r1 ");
        buffer.append("ObjectIntersectionOf(ObjectMaxCardinality(1 tt) ");
        buffer.append("ObjectSomeValuesFrom(t1 c)))");
        buffer.append("ObjectSomeValuesFrom(r2 ");
        buffer.append("ObjectIntersectionOf(ObjectMaxCardinality(1 tt) ");
        buffer.append("ObjectSomeValuesFrom(t2 d)))");
        buffer.append("ObjectSomeValuesFrom(r2 ");
        buffer.append("ObjectIntersectionOf(ObjectMaxCardinality(1 tt) ");
        buffer.append("ObjectSomeValuesFrom(t2 d)))");
        buffer.append("ObjectSomeValuesFrom(r3 ");
        buffer.append("ObjectIntersectionOf(ObjectMaxCardinality(1 tt) ");
        buffer.append("ObjectSomeValuesFrom(t3 e)))");
        buffer.append("))");
        buffer.append("EquivalentClasses(complex4b ");
        buffer.append("ObjectMinCardinality(2 r))");
        loadOntologyWithAxioms(buffer.toString(), null);
        assertSubsumedBy("complex1a", "complex1b", true);
        assertSubsumedBy("complex2a", "complex2b", true);
        assertSubsumedBy("complex3a", "complex3b", true);
        assertSubsumedBy("complex4a", "complex4b", true);
    }

     public void testHeinsohnTBox3Modified() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(C D)");
         buffer.append("SubClassOf(A ObjectMaxCardinality(2 r))");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(r C))");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(r D))");
         buffer.append("SubClassOf(owl:Thing ObjectUnionOf(ObjectMinCardinality(2 r C) ObjectMinCardinality(2 r D) B))");
         loadOntologyWithAxioms(buffer.toString(), null);
         assertSubsumedBy("A","B",true);
     }
     public void testHeinsohnTBox4a() throws Exception {
         // Tests role restrictions
         loadOntologyWithAxioms("", null);
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription D = df.getOWLClass(URI.create("file:/c/test.owl#D"));
         OWLDescription E = df.getOWLClass(URI.create("file:/c/test.owl#E"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         OWLDescription desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectAllRestriction(r, D), df.getOWLObjectAllRestriction(r, df.getOWLObjectUnionOf(df.getOWLObjectComplementOf(D), E)));
         OWLDescription desc2 = df.getOWLObjectAllRestriction(r, E);
         assertSubsumedBy(desc1,desc2,true);
     }

     public void testHeinsohnTBox4b() throws Exception {
         // Tests role restrictions
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(C D)");
         loadOntologyWithAxioms(buffer.toString(), null);
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription C = df.getOWLClass(URI.create("file:/c/test.owl#C"));
         OWLDescription D = df.getOWLClass(URI.create("file:/c/test.owl#D"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         OWLObjectProperty s = df.getOWLObjectProperty(URI.create("file:/c/test.owl#s"));
         OWLDescription desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectAllRestriction(r, df.getOWLObjectUnionOf(df.getOWLObjectComplementOf(df.getOWLObjectMinCardinalityRestriction(s, 2)), C)), df.getOWLObjectAllRestriction(r, D));
         OWLDescription desc2 = df.getOWLObjectAllRestriction(r, df.getOWLObjectMaxCardinalityRestriction(s, 1));
         assertSubsumedBy(desc1,desc2,true);
     }
     
      public void testHeinsohnTBox7() throws Exception {
          // Tests inverse roles
          StringBuffer buffer = new StringBuffer();
          buffer.append("InverseObjectProperties(r InverseObjectProperty(r))");
          loadOntologyWithAxioms(buffer.toString(), null);
          OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
          OWLDescription A = df.getOWLClass(URI.create("file:/c/test.owl#A"));
          OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
          OWLObjectPropertyExpression invr = df.getOWLObjectPropertyInverse(r);
          OWLDescription desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectAllRestriction(r, df.getOWLObjectAllRestriction(invr, A)), df.getOWLObjectSomeRestriction(r, df.getOWLThing()));
          assertSubsumedBy(desc1,A,true);
     }
      
     public void testIanT1a() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(r InverseObjectProperty(r))");
         buffer.append("SubClassOf(p1 ObjectComplementOf(ObjectUnionOf(p2 p3 p4 p5)))");
         buffer.append("SubClassOf(p2 ObjectComplementOf(ObjectUnionOf(p3 p4 p5)))");
         buffer.append("SubClassOf(p3 ObjectComplementOf(ObjectUnionOf(p4 p5)))");
         buffer.append("SubClassOf(p4 ObjectComplementOf(p5))");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p1 = df.getOWLClass(URI.create("file:/c/test.owl#p1"));
         OWLDescription p2 = df.getOWLClass(URI.create("file:/c/test.owl#p2"));
         OWLDescription p3 = df.getOWLClass(URI.create("file:/c/test.owl#p3"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));

         OWLDescription desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectSomeRestriction(r, p1), df.getOWLObjectSomeRestriction(r, p2), df.getOWLObjectSomeRestriction(r, p3), df.getOWLObjectMaxCardinalityRestriction(r, 2));
         assertSatisfiable(desc1,false);
     }
     public void testIanT1b() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(r InverseObjectProperty(r))");
         buffer.append("SubClassOf(p1 ObjectComplementOf(ObjectUnionOf(p2 p3 p4 p5)))");
         buffer.append("SubClassOf(p2 ObjectComplementOf(ObjectUnionOf(p3 p4 p5)))");
         buffer.append("SubClassOf(p3 ObjectComplementOf(ObjectUnionOf(p4 p5)))");
         buffer.append("SubClassOf(p4 ObjectComplementOf(p5))");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p1 = df.getOWLClass(URI.create("file:/c/test.owl#p1"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         OWLObjectPropertyExpression invr = df.getOWLObjectPropertyInverse(r);
         
         OWLDescription desc1 = df.getOWLObjectSomeRestriction(invr, df.getOWLObjectIntersectionOf(df.getOWLObjectSomeRestriction(r, p1), df.getOWLObjectMaxCardinalityRestriction(r, 1, p1)));
         assertSatisfiable(desc1,true);
     }
     public void testIanT1c() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(r InverseObjectProperty(r))");
         buffer.append("SubClassOf(p1 ObjectComplementOf(ObjectUnionOf(p2 p3 p4 p5)))");
         buffer.append("SubClassOf(p2 ObjectComplementOf(ObjectUnionOf(p3 p4 p5)))");
         buffer.append("SubClassOf(p3 ObjectComplementOf(ObjectUnionOf(p4 p5)))");
         buffer.append("SubClassOf(p4 ObjectComplementOf(p5))");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p1 = df.getOWLClass(URI.create("file:/c/test.owl#p1"));
         OWLDescription p2 = df.getOWLClass(URI.create("file:/c/test.owl#p2"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         OWLObjectPropertyExpression invr = df.getOWLObjectPropertyInverse(r);
         
         OWLDescription desc1 = df.getOWLObjectIntersectionOf(p2, df.getOWLObjectSomeRestriction(invr, df.getOWLObjectIntersectionOf(df.getOWLObjectSomeRestriction(r, p1), df.getOWLObjectMaxCardinalityRestriction(r, 1))));
         assertSatisfiable(desc1,false);
     }
     
     public void testIanT2() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubObjectPropertyOf(r f1)");
         buffer.append("SubObjectPropertyOf(r f2)");
         buffer.append("SubClassOf(p1 ObjectComplementOf(p2))");
         buffer.append("FunctionalObjectProperty(f1)");
         buffer.append("FunctionalObjectProperty(f2)");
         loadOntologyWithAxioms(buffer.toString(), null);

         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p1 = df.getOWLClass(URI.create("file:/c/test.owl#p1"));
         OWLDescription p2 = df.getOWLClass(URI.create("file:/c/test.owl#p2"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         OWLObjectProperty f1 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f1"));
         OWLObjectProperty f2 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f2"));
         
         OWLDescription desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectSomeRestriction(f1, p1), df.getOWLObjectSomeRestriction(f2, p2));
         assertSatisfiable(desc1,true);
         
         desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectSomeRestriction(f1, p1), df.getOWLObjectSomeRestriction(f2, p2), df.getOWLObjectSomeRestriction(r, df.getOWLThing()));
         assertSatisfiable(desc1, false);
     }
     
     public void testIanT3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(p1 ObjectComplementOf(ObjectUnionOf(p2 p3 p4 p5)))");
         buffer.append("SubClassOf(p2 ObjectComplementOf(ObjectUnionOf(p3 p4 p5)))");
         buffer.append("SubClassOf(p3 ObjectComplementOf(ObjectUnionOf(p4 p5)))");
         buffer.append("SubClassOf(p4 ObjectComplementOf(p5))");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p = df.getOWLClass(URI.create("file:/c/test.owl#p"));
         OWLDescription p1 = df.getOWLClass(URI.create("file:/c/test.owl#p1"));
         OWLDescription p2 = df.getOWLClass(URI.create("file:/c/test.owl#p2"));
         OWLDescription p3 = df.getOWLClass(URI.create("file:/c/test.owl#p3"));
         OWLDescription p4 = df.getOWLClass(URI.create("file:/c/test.owl#p4"));
         OWLDescription p5 = df.getOWLClass(URI.create("file:/c/test.owl#p5"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));

         OWLDescription desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeRestriction(r, p1), 
                 df.getOWLObjectSomeRestriction(r, p2), 
                 df.getOWLObjectSomeRestriction(r, p3),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p1, p)), 
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p2, p)), 
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p3, p)), 
                 df.getOWLObjectMaxCardinalityRestriction(r, 3));         
         assertSatisfiable(desc,true);
     
         desc = df.getOWLObjectIntersectionOf(
             df.getOWLObjectSomeRestriction(r, p1), 
             df.getOWLObjectSomeRestriction(r, p2), 
             df.getOWLObjectSomeRestriction(r, p3),
             df.getOWLObjectSomeRestriction(r, p4), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p1, p)), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p2, p)), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p3, p)), 
             df.getOWLObjectMaxCardinalityRestriction(r, 3));
         assertSatisfiable(desc,false);
     
         desc = df.getOWLObjectIntersectionOf(
             df.getOWLObjectSomeRestriction(r, p1), 
             df.getOWLObjectSomeRestriction(r, p2), 
             df.getOWLObjectSomeRestriction(r, p3),
             df.getOWLObjectSomeRestriction(r, p4), 
             df.getOWLObjectMaxCardinalityRestriction(r, 3));
         assertSatisfiable(desc,false);
     
         desc = df.getOWLObjectIntersectionOf(
             df.getOWLObjectSomeRestriction(r, p1), 
             df.getOWLObjectSomeRestriction(r, p2), 
             df.getOWLObjectSomeRestriction(r, p3),
             df.getOWLObjectSomeRestriction(r, p4), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p1, p)), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p2, p)), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p3, p)),
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p4, p)),
             df.getOWLObjectMaxCardinalityRestriction(r, 4));
         assertSatisfiable(desc,true);

         desc = df.getOWLObjectIntersectionOf(
             df.getOWLObjectSomeRestriction(r, p1), 
             df.getOWLObjectSomeRestriction(r, p2), 
             df.getOWLObjectSomeRestriction(r, p3),
             df.getOWLObjectSomeRestriction(r, p4),
             df.getOWLObjectSomeRestriction(r, p5), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p1, p)), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p2, p)), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p3, p)),
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p4, p)),
             df.getOWLObjectMaxCardinalityRestriction(r, 4));
         assertSatisfiable(desc,false);

         desc = df.getOWLObjectIntersectionOf(
             df.getOWLObjectSomeRestriction(r, p1), 
             df.getOWLObjectSomeRestriction(r, p2), 
             df.getOWLObjectSomeRestriction(r, p3),
             df.getOWLObjectSomeRestriction(r, p4),
             df.getOWLObjectSomeRestriction(r, p5), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p1, p)), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p2, p)), 
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p3, p)),
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p4, p)),
             df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(p5, p)),
             df.getOWLObjectMaxCardinalityRestriction(r, 5));
         assertSatisfiable(desc,true);
     }
     
     public void testIanT4() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("TransitiveObjectProperty(p)");
         buffer.append("InverseObjectProperties(r InverseObjectProperty(r))");
         buffer.append("InverseObjectProperties(p InverseObjectProperty(p))");
         buffer.append("InverseObjectProperties(s InverseObjectProperty(s))");
         buffer.append("EquivalentClasses(c ObjectAllValuesFrom(InverseObjectProperty(r) ObjectAllValuesFrom(InverseObjectProperty(p) ObjectAllValuesFrom(InverseObjectProperty(s) ObjectComplementOf(a)))))");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription a = df.getOWLClass(URI.create("file:/c/test.owl#a"));
         OWLDescription c = df.getOWLClass(URI.create("file:/c/test.owl#c"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         OWLObjectProperty s = df.getOWLObjectProperty(URI.create("file:/c/test.owl#s"));
         OWLObjectProperty p = df.getOWLObjectProperty(URI.create("file:/c/test.owl#p"));
         
         OWLDescription desc = df.getOWLObjectIntersectionOf(a, 
                 df.getOWLObjectSomeRestriction(s, 
                         df.getOWLObjectIntersectionOf(
                                 df.getOWLObjectSomeRestriction(r, df.getOWLThing()), 
                                 df.getOWLObjectSomeRestriction(p, df.getOWLThing()), 
                                 df.getOWLObjectAllRestriction(r, c), 
                                 df.getOWLObjectAllRestriction(p, df.getOWLObjectSomeRestriction(r, df.getOWLThing())), 
                                 df.getOWLObjectAllRestriction(p, df.getOWLObjectSomeRestriction(p, df.getOWLThing())),
                                 df.getOWLObjectAllRestriction(p, df.getOWLObjectAllRestriction(r, c)))));  
         assertSatisfiable(desc,false);
     }

      public void testIanT5() throws Exception {
          StringBuffer buffer = new StringBuffer();
          buffer.append("InverseObjectProperties(r r-)");
          buffer.append("InverseObjectProperties(f f-)");
          buffer.append("TransitiveObjectProperty(r)");
          buffer.append("SubObjectPropertyOf(f r)");
          buffer.append("FunctionalObjectProperty(f)");
          loadOntologyWithAxioms(buffer.toString(), null);
          
          OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
          OWLDescription a = df.getOWLClass(URI.create("file:/c/test.owl#a"));
          OWLObjectProperty invr = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r-"));
          OWLObjectProperty invf = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f-"));
          
          OWLDescription desc = df.getOWLObjectIntersectionOf(
                  df.getOWLObjectComplementOf(a), 
                  df.getOWLObjectSomeRestriction(invf, a), 
                  df.getOWLObjectAllRestriction(invr, df.getOWLObjectSomeRestriction(invf, a))
              );  
          assertSatisfiable(desc,true);
     }
     public void testIanT6() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(r r-)");
         buffer.append("InverseObjectProperties(f f-)");
         buffer.append("TransitiveObjectProperty(r)");
         buffer.append("SubObjectPropertyOf(f r)");
         buffer.append("FunctionalObjectProperty(f)");
         buffer.append("EquivalentClasses(d ObjectIntersectionOf(c ObjectSomeValuesFrom(f ObjectComplementOf(c))))");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription c = df.getOWLClass(URI.create("file:/c/test.owl#c"));
         OWLDescription d = df.getOWLClass(URI.create("file:/c/test.owl#d"));
         OWLObjectProperty invr = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r-"));
         OWLObjectProperty invf = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f-"));
         
         OWLDescription desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectComplementOf(c), 
                 df.getOWLObjectSomeRestriction(invf, d), 
                 df.getOWLObjectAllRestriction(invr, df.getOWLObjectSomeRestriction(invf, d))
             );  
         assertSatisfiable(desc,false);
     }
     
     public void testIanT7a() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(r r-)");
         buffer.append("InverseObjectProperties(f f-)");
         buffer.append("TransitiveObjectProperty(r)");
         buffer.append("FunctionalObjectProperty(f)");
         //buffer.append("EquivalentClasses(Test ObjectIntersectionOf(p1 ObjectSomeValuesFrom(r ObjectSomeValuesFrom(r ObjectIntersectionOf(p1 ObjectAllValuesFrom(r- ObjectComplementOf(p1)))))))");
         loadOntologyWithAxioms(buffer.toString(), null);
         
//         addAxiom("[objectInverse r r-]");
//         addAxiom("[objectInverse f f-]");
//         addAxiom("[objectTransitive r]");
//         addAxiom("[objectFunctional f]");
         
         //assertSatisfiable("Test", false);
         // The concept Test, if defined in the ontology, but the according OWLDescription (below) is considered satisfiable

         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p1 = df.getOWLClass(URI.create("file:/c/test.owl#p1"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         OWLObjectProperty invr = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r-"));
         
         OWLDescription desc;
         desc = df.getOWLObjectIntersectionOf(
                 p1, 
                 df.getOWLObjectSomeRestriction(r, 
                         df.getOWLObjectSomeRestriction(r, 
                                 df.getOWLObjectIntersectionOf(
                                         p1, 
                                         df.getOWLObjectAllRestriction(invr, 
                                                 df.getOWLObjectComplementOf(p1)
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
         buffer.append("InverseObjectProperties(r r-)");
         buffer.append("InverseObjectProperties(f f-)");
         buffer.append("TransitiveObjectProperty(r)");
         buffer.append("FunctionalObjectProperty(f)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p1 = df.getOWLClass(URI.create("file:/c/test.owl#p1"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         OWLObjectProperty invr = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r-"));

         OWLDescription desc;
         desc = df.getOWLObjectIntersectionOf(
                 p1, 
                 df.getOWLObjectSomeRestriction(r, 
                         df.getOWLObjectSomeRestriction(
                                 r, 
                                 df.getOWLObjectIntersectionOf(
                                         p1, 
                                         df.getOWLObjectAllRestriction(invr, 
                                                 df.getOWLObjectUnionOf(
                                                         df.getOWLObjectComplementOf(p1), 
                                                         df.getOWLObjectAllRestriction(r, p1)
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
         buffer.append("InverseObjectProperties(r r-)");
         buffer.append("InverseObjectProperties(f f-)");
         buffer.append("TransitiveObjectProperty(r)");
         buffer.append("FunctionalObjectProperty(f)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p1 = df.getOWLClass(URI.create("file:/c/test.owl#p1"));
         OWLObjectProperty f = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f"));
         OWLObjectProperty invf = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f-"));
         
         OWLDescription desc;
         desc = df.getOWLObjectSomeRestriction(f, 
                 df.getOWLObjectIntersectionOf(
                         p1,
                         df.getOWLObjectAllRestriction(invf, 
                                 df.getOWLObjectSomeRestriction(
                                         f,
                                         df.getOWLObjectComplementOf(p1)
                                 )
                         )
                 )
             ); 
         assertSatisfiable(desc,false);
     }
     
     public void testIanT8a() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(r r-)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p = df.getOWLClass(URI.create("file:/c/test.owl#p"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         OWLObjectProperty invr = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r-"));
         OWLObjectProperty r1 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r1"));
         
         OWLDescription desc;
         desc = df.getOWLObjectIntersectionOf(df.getOWLObjectSomeRestriction(r, 
                 df.getOWLObjectAllRestriction(invr, df.getOWLObjectAllRestriction(r1, p))), 
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectAllRestriction(invr, 
                         df.getOWLObjectAllRestriction(r1, df.getOWLObjectComplementOf(p)))));
         
         assertSatisfiable(desc,true);
     }
     
     public void testIanT8() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(r r-)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p = df.getOWLClass(URI.create("file:/c/test.owl#p"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         OWLObjectProperty invr = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r-"));
         OWLObjectProperty r1 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r1"));
         
         OWLDescription desc;
         desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeRestriction(r1, df.getOWLThing()), 
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectAllRestriction(invr, df.getOWLObjectAllRestriction(r1, p))), 
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectAllRestriction(invr, df.getOWLObjectAllRestriction(r1, df.getOWLObjectComplementOf(p)))));
         assertSatisfiable(desc,false);
     }
     
     public void testIanT9() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(successor successor-)");
         buffer.append("TransitiveObjectProperty(descendant)");
         buffer.append("SubObjectPropertyOf(successor descendant)");
         buffer.append("InverseFunctionalObjectProperty(successor)");
         buffer.append("SubClassOf(root ObjectComplementOf(ObjectSomeValuesFrom(successor- owl:Thing)))");
         buffer.append("SubClassOf(Infinite-Tree-Node ObjectIntersectionOf(node ObjectSomeValuesFrom(successor Infinite-Tree-Node)))");
         buffer.append("SubClassOf(Infinite-Tree-Root ObjectIntersectionOf(Infinite-Tree-Node root))");
//         addAxiom("[objectInverse successor successor-]");
//         addAxiom("[objectTransitive descendant]");
//         addAxiom("[subObjectPropertyOf successor descendant]");
//         addAxiom("[objectInverseFunctional successor]");
//         addAxiom("[subClassOf root [not [some successor- owl:Thing]]]");
//         addAxiom("[subClassOf Infinite-Tree-Node [and node [some successor Infinite-Tree-Node]]]");
//         addAxiom("[subClassOf Infinite-Tree-Root [and Infinite-Tree-Node root]]");
         
         loadOntologyWithAxioms(buffer.toString(), null);
         assertSatisfiable("Infinite-Tree-Root",true);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription itr = df.getOWLClass(URI.create("file:/c/test.owl#Infinite-Tree-Root"));
         OWLDescription root = df.getOWLClass(URI.create("file:/c/test.owl#root"));
         OWLObjectProperty descendant = df.getOWLObjectProperty(URI.create("file:/c/test.owl#descendant"));
         OWLObjectProperty invsuccessor = df.getOWLObjectProperty(URI.create("file:/c/test.owl#successor-"));
         
         OWLDescription desc;
         desc = df.getOWLObjectIntersectionOf(
                 itr, 
                 df.getOWLObjectAllRestriction(descendant, 
                         df.getOWLObjectSomeRestriction(invsuccessor, root)
                 )
         );
         //[and Infinite-Tree-Root [all descendant [some successor- root]]]
         assertSatisfiable(desc,false);
     }
     public void testIanT10() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(s s-)");
         buffer.append("InverseObjectProperties(f f-)");
         buffer.append("InverseObjectProperties(f1 f1-)");
         buffer.append("FunctionalObjectProperty(f)");
         buffer.append("FunctionalObjectProperty(f1)");
         buffer.append("SubObjectPropertyOf(s f)");
         buffer.append("SubObjectPropertyOf(s f1)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p = df.getOWLClass(URI.create("file:/c/test.owl#p"));
         OWLObjectProperty f = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f"));
         OWLObjectProperty invf = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f-"));
         OWLObjectProperty f1 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f1"));
         OWLObjectProperty invf1 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f1-"));
         OWLObjectProperty s= df.getOWLObjectProperty(URI.create("file:/c/test.owl#s"));
         OWLObjectProperty invs= df.getOWLObjectProperty(URI.create("file:/c/test.owl#s-"));
         
         OWLDescription desc;
         desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectComplementOf(p), 
                 df.getOWLObjectSomeRestriction(f, 
                         df.getOWLObjectIntersectionOf(
                                 df.getOWLObjectAllRestriction(invs, p), 
                                 df.getOWLObjectAllRestriction(invf, df.getOWLObjectSomeRestriction(s, p))
                         )
                 )
         );
         
         assertSatisfiable(desc,false);
         
         desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectAllRestriction(s, 
                         df.getOWLObjectComplementOf(p)
                 ), 
                 df.getOWLObjectSomeRestriction(s, 
                         df.getOWLObjectIntersectionOf(
                                 p, 
                                 df.getOWLObjectSomeRestriction(invs, p)
                         )
                 )
         );
         assertSatisfiable(desc,false);
         
         desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeRestriction(f, p), 
                 df.getOWLObjectSomeRestriction(f1, df.getOWLObjectComplementOf(p))
         );
         assertSatisfiable(desc,true);
         
         desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeRestriction(f, p), 
                 df.getOWLObjectSomeRestriction(s, df.getOWLThing()),
                 df.getOWLObjectSomeRestriction(f1, df.getOWLObjectComplementOf(p))
         );
         assertSatisfiable(desc,false);
         
         desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeRestriction(f1, p), 
                 df.getOWLObjectSomeRestriction(f1, 
                         df.getOWLObjectIntersectionOf(
                                 df.getOWLObjectComplementOf(p),
                                 df.getOWLObjectAllRestriction(invf1, 
                                         df.getOWLObjectSomeRestriction(s, df.getOWLThing())
                                 )
                         )
                 )
         );
         assertSatisfiable(desc,false);
     }
     
     public void testIanT11() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(s s-)");
         buffer.append("SubObjectPropertyOf(s r)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p = df.getOWLClass(URI.create("file:/c/test.owl#p"));
         OWLObjectProperty s= df.getOWLObjectProperty(URI.create("file:/c/test.owl#s"));
         OWLObjectProperty invs = df.getOWLObjectProperty(URI.create("file:/c/test.owl#s-"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));

         OWLDescription desc;
         desc = df.getOWLObjectIntersectionOf(
                     df.getOWLObjectComplementOf(p), 
                     df.getOWLObjectMaxCardinalityRestriction(r, 1), 
                     df.getOWLObjectSomeRestriction(r, 
                             df.getOWLObjectAllRestriction(invs, p)
                     ), 
                     df.getOWLObjectSomeRestriction(s, p)
         );
         
         assertSatisfiable(desc,false);
     }
     
     public void testIanT12() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(r r-)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p = df.getOWLClass(URI.create("file:/c/test.owl#p"));
         OWLDescription q = df.getOWLClass(URI.create("file:/c/test.owl#q"));
         OWLObjectProperty s = df.getOWLObjectProperty(URI.create("file:/c/test.owl#s"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         OWLObjectProperty invr = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r-"));
         
         OWLDescription desc;
         desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeRestriction(s, 
                         df.getOWLObjectIntersectionOf(
                                 df.getOWLObjectComplementOf(p), 
                                 df.getOWLObjectComplementOf(q)
                         )
                 ), 
                 df.getOWLObjectSomeRestriction(r, 
                         df.getOWLObjectIntersectionOf(
                                 df.getOWLObjectMaxCardinalityRestriction(invr, 1), 
                                 df.getOWLObjectSomeRestriction(invr, 
                                         df.getOWLObjectAllRestriction(s, p)
                                 )
                         )
                 )
         );
         
         assertSatisfiable(desc,false);
     }
     
     public void testIanT13() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(s s-)");
         buffer.append("EquivalentClasses(a1 ObjectSomeValuesFrom(s ObjectAllValuesFrom(s- ObjectAllValuesFrom(r c))))");
         buffer.append("EquivalentClasses(a2 ObjectSomeValuesFrom(s ObjectAllValuesFrom(s- ObjectAllValuesFrom(r ObjectComplementOf(c)))))");
         buffer.append("EquivalentClasses(a3a ObjectSomeValuesFrom(s ObjectAllValuesFrom(s- ObjectUnionOf(ObjectSomeValuesFrom(r d) ObjectSomeValuesFrom(s d)))))");
         buffer.append("EquivalentClasses(a3b ObjectUnionOf(ObjectSomeValuesFrom(r d) ObjectSomeValuesFrom(s d)))");
         buffer.append("EquivalentClasses(a3c ObjectUnionOf(ObjectSomeValuesFrom(r d) d))");
         buffer.append("EquivalentClasses(a3e ObjectSomeValuesFrom(r d))");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription a = df.getOWLClass(URI.create("file:/c/test.owl#a"));
         OWLDescription a1 = df.getOWLClass(URI.create("file:/c/test.owl#a1"));
         OWLDescription a2 = df.getOWLClass(URI.create("file:/c/test.owl#a2"));
         OWLDescription a3a = df.getOWLClass(URI.create("file:/c/test.owl#a3a"));
         OWLDescription a3b = df.getOWLClass(URI.create("file:/c/test.owl#a3b"));
         OWLDescription a3c = df.getOWLClass(URI.create("file:/c/test.owl#a3c"));
         OWLDescription a3e = df.getOWLClass(URI.create("file:/c/test.owl#a3e"));

         OWLDescription desc = df.getOWLObjectIntersectionOf(a3a, a2, a1);
         assertSatisfiable(desc,true);
         desc = df.getOWLObjectIntersectionOf(a3b, a2, a1);
         assertSatisfiable(desc,true);
         desc = df.getOWLObjectIntersectionOf(a3c, a2, a1);
         assertSatisfiable(desc,true);
         desc = df.getOWLObjectIntersectionOf(a3e, a2, a1);
         assertSatisfiable(desc,false);
         desc = df.getOWLObjectIntersectionOf(a, a2, a1);
         assertSatisfiable(desc,true);
         desc = df.getOWLObjectIntersectionOf(df.getOWLObjectIntersectionOf(a3a, a2, a1), df.getOWLObjectComplementOf(df.getOWLObjectIntersectionOf(a3b, a2, a1)));
         assertSatisfiable(desc,false);
         desc = df.getOWLObjectIntersectionOf(df.getOWLObjectComplementOf(df.getOWLObjectIntersectionOf(a3a, a2, a1)), df.getOWLObjectIntersectionOf(a3b, a2, a1));
         assertSatisfiable(desc,false);
         desc = df.getOWLObjectIntersectionOf(df.getOWLObjectIntersectionOf(a3c, a2, a1), df.getOWLObjectComplementOf(df.getOWLObjectIntersectionOf(a3c, a2, a1)));
         assertSatisfiable(desc,false);
     }
     
     public void testIanFact1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(a b c)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription a = df.getOWLClass(URI.create("file:/c/test.owl#a"));
         OWLDescription b = df.getOWLClass(URI.create("file:/c/test.owl#b"));
         OWLDescription c = df.getOWLClass(URI.create("file:/c/test.owl#c"));

         OWLDescription desc = df.getOWLObjectUnionOf(
                 df.getOWLObjectIntersectionOf(a, b),
                 df.getOWLObjectIntersectionOf(a, c),
                 df.getOWLObjectIntersectionOf(b, c)
         );
         assertSatisfiable(desc,false);
     }
     
     public void testIanFact2() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(c ObjectAllValuesFrom(r c))");
         buffer.append("SubClassOf(ObjectAllValuesFrom(r c) d)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         assertSubsumedBy("c","d",true);
     }
     
     public void testIanFact3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("FunctionalObjectProperty(f1)");
         buffer.append("FunctionalObjectProperty(f2)");
         buffer.append("FunctionalObjectProperty(f3)");
         buffer.append("SubObjectPropertyOf(f3 f1)");
         buffer.append("SubObjectPropertyOf(f3 f2)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p1 = df.getOWLClass(URI.create("file:/c/test.owl#p1"));
         OWLDescription p2 = df.getOWLClass(URI.create("file:/c/test.owl#p2"));
         OWLObjectProperty f1 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f1"));
         OWLObjectProperty f2 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f2"));
         OWLObjectProperty f3 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#f3"));
         
         OWLDescription desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeRestriction(f1, p1),
                 df.getOWLObjectSomeRestriction(f2, df.getOWLObjectComplementOf(p1)),
                 df.getOWLObjectSomeRestriction(f3, p2) 
         );
         assertSatisfiable(desc,false);
     }
     
     public void testIanFact4() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("FunctionalObjectProperty(rx)");
        buffer.append("FunctionalObjectProperty(rx3)");
        buffer.append("SubObjectPropertyOf(rx3 rx)");
        buffer.append("SubObjectPropertyOf(rx3 rx1)");
        buffer.append("FunctionalObjectProperty(rx4)");
        buffer.append("SubObjectPropertyOf(rx4 rx)");
        buffer.append("SubObjectPropertyOf(rx4 rx2)");
        buffer.append("FunctionalObjectProperty(rx3a)");
        buffer.append("SubObjectPropertyOf(rx3a rxa)");
        buffer.append("SubObjectPropertyOf(rx3a rx1a)");
        buffer.append("FunctionalObjectProperty(rx4a)");
        buffer.append("SubObjectPropertyOf(rx4a rxa)");
        buffer.append("SubObjectPropertyOf(rx4a rx2a)");

        loadOntologyWithAxioms(buffer.toString(), null);

        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLDescription c1 = df.getOWLClass(URI.create("file:/c/test.owl#c1"));
        OWLDescription c2 = df.getOWLClass(URI.create("file:/c/test.owl#c2"));
        OWLObjectProperty rx3 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#rx3"));
        OWLObjectProperty rx4 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#rx4"));
        OWLObjectProperty rx3a = df.getOWLObjectProperty(URI.create("file:/c/test.owl#rx3a"));
        OWLObjectProperty rx4a = df.getOWLObjectProperty(URI.create("file:/c/test.owl#rx4a"));

        OWLDescription desc1 = df.getOWLObjectIntersectionOf(
                df.getOWLObjectSomeRestriction(rx3, c1),
                df.getOWLObjectSomeRestriction(rx4, c2)
        );
        OWLDescription desc2 = df.getOWLObjectSomeRestriction(rx3, df.getOWLObjectIntersectionOf(c1, c2));
        assertSubsumedBy(desc1,desc2,true);
        desc1 = df.getOWLObjectIntersectionOf(
                df.getOWLObjectSomeRestriction(rx3a, c1),
                df.getOWLObjectSomeRestriction(rx4a, c2)
        );
        desc2 = df.getOWLObjectSomeRestriction(rx3a, df.getOWLObjectIntersectionOf(c1, c2));
        assertSubsumedBy(desc1,desc2,false);
    }
     public void testIanBug1b() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("EquivalentClasses(c ObjectIntersectionOf(a ObjectComplementOf(b)))");
         buffer.append("SubClassOf(a ObjectIntersectionOf(d ObjectComplementOf(c)))");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription a = df.getOWLClass(URI.create("file:/c/test.owl#a"));
         OWLDescription b = df.getOWLClass(URI.create("file:/c/test.owl#b"));
         OWLDescription c = df.getOWLClass(URI.create("file:/c/test.owl#c"));
         OWLDescription d = df.getOWLClass(URI.create("file:/c/test.owl#d"));
         
         OWLDescription desc = df.getOWLObjectIntersectionOf(df.getOWLObjectComplementOf(c), a, df.getOWLObjectComplementOf(b), d);
         assertSatisfiable(desc,false);
     }
     public void testIanBug3() throws Exception {
         // slow, but works!
         loadOntologyWithAxioms("", null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription a = df.getOWLClass(URI.create("file:/c/test.owl#a"));
         OWLDescription c = df.getOWLClass(URI.create("file:/c/test.owl#c"));
         OWLDescription d = df.getOWLClass(URI.create("file:/c/test.owl#d"));
         OWLDescription e = df.getOWLClass(URI.create("file:/c/test.owl#e"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         
         OWLDescription desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeRestriction(r, a), 
                 df.getOWLObjectMinCardinalityRestriction(r, 3, c), 
                 df.getOWLObjectMinCardinalityRestriction(r, 3, d),
                 df.getOWLObjectMinCardinalityRestriction(r, 2, df.getOWLObjectIntersectionOf(
                         e, 
                         df.getOWLObjectComplementOf(df.getOWLObjectIntersectionOf(c, d)))), 
                 df.getOWLObjectMaxCardinalityRestriction(r, 4), 
                 df.getOWLObjectMaxCardinalityRestriction(r, 2, df.getOWLObjectIntersectionOf(c, d)) 
         );
         assertSatisfiable(desc,true);
     }
     
     public void testIanBug4() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(r r-)");
         buffer.append("SubObjectPropertyOf(r r-)");
         buffer.append("TransitiveObjectProperty(r)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
//         addAxiom("[objectInverse r r-]");
//         addAxiom("[subObjectPropertyOf r r-]");
//         addAxiom("[objectTransitive r]");
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription c = df.getOWLClass(URI.create("file:/c/test.owl#c"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         
         OWLDescription desc = df.getOWLObjectIntersectionOf(
                 c, 
                 df.getOWLObjectSomeRestriction(r, df.getOWLThing()), 
                 df.getOWLObjectAllRestriction(r, df.getOWLObjectComplementOf(c))
         );
         assertSatisfiable(desc,false);
//       assertSatisfiable("[and c [some r owl:Thing] [all r [not c]]]", false);
         
         desc = df.getOWLObjectIntersectionOf(
                 c, 
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectSomeRestriction(r, c)), 
                 df.getOWLObjectAllRestriction(r, df.getOWLObjectComplementOf(c))
         );
         assertSatisfiable(desc,false);
//         assertSatisfiable("[and c [some r [some r c]] [all r [not c]]]", false);
     }
     
     public void testIanBug5() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("TransitiveObjectProperty(r1)");
         buffer.append("SubObjectPropertyOf(r2 r1)");
         buffer.append("TransitiveObjectProperty(r2)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
//         addAxiom("[objectTransitive r1]");
//         addAxiom("[subObjectPropertyOf r2 r1]");
//         addAxiom("[objectTransitive r2]");
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription p = df.getOWLClass(URI.create("file:/c/test.owl#p"));
         OWLObjectProperty r1 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r1"));
         OWLObjectProperty r2 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r2"));
         
         OWLDescription desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectAllRestriction(r1, p), 
                 df.getOWLObjectSomeRestriction(r2, df.getOWLObjectSomeRestriction(r1, df.getOWLObjectComplementOf(p)))
         );
         assertSatisfiable(desc,false);
//         assertSatisfiable("[and [all r1 p] [some r2 [some r1 [not p]]]]",false);
     }
     
     public void testIanBug6() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubObjectPropertyOf(S1 R)");
         buffer.append("TransitiveObjectProperty(S1)");
         buffer.append("SubObjectPropertyOf(S2 R)");
         buffer.append("TransitiveObjectProperty(S2)");
         buffer.append("SubObjectPropertyOf(P S1)");
         buffer.append("SubObjectPropertyOf(P S2)");
         loadOntologyWithAxioms(buffer.toString(), null);
         
//         addAxiom("[subObjectPropertyOf S1 R]");
//         addAxiom("[objectTransitive S1]");
//         addAxiom("[subObjectPropertyOf S2 R]");
//         addAxiom("[objectTransitive S2]");
//         addAxiom("[subObjectPropertyOf P S1]");
//         addAxiom("[subObjectPropertyOf P S2]");
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription C = df.getOWLClass(URI.create("file:/c/test.owl#C"));
         OWLObjectProperty R = df.getOWLObjectProperty(URI.create("file:/c/test.owl#R"));
         OWLObjectProperty P = df.getOWLObjectProperty(URI.create("file:/c/test.owl#P"));
         OWLObjectProperty S1 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#S1"));
         OWLObjectProperty S2 = df.getOWLObjectProperty(URI.create("file:/c/test.owl#S2"));
         
         OWLDescription desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectAllRestriction(R, C), 
                 df.getOWLObjectSomeRestriction(P, df.getOWLObjectSomeRestriction(S1, df.getOWLObjectComplementOf(C)))
         );
         assertSatisfiable(desc,false);
         //assertSatisfiable("[and [all R C] [some P [some S1 [not C]]]]",false);
         
         desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectAllRestriction(R, C), 
                 df.getOWLObjectSomeRestriction(P, df.getOWLObjectSomeRestriction(S2, df.getOWLObjectComplementOf(C)))
         );
         assertSatisfiable(desc,false);
         //assertSatisfiable("[and [all R C] [some P [some S2 [not C]]]]",false);
     }
     
     public void testIanBug7() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(A ObjectComplementOf(B))");
         loadOntologyWithAxioms(buffer.toString(), null);
         //addAxiom("[subClassOf A [not B]]");
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription A = df.getOWLClass(URI.create("file:/c/test.owl#A"));
         OWLDescription B = df.getOWLClass(URI.create("file:/c/test.owl#B"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         
         OWLDescription desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeRestriction(r, A), 
                 df.getOWLObjectMaxCardinalityRestriction(r, 1, A),
                 df.getOWLObjectSomeRestriction(r, B),
                 df.getOWLObjectMaxCardinalityRestriction(r, 1, B)
         );
         assertSatisfiable(desc,true);
         //assertSatisfiable("[and [some r A] [atMost 1 r A] [some r B] [atMost 1 r B]]",true);
     }
     public void testIanBug8() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(X ObjectComplementOf(Y))");
         buffer.append("SubClassOf(A ObjectIntersectionOf(ObjectMinCardinality(1 r X) ObjectMaxCardinality(1 r X)))");
         buffer.append("SubClassOf(A ObjectIntersectionOf(ObjectMinCardinality(1 r Y) ObjectMaxCardinality(1 r Y)))");
         loadOntologyWithAxioms(buffer.toString(), null);
         assertSatisfiable("A",true);
     }
     
     public void testIanMergeTest1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(r r-)");
         buffer.append("SubClassOf(c ObjectSomeValuesFrom(r ObjectAllValuesFrom(r- ObjectComplementOf(d))))");
         loadOntologyWithAxioms(buffer.toString(), null);

         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription c = df.getOWLClass(URI.create("file:/c/test.owl#c"));
         OWLDescription c1 = df.getOWLClass(URI.create("file:/c/test.owl#c1"));
         OWLDescription c2 = df.getOWLClass(URI.create("file:/c/test.owl#c2"));
         OWLDescription c3 = df.getOWLClass(URI.create("file:/c/test.owl#c3"));
         OWLDescription c4 = df.getOWLClass(URI.create("file:/c/test.owl#c4"));
         OWLDescription c5 = df.getOWLClass(URI.create("file:/c/test.owl#c5"));
         OWLDescription c6 = df.getOWLClass(URI.create("file:/c/test.owl#c6"));
         OWLDescription c7 = df.getOWLClass(URI.create("file:/c/test.owl#c7"));
         OWLDescription c8 = df.getOWLClass(URI.create("file:/c/test.owl#c8"));
         OWLDescription c9 = df.getOWLClass(URI.create("file:/c/test.owl#c9"));
         OWLDescription c10 = df.getOWLClass(URI.create("file:/c/test.owl#c10"));
         OWLDescription c11 = df.getOWLClass(URI.create("file:/c/test.owl#c11"));
         OWLDescription c12 = df.getOWLClass(URI.create("file:/c/test.owl#c12"));
         OWLDescription c13 = df.getOWLClass(URI.create("file:/c/test.owl#c13"));
         OWLDescription c14 = df.getOWLClass(URI.create("file:/c/test.owl#c14"));
         OWLDescription c15 = df.getOWLClass(URI.create("file:/c/test.owl#c15"));
         OWLDescription c16 = df.getOWLClass(URI.create("file:/c/test.owl#c16"));
         OWLDescription c17 = df.getOWLClass(URI.create("file:/c/test.owl#c17"));
         OWLDescription c18 = df.getOWLClass(URI.create("file:/c/test.owl#c18"));
         OWLDescription d = df.getOWLClass(URI.create("file:/c/test.owl#d"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         
         OWLDescription desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeRestriction(r, c1), 
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c2)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c3)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c4)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c5)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c6)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c7)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c8)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c9)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c10)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c11)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c12)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c13)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c14)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c15)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c16)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c17)),
                 df.getOWLObjectSomeRestriction(r, c18),
                 df.getOWLObjectMaxCardinalityRestriction(r, 1, d)
         );
         assertSatisfiable(desc,true);
     }
     public void testIanMergeTest2() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(r r-)");
         buffer.append("SubClassOf(c ObjectSomeValuesFrom(r ObjectAllValuesFrom(r- d)))");
         loadOntologyWithAxioms(buffer.toString(), null);
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription c = df.getOWLClass(URI.create("file:/c/test.owl#c"));
         OWLDescription c1 = df.getOWLClass(URI.create("file:/c/test.owl#c1"));
         OWLDescription c2 = df.getOWLClass(URI.create("file:/c/test.owl#c2"));
         OWLDescription c3 = df.getOWLClass(URI.create("file:/c/test.owl#c3"));
         OWLDescription c4 = df.getOWLClass(URI.create("file:/c/test.owl#c4"));
         OWLDescription c5 = df.getOWLClass(URI.create("file:/c/test.owl#c5"));
         OWLDescription c6 = df.getOWLClass(URI.create("file:/c/test.owl#c6"));
         OWLDescription c7 = df.getOWLClass(URI.create("file:/c/test.owl#c7"));
         OWLDescription c8 = df.getOWLClass(URI.create("file:/c/test.owl#c8"));
         OWLDescription c9 = df.getOWLClass(URI.create("file:/c/test.owl#c9"));
         OWLDescription c10 = df.getOWLClass(URI.create("file:/c/test.owl#c10"));
         OWLDescription c11 = df.getOWLClass(URI.create("file:/c/test.owl#c11"));
         OWLDescription c12 = df.getOWLClass(URI.create("file:/c/test.owl#c12"));
         OWLDescription c13 = df.getOWLClass(URI.create("file:/c/test.owl#c13"));
         OWLDescription c14 = df.getOWLClass(URI.create("file:/c/test.owl#c14"));
         OWLDescription c15 = df.getOWLClass(URI.create("file:/c/test.owl#c15"));
         OWLDescription c16 = df.getOWLClass(URI.create("file:/c/test.owl#c16"));
         OWLDescription c17 = df.getOWLClass(URI.create("file:/c/test.owl#c17"));
         OWLDescription c18 = df.getOWLClass(URI.create("file:/c/test.owl#c18"));
         OWLDescription d = df.getOWLClass(URI.create("file:/c/test.owl#d"));
         OWLObjectProperty r = df.getOWLObjectProperty(URI.create("file:/c/test.owl#r"));
         
         OWLDescription desc = df.getOWLObjectIntersectionOf(
                 df.getOWLObjectSomeRestriction(r, c1), 
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c2)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c3)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c4)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c5)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c6)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c7)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c8)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c9)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c10)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c11)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c12)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c13)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c14)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c15)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c16)),
                 df.getOWLObjectSomeRestriction(r, df.getOWLObjectIntersectionOf(c, c17)),
                 df.getOWLObjectSomeRestriction(r, c18),
                 df.getOWLObjectMaxCardinalityRestriction(r, 1, d)
         );
         
         assertSatisfiable(desc,true);
     }
     
     public void testIanQNRTest() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubObjectPropertyOf(son child)");
         buffer.append("SubObjectPropertyOf(daughter child)");
         buffer.append("EquivalentClasses(A ObjectIntersectionOf(ObjectMinCardinality(2 son male) ObjectMinCardinality(2 daughter ObjectComplementOf(male))))");
         buffer.append("EquivalentClasses(B ObjectMinCardinality(4 child))");
         loadOntologyWithAxioms(buffer.toString(), null);
         assertSubsumedBy("A","B",true);
     }
     
     public void testIanRecursiveDefinitionTest1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(A ObjectIntersectionOf(ObjectSomeValuesFrom(R0 B) ObjectSomeValuesFrom(R1 B) ObjectSomeValuesFrom(R2 B) ObjectSomeValuesFrom(R3 B) ObjectSomeValuesFrom(R4 B) ObjectSomeValuesFrom(R5 B) ObjectSomeValuesFrom(R6 B) ObjectSomeValuesFrom(R7 B) ObjectSomeValuesFrom(R8 B) ObjectSomeValuesFrom(R9 B)))");
         buffer.append("SubClassOf(B ObjectIntersectionOf(ObjectSomeValuesFrom(R0 A) ObjectSomeValuesFrom(R1 A) ObjectSomeValuesFrom(R2 A) ObjectSomeValuesFrom(R3 A) ObjectSomeValuesFrom(R4 A) ObjectSomeValuesFrom(R5 A) ObjectSomeValuesFrom(R6 A) ObjectSomeValuesFrom(R7 A) ObjectSomeValuesFrom(R8 A) ObjectSomeValuesFrom(R9 A)))");
         loadOntologyWithAxioms(buffer.toString(), null);
         assertSatisfiable("A",true);
     }

      public void testIanRecursiveDefinitionTest2() throws Exception {
          StringBuffer buffer = new StringBuffer();
          buffer.append("SubClassOf(A ObjectIntersectionOf(ObjectSomeValuesFrom(R0 B) ObjectSomeValuesFrom(R1 B) ObjectSomeValuesFrom(R2 B) ObjectSomeValuesFrom(R3 B) ObjectSomeValuesFrom(R4 B) ObjectSomeValuesFrom(R5 B) ObjectSomeValuesFrom(R6 B) ObjectSomeValuesFrom(R7 B) ObjectSomeValuesFrom(R8 B) ObjectSomeValuesFrom(R9 B)))");
          buffer.append("SubClassOf(B ObjectIntersectionOf(ObjectSomeValuesFrom(R0 C) ObjectSomeValuesFrom(R1 C) ObjectSomeValuesFrom(R2 C) ObjectSomeValuesFrom(R3 C) ObjectSomeValuesFrom(R4 C) ObjectSomeValuesFrom(R5 C) ObjectSomeValuesFrom(R6 C) ObjectSomeValuesFrom(R7 C) ObjectSomeValuesFrom(R8 C) ObjectSomeValuesFrom(R9 C)))");
          buffer.append("SubClassOf(C ObjectIntersectionOf(ObjectSomeValuesFrom(R0 A) ObjectSomeValuesFrom(R1 A) ObjectSomeValuesFrom(R2 A) ObjectSomeValuesFrom(R3 A) ObjectSomeValuesFrom(R4 A) ObjectSomeValuesFrom(R5 A) ObjectSomeValuesFrom(R6 A) ObjectSomeValuesFrom(R7 A) ObjectSomeValuesFrom(R8 A) ObjectSomeValuesFrom(R9 A)))");
          loadOntologyWithAxioms(buffer.toString(), null);
          assertSatisfiable("A",true);
      }
     public void testIanRecursiveDefinitionTest3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(A ObjectIntersectionOf(ObjectSomeValuesFrom(R0 B) ObjectSomeValuesFrom(R1 B) ObjectSomeValuesFrom(R2 B) ObjectSomeValuesFrom(R3 B) ObjectSomeValuesFrom(R4 B) ObjectSomeValuesFrom(R5 B) ObjectSomeValuesFrom(R6 B) ObjectSomeValuesFrom(R7 B) ObjectSomeValuesFrom(R8 B) ObjectSomeValuesFrom(R9 B)))");
         buffer.append("SubClassOf(B ObjectIntersectionOf(ObjectSomeValuesFrom(R0 C) ObjectSomeValuesFrom(R1 C) ObjectSomeValuesFrom(R2 C) ObjectSomeValuesFrom(R3 C) ObjectSomeValuesFrom(R4 C) ObjectSomeValuesFrom(R5 C) ObjectSomeValuesFrom(R6 C) ObjectSomeValuesFrom(R7 C) ObjectSomeValuesFrom(R8 C) ObjectSomeValuesFrom(R9 C)))");
         buffer.append("SubClassOf(C ObjectIntersectionOf(ObjectSomeValuesFrom(R0 D) ObjectSomeValuesFrom(R1 D) ObjectSomeValuesFrom(R2 D) ObjectSomeValuesFrom(R3 D) ObjectSomeValuesFrom(R4 D) ObjectSomeValuesFrom(R5 D) ObjectSomeValuesFrom(R6 D) ObjectSomeValuesFrom(R7 D) ObjectSomeValuesFrom(R8 D) ObjectSomeValuesFrom(R9 D)))");
         buffer.append("SubClassOf(D ObjectIntersectionOf(ObjectSomeValuesFrom(R0 A) ObjectSomeValuesFrom(R1 A) ObjectSomeValuesFrom(R2 A) ObjectSomeValuesFrom(R3 A) ObjectSomeValuesFrom(R4 A) ObjectSomeValuesFrom(R5 A) ObjectSomeValuesFrom(R6 A) ObjectSomeValuesFrom(R7 A) ObjectSomeValuesFrom(R8 A) ObjectSomeValuesFrom(R9 A)))");
         loadOntologyWithAxioms(buffer.toString(), null);
         assertSatisfiable("A",true);
     }
     public void testIanBackjumping1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(C1 ObjectIntersectionOf(" +
         		"ObjectUnionOf(A0 B0)" +
         		"ObjectUnionOf(A1 B1)" +
         		"ObjectUnionOf(A2 B2)" +
         		"ObjectUnionOf(A3 B3)" +
         		"ObjectUnionOf(A4 B4)" +
         		"ObjectUnionOf(A5 B5)" +
         		"ObjectUnionOf(A6 B6)" +
         		"ObjectUnionOf(A7 B7)" +
         		"ObjectUnionOf(A8 B8)" +
         		"ObjectUnionOf(A9 B9)" +
         		"ObjectUnionOf(A10 B10)" +
         		"ObjectUnionOf(A11 B11)" +
         		"ObjectUnionOf(A12 B12)" +
         		"ObjectUnionOf(A13 B13)" +
         		"ObjectUnionOf(A14 B14)" +
         		"ObjectUnionOf(A15 B15)" +
         		"ObjectUnionOf(A16 B16)" +
         		"ObjectUnionOf(A17 B17)" +
         		"ObjectUnionOf(A18 B18)" +
         		"ObjectUnionOf(A19 B19)" +
         		"ObjectUnionOf(A20 B20)" +
         		"ObjectUnionOf(A21 B21)" +
         		"ObjectUnionOf(A22 B22)" +
         		"ObjectUnionOf(A23 B23)" +
         		"ObjectUnionOf(A24 B24)" +
         		"ObjectUnionOf(A25 B25)" +
         		"ObjectUnionOf(A26 B26)" +
         		"ObjectUnionOf(A27 B27)" +
         		"ObjectUnionOf(A28 B28)" +
         		"ObjectUnionOf(A29 B29)" +
         		"ObjectUnionOf(A30 B30)" +
         		"ObjectUnionOf(A31 B31)" +
         		"))");
         buffer.append("SubClassOf(C2 ObjectIntersectionOf(ObjectUnionOf(A B) ObjectUnionOf(A ObjectComplementOf(B))))");
         buffer.append("SubClassOf(C3 ObjectIntersectionOf(ObjectUnionOf(ObjectComplementOf(A) B) ObjectUnionOf(ObjectComplementOf(A) ObjectComplementOf(B))))");
         buffer.append("SubClassOf(C4 ObjectSomeValuesFrom(R C2))");
         buffer.append("SubClassOf(C5 ObjectAllValuesFrom(R C3))");
         buffer.append("SubClassOf(test ObjectIntersectionOf(C1 C4 C5))");
         loadOntologyWithAxioms(buffer.toString(), null);
         assertSatisfiable("test",false);
     }
     
     public void testIanBackjumping2() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(C2 ObjectIntersectionOf(ObjectUnionOf(A B) ObjectUnionOf(A ObjectComplementOf(B))))");
         buffer.append("SubClassOf(C3 ObjectIntersectionOf(ObjectUnionOf(ObjectComplementOf(A) B) ObjectUnionOf(ObjectComplementOf(A) ObjectComplementOf(B))))");
         buffer.append("SubClassOf(C4 ObjectSomeValuesFrom(R ObjectIntersectionOf(C2 C8)))");
         buffer.append("SubClassOf(C5 ObjectAllValuesFrom(R ObjectIntersectionOf(C3 C9)))");
         buffer.append("SubClassOf(C6 ObjectSomeValuesFrom(R ObjectIntersectionOf(C2 C10)))");
         buffer.append("SubClassOf(C7 ObjectAllValuesFrom(R ObjectIntersectionOf(C3 C11)))");
         buffer.append("SubClassOf(test ObjectIntersectionOf(" +
         		"ObjectUnionOf(A0 B0) " +
         		"ObjectUnionOf(A1 B1)" +
         		"ObjectUnionOf(A2 B2)" +
                        "ObjectUnionOf(A3 B3)" +
                        "ObjectUnionOf(A4 B4)" +
                        "ObjectUnionOf(A5 B5)" +
                        "ObjectUnionOf(A6 B6)" +
                        "ObjectUnionOf(A7 B7)" +
                        "ObjectUnionOf(A8 B8)" +
                        "ObjectUnionOf(A9 B9)" +
                        "ObjectUnionOf(A10 B10)" +
                        "ObjectUnionOf(A11 B11)" +
                        "ObjectUnionOf(A12 B12)" +
                        "ObjectUnionOf(A13 B13)" +
                        "ObjectUnionOf(A14 B14)" +
                        "ObjectUnionOf(A15 B15)" +
                        "ObjectUnionOf(A16 B16)" +
                        "ObjectUnionOf(A17 B17)" +
                        "ObjectUnionOf(A18 B18)" +
                        "ObjectUnionOf(A19 B19)" +
                        "ObjectUnionOf(A20 B20)" +
                        "ObjectUnionOf(A21 B21)" +
                        "ObjectUnionOf(A22 B22)" +
                        "ObjectUnionOf(A23 B23)" +
                        "ObjectUnionOf(A24 B24)" +
                        "ObjectUnionOf(A25 B25)" +
                        "ObjectUnionOf(A26 B26)" +
                        "ObjectUnionOf(A27 B27)" +
                        "ObjectUnionOf(A28 B28)" +
                        "ObjectUnionOf(A29 B29)" +
                        "ObjectUnionOf(A30 B30)" +
                        "ObjectUnionOf(A31 B31)" +
         		"))");
         loadOntologyWithAxioms(buffer.toString(), null);
         assertSatisfiable("test",true);
     }
     public void testIanBackjumping3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(C2 ObjectIntersectionOf(ObjectUnionOf(A B) ObjectUnionOf(A ObjectComplementOf(B))))");
         buffer.append("SubClassOf(C3 ObjectIntersectionOf(ObjectUnionOf(ObjectComplementOf(A) B) ObjectUnionOf(ObjectComplementOf(A) ObjectComplementOf(B))))");
         buffer.append("SubClassOf(C4 ObjectSomeValuesFrom(R ObjectIntersectionOf(C2 C8)))");
         buffer.append("SubClassOf(C5 ObjectAllValuesFrom(R ObjectIntersectionOf(C3 C9)))");
         buffer.append("SubClassOf(C6 ObjectSomeValuesFrom(R ObjectIntersectionOf(C2 C10)))");
         buffer.append("SubClassOf(C7 ObjectAllValuesFrom(R ObjectIntersectionOf(C3 C11)))");
         buffer.append("SubClassOf(test ObjectIntersectionOf(" +
                        "ObjectUnionOf(A0 B0) " +
                        "ObjectUnionOf(A1 B1)" +
                        "ObjectUnionOf(A2 B2)" +
                        "ObjectUnionOf(A3 B3)" +
                        "ObjectUnionOf(A4 B4)" +
                        "ObjectUnionOf(A5 B5)" +
                        "ObjectUnionOf(A6 B6)" +
                        "ObjectUnionOf(A7 B7)" +
                        "ObjectUnionOf(A8 B8)" +
                        "ObjectUnionOf(A9 B9)" +
                        "ObjectUnionOf(A10 B10)" +
                        "ObjectUnionOf(A11 B11)" +
                        "ObjectUnionOf(A12 B12)" +
                        "ObjectUnionOf(A13 B13)" +
                        "ObjectUnionOf(A14 B14)" +
                        "ObjectUnionOf(A15 B15)" +
                        "ObjectUnionOf(A16 B16)" +
                        "ObjectUnionOf(A17 B17)" +
                        "ObjectUnionOf(A18 B18)" +
                        "ObjectUnionOf(A19 B19)" +
                        "ObjectUnionOf(A20 B20)" +
                        "ObjectUnionOf(A21 B21)" +
                        "ObjectUnionOf(A22 B22)" +
                        "ObjectUnionOf(A23 B23)" +
                        "ObjectUnionOf(A24 B24)" +
                        "ObjectUnionOf(A25 B25)" +
                        "ObjectUnionOf(A26 B26)" +
                        "ObjectUnionOf(A27 B27)" +
                        "ObjectUnionOf(A28 B28)" +
                        "ObjectUnionOf(A29 B29)" +
                        "ObjectUnionOf(A30 B30)" +
                        "ObjectUnionOf(A31 B31)" +
                        "ObjectUnionOf(C4 C6)" +
                        "ObjectUnionOf(C5 C7)" +
                        "))"); 
         loadOntologyWithAxioms(buffer.toString(), null);
         assertSatisfiable("test",false);
     }
     
     public void testNominals1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("ClassAssertion(a A) ");
         buffer.append("ClassAssertion(b A) ");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(R A)) ");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(S ObjectOneOf(n))) ");
         loadOntologyWithAxioms(buffer.toString(), null);
         assertABoxSatisfiable(true);
     }
     
     public void testNominals2() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("ClassAssertion(a A) ");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(R A)) ");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(S ObjectOneOf(n))) ");
         buffer.append("ClassAssertion(b B) ");
         buffer.append("SubClassOf(B ObjectSomeValuesFrom(R B)) ");
         buffer.append("SubClassOf(B ObjectSomeValuesFrom(S ObjectOneOf(n))) ");
         buffer.append("DisjointClasses(A B) ");
         buffer.append("ClassAssertion(n ObjectMaxCardinality(5 InverseObjectProperty(S))) ");
         loadOntologyWithAxioms(buffer.toString(), null);
         assertABoxSatisfiable(true);
     }
     public void testNominals3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(R A)) ");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(S ObjectOneOf(n))) ");
         buffer.append("ClassAssertion(a ObjectSomeValuesFrom(R A)) ");
         buffer.append("SubClassOf(B ObjectSomeValuesFrom(R B)) ");
         buffer.append("SubClassOf(B ObjectSomeValuesFrom(S ObjectOneOf(n))) ");
         buffer.append("ClassAssertion(b ObjectSomeValuesFrom(R B)) ");
         buffer.append("ClassAssertion(n ObjectMaxCardinality(1 InverseObjectProperty(S))) ");
         loadOntologyWithAxioms(buffer.toString(), null);
//         addAxiom("[subClassOf A [some R A]]");
//         addAxiom("[subClassOf A [some S [oneOf n]]]");
//         addAxiom("[classMember [some R A] a]");
//         addAxiom("[subClassOf B [some R B]]");
//         addAxiom("[subClassOf B [some S [oneOf n]]]");
//         addAxiom("[classMember [some R B] b]");
//         addAxiom("[classMember [atMost 1 [inv S]] n]");
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription A = df.getOWLClass(URI.create("file:/c/test.owl#A"));
         OWLDescription B = df.getOWLClass(URI.create("file:/c/test.owl#B"));
         OWLObjectProperty S = df.getOWLObjectProperty(URI.create("file:/c/test.owl#S"));
         OWLObjectPropertyExpression invS = df.getOWLObjectPropertyInverse(S);
         OWLObjectProperty R = df.getOWLObjectProperty(URI.create("file:/c/test.owl#R"));
         OWLIndividual n = df.getOWLIndividual(URI.create("file:/c/test.owl#n"));
         
         OWLDescription desc = df.getOWLObjectSomeRestriction(invS, 
                 df.getOWLObjectIntersectionOf(A, B, 
                         df.getOWLObjectSomeRestriction(R, df.getOWLObjectIntersectionOf(A, B))));
         
//         assertInstanceOf("[some [inv S] [and A B [some R [and A B]]]]", "n",true);
         assertInstanceOf(desc,n,true);
     }
     
     public void testNominals4() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(A B) ");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(R A))");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(S ObjectOneOf(n)))");
         buffer.append("ClassAssertion(a ObjectSomeValuesFrom(R A)) ");
         buffer.append("SubClassOf(B ObjectSomeValuesFrom(R B)) ");
         buffer.append("SubClassOf(B ObjectSomeValuesFrom(S ObjectOneOf(n))) ");
         buffer.append("ClassAssertion(b ObjectSomeValuesFrom(R B)) ");
         buffer.append("ClassAssertion(n ObjectMaxCardinality(2 InverseObjectProperty(S))) ");
         loadOntologyWithAxioms(buffer.toString(), null);
         
//         addAxiom("[disjoint A B]");
//         addAxiom("[subClassOf A [some R A]]");
//         addAxiom("[subClassOf A [some S [oneOf n]]]");
//         addAxiom("[classMember [some R A] a]");
//         addAxiom("[subClassOf B [some R B]]");
//         addAxiom("[subClassOf B [some S [oneOf n]]]");
//         addAxiom("[classMember [some R B] b]");
//         addAxiom("[classMember [atMost 2 [inv S]] n]");
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription A = df.getOWLClass(URI.create("file:/c/test.owl#A"));
         OWLDescription B = df.getOWLClass(URI.create("file:/c/test.owl#B"));
         OWLObjectProperty S = df.getOWLObjectProperty(URI.create("file:/c/test.owl#S"));
         OWLObjectPropertyExpression invS = df.getOWLObjectPropertyInverse(S);
         OWLObjectProperty R = df.getOWLObjectProperty(URI.create("file:/c/test.owl#R"));
         OWLIndividual n = df.getOWLIndividual(URI.create("file:/c/test.owl#n"));
         
         OWLDescription desc = df.getOWLObjectSomeRestriction(invS, 
                 df.getOWLObjectIntersectionOf(A, 
                         df.getOWLObjectSomeRestriction(R, A)));
         assertInstanceOf(desc, n, true);
//         assertInstanceOf("[some [inv S] [and A [some R A]]]","n",true);
         desc = df.getOWLObjectSomeRestriction(invS, 
                 df.getOWLObjectIntersectionOf(B, 
                         df.getOWLObjectSomeRestriction(R, B)));
         assertInstanceOf(desc, n, true);
//         assertInstanceOf("[some [inv S] [and B [some R B]]]","n",true);
     }
     
     public void testNominals5() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(A B) ");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(R A))");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(S ObjectOneOf(n)))");
         buffer.append("ClassAssertion(a ObjectSomeValuesFrom(R A)) ");
         buffer.append("SubClassOf(B ObjectSomeValuesFrom(R B)) ");
         buffer.append("SubClassOf(B ObjectSomeValuesFrom(S ObjectOneOf(n))) ");
         buffer.append("ClassAssertion(b ObjectSomeValuesFrom(R B)) ");
         buffer.append("ClassAssertion(n ObjectMaxCardinality(2 InverseObjectProperty(S))) ");
         loadOntologyWithAxioms(buffer.toString(), null);
         
//         addAxiom("[disjoint A B]");
//         addAxiom("[subClassOf A [some R A]]");
//         addAxiom("[subClassOf A [some S [oneOf n]]]");
//         addAxiom("[classMember [some R A] a]");
//         addAxiom("[subClassOf B [some R B]]");
//         addAxiom("[subClassOf B [some S [oneOf n]]]");
//         addAxiom("[classMember [some R B] b]");
//         addAxiom("[classMember [atMost 2 [inv S]] n]");
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription A = df.getOWLClass(URI.create("file:/c/test.owl#A"));
         OWLDescription B = df.getOWLClass(URI.create("file:/c/test.owl#B"));
         OWLObjectProperty S = df.getOWLObjectProperty(URI.create("file:/c/test.owl#S"));
         OWLObjectPropertyExpression invS = df.getOWLObjectPropertyInverse(S);
         OWLIndividual n = df.getOWLIndividual(URI.create("file:/c/test.owl#n"));
         
         OWLDescription desc = df.getOWLObjectMinCardinalityRestriction(invS, 2, df.getOWLObjectUnionOf(A, B));
         assertInstanceOf(desc, n, true);
         //assertInstanceOf("[atLeast 2 [inv S] [or A B]]","n",true);
     }
     public void testNominals6() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(A B) ");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(R A))");
         buffer.append("SubClassOf(A ObjectSomeValuesFrom(S ObjectOneOf(n)))");
         buffer.append("ClassAssertion(a ObjectSomeValuesFrom(R A)) ");
         buffer.append("SubClassOf(B ObjectSomeValuesFrom(R B)) ");
         buffer.append("SubClassOf(B ObjectSomeValuesFrom(S ObjectOneOf(n))) ");
         buffer.append("ClassAssertion(b ObjectSomeValuesFrom(R B)) ");
         buffer.append("ClassAssertion(n ObjectMaxCardinality(2 InverseObjectProperty(S))) ");
         loadOntologyWithAxioms(buffer.toString(), null);
         
//         addAxiom("[disjoint A B]");
//         addAxiom("[subClassOf A [some R A]]");
//         addAxiom("[subClassOf A [some S [oneOf n]]]");
//         addAxiom("[classMember [some R A] a]");
//         addAxiom("[subClassOf B [some R B]]");
//         addAxiom("[subClassOf B [some S [oneOf n]]]");
//         addAxiom("[classMember [some R B] b]");
//         addAxiom("[classMember [atMost 2 [inv S]] n]");
         
         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
         OWLDescription A = df.getOWLClass(URI.create("file:/c/test.owl#A"));
         OWLObjectProperty S = df.getOWLObjectProperty(URI.create("file:/c/test.owl#S"));
         OWLObjectPropertyExpression invS = df.getOWLObjectPropertyInverse(S);
         OWLIndividual n = df.getOWLIndividual(URI.create("file:/c/test.owl#n"));
         
         OWLDescription desc = df.getOWLObjectMinCardinalityRestriction(invS, 1, df.getOWLObjectComplementOf(A));
         assertInstanceOf(desc, n, true);
//       assertInstanceOf("[atLeast 1 [inv S] [not A]]","n",true);
         desc = df.getOWLObjectMinCardinalityRestriction(invS, 2, df.getOWLObjectComplementOf(A));
         assertInstanceOf(desc, n, false);
//         assertInstanceOf("[atLeast 2 [inv S] [not A]]","n",false);
     }
     
    public void testDependencyDisjunctionMergingBug() throws Exception {
        loadOntologyFromResource("../res/dependency-disjuntion-merging-bug.xml", null);
        assertSubsumedBy(
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Anjou",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#FullBodiedWine",
                false);
    }
    
    public void testNovelNominals() throws Exception {
        // Uncomment this once complex concept classification is supported properly
        OWLDataFactory df = m_ontologyManager.getOWLDataFactory();
        String axioms = "ClassAssertion(a C)";
        loadOntologyWithAxioms(axioms, null);
        OWLIndividual a = df.getOWLIndividual(URI.create("file:/c/test.owl#a"));
        OWLClass c = df.getOWLClass(URI.create("file:/c/test.owl#C"));
        OWLDescription desc = m_ontologyManager.getOWLDataFactory().getOWLObjectIntersectionOf(
            df.getOWLObjectOneOf(a),
            df.getOWLObjectComplementOf(c));

        assertFalse(m_reasoner.isClassSatisfiable(desc));
    }
}
