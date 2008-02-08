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
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#BodySystem");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#BodyCavity");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#Lymphnode");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#Lobe");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDBodyPart");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDBodySpace");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDArtery");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDVein");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDAtrium");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDVentricle");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDMembrane");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDMuscle");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDJunction");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDOrifice");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDNerve");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDNervePlexus");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDLigament");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDInternalBodySubPart");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#NAMEDGland");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#discrete");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#normal");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#infinitelyDivisible");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#internal");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#internalSelection");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#OrganicRole");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#atLeastPaired");
        addSubconceptsToReuse("http://www.co-ode.org/ontologies/galen#mirrorImaged");
        
        removeConceptToReuse("http://www.co-ode.org/ontologies/galen#CarotidBifurcation");
        removeConceptToReuse("http://www.co-ode.org/ontologies/galen#Axon");
        
        System.out.println("Total number of reused concepts: "+m_reuseConcepts.size());
    }
    protected void addSubconceptsToReuse(String rootURI) {
        Set<Concept> allSubconcepts=m_conceptHierarchy.getAllSubobjects(AtomicConcept.create(rootURI));
        for (Concept concept : allSubconcepts)
            m_reuseConcepts.add((AtomicConcept)concept);
    }
    protected void removeConceptToReuse(String rootURI) {
        m_reuseConcepts.remove(AtomicConcept.create(rootURI));
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
        String physicalURI="file:/C:/Work/TestOntologies/GALEN/galen-module1.owl";
        
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
