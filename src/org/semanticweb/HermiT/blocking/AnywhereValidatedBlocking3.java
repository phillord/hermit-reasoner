// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.semanticweb.HermiT.tableau.Tableau;

public class AnywhereValidatedBlocking3 implements BlockingStrategy {

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
    
    protected ExtensionManager m_extensionManager;
    protected ExtensionTable.Retrieval m_ternaryTableSearchZeroOneBound;
    protected ExtensionTable.Retrieval m_ternaryTableSearchZeroTwoBound;
    protected ExtensionTable.Retrieval m_binaryTableSearch1Bound;
    protected final Object[] m_auxiliaryTuple;
    
    protected final boolean m_hasInverses;
    protected boolean m_immediatelyValidateBlocks = false;
    
    // statistics: 
    protected int numDirectlyBlocked=0;
    protected int numIndirectlyBlocked=0;
    protected final boolean debuggingMode=false;
    protected Map<String, Integer> m_violationCount=new HashMap<String, Integer>();
    protected Map<BlockingViolationType, Integer> m_causesCount=new HashMap<BlockingViolationType, Integer>();

    public AnywhereValidatedBlocking3(DirectBlockingChecker directBlockingChecker,BlockingSignatureCache blockingSignatureCache,Map<AtomicConcept, Set<Set<Concept>>> unaryValidBlockConditions, Map<Set<AtomicConcept>, Set<Set<Concept>>> nAryValidBlockConditions, boolean hasInverses) {
        m_directBlockingChecker=directBlockingChecker;
        m_currentBlockersCache=new ValidatedBlockersCache(m_directBlockingChecker);
        m_blockingSignatureCache=blockingSignatureCache;
        m_unaryValidBlockConditions = unaryValidBlockConditions;
        m_nAryValidBlockConditions = nAryValidBlockConditions;
        m_hasInverses = hasInverses;
        m_auxiliaryTuple=new Object[2];
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_directBlockingChecker.initialize(tableau);
        m_extensionManager=m_tableau.getExtensionManager();
        m_ternaryTableSearchZeroOneBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
        m_ternaryTableSearchZeroTwoBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.TOTAL);
        m_binaryTableSearch1Bound=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
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
                    if (m_directBlockingChecker.hasBlockingInfoChanged(node) || (m_lastValidatedUnchangedNode!=null&&m_directBlockingChecker.hasChangedSinceValidation(node)) || !node.isDirectlyBlocked() || node.getBlocker().getNodeID()>=m_firstChangedNode.getNodeID()) {
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
                        Node validBlocker=null;
                        if (node.isDirectlyBlocked() || !node.getParent().isBlocked()) {
                            if (!node.getParent().isBlocked() || m_directBlockingChecker.hasChangedSinceValidation(node) || m_directBlockingChecker.hasChangedSinceValidation(node.getParent()) || m_directBlockingChecker.hasChangedSinceValidation(node.getBlocker()) || m_directBlockingChecker.hasChangedSinceValidation(node.getBlocker().getParent())) {
                                List<Node> possibleBlockers = m_currentBlockersCache.getPossibleBlockers(node);
                                if (!possibleBlockers.isEmpty()) {
                                    int i=0;
                                    if (possibleBlockers.contains(node.getBlocker())) {
                                        // we always assign the smallest node that has been modified since the last validation
                                        // re-testing smaller (unmodified) ones makes no sense 
                                        i=possibleBlockers.indexOf(node.getBlocker());
                                    }
                                    for (; i<possibleBlockers.size(); i++) {
                                        Node blocker=possibleBlockers.get(i);
                                        boolean blockerIsSuitable = true;
                                        // get all existentials of the parent of the blocked node
                                        m_binaryTableSearch1Bound.getBindingsBuffer()[1]=node.getParent();
                                        m_binaryTableSearch1Bound.open();
                                        Object[] tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
                                        while (!m_binaryTableSearch1Bound.afterLast() && blockerIsSuitable) {
                                            Concept c=(Concept)tupleBuffer[0];
                                            if (c instanceof AtLeastConcept) {
                                                AtLeastConcept atLeast = (AtLeastConcept) c;
                                                Role r = atLeast.getOnRole();
                                                LiteralConcept filler = atLeast.getToConcept();
                                                if (isInLabel(filler, node) && !isInLabel(filler, blocker) && isInLabelFromParentToNode(r, node)) {
                                                    blockerIsSuitable=false;
                                                } 
                                            }
                                            m_binaryTableSearch1Bound.next();
                                        }
                                        // get all existentials of the blocker and in case they use the parent of the blocker
                                        // check whether the parent of the blocked is also suitable
                                        if (m_hasInverses && blockerIsSuitable) {
                                            m_binaryTableSearch1Bound.getBindingsBuffer()[1]=blocker;
                                            m_binaryTableSearch1Bound.open();
                                            tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
                                            while (!m_binaryTableSearch1Bound.afterLast() && blockerIsSuitable) {
                                                Concept c=(Concept)tupleBuffer[0];
                                                if (c instanceof AtLeastConcept) {
                                                    AtLeastConcept atLeast = (AtLeastConcept) c;
                                                    Role r = atLeast.getOnRole();
                                                    LiteralConcept filler = atLeast.getToConcept();
                                                    if (isInLabel(filler, blocker.getParent()) && isInLabelFromNodeToParent(r, blocker) && (!isInLabel(filler, node.getParent()) || !isInLabelFromNodeToParent(r, node))) {
                                                        if (!hasAtLeastNSuccessors(blocker, atLeast.getNumber(), r, filler)) {
                                                            blockerIsSuitable=false;
                                                        }
                                                    }
                                                }
                                                m_binaryTableSearch1Bound.next();
                                            }
                                        }
                                        if (blockerIsSuitable && constraintsSatisfied(node, blocker)) {
                                            // so far so good: we have checked all existentials and now we check whether also the forall/atmost 
                                            // constraints are ok
                                            validBlocker=blocker;
                                            break;
                                        }
                                    }
                                }
                                if (validBlocker == null) {
                                    invalidBlocks++;
                                    if (firstInvalidlyBlockedNode==null) firstInvalidlyBlockedNode=node;
                                }
                                node.setBlocked(validBlocker,validBlocker!=null);
                            } 
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
                if (node.isActive()) {
                    m_directBlockingChecker.setHasChangedSinceValidation(node, false);
                    m_directBlockingChecker.clearBlockingInfoChanged(node);
                }
                node=node.getNextTableauNode();
            }
            // if set to some node, then computePreblocking will be asked to check from that node onwards in case of invalid blocks 
            m_firstChangedNode=firstInvalidlyBlockedNode;
            //m_firstChangedNode=firstValidatedNode;
            //m_firstChangedNode=null;
            if (debuggingMode) System.out.println("Checked " + checkedBlocks + " blocked nodes of which " + invalidBlocks + " were invalid.");
        }
    }
    protected boolean constraintsSatisfied(Node blocked, Node blocker) {
        Set<AtomicConcept> blockedParentLabel=m_directBlockingChecker.getFullAtomicConceptsLabel(blocked.getParent());
        boolean blockerIsSuitable = true;
        // check whether max cardinalities of the parent of the blocked node could be violated
        // universals have been converted to max restrictions for convenience
        AtomicConcept c;
        for (Iterator<AtomicConcept> bpIt = blockedParentLabel.iterator(); bpIt.hasNext() && blockerIsSuitable; ) {
            c = bpIt.next();
            if (m_unaryValidBlockConditions.containsKey(c)) {
                Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_unaryValidBlockConditions.get(c), blocker, blocked);
                if (violationCauses.size()!=0) { 
                    blockerIsSuitable = false;
                    //if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, c, violationCauses));
                }
            }
        }
        // check top, which is not explicitly present in the label, but might be the premise of some constraint
        if (blockerIsSuitable && m_unaryValidBlockConditions.containsKey(AtomicConcept.THING)) {
            Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_unaryValidBlockConditions.get(AtomicConcept.THING), blocker, blocked); 
            if (violationCauses.size()!=0) { 
                blockerIsSuitable = false;
                //if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, AtomicConcept.THING, violationCauses));
            }
        }
        // repeat the same checks for non-unary premises (less efficient matching operations)
        if (blockerIsSuitable) {
            for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
                if (blockedParentLabel.containsAll(premises)) {
                    Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_nAryValidBlockConditions.get(premises), blocker, blocked);
                    if (violationCauses.size()!=0) { 
                        blockerIsSuitable = false;
                        //if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, premises, violationCauses));
                    }
                }
                if (!blockerIsSuitable) break;
            }
        }
        // check whether min/max cardinalities of the blocker are not violated when copied to the blocked node
        Set<AtomicConcept> blockerLabel=m_directBlockingChecker.getFullAtomicConceptsLabel(blocker);
        if (blockerIsSuitable && m_hasInverses) {
            for (Iterator<AtomicConcept> blIt = blockerLabel.iterator(); blIt.hasNext() && blockerIsSuitable; ) {
                c = blIt.next();
                if (m_unaryValidBlockConditions.containsKey(c)) {
                    Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_unaryValidBlockConditions.get(c), blocker, blocked); 
                    if (violationCauses.size()!=0) { 
                        blockerIsSuitable = false;
                        //if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, c, violationCauses));
                    }
                }
            }
        }
        // check top, which is not explicitly present in the label, but might be the premise of some constraint
        if (blockerIsSuitable && m_unaryValidBlockConditions.containsKey(AtomicConcept.THING)) {
            Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_unaryValidBlockConditions.get(AtomicConcept.THING), blocker, blocked);    
            if (violationCauses.size()!=0) { 
                blockerIsSuitable = false;
                //if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, AtomicConcept.THING, violationCauses));
            }
        }
        // repeat the same checks for non-unary premises (less efficient matching operations)
        if (blockerIsSuitable && m_hasInverses) {
            for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
                if (blockerLabel.containsAll(premises)) {
                    Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_nAryValidBlockConditions.get(premises), blocker, blocked); 
                    if (violationCauses.size()!=0) { 
                        blockerIsSuitable = false;
                        //if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, premises, violationCauses));
                    }
                }
                if (!blockerIsSuitable) break;
            }
        }
        return blockerIsSuitable;
    }
    protected Map<Set<Concept>, Map<Concept, BlockingViolationType>> getBlockedParentViolations(Set<Set<Concept>> conclusions, Node blocker, Node blocked) {
        Map<Concept, BlockingViolationType> disjunct2Cause=new HashMap<Concept, BlockingViolationType>();
        Map<Set<Concept>, Map<Concept, BlockingViolationType>> conjunct2violations=new HashMap<Set<Concept>, Map<Concept, BlockingViolationType>>();
        for (Set<Concept> conjunct : conclusions) {
            boolean satisfied = false;
            for (Iterator<Concept> it = conjunct.iterator(); it.hasNext() && !satisfied; ) {
                Concept disjunct = it.next();
                satisfied = true;
                if (disjunct instanceof AtLeastConcept) {
                    // we have something like A -> >=1 r.C or <= 0 s.not D for example
                    // we checked at leasts already, so just check the label
                    AtLeastConcept atLeast = (AtLeastConcept) disjunct;
                    if (!m_extensionManager.containsAssertion(atLeast, blocked.getParent())) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATLEASTBLOCKEDPARENT);
                    }
                } else if (disjunct instanceof AtMostConcept) {
                    // (<= n r.B) must hold at blockedParent, therefore, ar(r, blockedParent, blocked) in ABox and B(blocked) not in ABox implies B(blocker) not in ABox
                    // to avoid table lookup, we check: not B(blocked) and B(blocker) implies not ar(r, blockedParent, blocked)
                    AtMostConcept atMost = (AtMostConcept)disjunct;
                    Role r = atMost.getOnRole(); // r
                    LiteralConcept filler = atMost.getToConcept();
                    if (!isInLabel(filler, blocked) && isInLabel(filler, blocker) && isInLabelFromParentToNode(r, blocked)) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKEDPARENT);
                    }
                } else if (disjunct instanceof AtomicConcept) {
                    // happens if we have something like A -> (<= 0 r.not B) or C. If (<= 0 r.not B) is not guaranteed 
                    // for the parent of the blocked node, but C is, then we are fine, so only if C does not hold, we have to look further. 
                    if(!isInLabel((AtomicConcept)disjunct, blocked.getParent())) {
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
        Map<Concept, BlockingViolationType> disjunct2Cause=new HashMap<Concept, BlockingViolationType>();
        Map<Set<Concept>, Map<Concept, BlockingViolationType>> conjunct2violations=new HashMap<Set<Concept>, Map<Concept, BlockingViolationType>>();
        for (Set<Concept> conjunct : conclusions) {
            boolean satisfied = false;
            for (Iterator<Concept> it = conjunct.iterator(); it.hasNext() && !satisfied; ) {
                Concept disjunct = it.next();
                satisfied = true;
                if (disjunct instanceof AtLeastConcept) {
                    // we checked at leasts already, so only check existence in the label for constraints such as 
                    // A -> >= 1 r.C or <= 0 s.not B
                    AtLeastConcept atLeast = (AtLeastConcept) disjunct;
                    if (!m_extensionManager.containsAssertion(atLeast, blocker)) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATLEASTBLOCKER);
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
        updateNodeChange(m_directBlockingChecker.assertionAdded(concept,node,isCore));
        validationInfoChanged(node);
    }
    public void assertionCoreSet(Concept concept,Node node) {
        updateNodeChange(m_directBlockingChecker.assertionAdded(concept,node,true));
        validationInfoChanged(node);
    }
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        updateNodeChange(m_directBlockingChecker.assertionRemoved(concept,node,isCore));
        validationInfoChanged(node);
    }
    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        updateNodeChange(m_directBlockingChecker.assertionAdded(atomicRole,nodeFrom,nodeTo,isCore));
        validationInfoChanged(nodeFrom);
        validationInfoChanged(nodeTo);
    }
    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        updateNodeChange(m_directBlockingChecker.assertionAdded(atomicRole,nodeFrom,nodeTo,true));
        validationInfoChanged(nodeFrom);
        validationInfoChanged(nodeTo);
    }
    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        updateNodeChange(m_directBlockingChecker.assertionRemoved(atomicRole,nodeFrom,nodeTo,isCore));
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
    }
    public boolean isExact() {
        return false;
    }
    public void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,List<Variable> variables,Object[] valuesBuffer,boolean[] coreVariables) {
        for (int i=0;i<coreVariables.length;i++) {
            coreVariables[i]=false;
        }
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