package org.semanticweb.HermiT.monitor;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class TableauMonitorAdapter implements TableauMonitor,Serializable  {
    private static final long serialVersionUID=6336033031431260208L;

    protected Tableau m_tableau;
    
    public void setTableau(Tableau tableau) {
        m_tableau=tableau;
    }
    public void isSatisfiableStarted(AtomicConcept atomicConcept) {
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
    }
    public void isABoxSatisfiableStarted() {
    }
    public void isABoxSatisfiableFinished(boolean result) {
    }
    public void tableauCleared() {
    }
    public void saturateStarted() {
    }
    public void saturateFinished() {
    }
    public void iterationStarted() {
    }
    public void iterationFinished() {
    }
    public void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
    }
    public void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
    }
    public void addFactStarted(Object[] tuple) {
    }
    public void addFactFinished(Object[] tuple,boolean factAdded) {
    }
    public void mergeStarted(Node mergeFrom,Node mergeInto) {
    }
    public void nodePruned(Node node) {
    }
    public void mergeFactStarted(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
    }
    public void mergeFactFinished(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
    }
    public void mergeFinished(Node mergeFrom,Node mergeInto) {
    }
    public void mergeGraphsStarted(Object[] graph1,Object[] graph2,int position) {
    }
    public void mergeGraphsFinished(Object[] graph1,Object[] graph2,int position) {
    }
    public void clashDetected(Object[]... causes) {
    }
    public void backtrackToStarted(BranchingPoint newCurrentBrancingPoint) {
    }
    public void tupleRemoved(Object[] tuple) {
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
    }
    public void groundDisjunctionDerived(GroundDisjunction groundDisjunction) {
    }
    public void processGroundDisjunctionStarted(GroundDisjunction groundDisjunction) {
    }
    public void groundDisjunctionSatisfied(GroundDisjunction groundDisjunction) {
    }
    public void processGroundDisjunctionFinished(GroundDisjunction groundDisjunction) {
    }
    public void disjunctProcessingStarted(GroundDisjunction groundDisjunction,int disjunct) {
    }
    public void disjunctProcessingFinished(GroundDisjunction groundDisjunction,int disjunct) {
    }
    public void pushBranchingPointStarted(BranchingPoint branchingPoint) {
    }
    public void pushBranchingPointFinished(BranchingPoint branchingPoint) {
    }
    public void startNextBranchingPointStarted(BranchingPoint branchingPoint) {
    }
    public void startNextBranchingPointFinished(BranchingPoint branchingPoint) {
    }
    public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
    }
    public void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode) {
    }
    public void existentialSatisfied(ExistentialConcept existentialConcept,Node forNode) {
    }
}
