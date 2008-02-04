package org.semanticweb.HermiT.run;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Collections;

import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.KAON2Connection;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;

import org.semanticweb.HermiT.kaon2.structural.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class ReuseAnalysis2 {
    protected final DLOntology m_dlOntology;
    protected final Set<AtomicConcept> m_reuseConcepts;
    protected final ObjectHierarchy<Concept> m_conceptHierarchy;
    
    public ReuseAnalysis2(DLOntology dlOntology) {
        m_dlOntology=dlOntology;
        m_reuseConcepts=new TreeSet<AtomicConcept>(DLOntology.AtomicConceptComparator.INSTANCE);
        m_conceptHierarchy=new ObjectHierarchy<Concept>();
        analyzeDLOntology();
        Set<Concept> allSubconcepts=m_conceptHierarchy.getAllSubobjects(AtomicConcept.create("http://www.co-ode.org/ontologies/galen#BodyStructure"));
        for (Concept concept : allSubconcepts)
            m_reuseConcepts.add((AtomicConcept)concept);
        System.out.println("Total number of reused concepts: "+m_reuseConcepts.size());
    }
    protected void analyzeDLOntology() {
        for (DLClause dlClause : m_dlOntology.getDLClauses()) {
            if (dlClause.isConceptInclusion()) {
                AtomicConcept bodyConcept=(AtomicConcept)dlClause.getBodyAtom(0).getDLPredicate();
                Concept headConcept=(Concept)dlClause.getHeadAtom(0).getDLPredicate();
                m_conceptHierarchy.addInclusion(bodyConcept,headConcept);
            }
        }
    }
    public void save(File reuseConcepts,File dontReuseConcepts) throws IOException {
        PrintWriter dontReuseWriter=new PrintWriter(new FileWriter(dontReuseConcepts));
        try {
            for (AtomicConcept atomicConcept : m_dlOntology.getAllAtomicConcepts())
                if (!m_reuseConcepts.contains(atomicConcept))
                    dontReuseWriter.println(atomicConcept.getURI());
        }
        finally {
            dontReuseWriter.close();
        }
        PrintWriter reuseWriter=new PrintWriter(new FileWriter(reuseConcepts));
        try {
            for (AtomicConcept atomicConcept : m_reuseConcepts)
                reuseWriter.println(atomicConcept.getURI());
        }
        finally {
            reuseWriter.close();
        }
    }
    
    public static void main(String[] args) throws Exception {
//        String physicalURI="file:/C:/Work/ontologies/GALEN/galen-ians-full-undoctored.owl";
        String physicalURI="file:/C:/Work/ontologies/GALEN/galen-module1.owl";
        
        DLOntology dlOntology=loadDLOntology(physicalURI);
        ReuseAnalysis2 analysis=new ReuseAnalysis2(dlOntology);
        analysis.save(new File("c:\\Temp\\reuse.txt"),new File("c:\\Temp\\dont-reuse.txt"));
    }
    protected static DLOntology loadDLOntology(String physicalURI) throws Exception {
        DefaultOntologyResolver resolver=new DefaultOntologyResolver();
        String ontologyURI=resolver.registerOntology(physicalURI);
        KAON2Connection connection=KAON2Manager.newConnection();
        connection.setOntologyResolver(resolver);
        Ontology ontology=connection.openOntology(ontologyURI,new HashMap<String,Object>());
        Clausification clausification=new Clausification();
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        return clausification.clausify(false,ontology,true,noDescriptionGraphs);
    }
}
