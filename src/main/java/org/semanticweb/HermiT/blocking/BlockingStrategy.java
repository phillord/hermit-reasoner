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

/**
 * @author ignazio
 *
 */
public interface BlockingStrategy {
    /**
     * @param tableau tableau
     */
    void initialize(Tableau tableau);
    /**
     * @param additionalDLOntology additionalDLOntology
     */
    void additionalDLOntologySet(DLOntology additionalDLOntology);
    /**
     * 
     */
    void additionalDLOntologyCleared();
    /**
     * 
     */
    void clear();
    /**
     * @param finalChance finalChance
     */
    void computeBlocking(boolean finalChance);
    /**
     * @param concept concept
     * @param node node
     * @return true if permanent
     */
    boolean isPermanentAssertion(Concept concept,Node node);
    /**
     * @param range range
     * @param node node
     * @return true if permanent
     */
    boolean isPermanentAssertion(DataRange range,Node node);
    /**
     * @param concept concept
     * @param node node
     * @param isCore isCore
     */
    void assertionAdded(Concept concept,Node node,boolean isCore);
    /**
     * @param concept concept
     * @param node node
     */
    void assertionCoreSet(Concept concept,Node node);
    /**
     * @param concept concept
     * @param node node
     * @param isCore isCore
     */
    void assertionRemoved(Concept concept,Node node,boolean isCore);
    /**
     * @param range range
     * @param node node
     * @param isCore isCore
     */
    void assertionAdded(DataRange range,Node node,boolean isCore);
    /**
     * @param range range
     * @param node node
     */
    void assertionCoreSet(DataRange range,Node node);
    /**
     * @param range range
     * @param node node
     * @param isCore isCore
     */
    void assertionRemoved(DataRange range,Node node,boolean isCore);
    /**
     * @param atomicRole atomicRole
     * @param nodeFrom nodeFrom
     * @param nodeTo nodeTo
     * @param isCore isCore
     */
    void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    /**
     * @param atomicRole atomicRole
     * @param nodeFrom nodeFrom
     * @param nodeTo nodeTo
     */
    void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo);
    /**
     * @param atomicRole atomicRole
     * @param nodeFrom nodeFrom
     * @param nodeTo nodeTo
     * @param isCore isCore
     */
    void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    /**
     * @param mergeFrom mergeFrom
     * @param mergeInto mergeInto
     */
    void nodesMerged(Node mergeFrom,Node mergeInto);
    /**
     * @param mergeFrom mergeFrom
     * @param mergeInto mergeInto
     */
    void nodesUnmerged(Node mergeFrom,Node mergeInto);
    /**
     * @param node node
     */
    void nodeStatusChanged(Node node);
    /**
     * @param node node
     */
    void nodeInitialized(Node node);
    /**
     * @param node node
     */
    void nodeDestroyed(Node node);
    /**
     * 
     */
    void modelFound();
    /**
     * @return true if exact
     */
    boolean isExact();
    /**
     * @param workers workers
     * @param dlClause dlClause
     * @param variables variables
     * @param valuesBuffer valuesBuffer
     * @param coreVariables coreVariables
     */
    void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,List<Variable> variables,Object[] valuesBuffer,boolean[] coreVariables);
}
