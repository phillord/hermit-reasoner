// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
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
    /**
     * @param node
     * @return true if node is a tree node
     */
    boolean canBeBlocked(Node node);
    boolean hasBlockingInfoChanged(Node node);
    void clearBlockingInfoChanged(Node node);
    boolean hasChangedSinceValidation(Node node);
    void setHasChangedSinceValidation(Node node, boolean hasChanged);
    void nodeInitialized(Node node);
    void nodeDestroyed(Node node);
    Node assertionAdded(Concept concept,Node node,boolean isCore);
    Node assertionRemoved(Concept concept,Node node,boolean isCore);
    Node assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    Node assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    BlockingSignature getBlockingSignatureFor(Node node);
    Set<AtomicConcept> getBlockingRelevantConceptsLabel(Node node);
    Set<AtomicConcept> getFullAtomicConceptsLabel(Node node);
    Set<AtomicRole> getFullToParentLabel(Node node);
    Set<AtomicRole> getFullFromParentLabel(Node node);
}
