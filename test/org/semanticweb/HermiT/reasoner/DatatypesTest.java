package org.semanticweb.HermiT.reasoner;

public class DatatypesTest extends AbstractReasonerTest {

    public DatatypesTest(String name) {
        super(name);
    }
    
    public void testDatatypesUnsat1() throws Exception {
        loadOntologyFromResource("../res/datatypes1.owl");
        assertABoxSatisfiable(false);
    }

    public void testDatatypesUnsat2() throws Exception {
        loadOntologyFromResource("../res/datatypes2.owl");
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypesUnsat3() throws Exception {
        loadOntologyFromResource("../res/datatypes3.owl");
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypesUnsat4() throws Exception {
        loadOntologyFromResource("../res/datatypes4.owl");
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypesSat1() throws Exception {
        loadOntologyFromResource("../res/datatypes5.owl");
        assertABoxSatisfiable(true);
    }
    
    public void testminInclMaxIncl() throws Exception {
        String axioms = "SubClassOf(A DataSomeValuesFrom(dp DatatypeRestriction(xsd:integer minInclusive \"18\"^^xsd:integer))) "
                + "SubClassOf(A DataAllValuesFrom(dp DatatypeRestriction(xsd:integer maxInclusive \"10\"^^xsd:integer))) " 
                + "ClassAssertion(a A)";
        loadOntologyWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDisjointDPsSatInteger() throws Exception {
        String axioms = "DisjointDataProperties(dp1 dp2) " 
                + "DataPropertyAssertion(dp1 a \"10\"^^xsd:integer)"
                + "SubClassOf(A DataSomeValuesFrom(dp2 DatatypeRestriction(xsd:integer minInclusive \"18\"^^xsd:integer maxInclusive \"18\"^^xsd:integer)))"
                + "ClassAssertion(a A)";
        loadOntologyWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testDisjointDPsUnsat() throws Exception {
        String axioms = "DisjointDataProperties(dp1 dp2) " 
                + "DataPropertyAssertion(dp1 a \"10\"^^xsd:integer)"
                + "SubClassOf(A DataSomeValuesFrom(dp2 DatatypeRestriction(xsd:integer minInclusive \"10\"^^xsd:integer maxInclusive \"10\"^^xsd:integer)))"
                + "ClassAssertion(a A)";
        loadOntologyWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDisjointDPsUnsatStringPattern() throws Exception {
        String axioms = "DisjointDataProperties(dp1 dp2) " 
                + "DataPropertyAssertion(dp1 a \"ab\"^^xsd:string)"
                + "DataPropertyAssertion(dp1 a \"ac\"^^xsd:string)"
                + "SubClassOf(A DataSomeValuesFrom(dp2 DatatypeRestriction(xsd:string pattern \"a(b|c)\"^^xsd:string)))"
                + "ClassAssertion(a A)";
        loadOntologyWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
}
