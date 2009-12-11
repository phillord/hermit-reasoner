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
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.DLClause.ClauseType;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLTypedLiteral;

/**
 * A cache for role subsumption and role satisfiability tests. This class also maintains the set of known and possible subsumers
 * for a role. This information can be used to optimize classification.
 */
public class RoleSubsumptionCache implements Serializable,SubsumptionCache<Role> {
    private static final long serialVersionUID = 5380180660934814631L;
    
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
        Set<Role> allRoles;
        if (m_topRole==AtomicRole.TOP_OBJECT_ROLE) {
            allRoles=new HashSet<Role>(m_reasoner.getDLOntology().getAllAtomicObjectRoles());
            if (hasInverse)
                for (AtomicRole atomicRole : m_reasoner.getDLOntology().getAllAtomicObjectRoles())
                    allRoles.add(atomicRole.getInverse());
        }
        else
            allRoles=new HashSet<Role>(m_reasoner.getDLOntology().getAllAtomicDataRoles());
        Graph<Role> roleGraph=new Graph<Role>();
        for (DLClause dlClause : m_reasoner.getDLOntology().getDLClauses()) {
            if (dlClause.m_clauseType==ClauseType.OBJECT_PROPERTY_INCLUSION || dlClause.m_clauseType==ClauseType.DATA_PROPERTY_INCLUSION) {
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
        roleGraph.transitivelyClose();
        for (Role subrole : allRoles) {
            Set<Role> superroles=roleGraph.getSuccessors(subrole);
            for (Role superrole : superroles)
                getRoleInfo(subrole).addKnownSubsumer(superrole);
        }
    }
    public Set<Role> getAllKnownSubsumers(Role role) {
        boolean isSatisfiable=isSatisfiable(role,false,m_reasoner.getTableau());
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
        return isSatisfiable(role,true,m_reasoner.getTableau());
    }
    protected boolean isSatisfiable(Role role,boolean updatePossibleSubsumers,Tableau tableau) {
        if (m_bottomRole.equals(role))
            return false;
        RoleInfo roleInfo=getRoleInfo(role);
        if (roleInfo.m_isSatisfiable==null) {
            boolean isSatisfiable=tableau.isSatisfiable(role,m_topRole==AtomicRole.TOP_DATA_ROLE);
            roleInfo.m_isSatisfiable=(isSatisfiable ? Boolean.TRUE : Boolean.FALSE);
            if (isSatisfiable) {
                updateKnownSubsumers(role,tableau);
                if (updatePossibleSubsumers)
                    updatePossibleSubsumers(tableau);
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
            return !isSatisfiable(subrole,true,m_reasoner.getTableau());
        if (subroleInfo.isKnownSubsumer(superrole))
            return true;
        else if (subroleInfo.isKnownNotSubsumer(superrole))
            return false;
        // Perform the actual satisfiability test
        if (!m_reasoner.getTableau().isDeterministic() || m_topRole==AtomicRole.TOP_DATA_ROLE) {
            if (m_topRole==AtomicRole.TOP_OBJECT_ROLE) {
                // object properties
                return isSubObjectRoleOf(subroleInfo,superrole);
            } else {
                // data properties
                return isSubDataRoleOf(subroleInfo,superrole);
            }
        }
        else {
            isSatisfiable(subrole,true,m_reasoner.getTableau());
            assert subroleInfo.m_allSubsumersKnown;
            return subroleInfo.isKnownSubsumer(superrole);
        }
    }
    protected boolean isSubObjectRoleOf(RoleInfo subroleInfo,Role superrole) {
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
    protected boolean isSubDataRoleOf(RoleInfo subroleInfo,Role superrole) {
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
        protected Role m_forRole; // just for debugging
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
