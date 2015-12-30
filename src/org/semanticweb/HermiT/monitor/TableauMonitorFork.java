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
/**TableauMonitorFork.*/
public class TableauMonitorFork implements TableauMonitor,Serializable  {
    private static final long serialVersionUID=8321902665477431455L;

    protected final TableauMonitor m_first;
    protected final TableauMonitor m_second;

    /**
     * @param first first
     * @param second second
     */
    public TableauMonitorFork(TableauMonitor first,TableauMonitor second) {
        m_first=first;
        m_second=second;
    }
    @Override
    public void setTableau(Tableau tableau) {
        m_first.setTableau(tableau);
        m_second.setTableau(tableau);
    }
    @Override
    public void isSatisfiableStarted(ReasoningTaskDescription reasoningTaskDescription) {
        m_first.isSatisfiableStarted(reasoningTaskDescription);
        m_second.isSatisfiableStarted(reasoningTaskDescription);
    }
    @Override
    public void isSatisfiableFinished(ReasoningTaskDescription reasoningTaskDescription,boolean result) {
        m_first.isSatisfiableFinished(reasoningTaskDescription,result);
        m_second.isSatisfiableFinished(reasoningTaskDescription,result);
    }
    @Override
    public void tableauCleared() {
        m_first.tableauCleared();
        m_second.tableauCleared();
    }
    @Override
    public void saturateStarted() {
        m_first.saturateStarted();
        m_second.saturateStarted();
    }
    @Override
    public void saturateFinished(boolean modelFound) {
        m_first.saturateFinished(modelFound);
        m_second.saturateFinished(modelFound);
    }
    @Override
    public void iterationStarted() {
        m_first.iterationStarted();
        m_second.iterationStarted();
    }
    @Override
    public void iterationFinished() {
        m_first.iterationFinished();
        m_second.iterationFinished();
    }
    @Override
    public void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
        m_first.dlClauseMatchedStarted(dlClauseEvaluator,dlClauseIndex);
        m_second.dlClauseMatchedStarted(dlClauseEvaluator,dlClauseIndex);
    }
    @Override
    public void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
        m_first.dlClauseMatchedFinished(dlClauseEvaluator,dlClauseIndex);
        m_second.dlClauseMatchedFinished(dlClauseEvaluator,dlClauseIndex);
    }
    @Override
    public void addFactStarted(Object[] tuple,boolean isCore) {
        m_first.addFactStarted(tuple,isCore);
        m_second.addFactStarted(tuple,isCore);
    }
    @Override
    public void addFactFinished(Object[] tuple,boolean isCore,boolean factAdded) {
        m_first.addFactFinished(tuple,isCore,factAdded);
        m_second.addFactFinished(tuple,isCore,factAdded);
    }
    @Override
    public void mergeStarted(Node mergeFrom,Node mergeInto) {
        m_first.mergeStarted(mergeFrom,mergeInto);
        m_second.mergeStarted(mergeFrom,mergeInto);
    }
    @Override
    public void nodePruned(Node node) {
        m_first.nodePruned(node);
        m_second.nodePruned(node);
    }
    @Override
    public void mergeFactStarted(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
        m_first.mergeFactStarted(mergeFrom,mergeInto,sourceTuple,targetTuple);
        m_second.mergeFactStarted(mergeFrom,mergeInto,sourceTuple,targetTuple);
    }
    @Override
    public void mergeFactFinished(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
        m_first.mergeFactFinished(mergeFrom,mergeInto,sourceTuple,targetTuple);
        m_second.mergeFactFinished(mergeFrom,mergeInto,sourceTuple,targetTuple);
    }
    @Override
    public void mergeFinished(Node mergeFrom,Node mergeInto) {
        m_first.mergeFinished(mergeFrom,mergeInto);
        m_second.mergeFinished(mergeFrom,mergeInto);
    }
    @Override
    public void clashDetectionStarted(Object[]... tuples) {
        m_first.clashDetectionStarted(tuples);
        m_second.clashDetectionStarted(tuples);
    }
    @Override
    public void clashDetectionFinished(Object[]... tuples) {
        m_first.clashDetectionFinished(tuples);
        m_second.clashDetectionFinished(tuples);
    }
    @Override
    public void clashDetected() {
        m_first.clashDetected();
        m_second.clashDetected();
    }
    @Override
    public void backtrackToStarted(BranchingPoint newCurrentBrancingPoint) {
        m_first.backtrackToStarted(newCurrentBrancingPoint);
        m_second.backtrackToStarted(newCurrentBrancingPoint);
    }
    @Override
    public void tupleRemoved(Object[] tuple) {
        m_first.tupleRemoved(tuple);
        m_second.tupleRemoved(tuple);
    }
    @Override
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_first.backtrackToFinished(newCurrentBrancingPoint);
        m_second.backtrackToFinished(newCurrentBrancingPoint);
    }
    @Override
    public void groundDisjunctionDerived(GroundDisjunction groundDisjunction) {
        m_first.groundDisjunctionDerived(groundDisjunction);
        m_second.groundDisjunctionDerived(groundDisjunction);
    }
    @Override
    public void processGroundDisjunctionStarted(GroundDisjunction groundDisjunction) {
        m_first.processGroundDisjunctionStarted(groundDisjunction);
        m_second.processGroundDisjunctionStarted(groundDisjunction);
    }
    @Override
    public void groundDisjunctionSatisfied(GroundDisjunction groundDisjunction) {
        m_first.groundDisjunctionSatisfied(groundDisjunction);
        m_second.groundDisjunctionSatisfied(groundDisjunction);
    }
    @Override
    public void processGroundDisjunctionFinished(GroundDisjunction groundDisjunction) {
        m_first.processGroundDisjunctionFinished(groundDisjunction);
        m_second.processGroundDisjunctionFinished(groundDisjunction);
    }
    @Override
    public void disjunctProcessingStarted(GroundDisjunction groundDisjunction,int disjunct) {
        m_first.disjunctProcessingStarted(groundDisjunction,disjunct);
        m_second.disjunctProcessingStarted(groundDisjunction,disjunct);
    }
    @Override
    public void disjunctProcessingFinished(GroundDisjunction groundDisjunction,int disjunct) {
        m_first.disjunctProcessingFinished(groundDisjunction,disjunct);
        m_second.disjunctProcessingFinished(groundDisjunction,disjunct);
    }
    @Override
    public void pushBranchingPointStarted(BranchingPoint branchingPoint) {
        m_first.pushBranchingPointStarted(branchingPoint);
        m_second.pushBranchingPointStarted(branchingPoint);
    }
    @Override
    public void pushBranchingPointFinished(BranchingPoint branchingPoint) {
        m_first.pushBranchingPointFinished(branchingPoint);
        m_second.pushBranchingPointFinished(branchingPoint);
    }
    @Override
    public void startNextBranchingPointStarted(BranchingPoint branchingPoint) {
        m_first.startNextBranchingPointStarted(branchingPoint);
        m_second.startNextBranchingPointStarted(branchingPoint);
    }
    @Override
    public void startNextBranchingPointFinished(BranchingPoint branchingPoint) {
        m_first.startNextBranchingPointFinished(branchingPoint);
        m_second.startNextBranchingPointFinished(branchingPoint);
    }
    @Override
    public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
        m_first.existentialExpansionStarted(existentialConcept,forNode);
        m_second.existentialExpansionStarted(existentialConcept,forNode);
    }
    @Override
    public void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode) {
        m_first.existentialExpansionFinished(existentialConcept,forNode);
        m_second.existentialExpansionFinished(existentialConcept,forNode);
    }
    @Override
    public void existentialSatisfied(ExistentialConcept existentialConcept,Node forNode) {
        m_first.existentialSatisfied(existentialConcept,forNode);
        m_second.existentialSatisfied(existentialConcept,forNode);
    }
    @Override
    public void nominalIntorductionStarted(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2) {
        m_first.nominalIntorductionStarted(rootNode,treeNode,annotatedEquality,argument1,argument2);
        m_second.nominalIntorductionStarted(rootNode,treeNode,annotatedEquality,argument1,argument2);
    }
    @Override
    public void nominalIntorductionFinished(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2) {
        m_first.nominalIntorductionFinished(rootNode,treeNode,annotatedEquality,argument1,argument2);
        m_second.nominalIntorductionFinished(rootNode,treeNode,annotatedEquality,argument1,argument2);
    }
    @Override
    public void descriptionGraphCheckingStarted(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
        m_first.descriptionGraphCheckingStarted(graphIndex1,tupleIndex1,position1,graphIndex2,tupleIndex2,position2);
        m_second.descriptionGraphCheckingStarted(graphIndex1,tupleIndex1,position1,graphIndex2,tupleIndex2,position2);
    }
    @Override
    public void descriptionGraphCheckingFinished(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
        m_first.descriptionGraphCheckingFinished(graphIndex1,tupleIndex1,position1,graphIndex2,tupleIndex2,position2);
        m_second.descriptionGraphCheckingFinished(graphIndex1,tupleIndex1,position1,graphIndex2,tupleIndex2,position2);
    }
    @Override
    public void nodeCreated(Node node) {
        m_first.nodeCreated(node);
        m_second.nodeCreated(node);
    }
    @Override
    public void nodeDestroyed(Node node) {
        m_first.nodeDestroyed(node);
        m_second.nodeDestroyed(node);
    }
    @Override
    public void unknownDatatypeRestrictionDetectionStarted(DataRange dataRange1,Node node1,DataRange dataRange2,Node node2) {
        m_first.unknownDatatypeRestrictionDetectionStarted(dataRange1,node1,dataRange2,node2);
        m_second.unknownDatatypeRestrictionDetectionStarted(dataRange1,node1,dataRange2,node2);
    }
    @Override
    public void unknownDatatypeRestrictionDetectionFinished(DataRange dataRange1,Node node1, DataRange dataRange2,Node node2) {
        m_first.unknownDatatypeRestrictionDetectionFinished(dataRange1,node1,dataRange2,node2);
        m_second.unknownDatatypeRestrictionDetectionFinished(dataRange1,node1,dataRange2,node2);
    }
    @Override
    public void datatypeCheckingStarted() {
        m_first.datatypeCheckingStarted();
        m_second.datatypeCheckingStarted();
    }
    @Override
    public void datatypeCheckingFinished(boolean result) {
        m_first.datatypeCheckingFinished(result);
        m_second.datatypeCheckingFinished(result);
    }
    @Override
    public void datatypeConjunctionCheckingStarted(DatatypeManager.DConjunction conjunction) {
        m_first.datatypeConjunctionCheckingStarted(conjunction);
        m_second.datatypeConjunctionCheckingStarted(conjunction);
    }
    @Override
    public void datatypeConjunctionCheckingFinished(DatatypeManager.DConjunction conjunction,boolean result) {
        m_first.datatypeConjunctionCheckingFinished(conjunction,result);
        m_second.datatypeConjunctionCheckingFinished(conjunction,result);
    }
    @Override
    public void blockingValidationStarted() {
        m_first.blockingValidationStarted();
        m_second.blockingValidationStarted();
    }
    @Override
    public void blockingValidationFinished(int noInvalidlyBlocked) {
        m_first.blockingValidationFinished(noInvalidlyBlocked);
        m_second.blockingValidationFinished(noInvalidlyBlocked);
    }
    @Override
    public void possibleInstanceIsInstance() {
        m_first.possibleInstanceIsInstance();
        m_second.possibleInstanceIsInstance();
    }
    @Override
    public void possibleInstanceIsNotInstance() {
        m_first.possibleInstanceIsNotInstance();
        m_second.possibleInstanceIsNotInstance();
    }
}
