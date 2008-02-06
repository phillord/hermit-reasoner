package org.semanticweb.HermiT.monitor;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class TableauMonitorForwarder implements TableauMonitor,Serializable {
    private static final long serialVersionUID=-371801782567741632L;

    protected final TableauMonitor m_forwardingTargetMonitor;
    protected boolean m_forwardingOn;

    public TableauMonitorForwarder(TableauMonitor forwardingTargetMontior) {
        m_forwardingTargetMonitor=forwardingTargetMontior;
    }
    public void setTableau(Tableau tableau) {
        m_forwardingTargetMonitor.setTableau(tableau);
    }
    public void isSatisfiableStarted(AtomicConcept atomicConcept) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.isSatisfiableStarted(atomicConcept);
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.isSatisfiableFinished(atomicConcept,result);
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.isSubsumedByStarted(subconcept,superconcept);
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.isSubsumedByFinished(subconcept,superconcept,result);
    }
    public void isABoxSatisfiableStarted() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.isABoxSatisfiableStarted();
    }
    public void isABoxSatisfiableFinished(boolean result) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.isABoxSatisfiableFinished(result);
    }
    public void tableauCleared() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.tableauCleared();
    }
    public void saturateStarted() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.saturateStarted();
    }
    public void saturateFinished() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.saturateFinished();
    }
    public void iterationStarted() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.iterationStarted();
    }
    public void iterationFinished() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.iterationFinished();
    }
    public void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.dlClauseMatchedStarted(dlClauseEvaluator,dlClauseIndex);
    }
    public void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.dlClauseMatchedStarted(dlClauseEvaluator,dlClauseIndex);
    }
    public void addFactStarted(Object[] tuple) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.addFactStarted(tuple);
    }
    public void addFactFinished(Object[] tuple,boolean factAdded) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.addFactFinished(tuple,factAdded);
    }
    public void mergeStarted(Node mergeFrom,Node mergrInto) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.mergeStarted(mergeFrom,mergrInto);
    }
    public void nodePruned(Node node) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nodePruned(node);
    }
    public void mergeFactStarted(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.mergeFactStarted(mergeFrom,mergeInto,sourceTuple,targetTuple);
    }
    public void mergeFactFinished(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.mergeFactFinished(mergeFrom,mergeInto,sourceTuple,targetTuple);
    }
    public void mergeFinished(Node mergeFrom,Node mergeInto) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.mergeFinished(mergeFrom,mergeInto);
    }
    public void mergeGraphsStarted(Object[] graph1,Object[] graph2,int position) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.mergeGraphsStarted(graph1,graph2,position);
    }
    public void mergeGraphsFinished(Object[] graph1,Object[] graph2,int position) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.mergeGraphsFinished(graph1,graph2,position);
    }
    public void clashDetected(Object[]... causes) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.clashDetected(causes);
    }
    public void backtrackToStarted(BranchingPoint newCurrentBrancingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.backtrackToStarted(newCurrentBrancingPoint);
    }
    public void tupleRemoved(Object[] tuple) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.tupleRemoved(tuple);
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.backtrackToFinished(newCurrentBrancingPoint);
    }
    public void groundDisjunctionDerived(GroundDisjunction groundDisjunction) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.groundDisjunctionDerived(groundDisjunction);
    }
    public void processGroundDisjunctionStarted(GroundDisjunction groundDisjunction) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.processGroundDisjunctionStarted(groundDisjunction);
    }
    public void groundDisjunctionSatisfied(GroundDisjunction groundDisjunction) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.groundDisjunctionSatisfied(groundDisjunction);
    }
    public void processGroundDisjunctionFinished(GroundDisjunction groundDisjunction) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.processGroundDisjunctionFinished(groundDisjunction);
    }
    public void disjunctProcessingStarted(GroundDisjunction groundDisjunction,int disjunct) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.disjunctProcessingStarted(groundDisjunction,disjunct);
    }
    public void disjunctProcessingFinished(GroundDisjunction groundDisjunction,int disjunct) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.disjunctProcessingFinished(groundDisjunction,disjunct);
    }
    public void pushBranchingPointStarted(BranchingPoint branchingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.pushBranchingPointStarted(branchingPoint);
    }
    public void pushBranchingPointFinished(BranchingPoint branchingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.pushBranchingPointFinished(branchingPoint);
    }
    public void startNextBranchingPointStarted(BranchingPoint branchingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.startNextBranchingPointStarted(branchingPoint);
    }
    public void startNextBranchingPointFinished(BranchingPoint branchingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.startNextBranchingPointFinished(branchingPoint);
    }
    public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.existentialExpansionStarted(existentialConcept,forNode);
    }
    public void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.existentialExpansionFinished(existentialConcept,forNode);
    }
    public void existentialSatisfied(ExistentialConcept existentialConcept,Node forNode) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.existentialSatisfied(existentialConcept,forNode);
    }
    public void nodeCreated(Node node) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nodeCreated(node);
    }
    public void nodeDestroyed(Node node) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nodeDestroyed(node);
    }
}
