package org.semanticweb.HermiT.run;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.existentials.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;
import org.semanticweb.HermiT.tableau.*;

@SuppressWarnings("serial")
public class ReuseAnalysis3 extends TableauMonitorAdapter {
    protected final HermiT m_hermit;
    protected final IndividualReuseStrategy m_individualReuseStrategy;
    protected final ObjectHierarchy<AtomicConcept> m_conceptHierarchy;
    protected final Map<AtomicConcept,Set<AtomicConcept>> m_superconcepts;
    protected final PrintWriter m_dontReuseConcepts;
    protected final PrintWriter m_events;
    protected final Set<AtomicConcept> m_overrideReuse;
    protected DLClauseEvaluator m_currentDLClauseEvaluator;
    
    public ReuseAnalysis3(String physicalURI) throws Exception {
        m_hermit=new HermiT();
//        m_hermit.setTimingOn();
        m_hermit.setExistentialsType(HermiT.ExistentialsType.INDIVIDUAL_REUSE);
        m_hermit.setUserTableauMonitor(this);
        m_hermit.loadOntology(physicalURI);
        m_individualReuseStrategy=(IndividualReuseStrategy)m_hermit.getTableau().getExistentialsExpansionStrategy();
        m_conceptHierarchy=new ObjectHierarchy<AtomicConcept>();
        for (DLClause dlClause : m_hermit.getDLOntology().getDLClauses())
            if (dlClause.isConceptInclusion() && dlClause.getHeadAtom(0).getDLPredicate() instanceof AtomicConcept) {
                AtomicConcept subconcept=(AtomicConcept)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicConcept superconcept=(AtomicConcept)dlClause.getHeadAtom(0).getDLPredicate();
                m_conceptHierarchy.addInclusion(subconcept,superconcept);
            }
        m_superconcepts=new HashMap<AtomicConcept,Set<AtomicConcept>>();
        m_dontReuseConcepts=new PrintWriter(new FileWriter("c:\\Temp\\dont-reuse-new-analysis.txt"),true);
        m_events=new PrintWriter(new FileWriter("c:\\Temp\\events.txt"),true);
        m_overrideReuse=new HashSet<AtomicConcept>();
        loadOverrideConcepts("c:\\Temp\\override-reuse.txt");
    }
    public void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
        m_currentDLClauseEvaluator=dlClauseEvaluator;
    }
    public void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
        m_currentDLClauseEvaluator=null;
    }
    public void mergeStarted(Node mergeFrom,Node mergeInto) {
        if (m_currentDLClauseEvaluator!=null) {
            Node centralNode=(Node)m_currentDLClauseEvaluator.getTupleMatchedToBody(0)[1];
            AtomicConcept centralConcept=m_individualReuseStrategy.getConceptForNode(centralNode);
            AtomicConcept fromConcept=m_individualReuseStrategy.getConceptForNode(mergeFrom);
            AtomicConcept intoConcept=m_individualReuseStrategy.getConceptForNode(mergeInto);
            if (centralConcept!=null && fromConcept!=null && intoConcept!=null) {
                AtomicAbstractRole atomicAbstractRole=null;
                for (int index=0;index<m_currentDLClauseEvaluator.getBodyLength();index++)
                    if (m_currentDLClauseEvaluator.getTupleMatchedToBody(index)[0] instanceof AtomicAbstractRole) {
                        atomicAbstractRole=(AtomicAbstractRole)m_currentDLClauseEvaluator.getTupleMatchedToBody(index)[0];
                        break;
                    }
                if (!getSuperconcepts(fromConcept).contains(intoConcept) && !getSuperconcepts(intoConcept).contains(fromConcept) && !m_overrideReuse.contains(centralConcept)) {
                    String event=
                        "In tableau: "+
                        m_hermit.getTableau().getNumberOfNodesInTableau()+
                        "  merged: "+
                        m_hermit.getTableau().getNumberOfMergedOrPrunedNodes()+
                        "    "+
                        m_hermit.getNamespaces().abbreviateAsNamespace(fromConcept.getURI())+
                        " --> "+
                        m_hermit.getNamespaces().abbreviateAsNamespace(intoConcept.getURI())+
                        " on "+
                        m_hermit.getNamespaces().abbreviateAsNamespace(atomicAbstractRole.getURI())+
                        " around     "+
                        m_hermit.getNamespaces().abbreviateAsNamespace(centralConcept.getURI());
                    System.out.println(event);
                    m_events.println(event);
                    m_events.flush();
                    m_dontReuseConcepts.println(centralConcept.getURI());
                    m_dontReuseConcepts.flush();
                    m_individualReuseStrategy.getDontReuseConceptsEver().add(centralConcept);
                    throw new FunctionalityMergeException();
                }
            }
        }
    }
    public void clear() {
        m_currentDLClauseEvaluator=null;
    }
    protected Set<AtomicConcept> getSuperconcepts(AtomicConcept atomicConcept) {
        Set<AtomicConcept> superconcepts=m_superconcepts.get(atomicConcept);
        if (superconcepts==null) {
            superconcepts=m_conceptHierarchy.getAllSuperobjects(atomicConcept);
            m_superconcepts.put(atomicConcept,superconcepts);
        }
        return superconcepts;
    }
    protected void loadOverrideConcepts(String fileName) {
        try {
            BufferedReader reader=new BufferedReader(new FileReader(fileName));
            try {
                String line=reader.readLine();
                while (line!=null) {
                    m_overrideReuse.add(AtomicConcept.create(line));
                    line=reader.readLine();
                }
            }
            finally {
                reader.close();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Can't read the nonreused concepts.",e);
        }
    }
    
    public static void main(String[] args) throws Exception {
        String physicalURI="file:/C:/Work/TestOntologies/GALEN/galen-module1.owl";
        ReuseAnalysis3 analysis=new ReuseAnalysis3(physicalURI);
        while (true) {
            try {
                analysis.clear();
                analysis.m_hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#AbdominalCavity","http://www.co-ode.org/ontologies/galen#ActualCavity");
                System.out.println("Success!!!");
                break;
            }
            catch (FunctionalityMergeException e) {
            }
        }
    }
    
    protected static class FunctionalityMergeException extends RuntimeException {
    }
}
