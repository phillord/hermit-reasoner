// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.AbstractRole;
import org.semanticweb.HermiT.model.AtLeastAbstractRoleConcept;
import org.semanticweb.HermiT.model.AtLeastConcreteRoleConcept;
import org.semanticweb.HermiT.model.AtomicAbstractRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseAbstractRole;
import org.semanticweb.HermiT.model.LiteralConcept;

/**
 * Manages the expansion of at least restrictions in a tableau. 
 */
public final class ExistentialExpansionManager implements Serializable {
    private static final long serialVersionUID=4794168582297181623L;

    private final Tableau m_tableau;
    private final ExtensionManager m_extensionManager;
    
    /** Table of existentials which have already been expanded.
      * 
      * This is used in backtracking to determine what existentials need
      * to be added back to nodes.
     */
    private final TupleTable m_expandedExistentials;

    // Local caches to avoid allocating new objects:
    private final Object[] m_auxiliaryTuple;
    private final List<Node> m_auxiliaryNodes1;
    private final List<Node> m_auxiliaryNodes2;

    private final ExtensionTable.Retrieval m_ternaryExtensionTableSearch01Bound;
    private final ExtensionTable.Retrieval m_ternaryExtensionTableSearch02Bound;
    private final Map<AbstractRole,AbstractRole[]> m_functionalAbstractRoles;
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
        m_functionalAbstractRoles=buildFunctionalRoles();
        m_binaryUnionDependencySet=new UnionDependencySet(2);
        m_indicesByBranchingPoint=new int[2];
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

