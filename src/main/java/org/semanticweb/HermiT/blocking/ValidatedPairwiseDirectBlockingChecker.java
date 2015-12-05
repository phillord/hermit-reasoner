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

import org.semanticweb.HermiT.blocking.ValidatedSingleDirectBlockingChecker.ValidatedBlockingObject;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;

public class ValidatedPairwiseDirectBlockingChecker implements DirectBlockingChecker,Serializable {
    private static final long serialVersionUID=9093753046859877016L;

    protected final SetFactory<AtomicConcept> m_atomicConceptsSetFactory=new SetFactory<AtomicConcept>();
    protected final SetFactory<AtomicRole> m_atomicRolesSetFactory=new SetFactory<AtomicRole>();
    protected final List<AtomicConcept> m_atomicConceptsBuffer=new ArrayList<AtomicConcept>();
    protected final List<AtomicRole> m_atomicRolesBuffer=new ArrayList<AtomicRole>();
    protected final boolean m_hasInverses;
    protected Tableau m_tableau;
    protected ExtensionTable.Retrieval m_binaryTableSearch1Bound;
    protected ExtensionTable.Retrieval m_ternaryTableSearch12Bound;

    public ValidatedPairwiseDirectBlockingChecker(boolean hasInverses) {
    	m_hasInverses=hasInverses;
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
        ValidatedPairwiseBlockingObject blockerObject=(ValidatedPairwiseBlockingObject)blocker.getBlockingObject();
        ValidatedPairwiseBlockingObject blockedObject=(ValidatedPairwiseBlockingObject)blocked.getBlockingObject();
        boolean isBlockedBy=
            !blocker.isBlocked() &&
            blocker.getNodeType()==NodeType.TREE_NODE &&
            blocked.getNodeType()==NodeType.TREE_NODE &&
            blockerObject.getAtomicConceptsLabel()==blockedObject.getAtomicConceptsLabel() &&
            ((ValidatedPairwiseBlockingObject)blocker.getParent().getBlockingObject()).getAtomicConceptsLabel()==((ValidatedPairwiseBlockingObject)blocked.getParent().getBlockingObject()).getAtomicConceptsLabel();
        return isBlockedBy;
    }
    public int blockingHashCode(Node node) {
        ValidatedPairwiseBlockingObject nodeObject=(ValidatedPairwiseBlockingObject)node.getBlockingObject();
        return
            nodeObject.m_blockingRelevantHashCode+
            ((ValidatedPairwiseBlockingObject)node.getParent().getBlockingObject()).m_blockingRelevantHashCode;
    }
    public boolean canBeBlocker(Node node) {
    	Node parent=node.getParent();
        return node.getNodeType()==NodeType.TREE_NODE && (!m_hasInverses || node.getParent().getNodeType()==NodeType.TREE_NODE || parent.getNodeType()==NodeType.GRAPH_NODE);
    }
    public boolean canBeBlocked(Node node) {
    	Node parent=node.getParent();
        return node.getNodeType()==NodeType.TREE_NODE && (!m_hasInverses || node.getParent().getNodeType()==NodeType.TREE_NODE || parent.getNodeType()==NodeType.GRAPH_NODE);
    }
    public boolean hasBlockingInfoChanged(Node node) {
        return ((ValidatedPairwiseBlockingObject)node.getBlockingObject()).m_hasChangedForBlocking;
    }
    public void clearBlockingInfoChanged(Node node) {
        ((ValidatedPairwiseBlockingObject)node.getBlockingObject()).m_hasChangedForBlocking=false;
    }
    public boolean hasChangedSinceValidation(Node node) {
        return ((ValidatedPairwiseBlockingObject)node.getBlockingObject()).m_hasChangedForValidation;
    }
    public void setHasChangedSinceValidation(Node node, boolean hasChanged) {
        ((ValidatedPairwiseBlockingObject)node.getBlockingObject()).m_hasChangedForValidation=hasChanged;
    }
    public void nodeInitialized(Node node) {
        if (node.getBlockingObject()==null)
            node.setBlockingObject(new ValidatedPairwiseBlockingObject(node));
        ((ValidatedPairwiseBlockingObject)node.getBlockingObject()).initialize();
    }
    public void nodeDestroyed(Node node) {
        ((ValidatedPairwiseBlockingObject)node.getBlockingObject()).destroy();
    }
    public Node assertionAdded(Concept concept,Node node,boolean isCore) {
        ((ValidatedPairwiseBlockingObject)node.getBlockingObject()).addConcept(concept, isCore);
        return (concept instanceof AtomicConcept && isCore)?node:null;
    }
    public Node assertionRemoved(Concept concept, Node node, boolean isCore) {
        ((ValidatedPairwiseBlockingObject) node.getBlockingObject()).removeConcept(concept, isCore);
        return (concept instanceof AtomicConcept && isCore)?node:null;
    }
    public Node assertionAdded(DataRange range,Node node,boolean isCore) {
        return null;
    }
    public Node assertionRemoved(DataRange range,Node node,boolean isCore) {
        return null;
    }
    public Node assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        return null;
    }
    public Node assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        return null;
    }
    public Node nodesMerged(Node mergeFrom,Node mergeInto) {
        return null;
    }
    public Node nodesUnmerged(Node mergeFrom,Node mergeInto) {
        return null;
    }
    protected Set<AtomicConcept> fetchAtomicConceptsLabel(Node node,boolean onlyCore) {
        m_atomicConceptsBuffer.clear();
        m_binaryTableSearch1Bound.getBindingsBuffer()[1]=node;
        m_binaryTableSearch1Bound.open();
        Object[] tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
        while (!m_binaryTableSearch1Bound.afterLast()) {
            Object concept=tupleBuffer[0];
            if (concept instanceof AtomicConcept) {
                if (!onlyCore || m_binaryTableSearch1Bound.isCore()) {
                    m_atomicConceptsBuffer.add((AtomicConcept)concept);
                }
            }
            m_binaryTableSearch1Bound.next();
        }
        Set<AtomicConcept> result=m_atomicConceptsSetFactory.getSet(m_atomicConceptsBuffer);
        m_atomicConceptsBuffer.clear();
        return result;
    }
    protected Set<AtomicRole> fetchAtomicRolesLabel(Node nodeFrom,Node nodeTo,boolean onlyCore) {
        m_atomicRolesBuffer.clear();
        m_ternaryTableSearch12Bound.getBindingsBuffer()[1] = nodeFrom;
        m_ternaryTableSearch12Bound.getBindingsBuffer()[2] = nodeTo;
        m_ternaryTableSearch12Bound.open();
        Object[] tupleBuffer = m_ternaryTableSearch12Bound.getTupleBuffer();
        while (!m_ternaryTableSearch12Bound.afterLast()) {
            Object atomicRole = tupleBuffer[0];
            if (atomicRole instanceof AtomicRole && (!onlyCore || m_binaryTableSearch1Bound.isCore())) {
                m_atomicRolesBuffer.add((AtomicRole) atomicRole);
            }
            m_ternaryTableSearch12Bound.next();
        }
        Set<AtomicRole> result = m_atomicRolesSetFactory.getSet(m_atomicRolesBuffer);
        m_atomicRolesBuffer.clear();
        return result;
    }
    public BlockingSignature getBlockingSignatureFor(Node node) {
        return new ValidatedBlockingSignature(this,node);
    }

    protected final class ValidatedPairwiseBlockingObject implements ValidatedBlockingObject {
        protected final Node m_node;
        protected boolean m_hasChangedForBlocking;
        protected boolean m_hasChangedForValidation;
        protected Set<AtomicConcept> m_blockingRelevantLabel;
        protected Set<AtomicConcept> m_fullAtomicConceptsLabel;
        protected Set<AtomicRole> m_fullFromParentLabel;
        protected Set<AtomicRole> m_fullToParentLabel;
        protected int m_blockingRelevantHashCode;
        public boolean m_blockViolatesParentConstraints=false;
        public boolean m_hasAlreadyBeenChecked=false;

        public ValidatedPairwiseBlockingObject(Node node) {
            m_node=node;
        }
        public void initialize() {
            m_blockingRelevantLabel=null;
            m_blockingRelevantHashCode=0;
            m_fullAtomicConceptsLabel=null;
            m_fullFromParentLabel=null;
            m_fullToParentLabel=null;
            m_hasChangedForBlocking=true;
            m_hasChangedForValidation=true;
        }
        public void destroy() {
            if (m_blockingRelevantLabel!=null) {
                m_atomicConceptsSetFactory.removeReference(m_blockingRelevantLabel);
                m_blockingRelevantLabel=null;
            }
            if (m_fullAtomicConceptsLabel!=null) {
                m_atomicConceptsSetFactory.removeReference(m_fullAtomicConceptsLabel);
                m_fullAtomicConceptsLabel=null;
            }
            if (m_fullFromParentLabel!=null) {
                m_atomicRolesSetFactory.removeReference(m_fullFromParentLabel);
                m_fullFromParentLabel=null;
            }
            if (m_fullToParentLabel!=null) {
                m_atomicRolesSetFactory.removeReference(m_fullToParentLabel);
                m_fullToParentLabel=null;
            }
            m_hasChangedForBlocking=true;
            m_hasChangedForValidation=true;
        }
        public Set<AtomicConcept> getAtomicConceptsLabel() {
            if (m_blockingRelevantLabel==null) {
                m_blockingRelevantLabel=ValidatedPairwiseDirectBlockingChecker.this.fetchAtomicConceptsLabel(m_node,true);
                m_atomicConceptsSetFactory.addReference(m_blockingRelevantLabel);
            }
            return m_blockingRelevantLabel;
        }
        public void addConcept(Concept concept, boolean isCore) {
          // for validation purposes not only the core and atomic concept changes matter
          m_hasChangedForValidation=true;
          if (concept instanceof AtomicConcept) {
              // relevant for blocking
              if (m_fullAtomicConceptsLabel!=null) {
                  // invalidate, recompute real label later if necessary
                  m_atomicConceptsSetFactory.removeReference(m_fullAtomicConceptsLabel);
                  m_fullAtomicConceptsLabel=null;
              }
              if (isCore) {
                  if (m_blockingRelevantLabel!=null) {
                      // invalidate, recompute real label later if necessary
                      m_atomicConceptsSetFactory.removeReference(m_blockingRelevantLabel);
                      m_blockingRelevantLabel=null;
                  }
                  m_blockingRelevantHashCode+=concept.hashCode();
                  m_hasChangedForBlocking=true;
              }
          }
        }
        public void removeConcept(Concept concept, boolean isCore) {
            // for validation purposes not only the core and atomicConcept
            // changes matter
            m_hasChangedForValidation = true;
            if (concept instanceof AtomicConcept) {
                if (m_fullAtomicConceptsLabel != null) {
                    // invalidate, recompute real label later if necessary
                    m_atomicConceptsSetFactory.removeReference(m_fullAtomicConceptsLabel);
                    m_fullAtomicConceptsLabel = null;
                }
                if (isCore) {
                    if (m_blockingRelevantLabel != null) {
                        // invalidate, recompute real label later if necessary
                        m_atomicConceptsSetFactory.removeReference(m_blockingRelevantLabel);
                        m_blockingRelevantLabel = null;
                    }
                    m_blockingRelevantHashCode-=concept.hashCode();
                    m_hasChangedForBlocking=true;
                }
            }
        }
        public Set<AtomicConcept> getFullAtomicConceptsLabel() {
            if (m_fullAtomicConceptsLabel==null) {
                m_fullAtomicConceptsLabel=ValidatedPairwiseDirectBlockingChecker.this.fetchAtomicConceptsLabel(m_node,false);
                m_atomicConceptsSetFactory.addReference(m_fullAtomicConceptsLabel);
            }
            return m_fullAtomicConceptsLabel;
        }
        public Set<AtomicRole> getFullFromParentLabel() {
            if (m_hasChangedForValidation || m_fullFromParentLabel==null) {
                m_fullFromParentLabel=ValidatedPairwiseDirectBlockingChecker.this.fetchAtomicRolesLabel(m_node.getParent(),m_node,false);
                m_atomicRolesSetFactory.addReference(m_fullFromParentLabel);
            }
            return m_fullFromParentLabel;
        }
        public Set<AtomicRole> getFullToParentLabel() {
            if (m_hasChangedForValidation || m_fullToParentLabel==null) {
                m_fullToParentLabel=ValidatedPairwiseDirectBlockingChecker.this.fetchAtomicRolesLabel(m_node,m_node.getParent(),false);
                m_atomicRolesSetFactory.addReference(m_fullToParentLabel);
            }
            return m_fullToParentLabel;
        }
        public void setBlockViolatesParentConstraints(boolean violates) {
            m_blockViolatesParentConstraints=violates;
        }
        public void setHasAlreadyBeenChecked(boolean hasBeenChecked) {
            m_hasAlreadyBeenChecked=hasBeenChecked;
        }
        public boolean hasAlreadyBeenChecked() {
            return m_hasAlreadyBeenChecked;
        }
        public boolean blockViolatesParentConstraints() {
            return m_blockViolatesParentConstraints;
        }
    }

    protected static class ValidatedBlockingSignature extends BlockingSignature {
        protected final Set<AtomicConcept> m_blockingRelevantConceptsLabel;
        protected final Set<AtomicConcept> m_fullAtomicConceptsLabel;
        protected final Set<AtomicConcept> m_parentFullAtomicConceptsLabel;
        protected final Set<AtomicRole> m_fromParentLabel;
        protected final Set<AtomicRole> m_toParentLabel;
        protected final int m_hashCode;

        public ValidatedBlockingSignature(ValidatedPairwiseDirectBlockingChecker checker,Node node) {
            ValidatedPairwiseBlockingObject nodeBlockingObject=(ValidatedPairwiseBlockingObject)node.getBlockingObject();
            m_blockingRelevantConceptsLabel=nodeBlockingObject.getAtomicConceptsLabel();
            m_fullAtomicConceptsLabel=nodeBlockingObject.getFullAtomicConceptsLabel();
            m_parentFullAtomicConceptsLabel=((ValidatedPairwiseBlockingObject)node.getParent().getBlockingObject()).getFullAtomicConceptsLabel();
            m_fromParentLabel=nodeBlockingObject.getFullFromParentLabel();
            m_toParentLabel=nodeBlockingObject.getFullToParentLabel();
            m_hashCode=m_blockingRelevantConceptsLabel.hashCode();
            checker.m_atomicConceptsSetFactory.makePermanent(m_fullAtomicConceptsLabel);
            checker.m_atomicConceptsSetFactory.makePermanent(m_parentFullAtomicConceptsLabel);
            checker.m_atomicRolesSetFactory.makePermanent(m_fromParentLabel);
            checker.m_atomicRolesSetFactory.makePermanent(m_toParentLabel);
        }
        public boolean blocksNode(Node node) {
            ValidatedPairwiseBlockingObject nodeBlockingObject = (ValidatedPairwiseBlockingObject) node.getBlockingObject();
            return nodeBlockingObject.getAtomicConceptsLabel() == m_blockingRelevantConceptsLabel;
        }
        public int hashCode() {
            return m_hashCode;
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof ValidatedBlockingSignature || that instanceof Node))
                return false;
            if (that instanceof Node) {
                Node thatNode=(Node)that;
                Node thatParent=thatNode.getParent();
                if (thatParent==null) return false;
                ValidatedPairwiseBlockingObject nodeBlockingObject=(ValidatedPairwiseBlockingObject)thatNode.getBlockingObject();
                ValidatedPairwiseBlockingObject parentBlockingObject=(ValidatedPairwiseBlockingObject)thatNode.getBlockingObject();
                return
                    m_blockingRelevantConceptsLabel==nodeBlockingObject.m_blockingRelevantLabel &&
                    m_fullAtomicConceptsLabel==nodeBlockingObject.m_fullAtomicConceptsLabel &&
                    m_parentFullAtomicConceptsLabel==parentBlockingObject.m_fullAtomicConceptsLabel &&
                    m_fromParentLabel==nodeBlockingObject.m_fullFromParentLabel &&
                    m_toParentLabel==nodeBlockingObject.m_fullToParentLabel;
            }
            else {
                ValidatedBlockingSignature thatSignature=(ValidatedBlockingSignature)that;
                return
                    m_blockingRelevantConceptsLabel==thatSignature.m_blockingRelevantConceptsLabel &&
                    m_fullAtomicConceptsLabel==thatSignature.m_fullAtomicConceptsLabel &&
                    m_parentFullAtomicConceptsLabel==thatSignature.m_parentFullAtomicConceptsLabel &&
                    m_fromParentLabel==thatSignature.m_fromParentLabel &&
                    m_toParentLabel==thatSignature.m_toParentLabel;
            }
        }
    }
}
