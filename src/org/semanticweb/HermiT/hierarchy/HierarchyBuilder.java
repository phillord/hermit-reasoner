// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class HierarchyBuilder<E> {
    protected final Relation<E> m_hierarchyRelation;
    protected final ClassificationProgressMonitor<E> m_progressMonitor;

    public HierarchyBuilder(Relation<E> hierarchyRelation,ClassificationProgressMonitor<E> progressMonitor) {
        m_hierarchyRelation=hierarchyRelation;
        m_progressMonitor=progressMonitor;
    }
    public Hierarchy<E> buildHierarchy(E topElement,E bottomElement,Collection<E> elements) {
        if (m_hierarchyRelation.doesSubsume(bottomElement,topElement))
            return Hierarchy.emptyHierarchy(elements,topElement,bottomElement);
        else {
            HierarchyNode<E> topNode=new HierarchyNode<E>(topElement);
            HierarchyNode<E> bottomNode=new HierarchyNode<E>(bottomElement);
            topNode.m_childNodes.add(bottomNode);
            bottomNode.m_parentNodes.add(topNode);
            Hierarchy<E> hierarchy=new Hierarchy<E>(topNode,bottomNode);
            for (E element : elements) {
                HierarchyNode<E> node=findPosition(element,topNode,bottomNode);
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
                if (m_progressMonitor!=null)
                    m_progressMonitor.elementClassified(element);
            }
            return hierarchy;
        }
    }
    public HierarchyNode<E> findPosition(E element,HierarchyNode<E> topNode,HierarchyNode<E> bottomNode) {
        Set<HierarchyNode<E>> parentNodes=findParents(element,topNode);
        Set<HierarchyNode<E>> childNodes=findChildren(element,bottomNode,parentNodes);
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
    protected Set<HierarchyNode<E>> findParents(final E element,HierarchyNode<E> topNode) {
        return search(
            new SearchPredicate<HierarchyNode<E>>() {
                public Set<HierarchyNode<E>> getSuccessorElements(HierarchyNode<E> u) {
                    return u.m_childNodes;
                }
                public Set<HierarchyNode<E>> getAncestorElements(HierarchyNode<E> u) {
                    return u.m_parentNodes;
                }
                public boolean trueOf(HierarchyNode<E> u) {
                    return m_hierarchyRelation.doesSubsume(u.getRepresentative(),element);
                }
            },Collections.singleton(topNode),null);
    }

    protected Set<HierarchyNode<E>> findChildren(final E element,HierarchyNode<E> bottomNode,Set<HierarchyNode<E>> parentNodes) {
        if (parentNodes.size()==1 && m_hierarchyRelation.doesSubsume(element,parentNodes.iterator().next().getRepresentative()))
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
                if (node.m_childNodes.contains(bottomNode) && m_hierarchyRelation.doesSubsume(element,node.getRepresentative()))
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
                        public Set<HierarchyNode<E>> getAncestorElements(HierarchyNode<E> u) {
                            return u.m_childNodes;
                        }
                        public boolean trueOf(HierarchyNode<E> u) {
                            return m_hierarchyRelation.doesSubsume(element,u.getRepresentative());
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
        Set<U> getAncestorElements(U u);
        boolean trueOf(U u);
    }

    public static interface ClassificationProgressMonitor<U> {
        void elementClassified(U element);
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
                for (U superordinateElement : m_searchPredicate.getAncestorElements(element)) {
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
