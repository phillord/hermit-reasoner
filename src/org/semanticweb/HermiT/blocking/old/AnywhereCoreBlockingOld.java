// Copyright 2009 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking.old;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
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

public class AnywhereCoreBlockingOld implements BlockingStrategy, Serializable {
    private static final long serialVersionUID=-2959900333817197464L;

    public static enum BlockingViolationType {
        ATLEASTBLOCKEDPARENT, 
        ATMOSTBLOCKEDPARENT,
        ATOMICBLOCKEDPARENT,
        ATLEASTBLOCKER, 
        ATMOSTBLOCKER,
        ATOMICBLOCKER
    }
    
    protected Tableau m_tableau;
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final CoreBlockersCacheOld m_currentBlockersCache;
    protected final Object[] m_auxiliaryTuple;
    protected final boolean m_hasInverses;
    protected ExtensionManager m_extensionManager;
    protected Node m_firstChangedNode;
    protected final Map<AtomicConcept, Set<Set<Concept>>> m_unaryValidBlockConditions; 
    protected final Map<Set<AtomicConcept>, Set<Set<Concept>>> m_nAryValidBlockConditions;
    protected ExtensionTable.Retrieval m_ternaryTableSearchAllBound;
    protected ExtensionTable.Retrieval m_ternaryTableSearchZeroOneBound;
    protected ExtensionTable.Retrieval m_ternaryTableSearchZeroTwoBound;
    protected ExtensionTable.Retrieval m_ternaryTableSearchOneTwoBound;
    protected ExtensionTable.Retrieval m_binaryTableAllBound;
    protected ExtensionTable.Retrieval m_binaryTableOneBound;
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
    protected int numDirectlyBlocked=0;
    protected int numIndirectlyBlocked=0;
    protected final boolean debuggingMode=true;
    protected Map<String, Integer> m_violationCount=new HashMap<String, Integer>();
    protected Map<AnywhereCoreBlockingOld.BlockingViolationType, Integer> m_causesCount=new HashMap<BlockingViolationType, Integer>();
    
