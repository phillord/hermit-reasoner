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

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Configuration.TableauMonitorType;
import org.semanticweb.HermiT.monitor.CountingMonitor;
import org.semanticweb.HermiT.monitor.CountingMonitor.TestRecord;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription.StandardTestType;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;


/**
 * This examples demonstrates how HermiT can be used with custom configurations.
 */
public class HermiTConfigurations {

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
        // Now we create a configuration object that we use to overwrite HermiT's default
        // settings.
        Configuration config=new Configuration();
        // Lets make HermiT show information about the tableau for each reasoning task at the
        // start and end of a task and in certain time intervals.
        config.tableauMonitorType=TableauMonitorType.TIMING;
        // Now we can start and create the reasoner with the above created configuration.
	    Reasoner hermit = new Reasoner(config,ontology);
	    // Lets see whether HermiT finds that the icecream class in the pizza ontology is unsatisfiable
	    // (I know it is).
	    // First, create an instance of the OWLClass object for the icecream class.
	    IRI icecreamIRI=IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#IceCream");
	    OWLClass owlClass=manager.getOWLDataFactory().getOWLClass(icecreamIRI);
	    // Since we have used the timing monitor HermiT will print some information while it
	    // is doing the reasoning and then it will print the result as instructed below.
	    System.out.println("Is the icecream class satisfiable? "+hermit.isSatisfiable(owlClass));
	    System.out.println("--------------------------");
	    // Lets also try and make HermiT count how many satisfiability and subsumption tests 
        // it made and how long tests take on average. 
	    config=new Configuration();
        CountingMonitor countingMonitor=new CountingMonitor();
        config.monitor=countingMonitor;
        // Now we can start and create the reasoner with the above created configuration.
        hermit = new Reasoner(config,ontology);
        // Let's classify the ontology and see which were the 2 hardest satisfiability 
        // and subsumption tests that HermiT performed during the classification. 
        hermit.precomputeInferences(InferenceType.CLASS_HIERARCHY);
	    System.out.println("HermiT did "+countingMonitor.getOverallNumberOfTests()+" tests. ");
	    System.out.println("This took "+countingMonitor.getOverallTime()+" ms. ");
	    System.out.println("The last test took "+countingMonitor.getTime()+" ms. ");
	    System.out.println("The last model contained "+countingMonitor.getNumberOfNodes()+" nodes/individuals. ");
        System.out.println("The 2 hardest satisfiability tests were:");
        for (TestRecord record : countingMonitor.getTimeSortedTestRecords(2,StandardTestType.CONCEPT_SATISFIABILITY)) 
            System.out.println(record.toString());
        System.out.println("The 2 hardest subsumption tests were:");
        for (TestRecord record : countingMonitor.getTimeSortedTestRecords(2,StandardTestType.CONCEPT_SUBSUMPTION)) 
            System.out.println(record.toString());
	}
}
