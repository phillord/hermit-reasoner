package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.Configuration;

public class ReasonerIndividualReuseTest extends ReasonerTest {

    public ReasonerIndividualReuseTest(String name) {
        super(name);
    }
    protected Configuration getConfiguration() {
        Configuration configuration=new Configuration();
        configuration.existentialStrategyType=Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
        configuration.throwInconsistentOntologyException=false;
        return configuration;
    }
}
