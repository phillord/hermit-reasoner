package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.Configuration;

public class ClassificationIndividualReuseTest extends ClassificationTest {

    public ClassificationIndividualReuseTest(String name) {
        super(name);
    }
    public void testGalenIansFullUndoctored() throws Exception {
        // omitted for now until we get this under control
    }
    public void testDolce() throws Exception {
        loadReasonerFromResource("res/dolce_all.xml");
        assertHierarchies("res/dolce_all.xml.txt");
    }
    protected Configuration getConfiguration() {
        Configuration configuration=super.getConfiguration();
        configuration.existentialStrategyType=Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
        return configuration;
    }
}
