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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Role;

/**
 * A cache for role subsumption and role satisfiability tests. This class also maintains the set of known and possible subsumers for a role. This information can be used to optimize classification.
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
    protected abstract boolean doSubsumptionCheck(Role subrole,Role superrole);
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
        protected Set<Role> m_knownNonsubsumers;

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
        public boolean isKnownNonsubsumer(Role potentialSubsumer) {
            return m_knownNonsubsumers!=null && m_knownNonsubsumers.contains(potentialSubsumer);
        }
        public void addKnownNonsubsumer(Role role) {
            if (m_knownNonsubsumers==null)
                m_knownNonsubsumers=new HashSet<Role>();
            m_knownNonsubsumers.add(role);
        }
    }
}
