package org.semanticweb.HermiT.tableau;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.blocking.AnywhereValidatedBlocking;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.BlockingValidator;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.ValidatedSingleDirectBlockingChecker;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Variable;

public class BlockingValidatorTest extends AbstractReasonerInternalsTest {
    protected final AtomicConcept A=AtomicConcept.create("A");
    protected final AtomicConcept B=AtomicConcept.create("B");
    protected final AtomicConcept C=AtomicConcept.create("C");
    protected final AtomicConcept D=AtomicConcept.create("D");
    protected final AtomicConcept E=AtomicConcept.create("E");
    protected final AtomicRole R=AtomicRole.create("R");
    protected final InverseRole INVR=InverseRole.create(R);
    protected final AtomicRole S=AtomicRole.create("S");
    protected final AtomicRole T=AtomicRole.create("T");
    protected final AtLeastConcept ATLEAST2RA=AtLeastConcept.create(2,R,A);
    protected final AtLeastConcept ATLEAST2TA=AtLeastConcept.create(2,T,A);
    protected final AtLeastConcept ATLEAST2TD=AtLeastConcept.create(2,T,D);
    protected final AtLeastConcept ATLEAST2INVRB=AtLeastConcept.create(2,INVR,B);
    protected final AtLeastConcept ATLEAST1INVRE=AtLeastConcept.create(1,INVR,E);
    protected final AtLeastConcept ATLEAST1SA=AtLeastConcept.create(1,S,A);
    protected final AtLeastConcept ATLEAST1SB=AtLeastConcept.create(1,S,B);
    protected final AtLeastConcept ATLEAST1RC=AtLeastConcept.create(1,R,C);
    protected final AtLeastConcept ATLEAST1TD=AtLeastConcept.create(1,T,D);
    protected final AtLeastConcept ATLEAST1INVRB=AtLeastConcept.create(1,INVR,B);
    protected DLOntology TEST_DL_ONTOLOGY;
    protected final Variable X=Variable.create("X");
    protected final Variable Y=Variable.create("Y");
    protected final Variable Y1=Variable.create("Y1");
    protected final Variable Y2=Variable.create("Y2");

    protected Tableau m_tableau;
    protected BlockingStrategy m_blockingStrategy;
    protected ExtensionManager m_extensionManager;

