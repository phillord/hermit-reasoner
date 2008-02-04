package org.semanticweb.HermiT.tableau;

import java.util.Collections;
import java.util.Set;

import org.semanticweb.HermiT.blocking.*;
import org.semanticweb.HermiT.existentials.*;
import org.semanticweb.HermiT.model.*;

public class DLClauseEvaluationTest extends AbstractHermiTTest {
    protected static final AtomicAbstractRole R=AtomicAbstractRole.create("R");
    protected static final AtomicAbstractRole S=AtomicAbstractRole.create("S");
    protected static final AtomicAbstractRole T=AtomicAbstractRole.create("T");
    protected static final AtomicAbstractRole U=AtomicAbstractRole.create("U");
    protected static final DLClause CL_1;
    protected static final DLOntology TEST_DL_ONTOLOGY;
    static {
        Variable X=Variable.create("X");
        Variable Y=Variable.create("Y");
        Variable Z=Variable.create("Z");
        Variable W=Variable.create("W");

        CL_1=DLClause.create(new Atom[] { Atom.create(U,Z,W) },new Atom[] { Atom.create(R,X,Y),Atom.create(S,Y,Z),Atom.create(T,W,W) });
        Set<DLClause> dlClauses=Collections.singleton(CL_1);
        Set<Atom> atoms=Collections.emptySet();
        TEST_DL_ONTOLOGY=new DLOntology("opaque:test",dlClauses,atoms,atoms,false,false,false,false);
    }

    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;

    public DLClauseEvaluationTest(String name) {
        super(name);
    }
    protected void setUp() {
        BlockingSignatureCache blockingSignatureCache=new BlockingSignatureCache(PairWiseDirectBlockingChecker.INSTANCE);
        BlockingStrategy blockingStrategy=new AnywhereBlocking(PairWiseDirectBlockingChecker.INSTANCE,blockingSignatureCache);
        ExistentialsExpansionStrategy existentialsExpansionStrategy=new CreationOrderStrategy(blockingStrategy);
        m_tableau=new Tableau(null,existentialsExpansionStrategy,TEST_DL_ONTOLOGY);
        m_extensionManager=m_tableau.getExtensionManager();
    }
    public void testEvaluator() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        
        Node a=m_tableau.createNewRootNode(emptySet,0);
        Node b=m_tableau.createNewRootNode(emptySet,0);
        Node c=m_tableau.createNewRootNode(emptySet,0);
        Node d=m_tableau.createNewRootNode(emptySet,0);
        Node e=m_tableau.createNewRootNode(emptySet,0);
        
        m_extensionManager.addRoleAssertion(R,a,b,emptySet);
        m_extensionManager.addRoleAssertion(R,a,c,emptySet);

        m_extensionManager.addRoleAssertion(S,b,d,emptySet);

        m_extensionManager.addRoleAssertion(T,e,e,emptySet);
        m_extensionManager.addRoleAssertion(T,c,d,emptySet);

        assertTrue(m_tableau.isSatisfiable());
        
        assertRetrieval(m_extensionManager.getTernaryExtensionTable(),T(U,null,null),ExtensionTable.View.EXTENSION_THIS,new Object[][] { T(U,d,e) });
    }
}
