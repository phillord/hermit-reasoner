package org.semanticweb.HermiT.blocking;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

/**
 * Checks whether the rules from some set are applicable given the current state of the extensions.
 */
public class BlockingValidator {
    protected final Tableau m_tableau;
    protected final ExtensionManager m_extensionManager;
    protected final List<DLClauseInfo> m_dlClauseInfos;

    public BlockingValidator(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=tableau.getExtensionManager();
        m_dlClauseInfos=new ArrayList<DLClauseInfo>();
        for (DLClause dlClause : tableau.getDLOntology().getDLClauses()) {
            if (!dlClause.isConceptInclusion() && !dlClause.isRoleInclusion() && !dlClause.isRoleInverseInclusion())
                m_dlClauseInfos.add(new DLClauseInfo(dlClause));
        }
    }
    /**
     * This method assumes that blocking has been computed and it then
     * checks whether any of the rules is applicable after we unravel the model
     * according to the rules described in the HT paper.
     */ 
    public boolean isBlockingValid() {
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (node.isActive() && node.isDirectlyBlocked()) {
                if (!satisfiesConstraintsForBlockedX(node))
                    return false;
                // TODO: This will potentially check the parent several times,
                // if it has multiple blocked children. This should be fixed.
                if (!satisfiesConstraintsForNonblockedX(node.getParent()))
                    return false;
            }
            node=node.getNextTableauNode();
        }
        return true;
    }
    
    // These methods check the constraint satisfaction for the case when X is matched to a blocked node 
    
    protected boolean satisfiesConstraintsForBlockedX(Node blockedX) {
        // TODO: Check existentials!
        for (DLClauseInfo dlClauseInfo : m_dlClauseInfos)
            if (!satisfiesDLClauseForBlockedX(dlClauseInfo,blockedX))
                return false;
        return true;
    }
    protected boolean satisfiesDLClauseForBlockedX(DLClauseInfo dlClauseInfo,Node blockedX) {
        assert blockedX.isDirectlyBlocked();
        Node blockedXParent=blockedX.getParent();
        Node blocker=blockedX.getBlocker();
        // Check whether some of the X concepts can be matched to the blocker
        for (AtomicConcept atomicConcept : dlClauseInfo.m_xConcepts)
            if (!m_extensionManager.containsAssertion(atomicConcept,blocker))
                return false;
        // Find one yConstraint that involves a parent of blockedX
        int matchingYConstraintIndex=-1;
        for (int yIndex=0;matchingYConstraintIndex==-1 && yIndex<dlClauseInfo.m_yConstraints.length;yIndex++)
            if (dlClauseInfo.m_yConstraints[yIndex].isSatisfiedExplicitly(m_extensionManager,blockedX,blockedXParent))
                matchingYConstraintIndex=yIndex;
        if (matchingYConstraintIndex==-1)
            return true;
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
            return satisfiesDLClauseForBlockedXAnyZAndAnyY(dlClauseInfo,blockedX,parentOfBlockedXIndex,toMatchIndex+1);
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
                retrieval.getBindingsBuffer()[0]=yConstraint.m_x2yRoles[0];
                retrieval.getBindingsBuffer()[1]=blocker;
                yNodeIndex=2;
            }
            else {
                retrieval=dlClauseInfo.m_y2xRetrievals[toMatchIndex];
                retrieval.getBindingsBuffer()[0]=yConstraint.m_y2xRoles[0];
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
                    dlClauseInfo.m_yNodes[toMatchIndex]=null;
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

    protected boolean satisfiesConstraintsForNonblockedX(Node nonblockedX) {
        // TODO: Check existentials!
        for (DLClauseInfo dlClauseInfo : m_dlClauseInfos)
            if (!satisfiesDLClauseForNonblockedX(dlClauseInfo,nonblockedX))
                return false;
        return true;
    }
    protected boolean satisfiesDLClauseForNonblockedX(DLClauseInfo dlClauseInfo,Node nonblockedX) {
        // Check whether some of the X concepts can be matched to the node
        for (AtomicConcept atomicConcept : dlClauseInfo.m_xConcepts)
            if (!m_extensionManager.containsAssertion(atomicConcept,nonblockedX))
                return false;
        dlClauseInfo.m_xNode=nonblockedX;
        // Examine all possible matches for the Zs (and recursively for Ys then as well)
        boolean result=satisfiesDLClauseForNonblockedXAndAnyZ(dlClauseInfo,nonblockedX,0);
        dlClauseInfo.m_xNode=null;
        return result;
    }
    protected boolean satisfiesDLClauseForNonblockedXAndAnyZ(DLClauseInfo dlClauseInfo,Node nonblockedX,int toMatchIndex) {
        if (toMatchIndex==dlClauseInfo.m_zNodes.length)
            return satisfiesDLClauseForNonblockedXAnyZAndAnyY(dlClauseInfo,nonblockedX,0);
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
                    boolean result=satisfiesDLClauseForNonblockedXAndAnyZ(dlClauseInfo,nonblockedX,toMatchIndex+1);
                    dlClauseInfo.m_zNodes[toMatchIndex]=null;
                    if (!result)
                        return false;
                }
                retrieval.next();
            }
            return true;
        }
    }
    protected boolean satisfiesDLClauseForNonblockedXAnyZAndAnyY(DLClauseInfo dlClauseInfo,Node nonblockedX,int toMatchIndex) {
        if (toMatchIndex==dlClauseInfo.m_yConstraints.length)
            return satisfiesDLClauseForNonblockedXAndMatchedNodes(dlClauseInfo,nonblockedX);
        else {
            Node blocker=nonblockedX.getBlocker();
            Node blockerParent=blocker.getParent();
            YConstraint yConstraint=dlClauseInfo.m_yConstraints[toMatchIndex];
            assert yConstraint.m_x2yRoles.length!=0 || yConstraint.m_y2xRoles.length!=0;
            int yNodeIndex;
            ExtensionTable.Retrieval retrieval;
            if (yConstraint.m_x2yRoles.length!=0) {
                retrieval=dlClauseInfo.m_x2yRetrievals[toMatchIndex];
                retrieval.getBindingsBuffer()[0]=yConstraint.m_x2yRoles[0];
                retrieval.getBindingsBuffer()[1]=blocker;
                yNodeIndex=2;
            }
            else {
                retrieval=dlClauseInfo.m_y2xRetrievals[toMatchIndex];
                retrieval.getBindingsBuffer()[0]=yConstraint.m_y2xRoles[0];
                yNodeIndex=1;
                retrieval.getBindingsBuffer()[2]=blocker;
            }
            retrieval.open();
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                Node nodeY=(Node)tupleBuffer[yNodeIndex];
                if (nodeY!=blockerParent && yConstraint.isSatisfiedViaMirroringY(m_extensionManager,blocker,nodeY)) {
                    dlClauseInfo.m_yNodes[toMatchIndex]=nodeY;
                    boolean result=satisfiesDLClauseForNonblockedXAnyZAndAnyY(dlClauseInfo,nonblockedX,toMatchIndex+1);
                    dlClauseInfo.m_yNodes[toMatchIndex]=null;
                    if (!result)
                        return false;
                }
                retrieval.next();
            }
            return true;
        }
    }
    protected boolean satisfiesDLClauseForNonblockedXAndMatchedNodes(DLClauseInfo dlClauseInfo,Node nonblockedX) {
        for (ConsequenceAtom consequenceAtom : dlClauseInfo.m_consequencesForNonblockedX) {
            if (consequenceAtom.isSatisfied(m_extensionManager,dlClauseInfo,nonblockedX))
                return true;
        }
        return false;
    }
    
    protected static class DLClauseInfo {
        protected final AtomicConcept[] m_xConcepts;
        protected final YConstraint[] m_yConstraints;
        protected final AtomicConcept[][] m_zConcepts;
        protected final ExtensionTable.Retrieval[] m_x2yRetrievals;
        protected final ExtensionTable.Retrieval[] m_y2xRetrievals;
        protected final ExtensionTable.Retrieval[] m_zRetrievals;
        protected final ConsequenceAtom[] m_consequencesForBlockedX;
        protected final ConsequenceAtom[] m_consequencesForNonblockedX;
        protected Node m_xNode;
        protected Node[] m_yNodes;
        protected Node[] m_zNodes;
        
        public DLClauseInfo(DLClause dlClause) {
            // TODO: We'll sort our the variables by names. This introduces a dependency
            // to clausification. That's ugly and should be fixed later.
            // TODO: Initialize these values properly!
            m_xConcepts=null;
            m_yConstraints=null;
            m_zConcepts=null;
            m_x2yRetrievals=null;
            m_y2xRetrievals=null;
            m_zRetrievals=null;
            m_consequencesForBlockedX=null;
            m_consequencesForNonblockedX=null;
            m_xNode=null;
            m_yNodes=null;
            m_zNodes=null;
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
