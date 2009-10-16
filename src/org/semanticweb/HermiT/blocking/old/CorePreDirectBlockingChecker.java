// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking.old;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.blocking.BlockingSignature;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.SetFactory;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;

public class CorePreDirectBlockingChecker implements DirectBlockingChecker {
    protected final boolean m_useOnlyCore;
    protected final SetFactory<AtomicConcept> m_atomicConceptsSetFactory;
    protected final List<AtomicConcept> m_atomicConceptsBuffer;
    protected Tableau m_tableau;
    protected ExtensionTable.Retrieval m_binaryTableSearch1Bound;

    public CorePreDirectBlockingChecker(boolean useOnlyCore) {
        m_useOnlyCore=useOnlyCore;
        m_atomicConceptsSetFactory=new SetFactory<AtomicConcept>();
        m_atomicConceptsBuffer=new ArrayList<AtomicConcept>();
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_binaryTableSearch1Bound=tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
    }
    public void clear() {
        m_atomicConceptsSetFactory.clearNonpermanent();
    }
    public boolean isBlockedBy(Node blocker,Node blocked) {
        return
            !blocker.isBlocked() &&
            blocker.getNodeType()==NodeType.TREE_NODE &&
            blocked.getNodeType()==NodeType.TREE_NODE &&
            ((SinglePreCoreBlockingObject)blocker.getBlockingObject()).getAtomicConceptsLabel()==((SinglePreCoreBlockingObject)blocked.getBlockingObject()).getAtomicConceptsLabel();
    }
    public int blockingHashCode(Node node) {
        return ((SinglePreCoreBlockingObject)node.getBlockingObject()).m_atomicConceptsLabelHashCode;
    }
    public boolean canBeBlocker(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean canBeBlocked(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean hasBlockingInfoChanged(Node node) {
        return ((SinglePreCoreBlockingObject)node.getBlockingObject()).m_hasChanged;
    }
    public boolean hasChangedSinceValidation(Node node) {
        return ((SinglePreCoreBlockingObject)node.getBlockingObject()).m_hasChangedSinceValidation;
    }
    public void clearBlockingInfoChanged(Node node) {
        ((SinglePreCoreBlockingObject)node.getBlockingObject()).m_hasChanged=false;
    }
    public void clearValidationInfoChanged(Node node) {
        ((SinglePreCoreBlockingObject)node.getBlockingObject()).m_hasChangedSinceValidation=false;
    }
    public void nodeInitialized(Node node) {
        if (node.getBlockingObject()==null)
            node.setBlockingObject(new SinglePreCoreBlockingObject(node));
        ((SinglePreCoreBlockingObject)node.getBlockingObject()).initialize();
    }
    public void nodeDestroyed(Node node) {
        ((SinglePreCoreBlockingObject)node.getBlockingObject()).destroy();
    }
    public Node assertionAdded(Concept concept,Node node,boolean isCore) {
        if ((isCore || !m_useOnlyCore) && concept instanceof AtomicConcept) {
            ((SinglePreCoreBlockingObject)node.getBlockingObject()).addAtomicConcept((AtomicConcept)concept);
            return node;
        } else return null;
    }
    public Node assertionRemoved(Concept concept,Node node,boolean isCore) {
        if ((isCore || !m_useOnlyCore) && concept instanceof AtomicConcept) {
            ((SinglePreCoreBlockingObject)node.getBlockingObject()).removeAtomicConcept((AtomicConcept)concept);
            return node;
        } else return null;
    }
    public Node assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        return null;
    }
    public Node assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        return null;
    }
    public BlockingSignature getBlockingSignatureFor(Node node) {
        return null;//new SingleBlockingSignature(this,node);
    }
    protected Set<AtomicConcept> fetchAtomicConceptsLabel(Node node) {
        m_atomicConceptsBuffer.clear();
        m_binaryTableSearch1Bound.getBindingsBuffer()[1]=node;
        m_binaryTableSearch1Bound.open();
        Object[] tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
        while (!m_binaryTableSearch1Bound.afterLast()) {
            Object concept=tupleBuffer[0];
            if (concept instanceof AtomicConcept) {
                if (!m_useOnlyCore || m_binaryTableSearch1Bound.isCore()) {
                    m_atomicConceptsBuffer.add((AtomicConcept)concept);
                }
            }
            m_binaryTableSearch1Bound.next();
        }
        Set<AtomicConcept> result=m_atomicConceptsSetFactory.getSet(m_atomicConceptsBuffer);
        m_atomicConceptsBuffer.clear();
        return result;
    }
    
    protected final class SinglePreCoreBlockingObject {
        
        protected final Node m_node;
        protected boolean m_hasChanged;
        protected boolean m_hasChangedSinceValidation;
        protected Set<AtomicConcept> m_atomicConceptsLabel;
        protected int m_atomicConceptsLabelHashCode;
        