    public static enum SatType
        { NOT_SATISFIED, PERMANENTLY_SATISFIED, CURRENTLY_SATISFIED };
    public SatType isSatisfied(AtLeastAbstractRoleConcept
                                    atLeastAbstractRoleConcept,
                                Node forNode) {
        int cardinality = atLeastAbstractRoleConcept.getNumber();
        if (cardinality <= 0) {
            return SatType.PERMANENTLY_SATISFIED;
        }
        AbstractRole onAbstractRole
            = atLeastAbstractRoleConcept.getOnAbstractRole();
        LiteralConcept toConcept
            = atLeastAbstractRoleConcept.getToConcept();
        ExtensionTable.Retrieval retrieval;
        int toNodeIndex;
        if (onAbstractRole instanceof AtomicAbstractRole) {
            retrieval=m_ternaryExtensionTableSearch01Bound;
            retrieval.getBindingsBuffer()[0] = onAbstractRole;
            retrieval.getBindingsBuffer()[1] = forNode;
            toNodeIndex = 2;
        } else {
            retrieval = m_ternaryExtensionTableSearch02Bound;
            retrieval.getBindingsBuffer()[0]
                = ((InverseAbstractRole) onAbstractRole).getInverseOf();
            retrieval.getBindingsBuffer()[2] = forNode;
            toNodeIndex = 1;
        }
        if (cardinality == 1) {
            retrieval.open();
            Object[] tupleBuffer = retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                Node toNode = (Node) tupleBuffer[toNodeIndex];
                if (m_extensionManager.containsConceptAssertion
                        (toConcept,toNode) &&
                    !toNode.isIndirectlyBlocked()) {
                    if (forNode.getParent() == toNode ||
                        toNode.getParent() == forNode) {
                        return SatType.PERMANENTLY_SATISFIED;
                    } else {
                        return SatType.CURRENTLY_SATISFIED;
                    }
                }
                retrieval.next();
            }
            return SatType.NOT_SATISFIED;
        } else {
            m_auxiliaryNodes1.clear();
            retrieval.open();
            Object[] tupleBuffer = retrieval.getTupleBuffer();
            boolean permanent = true;
            while (!retrieval.afterLast()) {
                Node toNode = (Node) tupleBuffer[toNodeIndex];
                if (m_extensionManager.containsConceptAssertion
                        (toConcept,toNode) &&
                    !toNode.isIndirectlyBlocked()) {
                    if (forNode.getParent() != toNode &&
                        toNode.getParent() != forNode) {
                        permanent = false;
                    }
                    m_auxiliaryNodes1.add(toNode);
                }
                retrieval.next();
            }
            if (m_auxiliaryNodes1.size() >= cardinality) {
                m_auxiliaryNodes2.clear();
                if (containsSubsetOfNUnequalNodes(forNode, m_auxiliaryNodes1,
                                                    0, m_auxiliaryNodes2,
                                                    cardinality)) {
                    return permanent ? SatType.PERMANENTLY_SATISFIED
                                     : SatType.CURRENTLY_SATISFIED;
            }
        }
            return SatType.NOT_SATISFIED;
    }
    }
    
    private boolean containsSubsetOfNUnequalNodes(Node forNode,List<Node> nodes,int startAt,List<Node> selectedNodes,int cardinality) {
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
    private boolean getFunctionalExpansionNode(AbstractRole abstractRole,Node forNode,Object[] result) {
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
    /**
     * Creates a new node in the tableau if at least concept that caused the 
     * expansion is for cardinality 1. If it is not of cardinality 1 and the 
     * role in the at most concept is a functional role, it sets a clash in the 
     * extension manager. 
     * @return true if the at least cardinality is 1 (causes an expansion) or it 
     * is greater than one but the role is functional (causes a clash) and false 
     * otherwise.
     */
    public boolean tryFunctionalExpansion(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node forNode) {
        if (atLeastAbstractRoleConcept.getNumber()==1) {
            if (getFunctionalExpansionNode(atLeastAbstractRoleConcept.getOnAbstractRole(),forNode,m_auxiliaryTuple)) {
                if (m_tableau.m_tableauMonitor!=null)
                    m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastAbstractRoleConcept,forNode);
                Node functionalityNode=(Node)m_auxiliaryTuple[0];
                m_binaryUnionDependencySet.m_dependencySets[0]=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractRoleConcept,forNode);
                m_binaryUnionDependencySet.m_dependencySets[1]=(DependencySet)m_auxiliaryTuple[1];
                m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),forNode,functionalityNode,m_binaryUnionDependencySet);
                m_extensionManager.addConceptAssertion(atLeastAbstractRoleConcept.getToConcept(),functionalityNode,m_binaryUnionDependencySet);
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
    /**
     * Does a normal expansion of the tableau (ABox) for an at least concept. 
     * @param atLeastAbstractRoleConcept
     * @param forNode
     */
    public void doNormalExpansion(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node forNode) {
        if (m_tableau.m_tableauMonitor!=null)
            m_tableau.m_tableauMonitor.existentialExpansionStarted(atLeastAbstractRoleConcept,forNode);
        DependencySet existentialDependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractRoleConcept,forNode);
        int cardinality=atLeastAbstractRoleConcept.getNumber();
        if (cardinality==1) {
            Node newNode=m_tableau.createNewTreeNode(existentialDependencySet,forNode);
            m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),forNode,newNode,existentialDependencySet);
            m_extensionManager.addConceptAssertion(atLeastAbstractRoleConcept.getToConcept(),newNode,existentialDependencySet);
        }
        else {
            m_auxiliaryNodes1.clear();
            for (int index=0;index<cardinality;index++) {
                Node newNode=m_tableau.createNewTreeNode(existentialDependencySet,forNode);
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
    /**
     * Expands an at least concept by first trying a functional expansion (if 
     * the cardinality is 1 or the role is functional) and a normal expansion 
     * otherwise. 
     * @param atLeastAbstractRoleConcept
     * @param forNode
     */
    public void expand(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node forNode) {
        if (!tryFunctionalExpansion(atLeastAbstractRoleConcept,forNode))
            doNormalExpansion(atLeastAbstractRoleConcept,forNode);
    }
    private Map<AbstractRole,AbstractRole[]> buildFunctionalRoles() {
        Set<AbstractRole> functionalRoles=new HashSet<AbstractRole>();
        ObjectHierarchy<AbstractRole> roleHierarchy=new ObjectHierarchy<AbstractRole>();
        for (DLClause dlClause : m_tableau.getDLOntology().getDLClauses()) {
            if (dlClause.isRoleInclusion()) {
                AtomicAbstractRole subrole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicAbstractRole superrole=(AtomicAbstractRole)dlClause.getHeadAtom(0).getDLPredicate();
                roleHierarchy.addInclusion(subrole,superrole);
                roleHierarchy.addInclusion(subrole.getInverseRole(),superrole.getInverseRole());
            }
            else if (dlClause.isRoleInverseInclusion()) {
                AtomicAbstractRole subrole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicAbstractRole superrole=(AtomicAbstractRole)dlClause.getHeadAtom(0).getDLPredicate();
                roleHierarchy.addInclusion(subrole,superrole.getInverseRole());
                roleHierarchy.addInclusion(subrole.getInverseRole(),superrole);
            }
            else if (dlClause.isFunctionalityAxiom()) {
                AtomicAbstractRole atomicAbstractRole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(atomicAbstractRole);
                roleHierarchy.addInclusion(atomicAbstractRole,atomicAbstractRole);
                roleHierarchy.addInclusion(atomicAbstractRole.getInverseRole(),atomicAbstractRole.getInverseRole());
            }
            else if (dlClause.isGuardedFunctionalityAxiom()) {
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
            else if (dlClause.isGuardedInverseFunctionalityAxiom()) {
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
    public boolean isSatisfied(AtLeastConcreteRoleConcept atLeastConcreteRoleConcept,Node forNode) {
        // TODO: Write me
        return false;
    }
    /**
     * Creates a new node in the tableau if at least concept that caused the 
     * expansion is for cardinality 1. If it is not of cardinality 1 and the 
     * role in the at most concept is a functional role, it sets a clash in the 
     * extension manager. 
     * @param atLeastConcreteRoleConcept
     * @param forNode
     * @return true if the at least cardinality is 1 (causes an expansion) or it 
     * is greater than one but the role is functional (causes a clash) and false 
     * otherwise.
     */
    public boolean tryFunctionalExpansion(AtLeastConcreteRoleConcept atLeastConcreteRoleConcept,Node forNode) {
        // TODO: Write me
        return false;
    }
    /**
     * Does a normal expansion of the tableau (ABox) for an at least concept. 
     * @param atLeastConcreteRoleConcept
     * @param forNode
     */
    public void doNormalExpansion(AtLeastConcreteRoleConcept atLeastConcreteRoleConcept,Node forNode) {
        // TODO: Write me
    }
    /**
     * Expands an at least concept by first trying a functional expansion (if 
     * the cardinality is 1 or the role is functional) and a normal expansion 
     * otherwise. 
     * @param atLeastConcreteRoleConcept
     * @param forNode
     */
    public void expand(AtLeastConcreteRoleConcept atLeastConcreteRoleConcept,Node forNode) {
        if (!tryFunctionalExpansion(atLeastConcreteRoleConcept,forNode))
            doNormalExpansion(atLeastConcreteRoleConcept,forNode);
    }

}
