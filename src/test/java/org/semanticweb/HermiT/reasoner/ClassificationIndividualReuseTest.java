package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.Configuration;
@SuppressWarnings("javadoc")
public class ClassificationIndividualReuseTest extends AbstractReasonerTest {

    public ClassificationIndividualReuseTest(String name) {
        super(name);
    }

    public void testDolce() throws Exception {
        loadReasonerFromResource("res/dolce_all.xml");
        assertHierarchies("res/dolce_all.xml.txt");
    }

    @Override
    protected Configuration getConfiguration() {
        Configuration configuration = super.getConfiguration();
        configuration.existentialStrategyType = Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
        return configuration;
    }
}
