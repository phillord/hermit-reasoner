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
import java.util.LinkedList;

import org.semanticweb.HermiT.model.*;

/**
 * Implements the subsumption hierarchy and the classification algorithm.
 */
public class SubsumptionHierarchy implements Set<SubsumptionHierarchyNode>,Serializable {
    private static final long serialVersionUID=-2655453376366628806L;
    protected static enum SubsumptionType { POSITIVE,NEGATIVE };

    /** The map of classes to nodes. */
    protected final Map<String,SubsumptionHierarchyNode> m_owlClassesToNodes;
    /** The set of nodes in the collection. */
    protected final Set<SubsumptionHierarchyNode> m_nodes;

    public SubsumptionHierarchy(SubsumptionChecker subsumptionChecker) throws SubusmptionCheckerException {
        m_owlClassesToNodes=new HashMap<String,SubsumptionHierarchyNode>();
        SubsumptionHierarchyNode thing=getNodeForEx(AtomicConcept.THING.getURI());
        SubsumptionHierarchyNode nothing=getNodeForEx(AtomicConcept.NOTHING.getURI());
        thing.m_childNodes.add(nothing);
        nothing.m_parentNodes.add(thing);
        for (String owlClassURI : subsumptionChecker.getAllAtomicClasses())
            insertClass(owlClassURI,subsumptionChecker);
        m_nodes=new HashSet<SubsumptionHierarchyNode>();
        for (Map.Entry<String,SubsumptionHierarchyNode> entries : m_owlClassesToNodes.entrySet())
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
    public SubsumptionHierarchyNode getNodeFor(String owlClassURI) {
        return m_owlClassesToNodes.get(owlClassURI);
    }
    public SubsumptionHierarchyNode thingNode() {
        return getNodeFor(AtomicConcept.THING.getURI());
    }
    public SubsumptionHierarchyNode nothingNode() {
        return getNodeFor(AtomicConcept.NOTHING.getURI());
    }
    public Map<String,Set<String>> getFlattenedHierarchy() {
        Map<String,Set<String>> flattenedHierarchy=new TreeMap<String,Set<String>>();
        for (String subconcept : m_owlClassesToNodes.keySet()) {
            if (!AtomicConcept.THING.getURI().equals(subconcept) && !AtomicConcept.NOTHING.getURI().equals(subconcept)) {
                Set<String> superconcepts=new TreeSet<String>();
                SubsumptionHierarchyNode subconceptNode=getNodeFor(subconcept);
                superconcepts.addAll(subconceptNode.getAtomicConcepts());
                for (SubsumptionHierarchyNode ancestorNode : subconceptNode.getAncestorNodes())
                    superconcepts.addAll(ancestorNode.getAtomicConcepts());
                superconcepts.remove(AtomicConcept.THING.getURI());
                flattenedHierarchy.put(subconcept,superconcepts);
            }
        }
        return flattenedHierarchy;
    }
    protected SubsumptionHierarchyNode getNodeForEx(String owlClassURI) {
        SubsumptionHierarchyNode node=m_owlClassesToNodes.get(owlClassURI);
        if (node==null) {
            node=new SubsumptionHierarchyNode(owlClassURI);
            node.m_owlClasses.add(owlClassURI);
            m_owlClassesToNodes.put(owlClassURI,node);
        }
        return node;
    }
    protected void insertClass(String newClassURI,SubsumptionChecker subsumptionChecker) throws SubusmptionCheckerException {
        Set<SubsumptionHierarchyNode> topSet=new HashSet<SubsumptionHierarchyNode>();
        Set<SubsumptionHierarchyNode> visited=new HashSet<SubsumptionHierarchyNode>();
        Map<String,SubsumptionType> subsumptionCache=new HashMap<String,SubsumptionType>();
        topSearch(newClassURI,(SubsumptionHierarchyNode)getNodeFor(AtomicConcept.THING.getURI()),subsumptionChecker,topSet,visited,subsumptionCache);
        if (topSet.size()==1) {
            SubsumptionHierarchyNode node=topSet.iterator().next();
            if (subsumptionChecker.isSubsumedBy(node.m_representativeURI,newClassURI)) {
                node.m_owlClasses.add(newClassURI);
                m_owlClassesToNodes.put(newClassURI,node);
                return;
            }
        }
        Set<SubsumptionHierarchyNode> bottomSet=new HashSet<SubsumptionHierarchyNode>();
        visited.clear();
        subsumptionCache.clear();
        bottomSearch(newClassURI,(SubsumptionHierarchyNode)getNodeFor(AtomicConcept.NOTHING.getURI()),subsumptionChecker,bottomSet,visited,subsumptionCache);
        SubsumptionHierarchyNode newNode=getNodeForEx(newClassURI);
        for (SubsumptionHierarchyNode topNode : topSet) {
            topNode.m_childNodes.removeAll(bottomSet);
            topNode.m_childNodes.add(newNode);
            newNode.m_parentNodes.add(topNode);
        }
        for (SubsumptionHierarchyNode bottomNode : bottomSet) {
            bottomNode.m_parentNodes.removeAll(topSet);
            bottomNode.m_parentNodes.add(newNode);
            newNode.m_childNodes.add(bottomNode);
        }
    }
    protected void topSearch(String newClassURI,SubsumptionHierarchyNode current,SubsumptionChecker subsumptionChecker,Set<SubsumptionHierarchyNode> topSet,Set<SubsumptionHierarchyNode> visited,Map<String,SubsumptionType> subsumptionCache) throws SubusmptionCheckerException {
        visited.add(current);
        List<SubsumptionHierarchyNode> toExamine=null;
        for (SubsumptionHierarchyNode child : current.m_childNodes) {
            SubsumptionHierarchyNode childStub=(SubsumptionHierarchyNode)child;
            if (topSubsumes(childStub.m_representativeURI,newClassURI,subsumptionChecker,subsumptionCache)) {
                if (toExamine==null)
                    toExamine=new LinkedList<SubsumptionHierarchyNode>();
                toExamine.add(childStub);
            }
        }
        if (toExamine==null)
            topSet.add(current);
        else {
            for (SubsumptionHierarchyNode child : toExamine)
                if (!visited.contains(child))
                    topSearch(newClassURI,child,subsumptionChecker,topSet,visited,subsumptionCache);
        }
    }
    protected boolean topSubsumes(String superClassURI,String subClassURI,SubsumptionChecker subsumptionChecker,Map<String,SubsumptionType> subsumptionCache) throws SubusmptionCheckerException {
        SubsumptionType subsumptionType=subsumptionCache.get(superClassURI);
        if (SubsumptionType.POSITIVE.equals(subsumptionType))
            return true;
        else if (SubsumptionType.NEGATIVE.equals(subsumptionType))
            return false;
        else {
            if (subsumptionChecker.isSubsumedBy(subClassURI,superClassURI)) {
                subsumptionCache.put(superClassURI,SubsumptionType.POSITIVE);
                return true;
            }
            else {
                subsumptionCache.put(superClassURI,SubsumptionType.NEGATIVE);
                return false;
            }
        }
    }
    protected void bottomSearch(String newClassURI,SubsumptionHierarchyNode current,SubsumptionChecker subsumptionChecker,Set<SubsumptionHierarchyNode> bottomSet,Set<SubsumptionHierarchyNode> visited,Map<String,SubsumptionType> subsumptionCache) throws SubusmptionCheckerException {
        visited.add(current);
        List<SubsumptionHierarchyNode> toExamine=null;
        for (SubsumptionHierarchyNode child : current.m_parentNodes) {
            SubsumptionHierarchyNode childStub=(SubsumptionHierarchyNode)child;
            if (bottomSubsumes(newClassURI,childStub.m_representativeURI,subsumptionChecker,subsumptionCache)) {
                if (toExamine==null)
                    toExamine=new LinkedList<SubsumptionHierarchyNode>();
                toExamine.add(childStub);
            }
        }
        if (toExamine==null)
            bottomSet.add(current);
        else {
            for (SubsumptionHierarchyNode child : toExamine)
                if (!visited.contains(child))
                    bottomSearch(newClassURI,child,subsumptionChecker,bottomSet,visited,subsumptionCache);
        }
    }
    protected boolean bottomSubsumes(String superClassURI,String subClassURI,SubsumptionChecker subsumptionChecker,Map<String,SubsumptionType> subsumptionCache) throws SubusmptionCheckerException {
        SubsumptionType subsumptionType=subsumptionCache.get(subClassURI);
        if (SubsumptionType.POSITIVE.equals(subsumptionType))
            return true;
        else if (SubsumptionType.NEGATIVE.equals(subsumptionType))
            return false;
        else {
            if (subsumptionChecker.isSubsumedBy(subClassURI,superClassURI)) {
                subsumptionCache.put(subClassURI,SubsumptionType.POSITIVE);
                return true;
            }
            else {
                subsumptionCache.put(subClassURI,SubsumptionType.NEGATIVE);
                return false;
            }
        }
    }

    public static interface SubsumptionChecker {
        Set<String> getAllAtomicClasses() throws SubusmptionCheckerException;
        boolean isSubsumedBy(String subClassURI,String superClassURI) throws SubusmptionCheckerException;
    }

    @SuppressWarnings("serial")
    public static class SubusmptionCheckerException extends Exception {
        public SubusmptionCheckerException(Throwable cause) {
            super(cause);
        }
    }
}
