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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.tableau.BranchingPoint;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription.StandardTestType;

public class CountingMonitor extends TableauMonitorAdapter {
    private static final long serialVersionUID=-8144444618897251350L;

    protected long m_problemStartTime;
    protected long m_validationStartTime;
    protected int m_testNo=0;
    // current test
    protected long m_time;
    protected int m_numberOfBacktrackings;
    protected int m_numberOfNodes;
    protected int m_numberOfBlockedNodes;
    protected ReasoningTaskDescription m_reasoningTaskDescription;
    protected boolean m_testResult;
    // validated blocking
    protected int m_initialModelSize;
    protected int m_initiallyBlocked;
    protected int m_initiallyInvalid;
    protected int m_noValidations;
    protected long m_validationTime;

    // overall numbers
    protected final Map<String,List<TestRecord>> m_testRecords=new HashMap<String, List<TestRecord>>();
    protected long m_overallTime=0;
    protected int m_overallNumberOfBacktrackings=0;
    protected int m_overallNumberOfNodes=0;
    protected int m_overallNumberOfBlockedNodes=0;
    protected int m_overallNumberOfTests=0;
    protected int m_overallNumberOfClashes=0;
    protected int m_possibleInstancesTested=0;
    protected int m_possibleInstancesInstances=0;
    
    // validated blocking
    protected int m_overallInitialModelSize=0;
    protected int m_overallInitiallyBlocked=0;
    protected int m_overallInitiallyInvalid=0;
    protected int m_overallNoValidations=0;
    protected long m_overallValidationTime=0;


    public void reset() {
        m_problemStartTime=0;
        m_validationStartTime=0;
        m_time=0;
        m_numberOfBacktrackings=0;
        m_numberOfNodes=0;
        m_numberOfBlockedNodes=0;
        m_reasoningTaskDescription=null;
        m_testResult=false;
        m_initialModelSize=0;
        m_initiallyBlocked=0;
        m_initiallyInvalid=0;
        m_noValidations=0;
        m_validationTime=0;
        m_testRecords.clear();
        m_overallTime=0;
        m_overallNumberOfBacktrackings=0;
        m_overallNumberOfNodes=0;
        m_overallNumberOfBlockedNodes=0;
        m_overallNumberOfTests=0;
        m_overallNumberOfClashes=0;
        m_possibleInstancesTested=0;
        m_possibleInstancesInstances=0;
        m_overallInitialModelSize=0;
        m_overallInitiallyBlocked=0;
        m_overallInitiallyInvalid=0;
        m_overallNoValidations=0;
        m_overallValidationTime=0;
    }

