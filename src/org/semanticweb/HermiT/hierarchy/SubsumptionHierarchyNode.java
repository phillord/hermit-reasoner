package org.semanticweb.HermiT.hierarchy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Represents a node in the hierarchy.
 */
public class SubsumptionHierarchyNode implements Serializable {
    private static final long serialVersionUID=4922639552505751770L;

    protected final String m_representativeURI;
    protected final Set<String> m_owlClasses;
    protected final Set<SubsumptionHierarchyNode> m_parentNodes;
    protected final Set<SubsumptionHierarchyNode> m_childNodes;

    public SubsumptionHierarchyNode(String representativeURI) {
        m_representativeURI=representativeURI;
        m_owlClasses=new HashSet<String>(2);
        m_owlClasses.add(m_representativeURI);
        m_parentNodes=new HashSet<SubsumptionHierarchyNode>();
        m_childNodes=new HashSet<SubsumptionHierarchyNode>();
    }
    public Set<String> getAtomicConcepts() {
        return m_owlClasses;
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
    public String toString() {
        return m_owlClasses.toString();
    }
}
