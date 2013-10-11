package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.EntailmentChecker;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

public class EntailmentTest extends AbstractReasonerTest {

    public EntailmentTest(String name) {
        super(name);
    }
    public void testIntegerEntailment() throws Exception {
        String axioms = "Declaration(DataProperty(:dp))"
            + "Declaration(NamedIndividual(:a))"
            + "DataPropertyAssertion(:dp :a \"010\"^^xsd:integer)";
        loadReasonerWithAxioms(axioms);
        axioms = "DataPropertyAssertion(:dp :a \"0010\"^^xsd:integer)";
        OWLOntology conlusions=getOntologyWithAxioms(axioms);
        assertEntails(conlusions.getLogicalAxioms(), true);
    }
    public void testHasKey() throws Exception {
        String axioms = "Declaration(DataProperty(:dp))"
            + "ClassAssertion(owl:Thing :a)"
            + "SubClassOf(owl:Thing ObjectIntersectionOf(DataAllValuesFrom(:dp xsd:string) DataAllValuesFrom(:dp xsd:integer)))";
        loadReasonerWithAxioms(axioms);
        axioms = "HasKey(owl:Thing () (:dp))";
        OWLOntology conlusions=getOntologyWithAxioms(axioms);
        assertEntails(conlusions.getLogicalAxioms(), true);
    }
    public void testBlankNodes1() throws Exception {
        String axioms = "Declaration(ObjectProperty(:p))"
            + "ClassAssertion(owl:Thing :a )"
            + "ObjectPropertyAssertion(:p :a _:anon)";
        loadReasonerWithAxioms(axioms);
        m_ontologyManager.removeOntology(m_ontology);
        axioms = "ClassAssertion(ObjectSomeValuesFrom(:p owl:Thing) :a)";
        OWLOntology conlusions=getOntologyWithAxioms(axioms);
        assertEntails(conlusions.getLogicalAxioms(), true);
    }
    public void testInvalidBlankNodes() throws Exception {
        String axioms = "ClassAssertion(ObjectSomeValuesFrom(:p ObjectSomeValuesFrom(:s owl:Thing)) :a)"
            + "SubObjectPropertyOf( :s :r- )"
            + "InverseObjectProperties( :r- :r ) ";
        loadReasonerWithAxioms(axioms);
        m_ontologyManager.removeOntology(m_ontology);
        axioms = "ObjectPropertyAssertion(:p :a _:anon1)"
            + "ObjectPropertyAssertion(:s _:anon1 _:anon2)"
            + "ObjectPropertyAssertion(:r _:anon2 _:anon1)";
        OWLOntology conlusions=getOntologyWithAxioms(axioms);
        try {
            new EntailmentChecker(m_reasoner, m_dataFactory).entails(conlusions.getLogicalAxioms());
            fail();
        }
        catch (Exception e) {
            // blank nodes in the conclusion ontology should not contain cycles
        }
    }
    public void testValidBlankNodesWithNominals() throws Exception {
        String axioms = "ClassAssertion(ObjectSomeValuesFrom(:p ObjectSomeValuesFrom(:s ObjectOneOf(:b))) :a)"
            + "SubObjectPropertyOf( :s :r )";
        loadReasonerWithAxioms(axioms);
        m_ontologyManager.removeOntology(m_ontology);
        axioms = "ObjectPropertyAssertion(:p :a _:anon1)"
            + "ObjectPropertyAssertion(:r _:anon1 :b)";
        OWLOntology conlusions=getOntologyWithAxioms(axioms);
        assertEntails(conlusions.getLogicalAxioms(), true);
    }
    public void testValidBlankNodesInPremise() throws Exception {
        String axioms = "ObjectPropertyAssertion(:r :a _:anon1)"
            + "ObjectPropertyAssertion(:s _:anon1 _:anon2)";
        loadReasonerWithAxioms(axioms);
        m_ontologyManager.removeOntology(m_ontology);
        axioms = "ObjectPropertyAssertion(:r _:anon1 _:anon2)";
        OWLOntology conlusions=getOntologyWithAxioms(axioms);
        assertEntails(conlusions.getLogicalAxioms(), true);
    }
    public void testValidBlankNodes() throws Exception {
        String axioms = "ObjectPropertyAssertion(:r :a :b)"
            + "ObjectPropertyAssertion(:s :b :c)";
        loadReasonerWithAxioms(axioms);
        m_ontologyManager.removeOntology(m_ontology);
        axioms = "ObjectPropertyAssertion(:r _:anon1 _:anon2)";
        OWLOntology conlusions=getOntologyWithAxioms(axioms);
        assertEntails(conlusions.getLogicalAxioms(), true);
    }
    public void testBlankWithDTs() throws Exception {
        String axioms = "ObjectPropertyAssertion(:r :a :b)"
            + "ObjectPropertyAssertion(:s :b :c)";
        loadReasonerWithAxioms(axioms);
        m_ontologyManager.removeOntology(m_ontology);
        axioms = "DataPropertyAssertion(:dp _:anon1 \"test\")";
        OWLOntology conlusions=getOntologyWithAxioms(axioms);
        assertEntails(conlusions.getLogicalAxioms(), false);
    }
    public void testBlankWithDTs2() throws Exception {
        String axioms = "DataPropertyAssertion(:dp :a \"test\")"
            + "ObjectPropertyAssertion(:s :b :c)";
        loadReasonerWithAxioms(axioms);
        m_ontologyManager.removeOntology(m_ontology);
        axioms = "DataPropertyAssertion(:dp _:anon1 \"test\")";
        OWLOntology conlusions=getOntologyWithAxioms(axioms);
        assertEntails(conlusions.getLogicalAxioms(), true);
    }
    public void testBlankWithDTs3() throws Exception {
        String axioms = "DataPropertyAssertion(:dp :a \"test\")"
            + "ObjectPropertyAssertion(:s :b :c)";
        loadReasonerWithAxioms(axioms);
        m_ontologyManager.removeOntology(m_ontology);
        axioms = "DataPropertyAssertion(:dp _:anon1 \"test\"^^xsd:string)";
        OWLOntology conlusions=getOntologyWithAxioms(axioms);
        assertEntails(conlusions.getLogicalAxioms(), true);
    }
    protected OWLOntology getOntologyFromRessource(String resourceName) throws Exception {
        IRI physicalIRI=IRI.create(getClass().getResource(resourceName).toURI());
        return m_ontologyManager.loadOntologyFromOntologyDocument(physicalIRI);
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
        buffer.append("Ontology(");
        buffer.append(axioms);
        buffer.append(")");
        OWLOntologyDocumentSource input=new StringDocumentSource(buffer.toString());
        return m_ontologyManager.loadOntologyFromOntologyDocument(input);
    }
}
