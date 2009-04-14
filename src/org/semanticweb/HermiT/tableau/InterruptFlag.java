package org.semanticweb.HermiT.tableau;

public final class InterruptFlag {
    protected boolean m_taskRunning;
    protected volatile boolean m_interrupt;
    
    public InterruptFlag() {
    }
    public void checkInterrupt() {
        if (m_interrupt) {
            m_interrupt=false;
            throw new InterruptException();
        }
    }
    public synchronized void startTask() {
        m_interrupt=false;
        m_taskRunning=true;
    }
    public synchronized void endTask() {
        m_interrupt=false;
        m_taskRunning=false;
    }
    public synchronized void interrupt() {
        if (m_taskRunning)
            m_interrupt=true;
    }
}
