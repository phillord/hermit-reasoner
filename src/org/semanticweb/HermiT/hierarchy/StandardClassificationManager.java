// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.InternalNames;
import org.semanticweb.HermiT.model.AtomicConcept;

public class StandardClassificationManager implements Serializable {
    private static final long serialVersionUID = -3348763284557840912L;
    protected static enum SubsumptionType { POSITIVE,NEGATIVE };

    protected final SubsumptionHierarchy m_subsumptionHierarchy;
    protected final SubsumptionHierarchy.SubsumptionChecker m_subsumptionChecker;
    protected final Set<SubsumptionHierarchyNode> m_visitedNodes;
    public final List<SubsumptionHierarchyNode> m_topSet;
    public final List<SubsumptionHierarchyNode> m_bottomSet;
    protected final List<SubsumptionHierarchyNode> m_toProcess;
    protected final List<SubsumptionHierarchyNode> m_relevantBottomParents;
    protected final Map<SubsumptionHierarchyNode,SubsumptionType> m_subsumptionCache;
    
    public StandardClassificationManager(SubsumptionHierarchy subsumptionHierarchy,SubsumptionHierarchy.SubsumptionChecker subsumptionChecker) {
        m_subsumptionHierarchy=subsumptionHierarchy;
        m_subsumptionChecker=subsumptionChecker;
        m_visitedNodes=new HashSet<SubsumptionHierarchyNode>();
        m_topSet=new ArrayList<SubsumptionHierarchyNode>();
        m_bottomSet=new ArrayList<SubsumptionHierarchyNode>();
        m_toProcess=new ArrayList<SubsumptionHierarchyNode>();
        m_relevantBottomParents=new ArrayList<SubsumptionHierarchyNode>();
        m_subsumptionCache=new HashMap<SubsumptionHierarchyNode,SubsumptionType>();
    }
    public void buildHierarchy() throws SubsumptionHierarchy.SubusmptionCheckerException {
        for (AtomicConcept atomicConcept : m_subsumptionChecker.getAllAtomicConcepts()) {
            if (!InternalNames.isInternalUri(atomicConcept.getURI())) {
                insertConcept(atomicConcept);
            }
        }
    }
    public void findPosition(AtomicConcept atomicConcept) throws SubsumptionHierarchy.SubusmptionCheckerException {
        topSeach(atomicConcept);
        if (m_topSet.size()==1) {
            SubsumptionHierarchyNode node=m_topSet.get(0);
            if (m_subsumptionChecker.isSubsumedBy(node.m_representativeConcept,atomicConcept)) {
                m_bottomSet.clear();
                m_bottomSet.add(node);
            } else if (node.m_childNodes.size()==1 && node.m_childNodes.contains(m_subsumptionHierarchy.m_nothingNode)) {
                m_bottomSet.clear();
                m_bottomSet.add(m_subsumptionHierarchy.m_nothingNode);
            } else {
                bottomSearch(atomicConcept);
            }
        } else {
            bottomSearch(atomicConcept);
        }
    }
    
