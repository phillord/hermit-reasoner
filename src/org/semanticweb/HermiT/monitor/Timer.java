/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory
   
   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
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
    protected int m_numberOfSatTests=0;
    
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
        m_output.print("Testing "+atomicConcept.getIRI()+" ...");
        m_output.flush();
        start();
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        m_output.println(result ? "YES" : "NO");
        doStatistics();
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
        m_output.print("Testing "+subconcept.getIRI()+" ==> "+superconcept.getIRI()+" ...");
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
        m_output.print("Testing "+concept.getIRI()+" : "+individual.getIRI()+" ...");
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
    public void saturateStarted() {
        m_numberOfSatTests++;
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_numberOfBacktrackings++;
    }
    protected void doStatistics() {
        long duartionSoFar=System.currentTimeMillis()-m_problemStartTime;
        m_output.print(duartionSoFar);
        m_output.print(" ms   sat test no: "+m_numberOfSatTests);
        m_output.print("    allocated nodes: ");
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
