package org.semanticweb.HermiT.blocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.blocking.ValidatedDirectBlockingChecker.ValidatedBlockingObject;
import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.model.DLClause.ClauseType;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.HermiT.tableau.ExtensionTable.Retrieval;

/**
 * Checks whether the rules from some set are applicable given the current state of the extensions.
 */
public class BlockingValidator {
    protected final Tableau m_tableau;
    protected final ExtensionManager m_extensionManager;
    protected final ExtensionTable.Retrieval m_binaryRetrieval1Bound;
    protected final ExtensionTable.Retrieval m_ternaryRetrieval01Bound;
    protected final ExtensionTable.Retrieval m_ternaryRetrieval02Bound;
    protected final List<DLClauseInfo> m_dlClauseInfos;

    public BlockingValidator(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=tableau.getExtensionManager();
        m_binaryRetrieval1Bound=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false, true }, ExtensionTable.View.TOTAL);
        m_ternaryRetrieval01Bound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false }, ExtensionTable.View.TOTAL);
        m_ternaryRetrieval02Bound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true }, ExtensionTable.View.TOTAL);
        m_dlClauseInfos=new ArrayList<DLClauseInfo>();
        for (DLClause dlClause : tableau.getDLOntology().getDLClauses()) {
            if (dlClause.m_clauseType==ClauseType.CONCEPT_INCLUSION)
                m_dlClauseInfos.add(new DLClauseInfo(dlClause,m_extensionManager));
//            if (!dlClause.isConceptInclusion() && !dlClause.isRoleInclusion() && !dlClause.isRoleInverseInclusion())
//                m_dlClauseInfos.add(new DLClauseInfo(dlClause,m_extensionManager));
        }
    }
    public boolean isBlockValid(Node blocked) {
        Node blockedParent=blocked.getParent();
        if (!((ValidatedBlockingObject)blockedParent.getBlockingObject()).m_hasAlreadyBeenChecked) {
            // if the parent has not been checked yet, check the parent's constraints and mark all its 
            // blocked successors that would invalidate the parent constraints in the model construction   
            checkConstraintsForNonblockedX(blockedParent);
            ((ValidatedBlockingObject)blockedParent.getBlockingObject()).m_hasAlreadyBeenChecked=true;
        } 
        // from previous check on the parent we know whether the block is invalid
        if (((ValidatedBlockingObject)blocked.getBlockingObject()).m_blockViolatesParentConstraints)
            return false;
        if (!satisfiesConstraintsForBlockedX(blocked))
            return false;
        return true;
    }
    // These methods check the constraint satisfaction for the case when X is matched to a blocked node 
    protected boolean satisfiesConstraintsForBlockedX(Node blockedX) {
        Node blocker=blockedX.getBlocker();
        Node blockerParent=blocker.getParent();
        m_binaryRetrieval1Bound.getBindingsBuffer()[1]=blocker;
        m_binaryRetrieval1Bound.open();
        Object[] tupleBuffer=m_binaryRetrieval1Bound.getTupleBuffer();
        while (!m_binaryRetrieval1Bound.afterLast()) {
            if (tupleBuffer[0] instanceof AtLeastConcept) {
                AtLeastConcept atleast=(AtLeastConcept)tupleBuffer[0];
                if (m_extensionManager.containsRoleAssertion(atleast.getOnRole(),blocker,blockerParent)
                        && m_extensionManager.containsConceptAssertion(atleast.getToConcept(), blockerParent)) {
                    // blocker possibly uses its parent to satisfy the existential, so check if it is satisfied after copying the labels 
                    if (!isSatisfiedAtLeastForBlocked(atleast,blockedX,blocker,blockerParent)) {
                        return false;
                    }
                }
            }
            m_binaryRetrieval1Bound.next();
        }
        for (DLClauseInfo dlClauseInfo : m_dlClauseInfos)
            if (!satisfiesDLClauseForBlockedX(dlClauseInfo,blockedX))
                return false;
        return true;
    }
    protected boolean isSatisfiedAtLeastForBlocked(AtLeastConcept atleast,Node blockedX, Node blocker,Node blockerParent) {
        Role r=atleast.getOnRole();
        LiteralConcept c=atleast.getToConcept();
        Node blockedXParent=blockedX.getParent();
        if (m_extensionManager.containsRoleAssertion(r,blockedX,blockedXParent) 
                && m_extensionManager.containsConceptAssertion(c,blockedXParent))
            return true;
        // blockerParent cannot be used to satisfy the existential, so check whether the blocker has enough suitable children
        Retrieval retrieval;
        int position;
        if (r instanceof AtomicRole) {
            retrieval=m_ternaryRetrieval01Bound;
            retrieval.getBindingsBuffer()[0]=(AtomicRole)r;
            retrieval.getBindingsBuffer()[1]=blocker;
            position=2;
        } else {
            retrieval=m_ternaryRetrieval02Bound;
            retrieval.getBindingsBuffer()[0]=((InverseRole)r).getInverseOf();
            position=1;
            retrieval.getBindingsBuffer()[2]=blocker;
        }
        retrieval.open();
        Object[] tupleBuffer=retrieval.getTupleBuffer();
        int suitableSuccessors=0;
        int requiredSuccessors=atleast.getNumber();
        while (!retrieval.afterLast()&&suitableSuccessors<requiredSuccessors) {
            Node rSuccessor=(Node)tupleBuffer[position];
            if (rSuccessor!=blockerParent && m_extensionManager.containsConceptAssertion(c,rSuccessor))
               suitableSuccessors++;
            retrieval.next();
        }
        return requiredSuccessors<=suitableSuccessors;
    }
    protected boolean satisfiesDLClauseForBlockedX(DLClauseInfo dlClauseInfo,Node blockedX) {
        assert blockedX.isDirectlyBlocked();
        Node blockedXParent=blockedX.getParent();
        Node blocker=blockedX.getBlocker();
        // Check whether some of the X concepts can be matched to the blocker
        for (AtomicConcept atomicConcept : dlClauseInfo.m_xConcepts)
            if (!m_extensionManager.containsAssertion(atomicConcept,blocker))
                return true; // clause not applicable (trivially satisfied)
        for (AtomicRole atomicRole : dlClauseInfo.m_x2xRoles)
            if (!m_extensionManager.containsAssertion(atomicRole,blocker,blocker))
                return true; // clause not applicable (trivially satisfied)
        // Find one yConstraint that involves a parent of blockedX, otherwise the clause is not relevant
        int matchingYConstraintIndex=-1;
        for (int yIndex=0;matchingYConstraintIndex==-1 && yIndex<dlClauseInfo.m_yConstraints.length;yIndex++) {
            if (dlClauseInfo.m_yConstraints[yIndex].isSatisfiedExplicitly(m_extensionManager,blockedX,blockedXParent))
                matchingYConstraintIndex=yIndex;
        }
        if (matchingYConstraintIndex==-1)
            return true;  // clause not relevant
        dlClauseInfo.m_xNode=blocker;
        dlClauseInfo.m_yNodes[matchingYConstraintIndex]=blockedXParent;
        // Examine all possible matches for the Zs (and recursively for Ys then as well)
        boolean result=satisfiesDLClauseForBlockedXAndAnyZ(dlClauseInfo,blockedX,matchingYConstraintIndex,0);
        dlClauseInfo.m_xNode=null;
        dlClauseInfo.m_yNodes[matchingYConstraintIndex]=null;
        return result;
    }
    protected boolean satisfiesDLClauseForBlockedXAndAnyZ(DLClauseInfo dlClauseInfo,Node blockedX,int parentOfBlockedXIndex,int toMatchIndex) {
        if (toMatchIndex==dlClauseInfo.m_zNodes.length)
            return satisfiesDLClauseForBlockedXAnyZAndAnyY(dlClauseInfo,blockedX,parentOfBlockedXIndex,0);
        else {
            AtomicConcept[] zConcepts=dlClauseInfo.m_zConcepts[toMatchIndex];
            ExtensionTable.Retrieval retrieval=dlClauseInfo.m_zRetrievals[toMatchIndex];
            retrieval.getBindingsBuffer()[0]=zConcepts[0];
            retrieval.open();
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                Node nodeZ=(Node)tupleBuffer[1];
                boolean allMatched=true;
                for (int index=1;index<zConcepts.length;index++)
                    if (!m_extensionManager.containsAssertion(zConcepts[index],nodeZ)) {
                        allMatched=false;
                        break;
                    }
                if (allMatched) {
                    dlClauseInfo.m_zNodes[toMatchIndex]=nodeZ;
                    boolean result=satisfiesDLClauseForBlockedXAndAnyZ(dlClauseInfo,blockedX,parentOfBlockedXIndex,toMatchIndex+1);
                    dlClauseInfo.m_zNodes[toMatchIndex]=null;
                    if (!result)
                        return false;
                }
                retrieval.next();
            }
            return true;
        }
    }
    protected boolean satisfiesDLClauseForBlockedXAnyZAndAnyY(DLClauseInfo dlClauseInfo,Node blockedX,int parentOfBlockedXIndex,int toMatchIndex) {
        if (toMatchIndex==parentOfBlockedXIndex)
            return satisfiesDLClauseForBlockedXAnyZAndAnyY(dlClauseInfo,blockedX,parentOfBlockedXIndex,toMatchIndex+1); // assignment already fixed, skip
        else if (toMatchIndex==dlClauseInfo.m_yConstraints.length)
            return satisfiesDLClauseForBlockedXAndMatchedNodes(dlClauseInfo,blockedX,parentOfBlockedXIndex);
        else {
            Node blocker=blockedX.getBlocker();
            Node blockerParent=blocker.getParent();
            YConstraint yConstraint=dlClauseInfo.m_yConstraints[toMatchIndex];
            assert yConstraint.m_x2yRoles.length!=0 || yConstraint.m_y2xRoles.length!=0;
            int yNodeIndex;
            ExtensionTable.Retrieval retrieval;
            if (yConstraint.m_x2yRoles.length!=0) {
                retrieval=dlClauseInfo.m_x2yRetrievals[toMatchIndex];
                retrieval.getBindingsBuffer()[1]=blocker;
                yNodeIndex=2;
            }
            else {
                retrieval=dlClauseInfo.m_y2xRetrievals[toMatchIndex];
                yNodeIndex=1;
                retrieval.getBindingsBuffer()[2]=blocker;
            }
            retrieval.open();
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                Node nodeY=(Node)tupleBuffer[yNodeIndex];
                if (nodeY!=blockerParent && yConstraint.isSatisfiedExplicitly(m_extensionManager,blocker,nodeY)) {
                    dlClauseInfo.m_yNodes[toMatchIndex]=nodeY;
                    boolean result=satisfiesDLClauseForBlockedXAnyZAndAnyY(dlClauseInfo,blockedX,parentOfBlockedXIndex,toMatchIndex+1);
                    dlClauseInfo.m_yNodes[toMatchIndex]=null; // checking done, reset assignment
                    if (!result)
                        return false;
                }
                retrieval.next();
            }
            return true;
        }
    }
    protected boolean satisfiesDLClauseForBlockedXAndMatchedNodes(DLClauseInfo dlClauseInfo,Node blockedX,int parentOfBlockedXIndex) {
        for (ConsequenceAtom consequenceAtom : dlClauseInfo.m_consequencesForBlockedX) {
            if (consequenceAtom.isSatisfied(m_extensionManager,dlClauseInfo,blockedX))
                return true;
        }
        return false;
    }

    // These methods check the constraint satisfaction for the case when X is matched to a parent of a blocked node 

    protected void checkConstraintsForNonblockedX(Node nonblockedX) {
        // check atleasts
        m_binaryRetrieval1Bound.getBindingsBuffer()[1]=nonblockedX;
        m_binaryRetrieval1Bound.open();
        Object[] tupleBuffer=m_binaryRetrieval1Bound.getTupleBuffer();
        while (!m_binaryRetrieval1Bound.afterLast()) {
            if (tupleBuffer[0] instanceof AtLeastConcept) {
                AtLeastConcept atleast=(AtLeastConcept)tupleBuffer[0];
                checkAtLeastForNonblocked(atleast,nonblockedX);
            }
            m_binaryRetrieval1Bound.next();
        }
        for (DLClauseInfo dlClauseInfo : m_dlClauseInfos)
            checkDLClauseForNonblockedX(dlClauseInfo,nonblockedX);
    }
    protected void checkAtLeastForNonblocked(AtLeastConcept atleast,Node nonblocked) {
        int suitableSuccessors=0;
        int requiredSuccessors=atleast.getNumber();
        Role r=atleast.getOnRole();
        LiteralConcept c=atleast.getToConcept();
        Retrieval retrieval;
        int position;
        if (r instanceof AtomicRole) {
            retrieval=m_ternaryRetrieval01Bound;
            retrieval.getBindingsBuffer()[0]=(AtomicRole)r;
            retrieval.getBindingsBuffer()[1]=nonblocked;
            position=2;
        } else {
            retrieval=m_ternaryRetrieval02Bound;
            retrieval.getBindingsBuffer()[0]=((InverseRole)r).getInverseOf();
            position=1;
            retrieval.getBindingsBuffer()[2]=nonblocked;
        }
        retrieval.open();
        Object[] tupleBuffer=retrieval.getTupleBuffer();
        Set<Node> possiblyInvalidlyBlocked=new HashSet<Node>();
        while (!retrieval.afterLast()&&suitableSuccessors<requiredSuccessors) {
            Node rSuccessor=(Node)tupleBuffer[position];
            if (rSuccessor.isBlocked()) {
                if (m_extensionManager.containsConceptAssertion(c,rSuccessor.getBlocker()))
                    suitableSuccessors++;
                else 
                    possiblyInvalidlyBlocked.add(rSuccessor);
            } else if (m_extensionManager.containsConceptAssertion(c,rSuccessor))
                suitableSuccessors++;
            retrieval.next();
        }
        if (suitableSuccessors<requiredSuccessors) {
            for (Node blocked : possiblyInvalidlyBlocked) {
                if (m_extensionManager.containsConceptAssertion(c,blocked))
                    ((ValidatedBlockingObject)blocked.getBlockingObject()).m_blockViolatesParentConstraints=true;
            }
        }
    }
    protected void checkDLClauseForNonblockedX(DLClauseInfo dlClauseInfo,Node nonblockedX) {
        // Check whether some of the X concepts can be matched to the node
        for (AtomicConcept atomicConcept : dlClauseInfo.m_xConcepts)
            if (!m_extensionManager.containsAssertion(atomicConcept,nonblockedX))
                return; // trivially satisfied (premise is false)
        for (AtomicRole atomicRole : dlClauseInfo.m_x2xRoles)
            if (!m_extensionManager.containsAssertion(atomicRole,nonblockedX,nonblockedX))
                return; // clause not applicable (trivially satisfied)
        dlClauseInfo.m_xNode=nonblockedX;
        // Examine all possible matches for the Zs (and recursively for Ys then as well)
        checkDLClauseForNonblockedXAndAnyZ(dlClauseInfo,nonblockedX,0);
        dlClauseInfo.m_xNode=null; // checking done, reset assignment 
    }
    protected void checkDLClauseForNonblockedXAndAnyZ(DLClauseInfo dlClauseInfo,Node nonblockedX,int toMatchIndex) {
        if (toMatchIndex==dlClauseInfo.m_zNodes.length)
            checkDLClauseForNonblockedXAnyZAndAnyY(dlClauseInfo,nonblockedX,0);
        else {
            AtomicConcept[] zConcepts=dlClauseInfo.m_zConcepts[toMatchIndex];
            ExtensionTable.Retrieval retrieval=dlClauseInfo.m_zRetrievals[toMatchIndex];
            retrieval.getBindingsBuffer()[0]=zConcepts[0];
            retrieval.open();
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                Node nodeZ=(Node)tupleBuffer[1];
                boolean allMatched=true;
                for (int index=1;index<zConcepts.length&&zConcepts[index]!=null;index++)
                    if (!m_extensionManager.containsAssertion(zConcepts[index],nodeZ)) {
                        allMatched=false;
                        break;
                    }
                if (allMatched) {
                    dlClauseInfo.m_zNodes[toMatchIndex]=nodeZ;
                    checkDLClauseForNonblockedXAndAnyZ(dlClauseInfo,nonblockedX,toMatchIndex+1);
                    dlClauseInfo.m_zNodes[toMatchIndex]=null; // checking done, reset assignments
                    return; // z nodes do not change in the model construction, so any assignment works
                }
                retrieval.next();
            }
            return;  // no z could be found that satisfies the constrains (clause is trivially satisfied) 
        }
    }
    protected void checkDLClauseForNonblockedXAnyZAndAnyY(DLClauseInfo dlClauseInfo,Node nonblockedX,int toMatchIndex) {
        if (toMatchIndex==dlClauseInfo.m_yConstraints.length)
            checkDLClauseForNonblockedXAndMatchedNodes(dlClauseInfo,nonblockedX);
        else {
            YConstraint yConstraint=dlClauseInfo.m_yConstraints[toMatchIndex];
            assert yConstraint.m_x2yRoles.length!=0 || yConstraint.m_y2xRoles.length!=0;
            int yNodeIndex;
            ExtensionTable.Retrieval retrieval;
            if (yConstraint.m_x2yRoles.length!=0) {
                retrieval=dlClauseInfo.m_x2yRetrievals[toMatchIndex];
                retrieval.getBindingsBuffer()[1]=nonblockedX;
                yNodeIndex=2;
            }
            else {
                retrieval=dlClauseInfo.m_y2xRetrievals[toMatchIndex];
                yNodeIndex=1;
                retrieval.getBindingsBuffer()[2]=nonblockedX;
            }
            retrieval.open();
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                Node nodeY=(Node)tupleBuffer[yNodeIndex];
                if (yConstraint.isSatisfiedViaMirroringY(m_extensionManager,nonblockedX,nodeY)) {
                    dlClauseInfo.m_yNodes[toMatchIndex]=nodeY;
                    checkDLClauseForNonblockedXAnyZAndAnyY(dlClauseInfo,nonblockedX,toMatchIndex+1);
                    dlClauseInfo.m_yNodes[toMatchIndex]=null; // checking done, reset assignments
                }
                retrieval.next();
            }
        }
    }
    protected void checkDLClauseForNonblockedXAndMatchedNodes(DLClauseInfo dlClauseInfo,Node nonblockedX) {
        for (ConsequenceAtom consequenceAtom : dlClauseInfo.m_consequencesForNonblockedX) {
            if (consequenceAtom.isSatisfied(m_extensionManager,dlClauseInfo,nonblockedX))
                return;
        }
        // go through the y's and if y is bound to a blocked node, set a flag that the block is invalid
        for (Node possiblyInvalidlyBlocked : dlClauseInfo.m_yNodes) {
            if (possiblyInvalidlyBlocked.isDirectlyBlocked()) {
                ((ValidatedBlockingObject)possiblyInvalidlyBlocked.getBlockingObject()).m_blockViolatesParentConstraints=true;
            }
        }
    }
    
    protected static class DLClauseInfo {
        protected final AtomicConcept[] m_xConcepts;
        protected final AtomicRole[] m_x2xRoles;
        protected final YConstraint[] m_yConstraints;
        protected final AtomicConcept[][] m_zConcepts;
        protected final ExtensionTable.Retrieval[] m_x2yRetrievals;
        protected final ExtensionTable.Retrieval[] m_y2xRetrievals;
        protected final ExtensionTable.Retrieval[] m_zRetrievals;
        protected final ConsequenceAtom[] m_consequencesForBlockedX;
        protected final ConsequenceAtom[] m_consequencesForNonblockedX;
        protected final DLClause m_dlClause; // for debugging
        protected Node m_xNode;
        protected Node[] m_yNodes;
        protected Variable[] m_yVariables;
        protected Node[] m_zNodes;
        protected Variable[] m_zVariables;
        
        
        public DLClauseInfo(DLClause dlClause,ExtensionManager extensionManager) {
            m_dlClause=dlClause;
            // TODO: We'll sort our the variables by names. This introduces a dependency
            // to clausification. That's ugly and should be fixed later.
            Variable X=Variable.create("X");
            Set<AtomicConcept> xConcepts=new HashSet<AtomicConcept>();
            Set<AtomicRole> x2xRoles=new HashSet<AtomicRole>();
            Set<Variable> ys=new HashSet<Variable>();
            Map<Variable,Set<AtomicConcept>> y2concepts=new HashMap<Variable, Set<AtomicConcept>>();
            Map<Variable,Set<AtomicConcept>> z2concepts=new HashMap<Variable, Set<AtomicConcept>>();
            int maxZConcepts=0;
            Map<Variable,Set<AtomicRole>> x2yRoles=new HashMap<Variable, Set<AtomicRole>>();
            Map<Variable,Set<AtomicRole>> y2xRoles=new HashMap<Variable, Set<AtomicRole>>();
            // Each atom in the antecedent is of the form A(x), R(x,x), R(x,yi), R(yi,x), A(yi), or A(zj).
            for (int i=0;i<dlClause.getBodyLength();i++) {
                Atom atom=dlClause.getBodyAtom(i);
                DLPredicate predicate=atom.getDLPredicate();
                Variable var1=atom.getArgumentVariable(0);
                if (predicate instanceof AtomicConcept) {
                    if (var1==X) {
                        xConcepts.add((AtomicConcept)predicate);
                    } else if (var1.getName().startsWith("Y")) {
                        ys.add(var1);
                        if (y2concepts.containsKey(var1)) {
                            y2concepts.get(var1).add((AtomicConcept)predicate);
                        } else {
                            Set<AtomicConcept> concepts=new HashSet<AtomicConcept>();
                            concepts.add((AtomicConcept)predicate);
                            y2concepts.put(var1, concepts);
                        }
                    } else if (var1.getName().startsWith("Z")) {
                        if (z2concepts.containsKey(var1)) {
                            Set<AtomicConcept> concepts=z2concepts.get(var1);
                            concepts.add((AtomicConcept)predicate);
                            if (maxZConcepts<concepts.size()) maxZConcepts++;
                        } else {
                            Set<AtomicConcept> concepts=new HashSet<AtomicConcept>();
                            concepts.add((AtomicConcept)predicate);
                            z2concepts.put(var1, concepts);
                            if (maxZConcepts==0) maxZConcepts=1;
                        }
                    } else {
                        throw new IllegalStateException("Internal error: Clause premise contained variables other than X, Yi, and Zi in a concept atom. ");
                    }
                } else if (predicate instanceof AtomicRole) {
                    Variable var2=atom.getArgumentVariable(1);
                    if (var1==X) {
                        if (var2==X) {
                            x2xRoles.add((AtomicRole)atom.getDLPredicate());
                        } else if (var2.getName().startsWith("Y")) {
                            ys.add(var2);
                            if (x2yRoles.containsKey(var2)) {
                                x2yRoles.get(var2).add((AtomicRole)predicate);
                            } else {
                                Set<AtomicRole> roles=new HashSet<AtomicRole>();
                                roles.add((AtomicRole)predicate);
                                x2yRoles.put(var2,roles);
                            }
                        } else {
                            throw new IllegalStateException("Internal error: Clause premise contains a role atom with virales other than X and Yi. ");
                        }
                    } else if (var2==X) {
                        if (var1.getName().startsWith("Y")) {
                            ys.add(var1);
                            if (y2xRoles.containsKey(var1)) {
                                y2xRoles.get(var1).add((AtomicRole)predicate);
                            } else {
                                Set<AtomicRole> roles=new HashSet<AtomicRole>();
                                roles.add((AtomicRole)predicate);
                                y2xRoles.put(var1,roles);
                            }
                        } else {
                            throw new IllegalStateException("Internal error: Clause premise contains a role atom with virales other than X and Yi. ");
                        }
                    } else {
                        throw new IllegalStateException("Internal error: Clause premise contained variables other than X and Yi in a role atom. ");
                    }
                }
            }
            AtomicConcept[] noConcepts=new AtomicConcept[0];
            AtomicRole[] noRoles=new AtomicRole[0];
            Variable[] noVariables=new Variable[0];
            
            // Variable X
            m_xNode=null;
            m_xConcepts=xConcepts.toArray(noConcepts);
            m_x2xRoles=x2xRoles.toArray(noRoles);
            
            // Variable Y
            m_yVariables=ys.toArray(noVariables);
            m_yNodes=new Node[m_yVariables.length];
            m_yConstraints=new YConstraint[ys.size()];
            m_x2yRetrievals=new Retrieval[x2yRoles.size()];
            m_y2xRetrievals=new Retrieval[y2xRoles.size()];
            int i=0;
            for (i=0;i<m_yVariables.length;i++) {
                Variable y=m_yVariables[i];
                Set<AtomicConcept> yConcepts=y2concepts.get(y);
                Set<AtomicRole> xyRoles=x2yRoles.get(y);
                if (xyRoles!=null) {
                    assert xyRoles.size()==1;
                    for (AtomicRole r : xyRoles) {
                        m_x2yRetrievals[i]=extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
                        m_x2yRetrievals[i].getBindingsBuffer()[0]=r;
                    }
                }
                Set<AtomicRole> yxRoles=y2xRoles.get(y);
                if (yxRoles!=null) {
                    assert yxRoles.size()==1;
                    for (AtomicRole r : yxRoles) {
                        m_y2xRetrievals[i]=extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.TOTAL);
                        m_y2xRetrievals[i].getBindingsBuffer()[0]=r;
                    }
                }
                m_yConstraints[i]=new YConstraint(yConcepts!=null?yConcepts.toArray(noConcepts):noConcepts, xyRoles!=null?xyRoles.toArray(noRoles):noRoles, yxRoles!=null?yxRoles.toArray(noRoles):noRoles);
            }
            
            // Variable Z
            m_zVariables=z2concepts.keySet().toArray(noVariables);
            m_zNodes=new Node[m_zVariables.length];
            m_zConcepts=new AtomicConcept[m_zNodes.length][maxZConcepts];
            for (int varIndex=0;varIndex<m_zVariables.length;varIndex++) {
                int conceptIndex=0;
                for (AtomicConcept c : z2concepts.get(m_zVariables[varIndex])) {
                    m_zConcepts[varIndex][conceptIndex]=c;
                    conceptIndex++;
                }
            }
            m_zRetrievals=new Retrieval[m_zNodes.length];
            for (i=0;i<m_zRetrievals.length;i++) {
                m_zRetrievals[i]=extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { true,false },ExtensionTable.View.TOTAL);
            }
            
            // Consequences
            m_consequencesForBlockedX=new ConsequenceAtom[dlClause.getHeadLength()];
            m_consequencesForNonblockedX=new ConsequenceAtom[dlClause.getHeadLength()];
            //Each atom in the consequent is of the form B(x), >= h S.B(x), B(yi), R(x, x), 
            // R(x,yi), R(yi,x), R(x,zj), R(zj,x), x==zj, or yi==yj @^x_{<=h S.B}.
            for (i=0;i<dlClause.getHeadLength();i++) {
                Atom atom=dlClause.getHeadAtom(i);
                DLPredicate predicate=atom.getDLPredicate();
                Variable var1=atom.getArgumentVariable(0); 
                Variable var2=null;
                if (predicate.getArity()==2) var2=atom.getArgumentVariable(1);
                
                if (predicate instanceof AtomicConcept) {
                    // B(x), B(yi)
                    ArgumentType argType=ArgumentType.YVAR;
                    int argIndex=getIndexFor(m_yVariables, var1);
                    if (argIndex==-1) {
                        assert var1==X;
                        argIndex=0;
                        argType=ArgumentType.XVAR;
                    }
                    m_consequencesForBlockedX[i]=new SimpleConsequenceAtom(predicate,new ArgumentType[] { argType },new int[] { argIndex });
                    if (argType==ArgumentType.XVAR) {
                        m_consequencesForNonblockedX[i]=m_consequencesForBlockedX[i];
                    } else {
                        m_consequencesForNonblockedX[i]=new MirroredYConsequenceAtom((AtomicConcept)predicate,argIndex);
                    }
                } else if (predicate instanceof AtLeastConcept) {
                    // >= h S.B(x)
                    assert var1==X;
                    m_consequencesForBlockedX[i]=new SimpleConsequenceAtom(predicate,new ArgumentType[] { ArgumentType.XVAR },new int[] { 0 });
                    m_consequencesForNonblockedX[i]=m_consequencesForBlockedX[i];
                } else if (predicate==Equality.INSTANCE) {
                    // x==zi or yi===zi
                    if (var1==X || var2==X) {
                        // x==zi
                        if (var2==X) {
                            Variable tmp=var1;
                            var1=var2;
                            var2=tmp;
                        }
                        assert var2.getName().startsWith("Z");
                        int var2Index=getIndexFor(m_zVariables, var2);
                        assert var1==X && var2Index!=-1;
                        m_consequencesForBlockedX[i]=new SimpleConsequenceAtom(predicate,new ArgumentType[] { ArgumentType.XVAR,ArgumentType.ZVAR },new int[] { 0,getIndexFor(m_zVariables,var2) });
                        m_consequencesForNonblockedX[i]=m_consequencesForBlockedX[i];
                    } else if (var1.getName().startsWith("Y") || var2.getName().startsWith("Y")) {
                        // yi==zi
                        if (var2.getName().startsWith("Y")) {
                            Variable tmp=var1;
                            var1=var2;
                            var2=tmp;
                        }
                        assert var2.getName().startsWith("Z");
                        int var1Index=getIndexFor(m_yVariables, var1);
                        int var2Index=getIndexFor(m_zVariables, var2);
                        assert var1Index>-1 && var2Index>-1;
                        m_consequencesForBlockedX[i]=new SimpleConsequenceAtom(predicate,new ArgumentType[] { ArgumentType.YVAR,ArgumentType.ZVAR },new int[] { getIndexFor(m_yVariables,var1),getIndexFor(m_zVariables,var2) });
                        m_consequencesForNonblockedX[i]=m_consequencesForBlockedX[i];
                    } else {
                        throw new IllegalArgumentException("Internal error: The clause "+dlClause+" is not an HT clause. ");
                    }
                } else if (predicate instanceof AnnotatedEquality) {
                    // (yi==yj @^x_{<=h S.B})(X)
                    // arity 3
                    var1=atom.getArgumentVariable(0);
                    var2=atom.getArgumentVariable(1);
                    int var1Index=getIndexFor(m_yVariables, var1);
                    int var2Index=getIndexFor(m_yVariables, var2);
                    assert var1Index!=-1 && var2Index!=-1;
                    m_consequencesForBlockedX[i]=new SimpleConsequenceAtom(predicate,new ArgumentType[] { ArgumentType.YVAR,ArgumentType.YVAR,ArgumentType.XVAR },new int[] { var1Index,var2Index,0 });
                    m_consequencesForNonblockedX[i]=m_consequencesForBlockedX[i];
                } else if (predicate instanceof AtomicRole) {
                    // R(x, x), R(x,yi), R(yi,x), R(x,zj), R(zj,x)
                    assert predicate instanceof AtomicRole;
                    AtomicRole role=(AtomicRole)predicate;
                    if (X==var1 && X==var2) {
                        m_consequencesForBlockedX[i]=new SimpleConsequenceAtom(predicate,new ArgumentType[] { ArgumentType.XVAR,ArgumentType.XVAR },new int[] { 0,0 });
                        m_consequencesForNonblockedX[i]=m_consequencesForBlockedX[i];
                    } else {
                        assert var1==X || var2==X;
                        int argIndex=-1;
                        if (var1==X) {
                            argIndex=getIndexFor(m_yVariables, var2);
                            if (argIndex==-1) {
                                argIndex=getIndexFor(m_zVariables, var2);
                                assert argIndex>-1;
                                m_consequencesForBlockedX[i]=new SimpleConsequenceAtom(predicate,new ArgumentType[] { ArgumentType.XVAR,ArgumentType.ZVAR },new int[] { 0,argIndex });
                                m_consequencesForNonblockedX[i]=m_consequencesForBlockedX[i];
                            } else {
                                m_consequencesForBlockedX[i]=new X2YOrY2XConsequenceAtom(role,argIndex,true);
                                m_consequencesForNonblockedX[i]=new SimpleConsequenceAtom(predicate,new ArgumentType[] { ArgumentType.XVAR,ArgumentType.YVAR },new int[] { 0,argIndex });
                            }
                        } else {
                            argIndex=getIndexFor(m_yVariables, var1);
                            if (argIndex==-1) {
                                argIndex=getIndexFor(m_zVariables, var1);
                                assert argIndex>-1;
                                m_consequencesForBlockedX[i]=new SimpleConsequenceAtom(predicate,new ArgumentType[] { ArgumentType.ZVAR,ArgumentType.XVAR },new int[] { argIndex,0 });
                                m_consequencesForNonblockedX[i]=m_consequencesForBlockedX[i];
                            } else {
                                m_consequencesForBlockedX[i]=new X2YOrY2XConsequenceAtom(role,argIndex,false);
                                m_consequencesForNonblockedX[i]=new SimpleConsequenceAtom(predicate,new ArgumentType[] { ArgumentType.YVAR,ArgumentType.XVAR },new int[] { argIndex,0 });
                            }
                        }
                    }
                }
            }
        }
        protected int getIndexFor(Variable[] variables, Variable variable) {
            for (int index=0;index<variables.length;index++) {
                if (variables[index]==variable) return index; 
            }
            return -1;
        }
        public String toString() {
            return m_dlClause.toString();
        }
    }
    
    protected static class YConstraint {
        protected final AtomicConcept[] m_yConcepts;
        protected final AtomicRole[] m_x2yRoles;
        protected final AtomicRole[] m_y2xRoles;
        
        public YConstraint(AtomicConcept[] yConcepts,AtomicRole[] x2yRoles,AtomicRole[] y2xRoles) {
            m_yConcepts=yConcepts;
            m_x2yRoles=x2yRoles;
            m_y2xRoles=y2xRoles;
        }
        public boolean isSatisfiedExplicitly(ExtensionManager extensionManager,Node nodeX,Node nodeY) {
            for (AtomicRole x2yRole : m_x2yRoles)
                if (!extensionManager.containsAssertion(x2yRole,nodeX,nodeY))
                    return false;
            for (AtomicRole y2xRole : m_y2xRoles)
                if (!extensionManager.containsAssertion(y2xRole,nodeY,nodeX))
                    return false;
            for (AtomicConcept yConcept : m_yConcepts)
                if (!extensionManager.containsAssertion(yConcept,nodeY))
                    return false;
            return true;
        }
        public boolean isSatisfiedViaMirroringY(ExtensionManager extensionManager,Node nodeX,Node nodeY) {
            for (AtomicRole x2yRole : m_x2yRoles)
                if (!extensionManager.containsAssertion(x2yRole,nodeX,nodeY))
                    return false;
            for (AtomicRole y2xRole : m_y2xRoles)
                if (!extensionManager.containsAssertion(y2xRole,nodeY,nodeX))
                    return false;
            Node nodeYMirror;
            if (nodeY.isBlocked())
                nodeYMirror=nodeY.getBlocker();
            else
                nodeYMirror=nodeY;
            for (AtomicConcept yConcept : m_yConcepts)
                if (!extensionManager.containsAssertion(yConcept,nodeYMirror))
                    return false;
            return true;
        }
    }
    
    protected static enum ArgumentType { XVAR,YVAR,ZVAR }

    protected static interface ConsequenceAtom {
        boolean isSatisfied(ExtensionManager extensionManager,DLClauseInfo dlClauseInfo,Node blockedX);
    }

    protected static class SimpleConsequenceAtom implements ConsequenceAtom {
        protected final Object[] m_assertionBuffer;
        protected final ArgumentType[] m_argumentTypes;
        protected final int[] m_argumentIndexes;
        
        public SimpleConsequenceAtom(DLPredicate dlPredicate,ArgumentType[] argumentTypes,int[] argumentIndexes) {
            // r(x,y) would have argumentTypes { XVAR, YVAR } and dlPredicate r
            // y1==y2 would have argumentTypes { YVAR, YVAR } and dlPredicate ==
            // argumentIndex indicates at which position in the DLClauseInfo the variable is
            // for binary atoms argumentIndex has length 2, for unary length 1 
            m_assertionBuffer=new Object[argumentIndexes.length+1]; 
            m_assertionBuffer[0]=dlPredicate;
            m_argumentTypes=argumentTypes;
            m_argumentIndexes=argumentIndexes;
        }
        public boolean isSatisfied(ExtensionManager extensionManager,DLClauseInfo dlClauseInfo,Node nodeX) {
            for (int argumentIndex=m_argumentIndexes.length-1;argumentIndex>=0;--argumentIndex) {
                switch (m_argumentTypes[argumentIndex]) {
                case XVAR:
                    m_assertionBuffer[argumentIndex+1]=dlClauseInfo.m_xNode;
                    break;
                case YVAR:
                    m_assertionBuffer[argumentIndex+1]=dlClauseInfo.m_yNodes[m_argumentIndexes[argumentIndex]];
                    break;
                case ZVAR:
                    m_assertionBuffer[argumentIndex+1]=dlClauseInfo.m_zNodes[m_argumentIndexes[argumentIndex]];
                    break;
                }
            }
            return extensionManager.containsTuple(m_assertionBuffer);
        }
    }
    
    protected static class X2YOrY2XConsequenceAtom implements ConsequenceAtom {
        protected final AtomicRole m_atomicRole;
        protected final int m_yArgumentIndex;
        protected final boolean m_isX2Y;
        
        public X2YOrY2XConsequenceAtom(AtomicRole atomicRole,int yArgumentIndex,boolean isX2Y) {
            m_atomicRole=atomicRole;
            m_yArgumentIndex=yArgumentIndex;
            m_isX2Y=isX2Y;
        }
        public boolean isSatisfied(ExtensionManager extensionManager,DLClauseInfo dlClauseInfo,Node nodeX) {
            Node nodeY=dlClauseInfo.m_yNodes[m_yArgumentIndex];
            Node nodeXReal;
            if (nodeY==nodeX.getParent())
                nodeXReal=nodeX;
            else
                nodeXReal=dlClauseInfo.m_xNode;
            if (m_isX2Y)
                return extensionManager.containsAssertion(m_atomicRole,nodeXReal,nodeY);
            else
                return extensionManager.containsAssertion(m_atomicRole,nodeY,nodeXReal);
        }
    }

    protected static class MirroredYConsequenceAtom implements ConsequenceAtom {
        protected final AtomicConcept m_atomicConcept;
        protected final int m_yArgumentIndex;
        
        public MirroredYConsequenceAtom(AtomicConcept atomicConcept,int yArgumentIndex) {
            m_atomicConcept=atomicConcept;
            m_yArgumentIndex=yArgumentIndex;
        }
        public boolean isSatisfied(ExtensionManager extensionManager,DLClauseInfo dlClauseInfo,Node nodeX) {
            Node nodeY=dlClauseInfo.m_yNodes[m_yArgumentIndex];
            Node nodeYMirror;
            if (nodeY.isBlocked())
                nodeYMirror=nodeY.getBlocker();
            else
                nodeYMirror=nodeY;
            return extensionManager.containsAssertion(m_atomicConcept,nodeYMirror);
        }
    }
}
