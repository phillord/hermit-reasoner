// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.monitor;

import java.io.PrintWriter;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.tableau.BranchingPoint;

public class Timer extends TableauMonitorAdapter {
    private static final long serialVersionUID=-8144444618897251350L;

    protected transient PrintWriter m_output;
    protected long m_problemStartTime;
    protected long m_lastStatusTime;
    protected int m_numberOfBacktrackings;

    public Timer() {
        m_output=new PrintWriter(System.out);
    }
    public Timer(PrintWriter inOutput) {
        m_output=inOutput;
    }
    protected Object readResolve() {
        m_output=new PrintWriter(System.out);
        return this;
    }
    protected void start() {
        m_numberOfBacktrackings=0;
        m_problemStartTime=System.currentTimeMillis();
        m_lastStatusTime=m_problemStartTime;
    }
    public void isSatisfiableStarted(AtomicConcept atomicConcept) {
        m_output.print("Testing "+atomicConcept.getURI()+" ...");
        m_output.flush();
        start();
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        m_output.println(result ? "YES" : "NO");
        doStatistics();
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
        m_output.print("Testing "+subconcept.getURI()+" ==> "+superconcept.getURI()+" ...");
        m_output.flush();
        start();
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
        m_output.println(result ? "YES" : "NO");
        doStatistics();
    }
    public void isABoxSatisfiableStarted() {
        m_output.print("Testing ABox satisfiability ...");
        m_output.flush();
        start();
    }
    public void isABoxSatisfiableFinished(boolean result) {
        m_output.println(result ? "YES" : "NO");
        doStatistics();
    }
    public void isInstanceOfStarted(AtomicConcept concept,Individual individual) {
        m_output.print("Testing "+concept.getURI()+" : "+individual.getURI()+" ...");
        m_output.flush();
        start();
    }
    public void isInstanceOfFinished(AtomicConcept concept,Individual individual,boolean result) {
        m_output.println(result ? "YES" : "NO");
        doStatistics();
    }
    public void iterationStarted() {
        if (System.currentTimeMillis()-m_lastStatusTime>30000) {
            if (m_lastStatusTime==m_problemStartTime)
                m_output.println();
            doStatistics();
            m_lastStatusTime=System.currentTimeMillis();
        }
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_numberOfBacktrackings++;
    }
    protected void doStatistics() {
        long duartionSoFar=System.currentTimeMillis()-m_problemStartTime;
        m_output.print(duartionSoFar);
        m_output.print(" ms: allocated nodes: ");
        m_output.print(m_tableau.getNumberOfAllocatedNodes());
        m_output.print("    used nodes: ");
        m_output.print(m_tableau.getNumberOfNodeCreations());
        m_output.print("    in tableau: ");
        m_output.print(m_tableau.getNumberOfNodesInTableau());
        if (m_tableau.getNumberOfMergedOrPrunedNodes()>0) {
            m_output.print("    merged/pruned: ");
            m_output.print(m_tableau.getNumberOfMergedOrPrunedNodes());
        }
        m_output.print("    branching point: ");
        m_output.print(m_tableau.getCurrentBranchingPointLevel());
        if (m_numberOfBacktrackings>0) {
            m_output.print("    backtrackings: ");
            m_output.print(m_numberOfBacktrackings);
        }
        m_output.println();
        m_output.print("    Binary table size:   ");
        m_output.print(m_tableau.getExtensionManager().getBinaryExtensionTable().sizeInMemory()/1000);
        m_output.print("kb    Ternary table size: ");
        m_output.print(m_tableau.getExtensionManager().getTernaryExtensionTable().sizeInMemory()/1000);
        m_output.print("kb    Dependency set factory size: ");
        m_output.print(m_tableau.getDependencySetFactory().sizeInMemory()/1000);
        m_output.println("kb");
        m_output.println();
        m_output.flush();
    }
}
