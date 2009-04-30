package org.semanticweb.HermiT.hierarchy;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

public class DeterministicHierarchyBuilder<T> {
    protected final Map<T,GraphNode<T>> m_graphNodes;
    protected final T m_topElement;
    protected final T m_bottomElement;
    protected final Hierarchy<T> m_hierarchy;
    protected final Stack<GraphNode<T>> m_stack;
    protected final List<HierarchyNode<T>> m_topologicalOrder;
    protected int m_dfsIndex;
    
    public DeterministicHierarchyBuilder(Map<T,GraphNode<T>> graphNodes,T topElement,T bottomElement) {
        m_graphNodes=graphNodes;
        m_topElement=topElement;
        m_bottomElement=bottomElement;
        HierarchyNode<T> topNode=new HierarchyNode<T>(m_topElement);
        HierarchyNode<T> bottomNode=new HierarchyNode<T>(m_bottomElement);
        m_hierarchy=new Hierarchy<T>(topNode,bottomNode);
        m_stack=new Stack<GraphNode<T>>();
        m_topologicalOrder=new ArrayList<HierarchyNode<T>>();
    }
    public Hierarchy<T> buildHierarchy() {
        // Compute SCCs, create hierarchy nodes, and topologically order them
        visit(m_graphNodes.get(m_bottomElement));
        // Process the nodes in the topological order
        Map<HierarchyNode<T>,Set<HierarchyNode<T>>> reachableFrom=new HashMap<HierarchyNode<T>,Set<HierarchyNode<T>>>();
        List<GraphNode<T>> allSuccessors=new ArrayList<GraphNode<T>>();
        for (int index=0;index<m_topologicalOrder.size();index++) {
            HierarchyNode<T> node=m_topologicalOrder.get(index);
            Set<HierarchyNode<T>> reachableFromNode=new HashSet<HierarchyNode<T>>();
            reachableFromNode.add(node);
            reachableFrom.put(node,reachableFromNode);
            allSuccessors.clear();
            for (T element : node.m_equivalentElements) {
                GraphNode<T> graphNode=m_graphNodes.get(element);
                for (T successor : graphNode.m_successors) {
                    GraphNode<T> successorGraphNode=m_graphNodes.get(successor);
                    if (successorGraphNode!=null)
                        allSuccessors.add(successorGraphNode);
                }
            }
            Collections.sort(allSuccessors,TopologicalOrderComparator.INSTANCE);
            for (int successorIndex=allSuccessors.size()-1;successorIndex>=0;--successorIndex) {
                GraphNode<T> successorGraphNode=allSuccessors.get(successorIndex);
                HierarchyNode<T> successorNode=m_hierarchy.m_nodesByElements.get(successorGraphNode.m_element);
                if (!reachableFromNode.contains(successorNode)) {
                    node.m_parentNodes.add(successorNode);
                    successorNode.m_childNodes.add(node);
                    reachableFromNode.add(successorNode);
                    reachableFromNode.addAll(reachableFrom.get(successorNode));
                }
            }
        }
        return m_hierarchy;
    }
    protected void visit(GraphNode<T> graphNode) {
        graphNode.m_dfsIndex=m_dfsIndex++;
        graphNode.m_SCChead=graphNode;
        m_stack.push(graphNode);
        for (T successor : graphNode.m_successors) {
            GraphNode<T> successorGraphNode=m_graphNodes.get(successor);
            if (successorGraphNode!=null) {
                if (successorGraphNode.notVisited())
                    visit(successorGraphNode);
                if (!successorGraphNode.isAssignedToSCC() && successorGraphNode.m_SCChead.m_dfsIndex<graphNode.m_SCChead.m_dfsIndex)
                    graphNode.m_SCChead=successorGraphNode.m_SCChead;
            }
        }
        if (graphNode.m_SCChead==graphNode) {
            int nextTopologicalOrderIndex=m_topologicalOrder.size();
            Set<T> equivalentElements=new HashSet<T>();
            GraphNode<T> poppedNode;
            do {
                poppedNode=m_stack.pop();
                poppedNode.m_topologicalOrderIndex=nextTopologicalOrderIndex;
                equivalentElements.add(poppedNode.m_element);
                
            } while (poppedNode!=graphNode);
            HierarchyNode<T> hierarchyNode;
            if (equivalentElements.contains(m_topElement))
                hierarchyNode=m_hierarchy.getTopNode();
            else if (equivalentElements.contains(m_bottomElement))
                hierarchyNode=m_hierarchy.getBottomNode();
            else
                hierarchyNode=new HierarchyNode<T>(graphNode.m_element);
            for (T element : equivalentElements) {
                hierarchyNode.m_equivalentElements.add(element);
                m_hierarchy.m_nodesByElements.put(element,hierarchyNode);
            }
            m_topologicalOrder.add(hierarchyNode);
        }
    }
    
    public static class GraphNode<T> {
        public final T m_element;
        public final Set<T> m_successors;
        public int m_dfsIndex;
        public GraphNode<T> m_SCChead;
        public int m_topologicalOrderIndex;
        
        public GraphNode(T element,Set<T> successors) {
            m_element=element;
            m_successors=successors;
            m_dfsIndex=-1;
            m_SCChead=null;
            m_topologicalOrderIndex=-1;
        }
        public boolean notVisited() {
            return m_dfsIndex==-1;
        }
        public boolean isAssignedToSCC() {
            return m_topologicalOrderIndex!=-1;
        }
    }

    protected static class TopologicalOrderComparator implements Comparator<GraphNode<?>> {
        public static final TopologicalOrderComparator INSTANCE=new TopologicalOrderComparator();

        public int compare(GraphNode<?> o1,GraphNode<?> o2) {
            return o1.m_topologicalOrderIndex-o2.m_topologicalOrderIndex;
        }
        
    }
}
