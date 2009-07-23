package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.blocking.CorePreDirectBlockingChecker.SinglePreCoreBlockingObject;
import org.semanticweb.HermiT.blocking.core.AtMostConcept;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;

public class AnywhereCoreBlocking implements BlockingStrategy, Serializable {
    private static final long serialVersionUID=-2959900333817197464L;

    protected Tableau m_tableau;
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final CoreBlockersCache m_currentBlockersCache;
    protected final Object[] m_auxiliaryTuple;
    protected final boolean m_hasInverses;
    protected ExtensionManager m_extensionManager;
    protected Node m_firstChangedNode;
    protected final Map<AtomicConcept, Set<Set<Concept>>> m_unaryValidBlockConditions; 
    protected final Map<Set<AtomicConcept>, Set<Set<Concept>>> m_nAryValidBlockConditions;
    protected ExtensionTable.Retrieval m_ternaryTableSearchAllBound;
    protected ExtensionTable.Retrieval m_ternaryTableSearchZeroOneBound;
    protected ExtensionTable.Retrieval m_ternaryTableSearchZeroTwoBound;
    protected ExtensionTable.Retrieval m_binaryTableAllBound;
    protected ExtensionTable.Retrieval m_ternaryTableSearchOneBound;
    protected ExtensionTable.Retrieval m_ternaryTableSearchTwoBound;
    protected boolean m_immediatelyValidateBlocks = false;
    protected Node m_lastValidatedUnchangedNode=null;
    // statistics: 
    protected final boolean printingOn=true;
    protected int numBlockingComputed = 0;
    protected int maxCore = 0;
    protected int maxLabel = 0;
    protected int avgCore = 0;
    protected int avgLabel = 0;
    protected int maxNodes = 0;
    protected long sumNodes = 0;
    protected int run = 1;
    
    public AnywhereCoreBlocking(DirectBlockingChecker directBlockingChecker, Map<AtomicConcept, Set<Set<Concept>>> unaryValidBlockConditions, Map<Set<AtomicConcept>, Set<Set<Concept>>> nAryValidBlockConditions, boolean hasInverses) {
        m_directBlockingChecker=directBlockingChecker;
        m_unaryValidBlockConditions = unaryValidBlockConditions;
        m_nAryValidBlockConditions = nAryValidBlockConditions;
        m_hasInverses = hasInverses;
        m_currentBlockersCache=new CoreBlockersCache(this, m_directBlockingChecker); // contains all nodes that block some node
        m_auxiliaryTuple=new Object[2];
    }
    public void initialize(Tableau tableau) {
        numBlockingComputed = 0;
        m_tableau=tableau;
        m_directBlockingChecker.initialize(tableau);
        m_extensionManager=m_tableau.getExtensionManager();
        m_ternaryTableSearchAllBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,true },ExtensionTable.View.TOTAL);
        m_ternaryTableSearchZeroOneBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
        m_ternaryTableSearchZeroTwoBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.TOTAL);
        m_binaryTableAllBound=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { true,true },ExtensionTable.View.TOTAL);
        m_ternaryTableSearchOneBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,false },ExtensionTable.View.TOTAL);
        m_ternaryTableSearchTwoBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { false,false,true },ExtensionTable.View.TOTAL);
    }
    public void clear() {
        m_currentBlockersCache.clear();
        m_directBlockingChecker.clear();
        m_immediatelyValidateBlocks=false;
        m_firstChangedNode=null;
        numBlockingComputed=0;
        run++;
        if (printingOn) printHeader();
    }
