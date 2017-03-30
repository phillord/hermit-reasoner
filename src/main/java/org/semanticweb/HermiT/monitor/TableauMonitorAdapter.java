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
/**TableauMonitorAdapter.*/
public class TableauMonitorAdapter implements TableauMonitor,Serializable  {
    private static final long serialVersionUID=6336033031431260208L;

    protected Tableau m_tableau;

    @Override
    public void setTableau(Tableau tableau) {
        m_tableau=tableau;
    }
    @Override
    public void isSatisfiableStarted(ReasoningTaskDescription reasoningTaskDescription) {
    }
    @Override
    public void isSatisfiableFinished(ReasoningTaskDescription reasoningTaskDescription,boolean result) {
    }
    @Override
    public void tableauCleared() {
    }
    @Override
    public void saturateStarted() {
    }
    @Override
    public void saturateFinished(boolean modelFound) {
    }
    @Override
    public void iterationStarted() {
    }
    @Override
    public void iterationFinished() {
    }
    @Override
    public void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
    }
    @Override
    public void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
    }
    @Override
    public void addFactStarted(Object[] tuple,boolean isCore) {
    }
    @Override
    public void addFactFinished(Object[] tuple,boolean isCore,boolean factAdded) {
    }
    @Override
    public void mergeStarted(Node mergeFrom,Node mergeInto) {
    }
    @Override
    public void nodePruned(Node node) {
    }
    @Override
    public void mergeFactStarted(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
    }
    @Override
    public void mergeFactFinished(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
    }
    @Override
    public void mergeFinished(Node mergeFrom,Node mergeInto) {
    }
    @Override
    public void clashDetectionStarted(Object[]... tuples) {
    }
    @Override
    public void clashDetectionFinished(Object[]... tuples) {
    }
    @Override
    public void clashDetected() {
    }
    @Override
    public void backtrackToStarted(BranchingPoint newCurrentBrancingPoint) {
    }
    @Override
    public void tupleRemoved(Object[] tuple) {
    }
    @Override
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
    }
    @Override
    public void groundDisjunctionDerived(GroundDisjunction groundDisjunction) {
    }
    @Override
    public void processGroundDisjunctionStarted(GroundDisjunction groundDisjunction) {
    }
    @Override
    public void groundDisjunctionSatisfied(GroundDisjunction groundDisjunction) {
    }
    @Override
    public void processGroundDisjunctionFinished(GroundDisjunction groundDisjunction) {
    }
    @Override
    public void disjunctProcessingStarted(GroundDisjunction groundDisjunction,int disjunct) {
    }
    @Override
    public void disjunctProcessingFinished(GroundDisjunction groundDisjunction,int disjunct) {
    }
    @Override
    public void pushBranchingPointStarted(BranchingPoint branchingPoint) {
    }
    @Override
    public void pushBranchingPointFinished(BranchingPoint branchingPoint) {
    }
    @Override
    public void startNextBranchingPointStarted(BranchingPoint branchingPoint) {
    }
    @Override
    public void startNextBranchingPointFinished(BranchingPoint branchingPoint) {
    }
    @Override
    public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
    }
    @Override
    public void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode) {
    }
    @Override
    public void existentialSatisfied(ExistentialConcept existentialConcept,Node forNode) {
    }
    @Override
    public void nominalIntorductionStarted(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2) {
    }
    @Override
    public void nominalIntorductionFinished(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2) {
    }
    @Override
    public void descriptionGraphCheckingStarted(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
    }
    @Override
    public void descriptionGraphCheckingFinished(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
    }
    @Override
    public void nodeCreated(Node node) {
    }
    @Override
    public void nodeDestroyed(Node node) {
    }
    @Override
    public void unknownDatatypeRestrictionDetectionStarted(DataRange dataRange1,Node node1,DataRange dataRange2,Node node2) {
    }
    @Override
    public void unknownDatatypeRestrictionDetectionFinished(DataRange dataRange1,Node node1, DataRange dataRange2,Node node2) {
    }
    @Override
    public void datatypeCheckingStarted() {
    }
    @Override
    public void datatypeCheckingFinished(boolean result) {
    }
    @Override
    public void datatypeConjunctionCheckingStarted(DatatypeManager.DConjunction conjunction) {
    }
    @Override
    public void datatypeConjunctionCheckingFinished(DatatypeManager.DConjunction conjunction,boolean result) {
    }
    @Override
    public void blockingValidationStarted() {
    }
    @Override
    public void blockingValidationFinished(int noInvalidlyBlocked) {
    }
    @Override
    public void possibleInstanceIsInstance() {
    }
    @Override
    public void possibleInstanceIsNotInstance() {
    }
}
