/* Copyright 2008, 2009 by the Oxford University Computing Laboratory
   
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

import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.DLClause.ClauseType;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * A cache for object roles.
 */
public class ObjectRoleSubsumptionCache extends RoleSubsumptionCache {

    public ObjectRoleSubsumptionCache(Reasoner reasoner) {
        super(reasoner,reasoner.getDLOntology().hasInverseRoles(),AtomicRole.BOTTOM_OBJECT_ROLE,AtomicRole.TOP_OBJECT_ROLE);
    }
    protected void loadRoleGraph(Set<Role> allRoles,Graph<Role> roleGraph) {
        allRoles.addAll(m_reasoner.getDLOntology().getAllAtomicObjectRoles());
        if (m_hasInverse)
            for (AtomicRole atomicRole : m_reasoner.getDLOntology().getAllAtomicObjectRoles())
                allRoles.add(atomicRole.getInverse());
        for (DLClause dlClause : m_reasoner.getDLOntology().getDLClauses()) {
            if (dlClause.m_clauseType==ClauseType.OBJECT_PROPERTY_INCLUSION) {
                AtomicRole sub=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicRole sup=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                if (allRoles.contains(sub) && allRoles.contains(sup)) {
                    roleGraph.addEdge(sub,sup);
                    if (m_hasInverse)
                        roleGraph.addEdge(sub.getInverse(),sup.getInverse());
                }
            }
            else if (dlClause.m_clauseType==ClauseType.INVERSE_OBJECT_PROPERTY_INCLUSION) {
                AtomicRole sub=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicRole sup=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                if (allRoles.contains(sub) && allRoles.contains(sup)) {
                    roleGraph.addEdge(sub.getInverse(),sup);
                    roleGraph.addEdge(sub,sup.getInverse());
                }
            }
        }
    }
    protected boolean doSatisfiabilityTest(Role role) {
        return m_reasoner.getTableau().isSatisfiable(role,false);
    }
    protected boolean doSubsumptionCheck(RoleInfo subroleInfo,Role superrole) {
        Role subrole=subroleInfo.m_forRole;
        // This code is different from data properties. This is because object properties can be transitive, so
        // we need to make sure that appropriate DL-clauses are added for negative object property assertions.
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLNamedIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
        OWLNamedIndividual individualB=factory.getOWLNamedIndividual(IRI.create("internal:individualB"));
        OWLAxiom subAssertion;
        if (subrole instanceof AtomicRole)
            subAssertion=factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(IRI.create(((AtomicRole) subrole).getIRI())),individualA,individualB);
        else 
            subAssertion=factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(IRI.create(((InverseRole) subrole).getInverseOf().getIRI())),individualB,individualA);
        OWLAxiom superNegatedAssertion;
        if (superrole instanceof AtomicRole)
            superNegatedAssertion=factory.getOWLNegativeObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(IRI.create(((AtomicRole) superrole).getIRI())),individualA,individualB);
        else 
            superNegatedAssertion=factory.getOWLNegativeObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(IRI.create(((InverseRole) superrole).getInverseOf().getIRI())),individualB,individualA);
        Tableau tableau=m_reasoner.getTableau(ontologyManager,subAssertion,superNegatedAssertion);
        boolean isSubsumedBy=!tableau.isABoxSatisfiable(Individual.create(individualA.getIRI().toString(),true), Individual.create(individualB.getIRI().toString(),true));
        if (!isSubsumedBy) {
            subroleInfo.m_isSatisfiable=Boolean.TRUE;
            updateKnownSubsumers(subrole,tableau);
            updatePossibleSubsumers(tableau);
        }
        else
            subroleInfo.addKnownSubsumer(superrole);
        return isSubsumedBy;
    }
}
