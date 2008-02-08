package org.semanticweb.HermiT.run;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;
import org.semanticweb.HermiT.tableau.*;

@SuppressWarnings("serial")
public class ReuseAnalysis5 extends TableauMonitorAdapter {
    protected final HermiT m_hermit;
    protected final Map<Node,AtLeastAbstractRoleConcept> m_existentialsForNode;
    protected final Map<Node,Set<Node>> m_childrenForNode;
    protected AtLeastAbstractRoleConcept m_lastExistentialConcept;
    protected long m_startTime;
    protected PrintWriter m_result;

    public ReuseAnalysis5(String physicalURI) throws Exception {
        m_hermit=new HermiT();
        m_hermit.setUserTableauMonitor(this);
        m_hermit.setExistentialsType(HermiT.ExistentialsType.INDIVIDUAL_REUSE);
        m_hermit.loadOntology(physicalURI);
        m_existentialsForNode=new HashMap<Node,AtLeastAbstractRoleConcept>();
        m_childrenForNode=new HashMap<Node,Set<Node>>();
    }
    public void saturateStarted() {
        m_startTime=System.currentTimeMillis();
    }
    public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
        m_lastExistentialConcept=(AtLeastAbstractRoleConcept)existentialConcept;
    }
    public void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode) {
        m_lastExistentialConcept=null;
    }
    public void iterationFinished() {
        long duration=System.currentTimeMillis()-m_startTime;
        if (duration>2*60*1000) {
            System.out.println("Time ran out!");
            // The following will recompute blocking.
            m_hermit.getTableau().getExistentialsExpansionStrategy().modelFound();
            printPathsWithRepetitions();
            throw new Done();
        }
        
    }
    public void nodeCreated(Node node) {
        if (m_lastExistentialConcept!=null) {
            m_existentialsForNode.put(node,m_lastExistentialConcept);
            m_lastExistentialConcept=null;
            if (node.getParent()!=null) {
                Set<Node> set=m_childrenForNode.get(node.getParent());
                if (set==null) {
                    set=new HashSet<Node>();
                    m_childrenForNode.put(node.getParent(),set);
                }
                set.add(node);
            }
        }
    }
    public void printPathsWithRepetitions() {
        try {
            m_result=new PrintWriter(new FileWriter("c:\\Temp\\paths.txt"),true);
            try {
                Set<AtomicConcept> conceptsOnPath=new HashSet<AtomicConcept>();
                List<Node> currentPath=new ArrayList<Node>();
                analyzeRepetitionsNodeChildren(m_hermit.getTableau().getCheckedNode(),conceptsOnPath,currentPath);
            }
            finally {
                m_result.close();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    protected void analyzeRepetitionsNode(Node node,Set<AtomicConcept> conceptsOnPath,List<Node> path) {
        path.add(node);
        AtLeastAbstractRoleConcept existential=m_existentialsForNode.get(node);
        AtomicConcept toAtomicConcept=(AtomicConcept)existential.getToConcept();
        if (conceptsOnPath.contains(toAtomicConcept)) {
            printPath(path);
            analyzeRepetitionsNodeChildren(node,conceptsOnPath,path);
        }
        else {
            conceptsOnPath.add(toAtomicConcept);
            analyzeRepetitionsNodeChildren(node,conceptsOnPath,path);
            conceptsOnPath.remove(toAtomicConcept);
        }
        path.remove(path.size()-1);
    }
    protected void analyzeRepetitionsNodeChildren(Node node,Set<AtomicConcept> conceptsOnPath,List<Node> path) {
        Set<Node> children=m_childrenForNode.get(node);
        if (children!=null)
            for (Node child : children)
                analyzeRepetitionsNode(child,conceptsOnPath,path);
    }
    protected void printPath(List<Node> path) {
        m_result.println("(root)");
        int indent=0;
        for (Node node : path) {
            indent+=4;
            for (int i=0;i<indent;i++)
                m_result.print(' ');
            m_result.print("--[");
            AtLeastAbstractRoleConcept existential=m_existentialsForNode.get(node);
            m_result.print(existential.getOnAbstractRole().toString(m_hermit.getNamespaces()));
            m_result.print("]--> ");
            m_result.print(node.getNodeID());
            m_result.print(":<");
            m_result.print(existential.getToConcept().toString(m_hermit.getNamespaces()));
            m_result.print(">");
            if (node.isBlocked()) {
                m_result.print("!{");
                m_result.print(node.getBlocker().getNodeID());
                m_result.print("}");
            }
            if (node.hasUnprocessedExistentials())
                m_result.print("*");
            m_result.println();
        }
        if (m_childrenForNode.get(path.get(path.size()-1))!=null) {
            indent+=4;
            for (int i=0;i<indent;i++)
                m_result.print(' ');
            m_result.println("...");
        }
        m_result.println();
        m_result.println();
    }
    
    public static void main(String[] args) throws Exception {
        try {
            String physicalURI="file:/C:/Work/TestOntologies/GALEN/galen-module1.owl";
            ReuseAnalysis5 analysis=new ReuseAnalysis5(physicalURI);
            analysis.m_hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#AbdominalCavity","http://www.co-ode.org/ontologies/galen#ActualCavity");
            System.out.println("Finished!");
        }
        catch (Done done) {
        }
    }
    
    protected static class Done extends RuntimeException {
    }
}
