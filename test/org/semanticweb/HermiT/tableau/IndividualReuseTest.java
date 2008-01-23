package org.semanticweb.HermiT.tableau;

import java.util.Collections;
import java.util.Set;

import org.semanticweb.HermiT.kaon2.structural.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.blocking.*;
import org.semanticweb.HermiT.disjunction.*;
import org.semanticweb.HermiT.existentials.*;

public class IndividualReuseTest extends ReasonerTest {

    public IndividualReuseTest(String name) {
        super(name);
    }
    protected Tableau getTableau() throws Exception {
        Clausification clausification=new Clausification();
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        DLOntology dlOntology=clausification.clausify(true,m_ontology,true,noDescriptionGraphs);
        DirectBlockingChecker directBlockingChecker=PairWiseDirectBlockingChecker.INSTANCE;
        BlockingCache blockingCache=new BlockingCache(directBlockingChecker);
        BlockingStrategy blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingCache);
        ExistentialsExpansionStrategy existentialsExpansionStrategy=new IndividualReuseStrategy(blockingStrategy,false);
        DisjunctionProcessingStrategy disjunctionProcessingStrategy=new MostRecentDisjunctionProcessingStrategy();
        return new Tableau(null,existentialsExpansionStrategy,disjunctionProcessingStrategy,dlOntology);
    }
}
