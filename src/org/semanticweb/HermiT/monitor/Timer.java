// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.monitor;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class Timer extends TableauMonitorAdapter {
    private static final long serialVersionUID=-8144444618897251350L;

    protected long m_problemStartTime;
    protected long m_lastStatusTime;
    protected int m_numberOfBacktrackings;
    
    public Timer() {
    }
    protected void start() {
        m_numberOfBacktrackings=0;
        m_problemStartTime=System.currentTimeMillis();
        m_lastStatusTime=m_problemStartTime;
    }
    public void isSatisfiableStarted(AtomicConcept atomicConcept) {
        System.out.print("Testing "+atomicConcept.getURI()+" ...");
        System.out.flush();
        start();
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        System.out.println(result ? "YES" : "NO");
        doStatistics();
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
        System.out.print("Testing "+subconcept.getURI()+" ==> "+superconcept.getURI()+" ...");
        System.out.flush();
        start();
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
        System.out.println(result ? "YES" : "NO");
        doStatistics();
    }
    public void isABoxSatisfiableStarted() {
        System.out.print("Testing ABox satisfiability ...");
        System.out.flush();
        start();
    }
    public void isABoxSatisfiableFinished(boolean result) {
        System.out.println(result ? "YES" : "NO");
        doStatistics();
    }
    public void iterationStarted() {
        if (System.currentTimeMillis()-m_lastStatusTime>30000) {
            if (m_lastStatusTime==m_problemStartTime)
                System.out.println();
            doStatistics();
            m_lastStatusTime=System.currentTimeMillis();
        }
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_numberOfBacktrackings++;
    }
    protected void doStatistics() {
        long duartionSoFar=System.currentTimeMillis()-m_problemStartTime;
        System.out.print(duartionSoFar);
        System.out.print(" ms: allocated nodes: ");
        System.out.print(m_tableau.getNumberOfAllocatedNodes());
        System.out.print("    used nodes: ");
        System.out.print(m_tableau.getNumberOfNodeCreations());
        System.out.print("    in tableau: ");
        System.out.print(m_tableau.getNumberOfNodesInTableau());
        if (m_tableau.getNumberOfMergedOrPrunedNodes()>0) {
            System.out.print("    merged/pruned: ");
            System.out.print(m_tableau.getNumberOfMergedOrPrunedNodes());
        }
        System.out.print("    branching point: ");
        System.out.print(m_tableau.getCurrentBranchingPointLevel());
        if (m_numberOfBacktrackings>0) {
            System.out.print("    backtrackings: ");
            System.out.print(m_numberOfBacktrackings);
        }
        System.out.println();
        System.out.print("    Binary table size:   ");
        System.out.print(m_tableau.getExtensionManager().getBinaryExtensionTable().sizeInMemory()/1000);
        System.out.print("kb    Ternary table size: ");
        System.out.print(m_tableau.getExtensionManager().getTernaryExtensionTable().sizeInMemory()/1000);
        System.out.print("kb    Dependency set factory size: ");
        System.out.print(m_tableau.getDependencySetFactory().sizeInMemory()/1000);
        System.out.println("kb");
        System.out.print("    Concept factory size: ");
        System.out.print(m_tableau.getLabelManager().sizeInMemoryConceptSetFactory()/1000);
        System.out.print("kb    Atomic role factory size: ");
        System.out.print(m_tableau.getLabelManager().sizeInMemoryAtomicAbstractRoleSetFactory()/1000);
        System.out.println("kb");
    }
}
