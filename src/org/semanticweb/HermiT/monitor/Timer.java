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
    protected void printTime(long duration) {
        System.out.print("    Number of created nodes: ");
        System.out.print(m_tableau.getNumberOfCreatedNodes());
        System.out.print("    Overall time: ");
        System.out.print(duration);
        System.out.print(" ms");
        if (m_numberOfBacktrackings>0) {
            System.out.print("    Total backtrackings: ");
            System.out.print(m_numberOfBacktrackings);
        }
        System.out.println();
    }
    public void isSatisfiableStarted(AtomicConcept atomicConcept) {
        System.out.print("Testing "+atomicConcept.getURI()+" ...");
        System.out.flush();
        start();
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        long duration=System.currentTimeMillis()-m_problemStartTime;
        System.out.println(result ? "YES" : "NO");
        printTime(duration);
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
        System.out.print("Testing "+subconcept.getURI()+" ==> "+superconcept.getURI()+" ...");
        System.out.flush();
        start();
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
        long duration=System.currentTimeMillis()-m_problemStartTime;
        System.out.println(result ? "YES" : "NO");
        printTime(duration);
    }
    public void isABoxSatisfiableStarted() {
        System.out.print("Testing ABox satisfiability ...");
        System.out.flush();
        start();
    }
    public void isABoxSatisfiableFinished(boolean result) {
        long duration=System.currentTimeMillis()-m_problemStartTime;
        System.out.println(result ? "YES" : "NO");
        printTime(duration);
    }
    public void iterationStarted() {
        long current=System.currentTimeMillis();
        if (current-m_lastStatusTime>30000) {
            if (m_lastStatusTime==m_problemStartTime)
                System.out.println();
            long duartionSoFar=current-m_problemStartTime;
            System.out.print(duartionSoFar);
            System.out.print(" ms: allocated nodes nodes: ");
            System.out.print(m_tableau.getNumberOfAllocatedNodes());
            System.out.print("    created nodes: ");
            System.out.print(m_tableau.getNumberOfCreatedNodes());
            System.out.print("    nodes in tableau: ");
            System.out.print(m_tableau.getNumberOfNodesInTableau());
            if (m_tableau.getNumberOfMergedOrPrunedNodes()>0) {
                System.out.print("    merged or pruned nodes: ");
                System.out.print(m_tableau.getNumberOfMergedOrPrunedNodes());
            }
            System.out.print("    current branching point: ");
            System.out.print(m_tableau.getCurrentBranchingPoint().getLevel());
            if (m_numberOfBacktrackings>0) {
                System.out.print("    backtrackings so far: ");
                System.out.print(m_numberOfBacktrackings);
            }
            System.out.println();
            System.out.print("    Binary table size:   ");
            System.out.print(m_tableau.getExtensionManager().getBinaryExtensionTable().size()/1000);
            System.out.print("kb    Ternary table size: ");
            System.out.print(m_tableau.getExtensionManager().getTernaryExtensionTable().size()/1000);
            System.out.print("kb    Dependency set factory size: ");
            System.out.print(m_tableau.getDependencySetFactory().size()/1000);
            System.out.println("kb");
            System.out.print("    Concept set factory: ");
            System.out.print(m_tableau.getConceptSetFactory().size()/1000);
            System.out.print("kb    Role set factory:   ");
            System.out.print(m_tableau.getAtomicAbstractRoleSetFactory().size()/1000);
            System.out.print("kb    Existential set factory:   ");
            System.out.print(m_tableau.getExistentialConceptSetFactory().size()/1000);
            System.out.println("kb");
            m_lastStatusTime=System.currentTimeMillis();
        }
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_numberOfBacktrackings++;
    }
}
