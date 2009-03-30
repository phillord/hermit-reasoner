package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.Configuration;

public class ClassificationIndividualReuseTest extends ClassificationTest {

    public ClassificationIndividualReuseTest(String name) {
        super(name);
    }
    public void testGalenIansFullUndoctored() throws Exception {
        // omitted for now until we get this under control
    }
    public void testDolceAllNoDatatype() throws Exception {
        loadReasonerFromResource("res/dolce_all_no_datatype.xml");
        assertHierarchies("res/dolce_all_no_datatype.xml.txt");
    }
    protected Configuration getConfiguration() {
        Configuration configuration=new Configuration();
        configuration.existentialStrategyType=Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
        return configuration;
    }
}
