package org.semanticweb.HermiT.existentials;

import org.semanticweb.HermiT.tableau.*;

public interface ExistentialsExpansionStrategy {
    void intialize(Tableau tableau);
    void clear();
    boolean expandExistentials();
    void nodeWillChange(Node node);
    void nodeWillBeDestroyed(Node node);
    void branchingPointPushed();
    void backtrack();
    void modelFound();
    boolean isDeterministic();
}
