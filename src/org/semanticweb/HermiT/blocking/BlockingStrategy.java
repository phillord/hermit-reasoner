package org.semanticweb.HermiT.blocking;

import org.semanticweb.HermiT.tableau.*;

public interface BlockingStrategy {
    void initialize(Tableau tableau);
    void clear();
    void computeBlocking();
    void nodeWillChange(Node node);
    void nodeWillBeDestroyed(Node node);
    void modelFound();
}
