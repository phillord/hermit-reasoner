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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;

public class DeterministicClassification {
    protected final Tableau m_tableau;
    protected final ClassificationProgressMonitor m_progressMonitor;
    protected final AtomicConcept m_topElement;
    protected final AtomicConcept m_bottomElement;
    protected final Set<AtomicConcept> m_elements;

    public DeterministicClassification(Tableau tableau,ClassificationProgressMonitor progressMonitor,AtomicConcept topElement,AtomicConcept bottomElement,Set<AtomicConcept> elements) {
        m_tableau=tableau;
        m_progressMonitor=progressMonitor;
        m_topElement=topElement;
        m_bottomElement=bottomElement;
        m_elements=elements;
    }
    public Hierarchy<AtomicConcept> classify() {
        if (!m_tableau.isDeterministic())
            throw new IllegalStateException("Internal error: DeterministicClassificationManager can be used only with a deterministic tableau.");
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        if (!m_tableau.isSatisfiable(true,Collections.singleton(Atom.create(m_topElement,freshIndividual)),null,null,null,null,ReasoningTaskDescription.isConceptSatisfiable(m_topElement)))
            return Hierarchy.emptyHierarchy(m_elements,m_topElement,m_bottomElement);
        Map<AtomicConcept,GraphNode<AtomicConcept>> allSubsumers=new HashMap<AtomicConcept,GraphNode<AtomicConcept>>();
        for (AtomicConcept element : m_elements) {
            Set<AtomicConcept> subsumers;
            Map<Individual,Node> nodesForIndividuals=new HashMap<Individual,Node>();
            nodesForIndividuals.put(freshIndividual,null);
            if (!m_tableau.isSatisfiable(true,Collections.singleton(Atom.create(element,freshIndividual)),null,null,null,nodesForIndividuals,ReasoningTaskDescription.isConceptSatisfiable(element)))
                subsumers=m_elements;
            else {
                subsumers=new HashSet<AtomicConcept>();
                subsumers.add(m_topElement);
                ExtensionTable.Retrieval retrieval=m_tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
                retrieval.getBindingsBuffer()[1]=nodesForIndividuals.get(freshIndividual).getCanonicalNode();
                retrieval.open();
                while (!retrieval.afterLast()) {
                    Object subsumer=retrieval.getTupleBuffer()[0];
                    if (subsumer instanceof AtomicConcept && m_elements.contains(subsumer))
                        subsumers.add((AtomicConcept)subsumer);
                    retrieval.next();
                }
            }
            allSubsumers.put(element,new GraphNode<AtomicConcept>(element,subsumers));
            m_progressMonitor.elementClassified(element);
        }
        return buildHierarchy(m_topElement,m_bottomElement,allSubsumers);
    }
    public static <T> Hierarchy<T> buildHierarchy(T topElement,T bottomElement,Map<T,GraphNode<T>> graphNodes) {
        HierarchyNode<T> topNode=new HierarchyNode<T>(topElement);
        HierarchyNode<T> bottomNode=new HierarchyNode<T>(bottomElement);
        Hierarchy<T> hierarchy=new Hierarchy<T>(topNode,bottomNode);
        // Compute SCCs (strongly connected components), create hierarchy nodes, and topologically order them
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
