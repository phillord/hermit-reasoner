// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.blocking.TwoPhaseDirectBlockingChecker.TwoPhaseBlockingObject;
import org.semanticweb.HermiT.blocking.core.AtMostConcept;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

public class AnywhereTwoPhaseBlocking implements BlockingStrategy, Serializable {
    private static final long serialVersionUID = -6999662045751906931L;
    
    protected Tableau m_tableau;
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final TwoPhaseBlockersCache m_currentBlockersCache;
    protected final boolean m_hasInverses;
    protected ExtensionManager m_extensionManager;
    protected Node m_firstChangedNode;
    protected final Map<AtomicConcept, Set<Set<Concept>>> m_unaryValidBlockConditions; 
    protected final Map<Set<AtomicConcept>, Set<Set<Concept>>> m_nAryValidBlockConditions;
    protected ExtensionTable.Retrieval m_ternaryTableSearchAllBound;
    protected ExtensionTable.Retrieval m_ternaryTableSearchZeroOneBound;
    protected ExtensionTable.Retrieval m_ternaryTableSearchZeroTwoBound;
    protected ExtensionTable.Retrieval m_binaryTableAllBound;
    protected boolean m_immediatelyValidateBlocks = false;
    // statistics: 
    protected final boolean printingOn=false;
    protected int numBlockingComputed = 0;
    protected int maxLabel = 0;
    protected double avgLabel = 0;
    protected int maxNodes = 0;
    protected long sumNodes = 0;
    protected int run = 1;
    
