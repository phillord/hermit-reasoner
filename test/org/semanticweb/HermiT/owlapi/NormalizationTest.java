package org.semanticweb.HermiT.owlapi;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.owlapi.structural.OWLAxioms;
import org.semanticweb.HermiT.owlapi.structural.OWLHasKeyDummy;
import org.semanticweb.HermiT.owlapi.structural.OwlNormalization;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;

public class NormalizationTest extends AbstractOWLOntologyTest {

    public NormalizationTest(String name) {
        super(name);
    }
//  
//  public void testDataPropertiesHasValue1() throws Exception {
//      String axioms = "SubClassOf(Eighteen DataHasValue(hasAge \"18\"^^xsd:integer))";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(Eighteen) DataSomeValuesFrom(hasAge DataOneOf(\"18\"^^integer ))))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//
//  public void testDataPropertiesHasValue2() throws Exception {
//      String axioms = "SubClassOf(DataHasValue(hasAge \"18\"^^xsd:integer) Eighteen)";
//      //hasValue( a:hasAge "17"^^xsd:integer )
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(Eighteen DataAllValuesFrom(hasAge DataComplementOf(DataOneOf(\"18\"^^integer )))))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesAll1() throws Exception {
//      String axioms = "SubClassOf(A DataAllValuesFrom(dp xsd:integer))";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataAllValuesFrom(dp integer)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesAll2() throws Exception {
//      String axioms = "SubClassOf(DataAllValuesFrom(dp xsd:integer) A)";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(A DataSomeValuesFrom(dp DataComplementOf(integer))))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesSome1() throws Exception {
//      String axioms = "SubClassOf(DataSomeValuesFrom(dp xsd:string) A)";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(A DataAllValuesFrom(dp DataComplementOf(string))))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesSome2() throws Exception {
//      String axioms = "SubClassOf(A DataSomeValuesFrom(dp xsd:string))";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataSomeValuesFrom(dp string)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesDataOneOf1() throws Exception {
//      String axioms = "SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"Peter\"^^xsd:string \"19\"^^xsd:integer)))";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataAllValuesFrom(dp DataOneOf(\"19\"^^integer \"Peter\"^^string ))))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesDataOneOf2() throws Exception {
//      String axioms = "SubClassOf(DataAllValuesFrom(dp DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer)) A)";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(A DataSomeValuesFrom(dp DataComplementOf(DataOneOf(\"18\"^^integer \"19\"^^integer )))))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesDataComplementOf1() throws Exception {
//      String axioms = "SubClassOf(A DataAllValuesFrom(dp DataComplementOf(DataComplementOf(DataOneOf(\"18\"^^integer \"19\"^^integer)))))";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataAllValuesFrom(dp DataComplementOf(DataComplementOf(DataOneOf(\"18\"^^integer \"19\"^^integer ))))))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesMax1() throws Exception {
//      String axioms = "SubClassOf(A DataMaxCardinality(1 dp xsd:string))";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataMaxCardinality(1 dp string)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesMax2() throws Exception {
//      String axioms = "SubClassOf(DataMaxCardinality(1 dp xsd:string) A)";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(A DataMinCardinality(2 dp string)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesMax3() throws Exception {
//      String axioms = "SubClassOf(A DataMaxCardinality(5 dp xsd:integer))";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataMaxCardinality(5 dp integer)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesMax4() throws Exception {
//      String axioms = "SubClassOf(DataMaxCardinality(5 dp xsd:integer) A)";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(A DataMinCardinality(6 dp integer)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesMin1() throws Exception {
//      String axioms = "SubClassOf(DataMinCardinality(1 dp xsd:string) A)";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(A DataMaxCardinality(0 dp string)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesMin2() throws Exception {
//      String axioms = "SubClassOf(DataMinCardinality(3 dp xsd:string) A)";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(A DataMaxCardinality(2 dp string)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesMin3() throws Exception {
//      String axioms = "SubClassOf(A DataMinCardinality(1 dp xsd:string))";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataMinCardinality(1 dp string)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesMin4() throws Exception {
//      String axioms = "SubClassOf(A DataMinCardinality(5 dp xsd:string))";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataMinCardinality(5 dp string)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesExact1() throws Exception {
//      String axioms = "SubClassOf(A DataExactCardinality(1 dp xsd:integer))";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataMaxCardinality(1 dp integer))), SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataMinCardinality(1 dp integer)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesExact2() throws Exception {
//      String axioms = "SubClassOf(A DataExactCardinality(3 dp xsd:integer))";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataMinCardinality(3 dp integer))), SubClassOf(Thing ObjectUnionOf(ObjectComplementOf(A) DataMaxCardinality(3 dp integer)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesExact3() throws Exception {
//      String axioms = "SubClassOf(DataExactCardinality(1 dp xsd:integer) A)";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(A DataMinCardinality(2 dp integer) DataMaxCardinality(0 dp integer)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testDataPropertiesExact4() throws Exception {
//      String axioms = "SubClassOf(DataExactCardinality(3 dp xsd:integer) A)";
//      OWLOntology ontology = getOWLOntologyWithAxioms(axioms);
//      Set<OWLAxiom> normalizedAxioms = getNormalizedAxioms(ontology);
//      String expectedResult = "[SubClassOf(Thing ObjectUnionOf(A DataMinCardinality(4 dp integer) DataMaxCardinality(2 dp integer)))]";
//      assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
//  }
//  
//  public void testFirst() throws Exception {
//      assertNormalization("../res/normalization-1-input.xml",
//              "../res/normalization-1-OWL-control.xml", 
//              "../res/normalization-1-OWL-control-variant.xml");
//  }

    public void testKeys() throws Exception {
        String axioms="SubClassOf(A B)";
        loadOWLOntologyWithAxioms(axioms);
        Set<OWLHasKeyDummy> keys=new HashSet<OWLHasKeyDummy>();
        OWLHasKeyDummy key=OWLHasKeyDummy.getDemoKey();
        keys.add(key);
        Set<OWLAxiom> normalizedAxioms=getNormalizedAxioms(keys);
        String expectedResult="[HasKey(int:C_test int:r_test int:dp_test), SubClassOf(Thing ObjectUnionOf(B ObjectComplementOf(A)))]";
        assertTrue(expectedResult.trim().equals(normalizedAxioms.toString().trim()));
    }

    protected Set<OWLAxiom> getNormalizedAxioms(Set<OWLHasKeyDummy> keys) throws Exception {
        Set<OWLAxiom> axioms=new HashSet<OWLAxiom>();
        OWLAxioms axiomHolder=new OWLAxioms();
        OwlNormalization normalization=new OwlNormalization(m_ontologyManager.getOWLDataFactory(),axiomHolder);
        if (keys!=null)
            normalization.processKeys(new Reasoner.Configuration(),keys);
        normalization.processOntology(new Reasoner.Configuration(),m_ontology);
        for (OWLDescription[] inclusion : axiomHolder.m_conceptInclusions) {
            OWLDescription superDescription;
            if (inclusion.length==1) {
                superDescription=inclusion[0];
            }
            else {
                superDescription=m_ontologyManager.getOWLDataFactory().getOWLObjectUnionOf(inclusion);
            }
            axioms.add(m_ontologyManager.getOWLDataFactory().getOWLSubClassAxiom(m_ontologyManager.getOWLDataFactory().getOWLThing(),superDescription));
        }
        for (OWLObjectPropertyExpression[] inclusion : axiomHolder.m_objectPropertyInclusions)
            axioms.add(m_ontologyManager.getOWLDataFactory().getOWLSubObjectPropertyAxiom(inclusion[0],inclusion[1]));
        for (OWLHasKeyDummy key : axiomHolder.m_hasKeys)
            axioms.add(key);
        axioms.addAll(axiomHolder.m_facts);
        return axioms;
    }

    protected void assertNormalization(String inputResourceName,String controlResourceName) throws Exception {
        loadOWLOntologyFromResource(inputResourceName);
        Set<OWLAxiom> normlizedAxioms=getNormalizedAxioms(null);
        assertEquals(normlizedAxioms,controlResourceName);
    }
    protected void assertNormalization(String inputResourceName,String controlResourceName,String controlResourceNameVariant) throws Exception {
        loadOWLOntologyFromResource(inputResourceName);
        Set<OWLAxiom> normlizedAxioms=getNormalizedAxioms(null);
        assertEquals(normlizedAxioms,controlResourceName,controlResourceNameVariant);
    }
}
