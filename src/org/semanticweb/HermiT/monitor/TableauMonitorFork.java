// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.monitor;

import java.io.Serializable;

import org.semanticweb.HermiT.model.AtMostGuard;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.tableau.BranchingPoint;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.GroundDisjunction;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

public class TableauMonitorFork implements TableauMonitor,Serializable  {
    private static final long serialVersionUID=8321902665477431455L;

    protected final TableauMonitor m_first;
    protected final TableauMonitor m_second;
    
    public TableauMonitorFork(TableauMonitor first,TableauMonitor second) {
        m_first=first;
        m_second=second;
    }
    public void setTableau(Tableau tableau) {
        m_first.setTableau(tableau);
        m_second.setTableau(tableau);
    }
    public void isSatisfiableStarted(AtomicConcept atomicConcept) {
        m_first.isSatisfiableStarted(atomicConcept);
        m_second.isSatisfiableStarted(atomicConcept);
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        m_first.isSatisfiableFinished(atomicConcept,result);
        m_second.isSatisfiableFinished(atomicConcept,result);
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
        m_first.isSubsumedByStarted(subconcept,superconcept);
        m_second.isSubsumedByStarted(subconcept,superconcept);
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
        m_first.isSubsumedByFinished(subconcept,superconcept,result);
        m_second.isSubsumedByFinished(subconcept,superconcept,result);
    }
    public void isABoxSatisfiableStarted() {
        m_first.isABoxSatisfiableStarted();
        m_second.isABoxSatisfiableStarted();
    }
    public void isABoxSatisfiableFinished(boolean result) {
        m_first.isABoxSatisfiableFinished(result);
        m_second.isABoxSatisfiableFinished(result);
    }
    public void isInstanceOfStarted(AtomicConcept concept,Individual individual) {
        m_first.isInstanceOfStarted(concept,individual);
        m_second.isInstanceOfStarted(concept,individual);
    }
    public void isInstanceOfFinished(AtomicConcept concept,Individual individual,boolean result) {
        m_first.isInstanceOfFinished(concept,individual,result);
        m_second.isInstanceOfFinished(concept,individual,result);
    }
    public void tableauCleared() {
        m_first.tableauCleared();
        m_second.tableauCleared();
    }
    public void saturateStarted() {
        m_first.saturateStarted();
        m_second.saturateStarted();
    }
    public void saturateFinished() {
        m_first.saturateFinished();
        m_second.saturateFinished();
    }
    public void iterationStarted() {
        m_first.iterationStarted();
        m_second.iterationStarted();
    }
    public void iterationFinished() {
        m_first.iterationFinished();
        m_second.iterationFinished();
    }
    public void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
        m_first.dlClauseMatchedStarted(dlClauseEvaluator,dlClauseIndex);
        m_second.dlClauseMatchedStarted(dlClauseEvaluator,dlClauseIndex);
    }
    public void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
        m_first.dlClauseMatchedFinished(dlClauseEvaluator,dlClauseIndex);
        m_second.dlClauseMatchedFinished(dlClauseEvaluator,dlClauseIndex);
    }
    public void addFactStarted(Object[] tuple) {
        m_first.addFactStarted(tuple);
        m_second.addFactStarted(tuple);
    }
    public void addFactFinished(Object[] tuple,boolean factAdded) {
        m_first.addFactFinished(tuple,factAdded);
        m_second.addFactFinished(tuple,factAdded);
    }
    public void mergeStarted(Node mergeFrom,Node mergeInto) {
        m_first.mergeStarted(mergeFrom,mergeInto);
        m_second.mergeStarted(mergeFrom,mergeInto);
    }
    public void nodePruned(Node node) {
        m_first.nodePruned(node);
        m_second.nodePruned(node);
    }
    public void mergeFactStarted(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
        m_first.mergeFactStarted(mergeFrom,mergeInto,sourceTuple,targetTuple);
        m_second.mergeFactStarted(mergeFrom,mergeInto,sourceTuple,targetTuple);
    }
    public void mergeFactFinished(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
        m_first.mergeFactFinished(mergeFrom,mergeInto,sourceTuple,targetTuple);
        m_second.mergeFactFinished(mergeFrom,mergeInto,sourceTuple,targetTuple);
    }
    public void mergeFinished(Node mergeFrom,Node mergeInto) {
        m_first.mergeFinished(mergeFrom,mergeInto);
        m_second.mergeFinished(mergeFrom,mergeInto);
    }
    public void mergeGraphsStarted(Object[] graph1,Object[] graph2,int position) {
        m_first.mergeGraphsStarted(graph1,graph2,position);
        m_second.mergeGraphsStarted(graph1,graph2,position);
    }
    public void mergeGraphsFinished(Object[] graph1,Object[] graph2,int position) {
        m_first.mergeGraphsFinished(graph1,graph2,position);
        m_second.mergeGraphsFinished(graph1,graph2,position);
    }
    public void clashDetected(Object[]... causes) {
        m_first.clashDetected(causes);
        m_second.clashDetected(causes);
    }
    public void backtrackToStarted(BranchingPoint newCurrentBrancingPoint) {
        m_first.backtrackToStarted(newCurrentBrancingPoint);
        m_second.backtrackToStarted(newCurrentBrancingPoint);
    }
    public void tupleRemoved(Object[] tuple) {
        m_first.tupleRemoved(tuple);
        m_second.tupleRemoved(tuple);
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_first.backtrackToFinished(newCurrentBrancingPoint);
        m_second.backtrackToFinished(newCurrentBrancingPoint);
    }
    public void groundDisjunctionDerived(GroundDisjunction groundDisjunction) {
        m_first.groundDisjunctionDerived(groundDisjunction);
        m_second.groundDisjunctionDerived(groundDisjunction);
    }
    public void processGroundDisjunctionStarted(GroundDisjunction groundDisjunction) {
        m_first.processGroundDisjunctionStarted(groundDisjunction);
        m_second.processGroundDisjunctionStarted(groundDisjunction);
    }
    public void groundDisjunctionSatisfied(GroundDisjunction groundDisjunction) {
        m_first.groundDisjunctionSatisfied(groundDisjunction);
        m_second.groundDisjunctionSatisfied(groundDisjunction);
    }
    public void processGroundDisjunctionFinished(GroundDisjunction groundDisjunction) {
        m_first.processGroundDisjunctionFinished(groundDisjunction);
        m_second.processGroundDisjunctionFinished(groundDisjunction);
    }
    public void disjunctProcessingStarted(GroundDisjunction groundDisjunction,int disjunct) {
        m_first.disjunctProcessingStarted(groundDisjunction,disjunct);
        m_second.disjunctProcessingStarted(groundDisjunction,disjunct);
    }
    public void disjunctProcessingFinished(GroundDisjunction groundDisjunction,int disjunct) {
        m_first.disjunctProcessingFinished(groundDisjunction,disjunct);
        m_second.disjunctProcessingFinished(groundDisjunction,disjunct);
    }
    public void pushBranchingPointStarted(BranchingPoint branchingPoint) {
        m_first.pushBranchingPointStarted(branchingPoint);
        m_second.pushBranchingPointStarted(branchingPoint);
    }
    public void pushBranchingPointFinished(BranchingPoint branchingPoint) {
        m_first.pushBranchingPointFinished(branchingPoint);
        m_second.pushBranchingPointFinished(branchingPoint);
    }
    public void startNextBranchingPointStarted(BranchingPoint branchingPoint) {
        m_first.startNextBranchingPointStarted(branchingPoint);
        m_second.startNextBranchingPointStarted(branchingPoint);
    }
    public void startNextBranchingPointFinished(BranchingPoint branchingPoint) {
        m_first.startNextBranchingPointFinished(branchingPoint);
        m_second.startNextBranchingPointFinished(branchingPoint);
    }
    public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
        m_first.existentialExpansionStarted(existentialConcept,forNode);
        m_second.existentialExpansionStarted(existentialConcept,forNode);
    }
    public void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode) {
        m_first.existentialExpansionFinished(existentialConcept,forNode);
        m_second.existentialExpansionFinished(existentialConcept,forNode);
    }
    public void existentialSatisfied(ExistentialConcept existentialConcept,Node forNode) {
        m_first.existentialSatisfied(existentialConcept,forNode);
        m_second.existentialSatisfied(existentialConcept,forNode);
    }
    public void nominalIntorductionStarted(Node rootNode,Node treeNode,AtMostGuard atMostRoleGuard) {
        m_first.nominalIntorductionStarted(rootNode,treeNode,atMostRoleGuard);
        m_second.nominalIntorductionStarted(rootNode,treeNode,atMostRoleGuard);
    }
    public void nominalIntorductionFinished(Node rootNode,Node treeNode,AtMostGuard atMostRoleGuard) {
        m_first.nominalIntorductionFinished(rootNode,treeNode,atMostRoleGuard);
        m_second.nominalIntorductionFinished(rootNode,treeNode,atMostRoleGuard);
    }
    public void nodeCreated(Node node) {
        m_first.nodeCreated(node);
        m_second.nodeCreated(node);
    }
    public void nodeDestroyed(Node node) {
        m_first.nodeDestroyed(node);
        m_second.nodeDestroyed(node);
    }
    public void datatypeCheckingStarted() {
        m_first.datatypeCheckingStarted();
        m_second.datatypeCheckingStarted();
    }
    public void datatypeCheckingFinished(boolean result) {
        m_first.datatypeCheckingFinished(result);
        m_second.datatypeCheckingFinished(result);
    }
}
