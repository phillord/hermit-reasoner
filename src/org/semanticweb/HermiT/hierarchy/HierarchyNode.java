/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
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
    public boolean isEquivalentElement(E element) {
        return m_equivalentElements.contains(element);
    }
    public boolean isAncestorElement(E ancestor) {
        for (HierarchyNode<E> node : getAncestorNodes())
            if (node.isEquivalentElement(ancestor))
                return true;
        return false;
    }
    public boolean isDescendantElement(E descendant) {
        for (HierarchyNode<E> node : getDescendantNodes())
            if (node.isEquivalentElement(descendant))
                return true;
        return false;
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
        return getAncestorNodes(Collections.singleton(this));
    }
    public Set<HierarchyNode<E>> getDescendantNodes() {
        return getDescendantNodes(Collections.singleton(this));
    }
    public String toString() {
        return m_equivalentElements.toString();
    }
    public static <T> Set<HierarchyNode<T>> getAncestorNodes(Set<HierarchyNode<T>> inputNodes) {
        Set<HierarchyNode<T>> result=new HashSet<HierarchyNode<T>>();
        Queue<HierarchyNode<T>> toVisit=new LinkedList<HierarchyNode<T>>(inputNodes);
        while (!toVisit.isEmpty()) {
            HierarchyNode<T> current=toVisit.poll();
            if (result.add(current))
                toVisit.addAll(current.getParentNodes());
        }
        return result;
    }
    public static <T> Set<HierarchyNode<T>> getDescendantNodes(Set<HierarchyNode<T>> inputNodes) {
        Set<HierarchyNode<T>> result=new HashSet<HierarchyNode<T>>();
        Queue<HierarchyNode<T>> toVisit=new LinkedList<HierarchyNode<T>>(inputNodes);
        while (!toVisit.isEmpty()) {
            HierarchyNode<T> current=toVisit.poll();
            if (result.add(current))
                toVisit.addAll(current.getChildNodes());
        }
        return result;
    }
}
