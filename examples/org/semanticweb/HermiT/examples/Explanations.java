package org.semanticweb.HermiT.examples;

import java.io.File;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;

public class Explanations {

    public static void main(String[] args) throws Exception {
        // First, we create an OWLOntologyManager object. The manager will load and 
        // save ontologies. 
        OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
        // We will create several things, so we save an instance of the data factory
        OWLDataFactory dataFactory=manager.getOWLDataFactory();
        // Now, we create the file from which the ontology will be loaded. 
        // Here the ontology is stored in a file locally in the ontologies subfolder
        // of the examples folder.
        File inputOntologyFile = new File("examples/ontologies/pizza.owl");
        // We use the OWL API to load the ontology. 
        OWLOntology ontology=manager.loadOntologyFromOntologyDocument(inputOntologyFile);
        
        // Lets make things worth and turn Pizza into an inconsistent ontology by asserting that the 
        // unsatisfiable icecream class has some instance. 
        // First, create an instance of the OWLClass object for the unsatisfiable  icecream class.
        IRI icecreamIRI=IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#IceCream");
        OWLClass icecream=dataFactory.getOWLClass(icecreamIRI);
        // Now we can start and create the reasoner. Since explanation is not natively supported by 
        // HermiT and is realised in the OWL API, we need to instantiate HermiT 
        // as an OWLReasoner. This is done via a ReasonerFactory object. 
        ReasonerFactory factory = new ReasonerFactory();
        // We don't want HermiT to thrown an exception for inconsistent ontologies because then we 
        // can't explain the inconsistency. This can be controlled via a configuration setting.  
        Configuration configuration=new Configuration();
        configuration.throwInconsistentOntologyException=false;
        // The factory can now be used to obtain an instance of HermiT as an OWLReasoner. 
        OWLReasoner reasoner=factory.createReasoner(ontology, configuration);
        // Let us confirm that icecream is indeed unsatisfiable:
        System.out.println("Is icecream satisfiable? "+reasoner.isSatisfiable(icecream)); 
        System.out.println("Computing explanations...");
        // Now we instantiate the explanation classes
        BlackBoxExplanation exp=new BlackBoxExplanation(ontology, factory, reasoner);
        HSTExplanationGenerator multExplanator=new HSTExplanationGenerator(exp);
        // Now we can get explanations for the unsatisfiability. 
        Set<Set<OWLAxiom>> explanations=multExplanator.getExplanations(icecream);
        // Let us print them. Each explanation is one possible set of axioms that cause the 
        // unsatisfiability. 
        for (Set<OWLAxiom> explanation : explanations) {
            System.out.println("------------------");
            System.out.println("Axioms causing the unsatisfiability: ");
            for (OWLAxiom causingAxiom : explanation) {
                System.out.println(causingAxiom);
            }
            System.out.println("------------------");
        }
        // Let us make the ontology inconsistent to also get explanations for an 
        // inconsistency, which is slightly more involved since we dynamically 
        // have to change the factory constructor; otherwise, we can't suppress 
        // the inconsistent ontology exceptions that the OWL API requires a 
        // reasoner to throw.  
        // Let's start by adding a dummy individual to the unsatisfiable Icecream class. 
        // This will cause an inconsistency. 
        OWLAxiom ax=dataFactory.getOWLClassAssertionAxiom(icecream, dataFactory.getOWLNamedIndividual(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#dummyIndividual")));
        manager.addAxiom(ontology, ax);
        // Let us confirm that the ontology is inconsistent
        reasoner=factory.createReasoner(ontology, configuration);
        System.out.println("Is the changed ontology consistent? "+reasoner.isConsistent());
        // Ok, here we go. Let's see why the ontology is inconsistent. 
        System.out.println("Computing explanations for the inconsistency...");
        factory=new Reasoner.ReasonerFactory() {
            protected OWLReasoner createHermiTOWLReasoner(org.semanticweb.HermiT.Configuration configuration,OWLOntology ontology) {
                // don't throw an exception since otherwise we cannot compte explanations 
                configuration.throwInconsistentOntologyException=false;
                return new Reasoner(configuration,ontology);
            }  
        };
        exp=new BlackBoxExplanation(ontology, factory, reasoner);
        multExplanator=new HSTExplanationGenerator(exp);
        // Now we can get explanations for the inconsistency 
        explanations=multExplanator.getExplanations(dataFactory.getOWLThing());
        // Let us print them. Each explanation is one possible set of axioms that cause the 
        // unsatisfiability. 
        for (Set<OWLAxiom> explanation : explanations) {
            System.out.println("------------------");
            System.out.println("Axioms causing the inconsistency: ");
            for (OWLAxiom causingAxiom : explanation) {
                System.out.println(causingAxiom);
            }
            System.out.println("------------------");
        }
    }
}
