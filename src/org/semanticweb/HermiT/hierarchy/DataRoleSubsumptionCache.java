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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.DLClause.ClauseType;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLTypedLiteral;

/**
 * A cache for role subsumption and role satisfiability tests. This class also maintains the set of known and possible subsumers for a role. This information can be used to optimize classification.
 */
public class DataRoleSubsumptionCache implements SubsumptionCache<AtomicRole> {
    protected final Reasoner m_reasoner;
    protected final Map<AtomicRole,RoleInfo> m_roleInfos;
    protected final AtomicRole m_bottomRole;
    protected final AtomicRole m_topRole;

    public DataRoleSubsumptionCache(Reasoner reasoner) {
        m_reasoner=reasoner;
        m_roleInfos=new HashMap<AtomicRole,RoleInfo>();
        m_bottomRole=AtomicRole.BOTTOM_DATA_ROLE;
        m_topRole=AtomicRole.TOP_DATA_ROLE;
        Set<AtomicRole> allRoles=new HashSet<AtomicRole>();
        Graph<AtomicRole> roleGraph=new Graph<AtomicRole>();
        loadRoleGraph(allRoles,roleGraph);
        roleGraph.transitivelyClose();
        for (AtomicRole subrole : allRoles) {
            Set<AtomicRole> superroles=roleGraph.getSuccessors(subrole);
            for (AtomicRole superrole : superroles)
                getRoleInfo(subrole).addKnownSubsumer(superrole);
        }
    }
    public void initialize() {   
    }
    protected void loadRoleGraph(Set<AtomicRole> allRoles,Graph<AtomicRole> roleGraph) {
        allRoles.addAll(m_reasoner.getDLOntology().getAllAtomicDataRoles());
        for (DLClause dlClause : m_reasoner.getDLOntology().getDLClauses()) {
            if (dlClause.getClauseType()==ClauseType.DATA_PROPERTY_INCLUSION) {
                AtomicRole sub=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicRole sup=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                if (allRoles.contains(sub) && allRoles.contains(sup))
                    roleGraph.addEdge(sub,sup);
            }
        }
    }
    public Set<AtomicRole> getAllKnownSubsumers(AtomicRole role) {
        return null;
    }
    public boolean isSatisfiable(AtomicRole role) {
        return isSatisfiable(role,true);
    }
    protected boolean isSatisfiable(AtomicRole role,boolean updatePossibleSubsumers) {
        if (m_bottomRole.equals(role))
            return false;
        RoleInfo roleInfo=getRoleInfo(role);
        if (roleInfo.m_isSatisfiable==null) {
            boolean isSatisfiable=doSatisfiabilityTest(role);
            roleInfo.m_isSatisfiable=(isSatisfiable ? Boolean.TRUE : Boolean.FALSE);
        }
        return roleInfo.m_isSatisfiable;
    }
    protected boolean doSatisfiabilityTest(AtomicRole role) {
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        Constant freshConstant=Constant.create(new Constant.AnonymousConstantValue("internal:fresh-constant"));
        return m_reasoner.getTableau().isSatisfiable(true,Collections.singleton(role.getRoleAssertion(freshIndividual,freshConstant)),null,null,null,null,ReasoningTaskDescription.isRoleSatisfiable(role,false));
    }
    public boolean isSubsumedBy(AtomicRole subrole,AtomicRole superrole) {
        if (m_topRole.equals(superrole) || m_bottomRole.equals(subrole))
            return true;
        RoleInfo subroleInfo=getRoleInfo(subrole);
        if (Boolean.FALSE.equals(subroleInfo.m_isSatisfiable))
            return true;
        else if (m_bottomRole.equals(superrole) || (m_roleInfos.containsKey(superrole) && Boolean.FALSE.equals(m_roleInfos.get(superrole).m_isSatisfiable)))
            return !isSatisfiable(subrole,true);
        if (subroleInfo.isKnownSubsumer(superrole))
            return true;
        else if (subroleInfo.isKnownNonsubsumer(superrole))
            return false;
        else {
            boolean isSubsumedBy=doSubsumptionCheck(subrole,superrole);
            if (isSubsumedBy)
                subroleInfo.addKnownSubsumer(superrole);
            else
                subroleInfo.addKnownNonsubsumer(superrole);
            return isSubsumedBy;
        }
    }
    protected boolean doSubsumptionCheck(AtomicRole subrole,AtomicRole superrole) {
        // This is different from object properties! This code is correct because we don't have
        // transitive data properties.
        OWLDataFactory factory=m_reasoner.getDataFactory();
        OWLIndividual individual=factory.getOWLAnonymousIndividual("fresh-individual");
        OWLDatatype anonymousConstantsDatatype=factory.getOWLDatatype(IRI.create("internal:anonymous-constants"));
        OWLTypedLiteral constant=factory.getOWLTypedLiteral("internal:fresh-constant",anonymousConstantsDatatype);
        OWLDataProperty subproperty=factory.getOWLDataProperty(IRI.create((subrole).getIRI()));
        OWLDataProperty superproperty=factory.getOWLDataProperty(IRI.create((superrole).getIRI()));
        OWLDataProperty negatedSuperproperty=factory.getOWLDataProperty(IRI.create("internal:negated-superproperty"));
        OWLAxiom subpropertyAssertion=factory.getOWLDataPropertyAssertionAxiom(subproperty,individual,constant);
        OWLAxiom negatedSuperpropertyAssertion=factory.getOWLDataPropertyAssertionAxiom(negatedSuperproperty,individual,constant);
        OWLAxiom superpropertyAxiomatization=factory.getOWLDisjointDataPropertiesAxiom(superproperty,negatedSuperproperty);
        Tableau tableau=m_reasoner.getTableau(subpropertyAssertion,negatedSuperpropertyAssertion,superpropertyAxiomatization);
        return !tableau.isSatisfiable(true,null,null,null,null,null,ReasoningTaskDescription.isRoleSubsumedBy(subrole,superrole,false));
    }
    protected RoleInfo getRoleInfo(AtomicRole role) {
        RoleInfo result=m_roleInfos.get(role);
        if (result==null) {
            result=new RoleInfo(role);
            m_roleInfos.put(role,result);
        }
        return result;
    }

    protected static final class RoleInfo {
        protected final AtomicRole m_forRole;
        protected Boolean m_isSatisfiable;
        protected Set<AtomicRole> m_knownSubsumers;
        protected Set<AtomicRole> m_knownNonsubsumers;

        public RoleInfo(AtomicRole role) {
            m_forRole=role;
        }
        public boolean isKnownSubsumer(AtomicRole potentialSubsumer) {
            return m_knownSubsumers!=null && m_knownSubsumers.contains(potentialSubsumer);
        }
        public void addKnownSubsumer(AtomicRole role) {
            if (m_knownSubsumers==null)
                m_knownSubsumers=new HashSet<AtomicRole>();
            m_knownSubsumers.add(role);
        }
        public boolean isKnownNonsubsumer(AtomicRole potentialSubsumer) {
            return m_knownNonsubsumers!=null && m_knownNonsubsumers.contains(potentialSubsumer);
        }
        public void addKnownNonsubsumer(AtomicRole role) {
            if (m_knownNonsubsumers==null)
                m_knownNonsubsumers=new HashSet<AtomicRole>();
            m_knownNonsubsumers.add(role);
        }
    }
}
