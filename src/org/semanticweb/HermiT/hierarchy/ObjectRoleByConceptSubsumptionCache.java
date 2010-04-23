package org.semanticweb.HermiT.hierarchy;

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

 import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;

 /**
  * A cache for role subsumption and role satisfiability tests. This class also maintains the set of known and possible subsumers for a role. This information can be used to optimize classification.
  */
 public class ObjectRoleByConceptSubsumptionCache<T extends Role> extends AtomicConceptSubsumptionCache {
     private static final long serialVersionUID = 3292946505136599501L;
     protected final boolean m_isForObjectProperties;
     protected final Map<T,AtomicConcept> m_conceptsForRoles;
     protected final Map<AtomicConcept,T> m_rolesForConcepts;
     protected final boolean m_hasInverses;
     protected final AtomicRole m_bottomRole;
     protected final AtomicRole m_topRole;

     public ObjectRoleByConceptSubsumptionCache(Tableau tableau,boolean isForObjectProperties,boolean hasInverse,AtomicRole bottomRole,AtomicRole topRole,Map<T,AtomicConcept> conceptsForRoles,Map<AtomicConcept,T> rolesForConcepts) {
         super(tableau);
         m_isForObjectProperties=isForObjectProperties;
         m_hasInverses=hasInverse;
         m_bottomRole=bottomRole;
         m_topRole=topRole;
         m_conceptsForRoles=conceptsForRoles;
         m_rolesForConcepts=rolesForConcepts;
     }
     protected void updateKnownSubsumptionsUsingToldSubsumers(Graph<AtomicConcept> roleGraph,Set<DLClause> dlClauses) {
         for (DLClause dlClause : dlClauses) {
             if (dlClause.getHeadLength()==1 && dlClause.getBodyLength()==1) {
                 DLPredicate headPredicate=dlClause.getHeadAtom(0).getDLPredicate();
                 DLPredicate bodyPredicate=dlClause.getBodyAtom(0).getDLPredicate();
                 if (headPredicate instanceof AtomicRole && m_conceptsForRoles.containsKey(headPredicate) && bodyPredicate instanceof AtomicRole && m_conceptsForRoles.containsKey(bodyPredicate)) {
                     AtomicRole headRole=(AtomicRole)headPredicate;
                     AtomicRole bodyRole=(AtomicRole)bodyPredicate;
                     AtomicConcept conceptForHeadRole=m_conceptsForRoles.get(headRole);
                     AtomicConcept conceptForBodyRole=m_conceptsForRoles.get(bodyRole);
                     assert conceptForBodyRole!=null;
                     assert conceptForHeadRole!=null;
                     if (dlClause.getBodyAtom(0).getArgument(0)!=dlClause.getHeadAtom(0).getArgument(0)) {
                         // r -> s^- and r^- -> s
                         AtomicConcept conceptForBodyInvRole=m_conceptsForRoles.get(bodyRole.getInverse());
                         addKnownSubsumption(roleGraph,conceptForBodyInvRole, conceptForHeadRole);
                     } else {
                         // r-> s and r^- -> s^-
                         addKnownSubsumption(roleGraph,conceptForBodyRole,conceptForHeadRole);
                     }
                 }
             }
         }
     }
     protected void addKnownSubsumption(Graph<AtomicConcept> knownSubsumptions,AtomicConcept subConcept, AtomicConcept superConcept) {
         knownSubsumptions.addEdge(subConcept, superConcept);
         if (m_hasInverses) {
             AtomicConcept subConceptForInverse=m_conceptsForRoles.get(((Role)m_rolesForConcepts.get(subConcept)).getInverse());
             AtomicConcept superConceptForInverse=m_conceptsForRoles.get(((Role)m_rolesForConcepts.get(superConcept)).getInverse());
             knownSubsumptions.addEdge(subConceptForInverse, superConceptForInverse);
         }
     }
     protected ReasoningTaskDescription getSatTestDescription(AtomicConcept atomicConcept) {
         return ReasoningTaskDescription.isRoleSatisfiable(m_rolesForConcepts.get(atomicConcept),m_isForObjectProperties);
     }
     protected ReasoningTaskDescription getSubsumptionTestDescription(AtomicConcept subConcept,AtomicConcept superConcept) {;
         return ReasoningTaskDescription.isRoleSubsumedBy(m_rolesForConcepts.get(subConcept),m_rolesForConcepts.get(superConcept),m_isForObjectProperties);
     }
     protected boolean isRelevantConcept(AtomicConcept atomicConcept) {
         return atomicConcept.equals(AtomicConcept.THING) || atomicConcept.equals(AtomicConcept.NOTHING) || (Prefixes.isInternalIRIForPropertyClassification(atomicConcept.getIRI()));
     }
 }
