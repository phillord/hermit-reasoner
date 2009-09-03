// Copyright 2009 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;

public class UnvalidatedAnywhereCoreBlocking implements BlockingStrategy, Serializable {
    private static final long serialVersionUID=-2959900333817197464L;

    protected Tableau m_tableau;
    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final BlockersCache m_currentBlockersCache;
    protected final BlockingSignatureCache m_blockingSignatureCache;
    protected final Object[] m_auxiliaryTuple;
    protected ExtensionManager m_extensionManager;
    protected Node m_firstChangedNode;
    
    // statistics: 
    protected final boolean printingOn=false;
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
    
    public UnvalidatedAnywhereCoreBlocking(DirectBlockingChecker directBlockingChecker,BlockingSignatureCache blockingSignatureCache) {
        m_directBlockingChecker=directBlockingChecker;
        m_currentBlockersCache=new BlockersCache(m_directBlockingChecker); // contains all nodes that block some node
        m_blockingSignatureCache=blockingSignatureCache;
        m_auxiliaryTuple=new Object[2];
    }
    public void initialize(Tableau tableau) {
        numBlockingComputed = 0;
        m_tableau=tableau;
        m_directBlockingChecker.initialize(tableau);
        m_extensionManager=m_tableau.getExtensionManager();
    }
    public void clear() {
        m_currentBlockersCache.clear();
        m_directBlockingChecker.clear();
        m_firstChangedNode=null;
        numBlockingComputed=0;
        run++;
        if (printingOn) printHeader();
    }
    public void computeBlocking(boolean finalChance) {
        computePreBlocking(null);
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
                        else if (parent.isBlocked()) {// parent is guaranteed not to change it's status in this computation since we process nodes in creation order and parent is smaller
                            if (node.getBlocker() == null) {
                                // previously not blocked
                                numIndirectlyBlocked++;
                            }
                            node.setBlocked(parent,false);
                        } else {
                            Node previousBlocker=node.getBlocker();
                            Node blocker = m_currentBlockersCache.getBlocker(node);
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
                if (nodesToExpand!=null && !node.isBlocked() && node.getUnprocessedExistentials().size()>0)
                    nodesToExpand.add(node);
                node=node.getNextTableauNode();
            }
            //if (printingOn) System.out.println("Num of changed blockers: " + numBlockersChanged);
            if (printingOn) System.out.println("Num nodes/dBlocked/indBlocked: " + (m_tableau.getNumberOfNodesInTableau()-m_tableau.getNumberOfMergedOrPrunedNodes()) + "/" + numDirectlyBlocked + "/" + numIndirectlyBlocked);
            m_firstChangedNode=null;
        }
    }
    public boolean isPermanentAssertion(Concept concept,Node node) {
        m_auxiliaryTuple[0]=concept;
        m_auxiliaryTuple[1]=node;
        return m_extensionManager.isCore(m_auxiliaryTuple);
    }
    // Assertions can be added directly into the core, but we also have the possibility of setting the core flag later?
    // In that case, assertionCoreSet (below) will be called?
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        if (isCore && concept!=AtomicConcept.THING) {
            updateNodeChange(m_directBlockingChecker.assertionAdded(concept,node));
        }
    }
    public void assertionCoreSet(Concept concept,Node node) {
        updateNodeChange(m_directBlockingChecker.assertionAdded(concept,node));
    }
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        if (isCore) {
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
        if (printingOn) printStatistics(false);
        System.out.println("Found model with " + (m_tableau.getNumberOfNodesInTableau()-m_tableau.getNumberOfMergedOrPrunedNodes()) + " nodes. ");
//        Node node=m_tableau.getFirstTableauNode();
//        while (node!=null) {
//            if (node.isActive() && node.isDirectlyBlocked()) 
//                System.out.println("Node " + node.getNodeID() + " is " + (node.isDirectlyBlocked()?"directly":"indirectly") + " blocked by node " + node.getBlocker().getNodeID());
//            node=node.getNextTableauNode();
//        }
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
