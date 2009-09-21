// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.old.AnywhereCoreBlockingOld;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.ExistsDescriptionGraph;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

/**
 * Strategy for expanding all existentials on the oldest node in the tableau with unexpanded existentials.
 * This usually closely approximates a breadth-first expansion. (Existentials introduced onto parent nodes
 * as a result of constraints on their children can produce newer nodes of lower depth than older nodes,
 * which could result in slight non-breadth-first behavior.)
 */
public class LazyStrategy extends AbstractExpansionStrategy implements Serializable {
    private static final long serialVersionUID=-64673639237063636L;
    
    protected final Set<Node> m_nodesToExpand=new HashSet<Node>();
    //protected final Set<Node> m_nodesWithFinishedExpansion=new HashSet<Node>();
    protected final Map<Node, Set<AtLeastConcept>> m_nodesNonPermanentSatExt=new HashMap<Node, Set<AtLeastConcept>>();
    protected Node m_smallestNodeThatNeedsExpansion=null;
    protected boolean expandOneAtATime=false;
    protected int numExpansions=0;
    protected final boolean printingOn=true;
    protected final boolean debuggingMode=false;
    
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
    public void initialize(Tableau tableau) {
        super.initialize(tableau);
    }
    public void clear() {
        super.clear();
        m_nodesToExpand.clear();
        m_nodesNonPermanentSatExt.clear();
        m_smallestNodeThatNeedsExpansion=null;
    }
//    public boolean expandExistentials(boolean finalChance) {
//        boolean extensionsChanged=false;
//        m_nodesToExpand.clear();
//        // check blocks and collect unblocked nodes that need existential expansion in m_nodesToExpand
//        if (!finalChance) {
//            ((AnywhereCoreBlocking)m_blockingStrategy).computePreBlocking(m_smallestNodeThatNeedsExpansion, m_nodesToExpand); // goes through all nodes from the smallest modified node and adds non-blocked nodes with unprocessed existentials to m_nodestoExpand
//        } else {
//            ((AnywhereCoreBlocking)m_blockingStrategy).validateBlocks(m_smallestNodeThatNeedsExpansion, m_nodesToExpand);
//        }
//        Set<Node> inactive=new HashSet<Node>();
//        for (Node n : m_nodesNonPermanentSatExt.keySet()) {
//            if (n.isActive() && !n.isBlocked()) {
//                for (AtLeastConcept c : m_nodesNonPermanentSatExt.get(n)) {
//                    if (isSatisfied(c, n)==SatType.NOT_SATISFIED) {
//                        m_nodesToExpand.add(n);
//                    }
//                }
//            } else {
//                inactive.add(n);
//            }
//        }
//        for (Node n : inactive) {
//            m_nodesNonPermanentSatExt.remove(n);
//        }
//        if (debuggingMode) doSanityCheck();
//        if (debuggingMode && printingOn) System.out.println(m_nodesToExpand.size() + " nodes need expansion. ");
//        m_interruptFlag.checkInterrupt();
//        Node smallestNodeThatNeedsExpansion=null;
//        for (Node node : m_nodesToExpand) {
//            if (node.isActive() && !node.isBlocked() && node.hasUnprocessedExistentials()) {
//                boolean hasChangedForThisNode=false;
//                for (ExistentialConcept ec : new HashSet<ExistentialConcept>(node.getUnprocessedExistentials())) {
//                    hasChangedForThisNode=doExpansion(node, ec);
//                    if (hasChangedForThisNode) {
//                        break; // expand one existential per node at a time
//                    }
//                }
//                extensionsChanged = (extensionsChanged || hasChangedForThisNode);
//            }
//            if (node.hasUnprocessedExistentials() && (smallestNodeThatNeedsExpansion==null || node.getNodeID()<smallestNodeThatNeedsExpansion.getNodeID())) {
//                smallestNodeThatNeedsExpansion=node;
//            }
//            m_interruptFlag.checkInterrupt();
//        }
//        m_smallestNodeThatNeedsExpansion=smallestNodeThatNeedsExpansion;
//        return extensionsChanged;
//    }
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
                // m_existentialExpansionManager.markExistentialProcessed(existentialConcept,node);
                // watch if still valid
                if (m_nodesNonPermanentSatExt.containsKey(node)) {
                    m_nodesNonPermanentSatExt.get(node).add(atLeastConcept);
                } else {
                    Set<AtLeastConcept> s=new HashSet<AtLeastConcept>();
                    s.add(atLeastConcept);
                    m_nodesNonPermanentSatExt.put(node, s);
                }
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
        m_interruptFlag.checkInterrupt();
        return extensionsChanged;
    }
    public void nodeDestroyed(Node node) {
        super.nodeDestroyed(node);
        m_nodesNonPermanentSatExt.remove(node);
        m_nodesToExpand.remove(node);
    }
    protected void doSanityCheck() {
        m_tableau.checkTableauList();
        for (Node n : m_nodesToExpand) {
            if (!n.isActive()) throw new IllegalStateException("LE: Node "+n+" is not active but in the set of nodes that need expansion. ");
            if (n.isBlocked()) throw new IllegalStateException("LE: Node "+n+" is blocked but in the set of nodes that need expansion. ");
            if (n.isMerged()) throw new IllegalStateException("LE: Node "+n+" is merged but in the set of nodes that need expansion. ");
            if (n.isPruned()) throw new IllegalStateException("LE: Node "+n+" is pruned but in the set of nodes that need expansion. ");
        }
        for (Node n : m_nodesNonPermanentSatExt.keySet()) {
            if (!n.isActive()) throw new IllegalStateException("LE: Node "+n+" is not active but in the set of nodes that need expansion. ");
            if (n.isBlocked()) throw new IllegalStateException("LE: Node "+n+" is blocked but in the set of nodes that need expansion. ");
            if (n.isMerged()) throw new IllegalStateException("LE: Node "+n+" is merged but in the set of nodes that need expansion. ");
            if (n.isPruned()) throw new IllegalStateException("LE: Node "+n+" is pruned but in the set of nodes that need expansion. ");
        }
        ((AnywhereCoreBlockingOld)m_blockingStrategy).doSanityCheck();
    }
    public void modelFound() {
        if (debuggingMode) {
            System.out.println("Checking all unblocked nodes for unprocessed existentials....");
            Node node=m_tableau.getFirstTableauNode();
            while (node!=null) {
                if (node.isActive() && !node.isBlocked() && node.hasUnprocessedExistentials()) {
                    for (ExistentialConcept ec : node.getUnprocessedExistentials()) {
                        if (ec instanceof AtLeastConcept) {
                            SatType st=isSatisfied((AtLeastConcept)ec, node);
                            if (st==SatType.NOT_SATISFIED) {
                                System.err.println("Node "+node+ " has unprocessed existential " + ec);
                            }
                        }
                    }
                }
                node=node.getNextTableauNode();
            }
        }
        super.modelFound();
    }
}
