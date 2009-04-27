// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

/**
 * Strategy objects are responsible for selecting which existentials should be
 * expanded first, as well as how the new nodes are introduced. The latter is
 * usually delegated to tableau.ExistentialExpansionManager, but strategies
 * are free to provide their own node-introduction implementations
 * (but be careful---it's tough to get right!)
 */
public interface ExistentialExpansionStrategy {
    void initialize(Tableau tableau);
    void clear();
    boolean expandExistentials();
    void assertionAdded(Concept concept,Node node,boolean isCore);
    void assertionCoreSet(Concept concept,Node node);
    void assertionRemoved(Concept concept,Node node,boolean isCore);
    void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo);
    void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    void nodeStatusChanged(Node node);
    void nodeInitialized(Node node);
    void nodeDestroyed(Node node);
    void branchingPointPushed();
    void backtrack();
    void modelFound();
    boolean isDeterministic();
}
