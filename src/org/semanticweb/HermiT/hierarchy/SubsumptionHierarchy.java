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
        if (subsumptionChecker.canGetAllSubsumersEasily()) {
            OptimizedClassificationManager classificationManager=new OptimizedClassificationManager(this,subsumptionChecker);
            classificationManager.buildHierarchy();
        }
        else {
            StandardClassificationManager classificationManager=new StandardClassificationManager(this,subsumptionChecker);
            classificationManager.buildHierarchy();
        }
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
        boolean canGetAllSubsumersEasily() throws SubusmptionCheckerException;
        Set<AtomicConcept> getAllSubsumers(AtomicConcept concept) throws SubusmptionCheckerException;
        boolean isSubsumedBy(AtomicConcept subconcept,AtomicConcept superconcept) throws SubusmptionCheckerException;
    }

    @SuppressWarnings("serial")
    public static class SubusmptionCheckerException extends Exception {
        public SubusmptionCheckerException(Throwable cause) {
            super(cause);
        }
        public SubusmptionCheckerException(String message) {
            super(message);
        }
    }
}
