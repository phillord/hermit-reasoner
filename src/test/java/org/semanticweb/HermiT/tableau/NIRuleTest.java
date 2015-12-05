package org.semanticweb.HermiT.tableau;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.NodeIDLessEqualThan;
import org.semanticweb.HermiT.model.NodeIDsAscendingOrEqual;
import org.semanticweb.HermiT.model.Variable;

public class NIRuleTest extends AbstractReasonerInternalsTest {
    protected static final AtomicConcept A=AtomicConcept.create("A");
    protected static final AtomicConcept B=AtomicConcept.create("B");
    protected static final AtomicNegationConcept NEG_A=AtomicNegationConcept.create(A);
    protected static final AtomicRole R=AtomicRole.create("R");
    protected static final AtomicRole S=AtomicRole.create("S");
    protected static final AtomicRole T=AtomicRole.create("T");
    protected static final AtomicConcept AT_MOST_ONE_R_A=AtomicConcept.create("AT_MOST_ONE_R_A");
    protected static final AtomicConcept AT_MOST_TWO_R_A=AtomicConcept.create("AT_MOST_TWO_R_A");
    protected static final AnnotatedEquality EQ_ONE_R_A=AnnotatedEquality.create(1,R,A);
    protected static final AnnotatedEquality EQ_TWO_R_A=AnnotatedEquality.create(2,R,A);
    protected static final AnnotatedEquality EQ_ONE_S_A=AnnotatedEquality.create(1,S,A);
    protected static final DLOntology TEST_DL_ONTOLOGY;
    static {
        // One disjunctive clause is needed in order to turn on nondeterminism in the tableau.
        Variable X=Variable.create("X");
        Variable Y1=Variable.create("Y1");
        Variable Y2=Variable.create("Y2");
        Variable Y3=Variable.create("Y3");
        Set<DLClause> dlClauses=new HashSet<DLClause>();
        dlClauses.add(DLClause.create(
            new Atom[] { Atom.create(A,X), Atom.create(B,X) },
            new Atom[] { Atom.create(B,X) }
        ));
        dlClauses.add(DLClause.create(
            new Atom[] { Atom.create(EQ_ONE_R_A,Y1,Y2,X) },
            new Atom[] { Atom.create(AT_MOST_ONE_R_A,X),Atom.create(R,X,Y1),Atom.create(A,Y1),Atom.create(R,X,Y2),Atom.create(A,Y2) }
        ));
        dlClauses.add(DLClause.create(
            new Atom[] { Atom.create(EQ_TWO_R_A,Y1,Y2,X),Atom.create(EQ_TWO_R_A,Y2,Y3,X),Atom.create(EQ_TWO_R_A,Y1,Y3,X) },
            new Atom[] { Atom.create(AT_MOST_TWO_R_A,X),
                         Atom.create(R,X,Y1),Atom.create(A,Y1),
                         Atom.create(R,X,Y2),Atom.create(A,Y2),
                         Atom.create(R,X,Y3),Atom.create(A,Y3),
                         Atom.create(NodeIDLessEqualThan.INSTANCE,Y1,Y2),Atom.create(NodeIDLessEqualThan.INSTANCE,Y2,Y3),
                         Atom.create(NodeIDsAscendingOrEqual.create(3),Y1,Y2,Y3)
            }
        ));
        TEST_DL_ONTOLOGY=getTestDLOntology(dlClauses);
    }

    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;
    protected NominalIntroductionManager m_manager;

    public NIRuleTest(String name) {
        super(name);
    }
    protected void setUp() {
        PairWiseDirectBlockingChecker directChecker=new PairWiseDirectBlockingChecker();
        BlockingSignatureCache blockingSignatureCache=new BlockingSignatureCache(directChecker);
        BlockingStrategy blockingStrategy=new AnywhereBlocking(directChecker,blockingSignatureCache);
        ExistentialExpansionStrategy expansionStrategy=new CreationOrderStrategy(blockingStrategy);
        m_tableau=new Tableau(new InterruptFlag(-1),null,expansionStrategy,false,TEST_DL_ONTOLOGY,null,new HashMap<String,Object>());
        m_extensionManager=m_tableau.getExtensionManager();
        m_manager=m_tableau.getNominalIntroductionManager();
    }
    protected void tearDown() {
        m_tableau=null;
        m_extensionManager=null;
        m_manager=null;
    }
    public void testNIRuleDeterministic() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node b=m_tableau.createNewNINode(emptySet);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        Node b11=m_tableau.createNewTreeNode(emptySet,b1);
        Node b111=m_tableau.createNewTreeNode(emptySet,b11);

