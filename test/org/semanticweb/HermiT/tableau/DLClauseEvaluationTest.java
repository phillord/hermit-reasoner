package org.semanticweb.HermiT.tableau;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.Variable;

public class DLClauseEvaluationTest extends AbstractReasonerInternalsTest {

    protected static final AtomicRole R=AtomicRole.create("R");
    protected static final AtomicRole S=AtomicRole.create("S");
    protected static final AtomicRole T=AtomicRole.create("T");
    protected static final AtomicRole U=AtomicRole.create("U");
    protected static final DLClause CL_1;
    protected static final DLOntology TEST_DL_ONTOLOGY;
    static {
        Variable X=Variable.create("X");
        Variable Y=Variable.create("Y");
        Variable Z=Variable.create("Z");
        Variable W=Variable.create("W");

        CL_1=DLClause.create(new Atom[] { Atom.create(U,Z,W) },new Atom[] { Atom.create(R,X,Y),Atom.create(S,Y,Z),Atom.create(T,W,W) });
        Set<DLClause> dlClauses=Collections.singleton(CL_1);
        TEST_DL_ONTOLOGY=getTestDLOntology(dlClauses);
    }

    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;

    public DLClauseEvaluationTest(String name) {
        super(name);
    }

    protected void setUp() {
        PairWiseDirectBlockingChecker directChecker=new PairWiseDirectBlockingChecker();
        BlockingSignatureCache blockingSignatureCache=new BlockingSignatureCache(directChecker);
        BlockingStrategy blockingStrategy=new AnywhereBlocking(directChecker,blockingSignatureCache);
        ExistentialExpansionStrategy ExpansionStrategy=new CreationOrderStrategy(blockingStrategy);
        m_tableau=new Tableau(new InterruptFlag(-1),null,ExpansionStrategy,false,TEST_DL_ONTOLOGY,null,new HashMap<String,Object>());
        m_extensionManager=m_tableau.getExtensionManager();
    }

    public void testEvaluator() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();

        Node a=m_tableau.createNewNINode(emptySet);
        Node b=m_tableau.createNewNINode(emptySet);
        Node c=m_tableau.createNewNINode(emptySet);
        Node d=m_tableau.createNewNINode(emptySet);
        Node e=m_tableau.createNewNINode(emptySet);

        m_extensionManager.addRoleAssertion(R,a,b,emptySet,false);
        m_extensionManager.addRoleAssertion(R,a,c,emptySet,false);

        m_extensionManager.addRoleAssertion(S,b,d,emptySet,false);

        m_extensionManager.addRoleAssertion(T,e,e,emptySet,false);
        m_extensionManager.addRoleAssertion(T,c,d,emptySet,false);

        assertTrue(m_tableau.runCalculus());

        assertRetrieval(m_extensionManager.getTernaryExtensionTable(),T(U,null,null),ExtensionTable.View.EXTENSION_THIS,new Object[][] { T(U,d,e) });
    }
}
