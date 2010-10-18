/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

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
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;

public class PairWiseDirectBlockingChecker implements DirectBlockingChecker,Serializable {
    private static final long serialVersionUID=-8296420442452625109L;

    protected final SetFactory<AtomicConcept> m_atomicConceptsSetFactory;
    protected final SetFactory<AtomicRole> m_atomicRolesSetFactory;
    protected final List<AtomicConcept> m_atomicConceptsBuffer;
    protected final List<AtomicRole> m_atomicRolesBuffer;
    protected Tableau m_tableau;
    protected ExtensionTable.Retrieval m_binaryTableSearch1Bound;
    protected ExtensionTable.Retrieval m_ternaryTableSearch12Bound;

    public PairWiseDirectBlockingChecker() {
        m_atomicConceptsSetFactory=new SetFactory<AtomicConcept>();
        m_atomicRolesSetFactory=new SetFactory<AtomicRole>();
        m_atomicConceptsBuffer=new ArrayList<AtomicConcept>();
        m_atomicRolesBuffer=new ArrayList<AtomicRole>();
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_binaryTableSearch1Bound=tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        m_ternaryTableSearch12Bound=tableau.getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,true },ExtensionTable.View.TOTAL);
    }
    public void clear() {
        m_atomicConceptsSetFactory.clearNonpermanent();
        m_atomicRolesSetFactory.clearNonpermanent();
        m_binaryTableSearch1Bound.clear();
        m_ternaryTableSearch12Bound.clear();
    }
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
    	Node parent=node.getParent();
        return node.getNodeType()==NodeType.TREE_NODE && (parent.getNodeType()==NodeType.TREE_NODE || parent.getNodeType()==NodeType.GRAPH_NODE);
    }
    public boolean canBeBlocked(Node node) {
    	Node parent=node.getParent();
        return node.getNodeType()==NodeType.TREE_NODE && (parent.getNodeType()==NodeType.TREE_NODE || parent.getNodeType()==NodeType.GRAPH_NODE);
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
    public Node assertionAdded(Concept concept,Node node,boolean isCore) {
        if (concept instanceof AtomicConcept) {
            ((PairWiseBlockingObject)node.getBlockingObject()).addAtomicConcept((AtomicConcept)concept);
            return node;
        }
        else
            return null;
    }
    public Node assertionRemoved(Concept concept,Node node,boolean isCore) {
        if (concept instanceof AtomicConcept) {
            ((PairWiseBlockingObject)node.getBlockingObject()).removeAtomicConcept((AtomicConcept)concept);
            return node;
        }
        else
            return null;
    }
    public Node assertionAdded(DataRange range,Node node,boolean isCore) {
        return null;
    }
    public Node assertionRemoved(DataRange range,Node node,boolean isCore) {
        return null;
    }
    public Node assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        if (nodeFrom.isParentOf(nodeTo)) {
            ((PairWiseBlockingObject)nodeTo.getBlockingObject()).addToFromParentLabel(atomicRole);
            return nodeTo;
        }
        else if (nodeTo.isParentOf(nodeFrom)) {
            ((PairWiseBlockingObject)nodeFrom.getBlockingObject()).addToToParentLabel(atomicRole);
            return nodeFrom;
        }
        else {
            // If the previous two tests fail, then the added assertion represents a relation between
            // root nodes or a back-link to the root node. Such assertions are not relevant for blocking;
            // hence, neither node should be marked as changed.
            return null;
        }
    }
    public Node assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        if (nodeFrom.isParentOf(nodeTo)) {
            ((PairWiseBlockingObject)nodeTo.getBlockingObject()).removeFromFromParentLabel(atomicRole);
            return nodeTo;
        }
        else if (nodeTo.isParentOf(nodeFrom)) {
            ((PairWiseBlockingObject)nodeFrom.getBlockingObject()).removeFromToParentLabel(atomicRole);
            return nodeFrom;
        }
        else {
            // If the previous two tests fail, then the removed assertion represents a relation between
            // root nodes or a back-link to the root node. Such assertions are not relevant for blocking;
            // hence, neither node should be marked as changed.
            return null;
        }
    }
    public Node nodesMerged(Node mergeFrom,Node mergeInto) {
        return null;
    }
    public Node nodesUnmerged(Node mergeFrom,Node mergeInto) {
        return null;
    }
    public BlockingSignature getBlockingSignatureFor(Node node) {
        return new PairWiseBlockingSignature(this,node);
    }
    protected Set<AtomicConcept> fetchAtomicConceptsLabel(Node node) {
        m_atomicConceptsBuffer.clear();
        m_binaryTableSearch1Bound.getBindingsBuffer()[1]=node;
        m_binaryTableSearch1Bound.open();
        Object[] tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
        while (!m_binaryTableSearch1Bound.afterLast()) {
            Object concept=tupleBuffer[0];
            if (concept instanceof AtomicConcept)
                m_atomicConceptsBuffer.add((AtomicConcept)concept);
            m_binaryTableSearch1Bound.next();
        }
        Set<AtomicConcept> result=m_atomicConceptsSetFactory.getSet(m_atomicConceptsBuffer);
        m_atomicConceptsBuffer.clear();
        return result;
    }
    public Set<AtomicRole> fetchEdgeLabel(Node nodeFrom,Node nodeTo) {
        m_atomicRolesBuffer.clear();
        m_ternaryTableSearch12Bound.getBindingsBuffer()[1]=nodeFrom;
        m_ternaryTableSearch12Bound.getBindingsBuffer()[2]=nodeTo;
        m_ternaryTableSearch12Bound.open();
        Object[] tupleBuffer=m_ternaryTableSearch12Bound.getTupleBuffer();
        while (!m_ternaryTableSearch12Bound.afterLast()) {
            Object atomicRole=tupleBuffer[0];
            if (atomicRole instanceof AtomicRole)
                m_atomicRolesBuffer.add((AtomicRole)atomicRole);
            m_ternaryTableSearch12Bound.next();
        }
        Set<AtomicRole> result=m_atomicRolesSetFactory.getSet(m_atomicRolesBuffer);
        m_atomicRolesBuffer.clear();
        return result;
    }
    public boolean hasChangedSinceValidation(Node node) {
        return false;
    }
    public void setHasChangedSinceValidation(Node node,boolean hasChanged) {
        // do nothing
    }

    protected final class PairWiseBlockingObject implements Serializable {
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
                m_atomicConceptsSetFactory.removeReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            if (m_fromParentLabel!=null) {
                m_atomicRolesSetFactory.removeReference(m_fromParentLabel);
                m_fromParentLabel=null;
            }
            if (m_toParentLabel!=null) {
                m_atomicRolesSetFactory.removeReference(m_toParentLabel);
                m_toParentLabel=null;
            }
        }
        public Set<AtomicConcept> getAtomicConceptsLabel() {
            if (m_atomicConceptsLabel==null) {
                m_atomicConceptsLabel=PairWiseDirectBlockingChecker.this.fetchAtomicConceptsLabel(m_node);
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
        public Set<AtomicRole> getFromParentLabel() {
            if (m_fromParentLabel==null) {
                m_fromParentLabel=fetchEdgeLabel(m_node.getParent(),m_node);
                m_atomicRolesSetFactory.addReference(m_fromParentLabel);
            }
            return m_fromParentLabel;
        }
        protected void addToFromParentLabel(AtomicRole atomicRole) {
            if (m_fromParentLabel!=null) {
                m_atomicRolesSetFactory.removeReference(m_fromParentLabel);
                m_fromParentLabel=null;
            }
            m_fromParentLabelHashCode+=atomicRole.hashCode();
            m_hasChanged=true;
        }
        protected void removeFromFromParentLabel(AtomicRole atomicRole) {
            if (m_fromParentLabel!=null) {
                m_atomicRolesSetFactory.removeReference(m_fromParentLabel);
                m_fromParentLabel=null;
            }
            m_fromParentLabelHashCode-=atomicRole.hashCode();
            m_hasChanged=true;
        }
        public Set<AtomicRole> getToParentLabel() {
            if (m_toParentLabel==null) {
                m_toParentLabel=fetchEdgeLabel(m_node,m_node.getParent());
                m_atomicRolesSetFactory.addReference(m_toParentLabel);
            }
            return m_toParentLabel;
        }
        protected void addToToParentLabel(AtomicRole atomicRole) {
            if (m_toParentLabel!=null) {
                m_atomicRolesSetFactory.removeReference(m_toParentLabel);
                m_toParentLabel=null;
            }
            m_toParentLabelHashCode+=atomicRole.hashCode();
            m_hasChanged=true;
        }
        protected void removeFromToParentLabel(AtomicRole atomicRole) {
            if (m_toParentLabel!=null) {
                m_atomicRolesSetFactory.removeReference(m_toParentLabel);
                m_toParentLabel=null;
            }
            m_toParentLabelHashCode-=atomicRole.hashCode();
            m_hasChanged=true;
        }
    }


    protected static class PairWiseBlockingSignature extends BlockingSignature implements Serializable {
        private static final long serialVersionUID=4697990424058632618L;

        protected final Set<AtomicConcept> m_atomicConceptLabel;
        protected final Set<AtomicConcept> m_parentAtomicConceptLabel;
        protected final Set<AtomicRole> m_fromParentLabel;
        protected final Set<AtomicRole> m_toParentLabel;
        protected final int m_hashCode;

        public PairWiseBlockingSignature(PairWiseDirectBlockingChecker checker,Node node) {
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
            checker.m_atomicConceptsSetFactory.makePermanent(m_atomicConceptLabel);
            checker.m_atomicConceptsSetFactory.makePermanent(m_parentAtomicConceptLabel);
            checker.m_atomicRolesSetFactory.makePermanent(m_fromParentLabel);
            checker.m_atomicRolesSetFactory.makePermanent(m_toParentLabel);
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
