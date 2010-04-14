package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Configuration.BlockingSignatureCacheType;
import org.semanticweb.HermiT.Configuration.BlockingStrategyType;
import org.semanticweb.HermiT.Configuration.DirectBlockingType;

public class ReasonerCoreBlockingTest extends ReasonerTest {

    public ReasonerCoreBlockingTest(String name) {
        super(name);
    }
    
    protected Configuration getConfiguration() {
        Configuration c=super.getConfiguration();
        c.blockingStrategyType=BlockingStrategyType.SIMPLE_CORE;
        c.directBlockingType=DirectBlockingType.SINGLE;
        c.blockingSignatureCacheType=BlockingSignatureCacheType.NOT_CACHED;
        //c.tableauMonitorType=TableauMonitorType.DEBUGGER_HISTORY_ON;
        return c;
    }
    
    public void testExpansion() throws Exception {
        String axioms = "SubClassOf(:B ObjectSomeValuesFrom(:r :D)) "
          + "SubClassOf(owl:Thing ObjectMaxCardinality(1 :r :C))"
          + "SubClassOf(:D :C)"
          + "SubClassOf(:A ObjectSomeValuesFrom(ObjectInverseOf(:r) :B))"
          + "ClassAssertion(:C :a)"
          + "ClassAssertion(:A :a)";
      loadReasonerWithAxioms(axioms);
      assertABoxSatisfiable(true);
    }


    public void testWidmann2() throws Exception {
        // <r>q; 
        // <r->[r-][r][r][r]p 
        String axioms = "SubClassOf(owl:Thing ObjectSomeValuesFrom(:r :q)) "
            + "InverseObjectProperties(:r :r-)"
            + "SubClassOf(owl:Thing ObjectSomeValuesFrom(:r- ObjectAllValuesFrom(:r- ObjectAllValuesFrom(:r ObjectAllValuesFrom(:r ObjectAllValuesFrom(:r :p)))))) "
            + "ClassAssertion(ObjectSomeValuesFrom(:r- ObjectComplementOf(:p)) :a)";
        loadReasonerWithAxioms(axioms);
//        
//        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
//        OWLClassExpression p = df.getOWLClass(IRI.create("file:/c/test.owl#p"));
//        OWLObjectProperty invr = df.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));
//
//        OWLClassExpression desc = df.getOWLObjectSomeValuesFrom(invr, df.getOWLObjectComplementOf(p));
//        assertSatisfiable(desc,false);
        assertABoxSatisfiable(false);
    }
    
    public void testDependencyDisjunctionMergingBug() throws Exception {
        // not yet compatible with core blocking
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
        loadReasonerWithAxioms(buffer.toString());
        
//        OWLClassExpression c = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#c"));
//        OWLClassExpression d = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#d"));
//        OWLObjectProperty invr = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#r-"));
//        OWLObjectProperty invf = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#f-"));
//        
//        OWLClassExpression desc = m_dataFactory.getOWLObjectIntersectionOf(
//            m_dataFactory.getOWLObjectComplementOf(c), 
//            m_dataFactory.getOWLObjectSomeValuesFrom(invf, d), 
//            m_dataFactory.getOWLObjectAllValuesFrom(invr, m_dataFactory.getOWLObjectSomeValuesFrom(invf, d))
//        );  
//        assertSatisfiable(desc,false);
        assertABoxSatisfiable(false);
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
        buffer.append("ClassAssertion(ObjectIntersectionOf(:Infinite-Tree-Root ObjectAllValuesFrom(:descendant ObjectSomeValuesFrom(:successor- :root))) :a)");
        loadReasonerWithAxioms(buffer.toString());
//        assertSatisfiable("file:/c/test.owl#Infinite-Tree-Root",true);
//        
//        OWLClassExpression itr = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#Infinite-Tree-Root"));
//        OWLClassExpression root = m_dataFactory.getOWLClass(IRI.create("file:/c/test.owl#root"));
//        OWLObjectProperty descendant = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#descendant"));
//        OWLObjectProperty invsuccessor = m_dataFactory.getOWLObjectProperty(IRI.create("file:/c/test.owl#successor-"));
//        
//        // [and Infinite-Tree-Root [all descendant [some successor- root]]]
//        OWLClassExpression desc =
//            m_dataFactory.getOWLObjectIntersectionOf(
//                itr, 
//                m_dataFactory.getOWLObjectAllValuesFrom(descendant, 
//                    m_dataFactory.getOWLObjectSomeValuesFrom(invsuccessor, root)
//                )
//            );
//        assertSatisfiable(desc,false);
        assertABoxSatisfiable(false);
    }
}