    public void isSatisfiableStarted(ReasoningTaskDescription reasoningTaskDescription) {
        super.isSatisfiableStarted(reasoningTaskDescription);
        m_testNo++;
        m_reasoningTaskDescription=reasoningTaskDescription;
        m_overallNumberOfTests++;
        m_problemStartTime=System.currentTimeMillis();
        m_numberOfBacktrackings=0;
        m_numberOfNodes=0;
        m_numberOfBlockedNodes=0;
        m_initialModelSize=0;
        m_initiallyBlocked=0;
        m_initiallyInvalid=0;
        m_noValidations=0;
        m_validationTime=0;
    }
    public void isSatisfiableFinished(ReasoningTaskDescription reasoningTaskDescription,boolean result) {
        super.isSatisfiableFinished(reasoningTaskDescription,result);
        if (reasoningTaskDescription.flipSatisfiabilityResult())
            result=!result;
        m_testResult=result;
        m_time=System.currentTimeMillis()-m_problemStartTime;
        String messagePattern=m_reasoningTaskDescription.getMessagePattern();
        List<TestRecord> records=m_testRecords.get(messagePattern);
        if (records==null) {
            records=new ArrayList<TestRecord>();
            m_testRecords.put(messagePattern, records);
        }
        records.add(new TestRecord(m_time, m_reasoningTaskDescription.getTaskDescription(Prefixes.STANDARD_PREFIXES), m_testResult));
        m_overallTime+=m_time;
        m_overallNumberOfBacktrackings+=m_numberOfBacktrackings;
        m_numberOfNodes=m_tableau.getNumberOfNodesInTableau()-m_tableau.getNumberOfMergedOrPrunedNodes();
        Node node;
        node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (node.isActive() && node.isBlocked() && node.hasUnprocessedExistentials())
                m_numberOfBlockedNodes++;
            node=node.getNextTableauNode();
        }
        m_overallNumberOfNodes+=m_numberOfNodes;
        m_overallNumberOfBlockedNodes+=m_numberOfBlockedNodes;
        m_overallInitialModelSize+=m_initialModelSize;
        m_overallInitiallyBlocked+=m_initiallyBlocked;
        m_overallInitiallyInvalid+=m_initiallyInvalid;
        m_overallNoValidations+=m_noValidations;
        m_overallValidationTime+=m_validationTime;
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_numberOfBacktrackings++;
    }
    public void possibleInstanceIsInstance() {
        m_possibleInstancesTested++;
        m_possibleInstancesInstances++;
    }
    public void possibleInstanceIsNotInstance() {
        m_possibleInstancesTested++;
    }
    public void blockingValidationStarted() {
    	m_noValidations++;
    	Node node;
    	if (m_noValidations==1) {
            node=m_tableau.getFirstTableauNode();
            while (node!=null) {
                if (node.isActive()) {
                    m_initialModelSize++;
                    if (node.isBlocked() && node.hasUnprocessedExistentials()) {
                        m_initiallyBlocked++;
                    }
                }
                node=node.getNextTableauNode();
            }
    	}
    	m_validationStartTime=System.currentTimeMillis();
    }
    public void blockingValidationFinished(int noInvalidlyBlocked) {
    	m_validationTime+=(System.currentTimeMillis()-m_validationStartTime);
    	if (m_noValidations==1)
    	    m_initiallyInvalid=noInvalidlyBlocked;
    }
    // getters for test records
    public Set<String> getUsedMessagePatterns() {
        return m_testRecords.keySet();
    }
    public List<TestRecord> getTimeSortedTestRecords(int limit) {
        return getTimeSortedTestRecords(limit,(String)null);
    }
    public List<TestRecord> getTimeSortedTestRecords(int limit, StandardTestType standardTestType) {
        return getTimeSortedTestRecords(limit, standardTestType.messagePattern);
    }
    public List<TestRecord> getTimeSortedTestRecords(int limit, String messagePattern) {
        List<TestRecord> filteredRecords=new ArrayList<TestRecord>();;
        if (messagePattern==null) {
            for (List<TestRecord> records : m_testRecords.values())
                filteredRecords.addAll(records);
        }
        else
            filteredRecords=m_testRecords.get(messagePattern);
        Collections.sort(filteredRecords);
        if (limit>filteredRecords.size()) limit=filteredRecords.size();
        return filteredRecords.subList(0, limit);
    }
    // getters for current test measurements
    public long getTime() {
        return m_time;
    }
	public int getNumberOfBacktrackings() {
		return m_numberOfBacktrackings;
	}
	public int getNumberOfNodes() {
		return m_numberOfNodes;
	}
	public int getNumberOfBlockedNodes() {
        return m_numberOfBlockedNodes;
    }
	public String getTestDescription() {
	    return m_reasoningTaskDescription.getTaskDescription(Prefixes.STANDARD_PREFIXES);
	}
	public boolean getTestResult() {
        return m_testResult;
    }
	// getters for current test blocking validation measurements
	public int getInitialModelSize() {
        return m_initialModelSize;
    }
    public int getInitiallyBlocked() {
        return m_initiallyBlocked;
    }
    public int getInitiallyInvalid() {
        return m_initiallyInvalid;
    }
    public int getNoValidations() {
        return m_noValidations;
    }
	public long getValidationTime() {
		return m_validationTime;
	}

	// getters for overall measurements
	public long getOverallTime() {
		return m_overallTime;
	}
	public int getOverallNumberOfBacktrackings() {
		return m_overallNumberOfBacktrackings;
	}
	public int getOverallNumberOfNodes() {
		return m_overallNumberOfNodes;
	}
	public int getOverallNumberOfBlockedNodes() {
        return m_overallNumberOfBlockedNodes;
    }
	public int getOverallNumberOfTests() {
		return m_overallNumberOfTests;
	}
	public int getOverallNumberOfTests(StandardTestType testType) {
		return m_testRecords.containsKey(testType.messagePattern) ? m_testRecords.get(testType.messagePattern).size() : 0;
	}
	public int getOverallNumberOfClashes() {
		return m_overallNumberOfClashes;
	}
	public int getNumberOfPossibleInstancesTested() {
        return m_possibleInstancesTested;
    }
	public int getNumberOfPossibleInstancesInstances() {
        return m_possibleInstancesInstances;
    }
	// getters for overall blocking validation measurements
    public int getOverallInitialModelSize() {
        return m_overallInitialModelSize;
    }
    public int getOverallInitiallyBlocked() {
        return m_overallInitiallyBlocked;
    }
    public int getOverallInitiallyInvalid() {
        return m_overallInitiallyInvalid;
    }
	public int getOverallNoValidations() {
		return m_overallNoValidations;
	}
    public long getOverallValidationTime() {
        return m_overallValidationTime;
    }

    // getters for average measurements
    public long getAverageTime() {
        if (m_testNo==0)
            return m_testNo;
        return m_overallTime/m_testNo;
    }
    public double getAverageNumberOfBacktrackings() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallNumberOfBacktrackings, m_testNo);
    }
    protected double getRounded(long nominator, long denominator) {
        return getRounded(nominator, denominator, 2);
    }
    protected double getRounded(long nominator, long denominator, int noDecimalPlaces) {
        double number=(double)(nominator)/(double)denominator;
        int tmp=(int)((number*Math.pow(10,noDecimalPlaces)));
        return (tmp/Math.pow(10,noDecimalPlaces));
    }
    public double getAverageNumberOfNodes() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallNumberOfNodes, m_testNo);
    }
    public double getAverageNumberOfBlockedNodes() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallNumberOfBlockedNodes, m_testNo);
    }
    public double getAverageNumberOfClashes() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallNumberOfClashes, m_testNo);
    }
    public double getPossiblesToInstances() {
        if (m_possibleInstancesTested==0)
            return 0;
        return getRounded(m_possibleInstancesInstances,m_possibleInstancesTested);
    }
    // getters for average blocking validation measurements
    public double getAverageInitialModelSize() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallInitialModelSize, m_testNo);
    }
    public double getAverageInitiallyBlocked() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallInitiallyBlocked, m_testNo);
    }
    public double getAverageInitiallyInvalid() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallInitiallyInvalid, m_testNo);
    }
    public double getAverageNoValidations() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallNoValidations, m_testNo);
    }
    public long getAverageValidationTime() {
        if (m_testNo==0)
            return m_testNo;
        return m_overallValidationTime/m_testNo;
    }

    public static String millisToHoursMinutesSecondsString(long millis) {
        long time=millis/1000;
        long ms=time%1000;
        String timeStr=String.format(String.format("%%0%dd", 3), ms)+"ms";
        String format=String.format("%%0%dd", 2);
        long secs=time%60;
        if (secs>0) timeStr=String.format(format, secs)+"s"+timeStr;
        long mins=(time%3600)/60;
        if (mins>0) timeStr=String.format(format, mins)+"m"+timeStr;
        long hours=time/3600;
        if (hours>0) timeStr=String.format(format, hours)+"h"+timeStr;
        return timeStr;
    }

	public static class TestRecord implements Comparable<TestRecord>, Serializable {
        private static final long serialVersionUID = -3815493500625020183L;
	    protected final long m_testTime;
	    protected final String m_testDescription;
	    protected final boolean m_testResult;

	    public TestRecord(long testTime, String testDescription, boolean result) {
	        m_testTime=testTime;
	        m_testDescription=testDescription;
	        m_testResult=result;
	    }
        public int compareTo(TestRecord that) {
            if (this==that) return 0;
            int result=((Long)that.m_testTime).compareTo(m_testTime);
            if (result!=0) return result;
            else return this.m_testDescription.compareToIgnoreCase(that.m_testDescription);
        }
        public long getTestTime() {
            return m_testTime;
        }
        public String getTestDescription() {
            return m_testDescription;
        }
        public boolean getTestResult() {
            return m_testResult;
        }
        public String toString() {
            return m_testTime+" ms"+(m_testTime>1000?" ("+CountingMonitor.millisToHoursMinutesSecondsString(m_testTime)+")":"")+" for "+m_testDescription+" (result: "+m_testResult+")";
        }
	}
}