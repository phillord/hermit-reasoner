package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.tableau.AbstractOntologyTest;

public class ReasonerTestKAON2Heavy extends AbstractOntologyTest {

    public ReasonerTestKAON2Heavy(String name) {
        super(name);
    }
    
    public void testWineNoDataProperties() throws Exception {
        loadResource("../res/wine-no-data-properties.xml");
        assertSubsumptionHierarchy("../res/wine-no-data-properties.xml.txt");
    }

    public void testGalenIansFullUndoctored() throws Exception {
        loadResource("../res/galen-ians-full-undoctored.xml");
        assertSubsumptionHierarchy("../res/galen-ians-full-undoctored.xml.txt");
    }

    public void testPropreo() throws Exception {
        loadResource("../res/propreo.xml");
        assertSubsumptionHierarchy("../res/propreo.xml.txt");
    }
}
