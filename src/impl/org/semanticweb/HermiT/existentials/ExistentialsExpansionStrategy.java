// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;


/**
 * Strategy objects are responsible for selecting which existentials should 
 * be expanded first, as well as how the new nodes are introduced. The latter
 * is usually delegated to tableau.ExistentialExpansionManager, but strategies
 * are free to provide their own node-introduction implementations (but be
 * careful---it's tough to get right!)
 */
public interface ExistentialsExpansionStrategy {
    void initialize(Tableau tableau);
    void clear();

    /**
     * The main workhorse of the interface: pick some existential in the
     * current tableau and expand it.
     * 
     * @return false if the tableau contains no new existentials to expand;
     * otherwise true
     */
    boolean expandExistentials();

    void assertionAdded(Concept concept,Node node);
    void assertionRemoved(Concept concept,Node node);
    void assertionAdded(AtomicAbstractRole atomicAbstractRole,Node nodeFrom,Node nodeTo);
    void assertionRemoved(AtomicAbstractRole atomicAbstractRole,Node nodeFrom,Node nodeTo);
    void nodeStatusChanged(Node node);
    void nodeDestroyed(Node node);
    void branchingPointPushed();
    void backtrack();
    void modelFound();
    boolean isDeterministic();
}
