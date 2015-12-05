package org.semanticweb.HermiT.tableau;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.Variable;

public class MergeTest extends AbstractReasonerInternalsTest {
    protected static final AtomicConcept A=AtomicConcept.create("A");
    protected static final AtomicConcept B=AtomicConcept.create("B");
    protected static final AtomicConcept C=AtomicConcept.create("C");
    protected static final AtomicConcept D=AtomicConcept.create("D");
    protected static final AtomicRole R=AtomicRole.create("R");
    protected static final AtomicNegationConcept NEG_A=AtomicNegationConcept.create(A);
    protected static final AtLeastConcept EXISTS_NEG_A=AtLeastConcept.create(1,R,NEG_A);
    protected static final DLOntology TEST_DL_ONTOLOGY;
    static {
        Variable X=Variable.create("X");
        Variable Y=Variable.create("Y");
        DLClause cl=DLClause.create(new Atom[] { Atom.create(EXISTS_NEG_A,X) },new Atom[] { Atom.create(R,X,Y),Atom.create(A,Y) });
        Set<DLClause> dlClauses=Collections.singleton(cl);
        TEST_DL_ONTOLOGY=getTestDLOntology(dlClauses);
    }

    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;

    public MergeTest(String name) {
        super(name);
    }
    protected void setUp() {
        DirectBlockingChecker directBlockingChecker=new PairWiseDirectBlockingChecker();
        BlockingSignatureCache blockingSignatureCache=new BlockingSignatureCache(directBlockingChecker);
        BlockingStrategy blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingSignatureCache);
        ExistentialExpansionStrategy ExpansionStrategy=new CreationOrderStrategy(blockingStrategy);
        m_tableau=new Tableau(new InterruptFlag(-1),null,ExpansionStrategy,false,TEST_DL_ONTOLOGY,null,new HashMap<String,Object>());
        m_extensionManager=m_tableau.getExtensionManager();
    }
    public void testMergeAndBacktrack() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node b=m_tableau.createNewNINode(emptySet);
        Node a1=m_tableau.createNewTreeNode(emptySet,a);
        Node a2=m_tableau.createNewTreeNode(emptySet,a);
        Node a11=m_tableau.createNewTreeNode(emptySet,a1);
        Node a12=m_tableau.createNewTreeNode(emptySet,a1);

        m_extensionManager.addAssertion(R,a,a1,emptySet,false);
        m_extensionManager.addAssertion(R,a,a2,emptySet,false);

        m_extensionManager.addConceptAssertion(A,a1,emptySet,false);
        m_extensionManager.addConceptAssertion(EXISTS_NEG_A,a1,emptySet,false);

        m_extensionManager.addConceptAssertion(NEG_A,a2,emptySet,false);
        m_extensionManager.addConceptAssertion(B,a2,emptySet,false);
        m_extensionManager.addConceptAssertion(C,a2,emptySet,false);
        m_extensionManager.addConceptAssertion(D,a2,emptySet,false);

        m_extensionManager.addAssertion(R,a1,a11,emptySet,false);
        m_extensionManager.addAssertion(R,a1,a12,emptySet,false);

        m_extensionManager.addConceptAssertion(A,a11,emptySet,false);
        m_extensionManager.addConceptAssertion(A,a12,emptySet,false);

        m_extensionManager.addAssertion(R,a1,b,emptySet,false);

        BranchingPoint bp=new BranchingPoint(m_tableau);
        m_tableau.pushBranchingPoint(bp);

        // The label of a2 is larger, so a1 is merged into a2
        m_extensionManager.addAssertion(Equality.INSTANCE,a1,a2,emptySet,false);

        assertTrue(m_extensionManager.containsClash());
        assertLabel(a2,A,B,C,D,NEG_A,EXISTS_NEG_A);

        assertTrue(a1.isMerged());
        assertSame(a1.getCanonicalNode(),a2);
        assertFalse(a11.isActive());
        assertFalse(a12.isActive());

        assertRetrieval(m_extensionManager.getTernaryExtensionTable(),T(R,null,null),ExtensionTable.View.TOTAL,new Object[][] { T(R,a,a2),T(R,a2,b) });
        assertRetrieval(m_extensionManager.getBinaryExtensionTable(),T(A,null),ExtensionTable.View.TOTAL,new Object[][] { T(A,a2) });


        m_tableau.backtrackTo(bp.getLevel());

        assertFalse(m_extensionManager.containsClash());
        assertLabel(a2,B,C,D,NEG_A);

        assertFalse(a1.isMerged());
        assertSame(a1.getCanonicalNode(),a1);
        assertTrue(a11.isActive());
        assertTrue(a12.isActive());

        assertRetrieval(m_extensionManager.getTernaryExtensionTable(),T(R,null,null),ExtensionTable.View.TOTAL,new Object[][] { T(R,a,a1),T(R,a1,a11),T(R,a1,a12),T(R,a1,b),T(R,a,a2) });
        assertRetrieval(m_extensionManager.getBinaryExtensionTable(),T(A,null),ExtensionTable.View.TOTAL,new Object[][] { T(A,a1),T(A,a11),T(A,a12) });


        m_extensionManager.addAssertion(Inequality.INSTANCE,a11,a12,emptySet,false);
        assertRetrieval(m_extensionManager.getTernaryExtensionTable(),T(Inequality.INSTANCE,null,null),ExtensionTable.View.TOTAL,new Object[][] { T(Inequality.INSTANCE,a11,a12) });


        m_extensionManager.addAssertion(Equality.INSTANCE,a11,a12,emptySet,false);
        assertTrue(m_extensionManager.containsClash());


        m_tableau.backtrackTo(bp.getLevel());

        assertFalse(m_extensionManager.containsClash());
        assertRetrieval(m_extensionManager.getTernaryExtensionTable(),T(Inequality.INSTANCE,null,null),ExtensionTable.View.TOTAL,new Node[0][]);
    }
    protected void assertLabel(Node node,Concept... expected) {
        assertLabel(m_tableau,node,expected);
    }
}