    public AnywhereTwoPhaseBlocking(DirectBlockingChecker directBlockingChecker, Map<AtomicConcept, Set<Set<Concept>>> unaryValidBlockConditions, Map<Set<AtomicConcept>, Set<Set<Concept>>> blockRelNary, boolean hasInverses) {
        m_directBlockingChecker=directBlockingChecker;
        m_unaryValidBlockConditions = unaryValidBlockConditions;
        m_nAryValidBlockConditions = blockRelNary;
        m_hasInverses = hasInverses;
        m_currentBlockersCache=new TwoPhaseBlockersCache(m_directBlockingChecker); // contains all nodes that block some node
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_directBlockingChecker.initialize(tableau);
        m_extensionManager=m_tableau.getExtensionManager();
        m_ternaryTableSearchAllBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,true },ExtensionTable.View.TOTAL);
        m_ternaryTableSearchZeroOneBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
        m_ternaryTableSearchZeroTwoBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.TOTAL);
        m_binaryTableAllBound=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { true,true },ExtensionTable.View.TOTAL);
    }
    public void clear() {
        m_currentBlockersCache.clear();
        m_firstChangedNode=null;
        m_immediatelyValidateBlocks=false;
        numBlockingComputed=0;
        run++;
        if (printingOn) printHeader();
    }
    public boolean computeIsBlocked(Node node) {
        throw new UnsupportedOperationException("Unsupported operation: Two-Phase blocking cannot be used with a lazy expansion strategy. ");
    }
    public Set<Node> checkAllBlocks() {
        throw new UnsupportedOperationException("Unsupported operation: Two-Phase blocking cannot be used with a lazy expansion strategy. ");
    }
    public void computeBlocking(boolean finalChance) {
        computePreBlocking();
        if (finalChance) {
            validateBlocks();
        }
    }
    protected void computePreBlocking() {
        numBlockingComputed++;
        if (printingOn && numBlockingComputed % 2000 == 0) {
            if (numBlockingComputed % 20000 == 0) {
                printHeader();
            }
            printStatistics(true);
        }
        if (m_firstChangedNode!=null) {
            Node node=m_firstChangedNode;
            while (node!=null) {
                m_currentBlockersCache.removeNode(node); // it says node, but in fact we just use the node to get to its core hash and use that
                node=node.getNextTableauNode();
            }
            node=m_firstChangedNode;
            while (node!=null) {
                if (node.isActive() && (m_directBlockingChecker.canBeBlocked(node) || m_directBlockingChecker.canBeBlocker(node))) {
                    // otherwise the node is not relevant for blocking since (it is a root node) since it will not be blocked and cannot block
                    if (m_directBlockingChecker.hasBlockingInfoChanged(node) || !node.isDirectlyBlocked() || node.getBlocker().getNodeID()>=m_firstChangedNode.getNodeID()) {
                        //m_directBlockingChecker.hasBlockingInfoChanged(node) == true if concepts or relation from/to parent has changed
                        //node.isDirectlyBlocked() == true only if a blocker is set and while setting the blocker it has been added as direct
                        Node parent=node.getParent();
                        if (parent==null)
                            node.setBlocked(null,false); // no parent means it cannot be blocked and cannot be blocker
                        else if (parent.isBlocked()) // parent is guaranteed not to change it's status in this computation since we process nodes in creation order and parent is smaller
                            node.setBlocked(parent,false);
                        else {
                            Node blocker;
                            if (m_immediatelyValidateBlocks) {
                                blocker = getValidBlocker(node);
                            } else {
                                blocker = m_currentBlockersCache.getBlockerRepresentative(node);
                            }
                            // get Blocker will always return a node with lower id and that only if all nodes in the cache with this core have a node ID that is smaller than this one  
                            // note that we removed only nodes from the cache that are of order higher than the first changed element
                            // only nodes of lower order than this one can be blockers and all those have already been looked at in this computation or 
                            // were not removed from the cache since they did not change
                            node.setBlocked(blocker,blocker!=null);
                        }
                        if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                            m_currentBlockersCache.addNode(node); // adds node either as main or as an alternative for a node with smaller node ID and identical core
                    }
                    m_directBlockingChecker.clearBlockingInfoChanged(node);
                }
                node=node.getNextTableauNode();
            }
            m_firstChangedNode=null;
        }
    }
    protected void validateBlocks() {
        // after first complete validation, we switch to only checking block validity immediately
        m_immediatelyValidateBlocks = true;
        if (printingOn) System.out.print("Validate blocks...");
        // check if some extra constraints for the parent of the blocked and the blocking node were given
        if (!m_unaryValidBlockConditions.isEmpty() || !m_nAryValidBlockConditions.isEmpty()) {
            // go through all nodes and not just the ones modified in the last run
            
            // statistics:
            int checkedBlocks = 0;
            int invalidBlocks = 0;
            
            Node node = m_tableau.getFirstTableauNode();
            while (node!=null) {
                if (node.isActive() && node.isBlocked()) {
                    // check whether the block is a correct one
                    if (node.isDirectlyBlocked()) {
                        checkedBlocks++;
                        Node validBlocker = getValidBlocker(node); 
                        if (validBlocker == null) {
                            //System.out.println("Node " + node.getBlocker().getNodeID() + " invalidly blocks " + node.getNodeID() + "!");
                            invalidBlocks++;
                            ((TwoPhaseBlockingObject)node.getBlockingObject()).setGreatestInvalidBlocker(m_currentBlockersCache.getPossibleBlockers(node).last());
                        }
                        node.setBlocked(validBlocker,validBlocker!=null);
                    } else if (!node.getParent().isBlocked()) {
                        // indirectly blocked since we proceed in creation order, 
                        // parent has already been checked for proper blocking
                        // if the parent is no longer blocked, unblock this one too
                        node.setBlocked(null,false);
                    }
                    if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                        m_currentBlockersCache.addNode(node);
                }
                node=node.getNextTableauNode();
            }
            m_firstChangedNode=null;
            if (printingOn) System.out.println("Checked " + checkedBlocks + " directly blocked nodes of which " + invalidBlocks + " were invalid.");
        }
    }
    protected Node getValidBlocker(Node blocked) {
        // we have that blocker (pre-)blocks blocked and we have to validate whether the block is valid 
        // that is we can create a model from the block by unravelling
        
        SortedSet<Node> possibleValidBlockers = m_currentBlockersCache.getPossibleBlockers(blocked);
        Node greatestInvalidBlocker = ((TwoPhaseBlockingObject)blocked.getBlockingObject()).m_greatestInvalidBlocker;
        if (greatestInvalidBlocker != null) {
            possibleValidBlockers = new TreeSet<Node>(possibleValidBlockers.tailSet(greatestInvalidBlocker));
            possibleValidBlockers.remove(greatestInvalidBlocker);
            if (possibleValidBlockers.isEmpty()) return null;
        }
        
        Set<AtomicConcept> blockedLabel = ((TwoPhaseBlockingObject)blocked.getBlockingObject()).getAtomicConceptLabel();
        Set<AtomicConcept> blockedParentLabel = ((TwoPhaseBlockingObject)blocked.getParent().getBlockingObject()).getAtomicConceptLabel();
        
        boolean blockerIsSuitable = true;
        greatestInvalidBlocker = null;
        for (Node possibleBlocker : possibleValidBlockers) {
            Set<AtomicConcept> blockerLabel = ((TwoPhaseBlockingObject)possibleBlocker.getBlockingObject()).getAtomicConceptLabel();
            Set<AtomicConcept> blockerParentLabel = ((TwoPhaseBlockingObject)possibleBlocker.getParent().getBlockingObject()).getAtomicConceptLabel();
            
            // check whether min/max cardinalities of the parent of the blocked node could be violated
            // universals and existential have been converted to min/max restrictions for convenience
            AtomicConcept c;
            for (Iterator<AtomicConcept> it = blockedParentLabel.iterator(); it.hasNext() && blockerIsSuitable; ) {
                c = it.next();
                if (m_unaryValidBlockConditions.containsKey(c) && !isBlockedParentSuitable(m_unaryValidBlockConditions.get(c), blocked, blocked.getParent(), blockerLabel, blockerParentLabel, blockedLabel, blockedParentLabel)) 
                    blockerIsSuitable = false;
            }
            // repeat the same checks for non-unary premises (less efficient matching operations)
            if (blockerIsSuitable) {
                for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
                    if (blockerIsSuitable && blockedParentLabel.containsAll(premises) && !isBlockedParentSuitable(m_nAryValidBlockConditions.get(premises), blocked, blocked.getParent(), blockedLabel, blockedParentLabel, blockerLabel, blockerParentLabel))
                        blockerIsSuitable = false;
                }
            }
            // check whether min/max cardinalities of the blocker are not violated when copied to the blocked node
            if (blockerIsSuitable && m_hasInverses) {
                for (Iterator<AtomicConcept> it = blockerLabel.iterator(); it.hasNext() && blockerIsSuitable; ) {
                    c = it.next();
                    if (m_unaryValidBlockConditions.containsKey(c) && !isBlockerSuitable(m_unaryValidBlockConditions.get(c), possibleBlocker, possibleBlocker.getParent(), blocked, blocked.getParent(), blockerLabel, blockerParentLabel, blockedLabel, blockedParentLabel)) 
                        blockerIsSuitable = false;
                }
            }
            // repeat the same checks for non-unary premises (less efficient matching operations)
            if (blockerIsSuitable && m_hasInverses) {
                for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
                    if (blockerIsSuitable && blockerLabel.containsAll(premises) && !isBlockerSuitable(m_nAryValidBlockConditions.get(premises), possibleBlocker, possibleBlocker.getParent(), blocked, blocked.getParent(), blockerLabel, blockerParentLabel, blockedLabel, blockedParentLabel)) 
                        blockerIsSuitable = false;
                }
            }
            if (blockerIsSuitable) {
                if (greatestInvalidBlocker != null) {
                    ((TwoPhaseBlockingObject)blocked.getBlockingObject()).m_greatestInvalidBlocker = greatestInvalidBlocker;
                }
                return possibleBlocker;
            }
            greatestInvalidBlocker = possibleBlocker;
            // else try alternative blockers with the same core
        }
        if (greatestInvalidBlocker != null) {
            ((TwoPhaseBlockingObject)blocked.getBlockingObject()).m_greatestInvalidBlocker = greatestInvalidBlocker;
        }
        return null;
    }
    protected boolean isBlockedParentSuitable(Set<Set<Concept>> conclusions, Node blocked, Node blockedParent, Set<AtomicConcept> blockerLabel, Set<AtomicConcept> blockerParentLabel, Set<AtomicConcept> blockedLabel, Set<AtomicConcept> blockedParentLabel) {
        for (Set<Concept> conjunct : conclusions) {
            boolean disjunctSatisfied = false;
            for (Iterator<Concept> it = conjunct.iterator(); it.hasNext() && !disjunctSatisfied; ) {
                Concept disjunct = it.next();
                disjunctSatisfied = true;
                if (disjunct instanceof AtLeastConcept) {
                    // (>= n r.B) must hold at blockedParent, therefore, ar(r, blockedParent, blocked) and B(blocked) in ABox implies B(blocker) in ABox
                    // to avoid table lookup check: B(blocked) and not B(blocker) implies not ar(r, blockedParent, blocked)
                    AtLeastConcept atLeast = (AtLeastConcept) disjunct;
                    Role r = atLeast.getOnRole();
                    LiteralConcept filler = atLeast.getToConcept();
                    if (isInLabel(filler, blockedLabel) && !isInLabel(filler, blockerLabel) && isInABox(r, blockedParent, blocked)) {
                        disjunctSatisfied = false;
                    }
                } else if (disjunct instanceof AtMostConcept) {
                    // (<= n r.B) must hold at blockedParent, therefore, ar(r, blockedParent, blocked) in ABox and B(blocked) not in ABox implies B(blocker) not in ABox
                    // to avoid table lookup, we check: not B(blocked) and B(blocker) implies not ar(r, blockedParent, blocked)
                    AtMostConcept atMost = (AtMostConcept)disjunct;
                    Role r = atMost.getOnRole();
                    LiteralConcept filler = atMost.getToConcept();
                    if (!isInLabel(filler, blockedLabel) && isInLabel(filler, blockerLabel) && isInABox(r, blockedParent, blocked)) {
                        disjunctSatisfied = false;
                    }
                } else if (disjunct instanceof AtomicConcept) {
                    // happens if we have something like A -> (>= n r.B) or C. If (>= n r.B) is not guaranteed 
                    // for the parent of the blocked node, but C is, then we are fine, so only if C does not hold, we have to look further. 
                    if(!isInLabel((AtomicConcept) disjunct, blockedParentLabel)) {
                        // must be an atomic concept or normal form is violated
                        disjunctSatisfied = false;
                    }
                } else {
                    throw new IllegalStateException("Internal error: Concepts in the conclusion of core blocking constraints are supposed to be atomic classes, at least or at most constraints, but this class is an instance of " + disjunct.getClass().getSimpleName());
                }
            }
            if (!disjunctSatisfied) {
                return false;
            }
        }
        return true;
    }
    protected boolean isBlockerSuitable(Set<Set<Concept>> conclusions, Node blocker, Node blockerParent, Node blocked, Node blockedParent, Set<AtomicConcept> blockerLabel, Set<AtomicConcept> blockerParentLabel, Set<AtomicConcept> blockedLabel, Set<AtomicConcept> blockedParentLabel) {
        for (Set<Concept> conjunct : conclusions) {
            boolean disjunctSatisfied = false;
            for (Iterator<Concept> it = conjunct.iterator(); it.hasNext() && !disjunctSatisfied; ) {
                Concept disjunct = it.next();
                disjunctSatisfied = true;
                if (disjunct instanceof AtLeastConcept) {
                    // (>= n r.B)(blocker) in the ABox, so in the model construction, (>= n r.B) will be copied to blocked, 
                    // so we have to make sure that it will be satisfied at blocked
                    // check B(blockerParent) and ar(r, blocker, blockerParent) in ABox implies B(blockedParent) and ar(r, blocked, blockedParent) in ABox
                    // or blocker has at least n r-successors bs such that B(bs) holds
                    AtLeastConcept atLeast = (AtLeastConcept) disjunct;
                    Role r = atLeast.getOnRole();
                    LiteralConcept filler = atLeast.getToConcept();
                    if (isInLabel(filler, blockerParentLabel) && isInABox(r, blocker, blockerParent) && (!isInLabel(filler, blockedParentLabel) || !isInABox(r, blocked, blockedParent))) {
                        if (!hasMoreThanNSuccessors(blocker, atLeast.getNumber(), r, filler)) {
                            disjunctSatisfied = false;
                        }
                    }
                } else if (disjunct instanceof AtMostConcept) {
                    // (<= n r.B)(blocker) is in the ABox and in the model construction (<= n r.B) will be copied to blocked,  
                    // so we have to make sure that it will be satisfied at blocked
                    // r(blocked, blockedParent) and B(blockedParent) -> r(blocker, blockerParent) and B(blockerParent)
                    AtMostConcept atMost = (AtMostConcept) disjunct;
                    Role r = atMost.getOnRole();
                    LiteralConcept filler = atMost.getToConcept();
                    if (isInLabel(filler, blockedParentLabel) && isInABox(r, blocked, blockedParent) && (!isInLabel(filler, blockerParentLabel) || !isInABox(r, blocker, blockerParent))) {
                        if (hasMoreThanNSuccessors(blocker, atMost.getNumber()-1, r, filler)) {
                            disjunctSatisfied = false;
                        }
                    }
                } else if (disjunct instanceof AtomicConcept) {
                    // happens if we have something like A -> (>= n r.B) or C. If (>= n r.B) is not guaranteed 
                    // for the blocker, but C is, then we are fine, so only if C does not hold, we have to look further.
                    if (!isInLabel((AtomicConcept) disjunct, blockerLabel)) {
                        disjunctSatisfied = false;
                    }
                } else {
                    throw new IllegalStateException("Internal error: Concepts in the conclusion of core blocking constraints are supposed to be atomic classes, at least or at most constraints, but this class is an instance of " + disjunct.getClass().getSimpleName());
                }
            }
            if (!disjunctSatisfied) {
                return false;
            }
        }
        return true;
    }
    protected boolean hasMoreThanNSuccessors(Node blocker, int n, Role r, LiteralConcept filler) {
        int suitableSuccessors = 0;
        if (r instanceof AtomicRole) {
            m_ternaryTableSearchZeroOneBound.getBindingsBuffer()[0]=r;
            m_ternaryTableSearchZeroOneBound.getBindingsBuffer()[1]=blocker;
            m_ternaryTableSearchZeroOneBound.open();
            Object[] tupleBuffer=m_ternaryTableSearchZeroOneBound.getTupleBuffer();
            while (!m_ternaryTableSearchZeroOneBound.afterLast() && suitableSuccessors < n) {
                if (filler instanceof AtomicConcept) {
                    m_binaryTableAllBound.getBindingsBuffer()[0]=filler;
                    m_binaryTableAllBound.getBindingsBuffer()[1]=tupleBuffer[2];
                    m_binaryTableAllBound.open();
                    m_binaryTableAllBound.getTupleBuffer();
                    if (!m_binaryTableAllBound.afterLast()) {
                        suitableSuccessors++;
                    }
                } else {
                    // negated atomic concept
                    m_binaryTableAllBound.getBindingsBuffer()[0]=((AtomicNegationConcept)filler).getNegatedAtomicConcept();
                    m_binaryTableAllBound.getBindingsBuffer()[1]=tupleBuffer[2];
                    m_binaryTableAllBound.open();
                    m_binaryTableAllBound.getTupleBuffer();
                    if (m_binaryTableAllBound.afterLast()) {
                        suitableSuccessors++;
                    }
                }
                m_ternaryTableSearchZeroOneBound.next();
            }
            return (suitableSuccessors >= n);
        } else {
            // inverse role
            m_ternaryTableSearchZeroTwoBound.getBindingsBuffer()[0]=r.getInverse();
            m_ternaryTableSearchZeroTwoBound.getBindingsBuffer()[2]=blocker;
            m_ternaryTableSearchZeroTwoBound.open();
            Object[] tupleBuffer=m_ternaryTableSearchZeroTwoBound.getTupleBuffer();
            while (!m_ternaryTableSearchZeroTwoBound.afterLast() && suitableSuccessors < n) {
                if (filler instanceof AtomicConcept) {
                    m_binaryTableAllBound.getBindingsBuffer()[0]=filler;
                    m_binaryTableAllBound.getBindingsBuffer()[1]=tupleBuffer[1];
                    m_binaryTableAllBound.open();
                    m_binaryTableAllBound.getTupleBuffer();
                    if (!m_binaryTableAllBound.afterLast()) {
                        suitableSuccessors++;
                    }
                } else {
                    // negated atomic concept
                    m_binaryTableAllBound.getBindingsBuffer()[0]=((AtomicNegationConcept)filler).getNegatedAtomicConcept();
                    m_binaryTableAllBound.getBindingsBuffer()[1]=tupleBuffer[1];
                    m_binaryTableAllBound.open();
                    m_binaryTableAllBound.getTupleBuffer();
                    if (m_binaryTableAllBound.afterLast()) {
                        suitableSuccessors++;
                    }
                }
                m_ternaryTableSearchZeroTwoBound.next();
            }
            return (suitableSuccessors >= n);
        }
    }
    protected boolean isInABox(Role r, Node first, Node second) {
        if (r instanceof AtomicRole) {
            m_ternaryTableSearchAllBound.getBindingsBuffer()[0]=r;
            m_ternaryTableSearchAllBound.getBindingsBuffer()[1]=first;
            m_ternaryTableSearchAllBound.getBindingsBuffer()[2]=second;
        } else {
            // inverse role
            m_ternaryTableSearchAllBound.getBindingsBuffer()[0]=r.getInverse();
            m_ternaryTableSearchAllBound.getBindingsBuffer()[1]=second;
            m_ternaryTableSearchAllBound.getBindingsBuffer()[2]=first;
        }
        m_ternaryTableSearchAllBound.open();
        m_ternaryTableSearchAllBound.getTupleBuffer(); // maybe this is unneccessary
        return (!m_ternaryTableSearchAllBound.afterLast());
    }
    protected boolean isInLabel(LiteralConcept c, Set<AtomicConcept> nodeLabel) {
        if (c instanceof AtomicConcept && nodeLabel.contains((AtomicConcept) c)) return true;
        if (c instanceof AtomicNegationConcept && !nodeLabel.contains(((AtomicNegationConcept) c).getNegatedAtomicConcept())) return true;
        return false;
    }
    public boolean isPermanentAssertion(Concept concept,Node node) {
        return true;
    }
    // Assertions can be added directly into the core, but we also have the possibility of setting the core flag later?
    // In that case, assertionCoreSet (below) will be called?
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        updateNodeChange(m_directBlockingChecker.assertionAdded(concept,node));
    }
    public void assertionCoreSet(Concept concept,Node node) {
    }
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        updateNodeChange(m_directBlockingChecker.assertionRemoved(concept,node));
    }
    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        updateNodeChange(m_directBlockingChecker.assertionAdded(atomicRole, nodeFrom, nodeTo));
    }
    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
    }
    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        updateNodeChange(m_directBlockingChecker.assertionRemoved(atomicRole, nodeFrom, nodeTo));
    }
    public void nodeStatusChanged(Node node) {
        updateNodeChange(node);
    }
    protected final void updateNodeChange(Node node) {
        if (node!=null && (m_firstChangedNode==null || node.getNodeID()<m_firstChangedNode.getNodeID()))
            m_firstChangedNode=node;
    }
    public void nodeInitialized(Node node) {
        m_directBlockingChecker.nodeInitialized(node);
    }
    public void nodeDestroyed(Node node) {
        m_currentBlockersCache.removeNode(node);
        boolean hadBlockingCargo = node.getBlockingCargo() != null;
        boolean wasRemoved = m_currentBlockersCache.removeNode(node);
        if (hadBlockingCargo && !wasRemoved) {
            throw new IllegalStateException("Node with blocking Cargo was not removed!");
        }
        m_directBlockingChecker.nodeDestroyed(node);
        if (m_firstChangedNode!=null && m_firstChangedNode.getNodeID()>=node.getNodeID())
            m_firstChangedNode=null;
    }
    public void modelFound() {
        if (printingOn) printStatistics(false);
        //System.out.println("Found  model with " + (m_tableau.getNumberOfNodesInTableau()-m_tableau.getNumberOfMergedOrPrunedNodes()));
    }
    protected void printStatistics(boolean intermediate) {
        int currentNumOfNodes = m_tableau.getNumberOfNodesInTableau(); // These are the active ones plus the merged and pruned ones
        currentNumOfNodes-=m_tableau.getNumberOfMergedOrPrunedNodes();
        int currentMaxLabelSize = 0;
        long currentSumLabelSize = 0;
        Node node = m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (node.isActive()) {
                int labelSize = node.getNumberOfPositiveAtomicConcepts();
                if (labelSize > currentMaxLabelSize) currentMaxLabelSize = labelSize;
                currentSumLabelSize += labelSize;
            }
            node=node.getNextTableauNode();
        }
        double currentAvgLabelSize = (double)currentSumLabelSize / currentNumOfNodes;
        System.out.printf("%-8s%-8s%-8s%-8s%-3s", run, currentNumOfNodes, sd(currentAvgLabelSize), currentMaxLabelSize, "|");
        if (!intermediate) {
            sumNodes += currentNumOfNodes;
            avgLabel += currentAvgLabelSize;
            if (currentMaxLabelSize > maxLabel) maxLabel = currentMaxLabelSize;
            if (currentNumOfNodes > maxNodes) maxNodes = currentNumOfNodes;
            System.out.printf("%-8s%-8s%-8s%-8s", maxNodes, sd(((double)sumNodes/run)), sd(((double)avgLabel/run)), maxLabel);
        }
        System.out.printf("%n");
    }
    protected void printHeader() {
        System.out.printf("%n%-39s %-2s%n", "This run:", "All runs:");
        System.out.printf("%-8s%-8s%-8s%-8s%-3s%-8s%-8s%-8s%-8s%n", "No", "Nodes", "avg", "max", "|", "max", "avg", "avg", "max");
        System.out.printf("%-8s%-8s%-8s%-8s%-3s%-8s%-8s%-8s%-8s%n", "", "", "lab", "lab", "|", "node", "node", "lab", "lab");
    }
    protected String sd(double d) {
        return new DecimalFormat("#.##").format(d);
    }
    public boolean isExact() {
        return false;
    }
    public void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,List<Variable> variables,Object[] valuesBuffer,boolean[] coreVariables) {
    }    
}
// The blockers set is a hash set of sorted sets of nodes. Each set in the cache contains nodes with equal label. In case of non-singleton sets, 
// the nodes in the set cannot block each other since their parents cause an invalid block. 
class TwoPhaseBlockersCache implements Serializable {
    private static final long serialVersionUID = 6016176408452543089L;
    protected Tableau m_tableau;
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected CacheEntry[] m_buckets;
    protected int m_numberOfElements;
    protected int m_threshold;
    protected CacheEntry m_emptyEntries;

