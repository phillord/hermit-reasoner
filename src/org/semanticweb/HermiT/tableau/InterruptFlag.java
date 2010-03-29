/* Copyright 2009 by the Oxford University Computing Laboratory

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
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;

public final class InterruptFlag implements Serializable {
    private static final long serialVersionUID=-6983680374511847003L;
    protected InterruptTimer m_interruptTimer;
    protected final long m_individualTaskTimeout;
    protected volatile boolean m_taskRunning;
    protected volatile boolean m_taskInterrupted;
    protected volatile boolean m_interrupt;

    public InterruptFlag() {
        this(-1);
    }
    public InterruptFlag(long individualTaskTimeout) {
        m_individualTaskTimeout=individualTaskTimeout;
        m_taskInterrupted=false;
        if (m_individualTaskTimeout>0)
            m_interruptTimer=new RealInterruptTimer(m_individualTaskTimeout,this);
        else
            m_interruptTimer=new DummyInterruptTimer();
    }
    public void checkInterrupt() {
        if (m_interrupt) {
            m_interrupt=false;
            if (m_taskInterrupted)
                throw new TimeOutException();
            else
                throw new ReasonerInterruptedException();
        }
    }
    public synchronized void startTask() {
        m_interrupt=false;
        m_taskInterrupted=false;
        m_taskRunning=true;
    }
    public synchronized void startTimedTask() {
        m_interruptTimer.start();
        startTask();
    }
    public synchronized void endTask() {
        m_interrupt=false;
        m_taskRunning=false;
        if (!m_interruptTimer.getTimingStopped()) {
            m_interruptTimer.interrupt();
            m_interruptTimer.stopTiming();
        }
        m_interruptTimer=m_interruptTimer.getNextInterruptTimer();
    }
    public synchronized void interrupt() {
        if (m_taskRunning)
            m_interrupt=true;
    }
    public synchronized void interruptCurrentTask() {
        m_taskInterrupted=true;
        interrupt();
    }

    interface InterruptTimer extends Runnable {
        public InterruptTimer getNextInterruptTimer();
        public void start();
        public void stopTiming();
        public boolean getTimingStopped();
        public void interrupt();
    }

    protected static class RealInterruptTimer extends Thread implements InterruptTimer {
        protected final long m_timeout;
        protected final InterruptFlag m_interruptFlag;
        protected boolean m_timingStopped;

        public RealInterruptTimer(long timeout,InterruptFlag interruptFlag) {
            super("HermiT Interrupt Current Task Thread");
            setDaemon(true);
            m_timeout=timeout;
            m_interruptFlag=interruptFlag;
            m_timingStopped=false;
        }
        public InterruptTimer getNextInterruptTimer() {
            return new RealInterruptTimer(m_timeout,m_interruptFlag);
        }
        public synchronized void run() {
            if (m_timeout>=0) {
                try {
                    if (!m_timingStopped) {
                        wait(m_timeout);
                        if (!m_timingStopped)
                            m_interruptFlag.interruptCurrentTask();
                    }
                }
                catch (InterruptedException stopped) {
                }
            }
        }
        public synchronized void stopTiming() {
            m_timingStopped=true;
            notifyAll();
        }
        public boolean getTimingStopped() {
            return m_timingStopped;
        }
    }

    protected static class DummyInterruptTimer implements InterruptTimer {
        public InterruptTimer getNextInterruptTimer() {
            return this;
        }
        public synchronized void run() {
        }
        public synchronized void stopTiming() {
        }
        public boolean getTimingStopped() {
            return true;
        }
        public void interrupt() {
        }
        public void start() {
        }
    }
}
