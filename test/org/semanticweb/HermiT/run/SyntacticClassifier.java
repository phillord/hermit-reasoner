package org.semanticweb.HermiT.run;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.io.PrintWriter;

import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.KAON2Connection;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.kaon2.structural.*;
import org.semanticweb.HermiT.model.*;

/**
 * This class can be used to compute the classification of an ontology by just taking the transitive closure
 * of the explicit hierarchy. The class is a bit smarter, in that it works on the normal form.
 */
public class SyntacticClassifier {
    protected final Map<String,Set<String>> m_classification;
    protected final Namespaces m_namespaces;
    
    public SyntacticClassifier(DLOntology dlOntology) throws Exception {
        m_classification=new TreeMap<String,Set<String>>();
        m_namespaces=new Namespaces();
        m_namespaces.registerPrefix("a",dlOntology.getOntologyURI()+"#");
        for (AtomicConcept atomicConcept : dlOntology.getAllAtomicConcepts()) {
            Set<String> superclasses=new TreeSet<String>();
            superclasses.add(atomicConcept.getURI());
            m_classification.put(atomicConcept.getURI(),superclasses);
        }
        for (DLClause dlClause : dlOntology.getDLClauses()) {
            if (dlClause.getBodyLength()==1 && dlClause.getHeadLength()==1) {
                Atom bodyAtom=dlClause.getBodyAtom(0);
                Atom headAtom=dlClause.getHeadAtom(0);
                if (bodyAtom.getDLPredicate() instanceof AtomicConcept && headAtom.getDLPredicate() instanceof AtomicConcept) {
                    AtomicConcept subconcept=(AtomicConcept)bodyAtom.getDLPredicate();
                    AtomicConcept superconcept=(AtomicConcept)headAtom.getDLPredicate();
                    m_classification.get(subconcept.getURI()).add(superconcept.getURI());
                }
            }
        }
        boolean change=true;
        while (change) {
            change=false;
            for (Map.Entry<String,Set<String>> subclass : m_classification.entrySet()) {
                Set<String> newSuperclasses=new HashSet<String>();
                for (String superclass : subclass.getValue())
                    newSuperclasses.addAll(m_classification.get(superclass));
                if (!newSuperclasses.isEmpty())
                    if (subclass.getValue().addAll(newSuperclasses))
                        change=true;
            }
        }
    }
    public void printFlattenedHierarchy(PrintWriter output) throws Exception {
        for (Map.Entry<String,Set<String>> entry : m_classification.entrySet()) {
            if (!entry.getKey().startsWith("internal:")) {
                output.println(m_namespaces.abbreviateAsNamespace(entry.getKey()));
                for (String owlClassURI : entry.getValue())
                    if (!owlClassURI.startsWith("internal:")) {
                        output.print("    ");
                        output.println(m_namespaces.abbreviateAsNamespace(owlClassURI));
                    }
                output.println("-----------------------------------------------");
            }
        }
        output.println("! THE END !");
        output.flush();
    }
    public static void main(String[] args) throws Exception {
//        DLOntology dlOntology=DLOntology.load(new java.io.File("c:\\Temp\\graphs\\galen-ians-full-undoctored-concrete.ser"));
//        DLOntology dlOntology=DLOntology.load(new java.io.File("c:\\Temp\\fma-module2-concrete.ser"));
        DLOntology dlOntology=loadOntology("file:/C:/Work/My%20Papers/2007/Representing%20and%20Reasoning%20about%20Structured%20Objects%20in%20OWL/ontologies/FMA-Full.owl");
        
        SyntacticClassifier classifier=new SyntacticClassifier(dlOntology);
        classifier.printFlattenedHierarchy(new PrintWriter("c:\\Temp\\stupid-classification.txt"));
    }
    protected static DLOntology loadOntology(String physicalURI) throws Exception {
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
