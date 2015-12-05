/* Copyright 2009 by the Oxford University Computing Laboratory

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
// An update for the tests (all.rdf) should regularly be downloaded to the
// ontologies folder from http://wiki.webont.org/exports/
package org.semanticweb.HermiT.owl_wg_tests;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class WGTestRegistry {
    public static String URI_BASE="http://www.w3.org/2007/OWL/testOntology#";
    public static String TEST_ID_PREFIX="http://owl.semanticweb.org/id/";

    protected final OWLOntologyManager m_ontologyManager;
    protected final OWLOntology m_testContainer;
    protected final List<WGTestDescriptor> m_testDescriptors;

    public WGTestRegistry() throws Exception {
        m_ontologyManager=OWLManager.createOWLOntologyManager();
        m_ontologyManager.loadOntologyFromOntologyDocument(IRI.create(WGTestRegistry.class.getResource("ontologies/test-ontology.owl").toURI()));
        m_testContainer=m_ontologyManager.loadOntologyFromOntologyDocument(IRI.create(WGTestRegistry.class.getResource("ontologies/all.rdf").toURI()));
        m_testDescriptors=new ArrayList<WGTestDescriptor>();
        OWLClass testCaseClass=m_ontologyManager.getOWLDataFactory().getOWLClass(IRI.create(URI_BASE+"TestCase"));
        for (OWLClassAssertionAxiom ax : m_testContainer.getClassAssertionAxioms(testCaseClass)) {
            WGTestDescriptor wgTestDescriptor=new WGTestDescriptor(m_ontologyManager,m_testContainer,ax.getIndividual());
            m_testDescriptors.add(wgTestDescriptor);
        }
    }
    public List<WGTestDescriptor> getTestDescriptors() {
        return m_testDescriptors;
    }
    public WGTestDescriptor getDescriptorByIdentifier(String identifier) throws Exception {
        for (WGTestDescriptor testDescriptor : m_testDescriptors)
            if (identifier.equals(testDescriptor.identifier))
                return testDescriptor;
        return null;
    }
}
