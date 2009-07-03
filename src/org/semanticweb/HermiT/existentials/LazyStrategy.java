// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.blocking.AnywhereCoreBlocking;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.model.AtLeastConcept;
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
public class LazyStrategy extends AbstractExpansionStrategy implements Serializable {
    private static final long serialVersionUID=-64673639237063636L;
    
    protected final Set<Node> m_nodesToExpand=new HashSet<Node>();
    protected final Set<Node> m_nodesWithFinishedExpansion=new HashSet<Node>();
    protected final Set<Node> m_nodesToCheckBlocking=new HashSet<Node>();
    protected boolean expandOneAtATime=false;
    protected int numExpansions=0;
    
    protected final boolean printingOn=true;
    
    public LazyStrategy(BlockingStrategy strategy) {
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
        m_nodesToCheckBlocking.clear();
        m_nodesToExpand.clear();
    }
    public boolean expandExistentials(boolean finalChance) {
        boolean extensionsChanged=false;
        m_nodesToExpand.addAll(((AnywhereCoreBlocking)m_blockingStrategy).getUnblockedNodesWithUnprocessedExistentials());
        if (printingOn) System.out.println(m_nodesToExpand.size() + " nodes need expansion. ");
        if (m_nodesToExpand.size()==0 && finalChance) {
            m_nodesToExpand.addAll(m_blockingStrategy.checkAllBlocks());
            System.out.println("Checked all blocks");
            m_interruptFlag.checkInterrupt();
            if (m_nodesToExpand.size() > 0) return true;
        }
        for (Node node : m_nodesToExpand) {
            if (node.isActive() && !node.isBlocked() && node.hasUnprocessedExistentials()) {
                boolean hasChangedForThisNode=doExpansion(node);
                extensionsChanged = (extensionsChanged || hasChangedForThisNode);
            }
        }
        if (!extensionsChanged) {
            System.out.println("No more extension changes...");
        }
        m_interruptFlag.checkInterrupt();
        m_nodesToExpand.removeAll(m_nodesWithFinishedExpansion);
        m_nodesWithFinishedExpansion.clear();
        return extensionsChanged;
    }
//    public boolean expandExistentials(boolean finalChance) {
//        boolean extensionsChanged=false;
//        if (finalChance && !expandOneAtATime) {
//            m_nodesToCheckBlocking.clear();
//            m_nodesToExpand.clear();
//            //m_nodesToExpand.addAll(m_blockingStrategy.checkAllBlocks());
//            expandOneAtATime=true;
//            if (printingOn) System.out.println("After checking all nodes, " + m_nodesToExpand.size() + " might need existential expansion. ");
//        } else {
//            m_nodesToCheckBlocking.removeAll(m_nodesToExpand);
//            if (printingOn) System.out.println("Nodes to be check for blocking: " + m_nodesToCheckBlocking.size() + ", for expansion: " + m_nodesToExpand.size());
//            Set<Node> newlyUnblockedNodes=new HashSet<Node>();
//            for (Node node : m_nodesToCheckBlocking) {
//                boolean wasBlocked=node.isBlocked();
//                if (wasBlocked && !m_blockingStrategy.computeIsBlocked(node)) {
//                    newlyUnblockedNodes.add(node);
//                }
//            }
//            m_nodesToCheckBlocking.clear();
//            Set<Node> newlyBlockedNodes=new HashSet<Node>();
//            for (Node node : m_nodesToExpand) {
//                if (m_blockingStrategy.computeIsBlocked(node)) {
//                    newlyBlockedNodes.add(node);
//                }
//            }
//            m_nodesToExpand.removeAll(newlyBlockedNodes);
//            m_nodesToExpand.addAll(newlyUnblockedNodes);
//            if (m_nodesToExpand.isEmpty()) {
//                Node node=m_tableau.getFirstTableauNode();
//                while (node!=null) {
//                    boolean wasBlocked=node.isBlocked();
//                    if (wasBlocked && !m_blockingStrategy.computeIsBlocked(node)) {
//                        newlyUnblockedNodes.add(node);
//                    }
//                    node=node.getNextTableauNode();
//                }
//            }
//            m_nodesToExpand.addAll(newlyUnblockedNodes);
//        }
//        if (expandOneAtATime) {
//            m_blockingStrategy.computeBlocking(finalChance);
//            Node node=m_tableau.getFirstTableauNode();
//            while (node!=null && !extensionsChanged) {
//                extensionsChanged=doExpansion(node);
//                node=node.getNextTableauNode();
//            }
//        } else {
//            for (Node node : m_nodesToExpand) {
//                if (node.isActive() && !node.isBlocked() && node.hasUnprocessedExistentials()) {
//                    boolean hasChangedForThisNode=doExpansion(node);
//                    extensionsChanged = (extensionsChanged || hasChangedForThisNode);
//                }
//            }
//            if (!extensionsChanged) {
//                System.out.println("No more extension changes...");
//            }
//        }
//        m_interruptFlag.checkInterrupt();
//        m_nodesToExpand.clear();
//        return extensionsChanged;
//    }
    protected boolean doExpansion(Node node) {
        boolean extensionsChanged=false;
        TableauMonitor monitor=m_tableau.getTableauMonitor();
        ExistentialConcept existentialConcept=node.getSomeUnprocessedExistential();
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
//    public boolean expandExistentials(boolean finalChance) {
////        TableauMonitor monitor=m_tableau.getTableauMonitor();
//        boolean extensionsChanged=false;
//        if (finalChance) {
//            m_nodesToCheckBlocking.clear();
//            m_nodesToExpand.clear();
//            m_nodesToExpand.addAll(m_blockingStrategy.checkAllBlocks());
//            if (printingOn) System.out.println("After checking all nodes, " + m_nodesToExpand.size() + " might need existential expansion. ");
//            expandOneAtATime=true;
//        } else {
//            m_nodesToCheckBlocking.removeAll(m_nodesToExpand);
//            if (printingOn) System.out.println("Nodes to be check for blocking: " + m_nodesToCheckBlocking.size());
//            if (printingOn) System.out.println("Nodes to be check for expansion: " + m_nodesToExpand.size());
//            Set<Node> newlyUnblockedNodes=new HashSet<Node>();
//            for (Node node : m_nodesToCheckBlocking) {
//                boolean wasBlocked=node.isBlocked();
//                if (wasBlocked && !m_blockingStrategy.computeIsBlocked(node)) {
//                    newlyUnblockedNodes.add(node);
//                }
//            }
//            m_nodesToCheckBlocking.clear();
//            for (Node node : m_nodesToExpand) {
//                m_blockingStrategy.computeIsBlocked(node);
//            }
//            m_nodesToExpand.addAll(newlyUnblockedNodes);
//        }
//        for (Node node : m_nodesToExpand) {
//            if (node.isActive() && !node.isBlocked() && node.hasUnprocessedExistentials()) {
//                boolean hasChangedForThisNode=doExpansion(node);
//                extensionsChanged = (extensionsChanged || hasChangedForThisNode);
//            }
//            m_interruptFlag.checkInterrupt();
//        }
//        m_nodesToExpand.clear();
//        return extensionsChanged;
//    }
//    public void assertionAdded(Concept concept,Node node,boolean isCore) {
//        super.assertionAdded(concept,node,isCore);
//        if (concept instanceof AtLeastConcept) {
//            m_nodesToExpand.add(node);
//        } 
//        else if (isCore) {
//            m_nodesToCheckBlocking.add(node);
//        }
//    }
//    public void assertionCoreSet(Concept concept,Node node) {
//        super.assertionCoreSet(concept,node);
//        m_nodesToCheckBlocking.add(node);
//    }
//    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
//        super.assertionAdded(atomicRole,nodeFrom,nodeTo,isCore);
//        if (isCore) {
//            nodesToCheckBlocking.add(nodeTo);
//        }
//    }
//    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
//        super.assertionCoreSet(atomicRole,nodeFrom,nodeTo);
//        nodesToCheckBlocking.add(nodeTo);
//    }
//    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
//        super.assertionRemoved(atomicRole,nodeFrom,nodeTo,isCore);
//        if (isCore) {
//            nodesToCheckBlocking.add(nodeTo);
//        }
//    }
}