//    public boolean computeIsBlocked(Node node) {
//        m_currentBlockersCache.removeNode(node);
//        if (node.isActive() && m_directBlockingChecker.canBeBlocked(node)) {
//            Node parent=node.getParent();
//            boolean wasBlockedBefore=false;
//            if (node.isBlocked()) {
//                wasBlockedBefore=true;
//                if (!node.isDirectlyBlocked()) {
//                    computeIsBlocked(parent); // go upwards to the directly blocked node and check that first
//                    if (parent.isBlocked()) {
//                        node.setBlocked(parent,false);
//                        return true;
//                    }
//                }
//            }
//            // ok, the node is not indirectly blocked, lets see whether it is directly blocked
//            Node blocker;
//            if (m_immediatelyValidateBlocks) {
//                blocker = getValidBlocker(node);
//            } else {
//                blocker = m_currentBlockersCache.getBlockerRepresentativeLazy(node);
//            }
//            // get Blocker will always return a node with lower id and that only if all nodes in the cache with this core have a node ID that is smaller than this one  
//            // note that we removed only nodes from the cache that are of order higher than the first changed element
//            // only nodes of lower order than this one can be blockers and all those have already been looked at in this computation or 
//            // were not removed from the cache since they did not change
//            node.setBlocked(blocker,blocker!=null);
//            if (!node.isBlocked()) {
//                m_currentBlockersCache.addNode(node); // adds node either as main or as an alternative for a node with smaller node ID and identical core
//            } else {
//                if (!wasBlockedBefore) blockChildren(node);
//            }
//            m_directBlockingChecker.clearBlockingInfoChanged(node);
//        }
//        return node.isBlocked();
//    }
    protected void blockChildren(Node node) {
        List<Node> children=new ArrayList<Node>();
        m_ternaryTableSearchOneBound.getBindingsBuffer()[1]=node;
        m_ternaryTableSearchOneBound.open();
        Object[] tupleBuffer=m_ternaryTableSearchOneBound.getTupleBuffer();
        while (!m_ternaryTableSearchOneBound.afterLast()) {
            Node possibleChild=(Node)tupleBuffer[2];
            if (possibleChild.getParent()==node) {
                children.add(possibleChild);
            }
            m_ternaryTableSearchOneBound.next();
        }
        m_ternaryTableSearchTwoBound.getBindingsBuffer()[2]=node;
        m_ternaryTableSearchTwoBound.open();
        tupleBuffer=m_ternaryTableSearchTwoBound.getTupleBuffer();
        while (!m_ternaryTableSearchTwoBound.afterLast()) {
            Node possibleChild=(Node)tupleBuffer[1];
            if (possibleChild.getParent()==node) {
                children.add(possibleChild);
            }
            m_ternaryTableSearchTwoBound.next();
        }
        for (Node child : children) {
            child.setBlocked(node, false);
            blockChildren(child);
        }
    } 
    public void computeBlocking(boolean finalChance) {
        computePreBlocking(null);
        if (finalChance) {
            validateBlocks(null);
        }
    }
    public void computePreBlocking(Set<Node> nodesToExpand) {
        numBlockingComputed++;
        int numBlockersChanged=0;
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
                            Node previousBlocker=node.getBlocker();
                            if (m_immediatelyValidateBlocks) {
                                blocker = getValidBlocker(node);
                            } else {
                                blocker = m_currentBlockersCache.getSmallestPossibleBlocker(node);
                            }
                            if (previousBlocker!=null&&blocker==null || previousBlocker==null&&blocker!=null)
                                numBlockersChanged++;
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
                if (nodesToExpand!=null && !node.isBlocked() && node.getUnprocessedExistentials().size()>0)
                    nodesToExpand.add(node);
                node=node.getNextTableauNode();
            }
            //if (printingOn) System.out.println("Num of changed blockers: " + numBlockersChanged);
            m_firstChangedNode=null;
        }
    }
    public void validateBlocks(Set<Node> invalidlyBlockedNodes) {
        // after first complete validation, we switch to only checking block validity immediately
        //m_immediatelyValidateBlocks = true;
        if (!m_unaryValidBlockConditions.isEmpty() || !m_nAryValidBlockConditions.isEmpty()) {
            // statistics:
            int checkedBlocks = 0;
            int invalidBlocks = 0;
            
            Node node = m_lastValidatedUnchangedNode==null?m_tableau.getFirstTableauNode():m_lastValidatedUnchangedNode;
            if (m_firstChangedNode!=null&&m_firstChangedNode.getNodeID()<node.getNodeID()) node=m_firstChangedNode;
            int activeNodes=m_tableau.getNumberOfNodesInTableau()-m_tableau.getNumberOfMergedOrPrunedNodes();
            if (printingOn) System.out.print("Validate blocks (active nodes: "+activeNodes+", number of node that need validation: "+(m_tableau.getNumberOfNodesInTableau()-node.getNodeID())+") ...");
            while (node!=null) {
                if (node.isActive() && node.isBlocked()) {
                    // check whether the block is a correct one
                    if (node.isDirectlyBlocked()) {
                        checkedBlocks++;
                        Node validBlocker = getValidBlocker(node); 
                        if (validBlocker == null) {
                            //System.out.println("Node " + node.getBlocker().getNodeID() + " invalidly blocks " + node.getNodeID() + "!");
                            invalidBlocks++;
                            if (invalidlyBlockedNodes!=null && node.getUnprocessedExistentials().size()>0) { 
                                invalidlyBlockedNodes.add(node);
                            }
                            //((SinglePreCoreBlockingObject)node.getBlockingObject()).setGreatestInvalidBlocker(m_currentBlockersCache.getPossibleBlockers(node).last());
                        }
                        node.setBlocked(validBlocker,validBlocker!=null);
                    } else if (!node.getParent().isBlocked()) {
                        checkedBlocks++;
                        // indirectly blocked since we proceed in creation order, 
                        // parent has already been checked for proper blocking
                        // if the parent is no longer blocked, unblock this one too
                        Node validBlocker = getValidBlocker(node); 
                        if (validBlocker == null) {
                            invalidBlocks++;
                            if (invalidlyBlockedNodes!=null && node.getUnprocessedExistentials().size()>0) { 
                                invalidlyBlockedNodes.add(node);
                            }
                        }
                        node.setBlocked(validBlocker,validBlocker!=null);
                    }
                    if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                        m_currentBlockersCache.addNode(node);
                }
                m_lastValidatedUnchangedNode=node;
                node=node.getNextTableauNode();
            }
            // if set to some node, then computePreblocking will be asked to check from that node onwards in case of invalid blocks 
            m_firstChangedNode=null;
            if (printingOn) System.out.println("Checked " + checkedBlocks + " blocked nodes of which " + invalidBlocks + " were invalid.");
            if (printingOn&&invalidlyBlockedNodes!=null) System.out.println("Invalidly blocked nodes that need expansion: " + invalidlyBlockedNodes.size());
        }
    }
    protected Node getValidBlocker(Node blocked) {
        // we have that blocker (pre-)blocks blocked and we have to validate whether the block is valid 
        // that is we can create a model from the block by unravelling
        
        SortedSet<Node> possibleValidBlockers = m_currentBlockersCache.getPossibleBlockers(blocked);
        Node greatestInvalidBlocker = ((SinglePreCoreBlockingObject)blocked.getBlockingObject()).m_greatestInvalidBlocker;
        if (greatestInvalidBlocker != null) {
            possibleValidBlockers = new TreeSet<Node>(possibleValidBlockers.tailSet(greatestInvalidBlocker));
            possibleValidBlockers.remove(greatestInvalidBlocker);
            if (possibleValidBlockers.isEmpty()) return null;
        }
        
        Set<AtomicConcept> blockedLabel = ((SinglePreCoreBlockingObject)blocked.getBlockingObject()).getAtomicConceptLabel();
        Set<AtomicConcept> blockedParentLabel = ((SinglePreCoreBlockingObject)blocked.getParent().getBlockingObject()).getAtomicConceptLabel();
        
        boolean blockerIsSuitable = true;
        for (Iterator<Node> it=possibleValidBlockers.iterator(); it.hasNext(); ) {
            Node possibleBlocker=it.next();
            Set<AtomicConcept> blockerLabel = ((SinglePreCoreBlockingObject)possibleBlocker.getBlockingObject()).getAtomicConceptLabel();
            Set<AtomicConcept> blockerParentLabel = ((SinglePreCoreBlockingObject)possibleBlocker.getParent().getBlockingObject()).getAtomicConceptLabel();
            
            // check whether min/max cardinalities of the parent of the blocked node could be violated
            // universals and existential have been converted to min/max restrictions for convenience
            AtomicConcept c;
            for (Iterator<AtomicConcept> bpIt = blockedParentLabel.iterator(); bpIt.hasNext() && blockerIsSuitable; ) {
                c = bpIt.next();
                if (m_unaryValidBlockConditions.containsKey(c) && !isBlockedParentSuitable(m_unaryValidBlockConditions.get(c), blocked, blocked.getParent(), blockerLabel, blockerParentLabel, blockedLabel, blockedParentLabel)) 
                    blockerIsSuitable = false;
            }
            // check top, which is not explicitly present in the label, but might be the premise of some constraint
            if (m_unaryValidBlockConditions.containsKey(AtomicConcept.THING) && !isBlockedParentSuitable(m_unaryValidBlockConditions.get(AtomicConcept.THING), blocked, blocked.getParent(), blockerLabel, blockerParentLabel, blockedLabel, blockedParentLabel)) 
                blockerIsSuitable = false;
            
            // repeat the same checks for non-unary premises (less efficient matching operations)
            if (blockerIsSuitable) {
                for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
                    if (blockerIsSuitable && blockedParentLabel.containsAll(premises) && !isBlockedParentSuitable(m_nAryValidBlockConditions.get(premises), blocked, blocked.getParent(), blockedLabel, blockedParentLabel, blockerLabel, blockerParentLabel)) {
                        blockerIsSuitable = false;
                    }
                }
            }
            // check whether min/max cardinalities of the blocker are not violated when copied to the blocked node
            if (blockerIsSuitable && m_hasInverses) {
                for (Iterator<AtomicConcept> blIt = blockerLabel.iterator(); blIt.hasNext() && blockerIsSuitable; ) {
                    c = blIt.next();
                    if (m_unaryValidBlockConditions.containsKey(c) && !isBlockerSuitable(m_unaryValidBlockConditions.get(c), possibleBlocker, possibleBlocker.getParent(), blocked, blocked.getParent(), blockerLabel, blockerParentLabel, blockedLabel, blockedParentLabel)) 
                        blockerIsSuitable = false;
                }
            }
            // check top, which is not explicitly present in the label, but might be the premise of some constraint
            if (m_unaryValidBlockConditions.containsKey(AtomicConcept.THING) && !isBlockerSuitable(m_unaryValidBlockConditions.get(AtomicConcept.THING), possibleBlocker, possibleBlocker.getParent(), blocked, blocked.getParent(), blockerLabel, blockerParentLabel, blockedLabel, blockedParentLabel)) {    
                blockerIsSuitable = false;
            }
            // repeat the same checks for non-unary premises (less efficient matching operations)
            if (blockerIsSuitable && m_hasInverses) {
                for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
                    if (blockerIsSuitable && blockerLabel.containsAll(premises) && !isBlockerSuitable(m_nAryValidBlockConditions.get(premises), possibleBlocker, possibleBlocker.getParent(), blocked, blocked.getParent(), blockerLabel, blockerParentLabel, blockedLabel, blockedParentLabel)) 
                        blockerIsSuitable = false;
                }
            }
            if (blockerIsSuitable) {
                return possibleBlocker;
            } else {
                ((SinglePreCoreBlockingObject)blocked.getBlockingObject()).m_greatestInvalidBlocker=possibleBlocker;
            }
            blockerIsSuitable=true;
            // else try alternative blockers with the same core
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
                    // or blocker has at least n unblocked r-successors bs such that B(bs) holds
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
                        if (atMost.getNumber()==0 || hasMoreThanNSuccessors(blocker, atMost.getNumber()-1, r, filler)) {
                            disjunctSatisfied = false;
                        }
                    }
                } else if (disjunct instanceof AtomicConcept) {
                    // happens if we have something like A -> (>= n r.B) or C. If (>= n r.B) is not guaranteed 
                    // for the blocker, but C is, then we are fine, so only if C does not hold, we have to look further.
                    if (!isInLabel((AtomicConcept) disjunct, blockerLabel)) {
                        disjunctSatisfied = false;
                    }
                } else if (disjunct == null) {
                    // bottom
                    disjunctSatisfied=false;
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
                Node possibleSuccessor=(Node)tupleBuffer[2];
                if (!possibleSuccessor.isBlocked() && !possibleSuccessor.isAncestorOf(blocker)) {
                    if (filler.isAlwaysTrue()) {
                        suitableSuccessors++;
                    } else if (!filler.isAlwaysFalse()) {
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
                Node possibleSuccessor=(Node)tupleBuffer[2];
                if (!possibleSuccessor.isBlocked() && !possibleSuccessor.isAncestorOf(blocker)) {
                    if (filler.isAlwaysTrue()) {
                        suitableSuccessors++;
                    } else if (!filler.isAlwaysFalse()) {
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
                    }
                }
                m_ternaryTableSearchZeroTwoBound.next();
            }
            return (suitableSuccessors >= n);
        }
    }
    protected boolean isInABox(Role r, Node first, Node second) {
        if (r==AtomicRole.TOP_OBJECT_ROLE) return true;
        if (r instanceof InverseRole && ((InverseRole)r).getInverseOf()==AtomicRole.TOP_OBJECT_ROLE) return true;
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
        if (c.isAlwaysTrue()) {
            return true; //top
        }
        if (c.isAlwaysFalse()) {
            return false;
        }
        if (c instanceof AtomicConcept && nodeLabel.contains((AtomicConcept) c)) return true;
        if (c instanceof AtomicNegationConcept && (!nodeLabel.contains(((AtomicNegationConcept) c).getNegatedAtomicConcept()))) return true;
        return false;
    }
    public boolean isPermanentAssertion(Concept concept,Node node) {
        m_auxiliaryTuple[0]=concept;
        m_auxiliaryTuple[1]=node;
        return m_extensionManager.isCore(m_auxiliaryTuple);
    }
    // Assertions can be added directly into the core, but we also have the possibility of setting the core flag later?
    // In that case, assertionCoreSet (below) will be called?
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        if ((isCore && concept instanceof AtomicConcept) || (concept instanceof AtLeastConcept)) {
            updateNodeChange(m_directBlockingChecker.assertionAdded(concept,node));
        }
    }
    public void assertionCoreSet(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            updateNodeChange(m_directBlockingChecker.assertionAdded(concept,node));
        } 
    }
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        if (isCore && concept instanceof AtomicConcept) {
            updateNodeChange(m_directBlockingChecker.assertionRemoved(concept,node));
        }
    }
    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        if (isCore) {
            updateNodeChange(m_directBlockingChecker.assertionAdded(atomicRole, nodeFrom, nodeTo));
        }
    }
    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        updateNodeChange(m_directBlockingChecker.assertionAdded(atomicRole, nodeFrom, nodeTo));
    }
    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        if (isCore) {
            updateNodeChange(m_directBlockingChecker.assertionRemoved(atomicRole, nodeFrom, nodeTo));
        }
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
        System.out.println("Found  model with " + (m_tableau.getNumberOfNodesInTableau()-m_tableau.getNumberOfMergedOrPrunedNodes()) + " nodes. ");
    }
    protected void printStatistics(boolean intermediate) {
        if (!intermediate) run++;
        int currentNumOfNodes = m_tableau.getNumberOfNodesInTableau(); // These are the active ones plus the merged and pruned ones
        currentNumOfNodes-=m_tableau.getNumberOfMergedOrPrunedNodes();
        int maxCoreSizeThisRun = 0;
        int maxLabelSizeThisRun = 0;
        double avgCoreSizeThisRun = 0.0;
        double avgLabelSizeThisRun = 0.0;
        long sumLabelSizeThisRun = 0;
        long sumCoreSizeThisRun = 0;
        Node node = m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (node.isActive()) {
                if (node.getNumberOfCoreAtoms() > maxCoreSizeThisRun) maxCoreSizeThisRun = node.getNumberOfCoreAtoms();
                if (node.getNumberOfPositiveAtomicConcepts() > maxLabelSizeThisRun) maxLabelSizeThisRun = node.getNumberOfPositiveAtomicConcepts();
                sumCoreSizeThisRun += node.getNumberOfCoreAtoms();
                sumLabelSizeThisRun += node.getNumberOfPositiveAtomicConcepts();
            }
            node=node.getNextTableauNode();
        }
        avgCoreSizeThisRun = (double)sumCoreSizeThisRun / currentNumOfNodes;
        avgLabelSizeThisRun = (double)sumLabelSizeThisRun / currentNumOfNodes;
        System.out.printf("%-8s%-8s%-8s%-8s%-8s%-8s%-3s", run, currentNumOfNodes, sd(avgLabelSizeThisRun), sd(avgCoreSizeThisRun), maxLabelSizeThisRun, maxCoreSizeThisRun, "|");
        if (!intermediate) {
            if (currentNumOfNodes > maxNodes) maxNodes = currentNumOfNodes;
            if (maxCoreSizeThisRun > maxCore) maxCore = maxCoreSizeThisRun;
            if (maxLabelSizeThisRun > maxLabel) maxLabel = maxLabelSizeThisRun;
            sumNodes += currentNumOfNodes;
            avgCore += avgCoreSizeThisRun;
            avgLabel += avgLabelSizeThisRun;
            System.out.printf("%-8s%-8s%-8s%-8s%-8s%-8s", maxNodes, sd(((double)sumNodes/run)), sd(((double)avgLabel/run)), sd(((double)avgCore/run)), maxLabel, maxCore);
        }
        System.out.printf("%n");
    }
    protected void printHeader() {
        System.out.printf("%n%-55s %-31s%n", "This run:", "All runs:");
        System.out.printf("%-8s%-8s%-8s%-8s%-8s%-8s%-3s%-8s%-8s%-8s%-8s%-8s%-8s%n", "No", "Nodes", "avg", "avg", "max", "max", "|", "max", "avg", "avg", "avg", "max", "max");
        System.out.printf("%-8s%-8s%-8s%-8s%-8s%-8s%-3s%-8s%-8s%-8s%-8s%-8s%-8s%n", "", "", "lab", "core", "lab", "core", "|", "node", "node", "lab", "core", "lab", "core");
    }
    protected String sd(double d) {
        return new DecimalFormat("#.##").format(d);
    }
    public boolean isExact() {
        return false;
    }
    public void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,List<Variable> variables,Object[] valuesBuffer,boolean[] coreVariables) {
        //System.out.println(dlClause.toString());
        if (dlClause.isConceptInclusion() || dlClause.isRoleInclusion() || dlClause.isRoleInverseInclusion()) {
            for (int i=0;i<coreVariables.length;i++) {
                coreVariables[i]=false;
                return;
            }
        }
        if (dlClause.getHeadLength() > 2) {
            // in case of a disjunction, there is nothing to compute, the choice must go into the core
            // I assume that disjunctions are always only for the centre variable X and I assume that X is the first
            // variable in the array ???
            coreVariables[0] = true;
            return;
        } else {
            workers.add(new ComputeCoreVariables(dlClause,valuesBuffer,coreVariables));
        }
    }
    protected static final class ComputeCoreVariables implements DLClauseEvaluator.Worker,Serializable {
        private static final long serialVersionUID=899293772370136783L;

        protected final DLClause m_dlClause;
        protected final Object[] m_valuesBuffer;
        protected final boolean[] m_coreVariables;

        public ComputeCoreVariables(DLClause dlClause,Object[] valuesBuffer,boolean[] coreVariables) {
            m_dlClause=dlClause;
            m_valuesBuffer=valuesBuffer;
            m_coreVariables=coreVariables;
        }
        public int execute(int programCounter) {
            if (m_dlClause.getHeadLength() > 0 && (m_dlClause.getHeadAtom(0).getArity() != 1 || !m_dlClause.getHeadAtom(0).containsVariable(Variable.create("X")))) {
                Node potentialNoncore=null;
                int potentialNoncoreIndex=-1;
                for (int variableIndex=m_coreVariables.length-1;variableIndex>=0;--variableIndex) {
                    m_coreVariables[variableIndex]=true;
                    Node node=(Node)m_valuesBuffer[variableIndex];
                    if (node.getNodeType()==NodeType.TREE_NODE && (potentialNoncore==null || node.getTreeDepth()>potentialNoncore.getTreeDepth())) {
                        potentialNoncore=node;
                        potentialNoncoreIndex=variableIndex;
                    }
                }
                if (potentialNoncore!=null) {
                    boolean isNoncore=true;
                    for (int variableIndex=m_coreVariables.length-1;isNoncore && variableIndex>=0;--variableIndex) {
                        Node node=(Node)m_valuesBuffer[variableIndex];
                        if (!node.isRootNode() && potentialNoncore!=node && potentialNoncore.isAncestorOf(node))
                            isNoncore=false;
                    }
                    if (isNoncore) {
                        m_coreVariables[potentialNoncoreIndex]=false;
                    }
                }
            }
            return programCounter+1;
        }
        // What is this doing exactly?
        // Wht do you not need the DL clase itself?
//        public int execute(int programCounter) {
//            Node potentialNoncore=null;
//            int potentialNoncoreIndex=-1;
//            for (int variableIndex=m_coreVariables.length-1;variableIndex>=0;--variableIndex) {
//                m_coreVariables[variableIndex]=true;
//                Node node=(Node)m_valuesBuffer[variableIndex];
//                if (node.getNodeType()==NodeType.TREE_NODE && (potentialNoncore==null || node.getTreeDepth()<potentialNoncore.getTreeDepth())) {
//                    potentialNoncore=node;
//                    potentialNoncoreIndex=variableIndex;
//                }
//            }
//            if (potentialNoncore!=null) {
//                boolean isNoncore=true;
//                for (int variableIndex=m_coreVariables.length-1;isNoncore && variableIndex>=0;--variableIndex) {
//                    Node node=(Node)m_valuesBuffer[variableIndex];
//                    if (!node.isRootNode() && potentialNoncore!=node && !potentialNoncore.isAncestorOf(node))
//                        isNoncore=false;
//                }
//                if (isNoncore) {
//                    m_coreVariables[potentialNoncoreIndex]=false;
//                }
//            }
//            return programCounter+1;
//        }
        public String toString() {
            return "Compute core variables";
        }
    }
}
// The core blockers set is a hash set of sorted sets of nodes. Each set in the cache contains nodes with equal core. In case of non-singleton sets, the nodes in the set cannot block each other since their parents or non-core parts result in a potentially invalid block. 
class CoreBlockersCache implements Serializable {
    private static final long serialVersionUID=-7692825443489644667L;

