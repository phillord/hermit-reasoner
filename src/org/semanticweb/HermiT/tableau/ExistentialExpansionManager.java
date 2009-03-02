// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.AtLeastAbstractRoleConcept;
import org.semanticweb.HermiT.model.AtLeastConcreteRoleConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.Role;

/**
 * Manages the expansion of at least restrictions in a tableau.
 */
public final class ExistentialExpansionManager implements Serializable {
    private static final long serialVersionUID=4794168582297181623L;

    public static enum SatType { NOT_SATISFIED,PERMANENTLY_SATISFIED,CURRENTLY_SATISFIED };

    private final Tableau m_tableau;
    private final ExtensionManager m_extensionManager;

    /**
     * Table of existentials which have already been expanded.
     * It is used in backtracking to determine what existentials need to be added back to nodes.
     */
    private final TupleTable m_expandedExistentials;
    private final Object[] m_auxiliaryTuple;
    private final List<Node> m_auxiliaryNodes1;
    private final List<Node> m_auxiliaryNodes2;
    private final ExtensionTable.Retrieval m_ternaryExtensionTableSearch01Bound;
    private final ExtensionTable.Retrieval m_ternaryExtensionTableSearch02Bound;
    private final Map<Role,Role[]> m_functionalRoles;
    private final UnionDependencySet m_binaryUnionDependencySet;
    private int[] m_indicesByBranchingPoint;

