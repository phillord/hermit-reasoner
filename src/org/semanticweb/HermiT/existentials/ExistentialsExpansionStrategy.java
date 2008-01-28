package org.semanticweb.HermiT.existentials;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public interface ExistentialsExpansionStrategy {
    void intialize(Tableau tableau);
    void clear();
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
