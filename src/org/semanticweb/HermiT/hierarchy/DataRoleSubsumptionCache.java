// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.DLClause.ClauseType;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLTypedLiteral;

/**
 * A cache for data roles.
 */
public class DataRoleSubsumptionCache extends RoleSubsumptionCache {

    public DataRoleSubsumptionCache(Reasoner reasoner) {
        super(reasoner,false,AtomicRole.BOTTOM_DATA_ROLE,AtomicRole.TOP_DATA_ROLE);
    }
    protected void loadRoleGraph(Set<Role> allRoles,Graph<Role> roleGraph) {
        allRoles.addAll(m_reasoner.getDLOntology().getAllAtomicDataRoles());
        for (DLClause dlClause : m_reasoner.getDLOntology().getDLClauses()) {
            if (dlClause.m_clauseType==ClauseType.DATA_PROPERTY_INCLUSION) {
                AtomicRole sub=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicRole sup=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                if (allRoles.contains(sub) && allRoles.contains(sup)) {
                    roleGraph.addEdge(sub,sup);
                    if (m_hasInverse)
                        roleGraph.addEdge(sub.getInverse(),sup.getInverse());
                }
            }
        }
    }
    protected boolean doSatisfiabilityTest(Role role) {
        return m_reasoner.getTableau().isSatisfiable(role,true);
    }
    protected boolean doSubsumptionCheck(RoleInfo subroleInfo,Role superrole) {
        Role subrole=subroleInfo.m_forRole;
        // This is different from object properties! This code is correct because we don't have 
        // transitive data properties.
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLIndividual individual=factory.getOWLNamedIndividual(IRI.create("internal:individual"));
        OWLDataProperty negatedSuperProperty=factory.getOWLDataProperty(IRI.create("internal:negated-superproperty"));
        OWLDataProperty subProperty=factory.getOWLDataProperty(IRI.create(((AtomicRole)subrole).getIRI()));
        OWLDataProperty superProperty=factory.getOWLDataProperty(IRI.create(((AtomicRole)superrole).getIRI()));
        OWLDatatype anonymousConstantsDatatype=factory.getOWLDatatype(IRI.create("internal:anonymous-constants"));
        OWLTypedLiteral constant=factory.getOWLTypedLiteral("internal:constant",anonymousConstantsDatatype);
        OWLAxiom subAssertion=factory.getOWLDataPropertyAssertionAxiom(subProperty,individual,constant);
        OWLAxiom superAssertion=factory.getOWLDataPropertyAssertionAxiom(negatedSuperProperty,individual,constant);
        OWLAxiom superDisjoint=factory.getOWLDisjointDataPropertiesAxiom(superProperty,negatedSuperProperty);
        Tableau tableau=m_reasoner.getTableau(ontologyManager,subAssertion,superAssertion,superDisjoint);
        boolean isSubsumedBy=!tableau.isABoxSatisfiable();
        if (!isSubsumedBy)
            subroleInfo.m_isSatisfiable=Boolean.TRUE;
        else
            subroleInfo.addKnownSubsumer(superrole);
        return isSubsumedBy;
    }
}
