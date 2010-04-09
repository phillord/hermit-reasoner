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

import java.util.Collections;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.DLClause.ClauseType;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/**
 * A cache for object roles.
 */
public class ObjectRoleSubsumptionCache extends RoleSubsumptionCache<Role> {

    public ObjectRoleSubsumptionCache(Reasoner reasoner) {
        super(reasoner,reasoner.getDLOntology().hasInverseRoles(),AtomicRole.BOTTOM_OBJECT_ROLE,AtomicRole.TOP_OBJECT_ROLE);
    }
    protected void loadRoleGraph(Set<Role> allRoles,Graph<Role> roleGraph) {
        allRoles.addAll(m_reasoner.getDLOntology().getAllAtomicObjectRoles());
        if (m_hasInverse)
            for (AtomicRole atomicRole : m_reasoner.getDLOntology().getAllAtomicObjectRoles())
                allRoles.add(atomicRole.getInverse());
        for (DLClause dlClause : m_reasoner.getDLOntology().getDLClauses()) {
            if (dlClause.getClauseType()==ClauseType.OBJECT_PROPERTY_INCLUSION) {
                AtomicRole sub=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicRole sup=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                if (allRoles.contains(sub) && allRoles.contains(sup)) {
                    roleGraph.addEdge(sub,sup);
                    if (m_hasInverse)
                        roleGraph.addEdge(sub.getInverse(),sup.getInverse());
                }
            }
            else if (dlClause.getClauseType()==ClauseType.INVERSE_OBJECT_PROPERTY_INCLUSION) {
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
        Individual freshIndividualA=Individual.createAnonymous("fresh-individual-A");
        Individual freshIndividualB=Individual.createAnonymous("fresh-individual-B");
        return m_reasoner.getTableau().isSatisfiable(true,Collections.singleton(role.getRoleAssertion(freshIndividualA,freshIndividualB)),null,null,null,null,ReasoningTaskDescription.isRoleSatisfiable(role,true));
    }
    protected boolean doSubsumptionCheck(Role subrole,Role superrole) {
        // This code is different from data properties. If the superrole is not simple, then
        // we can use negative role assertions; otherwise, we need to take into account the
        // complex role inclusions into account.
        DLOntology dlOntology=m_reasoner.getDLOntology();
        if (!dlOntology.isComplexObjectRole(superrole)) {
            Individual freshIndividualA=Individual.createAnonymous("fresh-individual-A");
            Individual freshIndividualB=Individual.createAnonymous("fresh-individual-B");
            Atom subroleAtom=subrole.getRoleAssertion(freshIndividualA,freshIndividualB);
            Atom superroleAtom=superrole.getRoleAssertion(freshIndividualA,freshIndividualB);
            Tableau tableau=m_reasoner.getTableau();
            return !tableau.isSatisfiable(false,Collections.singleton(subroleAtom),Collections.singleton(superroleAtom),null,null,null,ReasoningTaskDescription.isRoleSubsumedBy(subrole,superrole,true));
        }
        else {
            OWLDataFactory factory=m_reasoner.getDataFactory();
            OWLIndividual freshIndividualA=factory.getOWLAnonymousIndividual("fresh-individual-A");
            OWLIndividual freshIndividualB=factory.getOWLAnonymousIndividual("fresh-individual-B");
            OWLObjectPropertyExpression subpropertyExpression=getObjectPropertyExpression(factory,subrole);
            OWLObjectPropertyExpression superpropertyExpression=getObjectPropertyExpression(factory,superrole);
            OWLClass pseudoNominal=factory.getOWLClass(IRI.create("internal:pseudo-nominal"));
            OWLAxiom subpropertyAssertion=factory.getOWLObjectPropertyAssertionAxiom(subpropertyExpression,freshIndividualA,freshIndividualB);
            OWLClassExpression allSuperNotPseudoNominal=factory.getOWLObjectAllValuesFrom(superpropertyExpression,pseudoNominal.getObjectComplementOf());
            OWLAxiom pseudoNominalAssertion=factory.getOWLClassAssertionAxiom(pseudoNominal,freshIndividualB);
            OWLAxiom superpropertyAssertion=factory.getOWLClassAssertionAxiom(allSuperNotPseudoNominal,freshIndividualA);
            Tableau tableau=m_reasoner.getTableau(subpropertyAssertion,pseudoNominalAssertion,superpropertyAssertion);
            return !tableau.isSatisfiable(true,null,null,null,null,null,ReasoningTaskDescription.isRoleSubsumedBy(subrole,superrole,true));
        }
    }
    protected static OWLObjectPropertyExpression getObjectPropertyExpression(OWLDataFactory factory,Role role) {
        if (role instanceof AtomicRole)
            return factory.getOWLObjectProperty(IRI.create(((AtomicRole)role).getIRI()));
        else {
            AtomicRole inverseOf=((InverseRole)role).getInverseOf();
            return factory.getOWLObjectProperty(IRI.create(inverseOf.getIRI())).getInverseProperty();
        }
    }
}
