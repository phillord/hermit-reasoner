/* Copyright 2009 by the Oxford University Computing Laboratory
   
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class StandardClassificationManager<E> implements ClassificationManager<E> {
    protected final SubsumptionCache<E> m_subsumptionCache;
    
    public StandardClassificationManager(SubsumptionCache<E> subsumptionCache) {
        m_subsumptionCache=subsumptionCache;
    }
    public boolean isSatisfiable(E element) {
        return m_subsumptionCache.isSatisfiable(element);
    }
    public boolean isSubsumedBy(E subelement,E superelement) {
        return m_subsumptionCache.isSubsumedBy(subelement,superelement);
    }
    public Hierarchy<E> classify(ProgressMonitor<E> progressMonitor,E topElement,E bottomElement,Set<E> elements) {
        if (!m_subsumptionCache.isSatisfiable(topElement))
            return Hierarchy.emptyHierarchy(elements,topElement,bottomElement);
        Relation<E> relation=new Relation<E>() {
            public boolean doesSubsume(E parent,E child) {
                return m_subsumptionCache.isSubsumedBy(child,parent);
            }
        };
        return buildHierarchy(progressMonitor,relation,topElement,bottomElement,elements);
    }
    public static <E> Hierarchy<E> buildHierarchy(ProgressMonitor<E> progressMonitor,Relation<E> hierarchyRelation,E topElement,E bottomElement,Collection<E> elements) {
        if (hierarchyRelation.doesSubsume(bottomElement,topElement))
            return Hierarchy.emptyHierarchy(elements,topElement,bottomElement);
        else {
            HierarchyNode<E> topNode=new HierarchyNode<E>(topElement);
            HierarchyNode<E> bottomNode=new HierarchyNode<E>(bottomElement);
            topNode.m_childNodes.add(bottomNode);
            bottomNode.m_parentNodes.add(topNode);
            Hierarchy<E> hierarchy=new Hierarchy<E>(topNode,bottomNode);
            for (E element : elements) {
                if (!element.equals(topElement) && !element.equals(bottomElement)) {
                    HierarchyNode<E> node=findPosition(hierarchyRelation,element,topNode,bottomNode);
                    hierarchy.m_nodesByElements.put(element,node);
                    if (!node.m_equivalentElements.contains(element)) {
                        // Existing node: just add the element to the node label
                        node.m_equivalentElements.add(element);
                    }
                    else {
                        // New node: insert it into the hierarchy
                        for (HierarchyNode<E> parent : node.m_parentNodes) {
                            parent.m_childNodes.add(node);
                            parent.m_childNodes.removeAll(node.m_childNodes);
                        }
                        for (HierarchyNode<E> child : node.m_childNodes) {
                            child.m_parentNodes.add(node);
                            child.m_parentNodes.removeAll(node.m_parentNodes);
                        }
                    }
                }
                progressMonitor.elementClassified(element);
            }
            return hierarchy;
        }
    }
    public static <E> HierarchyNode<E> findPosition(Relation<E> hierarchyRelation,E element,HierarchyNode<E> topNode,HierarchyNode<E> bottomNode) {
        Set<HierarchyNode<E>> parentNodes=findParents(hierarchyRelation,element,topNode);
        Set<HierarchyNode<E>> childNodes=findChildren(hierarchyRelation,element,bottomNode,parentNodes);
        if (parentNodes.equals(childNodes)) {
            assert parentNodes.size()==1 && childNodes.size()==1;
            return parentNodes.iterator().next();
        }
        else {
            Set<E> equivalentElements=new HashSet<E>();
            equivalentElements.add(element);
            return new HierarchyNode<E>(element,equivalentElements,parentNodes,childNodes);
        }
    }
    protected static <E> Set<HierarchyNode<E>> findParents(final Relation<E> hierarchyRelation,final E element,HierarchyNode<E> topNode) {
        return search(
            new SearchPredicate<HierarchyNode<E>>() {
                public Set<HierarchyNode<E>> getSuccessorElements(HierarchyNode<E> u) {
                    return u.m_childNodes;
                }
                public Set<HierarchyNode<E>> getPredecessorElements(HierarchyNode<E> u) {
                    return u.m_parentNodes;
                }
                public boolean trueOf(HierarchyNode<E> u) {
                    return hierarchyRelation.doesSubsume(u.getRepresentative(),element);
                }
            },Collections.singleton(topNode),null);
    }

    protected static <E> Set<HierarchyNode<E>> findChildren(final Relation<E> hierarchyRelation,final E element,HierarchyNode<E> bottomNode,Set<HierarchyNode<E>> parentNodes) {
        if (parentNodes.size()==1 && hierarchyRelation.doesSubsume(element,parentNodes.iterator().next().getRepresentative()))
            return parentNodes;
        else {
            // We now determine the set of nodes that are descendants of each node in parentNodes
            Iterator<HierarchyNode<E>> parentNodesIterator=parentNodes.iterator();
            Set<HierarchyNode<E>> marked=new HashSet<HierarchyNode<E>>(parentNodesIterator.next().getDescendantNodes());
            while (parentNodesIterator.hasNext()) {
                Set<HierarchyNode<E>> freshlyMarked=new HashSet<HierarchyNode<E>>();
                Set<HierarchyNode<E>> visited=new HashSet<HierarchyNode<E>>();
                Queue<HierarchyNode<E>> toProcess=new LinkedList<HierarchyNode<E>>();
                toProcess.add(parentNodesIterator.next());
                while (!toProcess.isEmpty()) {
                    HierarchyNode<E> currentNode=toProcess.remove();
                    for (HierarchyNode<E> childNode : currentNode.m_childNodes)
                        if (marked.contains(childNode))
                            freshlyMarked.add(childNode);
                        else if (visited.add(childNode))
                            toProcess.add(childNode);
                }
                toProcess.addAll(freshlyMarked);
                while (!toProcess.isEmpty()) {
                    HierarchyNode<E> currentNode=toProcess.remove();
                    for (HierarchyNode<E> childNode : currentNode.m_childNodes)
                        if (freshlyMarked.add(childNode))
                            toProcess.add(childNode);
                }
                marked=freshlyMarked;
            }
            // Determine the subset of marked that is directly above the bottomNode and that is below the current element.
            Set<HierarchyNode<E>> aboveBottomNodes=new HashSet<HierarchyNode<E>>();
            for (HierarchyNode<E> node : marked)
                if (node.m_childNodes.contains(bottomNode) && hierarchyRelation.doesSubsume(element,node.getRepresentative()))
                    aboveBottomNodes.add(node);
            // If this set is empty, then we omit the bottom search phase.
            if (aboveBottomNodes.isEmpty()) {
                Set<HierarchyNode<E>> childNodes=new HashSet<HierarchyNode<E>>();
                childNodes.add(bottomNode);
                return childNodes;
            }
            else {
                return search(
                    new SearchPredicate<HierarchyNode<E>>() {
                        public Set<HierarchyNode<E>> getSuccessorElements(HierarchyNode<E> u) {
                            return u.m_parentNodes;
                        }
                        public Set<HierarchyNode<E>> getPredecessorElements(HierarchyNode<E> u) {
                            return u.m_childNodes;
                        }
                        public boolean trueOf(HierarchyNode<E> u) {
                            return hierarchyRelation.doesSubsume(element,u.getRepresentative());
                        }
                    },aboveBottomNodes,marked);
            }
        }
    }

    public static <U> Set<U> search(SearchPredicate<U> searchPredicate,Collection<U> startSearch,Set<U> possibilities) {
        SearchCache<U> cache=new SearchCache<U>(searchPredicate,possibilities);
        Set<U> result=new HashSet<U>();
        Set<U> visited=new HashSet<U>(startSearch);
        Queue<U> toProcess=new LinkedList<U>(startSearch);
        while (!toProcess.isEmpty()) {
            U current=toProcess.remove();
            boolean foundSubordinateElement=false;
            Set<U> subordinateElements=searchPredicate.getSuccessorElements(current);
            for (U subordinateElement : subordinateElements)
                if (cache.trueOf(subordinateElement)) {
                    foundSubordinateElement=true;
                    if (visited.add(subordinateElement))
                        toProcess.add(subordinateElement);
                }
            if (!foundSubordinateElement)
                result.add(current);
        }
        return result;
    }

    public static interface Relation<U> {
        boolean doesSubsume(U parent,U child);
    }

    public static interface SearchPredicate<U> {
        Set<U> getSuccessorElements(U u);
        Set<U> getPredecessorElements(U u);
        boolean trueOf(U u);
    }

    protected static final class SearchCache<U> {
        protected final SearchPredicate<U> m_searchPredicate;
        protected final Set<U> m_possibilities;
        protected final Set<U> m_positives;
        protected final Set<U> m_negatives;

        public SearchCache(SearchPredicate<U> f,Set<U> possibilities) {
            m_searchPredicate=f;
            m_possibilities=possibilities;
            m_positives=new HashSet<U>();
            m_negatives=new HashSet<U>();
        }
        public boolean trueOf(U element) {
            if (m_positives.contains(element))
                return true;
            else if (m_negatives.contains(element) || (m_possibilities!=null && !m_possibilities.contains(element)))
                return false;
            else {
                for (U superordinateElement : m_searchPredicate.getPredecessorElements(element)) {
                    if (!trueOf(superordinateElement)) {
                        m_negatives.add(element);
                        return false;
                    }
                }
                if (m_searchPredicate.trueOf(element)) {
                    m_positives.add(element);
                    return true;
                }
                else {
                    m_negatives.add(element);
                    return false;
                }
            }
        }
    }
}
