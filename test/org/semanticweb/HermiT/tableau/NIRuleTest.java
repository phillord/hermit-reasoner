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
import org.semanticweb.HermiT.model.AtMostAbstractRoleGuard;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Variable;

public class NIRuleTest extends AbstractHermiTTest {
    protected static final AtomicConcept A=AtomicConcept.create("A");
    protected static final AtomicConcept B=AtomicConcept.create("B");
    protected static final AtomicNegationConcept NEG_A=AtomicNegationConcept.create(A);
    protected static final AtomicRole R=AtomicRole.createObjectRole("R");
    protected static final InverseRole INV_R=InverseRole.create(R);
    protected static final AtomicRole S=AtomicRole.createObjectRole("S");
    protected static final AtMostAbstractRoleGuard AT_MOST_ONE_R_A=AtMostAbstractRoleGuard.create(1,R,A);
    protected static final AtMostAbstractRoleGuard AT_MOST_TWO_R_A=AtMostAbstractRoleGuard.create(2,R,A);
    protected static final AtMostAbstractRoleGuard AT_MOST_ONE_INV_R_A=AtMostAbstractRoleGuard.create(1,INV_R,A);
    protected static final DLOntology TEST_DL_ONTOLOGY;
    static {
        // One disjunctive clause is needed in order to turn on nondeterminism in the tableau.
        Variable X=Variable.create("X");
        DLClause cl=DLClause.create(new Atom[] { Atom.create(A,X), Atom.create(B,X) },new Atom[] { Atom.create(B,X) });
        Set<DLClause> dlClauses=Collections.singleton(cl);
        Set<Atom> atoms=Collections.emptySet();
        TEST_DL_ONTOLOGY = new DLOntology(
                "opaque:test", // ontology_URI
                dlClauses, // clauses
                atoms, // positive facts
                atoms, // negative facts 
                null, // atomic concepts
                null, // individuals
                null, // role hierarchy
                false, // hasInverseRoles
                false, // hasAtMostRestrictions
                false, // hasNominals
                false, // canUseNIRule
                false, // hasReflexivity
                false); // hasDatatypes;
    }

    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;
    protected NominalIntroductionManager m_manager;

    public NIRuleTest(String name) {
        super(name);
    }
    protected void setUp() {
        BlockingSignatureCache blockingSignatureCache=new BlockingSignatureCache(PairWiseDirectBlockingChecker.INSTANCE);
        BlockingStrategy blockingStrategy=new AnywhereBlocking(PairWiseDirectBlockingChecker.INSTANCE,blockingSignatureCache);
        ExpansionStrategy ExpansionStrategy=new CreationOrderStrategy(blockingStrategy);
        m_tableau=new Tableau(null,ExpansionStrategy,TEST_DL_ONTOLOGY,false,new HashMap<String,Object>());
        m_extensionManager=m_tableau.getExtensionManager();
        m_manager=m_tableau.getNominalIntroductionManager();
    }
    public void testNIRule1() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewRootNode(emptySet,0);
        Node b=m_tableau.createNewRootNode(emptySet,0);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        Node b11=m_tableau.createNewTreeNode(emptySet,b1);
        Node b111=m_tableau.createNewTreeNode(emptySet,b11);
        
        m_extensionManager.addAssertion(AT_MOST_TWO_R_A,a,emptySet);
        m_extensionManager.addAssertion(S,b,b1,emptySet);
        m_extensionManager.addAssertion(S,b1,b11,emptySet);
        m_extensionManager.addAssertion(S,b11,b111,emptySet);

        m_extensionManager.addAssertion(R,a,b11,emptySet);
        assertEquals(0,m_manager.m_targets.getFirstFreeTupleIndex());
        assertEquals(1,a.m_numberOfNIAssertionsFromNode);
        assertEquals(0,a.m_numberOfNIAssertionsToNode);
        assertEquals(0,b11.m_numberOfNIAssertionsFromNode);
        assertEquals(1,b11.m_numberOfNIAssertionsToNode);
        