    public AnywhereCoreBlockingOld(DirectBlockingChecker directBlockingChecker, Map<AtomicConcept, Set<Set<Concept>>> unaryValidBlockConditions, Map<Set<AtomicConcept>, Set<Set<Concept>>> nAryValidBlockConditions, boolean hasInverses) {
        m_directBlockingChecker=directBlockingChecker;
        m_unaryValidBlockConditions = unaryValidBlockConditions;
        m_nAryValidBlockConditions = nAryValidBlockConditions;
        m_hasInverses = hasInverses;
        m_currentBlockersCache=new CoreBlockersCacheOld(m_directBlockingChecker); // contains all nodes that block some node
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
        m_ternaryTableSearchOneTwoBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,true },ExtensionTable.View.TOTAL);
        m_binaryTableAllBound=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { true,true },ExtensionTable.View.TOTAL);
        m_binaryTableOneBound=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        m_ternaryTableSearchOneBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,false },ExtensionTable.View.TOTAL);
        m_ternaryTableSearchTwoBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { false,false,true },ExtensionTable.View.TOTAL);
        if (debuggingMode) {
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATLEASTBLOCKEDPARENT, 0);
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATLEASTBLOCKER, 0);
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATMOSTBLOCKEDPARENT, 0);
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATMOSTBLOCKER, 0);
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATOMICBLOCKEDPARENT, 0);
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATOMICBLOCKER, 0);
        }
    }
    public void clear() {
        m_currentBlockersCache.clear();
        m_directBlockingChecker.clear();
        m_immediatelyValidateBlocks=false;
        m_firstChangedNode=null;
        numBlockingComputed=0;
        run++;
        if (printingOn) printHeader();
        if (debuggingMode) {
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATLEASTBLOCKEDPARENT, 0);
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATLEASTBLOCKER, 0);
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATMOSTBLOCKEDPARENT, 0);
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATMOSTBLOCKER, 0);
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATOMICBLOCKEDPARENT, 0);
            m_causesCount.put(AnywhereCoreBlockingOld.BlockingViolationType.ATOMICBLOCKER, 0);
        }
    }
    public void computeBlocking(boolean finalChance) {
        if (finalChance) {
            validateBlocks();
        } else {
            computePreBlocking();
        }
    }
    public void computePreBlocking() {
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
                if (node.isActive()) {
                    if (m_directBlockingChecker.canBeBlocked(node) || m_directBlockingChecker.canBeBlocker(node)) {
                        // otherwise the node is not relevant for blocking since (it is a root node) since it will not be blocked and cannot block
                        if (m_directBlockingChecker.hasBlockingInfoChanged(node) || !node.isDirectlyBlocked() || node.getBlocker().getNodeID()>=m_firstChangedNode.getNodeID()) {
                            // m_directBlockingChecker.hasBlockingInfoChanged(node): if relevant label has changed
                            // !node.isDirectlyBlocked(): either the node is not blocked or indirectly blocked, if it is not blocked, maybe we find now a blocker and if it is indirectly blocked, maybe the directly blocked ancestor is now no longer blocked, so we better check
                            // node.getBlocker().getNodeID()>=m_firstChangedNode.getNodeID(): node is directly blocked and unchanged, but the blocker possibly changed, so check again
                            Node parent=node.getParent();
                            if (parent==null)
                                node.setBlocked(null,false); // no parent means it cannot be blocked and cannot be blocker, but we should never be here...
                            else if (parent.isBlocked()) {// parent is guaranteed not to change it's status in this computation since we process nodes in creation order and parent is smaller
                                if (node.getBlocker() == null) {
                                    // previously not blocked
                                    numIndirectlyBlocked++;
                                }
                                node.setBlocked(parent,false);
                            } else {
                                Node blocker=null;
                                Node previousBlocker=node.getBlocker();
                                if (m_immediatelyValidateBlocks) {
                                    blocker = getValidBlocker(node);
                                } else {
                                    List<Node> possibleBlockers=m_currentBlockersCache.getPossibleBlockers(node);
                                    if (!possibleBlockers.isEmpty()) {
                                        if (m_directBlockingChecker.hasChangedSinceValidation(node) || m_directBlockingChecker.hasChangedSinceValidation(node.getParent())) {
                                            // the node or its parent has changed since we last validated the blocks, so even if all the blockers 
                                            // in the cache were invalid last time we validated, we'll give it another try
                                            blocker=possibleBlockers.get(0);
                                        } else {
                                            // neither the node nor its parent has not changed since the last validation
                                            // if also the possible blockers in the blockers cache and their parents have not changed
                                            // since the last validation, there is no point in blocking again
                                            // the only exception is that the blockers cache contains the node that blocked this node previously 
                                            // if that node and its parent is unchanged, then the block is still ok 
                                            for (Node n : possibleBlockers) {
                                                if (m_directBlockingChecker.hasChangedSinceValidation(n) || m_directBlockingChecker.hasChangedSinceValidation(n.getParent()) || n==previousBlocker) {
                                                    blocker=n;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    possibleBlockers=m_currentBlockersCache.getPossibleBlockers(node);
                                }
                                if (previousBlocker!=null&&blocker==null) {
                                    numBlockersChanged++;
                                    if (node.isDirectlyBlocked()) numDirectlyBlocked--;
                                    else numIndirectlyBlocked--;
                                } 
                                if (previousBlocker==null&&blocker!=null) {
                                    numBlockersChanged++;
                                    numDirectlyBlocked++;
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
                }
                node=node.getNextTableauNode();
            }
            //if (printingOn) System.out.println("Num of changed blockers: " + numBlockersChanged);
            if (debuggingMode && printingOn) System.out.println("Num nodes/dBlocked/indBlocked: " + (m_tableau.getNumberOfNodesInTableau()-m_tableau.getNumberOfMergedOrPrunedNodes()) + "/" + numDirectlyBlocked + "/" + numIndirectlyBlocked);
            m_firstChangedNode=null;
        }
    }
    protected final class BlockingViolation{
        private final Node m_blocked;
        private final Node m_blockedParent;
        private final Node m_invalidBlocker;
        private final Node m_invalidBlockerParent;
        private final Set<AtomicConcept> m_blockedLabel;
        private final Set<AtomicConcept> m_blockedParentLabel;
        private final Set<AtomicConcept> m_invalidBlockerLabel;
        private final Set<AtomicConcept> m_invalidBlockerParentLabel;
        private final Set<AtomicConcept> m_violationPremises; 
        private final Set<Concept> m_violatedConjunct;
        private final Map<Concept, BlockingViolationType> m_causes;
        private final Set<Role> m_blockerParentBlockerLabel;
        private final Set<Role> m_blockedParentBlockedLabel;

        public BlockingViolation(Node blocked, Node invalidBlocker, Set<AtomicConcept> blockedLabel, Set<AtomicConcept> blockedParentLabel, Set<AtomicConcept> invalidBlockerLabel, Set<AtomicConcept> invalidBlockerParentLabel, Set<AtomicConcept> c, Map<Set<Concept>,Map<Concept, BlockingViolationType>> causes) {
            // Nodes involved
            m_blocked=blocked;
            m_blockedParent=blocked.getParent();
            m_invalidBlocker=invalidBlocker;
            m_invalidBlockerParent=invalidBlocker.getParent();
            // Concept labels involved 
            m_blockedLabel=blockedLabel;
            m_blockedParentLabel=blockedParentLabel;
            m_invalidBlockerLabel=invalidBlockerLabel;
            m_invalidBlockerParentLabel=invalidBlockerParentLabel;
            // Edge labels involved
            m_blockerParentBlockerLabel=getEdgeLabel(m_invalidBlockerParent, m_invalidBlocker);
            m_blockedParentBlockedLabel=getEdgeLabel(m_blockedParent, m_blocked);
            // violation constraint
            m_violationPremises=c;
            m_violatedConjunct=causes.entrySet().iterator().next().getKey();
            m_causes=causes.get(m_violatedConjunct);
        }
        public BlockingViolation(Node blocked, Node invalidBlocker, Set<AtomicConcept> blockedLabel, Set<AtomicConcept> blockedParentLabel, Set<AtomicConcept> invalidBlockerLabel, Set<AtomicConcept> invalidBlockerParentLabel, AtomicConcept c, Map<Set<Concept>,Map<Concept, BlockingViolationType>> causes) {
            // Nodes involved
            m_blocked=blocked;
            m_blockedParent=blocked.getParent();
            m_invalidBlocker=invalidBlocker;
            m_invalidBlockerParent=invalidBlocker.getParent();
            // Concept labels involved 
            m_blockedLabel=blockedLabel;
            m_blockedParentLabel=blockedParentLabel;
            m_invalidBlockerLabel=invalidBlockerLabel;
            m_invalidBlockerParentLabel=invalidBlockerParentLabel;
            // Edge labels involved
            m_blockerParentBlockerLabel=getEdgeLabel(m_invalidBlockerParent, m_invalidBlocker);
            m_blockedParentBlockedLabel=getEdgeLabel(m_blockedParent, m_blocked);
            // violation constraint
            Set<AtomicConcept> premises=new HashSet<AtomicConcept>();
            premises.add(c);
            m_violationPremises=premises;
            m_violatedConjunct=causes.entrySet().iterator().next().getKey();
            m_causes=causes.get(m_violatedConjunct);
        }
        public String getViolatedConstraint() {
            String constraint="";
            boolean isFirst=true;
            for (Concept c : m_violationPremises) {
                if (!isFirst) constraint+=" /\\ ";
                constraint+=c;
                isFirst=false;
            }
            constraint+="->";
            isFirst=true;
            for (Concept c : m_violatedConjunct) {
                if (!isFirst) constraint+=" \\/ ";
                constraint+=c;
                isFirst=false;
            }
            return constraint;
        }
        public String toString() {
            StringBuffer b=new StringBuffer();
            b.append("Violation constraint: "+getViolatedConstraint()+"\n");
            b.append("Causes: ");
            for (Concept c : m_causes.keySet()) {
                b.append(c + " " + m_causes.get(c) + "\n");
            }
            if (m_directBlockingChecker instanceof CorePreDirectBlockingChecker) {
                b.append("Blocker parent: ");
                for (AtomicConcept c : m_invalidBlockerParentLabel) {
                    b.append(c.toString());
                }
                b.append("\n");
                b.append("BlockerParent->Blocker: ");
                for (Role r : m_blockerParentBlockerLabel) {
                    b.append(r.toString());
                }
                b.append("\n");
                b.append("Blocker: ");
                for (AtomicConcept c : m_invalidBlockerLabel) {
                    b.append(c.toString());
                }
                b.append("\n\n");
                b.append("Blocked parent: ");
                for (AtomicConcept c : m_blockedParentLabel) {
                    b.append(c.toString());
                }
                b.append("\n");
                b.append("BlockedParent->Blocked: ");
                for (Role r : m_blockedParentBlockedLabel) {
                    b.append(r.toString());
                }
                b.append("\n");
                b.append("Blocked: ");
                for (AtomicConcept c : m_blockedLabel) {
                    b.append(c.toString());
                }
                b.append("\n");
            }
            return b.toString();
        }
    }
    public void validateBlocks() {
        // after first complete validation, we can switch to only checking block validity immediately
        //m_immediatelyValidateBlocks = true;
        if (!m_unaryValidBlockConditions.isEmpty() || !m_nAryValidBlockConditions.isEmpty()) {
            // statistics:
            int checkedBlocks = 0;
            int invalidBlocks = 0;
            
            Node node = m_tableau.getFirstTableauNode();
            if (debuggingMode && printingOn) System.out.println("Validate blocks (active nodes: "+(m_tableau.getNumberOfNodesInTableau()-m_tableau.getNumberOfMergedOrPrunedNodes())+", highest nodeID: " + m_tableau.getNumberOfNodeCreations() + ", smallest node to validate: "+ node.getNodeID()+") ...");
            if (printingOn && m_tableau.getNumberOfNodesInTableau() >= 1000) System.out.print("Current ID:");
            while (node!=null) {
                if (node.isActive()) {
                    if (node.isBlocked()) {
                        // check whether the block is a correct one
                        if (node.isDirectlyBlocked()) {
                            checkedBlocks++;
                            Node validBlocker = getValidBlocker(node); 
                            if (validBlocker == null) {
                                //System.out.println("Node " + node.getBlocker().getNodeID() + " invalidly blocks " + node.getNodeID() + "!");
                                invalidBlocks++;
                            }
                            node.setBlocked(validBlocker,validBlocker!=null);
                        } else if (!node.getParent().isBlocked()) {
                            checkedBlocks++;
                            // still marked as indirectly blocked since we proceed in creation order, 
                            // but the parent has already been checked for proper blocking and is not 
                            // really blocked
                            // if this node cannot be blocked directly, unblock this one too
                            Node validBlocker = getValidBlocker(node); 
                            if (validBlocker == null) {
                                invalidBlocks++;
                            }
                            node.setBlocked(validBlocker,validBlocker!=null);
                        }
                        if (!node.isBlocked()&&m_directBlockingChecker.canBeBlocker(node)) {
                            m_currentBlockersCache.addNode(node);
                        }
                    }
                    m_directBlockingChecker.setHasChangedSinceValidation(node, false);
                    m_lastValidatedUnchangedNode=node;
                }
                if (printingOn && node.getNodeID() % 1000 == 0) System.out.print(" " + node.getNodeID());
                node=node.getNextTableauNode();
            }
            if (printingOn) System.out.println("");
            // if set to some node, then computePreblocking will be asked to check from that node onwards in case of invalid blocks 
            m_firstChangedNode=null;
            if (printingOn) System.out.println("Checked " + checkedBlocks + " blocked nodes of which " + invalidBlocks + " were invalid.");
            //if (printingOn&&invalidlyBlockedNodes!=null) System.out.println("Nodes with non-permanently satisfied existentials & invalidly blocked nodes that need expansion: " + invalidlyBlockedNodes.size());
        }
    }
    protected Node getValidBlocker(Node blocked) {
        // we have that the node blocked is (pre-)blocked and we have to validate whether the block is valid 
        // that is we can create a model from the block by unravelling
        Node blocker=m_currentBlockersCache.getBlocker(blocked);
        Set<AtomicConcept> blockedLabel=getLabel(blocked);
        Set<AtomicConcept> blockedParentLabel=getLabel(blocked.getParent());
        Set<AtomicConcept> blockerLabel=getLabel(blocker);
        Set<AtomicConcept> blockerParentLabel=getLabel(blocker.getParent());
        boolean blockerIsSuitable = true;
        // check whether min/max cardinalities of the parent of the blocked node could be violated
        // universals and existential have been converted to min/max restrictions for convenience
        AtomicConcept c;
        for (Iterator<AtomicConcept> bpIt = blockedParentLabel.iterator(); bpIt.hasNext() && blockerIsSuitable; ) {
            c = bpIt.next();
            if (m_unaryValidBlockConditions.containsKey(c)) {
                Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_unaryValidBlockConditions.get(c), blocker, blocked);
                if (violationCauses.size()!=0) { 
                    blockerIsSuitable = false;
                    if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, blockedLabel, blockedParentLabel, blockerLabel, blockerParentLabel, c, violationCauses));
                }
            }
        }
        // check top, which is not explicitly present in the label, but might be the premise of some constraint
        if (blockerIsSuitable && m_unaryValidBlockConditions.containsKey(AtomicConcept.THING)) {
            Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_unaryValidBlockConditions.get(AtomicConcept.THING), blocker, blocked); 
            if (violationCauses.size()!=0) { 
                blockerIsSuitable = false;
                if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, blockedLabel, blockedParentLabel, blockerLabel, blockerParentLabel, AtomicConcept.THING, violationCauses));
            }
        }
        // repeat the same checks for non-unary premises (less efficient matching operations)
        if (blockerIsSuitable) {
            for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
                if (blockedParentLabel.containsAll(premises)) {
                    Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_nAryValidBlockConditions.get(premises), blocker, blocked);
                    if (violationCauses.size()!=0) { 
                        blockerIsSuitable = false;
                        if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, blockedLabel, blockedParentLabel, blockerLabel, blockerParentLabel, premises, violationCauses));
                    }
                }
                if (!blockerIsSuitable) break;
            }
        }
        // check whether min/max cardinalities of the blocker are not violated when copied to the blocked node
        if (blockerIsSuitable && m_hasInverses) {
            for (Iterator<AtomicConcept> blIt = blockerLabel.iterator(); blIt.hasNext() && blockerIsSuitable; ) {
                c = blIt.next();
                if (m_unaryValidBlockConditions.containsKey(c)) {
                    Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_unaryValidBlockConditions.get(c), blocker, blocked); 
                    if (violationCauses.size()!=0) { 
                        blockerIsSuitable = false;
                        if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, blockedLabel, blockedParentLabel, blockerLabel, blockerParentLabel, AtomicConcept.THING, violationCauses));
                    }
                }
            }
        }
        // check top, which is not explicitly present in the label, but might be the premise of some constraint
        if (blockerIsSuitable && m_unaryValidBlockConditions.containsKey(AtomicConcept.THING)) {
            Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_unaryValidBlockConditions.get(AtomicConcept.THING), blocker, blocked);    
            if (violationCauses.size()!=0) { 
                blockerIsSuitable = false;
                if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, blockedLabel, blockedParentLabel, blockerLabel, blockerParentLabel, AtomicConcept.THING, violationCauses));
            }
        }
        // repeat the same checks for non-unary premises (less efficient matching operations)
        if (blockerIsSuitable && m_hasInverses) {
            for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
                if (blockerLabel.containsAll(premises)) {
                    Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_nAryValidBlockConditions.get(premises), blocker, blocked); 
                    if (violationCauses.size()!=0) { 
                        blockerIsSuitable = false;
                        if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, blockedLabel, blockedParentLabel, blockerLabel, blockerParentLabel, AtomicConcept.THING, violationCauses));
                    }
                }
                if (!blockerIsSuitable) break;
            }
        }
        if (blockerIsSuitable) {
            return blocker;
        }
        return null;
    }
    protected void addViolation(BlockingViolation violation) {
        String violatedConstraint=violation.getViolatedConstraint();
        Integer i=m_violationCount.get(violatedConstraint);
        if (i!=null) {
            m_violationCount.put(violatedConstraint, (i+1));
        } else {
            m_violationCount.put(violatedConstraint, new Integer(1));
        }
        for (BlockingViolationType t : violation.m_causes.values()) {
            Integer count=m_causesCount.get(t);
            m_causesCount.put(t, count+1);
        }
    }
    protected Map<Set<Concept>, Map<Concept, BlockingViolationType>> getBlockedParentViolations(Set<Set<Concept>> conclusions, Node blocker, Node blocked) {
        Node blockedParent=blocked.getParent();
        Map<Concept, BlockingViolationType> disjunct2Cause=new HashMap<Concept, BlockingViolationType>();
        Map<Set<Concept>, Map<Concept, BlockingViolationType>> conjunct2violations=new HashMap<Set<Concept>, Map<Concept, BlockingViolationType>>();
        for (Set<Concept> conjunct : conclusions) {
            boolean satisfied = false;
            for (Iterator<Concept> it = conjunct.iterator(); it.hasNext() && !satisfied; ) {
                Concept disjunct = it.next();
                satisfied = true;
                if (disjunct instanceof AtLeastConcept) {
                    // (>= n r.B) must hold at blockedParent, therefore, ar(r, blockedParent, blocked) and B(blocked) in ABox implies B(blocker) in ABox
                    // to avoid table lookup check: B(blocked) and not B(blocker) implies not ar(r, blockedParent, blocked)
                    AtLeastConcept atLeast = (AtLeastConcept) disjunct;
                    Role r = atLeast.getOnRole();
                    LiteralConcept filler = atLeast.getToConcept();
                    //if (isInLabel(filler, blockedLabel) && !isInLabel(filler, blockerLabel) && isInABox(r, blockedParent, blocked)) {
                    if (isInABox(filler, blocked) && !isInABox(filler, blocker) && isInABox(r, blockedParent, blocked)) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATLEASTBLOCKEDPARENT);
                    }
                } else if (disjunct instanceof AtMostConcept) {
                    // (<= n r.B) must hold at blockedParent, therefore, ar(r, blockedParent, blocked) in ABox and B(blocked) not in ABox implies B(blocker) not in ABox
                    // to avoid table lookup, we check: not B(blocked) and B(blocker) implies not ar(r, blockedParent, blocked)
                    AtMostConcept atMost = (AtMostConcept)disjunct;
                    Role r = atMost.getOnRole(); // r
                    LiteralConcept filler = atMost.getToConcept();
                    //if (!isInLabel(filler, blockedLabel) && isInLabel(filler, blockerLabel) && isInABox(r, blockedParent, blocked)) {
                    if (!isInABox(filler, blocked) && isInABox(filler, blocker) && isInABox(r, blockedParent, blocked)) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKEDPARENT);
                    }
                } else if (disjunct instanceof AtomicConcept) {
                    // happens if we have something like A -> (>= n r.B) or C. If (>= n r.B) is not guaranteed 
                    // for the parent of the blocked node, but C is, then we are fine, so only if C does not hold, we have to look further. 
                    //if(!isInLabel((AtomicConcept) disjunct, blockedParentLabel)) {
                    if(!isInABox((AtomicConcept) disjunct, blockedParent)) {
                        // must be an atomic concept or normal form is violated
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATOMICBLOCKEDPARENT);
                    }
                } else {
                    throw new IllegalStateException("Internal error: Concepts in the conclusion of core blocking constraints are supposed to be atomic classes, at least or at most constraints, but this class is an instance of " + disjunct.getClass().getSimpleName());
                }
            }
            if (!satisfied) {
                conjunct2violations.put(conjunct, disjunct2Cause);
                return conjunct2violations;
            }
        }
        return conjunct2violations;
    }
    protected Map<Set<Concept>, Map<Concept, BlockingViolationType>> getBlockerViolations(Set<Set<Concept>> conclusions, Node blocker, Node blocked) {
        Node blockerParent=blocker.getParent();
        Node blockedParent=blocked.getParent();
        Map<Concept, BlockingViolationType> disjunct2Cause=new HashMap<Concept, BlockingViolationType>();
        Map<Set<Concept>, Map<Concept, BlockingViolationType>> conjunct2violations=new HashMap<Set<Concept>, Map<Concept, BlockingViolationType>>();
        for (Set<Concept> conjunct : conclusions) {
            boolean satisfied = false;
            for (Iterator<Concept> it = conjunct.iterator(); it.hasNext() && !satisfied; ) {
                Concept disjunct = it.next();
                satisfied = true;
                if (disjunct instanceof AtLeastConcept) {
                    // (>= n r.B)(blocker) in the ABox, so in the model construction, (>= n r.B) will be copied to blocked, 
                    // so we have to make sure that it will be satisfied at blocked
                    // check B(blockerParent) and ar(r, blocker, blockerParent) in ABox implies B(blockedParent) and ar(r, blocked, blockedParent) in ABox
                    // or blocker has at least n r-successors bs such that B(bs) holds
                    AtLeastConcept atLeast = (AtLeastConcept) disjunct;
                    Role r = atLeast.getOnRole();
                    LiteralConcept filler = atLeast.getToConcept();
                    //if (isInLabel(filler, blockerParentLabel) && isInABox(r, blocker, blockerParent) && (!isInLabel(filler, blockedParentLabel) || !isInABox(r, blocked, blockedParent))) {
                    if (isInABox(filler, blockerParent) && isInABox(r, blocker, blockerParent) && (!isInABox(filler, blockedParent) || !isInABox(r, blocked, blockedParent))) {
                        if (!hasAtLeastNSuccessors(blocker, atLeast.getNumber(), r, filler)) {
                            satisfied = false;
                            disjunct2Cause.put(disjunct, BlockingViolationType.ATLEASTBLOCKER);
                        }
                    }
                } else if (disjunct instanceof AtMostConcept) {
                    // (<= n r.B)(blocker) is in the ABox and in the model construction (<= n r.B) will be copied to blocked,  
                    // so we have to make sure that it will be satisfied at blocked
                    // r(blocked, blockedParent) and B(blockedParent) -> r(blocker, blockerParent) and B(blockerParent)
                    // or blocker has at most n-1 r-successors with B in their label
                    AtMostConcept atMost = (AtMostConcept) disjunct;
                    Role r = atMost.getOnRole();
                    LiteralConcept filler = atMost.getToConcept();
                    //if (isInLabel(filler, blockedParentLabel) && isInABox(r, blocked, blockedParent) && (!isInLabel(filler, blockerParentLabel) || !isInABox(r, blocker, blockerParent))) {
                    if (isInABox(filler, blockedParent) && isInABox(r, blocked, blockedParent) && (!isInABox(filler, blockerParent) || !isInABox(r, blocker, blockerParent))) {
                        if (atMost.getNumber()==0 || hasAtLeastNSuccessors(blocker, atMost.getNumber(), r, filler)) {
                            satisfied = false;
                            disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKER);
                        }
                    }
                } else if (disjunct instanceof AtomicConcept) {
                    // happens if we have something like A -> (>= n r.B) or C. If (>= n r.B) is not guaranteed 
                    // for the blocker, but C is, then we are fine, so only if C does not hold, we have to look further.
                    //if (!isInLabel((AtomicConcept) disjunct, blockerLabel)) {
                    if (!isInABox((AtomicConcept) disjunct, blocker)) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATOMICBLOCKER);
                    }
                } else if (disjunct == null) {
                    // bottom
                    satisfied=false;
                    throw new IllegalStateException("Internal error: A blocking constraint has no consequence! ");
                } else {
                    throw new IllegalStateException("Internal error: Concepts in the conclusion of core blocking constraints are supposed to be atomic classes, at least or at most constraints, but this class is an instance of " + disjunct.getClass().getSimpleName());
                }
            }
            if (!satisfied) {
                conjunct2violations.put(conjunct, disjunct2Cause);
                return conjunct2violations;
            }
            disjunct2Cause.clear();
        }
        return conjunct2violations;
    }
    protected boolean hasAtLeastNSuccessors(Node blocker, int n, Role r, LiteralConcept filler) {
        if (n==0) return true;
        int suitableSuccessors = 0;
        if (r instanceof AtomicRole) {
            m_ternaryTableSearchZeroOneBound.getBindingsBuffer()[0]=r;
            m_ternaryTableSearchZeroOneBound.getBindingsBuffer()[1]=blocker;
            m_ternaryTableSearchZeroOneBound.open();
            Object[] tupleBuffer=m_ternaryTableSearchZeroOneBound.getTupleBuffer();
            while (!m_ternaryTableSearchZeroOneBound.afterLast() && suitableSuccessors < n) {
                Node possibleSuccessor=(Node)tupleBuffer[2];
                if (!possibleSuccessor.isAncestorOf(blocker)) {
                    if (filler.isAlwaysTrue()) {
                        suitableSuccessors++;
                    } else if (!filler.isAlwaysFalse()) {
                        if (filler instanceof AtomicConcept) {
                            m_binaryTableAllBound.getBindingsBuffer()[0]=filler;
                            m_binaryTableAllBound.getBindingsBuffer()[1]=possibleSuccessor;
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
                Node possibleSuccessor=(Node)tupleBuffer[1];
                if (!possibleSuccessor.isAncestorOf(blocker)) {
                    if (filler.isAlwaysTrue()) {
                        suitableSuccessors++;
                    } else if (!filler.isAlwaysFalse()) {
                        if (filler instanceof AtomicConcept) {
                            m_binaryTableAllBound.getBindingsBuffer()[0]=filler;
                            m_binaryTableAllBound.getBindingsBuffer()[1]=possibleSuccessor;
                            m_binaryTableAllBound.open();
                            m_binaryTableAllBound.getTupleBuffer();
                            if (!m_binaryTableAllBound.afterLast()) {
                                suitableSuccessors++;
                            }
                        } else {
                            // negated atomic concept
                            m_binaryTableAllBound.getBindingsBuffer()[0]=((AtomicNegationConcept)filler).getNegatedAtomicConcept();
                            m_binaryTableAllBound.getBindingsBuffer()[1]=possibleSuccessor;
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
    protected boolean isInABox(LiteralConcept c, Node node) {
        if (c.isAlwaysTrue()) return true;
        if (c.isAlwaysFalse()) return false;
        if (c instanceof AtomicConcept) {
            m_binaryTableAllBound.getBindingsBuffer()[0]=c;
            m_binaryTableAllBound.getBindingsBuffer()[1]=node;
            m_binaryTableAllBound.open();
            m_binaryTableAllBound.getTupleBuffer(); // maybe this is unneccessary 
            return !m_binaryTableAllBound.afterLast();
        } else {
            m_binaryTableAllBound.getBindingsBuffer()[0]=((AtomicNegationConcept)c).getNegatedAtomicConcept();
            m_binaryTableAllBound.getBindingsBuffer()[1]=node;
            m_binaryTableAllBound.open();
            m_binaryTableAllBound.getTupleBuffer(); // maybe this is unneccessary 
            return m_binaryTableAllBound.afterLast();
        }
    }
    protected Set<AtomicConcept> getLabel(Node node) {
        Set<AtomicConcept> atomicConcetLabel=new HashSet<AtomicConcept>();
        m_binaryTableOneBound.getBindingsBuffer()[1]=node;
        m_binaryTableOneBound.open();
        Object[] tupleBuffer=m_binaryTableOneBound.getTupleBuffer();
        while (!m_binaryTableOneBound.afterLast()) {
            Object concept=tupleBuffer[0];
            if (concept instanceof AtomicConcept)
                atomicConcetLabel.add((AtomicConcept)concept);
            m_binaryTableOneBound.next();
        } 
        return atomicConcetLabel;
    }
    protected Set<Role> getEdgeLabel(Node from, Node to) {
        Set<Role> edgeLabel=new HashSet<Role>();
        m_ternaryTableSearchOneTwoBound.getBindingsBuffer()[1]=from;
        m_ternaryTableSearchOneTwoBound.getBindingsBuffer()[2]=to;
        m_ternaryTableSearchOneTwoBound.open();
        Object[] tupleBuffer=m_ternaryTableSearchOneTwoBound.getTupleBuffer();
        while (!m_ternaryTableSearchOneTwoBound.afterLast()) {
            Object role=tupleBuffer[0];
            if (role instanceof AtomicRole)
                edgeLabel.add((AtomicRole)role);
            m_ternaryTableSearchOneTwoBound.next();
        }
        // inverses
        m_ternaryTableSearchOneTwoBound.getBindingsBuffer()[1]=to;
        m_ternaryTableSearchOneTwoBound.getBindingsBuffer()[2]=from;
        m_ternaryTableSearchOneTwoBound.open();
        tupleBuffer=m_ternaryTableSearchOneTwoBound.getTupleBuffer();
        while (!m_ternaryTableSearchOneTwoBound.afterLast()) {
            Object role=tupleBuffer[0];
            if (role instanceof AtomicRole)
                edgeLabel.add(InverseRole.create((AtomicRole)role));
            m_ternaryTableSearchOneTwoBound.next();
        }
        return edgeLabel;
    }
    public boolean isPermanentAssertion(Concept concept,Node node) {
        m_auxiliaryTuple[0]=concept;
        m_auxiliaryTuple[1]=node;
        return m_extensionManager.isCore(m_auxiliaryTuple);
    }
    // Assertions can be added directly into the core, but we also have the possibility of setting the core flag later?
    // In that case, assertionCoreSet (below) will be called?
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        m_directBlockingChecker.assertionAdded(concept,node,isCore);
        updateSmallestNodeToValidate(node);
        if (concept instanceof AtLeastConcept || isCore) updateNodeChange(node);
    }
    public void assertionCoreSet(Concept concept,Node node) {
        m_directBlockingChecker.assertionAdded(concept,node,true);
        updateNodeChange(node);
    }
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        m_directBlockingChecker.assertionRemoved(concept,node,isCore);
        updateSmallestNodeToValidate(node);
        if (isCore) updateNodeChange(node);
    }
    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        updateSmallestNodeToValidate(nodeFrom);
        updateSmallestNodeToValidate(nodeTo);
        m_directBlockingChecker.assertionAdded(atomicRole, nodeFrom, nodeTo,isCore);
    }
    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        m_directBlockingChecker.assertionAdded(atomicRole, nodeFrom, nodeTo,true);
    }
    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        updateSmallestNodeToValidate(nodeFrom);
        updateSmallestNodeToValidate(nodeTo);
        m_directBlockingChecker.assertionRemoved(atomicRole, nodeFrom, nodeTo,isCore);
    }
    public void nodeStatusChanged(Node node) {
        updateNodeChange(node);
    }
    protected final void updateNodeChange(Node node) {
        if (node!=null && (m_firstChangedNode==null || node.getNodeID()<m_firstChangedNode.getNodeID()))
            m_firstChangedNode=node;
    }
    protected final void updateSmallestNodeToValidate(Node node) {
        if (m_lastValidatedUnchangedNode==null || node.getNodeID()<m_lastValidatedUnchangedNode.getNodeID())
            m_lastValidatedUnchangedNode=node;
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
//        Node node=m_tableau.getFirstTableauNode();
//        while (node!=null) {
//            if (node.isActive() && node.isBlocked()) 
//                System.out.println("Node " + node.getNodeID() + " is " + (node.isDirectlyBlocked()?"directly":"indirectly") + " blocked by node " + node.getBlocker().getNodeID());
//            node=node.getNextTableauNode();
//        }
//        m_lastValidatedUnchangedNode=null;
        m_lastValidatedUnchangedNode=null;
        if (debuggingMode) validateBlocks();
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
            }
            return;
        }
        if (dlClause.getHeadLength() > 2) {
            // in case of a disjunction, there is nothing to compute, the choice must go into the core
            // I assume that disjunctions are always only for the centre variable X and I assume that X is the first
            // variable in the array ???
            coreVariables[0] = true;
        } else {
            workers.add(new ComputeCoreVariables(dlClause,valuesBuffer,coreVariables));
        }
    }
    public void doSanityCheck() {
        m_currentBlockersCache.doSanityCheck();
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
class CoreBlockersCacheOld {
    protected Tableau m_tableau;
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected CacheEntry[] m_buckets;
    protected int m_numberOfElements;
    protected int m_threshold;
    protected CacheEntry m_emptyEntries;

    public CoreBlockersCacheOld(DirectBlockingChecker directBlockingChecker) {
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
    public boolean removeNode(Node node) {
        // Check addNode() for an explanation of why we associate the entry with the node.
        CoreBlockersCacheOld.CacheEntry removeEntry=(CoreBlockersCacheOld.CacheEntry)node.getBlockingCargo();
        if (removeEntry!=null) {
            int bucketIndex=getIndexFor(removeEntry.m_hashCode,m_buckets.length);
            CacheEntry lastEntry=null;
            CacheEntry entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (entry==removeEntry) {
                    if (node == entry.m_nodes.get(0)) {
                        // the whole entry needs to be removed
                        for (Node n : entry.m_nodes) {
                            n.setBlockingCargo(null);
                        }
                        if (lastEntry==null)
                            m_buckets[bucketIndex]=entry.m_nextEntry;
                        else
                            lastEntry.m_nextEntry=entry.m_nextEntry;
                        entry.m_nextEntry=m_emptyEntries;
                        entry.m_nodes=new ArrayList<Node>();
                        entry.m_hashCode=0;
                        m_emptyEntries=entry;
                        m_numberOfElements--;
                    } else if (entry.m_nodes.contains(node)) {
                        for (int i=entry.m_nodes.size()-1; i>=entry.m_nodes.indexOf(node); i--) {
                            entry.m_nodes.get(i).setBlockingCargo(null);
                        }
                        entry.m_nodes.subList(0, entry.m_nodes.indexOf(node)).clear();
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
            if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_nodes.get(0),node)) {
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
    public Node getBlocker(Node node) {
        List<Node> possibleBlockers=getPossibleBlockers(node);
        return possibleBlockers.isEmpty()?null:possibleBlockers.get(0);
    }
    public List<Node> getPossibleBlockers(Node node) {
        if (m_directBlockingChecker.canBeBlocked(node)) {
            int hashCode=m_directBlockingChecker.blockingHashCode(node);
            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
            CacheEntry entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_nodes.get(0),node)) {
                    // We block only with nodes that have a smaller ID than the node that is to be blocked
                    // so return the head set
                    if (entry.m_nodes.contains(node)) { 
                        throw new IllegalStateException("Internal error: We try to block a node that is in the blockers cache. ");
                    } else {
                        return entry.m_nodes;
                    }
                }
                entry=entry.m_nextEntry;
            }
        }
        return new ArrayList<Node>();
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

    public void doSanityCheck() {
        System.out.println("Doing a sanity check on the cache...");
        for (int i=0;i<m_buckets.length;i++) {
            CacheEntry entry=m_buckets[i];
            while (entry!=null) {
                entry.doSanityCheck(m_directBlockingChecker);
                CacheEntry nextEntry=entry.m_nextEntry;
                entry=nextEntry;
            }
        }
    }
    public static class CacheEntry implements Serializable {
        private static final long serialVersionUID=-7047487963170250200L;

        protected List<Node> m_nodes;
        protected int m_hashCode;
        protected CacheEntry m_nextEntry;

        public void initialize(Node node,int hashCode,CacheEntry nextEntry) {
            m_nodes = new ArrayList<Node>();
            add(node);
            m_hashCode=hashCode;
            m_nextEntry=nextEntry;
        }
        public boolean add(Node node) {
            for (Node n : m_nodes) {
                if (n.getNodeID() >= node.getNodeID()) 
                    throw new IllegalStateException("Internal error: a node is added to a cache entry that is smaller than other nodes in this cache entry. ");
            }
            return m_nodes.add(node);
        }
        public String toString() {
            String nodes = "HashCode: " + m_hashCode + " Nodes: ";
            for (Node n : m_nodes) {
                nodes += n.getNodeID() + " ";
            }
            return nodes;
        }
        public void doSanityCheck(DirectBlockingChecker directBlockingChecker) {
            for (Node n : m_nodes) {
                if (m_hashCode!=directBlockingChecker.blockingHashCode(n)) 
                if (!n.isActive()) throw new IllegalStateException("Internal error: Node "+n+" is not active but in the blocking cache. ");
                if (n.isBlocked()) throw new IllegalStateException("Internal error: Node "+n+" is blocked but in the blocking cache. ");
                if (n.isMerged()) throw new IllegalStateException("Internal error: Node "+n+" is merged but in the blocking cache. ");
                if (n.isPruned()) throw new IllegalStateException("Internal error: Node "+n+" is pruned but in the blocking cache. ");
            }
        }
    }
}
//class NodeIDComparator implements Serializable, Comparator<Node> {
//    private static final long serialVersionUID = 2112323818144484750L;
//    public static final Comparator<Node> INSTANCE = new NodeIDComparator();
//
//    public int compare(Node n1, Node n2) {
//        return n2.getNodeID() - n1.getNodeID();
//    }
//}