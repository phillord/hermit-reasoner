// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;
import org.semanticweb.HermiT.blocking.*;
import org.semanticweb.HermiT.monitor.TableauMonitor;

public class DepthFirstStrategy extends StrategyBase implements Serializable {

    public DepthFirstStrategy(BlockingStrategy strategy) {
        super(strategy);
        // expander = new StrategyBase.Expander() {
        //     Node expanded;
        //     public boolean expand(AtLeastAbstractRoleConcept c, Node n) {
        //         if (expanded == null) expanded = n;
        //         else if (expanded != n) return true;
        //         existentialExpansionManager.expand(c, n);
        //         existentialExpansionManager.markExistentialProcessed(c, n);
        //         return false;
        //     }
        //     public boolean completeExpansion() {
        //         if (expanded != null) {
        //             expanded = null;
        //             return true;
        //         } else {
        //             return false;
        //         }
        //     }
        // };
    }

    public boolean expandExistentials() {
        return false;
        // m_blockingStrategy.computeBlocking();
        // Node node = null;
        // int numUnprocessed = 0;
        // int numDBlocked = 0;
        // int numIBlocked = 0;
        // int maxDepth = -1;
        // for (Node curNode = m_tableau.getFirstTableauNode();
        //      curNode != null;
        //      curNode = curNode.getNextTableauNode()) {
        //     if (curNode.isIndirectlyBlocked()) ++numIBlocked;
        //     if (curNode.isDirectlyBlocked()) ++numDBlocked;
        //     if (curNode.getTreeDepth() > maxDepth) maxDepth = curNode.getTreeDepth();
        //     if (curNode.isActive() &&
        //         !curNode.isBlocked() &&
        //         curNode.hasUnprocessedExistentials()) {
        //         numUnprocessed += curNode.getUnprocessedExistentials().size();
        //         if (node == null ||
        //             (node.getTreeDepth() <= curNode.getTreeDepth())) {
        //             node = curNode;
        //         }
        //     }
        // }
        // TableauMonitor monitor = m_tableau.getTableauMonitor();
        // if (monitor != null) {
        //     monitor.setValue("unexpanded", String.valueOf(numUnprocessed));
        //     monitor.setValue("max-unexpanded-depth", node == null ? "-1" : String.valueOf(node.getTreeDepth()));
        //     monitor.setValue("dblocked", String.valueOf(numDBlocked));
        //     monitor.setValue("iblocked", String.valueOf(numIBlocked));
        //     monitor.setValue("max-depth", String.valueOf(maxDepth));
        // }
        // boolean didSomething = false;
        // if (node != null) {
        //     while (node.hasUnprocessedExistentials() && !didSomething) {
        //         ExistentialConcept existentialConcept
        //             = node.getSomeUnprocessedExistential();
        //         if (existentialConcept
        //             instanceof AtLeastAbstractRoleConcept) {
        //             AtLeastAbstractRoleConcept atLeastAbstractConcept
        //                 = (AtLeastAbstractRoleConcept) existentialConcept;
        //             switch (m_existentialExpansionManager.isSatisfied
        //                         (atLeastAbstractConcept, node)) {
        //             case NOT_SATISFIED: {
        //                 m_existentialExpansionManager.expand(
        //                     atLeastAbstractConcept, node
        //                 );
        //                 didSomething = true;
        //                 m_existentialExpansionManager
        //                     .markExistentialProcessed
        //                         (existentialConcept, node);
        //             } break;
        //             case PERMANENTLY_SATISFIED: {
        //                 m_existentialExpansionManager
        //                     .markExistentialProcessed
        //                         (existentialConcept, node);
        //                 monitor.existentialSatisfied(
        //                     atLeastAbstractConcept, node
        //                 );
        //             } break;
        //             case CURRENTLY_SATISFIED: {
        //                 // do nothing
        //                 monitor.existentialSatisfied(
        //                     atLeastAbstractConcept, node
        //                 );
        //             } break;
        //             }
        //         } else if (existentialConcept
        //                     instanceof ExistsDescriptionGraph) {
        //             ExistsDescriptionGraph existsDescriptionGraph
        //                 = (ExistsDescriptionGraph) existentialConcept;
        //             if (!m_descriptionGraphManager.isSatisfied
        //                     (existsDescriptionGraph, node)) {
        //                 m_descriptionGraphManager.expand(
        //                     existsDescriptionGraph, node
        //                 );
        //                 didSomething = true;
        //             } else if (monitor != null) {
        //                 monitor.existentialSatisfied(
        //                     existsDescriptionGraph,node
        //                 );
        //             }
        //         } else {
        //             throw new IllegalStateException("Unsupported type of existential concept in CreationOrderStrategy.");
        //         }
        //     } // end while
        // } // end if (node != null)
        // return didSomething;
    }
}
