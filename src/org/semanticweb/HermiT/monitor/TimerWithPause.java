// Copyright 2008 by Oxford University; see license.txt for details
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
