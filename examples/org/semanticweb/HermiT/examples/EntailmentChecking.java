package org.semanticweb.HermiT.examples;

import java.io.File;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.SimpleRenderer;

public class EntailmentChecking {

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
        
        // First, create several OWL API objects that we will use in our queries
        OWLClass margherita=dataFactory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Margherita"));
        OWLObjectProperty hasTopping=dataFactory.getOWLObjectProperty(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#hasTopping"));
        OWLClass mozzarellaTopping=dataFactory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#MozzarellaTopping"));
        OWLClass goatsCheeseTopping=dataFactory.getOWLClass(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#GoatsCheeseTopping"));
        OWLClassExpression mozarellaOrGoatsCheese=dataFactory.getOWLObjectUnionOf(mozzarellaTopping, goatsCheeseTopping);
        OWLClassExpression hasToppingMozarellaOrGoatsCheese=dataFactory.getOWLObjectSomeValuesFrom(hasTopping, mozarellaOrGoatsCheese);
        OWLAxiom axiom=dataFactory.getOWLSubClassOfAxiom(margherita, hasToppingMozarellaOrGoatsCheese);
        
        // Now we can start and create the reasoner. Lets this time use HermiT's native interface.
        // For this we don't need a factory.
        // The OWLReasoner interface is very similar though, it just has fewer methods
        Reasoner reasoner=new Reasoner(ontology);
        // Let us check whether the axiom is entailed:
        System.out.println("Do margherita pizzas have a topping that is morzarella or goats cheese? "+reasoner.isEntailed(axiom)); 
        
        // Let us now also see what other (named) subclasses the complex superclass has
        // Setting the boolean flag to false means we are not only interested in direct subclasses
        // but also indirect ones
        // For printing the classes we want to make use of the abbreviations defined in the
        // ontology. We can set a suitable renderer in the OWL API that will then abbreviate 
        // long IRIs for which there as a prefix declared in the ontology.
        SimpleRenderer renderer=new SimpleRenderer();
        renderer.setPrefixesFromOntologyFormat(ontology, manager, true);
        ToStringRenderer.getInstance().setRenderer(renderer);
        
        NodeSet<OWLClass> subs=reasoner.getSubClasses(hasToppingMozarellaOrGoatsCheese, false);
        System.out.println("Subclasses of the complex class: ");
        for (Node<OWLClass> equivalents : subs.getNodes()) {
            // The node set contains several sets of classes
            // Each set contains classes that are equivalent to each other 
            // (if there are any otherwise it is a singleton set)
            // here only owl:Nothing, which is a subclass of every class
            // has some eqivalents (other unsatisfiable classes) 
            for (OWLClass equivalent : equivalents) {
                System.out.print(equivalent+" ");
            }
            System.out.println();
        }
    }
}
