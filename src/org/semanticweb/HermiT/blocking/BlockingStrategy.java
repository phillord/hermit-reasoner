// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public interface BlockingStrategy {
    void initialize(Tableau tableau);
    void clear();
    void computeBlocking();
    void assertionAdded(Concept concept,Node node);
    void assertionRemoved(Concept concept,Node node);
    void assertionAdded(AtomicAbstractRole atomicAbstractRole,Node nodeFrom,Node nodeTo);
    void assertionRemoved(AtomicAbstractRole atomicAbstractRole,Node nodeFrom,Node nodeTo);
    void nodeStatusChanged(Node node);
    void nodeDestroyed(Node node);
    void modelFound();
}
