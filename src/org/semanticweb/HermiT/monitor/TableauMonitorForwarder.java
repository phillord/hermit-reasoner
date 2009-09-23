// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.monitor;

import java.io.Serializable;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.tableau.BranchingPoint;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.DatatypeManager;
import org.semanticweb.HermiT.tableau.GroundDisjunction;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

public class TableauMonitorForwarder implements TableauMonitor,Serializable {
    private static final long serialVersionUID=-371801782567741632L;

    protected final TableauMonitor m_forwardingTargetMonitor;
    protected boolean m_forwardingOn;

    public TableauMonitorForwarder(TableauMonitor forwardingTargetMontior) {
        m_forwardingTargetMonitor=forwardingTargetMontior;
    }
    public boolean isForwardingOn() {
        return m_forwardingOn;
    }
    public void setForwardingOn(boolean forwardingOn) {
        m_forwardingOn=forwardingOn;
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
    public void isInstanceOfStarted(AtomicConcept concept,Individual individual) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.isInstanceOfStarted(concept,individual);
    }
    public void isInstanceOfFinished(AtomicConcept concept,Individual individual,boolean result) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.isInstanceOfFinished(concept,individual,result);
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
            m_forwardingTargetMonitor.dlClauseMatchedFinished(dlClauseEvaluator,dlClauseIndex);
    }
    public void addFactStarted(Object[] tuple,boolean isCore) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.addFactStarted(tuple,isCore);
    }
    public void addFactFinished(Object[] tuple,boolean isCore,boolean factAdded) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.addFactFinished(tuple,isCore,factAdded);
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
    public void clashDetectionStarted(Object[]... tuples) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.clashDetectionStarted(tuples);
    }
    public void clashDetectionFinished(Object[]... tuples) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.clashDetectionFinished(tuples);
    }
    public void clashDetected() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.clashDetected();
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
    public void nominalIntorductionStarted(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nominalIntorductionStarted(rootNode,treeNode,annotatedEquality,argument1,argument2);
    }
    public void nominalIntorductionFinished(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nominalIntorductionFinished(rootNode,treeNode,annotatedEquality,argument1,argument2);
    }
    public void descriptionGraphCheckingStarted(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.descriptionGraphCheckingStarted(graphIndex1,tupleIndex1,position1,graphIndex2,tupleIndex2,position2);
    }
    public void descriptionGraphCheckingFinished(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.descriptionGraphCheckingFinished(graphIndex1,tupleIndex1,position1,graphIndex2,tupleIndex2,position2);
    }
    public void nodeCreated(Node node) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nodeCreated(node);
    }
    public void nodeDestroyed(Node node) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nodeDestroyed(node);
    }
    public void datatypeCheckingStarted() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.datatypeCheckingStarted();
    }
    public void datatypeCheckingFinished(boolean result) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.datatypeCheckingFinished(result);
    }
    public void datatypeConjunctionCheckingStarted(DatatypeManager.DConjunction conjunction) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.datatypeConjunctionCheckingStarted(conjunction);
    }
    public void datatypeConjunctionCheckingFinished(DatatypeManager.DConjunction conjunction,boolean result) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.datatypeConjunctionCheckingFinished(conjunction,result);
    }
    public void blockingValidationStarted() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.blockingValidationStarted();
    }
    public void blockingValidationFinished() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.blockingValidationFinished();
    }
}