        public SinglePreCoreBlockingObject(Node node) {
            m_node=node;
        }
        public void initialize() {
            m_atomicConceptsLabel=null;
            m_atomicConceptsLabelHashCode=0;
            m_hasChanged=true;
            m_hasChangedSinceValidation=true;
        }
        public void destroy() {
            if (m_atomicConceptsLabel!=null) {
                m_atomicConceptsSetFactory.removeReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
        }
        public Set<AtomicConcept> getAtomicConceptsLabel() {
            if (m_atomicConceptsLabel==null) {
                m_atomicConceptsLabel=CorePreDirectBlockingChecker.this.fetchAtomicConceptsLabel(m_node);
                m_atomicConceptsSetFactory.addReference(m_atomicConceptsLabel);
            }
            return m_atomicConceptsLabel;
        }
        public void addAtomicConcept(AtomicConcept atomicConcept) {
            if (m_atomicConceptsLabel!=null) {
                // invalidate, recompute real label later if necessary
                m_atomicConceptsSetFactory.removeReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            m_atomicConceptsLabelHashCode+=atomicConcept.hashCode();
            m_hasChanged=true;
        }
        public void removeAtomicConcept(AtomicConcept atomicConcept) {
            if (m_atomicConceptsLabel!=null) {
                // invalidate, recompute real label later if necessary
                m_atomicConceptsSetFactory.removeReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            m_atomicConceptsLabelHashCode-=atomicConcept.hashCode();
            m_hasChanged=true;
        }
    }
//    
//    protected static class SinglePreCoreBlockingSignature extends BlockingSignature implements Serializable {
//        private static final long serialVersionUID=-7349489846772132258L;
//
//        protected final Set<AtomicConcept> m_coreConcepts;
//        protected final Set<AtomicConcept> m_NonCoreConcepts;
//        protected final Set<AtomicConcept> m_parentAtomicConceptLabel;
//        protected final Set<AtomicRole> m_fromParentLabel;
//        protected final Set<AtomicRole> m_toParentLabel;
//
//        public SinglePreCoreBlockingSignature(CorePreDirectBlockingChecker checker,Node node) {
//            m_coreConcepts=((SinglePreCoreBlockingObject)node.getBlockingObject()).getCoreConcepts();
//            m_NonCoreConcepts=((SinglePreCoreBlockingObject)node.getBlockingObject()).getNonCoreConcepts();
//            m_parentAtomicConceptLabel=((SinglePreCoreBlockingObject)node.getParent().getBlockingObject()).getNonCoreConcepts();
//            m_fromParentLabel=((SinglePreCoreBlockingObject)node.getBlockingObject()).getFromParentLabel();
//            m_toParentLabel=((SinglePreCoreBlockingObject)node.getBlockingObject()).getToParentLabel();
//            checker.m_atomicConceptsSetFactory.addReference(m_coreConcepts);
//            checker.m_atomicConceptsSetFactory.makePermanent(m_coreConcepts);
//            checker.m_atomicConceptsSetFactory.addReference(m_NonCoreConcepts);
//            checker.m_atomicConceptsSetFactory.makePermanent(m_NonCoreConcepts);
//            checker.m_atomicConceptsSetFactory.addReference(m_parentAtomicConceptLabel);
//            checker.m_atomicConceptsSetFactory.makePermanent(m_parentAtomicConceptLabel);
//            checker.m_atomicRolesSetFactory.addReference(m_fromParentLabel);
//            checker.m_atomicRolesSetFactory.makePermanent(m_fromParentLabel);
//            checker.m_atomicRolesSetFactory.addReference(m_toParentLabel);
//            checker.m_atomicRolesSetFactory.makePermanent(m_toParentLabel);
//        }
//        public boolean blocksNode(Node node) {
//            return ((SinglePreCoreBlockingObject)node.getBlockingObject()).getCoreConcepts()==m_coreConcepts;
//        }
//        public int hashCode() {
//            return m_coreConcepts.hashCode();
//        }
//        public boolean equals(Object that) {
//            if (this==that)
//                return true;
//            if (!(that instanceof SinglePreCoreBlockingSignature))
//                return false;
//            return m_coreConcepts==((SinglePreCoreBlockingSignature)that).m_coreConcepts;
//        }
//    }

    public void setHasChangedSinceValidation(Node node, boolean hasChanged) {
        // do nothing
    }
    public Set<AtomicConcept> getAtomicConceptsLabel(Node node) {
        // TODO Auto-generated method stub
        return null;
    }
    public Set<AtomicConcept> getCoreConceptsLabel(Node node) {
        // TODO Auto-generated method stub
        return null;
    }
    public Set<AtomicConcept> getBlockingRelevantConceptsLabel(Node node) {
        // TODO Auto-generated method stub
        return null;
    }
    public Set<AtomicConcept> getFullAtomicConceptsLabel(Node node) {
        // TODO Auto-generated method stub
        return null;
    }
    public Set<AtomicRole> getFullFromParentLabel(Node node) {
        // TODO Auto-generated method stub
        return null;
    }
    public Set<AtomicRole> getFullToParentLabel(Node node) {
        // TODO Auto-generated method stub
        return null;
    }
}
