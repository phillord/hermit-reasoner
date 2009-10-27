// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.blocking.core.AtMostConcept;
import org.semanticweb.HermiT.blocking.core.AtMostConjunctionConcept;
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
import org.semanticweb.HermiT.model.DLClause.ClauseType;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;

public class AnywhereValidatedBlocking implements BlockingStrategy {

    public static enum BlockingViolationType {
        ATLEASTBLOCKEDPARENT, 
        ATMOSTBLOCKEDPARENT,
        ATOMICBLOCKEDPARENT,
        ATLEASTBLOCKER, 
        ATMOSTBLOCKER,
        ATOMICBLOCKER
    }
    
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final ValidatedBlockersCache m_currentBlockersCache;
    protected final BlockingSignatureCache m_blockingSignatureCache;
    protected Tableau m_tableau;
    protected Node m_firstChangedNode;
    protected Node m_lastValidatedUnchangedNode=null;
    
    protected final Map<AtomicConcept, Set<Set<Concept>>> m_unaryValidBlockConditions; 
    protected final Map<Set<AtomicConcept>, Set<Set<Concept>>> m_nAryValidBlockConditions;
    protected boolean m_useSingletonCore;
    
    protected ExtensionManager m_extensionManager;
    protected ExtensionTable.Retrieval m_ternaryTableSearchZeroOneBound;
    protected ExtensionTable.Retrieval m_ternaryTableSearchZeroTwoBound;
    protected final Object[] m_auxiliaryTuple;
    
    protected final boolean m_hasInverses;
    protected boolean m_immediatelyValidateBlocks = false;
    
    // statistics: 
    protected int numDirectlyBlocked=0;
    protected int numIndirectlyBlocked=0;
    protected final boolean debuggingMode=false;
    protected Map<String, Integer> m_violationCountBlocker=new HashMap<String, Integer>();
    protected Map<String, Integer> m_violationCountBlockedParent=new HashMap<String, Integer>();
    protected Map<BlockingViolationType, Integer> m_causesCount=new HashMap<BlockingViolationType, Integer>();
    
    protected int one=0;
    protected int two=0;
    protected int three=0;
    protected int four=0;
    protected int five=0;
    protected int six=0;
    
