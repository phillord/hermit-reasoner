// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

public interface DirectBlockingChecker {
    void initialize(Tableau tableau);
    void clear();
    boolean isBlockedBy(Node blocker,Node blocked);
    int blockingHashCode(Node node);
    boolean canBeBlocker(Node node);
    boolean canBeBlocked(Node node);
    boolean hasBlockingInfoChanged(Node node);
    void clearBlockingInfoChanged(Node node);
    void nodeInitialized(Node node);
    void nodeDestroyed(Node node);
    Node assertionAdded(Concept concept,Node node);
    Node assertionRemoved(Concept concept,Node node);
    Node assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo);
    Node assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo);
    BlockingSignature getBlockingSignatureFor(Node node);
}
