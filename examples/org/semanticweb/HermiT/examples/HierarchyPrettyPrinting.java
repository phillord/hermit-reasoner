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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


/**
 * This examples demonstrates how HermiT can be used to print the inferred class (object/data property)
 * hierarchy in function-style syntax. The resulting file will contain an axiom SubClassOf(:A :B)
 * if the given ontology entails that :A is a strict and direct subclass of :B for :A and :B classes 
 * (similarly for object and data properties). Equivalent classes are printed with an EquivalentClasses
 * axiom.   
 * 
 * Printing can be done with either the pretty printer or just as writing out the axioms. With the pretty printer, 
 * the axioms are printed with indentations so that the inferred hierarchy is kind of
 * visible in the produced ontology file and classes on the same level are ordered alphabetically.
 * Declarations are printed behind the axioms whenever a class occured for the first time in an axioms.
 * Furthermore, the output is a complete ontology including an ontology header.  
 * 
 * When just writing out the axioms, they are not indented and in no particular order. The IRIs are not 
 * abbreviated and only within equivalent classes (properties) axioms the IRIs are ordered alphabetically. 
 * Furthermore, the output is not a complete ontology (no ontology header and no declarations), but just 
 * a set of axioms in FSS.   
 */
public class HierarchyPrettyPrinting {

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
	    Reasoner hermit = new Reasoner(ontology);
	    // Now we create an output stream that HermiT can use to write the axioms. The output stream is
	    // a wrapper around the file into which the axioms are written.
	    File prettyPrintHierarchyFile=new File("examples/ontologies/pizza-prettyPrint.owl");
	    if (!prettyPrintHierarchyFile.exists())
            prettyPrintHierarchyFile.createNewFile();
	    File dumpFile=new File("examples/ontologies/pizza-dump.owl");
	    if (!dumpFile.exists())
            dumpFile.createNewFile();
	    // turn to an absolute file, so that we can write to it
	    prettyPrintHierarchyFile=prettyPrintHierarchyFile.getAbsoluteFile();
	    dumpFile=dumpFile.getAbsoluteFile();
	    BufferedOutputStream prettyPrintHierarchyStreamOut=new BufferedOutputStream(new FileOutputStream(prettyPrintHierarchyFile));
	    BufferedOutputStream dumpStreamOut=new BufferedOutputStream(new FileOutputStream(dumpFile));
	    // The output stream is wrapped into a print write with autoflush.
	    PrintWriter output=new PrintWriter(prettyPrintHierarchyStreamOut,true);
	    PrintWriter dumpOutput=new PrintWriter(dumpStreamOut,true);
	    // Now we let HermiT pretty print the hierarchies. Since all parameters are set to true,
	    // HermiT will print the class and the object property and the data property hierarchy.
	    long t=System.currentTimeMillis();
	    hermit.printHierarchies(output,true,true,true);
	    t=System.currentTimeMillis()-t;
	    // Now we let HermiT just dump out the axioms (faster, but not as pretty). Since all 
	    // parameters are set to true, HermiT will print the axioms for class, object property, 
	    // and the data property subsumptions and equivalences.
	    long tDump=System.currentTimeMillis();
        hermit.dumpHierarchies(dumpOutput,true,true,true);
        tDump=System.currentTimeMillis()-tDump;
	    // Now that file contain an ontology with the inferred axioms and should be in the ontologies
	    // subfolder (you Java IDE, e.g., Eclipse, might have to refresh its view of files in the file system)
        // before the file is visible.
	    System.out.println("The ontology in examples/ontologies/pizza-prettyPrint.owl should now contain all subclass relationships between named classes as SubClassOf axioms pretty printed in functional-style syntax. ");
	    System.out.println("The ontology in examples/ontologies/pizza-dump.owl should now contain all relevant axioms for class and property subsumptions in functional-style syntax. ");
	    System.out.println("You might need to refresh the IDE file view.");
	    System.out.println("Pretty printing took: "+t+"ms, dumping took: "+tDump+"ms.");
	}
}
