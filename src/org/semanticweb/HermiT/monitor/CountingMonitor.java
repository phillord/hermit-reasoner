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

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.tableau.BranchingPoint;

public class CountingMonitor extends TableauMonitorAdapter {
    private static final long serialVersionUID=-8144444618897251350L;

    protected long m_problemStartTime;
    protected long m_validationStartTime;
    
    // current test
    protected long m_time;
    protected long m_validationTime;
    protected int m_numberOfBacktrackings;
    protected int m_numberOfNodes;
    protected int m_numberOfBlockingValidations;
    
    // overall numbers
    protected long m_overallTime=0;
    protected long m_overallValidationTime=0;
    protected int m_overallNumberOfBacktrackings=0;
    protected int m_overallNumberOfNodes=0;
    protected int m_overallNumberOfTests=0;
    protected int m_overallNumberOfSatTests=0;
    protected int m_overallNumberOfSubsumptionTests=0;
    protected int m_overallNumberOfABoxSatTests=0;
    protected int m_overallNumberOfInstanceOfTests=0;
    protected int m_overallNumberOfClashes=0;
    protected int m_overallNumberOfBlockingValidations=0;
    
    protected void start() {
    	m_overallNumberOfTests++;
        m_numberOfBacktrackings=0;
        m_problemStartTime=System.currentTimeMillis();
        m_validationTime=0;
        m_numberOfNodes=0;
        m_numberOfBlockingValidations=0;
    }
    public void isSatisfiableStarted(AtomicConcept atomicConcept) {
    	m_overallNumberOfSatTests++;
        start();
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        doStatistics();
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
    	m_overallNumberOfSubsumptionTests++;
        start();
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
        doStatistics();
    }
    public void isABoxSatisfiableStarted() {
    	m_overallNumberOfABoxSatTests++;
        start();
    }
    public void isABoxSatisfiableFinished(boolean result) {
        doStatistics();
    }
    public void isInstanceOfStarted(AtomicConcept concept,Individual individual) {
    	m_overallNumberOfInstanceOfTests++;
        start();
    }
    public void isInstanceOfFinished(AtomicConcept concept,Individual individual,boolean result) {
        doStatistics();
    }
    public void saturateFinished(boolean modelFound) {
    	if (!modelFound) m_overallNumberOfClashes++;
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_numberOfBacktrackings++;
    }
    public void blockingValidationStarted() {
    	m_numberOfBlockingValidations++;
    	m_validationStartTime=System.currentTimeMillis();
    }
    public void blockingValidationFinished() {
    	m_validationTime+=(System.currentTimeMillis()-m_validationStartTime);
    }
    public void doStatistics() {
        m_time=System.currentTimeMillis()-m_problemStartTime;
        m_overallTime+=m_time;
        m_overallValidationTime+=m_validationTime;
        m_overallNumberOfBlockingValidations+=m_numberOfBlockingValidations;
        m_overallNumberOfBacktrackings+=m_numberOfBacktrackings;
        m_numberOfNodes=m_tableau.getNumberOfNodesInTableau()-m_tableau.getNumberOfMergedOrPrunedNodes();
        m_overallNumberOfNodes+=m_numberOfNodes;
    }
    
	public int getNumberOfBacktrackings() {
		return m_numberOfBacktrackings;
	}
	public int getNumberOfNodes() {
		return m_numberOfNodes;
	}
	public int getNumberOfBlockingValidations() {
		return m_numberOfBlockingValidations;
	}
	public long getTime() {
		return m_time;
	}
	public long getValidationTime() {
		return m_validationTime;
	}
	
	public long getOverallTime() {
		return m_overallTime;
	}
	public long getOverallValidationTime() {
		return m_overallValidationTime;
	}
	public int getOverallNumberOfBacktrackings() {
		return m_overallNumberOfBacktrackings;
	}
	public int getOverallNumberOfNodes() {
		return m_overallNumberOfNodes;
	}
	public int getOverallNumberOfTests() {
		return m_overallNumberOfTests;
	}
	public int getOverallNumberOfSatTests() {
		return m_overallNumberOfSatTests;
	}
	public int getOverallNumberOfSubsumptionTests() {
		return m_overallNumberOfSubsumptionTests;
	}
	public int getOverallNumberOfABoxSatTests() {
		return m_overallNumberOfABoxSatTests;
	}
	public int getOverallNumberOfInstanceOfTests() {
		return m_overallNumberOfInstanceOfTests;
	}
	public int getOverallNumberOfClashes() {
		return m_overallNumberOfClashes;
	}
	public int getOverallNumberOfBlockingValidations() {
		return m_overallNumberOfBlockingValidations;
	}
}
