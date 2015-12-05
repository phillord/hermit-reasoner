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

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.tableau.BranchingPoint;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;

public class Timer extends TableauMonitorAdapter {
    private static final long serialVersionUID=-8144444618897251350L;

    protected transient PrintWriter m_output;
    protected long m_problemStartTime;
    protected long m_lastStatusTime;
    protected int m_numberOfBacktrackings;
    protected int m_testNumber=0;

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
    public void isSatisfiableStarted(ReasoningTaskDescription reasoningTaskDescription) {
        m_output.print(reasoningTaskDescription.getTaskDescription(Prefixes.STANDARD_PREFIXES)+" ...");
        m_output.flush();
        start();
    }
    public void isSatisfiableFinished(ReasoningTaskDescription reasoningTaskDescription,boolean result) {
        if (reasoningTaskDescription.flipSatisfiabilityResult())
            result=!result;
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
        m_testNumber++;
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_numberOfBacktrackings++;
    }
    protected void doStatistics() {
        long duartionSoFar=System.currentTimeMillis()-m_problemStartTime;
        m_output.print("    Test:   ");
        printPadded(m_testNumber,7);
        m_output.print("  Duration:  ");
        printPaddedMS(duartionSoFar,7);
        m_output.print("   Current branching point: ");
        printPadded(m_tableau.getCurrentBranchingPointLevel(),7);
        if (m_numberOfBacktrackings>0) {
            m_output.print("    Backtrackings: ");
            m_output.print(m_numberOfBacktrackings);
        }
        m_output.println();
        m_output.print("    Nodes:  allocated:    ");
        printPadded(m_tableau.getNumberOfAllocatedNodes(),7);
        m_output.print("    used: ");
        printPadded(m_tableau.getNumberOfNodeCreations(),7);
        m_output.print("    in tableau: ");
        printPadded(m_tableau.getNumberOfNodesInTableau(),7);
        if (m_tableau.getNumberOfMergedOrPrunedNodes()>0) {
            m_output.print("    merged/pruned: ");
            m_output.print(m_tableau.getNumberOfMergedOrPrunedNodes());
        }
        m_output.println();
        m_output.print("    Sizes:  binary table: ");
        printPaddedKB(m_tableau.getExtensionManager().getBinaryExtensionTable().sizeInMemory()/1000,7);
        m_output.print("    ternary table: ");
        printPaddedKB(m_tableau.getExtensionManager().getTernaryExtensionTable().sizeInMemory()/1000,7);
        m_output.print("    dependency set factory: ");
        printPaddedKB(m_tableau.getDependencySetFactory().sizeInMemory()/1000,7);
        m_output.println();
        m_output.println();
        m_output.flush();
    }
    protected void printPadded(int number,int padding) {
        String numberString=String.valueOf(number);
        m_output.print(numberString);
        for (int index=numberString.length();index<padding;index++)
            m_output.print(' ');
    }
    protected void printPaddedMS(long number,int padding) {
        String numberString=String.valueOf(number)+" ms";
        m_output.print(numberString);
        for (int index=numberString.length();index<padding;index++)
            m_output.print(' ');
    }
    protected void printPaddedKB(int number,int padding) {
        String numberString=String.valueOf(number)+" kb";
        m_output.print(numberString);
        for (int index=numberString.length();index<padding;index++)
            m_output.print(' ');
    }
}
