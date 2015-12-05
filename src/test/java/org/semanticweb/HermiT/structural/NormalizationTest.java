package org.semanticweb.HermiT.structural;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class NormalizationTest extends AbstractStructuralTest {

    public NormalizationTest(String name) {
        super(name);
    }

    public void testDataPropertiesHasValue1() throws Exception {
        assertNormalization(
            "Declaration(Class(:Eighteen)) Declaration(DataProperty(:hasAge)) SubClassOf(:Eighteen DataHasValue(:hasAge \"18\"^^xsd:integer))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"Eighteen>) DataSomeValuesFrom(<"+NS+"hasAge> DataOneOf(\"18\"^^xsd:integer ))))"
        );
    }

    public void testDataPropertiesHasValue2() throws Exception {
        assertNormalization(
            "Declaration(Class(:Eighteen)) Declaration(DataProperty(:hasAge)) SubClassOf(DataHasValue(:hasAge \"18\"^^xsd:integer) :Eighteen)",
            "SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"Eighteen> DataAllValuesFrom(<"+NS+"hasAge> DataComplementOf(DataOneOf(\"18\"^^xsd:integer )))))"
        );
    }

    public void testDataPropertiesAll1() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataAllValuesFrom(:dp xsd:integer))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataAllValuesFrom(<"+NS+"dp> xsd:integer)))"
        );
    }

    public void testDataPropertiesAll2() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataAllValuesFrom(:dp xsd:integer) :A)",
            "SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataSomeValuesFrom(<"+NS+"dp> DataComplementOf(xsd:integer))))"
        );
    }

    public void testDataPropertiesSome1() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataSomeValuesFrom(:dp xsd:string) :A)",
            "SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataAllValuesFrom(<"+NS+"dp> DataComplementOf(xsd:string))))"
        );
    }

    public void testDataPropertiesSome2() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataSomeValuesFrom(:dp xsd:string))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataSomeValuesFrom(<"+NS+"dp> xsd:string)))"
        );
    }

    public void testDataPropertiesDataOneOf1() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"Peter\"^^xsd:string \"19\"^^xsd:integer)))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataAllValuesFrom(<"+NS+"dp> DataOneOf(\"19\"^^xsd:integer \"Peter\"^^xsd:string ))))"
        );
    }

    public void testDataPropertiesDataOneOf2() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataAllValuesFrom(:dp DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer)) :A)",
            "SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataSomeValuesFrom(<"+NS+"dp> DataComplementOf(DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer )))))"
        );
    }

    public void testDataPropertiesDataComplementOf1() throws Exception {
        assertNormalization(
            "SubClassOf(:A DataAllValuesFrom(:dp DataComplementOf(DataComplementOf(DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer)))))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataAllValuesFrom(<"+NS+"dp> DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer ))))"
        );
    }

    public void testDataPropertiesMax1() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMaxCardinality(1 :dp xsd:string))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMaxCardinality(1 <"+NS+"dp> xsd:string)))"
        );
    }

    public void testDataPropertiesMax2() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMaxCardinality(1 :dp xsd:string) :A)",
            "SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataMinCardinality(2 <"+NS+"dp> xsd:string)))"
        );
    }

    public void testDataPropertiesMax3() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMaxCardinality(5 :dp xsd:integer))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMaxCardinality(5 <"+NS+"dp> xsd:integer)))"
        );
    }

    public void testDataPropertiesMax4() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMaxCardinality(5 :dp xsd:integer) :A)",
            "SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataMinCardinality(6 <"+NS+"dp> xsd:integer)))"
        );
    }

    public void testDataPropertiesMin1() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMinCardinality(1 :dp xsd:string) :A)",
            "SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataAllValuesFrom(<"+NS+"dp> DataComplementOf(xsd:string))))"
        );
    }

    public void testDataPropertiesMin2() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMinCardinality(3 :dp xsd:string) :A)",
            "SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataMaxCardinality(2 <"+NS+"dp> xsd:string)))"
        );
    }

    public void testDataPropertiesMin3() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMinCardinality(1 :dp xsd:string))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataSomeValuesFrom(<"+NS+"dp> xsd:string)))"
        );
    }

    public void testDataPropertiesMin4() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMinCardinality(5 :dp xsd:string))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMinCardinality(5 <"+NS+"dp> xsd:string)))"
        );
    }

    public void testDataPropertiesExact1() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataExactCardinality(1 :dp xsd:integer))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMaxCardinality(1 <"+NS+"dp> xsd:integer)))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataSomeValuesFrom(<"+NS+"dp> xsd:integer)))"
        );
    }

    public void testDataPropertiesExact2() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataExactCardinality(3 :dp xsd:integer))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMinCardinality(3 <"+NS+"dp> xsd:integer)))",
            "SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMaxCardinality(3 <"+NS+"dp> xsd:integer)))"
        );
    }

    public void testDataPropertiesExact3() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataExactCardinality(1 :dp xsd:integer) :A)",
            "SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataAllValuesFrom(<"+NS+"dp> DataComplementOf(xsd:integer)) DataMinCardinality(2 <"+NS+"dp> xsd:integer)))");
    }

    public void testDataPropertiesExact4() throws Exception {
        assertNormalization(
            "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataExactCardinality(3 :dp xsd:integer) :A)",
            "SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataMinCardinality(4 <"+NS+"dp> xsd:integer) DataMaxCardinality(2 <"+NS+"dp> xsd:integer)))"
         );
    }

    public void testKeys1() throws Exception {
        assertNormalization(
            "HasKey(:C (:r) (:dp))",
            "HasKey(<"+NS+"C> (<"+NS+"r> ) (<"+NS+"dp> ))"
        );
    }

    public void testKeys2() throws Exception {
        Set<String> normalizedAxiomsStrings=getNormalizedAxiomsString("HasKey(ObjectIntersectionOf(:A :B) (:r) (:dp))");
        Set<String> control1=new HashSet<String>();
        control1.add("SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> ObjectComplementOf(<internal:def#0>)))");
        control1.add("SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"B> ObjectComplementOf(<internal:def#0>))))");
        control1.add("HasKey(<internal:def#0> (<"+NS+"r> ) ())");
        Set<String> control2=new HashSet<String>();
        control2.add("SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"B> ObjectComplementOf(<internal:def#0>)))");
        control2.add("SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> ObjectComplementOf(<internal:def#0>)))");
        control2.add("HasKey(<internal:def#0> (<"+NS+"r> ) ())");
        if (!normalizedAxiomsStrings.equals(control1) && !normalizedAxiomsStrings.equals(control2))
            fail();
    }

    public void testTopObjectPropertyInSuperPosition() throws Exception {
        assertNormalization(
            "SubObjectPropertyOf(:A owl:topObjectProperty)"
        );
    }

    protected Set<OWLAxiom> getNormalizedAxioms() throws Exception {
        Set<OWLAxiom> axioms=new HashSet<OWLAxiom>();
        OWLAxioms axiomHolder=new OWLAxioms();
        OWLNormalization normalization=new OWLNormalization(m_ontologyManager.getOWLDataFactory(),axiomHolder,0);
        normalization.processOntology(m_ontology);
        for (OWLClassExpression[] inclusion : axiomHolder.m_conceptInclusions) {
            OWLClassExpression superDescription;
            if (inclusion.length==1)
                superDescription=inclusion[0];
            else
                superDescription=m_dataFactory.getOWLObjectUnionOf(inclusion);
            axioms.add(m_dataFactory.getOWLSubClassOfAxiom(m_ontologyManager.getOWLDataFactory().getOWLThing(),superDescription));
        }
        for (OWLObjectPropertyExpression[] inclusion : axiomHolder.m_simpleObjectPropertyInclusions)
            axioms.add(m_dataFactory.getOWLSubObjectPropertyOfAxiom(inclusion[0],inclusion[1]));
        for (OWLDataPropertyExpression[] inclusion : axiomHolder.m_dataPropertyInclusions)
            axioms.add(m_dataFactory.getOWLSubDataPropertyOfAxiom(inclusion[0],inclusion[1]));
        for (OWLHasKeyAxiom axiom : axiomHolder.m_hasKeys)
            axioms.add(m_dataFactory.getOWLHasKeyAxiom(axiom.getClassExpression(),axiom.getPropertyExpressions()));
        axioms.addAll(axiomHolder.m_facts);
        return axioms;
    }

    protected Set<String> getNormalizedAxiomsString(String axiomsString) throws Exception {
        loadOntologyWithAxioms(axiomsString);
        Set<OWLAxiom> normalizedAxioms=getNormalizedAxioms();
        Set<String> normalizedAxiomsString=new HashSet<String>();
        for (OWLAxiom axiom : normalizedAxioms)
            normalizedAxiomsString.add(axiom.toString());
        return normalizedAxiomsString;
    }

    protected void assertNormalization(String axiomsString,String... expectedAxiomsString) throws Exception {
        Set<String> normalizedAxiomsString=getNormalizedAxiomsString(axiomsString);
        assertContainsAll(normalizedAxiomsString,expectedAxiomsString);
    }
}
