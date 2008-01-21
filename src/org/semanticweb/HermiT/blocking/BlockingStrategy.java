package org.semanticweb.HermiT.blocking;

import org.semanticweb.HermiT.tableau.*;

public interface BlockingStrategy {
    void setTableau(Tableau tableau);
    void clear();
    void computeBlocking();
    void nodeWillChange(Node node);
    void modelFound();
}
