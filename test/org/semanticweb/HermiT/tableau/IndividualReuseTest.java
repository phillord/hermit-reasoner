package org.semanticweb.HermiT.tableau;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.HermiT.kaon2.structural.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.blocking.*;
import org.semanticweb.HermiT.existentials.*;

public class IndividualReuseTest extends ReasonerTest {

    public IndividualReuseTest(String name) {
        super(name);
    }
    public void testGalenIansFullUndoctored() throws Exception {
        // omitted for now until we get this under control
    }
    public void testDolceAllNoDatatype() throws Exception {
        loadResource("res/dolce_all_no_datatype.xml");
        assertSubsumptionHierarchy("res/dolce_all_no_datatype.xml.txt");
    }
    protected Tableau getTableau() throws Exception {
        Clausification clausification=new Clausification();
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        DLOntology dlOntology=clausification.clausify(true,m_ontology,noDescriptionGraphs);
        DirectBlockingChecker directBlockingChecker=PairWiseDirectBlockingChecker.INSTANCE;
        BlockingSignatureCache blockingSignatureCache=new BlockingSignatureCache(directBlockingChecker);
        BlockingStrategy blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingSignatureCache);
        ExistentialsExpansionStrategy existentialsExpansionStrategy=new IndividualReuseStrategy(blockingStrategy,false);
        return new Tableau(null,existentialsExpansionStrategy,dlOntology,new HashMap<String,Object>());
    }
}
