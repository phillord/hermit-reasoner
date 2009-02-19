// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.monitor;

import java.io.PrintWriter;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class Timer extends TableauMonitorAdapter {
    private static final long serialVersionUID=-8144444618897251350L;

    protected long m_problemStartTime;
    protected long m_lastStatusTime;
    protected int m_numberOfBacktrackings;
    
    protected PrintWriter output;
    
    public Timer() {
        output = new PrintWriter(System.out);
    }

    public Timer(PrintWriter inOutput) {
        output = inOutput;
    }
    protected void start() {
        m_numberOfBacktrackings=0;
        m_problemStartTime=System.currentTimeMillis();
        m_lastStatusTime=m_problemStartTime;
    }
    public void isSatisfiableStarted(AtomicConcept atomicConcept) {
        output.print("Testing "+atomicConcept.getURI()+" ...");
        output.flush();
        start();
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        output.println(result ? "YES" : "NO");
        doStatistics();
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
        output.print("Testing "+subconcept.getURI()+" ==> "+superconcept.getURI()+" ...");
        output.flush();
        start();
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
        output.println(result ? "YES" : "NO");
        doStatistics();
    }
    public void isABoxSatisfiableStarted() {
        output.print("Testing ABox satisfiability ...");
        output.flush();
        start();
    }
    public void isABoxSatisfiableFinished(boolean result) {
        output.println(result ? "YES" : "NO");
        doStatistics();
    }
    public void iterationStarted() {
        if (System.currentTimeMillis()-m_lastStatusTime>30000) {
            if (m_lastStatusTime==m_problemStartTime)
                output.println();
            doStatistics();
            m_lastStatusTime=System.currentTimeMillis();
        }
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_numberOfBacktrackings++;
    }
    protected void doStatistics() {
        long duartionSoFar=System.currentTimeMillis()-m_problemStartTime;
        output.print(duartionSoFar);
        output.print(" ms: allocated nodes: ");
        output.print(m_tableau.getNumberOfAllocatedNodes());
        output.print("    used nodes: ");
        output.print(m_tableau.getNumberOfNodeCreations());
        output.print("    in tableau: ");
        output.print(m_tableau.getNumberOfNodesInTableau());
        if (m_tableau.getNumberOfMergedOrPrunedNodes()>0) {
            output.print("    merged/pruned: ");
            output.print(m_tableau.getNumberOfMergedOrPrunedNodes());
        }
        output.print("    branching point: ");
        output.print(m_tableau.getCurrentBranchingPointLevel());
        if (m_numberOfBacktrackings>0) {
            output.print("    backtrackings: ");
            output.print(m_numberOfBacktrackings);
        }
        output.println();
        output.print("    Binary table size:   ");
        output.print(m_tableau.getExtensionManager().getBinaryExtensionTable().sizeInMemory()/1000);
        output.print("kb    Ternary table size: ");
        output.print(m_tableau.getExtensionManager().getTernaryExtensionTable().sizeInMemory()/1000);
        output.print("kb    Dependency set factory size: ");
        output.print(m_tableau.getDependencySetFactory().sizeInMemory()/1000);
        output.println("kb");
        output.print("    Concept factory size: ");
        output.print(m_tableau.getLabelManager().sizeInMemoryConceptSetFactory()/1000);
        output.print("kb    Atomic role factory size: ");
        output.print(m_tableau.getLabelManager().sizeInMemoryAtomicRoleSetFactory()/1000);
        output.println("kb");
        output.println();
        output.flush();
    }
}
