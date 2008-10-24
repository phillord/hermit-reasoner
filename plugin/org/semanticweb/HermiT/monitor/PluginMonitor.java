// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.monitor;

import org.semanticweb.HermiT.model.AtomicConcept;
import java.util.Set;
import java.util.HashSet;

public class PluginMonitor extends TableauMonitorAdapter {
    private static final long serialVersionUID=-8144234618897251350L;
    private org.semanticweb.owl.util.ProgressMonitor monitor;
    
    private Set<AtomicConcept> checked;
    private AtomicConcept current;
    
    public PluginMonitor() {
        checked = new HashSet<AtomicConcept>();
    }
    
    void setMonitor(org.semanticweb.owl.util.ProgressMonitor monitor) {
        this.monitor = monitor;
    }
    
    synchronized public void isSatisfiableStarted(AtomicConcept atomicConcept) {
        current = atomicConcept;
    }
    
    synchronized public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        checked.add(atomicConcept);
        current = null;
    }
    
    synchronized public int numConceptsChecked() {
        return checked.size();
    }
    
    synchronized public String curConcept() {
        if (current == null) {
            return null;
        } else {
            return current.getURI();
        }
    }
    
    synchronized public void clear() {
        checked.clear();
        current = null;
    }
}