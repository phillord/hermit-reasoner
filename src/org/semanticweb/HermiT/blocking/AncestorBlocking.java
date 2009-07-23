// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.List;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

public class AncestorBlocking implements BlockingStrategy,Serializable {
    private static final long serialVersionUID=1075850000309773283L;
    
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final BlockingSignatureCache m_blockingSignatureCache;
    protected Tableau m_tableau;
    
    public AncestorBlocking(DirectBlockingChecker directBlockingChecker,BlockingSignatureCache blockingSignatureCache) {
        m_directBlockingChecker=directBlockingChecker;
        m_blockingSignatureCache=blockingSignatureCache;
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_directBlockingChecker.initialize(tableau);
    }
    public void clear() {
        m_directBlockingChecker.clear();
    }
    public void computeBlocking(boolean finalChance) {
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (node.isActive()) {
                Node parent=node.getParent();
                if (parent==null)
                    node.setBlocked(null,false);
                else if (parent.isBlocked())
                    node.setBlocked(parent,false);
                else if (m_blockingSignatureCache!=null && m_blockingSignatureCache.containsSignature(node))
                    node.setBlocked(Node.SIGNATURE_CACHE_BLOCKER,true);
                else
                    checkParentBlocking(node);
            }
            node=node.getNextTableauNode();
        }
    }
    public boolean computeIsBlocked(Node node) {
        throw new UnsupportedOperationException("Unsupported operation: Ancestor blocking cannot be used with a lazy expansion strategy. ");
    }
    protected final void checkParentBlocking(Node node) {
        Node blocker=node.getParent();
        while (blocker!=null) { 
            if (m_directBlockingChecker.isBlockedBy(blocker,node)) {
                node.setBlocked(blocker,true);
                break;
            }
            blocker=blocker.getParent();
        }
    }
    public boolean isPermanentAssertion(Concept concept,Node node) {
        return true;
    }
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        m_directBlockingChecker.assertionAdded(concept,node);
    }
    public void assertionCoreSet(Concept concept,Node node) {
    }
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        m_directBlockingChecker.assertionRemoved(concept,node);
    }
    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        m_directBlockingChecker.assertionAdded(atomicRole,nodeFrom,nodeTo);
    }
    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
    }
    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        m_directBlockingChecker.assertionRemoved(atomicRole,nodeFrom,nodeTo);
    }
    public void nodeStatusChanged(Node node) {
    }
    public void nodeInitialized(Node node) {
        m_directBlockingChecker.nodeInitialized(node);
    }
    public void nodeDestroyed(Node node) {
        m_directBlockingChecker.nodeDestroyed(node);
    }
    public void modelFound() {
        if (m_blockingSignatureCache!=null) {
            computeBlocking(false);
            Node node=m_tableau.getFirstTableauNode();
            while (node!=null) {
                if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                    m_blockingSignatureCache.addNode(node);
                node=node.getNextTableauNode();
            }
        }
    }
    public boolean isExact() {
        return true;
    }
    public void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,List<Variable> variables,Object[] valuesBuffer,boolean[] coreVariables) {
    }
}
