package org.semanticweb.HermiT.hierarchy;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

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
    public static <T> Hierarchy<T> emptyHierarchy(Collection<T> elements,T topElement,T bottomElement) {
        HierarchyNode<T> topBottomNode=new HierarchyNode<T>(topElement);
        topBottomNode.m_equivalentElements.add(topElement);
        topBottomNode.m_equivalentElements.add(bottomElement);
        topBottomNode.m_equivalentElements.addAll(elements);
        return new Hierarchy<T>(topBottomNode,topBottomNode);
    }
}
