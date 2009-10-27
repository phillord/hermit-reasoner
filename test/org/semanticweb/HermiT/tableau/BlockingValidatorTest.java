package org.semanticweb.HermiT.tableau;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.blocking.AnywhereValidatedBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.BlockingValidator;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.ValidatedDirectBlockingChecker;
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
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.model.DLClause.ClauseType;

public class BlockingValidatorTest extends AbstractReasonerInternalsTest {
    protected static final AtomicConcept A=AtomicConcept.create("A");
    protected static final AtomicConcept B=AtomicConcept.create("B");
    protected static final AtomicConcept C=AtomicConcept.create("C");
    protected static final AtomicConcept D=AtomicConcept.create("D");
    protected static final AtomicConcept E=AtomicConcept.create("E");
    protected static final AtomicRole R=AtomicRole.create("R");
    protected static final InverseRole INVR=InverseRole.create(R);
    protected static final AtomicRole S=AtomicRole.create("S");
    protected static final AtomicRole T=AtomicRole.create("T");
    protected static final AtLeastConcept ATLEAST2RA=AtLeastConcept.create(2,R,A);
    protected static final AtLeastConcept ATLEAST2TA=AtLeastConcept.create(2,T,A);
    protected static final AtLeastConcept ATLEAST2TD=AtLeastConcept.create(2,T,D);
    protected static final AtLeastConcept ATLEAST2INVRB=AtLeastConcept.create(2,INVR,B);
    protected static final AtLeastConcept ATLEAST1INVRE=AtLeastConcept.create(1,INVR,E);
    protected static final AtLeastConcept ATLEAST1SA=AtLeastConcept.create(1,S,A);
    protected static final DLOntology TEST_DL_ONTOLOGY;
    static {
        Variable X=Variable.create("X");
        Variable Y=Variable.create("Y");
        Variable Y1=Variable.create("Y1");
        Variable Y2=Variable.create("Y2");
        Set<DLClause> dlClauses=new HashSet<DLClause>();
        DLClause cl=DLClause.create(new Atom[] { Atom.create(ATLEAST2RA,X) }, new Atom[] { Atom.create(B,X) },ClauseType.CONCEPT_INCLUSION);
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST2INVRB,X) }, new Atom[] { Atom.create(A,X) },ClauseType.CONCEPT_INCLUSION);
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST1SA,X) }, new Atom[] { Atom.create(C,X) },ClauseType.CONCEPT_INCLUSION);
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST2RA,X) }, new Atom[] { Atom.create(C,X) },ClauseType.CONCEPT_INCLUSION);
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(ATLEAST1INVRE,X) }, new Atom[] { Atom.create(D,X) },ClauseType.CONCEPT_INCLUSION);
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(B,X) }, new Atom[] { Atom.create(E,X) },ClauseType.CONCEPT_INCLUSION);
        dlClauses.add(cl);
        cl=DLClause.create(new Atom[] { Atom.create(D,X) }, new Atom[] { Atom.create(R,Y,X), Atom.create(B,Y) },ClauseType.CONCEPT_INCLUSION);
        dlClauses.add(cl);
        //  [Y1 == Y2]@atMost(1 <r> <D>)(X) :- <r>(X,Y1), <D>(Y1), <r>(X,Y2), <D>(Y2), <C>(X)
        cl=DLClause.create(new Atom[] { Atom.create(AnnotatedEquality.create(1, R, D),Y1,Y2,X) }, new Atom[] { Atom.create(C,X),Atom.create(R,X,Y1),Atom.create(D,Y1),Atom.create(R,X,Y2),Atom.create(D,Y2) },ClauseType.CONCEPT_INCLUSION);
        dlClauses.add(cl);
        Set<Atom> atoms=Collections.emptySet();
        TEST_DL_ONTOLOGY = new DLOntology(
                "opaque:test", // ontology_URI
                dlClauses, // clauses
                atoms, // positive facts
                atoms, // negative facts 
                null, // atomic concepts
                null, // complex role inclusions
                null, // object roles
                null, // data roles
                null, // custom datatype definitions
                null, // individuals
                true, // hasInverseRoles
                false, // hasAtMostRestrictions
                false, // hasNominals
                false); // hasDatatypes
    }

    protected Tableau m_tableau;
    protected BlockingStrategy m_blockingStrategy;
    protected ExtensionManager m_extensionManager;

    public BlockingValidatorTest(String name) {
        super(name);
    }
    protected void setUp() {
        DirectBlockingChecker directBlockingChecker=new ValidatedDirectBlockingChecker();
        BlockingSignatureCache blockingSignatureCache=null;
        m_blockingStrategy=new AnywhereValidatedBlocking(directBlockingChecker,blockingSignatureCache,null,null,true,true);
        ExistentialExpansionStrategy ExpansionStrategy=new CreationOrderStrategy(m_blockingStrategy);
        m_tableau=new Tableau(new InterruptFlag(),null,ExpansionStrategy,TEST_DL_ONTOLOGY,new HashMap<String,Object>());
        m_extensionManager=m_tableau.getExtensionManager();
    }
    public void testOneInvalidBlock() {
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
        BlockingValidator validator=new BlockingValidator(m_tableau);
        assertTrue(validator.isBlockValid(a2));
        assertTrue(validator.isBlockValid(a111));
        assertTrue(validator.isBlockValid(b1));
        assertFalse(validator.isBlockValid(b2));
        assertFalse(validator.isBlockValid(b3));
        assertTrue(validator.isBlockValid(a121));
        b2.setBlocked(null, false);
        b3.setBlocked(null, false);
        
        Node b21=m_tableau.createNewTreeNode(emptySet,b2);   // 12
        Node b22=m_tableau.createNewTreeNode(emptySet,b2);   // 13
        Node b31=m_tableau.createNewTreeNode(emptySet,b3);   // 14
        Node b32=m_tableau.createNewTreeNode(emptySet,b3);   // 15
        
        m_extensionManager.addAssertion(R,b21,b2,emptySet,true);
        m_extensionManager.addAssertion(R,b22,b2,emptySet,true);
        m_extensionManager.addAssertion(R,b31,b3,emptySet,true);
        m_extensionManager.addAssertion(R,b32,b3,emptySet,true);
        
        m_extensionManager.addConceptAssertion(B,b21,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2RA,b21,emptySet,false);
        m_extensionManager.addConceptAssertion(B,b22,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2RA,b22,emptySet,false);
        m_extensionManager.addConceptAssertion(B,b31,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2RA,b31,emptySet,false);
        m_extensionManager.addConceptAssertion(B,b32,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2RA,b32,emptySet,false);
        
        m_extensionManager.addAssertion(Equality.INSTANCE,b2,b3,emptySet,false);
        
        m_blockingStrategy.computeBlocking(false);
        // correct blocking because blocking checker does not know whether labels have changed or not  
        b2.setBlocked(null, false);
        b3.setBlocked(null, false);
        b21.setBlocked(a11, true);
        b22.setBlocked(a11, true);
        b31.setBlocked(a11, true);
        b32.setBlocked(a11, true);
        assertFalse(b3.isBlocked());
        assertFalse(a.isBlocked());
        assertFalse(b.isBlocked());
        assertFalse(a1.isBlocked());
        assertFalse(a11.isBlocked());
        assertFalse(a12.isBlocked());
        assertTrue(a111.isDirectlyBlocked() && a111.getBlocker()==a1);
        assertTrue(a2.isDirectlyBlocked() && a2.getBlocker()==a1);
        assertTrue(b1.isDirectlyBlocked() && b1.getBlocker()==a1);
        assertTrue(a121.isDirectlyBlocked() && a121.getBlocker()==a1);
        assertTrue(b21.isDirectlyBlocked() && b21.getBlocker()==a11);
        assertTrue(b22.isDirectlyBlocked() && b22.getBlocker()==a11);
        assertTrue(b31.isDirectlyBlocked() && b31.getBlocker()==a11);
        assertTrue(b32.isDirectlyBlocked() && b32.getBlocker()==a11);
        
        assertTrue(validator.isBlockValid(a111));
        assertTrue(validator.isBlockValid(a2));
        assertTrue(validator.isBlockValid(b1));
        assertTrue(validator.isBlockValid(a121));
        assertTrue(validator.isBlockValid(b21));
        assertTrue(validator.isBlockValid(b22));
        assertTrue(validator.isBlockValid(b31));
        assertTrue(validator.isBlockValid(b32));
    }
    protected void assertLabel(Node node,Concept... expected) {
        assertLabel(m_tableau,node,expected);
    }
}
