// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.monitor;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public interface TableauMonitor {
    void setTableau(Tableau tableau);
    void isSatisfiableStarted(AtomicConcept atomicConcept);
    void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result);
    void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept);
    void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result);
    void isABoxSatisfiableStarted();
    void isABoxSatisfiableFinished(boolean result);
    void tableauCleared();
    void saturateStarted();
    void saturateFinished();
    void iterationStarted();
    void iterationFinished();
    void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex);
    void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex);
    void addFactStarted(Object[] tuple);
    void addFactFinished(Object[] tuple,boolean factAdded);
    void mergeStarted(Node mergeFrom,Node mergeInto);
    void nodePruned(Node node);
    void mergeFactStarted(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple);
    void mergeFactFinished(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple);
    void mergeFinished(Node mergeFrom,Node mergeInto);
    void mergeGraphsStarted(Object[] graph1,Object[] graph2,int position);
    void mergeGraphsFinished(Object[] graph1,Object[] graph2,int position);
    void clashDetected(Object[]... causes);
    void backtrackToStarted(BranchingPoint newCurrentBrancingPoint);
    void tupleRemoved(Object[] tuple);
    void backtrackToFinished(BranchingPoint newCurrentBrancingPoint);
    void groundDisjunctionDerived(GroundDisjunction groundDisjunction);
    void processGroundDisjunctionStarted(GroundDisjunction groundDisjunction);
    void groundDisjunctionSatisfied(GroundDisjunction groundDisjunction);
    void processGroundDisjunctionFinished(GroundDisjunction groundDisjunction);
    void disjunctProcessingStarted(GroundDisjunction groundDisjunction,int disjunct);
    void disjunctProcessingFinished(GroundDisjunction groundDisjunction,int disjunct);
    void pushBranchingPointStarted(BranchingPoint branchingPoint);
    void pushBranchingPointFinished(BranchingPoint branchingPoint);
    void startNextBranchingPointStarted(BranchingPoint branchingPoint);
    void startNextBranchingPointFinished(BranchingPoint branchingPoint);
    void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode);
    void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode);
    void existentialSatisfied(ExistentialConcept existentialConcept,Node forNode);
    void nominalIntorductionStarted(Node rootNode,Node treeNode,AtMostAbstractRoleGuard atMostAbstractRoleGuard);
    void nominalIntorductionFinished(Node rootNode,Node treeNode,AtMostAbstractRoleGuard atMostAbstractRoleGuard);
    void nodeCreated(Node node);
    void nodeDestroyed(Node node);
    void setValue(String key, String value);
}
