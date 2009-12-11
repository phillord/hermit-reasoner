package org.semanticweb.HermiT.hierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class DeterministicClassificationManager<E> implements ClassificationManager<E> {
    protected final SubsumptionCache<E> m_subsumptionCache;
    
    public DeterministicClassificationManager(SubsumptionCache<E> subsumptionCache) {
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
        Map<E,GraphNode<E>> allSubsumers=new HashMap<E,GraphNode<E>>();
        for (E element : elements) {
            Set<E> subsumers=m_subsumptionCache.getAllKnownSubsumers(element);
            if (subsumers==null)
                subsumers=elements;
            allSubsumers.put(element,new GraphNode<E>(element,subsumers));
            progressMonitor.elementClassified(element);
        }
        return buildHierarchy(topElement,bottomElement,allSubsumers);
    }
    public static <T> Hierarchy<T> buildHierarchy(T topElement,T bottomElement,Map<T,GraphNode<T>> graphNodes) {
        HierarchyNode<T> topNode=new HierarchyNode<T>(topElement);
        HierarchyNode<T> bottomNode=new HierarchyNode<T>(bottomElement);
        Hierarchy<T> hierarchy=new Hierarchy<T>(topNode,bottomNode);
        // Compute SCCs, create hierarchy nodes, and topologically order them
        List<HierarchyNode<T>> topologicalOrder=new ArrayList<HierarchyNode<T>>();
        visit(new Stack<GraphNode<T>>(),new DFSIndex(),graphNodes,graphNodes.get(bottomElement),hierarchy,topologicalOrder);
        // Process the nodes in the topological order
        Map<HierarchyNode<T>,Set<HierarchyNode<T>>> reachableFrom=new HashMap<HierarchyNode<T>,Set<HierarchyNode<T>>>();
        List<GraphNode<T>> allSuccessors=new ArrayList<GraphNode<T>>();
        for (int index=0;index<topologicalOrder.size();index++) {
            HierarchyNode<T> node=topologicalOrder.get(index);
            Set<HierarchyNode<T>> reachableFromNode=new HashSet<HierarchyNode<T>>();
            reachableFromNode.add(node);
            reachableFrom.put(node,reachableFromNode);
            allSuccessors.clear();
            for (T element : node.m_equivalentElements) {
                GraphNode<T> graphNode=graphNodes.get(element);
                for (T successor : graphNode.m_successors) {
                    GraphNode<T> successorGraphNode=graphNodes.get(successor);
                    if (successorGraphNode!=null)
                        allSuccessors.add(successorGraphNode);
                }
            }
            Collections.sort(allSuccessors,TopologicalOrderComparator.INSTANCE);
            for (int successorIndex=allSuccessors.size()-1;successorIndex>=0;--successorIndex) {
                GraphNode<T> successorGraphNode=allSuccessors.get(successorIndex);
                HierarchyNode<T> successorNode=hierarchy.m_nodesByElements.get(successorGraphNode.m_element);
                if (!reachableFromNode.contains(successorNode)) {
                    node.m_parentNodes.add(successorNode);
                    successorNode.m_childNodes.add(node);
                    reachableFromNode.add(successorNode);
                    reachableFromNode.addAll(reachableFrom.get(successorNode));
                }
            }
        }
        return hierarchy;
    }
    protected static <T> void visit(Stack<GraphNode<T>> stack,DFSIndex dfsIndex,Map<T,GraphNode<T>> graphNodes,GraphNode<T> graphNode,Hierarchy<T> hierarchy,List<HierarchyNode<T>> topologicalOrder) {
        graphNode.m_dfsIndex=dfsIndex.m_value++;
        graphNode.m_SCChead=graphNode;
        stack.push(graphNode);
        for (T successor : graphNode.m_successors) {
            GraphNode<T> successorGraphNode=graphNodes.get(successor);
            if (successorGraphNode!=null) {
                if (successorGraphNode.notVisited())
                    visit(stack,dfsIndex,graphNodes,successorGraphNode,hierarchy,topologicalOrder);
                if (!successorGraphNode.isAssignedToSCC() && successorGraphNode.m_SCChead.m_dfsIndex<graphNode.m_SCChead.m_dfsIndex)
                    graphNode.m_SCChead=successorGraphNode.m_SCChead;
            }
        }
        if (graphNode.m_SCChead==graphNode) {
            int nextTopologicalOrderIndex=topologicalOrder.size();
            Set<T> equivalentElements=new HashSet<T>();
            GraphNode<T> poppedNode;
            do {
                poppedNode=stack.pop();
                poppedNode.m_topologicalOrderIndex=nextTopologicalOrderIndex;
                equivalentElements.add(poppedNode.m_element);
                
            } while (poppedNode!=graphNode);
            HierarchyNode<T> hierarchyNode;
            if (equivalentElements.contains(hierarchy.getTopNode().m_representative))
                hierarchyNode=hierarchy.getTopNode();
            else if (equivalentElements.contains(hierarchy.getBottomNode().m_representative))
                hierarchyNode=hierarchy.getBottomNode();
            else
                hierarchyNode=new HierarchyNode<T>(graphNode.m_element);
            for (T element : equivalentElements) {
                hierarchyNode.m_equivalentElements.add(element);
                hierarchy.m_nodesByElements.put(element,hierarchyNode);
            }
            topologicalOrder.add(hierarchyNode);
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

    protected static class DFSIndex {
        public int m_value;
    }
}