    public ExistentialExpansionManager(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=m_tableau.getExtensionManager();
        m_expandedExistentials=new TupleTable(2);
        m_auxiliaryTuple=new Object[2];
        m_auxiliaryNodes1=new ArrayList<Node>();
        m_auxiliaryNodes2=new ArrayList<Node>();
        m_ternaryExtensionTableSearch01Bound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
        m_ternaryExtensionTableSearch02Bound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.TOTAL);
        m_functionalRoles=buildFunctionalRoles();
        m_binaryUnionDependencySet=new UnionDependencySet(2);
        m_indicesByBranchingPoint=new int[2];
    }
    protected Map<Role,Role[]> buildFunctionalRoles() {
        Set<Role> functionalRoles=new HashSet<Role>();
        ObjectHierarchy<Role> roleHierarchy=new ObjectHierarchy<Role>();
        for (DLClause dlClause : m_tableau.getDLOntology().getDLClauses()) {
            if (dlClause.isRoleInclusion()) {
                AtomicRole subrole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicRole superrole=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                roleHierarchy.addInclusion(subrole,superrole);
                roleHierarchy.addInclusion(subrole.getInverse(),superrole.getInverse());
            }
            else if (dlClause.isRoleInverseInclusion()) {
                AtomicRole subrole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicRole superrole=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                roleHierarchy.addInclusion(subrole,superrole.getInverse());
                roleHierarchy.addInclusion(subrole.getInverse(),superrole);
            }
            else if (dlClause.isFunctionalityAxiom()) {
                AtomicRole atomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(atomicRole);
                roleHierarchy.addInclusion(atomicRole,atomicRole);
                roleHierarchy.addInclusion(atomicRole.getInverse(),atomicRole.getInverse());
            }
            else if (dlClause.isGuardedFunctionalityAxiom()) {
                AtomicRole atomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(atomicRole);
                roleHierarchy.addInclusion(atomicRole,atomicRole);
                roleHierarchy.addInclusion(atomicRole.getInverse(),atomicRole.getInverse());
            }
            else if (dlClause.isInverseFunctionalityAxiom()) {
                AtomicRole atomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(atomicRole.getInverse());
                roleHierarchy.addInclusion(atomicRole,atomicRole);
                roleHierarchy.addInclusion(atomicRole.getInverse(),atomicRole.getInverse());
            }
            else if (dlClause.isGuardedInverseFunctionalityAxiom()) {
                AtomicRole atomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(atomicRole.getInverse());
                roleHierarchy.addInclusion(atomicRole,atomicRole);
                roleHierarchy.addInclusion(atomicRole.getInverse(),atomicRole.getInverse());
            }
        }
        Map<Role,Role[]> result=new HashMap<Role,Role[]>();
        for (Role role : roleHierarchy.getAllObjects()) {
            Set<Role> relevantRoles=new HashSet<Role>();
            Set<Role> allSuperroles=roleHierarchy.getAllSuperobjects(role);
            for (Role abstractSuperrole : allSuperroles)
                if (functionalRoles.contains(abstractSuperrole))
                    relevantRoles.addAll(roleHierarchy.getAllSubobjects(abstractSuperrole));
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
    public SatType isSatisfied(AtLeastAbstractRoleConcept atLeastAbstractConcept,Node forNode) {
        int cardinality=atLeastAbstractConcept.getNumber();
        if (cardinality<=0)
            return SatType.PERMANENTLY_SATISFIED;
        Role onRole=atLeastAbstractConcept.getOnRole();
        LiteralConcept toConcept=atLeastAbstractConcept.getToConcept();
        ExtensionTable.Retrieval retrieval;
        int toNodeIndex;
        if (onRole instanceof AtomicRole) {
            retrieval=m_ternaryExtensionTableSearch01Bound;
            retrieval.getBindingsBuffer()[0]=onRole;
            retrieval.getBindingsBuffer()[1]=forNode;
            toNodeIndex=2;
        }
        else {
            retrieval=m_ternaryExtensionTableSearch02Bound;
            retrieval.getBindingsBuffer()[0]=((InverseRole)onRole).getInverseOf();
            retrieval.getBindingsBuffer()[2]=forNode;
            toNodeIndex=1;
        }
        if (cardinality==1) {
            retrieval.open();
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                Node toNode=(Node)tupleBuffer[toNodeIndex];
                if (m_extensionManager.containsConceptAssertion(toConcept,toNode) && !toNode.isIndirectlyBlocked()) {
                    if (forNode.getParent()==toNode || toNode.getParent()==forNode)
                        return SatType.PERMANENTLY_SATISFIED;
                    else
                        return SatType.CURRENTLY_SATISFIED;
                }
                retrieval.next();
            }
            return SatType.NOT_SATISFIED;
        }
        else {
            m_auxiliaryNodes1.clear();
            retrieval.open();
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            boolean permanent=true;
            while (!retrieval.afterLast()) {
                Node toNode=(Node)tupleBuffer[toNodeIndex];
                if (m_extensionManager.containsConceptAssertion(toConcept,toNode) && !toNode.isIndirectlyBlocked()) {
                    if (forNode.getParent()!=toNode && toNode.getParent()!=forNode)
                        permanent=false;
                    m_auxiliaryNodes1.add(toNode);
                }
                retrieval.next();
            }
            if (m_auxiliaryNodes1.size()>=cardinality) {
                m_auxiliaryNodes2.clear();
                if (containsSubsetOfNUnequalNodes(forNode,m_auxiliaryNodes1,0,m_auxiliaryNodes2,cardinality))
                    return permanent ? SatType.PERMANENTLY_SATISFIED : SatType.CURRENTLY_SATISFIED;
            }
            return SatType.NOT_SATISFIED;
        }
    }
    protected boolean containsSubsetOfNUnequalNodes(Node forNode,List<Node> nodes,int startAt,List<Node> selectedNodes,int cardinality) {
        if (selectedNodes.size()==cardinality) {
            // Check the condition on safe successors (condition 3.2 of the \geq-rule)
            if (forNode.getNodeType()!=NodeType.TREE_NODE)
                for (int index=0;index<selectedNodes.size();index++)
                    if (selectedNodes.get(index).isIndirectlyBlocked())
                        return false;
            return true;
        }
        else {
            outer: for (int index=startAt;index<nodes.size();index++) {
                Node node=nodes.get(index);
                for (int selectedNodeIndex=0;selectedNodeIndex<selectedNodes.size();selectedNodeIndex++) {
                    Node selectedNode=selectedNodes.get(selectedNodeIndex);
                    if (!m_extensionManager.containsAssertion(Inequality.INSTANCE,node,selectedNode) && !m_extensionManager.containsAssertion(Inequality.INSTANCE,selectedNode,node))
                        continue outer;
                }
                selectedNodes.add(node);
                if (containsSubsetOfNUnequalNodes(forNode,nodes,index+1,selectedNodes,cardinality))
                    return true;
                selectedNodes.remove(selectedNodes.size()-1);
            }
            return false;
        }
    }
    /**
     * Creates a new node in the tableau if at least concept that caused the expansion is for cardinality 1. If it is not of cardinality 1 and the role in the at most concept is a functional role, it sets a clash in the extension manager.
     * 
     * @return true if the at least cardinality is 1 (causes an expansion) or it is greater than one but the role is functional (causes a clash) and false otherwise.
     */
    public boolean tryFunctionalExpansion(AtLeastAbstractRoleConcept atLeastAbstractConcept,Node forNode) {
        if (atLeastAbstractConcept.getNumber()==1) {
            if (getFunctionalExpansionNode(atLeastAbstractConcept.getOnRole(),forNode,m_auxiliaryTuple)) {
                if (m_tableau.m_tableauMonitor!=null)
                    m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastAbstractConcept,forNode);
                Node functionalityNode=(Node)m_auxiliaryTuple[0];
                m_binaryUnionDependencySet.m_dependencySets[0]=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractConcept,forNode);
                m_binaryUnionDependencySet.m_dependencySets[1]=(DependencySet)m_auxiliaryTuple[1];
                m_extensionManager.addRoleAssertion(atLeastAbstractConcept.getOnRole(),forNode,functionalityNode,m_binaryUnionDependencySet);
                m_extensionManager.addConceptAssertion(atLeastAbstractConcept.getToConcept(),functionalityNode,m_binaryUnionDependencySet);
                if (m_tableau.m_tableauMonitor!=null)
                    m_tableau.m_tableauMonitor.existentialExpansionFinished(atLeastAbstractConcept,forNode);
                return true;
            }
        }
        else if (atLeastAbstractConcept.getNumber()>1 && m_functionalRoles.containsKey(atLeastAbstractConcept.getOnRole())) {
            if (m_tableau.m_tableauMonitor!=null)
                m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastAbstractConcept,forNode);
            DependencySet existentialDependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractConcept,forNode);
            m_extensionManager.setClash(existentialDependencySet);
            if (m_tableau.m_tableauMonitor!=null) {
                m_tableau.m_tableauMonitor.clashDetected(new Object[] { atLeastAbstractConcept,forNode });
                m_tableau.m_tableauMonitor.existentialExpansionFinished(atLeastAbstractConcept,forNode);
            }
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
    /**
     * Performs a normal expansion of the tableau (ABox) for an at least concept.
     */
    public void doNormalExpansion(AtLeastAbstractRoleConcept atLeastAbstractConcept,Node forNode) {
        if (m_tableau.m_tableauMonitor!=null)
            m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastAbstractConcept,forNode);
        DependencySet existentialDependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractConcept,forNode);
        int cardinality=atLeastAbstractConcept.getNumber();
        if (cardinality==1) {
            Node newNode=m_tableau.createNewTreeNode(existentialDependencySet,forNode);
            m_extensionManager.addRoleAssertion(atLeastAbstractConcept.getOnRole(),forNode,newNode,existentialDependencySet);
            m_extensionManager.addConceptAssertion(atLeastAbstractConcept.getToConcept(),newNode,existentialDependencySet);
        }
        else {
            m_auxiliaryNodes1.clear();
            for (int index=0;index<cardinality;index++) {
                Node newNode=m_tableau.createNewTreeNode(existentialDependencySet,forNode);
                m_extensionManager.addRoleAssertion(atLeastAbstractConcept.getOnRole(),forNode,newNode,existentialDependencySet);
                m_extensionManager.addConceptAssertion(atLeastAbstractConcept.getToConcept(),newNode,existentialDependencySet);
                m_auxiliaryNodes1.add(newNode);
            }
            for (int outerIndex=0;outerIndex<cardinality;outerIndex++) {
                Node outerNode=m_auxiliaryNodes1.get(outerIndex);
                for (int innerIndex=outerIndex+1;innerIndex<cardinality;innerIndex++)
                    m_extensionManager.addAssertion(Inequality.INSTANCE,outerNode,m_auxiliaryNodes1.get(innerIndex),existentialDependencySet);
            }
            m_auxiliaryNodes1.clear();
        }
        if (m_tableau.m_tableauMonitor!=null)
            m_tableau.m_tableauMonitor.existentialExpansionFinished(atLeastAbstractConcept,forNode);
    }
    /**
     * Expands an at least concept by first trying a functional expansion (if the cardinality is 1 or the role is functional) and a normal expansion otherwise.
     * 
     * @param atLeastAbstractConcept
     * @param forNode
     */
    public void expand(AtLeastAbstractRoleConcept atLeastAbstractConcept,Node forNode) {
        if (!tryFunctionalExpansion(atLeastAbstractConcept,forNode))
            doNormalExpansion(atLeastAbstractConcept,forNode);
    }
    public void expand(AtLeastConcreteRoleConcept atLeastConcreteRoleConcept,Node forNode) {
        if (m_tableau.m_tableauMonitor!=null)
            m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastConcreteRoleConcept,forNode);
        DependencySet existentialDependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastConcreteRoleConcept,forNode);
        int cardinality=atLeastConcreteRoleConcept.getNumber();
        if (cardinality==1) {
            Node newNode=m_tableau.createNewConcreteNode(existentialDependencySet,forNode);
            m_extensionManager.addRoleAssertion(atLeastConcreteRoleConcept.getOnAtomicConcreteRole(),forNode,newNode,existentialDependencySet);
            m_extensionManager.addAssertion(atLeastConcreteRoleConcept.getToDataRange(),newNode,existentialDependencySet);
        }
        else {
            m_auxiliaryNodes1.clear();
            for (int index=0;index<cardinality;++index) {
                Node newNode=m_tableau.createNewConcreteNode(existentialDependencySet,forNode);
                m_extensionManager.addRoleAssertion(atLeastConcreteRoleConcept.getOnAtomicConcreteRole(),forNode,newNode,existentialDependencySet);
                m_extensionManager.addAssertion(atLeastConcreteRoleConcept.getToDataRange(),newNode,existentialDependencySet);
                m_auxiliaryNodes1.add(newNode);
            }
            for (int outerIndex=0;outerIndex<cardinality;++outerIndex) {
                Node outerNode=m_auxiliaryNodes1.get(outerIndex);
                for (int innerIndex=outerIndex+1;innerIndex<cardinality;++innerIndex)
                    m_extensionManager.addAssertion(Inequality.INSTANCE,outerNode,m_auxiliaryNodes1.get(innerIndex),existentialDependencySet);
            }
            m_auxiliaryNodes1.clear();
        }
        if (m_tableau.m_tableauMonitor!=null)
            m_tableau.m_tableauMonitor.existentialExpansionFinished(atLeastConcreteRoleConcept,forNode);
    }
}
