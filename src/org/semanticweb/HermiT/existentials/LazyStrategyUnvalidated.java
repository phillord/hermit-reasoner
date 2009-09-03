// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.UnvalidatedAnywhereCoreBlocking;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.ExistsDescriptionGraph;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.tableau.Node;

/**
 * Strategy for expanding all existentials on the oldest node in the tableau with unexpanded existentials.
 * This usually closely approximates a breadth-first expansion. (Existentials introduced onto parent nodes
 * as a result of constraints on their children can produce newer nodes of lower depth than older nodes,
 * which could result in slight non-breadth-first behavior.)
 */
public class LazyStrategyUnvalidated extends AbstractExpansionStrategy implements Serializable {
    private static final long serialVersionUID=-64673639237063636L;
    
    protected final Set<Node> m_nodesToExpand=new HashSet<Node>();
    protected final Set<Node> m_nodesWithFinishedExpansion=new HashSet<Node>();
    protected boolean expandOneAtATime=false;
    protected int numExpansions=0;
    protected final boolean printingOn=false;
    
    public LazyStrategyUnvalidated(BlockingStrategy strategy) {
        super(strategy,false);
    }
    public boolean isDeterministic() {
        return true;
    }
    protected void expandExistential(AtLeastConcept atLeastConcept,Node forNode) {
        m_existentialExpansionManager.expand(atLeastConcept,forNode);
        m_existentialExpansionManager.markExistentialProcessed(atLeastConcept,forNode);
    }
    public void clear() {
        super.clear();
        m_nodesToExpand.clear();
    }
    public boolean expandExistentials(boolean finalChance) {
        boolean extensionsChanged=false;
        // check blocks and collect unblocked nodes that need existential expansion in m_nodesToExpand
        ((UnvalidatedAnywhereCoreBlocking)m_blockingStrategy).computePreBlocking(m_nodesToExpand);
        if (printingOn) System.out.println(m_nodesToExpand.size() + " nodes need expansion. ");
        m_interruptFlag.checkInterrupt();
        for (Node node : m_nodesToExpand) {
            if (node.isActive() && !node.isBlocked() && node.hasUnprocessedExistentials()) {
                boolean hasChangedForThisNode=false;
                for (ExistentialConcept ec : new HashSet<ExistentialConcept>(node.getUnprocessedExistentials())) {
                    hasChangedForThisNode=doExpansion(node, ec);
                    if (hasChangedForThisNode) break; // expand one existential per node at a time
                }
                extensionsChanged = (extensionsChanged || hasChangedForThisNode);
            } else {
                m_nodesWithFinishedExpansion.add(node);
            }
            m_interruptFlag.checkInterrupt();
        }
        m_nodesToExpand.removeAll(m_nodesWithFinishedExpansion);
        m_nodesWithFinishedExpansion.clear();
        return extensionsChanged;
    }
    protected boolean doExpansion(Node node, ExistentialConcept existentialConcept) {
        boolean extensionsChanged=false;
        TableauMonitor monitor=m_tableau.getTableauMonitor();
        if (existentialConcept instanceof AtLeastConcept) {
            AtLeastConcept atLeastConcept=(AtLeastConcept)existentialConcept;
            switch (isSatisfied(atLeastConcept,node)) {
            case NOT_SATISFIED:
                expandExistential(atLeastConcept,node); // this will also mark the existential as processed
                extensionsChanged=true;
                break;
            case PERMANENTLY_SATISFIED: // not satisfied by a nominal so that the NN/NI rule can break the existential 
                m_existentialExpansionManager.markExistentialProcessed(existentialConcept,node);
                if (monitor!=null)
                    monitor.existentialSatisfied(atLeastConcept,node);
                break;
            case CURRENTLY_SATISFIED: // satisfied until the NN/NI rule is applied and after which the existential might no longer be satisfied
                // do nothing
                //if (currentlySatisfiedIsPermanentlySatisfied) {
                    m_existentialExpansionManager.markExistentialProcessed(existentialConcept,node);
                //}
                if (monitor!=null)
                    monitor.existentialSatisfied(atLeastConcept,node);
                break;
            }
        } else if (existentialConcept instanceof ExistsDescriptionGraph) {
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
        } else
            throw new IllegalStateException("Unsupported type of existential.");
        if (!node.hasUnprocessedExistentials()) {
            m_nodesWithFinishedExpansion.add(node);
        }
        m_interruptFlag.checkInterrupt();
        return extensionsChanged;
    }
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        super.assertionAdded(concept,node,isCore);
        if (concept instanceof AtLeastConcept) {
            m_nodesToExpand.add(node);
        } 
    }
}
