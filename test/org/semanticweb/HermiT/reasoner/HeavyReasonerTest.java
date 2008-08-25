package org.semanticweb.HermiT.reasoner;

public class HeavyReasonerTest extends AbstractReasonerTest {

    public HeavyReasonerTest(String name) {
        super(name);
    }

    public void testWineNoDataProperties() throws Exception {
        assertSubsumptionHierarchy("res/wine-no-data-properties.xml",
                "res/wine-no-data-properties.xml.txt");
    }

    public void testGalenIansFullUndoctored() throws Exception {
        assertSubsumptionHierarchy("res/galen-ians-full-undoctored.xml",
                "res/galen-ians-full-undoctored.xml.txt");
    }

    public void testPizza() throws Exception {
        assertSubsumptionHierarchy("res/pizza.xml", "res/pizza.xml.txt");
    }

    public void testPropreo() throws Exception {
        assertSubsumptionHierarchy("res/propreo.xml", "res/propreo.xml.txt");
    }
}
