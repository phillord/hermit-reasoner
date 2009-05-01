// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.util.List;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

public interface BlockingStrategy {
    void initialize(Tableau tableau);
    void clear();
    void computeBlocking(boolean finalChance);
    boolean isPermanentAssertion(Concept concept,Node node);
    void assertionAdded(Concept concept,Node node,boolean isCore);
    void assertionCoreSet(Concept concept,Node node);
    void assertionRemoved(Concept concept,Node node,boolean isCore);
    void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo);
    void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    void nodeStatusChanged(Node node);
    void nodeInitialized(Node node);
    void nodeDestroyed(Node node);
    void modelFound();
    boolean isExact();
    void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,Object[] valuesBuffer,boolean[] coreVariables);
}