        m_extensionManager.addAssertion(S,b,b1,emptySet,false);
        m_extensionManager.addAssertion(S,b1,b11,emptySet,false);
        m_extensionManager.addAssertion(S,b11,b111,emptySet,false);
        m_extensionManager.addAssertion(R,a,b11,emptySet,false);
        m_extensionManager.addAssertion(A,b11,emptySet,false);

        // No action until now is expected
        assertEquals(0,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());

        // Now add an annotated equality
        m_manager.addAnnotatedEquality(EQ_ONE_R_A,b11,b11,a,emptySet);

        // The equality is deterministic, so it should be processed straight away
        assertEquals(0,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());

        Node newRoot=getRootNodeFor(a,EQ_ONE_R_A,1);
        assertTrue(newRoot.isActive());

        assertFalse(b11.isActive());
        assertSame(newRoot,b11.getCanonicalNode());

        assertFalse(b111.isActive());
        assertFalse(m_extensionManager.containsAssertion(S,b11,b111));

        assertTrue(m_extensionManager.containsAssertion(S,b1,newRoot));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b1,newRoot));

        assertTrue(m_extensionManager.containsAssertion(R,a,newRoot));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,a,newRoot));
    }
    public void testNondeterministicEquality() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node b=m_tableau.createNewNINode(emptySet);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        Node b11=m_tableau.createNewTreeNode(emptySet,b1);
        Node b111=m_tableau.createNewTreeNode(emptySet,b11);

        m_extensionManager.addAssertion(S,b,b1,emptySet,false);
        m_extensionManager.addAssertion(S,b1,b11,emptySet,false);
        m_extensionManager.addAssertion(S,b11,b111,emptySet,false);
        m_extensionManager.addAssertion(R,a,b11,emptySet,false);
        m_extensionManager.addAssertion(A,b11,emptySet,false);

        // No action until now is expected
        assertEquals(0,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());

        // Now add an annotated equality
        m_manager.addAnnotatedEquality(EQ_TWO_R_A,b11,b11,a,emptySet);

        // The equality is nondeterministic, so it should not be processed straight away
        assertEquals(1,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());
        Node newRoot1=getRootNodeFor(a,EQ_TWO_R_A,1);
        assertNull(newRoot1);
        assertTrue(b11.isActive());
        assertTrue(b111.isActive());

        // This will now start the first possibility
        assertTrue(m_tableau.doIteration());

        newRoot1=getRootNodeFor(a,EQ_TWO_R_A,1);
        assertTrue(newRoot1.isActive());

        assertFalse(b11.isActive());
        assertSame(newRoot1,b11.getCanonicalNode());

        assertFalse(b111.isActive());
        assertFalse(m_extensionManager.containsAssertion(S,b11,b111));

        assertTrue(m_extensionManager.containsAssertion(S,b1,newRoot1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b1,newRoot1),0);

        assertTrue(m_extensionManager.containsAssertion(R,a,newRoot1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,a,newRoot1),0);

        // The following cause a clash and backtrack it
        m_extensionManager.addConceptAssertion(NEG_A,newRoot1,emptySet,false);
        assertTrue(m_tableau.doIteration());

        // The NI rule should have backtracked newRoot1 and merged b11 into newRoot2
        newRoot1=getRootNodeFor(a,EQ_TWO_R_A,1);
        assertNull(newRoot1);

        Node newRoot2=getRootNodeFor(a,EQ_TWO_R_A,2);
        assertTrue(newRoot2.isActive());

        assertFalse(b11.isActive());
        assertSame(newRoot2,b11.getCanonicalNode());

        assertFalse(b111.isActive());
        assertFalse(m_extensionManager.containsAssertion(S,b11,b111));

        // Dependency sets are empty because this is the last choice
        assertTrue(m_extensionManager.containsAssertion(S,b1,newRoot2));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b1,newRoot2));

        assertTrue(m_extensionManager.containsAssertion(R,a,newRoot2));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,a,newRoot2));

        // We cause a clash again; this time it cannot be backtracked
        m_extensionManager.addConceptAssertion(NEG_A,newRoot2,emptySet,false);
        assertFalse(m_tableau.doIteration());
    }
    public void testNIPrunesOneNode() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node b=m_tableau.createNewNINode(emptySet);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        Node b11=m_tableau.createNewTreeNode(emptySet,b1);
        Node b111=m_tableau.createNewTreeNode(emptySet,b11);

        m_extensionManager.addAssertion(S,b,b1,emptySet,false);
        m_extensionManager.addAssertion(S,b1,b11,emptySet,false);
        m_extensionManager.addAssertion(S,b11,b111,emptySet,false);
        m_extensionManager.addAssertion(R,a,b1,emptySet,false);
        m_extensionManager.addAssertion(R,a,b11,emptySet,false);
        m_extensionManager.addAssertion(A,b11,emptySet,false);

        // No action until now is expected
        assertEquals(0,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());

        // Now add an annotated equality
        m_manager.addAnnotatedEquality(EQ_ONE_R_A,b1,b11,a,emptySet);

        // The equality is deterministic, so it should be processed straight away
        assertEquals(0,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());

        // The code in the NI manager ensures that b1 is merged into newRoot, which prunes b11.
        // As a consequence, newRoot will *not* contain A.
        Node newRoot=getRootNodeFor(a,EQ_ONE_R_A,1);
        assertTrue(newRoot.isActive());

        assertTrue(b1.isMerged());
        assertSame(newRoot,b1.getCanonicalNode());

        assertTrue(b11.isPruned());
        assertTrue(b111.isPruned());

        assertFalse(m_extensionManager.containsAssertion(A,newRoot));

        assertTrue(m_extensionManager.containsAssertion(R,a,newRoot));

        assertTrue(m_extensionManager.containsAssertion(S,b,newRoot));
    }
    public void testNIDoesNotPrune() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node b=m_tableau.createNewNINode(emptySet);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        Node b11=m_tableau.createNewTreeNode(emptySet,b1);
        Node c=m_tableau.createNewNINode(emptySet);
        Node c1=m_tableau.createNewTreeNode(emptySet,c);
        Node c11=m_tableau.createNewTreeNode(emptySet,c1);

        m_extensionManager.addAssertion(S,b,b1,emptySet,false);
        m_extensionManager.addAssertion(S,b1,b11,emptySet,false);
        m_extensionManager.addAssertion(A,b1,emptySet,false);
        m_extensionManager.addAssertion(R,a,b1,emptySet,false);

        m_extensionManager.addAssertion(T,c,c1,emptySet,false);
        m_extensionManager.addAssertion(T,c1,c11,emptySet,false);
        m_extensionManager.addAssertion(B,c1,emptySet,false);
        m_extensionManager.addAssertion(R,a,c1,emptySet,false);

        // No action until now is expected
        assertEquals(0,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());

        // Now add an annotated equality
        m_manager.addAnnotatedEquality(EQ_ONE_R_A,b1,c1,a,emptySet);

        // The equality is deterministic, so it should be processed straight away
        assertEquals(0,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());

        // The code in the NI manager ensures that b1 is merged into newRoot,
        // and then c1 is merged into newRoot because of the equality.
        Node newRoot=getRootNodeFor(a,EQ_ONE_R_A,1);
        assertTrue(newRoot.isActive());

        assertTrue(b1.isMerged());
        assertSame(newRoot,b1.getCanonicalNode());

        assertTrue(b11.isPruned());
        assertTrue(c11.isPruned());

        assertTrue(m_extensionManager.containsAssertion(A,newRoot));
        assertTrue(m_extensionManager.containsAssertion(B,newRoot));

        assertTrue(m_extensionManager.containsAssertion(R,a,newRoot));
        assertTrue(m_extensionManager.containsAssertion(S,b,newRoot));
        assertTrue(m_extensionManager.containsAssertion(T,c,newRoot));
    }
    public void testRepeatedNIApplications() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node b=m_tableau.createNewNINode(emptySet);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);

        // Add an S-successor of b.
        m_extensionManager.addAssertion(S,b,b1,emptySet,false);
        m_extensionManager.addAssertion(R,a,b1,emptySet,false);
        m_extensionManager.addAssertion(A,b1,emptySet,false);

        // This should convert b1 into a root node
        m_manager.addAnnotatedEquality(EQ_TWO_R_A,b1,b1,a,emptySet);
        assertNull(getRootNodeFor(a,EQ_TWO_R_A,1));
        assertTrue(m_tableau.doIteration());

        Node a_n1=getRootNodeFor(a,EQ_TWO_R_A,1);
        assertSame(a_n1,b1.getCanonicalNode());
        assertTrue(m_extensionManager.containsAssertion(A,a_n1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(A,a_n1),0);

        // Add an S-successor of b1_n1.
        Node b11=m_tableau.createNewTreeNode(emptySet,a_n1);
        m_extensionManager.addAssertion(S,a_n1,b11,emptySet,false);
        m_extensionManager.addAssertion(R,a,b11,emptySet,false);
        m_extensionManager.addAssertion(A,b11,emptySet,false);

        // This should convert b11 into a root node
        m_manager.addAnnotatedEquality(EQ_TWO_R_A,b11,b11,a,emptySet);
        assertTrue(m_tableau.doIteration());

        assertSame(a_n1,b11.getCanonicalNode());
        assertTrue(m_extensionManager.containsAssertion(S,a_n1,a_n1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,a_n1,a_n1),1);

        // Now backtrack the second choice.
        m_extensionManager.addConceptAssertion(NEG_A,a_n1,m_extensionManager.getAssertionDependencySet(S,a_n1,a_n1),false);
        assertTrue(m_tableau.doIteration());

        // b11 was now rerouted to a_n2.
        Node a_n2=getRootNodeFor(a,EQ_TWO_R_A,2);
        assertNotSame(a_n1,a_n2);

        assertSame(a_n2,b11.getCanonicalNode());
        assertFalse(m_extensionManager.containsAssertion(S,a_n1,a_n1));
        assertTrue(m_extensionManager.containsAssertion(S,a_n1,a_n2));
        assertFalse(m_extensionManager.containsAssertion(S,a_n2,a_n2));
    }
    public void testContentingNIs() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node b=m_tableau.createNewNINode(emptySet);
        Node c=m_tableau.createNewNINode(emptySet);
        Node c1=m_tableau.createNewTreeNode(emptySet,c);
        Node d=m_tableau.createNewNINode(emptySet);
        Node d1=m_tableau.createNewTreeNode(emptySet,d);

        m_extensionManager.addAssertion(T,c,c1,emptySet,false);
        m_extensionManager.addAssertion(A,c1,emptySet,false);
        m_extensionManager.addAssertion(R,c1,a,emptySet,false);
        m_extensionManager.addAssertion(S,c1,b,emptySet,false);

        m_extensionManager.addAssertion(T,d,d1,emptySet,false);
        m_extensionManager.addAssertion(B,d1,emptySet,false);
        m_extensionManager.addAssertion(R,d1,a,emptySet,false);
        m_extensionManager.addAssertion(S,d1,b,emptySet,false);

        // The following equality is nondeterministic, so it is not processed immediately.
        m_manager.addAnnotatedEquality(EQ_TWO_R_A,c1,d1,a,emptySet);
        assertTrue(c1.isActive());
        assertTrue(d1.isActive());

        // The following equality is deterministic, so it is processed immediately.
        m_manager.addAnnotatedEquality(EQ_ONE_S_A,d1,d1,b,emptySet);
        Node b_n1=getRootNodeFor(b,EQ_ONE_S_A,1);
        assertSame(b_n1,d1.getCanonicalNode());

        // We now apply the nondeterminsitic NI rule.
        assertTrue(m_tableau.doIteration());

        // Since d1 was converted to a root node, no new nominal is introduced;
        // rather, c1 is simply merged (deterministically) into b_n1.
        assertNull(getRootNodeFor(a,EQ_TWO_R_A,1));
        assertNull(getRootNodeFor(a,EQ_TWO_R_A,2));
        assertSame(b_n1,c1.getCanonicalNode());
        assertEquals(1,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());

        assertTrue(m_extensionManager.containsAssertion(T,c,b_n1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(T,c,b_n1));
    }
    public void testNIAndPruning() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node a1=m_tableau.createNewTreeNode(emptySet,a);
        Node b=m_tableau.createNewNINode(emptySet);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        Node b11=m_tableau.createNewTreeNode(emptySet,b1);
        Node c=m_tableau.createNewNINode(emptySet);

        m_extensionManager.addAssertion(S,a,a1,emptySet,false);
        m_extensionManager.addAssertion(R,c,a1,emptySet,false);
        m_extensionManager.addAssertion(T,b,b1,emptySet,false);
        m_extensionManager.addAssertion(T,b1,b11,emptySet,false);
        m_extensionManager.addAssertion(R,c,b11,emptySet,false);

        // The following disjunction is nondeterministic, so nothing is done yet.
        m_manager.addAnnotatedEquality(EQ_TWO_R_A,a1,b11,c,emptySet);

        // The following merging prunes b11, which is contained in the previous annotated equality.
        m_extensionManager.addAssertion(Equality.INSTANCE,b1,c,emptySet,false);
        assertTrue(b11.isPruned());

        // Since b11 is pruned, applying the NI rule does nothing.
        assertTrue(m_tableau.doIteration());
        assertTrue(a1.isActive());
        assertEquals(1,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());
    }
    public void testDeterministicRuleApplication() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node a1=m_tableau.createNewTreeNode(emptySet,a);
        Node b=m_tableau.createNewNINode(emptySet);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        Node c=m_tableau.createNewNINode(emptySet);

        m_extensionManager.addAssertion(S,a,a1,emptySet,false);
        m_extensionManager.addAssertion(R,c,a1,emptySet,false);
        m_extensionManager.addAssertion(A,a1,emptySet,false);

        m_extensionManager.addAssertion(S,b,b1,emptySet,false);
        m_extensionManager.addAssertion(R,c,b1,emptySet,false);
        m_extensionManager.addAssertion(A,b1,emptySet,false);

        m_extensionManager.addAssertion(AT_MOST_ONE_R_A,c,emptySet,false);

        // The following should trigger the deterministic NI rule on a1 and b1.
        assertTrue(m_tableau.doIteration());

        Node c_n1=getRootNodeFor(c,EQ_ONE_R_A,1);
        assertSame(c_n1,a1.getCanonicalNode());
        assertSame(c_n1,b1.getCanonicalNode());

        assertTrue(m_extensionManager.containsAssertion(S,a,c_n1));
        assertTrue(m_extensionManager.containsAssertion(S,b,c_n1));
        assertTrue(m_extensionManager.containsAssertion(R,c,c_n1));
    }
    public void testDisjunctionDerivation() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node a1=m_tableau.createNewTreeNode(emptySet,a);
        Node b=m_tableau.createNewNINode(emptySet);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        Node c=m_tableau.createNewNINode(emptySet);

        m_extensionManager.addAssertion(S,a,a1,emptySet,false);
        m_extensionManager.addAssertion(R,c,a1,emptySet,false);
        m_extensionManager.addAssertion(A,a1,emptySet,false);

        m_extensionManager.addAssertion(S,b,b1,emptySet,false);
        m_extensionManager.addAssertion(R,c,b1,emptySet,false);
        m_extensionManager.addAssertion(A,b1,emptySet,false);

        m_extensionManager.addAssertion(AT_MOST_TWO_R_A,c,emptySet,false);

        // The following should generate disjunctions with annotated equalities.
        assertTrue(m_tableau.doIteration());

        assertUnprocessedDisjunctions(false,
            "[2 == 2]@atMost(2 <R> <A>)(5) v [2 == 2]@atMost(2 <R> <A>)(5) v [2 == 2]@atMost(2 <R> <A>)(5)",
            "[4 == 4]@atMost(2 <R> <A>)(5) v [4 == 4]@atMost(2 <R> <A>)(5) v [4 == 4]@atMost(2 <R> <A>)(5)"
        );

        // The following just derive the first disjunction.
        assertTrue(m_tableau.doIteration());

        // The equality was just stored into the NI manager.
        assertEquals(1,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());
        assertTrue(a1.isActive());
        assertTrue(b1.isActive());

        // The following applies the NI rule.
        assertTrue(m_tableau.doIteration());

        // The derivation of this disjunction merged a1 into c_n1.
        Node c_n1=getRootNodeFor(c,EQ_TWO_R_A,1);
        assertSame(c_n1,a1.getCanonicalNode());
        assertFalse(b1.isMerged());

        // Now only the other disjunction is not satisfied.
        assertUnprocessedDisjunctions(true,
            "[4 == 4]@atMost(2 <R> <A>)(5) v [4 == 4]@atMost(2 <R> <A>)(5) v [4 == 4]@atMost(2 <R> <A>)(5)"
        );

        // The following just derive the first disjunction.
        assertTrue(m_tableau.doIteration());

        // The equality was just stored into the NI manager.
        assertEquals(2,m_manager.m_annotatedEqualities.getFirstFreeTupleIndex());
        assertTrue(b1.isActive());

        // The following applies the NI rule.
        assertTrue(m_tableau.doIteration());

        // The derivation of this disjunction merged b1 into c_n1.
        assertSame(c_n1,a1.getCanonicalNode());
        assertSame(c_n1,b1.getCanonicalNode());

        // There are no more unsatisfied disjunctions.
        assertUnprocessedDisjunctions(true);

        // Nothing else is left to do.
        assertFalse(m_tableau.doIteration());
    }
    public void testDisjunctionsInTreePart() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node a1=m_tableau.createNewTreeNode(emptySet,a);
        Node a11=m_tableau.createNewTreeNode(emptySet,a1);
        Node a12=m_tableau.createNewTreeNode(emptySet,a1);

        m_extensionManager.addAssertion(R,a1,a11,emptySet,false);
        m_extensionManager.addAssertion(A,a11,emptySet,false);

        m_extensionManager.addAssertion(R,a1,a12,emptySet,false);
        m_extensionManager.addAssertion(A,a12,emptySet,false);

        m_extensionManager.addAssertion(AT_MOST_TWO_R_A,a1,emptySet,false);

        // The following should not generate any disjunctions.
        // In the tree part, all annotations can be thrown away,
        // so all disjunctions are tautologies.
        assertTrue(m_tableau.doIteration());

        assertUnprocessedDisjunctions(false);
    }
    protected void assertDependencySet(DependencySet dependencySet,int... requiredBranchingPoints) {
        DependencySet control=m_tableau.getDependencySetFactory().emptySet();
        for (int branchingPoint : requiredBranchingPoints)
            control=m_tableau.getDependencySetFactory().addBranchingPoint(control,branchingPoint);
        assertSame(dependencySet,control);
    }
    protected Node getRootNodeFor(Node rootNode,AnnotatedEquality annotatedEquality,int index) {
        int tupleIndex=m_manager.m_newRootNodesIndex.getTupleIndex(new Object[] { rootNode,annotatedEquality,index });
        if (tupleIndex==-1)
            return null;
        else
            return (Node)m_manager.m_newRootNodesTable.getTupleObject(tupleIndex,3);
    }
    protected void assertUnprocessedDisjunctions(boolean onlyNotSatisfied,String... disjunctions) {
        Set<String> actual=new HashSet<String>();
        GroundDisjunction disjunction=m_tableau.getFirstUnprocessedGroundDisjunction();
        while (disjunction!=null) {
            if (!onlyNotSatisfied || !disjunction.isSatisfied(m_tableau))
                actual.add(disjunction.toString());
            disjunction=disjunction.getPreviousGroundDisjunction();
        }
        assertContainsAll(actual,disjunctions);
    }
}
