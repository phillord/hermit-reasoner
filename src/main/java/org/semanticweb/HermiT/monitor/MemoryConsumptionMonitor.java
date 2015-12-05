package org.semanticweb.HermiT.monitor;

import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;

public class MemoryConsumptionMonitor extends CountingMonitor {
    private static final long serialVersionUID = -483824095933491230L;
    
    protected int m_binaryTableMem=0; // in KB
    protected int m_ternaryTableMem=0; // in KB
    protected int m_dependencySetsMem=0; // in KB
    protected int m_sumBinaryTableMem=0; // in KB
    protected int m_sumTernaryTableMem=0; // in KB
    protected int m_sumDependencySetsMem=0; // in KB
    protected int m_maxMem=0; // in KB
    protected int m_testNumber=0;
    
    public void isSatisfiableStarted(ReasoningTaskDescription reasoningTaskDescription) {
        super.isSatisfiableStarted(reasoningTaskDescription);
        m_testNumber++;
    }
    public void isSatisfiableFinished(ReasoningTaskDescription reasoningTaskDescription,boolean result) {
        super.isSatisfiableFinished(reasoningTaskDescription, result);
        m_binaryTableMem=m_tableau.getExtensionManager().getBinaryExtensionTable().sizeInMemory()/1024;
        m_ternaryTableMem=m_tableau.getExtensionManager().getTernaryExtensionTable().sizeInMemory()/1024;
        m_dependencySetsMem=m_tableau.getDependencySetFactory().sizeInMemory()/1024;
        m_sumBinaryTableMem+=m_binaryTableMem;
        m_sumTernaryTableMem+=m_ternaryTableMem;
        m_sumDependencySetsMem+=m_dependencySetsMem;
        int sum=m_binaryTableMem+m_ternaryTableMem+m_dependencySetsMem;
        if (sum>m_maxMem)
            m_maxMem=sum;
    }
    public void reset() {
        super.reset();
        m_binaryTableMem=0; // in KB
        m_ternaryTableMem=0; // in KB
        m_dependencySetsMem=0; // in KB
        m_sumBinaryTableMem=0; // in KB
        m_sumTernaryTableMem=0; // in KB
        m_sumDependencySetsMem=0; // in KB
        m_maxMem=0; // in KB
        m_testNumber=0;
    }
    public long getCurrentTableauExpansionMemoryUse() {
        return m_binaryTableMem+m_ternaryTableMem+m_dependencySetsMem;
    }
    public long getCurrentTableauExpansionBinaryTableSize() {
        return m_binaryTableMem;
    }
    public long getCurrentTableauExpansionTernaryTableSize() {
        return m_ternaryTableMem;
    }
    public long getCurrentTableauExpansionDependencySetsSize() {
        return m_dependencySetsMem;
    }
    public long getAverageTableauExpansionMemoryUse() {
        if (m_testNumber==0)
            return 0;
        return (m_sumBinaryTableMem+m_sumTernaryTableMem+m_sumDependencySetsMem)/m_testNumber;
    }
    public long getAverageTableauExpansionBinaryTableSize() {
        if (m_testNumber==0)
            return 0;
        return m_sumBinaryTableMem/m_testNumber;
    }
    public long getAverageTableauExpansionTernaryTableSize() {
        if (m_testNumber==0)
            return 0;
        return m_sumTernaryTableMem/m_testNumber;
    }
    public long getAverageTableauExpansionDependencySetsSize() {
        if (m_testNumber==0)
            return 0;
        return m_sumDependencySetsMem/m_testNumber;
    }
    public long getMaxTableauExpansionMemoryUse() {
        return m_maxMem;
    }
}
