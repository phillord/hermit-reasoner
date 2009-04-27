// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.LabelManager;

public class PairWiseDirectBlockingChecker implements DirectBlockingChecker,Serializable {
    private static final long serialVersionUID=-8296420442452625109L;

    public static final DirectBlockingChecker INSTANCE=new PairWiseDirectBlockingChecker();

    public boolean isBlockedBy(Node blocker,Node blocked) {
        PairWiseBlockingObject blockerObject=(PairWiseBlockingObject)blocker.getBlockingObject();
        PairWiseBlockingObject blockedObject=(PairWiseBlockingObject)blocked.getBlockingObject();
        return
            !blocker.isBlocked() &&
            blocker.getNodeType()==NodeType.TREE_NODE &&
            blocked.getNodeType()==NodeType.TREE_NODE &&
            blockerObject.getAtomicConceptsLabel()==blockedObject.getAtomicConceptsLabel() &&
            ((PairWiseBlockingObject)blocker.getParent().getBlockingObject()).getAtomicConceptsLabel()==((PairWiseBlockingObject)blocked.getParent().getBlockingObject()).getAtomicConceptsLabel() &&
            blockerObject.getFromParentLabel()==blockedObject.getFromParentLabel() && 
            blockerObject.getToParentLabel()==blockedObject.getToParentLabel();
    }
    public int blockingHashCode(Node node) {
        PairWiseBlockingObject nodeObject=(PairWiseBlockingObject)node.getBlockingObject();
        return
            nodeObject.m_atomicConceptsLabelHashCode+
            ((PairWiseBlockingObject)node.getParent().getBlockingObject()).m_atomicConceptsLabelHashCode+
            nodeObject.m_fromParentLabelHashCode+
            nodeObject.m_toParentLabelHashCode;
    }
    public boolean canBeBlocker(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean canBeBlocked(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean hasBlockingInfoChanged(Node node) {
        return ((PairWiseBlockingObject)node.getBlockingObject()).m_hasChanged;
    }
    public void clearBlockingInfoChanged(Node node) {
        ((PairWiseBlockingObject)node.getBlockingObject()).m_hasChanged=false;
    }
    public void nodeInitialized(Node node) {
        if (node.getBlockingObject()==null)
            node.setBlockingObject(new PairWiseBlockingObject(node));
        ((PairWiseBlockingObject)node.getBlockingObject()).initialize();
    }
    public void nodeDestroyed(Node node) {
        ((PairWiseBlockingObject)node.getBlockingObject()).destroy();
    }
    public Node assertionAdded(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            ((PairWiseBlockingObject)node.getBlockingObject()).addAtomicConcept((AtomicConcept)concept);
            return node;
        }
        else
            return null;
    }
    public Node assertionRemoved(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            ((PairWiseBlockingObject)node.getBlockingObject()).removeAtomicConcept((AtomicConcept)concept);
            return node;
        }
        else
            return null;
    }
    public Node assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        if (nodeFrom.isParentOf(nodeTo)) {
            ((PairWiseBlockingObject)nodeTo.getBlockingObject()).addToFromParentLabel(atomicRole);
            return nodeTo;
        }
        else if (nodeTo.isParentOf(nodeFrom)) {
            ((PairWiseBlockingObject)nodeFrom.getBlockingObject()).addToToParentLabel(atomicRole);
            return nodeFrom;
        }
        else
            return null;
    }
    public Node assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        if (nodeFrom.isParentOf(nodeTo)) {
            ((PairWiseBlockingObject)nodeTo.getBlockingObject()).removeFromFromParentLabel(atomicRole);
            return nodeTo;
        }
        else if (nodeTo.isParentOf(nodeFrom)) {
            ((PairWiseBlockingObject)nodeFrom.getBlockingObject()).removeFromToParentLabel(atomicRole);
            return nodeFrom;
        }
        else
            return null;
    }
    public BlockingSignature getBlockingSignatureFor(Node node) {
        return new PairWiseBlockingSignature(node);
    }

    protected static final class PairWiseBlockingObject implements Serializable {
        private static final long serialVersionUID=-5439737072100509531L;

        protected final Node m_node;
        protected boolean m_hasChanged;
        protected Set<AtomicConcept> m_atomicConceptsLabel;
        protected int m_atomicConceptsLabelHashCode;
        protected Set<AtomicRole> m_fromParentLabel;
        protected int m_fromParentLabelHashCode;
        protected Set<AtomicRole> m_toParentLabel;
        protected int m_toParentLabelHashCode;

        public PairWiseBlockingObject(Node node) {
            m_node=node;
        }
        public void initialize() {
            m_atomicConceptsLabel=null;
            m_atomicConceptsLabelHashCode=0;
            m_fromParentLabel=null;
            m_fromParentLabelHashCode=0;
            m_toParentLabel=null;
            m_toParentLabelHashCode=0;
            m_hasChanged=true;
        }
        public void destroy() {
            if (m_atomicConceptsLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicConceptSetReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            if (m_fromParentLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicRoleSetReference(m_fromParentLabel);
                m_fromParentLabel=null;
            }
            if (m_toParentLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicRoleSetReference(m_toParentLabel);
                m_toParentLabel=null;
            }
        }
        public Set<AtomicConcept> getAtomicConceptsLabel() {
            if (m_atomicConceptsLabel==null) {
                LabelManager labelManager=m_node.getTableau().getLabelManager();
                m_atomicConceptsLabel=labelManager.getPositiveLabel(m_node);
                labelManager.addAtomicConceptSetReference(m_atomicConceptsLabel);
            }
            return m_atomicConceptsLabel;
        }
        public void addAtomicConcept(AtomicConcept atomicConcept) {
            if (m_atomicConceptsLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicConceptSetReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            m_atomicConceptsLabelHashCode+=atomicConcept.hashCode();
            m_hasChanged=true;
        }
        public void removeAtomicConcept(AtomicConcept atomicConcept) {
            if (m_atomicConceptsLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicConceptSetReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            m_atomicConceptsLabelHashCode-=atomicConcept.hashCode();
            m_hasChanged=true;
        }
        public Set<AtomicRole> getFromParentLabel() {
            if (m_fromParentLabel==null) {
                LabelManager labelManager=m_node.getTableau().getLabelManager();
                m_fromParentLabel=labelManager.getEdgeLabel(m_node.getParent(),m_node);
                labelManager.addAtomicRoleSetReference(m_fromParentLabel);
            }
            return m_fromParentLabel;
        }
        protected void addToFromParentLabel(AtomicRole atomicRole) {
            if (m_fromParentLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicRoleSetReference(m_fromParentLabel);
                m_fromParentLabel=null;
            }
            m_fromParentLabelHashCode+=atomicRole.hashCode();
        }
        protected void removeFromFromParentLabel(AtomicRole atomicRole) {
            if (m_fromParentLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicRoleSetReference(m_fromParentLabel);
                m_fromParentLabel=null;
            }
            m_fromParentLabelHashCode-=atomicRole.hashCode();
        }
        public Set<AtomicRole> getToParentLabel() {
            if (m_toParentLabel==null) {
                LabelManager labelManager=m_node.getTableau().getLabelManager();
                m_toParentLabel=labelManager.getEdgeLabel(m_node,m_node.getParent());
                labelManager.addAtomicRoleSetReference(m_toParentLabel);
            }
            return m_toParentLabel;
        }
        protected void addToToParentLabel(AtomicRole atomicRole) {
            if (m_toParentLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicRoleSetReference(m_toParentLabel);
                m_toParentLabel=null;
            }
            m_toParentLabelHashCode+=atomicRole.hashCode();
        }
        protected void removeFromToParentLabel(AtomicRole atomicRole) {
            if (m_toParentLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicRoleSetReference(m_toParentLabel);
                m_toParentLabel=null;
            }
            m_toParentLabelHashCode-=atomicRole.hashCode();
        }
    }
    

    protected static class PairWiseBlockingSignature extends BlockingSignature implements Serializable {
        private static final long serialVersionUID=4697990424058632618L;

        protected final Set<AtomicConcept> m_atomicConceptLabel;
        protected final Set<AtomicConcept> m_parentAtomicConceptLabel;
        protected final Set<AtomicRole> m_fromParentLabel;
        protected final Set<AtomicRole> m_toParentLabel;
        protected final int m_hashCode;

        public PairWiseBlockingSignature(Node node) {
            PairWiseBlockingObject nodeBlockingObject=(PairWiseBlockingObject)node.getBlockingObject();
            m_atomicConceptLabel=nodeBlockingObject.getAtomicConceptsLabel();
            m_parentAtomicConceptLabel=((PairWiseBlockingObject)node.getParent().getBlockingObject()).getAtomicConceptsLabel();
            m_fromParentLabel=nodeBlockingObject.getFromParentLabel();
            m_toParentLabel=nodeBlockingObject.getToParentLabel();
            m_hashCode=
                m_atomicConceptLabel.hashCode()+
                m_parentAtomicConceptLabel.hashCode()+
                m_fromParentLabel.hashCode()+
                m_toParentLabel.hashCode();
            LabelManager labelManager=node.getTableau().getLabelManager();
            labelManager.makePermanentAtomicConceptSet(m_atomicConceptLabel);
            labelManager.makePermanentAtomicConceptSet(m_parentAtomicConceptLabel);
            labelManager.makePermanentAtomicRoleSet(m_fromParentLabel);
            labelManager.makePermanentAtomicRoleSet(m_toParentLabel);
        }
        public boolean blocksNode(Node node) {
            PairWiseBlockingObject nodeBlockingObject=(PairWiseBlockingObject)node.getBlockingObject();
            return
                nodeBlockingObject.getAtomicConceptsLabel()==m_atomicConceptLabel &&
                ((PairWiseBlockingObject)node.getParent().getBlockingObject()).getAtomicConceptsLabel()==m_parentAtomicConceptLabel &&
                nodeBlockingObject.getFromParentLabel()==m_fromParentLabel &&
                nodeBlockingObject.getToParentLabel()==m_toParentLabel;
        }
        public int hashCode() {
            return m_hashCode;
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof PairWiseBlockingSignature))
                return false;
            PairWiseBlockingSignature thatSignature=(PairWiseBlockingSignature)that;
            return
                m_atomicConceptLabel==thatSignature.m_atomicConceptLabel &&
                m_parentAtomicConceptLabel==thatSignature.m_parentAtomicConceptLabel &&
                m_fromParentLabel==thatSignature.m_fromParentLabel &&
                m_toParentLabel==thatSignature.m_toParentLabel;
        }
    }
}
