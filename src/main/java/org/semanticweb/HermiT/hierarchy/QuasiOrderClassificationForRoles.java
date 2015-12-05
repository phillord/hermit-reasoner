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
package org.semanticweb.HermiT.hierarchy;

import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;

public class QuasiOrderClassificationForRoles extends QuasiOrderClassification {
    protected final boolean m_hasInverses;
    protected final Map<Role,AtomicConcept> m_conceptsForRoles;
    protected final Map<AtomicConcept,Role> m_rolesForConcepts;

    public QuasiOrderClassificationForRoles(Tableau tableau,ClassificationProgressMonitor progressMonitor,AtomicConcept topElement,AtomicConcept bottomElement,Set<AtomicConcept> elements,boolean hasInverses,Map<Role,AtomicConcept> conceptsForRoles,Map<AtomicConcept,Role> rolesForConcepts) {
        super(tableau,progressMonitor,topElement,bottomElement,elements);
        m_hasInverses=hasInverses;
        m_conceptsForRoles=conceptsForRoles;
        m_rolesForConcepts=rolesForConcepts;
    }
    protected void initialiseKnownSubsumptionsUsingToldSubsumers(Set<DLClause> dlClauses) {
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
                        addKnownSubsumption(conceptForBodyInvRole,conceptForHeadRole);
                    }
                    else {
                        // r-> s and r^- -> s^-
                        addKnownSubsumption(conceptForBodyRole,conceptForHeadRole);
                    }
                }
            }
        }
    }
    protected void addKnownSubsumption(AtomicConcept subConcept,AtomicConcept superConcept) {
        super.addKnownSubsumption(subConcept,superConcept);
        if (m_hasInverses) {
            AtomicConcept subConceptForInverse=m_conceptsForRoles.get((m_rolesForConcepts.get(subConcept)).getInverse());
            AtomicConcept superConceptForInverse=m_conceptsForRoles.get((m_rolesForConcepts.get(superConcept)).getInverse());
            super.addKnownSubsumption(subConceptForInverse,superConceptForInverse);
        }
    }
    protected void addPossibleSubsumption(AtomicConcept subConcept,AtomicConcept superConcept) {
        super.addPossibleSubsumption(subConcept,superConcept);
        if (m_hasInverses) {
            AtomicConcept subConceptForInverse=m_conceptsForRoles.get((m_rolesForConcepts.get(subConcept)).getInverse());
            AtomicConcept superConceptForInverse=m_conceptsForRoles.get((m_rolesForConcepts.get(superConcept)).getInverse());
            super.addPossibleSubsumption(subConceptForInverse,superConceptForInverse);
        }
    }
    protected ReasoningTaskDescription getSatTestDescription(AtomicConcept atomicConcept) {
        return ReasoningTaskDescription.isRoleSatisfiable(m_rolesForConcepts.get(atomicConcept),true);
    }
    protected ReasoningTaskDescription getSubsumptionTestDescription(AtomicConcept subConcept,AtomicConcept superConcept) {
        return ReasoningTaskDescription.isRoleSubsumedBy(m_rolesForConcepts.get(subConcept),m_rolesForConcepts.get(superConcept),true);
    }
    protected ReasoningTaskDescription getSubsumedByListTestDescription(AtomicConcept subConcept,Object[] superconcepts) {
        Object[] roles=new Object[superconcepts.length];
        for (int i=0;i<roles.length;i++) {
            assert superconcepts[i] instanceof AtomicConcept;
            roles[i]=m_rolesForConcepts.get(superconcepts[i]);
        }
        return ReasoningTaskDescription.isRoleSubsumedByList(m_rolesForConcepts.get(subConcept),roles,true);
    }
}
