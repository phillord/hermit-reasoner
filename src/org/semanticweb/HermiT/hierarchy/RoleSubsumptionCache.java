// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

/**
 * A cache for role subsumption and role satisfiability tests. This class also maintains the set of known and possible subsumers
 * for a role. This information can be used to optimize classification.
 */
public class RoleSubsumptionCache implements Serializable {
    private static final long serialVersionUID = 5380180660934814631L;
    protected final Reasoner m_reasoner;
    protected final Map<Role,RoleInfo> m_roleInfos;
    protected final boolean m_hasInverse;
    protected final Set<Role> m_allRoles;
    protected final AtomicRole m_bottomRole;
    protected final AtomicRole m_topRole;

    public RoleSubsumptionCache(Reasoner reasoner,boolean hasInverse,Set<Role> allRoles,AtomicRole bottomRole,AtomicRole topRole) {
        m_reasoner=reasoner;
        m_roleInfos=new HashMap<Role,RoleInfo>();
        m_hasInverse=hasInverse;
        m_allRoles=allRoles;
        m_bottomRole=bottomRole;
        m_topRole=topRole;
        Graph<Role> roleGraph=new Graph<Role>();
        for (DLClause dlClause : m_reasoner.getDLOntology().getDLClauses()) {
            if (dlClause.isRoleInclusion()) {
                AtomicRole sub=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicRole sup=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                if (m_allRoles.contains(sub) && m_allRoles.contains(sup)) {
                    roleGraph.addEdge(sub,sup);
                    if (m_hasInverse)
                        roleGraph.addEdge(sub.getInverse(),sup.getInverse());
                }
            }
            else if (dlClause.isRoleInverseInclusion()) {
                AtomicRole sub=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicRole sup=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                if (m_allRoles.contains(sub) && m_allRoles.contains(sup)) {
                    roleGraph.addEdge(sub.getInverse(),sup);
                    roleGraph.addEdge(sub,sup.getInverse());
                }
            }
        }
        roleGraph.transitivelyClose();
        for (Role subrole : m_allRoles) {
            Set<Role> superroles=roleGraph.getSuccessors(subrole);
            for (Role superrole : superroles)
                getRoleInfo(subrole).addKnownSubsumer(superrole);
        }
    }
    public boolean canGetAllSubsumersEasily() {
        return m_reasoner.getTableau().isDeterministic();
    }
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
        if (AtomicConcept.NOTHING.equals(role))
            return false;
        RoleInfo roleInfo=getRoleInfo(role);
        if (roleInfo.m_isSatisfiable==null) {
            boolean isSatisfiable=m_reasoner.getTableau().isSatisfiable(role);
            roleInfo.m_isSatisfiable=(isSatisfiable ? Boolean.TRUE : Boolean.FALSE);
            if (isSatisfiable) {
                updateKnownSubsumers(role);
                if (updatePossibleSubsumers)
                    updatePossibleSubsumers();
            }
        }
        return roleInfo.m_isSatisfiable;
    }
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
        if (!m_reasoner.getTableau().isDeterministic()) {
//            boolean isSubsumedBy=m_reasoner.getTableau().isSubsumedBy(subrole,superrole);
            // FIX ME!
            boolean isSubsumedBy=false;
            if (m_reasoner.getTableau().getExtensionManager().containsClash() && m_reasoner.getTableau().getExtensionManager().getClashDependencySet().isEmpty())
                subroleInfo.m_isSatisfiable=Boolean.FALSE;
            else if (!isSubsumedBy) {
                subroleInfo.m_isSatisfiable=Boolean.TRUE;
                updateKnownSubsumers(subrole);
                updatePossibleSubsumers();
            }
            else
                subroleInfo.addKnownSubsumer(superrole);
            return isSubsumedBy;
        }
        else {
            isSatisfiable(subrole,true);
            assert subroleInfo.m_allSubsumersKnown;
            return subroleInfo.isKnownSubsumer(superrole);
        }
    }
    protected void updateKnownSubsumers(Role subrole) {
        Node checkedNode0=m_reasoner.getTableau().getCheckedNode0().getCanonicalNode();
        Node checkedNode1=m_reasoner.getTableau().getCheckedNode1().getCanonicalNode();
        RoleInfo subconceptInfo=getRoleInfo(subrole);
        subconceptInfo.addKnownSubsumer(m_topRole);
        ExtensionTable.Retrieval retrieval=m_reasoner.getTableau().getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,true },ExtensionTable.View.TOTAL);
        retrieval.getBindingsBuffer()[1]=checkedNode0;
        retrieval.getBindingsBuffer()[2]=checkedNode1;
        retrieval.open();
        while (!retrieval.afterLast()) {
            Object role=retrieval.getTupleBuffer()[0];
            if (role instanceof AtomicRole && retrieval.getDependencySet().isEmpty())
                subconceptInfo.addKnownSubsumer((AtomicRole)role);
            retrieval.next();
        }
        if (m_hasInverse) {
            retrieval.getBindingsBuffer()[1]=checkedNode1;
            retrieval.getBindingsBuffer()[2]=checkedNode0;
            retrieval.open();
            while (!retrieval.afterLast()) {
                Object role=retrieval.getTupleBuffer()[0];
                if (role instanceof AtomicRole && retrieval.getDependencySet().isEmpty())
                    subconceptInfo.addKnownSubsumer(((AtomicRole)role).getInverse());
                retrieval.next();
            }
        }
        if (m_reasoner.getTableau().isCurrentModelDeterministic())
            subconceptInfo.setAllSubsumersKnown();
    }
    protected void updatePossibleSubsumers() {
        ExtensionTable.Retrieval retrieval=m_reasoner.getTableau().getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,false,false },ExtensionTable.View.TOTAL);
        retrieval.open();
        Object[] tupleBuffer=retrieval.getTupleBuffer();
        while (!retrieval.afterLast()) {
            Object atomicRoleObject=tupleBuffer[0];
            if (atomicRoleObject instanceof AtomicRole) {
                AtomicRole atomicRole=(AtomicRole)atomicRoleObject;
                Node node0=(Node)tupleBuffer[1];
                Node node1=(Node)tupleBuffer[2];
                if (node0.isActive() && !node0.isBlocked() && node1.isActive() && !node1.isBlocked()) {
                    getRoleInfo(atomicRole).updatePossibleSubsumers(m_reasoner.getTableau(),node0,node1,m_hasInverse);
                    if (m_hasInverse)
                        getRoleInfo(atomicRole.getInverse()).updatePossibleSubsumers(m_reasoner.getTableau(),node1,node0,m_hasInverse);
                }
            }
            retrieval.next();
        }
    }
    protected RoleInfo getRoleInfo(Role role) {
        RoleInfo result=m_roleInfos.get(role);
        if (result==null) {
            result=new RoleInfo();
            m_roleInfos.put(role,result);
        }
        return result;
    }
    
    protected static final class RoleInfo {
        protected Boolean m_isSatisfiable;
        protected Set<Role> m_knownSubsumers;
        protected Set<Role> m_possibleSubsumers;
        protected boolean m_allSubsumersKnown;
        
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
        public void updatePossibleSubsumers(Tableau tableau,Node node0,Node node1,boolean hasInverse) {
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
                    if (hasInverse) {
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
                        else if (hasInverse && !tableau.getExtensionManager().containsRoleAssertion(role.getInverse(),node1,node0))
                            iterator.remove();
                    }
                }
            }
        }
    }
}
