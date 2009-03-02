package org.semanticweb.HermiT.tableau;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExpansionStrategy;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.tableau.DependencySet;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

public class DLClauseEvaluationTest extends AbstractReasonerInternalsTest {
    
    protected static final AtomicRole R=AtomicRole.createAtomicRole("R");
    protected static final AtomicRole S=AtomicRole.createAtomicRole("S");
    protected static final AtomicRole T=AtomicRole.createAtomicRole("T");
    protected static final AtomicRole U=AtomicRole.createAtomicRole("U");
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
        TEST_DL_ONTOLOGY = new DLOntology(
                    "opaque:test", // ontology_URI
                    dlClauses, // clauses
                    atoms, // positive facts
                    atoms, // negative facts 
                    null, // atomic concepts
                    null, // transitive roles
                    null, // object roles
                    null, // data roles
                    null, // individuals
                    false, // hasInverseRoles
                    false, // hasAtMostRestrictions
                    false, // hasNominals
                    false, // canUseNIRule
                    false); // hasDatatypes
    }

    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;

    public DLClauseEvaluationTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        BlockingSignatureCache blockingSignatureCache=new BlockingSignatureCache(PairWiseDirectBlockingChecker.INSTANCE);
        BlockingStrategy blockingStrategy=new AnywhereBlocking(PairWiseDirectBlockingChecker.INSTANCE,blockingSignatureCache);
        ExpansionStrategy ExpansionStrategy=new CreationOrderStrategy(blockingStrategy);
        m_tableau=new Tableau(null,ExpansionStrategy,TEST_DL_ONTOLOGY,new HashMap<String,Object>());
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
