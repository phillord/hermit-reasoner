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

public class TableauMonitorAdapter implements TableauMonitor,Serializable  {
    private static final long serialVersionUID=6336033031431260208L;

    protected Tableau m_tableau;

    public TableauMonitorAdapter() {
    }
    public void setTableau(Tableau tableau) {
        m_tableau=tableau;
    }
    public void isSatisfiableStarted(ReasoningTaskDescription reasoningTaskDescription) {
    }
    public void isSatisfiableFinished(ReasoningTaskDescription reasoningTaskDescription,boolean result) {
    }
    public void tableauCleared() {
    }
    public void saturateStarted() {
    }
    public void saturateFinished(boolean modelFound) {
    }
    public void iterationStarted() {
    }
    public void iterationFinished() {
    }
    public void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
    }
    public void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
    }
    public void addFactStarted(Object[] tuple,boolean isCore) {
    }
    public void addFactFinished(Object[] tuple,boolean isCore,boolean factAdded) {
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
    public void clashDetectionStarted(Object[]... tuples) {
    }
    public void clashDetectionFinished(Object[]... tuples) {
    }
    public void clashDetected() {
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
    public void nominalIntorductionStarted(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2) {
    }
    public void nominalIntorductionFinished(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2) {
    }
    public void descriptionGraphCheckingStarted(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
    }
    public void descriptionGraphCheckingFinished(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
    }
    public void nodeCreated(Node node) {
    }
    public void nodeDestroyed(Node node) {
    }
    public void unknownDatatypeRestrictionDetectionStarted(DataRange dataRange1,Node node1,DataRange dataRange2,Node node2) {
    }
    public void unknownDatatypeRestrictionDetectionFinished(DataRange dataRange1,Node node1, DataRange dataRange2,Node node2) {
    }
    public void datatypeCheckingStarted() {
    }
    public void datatypeCheckingFinished(boolean result) {
    }
    public void datatypeConjunctionCheckingStarted(DatatypeManager.DConjunction conjunction) {
    }
    public void datatypeConjunctionCheckingFinished(DatatypeManager.DConjunction conjunction,boolean result) {
    }
    public void blockingValidationStarted() {
    }
    public void blockingValidationFinished(int noInvalidlyBlocked) {
    }
    public void possibleInstanceIsInstance() {
    }
    public void possibleInstanceIsNotInstance() {
    }
}
