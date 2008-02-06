package org.semanticweb.HermiT.run;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;
import org.semanticweb.HermiT.tableau.*;

@SuppressWarnings("serial")
public class ReuseAnalysis4 extends TableauMonitorAdapter {
    protected final HermiT m_hermit;
    protected final Map<AtomicConcept,ExpansionCounter> m_expansions;
    protected long m_startTime;

    public ReuseAnalysis4(String physicalURI) throws Exception {
        m_hermit=new HermiT();
        m_hermit.setUserTableauMonitor(this);
        m_hermit.loadOntology(physicalURI);
        m_expansions=new HashMap<AtomicConcept,ExpansionCounter>();
    }
    public void saturateStarted() {
        m_startTime=System.currentTimeMillis();
    }
    public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
        AtomicConcept toConcept=(AtomicConcept)((AtLeastAbstractRoleConcept)existentialConcept).getToConcept();
        ExpansionCounter counter=m_expansions.get(toConcept);
        if (counter==null) {
            counter=new ExpansionCounter(toConcept);
            m_expansions.put(toConcept,counter);
        }
        counter.m_number++;
        long duration=System.currentTimeMillis()-m_startTime;
        if (duration>2*60*1000) {
            System.out.println("I'm out of here!");
            printResults();
            throw new RuntimeException();
        }
    }
    public void printResults() {
        try {
            PrintWriter result=new PrintWriter(new FileWriter("c:\\Temp\\expansions.txt"),true);
            try {
                List<ExpansionCounter> list=new ArrayList<ExpansionCounter>();
                for (Map.Entry<AtomicConcept,ExpansionCounter> entry : m_expansions.entrySet())
                    list.add(entry.getValue());
                Collections.sort(list,new SortByCounter());
                for (ExpansionCounter counter : list) {
                    String number=String.valueOf(counter.m_number);
                    while (number.length()<5)
                        number=" "+number;
                    result.println(number+"        "+counter.m_atomicConcept.getURI());
                }
            }
            finally {
                result.close();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) throws Exception {
        String physicalURI="file:/C:/Work/TestOntologies/GALEN/galen-module1.owl";
        ReuseAnalysis4 analysis=new ReuseAnalysis4(physicalURI);
        analysis.m_hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#AbdominalCavity","http://www.co-ode.org/ontologies/galen#ActualCavity");
        System.out.println("Success!!!");
    }
    
    protected final class ExpansionCounter {
        public final AtomicConcept m_atomicConcept;
        public int m_number;
        
        public ExpansionCounter(AtomicConcept atomicConcept) {
            m_atomicConcept=atomicConcept;
        }
    }
    
    protected final class SortByCounter implements Comparator<ExpansionCounter> {

        public int compare(ExpansionCounter o1,ExpansionCounter o2) {
            if (o1.m_number!=o2.m_number)
                return o1.m_number-o2.m_number;
            return o1.m_atomicConcept.getURI().compareTo(o2.m_atomicConcept.getURI());
        }
        
    }
}
