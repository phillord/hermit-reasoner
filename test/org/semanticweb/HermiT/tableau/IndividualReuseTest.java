package org.semanticweb.HermiT.tableau;

import java.util.Collections;
import java.util.Set;

import org.semanticweb.HermiT.kaon2.structural.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.disjunction.*;
import org.semanticweb.HermiT.existentials.*;

public class IndividualReuseTest extends ReasonerTest {

    public IndividualReuseTest(String name) {
        super(name);
    }
    public void testIanT5() {
        // requires blocking!
    }
    public void testIanT9() {
        // requires blocking!
    }
    public void testHeinsohnTBox3Modified() {
        // requires blocking
    }
    protected Tableau getTableau() throws Exception {
        Clausification clausification=new Clausification();
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        DLOntology dlOntology=clausification.clausify(m_ontology,true,noDescriptionGraphs);
        ExistentialsExpansionStrategy existentialsExpansionStrategy=new IndividualReuseStrategy(false);
        DisjunctionProcessingStrategy disjunctionProcessingStrategy=new MostRecentDisjunctionProcessingStrategy();
        return new Tableau(null,existentialsExpansionStrategy,disjunctionProcessingStrategy,dlOntology);
    }
}
