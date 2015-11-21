package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.Configuration;
@SuppressWarnings("javadoc")
public class ReasonerIndividualReuseTest extends ReasonerTest {

    public ReasonerIndividualReuseTest(String name) {
        super(name);
    }

    @Override
    protected Configuration getConfiguration() {
        Configuration configuration = super.getConfiguration();
        configuration.existentialStrategyType = Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
        return configuration;
    }
}
