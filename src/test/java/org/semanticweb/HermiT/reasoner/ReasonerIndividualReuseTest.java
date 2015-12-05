package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.Configuration;

public class ReasonerIndividualReuseTest extends ReasonerTest {

    public ReasonerIndividualReuseTest(String name) {
        super(name);
    }
    protected Configuration getConfiguration() {
        Configuration configuration=super.getConfiguration();
        configuration.existentialStrategyType=Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
        return configuration;
    }
}