    protected Tableau m_tableau;
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final BlockingStrategy m_blockingStrategy;
    protected CacheEntry[] m_buckets;
    protected int m_numberOfElements;
    protected int m_threshold;
    protected CacheEntry m_emptyEntries;

    public CoreBlockersCache(BlockingStrategy blockingStrategy, DirectBlockingChecker directBlockingChecker) {
        m_directBlockingChecker=directBlockingChecker;
        m_blockingStrategy=blockingStrategy;
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
    public boolean removeNode(Node node) {
        // Check addNode() for an explanation of why we associate the entry with the node.
        CoreBlockersCache.CacheEntry removeEntry=(CoreBlockersCache.CacheEntry)node.getBlockingCargo();
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
    public void addNode(Node node) {
        if (node == null) {
            throw new IllegalStateException("Internal error: chache entry should not be null!");
        }
        int hashCode=m_directBlockingChecker.blockingHashCode(node);
        int bucketIndex=getIndexFor(hashCode,m_buckets.length);
        CacheEntry entry=m_buckets[bucketIndex];
        while (entry!=null) {
            if (hashCode==entry.m_hashCode && (entry.m_nodes.first()==node || m_directBlockingChecker.isBlockedBy(entry.m_nodes.first(),node))) {
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
    public Node getSmallestPossibleBlocker(Node node) {
        SortedSet<Node> possibleValidBlockers=getPossibleBlockers(node);
        return possibleValidBlockers.isEmpty()?null:possibleValidBlockers.first();
    }
//    public Node getBlockerRepresentativeLazy(Node node) {
//        assert m_directBlockingChecker.canBeBlocked(node) == true;
//        int hashCode=m_directBlockingChecker.blockingHashCode(node); // this gets the up to date hash code
//        int bucketIndex=getIndexFor(hashCode,m_buckets.length);
//        CacheEntry entry=m_buckets[bucketIndex];
//        while (entry!=null) {
//            if (hashCode==entry.m_hashCode) {
//                for (Node possibleBlocker : entry.m_nodes) {
//                    if (m_directBlockingChecker.hasBlockingInfoChanged(possibleBlocker)) {
//                        // we have to update the cash before we can proceed
//                        m_blockingStrategy.computeIsBlocked(possibleBlocker);
//                        return getBlockerRepresentativeLazy(node);
//                    } else if (m_directBlockingChecker.isBlockedBy(possibleBlocker,node) && possibleBlocker.getNodeID() < node.getNodeID()) {
//                        // isBlockedBy updates the real label, so after that call, 
//                        // the hash code and he label should be consistent and we can reset the m_hasChanged flag
//                        
//                        // maybe we have to take the greatestInvalidBlocker into account somehow
//                        return possibleBlocker;
//                    }
//                }
//            }
//            entry=entry.m_nextEntry;
//        }
//        return null;
//    }
    public SortedSet<Node> getPossibleBlockers(Node node) {
        if (m_directBlockingChecker.canBeBlocked(node)) {
            int hashCode=m_directBlockingChecker.blockingHashCode(node);
            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
            CacheEntry entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_nodes.first(),node)) {
                    // We block only with nodes that have a smaller ID than the node that is to be blocked
                    // so return the head set
                    if (node.getBlockingObject()!=null) {
                        Node greatestInvalidBlocker=((SinglePreCoreBlockingObject)node.getBlockingObject()).m_greatestInvalidBlocker;
                        if (greatestInvalidBlocker != null) {
                            SortedSet<Node> possibleValidBlockers = new TreeSet<Node>(entry.m_nodes.tailSet(greatestInvalidBlocker));
                            possibleValidBlockers.remove(greatestInvalidBlocker);
                            return possibleValidBlockers;
                        } 
                    }
                    return entry.m_nodes;
                }
                entry=entry.m_nextEntry;
            }
        }
        return new TreeSet<Node>(NodeIDComparator.INSTANCE);
    }
    public SortedSet<Node> getPossibleBlockersLazy(Node node) {
        assert m_directBlockingChecker.canBeBlocked(node) == true;
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
        private static final long serialVersionUID=-7047487963170250200L;

        protected SortedSet<Node> m_nodes;
        protected int m_hashCode;
        protected CacheEntry m_nextEntry;

        public void initialize(Node node,int hashCode,CacheEntry nextEntry) {
            m_nodes = new TreeSet<Node>(new NodeIDComparator());
            add(node);
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
        public String toString() {
            String nodes = "HashCode: " + m_hashCode + " Nodes: ";
            for (Node n : m_nodes) {
                nodes += n.getNodeID() + " ";
            }
            return nodes;
        }
    }
}
class NodeIDComparator implements Serializable, Comparator<Node> {
    private static final long serialVersionUID = 2112323818144484750L;
    public static final Comparator<Node> INSTANCE = new NodeIDComparator();

    public int compare(Node n1, Node n2) {
        return n1.getNodeID() - n2.getNodeID();
    }
}