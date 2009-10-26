package org.semanticweb.HermiT.tableau;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.blocking.AnywhereValidatedBlocking2;
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
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Variable;

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
        m_blockingStrategy=new AnywhereValidatedBlocking2(directBlockingChecker,blockingSignatureCache,null,null,true,true);
        ExistentialExpansionStrategy ExpansionStrategy=new CreationOrderStrategy(m_blockingStrategy);
        m_tableau=new Tableau(new InterruptFlag(),null,ExpansionStrategy,TEST_DL_ONTOLOGY,new HashMap<String,Object>());
        m_extensionManager=m_tableau.getExtensionManager();
    }
    public void testOneInvalidBlock() {
        DependencySet emptySet=m_tableau.getDependencySetFactory().emptySet();
        Node a=m_tableau.createNewNINode(emptySet);
        Node b=m_tableau.createNewNINode(emptySet);
        Node a1=m_tableau.createNewTreeNode(emptySet,a);
        Node a11=m_tableau.createNewTreeNode(emptySet,a1);
        Node a111=m_tableau.createNewTreeNode(emptySet,a11);
        Node b1=m_tableau.createNewTreeNode(emptySet,b);
        Node b2=m_tableau.createNewTreeNode(emptySet,b);
        Node b3=m_tableau.createNewTreeNode(emptySet,b);
        Node a12=m_tableau.createNewTreeNode(emptySet,a1);
        Node a121=m_tableau.createNewTreeNode(emptySet,a12);
        
        m_extensionManager.addAssertion(R,a,a1,emptySet,true);
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
        
        m_extensionManager.addConceptAssertion(B,a11,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2RA,a11,emptySet,false);
        
        m_extensionManager.addConceptAssertion(A,a111,emptySet,true);
        m_extensionManager.addConceptAssertion(ATLEAST2INVRB,a111,emptySet,false);
        m_extensionManager.addConceptAssertion(D,a111,emptySet,false);
        
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
        
        assertFalse(m_extensionManager.containsClash());
        m_blockingStrategy.computeBlocking(false);
        assertTrue(a111.isDirectlyBlocked() && a111.getBlocker()==a1);
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
        assertTrue(validator.isBlockValid(a111, a111.getBlocker()));
        assertFalse(validator.isBlockValid(b1, b1.getBlocker()));
        assertFalse(validator.isBlockValid(b2, b2.getBlocker()));
        assertFalse(validator.isBlockValid(b3, b3.getBlocker()));
        assertTrue(validator.isBlockValid(a121, a121.getBlocker()));
    }
    protected void assertLabel(Node node,Concept... expected) {
        assertLabel(m_tableau,node,expected);
    }
}
