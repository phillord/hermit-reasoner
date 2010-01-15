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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TimerWithPause extends Timer {
    private static final long serialVersionUID=-9176603965017225734L;

    protected BufferedReader m_in;
    
    public TimerWithPause() {
        m_in=new BufferedReader(new InputStreamReader(System.in));
    }
    protected void doStatistics() {
        super.doStatistics();
        System.out.print("Press something to continue.. ");
        System.out.flush();
        try {
            m_in.readLine();
        }
        catch (IOException ignored) {
        }
    }
}
