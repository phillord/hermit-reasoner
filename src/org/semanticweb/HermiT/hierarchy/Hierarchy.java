package org.semanticweb.HermiT.hierarchy;

import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class Hierarchy<E> {
    protected final HierarchyNode<E> m_topNode;
    protected final HierarchyNode<E> m_bottomNode;
    protected final Map<E,HierarchyNode<E>> m_nodesByElements;
    
    public Hierarchy(HierarchyNode<E> topNode,HierarchyNode<E> bottomNode) {
        m_topNode=topNode;
        m_bottomNode=bottomNode;
        m_nodesByElements=new HashMap<E,HierarchyNode<E>>();
        for (E element : m_topNode.m_equivalentElements)
            m_nodesByElements.put(element,m_topNode);
        for (E element : m_bottomNode.m_equivalentElements)
            m_nodesByElements.put(element,m_bottomNode);
    }
    public HierarchyNode<E> getTopNode() {
        return m_topNode;
    }
    public HierarchyNode<E> getBottomNode() {
        return m_bottomNode;
    }
    public HierarchyNode<E> getNodeForElement(E element) {
        return m_nodesByElements.get(element);
    }
    public Collection<HierarchyNode<E>> getAllNodes() {
        return Collections.unmodifiableCollection(m_nodesByElements.values());
    }
    public Set<E> getAllElements() {
        return Collections.unmodifiableSet(m_nodesByElements.keySet());
    }
    public <T> Hierarchy<T> transform(Transformer<E,T> transformer,Comparator<T> comparator) {
        HierarchyNodeComparator<T> newNodeComparator=new HierarchyNodeComparator<T>(comparator);
        Map<HierarchyNode<E>,HierarchyNode<T>> oldToNew=new HashMap<HierarchyNode<E>,HierarchyNode<T>>();
        for (HierarchyNode<E> oldNode : m_nodesByElements.values()) {
            Set<T> newEquivalentElements;
            Set<HierarchyNode<T>> newParentNodes;
            Set<HierarchyNode<T>> newChildNodes;
            if (comparator==null) {
                newEquivalentElements=new HashSet<T>();
                newParentNodes=new HashSet<HierarchyNode<T>>();
                newChildNodes=new HashSet<HierarchyNode<T>>();
            }
            else {
                newEquivalentElements=new TreeSet<T>(comparator);
                newParentNodes=new TreeSet<HierarchyNode<T>>(newNodeComparator);
                newChildNodes=new TreeSet<HierarchyNode<T>>(newNodeComparator);
            }
            for (E oldElement : oldNode.m_equivalentElements) {
                T newElement=transformer.transform(oldElement);
                newEquivalentElements.add(newElement);
            }
            T newRepresentative=transformer.determineRepresentative(oldNode.m_representative,newEquivalentElements);
            HierarchyNode<T> newNode=new HierarchyNode<T>(newRepresentative,newEquivalentElements,newParentNodes,newChildNodes);
            oldToNew.put(oldNode,newNode);
        }
        for (HierarchyNode<E> oldParentNode : m_nodesByElements.values()) {
            HierarchyNode<T> newParentNode=oldToNew.get(oldParentNode);
            for (HierarchyNode<E> oldChildNode : oldParentNode.m_childNodes) {
                HierarchyNode<T> newChildNode=oldToNew.get(oldChildNode);
                newParentNode.m_childNodes.add(newChildNode);
                newChildNode.m_parentNodes.add(newParentNode);
            }
        }
        HierarchyNode<T> newTopNode=oldToNew.get(m_topNode);
        HierarchyNode<T> newBottomNode=oldToNew.get(m_bottomNode);
        Hierarchy<T> newHierarchy=new Hierarchy<T>(newTopNode,newBottomNode);
        for (HierarchyNode<T> newNode : oldToNew.values())
            for (T newElement : newNode.m_equivalentElements)
                newHierarchy.m_nodesByElements.put(newElement,newNode);
        return newHierarchy;
    }
    public void traverseDepthFirst(HierarchyNodeVisitor<E> visitor) {
        Set<HierarchyNode<E>> visited=new HashSet<HierarchyNode<E>>();
        traverseDepthFirst(visitor,0,m_topNode,null,visited);
    }
    protected void traverseDepthFirst(HierarchyNodeVisitor<E> visitor,int level,HierarchyNode<E> node,HierarchyNode<E> parentNode,Set<HierarchyNode<E>> visited) {
        boolean firstVisit=visited.add(node);
        visitor.visit(level,node,parentNode,firstVisit);
        if (firstVisit)
            for (HierarchyNode<E> childNode : node.m_childNodes)
                traverseDepthFirst(visitor,level+1,childNode,node,visited);
    }
    public static <T> Hierarchy<T> emptyHierarchy(Collection<T> elements,T topElement,T bottomElement) {
        HierarchyNode<T> topBottomNode=new HierarchyNode<T>(topElement);
        topBottomNode.m_equivalentElements.add(topElement);
        topBottomNode.m_equivalentElements.add(bottomElement);
        topBottomNode.m_equivalentElements.addAll(elements);
        return new Hierarchy<T>(topBottomNode,topBottomNode);
    }

    protected static interface HierarchyNodeVisitor<E> {
        void visit(int level,HierarchyNode<E> node,HierarchyNode<E> parentNode,boolean firstVisit);
    }

    protected static interface Transformer<E,T> {
        T transform(E element);
        T determineRepresentative(E oldRepresentative,Set<T> newEquivalentElements);
    }
    
    protected static class HierarchyNodeComparator<E> implements Comparator<HierarchyNode<E>> {
        protected final Comparator<E> m_elementComparator;

        public HierarchyNodeComparator(Comparator<E> elementComparator) {
            m_elementComparator=elementComparator;
        }
        public int compare(HierarchyNode<E> n1,HierarchyNode<E> n2) {
            return m_elementComparator.compare(n1.m_representative,n2.m_representative);
        }
        
    }
}
