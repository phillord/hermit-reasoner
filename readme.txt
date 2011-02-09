HermiT is a conformant OWL 2 DL reasoner that uses the direct semantics. It
supports all OWL2 DL constructs and the datatypes required by the OWL
2 specification.  

HermiT is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
   
HermiT is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.
   
Copies of the GNU General Public License and the GNU Lesser General Public 
License have been included with this distribution in the file `gpl.txt` and 
lgpl-3.0.txt, respectively. An online version is available at
<http://www.gnu.org/licenses/>.

More information about HermiT and additional licensing information is
available at <http://hermit-reasoner.com>, or by contacting Boris Motik or 
Ian Horrocks at the Oxford University Computing Laboratory.

HermiT uses the following libraries in unmodified form:

1) dk.brics.automaton, Copyright (C) 2001-2009 Anders Moeller
   http://www.brics.dk/automaton/
   released under BSD license, see dk.brics.automaton.COPYING and 
   dk.brics.automaton.README in project/lib or in the Protege 
   plug-in jar file 
2) JAutomata, 
   http://jautomata.sourceforge.net/
   released under LGPL 2.1, see jautomata.LICENSE in project/lib or 
   in the Protege plug-in jar file
3) The OWL API, 
   http://owlapi.sourceforge.net
   released under LGPL 3.0, see owlapi.LICENSE in project/lib or in 
   the Protege plug-in jar file
4) Apache Axiom,
   http://ws.apache.org/commons/axiom/ 
   released under The Apache Software License, Version 2.0, see 
   axiom-1.2.8.LICENSE in project/lib or in the Protege plug-in jar 
   file
5) Apache Commons Logging,
   http://commons.apache.org/logging/ 
   released under The Apache Software License, Version 2.0, see 
   commons-logging.LICENSE in project/lib or in the Protege 
   plug-in jar file

The release is organised as follows:
HermiT.jar is a stand-alone version of HermiT that can be used from the command 
line or from within other Java programs. It contains all required libraries.
org.semanticweb.HermiT.jar is a plug-in for Protege version 4.1. It can be copied 
into the plugins folder of Protege. 
project/* is the Java Eclipse project for HermiT. It contains all sources 
(folders src, test, examples, and getopt) and libraries (folder lib) incl. sources 
and can directly be imported into Eclipse.  

HermiT 1.0 
- supports all of OWL 2
HermiT 1.1 
- supports DL-safe SWRL rules
HermiT 1.2 
- contains a novel blocking strategy
- minor bug fixes (nominals and equality statements in the ABox were not handled 
  correctly in some case)
HermiT 1.2.1 
- minor bug fixes (nominals and equality statements in the ABox were still not 
  handled correctly if the ontology was nondeterministic, custom data ranges were not 
  loaded correctly in some case)
HermiT 1.2.2
- minor bug fixes (complex concept queries didn't work properly in the 
  presence of role chains and transitivity), added a progress monitor for realisation
  that is used by Protege
HermiT 1.2.3
- several bug fixes (getDisjointObjectProperties(...) and 
  getDisjointDataProperties(...) were not terminating, a null pointer exception in 
  getDataPropertyValues(), language tags were case sensitive, ontology change listener 
  was not removed on dispose), new concept classification algorithm for 
  non-deterministic ontologies, new object property classification, HermiT will now 
  always track changes even when not instantiated as an OWLReasoner, Protege reasoner 
  preferences will be taken into account for the next Protege release   
HermiT 1.2.4
- some bug fixes (bottomObjectProperty encoding corrected, blocking cache cached 
  inactive nodes under single blocking, correct INF/-INF parsing for doubles, no empty 
  node sets in case the next node in the property hierarchy has only inverses), 
  improved property instance retrieval, new disjunction ordering strategy, negative 
  object property assertion encoding no longer requires nominals, command line 
  outputs full IRIs instead of abbreviated ones with HermiT's prefixes, apply changes 
  in non-buffering mode only when needed for a query, data values are stored also 
  with their lexical form, new data property classification
HermiT 1.2.5
- improved instance handling (class and object property instances, sameAs), new OWL 
  API 3.1, never officially release, but shipped with Protege's beta test version
  for the new OWL API 
HermiT 1.3.0
- as the inofficial 1.2.5, fixed a null pointer exception in the instance manager
HermiT 1.3.1
- fixed a bug that caused HermiT to return equivalent properties instead of inverse 
  properties in the method getInverseObjectProperties, automata for complex roles 
  are no longer determinised to safe time, extended explanation example to cover
  inconsistency explanation, improved disjoint classes code in the materialisation 
  example, anonymous individuals are no longer allowed in OneOf constructs (OWL 2 
  DL conformance)
HermiT 1.3.2
- fixed a bug in the classification (in the case of several unsatisfiable classes, 
  HermiT skipped satisfiability tests for some of them and wrongly considered them 
  satisfiable), fixed a bug in object property instance retrieval that left out 
  related individuals if merging has occurred due to number restrictions, made 
  instance manager work also with unknown individuals (fresh entities), complex 
  data ranges are no longer simulated via concepts, but have their own 
  representations to fix a bug with rdfs:Literal
HermiT 1.3.3
- New bug-fix release of the OWL API is used (3.2.1). Added a method to just write 
  out axioms for the transitively reduced (class/object property/dataproperty) 
  hierarchy, much faster than pretty printing but no longer a complete ontology 
  (missing header and declarations) and not nicely ordered and indented. Only 
  equivalent classes (properties) are ordered in the according axiom. The 
  command line by default now just writes out the axioms, but pretty printing can 
  be enforced with the --prettyPrint argument. Command line interface has been 
  slightly tidied up and extended.  
  precomputeInferences() now silently ignores unsupported tasks and disjointClasses 
  and data property assertions are no longer precomputable via the OWLReasoner 
  interface (disj. classes too slow, data property assertions are anyway only using 
  the data property hierarchy and sameAs individuals). Disj. classes can still be 
  precomputed when using HermiT's native interface (Reasoner).    
  Fixed a bug that sometimes made sameAs computation hang.
  