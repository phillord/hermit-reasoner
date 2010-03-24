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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


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
        // Lets also try and switch on disjunction learning.
        // If set to true, then each disjunct of a disjunction is associated with a punish
        // factor and whenever a disjunct causes a clash, the punish factor is increased.
        // Whenever HermiT has to pick a disjunction, it picks the disjunction with
        // the least punish factor that has not yet been tried for that node and disjunction.
        config.useDisjunctionLearning=true;
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
	}
}
