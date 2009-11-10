package org.semanticweb.HermiT.blocking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.blocking.core.AtMostConcept;
import org.semanticweb.HermiT.blocking.core.AtMostConjunctionConcept;
import org.semanticweb.HermiT.blocking.core.AtMostDisjunctionConcept;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.HermiT.tableau.ExtensionTable.Retrieval;

/**
 * Checks whether the rules from some set are applicable given the current state of the extensions.
 */
public class BlockingValidatorConstraints implements BlockingValidator {
    public static enum BlockingViolationType {
        ATLEASTBLOCKEDPARENT, 
        ATMOSTBLOCKEDPARENT,
        ATOMICBLOCKEDPARENT,
        ATLEASTBLOCKER, 
        ATMOSTBLOCKER,
        ATOMICBLOCKER
    }
    protected final Tableau m_tableau;
    protected final ExtensionManager m_extensionManager;
    protected final ExtensionTable.Retrieval m_ternaryTableSearchZeroOneBound;
    protected final ExtensionTable.Retrieval m_ternaryTableSearchZeroTwoBound;
    protected final Object[] m_auxiliaryTuple;
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final Map<AtomicConcept, Set<Set<Concept>>> m_unaryValidBlockConditions; 
    protected final Map<Set<AtomicConcept>, Set<Set<Concept>>> m_nAryValidBlockConditions;
    protected final boolean m_hasInverses;
    
    protected final boolean debuggingMode=false;
    protected final Map<String, Integer> m_violationCountBlocker=new HashMap<String, Integer>();
    protected final Map<String, Integer> m_violationCountBlockedParent=new HashMap<String, Integer>();
    protected final Map<BlockingViolationType, Integer> m_causesCount=new HashMap<BlockingViolationType, Integer>();
    protected int one=0;
    protected int two=0;
    protected int three=0;
    protected int four=0;
    protected int five=0;
    protected int six=0;
    
