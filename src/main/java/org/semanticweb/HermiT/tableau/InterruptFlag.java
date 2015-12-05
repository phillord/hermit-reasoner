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

    protected static enum InterruptType { INTERRUPTED,TIMEOUT };

    protected final InterruptTimer m_interruptTimer;
    protected volatile InterruptType m_interruptType;

    public InterruptFlag(long individualTaskTimeout) {
        if (individualTaskTimeout>0)
            m_interruptTimer=new InterruptTimer(individualTaskTimeout);
        else
            m_interruptTimer=null;
    }
    public void checkInterrupt() {
        InterruptType interruptType=m_interruptType;
        if (interruptType!=null) {
            if (interruptType==InterruptType.TIMEOUT)
                throw new TimeOutException();
            else
                throw new ReasonerInterruptedException();
        }
    }
    public void interrupt() {
        m_interruptType=InterruptType.INTERRUPTED;
    }
    public void startTask() {
        m_interruptType=null;
        if (m_interruptTimer!=null)
            m_interruptTimer.startTiming();
    }
    public void endTask() {
        if (m_interruptTimer!=null)
            m_interruptTimer.stopTiming();
        m_interruptType=null;
    }
    public void dispose() {
        if (m_interruptTimer!=null)
            m_interruptTimer.dispose();
    }

    protected static enum TimerState { WAIT_FOR_TASK,TIMING,TIMING_STOPPED,DISPOSED };

    protected class InterruptTimer extends Thread {
        protected final long m_timeout;
        protected TimerState m_timerState;

        public InterruptTimer(long timeout) {
            super("HermiT Interrupt Current Task Thread");
            setDaemon(true);
            m_timeout=timeout;
            start();
        }
        public synchronized void run() {
            while (m_timerState!=TimerState.DISPOSED) {
                m_timerState=TimerState.WAIT_FOR_TASK;
                notifyAll();
                while (m_timerState==TimerState.WAIT_FOR_TASK) {
                    try {
                        wait();
                    }
                    catch (InterruptedException stopped) {
                        m_timerState=TimerState.DISPOSED;
                    }
                }
                if (m_timerState==TimerState.TIMING) {
                    try {
                        wait(m_timeout);
                        if (m_timerState==TimerState.TIMING)
                            m_interruptType=InterruptType.TIMEOUT;
                    }
                    catch (InterruptedException stopped) {
                        m_timerState=TimerState.DISPOSED;
                    }
                }
            }
        }
        public synchronized void startTiming() {
            while (m_timerState!=TimerState.WAIT_FOR_TASK && m_timerState!=TimerState.DISPOSED) {
                try {
                    wait();
                }
                catch (InterruptedException stopped) {
                }
            }
            if (m_timerState==TimerState.WAIT_FOR_TASK) {
                m_timerState=TimerState.TIMING;
                notifyAll();
            }
        }
        public synchronized void stopTiming() {
            if (m_timerState==TimerState.TIMING) {
                m_timerState=TimerState.TIMING_STOPPED;
                notifyAll();
                while (m_timerState!=TimerState.WAIT_FOR_TASK && m_timerState!=TimerState.DISPOSED) {
                    try {
                        wait();
                    }
                    catch (InterruptedException stopped) {
                        return;
                    }
                }
            }
        }
        public synchronized void dispose() {
            m_timerState=TimerState.DISPOSED;
            notifyAll();
            try {
                join();
            }
            catch (InterruptedException e) {
            }
        }
    }
}
