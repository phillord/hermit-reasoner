// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.monitor;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;
import java.io.PrintWriter;
import java.util.Map;

public class ReasoningOperations extends TableauMonitorAdapter {
    private static final long serialVersionUID=-8144444618897251350L;

    public int numSatTests;
    public int numSubsumptionTests;
    public int numConsistencyTests;
    
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        ++numSatTests;
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
        ++numSubsumptionTests;
    }
    public void isABoxSatisfiableFinished(boolean result) {
        ++numConsistencyTests;
    }
}