    public TwoPhaseBlockersCache(DirectBlockingChecker directBlockingChecker) {
        m_directBlockingChecker=directBlockingChecker;
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
//    public void removeNode(Node node) {
//        // Check addNode() for an explanation of why we associate the entry with the node.
//        CacheEntry removeEntry=(TwoPhaseBlockersCache.CacheEntry)node.getBlockingCargo();
//        if (removeEntry!=null) {
//            int bucketIndex=getIndexFor(removeEntry.m_hashCode,m_buckets.length);
//            CacheEntry lastEntry=null;
//            CacheEntry entry=m_buckets[bucketIndex];
//            while (entry!=null) {
//                if (entry==removeEntry) {
//                    if (lastEntry==null)
//                        m_buckets[bucketIndex]=entry.m_nextEntry;
//                    else
//                        lastEntry.m_nextEntry=entry.m_nextEntry;
//                    entry.m_nextEntry=m_emptyEntries;
//                    entry.m_node=null;
//                    entry.m_hashCode=0;
//                    m_emptyEntries=entry;
//                    m_numberOfElements--;
//                    node.setBlockingCargo(null);
//                    return;
//                }
//                lastEntry=entry;
//                entry=entry.m_nextEntry;
//            }
//            throw new IllegalStateException("Internal error: entry not in cache!");
//        }
//    }
    public boolean removeNode(Node node) {
        // Check addNode() for an explanation of why we associate the entry with the node.
        TwoPhaseBlockersCache.CacheEntry removeEntry=(TwoPhaseBlockersCache.CacheEntry)node.getBlockingCargo();
        if (removeEntry!=null) {
            int bucketIndex=getIndexFor(removeEntry.m_hashCode,m_buckets.length);
            CacheEntry lastEntry=null;
            CacheEntry entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (entry==removeEntry) {
                    if (node == entry.m_nodes.first()) {
                        // the whole entry needs to be removed
                        for (Node n : entry.m_nodes) {
                            n.setBlockingCargo(null);
                        }
                        if (lastEntry==null)
                            m_buckets[bucketIndex]=entry.m_nextEntry;
                        else
                            lastEntry.m_nextEntry=entry.m_nextEntry;
                        entry.m_nextEntry=m_emptyEntries;
                        entry.m_nodes=new TreeSet<Node>(NodeIDComparator.INSTANCE);
                        entry.m_hashCode=0;
                        m_emptyEntries=entry;
                        m_numberOfElements--;
                    } else if (entry.m_nodes.contains(node)) {
                        for (Node n : entry.m_nodes.tailSet(node)) {
                            n.setBlockingCargo(null);
                        }
                        entry.removeNodesGreaterThan(node);
                    } else {
                        throw new IllegalStateException("Internal error: entry not in cache!");
                    }
                    return true;
                }
                lastEntry=entry;
                entry=entry.m_nextEntry;
            }
            throw new IllegalStateException("Internal error: entry not in cache!");
        }
        return false;
    }
//    public void addNode(Node node) {
//        int hashCode=m_directBlockingChecker.blockingHashCode(node);
//        int bucketIndex=getIndexFor(hashCode,m_buckets.length);
//        CacheEntry entry=m_buckets[bucketIndex];
//        while (entry!=null) {
//            //if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_node,node))
//            if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_nodes.first(),node))
//                throw new IllegalStateException("Internal error: node already in the cache!");
//            entry=entry.m_nextEntry;
//        }
//        if (m_emptyEntries==null)
//            entry=new CacheEntry();
//        else {
//            entry=m_emptyEntries;
//            m_emptyEntries=m_emptyEntries.m_nextEntry;
//        }
//        entry.initialize(node,hashCode,m_buckets[bucketIndex]);
//        m_buckets[bucketIndex]=entry;
//        // When a node is added to the cache, we record with the node the entry.
//        // This is used to remove nodes from the cache. Note that changes to a node
//        // can affect its label. Therefore, we CANNOT remove a node by taking its present
//        // blocking hash-code, as this can be different from the hash-code used at the
//        // time the node has been added to the cache.
//        node.setBlockingCargo(entry);
//        m_numberOfElements++;
//        if (m_numberOfElements>=m_threshold)
//            resize(m_buckets.length*2);
//    }
    public void addNode(Node node) {
        int hashCode=m_directBlockingChecker.blockingHashCode(node);
        int bucketIndex=getIndexFor(hashCode,m_buckets.length);
        CacheEntry entry=m_buckets[bucketIndex];
        while (entry!=null) {
            if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_nodes.first(),node)) {
                if (!entry.m_nodes.contains(node)) {
                    entry.add(node);
                    node.setBlockingCargo(entry);
                    return;
                } else {
                    throw new IllegalStateException("Internal error: node already in the cache!");
                }
            }
            entry=entry.m_nextEntry;
        }
        // all the entries in the bucket have a different core, so we have to add a new entry 
        if (m_emptyEntries==null)
            entry=new CacheEntry();
        else {
            entry=m_emptyEntries;
            m_emptyEntries=m_emptyEntries.m_nextEntry;
        }
        entry.initialize(node,hashCode,m_buckets[bucketIndex]);
        m_buckets[bucketIndex]=entry;
        // When a node is added to the cache, we record with the node the entry.
        // This is used to remove nodes from the cache. Note that changes to a node
        // can affect its label. Therefore, we CANNOT remove a node by taking its present
        // blocking hash-code, as this can be different from the hash-code used at the
        // time the node has been added to the cache.
        node.setBlockingCargo(entry);
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
    public Node getBlockerRepresentative(Node node) {
        if (m_directBlockingChecker.canBeBlocked(node)) {
            int hashCode=m_directBlockingChecker.blockingHashCode(node);
            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
            CacheEntry entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_nodes.first(),node)) {
                    return entry.m_nodes.first();
                }
