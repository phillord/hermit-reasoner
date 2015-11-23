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
/**HierarchyNode.
 * @param <E> type*/
public class HierarchyNode<E> {
    protected final E m_representative;
    protected final Set<E> m_equivalentElements;
    protected final Set<HierarchyNode<E>> m_parentNodes;
    protected final Set<HierarchyNode<E>> m_childNodes;

    /**
     * @param representative representative
     */
    public HierarchyNode(E representative) {
        m_representative=representative;
        m_equivalentElements=new HashSet<>();
        m_equivalentElements.add(m_representative);
        m_parentNodes=new HashSet<>();
        m_childNodes=new HashSet<>();
    }
    /**
     * @param element element
     * @param equivalentElements equivalentElements
     * @param parentNodes parentNodes
     * @param childNodes childNodes
     */
    public HierarchyNode(E element,Set<E> equivalentElements,Set<HierarchyNode<E>> parentNodes,Set<HierarchyNode<E>> childNodes) {
        m_representative=element;
        m_equivalentElements=equivalentElements;
        m_parentNodes=parentNodes;
        m_childNodes=childNodes;
    }
    /**
     * @return representative
     */
    public E getRepresentative() {
        return m_representative;
    }
    /**
     * @param element element
     * @return true if equivalent
     */
    public boolean isEquivalentElement(E element) {
        return m_equivalentElements.contains(element);
    }
    /**
     * @param ancestor ancestor
     * @return true if ancestor
     */
    public boolean isAncestorElement(E ancestor) {
        for (HierarchyNode<E> node : getAncestorNodes())
            if (node.isEquivalentElement(ancestor))
                return true;
        return false;
    }
    /**
     * @param descendant descendant
     * @return descendant element
     */
    public boolean isDescendantElement(E descendant) {
        for (HierarchyNode<E> node : getDescendantNodes())
            if (node.isEquivalentElement(descendant))
                return true;
        return false;
    }
    /**
     * @return equivalent
     */
    public Set<E> getEquivalentElements() {
        return Collections.unmodifiableSet(m_equivalentElements);
    }
    /**
     * @return parents
     */
    public Set<HierarchyNode<E>> getParentNodes() {
        return Collections.unmodifiableSet(m_parentNodes);
    }
    /**
     * @return children
     */
    public Set<HierarchyNode<E>> getChildNodes() {
        return Collections.unmodifiableSet(m_childNodes);
    }
    /**
     * @return ancestors
     */
    public Set<HierarchyNode<E>> getAncestorNodes() {
        return getAncestorNodes(Collections.singleton(this));
    }
    /**
     * @return descendants
     */
    public Set<HierarchyNode<E>> getDescendantNodes() {
        return getDescendantNodes(Collections.singleton(this));
    }
    @Override
    public String toString() {
        return m_equivalentElements.toString();
    }
    /**
     * @param inputNodes inputNodes
     * @param <T> type
     * @return ancestors
     */
    public static <T> Set<HierarchyNode<T>> getAncestorNodes(Set<HierarchyNode<T>> inputNodes) {
        Set<HierarchyNode<T>> result=new HashSet<>();
        Queue<HierarchyNode<T>> toVisit=new LinkedList<>(inputNodes);
        while (!toVisit.isEmpty()) {
            HierarchyNode<T> current=toVisit.poll();
            if (result.add(current))
                toVisit.addAll(current.getParentNodes());
        }
        return result;
    }
    /**
     * @param inputNodes inputNodes
     * @param <T> type
     * @return descendants
     */
    public static <T> Set<HierarchyNode<T>> getDescendantNodes(Set<HierarchyNode<T>> inputNodes) {
        Set<HierarchyNode<T>> result=new HashSet<>();
        Queue<HierarchyNode<T>> toVisit=new LinkedList<>(inputNodes);
        while (!toVisit.isEmpty()) {
            HierarchyNode<T> current=toVisit.poll();
            if (result.add(current))
                toVisit.addAll(current.getChildNodes());
        }
        return result;
    }
}
