// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;

public class AnywhereCoreBlocking implements BlockingStrategy, Serializable {
    private static final long serialVersionUID=-2959900333817197464L;

    protected Tableau m_tableau;
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final BlockersCache m_currentBlockersCache;
    protected final SetFactory<AtomicConcept> m_atomicConceptsSetFactory;
    protected final List<AtomicConcept> m_atomicConceptsBuffer;
    protected final Object[] m_auxiliaryTuple;
    protected ExtensionManager m_extensionManager;
    protected Node m_firstChangedNode;

    public AnywhereCoreBlocking(DirectBlockingChecker directBlockingChecker) {
        m_directBlockingChecker=directBlockingChecker;
        m_currentBlockersCache=new BlockersCache(m_directBlockingChecker); // contains all nodes that block some node
        m_atomicConceptsSetFactory=new SetFactory<AtomicConcept>();
        m_atomicConceptsBuffer=new ArrayList<AtomicConcept>();
        m_auxiliaryTuple=new Object[2];
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_directBlockingChecker.initialize(tableau);
        m_extensionManager=m_tableau.getExtensionManager();
    }
    public void clear() {
        m_currentBlockersCache.clear();
        m_firstChangedNode=null;
    }
    public void computeBlocking(boolean finalChance) {
        if (m_firstChangedNode!=null) {
            Node node=m_firstChangedNode;
            while (node!=null) {
                m_currentBlockersCache.removeNode(node);
                node=node.getNextTableauNode();
            }
            node=m_firstChangedNode;
            while (node!=null) {
                if (node.isActive() && (m_directBlockingChecker.canBeBlocked(node) || m_directBlockingChecker.canBeBlocker(node))) {
                    // otherwise the node is not relevant for blocking since (it is a root node) since it will not be blocked and cannot block
                    if (m_directBlockingChecker.hasBlockingInfoChanged(node) || !node.isDirectlyBlocked() || node.getBlocker().getNodeID()>=m_firstChangedNode.getNodeID()) {
                        // if the node has really changed or 
                        // the node is indirectly blocked in which case the directly blocked node that makes this one indirectly blocked might no longer be blocked or 
                        // the blocker might be among the changed nodes and is possibly not a suitable blocker any more
                        Node parent=node.getParent();
                        if (parent==null)
                            node.setBlocked(null,false); // no parent means it cannot be blocked
                        else if (parent.isBlocked()) // parent is guaranteed not to change it's status in this computation since we process nodes in creation order and parent is smaller
                            node.setBlocked(parent,false);
                        else {
                            Node blocker=m_currentBlockersCache.getBlocker(node); 
                            // note that we removed only nodes from the cache that are of order higher than the first changed element
                            // only nodes of lower order than this one can be blockers and all those have already been looked at in this computation or 
                            // were not removed from the cache since they did not change
                            node.setBlocked(blocker,blocker!=null);
                        }
                        if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                            m_currentBlockersCache.addNode(node);
                    }
                    m_directBlockingChecker.clearBlockingInfoChanged(node);
                }
                node=node.getNextTableauNode();
            }
            m_firstChangedNode=null;
        }
    }
    public boolean isPermanentAssertion(Concept concept,Node node) {
        m_auxiliaryTuple[0]=concept;
        m_auxiliaryTuple[1]=node;
        return m_extensionManager.isCore(m_auxiliaryTuple);
    }
    // Assertions can be added directly into the core, but we also have the possibility of setting the core flag later?
    // In that case, assertionCoreSet (below) will be called?
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        if (isCore && concept instanceof AtomicConcept) {
            updateNodeChange(m_directBlockingChecker.assertionAdded(concept,node));
        }
    }
    public void assertionCoreSet(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            updateNodeChange(m_directBlockingChecker.assertionAdded(concept,node));
        } 
    }
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        if (isCore && concept instanceof AtomicConcept) {
            updateNodeChange(m_directBlockingChecker.assertionRemoved(concept,node));
        }
    }
    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        if (isCore) {
            updateNodeChange(m_directBlockingChecker.assertionAdded(atomicRole, nodeFrom, nodeTo));
        }
    }
    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        updateNodeChange(m_directBlockingChecker.assertionAdded(atomicRole, nodeFrom, nodeTo));
    }
    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        if (isCore) {
            updateNodeChange(m_directBlockingChecker.assertionRemoved(atomicRole, nodeFrom, nodeTo));
        }
    }
    public void nodeStatusChanged(Node node) {
        updateNodeChange(node);
    }
    protected final void updateNodeChange(Node node) {
        if (node!=null && (m_firstChangedNode==null || node.getNodeID()<m_firstChangedNode.getNodeID()))
            m_firstChangedNode=node;
    }
    public void nodeInitialized(Node node) {
        m_directBlockingChecker.nodeInitialized(node);
    }
    public void nodeDestroyed(Node node) {
        m_currentBlockersCache.removeNode(node);
        m_directBlockingChecker.nodeDestroyed(node);
        if (m_firstChangedNode!=null && m_firstChangedNode.getNodeID()>=node.getNodeID())
            m_firstChangedNode=null;
    }
    public void modelFound() {
    }
    public boolean isExact() {
        return false;
    }
    public void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,Object[] valuesBuffer,boolean[] coreVariables) {
        workers.add(new ComputeCoreVariables(valuesBuffer,coreVariables));
    }

    protected static final class ComputeCoreVariables implements DLClauseEvaluator.Worker,Serializable {
        private static final long serialVersionUID=899293772370136783L;

        protected final Object[] m_valuesBuffer;
        protected final boolean[] m_coreVariables;

        public ComputeCoreVariables(Object[] valuesBuffer,boolean[] coreVariables) {
            m_valuesBuffer=valuesBuffer;
            m_coreVariables=coreVariables;
        }
        public int execute(int programCounter) {
            Node potentialNoncore=null;
            int potentialNoncoreIndex=-1;
            for (int variableIndex=m_coreVariables.length-1;variableIndex>=0;--variableIndex) {
                m_coreVariables[variableIndex]=true;
                Node node=(Node)m_valuesBuffer[variableIndex];
                if (node.getNodeType()==NodeType.TREE_NODE && (potentialNoncore==null || node.getTreeDepth()<potentialNoncore.getTreeDepth())) {
                    potentialNoncore=node;
                    potentialNoncoreIndex=variableIndex;
                }
            }
            if (potentialNoncore!=null) {
                boolean isNoncore=true;
                for (int variableIndex=m_coreVariables.length-1;isNoncore && variableIndex>=0;--variableIndex) {
                    Node node=(Node)m_valuesBuffer[variableIndex];
                    if (!node.isRootNode() && potentialNoncore!=node && !potentialNoncore.isAncestorOf(node))
                        isNoncore=false;
                }
                if (isNoncore)
                    m_coreVariables[potentialNoncoreIndex]=false;
            }
            return programCounter+1;
        }
        public String toString() {
            return "Compute core variables";
        }
    }
}