    public BlockingValidatorTest(String name) {
        super(name);
    }
    public void testOneInvalidBlock() {
        Set<DLClause> dlClauses=new HashSet<DLClause>();
        DLClause cl=DLClause.create(new Atom[] { Atom.create(ATLEAST2RA,X) }, new Atom[] { Atom.create(B,X) });
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST2INVRB,X) }, new Atom[] { Atom.create(A,X) });
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST1SA,X) }, new Atom[] { Atom.create(C,X) });
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST2RA,X) }, new Atom[] { Atom.create(C,X) });
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST1INVRE,X) }, new Atom[] { Atom.create(D,X) });
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(B,X) }, new Atom[] { Atom.create(E,X) });
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(D,X) }, new Atom[] { Atom.create(R,Y,X), Atom.create(B,Y) });
        dlClauses.add(cl);
        //  [Y1 == Y2]@atMost(1 <r> <D>)(X) :- <r>(X,Y1), <D>(Y1), <r>(X,Y2), <D>(Y2), <C>(X)
        cl=DLClause.create(new Atom[] { Atom.create(AnnotatedEquality.create(1, R, D),Y1,Y2,X) }, new Atom[] { Atom.create(C,X),Atom.create(R,X,Y1),Atom.create(D,Y1),Atom.create(R,X,Y2),Atom.create(D,Y2) });
        dlClauses.add(cl);
        TEST_DL_ONTOLOGY=getTestDLOntology(dlClauses);

        DirectBlockingChecker directBlockingChecker=new ValidatedSingleDirectBlockingChecker(TEST_DL_ONTOLOGY.hasInverseRoles());
        m_blockingStrategy=new AnywhereValidatedBlocking(directBlockingChecker,true,true);
        ExistentialExpansionStrategy ExpansionStrategy=new CreationOrderStrategy(m_blockingStrategy);
        m_tableau=new Tableau(new InterruptFlag(-1),null,ExpansionStrategy,false,TEST_DL_ONTOLOGY,null,new HashMap<String,Object>());
        m_extensionManager=m_tableau.getExtensionManager();

        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);          // 1
        Node b=m_tableau.createNewNINode(emptySet);          // 2
        Node a1=m_tableau.createNewTreeNode(emptySet,a);     // 3
        Node a2=m_tableau.createNewTreeNode(emptySet,a);     // 4
        Node a11=m_tableau.createNewTreeNode(emptySet,a1);   // 5
        Node a111=m_tableau.createNewTreeNode(emptySet,a11); // 6
        Node b1=m_tableau.createNewTreeNode(emptySet,b);     // 7
        Node b2=m_tableau.createNewTreeNode(emptySet,b);     // 8
        Node b3=m_tableau.createNewTreeNode(emptySet,b);     // 9
        Node a12=m_tableau.createNewTreeNode(emptySet,a1);   //10
        Node a121=m_tableau.createNewTreeNode(emptySet,a12); //11

        m_extensionManager.addAssertion(R,a,a1,emptySet,true);
        m_extensionManager.addAssertion(R,a,a2,emptySet,true);
        m_extensionManager.addAssertion(R,a11,a1,emptySet,true);
        m_extensionManager.addAssertion(R,a11,a111,emptySet,true);
        m_extensionManager.addAssertion(S,b,b1,emptySet,true);
        m_extensionManager.addAssertion(R,b,b2,emptySet,true);
        m_extensionManager.addAssertion(R,b,b3,emptySet,true);
        m_extensionManager.addAssertion(R,a12,a1,emptySet,true);
        m_extensionManager.addAssertion(R,a12,a121,emptySet,true);

        m_extensionManager.addConceptAssertion(B,a,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2RA,a,emptySet,false);

        m_extensionManager.addConceptAssertion(C,b,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST1SA,b,emptySet,false);
        m_extensionManager.addConceptAssertion(ATLEAST2RA,b,emptySet,false);

        m_extensionManager.addConceptAssertion(A,a1,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2INVRB,a1,emptySet,false);
        m_extensionManager.addConceptAssertion(D,a1,emptySet,false);
        m_extensionManager.addConceptAssertion(ATLEAST1INVRE,a1,emptySet,false);

        m_extensionManager.addConceptAssertion(A,a2,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2INVRB,a2,emptySet,false);
        m_extensionManager.addConceptAssertion(D,a2,emptySet,false);
        m_extensionManager.addConceptAssertion(ATLEAST1INVRE,a2,emptySet,false);

        m_extensionManager.addConceptAssertion(B,a11,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2RA,a11,emptySet,false);

        m_extensionManager.addConceptAssertion(A,a111,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2INVRB,a111,emptySet,false);
        m_extensionManager.addConceptAssertion(D,a111,emptySet,false);
        m_extensionManager.addConceptAssertion(ATLEAST1INVRE,a111,emptySet,false);

        m_extensionManager.addConceptAssertion(A,b1,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2INVRB,b1,emptySet,false);

        m_extensionManager.addConceptAssertion(A,b2,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2INVRB,b2,emptySet,false);

        m_extensionManager.addConceptAssertion(A,b3,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2INVRB,b3,emptySet,false);

        m_extensionManager.addConceptAssertion(E,a12,emptySet,true);
        m_extensionManager.addConceptAssertion(B,a12,emptySet,false);
        m_extensionManager.addConceptAssertion(ATLEAST2RA,a12,emptySet,false);

        m_extensionManager.addConceptAssertion(A,a121,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2INVRB,a121,emptySet,false);
        m_extensionManager.addConceptAssertion(D,a121,emptySet,false);
        m_extensionManager.addConceptAssertion(ATLEAST1INVRE,a121,emptySet,false);

        assertFalse(m_extensionManager.containsClash());
        m_blockingStrategy.computeBlocking(false);
        assertTrue(a111.isDirectlyBlocked() && a111.getBlocker()==a1);
        assertTrue(a2.isDirectlyBlocked() && a2.getBlocker()==a1);
        assertTrue(b1.isDirectlyBlocked() && b1.getBlocker()==a1);
        assertTrue(b2.isDirectlyBlocked() && b2.getBlocker()==a1);
        assertTrue(b3.isDirectlyBlocked() && b3.getBlocker()==a1);
        assertTrue(a121.isDirectlyBlocked() && a121.getBlocker()==a1);
        assertFalse(a.isBlocked());
        assertFalse(b.isBlocked());
        assertFalse(a1.isBlocked());
        assertFalse(a11.isBlocked());
        assertFalse(a12.isBlocked());
        BlockingValidator validator=new BlockingValidator(m_tableau,m_tableau.getPermanentDLOntology().getDLClauses());
        assertTrue(validator.isBlockValid(a2));
        assertTrue(validator.isBlockValid(a111));
        assertTrue(validator.isBlockValid(b1));
        assertTrue(validator.isBlockValid(b2)!=validator.isBlockValid(b3));
        assertTrue(validator.isBlockValid(a121));
    }
    public void testInvalidBlockWithAnnotatedEqualities() {
        Set<DLClause> dlClauses=new HashSet<DLClause>();
        DLClause cl=DLClause.create(new Atom[] { Atom.create(ATLEAST1SB,X) }, new Atom[] { Atom.create(A,X) });
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST1INVRB,X) }, new Atom[] { Atom.create(A,X) });
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST1RC,X) }, new Atom[] { Atom.create(S,Y,X), Atom.create(A,Y) });
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST1TD,X) }, new Atom[] { Atom.create(B,X) });
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST1INVRB,X) }, new Atom[] { Atom.create(E,X) });
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(C,X) }, new Atom[] { Atom.create(E,X) });
        dlClauses.add(cl);
        // B -> <= 1 r.C
        //  [Y1 == Y2]@atMost(1 <r> <C>)(X) :- <r>(X,Y1), <C>(Y1), <r>(X,Y2), <C>(Y2), <B>(X)
        cl=DLClause.create(new Atom[] { Atom.create(AnnotatedEquality.create(1, R, C),Y1,Y2,X) }, new Atom[] { Atom.create(B,X),Atom.create(R,X,Y1),Atom.create(C,Y1),Atom.create(R,X,Y2),Atom.create(C,Y2) });
        dlClauses.add(cl);
        TEST_DL_ONTOLOGY=getTestDLOntology(dlClauses);

        DirectBlockingChecker directBlockingChecker=new ValidatedSingleDirectBlockingChecker(TEST_DL_ONTOLOGY.hasInverseRoles());
        m_blockingStrategy=new AnywhereValidatedBlocking(directBlockingChecker,true,true);
        ExistentialExpansionStrategy ExpansionStrategy=new CreationOrderStrategy(m_blockingStrategy);
        m_tableau=new Tableau(new InterruptFlag(-1),null,ExpansionStrategy,false,TEST_DL_ONTOLOGY,null,new HashMap<String,Object>());
        m_extensionManager=m_tableau.getExtensionManager();

        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);          // 1
        Node a1=m_tableau.createNewTreeNode(emptySet,a);     // 2
        Node a2=m_tableau.createNewTreeNode(emptySet,a);     // 3
        Node a11=m_tableau.createNewTreeNode(emptySet,a1);   // 4
        Node a12=m_tableau.createNewTreeNode(emptySet,a1);   // 5

        m_extensionManager.addAssertion(S,a,a1,emptySet,true);
        m_extensionManager.addAssertion(R,a2,a,emptySet,true);
        m_extensionManager.addAssertion(R,a1,a11,emptySet,true);
        m_extensionManager.addAssertion(T,a1,a12,emptySet,true);

        m_extensionManager.addConceptAssertion(A,a,emptySet,true);
        m_extensionManager.addConceptAssertion(C,a,emptySet,false);
        m_extensionManager.addConceptAssertion(ATLEAST1SB,a,emptySet,false);
        m_extensionManager.addConceptAssertion(ATLEAST1INVRB,a,emptySet,false);

        m_extensionManager.addConceptAssertion(B,a1,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST1TD,a1,emptySet,false);

        m_extensionManager.addConceptAssertion(B,a2,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST1TD,a2,emptySet,false);

        m_extensionManager.addConceptAssertion(C,a11,emptySet,true);

        m_extensionManager.addConceptAssertion(D,a12,emptySet,true);

        assertFalse(m_extensionManager.containsClash());
        m_blockingStrategy.computeBlocking(false);
        assertTrue(a2.isDirectlyBlocked() && a2.getBlocker()==a1);
        assertFalse(a.isBlocked());
        assertFalse(a1.isBlocked());
        assertFalse(a11.isBlocked());
        assertFalse(a12.isBlocked());
        BlockingValidator validator=new BlockingValidator(m_tableau,m_tableau.getPermanentDLOntology().getDLClauses());
        assertFalse(validator.isBlockValid(a2));
    }
    protected void assertLabel(Node node,Concept... expected) {
        assertLabel(m_tableau,node,expected);
    }
}
