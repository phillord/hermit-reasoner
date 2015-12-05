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
package org.semanticweb.HermiT.existentials;

import java.util.List;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

/**
 * Strategy objects are responsible for selecting which existentials should be
 * expanded first, as well as how the new nodes are introduced. The latter is
 * usually delegated to tableau.ExistentialExpansionManager, but strategies
 * are free to provide their own node-introduction implementations
 * (but be careful---it's tough to get right!)
 */
public interface ExistentialExpansionStrategy {
    void initialize(Tableau tableau);
    void additionalDLOntologySet(DLOntology additionalDLOntology);
    void additionalDLOntologyCleared();
    void clear();
    boolean expandExistentials(boolean finalChance);
    void assertionAdded(Concept concept,Node node,boolean isCore);
    void assertionAdded(DataRange dataRange,Node node,boolean isCore);
    void assertionCoreSet(Concept concept,Node node);
    void assertionCoreSet(DataRange dataRange,Node node);
    void assertionRemoved(Concept concept,Node node,boolean isCore);
    void assertionRemoved(DataRange dataRange,Node node,boolean isCore);
    void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo);
    void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    void nodesMerged(Node mergeFrom,Node mergeInto);
    void nodesUnmerged(Node mergeFrom,Node mergeInto);
    void nodeStatusChanged(Node node);
    void nodeInitialized(Node node);
    void nodeDestroyed(Node node);
    void branchingPointPushed();
    void backtrack();
    void modelFound();
    boolean isDeterministic();
    boolean isExact();
    void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,List<Variable> variables,Object[] valuesBuffer,boolean[] coreVariables);
}
