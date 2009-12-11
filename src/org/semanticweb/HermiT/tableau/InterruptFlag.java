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

public final class InterruptFlag implements Serializable {
    private static final long serialVersionUID = -6983680374511847003L;
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
