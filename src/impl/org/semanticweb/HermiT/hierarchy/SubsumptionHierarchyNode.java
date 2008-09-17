// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.model.*;

/**
 * Represents a node in the hierarchy.
 */
public class SubsumptionHierarchyNode implements Serializable {
    private static final long serialVersionUID=4922639552505751770L;

    protected final AtomicConcept m_representativeConcept;
    protected final Set<AtomicConcept> m_equivalentConcepts;
    protected final Set<SubsumptionHierarchyNode> m_parentNodes;
    protected final Set<SubsumptionHierarchyNode> m_childNodes;

    public SubsumptionHierarchyNode(AtomicConcept representativeConcept) {
        m_representativeConcept=representativeConcept;
        m_equivalentConcepts=new HashSet<AtomicConcept>(2);
        m_equivalentConcepts.add(m_representativeConcept);
        m_parentNodes=new HashSet<SubsumptionHierarchyNode>();
        m_childNodes=new HashSet<SubsumptionHierarchyNode>();
    }
    public AtomicConcept getRepresentative() {
        return m_representativeConcept;
    }
    public Set<AtomicConcept> getEquivalentConcepts() {
        return m_equivalentConcepts;
    }
    public Set<SubsumptionHierarchyNode> getParentNodes() {
        return m_parentNodes;
    }
    public Set<SubsumptionHierarchyNode> getChildNodes() {
        return m_childNodes;
    }
    public Set<SubsumptionHierarchyNode> getAncestorNodes() {
        List<SubsumptionHierarchyNode> queue=new LinkedList<SubsumptionHierarchyNode>();
        queue.add(this);
        Set<SubsumptionHierarchyNode> result=new HashSet<SubsumptionHierarchyNode>();
        while (!queue.isEmpty()) {
            SubsumptionHierarchyNode node=queue.remove(0);
            for (SubsumptionHierarchyNode parentNode : node.getParentNodes()) {
                if (result.add(parentNode))
                    queue.add(parentNode);
            }
        }
        return result;
    }
    public Set<SubsumptionHierarchyNode> getDescendantNodes() {
        List<SubsumptionHierarchyNode> queue=new LinkedList<SubsumptionHierarchyNode>();
        queue.add(this);
        Set<SubsumptionHierarchyNode> result=new HashSet<SubsumptionHierarchyNode>();
        while (!queue.isEmpty()) {
            SubsumptionHierarchyNode node=queue.remove(0);
            for (SubsumptionHierarchyNode childNode : node.getChildNodes()) {
                if (result.add(childNode))
                    queue.add(childNode);
            }
        }
        return result;
    }
    public String toString(Namespaces namespaces) {
        StringBuffer buffer=new StringBuffer();
        buffer.append("{ ");
        boolean first=true;
        for (AtomicConcept atomicConcept : m_equivalentConcepts) {
            if (first)
                first=false;
            else
                buffer.append(", ");
            buffer.append(atomicConcept.toString(namespaces));
        }
        buffer.append(" }");
        return buffer.toString();
    }
    public String toString() {
        return toString(Namespaces.none);
    }
}