        m_extensionManager.addAssertion(A,b11,emptySet);
        assertEquals(1,m_manager.m_targets.getFirstFreeTupleIndex());

        // The following call should trigger the creation of the first branching point for the NI-rule
        assertTrue(m_tableau.doIteration());

        Node newRoot1=getRootNodeFor(a,AT_MOST_TWO_R_A,1);
        Node newRoot2=getRootNodeFor(a,AT_MOST_TWO_R_A,2);
        assertTrue(newRoot1.isActive());
        assertNull(newRoot2);

        assertFalse(b11.isActive());
        assertSame(newRoot1,b11.getCanonicalNode());

        assertFalse(b111.isActive());
        assertFalse(m_extensionManager.containsAssertion(S,b11,b111));

        assertTrue(m_extensionManager.containsAssertion(S,b1,newRoot1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b1,newRoot1),0);

        assertTrue(m_extensionManager.containsAssertion(R,a,newRoot1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,a,newRoot1),0);
        
        // The following call causes a clash.
        m_extensionManager.addConceptAssertion(NEG_A,newRoot1,emptySet);
        assertDependencySet(m_extensionManager.getClashDependencySet(),0);
        
        // The following call backtracks the clash and starts a second choice point.
        // Since this is the last choice point, the dependency sets will not contain the current branching point 1.
        assertTrue(m_tableau.doIteration());
        
        newRoot1=getRootNodeFor(a,AT_MOST_TWO_R_A,1);
        assertNull(newRoot1);
        
        newRoot2=getRootNodeFor(a,AT_MOST_TWO_R_A,2);
        assertTrue(newRoot2.isActive());
        
