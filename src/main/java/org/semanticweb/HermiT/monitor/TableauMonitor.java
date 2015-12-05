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

public interface TableauMonitor {
    void setTableau(Tableau tableau);
    void isSatisfiableStarted(ReasoningTaskDescription reasoningTaskDescription);
    void isSatisfiableFinished(ReasoningTaskDescription reasoningTaskDescription,boolean result);
    void tableauCleared();
    void saturateStarted();
    void saturateFinished(boolean modelFound);
    void iterationStarted();
    void iterationFinished();
    void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex);
    void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex);
    void addFactStarted(Object[] tuple,boolean isCore);
    void addFactFinished(Object[] tuple,boolean isCore,boolean factAdded);
    void mergeStarted(Node mergeFrom,Node mergeInto);
    void nodePruned(Node node);
    void mergeFactStarted(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple);
    void mergeFactFinished(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple);
    void mergeFinished(Node mergeFrom,Node mergeInto);
    void clashDetectionStarted(Object[]... tuples);
    void clashDetectionFinished(Object[]... tuples);
    void clashDetected();
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
    void nominalIntorductionStarted(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2);
    void nominalIntorductionFinished(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2);
    void descriptionGraphCheckingStarted(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2);
    void descriptionGraphCheckingFinished(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2);
    void nodeCreated(Node node);
    void nodeDestroyed(Node node);
    void unknownDatatypeRestrictionDetectionStarted(DataRange dataRange1,Node node1,DataRange dataRange2,Node node2);
    void unknownDatatypeRestrictionDetectionFinished(DataRange dataRange1,Node node1,DataRange dataRange2,Node node2);
    void datatypeCheckingStarted();
    void datatypeCheckingFinished(boolean result);
    void datatypeConjunctionCheckingStarted(DatatypeManager.DConjunction conjunction);
    void datatypeConjunctionCheckingFinished(DatatypeManager.DConjunction conjunction,boolean result);
    void blockingValidationStarted();
    void blockingValidationFinished(int noInvalidlyBlocked);
    void possibleInstanceIsInstance();
    void possibleInstanceIsNotInstance();
}
