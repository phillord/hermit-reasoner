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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


/**
 * This examples demonstrates how HermiT can be used to print the inferred class (object/data property)
 * hierarchy into an ontology in function-style syntax. The ontology will create an axiom SubClassOf(:A :B)
 * if the given ontology entails that :A is a subclass of :B for :A and :B classes (similarly for object 
 * and data properties). The axioms are printed with indentations so that the inferred hierarchy is kind of 
 * visible in the produced ontology file and classes on the same level are ordered alphabetically. 
 * Declarations are printed behind the axioms whenever a class occured for the first time in an axioms.   
 */
public class PrettyHierarchyPrinting {

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
        // Now we can start and create the reasoner. Here we create an instance of HermiT 
        // without any particular configuration given, which means HermiT uses default 
        // parameters for blocking etc.  
	    Reasoner hermit = new Reasoner(manager,ontology);
	    // Now we create an output stream that HermiT can use to write the axioms. The output stream is 
	    // a wrapper around the file into which the axioms are written.   
	    File prettyPrintHierarchyFile=new File("examples/ontologies/pizza-prettyPrint.owl");
	    // turn to an absolute file, so that we can write to it
	    prettyPrintHierarchyFile=prettyPrintHierarchyFile.getAbsoluteFile();
	    OutputStream prettyPrintHierarchyStreamOut=new FileOutputStream(prettyPrintHierarchyFile);
	    // The output stream is wrapped into a print write with autoflush. 
	    PrintWriter output=new PrintWriter(prettyPrintHierarchyStreamOut,true);
	    // Now we let HermiT print the hierarchies. Since all parameters are set to true, 
	    // HermiT will print the class and the object property and the data property hierarchy. 
	    hermit.printHierarchies(output,true,true,true);
	    // Now that file contain an ontology with the inferred axioms and should be in the ontologies 
	    // subfolder (you Java IDE, e.g., Eclipse, might have to refresh its view of files in the file system) 
        // before the file is visible.   
	    System.out.println("The ontology in examples/ontologies/pizza-prettyPrint.owl should now contain all subclass relationships between named classes as SubClassOf axioms pretty printed in functional-style syntax (you might need to refresh the IDE file view). ");
	}
}
