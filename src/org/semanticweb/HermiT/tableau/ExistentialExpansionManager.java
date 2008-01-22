package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.HermiT.model.*;

public final class ExistentialExpansionManager implements Serializable {
    private static final long serialVersionUID=4794168582297181623L;

    protected final Tableau m_tableau;
    protected final ExtensionManager m_extensionManager;
    protected final TupleTable m_tupleTable;
    protected final Object[] m_auxiliaryTuple;
    protected final List<Node> m_auxiliaryNodes1;
    protected final List<Node> m_auxiliaryNodes2;
    protected final ExtensionTable.Retrieval m_ternaryExtensionTableSearch01Bound;
    protected final ExtensionTable.Retrieval m_ternaryExtensionTableSearch02Bound;
    protected final Map<AbstractRole,AbstractRole[]> m_functionalAbstractRoles;
    protected int[] m_indicesByBranchingPoint;
    
    public ExistentialExpansionManager(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=m_tableau.getExtensionManager();
        m_tupleTable=new TupleTable(2);
        m_auxiliaryTuple=new Object[2];
        m_auxiliaryNodes1=new ArrayList<Node>();
        m_auxiliaryNodes2=new ArrayList<Node>();
        m_ternaryExtensionTableSearch01Bound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
        m_ternaryExtensionTableSearch02Bound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.TOTAL);
        m_functionalAbstractRoles=buildFunctionalRoles();
        m_indicesByBranchingPoint=new int[2];
    }
    public void markExistentialProcessed(ExistentialConcept existentialConcept,Node forNode) {
        m_auxiliaryTuple[0]=existentialConcept;
        m_auxiliaryTuple[1]=forNode;
        m_tupleTable.addTuple(m_auxiliaryTuple);
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
        m_indicesByBranchingPoint[start]=m_tupleTable.getFirstFreeTupleIndex();
    }
    public void backtrack() {
        int newFirstFreeTupleIndex=m_indicesByBranchingPoint[m_tableau.getCurrentBranchingPoint().m_level];
        for (int tupleIndex=m_tupleTable.getFirstFreeTupleIndex()-1;tupleIndex>=newFirstFreeTupleIndex;--tupleIndex) {
            m_tupleTable.retrieveTuple(m_auxiliaryTuple,tupleIndex);
            ExistentialConcept existentialConcept=(ExistentialConcept)m_auxiliaryTuple[0];
            Node forNode=(Node)m_auxiliaryTuple[1];
            forNode.addToUnprocessedExistentials(existentialConcept);
        }
        m_tupleTable.truncate(newFirstFreeTupleIndex);
    }
    public void clear() {
        m_tupleTable.clear();
        m_indicesByBranchingPoint[m_tableau.getCurrentBranchingPoint().m_level]=m_tupleTable.getFirstFreeTupleIndex();
        m_auxiliaryTuple[0]=null;
        m_auxiliaryTuple[1]=null;
    }
    public boolean isSatisfied(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node forNode) {
        int cardinality=atLeastAbstractRoleConcept.getNumber();
        if (cardinality<=0)
            return true;
        AbstractRole onAbstractRole=atLeastAbstractRoleConcept.getOnAbstractRole();
        LiteralConcept toConcept=atLeastAbstractRoleConcept.getToConcept();
        ExtensionTable.Retrieval retrieval;
        int toNodeIndex;
        if (onAbstractRole instanceof AtomicAbstractRole) {
            retrieval=m_ternaryExtensionTableSearch01Bound;
            retrieval.getBindingsBuffer()[0]=onAbstractRole;
            retrieval.getBindingsBuffer()[1]=forNode;
            toNodeIndex=2;
        }
        else {
            retrieval=m_ternaryExtensionTableSearch02Bound;
            retrieval.getBindingsBuffer()[0]=((InverseAbstractRole)onAbstractRole).getInverseOf();
            retrieval.getBindingsBuffer()[2]=forNode;
            toNodeIndex=1;
        }
        if (cardinality==1) {
            retrieval.open();
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                Node toNode=(Node)tupleBuffer[toNodeIndex];
                if (m_extensionManager.containsConceptAssertion(toConcept,toNode) && (forNode.getNodeType()==NodeType.TREE_NODE || !toNode.isIndirectlyBlocked()))
                    return true;
                retrieval.next();
            }
            return false;
        }
        else {
            m_auxiliaryNodes1.clear();
            retrieval.open();
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                Node toNode=(Node)tupleBuffer[toNodeIndex];
                if (m_extensionManager.containsConceptAssertion(toConcept,toNode))
                    m_auxiliaryNodes1.add(toNode);
                retrieval.next();
            }
            if (m_auxiliaryNodes1.size()>=cardinality) {
                m_auxiliaryNodes2.clear();
                return containsSubsetOfNUnequalNodes(forNode,m_auxiliaryNodes1,0,m_auxiliaryNodes2,cardinality);
            }
            else
                return false;
        }
    }
    protected boolean containsSubsetOfNUnequalNodes(Node forNode,List<Node> nodes,int startAt,List<Node> selectedNodes,int cardinality) {
        if (selectedNodes.size()==cardinality) {
            // Check the condition on safe successors (condition 3.2 of the \geq-rule)
            if (forNode.getNodeType()!=NodeType.TREE_NODE) {
                for (int index=0;index<selectedNodes.size();index++)
                    if (selectedNodes.get(index).isIndirectlyBlocked())
                        return false;
            }
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
    protected boolean getFunctionalExpansionNode(AbstractRole abstractRole,Node forNode,Object[] result) {
        AbstractRole[] relevantAbstractRoles=m_functionalAbstractRoles.get(abstractRole);
        if (relevantAbstractRoles!=null) {
            for (AbstractRole relevantAbstractRole : relevantAbstractRoles) {
                ExtensionTable.Retrieval retrieval;
                int toNodeIndex;
                if (relevantAbstractRole instanceof AtomicAbstractRole) {
                    retrieval=m_ternaryExtensionTableSearch01Bound;
                    retrieval.getBindingsBuffer()[0]=relevantAbstractRole;
                    retrieval.getBindingsBuffer()[1]=forNode;
                    toNodeIndex=2;
                }
                else {
                    retrieval=m_ternaryExtensionTableSearch02Bound;
                    retrieval.getBindingsBuffer()[0]=((InverseAbstractRole)relevantAbstractRole).getInverseOf();
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
    public boolean tryFunctionalExpansion(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node forNode) {
        if (atLeastAbstractRoleConcept.getNumber()==1) {
            if (getFunctionalExpansionNode(atLeastAbstractRoleConcept.getOnAbstractRole(),forNode,m_auxiliaryTuple)) {
                if (m_tableau.m_tableauMonitor!=null)
                    m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastAbstractRoleConcept,forNode);
                Node functionalityNode=(Node)m_auxiliaryTuple[0];
                DependencySet roleAssertionDependencySet=(DependencySet)m_auxiliaryTuple[1];
                DependencySet existentialDependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractRoleConcept,forNode);
                DependencySet unionDependencySet=m_tableau.m_dependencySetFactory.unionWith(existentialDependencySet,roleAssertionDependencySet);
                if (functionalityNode.isGloballyUnique()) {
                    m_extensionManager.setClash(unionDependencySet);
                    if (m_tableau.m_tableauMonitor!=null) {
                        Object[] roleTuple=new Object[3];
                        if (atLeastAbstractRoleConcept.getOnAbstractRole() instanceof AtomicAbstractRole) {
                            roleTuple[0]=atLeastAbstractRoleConcept.getOnAbstractRole();
                            roleTuple[1]=forNode;
                            roleTuple[1]=functionalityNode;
                        }
                        else {
                            roleTuple[0]=((InverseAbstractRole)atLeastAbstractRoleConcept.getOnAbstractRole()).getInverseOf();
                            roleTuple[1]=functionalityNode;
                            roleTuple[1]=forNode;
                        }
                        m_tableau.m_tableauMonitor.clashDetected(new Object[] { atLeastAbstractRoleConcept,forNode },roleTuple);
                    }
                }
                else {
                    m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),forNode,functionalityNode,unionDependencySet);
                    m_extensionManager.addConceptAssertion(atLeastAbstractRoleConcept.getToConcept(),functionalityNode,unionDependencySet);
                }
                if (m_tableau.m_tableauMonitor!=null)
                    m_tableau.m_tableauMonitor.existentialExpansionFinished(atLeastAbstractRoleConcept,forNode);
                return true;
            }
        }
        else if (atLeastAbstractRoleConcept.getNumber()>1 && m_functionalAbstractRoles.containsKey(atLeastAbstractRoleConcept.getOnAbstractRole())) {
            if (m_tableau.m_tableauMonitor!=null)
                m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastAbstractRoleConcept,forNode);
            DependencySet existentialDependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractRoleConcept,forNode);
            m_extensionManager.setClash(existentialDependencySet);
            if (m_tableau.m_tableauMonitor!=null) {
                m_tableau.m_tableauMonitor.clashDetected(new Object[] { atLeastAbstractRoleConcept,forNode });
                m_tableau.m_tableauMonitor.existentialExpansionFinished(atLeastAbstractRoleConcept,forNode);
            }
            return true;
        }
        return false;
    }
    public void doNormalExpansion(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node forNode) {
        if (m_tableau.m_tableauMonitor!=null)
            m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastAbstractRoleConcept,forNode);
        DependencySet existentialDependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractRoleConcept,forNode);
        int cardinality=atLeastAbstractRoleConcept.getNumber();
        if (cardinality==1) {
            Node newNode=m_tableau.createNewTreeNode(forNode,existentialDependencySet);
            m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),forNode,newNode,existentialDependencySet);
            m_extensionManager.addConceptAssertion(atLeastAbstractRoleConcept.getToConcept(),newNode,existentialDependencySet);
        }
        else {
            m_auxiliaryNodes1.clear();
            for (int index=0;index<cardinality;index++) {
                Node newNode=m_tableau.createNewTreeNode(forNode,existentialDependencySet);
                m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),forNode,newNode,existentialDependencySet);
                m_extensionManager.addConceptAssertion(atLeastAbstractRoleConcept.getToConcept(),newNode,existentialDependencySet);
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
            m_tableau.m_tableauMonitor.existentialExpansionFinished(atLeastAbstractRoleConcept,forNode);
    }
    public void expand(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node forNode) {
        if (!tryFunctionalExpansion(atLeastAbstractRoleConcept,forNode))
            doNormalExpansion(atLeastAbstractRoleConcept,forNode);
    }
    protected Map<AbstractRole,AbstractRole[]> buildFunctionalRoles() {
        Set<AbstractRole> functionalRoles=new HashSet<AbstractRole>();
        ObjectHierarchy<AbstractRole> roleHierarchy=new ObjectHierarchy<AbstractRole>();
        for (DLClause dlClause : m_tableau.getDLOntology().getDLClauses()) {
            if (dlClause.isRoleInclusion()) {
                AtomicAbstractRole subrole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicAbstractRole superrole=(AtomicAbstractRole)dlClause.getHeadAtom(0,0).getDLPredicate();
                roleHierarchy.addInclusion(subrole,superrole);
                roleHierarchy.addInclusion(subrole.getInverseRole(),superrole.getInverseRole());
            }
            else if (dlClause.isRoleInverseInclusion()) {
                AtomicAbstractRole subrole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicAbstractRole superrole=(AtomicAbstractRole)dlClause.getHeadAtom(0,0).getDLPredicate();
                roleHierarchy.addInclusion(subrole,superrole.getInverseRole());
                roleHierarchy.addInclusion(subrole.getInverseRole(),superrole);
            }
            else if (dlClause.isFunctionalityAxiom()) {
                AtomicAbstractRole atomicAbstractRole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(atomicAbstractRole);
                roleHierarchy.addInclusion(atomicAbstractRole,atomicAbstractRole);
                roleHierarchy.addInclusion(atomicAbstractRole.getInverseRole(),atomicAbstractRole.getInverseRole());
            }
            else if (dlClause.isInverseFunctionalityAxiom()) {
                AtomicAbstractRole atomicAbstractRole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(atomicAbstractRole.getInverseRole());
                roleHierarchy.addInclusion(atomicAbstractRole,atomicAbstractRole);
                roleHierarchy.addInclusion(atomicAbstractRole.getInverseRole(),atomicAbstractRole.getInverseRole());
            }
        }
        Map<AbstractRole,AbstractRole[]> result=new HashMap<AbstractRole,AbstractRole[]>();
        for (AbstractRole abstractRole : roleHierarchy.getAllObjects()) {
            Set<AbstractRole> relevantAbstractRoles=new HashSet<AbstractRole>();
            Set<AbstractRole> allSuperroles=roleHierarchy.getAllSuperobjects(abstractRole);
            for (AbstractRole abstractSuperrole : allSuperroles)
                if (functionalRoles.contains(abstractSuperrole))
                    relevantAbstractRoles.addAll(roleHierarchy.getAllSubobjects(abstractSuperrole));
            if (!relevantAbstractRoles.isEmpty()) {
                AbstractRole[] relevantAbstractRolesArray=new AbstractRole[relevantAbstractRoles.size()];
                relevantAbstractRoles.toArray(relevantAbstractRolesArray);
                result.put(abstractRole,relevantAbstractRolesArray);
            }
        }
        return result;
    }
}
