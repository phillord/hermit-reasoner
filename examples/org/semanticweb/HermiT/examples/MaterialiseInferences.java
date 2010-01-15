/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory
   
   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.semanticweb.HermiT.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;

/**
 * This example Shows how to use HermiT as an OWLReasoner for materialising inferences. 
 * The program loads the pizza ontology, computes implicit subclass relaionships and 
 * class assertion axioms and saves them into a new ontology in the same format as the 
 * input ontology. Further inferences can be added by adding more InferredAxiomGenerators.  
 */
public class MaterialiseInferences {
   
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
        // Now we can start and create the reasoner. Since materialisation of axioms is controlled 
        // by OWL API classes and is not natively supported by HermiT, we need to instantiate HermiT 
        // as an OWLReasoner. This is done via a ReasonerFactory object. 
        ReasonerFactory factory = new ReasonerFactory();
        // The factory can now be used to obtain an instance of HermiT as an OWLReasoner. 
        // Here buffering is switched on (createBufferedReasoner), although for this example  
        // that is not really required since we do not add any axioms to the ontology that is 
        // loaded in HermiT. Buffering means that additions are not handled when they occur, but 
        // only when the flush method is called, which is more efficient. 
        OWLReasoner reasoner=factory.createBufferedReasoner(ontology);
        // The call to prepareReasoner() is not required. It causes HermiT to compute the class, object, 
        // and data property hierarchies, which would otherwise been done only if required. 
        reasoner.prepareReasoner();
        // We now have to decide which kinds of inferences we want to compute. For different types 
        // there are different InferredAxiomGenerator implementations available in the OWL API and 
        // we use the InferredSubClassAxiomGenerator and the InferredClassAssertionAxiomGenerator 
        // here. The different generators are added to a list that is then passed to an 
        // InferredOntologyGenerator. 
        List<InferredAxiomGenerator<? extends OWLAxiom>> generators=new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
        generators.add(new InferredSubClassAxiomGenerator());
        generators.add(new InferredClassAssertionAxiomGenerator());
        // We can now create an instance of InferredOntologyGenerator. 
        InferredOntologyGenerator iog=new InferredOntologyGenerator(reasoner,generators);
        // Before we actually generate the axioms into an ontology, we first have to create that ontology. 
        // First, we create an IRI for the ontology into which the computed axioms will be saved. 
        // We do this by (virtually) creating a file with a relative path from which we get the absolute IRI. 
        // Since we cannot write to relative IRIs, we have to resolve the relative path to an absolute one 
        // in an OS independent form.  
        File inferredOntologyFile=new File("examples/ontologies/pizza-inferred.owl");
        // Calling toURI() before calling toURL() ensures that characters are escaped as required for URLs. 
        IRI inferredAxiomsOntologyIRI=IRI.create(inferredOntologyFile.toURI().toURL().toExternalForm());
        // The manager creates the for now empty ontology for the inferred axioms for us. 
        OWLOntology inferredAxiomsOntology=manager.createOntology(inferredAxiomsOntologyIRI);
        // Now we use the inferred ontology generator to fill the ontology. That might take some 
        // time since it involves possibly a lot of calls to the reasoner.    
        iog.fillOntology(manager, inferredAxiomsOntology);
        // Now the axioms are computed and added to the ontology, but we still have to save 
        // the ontology into a file. We use the same format as for the input ontology. 
        manager.saveOntology(inferredAxiomsOntology, manager.getOntologyFormat(ontology));
        // Now that ontology that contains the inferred axioms should be in the ontologies subfolder 
        // (you Java IDE, e.g., Eclipse, might have to refresh its view of files in the file system) 
        // before the file is visible.  
    }
}