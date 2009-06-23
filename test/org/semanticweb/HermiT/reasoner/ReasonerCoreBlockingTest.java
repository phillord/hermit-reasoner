package org.semanticweb.HermiT.reasoner;

import org.semanticweb.owl.model.IRI;
import org.semanticweb.owl.model.OWLClassExpression;
import org.semanticweb.owl.model.OWLObjectProperty;


public class ReasonerCoreBlockingTest extends AbstractReasonerTest {

    public ReasonerCoreBlockingTest(String name) {
        super(name);
    }
//    public void testExpansion() throws Exception {
//        String axioms = "SubClassOf(:A :B)"
//            + "SubClassOf(:B ObjectSomeValuesFrom(:r :C))"
//            + "SubClassOf(:C :D)"
//            + "SubClassOf(:D ObjectSomeValuesFrom(:r :E))"
//            + "SubClassOf(:E :F)"
//            + "SubClassOf(:F ObjectSomeValuesFrom(:r :G))"
//            + "SubClassOf(:G :H)"
//            + "SubClassOf(:H ObjectSomeValuesFrom(:r :I))"
//            + "SubClassOf(:I owl:Nothing)"
//            + "ClassAssertion(:A :a)";
//        loadReasonerWithAxioms(axioms);
//        createCoreBlockingReasoner();
//        assertABoxSatisfiable(false);
//    }
    public void testWidmann1() throws Exception {
        String axioms = "SubClassOf(owl:Thing ObjectSomeValuesFrom(:r :C)) "
                + "SubClassOf(owl:Thing ObjectSomeValuesFrom(:s ObjectAllValuesFrom(:r ObjectSomeValuesFrom(:r ObjectComplementOf(:C))))) "
                + "InverseObjectProperties(:r :r-)"
                + "InverseObjectProperties(:s :s-)"
                + "SubClassOf(owl:Thing ObjectAllValuesFrom(:r- ObjectAllValuesFrom(:r- ObjectAllValuesFrom(:s ObjectAllValuesFrom(:s- :C))))) ";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }
    
    public void testWidmann2() throws Exception {
        // <r>q; 
        // <r->[r-][r][r][r]p 
        String axioms = "SubClassOf(owl:Thing ObjectSomeValuesFrom(:r :q)) "
            + "InverseObjectProperties(:r :r-)"
            + "SubClassOf(owl:Thing ObjectSomeValuesFrom(:r- ObjectAllValuesFrom(:r- ObjectAllValuesFrom(:r ObjectAllValuesFrom(:r ObjectAllValuesFrom(:r :p))))))" 
            + "ClassAssertion(ObjectSomeValuesFrom(:r- ObjectComplementOf(:p)) :a)";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
//        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
//        OWLClassExpression p = df.getOWLClass(URI.create("file:/c/test.owl#p"));
//        OWLObjectProperty invr = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));
//
//        OWLClassExpression desc = df.getOWLObjectSomeValuesFrom(invr, df.getOWLObjectComplementOf(p));
//        assertSatisfiable(desc,false);
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
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }

    public void testReflexivity() throws Exception {
        String axioms = "ReflexiveObjectProperty(:r) "
                + "ClassAssertion(ObjectAllValuesFrom(:r " + "owl:Nothing) :a) "
                + "ClassAssertion(owl:Thing :a)";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }

    public void testIrreflexivity() throws Exception {
        String axioms = "IrreflexiveObjectProperty(:r) "
                + "ObjectPropertyAssertion(:r :a :a)";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }

    public void testRoleDisjointness1() throws Exception {
        String axioms = "DisjointObjectProperties(:r :s :t) "
                + "ObjectPropertyAssertion(:r :a :b) "
                + "ObjectPropertyAssertion(:s :a :b)";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }

    public void testRoleDisjointness2() throws Exception {
        String axioms = "DisjointObjectProperties(:r :s) "
                + "ClassAssertion(ObjectSomeValuesFrom(:r owl:Thing) :a) "
                + "ClassAssertion(ObjectSomeValuesFrom(:s owl:Thing) :a) "
                + "ClassAssertion(:C :a) "
                + "SubClassOf(:C ObjectMaxCardinality(1 :f)) "
                + "SubObjectPropertyOf(:r :f) " + "SubObjectPropertyOf(:s :f)";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }

