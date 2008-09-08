// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;
import org.semanticweb.HermiT.blocking.*;
import org.semanticweb.HermiT.monitor.TableauMonitor;

import java.util.Collection;
import java.util.ArrayList;

public abstract class StrategyBase
    implements ExistentialsExpansionStrategy {

    protected final BlockingStrategy blockingStrategy;
    protected Tableau tableau;
    protected ExtensionManager extensionManager;
    protected ExistentialExpansionManager existentialExpansionManager;
    protected DescriptionGraphManager descriptionGraphManager;
    
    /** Cache for expandExistentials to prevent concurrent modification */
    private Collection<ExistentialConcept> curExistentials;
    
    public StrategyBase(BlockingStrategy strategy) {
        blockingStrategy = strategy;
        curExistentials = new ArrayList<ExistentialConcept>();
    }
    
    public void initialize(Tableau tableau) {
        this.tableau = tableau;
        extensionManager = tableau.getExtensionManager();
        existentialExpansionManager = tableau.getExistentialExpansionManager();
        descriptionGraphManager = tableau.getDescriptionGraphManager();
        blockingStrategy.initialize(tableau);
    }
    
    public void clear() {
        blockingStrategy.clear();
    }
    
    protected interface Expander {
        boolean expand(AtLeastAbstractRoleConcept c, Node n);
        public boolean completeExpansion();
    }
    
    protected boolean expandExistentials(Expander e) {
        TableauMonitor monitor = tableau.getTableauMonitor();
        blockingStrategy.computeBlocking();
        boolean done = false;
        for (Node node = tableau.getFirstTableauNode();
                node != null && !done;
                node = node.getNextTableauNode()) {
            if (node.isActive() && !node.isBlocked()) {
                // The node's set of unprocessed existentials may be
                // changed during operation, so make a local copy to
                // loop over:
                curExistentials.clear();
                curExistentials.addAll(node.getUnprocessedExistentials());
                for (ExistentialConcept existentialConcept
                        : curExistentials) {
                    if (done) break;
                    if (existentialConcept instanceof
                            AtLeastAbstractRoleConcept) {
                        AtLeastAbstractRoleConcept atLeastAbstractRoleConcept
                            = (AtLeastAbstractRoleConcept) existentialConcept;
                        switch (existentialExpansionManager.isSatisfied(
                                    atLeastAbstractRoleConcept, node)) {
                        case NOT_SATISFIED: {
                            done = e.expand(
                                atLeastAbstractRoleConcept, node
                            );
                        } break;
                        case PERMANENTLY_SATISFIED: {
                            existentialExpansionManager
                                .markExistentialProcessed
                                    (existentialConcept, node);
                            if (monitor != null) {
                                monitor.existentialSatisfied(
                                    atLeastAbstractRoleConcept, node
                                );
                            }
                        } break;
                        case CURRENTLY_SATISFIED: {
                            // do nothing
                            if (monitor != null) {
                                monitor.existentialSatisfied(
                                    atLeastAbstractRoleConcept, node
                                );
                            }
                        } break;
                        }
                    } else if (existentialConcept instanceof
                                ExistsDescriptionGraph) {
                        ExistsDescriptionGraph existsDescriptionGraph
                            = (ExistsDescriptionGraph) existentialConcept;
                        if (!descriptionGraphManager.isSatisfied(
                                existsDescriptionGraph, node)) {
                            descriptionGraphManager.expand(
                                existsDescriptionGraph, node);
                        } else if (monitor != null) {
                            monitor.existentialSatisfied(
                                existsDescriptionGraph, node);
                        }
                        existentialExpansionManager.markExistentialProcessed
                            (existentialConcept, node);
                    } else {
                        throw new IllegalStateException("Unsupported type of existential concept in CreationOrderStrategy.");
                    }
                } // end for existentialConcept
            } // end if node.isActive...
        } // end for node
        return e.completeExpansion();
    }
    
    public void assertionAdded(Concept concept, Node node) {
        blockingStrategy.assertionAdded(concept, node);
    }
    
    public void assertionRemoved(Concept concept, Node node) {
        blockingStrategy.assertionRemoved(concept, node);
    }
    
    public void assertionAdded(AtomicAbstractRole atomicAbstractRole,
                                Node nodeFrom, Node nodeTo) {
        blockingStrategy.assertionAdded(atomicAbstractRole, nodeFrom, nodeTo);
    }
    
    public void assertionRemoved(AtomicAbstractRole atomicAbstractRole,
                                    Node nodeFrom, Node nodeTo) {
        blockingStrategy.assertionRemoved(atomicAbstractRole,
                                            nodeFrom, nodeTo);
    }
    
    public void nodeStatusChanged(Node node) {
        blockingStrategy.nodeStatusChanged(node);
    }
    
    public void nodeDestroyed(Node node) {
        blockingStrategy.nodeDestroyed(node);
    }
    
    public void branchingPointPushed() {
    }
    
    public void backtrack() {
    }
    
    public void modelFound() {
        blockingStrategy.modelFound();
    }
    
    public boolean isDeterministic() {
        return true;
    }
}
