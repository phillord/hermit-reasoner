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
}
