package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.Configuration;

public class IndividualReuseTest extends AbstractReasonerTest {

    public IndividualReuseTest(String name) {
        super(name);
    }
    public void testGalenIansFullUndoctored() throws Exception {
        // omitted for now until we get this under control
    }
    public void testDolceAllNoDatatype() throws Exception {
        Configuration c = new Configuration();
        c.blockingStrategyType = Configuration.BlockingStrategyType.ANYWHERE;
        c.existentialStrategyType = Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
        c.directBlockingType = Configuration.DirectBlockingType.PAIR_WISE;
        //c.blockingSignatureCacheType = Configuration.BlockingSignatureCacheType.CACHED;
        loadOntologyFromResource(c, "../res/dolce_all_no_datatype.xml");
        assertSubsumptionHierarchy("../res/dolce_all_no_datatype.xml.txt");
    }
    
//    protected Tableau getTableau() throws Exception {
//        Clausification clausification=new Clausification();
//        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
//        DLOntology dlOntology=clausification.clausify(true,m_ontology,noDescriptionGraphs);
//        
//        DirectBlockingChecker directBlockingChecker=PairWiseDirectBlockingChecker.INSTANCE;
//        BlockingSignatureCache blockingSignatureCache=new BlockingSignatureCache(directBlockingChecker);
//        BlockingStrategy blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingSignatureCache);
//        ExpansionStrategy ExpansionStrategy=new IndividualReuseStrategy(blockingStrategy,false);
//        return new Tableau(null,ExpansionStrategy,dlOntology,new HashMap<String,Object>());
//    }
}
