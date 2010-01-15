package org.semanticweb.HermiT.reasoner;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

public class ClassificationTest extends AbstractReasonerTest {

    public ClassificationTest(String name) {
        super(name);
    }

    public void testLearningBackTracking() throws Exception {
        String axioms = "SubClassOf(owl:Thing ObjectIntersectionOf(ObjectUnionOf(:C :D1) ObjectUnionOf(:C :D2) ObjectUnionOf(:C :D3) ObjectUnionOf(:C :D4) ObjectUnionOf(:C :D5) ObjectSomeValuesFrom(:r ObjectAllValuesFrom(ObjectInverseOf(:r) ObjectComplementOf(:C)))))";
        loadOntologyWithAxioms(axioms);
        Set<OWLAxiom> assertions=new HashSet<OWLAxiom>();
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractReasonerTest.NS + "A"));
        for (int i=0;i<1000;i++) {
        	assertions.add(m_dataFactory.getOWLClassAssertionAxiom(A, m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractReasonerTest.NS+"a"+i))));
        }
        m_ontologyManager.addAxioms(m_ontology, assertions);
        Configuration c1=new Configuration();
        c1.useDisjunctionLearning=false;
        createReasoner(c1,null);
        long t1=System.currentTimeMillis();
        assertTrue(m_reasoner.isConsistent());
        t1=System.currentTimeMillis()-t1;
        m_reasoner=null;
        Configuration c2=new Configuration();
        c2.useDisjunctionLearning=true;
        createReasoner(c2,null);
        long t2=System.currentTimeMillis();
        assertTrue(m_reasoner.isConsistent());
        t2=System.currentTimeMillis()-t2;
        assertTrue(t1>t2);
    }
    
    public void testWineNoDataProperties() throws Exception {
        loadReasonerFromResource("res/wine-no-data-properties.xml");
        assertHierarchies("res/wine-no-data-properties.xml.txt");
    }

    public void testGalenIansFullUndoctored() throws Exception {
        loadReasonerFromResource("res/galen-ians-full-undoctored.xml");
        assertHierarchies("res/galen-ians-full-undoctored.xml.txt");
    }

    public void testPizza() throws Exception {
        loadReasonerFromResource("res/pizza.xml");
        assertHierarchies("res/pizza.xml.txt");
    }

    public void testPropreo() throws Exception {
        loadReasonerFromResource("res/propreo.xml");
        assertHierarchies("res/propreo.xml.txt");
    }
}
