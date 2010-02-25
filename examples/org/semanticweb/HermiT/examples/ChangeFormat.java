package org.semanticweb.HermiT.examples;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ChangeFormat {

	public static void main(String[] args) throws Exception {
		// First, we create an OWLOntologyManager object. The manager will load and 
    	// save ontologies. 
        OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
        // Now, we create the file from which the ontology will be loaded. 
    	// Here the ontology is stored in a file locally in the ontologies subfolder
    	// of the examples folder.
        File inputOntologyFile = new File("examples/ontologies/pizza.owl");
        // We use the OWL API to load the ontology. 
        OWLOntology ontology=manager.loadOntologyFromOntologyDocument(inputOntologyFile);
        // Now the axioms should be saved in a different format, say functional style syntax. 
        // We do this by (virtually) creating a file with a relative path from which we get 
        // the absolute file.  
        File newOntologyFile=new File("examples/ontologies/pizza.fss.owl");
        newOntologyFile=newOntologyFile.getAbsoluteFile();
        // Now we create a buffered stream since the ontology manager can then write to that stream. 
        BufferedOutputStream outputStream=new BufferedOutputStream(new FileOutputStream(newOntologyFile));
        // We use the same format as for the input ontology.
        manager.saveOntology(ontology, new OWLFunctionalSyntaxOntologyFormat(), outputStream);
        // Now the ontology should be in the ontologies subfolder (you Java IDE, e.g., Eclipse, 
        // might have to refresh its view of files in the file system) before the file is visible.  
        System.out.println("The ontology in examples/ontologies/pizza.fss.owl should now contain all axioms from pizza.owl in functional style syntax (you might need to refresh the IDE file view). ");
	}

}
