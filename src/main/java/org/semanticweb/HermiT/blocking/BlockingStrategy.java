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
package org.semanticweb.HermiT.blocking;

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

public interface BlockingStrategy {
    void initialize(Tableau tableau);
    void additionalDLOntologySet(DLOntology additionalDLOntology);
    void additionalDLOntologyCleared();
    void clear();
    void computeBlocking(boolean finalChance);
    boolean isPermanentAssertion(Concept concept,Node node);
    boolean isPermanentAssertion(DataRange range,Node node);
    void assertionAdded(Concept concept,Node node,boolean isCore);
    void assertionCoreSet(Concept concept,Node node);
    void assertionRemoved(Concept concept,Node node,boolean isCore);
    void assertionAdded(DataRange range,Node node,boolean isCore);
    void assertionCoreSet(DataRange range,Node node);
    void assertionRemoved(DataRange range,Node node,boolean isCore);
    void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo);
    void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    void nodesMerged(Node mergeFrom,Node mergeInto);
    void nodesUnmerged(Node mergeFrom,Node mergeInto);
    void nodeStatusChanged(Node node);
    void nodeInitialized(Node node);
    void nodeDestroyed(Node node);
    void modelFound();
    boolean isExact();
    void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,List<Variable> variables,Object[] valuesBuffer,boolean[] coreVariables);
}
