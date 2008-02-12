package org.semanticweb.HermiT.hierarchy;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

import org.semanticweb.HermiT.model.*;

/**
 * Implements the subsumption hierarchy and the classification algorithm.
 */
public class SubsumptionHierarchy implements Set<SubsumptionHierarchyNode>,Serializable {
    private static final long serialVersionUID=-2655453376366628806L;

    /** The map of classes to nodes. */
    protected final Map<AtomicConcept,SubsumptionHierarchyNode> m_atomicConceptsToNodes;
    /** The top node. */
    protected final SubsumptionHierarchyNode m_thingNode;
    /** The bottom node. */
    protected final SubsumptionHierarchyNode m_nothingNode;
    /** The set of nodes in the collection. */
    protected final Set<SubsumptionHierarchyNode> m_nodes;

    public SubsumptionHierarchy(SubsumptionChecker subsumptionChecker) throws SubusmptionCheckerException {
        m_atomicConceptsToNodes=new HashMap<AtomicConcept,SubsumptionHierarchyNode>();
        m_thingNode=getNodeForEx(AtomicConcept.THING);
        m_nothingNode=getNodeForEx(AtomicConcept.NOTHING);
        m_thingNode.m_childNodes.add(m_nothingNode);
        m_nothingNode.m_parentNodes.add(m_thingNode);
        ClassificationManager classificationManager=new ClassificationManager(subsumptionChecker);
        for (AtomicConcept atomicConcept : subsumptionChecker.getAllAtomicConcepts())
            if (!atomicConcept.getURI().startsWith("internal:"))
                classificationManager.insertConcept(atomicConcept);
        m_nodes=new HashSet<SubsumptionHierarchyNode>();
        for (Map.Entry<AtomicConcept,SubsumptionHierarchyNode> entries : m_atomicConceptsToNodes.entrySet())
            m_nodes.add(entries.getValue());
    }
    public int size() {
        return m_nodes.size();
    }
    public boolean isEmpty() {
        return m_nodes.isEmpty();
    }
    public boolean contains(Object object) {
        return m_nodes.contains(object);
    }
    public Iterator<SubsumptionHierarchyNode> iterator() {
        return m_nodes.iterator();
    }
    public Object[] toArray() {
        return m_nodes.toArray();
    }
    public <T> T[] toArray(T[] a) {
        return m_nodes.toArray(a);
    }
    public boolean add(SubsumptionHierarchyNode o) {
        throw new UnsupportedOperationException("SubSumption hierarchies are immutable objects.");
    }
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Subsumption hierarchies are immutable objects.");
    }
    public boolean containsAll(Collection<?> collection) {
        for (Object object : collection)
            if (!m_nodes.contains(object))
                return false;
             
        return true;
    }
    public boolean addAll(Collection<? extends SubsumptionHierarchyNode> c) {
        throw new UnsupportedOperationException("Subsumption hierarchies are immutable objects.");
    }
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Subsumption hierarchies are immutable objects.");
    }
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Subsumption hierarchies are immutable objects.");
    }
    public void clear() {
        throw new UnsupportedOperationException("Subsumption hierarchies are immutable objects.");
    }
    public SubsumptionHierarchyNode getNodeFor(AtomicConcept atomicConcept) {
        return m_atomicConceptsToNodes.get(atomicConcept);
    }
    public SubsumptionHierarchyNode thingNode() {
        return m_thingNode;
    }
    public SubsumptionHierarchyNode nothingNode() {
        return m_nothingNode;
    }
    public Map<AtomicConcept,Set<AtomicConcept>> getFlattenedHierarchy() {
        Map<AtomicConcept,Set<AtomicConcept>> flattenedHierarchy=new TreeMap<AtomicConcept,Set<AtomicConcept>>(DLOntology.AtomicConceptComparator.INSTANCE);
        for (AtomicConcept subconcept : m_atomicConceptsToNodes.keySet()) {
            if (!AtomicConcept.THING.equals(subconcept) && !AtomicConcept.NOTHING.equals(subconcept)) {
                Set<AtomicConcept> superconcepts=new TreeSet<AtomicConcept>(DLOntology.AtomicConceptComparator.INSTANCE);
                SubsumptionHierarchyNode subconceptNode=getNodeFor(subconcept);
                superconcepts.addAll(subconceptNode.getEquivalentConcepts());
                for (SubsumptionHierarchyNode ancestorNode : subconceptNode.getAncestorNodes())
                    superconcepts.addAll(ancestorNode.getEquivalentConcepts());
                superconcepts.remove(AtomicConcept.THING);
                flattenedHierarchy.put(subconcept,superconcepts);
            }
        }
        return flattenedHierarchy;
    }
    protected SubsumptionHierarchyNode getNodeForEx(AtomicConcept atomicConcept) {
        SubsumptionHierarchyNode node=m_atomicConceptsToNodes.get(atomicConcept);
        if (node==null) {
            node=new SubsumptionHierarchyNode(atomicConcept);
            m_atomicConceptsToNodes.put(atomicConcept,node);
        }
        return node;
    }

    public static interface SubsumptionChecker {
        Set<AtomicConcept> getAllAtomicConcepts() throws SubusmptionCheckerException;
        boolean isSubsumedBy(AtomicConcept subconcept,AtomicConcept superconcept) throws SubusmptionCheckerException;
    }

    @SuppressWarnings("serial")
    public static class SubusmptionCheckerException extends Exception {
        public SubusmptionCheckerException(Throwable cause) {
            super(cause);
        }
    }

    protected static enum SubsumptionType { POSITIVE,NEGATIVE };

    protected class ClassificationManager {
        protected final SubsumptionChecker m_subsumptionChecker;
        protected final Set<SubsumptionHierarchyNode> m_visitedNodes;
        protected final List<SubsumptionHierarchyNode> m_topSet;
        protected final List<SubsumptionHierarchyNode> m_bottomSet;
        protected final List<SubsumptionHierarchyNode> m_toProcess;
        protected final List<SubsumptionHierarchyNode> m_relevantBottomParents;
        protected final Map<SubsumptionHierarchyNode,SubsumptionType> m_subsumptionCache;
        
        public ClassificationManager(SubsumptionChecker subsumptionChecker) {
            m_subsumptionChecker=subsumptionChecker;
            m_visitedNodes=new HashSet<SubsumptionHierarchyNode>();
            m_topSet=new ArrayList<SubsumptionHierarchyNode>();
            m_bottomSet=new ArrayList<SubsumptionHierarchyNode>();
            m_toProcess=new ArrayList<SubsumptionHierarchyNode>();
            m_relevantBottomParents=new ArrayList<SubsumptionHierarchyNode>();
            m_subsumptionCache=new HashMap<SubsumptionHierarchyNode,SubsumptionType>();
        }
        public void insertConcept(AtomicConcept atomicConcept) throws SubusmptionCheckerException {
            topSeach(atomicConcept);
            if (m_topSet.size()==1) {
                SubsumptionHierarchyNode node=m_topSet.get(0);
                if (m_subsumptionChecker.isSubsumedBy(node.m_representativeConcept,atomicConcept)) {
                    node.m_equivalentConcepts.add(atomicConcept);
                    m_atomicConceptsToNodes.put(atomicConcept,node);
                    return;
                }
                else if (node.m_childNodes.size()==1 && node.m_childNodes.contains(m_nothingNode)) {
                    m_bottomSet.clear();
                    m_bottomSet.add(m_nothingNode);
                }
                else
                    bottomSearch(atomicConcept);
            }
            else
                bottomSearch(atomicConcept);
            SubsumptionHierarchyNode newNode=getNodeForEx(atomicConcept);
            for (int topIndex=0;topIndex<m_topSet.size();topIndex++) {
                SubsumptionHierarchyNode topNode=m_topSet.get(topIndex);
                for (int bottomIndex=0;bottomIndex<m_bottomSet.size();bottomIndex++)
                    topNode.m_childNodes.remove(m_bottomSet.get(bottomIndex));
                topNode.m_childNodes.add(newNode);
                newNode.m_parentNodes.add(topNode);
            }
            for (int bottomIndex=0;bottomIndex<m_bottomSet.size();bottomIndex++) {
                SubsumptionHierarchyNode bottomNode=m_bottomSet.get(bottomIndex);
                for (int topIndex=0;topIndex<m_topSet.size();topIndex++)
                    bottomNode.m_parentNodes.remove(m_topSet.get(topIndex));
                bottomNode.m_parentNodes.add(newNode);
                newNode.m_childNodes.add(bottomNode);
            }
        }
        protected void topSeach(AtomicConcept atomicConcept) throws SubusmptionCheckerException {
            m_topSet.clear();
            m_visitedNodes.clear();
            m_toProcess.clear();
            m_subsumptionCache.clear();
            m_toProcess.add(m_thingNode);
            int processIndex=0;
            while (processIndex<m_toProcess.size()) {
                SubsumptionHierarchyNode subsumer=m_toProcess.get(processIndex++);
                if (m_visitedNodes.add(subsumer)) {
                    boolean childSubsumes=false;
                    for (SubsumptionHierarchyNode child : subsumer.m_childNodes) {
                        if (topSubsumedBy(atomicConcept,child)) {
                            childSubsumes=true;
                            if (!m_visitedNodes.contains(child))
                                m_toProcess.add(child);
                        }
                    }
                    if (!childSubsumes)
                        m_topSet.add(subsumer);
                }
            }
        }
        protected boolean topSubsumedBy(AtomicConcept atomicConcept,SubsumptionHierarchyNode node) throws SubusmptionCheckerException {
            SubsumptionType subsumptionType=m_subsumptionCache.get(node);
            if (subsumptionType==SubsumptionType.POSITIVE)
                return true;
            else if (subsumptionType==SubsumptionType.NEGATIVE)
                return false;
            else {
                boolean subsumedByAllParents=true;
                if (node.m_parentNodes.size()>1) {
                    for (SubsumptionHierarchyNode parent : node.m_parentNodes)
                        if (!topSubsumedBy(atomicConcept,parent)) {
                            subsumedByAllParents=false;
                            break;
                        }
                }
                boolean result;
                if (!subsumedByAllParents)
                    result=false;
                else
                    result=m_subsumptionChecker.isSubsumedBy(atomicConcept,node.m_representativeConcept);
                m_subsumptionCache.put(node,result ? SubsumptionType.POSITIVE : SubsumptionType.NEGATIVE);
                return result;
            }
        }
        protected void bottomSearch(AtomicConcept atomicConcept) throws SubusmptionCheckerException {
            m_relevantBottomParents.clear();
            m_subsumptionCache.clear();
            for (int topIndex=0;topIndex<m_topSet.size();topIndex++) {
                SubsumptionHierarchyNode topNode=m_topSet.get(topIndex);
                depthFirstSearch(topNode);
            }
            m_bottomSet.clear();
            m_visitedNodes.clear();
            m_toProcess.clear();
            m_toProcess.add(m_nothingNode);
            int processIndex=0;
            while (processIndex<m_toProcess.size()) {
                SubsumptionHierarchyNode subsumee=m_toProcess.get(processIndex++);
                if (m_visitedNodes.add(subsumee)) {
                    boolean parentSubsumer=false;
                    Collection<SubsumptionHierarchyNode> relevantParents=(subsumee==m_nothingNode ? m_relevantBottomParents : subsumee.m_parentNodes);
                    for (SubsumptionHierarchyNode parent : relevantParents) {
                        if (m_subsumptionCache.containsKey(parent) && bottomSubsumes(atomicConcept,parent)) {
                            parentSubsumer=true;
                            if (!m_visitedNodes.contains(parent))
                                m_toProcess.add(parent);
                        }
                    }
                    if (!parentSubsumer)
                        m_bottomSet.add(subsumee);
                }
            }
        }
        protected void depthFirstSearch(SubsumptionHierarchyNode node) {
            m_subsumptionCache.put(node,null); // This is a dummy marker saying that a node is on a path between a common child of all elements of the top set and bottom
            if (node!=m_nothingNode) {
                for (SubsumptionHierarchyNode childNode : node.m_childNodes) {
                    if (!m_subsumptionCache.containsKey(childNode))
                        depthFirstSearch(childNode);
                }
                if (node.m_childNodes.size()==1 && node.m_childNodes.contains(m_nothingNode))
                    m_relevantBottomParents.add(node);
            }
        }
        protected boolean bottomSubsumes(AtomicConcept atomicConcept,SubsumptionHierarchyNode node) throws SubusmptionCheckerException {
            SubsumptionType subsumptionType=m_subsumptionCache.get(node);
            if (subsumptionType==SubsumptionType.POSITIVE)
                return true;
            else if (subsumptionType==SubsumptionType.NEGATIVE)
                return false;
            else {
                boolean subsumesAllChildren=true;
                if (node.m_childNodes.size()>1) {
                    for (SubsumptionHierarchyNode child : node.m_childNodes)
                        if (!bottomSubsumes(atomicConcept,child)) {
                            subsumesAllChildren=false;
                            break;
                        }
                }
                boolean result;
                if (!subsumesAllChildren)
                    result=false;
                else
                    result=m_subsumptionChecker.isSubsumedBy(node.m_representativeConcept,atomicConcept);
                m_subsumptionCache.put(node,result ? SubsumptionType.POSITIVE : SubsumptionType.NEGATIVE);
                return result;
            }
        }
    }
}
