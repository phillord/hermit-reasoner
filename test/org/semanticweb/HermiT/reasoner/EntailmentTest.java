package org.semanticweb.HermiT.reasoner;

import java.net.URI;
import java.util.Set;

import org.semanticweb.owl.io.OWLOntologyInputSource;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import org.semanticweb.owl.model.OWLOntology;

public class EntailmentTest extends AbstractReasonerTest {

    public EntailmentTest(String name) {
        super(name);
    }
    public void testBlankNodes1() throws Exception {
        String axioms = "Declaration(ObjectProperty(:p))" 
            + "ClassAssertion(:a owl:Thing)"
            + "ObjectPropertyAssertion(:p :a _:anon)";
        loadReasonerWithAxioms(axioms);
        axioms = "ClassAssertion(ObjectSomeValuesFrom(:p owl:Thing) :a)";
        OWLOntology conlusions=getOntologyWithAxioms(axioms);
        assertPositiveEntailment(conlusions.getLogicalAxioms());
    }
//    public void testBlankNodes2() throws Exception {
//        loadReasonerFromResource("res/entailment/somevaluesfrom2bnode-premise.rdf");
//        OWLOntology conlusions=getOntologyFromRessource("res/entailment/somevaluesfrom2bnode-conclusion.rdf");
//        assertPositiveEntailment(conlusions.getLogicalAxioms());
//    }
    protected OWLOntology getOntologyFromRessource(String resourceName) throws Exception {
        URI physicalURI=getClass().getResource(resourceName).toURI();
        return m_ontologyManager.loadOntologyFromPhysicalURI(physicalURI);
    }
    protected OWLOntology getOntologyWithAxioms(String axioms) throws Exception {
        StringBuffer buffer=new StringBuffer();
        buffer.append("Prefix(:=<"+NS+">)");
        buffer.append("Prefix(a:=<"+NS+">)");
        buffer.append("Prefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)");
        buffer.append("Prefix(owl2xml:=<http://www.w3.org/2006/12/owl2-xml#>)");
        buffer.append("Prefix(test:=<"+NS+">)");
        buffer.append("Prefix(owl:=<http://www.w3.org/2002/07/owl#>)");
        buffer.append("Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)");
        buffer.append("Prefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)");
        buffer.append("Ontology(<"+ONTOLOGY_URI+">");
        buffer.append(axioms);
        buffer.append(")");
        OWLOntologyInputSource input=new StringInputSource(buffer.toString());
        return m_ontologyManager.loadOntology(input);
    }
    protected void assertPositiveEntailment(Set<OWLLogicalAxiom> axioms) {
        for (OWLAxiom axiom : axioms) {
            boolean isEntailed=m_reasoner.isEntailed(axiom);
            assertTrue(isEntailed);
        }
    }
}
