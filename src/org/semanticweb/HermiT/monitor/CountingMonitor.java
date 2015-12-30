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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.tableau.BranchingPoint;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
/**Counting monitor.*/
public class CountingMonitor extends TableauMonitorAdapter {
    private static final long serialVersionUID=-8144444618897251350L;

    protected long m_problemStartTime;
    protected long m_validationStartTime;
    protected long m_datatypeCheckingStartTime;
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
    // datatype checking
    protected int m_numberDatatypesChecked;
    protected int m_datatypeCheckingTime;

    // overall numbers
    protected final Map<String,List<TestRecord>> m_testRecords=new HashMap<>();
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
    // datatye checking
    protected int m_overallDatatypeCheckingTime=0;
    protected int m_overallNumberDatatypesChecked;

    @Override
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
        m_datatypeCheckingTime=0;
        m_numberDatatypesChecked=0;
    }
    @Override
    public void isSatisfiableFinished(ReasoningTaskDescription reasoningTaskDescription,boolean result) {
        super.isSatisfiableFinished(reasoningTaskDescription,result);
        if (reasoningTaskDescription.flipSatisfiabilityResult())
            result=!result;
        m_testResult=result;
        m_time=System.currentTimeMillis()-m_problemStartTime;
        String messagePattern=m_reasoningTaskDescription.getMessagePattern();
        List<TestRecord> records=m_testRecords.get(messagePattern);
        if (records==null) {
            records=new ArrayList<>();
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
        m_overallDatatypeCheckingTime+=m_datatypeCheckingTime;
        m_overallNumberDatatypesChecked+=m_numberDatatypesChecked;
    }
    @Override
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_numberOfBacktrackings++;
    }
    @Override
    public void possibleInstanceIsInstance() {
        m_possibleInstancesTested++;
        m_possibleInstancesInstances++;
    }
    @Override
    public void possibleInstanceIsNotInstance() {
        m_possibleInstancesTested++;
    }
    @Override
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
    @Override
    public void blockingValidationFinished(int noInvalidlyBlocked) {
        m_validationTime+=(System.currentTimeMillis()-m_validationStartTime);
        if (m_noValidations==1)
            m_initiallyInvalid=noInvalidlyBlocked;
    }
    @Override
    public void datatypeCheckingStarted() {
        m_numberDatatypesChecked++;
        m_datatypeCheckingStartTime=System.currentTimeMillis();
    }
    @Override
    public void datatypeCheckingFinished(boolean result) {
        m_datatypeCheckingTime+=(System.currentTimeMillis()-m_datatypeCheckingStartTime);
    }
    /**
     * @return getters for test records
     */ 
    public Set<String> getUsedMessagePatterns() {
        return m_testRecords.keySet();
    }
    /** @return getters for current test measurements*/
    public long getTime() {
        return m_time;
    }
    /**
     * @return backtrakcing
     */
    public int getNumberOfBacktrackings() {
        return m_numberOfBacktrackings;
    }
    /**
     * @return nodes
     */
    public int getNumberOfNodes() {
        return m_numberOfNodes;
    }
    /**
     * @return blocked nodes
     */
    public int getNumberOfBlockedNodes() {
        return m_numberOfBlockedNodes;
    }
    /**
     * @return test description
     */
    public String getTestDescription() {
        return m_reasoningTaskDescription.getTaskDescription(Prefixes.STANDARD_PREFIXES);
    }
    /**
     * @return test result
     */
    public boolean getTestResult() {
        return m_testResult;
    }
    /** @return getters for current test blocking validation measurements*/
    public int getInitialModelSize() {
        return m_initialModelSize;
    }
    /**
     * @return initially blocked
     */
    public int getInitiallyBlocked() {
        return m_initiallyBlocked;
    }
    /**
     * @return initially invalid
     */
    public int getInitiallyInvalid() {
        return m_initiallyInvalid;
    }
    /**
     * @return no validations
     */
    public int getNoValidations() {
        return m_noValidations;
    }
    /**
     * @return validation time
     */
    public long getValidationTime() {
        return m_validationTime;
    }
    /**
     * @return datatypes checked
     */
    public int getNumberDatatypesChecked() {
        return m_numberDatatypesChecked;
    }
    /**
     * @return checking time
     */
    public long getDatatypeCheckingTime() {
        return m_datatypeCheckingTime;
    }

    // getters for overall measurements
    /**
     * @return overall time
     */
    public long getOverallTime() {
        return m_overallTime;
    }
    /**
     * @return number of backtracking
     */
    public int getOverallNumberOfBacktrackings() {
        return m_overallNumberOfBacktrackings;
    }
    /**
     * @return number of nodes
     */
    public int getOverallNumberOfNodes() {
        return m_overallNumberOfNodes;
    }
    /**
     * @return number of blocked nodes
     */
    public int getOverallNumberOfBlockedNodes() {
        return m_overallNumberOfBlockedNodes;
    }
    /**
     * @return number of tests
     */
    public int getOverallNumberOfTests() {
        return m_overallNumberOfTests;
    }
    /**
     * @return number of clashes
     */
    public int getOverallNumberOfClashes() {
        return m_overallNumberOfClashes;
    }
    /**
     * @return pssible instances
     */
    public int getNumberOfPossibleInstancesTested() {
        return m_possibleInstancesTested;
    }
    /**
     * @return number of possible instances
     */
    public int getNumberOfPossibleInstancesInstances() {
        return m_possibleInstancesInstances;
    }
    // getters for overall blocking validation measurements
    /**
     * @return initial model size
     */
    public int getOverallInitialModelSize() {
        return m_overallInitialModelSize;
    }
    /**
     * @return initially blocked
     */
    public int getOverallInitiallyBlocked() {
        return m_overallInitiallyBlocked;
    }
    /**
     * @return initially invalid
     */
    public int getOverallInitiallyInvalid() {
        return m_overallInitiallyInvalid;
    }
    /**
     * @return no validation
     */
    public int getOverallNoValidations() {
        return m_overallNoValidations;
    }
    /**
     * @return overall validation time
     */
    public long getOverallValidationTime() {
        return m_overallValidationTime;
    }
    /**
     * @return number datatypes checked
     */
    public int getOverallNumberDatatypesChecked() {
        return m_overallNumberDatatypesChecked;
    }
    /**
     * @return datatype checking time
     */
    public long getOverallDatatypeCheckingTime() {
        return m_overallDatatypeCheckingTime;
    }

    // getters for average measurements
    /**
     * @return average time
     */
    public long getAverageTime() {
        if (m_testNo==0)
            return m_testNo;
        return m_overallTime/m_testNo;
    }
    /**
     * @return number of backtrackings
     */
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
    /**
     * @return number of nodes
     */
    public double getAverageNumberOfNodes() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallNumberOfNodes, m_testNo);
    }
    /**
     * @return number of blockend nodes
     */
    public double getAverageNumberOfBlockedNodes() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallNumberOfBlockedNodes, m_testNo);
    }
    /**
     * @return number of clashes
     */
    public double getAverageNumberOfClashes() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallNumberOfClashes, m_testNo);
    }
    /**
     * @return possible to instance
     */
    public double getPossiblesToInstances() {
        if (m_possibleInstancesTested==0)
            return 0;
        return getRounded(m_possibleInstancesInstances,m_possibleInstancesTested);
    }
    // getters for average blocking validation measurements
    /**
     * @return initial model size
     */
    public double getAverageInitialModelSize() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallInitialModelSize, m_testNo);
    }
    /**
     * @return initially blocked
     */
    public double getAverageInitiallyBlocked() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallInitiallyBlocked, m_testNo);
    }
    /**
     * @return initially invalid
     */
    public double getAverageInitiallyInvalid() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallInitiallyInvalid, m_testNo);
    }
    /**
     * @return no validation
     */
    public double getAverageNoValidations() {
        if (m_testNo==0)
            return m_testNo;
        return getRounded(m_overallNoValidations, m_testNo);
    }
    /**
     * @return validaion time
     */
    public long getAverageValidationTime() {
        if (m_testNo==0)
            return m_testNo;
        return m_overallValidationTime/m_testNo;
    }
    /**
     * @return number datatypes checked
     */
    public long getAverageNumberDatatypesChecked() {
        if (m_testNo==0)
            return m_testNo;
        return m_overallNumberDatatypesChecked/m_testNo;
    }
    /**
     * @return avg datatype checking time
     */
    public long getAverageDatatypeCheckingTime() {
        if (m_testNo==0)
            return m_testNo;
        return m_overallDatatypeCheckingTime/m_testNo;
    }

    /**
     * @param millis millis
     * @return formatted time
     */
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

    private static class TestRecord implements Comparable<TestRecord>, Serializable {
        private static final long serialVersionUID = -3815493500625020183L;
        protected final long m_testTime;
        protected final String m_testDescription;
        protected final boolean m_testResult;

        public TestRecord(long testTime, String testDescription, boolean result) {
            m_testTime=testTime;
            m_testDescription=testDescription;
            m_testResult=result;
        }
        @Override
        public int compareTo(TestRecord that) {
            if (this==that) return 0;
            int result=((Long)that.m_testTime).compareTo(m_testTime);
            if (result!=0) return result;
            else return this.m_testDescription.compareToIgnoreCase(that.m_testDescription);
        }
        @Override
        public String toString() {
            return m_testTime+" ms"+(m_testTime>1000?" ("+CountingMonitor.millisToHoursMinutesSecondsString(m_testTime)+")":"")+" for "+m_testDescription+" (result: "+m_testResult+")";
        }
    }
}