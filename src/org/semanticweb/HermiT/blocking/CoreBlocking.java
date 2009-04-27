// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class CoreBlocking implements BlockingStrategy,Serializable {
    private static final long serialVersionUID=-2959900333817197464L;

    protected final CoreBlockingCache m_currentBlockersCache;
    protected final SetFactory<AtomicConcept> m_atomicConceptsSetFactory;
    protected final List<AtomicConcept> m_atomicConceptsBuffer;
    protected final Object[] m_auxiliaryTuple;
    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;
    protected ExtensionTable.Retrieval m_binaryTableSearch1Bound;
    protected Node m_firstChangedNode;

    public CoreBlocking() {
        m_currentBlockersCache=new CoreBlockingCache();
        m_atomicConceptsSetFactory=new SetFactory<AtomicConcept>();
        m_atomicConceptsBuffer=new ArrayList<AtomicConcept>();
        m_auxiliaryTuple=new Object[2];
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=m_tableau.getExtensionManager();
        m_binaryTableSearch1Bound=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
    }
    public void clear() {
        m_currentBlockersCache.clear();
        m_firstChangedNode=null;
    }
    public void computeBlocking(boolean finalChance) {
        if (m_firstChangedNode!=null) {
            Node node=m_firstChangedNode;
            while (node!=null) {
                if (!node.isBlocked() && canBeBlocker(node))
                    m_currentBlockersCache.removeNode(node);
                node=node.getNextTableauNode();
            }
            node=m_firstChangedNode;
            while (node!=null) {
                if (node.isActive() && (canBeBlocked(node) || canBeBlocker(node))) {
                    if (hasBlockingInfoChanged(node) || !node.isDirectlyBlocked() || node.getBlocker().getNodeID()>=m_firstChangedNode.getNodeID()) {
                        Node parent=node.getParent();
                        if (parent==null)
                            node.setBlocked(null,false);
                        else if (parent.isBlocked())
                            node.setBlocked(parent,false);
                        else {
                            Node blocker=m_currentBlockersCache.getBlocker(node);
                            node.setBlocked(blocker,blocker!=null);
                        }
                        if (!node.isBlocked() && canBeBlocker(node))
                            m_currentBlockersCache.addNode(node);
                    }
                    clearBlockingInfoChanged(node);
                }
                node=node.getNextTableauNode();
            }
            m_firstChangedNode=null;
        }
    }
    public boolean isPermanentAssertion(Concept concept,Node node) {
        m_auxiliaryTuple[0]=concept;
        m_auxiliaryTuple[1]=node;
        return m_extensionManager.isCore(m_auxiliaryTuple);
    }
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        if (concept instanceof AtomicConcept) {
            ((CoreBlockingObject)node.getBlockingObject()).addAtomicConcept((AtomicConcept)concept);
            updateNodeChange(node);
        }
    }
    public void assertionCoreSet(Concept concept,Node node) {
    }
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        if (concept instanceof AtomicConcept) {
            ((CoreBlockingObject)node.getBlockingObject()).removeAtomicConcept((AtomicConcept)concept);
            updateNodeChange(node);
        }
    }
    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
    }
    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
    }
    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
    }
    public void nodeStatusChanged(Node node) {
        updateNodeChange(node);
    }
    protected final void updateNodeChange(Node node) {
        if (node!=null && (m_firstChangedNode==null || node.getNodeID()<m_firstChangedNode.getNodeID()))
            m_firstChangedNode=node;
    }
    public void nodeInitialized(Node node) {
        if (node.getBlockingObject()==null)
            node.setBlockingObject(new CoreBlockingObject(node));
        ((CoreBlockingObject)node.getBlockingObject()).initialize();
    }
    public void nodeDestroyed(Node node) {
        if (!node.isBlocked() && canBeBlocker(node))
            m_currentBlockersCache.removeNode(node);
        ((CoreBlockingObject)node.getBlockingObject()).destroy();
        if (m_firstChangedNode!=null && m_firstChangedNode.getNodeID()>=node.getNodeID())
            m_firstChangedNode=null;
    }
    public void modelFound() {
    }
    public boolean isExact() {
        return false;
    }
    public void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,Object[] valuesBuffer,boolean[] coreVariables) {
        workers.add(new ComputeCoreVariables(valuesBuffer,coreVariables));
    }
    protected static boolean canBeBlocker(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    protected static boolean canBeBlocked(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    protected static boolean isBlockedBy(Node blocker,Node blocked) {
        return
            !blocker.isBlocked() &&
            blocker.getNodeType()==NodeType.TREE_NODE &&
            blocked.getNodeType()==NodeType.TREE_NODE &&
            ((CoreBlockingObject)blocker.getBlockingObject()).getAtomicConceptsLabel()==((CoreBlockingObject)blocked.getBlockingObject()).getAtomicConceptsLabel();
    }
    protected static boolean hasBlockingInfoChanged(Node node) {
        return ((CoreBlockingObject)node.getBlockingObject()).m_hasChanged;
    }
    protected static void clearBlockingInfoChanged(Node node) {
        ((CoreBlockingObject)node.getBlockingObject()).m_hasChanged=false;
    }
    protected Set<AtomicConcept> getAtomicConceptsLabel(Node node) {
        m_atomicConceptsBuffer.clear();
        m_binaryTableSearch1Bound.getBindingsBuffer()[1]=node;
        m_binaryTableSearch1Bound.open();
        Object[] tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
        while (!m_binaryTableSearch1Bound.afterLast()) {
            Object concept=tupleBuffer[0];
            if (concept instanceof AtomicConcept && m_binaryTableSearch1Bound.isCore())
                m_atomicConceptsBuffer.add((AtomicConcept)concept);
            m_binaryTableSearch1Bound.next();
        }
        Set<AtomicConcept> result=m_atomicConceptsSetFactory.getSet(m_atomicConceptsBuffer);
        m_atomicConceptsBuffer.clear();
        return result;
    }

    protected final class CoreBlockingObject implements Serializable {
        private static final long serialVersionUID=-2611524541350832293L;

        protected final Node m_node;
        protected boolean m_hasChanged;
        protected Set<AtomicConcept> m_atomicConceptsLabel;
        protected int m_atomicConceptsLabelHashCode;

        public CoreBlockingObject(Node node) {
            m_node=node;
        }
        public void initialize() {
            m_atomicConceptsLabel=null;
            m_atomicConceptsLabelHashCode=0;
            m_hasChanged=true;
        }
        public void destroy() {
            if (m_atomicConceptsLabel!=null) {
                m_atomicConceptsSetFactory.removeReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
        }
        public Set<AtomicConcept> getAtomicConceptsLabel() {
            if (m_atomicConceptsLabel==null) {
                m_atomicConceptsLabel=CoreBlocking.this.getAtomicConceptsLabel(m_node);
                m_atomicConceptsSetFactory.addReference(m_atomicConceptsLabel);
            }
            return m_atomicConceptsLabel;
        }
        public void addAtomicConcept(AtomicConcept atomicConcept) {
            if (m_atomicConceptsLabel!=null) {
                m_atomicConceptsSetFactory.removeReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            m_atomicConceptsLabelHashCode+=atomicConcept.hashCode();
            m_hasChanged=true;
        }
        public void removeAtomicConcept(AtomicConcept atomicConcept) {
            if (m_atomicConceptsLabel!=null) {
                m_atomicConceptsSetFactory.removeReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            m_atomicConceptsLabelHashCode-=atomicConcept.hashCode();
            m_hasChanged=true;
        }
    }

    protected static final class ComputeCoreVariables implements DLClauseEvaluator.Worker,Serializable {
        private static final long serialVersionUID=899293772370136783L;

        protected final Object[] m_valuesBuffer;
        protected final boolean[] m_coreVariables;

        public ComputeCoreVariables(Object[] valuesBuffer,boolean[] coreVariables) {
            m_valuesBuffer=valuesBuffer;
            m_coreVariables=coreVariables;
        }
        public int execute(int programCounter) {
            Node potentialNoncore=null;
            int potentialNoncoreIndex=-1;
            for (int variableIndex=m_coreVariables.length-1;variableIndex>=0;--variableIndex) {
                m_coreVariables[variableIndex]=true;
                Node node=(Node)m_valuesBuffer[variableIndex];
                if (node.getNodeType()==NodeType.TREE_NODE && (potentialNoncore==null || node.getTreeDepth()<potentialNoncore.getTreeDepth())) {
                    potentialNoncore=node;
                    potentialNoncoreIndex=variableIndex;
                }
            }
            if (potentialNoncore!=null) {
                boolean isNoncore=true;
                for (int variableIndex=m_coreVariables.length-1;isNoncore && variableIndex>=0;--variableIndex) {
                    Node node=(Node)m_valuesBuffer[variableIndex];
                    if (!node.isRootNode() && potentialNoncore!=node && !potentialNoncore.isAncestorOf(node))
                        isNoncore=false;
                }
                if (isNoncore)
                    m_coreVariables[potentialNoncoreIndex]=false;
            }
            return programCounter+1;
        }
        public String toString() {
            return "Compute core variables";
        }
    }
}
class CoreBlockingCache implements Serializable {
    private static final long serialVersionUID=-7692825443489644667L;

    protected CacheEntry[] m_buckets;
    protected int m_numberOfElements;
    protected int m_threshold;
    protected CacheEntry m_emptyEntries;

    public CoreBlockingCache() {
        clear();
    }
    public boolean isEmpty() {
        return m_numberOfElements==0;
    }
    public void clear() {
        m_buckets=new CacheEntry[1024];
        m_threshold=(int)(m_buckets.length*0.75);
        m_numberOfElements=0;
        m_emptyEntries=null;
    }
    public void removeNode(Node node) {
        int hashCode=((CoreBlocking.CoreBlockingObject)node.getBlockingObject()).m_atomicConceptsLabelHashCode;
        int bucketIndex=getIndexFor(hashCode,m_buckets.length);
        CacheEntry lastEntry=null;
        CacheEntry entry=m_buckets[bucketIndex];
        while (entry!=null) {
            if (entry.m_node==node) {
                if (lastEntry==null)
                    m_buckets[bucketIndex]=entry.m_nextEntry;
                else
                    lastEntry.m_nextEntry=entry.m_nextEntry;
                entry.m_nextEntry=m_emptyEntries;
                entry.m_node=null;
                entry.m_hashCode=0;
                m_emptyEntries=entry;
                m_numberOfElements--;
                return;
            }
            lastEntry=entry;
            entry=entry.m_nextEntry;
        }
    }
    public void addNode(Node node) {
        int hashCode=((CoreBlocking.CoreBlockingObject)node.getBlockingObject()).m_atomicConceptsLabelHashCode;
        int bucketIndex=getIndexFor(hashCode,m_buckets.length);
        CacheEntry entry=m_buckets[bucketIndex];
        while (entry!=null) {
            if (hashCode==entry.m_hashCode && CoreBlocking.isBlockedBy(entry.m_node,node))
                return;
            entry=entry.m_nextEntry;
        }
        if (m_emptyEntries==null)
            entry=new CacheEntry();
        else {
            entry=m_emptyEntries;
            m_emptyEntries=m_emptyEntries.m_nextEntry;
        }
        entry.initialize(node,hashCode,m_buckets[bucketIndex]);
        m_buckets[bucketIndex]=entry;
        m_numberOfElements++;
        if (m_numberOfElements>=m_threshold)
            resize(m_buckets.length*2);
    }
    protected void resize(int newCapacity) {
        CacheEntry[] newBuckets=new CacheEntry[newCapacity];
        for (int i=0;i<m_buckets.length;i++) {
            CacheEntry entry=m_buckets[i];
            while (entry!=null) {
                CacheEntry nextEntry=entry.m_nextEntry;
                int newIndex=getIndexFor(entry.m_hashCode,newCapacity);
                entry.m_nextEntry=newBuckets[newIndex];
                newBuckets[newIndex]=entry;
                entry=nextEntry;
            }
        }
        m_buckets=newBuckets;
        m_threshold=(int)(newCapacity*0.75);
    }
    public Node getBlocker(Node node) {
        if (CoreBlocking.canBeBlocked(node)) {
            int hashCode=((CoreBlocking.CoreBlockingObject)node.getBlockingObject()).m_atomicConceptsLabelHashCode;
            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
            CacheEntry entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (hashCode==entry.m_hashCode && CoreBlocking.isBlockedBy(entry.m_node,node))
                    return entry.m_node;
                entry=entry.m_nextEntry;
            }
        }
        return null;
    }
    protected static int getIndexFor(int hashCode,int tableLength) {
        hashCode+=~(hashCode << 9);
        hashCode^=(hashCode >>> 14);
        hashCode+=(hashCode << 4);
        hashCode^=(hashCode >>> 10);
        return hashCode & (tableLength-1);
    }

    public static class CacheEntry implements Serializable {
        private static final long serialVersionUID=-7047487963170250200L;

        protected Node m_node;
        protected int m_hashCode;
        protected CacheEntry m_nextEntry;

        public void initialize(Node node,int hashCode,CacheEntry nextEntry) {
            m_node=node;
            m_hashCode=hashCode;
            m_nextEntry=nextEntry;
        }
    }
}
