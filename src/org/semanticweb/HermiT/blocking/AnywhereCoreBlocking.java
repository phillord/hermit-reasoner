// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.blocking.SingleDirectBlockingChecker.SingleBlockingObject;
import org.semanticweb.HermiT.blocking.core.AtMostConcept;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.Role;
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
    protected final BlockersCache m_currentBlockersCache;
    protected final SetFactory<AtomicConcept> m_atomicConceptsSetFactory;
    protected final List<AtomicConcept> m_atomicConceptsBuffer;
    protected final Object[] m_auxiliaryTuple;
    protected ExtensionManager m_extensionManager;
    protected Node m_firstChangedNode;
    protected final Map<AtomicConcept, Set<Concept>> m_blockRelUnary; 
    protected final Map<Set<AtomicConcept>, Set<Concept>> m_blockRelNAry;
    protected ExtensionTable.Retrieval m_ternaryTableSearchAllBound;
    protected int numBlockingComputed = 0;
    protected int maxCore = 0;
    protected int maxLabel = 0;
    protected int avgCore = 0;
    protected int avgLabel = 0;
    protected int maxNodes = 0;
    protected long sumNodes = 0;
    protected int runs = 0;
    
    public AnywhereCoreBlocking(DirectBlockingChecker directBlockingChecker, Map<AtomicConcept, Set<Concept>> blockRelUnary, Map<Set<AtomicConcept>, Set<Concept>> blockRelNary) {
        m_directBlockingChecker=directBlockingChecker;
        m_blockRelUnary = blockRelUnary;
        m_blockRelNAry = blockRelNary;
        m_currentBlockersCache=new BlockersCache(m_directBlockingChecker); // contains all nodes that block some node
        m_atomicConceptsSetFactory=new SetFactory<AtomicConcept>();
        m_atomicConceptsBuffer=new ArrayList<AtomicConcept>();
        m_auxiliaryTuple=new Object[2];
    }
    public void initialize(Tableau tableau) {
        numBlockingComputed = 0;
        m_tableau=tableau;
        m_directBlockingChecker.initialize(tableau);
        m_extensionManager=m_tableau.getExtensionManager();
        m_ternaryTableSearchAllBound=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,true },ExtensionTable.View.TOTAL);
    }
    public void clear() {
        m_currentBlockersCache.clear();
        m_firstChangedNode=null;
    }
    public void computeBlocking(boolean finalChance) {
        computePreBlocking();
        if (finalChance) {
            validateBlocks();
        }
    }
    protected void computePreBlocking() {
        numBlockingComputed++;
        if (numBlockingComputed > 2000) {
            printStatistics(true);
        }
        if (m_firstChangedNode!=null) {
            Node node=m_firstChangedNode;
            while (node!=null) {
                m_currentBlockersCache.removeNode(node);
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
                            Node blocker=m_currentBlockersCache.getBlocker(node); 
                            // note that we removed only nodes from the cache that are of order higher than the first changed element
                            // only nodes of lower order than this one can be blockers and all those have already been looked at in this computation or 
                            // were not removed from the cache since they did not change
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
    protected void validateBlocks() {
        // check if some extra constraints for the parent of the blocked and the blocking node were given
        if (!m_blockRelUnary.isEmpty() || !m_blockRelNAry.isEmpty()) {
            // go through all nodes and not just the ones modified in the last run
            System.out.println("Validate blocks...");
            int checkedBlocks = 0;
            int invalidBlocks = 0;
            Node node = m_tableau.getFirstTableauNode();
            while (node!=null) {
                if (node.isActive() && node.isBlocked()) {
                    // check whether the block is a correct one
                    if (node.isDirectlyBlocked()) {
                        checkedBlocks++;
                        if (!isValidBlock(node.getBlocker(), node.getBlocker().getParent(), node, node.getParent())) {
                            System.out.println("Node " + node.getBlocker().getNodeID() + " invalidly blocks " + node.getNodeID() + "!");
                            invalidBlocks++;
                            node.setBlocked(null,false);
                            // should I put it into the cache? maybe?
                            //m_currentBlockersCache.addNode(node); // node is no longer blocked, so can block others
                        }
                    } else if (!node.getParent().isBlocked()) {
                        // indirectly blocked since we proceed in creation order, 
                        // parent has already been checked for proper blocking
                        // if the parent is no longer blocked, unblock this one too
                        node.setBlocked(null,false);
                        // should I put it into the cache? maybe?
                        //m_currentBlockersCache.addNode(node); // node is no longer blocked, so can block others
                    }
                }
                node=node.getNextTableauNode();
            }
            m_firstChangedNode=null;
            System.out.println("Checked " + checkedBlocks + " directly blocked nodes of which " + invalidBlocks + " were invalid.");
        }
    }
    protected boolean isValidBlock(Node a, Node ap, Node b, Node bp) {
        Set<AtomicConcept> aConcepts = ((SingleBlockingObject)a.getBlockingSignature()).getAtomicConceptsLabel();
        Set<AtomicConcept> apConcepts = ((SingleBlockingObject)ap.getBlockingSignature()).getAtomicConceptsLabel();
        Set<AtomicConcept> bConcepts = ((SingleBlockingObject)b.getBlockingSignature()).getAtomicConceptsLabel();
        Set<AtomicConcept> bpConcepts = ((SingleBlockingObject)bp.getBlockingSignature()).getAtomicConceptsLabel();
        // we have that a (pre-)blocks b and we have to validate whether the block is valid (we can create a model from the block by unravelling) 
        // ap and bp are the parents of a and b respectively
        // check whether min/max cardinalities of the parent of the blocked node could be violated
        // universals and existential have been converted to min/max restrictions for convenience
        for (AtomicConcept c : bpConcepts) {
            if (m_blockRelUnary.containsKey(c) && (!isBlockedParentSuitable(m_blockRelUnary.get(c), b, bp, aConcepts, apConcepts, bConcepts, bpConcepts) || !isBlockerSuitable(m_blockRelUnary.get(c), a, ap, b, bp, aConcepts, apConcepts, bConcepts, bpConcepts))) 
                return false;
        }
        // repeat the same checks for non-unary premises (less efficient matching operations)
        for (Set<AtomicConcept> premises : m_blockRelNAry.keySet()) {
            if (bpConcepts.containsAll(premises) && (!isBlockedParentSuitable(m_blockRelNAry.get(premises), b, bp, bConcepts, bpConcepts, aConcepts, apConcepts) || !isBlockerSuitable(m_blockRelNAry.get(premises), a, ap, b, bp, aConcepts, apConcepts, bConcepts, bpConcepts))) 
                return false;
        }
        return true;
    }
    protected boolean isBlockedParentSuitable(Set<Concept> conclusions, Node b, Node bp, Set<AtomicConcept> aConcepts, Set<AtomicConcept> apConcepts, Set<AtomicConcept> bConcepts, Set<AtomicConcept> bpConcepts) {
        for (Concept c : conclusions) {
            boolean disjunctSatisfied = true;
            if (c instanceof AtLeastConcept) {
                // (>= n r.B) must hold at bp, therefore, ar(r, bp, b) and B(b) in ABox implies B(a) in ABox
                // to avoid table lookup check: B(b) and not B(a) implies not ar(r, bp, b)
                AtLeastConcept atLeast = (AtLeastConcept) c;
                Role r = atLeast.getOnRole();
                LiteralConcept filler = atLeast.getToConcept();
                // does B(b) hold? -> is B atomic and in label of b or is B a negated atomic concept and in the label of b
                if (isInABox(filler, bConcepts) && !isInABox(filler, aConcepts) && isInABox(r, bp, b)) 
                    disjunctSatisfied = false;
            } else if (c instanceof AtMostConcept) {
                // (<= n r.B) must hold at bp, therefore, ar(r, bp, b) in ABox and B(b) not in ABox implies B(a) not in ABox
                // to avoid table lookup, we check: not B(b) and B(a) implies not ar(r, bp, b)
                AtMostConcept atMost = (AtMostConcept)c;
                Role r = atMost.getOnRole();
                LiteralConcept filler = atMost.getToConcept();
                if (!isInABox(filler, bConcepts) && isInABox(filler, aConcepts) && isInABox(r, bp, b)) 
                        disjunctSatisfied = false;
            } else if (c instanceof AtomicConcept && !isInABox((AtomicConcept) c, aConcepts)) {
                // must be an atomic concept or normal form is violated
                disjunctSatisfied = false;
            } else {
                throw new IllegalStateException("Concepts in the conclusion of core blocking constraints are supposed to be atomic classes, at least or at most constraints, but this class is an instance of " + c.getClass().getSimpleName());
            }
            if (disjunctSatisfied) return true;
        }
        return false;
    }
    protected boolean isBlockerSuitable(Set<Concept> conclusions, Node a, Node ap, Node b, Node bp, Set<AtomicConcept> aConcepts, Set<AtomicConcept> apConcepts, Set<AtomicConcept> bConcepts, Set<AtomicConcept> bpConcepts) {
        for (Concept c : conclusions) {
            boolean disjunctSatisfied = true;
            if (c instanceof AtLeastConcept) {
                // (>= n r.B)(a) in the ABox, so in the model construction, (>= n r.B) will be copied to b, 
                // so we have to make sure that it will be satisfied at b
                // check B(ap) and ar(r, a, ap) in ABox implies B(bp) and ar(r, b, bp) in ABox
                AtLeastConcept atLeast = (AtLeastConcept) c;
                Role r = atLeast.getOnRole();
                LiteralConcept filler = atLeast.getToConcept();
                if (isInABox(filler, apConcepts) && isInABox(r, a, ap) && (!isInABox(filler, bpConcepts) || !isInABox(r, b, bp))) 
                    disjunctSatisfied = false;
            } else if (c instanceof AtMostConcept) {
                // (<= n r.B)(a) is in the ABox and in the model construction (<= n r.B) will be copied to b,  
                // so we have to make sure that it will be satisfied at b
                // r(b, bp) and B(bp) -> r(a, ap) and B(ap)
                // to avoid table lookup, check
                // not B(ap) and B(bp) -> not r(b, bp) or r(a, ap)
                AtMostConcept atMost = (AtMostConcept) c;
                Role r = atMost.getOnRole();
                LiteralConcept filler = atMost.getToConcept();
                if (isInABox(filler, bpConcepts) && isInABox(r, b, bp) && (!isInABox(filler, apConcepts) || !isInABox(r, a, ap))) 
                    disjunctSatisfied = false; 
            } else if (c instanceof AtomicConcept && !isInABox((AtomicConcept) c, aConcepts)) 
                    disjunctSatisfied = false;
            else {
                throw new IllegalStateException("Concepts in the conclusion of core blocking constraints are supposed to be atomic classes, at least or at most constraints, but this class is an instance of " + c.getClass().getSimpleName());
            }
            if (disjunctSatisfied) return true;
        }
        return false;
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
        return (!m_ternaryTableSearchAllBound.afterLast());
    }
    protected boolean isInABox(LiteralConcept c, Set<AtomicConcept> nodeLabel) {
        return (c instanceof AtomicConcept && nodeLabel.contains((AtomicConcept) c) 
                || c instanceof AtomicNegationConcept && !nodeLabel.contains((AtomicConcept) c));
    }
    public boolean isPermanentAssertion(Concept concept,Node node) {
        m_auxiliaryTuple[0]=concept;
        m_auxiliaryTuple[1]=node;
        return m_extensionManager.isCore(m_auxiliaryTuple);
    }
    // Assertions can be added directly into the core, but we also have the possibility of setting the core flag later?
    // In that case, assertionCoreSet (below) will be called?
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        if (isCore && concept instanceof AtomicConcept) {
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
        m_currentBlockersCache.removeNode(node);
        m_directBlockingChecker.nodeDestroyed(node);
        if (m_firstChangedNode!=null && m_firstChangedNode.getNodeID()>=node.getNodeID())
            m_firstChangedNode=null;
    }
    public void modelFound() {
        printStatistics(false);
    }
    protected void printStatistics(boolean intermediate) {
        if (!intermediate) runs++;
        int numNodesThisRun = m_tableau.getNumberOfNodesInTableau(); // I hope these are the active ones only, but only Boris knows...
        if (numNodesThisRun > maxNodes) maxNodes = numNodesThisRun;
        sumNodes += numNodesThisRun;
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
        if (!intermediate && maxCoreSizeThisRun > maxCore) maxCore = maxCoreSizeThisRun;
        if (!intermediate && maxLabelSizeThisRun > maxLabel) maxLabel = maxLabelSizeThisRun;
        avgCoreSizeThisRun = (double)sumCoreSizeThisRun / numNodesThisRun;
        avgLabelSizeThisRun = (double)sumLabelSizeThisRun / numNodesThisRun;
        avgCore += avgCoreSizeThisRun;
        avgLabel += avgLabelSizeThisRun;
        if (runs % 20 == 1 && !intermediate) {
            System.out.printf("%n%-55s %-44s%n", "This run:", "All runs:");
            System.out.printf("%-8s%-8s%-8s%-8s%-8s%-8s%-3s%-8s%-8s%-8s%-8s%-8s%-8s%-8s%n", "No", "Nodes", "avg", "avg", "max", "max", "|", "sum", "max", "avg", "avg", "avg", "max", "max");
            System.out.printf("%-8s%-8s%-8s%-8s%-8s%-8s%-3s%-8s%-8s%-8s%-8s%-8s%-8s%-8s%n", "", "", "lab", "core", "lab", "core", "|", "node", "node", "node", "lab", "core", "lab", "core");
        }
        System.out.printf("%-8s%-8s%-8s%-8s%-8s%-8s%-3s", runs, numNodesThisRun, sd(avgLabelSizeThisRun), sd(avgCoreSizeThisRun), maxLabelSizeThisRun, maxCoreSizeThisRun, "|");
        if (!intermediate) {
            System.out.printf("%-8s%-8s%-8s%-8s%-8s%-8s%-8s", sumNodes, maxNodes, sd(((double)sumNodes/runs)), sd(((double)avgLabel/runs)), sd(((double)avgCore/runs)), maxLabel, maxCore);
        }
        System.out.printf("%n");
        numBlockingComputed = 0;
    }
    protected String sd(double d) {
        return new DecimalFormat("#.##").format(d);
    }
    public boolean isExact() {
        return false;
    }
    public void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,Object[] valuesBuffer,boolean[] coreVariables) {
        workers.add(new ComputeCoreVariables(valuesBuffer,coreVariables));
    }
    protected static final class ComputeCoreVariables implements DLClauseEvaluator.Worker,Serializable {
        private static final long serialVersionUID=899293772370136783L;

        protected final Object[] m_valuesBuffer;
        protected final boolean[] m_coreVariables;

        public ComputeCoreVariables(Object[] valuesBuffer,boolean[] coreVariables) {
            m_valuesBuffer=valuesBuffer;
            m_coreVariables=coreVariables;
        }
        public int execute(int programCounter) {
            Node potentialNoncore=null;
            int potentialNoncoreIndex=-1;
            for (int variableIndex=m_coreVariables.length-1;variableIndex>=0;--variableIndex) {
                m_coreVariables[variableIndex]=true;
                Node node=(Node)m_valuesBuffer[variableIndex];
                if (node.getNodeType()==NodeType.TREE_NODE && (potentialNoncore==null || node.getTreeDepth()<potentialNoncore.getTreeDepth())) {
                    potentialNoncore=node;
                    potentialNoncoreIndex=variableIndex;
                }
            }
            if (potentialNoncore!=null) {
                boolean isNoncore=true;
                for (int variableIndex=m_coreVariables.length-1;isNoncore && variableIndex>=0;--variableIndex) {
                    Node node=(Node)m_valuesBuffer[variableIndex];
                    if (!node.isRootNode() && potentialNoncore!=node && !potentialNoncore.isAncestorOf(node))
                        isNoncore=false;
                }
                if (isNoncore) {
                    m_coreVariables[potentialNoncoreIndex]=false;
                }
            }
            return programCounter+1;
        }
        public String toString() {
            return "Compute core variables";
        }
    }
}