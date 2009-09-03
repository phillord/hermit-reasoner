HermiT is a conformant OWL 2 DL reasoner that uses the direct semantics. It
supports all OWL2 DL constructs and the datatypes required by the OWL
2 specification.  

HermiT is free software: you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but without
any warranty; without even the implied warranty of merchantability or fitness
for a particular purpose. See the GNU General Public License for more details.

A copy of the GNU General Public License has been included with this
distribution in the file `license.txt`. An online version is available at
<http://www.gnu.org/licenses/>.

More information about HermiT and additional licensing information is
available at <http://hermit-reasoner.com>, or by contacting Boris Motik or 
Ian Horrocks at the Oxford University Computing Laboratory.

HermiT uses the following libraries in unmodified form:

1) dk.brics.automaton, Copyright (C) 2001-2009 Anders Moeller
   http://www.brics.dk/automaton/
   released under BSD license, see dk.brics.automaton.COPYING and 
   dk.brics.automaton.README in project/lib 
2) JAutomata, 
   http://jautomata.sourceforge.net/
   released under LGPL 2.1, see jautomata.LICENSE in project/lib
3) The OWL API, 
   http://owlapi.sourceforge.net
   released under LGPL 3.0, see owlapi.LICENSE in project/lib

The release is organised as follows:
HermiT.jar is a stand-alone version of HermiT that can be used from the command 
line or from within other Java programs. It contains all required libraries.
org.semanticweb.HermiT.jar is a plugin for Protege version 4.1. It can be copied 
into the plugins folder of Protege. 
project/* is the Java Eclipse project for HermiT. It contains all sources 
(folders src, test, and getopt) and libraries (folder lib) incl. sources and can 
directly be imported into Eclipse.  