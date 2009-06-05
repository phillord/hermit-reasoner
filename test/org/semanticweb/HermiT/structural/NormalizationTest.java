package org.semanticweb.HermiT.structural;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClassExpression;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLHasKeyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;

public class NormalizationTest extends AbstractStructuralTest {

    public NormalizationTest(String name) {
        super(name);
    }
    
  public void testDataPropertiesHasValue1() throws Exception {
      String axioms = "Declaration(Class(:Eighteen)) Declaration(DataProperty(:hasAge)) SubClassOf(:Eighteen DataHasValue(:hasAge \"18\"^^xsd:integer))";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"Eighteen>) DataSomeValuesFrom(<"+NS+"hasAge> DataOneOf(\"18\"^^integer ))))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }

  public void testDataPropertiesHasValue2() throws Exception {
      String axioms = "Declaration(Class(:Eighteen)) Declaration(DataProperty(:hasAge)) SubClassOf(DataHasValue(:hasAge \"18\"^^xsd:integer) :Eighteen)";
      //hasValue( a:hasAge "17"^^xsd:integer )
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"Eighteen> DataAllValuesFrom(<"+NS+"hasAge> DataComplementOf(DataOneOf(\"18\"^^integer )))))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesAll1() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataAllValuesFrom(:dp xsd:integer))";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataAllValuesFrom(<"+NS+"dp> integer)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesAll2() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataAllValuesFrom(:dp xsd:integer) :A)";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataSomeValuesFrom(<"+NS+"dp> DataComplementOf(integer))))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesSome1() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataSomeValuesFrom(:dp xsd:string) :A)";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataAllValuesFrom(<"+NS+"dp> DataComplementOf(string))))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesSome2() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataSomeValuesFrom(:dp xsd:string))";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataSomeValuesFrom(<"+NS+"dp> string)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesDataOneOf1() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"Peter\"^^xsd:string \"19\"^^xsd:integer)))";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataAllValuesFrom(<"+NS+"dp> DataOneOf(\"19\"^^integer \"Peter\"^^string ))))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesDataOneOf2() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataAllValuesFrom(:dp DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer)) :A)";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataSomeValuesFrom(<"+NS+"dp> DataComplementOf(DataOneOf(\"18\"^^integer \"19\"^^integer )))))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }

  public void testDataPropertiesDataComplementOf1() throws Exception {
      String axioms = "SubClassOf(:A DataAllValuesFrom(:dp DataComplementOf(DataComplementOf(DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer)))))";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataAllValuesFrom(<"+NS+"dp> DataOneOf(\"18\"^^integer \"19\"^^integer ))))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesMax1() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMaxCardinality(1 :dp xsd:string))";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMaxCardinality(1 <"+NS+"dp> string)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesMax2() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMaxCardinality(1 :dp xsd:string) :A)";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataMinCardinality(2 <"+NS+"dp> string)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesMax3() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMaxCardinality(5 :dp xsd:integer))";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMaxCardinality(5 <"+NS+"dp> integer)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesMax4() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMaxCardinality(5 :dp xsd:integer) :A)";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataMinCardinality(6 <"+NS+"dp> integer)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesMin1() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMinCardinality(1 :dp xsd:string) :A)";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataAllValuesFrom(<"+NS+"dp> DataComplementOf(string))))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesMin2() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMinCardinality(3 :dp xsd:string) :A)";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataMaxCardinality(2 <"+NS+"dp> string)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesMin3() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMinCardinality(1 :dp xsd:string))";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataSomeValuesFrom(<"+NS+"dp> string)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesMin4() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMinCardinality(5 :dp xsd:string))";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMinCardinality(5 <"+NS+"dp> string)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesExact1() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataExactCardinality(1 :dp xsd:integer))";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMaxCardinality(1 <"+NS+"dp> integer))), SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataSomeValuesFrom(<"+NS+"dp> integer)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesExact2() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataExactCardinality(3 :dp xsd:integer))";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMinCardinality(3 <"+NS+"dp> integer))), SubClassOf(owl:Thing ObjectUnionOf(ObjectComplementOf(<"+NS+"A>) DataMaxCardinality(3 <"+NS+"dp> integer)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesExact3() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataExactCardinality(1 :dp xsd:integer) :A)";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataAllValuesFrom(<"+NS+"dp> DataComplementOf(integer)) DataMinCardinality(2 <"+NS+"dp> integer)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
  public void testDataPropertiesExact4() throws Exception {
      String axioms = "Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataExactCardinality(3 :dp xsd:integer) :A)";
      loadOntologyWithAxioms(axioms);
      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms();
      String expectedResult = "[SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> DataMinCardinality(4 <"+NS+"dp> integer) DataMaxCardinality(2 <"+NS+"dp> integer)))]";
      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
  }
  
    public void testKeys() throws Exception {
        String axioms="HasKey(:C (:r) (:dp))";
        loadOntologyWithAxioms(axioms);
        Set<OWLAxiom> normalizedAxioms=getNormalizedAxioms();
        String expectedResult="[HasKey(<"+NS+"C> (<"+NS+"r> ) (<"+NS+"dp> ))]";
        assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
    }
    
    public void testKeys2() throws Exception {
        String axioms="HasKey(ObjectIntersectionOf(:A :B) (:r) (:dp))";
        loadOntologyWithAxioms(axioms);
        Set<OWLAxiom> normalizedAxioms=getNormalizedAxioms();
        String expected="[SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"A> ObjectComplementOf(<internal:def#0>))), SubClassOf(owl:Thing ObjectUnionOf(<"+NS+"B> ObjectComplementOf(<internal:def#0>))), HasKey(<internal:def#0> (<"+NS+"r> ) ())]";
        assertTrue(expected.trim().equals(normalizedAxioms.toString().trim()));
    }

    protected Set<OWLAxiom> getNormalizedAxioms() throws Exception {
        Set<OWLAxiom> axioms=new HashSet<OWLAxiom>();
        OWLAxioms axiomHolder=new OWLAxioms();
        OWLNormalization normalization=new OWLNormalization(m_ontologyManager.getOWLDataFactory(),axiomHolder);
        normalization.processOntology(new Configuration(),m_ontology);
        for (OWLClassExpression[] inclusion : axiomHolder.m_conceptInclusions) {
            OWLClassExpression superDescription;
            if (inclusion.length==1) {
                superDescription=inclusion[0];
            }
            else {
                superDescription=m_dataFactory.getOWLObjectUnionOf(inclusion);
            }
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

    protected void assertNormalization(String inputResourceName,String controlResourceName) throws Exception {
        loadOntologyFromResource(inputResourceName);
        Set<OWLAxiom> normlizedAxioms=getNormalizedAxioms();
        assertEquals(normlizedAxioms,controlResourceName);
    }
    protected void assertNormalization(String inputResourceName,String controlResourceName,String controlResourceNameVariant) throws Exception {
        loadOntologyFromResource(inputResourceName);
        Set<OWLAxiom> normlizedAxioms=getNormalizedAxioms();
        assertEquals(normlizedAxioms,controlResourceName,controlResourceNameVariant);
    }
}
