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
/**TableauMonitor.*/
public interface TableauMonitor {
    /**
     * @param tableau tableau
     */
    void setTableau(Tableau tableau);
    /**
     * @param reasoningTaskDescription reasoningTaskDescription
     */
    void isSatisfiableStarted(ReasoningTaskDescription reasoningTaskDescription);
    /**
     * @param reasoningTaskDescription reasoningTaskDescription
     * @param result result
     */
    void isSatisfiableFinished(ReasoningTaskDescription reasoningTaskDescription,boolean result);
    /**
     * Tableau cleared.
     */
    void tableauCleared();
    /**
     * Saturate started.
     */
    void saturateStarted();
    /**
     * @param modelFound modelFound
     */
    void saturateFinished(boolean modelFound);
    /**
     * Iteration started.
     */
    void iterationStarted();
    /**
     * Iteration finished.
     */
    void iterationFinished();
    /**
     * @param dlClauseEvaluator dlClauseEvaluator
     * @param dlClauseIndex dlClauseIndex
     */
    void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex);
    /**
     * @param dlClauseEvaluator dlClauseEvaluator
     * @param dlClauseIndex dlClauseIndex
     */
    void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex);
    /**
     * @param tuple tuple
     * @param isCore isCore
     */
    void addFactStarted(Object[] tuple,boolean isCore);
    /**
     * @param tuple tuple
     * @param isCore isCore
     * @param factAdded factAdded
     */
    void addFactFinished(Object[] tuple,boolean isCore,boolean factAdded);
    /**
     * @param mergeFrom mergeFrom
     * @param mergeInto mergeInto
     */
    void mergeStarted(Node mergeFrom,Node mergeInto);
    /**
     * @param node node
     */
    void nodePruned(Node node);
    /**
     * @param mergeFrom mergeFrom
     * @param mergeInto mergeInto
     * @param sourceTuple sourceTuple
     * @param targetTuple targetTuple
     */
    void mergeFactStarted(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple);
    /**
     * @param mergeFrom mergeFrom
     * @param mergeInto mergeInto
     * @param sourceTuple sourceTuple
     * @param targetTuple targetTuple
     */
    void mergeFactFinished(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple);
    /**
     * @param mergeFrom mergeFrom
     * @param mergeInto mergeInto
     */
    void mergeFinished(Node mergeFrom,Node mergeInto);
    /**
     * @param tuples tuples
     */
    void clashDetectionStarted(Object[]... tuples);
    /**
     * @param tuples tuples
     */
    void clashDetectionFinished(Object[]... tuples);
    /**
     * Clash detected.
     */
    void clashDetected();
    /**
     * @param newCurrentBrancingPoint newCurrentBrancingPoint
     */
    void backtrackToStarted(BranchingPoint newCurrentBrancingPoint);
    /**
     * @param tuple tuple
     */
    void tupleRemoved(Object[] tuple);
    /**
     * @param newCurrentBrancingPoint newCurrentBrancingPoint
     */
    void backtrackToFinished(BranchingPoint newCurrentBrancingPoint);
    /**
     * @param groundDisjunction groundDisjunction
     */
    void groundDisjunctionDerived(GroundDisjunction groundDisjunction);
    /**
     * @param groundDisjunction groundDisjunction
     */
    void processGroundDisjunctionStarted(GroundDisjunction groundDisjunction);
    /**
     * @param groundDisjunction groundDisjunction
     */
    void groundDisjunctionSatisfied(GroundDisjunction groundDisjunction);
    /**
     * @param groundDisjunction groundDisjunction
     */
    void processGroundDisjunctionFinished(GroundDisjunction groundDisjunction);
    /**
     * @param groundDisjunction groundDisjunction
     * @param disjunct disjunct
     */
    void disjunctProcessingStarted(GroundDisjunction groundDisjunction,int disjunct);
    /**
     * @param groundDisjunction groundDisjunction
     * @param disjunct disjunct
     */
    void disjunctProcessingFinished(GroundDisjunction groundDisjunction,int disjunct);
    /**
     * @param branchingPoint branchingPoint
     */
    void pushBranchingPointStarted(BranchingPoint branchingPoint);
    /**
     * @param branchingPoint branchingPoint
     */
    void pushBranchingPointFinished(BranchingPoint branchingPoint);
    /**
     * @param branchingPoint branchingPoint
     */
    void startNextBranchingPointStarted(BranchingPoint branchingPoint);
    /**
     * @param branchingPoint branchingPoint
     */
    void startNextBranchingPointFinished(BranchingPoint branchingPoint);
    /**
     * @param existentialConcept existentialConcept
     * @param forNode forNode
     */
    void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode);
    /**
     * @param existentialConcept existentialConcept
     * @param forNode forNode
     */
    void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode);
    /**
     * @param existentialConcept existentialConcept
     * @param forNode forNode
     */
    void existentialSatisfied(ExistentialConcept existentialConcept,Node forNode);
    /**
     * @param rootNode rootNode
     * @param treeNode treeNode
     * @param annotatedEquality annotatedEquality
     * @param argument1 argument1
     * @param argument2 argument2
     */
    void nominalIntorductionStarted(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2);
    /**
     * @param rootNode rootNode
     * @param treeNode treeNode
     * @param annotatedEquality annotatedEquality
     * @param argument1 argument1
     * @param argument2 argument2
     */
    void nominalIntorductionFinished(Node rootNode,Node treeNode,AnnotatedEquality annotatedEquality,Node argument1,Node argument2);
    /**
     * @param graphIndex1 graphIndex1
     * @param tupleIndex1 tupleIndex1
     * @param position1 position1
     * @param graphIndex2 graphIndex2
     * @param tupleIndex2 tupleIndex2
     * @param position2 position2
     */
    void descriptionGraphCheckingStarted(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2);
    /**
     * @param graphIndex1 graphIndex1
     * @param tupleIndex1 tupleIndex1
     * @param position1 position1
     * @param graphIndex2 graphIndex2
     * @param tupleIndex2 tupleIndex2
     * @param position2 position2
     */
    void descriptionGraphCheckingFinished(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2);
    /**
     * @param node node
     */
    void nodeCreated(Node node);
    /**
     * @param node node
     */
    void nodeDestroyed(Node node);
    /**
     * @param dataRange1 dataRange1
     * @param node1 node1
     * @param dataRange2 dataRange2
     * @param node2 node2
     */
    void unknownDatatypeRestrictionDetectionStarted(DataRange dataRange1,Node node1,DataRange dataRange2,Node node2);
    /**
     * @param dataRange1 dataRange1
     * @param node1 node1
     * @param dataRange2 dataRange2
     * @param node2 node2
     */
    void unknownDatatypeRestrictionDetectionFinished(DataRange dataRange1,Node node1,DataRange dataRange2,Node node2);
    /**
     * Datatype checking started.
     */
    void datatypeCheckingStarted();
    /**
     * @param result result
     */
    void datatypeCheckingFinished(boolean result);
    /**
     * @param conjunction conjunction
     */
    void datatypeConjunctionCheckingStarted(DatatypeManager.DConjunction conjunction);
    /**
     * @param conjunction conjunction
     * @param result result
     */
    void datatypeConjunctionCheckingFinished(DatatypeManager.DConjunction conjunction,boolean result);
    /**
     * Blocking validation started.
     */
    void blockingValidationStarted();
    /**
     * @param noInvalidlyBlocked noInvalidlyBlocked
     */
    void blockingValidationFinished(int noInvalidlyBlocked);
    /**
     * Possible instance is instance.
     */
    void possibleInstanceIsInstance();
    /**
     * Possible instance is not instance.
     */
    void possibleInstanceIsNotInstance();
}
