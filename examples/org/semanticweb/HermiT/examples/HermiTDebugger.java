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
 * This examples demonstrates how HermiT's debugger can be used to see models or reasons 
 * for a clash. Note that this uses mainly an internal interface for HermiT that we use 
 * occasionally for debugging purposes. It assumes some understanding of hypertablau, 
 * normalisation, and structural transformation and is not meant as a general user 
 * interface for HermiT. Nevertheless it might be useful to some users, which is why we 
 * give an example of its use. No further support can, however, be given for this 
 * HermiT interface.  
 */
public class HermiTDebugger {

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
        // Lets make HermiT open a debugger window from which we can control the 
        // further actions that HermiT performs. 
        // DEBUGGER_HISTORY_ON will cause HermiT to save the deriviation tree for 
        // each derived conclusion. 
        // DEBUGGER_NO_HISTORY will not save the derivation tree, so no causes for a 
        // clash can be given, but the memory requirement is smaller. 
        config.tableauMonitorType=TableauMonitorType.DEBUGGER_HISTORY_ON;
        // Now we can start and create the reasoner with the above created configuration.
        Reasoner hermit = new Reasoner(config,ontology);
        // This will open the debugger window at runtime and it should say:
        // Good morning Dr. Chandra. This is HAL. I'm ready for my first lesson.
        // Derivation history is on.
        // Reasoning task started: ABox satisfiability
        // > 
        // you can press 'c' to make HermiT continue with checking whether the ontology 
        // is consistent (ABox is satisfiable). HermiT should then say:
        // Reasoning task finished: true
        // > 
        // I.e., the ontlogy is consistent. Now, HermiT will work on the reasoning 
        // tasks that are specified below. 
	    // The debugger window cannot be used to tell HermiT which tests should be performed. 
	    // This has to be done via Java. First, we will ask HermiT to see whether the 
	    // Siciliana pizza is satisfiable (it is).
	    // Once HermiT is done with testing consistency, you can type 'c' again to make 
	    // HermiT start testing the satisfiability of the Siciliana pizza.
        // HermiT will tell you that it started, but before really deriving anything,
        // HermiT waits again to see whether you want to set further breakpoints, etc, but 
        // just type 'c' again for now. 
	    IRI sicilianaIRI=IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Siciliana");
	    OWLClass siciliana=manager.getOWLDataFactory().getOWLClass(sicilianaIRI);
	    hermit.isSatisfiable(siciliana);
	    // HermiT should now have said 'Reasoning task finished: true' in the debugger window. 
	    // Now, you can type 'showModel' to see all the assertions in the ABox that HermiT generated. 
	    // You should see several assertions of the form ':AnchoviesTopping[8]' or 
	    // ':hasIngredient[6,7]'. This means that in the model abstractions that HermiT 
	    // constructed there is an individual '8' that belongs to the class AnchoviesTopping and 
	    // the individual '6' is related to the individual '7' via the property 'hasIngredient'. 
	    // There are also assertions of the form 'all:n[m]', where 'm' is an individual in the 
	    // model abstraction and 'all:n' with n an integer is a HermiT internal concept. Similarly 
	    // there are internal concepts of the form nnq:n, def:n, and nom2:xxx, where xxx is a 
	    // nominal in the ontology. 
	    // You can also type 'showSubtree 6' to see the part of the model for the siciliana pizza. 
	    // You have to ask for individul '6' because there are 5 nominals and we are not 
	    // interested in the subtrees rooted in the nominals. If there are n nominals in the 
	    // ontology, individuals 1...n will be nominal nodes as you can also see from the 
	    // assertions of the form nom2:xxx[n]. 
	    // The showSubtree command uses a kind of folder view to visualise the part of the 
	    // model abstraction. The colors of the nodes have the following meaning: 
	    // black: root node
        // green: blockable node (not blocked)
        // light gray: inactive node
        // cyan: blocked node
        // red: node with unprocessed existentials
        // magenta: description graph node
        // blue: concrete/data value node

	    // Lets continue and see whether HermiT finds that the CheeseyVegetableTopping class is 
	    // unsatisfiable (I know it is).
	    IRI cheeseyVegetableToppingIRI=IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#CheeseyVegetableTopping");
	    OWLClass cheeseyVegetableTopping=manager.getOWLDataFactory().getOWLClass(cheeseyVegetableToppingIRI);
        hermit.isSatisfiable(cheeseyVegetableTopping);
        // Type 'c' twice to start the task and continue to the end. 
        // You can still type 'showModel', but what you will get is just the last state of the 
        // model abstraction before HermiT had to give up because there is a clash and 
        // no further backtracking is possible.
        // You can now type 'dertree clash'. This opens a window that shows the derivation 
        // history of the clash. This uses HermiT's internal clause form, so you might not 
        // recognise the axioms in you ontology anymore. The original axioms are modified by 
        // HermiT to get to the internal clause form, e.g., A implies B and C 
        // (SubClassOf(:A ObjectIntersectionOf(:B :C))) becomes
        // A(x) -> B(x) and A(x) -> C(x), written in the debugger as 
        // B(x) :- A(x) and C(x) :- A(x)
        // More complex axioms are split even further, e.g., A implies exists r.(exists s.B)
        // (SubClassOf(:A ObjectSomeValuesFrom(:r ObjectSomeValuesFrom(:s :B))))
        // becomes
        // >=1r.Q(x) :- A(x) and >=1s.B(x) :- Q(x)
        // with Q a fresh concept/class. 
        // In the derivation history you will see something like:
        // (yellow icon) :CheeseTopping(6) <-- :CheeseTopping(X) :- :CheesyVegetableTopping(X)
        //    (pink icon) :CheesyVegetableTopping(6)
        // This means that the base fact (pink) ':CheesyVegetableTopping(6)' (this fact was 
        // given because we test the satisfiability of CheesyVegetableTopping and 6 is the 
        // first non-nominal individua) has been used in the clause 
        // ':CheeseTopping(X) :- :CheesyVegetableTopping(X)'
        // to derive
        // ':CheeseTopping(6)'
        // Similarly, the fact 
        // ':VegetableTopping(6)'
        // was derived. Now, the clause
        // ' :- :CheeseTopping(x), :VegetableTopping(x)'
        // can be used to derive a clash. The clause says that CheeseTopping and 
        // VegetableTopping disjoint, i.e., they imply owl:Nothing/bottom/a clash as denoted 
        // by the empty head of the clause. 
        // The color codes in te derivation tree view have the following meaning: 
        // yellow: DL clause application
        // cyan: disjunct application (choose and apply a disjunct)
        // blue: merged two nodes
        // dark grey: description graph checking
        // black: clash
        // red: existential expansion
        // magenta: base/given fact
        
        // If you want more debugging adventures, try to type 'help' in the debugger window
        // to see more commands and options that can be used. Otherwise just close the window 
        // or type 'exit'. Have fun with HermiT!
	}
}
