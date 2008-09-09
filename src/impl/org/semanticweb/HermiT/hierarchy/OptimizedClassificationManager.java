// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

import org.semanticweb.HermiT.model.*;

/**
 * This class constructs the subsumption hierarchy by taking the graph of all superclass relationships and then
 * identifying the strongly connected components using Tarjan's algorithm.
 */
public class OptimizedClassificationManager {
    protected final SubsumptionHierarchy m_subsumptionHierarchy;
    protected final SubsumptionHierarchy.SubsumptionChecker m_subsumptionChecker;
    protected final Map<AtomicConcept,Node> m_nodesByConcept;
    protected final Stack<Node> m_stack;
    protected int m_index;
    
    public OptimizedClassificationManager(SubsumptionHierarchy subsumptionHierarchy,SubsumptionHierarchy.SubsumptionChecker subsumptionChecker) {
        m_subsumptionHierarchy=subsumptionHierarchy;
        m_subsumptionChecker=subsumptionChecker;
        m_nodesByConcept=new HashMap<AtomicConcept,Node>();
        m_stack=new Stack<Node>();
    }
    public void buildHierarchy() throws SubsumptionHierarchy.SubusmptionCheckerException {
        for (AtomicConcept atomicConcept : m_subsumptionChecker.getAllAtomicConcepts())
            if (shouldProcessConcept(atomicConcept)) {
                Set<AtomicConcept> superconcepts=m_subsumptionChecker.getAllSubsumers(atomicConcept);
                if (superconcepts==null)
                    m_subsumptionHierarchy.m_nothingNode.m_equivalentConcepts.add(atomicConcept);
                else
                    m_nodesByConcept.put(atomicConcept,new Node(atomicConcept,superconcepts));
            }
        for (Map.Entry<AtomicConcept,Node> entry : m_nodesByConcept.entrySet()) {
            Node node=entry.getValue();
            if (node.m_index==-1)
                findSCCsStartingFrom(node);
        }
        for (SubsumptionHierarchyNode subsumptionHierarchyNode : m_subsumptionHierarchy.m_atomicConceptsToNodes.values()) {
            if (subsumptionHierarchyNode.m_parentNodes.isEmpty()) {
                subsumptionHierarchyNode.m_parentNodes.add(m_subsumptionHierarchy.m_thingNode);
                m_subsumptionHierarchy.m_thingNode.m_childNodes.add(subsumptionHierarchyNode);
            }
            if (subsumptionHierarchyNode.m_childNodes.isEmpty()) {
                subsumptionHierarchyNode.m_childNodes.add(m_subsumptionHierarchy.m_nothingNode);
                m_subsumptionHierarchy.m_nothingNode.m_parentNodes.add(subsumptionHierarchyNode);
            }
        }
    }
    protected void findSCCsStartingFrom(Node node) {
        // assert m_stack.isEmpty(); TODO: figure out what this assertion meant and whether it should work...
        node.m_index=m_index;
        node.m_lowlink=m_index;
        node.m_inStack=true;
        m_stack.push(node);
        m_index++;
        for (AtomicConcept superconcept : node.m_superconcepts) {
            Node superconceptNode=m_nodesByConcept.get(superconcept);
            if (superconceptNode!=null) {
                if (superconceptNode.m_index==-1) {
                    findSCCsStartingFrom(superconceptNode);
                    node.m_lowlink=Math.min(node.m_lowlink,superconceptNode.m_lowlink);
                }
                else if (superconceptNode.m_inStack)
                    node.m_lowlink=Math.min(node.m_lowlink,superconceptNode.m_lowlink);
            }
        }
        if (node.m_index==node.m_lowlink) {
            int index=node.m_index;
            SubsumptionHierarchyNode subsumptionHierarchyNode=m_subsumptionHierarchy.getNodeForEx(node.m_atomicConcept);
            node.m_subsumptionHierarchyNode=subsumptionHierarchyNode;
            Node sccNode;
            do {
                sccNode=m_stack.pop();
                assert sccNode.m_lowlink==index;
                sccNode.m_inStack=false;
                sccNode.m_subsumptionHierarchyNode=subsumptionHierarchyNode;
                subsumptionHierarchyNode.m_equivalentConcepts.add(sccNode.m_atomicConcept);
                m_subsumptionHierarchy.m_atomicConceptsToNodes.put(sccNode.m_atomicConcept,subsumptionHierarchyNode);
                for (AtomicConcept sccSuperconcept : sccNode.m_superconcepts) {
                    Node sccSuperconceptNode=m_nodesByConcept.get(sccSuperconcept);
                    if (sccSuperconceptNode!=null && sccSuperconceptNode.m_lowlink!=index) {
                        SubsumptionHierarchyNode sccSuperconceptSubsumptionHierarchyNode=sccSuperconceptNode.m_subsumptionHierarchyNode;
                        assert sccSuperconceptSubsumptionHierarchyNode!=null;
                        assert sccSuperconceptSubsumptionHierarchyNode!=subsumptionHierarchyNode;
                        subsumptionHierarchyNode.m_parentNodes.add(sccSuperconceptSubsumptionHierarchyNode);
                        sccSuperconceptSubsumptionHierarchyNode.m_childNodes.add(subsumptionHierarchyNode);
                    }
                }
            } while (node!=sccNode);
        }
    }
    protected boolean shouldProcessConcept(AtomicConcept atomicConcept) {
        return !atomicConcept.getURI().startsWith("internal:") && !AtomicConcept.THING.equals(atomicConcept) && !AtomicConcept.NOTHING.equals(atomicConcept);
    }
    
    protected static class Node {
        protected final AtomicConcept m_atomicConcept;
        protected final Set<AtomicConcept> m_superconcepts;
        protected int m_index;
        protected int m_lowlink;
        protected boolean m_inStack;
        protected SubsumptionHierarchyNode m_subsumptionHierarchyNode;
        
        public Node(AtomicConcept atomicConcept,Set<AtomicConcept> superconcepts) {
            m_atomicConcept=atomicConcept;
            m_superconcepts=superconcepts;
            m_index=-1;
            m_lowlink=-1;
            m_inStack=false;
        }
    }
}
