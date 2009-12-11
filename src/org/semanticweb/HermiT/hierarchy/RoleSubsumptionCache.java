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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

/**
 * A cache for role subsumption and role satisfiability tests. This class also maintains the set of known and possible subsumers
 * for a role. This information can be used to optimize classification.
 */
public abstract class RoleSubsumptionCache implements SubsumptionCache<Role> {
    protected final Reasoner m_reasoner;
    protected final Map<Role,RoleInfo> m_roleInfos;
    protected final boolean m_hasInverse;
    protected final AtomicRole m_bottomRole;
    protected final AtomicRole m_topRole;

    public RoleSubsumptionCache(Reasoner reasoner,boolean hasInverse,AtomicRole bottomRole,AtomicRole topRole) {
        m_reasoner=reasoner;
        m_roleInfos=new HashMap<Role,RoleInfo>();
        m_hasInverse=hasInverse;
        m_bottomRole=bottomRole;
        m_topRole=topRole;
        Set<Role> allRoles=new HashSet<Role>();
        Graph<Role> roleGraph=new Graph<Role>();
        loadRoleGraph(allRoles,roleGraph);
        roleGraph.transitivelyClose();
        for (Role subrole : allRoles) {
            Set<Role> superroles=roleGraph.getSuccessors(subrole);
            for (Role superrole : superroles)
                getRoleInfo(subrole).addKnownSubsumer(superrole);
        }

    }
    protected abstract void loadRoleGraph(Set<Role> allRoles,Graph<Role> roleGraph);
    public Set<Role> getAllKnownSubsumers(Role role) {
        boolean isSatisfiable=isSatisfiable(role,false);
        RoleInfo roleInfo=m_roleInfos.get(role);
        if (isSatisfiable) {
            if (!roleInfo.m_allSubsumersKnown)
                throw new IllegalStateException("Not all subsumers are known for '"+role.toString()+"'.");
            return roleInfo.m_knownSubsumers;
        }
        else
            return null;
    }
    public boolean isSatisfiable(Role role) {
        return isSatisfiable(role,true);
    }
    protected boolean isSatisfiable(Role role,boolean updatePossibleSubsumers) {
        if (m_bottomRole.equals(role))
            return false;
        RoleInfo roleInfo=getRoleInfo(role);
        if (roleInfo.m_isSatisfiable==null) {
            boolean isSatisfiable=doSatisfiabilityTest(role);
            roleInfo.m_isSatisfiable=(isSatisfiable ? Boolean.TRUE : Boolean.FALSE);
            if (isSatisfiable) {
                updateKnownSubsumers(role,m_reasoner.getTableau());
                if (updatePossibleSubsumers)
                    updatePossibleSubsumers(m_reasoner.getTableau());
            }
        }
        return roleInfo.m_isSatisfiable;
    }
    protected abstract boolean doSatisfiabilityTest(Role role);
    public boolean isSubsumedBy(Role subrole,Role superrole) {
        if (m_topRole.equals(superrole) || m_bottomRole.equals(subrole))
            return true;
        RoleInfo subroleInfo=getRoleInfo(subrole);
        if (Boolean.FALSE.equals(subroleInfo.m_isSatisfiable))
            return true;
        else if (m_bottomRole.equals(superrole) || (m_roleInfos.containsKey(superrole) && Boolean.FALSE.equals(m_roleInfos.get(superrole).m_isSatisfiable)))
            return !isSatisfiable(subrole,true);
        if (subroleInfo.isKnownSubsumer(superrole))
            return true;
        else if (subroleInfo.isKnownNotSubsumer(superrole))
            return false;
        // Perform the actual satisfiability test
        if (!m_reasoner.getTableau().isDeterministic() || m_topRole==AtomicRole.TOP_DATA_ROLE) {
            return doSubsumptionCheck(subroleInfo,superrole);
        }
        else {
            isSatisfiable(subrole,true);
            assert subroleInfo.m_allSubsumersKnown;
            return subroleInfo.isKnownSubsumer(superrole);
        }
    }
    protected abstract boolean doSubsumptionCheck(RoleInfo subroleInfo,Role superrole);
    protected void updateKnownSubsumers(Role subrole,Tableau tableau) {
        Node checkedNode0=tableau.getCheckedNode0().getCanonicalNode();
        Node checkedNode1=tableau.getCheckedNode1().getCanonicalNode();
        RoleInfo subroleInfo=getRoleInfo(subrole);
        subroleInfo.addKnownSubsumer(m_topRole);
        ExtensionTable.Retrieval retrieval=tableau.getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,true },ExtensionTable.View.TOTAL);
        retrieval.getBindingsBuffer()[1]=checkedNode0;
        retrieval.getBindingsBuffer()[2]=checkedNode1;
        retrieval.open();
        while (!retrieval.afterLast()) {
            Object role=retrieval.getTupleBuffer()[0];
            if (role instanceof AtomicRole && retrieval.getDependencySet().isEmpty())
                subroleInfo.addKnownSubsumer((AtomicRole)role);
            retrieval.next();
        }
        if (m_hasInverse) {
            retrieval.getBindingsBuffer()[1]=checkedNode1;
            retrieval.getBindingsBuffer()[2]=checkedNode0;
            retrieval.open();
            while (!retrieval.afterLast()) {
                Object role=retrieval.getTupleBuffer()[0];
                if (role instanceof AtomicRole && retrieval.getDependencySet().isEmpty())
                    subroleInfo.addKnownSubsumer(((AtomicRole)role).getInverse());
                retrieval.next();
            }
        }
        if (tableau.isCurrentModelDeterministic())
            subroleInfo.setAllSubsumersKnown();
    }
    protected void updatePossibleSubsumers(Tableau tableau) {
        ExtensionTable.Retrieval retrieval=tableau.getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,false,false },ExtensionTable.View.TOTAL);
        retrieval.open();
        Object[] tupleBuffer=retrieval.getTupleBuffer();
        while (!retrieval.afterLast()) {
            Object atomicRoleObject=tupleBuffer[0];
            if (atomicRoleObject instanceof AtomicRole) {
                AtomicRole atomicRole=(AtomicRole)atomicRoleObject;
                Node node0=(Node)tupleBuffer[1];
                Node node1=(Node)tupleBuffer[2];
                if (node0.isActive() && !node0.isBlocked() && node1.isActive() && !node1.isBlocked()) {
                    getRoleInfo(atomicRole).updatePossibleSubsumers(tableau,node0,node1,m_hasInverse);
                    if (m_hasInverse)
                        getRoleInfo(atomicRole.getInverse()).updatePossibleSubsumers(tableau,node1,node0,m_hasInverse);
                }
            }
            retrieval.next();
        }
    }
    protected RoleInfo getRoleInfo(Role role) {
        RoleInfo result=m_roleInfos.get(role);
        if (result==null) {
            result=new RoleInfo(role);
            m_roleInfos.put(role,result);
        }
        return result;
    }
    
    protected static final class RoleInfo {
        protected final Role m_forRole;
        protected Boolean m_isSatisfiable;
        protected Set<Role> m_knownSubsumers;
        protected Set<Role> m_possibleSubsumers;
        protected boolean m_allSubsumersKnown;
        
        public RoleInfo(Role role) {
            m_forRole=role;
        }
        public boolean isKnownSubsumer(Role potentialSubsumer) {
            return m_knownSubsumers!=null && m_knownSubsumers.contains(potentialSubsumer);
        }
        public void addKnownSubsumer(Role role) {
            if (m_knownSubsumers==null)
                m_knownSubsumers=new HashSet<Role>();
            m_knownSubsumers.add(role);
        }
        public void setAllSubsumersKnown() {
            m_allSubsumersKnown=true;
            m_possibleSubsumers=m_knownSubsumers;
        }
        public boolean isKnownNotSubsumer(Role potentialSubsumer) {
            return (!isKnownSubsumer(potentialSubsumer) && m_allSubsumersKnown) || (m_possibleSubsumers!=null && !m_possibleSubsumers.contains(potentialSubsumer));
        }
        public void updatePossibleSubsumers(Tableau tableau,Node node0,Node node1,boolean updateInverse) {
            if (!m_allSubsumersKnown) {
                if (m_possibleSubsumers==null) {
                    m_possibleSubsumers=new HashSet<Role>();
                    ExtensionTable.Retrieval retrieval=tableau.getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,true },ExtensionTable.View.TOTAL);
                    retrieval.getBindingsBuffer()[1]=node0;
                    retrieval.getBindingsBuffer()[2]=node1;
                    retrieval.open();
                    while (!retrieval.afterLast()) {
                        Object role=retrieval.getTupleBuffer()[0];
                        if (role instanceof AtomicRole)
                            m_possibleSubsumers.add((AtomicRole)role);
                        retrieval.next();
                    }
                    if (updateInverse) {
                        retrieval.getBindingsBuffer()[1]=node1;
                        retrieval.getBindingsBuffer()[2]=node0;
                        retrieval.open();
                        while (!retrieval.afterLast()) {
                            Object role=retrieval.getTupleBuffer()[0];
                            if (role instanceof AtomicRole)
                                m_possibleSubsumers.add(((AtomicRole)role).getInverse());
                            retrieval.next();
                        }
                    }
                }
                else {
                    Iterator<Role> iterator=m_possibleSubsumers.iterator();
                    while (iterator.hasNext()) {
                        Role role=iterator.next();
                        if (!tableau.getExtensionManager().containsRoleAssertion(role,node0,node1))
                            iterator.remove();
                        else if (updateInverse && !tableau.getExtensionManager().containsRoleAssertion(role.getInverse(),node1,node0))
                            iterator.remove();
                    }
                }
            }
        }
        public String toString() {
            String CRLF=System.getProperty("line.separator");
            String result=m_forRole+" ";
            if (m_isSatisfiable!=null) result+=(m_isSatisfiable?"sat":"unsat");
            if (m_allSubsumersKnown) result+="(all superroles known)";
            if (m_knownSubsumers!=null) {
                result+=(CRLF+"Known superroles: ");
                for (Role role : m_knownSubsumers) {
                    result+=(role+" ");
                }
            }
            if (m_possibleSubsumers!=null) {
                result+=(CRLF+"Possible superroles: ");
                for (Role role : m_possibleSubsumers) {
                    result+=(role+" ");
                }
            }
            return result;
        }
    }
}
