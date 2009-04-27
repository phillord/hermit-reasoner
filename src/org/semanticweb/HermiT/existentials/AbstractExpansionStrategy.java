// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.ExistsDescriptionGraph;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.tableau.DescriptionGraphManager;
import org.semanticweb.HermiT.tableau.ExistentialExpansionManager;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.HermiT.tableau.InterruptFlag;

/**
 * Implements the common bits of an ExistentialsExpansionStrategy, leaving only actual processing of existentials in need of expansion to subclasses.
 */
public abstract class AbstractExpansionStrategy implements Serializable,ExistentialExpansionStrategy {
    private static final long serialVersionUID=2831957929321676444L;

    protected final BlockingStrategy m_blockingStrategy;
    protected final boolean m_expandNodeAtATime;
    protected final List<ExistentialConcept> m_processedExistentials;
    protected Tableau m_tableau;
    protected InterruptFlag m_interruptFlag;
    protected ExtensionManager m_extensionManager;
    protected ExistentialExpansionManager m_existentialExpansionManager;
    protected DescriptionGraphManager m_descriptionGraphManager;

    public AbstractExpansionStrategy(BlockingStrategy blockingStrategy,boolean expandNodeAtATime) {
        m_blockingStrategy=blockingStrategy;
        m_expandNodeAtATime=expandNodeAtATime;
        m_processedExistentials=new ArrayList<ExistentialConcept>();
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_interruptFlag=m_tableau.getInterruptFlag();
        m_extensionManager=m_tableau.getExtensionManager();
        m_existentialExpansionManager=m_tableau.getExistentialExpansionManager();
        m_descriptionGraphManager=m_tableau.getDescriptionGraphManager();
        m_blockingStrategy.initialize(m_tableau);
    }
    public void clear() {
        m_blockingStrategy.clear();
        m_processedExistentials.clear();
    }
    public boolean expandExistentials() {
        TableauMonitor monitor=m_tableau.getTableauMonitor();
        m_blockingStrategy.computeBlocking();
        boolean extensionsChanged=false;
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null && (!extensionsChanged || !m_expandNodeAtATime)) {
            if (node.isActive() && !node.isBlocked() && node.hasUnprocessedExistentials()) {
                // The node's set of unprocessed existentials may be changed during operation, so make a local copy to loop over.
                m_processedExistentials.clear();
                m_processedExistentials.addAll(node.getUnprocessedExistentials());
                for (int index=0;index<m_processedExistentials.size();index++) {
                    ExistentialConcept existentialConcept=m_processedExistentials.get(index);
                    if (existentialConcept instanceof AtLeastConcept) {
                        AtLeastConcept atLeastConcept=(AtLeastConcept)existentialConcept;
                        switch (m_existentialExpansionManager.isSatisfied(atLeastConcept,node)) {
                        case NOT_SATISFIED:
                            expandExistential(atLeastConcept,node);
                            extensionsChanged=true;
                            break;
                        case PERMANENTLY_SATISFIED:
                            m_existentialExpansionManager.markExistentialProcessed(existentialConcept,node);
                            if (monitor!=null)
                                monitor.existentialSatisfied(atLeastConcept,node);
                            break;
                        case CURRENTLY_SATISFIED:
                            // do nothing
                            if (monitor!=null)
                                monitor.existentialSatisfied(atLeastConcept,node);
                            break;
                        }
                    }
                    else if (existentialConcept instanceof ExistsDescriptionGraph) {
                        ExistsDescriptionGraph existsDescriptionGraph=(ExistsDescriptionGraph)existentialConcept;
                        if (!m_descriptionGraphManager.isSatisfied(existsDescriptionGraph,node)) {
                            m_descriptionGraphManager.expand(existsDescriptionGraph,node);
                            extensionsChanged=true;
                        }
                        else {
                            if (monitor!=null)
                                monitor.existentialSatisfied(existsDescriptionGraph,node);
                        }
                        m_existentialExpansionManager.markExistentialProcessed(existentialConcept,node);
                    }
                    else
                        throw new IllegalStateException("Unsupported type of existential.");
                    m_interruptFlag.checkInterrupt();
                }
            }
            node=node.getNextTableauNode();
            m_interruptFlag.checkInterrupt();
        }
        return extensionsChanged;
    }
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        m_blockingStrategy.assertionAdded(concept,node,isCore);
    }
    public void assertionCoreSet(Concept concept,Node node) {
        m_blockingStrategy.assertionCoreSet(concept,node);
    }
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        m_blockingStrategy.assertionRemoved(concept,node,isCore);
    }
    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        m_blockingStrategy.assertionAdded(atomicRole,nodeFrom,nodeTo,isCore);
    }
    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        m_blockingStrategy.assertionCoreSet(atomicRole,nodeFrom,nodeTo);
    }
    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        m_blockingStrategy.assertionRemoved(atomicRole,nodeFrom,nodeTo,isCore);
    }
    public void nodeStatusChanged(Node node) {
        m_blockingStrategy.nodeStatusChanged(node);
    }
    public void nodeInitialized(Node node) {
        m_blockingStrategy.nodeInitialized(node);
    }
    public void nodeDestroyed(Node node) {
        m_blockingStrategy.nodeDestroyed(node);
    }
    public void branchingPointPushed() {
    }
    public void backtrack() {
    }
    public void modelFound() {
        m_blockingStrategy.modelFound();
    }
    /**
     * This method performs the actual expansion.
     */
    protected abstract void expandExistential(AtLeastConcept atLeastConcept,Node forNode);
}