    protected void insertConcept(AtomicConcept atomicConcept) throws SubsumptionHierarchy.SubusmptionCheckerException {
        findPosition(atomicConcept);
        if (m_topSet.equals(m_bottomSet)) {
            assert m_topSet.size() == 1;
            m_topSet.get(0).m_equivalentConcepts.add(atomicConcept);
            m_subsumptionHierarchy.m_atomicConceptsToNodes.put(atomicConcept,m_topSet.get(0));
            return;
        }
        SubsumptionHierarchyNode newNode=m_subsumptionHierarchy.getNodeForEx(atomicConcept);
        for (int topIndex=0;topIndex<m_topSet.size();topIndex++) {
            SubsumptionHierarchyNode topNode=m_topSet.get(topIndex);
            assert newNode != m_subsumptionHierarchy.thingNode()
                : "Thing can't have parents!";
            assert topNode != m_subsumptionHierarchy.nothingNode()
                : "Nothing can't be a parent";
            for (int bottomIndex=0;bottomIndex<m_bottomSet.size();bottomIndex++)
                topNode.m_childNodes.remove(m_bottomSet.get(bottomIndex));
            topNode.m_childNodes.add(newNode);
            newNode.m_parentNodes.add(topNode);
        }
        for (int bottomIndex=0;bottomIndex<m_bottomSet.size();bottomIndex++) {
            SubsumptionHierarchyNode bottomNode=m_bottomSet.get(bottomIndex);
            assert newNode != m_subsumptionHierarchy.nothingNode()
                : "Nothing can't have children";
            assert bottomNode != m_subsumptionHierarchy.thingNode()
                : "Thing can't be a child";
            for (int topIndex=0;topIndex<m_topSet.size();topIndex++)
                bottomNode.m_parentNodes.remove(m_topSet.get(topIndex));
            bottomNode.m_parentNodes.add(newNode);
            newNode.m_childNodes.add(bottomNode);
        }
    }
    protected void topSeach(AtomicConcept atomicConcept) throws SubsumptionHierarchy.SubusmptionCheckerException {
        m_topSet.clear();
        m_visitedNodes.clear();
        m_toProcess.clear();
        m_subsumptionCache.clear();
        m_toProcess.add(m_subsumptionHierarchy.m_thingNode);
        int processIndex=0;
        while (processIndex<m_toProcess.size()) {
            SubsumptionHierarchyNode subsumer=m_toProcess.get(processIndex++);
            if (m_visitedNodes.add(subsumer)) {
                boolean childSubsumes=false;
                for (SubsumptionHierarchyNode child : subsumer.m_childNodes) {
                    if (topSubsumedBy(atomicConcept,child)) {
                        childSubsumes=true;
                        if (!m_visitedNodes.contains(child))
                            m_toProcess.add(child);
                    }
                }
                if (!childSubsumes)
                    m_topSet.add(subsumer);
            }
        }
    }
    protected boolean topSubsumedBy(AtomicConcept atomicConcept,SubsumptionHierarchyNode node) throws SubsumptionHierarchy.SubusmptionCheckerException {
        SubsumptionType subsumptionType=m_subsumptionCache.get(node);
        if (subsumptionType==SubsumptionType.POSITIVE)
            return true;
        else if (subsumptionType==SubsumptionType.NEGATIVE)
            return false;
        else {
            boolean subsumedByAllParents=true;
            if (node.m_parentNodes.size()>1) {
                for (SubsumptionHierarchyNode parent : node.m_parentNodes)
                    if (!topSubsumedBy(atomicConcept,parent)) {
                        subsumedByAllParents=false;
                        break;
                    }
            }
            boolean result;
            if (!subsumedByAllParents)
                result=false;
            else
                result=m_subsumptionChecker.isSubsumedBy(atomicConcept,node.m_representativeConcept);
            m_subsumptionCache.put(node,result ? SubsumptionType.POSITIVE : SubsumptionType.NEGATIVE);
            return result;
        }
    }
    protected void bottomSearch(AtomicConcept atomicConcept) throws SubsumptionHierarchy.SubusmptionCheckerException {
        m_relevantBottomParents.clear();
        m_subsumptionCache.clear();
        for (int topIndex=0;topIndex<m_topSet.size();topIndex++) {
            SubsumptionHierarchyNode topNode=m_topSet.get(topIndex);
            depthFirstSearch(topNode);
        }
        m_bottomSet.clear();
        m_visitedNodes.clear();
        m_toProcess.clear();
        m_toProcess.add(m_subsumptionHierarchy.m_nothingNode);
        int processIndex=0;
        while (processIndex<m_toProcess.size()) {
            SubsumptionHierarchyNode subsumee=m_toProcess.get(processIndex++);
            if (m_visitedNodes.add(subsumee)) {
                boolean parentSubsumer=false;
                Collection<SubsumptionHierarchyNode> relevantParents=(subsumee==m_subsumptionHierarchy.m_nothingNode ? m_relevantBottomParents : subsumee.m_parentNodes);
                for (SubsumptionHierarchyNode parent : relevantParents) {
                    if (m_subsumptionCache.containsKey(parent) && bottomSubsumes(atomicConcept,parent)) {
                        parentSubsumer=true;
                        if (!m_visitedNodes.contains(parent))
                            m_toProcess.add(parent);
                    }
                }
                if (!parentSubsumer)
                    m_bottomSet.add(subsumee);
            }
        }
    }
    protected void depthFirstSearch(SubsumptionHierarchyNode node) {
        m_subsumptionCache.put(node,null); // This is a dummy marker saying that a node is on a path between a common child of all elements of the top set and bottom
        if (node!=m_subsumptionHierarchy.m_nothingNode) {
            for (SubsumptionHierarchyNode childNode : node.m_childNodes) {
                if (!m_subsumptionCache.containsKey(childNode))
                    depthFirstSearch(childNode);
            }
            if (node.m_childNodes.size()==1 && node.m_childNodes.contains(m_subsumptionHierarchy.m_nothingNode))
                m_relevantBottomParents.add(node);
        }
    }
    protected boolean bottomSubsumes(AtomicConcept atomicConcept,SubsumptionHierarchyNode node) throws SubsumptionHierarchy.SubusmptionCheckerException {
        SubsumptionType subsumptionType=m_subsumptionCache.get(node);
        if (subsumptionType==SubsumptionType.POSITIVE)
            return true;
        else if (subsumptionType==SubsumptionType.NEGATIVE)
            return false;
        else {
            boolean subsumesAllChildren=true;
            if (node.m_childNodes.size()>1) {
                for (SubsumptionHierarchyNode child : node.m_childNodes)
                    if (!bottomSubsumes(atomicConcept,child)) {
                        subsumesAllChildren=false;
                        break;
                    }
            }
            boolean result;
            if (!subsumesAllChildren)
                result=false;
            else
                result=m_subsumptionChecker.isSubsumedBy(node.m_representativeConcept,atomicConcept);
            m_subsumptionCache.put(node,result ? SubsumptionType.POSITIVE : SubsumptionType.NEGATIVE);
            return result;
        }
    }
}
