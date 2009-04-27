// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.DLClause;

/**
 * Manages the expansion of at least restrictions in a tableau.
 */
public final class ExistentialExpansionManager implements Serializable {
    private static final long serialVersionUID=4794168582297181623L;

    protected final Tableau m_tableau;
    protected final ExtensionManager m_extensionManager;
    protected final TupleTable m_expandedExistentials;
    protected final Object[] m_auxiliaryTuple;
    protected final List<Node> m_auxiliaryNodes;
    protected final ExtensionTable.Retrieval m_ternaryExtensionTableSearch01Bound;
    protected final ExtensionTable.Retrieval m_ternaryExtensionTableSearch02Bound;
    protected final Map<Role,Role[]> m_functionalRoles;
    protected final UnionDependencySet m_binaryUnionDependencySet;
    protected int[] m_indicesByBranchingPoint;

    public ExistentialExpansionManager(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=m_tableau.getExtensionManager();
        m_expandedExistentials=new TupleTable(2);
        m_auxiliaryTuple=new Object[2];
        m_auxiliaryNodes=new ArrayList<Node>();
        m_ternaryExtensionTableSearch01Bound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
        m_ternaryExtensionTableSearch02Bound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.TOTAL);
        m_functionalRoles=buildFunctionalRoles();
        m_binaryUnionDependencySet=new UnionDependencySet(2);
        m_indicesByBranchingPoint=new int[2];
    }
    protected Map<Role,Role[]> buildFunctionalRoles() {
        Graph<Role> superRoleGraph=new Graph<Role>();
        Set<Role> functionalRoles=new HashSet<Role>();
        for (DLClause dlClause : m_tableau.getDLOntology().getDLClauses()) {
            if (dlClause.isRoleInclusion()) {
                AtomicRole subrole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicRole superrole=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                superRoleGraph.addEdge(subrole,superrole);
                superRoleGraph.addEdge(subrole.getInverse(),superrole.getInverse());
            }
            else if (dlClause.isRoleInverseInclusion()) {
                AtomicRole subrole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicRole superrole=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                superRoleGraph.addEdge(subrole,superrole.getInverse());
                superRoleGraph.addEdge(subrole.getInverse(),superrole);
            }
            else if (dlClause.isFunctionalityAxiom()) {
                AtomicRole atomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(atomicRole);
            }
            else if (dlClause.isGuardedFunctionalityAxiom()) {
                AtomicRole atomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(atomicRole);
            }
            else if (dlClause.isInverseFunctionalityAxiom()) {
                AtomicRole atomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(atomicRole.getInverse());
            }
            else if (dlClause.isGuardedInverseFunctionalityAxiom()) {
                AtomicRole atomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(atomicRole.getInverse());
            }
        }
        for (Role role : superRoleGraph.getElements()) {
            superRoleGraph.addEdge(role,role);
            superRoleGraph.addEdge(role.getInverse(),role.getInverse());
        }
        superRoleGraph.transitivelyClose();
        Graph<Role> subRoleGraph=superRoleGraph.getInverse();
        Map<Role,Role[]> result=new HashMap<Role,Role[]>();
        for (Role role : superRoleGraph.getElements()) {
            Set<Role> relevantRoles=new HashSet<Role>();
            Set<Role> allSuperroles=superRoleGraph.getSuccessors(role);
            for (Role superrole : allSuperroles)
                if (functionalRoles.contains(superrole))
                    relevantRoles.addAll(subRoleGraph.getSuccessors(superrole));
            if (!relevantRoles.isEmpty()) {
                Role[] relevantRolesArray=new Role[relevantRoles.size()];
                relevantRoles.toArray(relevantRolesArray);
                result.put(role,relevantRolesArray);
            }
        }
        return result;
    }
    public void markExistentialProcessed(ExistentialConcept existentialConcept,Node forNode) {
        m_auxiliaryTuple[0]=existentialConcept;
        m_auxiliaryTuple[1]=forNode;
        m_expandedExistentials.addTuple(m_auxiliaryTuple);
        forNode.removeFromUnprocessedExistentials(existentialConcept);
    }
    public void branchingPointPushed() {
        int start=m_tableau.getCurrentBranchingPoint().m_level;
        int requiredSize=start+1;
        if (requiredSize>m_indicesByBranchingPoint.length) {
            int newSize=m_indicesByBranchingPoint.length*3/2;
            while (requiredSize>newSize)
                newSize=newSize*3/2;
            int[] newIndicesByBranchingPoint=new int[newSize];
            System.arraycopy(m_indicesByBranchingPoint,0,newIndicesByBranchingPoint,0,m_indicesByBranchingPoint.length);
            m_indicesByBranchingPoint=newIndicesByBranchingPoint;
        }
        m_indicesByBranchingPoint[start]=m_expandedExistentials.getFirstFreeTupleIndex();
    }
    public void backtrack() {
        int newFirstFreeTupleIndex=m_indicesByBranchingPoint[m_tableau.getCurrentBranchingPoint().m_level];
        for (int tupleIndex=m_expandedExistentials.getFirstFreeTupleIndex()-1;tupleIndex>=newFirstFreeTupleIndex;--tupleIndex) {
            m_expandedExistentials.retrieveTuple(m_auxiliaryTuple,tupleIndex);
            ExistentialConcept existentialConcept=(ExistentialConcept)m_auxiliaryTuple[0];
            Node forNode=(Node)m_auxiliaryTuple[1];
            forNode.addToUnprocessedExistentials(existentialConcept);
        }
        m_expandedExistentials.truncate(newFirstFreeTupleIndex);
    }
    public void clear() {
        m_expandedExistentials.clear();
        m_auxiliaryTuple[0]=null;
        m_auxiliaryTuple[1]=null;
    }
    /**
     * Creates a new node in the tableau if at least concept that caused the expansion is for cardinality 1. If it is not of cardinality 1 and the role in the at most concept is a functional role, it sets a clash in the extension manager.
     * 
     * @return true if the at least cardinality is 1 (causes an expansion) or it is greater than one but the role is functional (causes a clash) and false otherwise.
     */
    public boolean tryFunctionalExpansion(AtLeastConcept atLeastConcept,Node forNode) {
        if (atLeastConcept.getNumber()==1) {
            if (getFunctionalExpansionNode(atLeastConcept.getOnRole(),forNode,m_auxiliaryTuple)) {
                if (m_tableau.m_tableauMonitor!=null)
                    m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastConcept,forNode);
                Node functionalityNode=(Node)m_auxiliaryTuple[0];
                m_binaryUnionDependencySet.m_dependencySets[0]=m_extensionManager.getConceptAssertionDependencySet(atLeastConcept,forNode);
                m_binaryUnionDependencySet.m_dependencySets[1]=(DependencySet)m_auxiliaryTuple[1];
                m_extensionManager.addRoleAssertion(atLeastConcept.getOnRole(),forNode,functionalityNode,m_binaryUnionDependencySet,true);
                m_extensionManager.addConceptAssertion(atLeastConcept.getToConcept(),functionalityNode,m_binaryUnionDependencySet,true);
                if (m_tableau.m_tableauMonitor!=null)
                    m_tableau.m_tableauMonitor.existentialExpansionFinished(atLeastConcept,forNode);
                return true;
            }
        }
        else if (atLeastConcept.getNumber()>1 && m_functionalRoles.containsKey(atLeastConcept.getOnRole())) {
            if (m_tableau.m_tableauMonitor!=null)
                m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastConcept,forNode);
            DependencySet existentialDependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastConcept,forNode);
            m_extensionManager.setClash(existentialDependencySet);
            if (m_tableau.m_tableauMonitor!=null)
                m_tableau.m_tableauMonitor.existentialExpansionFinished(atLeastConcept,forNode);
            return true;
        }
        return false;
    }
    protected boolean getFunctionalExpansionNode(Role role,Node forNode,Object[] result) {
        Role[] relevantRoles=m_functionalRoles.get(role);
        if (relevantRoles!=null) {
            for (Role relevantRole : relevantRoles) {
                ExtensionTable.Retrieval retrieval;
                int toNodeIndex;
                if (relevantRole instanceof AtomicRole) {
                    retrieval=m_ternaryExtensionTableSearch01Bound;
                    retrieval.getBindingsBuffer()[0]=relevantRole;
                    retrieval.getBindingsBuffer()[1]=forNode;
                    toNodeIndex=2;
                }
                else {
                    retrieval=m_ternaryExtensionTableSearch02Bound;
                    retrieval.getBindingsBuffer()[0]=((InverseRole)relevantRole).getInverseOf();
                    retrieval.getBindingsBuffer()[2]=forNode;
                    toNodeIndex=1;
                }
                retrieval.open();
                if (!retrieval.afterLast()) {
                    result[0]=retrieval.getTupleBuffer()[toNodeIndex];
                    result[1]=retrieval.getDependencySet();
                    return true;
                }
            }
        }
        return false;
    }
    public void doNormalExpansion(AtLeastConcept atLeastConcept,Node forNode) {
        if (m_tableau.m_tableauMonitor!=null)
            m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastConcept,forNode);
        DependencySet existentialDependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastConcept,forNode);
        int cardinality=atLeastConcept.getNumber();
        if (cardinality==1) {
            Node newNode;
            if (atLeastConcept.getToConcept() instanceof DataRange)
                newNode=m_tableau.createNewConcreteNode(existentialDependencySet,forNode);
            else
                newNode=m_tableau.createNewTreeNode(existentialDependencySet,forNode);
            m_extensionManager.addRoleAssertion(atLeastConcept.getOnRole(),forNode,newNode,existentialDependencySet,true);
            m_extensionManager.addConceptAssertion(atLeastConcept.getToConcept(),newNode,existentialDependencySet,true);
        }
        else {
            m_auxiliaryNodes.clear();
            for (int index=0;index<cardinality;index++) {
                Node newNode;
                if (atLeastConcept.getToConcept() instanceof DataRange)
                    newNode=m_tableau.createNewConcreteNode(existentialDependencySet,forNode);
                else
                    newNode=m_tableau.createNewTreeNode(existentialDependencySet,forNode);
                m_extensionManager.addRoleAssertion(atLeastConcept.getOnRole(),forNode,newNode,existentialDependencySet,true);
                m_extensionManager.addConceptAssertion(atLeastConcept.getToConcept(),newNode,existentialDependencySet,true);
                m_auxiliaryNodes.add(newNode);
            }
            for (int outerIndex=0;outerIndex<cardinality;outerIndex++) {
                Node outerNode=m_auxiliaryNodes.get(outerIndex);
                for (int innerIndex=outerIndex+1;innerIndex<cardinality;innerIndex++)
                    m_extensionManager.addAssertion(Inequality.INSTANCE,outerNode,m_auxiliaryNodes.get(innerIndex),existentialDependencySet,true);
            }
            m_auxiliaryNodes.clear();
        }
        if (m_tableau.m_tableauMonitor!=null)
            m_tableau.m_tableauMonitor.existentialExpansionFinished(atLeastConcept,forNode);
    }
    public void expand(AtLeastConcept atLeastConcept,Node forNode) {
        if (!tryFunctionalExpansion(atLeastConcept,forNode))
            doNormalExpansion(atLeastConcept,forNode);
    }
}