    public BlockingValidatorConstraints(Tableau tableau,DirectBlockingChecker directBlockingChecker,Map<AtomicConcept, Set<Set<Concept>>> unaryValidBlockConditions, Map<Set<AtomicConcept>, Set<Set<Concept>>> nAryValidBlockConditions, boolean hasInverses) {
        m_tableau=tableau;
        m_extensionManager=m_tableau.getExtensionManager();
        m_ternaryTableSearchZeroOneBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
        m_ternaryTableSearchZeroTwoBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.TOTAL);
        m_directBlockingChecker=directBlockingChecker;
        m_unaryValidBlockConditions=unaryValidBlockConditions;
        m_nAryValidBlockConditions=nAryValidBlockConditions;
        m_hasInverses = hasInverses;
        m_auxiliaryTuple=new Object[2];
        if (debuggingMode) {
            m_causesCount.put(BlockingViolationType.ATLEASTBLOCKEDPARENT, 0);
            m_causesCount.put(BlockingViolationType.ATLEASTBLOCKER, 0);
            m_causesCount.put(BlockingViolationType.ATMOSTBLOCKEDPARENT, 0);
            m_causesCount.put(BlockingViolationType.ATMOSTBLOCKER, 0);
            m_causesCount.put(BlockingViolationType.ATOMICBLOCKEDPARENT, 0);
            m_causesCount.put(BlockingViolationType.ATOMICBLOCKER, 0);
        }
    }
    public void blockerChanged(Node node) {
        // nothing needs updating
    }
    public boolean isBlockValid(Node blocked) {
        // we have that the node blocked is (pre-)blocked and we have to validate whether the block is valid 
        // that is we can create a model from the block by unravelling

        Node blocker=blocked.getBlocker();
        // check whether min/max cardinalities of the blocker are not violated when copied to the blocked node
        Set<AtomicConcept> blockerLabel=m_directBlockingChecker.getFullAtomicConceptsLabel(blocker);
        Set<AtomicConcept> blockedParentLabel=m_directBlockingChecker.getFullAtomicConceptsLabel(blocked.getParent());
        AtomicConcept c;
        if (m_hasInverses) {
            for (Iterator<AtomicConcept> blIt = blockerLabel.iterator(); blIt.hasNext(); ) {
                c = blIt.next();
                if (m_unaryValidBlockConditions.containsKey(c)) {
                    Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_unaryValidBlockConditions.get(c), blocker, blocked); 
                    if (violationCauses.size()!=0) { 
                        if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, c, violationCauses),m_violationCountBlocker);
                        if (debuggingMode) one++;
                        return false;
                    }
                }
            }
        }
        // check whether min/max cardinalities of the parent of the blocked node could be violated
        // universals and existential have been converted to min/max restrictions for convenience
        for (Iterator<AtomicConcept> bpIt = blockedParentLabel.iterator(); bpIt.hasNext(); ) {
            c = bpIt.next();
            if (m_unaryValidBlockConditions.containsKey(c)) {
                Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_unaryValidBlockConditions.get(c), blocker, blocked);
                if (violationCauses.size()!=0) { 
                    if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, c, violationCauses),m_violationCountBlockedParent);
                    if (debuggingMode) two++;
                    return false;
                }
            }
        }
        // check top, which is not explicitly present in the label, but might be the premise of some constraint
        if (m_unaryValidBlockConditions.containsKey(AtomicConcept.THING)) {
            Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_unaryValidBlockConditions.get(AtomicConcept.THING), blocker, blocked);    
            if (violationCauses.size()!=0) { 
                if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, AtomicConcept.THING, violationCauses),m_violationCountBlocker);
                if (debuggingMode) three++;
                return false;
            }
        }
        // check top, which is not explicitly present in the label, but might be the premise of some constraint
        if (m_unaryValidBlockConditions.containsKey(AtomicConcept.THING)) {
            Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_unaryValidBlockConditions.get(AtomicConcept.THING), blocker, blocked); 
            if (violationCauses.size()!=0) { 
                if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, AtomicConcept.THING, violationCauses),m_violationCountBlockedParent);
                if (debuggingMode) four++;
                return false;
            }
        }
        // repeat the same checks for non-unary premises (less efficient matching operations)
        if (m_hasInverses) {
            for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
                if (blockerLabel.containsAll(premises)) {
                    Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockerViolations(m_nAryValidBlockConditions.get(premises), blocker, blocked); 
                    if (violationCauses.size()!=0) { 
                        if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, premises, violationCauses),m_violationCountBlocker);
                        if (debuggingMode) five++;
                        return false;
                    }
                }
            }
        }
        // repeat the same checks for non-unary premises (less efficient matching operations)
        for (Set<AtomicConcept> premises : m_nAryValidBlockConditions.keySet()) {
            if (blockedParentLabel.containsAll(premises)) {
                Map<Set<Concept>,Map<Concept, BlockingViolationType>> violationCauses=getBlockedParentViolations(m_nAryValidBlockConditions.get(premises), blocker, blocked);
                if (violationCauses.size()!=0) { 
                    if (debuggingMode) addViolation(new BlockingViolation(blocked, blocker, premises, violationCauses),m_violationCountBlockedParent);
                    if (debuggingMode) six++;
                    return false;
                }
            }
        }
        return true;
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
                    if (!isInLabel(filler, blocked) && isInLabel(filler, blocker) && isInLabelFromParentToNode(r, blocked)) {
                        // TODO: Fix Me! 
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKEDPARENT);
                    }
                } else if (disjunct instanceof AtMostConjunctionConcept) {
                    // A -> >= n s.C \/ forall r.{o_1, ... o_n}
                    // A(blockedParent) holds and (>= n s.C)(blockedParent) is possibly violated in the model construction, but we are still ok if 
                    // forall r.{o_1, ... o_n}(blockedParent) = (<= 0 r.not o_1 and ... and not o_n)(blockedParent) is ok. 
                    // Check the r successors of blockedParent to be nominal nodes for o_1 or ... or o_n 
                    AtMostConjunctionConcept atMost = (AtMostConjunctionConcept) disjunct;
                    Role r = atMost.getOnRole();
                    AtomicConcept[] oneOf=atMost.getToConcept();
                    if (!allRSuccessorsOneOf(blocked.getParent(), r, oneOf)) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKEDPARENT);
                    }
                } else if (disjunct instanceof AtMostDisjunctionConcept) {
                    // A -> >= n s.C \/ forall r.not {o_1, ... o_n}
                    // forall r.not {o_1, ... o_n} = (<= 0 r.{o_1 or ... or o_n}) = forall r.(not o_1 and ... and not o_n) 
                    // A(blockedParent) holds and (>= n s.C)(blockedParent) is possibly violated in the model construction, 
                    // but we are still ok if forall r.not {o_1, ... o_n}(blockedParent) is ok. 
                    // Check the r successors of blockedParent to be nominal nodes for o_1 or ... or o_n 
                    AtMostDisjunctionConcept atMost = (AtMostDisjunctionConcept) disjunct;
                    Role r = atMost.getOnRole();
                    AtomicConcept[] oneOf=atMost.getToConcept();
                    if (!allRSuccessorsNotOneOf(blocked.getParent(), r, oneOf)) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKEDPARENT);
                    }
                } else if (disjunct instanceof AtomicConcept) {
                    // happens if we have something like A -> (>= n r.B) or C and A(blockedParent) holds, 
                    // but (>= n r.B)(blockedParent) is not guaranteed. If C(blockedParent) holds, then we are 
                    // fine, so only if C does not hold, we have to look further. 
                    if(!isInLabel((AtomicConcept)disjunct, blocked.getParent())) {
                        // must be an atomic concept or normal form is violated
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATOMICBLOCKEDPARENT);
                    }
                } else if (disjunct instanceof AtLeastConcept) {
                    // (>= n r.B) must hold at blockedParent, therefore, ar(r, blockedParent, blocked) and B(blocked) in ABox implies B(blocker) in ABox
                    // to avoid table lookup check: B(blocked) and not B(blocker) implies not ar(r, blockedParent, blocked)
                    AtLeastConcept atLeast = (AtLeastConcept) disjunct;
                    if (m_extensionManager.containsConceptAssertion(atLeast, blocked.getParent())) {
                        Role r = atLeast.getOnRole();
                        LiteralConcept filler = atLeast.getToConcept();
                        if (isInLabel(filler, blocked) && !isInLabel(filler, blocker) && isInLabelFromParentToNode(r, blocked)) {
                            satisfied = false;
                            disjunct2Cause.put(disjunct, BlockingViolationType.ATLEASTBLOCKEDPARENT);
                        }
                    } else {
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
                    // so we have to make sure that it will be satisfied at the blocked node.
                    // Check B(blockerParent) and ar(r, blocker, blockerParent) in ABox implies B(blockedParent) and ar(r, blocked, blockedParent) in ABox
                    // or blocker has at least n r-successors bs such that B(bs) holds
                    AtLeastConcept atLeast = (AtLeastConcept) disjunct;
                    if (m_extensionManager.containsConceptAssertion(atLeast, blocker)) {
                        Role r = atLeast.getOnRole();
                        LiteralConcept filler = atLeast.getToConcept();
                        if (isInLabel(filler, blocker.getParent()) && isInLabelFromNodeToParent(r, blocker) && (!isInLabel(filler, blocked.getParent()) || !isInLabelFromNodeToParent(r, blocked))) {
                            if (!hasAtLeastNSuccessors(blocker, atLeast.getNumber(), r, filler)) {
                                satisfied = false;
                                disjunct2Cause.put(disjunct, BlockingViolationType.ATLEASTBLOCKER);
                            }
                        }
                    } else {
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
                } else if (disjunct instanceof AtMostConjunctionConcept) {
                    // forall r.{o_1 ... o_n} = (<= 0 r.not o_1 and ... and not o_n)(blocker)
                    // A -> >= m s.C \/ forall r.{o_1 ... o_n} and A(blocker) holds but (>= m s.C) is violated 
                    // either at the blocker or after the model construction
                    // the links to nominals will also be copied from the blocker to the blocked in the model 
                    // construction and replace the original nominal links of the blocked node
                    // check whether not r(blocked, blockedParent) holds and whether all r successors of the 
                    // blocked node are one of o_1 ... o_n
                    AtMostConjunctionConcept atMost = (AtMostConjunctionConcept) disjunct;
                    Role r = atMost.getOnRole();
                    AtomicConcept[] oneOf=atMost.getToConcept();
                    if (m_extensionManager.containsRoleAssertion(r, blocked, blocked.getParent()) || !allRSuccessorsOneOf(blocker, r, oneOf)) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKER);
                    }
                } else if (disjunct instanceof AtMostDisjunctionConcept) {
                    // A -> >= n s.C \/ forall r.not {o_1, ... o_n}
                    // forall r.not {o_1, ... o_n} = (<= 0 r.{o_1 or ... or o_n}) = forall r.(not o_1 and ... and not o_n) 
                    // A(blocker) holds and (>= n s.C)(blocker) is possibly violated now or in the model construction, 
                    // but we are still ok if forall r.not {o_1, ... o_n}(blocker) is ok (all nominal r-successors of blocker 
                    // are not o_1 and ... and not o_n). 
                    // The nominal sucessors of blocker replace those of blocked in the model construction.  
                    AtMostDisjunctionConcept atMost = (AtMostDisjunctionConcept) disjunct;
                    Role r = atMost.getOnRole();
                    AtomicConcept[] notOneOf=atMost.getToConcept();
                    if (!allRSuccessorsNotOneOf(blocker, r, notOneOf)) {
                        satisfied = false;
                        disjunct2Cause.put(disjunct, BlockingViolationType.ATMOSTBLOCKER);
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
        int queriedNodeIndex=2;
        Retrieval relevantRetrieval;
        if (r instanceof AtomicRole) {
            relevantRetrieval=m_ternaryTableSearchZeroOneBound;
            relevantRetrieval.getBindingsBuffer()[0]=r;
            relevantRetrieval.getBindingsBuffer()[1]=blocker;
        } else {
            // inverse role
            relevantRetrieval=m_ternaryTableSearchZeroTwoBound;
            relevantRetrieval.getBindingsBuffer()[0]=r.getInverse();
            relevantRetrieval.getBindingsBuffer()[2]=blocker;
            queriedNodeIndex=1;
        }
        relevantRetrieval.open();
        Object[] tupleBuffer=relevantRetrieval.getTupleBuffer();
        while (!relevantRetrieval.afterLast() && suitableSuccessors < n) {
            Node possibleSuccessor=(Node)tupleBuffer[queriedNodeIndex];
            if (!possibleSuccessor.isAncestorOf(blocker)) {
                if (filler.isAlwaysTrue() 
                        || (!filler.isAlwaysFalse() 
                                && (filler instanceof AtomicConcept && m_extensionManager.containsConceptAssertion(filler, possibleSuccessor)) 
                                || (filler instanceof AtomicNegationConcept && !m_extensionManager.containsConceptAssertion(((AtomicNegationConcept)filler).getNegatedAtomicConcept(), possibleSuccessor)))) {
                    suitableSuccessors++;
                } 
            }
            relevantRetrieval.next();
        }
        return (suitableSuccessors >= n);
    }
    protected boolean allRSuccessorsOneOf(Node node, Role r, AtomicConcept[] oneOfs) {
        int queriedNodeIndex=2;
        Retrieval relevantRetrieval;
        if (r instanceof AtomicRole) {
            relevantRetrieval=m_ternaryTableSearchZeroOneBound;
            relevantRetrieval.getBindingsBuffer()[0]=r;
            relevantRetrieval.getBindingsBuffer()[1]=node;
        } else {
            // inverse role
            relevantRetrieval=m_ternaryTableSearchZeroTwoBound;
            relevantRetrieval.getBindingsBuffer()[0]=r.getInverse();
            relevantRetrieval.getBindingsBuffer()[2]=node;
            queriedNodeIndex=1;
        }
        relevantRetrieval.open();
        Object[] tupleBuffer=relevantRetrieval.getTupleBuffer();
        while (!relevantRetrieval.afterLast()) {
            Node shouldBeOneOf=(Node)tupleBuffer[queriedNodeIndex];
            if (!shouldBeOneOf.isRootNode()) return false;
            boolean isOneOf=false;
            for (AtomicConcept oneOf : oneOfs) {
                if (m_extensionManager.containsConceptAssertion(oneOf, shouldBeOneOf)) {
                    isOneOf=true;
                    break;
                }
            }
            if (!isOneOf) return false;
            relevantRetrieval.next();
        }
        return true;
    }
    protected boolean allRSuccessorsNotOneOf(Node node, Role r, AtomicConcept[] oneOfs) {
        int queriedNodeIndex=2;
        Retrieval relevantRetrieval;
        if (r instanceof AtomicRole) {
            relevantRetrieval=m_ternaryTableSearchZeroOneBound;
            relevantRetrieval.getBindingsBuffer()[0]=r;
            relevantRetrieval.getBindingsBuffer()[1]=node;
        } else {
            // inverse role
            relevantRetrieval=m_ternaryTableSearchZeroTwoBound;
            relevantRetrieval.getBindingsBuffer()[0]=r.getInverse();
            relevantRetrieval.getBindingsBuffer()[2]=node;
            queriedNodeIndex=1;
        }
        relevantRetrieval.open();
        Object[] tupleBuffer=relevantRetrieval.getTupleBuffer();
        while (!relevantRetrieval.afterLast()) {
            Node shouldBeNotOneOf=(Node)tupleBuffer[queriedNodeIndex];
            if (shouldBeNotOneOf.isRootNode()) {
                for (AtomicConcept oneOf : oneOfs) 
                    if (m_extensionManager.containsConceptAssertion(oneOf, shouldBeNotOneOf)) 
                        return false;
            }
            relevantRetrieval.next();
        }
        return true;
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
