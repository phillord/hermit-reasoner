package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.Configuration;

public class HeavyReasonerIndividualReuseTest extends HeavyReasonerTest {

    public HeavyReasonerIndividualReuseTest(String name) {
        super(name);
    }
    public void testGalenIansFullUndoctored() throws Exception {
        // omitted for now until we get this under control
    }
    public void testDolceAllNoDatatype() throws Exception {
        loadOntologyFromResource("../res/dolce_all_no_datatype.xml");
        assertSubsumptionHierarchy("../res/dolce_all_no_datatype.xml.txt");
    }
    protected Configuration getConfiguration() {
        Configuration configuration=new Configuration();
        configuration.existentialStrategyType=Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
        return configuration;
    }
}
