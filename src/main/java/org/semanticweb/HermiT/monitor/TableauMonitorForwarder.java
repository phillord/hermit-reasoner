/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.monitor;

import java.io.Serializable;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.tableau.BranchingPoint;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.DatatypeManager;
import org.semanticweb.HermiT.tableau.GroundDisjunction;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;
/**TableauMonitorForwarder.*/
public class TableauMonitorForwarder implements TableauMonitor,Serializable {
    private static final long serialVersionUID=-371801782567741632L;

    protected final TableauMonitor m_forwardingTargetMonitor;
    protected boolean m_forwardingOn;

    /**
     * @param forwardingTargetMontior forwardingTargetMontior
     */
    public TableauMonitorForwarder(TableauMonitor forwardingTargetMontior) {
        m_forwardingTargetMonitor=forwardingTargetMontior;
    }
    /**
     * @return true if forwarding on
     */
    public boolean isForwardingOn() {
        return m_forwardingOn;
    }
    /**
     * @param forwardingOn forwardingOn
     */
    public void setForwardingOn(boolean forwardingOn) {
        m_forwardingOn=forwardingOn;
    }
    @Override
    public void setTableau(Tableau tableau) {
        m_forwardingTargetMonitor.setTableau(tableau);
    }
    @Override
    public void isSatisfiableStarted(ReasoningTaskDescription reasoningTaskDescription) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.isSatisfiableStarted(reasoningTaskDescription);
    }
    @Override
    public void isSatisfiableFinished(ReasoningTaskDescription reasoningTaskDescription,boolean result) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.isSatisfiableFinished(reasoningTaskDescription,result);
    }
    @Override
    public void tableauCleared() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.tableauCleared();
    }
    @Override
    public void saturateStarted() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.saturateStarted();
    }
    @Override
    public void saturateFinished(boolean modelFound) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.saturateFinished(modelFound);
    }
    @Override
    public void iterationStarted() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.iterationStarted();
    }
    @Override
    public void iterationFinished() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.iterationFinished();
    }
    @Override
    public void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.dlClauseMatchedStarted(dlClauseEvaluator,dlClauseIndex);
    }
    @Override
    public void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.dlClauseMatchedFinished(dlClauseEvaluator,dlClauseIndex);
    }
    @Override
    public void addFactStarted(Object[] tuple,boolean isCore) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.addFactStarted(tuple,isCore);
    }
    @Override
    public void addFactFinished(Object[] tuple,boolean isCore,boolean factAdded) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.addFactFinished(tuple,isCore,factAdded);
    }
    @Override
    public void mergeStarted(Node mergeFrom,Node mergrInto) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.mergeStarted(mergeFrom,mergrInto);
    }
    @Override
    public void nodePruned(Node node) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nodePruned(node);
    }
    @Override
    public void mergeFactStarted(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.mergeFactStarted(mergeFrom,mergeInto,sourceTuple,targetTuple);
    }
    @Override
    public void mergeFactFinished(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.mergeFactFinished(mergeFrom,mergeInto,sourceTuple,targetTuple);
    }
    @Override
    public void mergeFinished(Node mergeFrom,Node mergeInto) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.mergeFinished(mergeFrom,mergeInto);
    }
    @Override
    public void clashDetectionStarted(Object[]... tuples) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.clashDetectionStarted(tuples);
    }
    @Override
    public void clashDetectionFinished(Object[]... tuples) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.clashDetectionFinished(tuples);
    }
    @Override
    public void clashDetected() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.clashDetected();
    }
    @Override
    public void backtrackToStarted(BranchingPoint newCurrentBrancingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.backtrackToStarted(newCurrentBrancingPoint);
    }
    @Override
    public void tupleRemoved(Object[] tuple) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.tupleRemoved(tuple);
    }
    @Override
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.backtrackToFinished(newCurrentBrancingPoint);
    }
    @Override
    public void groundDisjunctionDerived(GroundDisjunction groundDisjunction) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.groundDisjunctionDerived(groundDisjunction);
    }
    @Override
    public void processGroundDisjunctionStarted(GroundDisjunction groundDisjunction) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.processGroundDisjunctionStarted(groundDisjunction);
    }
    @Override
    public void groundDisjunctionSatisfied(GroundDisjunction groundDisjunction) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.groundDisjunctionSatisfied(groundDisjunction);
    }
    @Override
    public void processGroundDisjunctionFinished(GroundDisjunction groundDisjunction) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.processGroundDisjunctionFinished(groundDisjunction);
    }
    @Override
    public void disjunctProcessingStarted(GroundDisjunction groundDisjunction,int disjunct) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.disjunctProcessingStarted(groundDisjunction,disjunct);
    }
    @Override
    public void disjunctProcessingFinished(GroundDisjunction groundDisjunction,int disjunct) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.disjunctProcessingFinished(groundDisjunction,disjunct);
    }
    @Override
    public void pushBranchingPointStarted(BranchingPoint branchingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.pushBranchingPointStarted(branchingPoint);
    }
    @Override
    public void pushBranchingPointFinished(BranchingPoint branchingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.pushBranchingPointFinished(branchingPoint);
    }
    @Override
    public void startNextBranchingPointStarted(BranchingPoint branchingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.startNextBranchingPointStarted(branchingPoint);
    }
    @Override
    public void startNextBranchingPointFinished(BranchingPoint branchingPoint) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.startNextBranchingPointFinished(branchingPoint);
    }
    @Override
    public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.existentialExpansionStarted(existentialConcept,forNode);
    }
    @Override
    public void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.existentialExpansionFinished(existentialConcept,forNode);
    }
    @Override
    public void existentialSatisfied(ExistentialConcept existentialConcept,Node forNode) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.existentialSatisfied(existentialConcept,forNode);
    }
    @Override
    public void nominalIntorductionStarted(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nominalIntorductionStarted(rootNode,treeNode,annotatedEquality,argument1,argument2);
    }
    @Override
    public void nominalIntorductionFinished(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nominalIntorductionFinished(rootNode,treeNode,annotatedEquality,argument1,argument2);
    }
    @Override
    public void descriptionGraphCheckingStarted(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.descriptionGraphCheckingStarted(graphIndex1,tupleIndex1,position1,graphIndex2,tupleIndex2,position2);
    }
    @Override
    public void descriptionGraphCheckingFinished(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.descriptionGraphCheckingFinished(graphIndex1,tupleIndex1,position1,graphIndex2,tupleIndex2,position2);
    }
    @Override
    public void nodeCreated(Node node) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nodeCreated(node);
    }
    @Override
    public void nodeDestroyed(Node node) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.nodeDestroyed(node);
    }
    @Override
    public void unknownDatatypeRestrictionDetectionStarted(DataRange dataRange1,Node node1,DataRange dataRange2,Node node2) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.unknownDatatypeRestrictionDetectionStarted(dataRange1,node1,dataRange2,node2);
    }
    @Override
    public void unknownDatatypeRestrictionDetectionFinished(DataRange dataRange1,Node node1, DataRange dataRange2,Node node2) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.unknownDatatypeRestrictionDetectionFinished(dataRange1,node1,dataRange2,node2);
    }
    @Override
    public void datatypeCheckingStarted() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.datatypeCheckingStarted();
    }
    @Override
    public void datatypeCheckingFinished(boolean result) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.datatypeCheckingFinished(result);
    }
    @Override
    public void datatypeConjunctionCheckingStarted(DatatypeManager.DConjunction conjunction) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.datatypeConjunctionCheckingStarted(conjunction);
    }
    @Override
    public void datatypeConjunctionCheckingFinished(DatatypeManager.DConjunction conjunction,boolean result) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.datatypeConjunctionCheckingFinished(conjunction,result);
    }
    @Override
    public void blockingValidationStarted() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.blockingValidationStarted();
    }
    @Override
    public void blockingValidationFinished(int noInvalidlyBlocked) {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.blockingValidationFinished(noInvalidlyBlocked);
    }
    @Override
    public void possibleInstanceIsInstance() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.possibleInstanceIsInstance();
    }
    @Override
    public void possibleInstanceIsNotInstance() {
        if (m_forwardingOn)
            m_forwardingTargetMonitor.possibleInstanceIsNotInstance();
    }
}