//                if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_node,node)) {
//                    return entry.m_node;
//                }
                entry=entry.m_nextEntry;
            }
        }
        return null;
    }
    public SortedSet<Node> getPossibleBlockers(Node node) {
//        if (m_directBlockingChecker.canBeBlocked(node)) {
//            int hashCode=m_directBlockingChecker.blockingHashCode(node);
//            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
//            CacheEntry entry=m_buckets[bucketIndex];
//            while (entry!=null) {
//                if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_node,node)) {
//                    return entry.m_node;
//                }
//                entry=entry.m_nextEntry;
//            }
//        }
//        return null;
        if (m_directBlockingChecker.canBeBlocked(node)) {
            int hashCode=m_directBlockingChecker.blockingHashCode(node);
            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
            CacheEntry entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_nodes.first(),node)) {
                    // We block only with nodes that have a smaller ID than the node that is to be blocked
                    // so return the head set
                    return new TreeSet<Node>(entry.m_nodes.headSet(node));
                }
                entry=entry.m_nextEntry;
            }
        }
        return new TreeSet<Node>(NodeIDComparator.INSTANCE);
    }
    protected static int getIndexFor(int hashCode,int tableLength) {
        hashCode+=~(hashCode << 9);
        hashCode^=(hashCode >>> 14);
        hashCode+=(hashCode << 4);
        hashCode^=(hashCode >>> 10);
        return hashCode & (tableLength-1);
    }
    public String toString() {
        String buckets = "";
        for (int i = 0; i < m_buckets.length; i++) {
            CacheEntry entry=m_buckets[i];
            if (entry != null) {
                buckets += "Bucket " + i + ": [" + entry.toString() + "] ";
            }
        }
        return buckets;
    }

    public static class CacheEntry implements Serializable {
        private static final long serialVersionUID = 8784206693977395751L;
        protected SortedSet<Node> m_nodes;
//        protected Node m_node;
        protected int m_hashCode;
        protected CacheEntry m_nextEntry;

        public void initialize(Node node,int hashCode,CacheEntry nextEntry) {
            m_nodes = new TreeSet<Node>(new NodeIDComparator());
            m_nodes.add(node);
//            m_node=node;
            m_hashCode=hashCode;
            m_nextEntry=nextEntry;
        }
        public boolean add(Node node) {
//            if (!m_nodes.isEmpty() && m_nodes.last().getNodeID() >= node.getNodeID()) {
//                throw new IllegalStateException("Internal error: Tried to insert a node into the blocking cache which is not greater than the other nodes in the equivalence class. ");
//            }
            return m_nodes.add(node);
        }
        public void removeNodesGreaterThan(Node node) {
            m_nodes = new TreeSet<Node>(m_nodes.headSet(node));
        }
//        public String toString() {
//            String nodes = "HashCode: " + m_hashCode + " Nodes: ";
//            for (Node n : m_nodes) {
//                nodes += n.getNodeID() + " ";
//            }
//            return nodes;
//        }
    }
}