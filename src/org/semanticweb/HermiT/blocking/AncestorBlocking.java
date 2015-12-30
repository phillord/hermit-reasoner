/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.List;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

/**Ancestor blocking strategy.*/
public class AncestorBlocking implements BlockingStrategy,Serializable {
    private static final long serialVersionUID=1075850000309773283L;

    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final BlockingSignatureCache m_blockingSignatureCache;
    protected Tableau m_tableau;
    protected boolean m_useBlockingSignatureCache;

    /**
     * @param directBlockingChecker directBlockingChecker
     * @param blockingSignatureCache blockingSignatureCache
     */
    public AncestorBlocking(DirectBlockingChecker directBlockingChecker,BlockingSignatureCache blockingSignatureCache) {
        m_directBlockingChecker=directBlockingChecker;
        m_blockingSignatureCache=blockingSignatureCache;
    }
    @Override
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_directBlockingChecker.initialize(tableau);
        updateBlockingSignatureCacheUsage();
    }
    @Override
    public void additionalDLOntologySet(DLOntology additionalDLOntology) {
        updateBlockingSignatureCacheUsage();
    }
    @Override
    public void additionalDLOntologyCleared() {
        updateBlockingSignatureCacheUsage();
    }
    protected void updateBlockingSignatureCacheUsage() {
        m_useBlockingSignatureCache=(m_tableau.getAdditionalHyperresolutionManager()==null);
    }
    @Override
    public void clear() {
        m_directBlockingChecker.clear();
    }
    @Override
    public void computeBlocking(boolean finalChance) {
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (node.isActive()) {
                Node parent=node.getParent();
                if (parent==null)
                    node.setBlocked(null,false);
                else if (parent.isBlocked())
                    node.setBlocked(parent,false);
                else if (m_useBlockingSignatureCache && m_blockingSignatureCache!=null && m_blockingSignatureCache.containsSignature(node))
                    node.setBlocked(Node.SIGNATURE_CACHE_BLOCKER,true);
                else
                    checkParentBlocking(node);
            }
            node=node.getNextTableauNode();
        }
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
    @Override
    public boolean isPermanentAssertion(Concept concept,Node node) {
        return true;
    }
    @Override
    public boolean isPermanentAssertion(DataRange range,Node node) {
        return true;
    }
    @Override
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        m_directBlockingChecker.assertionAdded(concept,node,isCore);
    }
    @Override
    public void assertionCoreSet(Concept concept,Node node) {
    }
    @Override
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        m_directBlockingChecker.assertionRemoved(concept,node,isCore);
    }
    @Override
    public void assertionAdded(DataRange range,Node node,boolean isCore) {
        m_directBlockingChecker.assertionAdded(range,node,isCore);
    }
    @Override
    public void assertionCoreSet(DataRange range,Node node) {
    }
    @Override
    public void assertionRemoved(DataRange range,Node node,boolean isCore) {
        m_directBlockingChecker.assertionRemoved(range,node,isCore);
    }
    @Override
    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        m_directBlockingChecker.assertionAdded(atomicRole,nodeFrom,nodeTo,isCore);
    }
    @Override
    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        m_directBlockingChecker.assertionAdded(atomicRole,nodeFrom,nodeTo,true);
    }
    @Override
    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        m_directBlockingChecker.assertionRemoved(atomicRole,nodeFrom,nodeTo,isCore);
    }
    @Override
    public void nodesMerged(Node mergeFrom,Node mergeInto) {
        m_directBlockingChecker.nodesMerged(mergeFrom,mergeInto);
    }
    @Override
    public void nodesUnmerged(Node mergeFrom,Node mergeInto) {
        m_directBlockingChecker.nodesUnmerged(mergeFrom,mergeInto);
    }
    @Override
    public void nodeStatusChanged(Node node) {
    }
    @Override
    public void nodeInitialized(Node node) {
        m_directBlockingChecker.nodeInitialized(node);
    }
    @Override
    public void nodeDestroyed(Node node) {
        m_directBlockingChecker.nodeDestroyed(node);
    }
    @Override
    public void modelFound() {
        if (m_useBlockingSignatureCache && m_blockingSignatureCache!=null) {
            // Since we've found a model, we know what is blocked and what is not, so we don't need to update the blocking status.
            Node node=m_tableau.getFirstTableauNode();
            while (node!=null) {
                if (node.isActive() && !node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                    m_blockingSignatureCache.addNode(node);
                node=node.getNextTableauNode();
            }
        }
    }
    @Override
    public boolean isExact() {
        return true;
    }
    @Override
    public void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,List<Variable> variables,Object[] valuesBuffer,boolean[] coreVariables) {
        for (int i=0;i<coreVariables.length;i++) {
            coreVariables[i]=true;
        }
    }
}
