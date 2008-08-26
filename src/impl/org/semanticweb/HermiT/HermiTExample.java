package org.semanticweb.HermiT;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExistentialsExpansionStrategy;
import org.semanticweb.HermiT.hierarchy.SubsumptionHierarchy;
import org.semanticweb.HermiT.hierarchy.TableauSubsumptionChecker;
import org.semanticweb.HermiT.kaon2.structural.Clausification;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.OntologyManager;

public class HermiTExample {

    public static void main(String[] args) throws Exception {
        // printSubsumptionHierarchyAsText(
        // "file:/Users/rob/HermiT/test/org/semanticweb/HermiT/tableau/res/pizza.xml"
        // );
        HermiT hermit = new HermiT("file:/c:/pizza.xml");
        hermit.isConsistent();

        // DefaultOntologyResolver resolver = new DefaultOntologyResolver();
        // String ontologyURI = resolver.registerOntology("file:/c:/pizza.xml");
        // OntologyManager ontologyManager = KAON2Manager.newOntologyManager();
        // ontologyManager.setOntologyResolver(resolver);
        // Ontology ontology = ontologyManager.openOntology(ontologyURI,new
        // HashMap<String,Object>());

        // Tableau tableau = hermit.getTableau();
        // // PrintWriter output = new PrintWriter(
        // // new FileWriter("OutFile.txt"));
        // PrintWriter output = new PrintWriter(System.out, true);
        // SubsumptionHierarchy.SubsumptionChecker subsumptionChecker =
        // new TableauSubsumptionChecker(tableau);
        // SubsumptionHierarchy subsumptionHierarchy = new
        // SubsumptionHierarchy(subsumptionChecker);
        // hermit.printFlattenedHierarchy(output, subsumptionHierarchy);
        // System.out.println("ABox is sat: " + result);
    }

    public static void printSubsumptionHierarchyAsText(String physicalURI)
            throws Exception {
        HermiT hermit = new HermiT(physicalURI);
        // hermit.setLoaderType(LoaderType.KAON2);
        // hermit.loadOntology(physicalURI);
        // SubsumptionHierarchy subsumptionHierarchy =
        // hermit.getSubsumptionHierarchy();
        PrintWriter output = new PrintWriter(System.out, true);
        hermit.printSortedAncestorLists(output);
        //        
        // Map<AtomicConcept,Set<AtomicConcept>>
        // flattenedHierarchy=subsumptionHierarchy.getFlattenedHierarchy();
        // org.semanticweb.HermiT.Namespaces namespaces=new
        // org.semanticweb.HermiT.Namespaces();
        // // namespaces.registerPrefix("a",physicalURI+"#");
        // // namespaces.registerInternalPrefixes(physicalURI);
        // // namespaces.registerStandardPrefixes();
        // CharArrayWriter buffer=new CharArrayWriter();
        // PrintWriter output=new PrintWriter(buffer);
        // for (Map.Entry<AtomicConcept,Set<AtomicConcept>> entry :
        // flattenedHierarchy.entrySet()) {
        //output.println(namespaces.abbreviateAsNamespace(entry.getKey().getURI(
        // )));
        // for (AtomicConcept atomicConcept : entry.getValue()) {
        // output.print("    ");
        //output.println(namespaces.abbreviateAsNamespace(atomicConcept.getURI()
        // ));
        // }
        // output.println("-----------------------------------------------");
        // }
        // output.println("! THE END !");
        // output.flush();
        // return buffer.toString();
    }

    public String getSubsumptionHierarchyAsText(String physicalURI)
            throws Exception {
        DefaultOntologyResolver resolver = new DefaultOntologyResolver();
        String ontologyURI = resolver.registerOntology(physicalURI);
        OntologyManager ontologyManager = KAON2Manager.newOntologyManager();
        ontologyManager.setOntologyResolver(resolver);
        Ontology ontology = ontologyManager.openOntology(ontologyURI,
                new HashMap<String, Object>());

        Clausification clausification = new Clausification();
        Set<DescriptionGraph> noDescriptionGraphs = Collections.emptySet();
        DLOntology dlOntology = clausification.clausify(false, ontology,
                noDescriptionGraphs);

        DirectBlockingChecker directBlockingChecker = PairWiseDirectBlockingChecker.INSTANCE;
        BlockingSignatureCache blockingSignatureCache = new BlockingSignatureCache(
                directBlockingChecker);
        BlockingStrategy blockingStrategy = new AnywhereBlocking(
                directBlockingChecker, blockingSignatureCache);
        ExistentialsExpansionStrategy existentialsExpansionStrategy = new CreationOrderStrategy(
                blockingStrategy);
        Tableau tableau = new Tableau(null, existentialsExpansionStrategy,
                dlOntology, new HashMap<String, Object>());

        SubsumptionHierarchy.SubsumptionChecker checker = new TableauSubsumptionChecker(
                tableau);
        SubsumptionHierarchy subsumptionHierarchy = new SubsumptionHierarchy(
                checker);

        Map<AtomicConcept, Set<AtomicConcept>> flattenedHierarchy = subsumptionHierarchy.getFlattenedHierarchy();
        org.semanticweb.HermiT.Namespaces namespaces = new org.semanticweb.HermiT.Namespaces();
        namespaces.registerPrefix("a", ontology.getOntologyURI() + "#");
        namespaces.registerInternalPrefixes(ontology.getOntologyURI());
        namespaces.registerStandardPrefixes();
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter output = new PrintWriter(buffer);
        for (Map.Entry<AtomicConcept, Set<AtomicConcept>> entry : flattenedHierarchy.entrySet()) {
            output.println(namespaces.abbreviateAsNamespace(entry.getKey().getURI()));
            for (AtomicConcept atomicConcept : entry.getValue()) {
                output.print("    ");
                output.println(namespaces.abbreviateAsNamespace(atomicConcept.getURI()));
            }
            output.println("-----------------------------------------------");
        }
        output.println("! THE END !");
        output.flush();
        return buffer.toString();
    }
}
