// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.owlapi.reasoner;

import java.util.Set;
import java.util.HashSet;

import org.semanticweb.HermiT.monitor.TableauMonitorAdapter;
import org.semanticweb.HermiT.model.AtomicConcept;

public class PluginMonitor extends TableauMonitorAdapter {

    @SuppressWarnings("serial")
    public class Cancelled extends RuntimeException {
        public Cancelled() {
            super("Operation cancelled by user (via progress monitor)");
        }
    }

    private static final long serialVersionUID=-8144234618897251350L;
    private org.semanticweb.owl.util.ProgressMonitor monitor;

    private Set<AtomicConcept> checked;
    private AtomicConcept prev1;
    private AtomicConcept prev2;
    private AtomicConcept current;
    private int num;
    private int lastVal;
    private int floor;
    private final int ceiling=2;

    public PluginMonitor() {
        checked=new HashSet<AtomicConcept>();
    }

    public void beginTask(String message) {
        if (monitor!=null) {
            monitor.setStarted();
            monitor.setMessage(message);
            monitor.setIndeterminate(true);
        }
    }

    public void beginTask(String message,int numConcepts) {
        if (monitor!=null) {
            num=numConcepts;
            monitor.setStarted();
            monitor.setMessage(message);
            floor=(numConcepts+ceiling)/9+1;
            monitor.setIndeterminate(false);
            monitor.setSize(floor+num+ceiling);
            monitor.setProgress(floor);
            lastVal=floor;
        }
    }

    synchronized public void endTask() {
        checked.clear();
        prev1=prev2=current=null;
        num=lastVal=floor=0;
        if (monitor!=null) {
            monitor.setFinished();
        }
    }

    private void update() {
        int newVal=floor+(checked.size()>num ? num : checked.size());
        if (monitor!=null&&num>0&&(newVal-lastVal)>((floor+num+ceiling)/100)) {
            monitor.setProgress(newVal);
            lastVal=newVal;
        }
    }

    private void checkCancelled() {
        if (monitor!=null&&monitor.isCancelled()) {
            throw new Cancelled();
        }
    }

    synchronized public void setMonitor(org.semanticweb.owl.util.ProgressMonitor monitor) {
        this.monitor=monitor;
    }

    synchronized public void isSatisfiableStarted(AtomicConcept atomicConcept) {
        current=atomicConcept;
    }

    synchronized public void isSubsumedByStarted(AtomicConcept sub,AtomicConcept sup) {
        if (sup==prev1||sup==prev2) {
            current=sup;
        }
        else {
            current=sub;
        }
    }

    synchronized public void isSubsumedByFinished(AtomicConcept sub,AtomicConcept sup) {
        prev1=sub;
        prev2=sup;
        checked.add(current);
        update();
        current=null;
    }

    synchronized public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        checked.add(atomicConcept);
        update();
        current=null;
    }

    synchronized public AtomicConcept currentlyClassifiedConcept() {
        return current;
    }

    public void iterationFinished() {
        checkCancelled();
    }

}
