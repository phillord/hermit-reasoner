// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.blocking.ValidatedDirectBlockingChecker.ValidatedBlockingObject;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.model.DLClause.ClauseType;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;

public class AnywhereValidatedBlockingRules implements BlockingStrategy {

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
    protected BlockingValidator m_blockingValidator;
    protected Tableau m_tableau;
    protected Node m_firstChangedNode;
    protected Node m_lastValidatedUnchangedNode=null;
    
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
    
    public AnywhereValidatedBlockingRules(DirectBlockingChecker directBlockingChecker,BlockingSignatureCache blockingSignatureCache,boolean hasInverses,boolean useSingletonCore) {
        m_directBlockingChecker=directBlockingChecker;
        m_currentBlockersCache=new ValidatedBlockersCache(m_directBlockingChecker);
        m_blockingSignatureCache=blockingSignatureCache;
        m_useSingletonCore=useSingletonCore;
        m_hasInverses = hasInverses;
        m_auxiliaryTuple=new Object[2];
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_blockingValidator=new BlockingValidator(m_tableau);
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
                    if (node.isDirectlyBlocked() || !node.getParent().isBlocked()) {
                        if (m_directBlockingChecker.hasChangedSinceValidation(node) || m_directBlockingChecker.hasChangedSinceValidation(node.getParent()) || m_directBlockingChecker.hasChangedSinceValidation(node.getBlocker()) || m_directBlockingChecker.hasChangedSinceValidation(node.getBlocker().getParent())) {
                            List<Node> possibleBlockers = m_currentBlockersCache.getPossibleBlockers(node);
                            validBlocker=null;
                            if (!possibleBlockers.isEmpty()) {
                                int i=0;
                                if (node.getBlocker()!=null && possibleBlockers.contains(node.getBlocker())) {
                                    // we always assign the smallest node that has been modified since the last validation
                                    // re-testing smaller (unmodified) ones makes no sense 
                                    i=possibleBlockers.indexOf(node.getBlocker());
                                }
                                for (; i<possibleBlockers.size(); i++) {
                                    Node blocker=possibleBlockers.get(i);
                                    node.setBlocked(blocker,true);
                                    if (m_blockingValidator.isBlockValid(node)) {
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
                ValidatedBlockingObject blockingObject=(ValidatedBlockingObject)node.getBlockingObject();
                blockingObject.m_blockViolatesParentConstraints=false;
                blockingObject.m_hasAlreadyBeenChecked=false;
            }
            node=node.getNextTableauNode();
        }
        // if set to some node, then computePreblocking will be asked to check from that node onwards in case of invalid blocks 
        m_firstChangedNode=firstInvalidlyBlockedNode;
        if (monitor!=null) monitor.blockingValidationFinished();
        //m_firstChangedNode=firstValidatedNode;
        //m_firstChangedNode=null;
        if (debuggingMode) System.out.println("Checked " + checkedBlocks + " blocked nodes of which " + invalidBlocks + " were invalid.");
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