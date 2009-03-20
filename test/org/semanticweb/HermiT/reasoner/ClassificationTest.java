package org.semanticweb.HermiT.reasoner;

public class ClassificationTest extends AbstractReasonerTest {

    public ClassificationTest(String name) {
        super(name);
    }

    public void testWineNoDataProperties() throws Exception {
        loadOntologyFromResource("../res/wine-no-data-properties.xml");
        assertSubsumptionHierarchy("../res/wine-no-data-properties.xml.txt");
    }

    public void testGalenIansFullUndoctored() throws Exception {
        loadOntologyFromResource("../res/galen-ians-full-undoctored.xml");
        assertSubsumptionHierarchy("../res/galen-ians-full-undoctored.xml.txt");
    }

    public void testPizza() throws Exception {
        loadOntologyFromResource("../res/pizza.xml");
        assertSubsumptionHierarchy("../res/pizza.xml.txt");
    }

    public void testPropreo() throws Exception {
        loadOntologyFromResource("../res/propreo.xml");
        assertSubsumptionHierarchy("../res/propreo.xml.txt");
    }
}
