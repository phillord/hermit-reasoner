package org.semanticweb.HermiT.hierarchy;

import java.util.Map;
import java.util.Set;
import java.util.Collection;

import org.semanticweb.HermiT.util.GraphUtils;

public class OptimizedHierarchyBuilder {
    public static <T> Hierarchy<T> buildHierarchy(Map<T,Set<T>> knownSubsumers,Collection<T> elements,T topElement,T bottomElement) {
        GraphUtils.Acyclic<T> acyc=new GraphUtils.Acyclic<T>(knownSubsumers);
        GraphUtils.TransAnalyzed<T> trans=new GraphUtils.TransAnalyzed<T>(acyc.graph);
        HierarchyNode<T> topNode=new HierarchyNode<T>();
        topNode.m_equivalentElements.addAll(acyc.equivs.get(acyc.canonical.get(topElement)));
        HierarchyNode<T> bottomNode=new HierarchyNode<T>();
        bottomNode.m_equivalentElements.addAll(acyc.equivs.get(acyc.canonical.get(bottomElement)));
        Hierarchy<T> result=new Hierarchy<T>(topNode,bottomNode);
        for (T element : elements) {
            T canonicalElement=acyc.canonical.get(element);
            HierarchyNode<T> hierarchyNode=result.m_nodesByElements.get(canonicalElement);
            if (hierarchyNode==null) {
                hierarchyNode=new HierarchyNode<T>();
                hierarchyNode.m_equivalentElements.addAll(acyc.equivs.get(canonicalElement));
                result.m_nodesByElements.put(canonicalElement,hierarchyNode);
            }
            result.m_nodesByElements.put(element,hierarchyNode);
        }
        for (Map.Entry<T,Set<T>> entry : trans.reduced.entrySet()) {
            HierarchyNode<T> childNode=result.m_nodesByElements.get(entry.getKey());
            for (T parentElement : entry.getValue()) {
                HierarchyNode<T> parentNode=result.m_nodesByElements.get(parentElement);
                childNode.m_parentNodes.add(parentNode);
                parentNode.m_childNodes.add(childNode);
            }
        }
        return result;
    }
}