        assertTrue(m_extensionManager.containsAssertion(S,b1,newRoot2));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b1,newRoot2));

        assertTrue(m_extensionManager.containsAssertion(R,a,newRoot2));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,a,newRoot2));

        // The following call causes a clash.
        m_extensionManager.addConceptAssertion(NEG_A,newRoot2,emptySet);
        assertDependencySet(m_extensionManager.getClashDependencySet());
        
        // The following call backtracks the clash. Since we are backtracking the root choice point,
        // there is nothing else to do.
        assertFalse(m_tableau.doIteration());
        
        // The tableau is left in a messed up state; that is, it is not backtracked.
        assertTrue(m_extensionManager.containsAssertion(S,b1,newRoot2));
        assertTrue(m_extensionManager.containsAssertion(R,a,newRoot2));
    }
    public void testNIRule2() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewRootNode(emptySet,0);
        Node b=m_tableau.createNewRootNode(emptySet,0);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        
        m_extensionManager.addAssertion(AT_MOST_ONE_R_A,a,emptySet);
        m_extensionManager.addAssertion(S,b,b1,emptySet);
        m_extensionManager.addAssertion(R,a,b1,emptySet);
        m_extensionManager.addAssertion(A,b1,emptySet);
        
        // The following call should trigger the NI-rule, which is deterministic in this case.
        assertTrue(m_tableau.doIteration());

        Node newRoot1=getRootNodeFor(a,AT_MOST_ONE_R_A,1);
        assertTrue(newRoot1.isActive());
        
        assertFalse(b1.isActive());
        assertSame(newRoot1,b1.getCanonicalNode());

        assertTrue(m_extensionManager.containsAssertion(S,b,newRoot1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b,newRoot1));
        assertTrue(m_extensionManager.containsAssertion(R,a,newRoot1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,a,newRoot1));
        
        // The following causes a clash, which cannot be backtracked.
        m_extensionManager.addConceptAssertion(NEG_A,newRoot1,emptySet);
        assertFalse(m_tableau.doIteration());
    }
    public void testNIRule3() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewRootNode(emptySet,0);
        Node b=m_tableau.createNewRootNode(emptySet,0);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        
        // This test is the same as the previous one, with the difference in the order in which the assertions are added to the tableau.
        // This actually exercises different parts of the NominalIntroductionManager code. The net effect, however, should be the same
        m_extensionManager.addAssertion(AT_MOST_ONE_R_A,a,emptySet);
        m_extensionManager.addAssertion(S,b,b1,emptySet);
        m_extensionManager.addAssertion(A,b1,emptySet);
        m_extensionManager.addAssertion(R,a,b1,emptySet);
        
        // The following call should trigger the NI-rule, which is deterministic in this case.
        assertTrue(m_tableau.doIteration());

        Node newRoot1=getRootNodeFor(a,AT_MOST_ONE_R_A,1);
        assertTrue(newRoot1.isActive());
        
        assertFalse(b1.isActive());
        assertSame(newRoot1,b1.getCanonicalNode());

        assertTrue(m_extensionManager.containsAssertion(S,b,newRoot1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b,newRoot1));
        assertTrue(m_extensionManager.containsAssertion(R,a,newRoot1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,a,newRoot1));
        
        // The following causes a clash, which cannot be backtracked.
        m_extensionManager.addConceptAssertion(NEG_A,newRoot1,emptySet);
        assertFalse(m_tableau.doIteration());
    }
    public void testNIRule4() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewRootNode(emptySet,0);
        Node b=m_tableau.createNewRootNode(emptySet,0);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        
        // The following test puts two at-most guards on a; hence, there are two possible ways in which
        // the NI-rule could be applied.
        m_extensionManager.addAssertion(AT_MOST_ONE_R_A,a,emptySet);
        m_extensionManager.addAssertion(AT_MOST_TWO_R_A,a,emptySet);
        m_extensionManager.addAssertion(S,b,b1,emptySet);
        m_extensionManager.addAssertion(A,b1,emptySet);
        m_extensionManager.addAssertion(R,a,b1,emptySet);
        
        // The following call should trigger the NI-rule, which is deterministic in this case.
        assertTrue(m_tableau.doIteration());

        Node newRoot1=getRootNodeFor(a,AT_MOST_ONE_R_A,1);
        Node newRoot2=getRootNodeFor(a,AT_MOST_TWO_R_A,1);
        assertTrue(newRoot1!=null || newRoot2!=null);
        assertTrue((newRoot1==null && newRoot2!=null) || (newRoot1!=null && newRoot2==null));
        Node newRoot=(newRoot1!=null ? newRoot1 : newRoot2);
        
        assertTrue(newRoot.isActive());
        assertFalse(b1.isActive());
        assertSame(newRoot,b1.getCanonicalNode());
        assertTrue(m_extensionManager.containsAssertion(S,b,newRoot));
        assertTrue(m_extensionManager.containsAssertion(R,a,newRoot));
    }
    public void testNIRule5() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewRootNode(emptySet,0);
        Node b=m_tableau.createNewRootNode(emptySet,0);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        
        // We now test triggering the rule using guards with inverse roles.
        m_extensionManager.addAssertion(AT_MOST_ONE_INV_R_A,a,emptySet);
        m_extensionManager.addAssertion(S,b,b1,emptySet);
        m_extensionManager.addAssertion(A,b1,emptySet);
        m_extensionManager.addAssertion(R,b1,a,emptySet);
        
        // The following call should trigger the NI-rule, which is deterministic in this case.
        assertTrue(m_tableau.doIteration());

        Node newRoot1=getRootNodeFor(a,AT_MOST_ONE_INV_R_A,1);
        assertTrue(newRoot1.isActive());
        
        assertFalse(b1.isActive());
        assertSame(newRoot1,b1.getCanonicalNode());

        assertTrue(m_extensionManager.containsAssertion(S,b,newRoot1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b,newRoot1));
        assertTrue(m_extensionManager.containsAssertion(R,newRoot1,a));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,newRoot1,a));
        
        // The following causes a clash, which cannot be backtracked.
        m_extensionManager.addConceptAssertion(NEG_A,newRoot1,emptySet);
        assertFalse(m_tableau.doIteration());
    }
    public void testNIRule6() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewRootNode(emptySet,0);
        Node b=m_tableau.createNewRootNode(emptySet,0);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        
        // This test is the same as the previous one, save for the fact that it triggers the rule in different way.
        m_extensionManager.addAssertion(AT_MOST_ONE_INV_R_A,a,emptySet);
        m_extensionManager.addAssertion(S,b,b1,emptySet);
        m_extensionManager.addAssertion(R,b1,a,emptySet);
        m_extensionManager.addAssertion(A,b1,emptySet);
        
        // The following call should trigger the NI-rule, which is deterministic in this case.
        assertTrue(m_tableau.doIteration());

        Node newRoot1=getRootNodeFor(a,AT_MOST_ONE_INV_R_A,1);
        assertTrue(newRoot1.isActive());
        
        assertFalse(b1.isActive());
        assertSame(newRoot1,b1.getCanonicalNode());

        assertTrue(m_extensionManager.containsAssertion(S,b,newRoot1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b,newRoot1));
        assertTrue(m_extensionManager.containsAssertion(R,newRoot1,a));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,newRoot1,a));
        
        // The following causes a clash, which cannot be backtracked.
        m_extensionManager.addConceptAssertion(NEG_A,newRoot1,emptySet);
        assertFalse(m_tableau.doIteration());
    }
    public void testNIRule7() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewRootNode(emptySet,0);
        Node b=m_tableau.createNewRootNode(emptySet,0);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        
        // This test is the same as the previous one, save for the fact that it triggers the rule in different way.
        m_extensionManager.addAssertion(S,b,b1,emptySet);
        m_extensionManager.addAssertion(R,b1,a,emptySet);
        m_extensionManager.addAssertion(A,b1,emptySet);
        m_extensionManager.addAssertion(AT_MOST_ONE_INV_R_A,a,emptySet);
        
        // The following call should trigger the NI-rule, which is deterministic in this case.
        assertTrue(m_tableau.doIteration());

        Node newRoot1=getRootNodeFor(a,AT_MOST_ONE_INV_R_A,1);
        assertTrue(newRoot1.isActive());
        
        assertFalse(b1.isActive());
        assertSame(newRoot1,b1.getCanonicalNode());

        assertTrue(m_extensionManager.containsAssertion(S,b,newRoot1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b,newRoot1));
        assertTrue(m_extensionManager.containsAssertion(R,newRoot1,a));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,newRoot1,a));
        
        // The following causes a clash, which cannot be backtracked.
        m_extensionManager.addConceptAssertion(NEG_A,newRoot1,emptySet);
        assertFalse(m_tableau.doIteration());
    }
    public void testNIRule8() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewRootNode(emptySet,0);
        Node b=m_tableau.createNewRootNode(emptySet,0);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);

        Node c=m_tableau.createNewRootNode(emptySet,0);
        Node d=m_tableau.createNewRootNode(emptySet,0);
        Node d1=m_tableau.createNewTreeNode(emptySet,d);

        // This test checks backtracking of the NI-rule and the NominalIntroductionManager.

        // The following section starts the first choice point.
        m_extensionManager.addAssertion(AT_MOST_TWO_R_A,a,emptySet);
        m_extensionManager.addAssertion(S,b,b1,emptySet);
        m_extensionManager.addAssertion(R,a,b1,emptySet);
        m_extensionManager.addAssertion(A,b1,emptySet);
        assertTrue(m_tableau.doIteration());
        Node new1=getRootNodeFor(a,AT_MOST_TWO_R_A,1);
        
        assertTrue(new1.isActive());
        assertTrue(m_extensionManager.containsAssertion(S,b,new1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b,new1),0);
        assertTrue(m_extensionManager.containsAssertion(R,a,new1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,a,new1),0);
        
        // The following section starts the second choice point.
        m_extensionManager.addAssertion(AT_MOST_TWO_R_A,c,emptySet);
        m_extensionManager.addAssertion(S,d,d1,emptySet);
        m_extensionManager.addAssertion(R,c,d1,emptySet);
        m_extensionManager.addAssertion(A,d1,emptySet);
        assertTrue(m_tableau.doIteration());
        Node new2=getRootNodeFor(c,AT_MOST_TWO_R_A,1);

        assertTrue(new2.isActive());
        assertTrue(m_extensionManager.containsAssertion(S,d,new2));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,d,new2),1);
        assertTrue(m_extensionManager.containsAssertion(R,c,new2));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,c,new2),1);
        
        // We now backtrack to first choice point. This should correctly backtrack out of the second choice point.
        m_extensionManager.addConceptAssertion(NEG_A,new1,emptySet);
        assertTrue(m_tableau.doIteration());
        
        assertFalse(new2.isActive());
        assertNull(new2.getNextTableauNode());
        assertFalse(m_extensionManager.containsAssertion(S,d,new2));
        assertFalse(m_extensionManager.containsAssertion(R,c,new2));
        
        assertEquals(1,m_manager.m_targets.getFirstFreeTupleIndex());
        assertEquals(1,m_manager.m_firstUnprocessedTarget);

        // Make sure all pending actions have been processed.
        assertTrue(m_tableau.doIteration());
        assertFalse(m_tableau.doIteration());
        
        assertFalse(new2.isActive());
        
        // We now again cause the NI-rule to be applied to c.
        m_extensionManager.addAssertion(AT_MOST_TWO_R_A,c,emptySet);
        m_extensionManager.addAssertion(S,d,d1,emptySet);
        m_extensionManager.addAssertion(R,c,d1,emptySet);
        m_extensionManager.addAssertion(A,d1,emptySet);
        assertTrue(m_tableau.doIteration());
        Node new2second=getRootNodeFor(c,AT_MOST_TWO_R_A,1);
        
        assertSame(new2,new2second);
    }
    public void testNIRule9() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewRootNode(emptySet,0);
        Node b=m_tableau.createNewRootNode(emptySet,0);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);

        Node c=m_tableau.createNewRootNode(emptySet,0);
        Node d=m_tableau.createNewRootNode(emptySet,0);
        Node d1=m_tableau.createNewTreeNode(emptySet,d);

        // This test is similar to the previous one, but the order of the events is different.

        // The following code creates two individuals that need an application of the NI-rule.
        m_extensionManager.addAssertion(AT_MOST_TWO_R_A,a,emptySet);
        m_extensionManager.addAssertion(S,b,b1,emptySet);
        m_extensionManager.addAssertion(R,a,b1,emptySet);
        m_extensionManager.addAssertion(A,b1,emptySet);
        
        m_extensionManager.addAssertion(AT_MOST_TWO_R_A,c,emptySet);
        m_extensionManager.addAssertion(S,d,d1,emptySet);
        m_extensionManager.addAssertion(R,c,d1,emptySet);
        m_extensionManager.addAssertion(A,d1,emptySet);

        // New now process both candidates. Due to implementation side-effects, the individuals will be processed
        // in the order in which NI-rule conditions were detected.
        assertTrue(m_tableau.doIteration());

        Node new1=getRootNodeFor(a,AT_MOST_TWO_R_A,1);
        assertTrue(new1.isActive());
        assertTrue(m_extensionManager.containsAssertion(S,b,new1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,b,new1),0);
        assertTrue(m_extensionManager.containsAssertion(R,a,new1));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,a,new1),0);

        Node new2=getRootNodeFor(c,AT_MOST_TWO_R_A,1);
        assertTrue(new2.isActive());
        assertTrue(m_extensionManager.containsAssertion(S,d,new2));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(S,d,new2),1);
        assertTrue(m_extensionManager.containsAssertion(R,c,new2));
        assertDependencySet(m_extensionManager.getAssertionDependencySet(R,c,new2),1);
        
        // We now backtrack to first choice point.
        m_extensionManager.addConceptAssertion(NEG_A,new1,emptySet);
        assertTrue(m_tableau.doIteration());
        
        new1=getRootNodeFor(a,AT_MOST_TWO_R_A,2);
        assertTrue(new1.isActive());
        
        // The second individual should still be applicable.
        assertEquals(2,m_manager.m_targets.getFirstFreeTupleIndex());
        assertEquals(1,m_manager.m_firstUnprocessedTarget);
        assertFalse(new2.isActive());
        assertFalse(m_extensionManager.containsAssertion(S,d,new2));
        assertFalse(m_extensionManager.containsAssertion(R,c,new2));
        
        // Process now the second individual.
        assertTrue(m_tableau.doIteration());
        
        assertTrue(new2.isActive());
        assertTrue(m_extensionManager.containsAssertion(S,d,new2));
        assertTrue(m_extensionManager.containsAssertion(R,c,new2));
    }
    public void testNIRule10() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewRootNode(emptySet,0);
        Node b=m_tableau.createNewRootNode(emptySet,0);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        Node c=m_tableau.createNewRootNode(emptySet,0);
        Node c1=m_tableau.createNewTreeNode(emptySet,c);

        m_extensionManager.addAssertion(AT_MOST_TWO_R_A,a,emptySet);
        m_extensionManager.addAssertion(S,b,b1,emptySet);
        m_extensionManager.addAssertion(A,b1,emptySet);
        m_extensionManager.addAssertion(R,a,b1,emptySet);
        m_extensionManager.addAssertion(S,c,c1,emptySet);
        m_extensionManager.addAssertion(A,c1,emptySet);
        m_extensionManager.addAssertion(R,a,c1,emptySet);

        // This now triggers the NI-rule.
        assertTrue(m_tableau.doIteration());
        
        Node new1=getRootNodeFor(a,AT_MOST_TWO_R_A,1);
        Node new2=getRootNodeFor(a,AT_MOST_TWO_R_A,2);
        assertTrue(m_extensionManager.containsAssertion(R,a,new1));
        assertTrue(m_extensionManager.containsAssertion(S,b,new1));
        assertTrue(m_extensionManager.containsAssertion(S,c,new1));
        assertNull(new2);
        
        // This will ensure backtracking our of the choice for c1
        m_extensionManager.addConceptAssertion(NEG_A,new1,m_tableau.getDependencySetFactory().addBranchingPoint(emptySet,m_tableau.getCurrentBranchingPoint().getLevel()));
        assertTrue(m_tableau.doIteration());
        
        new1=getRootNodeFor(a,AT_MOST_TWO_R_A,1);
        assertTrue(m_extensionManager.containsAssertion(R,a,new1));
        assertTrue(m_extensionManager.containsAssertion(S,b,new1));

        new2=getRootNodeFor(a,AT_MOST_TWO_R_A,2);
        assertTrue(m_extensionManager.containsAssertion(R,a,new2));
        assertTrue(m_extensionManager.containsAssertion(S,c,new2));
    }
    protected void assertDependencySet(DependencySet dependencySet,int... requiredBranchingPoints) {
        DependencySet control=m_tableau.getDependencySetFactory().emptySet();
        for (int branchingPoint : requiredBranchingPoints)
            control=m_tableau.getDependencySetFactory().addBranchingPoint(control,branchingPoint);
        assertSame(dependencySet,control);
    }
    protected Node getRootNodeFor(Node rootNode,AtMostAbstractRoleGuard atMostAbstractRoleGuard,int index) {
        int tupleIndex=m_manager.m_newRootNodesIndex.getTupleIndex(new Object[] { rootNode,atMostAbstractRoleGuard,index });
        if (tupleIndex==-1)
            return null;
        else
            return (Node)m_manager.m_newRootNodesTable.getTupleObject(tupleIndex,3);
    }
}