    public AnywhereValidatedBlocking(DirectBlockingChecker directBlockingChecker,BlockingSignatureCache blockingSignatureCache,Map<AtomicConcept, Set<Set<Concept>>> unaryValidBlockConditions, Map<Set<AtomicConcept>, Set<Set<Concept>>> nAryValidBlockConditions, boolean hasInverses,boolean useSingletonCore) {
        m_directBlockingChecker=directBlockingChecker;
        m_currentBlockersCache=new ValidatedBlockersCache(m_directBlockingChecker);
        m_blockingSignatureCache=blockingSignatureCache;
        m_unaryValidBlockConditions = unaryValidBlockConditions;
        m_nAryValidBlockConditions = nAryValidBlockConditions;
        m_useSingletonCore=useSingletonCore;
        m_hasInverses = hasInverses;
        m_auxiliaryTuple=new Object[2];
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_directBlockingChecker.initialize(tableau);
        m_extensionManager=m_tableau.getExtensionManager();
        m_ternaryTableSearchZeroOneBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
        m_ternaryTableSearchZeroTwoBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.TOTAL);
        if (debuggingMode) {
            m_causesCount.put(BlockingViolationType.ATLEASTBLOCKEDPARENT, 0);
            m_causesCount.put(BlockingViolationType.ATLEASTBLOCKER, 0);
            m_causesCount.put(BlockingViolationType.ATMOSTBLOCKEDPARENT, 0);
            m_causesCount.put(BlockingViolationType.ATMOSTBLOCKER, 0);
            m_causesCount.put(BlockingViolationType.ATOMICBLOCKEDPARENT, 0);
            m_causesCount.put(BlockingViolationType.ATOMICBLOCKER, 0);
        }
    }
    public void clear() {
        m_currentBlockersCache.clear();
        m_firstChangedNode=null;
        m_directBlockingChecker.clear();
        m_immediatelyValidateBlocks=false;
        m_lastValidatedUnchangedNode=null;
    }
    public void computeBlocking(boolean finalChance) {
        if (finalChance) {
            validateBlocks();
        } else {
            computePreBlocking();
        }
    }
    public void computePreBlocking() {
        if (m_firstChangedNode!=null) {
            Node node=m_firstChangedNode;
            while (node!=null) {
                m_currentBlockersCache.removeNode(node);
                node=node.getNextTableauNode();
            }
            node=m_firstChangedNode;
            boolean checkBlockingSignatureCache=(m_blockingSignatureCache!=null && !m_blockingSignatureCache.isEmpty());
            while (node!=null) {
                if (node.isActive() && (m_directBlockingChecker.canBeBlocked(node) || m_directBlockingChecker.canBeBlocker(node))) {
                    if (m_directBlockingChecker.hasBlockingInfoChanged(node) || !node.isDirectlyBlocked() || node.getBlocker().getNodeID()>=m_firstChangedNode.getNodeID()) {
                        Node parent=node.getParent();
                        if (parent==null)
                            node.setBlocked(null,false);
                        else if (parent.isBlocked())
                            node.setBlocked(parent,false);
                        else if (checkBlockingSignatureCache && m_blockingSignatureCache.containsSignature(node)) {
                            node.setBlocked(Node.SIGNATURE_CACHE_BLOCKER,true);
                        } else {
                            Node blocker=null;
                            Node previousBlocker=node.getBlocker();
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
                                    // if its previous blocker is still in the cache then it has also not been modified because 
                                    // it would have a different hash code after the modification
                                    // if the previous blocker is no longer there, then it does not make sense to try any node with smaller 
                                    // node ID again unless it has been modified since the last validation (-> newly added to the cache), 
                                    if (previousBlocker!=null&&possibleBlockers.contains(previousBlocker)&&!m_directBlockingChecker.hasChangedSinceValidation(previousBlocker)&&!m_directBlockingChecker.hasChangedSinceValidation(previousBlocker.getParent())) {
                                        // reassign the valid and unchanged blocker
                                        blocker=previousBlocker;
                                    } else {
                                        for (Node n : possibleBlockers) {
                                            // find he smallest one that has changed since we last validated
                                            if (m_directBlockingChecker.hasChangedSinceValidation(n) || m_directBlockingChecker.hasChangedSinceValidation(n.getParent())) {
                                                blocker=n;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            node.setBlocked(blocker,blocker!=null);
                        }
                        if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                            m_currentBlockersCache.addNode(node);
                    }
                    m_directBlockingChecker.clearBlockingInfoChanged(node);
                }
                node=node.getNextTableauNode();
            }
            m_firstChangedNode=null;
        }
    }
    public void validateBlocks() {
        // after first complete validation, we can switch to only checking block validity immediately
        //m_immediatelyValidateBlocks = true;
        if (!m_unaryValidBlockConditions.isEmpty() || !m_nAryValidBlockConditions.isEmpty()) {
            // statistics:
            int checkedBlocks = 0;
            int invalidBlocks = 0;
            Node firstInvalidlyBlockedNode=null;
            
            TableauMonitor monitor=m_tableau.getTableauMonitor();
            if (monitor!=null) monitor.blockingValidationStarted();
            
            Node node = m_lastValidatedUnchangedNode==null?m_tableau.getFirstTableauNode():m_lastValidatedUnchangedNode;
            Node firstValidatedNode=node;
            while (node!=null) {
                m_currentBlockersCache.removeNode(node);
                node=node.getNextTableauNode();
            }
            node=firstValidatedNode;
            if (debuggingMode) System.out.print("Model size: "+(m_tableau.getNumberOfNodesInTableau()-m_tableau.getNumberOfMergedOrPrunedNodes())+" Current ID:");
            while (node!=null) {
                if (node.isActive()) {
                    if (node.isBlocked()) {
                        checkedBlocks++;
                        // check whether the block is a correct one
                        Node validBlocker;
                        if (node.isDirectlyBlocked()) {
                            if (m_directBlockingChecker.hasChangedSinceValidation(node) || m_directBlockingChecker.hasChangedSinceValidation(node.getParent()) || m_directBlockingChecker.hasChangedSinceValidation(node.getBlocker()) || m_directBlockingChecker.hasChangedSinceValidation(node.getBlocker().getParent())) {
                                validBlocker=getValidBlocker(node); 
                                if (validBlocker == null) {
                                    invalidBlocks++;
                                    if (firstInvalidlyBlockedNode==null) firstInvalidlyBlockedNode=node;
                                }
                                node.setBlocked(validBlocker,validBlocker!=null);
                            } 
                        } else if (!node.getParent().isBlocked()) {
                            // still marked as indirectly blocked since we proceed in creation order, 
                            // but the parent has already been checked for proper blocking and is not 
                            // really blocked
                            // if this node cannot be blocked directly, unblock this one too
                            validBlocker = getValidBlocker(node); 
                            if (validBlocker == null) {
                                invalidBlocks++;
                                if (firstInvalidlyBlockedNode==null) firstInvalidlyBlockedNode=node;
                            }
                            node.setBlocked(validBlocker,validBlocker!=null);
                        }
                    }
                    m_lastValidatedUnchangedNode=node;
                    if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                        m_currentBlockersCache.addNode(node);
                    if (debuggingMode && node.getNodeID() % 1000 == 0) System.out.print(" " + node.getNodeID());
                }
                node=node.getNextTableauNode();
            } 
            if (debuggingMode) System.out.println("");
            node=firstValidatedNode;
            while (node!=null) {
                if (node.isActive())
                    m_directBlockingChecker.setHasChangedSinceValidation(node, false);
                node=node.getNextTableauNode();
            }
            // if set to some node, then computePreblocking will be asked to check from that node onwards in case of invalid blocks 
            m_firstChangedNode=firstInvalidlyBlockedNode;
            if (monitor!=null) monitor.blockingValidationFinished();
            //m_firstChangedNode=firstValidatedNode;
            //m_firstChangedNode=null;
            if (debuggingMode) System.out.println("Checked " + checkedBlocks + " blocked nodes of which " + invalidBlocks + " were invalid.");
        }
    }
    protected Node getValidBlocker(Node blocked) {
        // we have that the node blocked is (pre-)blocked and we have to validate whether the block is valid 
        // that is we can create a model from the block by unravelling
        List<Node> possibleBlockers = m_currentBlockersCache.getPossibleBlockers(blocked);
        if (possibleBlockers.isEmpty()) return null;
        int i=0;
        if (blocked.getBlocker()!=null && possibleBlockers.contains(blocked.getBlocker())) {
            // we always assign the smallest node that has been modified since the last validation
            // re-testing smaller (unmodified) ones makes no sense 
            i=possibleBlockers.indexOf(blocked.getBlocker());
        }
        AtomicConcept c;
        Set<AtomicConcept> blockedParentLabel=m_directBlockingChecker.getFullAtomicConceptsLabel(blocked.getParent());
        for (; i<possibleBlockers.size(); i++) {
            Node blocker=possibleBlockers.get(i);
            boolean blockerIsSuitable = true;
            // check whether min/max cardinalities of the blocker are not violated when copied to the blocked node
            Set<AtomicConcept> blockerLabel=m_directBlockingChecker.getFullAtomicConceptsLabel(blocker);
            if (blockerIsSuitable && m_hasInverses) {
                for (Iterator<AtomicConcept> blIt = blockerLabel.iterator(); blIt.hasNext() && blockerIsSuitable; ) {
                    c = blIt.next();
                    if (m_unaryValidBlockConditions.containsKey(c)) {
                        Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_unaryValidBlockConditions.get(c), blocker, blocked); 
                        if (violationCauses.size()!=0) { 
                            blockerIsSuitable = false;
                            if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, c, violationCauses),m_violationCountBlocker);
                            if (debuggingMode) one++;
                        }
                    }
                    if (!blockerIsSuitable) break;
                }
            }
            // check whether min/max cardinalities of the parent of the blocked node could be violated
            // universals and existential have been converted to min/max restrictions for convenience
            if (blockerIsSuitable) {
                for (Iterator<AtomicConcept> bpIt = blockedParentLabel.iterator(); bpIt.hasNext() && blockerIsSuitable; ) {
                    c = bpIt.next();
                    if (m_unaryValidBlockConditions.containsKey(c)) {
                        Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_unaryValidBlockConditions.get(c), blocker, blocked);
                        if (violationCauses.size()!=0) { 
                            blockerIsSuitable = false;
                            if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, c, violationCauses),m_violationCountBlockedParent);
                            if (debuggingMode) two++;
                        }
                    }
                    if (!blockerIsSuitable) break;
                }
            }
            // check top, which is not explicitly present in the label, but might be the premise of some constraint
            if (blockerIsSuitable && m_unaryValidBlockConditions.containsKey(AtomicConcept.THING)) {
                Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_unaryValidBlockConditions.get(AtomicConcept.THING), blocker, blocked);    
                if (violationCauses.size()!=0) { 
                    blockerIsSuitable = false;
                    if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, AtomicConcept.THING, violationCauses),m_violationCountBlocker);
                    if (debuggingMode) three++;
                }
            }
            // check top, which is not explicitly present in the label, but might be the premise of some constraint
            if (blockerIsSuitable && m_unaryValidBlockConditions.containsKey(AtomicConcept.THING)) {
                Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_unaryValidBlockConditions.get(AtomicConcept.THING), blocker, blocked); 
                if (violationCauses.size()!=0) { 
                    blockerIsSuitable = false;
                    if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, AtomicConcept.THING, violationCauses),m_violationCountBlockedParent);
                    if (debuggingMode) four++;
                }
            }
            // repeat the same checks for non-unary premises (less efficient matching operations)
            if (blockerIsSuitable && m_hasInverses) {
                for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
                    if (blockerLabel.containsAll(premises)) {
                        Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_nAryValidBlockConditions.get(premises), blocker, blocked); 
                        if (violationCauses.size()!=0) { 
                            blockerIsSuitable = false;
                            if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, premises, violationCauses),m_violationCountBlocker);
                            if (debuggingMode) five++;
                        }
                    }
                    if (!blockerIsSuitable) break;
                }
            }
            // repeat the same checks for non-unary premises (less efficient matching operations)
            if (blockerIsSuitable) {
                for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
                    if (blockedParentLabel.containsAll(premises)) {
                        Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_nAryValidBlockConditions.get(premises), blocker, blocked);
                        if (violationCauses.size()!=0) { 
                            blockerIsSuitable = false;
                            if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, premises, violationCauses),m_violationCountBlockedParent);
                            if (debuggingMode) six++;
                        }
                    }
                    if (!blockerIsSuitable) break;
                }
            }
            if (blockerIsSuitable) {
                return blocker;
            }
        }
        return null;
    }
    protected Map<Set<Concept>, Map<Concept, BlockingViolationType>> getBlockedParentViolations(Set<Set<Concept>> conclusions, Node blocker, Node blocked) {
        Map<Concept, BlockingViolationType> disjunct2Cause=new HashMap<Concept, BlockingViolationType>();
        Map<Set<Concept>, Map<Concept, BlockingViolationType>> conjunct2violations=new HashMap<Set<Concept>, Map<Concept, BlockingViolationType>>();
        for (Set<Concept> conjunct : conclusions) {
            boolean satisfied = false;
            disjunct2Cause.clear();
            for (Iterator<Concept> it = conjunct.iterator(); it.hasNext() && !satisfied; ) {
                Concept disjunct = it.next();
                satisfied = true;
                if (disjunct instanceof AtMostConcept) {
                    // (<= n r.B) must hold at blockedParent, therefore, ar(r, blockedParent, blocked) in ABox and B(blocked) not in ABox implies B(blocker) not in ABox
                    // to avoid table lookup, we check: not B(blocked) and B(blocker) implies not ar(r, blockedParent, blocked)
                    AtMostConcept atMost = (AtMostConcept)disjunct;
                    Role r = atMost.getOnRole(); // r
                    LiteralConcept filler = atMost.getToConcept();
                    //if (!isInLabel(filler, blockedLabel) && isInLabel(filler, blockerLabel) && isInABox(r, blockedParent, blocked)) {
                    if (!isInLabel(filler, blocked) && isInLabel(filler, blocker) && isInLabelFromParentToNode(r, blocked)) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKEDPARENT);
                    }
                } else if (disjunct instanceof AtMostConjunctionConcept) {
                    // forall r.{o_1, ... o_n}
                    // (<= 0 r.not o_1 and ... and not o_n)(blockedParent), 
                    // so we have to make sure that it will be satisfied if the blocked node is replaced with the blocker
                    // r(blockedParent, blocked) -> o_1(blocker) or ... or o_n(blocker)) must be true
                    AtMostConjunctionConcept atMost = (AtMostConjunctionConcept) disjunct;
                    Role r = atMost.getOnRole();
                    Set<AtomicNegationConcept> disjunctionOfNegatedNominalConcepts=atMost.getToConcept();
                    if (isInLabelFromParentToNode(r, blocked)) {
                        boolean sat=false;
                        for (AtomicNegationConcept negatedNominal : disjunctionOfNegatedNominalConcepts) {
                            if (isInLabel(negatedNominal.getNegatedAtomicConcept(), blocked.getParent())) {
                                sat=true;
                                break;
                            }
                        }
                        if (!sat) { 
                            satisfied = false;
                            disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKER);
                        }
                    }
                } else if (disjunct instanceof AtomicConcept) {
                    // happens if we have something like A -> (>= n r.B) or C. If (>= n r.B) is not guaranteed 
                    // for the parent of the blocked node, but C is, then we are fine, so only if C does not hold, we have to look further. 
                    //if(!isInLabel((AtomicConcept) disjunct, blockedParentLabel)) {
                    if(!isInLabel((AtomicConcept)disjunct, blocked.getParent())) {
                        // must be an atomic concept or normal form is violated
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATOMICBLOCKEDPARENT);
                    }
                } else if (disjunct instanceof AtLeastConcept) {
                    // (>= n r.B) must hold at blockedParent, therefore, ar(r, blockedParent, blocked) and B(blocked) in ABox implies B(blocker) in ABox
                    // to avoid table lookup check: B(blocked) and not B(blocker) implies not ar(r, blockedParent, blocked)
                    AtLeastConcept atLeast = (AtLeastConcept) disjunct;
                    Role r = atLeast.getOnRole();
                    LiteralConcept filler = atLeast.getToConcept();
                    //if (isInLabel(filler, blockedLabel) && !isInLabel(filler, blockerLabel) && isInABox(r, blockedParent, blocked)) {
                    if (isInLabel(filler, blocked) && !isInLabel(filler, blocker) && isInLabelFromParentToNode(r, blocked)) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATLEASTBLOCKEDPARENT);
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
        Map<Concept, BlockingViolationType> disjunct2Cause=new HashMap<Concept, BlockingViolationType>();
        Map<Set<Concept>, Map<Concept, BlockingViolationType>> conjunct2violations=new HashMap<Set<Concept>, Map<Concept, BlockingViolationType>>();
        for (Set<Concept> conjunct : conclusions) {
            boolean satisfied = false;
            disjunct2Cause.clear();
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
                    if (isInLabel(filler, blocker.getParent()) && isInLabelFromNodeToParent(r, blocker) && (!isInLabel(filler, blocked.getParent()) || !isInLabelFromNodeToParent(r, blocked))) {
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
                    if (isInLabel(filler, blocked.getParent()) && isInLabelFromNodeToParent(r, blocked) && (!isInLabel(filler, blocker.getParent()) || !isInLabelFromNodeToParent(r, blocker))) {
                        if (atMost.getNumber()==0 || hasAtLeastNSuccessors(blocker, atMost.getNumber(), r, filler)) {
                            satisfied = false;
                            disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKER);
                        }
                    }
                } else if (disjunct instanceof AtMostConjunctionConcept) {
                    // forall r.{o_1 ... o_n}
                    // (<= 0 r.not o_1 and ... and not o_n)(blocker) is in the ABox and in the model construction 
                    // (<= 0 r.not o_1 and ... and not o_n) will be copied to blocked node,  
                    // so we have to make sure that it will be satisfied at blocked
                    // r(blocked, blockedParent) -> o_1(blockedParent) or ... or o_n(blockedParent)
                    AtMostConjunctionConcept atMost = (AtMostConjunctionConcept) disjunct;
                    Role r = atMost.getOnRole();
                    Set<AtomicNegationConcept> disjunctionOfNegatedNominalConcepts=atMost.getToConcept();
                    if (isInLabelFromNodeToParent(r, blocked)) {
                        boolean sat=false;
                        for (AtomicNegationConcept negatedNominal : disjunctionOfNegatedNominalConcepts) {
                            if (isInLabel(negatedNominal.getNegatedAtomicConcept(), blocked.getParent())) {
                                sat=true;
                                break;
                            }
                        }
                        if (!sat) {
                            satisfied = false;
                            disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKER);
                        }
                    }
                } else if (disjunct instanceof AtomicConcept) {
                    // happens if we have something like A -> (>= n r.B) or C. If (>= n r.B) is not guaranteed 
                    // for the blocker, but C is, then we are fine, so only if C does not hold, we have to look further.
                    //if (!isInLabel((AtomicConcept) disjunct, blockerLabel)) {
                    if (!isInLabel((AtomicConcept) disjunct, blocker)) {
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
                    if (filler.isAlwaysTrue() 
                            || (!filler.isAlwaysFalse() 
                                    && ((filler instanceof AtomicConcept && m_extensionManager.containsConceptAssertion(filler, possibleSuccessor)) 
                                    || (filler instanceof AtomicNegationConcept && !m_extensionManager.containsConceptAssertion(((AtomicNegationConcept)filler).getNegatedAtomicConcept(), possibleSuccessor))))) {
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
                Node possibleSuccessor=(Node)tupleBuffer[1];
                if (!possibleSuccessor.isAncestorOf(blocker)) {
                    if (filler.isAlwaysTrue() 
                            || (!filler.isAlwaysFalse() 
                                    && (filler instanceof AtomicConcept && m_extensionManager.containsConceptAssertion(filler, possibleSuccessor)) 
                                    || (filler instanceof AtomicNegationConcept && !m_extensionManager.containsConceptAssertion(((AtomicNegationConcept)filler).getNegatedAtomicConcept(), possibleSuccessor)))) {
                        suitableSuccessors++;
                    } 
                }
                m_ternaryTableSearchZeroTwoBound.next();
            }
            return (suitableSuccessors >= n);
        }
    }
    protected boolean isInLabelFromParentToNode(Role r, Node node) {
        if (r==AtomicRole.TOP_OBJECT_ROLE || (r instanceof InverseRole && ((InverseRole)r).getInverseOf()==AtomicRole.TOP_OBJECT_ROLE)) return true;
        if (r instanceof InverseRole) {
            Set<AtomicRole> fromNodeToParentLabel=m_directBlockingChecker.getFullToParentLabel(node);
            return fromNodeToParentLabel.contains(((InverseRole)r).getInverseOf());
        } else {
            Set<AtomicRole> fromParentToNodeLabel=m_directBlockingChecker.getFullFromParentLabel(node);
            return fromParentToNodeLabel.contains((AtomicRole)r);
        }
    }
    protected boolean isInLabelFromNodeToParent(Role r, Node node) {
        if (r==AtomicRole.TOP_OBJECT_ROLE || (r instanceof InverseRole && ((InverseRole)r).getInverseOf()==AtomicRole.TOP_OBJECT_ROLE)) return true;
        if (r instanceof InverseRole) {
            Set<AtomicRole> fromParentToNodeLabel=m_directBlockingChecker.getFullFromParentLabel(node);
            return fromParentToNodeLabel.contains(((InverseRole)r).getInverseOf());
        } else {
            Set<AtomicRole> fromNodeToParentLabel=m_directBlockingChecker.getFullToParentLabel(node);
            return fromNodeToParentLabel.contains((AtomicRole)r);
        }
    }
    protected boolean isInLabel(LiteralConcept c, Node node) {
        if (c.isAlwaysTrue()) return true;
        if (c.isAlwaysFalse()) return false;
        Set<AtomicConcept> label=m_directBlockingChecker.getFullAtomicConceptsLabel(node);
        if (c instanceof AtomicConcept) {
            return label.contains((AtomicConcept)c);
        } else {
            return !label.contains(((AtomicNegationConcept)c).getNegatedAtomicConcept());
        }
    }
    public boolean isPermanentAssertion(Concept concept,Node node) {
        return true;
    }
    protected void validationInfoChanged(Node node) {
        if (m_lastValidatedUnchangedNode!=null && node.getNodeID()<m_lastValidatedUnchangedNode.getNodeID()) {
            m_lastValidatedUnchangedNode=node;
        }
        m_directBlockingChecker.setHasChangedSinceValidation(node, true);
    }
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        if (m_directBlockingChecker.assertionAdded(concept,node,isCore)!=null || m_lastValidatedUnchangedNode!=null) updateNodeChange(node);
        validationInfoChanged(node);
    }
    public void assertionCoreSet(Concept concept,Node node) {
        if (m_directBlockingChecker.assertionAdded(concept,node,true)!=null || m_lastValidatedUnchangedNode!=null) updateNodeChange(node);
        validationInfoChanged(node);
    }
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        if (m_directBlockingChecker.assertionRemoved(concept,node,isCore)!=null || m_lastValidatedUnchangedNode!=null) updateNodeChange(node);
        validationInfoChanged(node);
    }
    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        if (isCore || m_lastValidatedUnchangedNode!=null) updateNodeChange(nodeFrom);
        if (isCore || m_lastValidatedUnchangedNode!=null) updateNodeChange(nodeTo);
        validationInfoChanged(nodeFrom);
        validationInfoChanged(nodeTo);
    }
    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        m_directBlockingChecker.assertionAdded(atomicRole,nodeFrom,nodeTo,true);
        if (m_lastValidatedUnchangedNode!=null) updateNodeChange(nodeFrom);
        if (m_lastValidatedUnchangedNode!=null) updateNodeChange(nodeTo);
        validationInfoChanged(nodeFrom);
        validationInfoChanged(nodeTo);
    }
    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        m_directBlockingChecker.assertionRemoved(atomicRole,nodeFrom,nodeTo,true);
        if (isCore || m_lastValidatedUnchangedNode!=null) updateNodeChange(nodeFrom);
        if (isCore || m_lastValidatedUnchangedNode!=null) updateNodeChange(nodeTo);
        validationInfoChanged(nodeFrom);
        validationInfoChanged(nodeTo);
    }
    public void nodeStatusChanged(Node node) {
        updateNodeChange(node);
    }
    protected final void updateNodeChange(Node node) {
        if (node!=null) {
            if (m_firstChangedNode==null || node.getNodeID()<m_firstChangedNode.getNodeID())
                m_firstChangedNode=node;
        }
    }
    public void nodeInitialized(Node node) {
        m_directBlockingChecker.nodeInitialized(node);
    }
    public void nodeDestroyed(Node node) {
        m_currentBlockersCache.removeNode(node);
        m_directBlockingChecker.nodeDestroyed(node);
        if (m_firstChangedNode!=null && m_firstChangedNode.getNodeID()>=node.getNodeID())
            m_firstChangedNode=null;
        if (m_lastValidatedUnchangedNode!=null && node.getNodeID()<m_lastValidatedUnchangedNode.getNodeID())
            m_lastValidatedUnchangedNode=node;
    }
    public void modelFound() {
        //System.out.println("Found  model with " + (m_tableau.getNumberOfNodesInTableau()-m_tableau.getNumberOfMergedOrPrunedNodes()) + " nodes. ");
        if (m_blockingSignatureCache!=null) {
            // Since we've found a model, we know what is blocked or not.
            // Therefore, we don't need to update the blocking status.
            assert m_firstChangedNode==null;
            Node node=m_tableau.getFirstTableauNode();
            while (node!=null) {
                if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                    m_blockingSignatureCache.addNode(node);
                node=node.getNextTableauNode();
            }
        }
//        System.out.println("Violations for the blocker:");
//        SortedSet<ViolationStatistic> counts=new TreeSet<ViolationStatistic>();
//        for (String key : m_violationCountBlocker.keySet()) {
//            counts.add(new ViolationStatistic(key,m_violationCountBlocker.get(key)));
//        }
//        for (ViolationStatistic vs : counts) {
//            System.out.println(vs);
//        }
//        counts.clear();
//        System.out.println("Violations for the blocked parent:");
//        for (String key : m_violationCountBlockedParent.keySet()) {
//            counts.add(new ViolationStatistic(key,m_violationCountBlockedParent.get(key)));
//        }
//        for (ViolationStatistic vs : counts) {
//            System.out.println(vs);
//        }
    }
    protected final class ViolationStatistic implements Comparable<ViolationStatistic>{
        public final String m_violatedConstraint;
        public final Integer m_numberOfViolations;
        public ViolationStatistic(String violatedConstraint, Integer numberOfViolations) {
            m_violatedConstraint=violatedConstraint;
            m_numberOfViolations=numberOfViolations;
        }
        public int compareTo(ViolationStatistic that) {
            if (this==that) return 0;
            if (that==null) throw new NullPointerException("Comparing to a null object is illegal. ");
            if (this.m_numberOfViolations==that.m_numberOfViolations) return m_violatedConstraint.compareTo(that.m_violatedConstraint);
            else return that.m_numberOfViolations-this.m_numberOfViolations;
        }
        public String toString() {
            return m_numberOfViolations + ": "+m_violatedConstraint.replaceAll("http://www.co-ode.org/ontologies/galen#", "");
        }
    }
    public boolean isExact() {
        return false;
    }
    public void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,List<Variable> variables,Object[] valuesBuffer,boolean[] coreVariables) {
        if (m_useSingletonCore) {
            for (int i=0;i<coreVariables.length;i++) {
                coreVariables[i]=false;
            }
        } else {
            if (dlClause.m_clauseType!=ClauseType.CONCEPT_INCLUSION) {
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
    protected void addViolation(BlockingViolation violation, Map<String, Integer> countMap) {
        String violatedConstraint=violation.getViolatedConstraint();
        Integer i=countMap.get(violatedConstraint);
        if (i!=null) {
            countMap.put(violatedConstraint, (i+1));
        } else {
            countMap.put(violatedConstraint, new Integer(1));
        }
        for (BlockingViolationType t : violation.m_causes.values()) {
            Integer count=m_causesCount.get(t);
            m_causesCount.put(t, count+1);
        }
    }
    
    public static class BlockingViolation{
        private final Node m_blocked;
        private final Node m_blocker;
        private final Set<AtomicConcept> m_violationPremises; 
        private final Set<Concept> m_violatedConjunct;
        private final Map<Concept, BlockingViolationType> m_causes;

        public BlockingViolation(Node blocked, Node blocker, Set<AtomicConcept> c, Map<Set<Concept>,Map<Concept, BlockingViolationType>> causes) {
            // Nodes involved
            m_blocked=blocked;
            m_blocker=blocker;
            // violation constraint
            m_violationPremises=c;
            m_violatedConjunct=causes.entrySet().iterator().next().getKey();
            m_causes=causes.get(m_violatedConjunct);
        }
        public BlockingViolation(Node blocked, Node blocker, AtomicConcept c, Map<Set<Concept>,Map<Concept, BlockingViolationType>> causes) {
            // Nodes involved
            m_blocked=blocked;
            m_blocker=blocker;
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
            b.append("\n");
            b.append("Blocker: "+m_blocker.getNodeID()+", blocked: "+m_blocked.getNodeID()+"\n");
            return b.toString();
        }
    }
}
class ValidatedBlockersCache {
    protected Tableau m_tableau;
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected CacheEntry[] m_buckets;
    protected int m_numberOfElements;
    protected int m_threshold;
    protected CacheEntry m_emptyEntries;

    public ValidatedBlockersCache(DirectBlockingChecker directBlockingChecker) {
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
        ValidatedBlockersCache.CacheEntry removeEntry=(ValidatedBlockersCache.CacheEntry)node.getBlockingCargo();
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
                    } else {
                        if (entry.m_nodes.contains(node)) {
                            for (int i=entry.m_nodes.size()-1; i>=entry.m_nodes.indexOf(node); i--) {
                                entry.m_nodes.get(i).setBlockingCargo(null);
                            }
                            entry.m_nodes.subList(entry.m_nodes.indexOf(node), entry.m_nodes.size()).clear();
                        } else {
                            throw new IllegalStateException("Internal error: entry not in cache!");
                        }
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
        if (m_directBlockingChecker.canBeBlocked(node)) {
            int hashCode=m_directBlockingChecker.blockingHashCode(node);
            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
            CacheEntry entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_nodes.get(0),node)) {
                    if (node.getBlocker()!=null && entry.m_nodes.contains(node.getBlocker())) {
                        // don't change the blocker unnecessarily, the blocking validation code will change the blocker if necessary
                        return node.getBlocker();
                    } else {
                        return entry.m_nodes.get(0);
                    }
                }
                entry=entry.m_nextEntry;
            }
        }
        return null;
    }
    public List<Node> getPossibleBlockers(Node node) {
        if (m_directBlockingChecker.canBeBlocked(node)) {
            int hashCode=m_directBlockingChecker.blockingHashCode(node);
            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
            CacheEntry entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_nodes.get(0),node)) {
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
    public void doSanityCheck(boolean allSingletonSets) {
        for (int i=0;i<m_buckets.length;i++) {
            CacheEntry entry=m_buckets[i];
            while (entry!=null) {
                if (allSingletonSets && entry.m_nodes.size() > 1) {
                    throw new IllegalStateException("Internal error: we expect all cache entries to be singleton sets, but the is a set with cardinality greater than one. ");
                }
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
            m_nodes=new ArrayList<Node>();
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