    public void testExistsSelf1() throws Exception {
        String axioms = "ClassAssertion(ObjectAllValuesFrom(:r "
                + "owl:Nothing) :a) " + "ClassAssertion(ObjectHasSelf(:r) :a)";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
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
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(true);
    }

    public void testAsymmetry() throws Exception {
        String axioms = "AsymmetricObjectProperty(:as) "
                + "SubObjectPropertyOf(:r :as) "
                + "ObjectPropertyAssertion(:as :b :a) "
                + "ObjectPropertyAssertion(:r :a :b)";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability1() throws Exception {
        String axioms = "ClassAssertion(:C :a) "
                + "ClassAssertion(ObjectComplementOf(:C) :a)";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability2() throws Exception {
        String axioms = "SubClassOf(owl:Thing :C) " + "SubClassOf(owl:Thing "
                + "ObjectComplementOf(:C))";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability3() throws Exception {
        String axioms = "SubClassOf(:Person "
                + "ObjectSomeValuesFrom(:hasParent :Person)) "
                + "SubClassOf(ObjectSomeValuesFrom(:hasParent "
                + "ObjectSomeValuesFrom(:hasParent :Person)) " + " :Grandchild) "
                + "ClassAssertion(:Person :peter) " + "ClassAssertion("
                + "ObjectComplementOf(:Grandchild) :peter)";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }

    public void testSatisfiability4() throws Exception {
        String axioms = "FunctionalObjectProperty(:R) "
                + "ObjectPropertyAssertion(:R :a :b) "
                + "SubClassOf(owl:Thing ObjectSomeValuesFrom(:R :C)) "
                + "ClassAssertion(ObjectComplementOf(:C) :b)";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }

    public void testChanges() throws Exception {
        String axioms = "SubClassOf(owl:Thing :C)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
        axioms = "SubClassOf(owl:Thing :C) "
                + "SubClassOf(owl:Thing ObjectComplementOf(:C))";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertABoxSatisfiable(false);
    }

    public void testSubsumption1() throws Exception {
        String axioms = "SubClassOf(:Person :Animal) "
                + "SubClassOf(:Student :Person) " + "SubClassOf(:Dog :Animal)";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertSubsumedBy("Student", "Animal", true);
        assertSubsumedBy("Animal", "Student", false);
        assertSubsumedBy("Student", "Dog", false);
        assertSubsumedBy("Dog", "Student", false);
    }

    public void testSubsumption2() throws Exception {
        String axioms = "SubObjectPropertyOf(:R :S) "
                + "EquivalentClasses(:A ObjectSomeValuesFrom(:R :C)) "
                + "EquivalentClasses(:B ObjectSomeValuesFrom(:S :C))";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
        assertSubsumedBy("A", "B", true);
        assertSubsumedBy("B", "A", false);
    }

    public void testSubsumption3() throws Exception {
        String axioms = "EquivalentObjectProperties(:R :S) "
                + "EquivalentClasses(:A ObjectSomeValuesFrom(:R :C)) "
                + "EquivalentClasses(:B ObjectSomeValuesFrom(:S :C))";
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
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
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
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
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
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
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
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
        loadOntologyWithAxioms(axioms);
        createCoreBlockingReasoner();
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
        loadOntologyWithAxioms(buffer.toString());
        createCoreBlockingReasoner();
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         assertSubsumedBy("A","B",true);
     }
     public void testHeinsohnTBox4a() throws Exception {
         // Tests role restrictions
         loadOntologyWithAxioms("SubClassOf(owl:Thing ObjectIntersectionOf(ObjectIntersectionOf(ObjectAllValuesFrom(:r :D) ObjectAllValuesFrom(:r ObjectUnionOf(ObjectComplementOf(:D) :E))) ObjectComplementOf(ObjectAllValuesFrom(:r :E))))");
         createCoreBlockingReasoner();
         assertABoxSatisfiable(false);
//         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
//         OWLClassExpression D = df.getOWLClass(IRI.create("file:/c/test.owl#D"));
//         OWLClassExpression E = df.getOWLClass(IRI.create("file:/c/test.owl#E"));
//         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
//         OWLClassExpression desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectAllValuesFrom(r, D), df.getOWLObjectAllValuesFrom(r, df.getOWLObjectUnionOf(df.getOWLObjectComplementOf(D), E)));
//         OWLClassExpression desc2 = df.getOWLObjectAllValuesFrom(r, E);
//         assertSubsumedBy(desc1,desc2,true);
     }

     public void testHeinsohnTBox4b() throws Exception {
         // Tests role restrictions
         StringBuffer buffer = new StringBuffer();
         buffer.append("DisjointClasses(:C :D)");
         buffer.append("SubClassOf(owl:Thing ObjectIntersectionOf(ObjectIntersectionOf(ObjectAllValuesFrom(:r ObjectUnionOf(ObjectComplementOf(ObjectMinCardinality(2 :s owl:Thing)) :C)) ObjectAllValuesFrom(:r :D)) ObjectComplementOf( ObjectAllValuesFrom(:r ObjectMaxCardinality(1 :s owl:Thing)) )))");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         assertABoxSatisfiable(false);
//         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
//         OWLClassExpression C = df.getOWLClass(IRI.create("file:/c/test.owl#C"));
//         OWLClassExpression D = df.getOWLClass(IRI.create("file:/c/test.owl#D"));
//         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
//         OWLObjectProperty s = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#s"));
//         OWLClassExpression desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectAllValuesFrom(r, df.getOWLObjectUnionOf(df.getOWLObjectComplementOf(df.getOWLObjectMinCardinality(s, 2)), C)), df.getOWLObjectAllValuesFrom(r, D));
//         OWLClassExpression desc2 = df.getOWLObjectAllValuesFrom(r, df.getOWLObjectMaxCardinality(s, 1));
//         assertSubsumedBy(desc1,desc2,true);
     }
     
      public void testHeinsohnTBox7() throws Exception {
          // Tests inverse roles
          loadOntologyWithAxioms("InverseObjectProperties(:r :r-) SubClassOf(owl:Thing ObjectIntersectionOf( ObjectIntersectionOf(ObjectAllValuesFrom(:r ObjectAllValuesFrom( :r- :A)) ObjectSomeValuesFrom(:r owl:Thing)) ObjectComplementOf(:A) ))");
          createCoreBlockingReasoner();
          assertABoxSatisfiable(false);
//          OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
//          OWLClassExpression A = df.getOWLClass(IRI.create("file:/c/test.owl#A"));
//          OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
//          OWLObjectPropertyExpression invr = df.getOWLObjectInverseOf(r);
//          OWLClassExpression desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectAllValuesFrom(r, df.getOWLObjectAllValuesFrom(invr, A)), df.getOWLObjectSomeValuesFrom(r, df.getOWLThing()));
//          assertSubsumedBy(desc1,A,true);
     }
      
     public void testIanT1a() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:p1 ObjectComplementOf(ObjectUnionOf(:p2 :p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p2 ObjectComplementOf(ObjectUnionOf(:p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p3 ObjectComplementOf(ObjectUnionOf(:p4 :p5)))");
         buffer.append("SubClassOf(:p4 ObjectComplementOf(:p5))");
         buffer.append("ClassAssertion(ObjectIntersectionOf(ObjectSomeValuesFrom(:r :p1) ObjectSomeValuesFrom(:r :p2) ObjectSomeValuesFrom(:r :p3) ObjectMaxCardinality(2 :r owl:Thing)) :a)");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         assertABoxSatisfiable(false);
//         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
//         OWLClassExpression p1 = df.getOWLClass(IRI.create("file:/c/test.owl#p1"));
//         OWLClassExpression p2 = df.getOWLClass(IRI.create("file:/c/test.owl#p2"));
//         OWLClassExpression p3 = df.getOWLClass(IRI.create("file:/c/test.owl#p3"));
//         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
//
//         OWLClassExpression desc1 = df.getOWLObjectIntersectionOf(df.getOWLObjectSomeValuesFrom(r, p1), df.getOWLObjectSomeValuesFrom(r, p2), df.getOWLObjectSomeValuesFrom(r, p3), df.getOWLObjectMaxCardinality(r, 2));
//         assertSatisfiable(desc1,false);
     }
     public void testIanT1b() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:p1 ObjectComplementOf(ObjectUnionOf(:p2 :p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p2 ObjectComplementOf(ObjectUnionOf(:p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p3 ObjectComplementOf(ObjectUnionOf(:p4 :p5)))");
         buffer.append("SubClassOf(:p4 ObjectComplementOf(:p5))");
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("ClassAssertion(ObjectSomeValuesFrom(:r- ObjectIntersectionOf(ObjectSomeValuesFrom(:r :p1) ObjectMaxCardinality(1 :r :p1))) :a)");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         assertABoxSatisfiable(true);
//         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
//         OWLClassExpression p1 = df.getOWLClass(IRI.create("file:/c/test.owl#p1"));
//         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
//         OWLObjectPropertyExpression invr = df.getOWLObjectInverseOf(r);
//         
//         OWLClassExpression desc1 = df.getOWLObjectSomeValuesFrom(invr, df.getOWLObjectIntersectionOf(df.getOWLObjectSomeValuesFrom(r, p1), df.getOWLObjectMaxCardinality(r, 1, p1)));
//         assertSatisfiable(desc1,true);
     }
     public void testIanT1c() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:p1 ObjectComplementOf(ObjectUnionOf(:p2 :p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p2 ObjectComplementOf(ObjectUnionOf(:p3 :p4 :p5)))");
         buffer.append("SubClassOf(:p3 ObjectComplementOf(ObjectUnionOf(:p4 :p5)))");
         buffer.append("SubClassOf(:p4 ObjectComplementOf(:p5))");
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("ClassAssertion(ObjectIntersectionOf(:p2 ObjectSomeValuesFrom(:r- ObjectIntersectionOf(ObjectSomeValuesFrom(:r :p1) ObjectMaxCardinality(1 :r owl:Thing)))) :a)");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         assertABoxSatisfiable(false);
//         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
//         OWLClassExpression p1 = df.getOWLClass(IRI.create("file:/c/test.owl#p1"));
//         OWLClassExpression p2 = df.getOWLClass(IRI.create("file:/c/test.owl#p2"));
//         OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
//         OWLObjectPropertyExpression invr = df.getOWLObjectInverseOf(r);
//         
//         OWLClassExpression desc1 = df.getOWLObjectIntersectionOf(p2, df.getOWLObjectSomeValuesFrom(invr, df.getOWLObjectIntersectionOf(df.getOWLObjectSomeValuesFrom(r, p1), df.getOWLObjectMaxCardinality(r, 1))));
//         assertSatisfiable(desc1,false);
     }

     public void testIanT5() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("InverseObjectProperties(:f :f-)");
         buffer.append("TransitiveObjectProperty(:r)");
         buffer.append("SubObjectPropertyOf(:f :r)");
         buffer.append("FunctionalObjectProperty(:f)");
         buffer.append("ClassAssertion(ObjectIntersectionOf(ObjectComplementOf(:a) ObjectSomeValuesFrom(:f- :a) ObjectAllValuesFrom(:r- ObjectSomeValuesFrom(:f- :a) )) :b)");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         assertABoxSatisfiable(true);
//         OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
//         OWLClassExpression a = df.getOWLClass(IRI.create("file:/c/test.owl#a"));
//         OWLObjectProperty invr = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));
//         OWLObjectProperty invf = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#f-"));
//      
//         OWLClassExpression desc = df.getOWLObjectIntersectionOf(
//              df.getOWLObjectComplementOf(a), 
//              df.getOWLObjectSomeValuesFrom(invf, a), 
//              df.getOWLObjectAllValuesFrom(invr, df.getOWLObjectSomeValuesFrom(invf, a))
//          );  
//         assertSatisfiable(desc,true);
     }
     public void testIanT6() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("InverseObjectProperties(:f :f-)");
         buffer.append("TransitiveObjectProperty(:r)");
         buffer.append("SubObjectPropertyOf(:f :r)");
         buffer.append("FunctionalObjectProperty(:f)");
         buffer.append("EquivalentClasses(:d ObjectIntersectionOf(:c ObjectSomeValuesFrom(:f ObjectComplementOf(:c))))");
         buffer.append("ClassAssertion(ObjectIntersectionOf(ObjectComplementOf(:c) ObjectSomeValuesFrom(:f- :d) ObjectAllValuesFrom(:r- ObjectSomeValuesFrom(:f- :d))) :a)");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         assertABoxSatisfiable(false);
//         OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c"));
//         OWLClassExpression d = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#d"));
//         OWLObjectProperty invr = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));
//         OWLObjectProperty invf = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f-"));
//         
//         OWLClassExpression desc = m_dataFactory.getOWLObjectIntersectionOf(
//             m_dataFactory.getOWLObjectComplementOf(c), 
//             m_dataFactory.getOWLObjectSomeValuesFrom(invf, d), 
//             m_dataFactory.getOWLObjectAllValuesFrom(invr, m_dataFactory.getOWLObjectSomeValuesFrom(invf, d))
//         );  
//         assertSatisfiable(desc,false);
     }
     
     public void testIanT7a() throws Exception {
         StringBuffer buffer=new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("InverseObjectProperties(:f :f-)");
         buffer.append("TransitiveObjectProperty(:r)");
         buffer.append("FunctionalObjectProperty(:f)");
         buffer.append("ClassAssertion(ObjectIntersectionOf(:p1 ObjectSomeValuesFrom(:r ObjectSomeValuesFrom(:r ObjectIntersectionOf(:p1 ObjectAllValuesFrom(:r- ObjectComplementOf(:p1)))))) :a)");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();

//         OWLClassExpression p1=m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p1"));
//         OWLObjectProperty r=m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
//         OWLObjectProperty invr=m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));
//
//         OWLClassExpression desc=
//             m_dataFactory.getOWLObjectIntersectionOf(
//             p1,
//             m_dataFactory.getOWLObjectSomeValuesFrom(r,
//                     m_dataFactory.getOWLObjectSomeValuesFrom(r,
//                         m_dataFactory.getOWLObjectIntersectionOf(
//                         p1,
//                         m_dataFactory.getOWLObjectAllValuesFrom(invr,
//                                 m_dataFactory.getOWLObjectComplementOf(p1)
//                         )
//                     )
//                 )
//             )
//         );
         // [and p1 [some r [some r [and p1 [all r- [not p1]]]]]]
         assertABoxSatisfiable(false);
//         assertSatisfiable(desc,false);
     }
     
     public void testIanT7b() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("InverseObjectProperties(:f :f-)");
         buffer.append("TransitiveObjectProperty(:r)");
         buffer.append("FunctionalObjectProperty(:f)");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
     
     public void testIanT9a() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:successor :successor-)");
         buffer.append("TransitiveObjectProperty(:descendant)");
         buffer.append("SubObjectPropertyOf(:successor :descendant)");
         buffer.append("InverseFunctionalObjectProperty(:successor)");
         buffer.append("SubClassOf(:root ObjectComplementOf(ObjectSomeValuesFrom(:successor- owl:Thing)))");
         buffer.append("SubClassOf(:Infinite-Tree-Node ObjectIntersectionOf(:node ObjectSomeValuesFrom(:successor :Infinite-Tree-Node)))");
         buffer.append("SubClassOf(:Infinite-Tree-Root ObjectIntersectionOf(:Infinite-Tree-Node :root))");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         assertSatisfiable("file:/c/test.owl#Infinite-Tree-Root",true);
     }
     public void testIanT9b() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:successor :successor-)");
         buffer.append("TransitiveObjectProperty(:descendant)");
         buffer.append("SubObjectPropertyOf(:successor :descendant)");
         buffer.append("InverseFunctionalObjectProperty(:successor)");
         buffer.append("SubClassOf(:root ObjectComplementOf(ObjectSomeValuesFrom(:successor- owl:Thing)))");
         buffer.append("SubClassOf(:Infinite-Tree-Node ObjectIntersectionOf(:node ObjectSomeValuesFrom(:successor :Infinite-Tree-Node)))");
         buffer.append("SubClassOf(:Infinite-Tree-Root ObjectIntersectionOf(:Infinite-Tree-Node :root))");
         buffer.append("ClassAssertion(ObjectIntersectionOf(:Infinite-Tree-Root ObjectAllValuesFrom(:descendant ObjectSomeValuesFrom(:successor- :root))) :a)");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         assertABoxSatisfiable(false);
         
//         OWLClassExpression itr = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#Infinite-Tree-Root"));
//         OWLClassExpression root = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#root"));
//         OWLObjectProperty descendant = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#descendant"));
//         OWLObjectProperty invsuccessor = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#successor-"));
//         
//         // [and Infinite-Tree-Root [all descendant [some successor- root]]]
//         OWLClassExpression desc =
//             m_dataFactory.getOWLObjectIntersectionOf(
//                 itr, 
//                 m_dataFactory.getOWLObjectAllValuesFrom(descendant, 
//                     m_dataFactory.getOWLObjectSomeValuesFrom(invsuccessor, root)
//                 )
//             );
//         assertSatisfiable(desc,false);
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
         OWLClassExpression p = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#p"));
         OWLObjectProperty s= m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#s"));
         OWLObjectProperty invs = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#s-"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));

         OWLClassExpression desc;
         desc = m_dataFactory.getOWLObjectIntersectionOf(
                     m_dataFactory.getOWLObjectComplementOf(p), 
                     m_dataFactory.getOWLObjectMaxCardinality(r, 1), 
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
                                 m_dataFactory.getOWLObjectMaxCardinality(invr, 1), 
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
                  
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
         assertSubsumedBy("c","d",true);
     }
     
     public void testIanFact3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("FunctionalObjectProperty(:f1)");
         buffer.append("FunctionalObjectProperty(:f2)");
         buffer.append("FunctionalObjectProperty(:f3)");
         buffer.append("SubObjectPropertyOf(:f3 :f1)");
         buffer.append("SubObjectPropertyOf(:f3 :f2)");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
         
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
        loadOntologyWithAxioms(buffer.toString());
        createCoreBlockingReasoner();
        

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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
         OWLClassExpression a = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a"));
         OWLClassExpression b = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#b"));
         OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c"));
         OWLClassExpression d = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#d"));
         
         OWLClassExpression desc = m_dataFactory.getOWLObjectIntersectionOf(m_dataFactory.getOWLObjectComplementOf(c), a, m_dataFactory.getOWLObjectComplementOf(b), d);
         assertSatisfiable(desc,false);
     }
     public void testIanBug3() throws Exception {
         // slow, but works!
         loadOntologyWithAxioms("");
         createCoreBlockingReasoner();
         
         
         OWLClassExpression a = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#a"));
         OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c"));
         OWLClassExpression d = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#d"));
         OWLClassExpression e = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#e"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         
         OWLClassExpression desc = m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, a), 
                 m_dataFactory.getOWLObjectMinCardinality(r, 3, c), 
                 m_dataFactory.getOWLObjectMinCardinality(r, 3, d),
                 m_dataFactory.getOWLObjectMinCardinality(r, 2, m_dataFactory.getOWLObjectIntersectionOf(
                         e, 
                         m_dataFactory.getOWLObjectComplementOf(m_dataFactory.getOWLObjectIntersectionOf(c, d)))), 
                 m_dataFactory.getOWLObjectMaxCardinality(r, 4), 
                 m_dataFactory.getOWLObjectMaxCardinality(r, 2, m_dataFactory.getOWLObjectIntersectionOf(c, d)) 
         );
         assertSatisfiable(desc,true);
     }
     
     public void testIanBug4() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("SubObjectPropertyOf(:r :r-)");
         buffer.append("TransitiveObjectProperty(:r)");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
         OWLClassExpression A = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#A"));
         OWLClassExpression B = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#B"));
         OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r"));
         
         // [and [some r A] [atMost 1 r A] [some r B] [atMost 1 r B]]
         OWLClassExpression desc =
             m_dataFactory.getOWLObjectIntersectionOf(
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, A), 
                 m_dataFactory.getOWLObjectMaxCardinality(r, 1, A),
                 m_dataFactory.getOWLObjectSomeValuesFrom(r, B),
                 m_dataFactory.getOWLObjectMaxCardinality(r, 1, B)
             );
         assertSatisfiable(desc,true);
     }
     public void testIanBug8() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:X ObjectComplementOf(:Y))");
         buffer.append("SubClassOf(:A ObjectIntersectionOf(ObjectMinCardinality(1 :r :X) ObjectMaxCardinality(1 :r :X)))");
         buffer.append("SubClassOf(:A ObjectIntersectionOf(ObjectMinCardinality(1 :r :Y) ObjectMaxCardinality(1 :r :Y)))");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         assertSatisfiable("file:/c/test.owl#A",true);
     }
     
     public void testIanMergeTest1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("SubClassOf(:c ObjectSomeValuesFrom(:r ObjectAllValuesFrom(:r- ObjectComplementOf(:d))))");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
                 m_dataFactory.getOWLObjectMaxCardinality(r, 1, d)
         );
         assertSatisfiable(desc,true);
     }
     public void testIanMergeTest2() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("InverseObjectProperties(:r :r-)");
         buffer.append("SubClassOf(:c ObjectSomeValuesFrom(:r ObjectAllValuesFrom(:r- :d)))");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         
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
                 m_dataFactory.getOWLObjectMaxCardinality(r, 1, d)
         );
         
         assertSatisfiable(desc,true);
     }
     
     public void testIanQNRTest() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubObjectPropertyOf(:son :child)");
         buffer.append("SubObjectPropertyOf(:daughter :child)");
         buffer.append("EquivalentClasses(:A ObjectIntersectionOf(ObjectMinCardinality(2 :son :male) ObjectMinCardinality(2 :daughter ObjectComplementOf(:male))))");
         buffer.append("EquivalentClasses(:B ObjectMinCardinality(4 :child))");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         assertSubsumedBy("file:/c/test.owl#A","file:/c/test.owl#B",true);
     }
     
     public void testIanRecursiveDefinitionTest1() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:A ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :B) ObjectSomeValuesFrom(:R1 :B) ObjectSomeValuesFrom(:R2 :B) ObjectSomeValuesFrom(:R3 :B) ObjectSomeValuesFrom(:R4 :B) ObjectSomeValuesFrom(:R5 :B) ObjectSomeValuesFrom(:R6 :B) ObjectSomeValuesFrom(:R7 :B) ObjectSomeValuesFrom(:R8 :B) ObjectSomeValuesFrom(:R9 :B)))");
         buffer.append("SubClassOf(:B ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :A) ObjectSomeValuesFrom(:R1 :A) ObjectSomeValuesFrom(:R2 :A) ObjectSomeValuesFrom(:R3 :A) ObjectSomeValuesFrom(:R4 :A) ObjectSomeValuesFrom(:R5 :A) ObjectSomeValuesFrom(:R6 :A) ObjectSomeValuesFrom(:R7 :A) ObjectSomeValuesFrom(:R8 :A) ObjectSomeValuesFrom(:R9 :A)))");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         assertSatisfiable("file:/c/test.owl#A",true);
     }

      public void testIanRecursiveDefinitionTest2() throws Exception {
          StringBuffer buffer = new StringBuffer();
          buffer.append("SubClassOf(:A ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :B) ObjectSomeValuesFrom(:R1 :B) ObjectSomeValuesFrom(:R2 :B) ObjectSomeValuesFrom(:R3 :B) ObjectSomeValuesFrom(:R4 :B) ObjectSomeValuesFrom(:R5 :B) ObjectSomeValuesFrom(:R6 :B) ObjectSomeValuesFrom(:R7 :B) ObjectSomeValuesFrom(:R8 :B) ObjectSomeValuesFrom(:R9 :B)))");
          buffer.append("SubClassOf(:B ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :C) ObjectSomeValuesFrom(:R1 :C) ObjectSomeValuesFrom(:R2 :C) ObjectSomeValuesFrom(:R3 :C) ObjectSomeValuesFrom(:R4 :C) ObjectSomeValuesFrom(:R5 :C) ObjectSomeValuesFrom(:R6 :C) ObjectSomeValuesFrom(:R7 :C) ObjectSomeValuesFrom(:R8 :C) ObjectSomeValuesFrom(:R9 :C)))");
          buffer.append("SubClassOf(:C ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :A) ObjectSomeValuesFrom(:R1 :A) ObjectSomeValuesFrom(:R2 :A) ObjectSomeValuesFrom(:R3 :A) ObjectSomeValuesFrom(:R4 :A) ObjectSomeValuesFrom(:R5 :A) ObjectSomeValuesFrom(:R6 :A) ObjectSomeValuesFrom(:R7 :A) ObjectSomeValuesFrom(:R8 :A) ObjectSomeValuesFrom(:R9 :A)))");
          loadOntologyWithAxioms(buffer.toString());
          createCoreBlockingReasoner();
          
          assertSatisfiable("file:/c/test.owl#A",true);
      }
     public void testIanRecursiveDefinitionTest3() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("SubClassOf(:A ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :B) ObjectSomeValuesFrom(:R1 :B) ObjectSomeValuesFrom(:R2 :B) ObjectSomeValuesFrom(:R3 :B) ObjectSomeValuesFrom(:R4 :B) ObjectSomeValuesFrom(:R5 :B) ObjectSomeValuesFrom(:R6 :B) ObjectSomeValuesFrom(:R7 :B) ObjectSomeValuesFrom(:R8 :B) ObjectSomeValuesFrom(:R9 :B)))");
         buffer.append("SubClassOf(:B ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :C) ObjectSomeValuesFrom(:R1 :C) ObjectSomeValuesFrom(:R2 :C) ObjectSomeValuesFrom(:R3 :C) ObjectSomeValuesFrom(:R4 :C) ObjectSomeValuesFrom(:R5 :C) ObjectSomeValuesFrom(:R6 :C) ObjectSomeValuesFrom(:R7 :C) ObjectSomeValuesFrom(:R8 :C) ObjectSomeValuesFrom(:R9 :C)))");
         buffer.append("SubClassOf(:C ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :D) ObjectSomeValuesFrom(:R1 :D) ObjectSomeValuesFrom(:R2 :D) ObjectSomeValuesFrom(:R3 :D) ObjectSomeValuesFrom(:R4 :D) ObjectSomeValuesFrom(:R5 :D) ObjectSomeValuesFrom(:R6 :D) ObjectSomeValuesFrom(:R7 :D) ObjectSomeValuesFrom(:R8 :D) ObjectSomeValuesFrom(:R9 :D)))");
         buffer.append("SubClassOf(:D ObjectIntersectionOf(ObjectSomeValuesFrom(:R0 :A) ObjectSomeValuesFrom(:R1 :A) ObjectSomeValuesFrom(:R2 :A) ObjectSomeValuesFrom(:R3 :A) ObjectSomeValuesFrom(:R4 :A) ObjectSomeValuesFrom(:R5 :A) ObjectSomeValuesFrom(:R6 :A) ObjectSomeValuesFrom(:R7 :A) ObjectSomeValuesFrom(:R8 :A) ObjectSomeValuesFrom(:R9 :A)))");
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
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
         loadOntologyWithAxioms(buffer.toString());
         createCoreBlockingReasoner();
         
         assertSatisfiable("file:/c/test.owl#test",false);
     }
}
