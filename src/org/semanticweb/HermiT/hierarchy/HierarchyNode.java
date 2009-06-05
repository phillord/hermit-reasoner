// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class HierarchyNode<E> {
    protected final E m_representative;
    protected final Set<E> m_equivalentElements;
    protected final Set<HierarchyNode<E>> m_parentNodes;
    protected final Set<HierarchyNode<E>> m_childNodes;
    
    public HierarchyNode(E representative) {
        m_representative=representative;
        m_equivalentElements=new HashSet<E>();
        m_equivalentElements.add(m_representative);
        m_parentNodes=new HashSet<HierarchyNode<E>>();
        m_childNodes=new HashSet<HierarchyNode<E>>();
    }
    public HierarchyNode(E element,Set<E> equivalentElements,Set<HierarchyNode<E>> parentNodes,Set<HierarchyNode<E>> childNodes) {
        m_representative=element;
        m_equivalentElements=equivalentElements;
        m_parentNodes=parentNodes;
        m_childNodes=childNodes;
    }
    public E getRepresentative() {
        return m_representative;
    }
    public Set<E> getEquivalentElements() {
        return Collections.unmodifiableSet(m_equivalentElements);
    }
    public Set<HierarchyNode<E>> getParentNodes() {
        return Collections.unmodifiableSet(m_parentNodes);
    }
    public Set<HierarchyNode<E>> getChildNodes() {
        return Collections.unmodifiableSet(m_childNodes);
    }
    public Set<HierarchyNode<E>> getAncestorNodes() {
        Set<HierarchyNode<E>> result=new HashSet<HierarchyNode<E>>();
        Queue<HierarchyNode<E>> toVisit=new LinkedList<HierarchyNode<E>>();
        toVisit.add(this);
        while (!toVisit.isEmpty()) {
            HierarchyNode<E> current=toVisit.poll();
            if (result.add(current))
                toVisit.addAll(current.getParentNodes());
        }
        return result;
    }
    public Set<HierarchyNode<E>> getDescendantNodes() {
        Set<HierarchyNode<E>> result=new HashSet<HierarchyNode<E>>();
        Queue<HierarchyNode<E>> toVisit=new LinkedList<HierarchyNode<E>>();
        toVisit.add(this);
        while (!toVisit.isEmpty()) {
            HierarchyNode<E> current=toVisit.poll();
            if (result.add(current))
                toVisit.addAll(current.getChildNodes());
        }
        return result;
    }
    public String toString() {
        return m_equivalentElements.toString();
    }
}
