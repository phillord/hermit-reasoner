package org.semanticweb.HermiT.tableau;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.owlapi.structural.OwlNormalization;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class NormalizationOWLTest extends AbstractHermiTOWLTest {
    
    public NormalizationOWLTest(String name) {
        super(name);
    }
    public void testFirst() throws Exception {
        assertNormalization("res/normalization-1-input.xml","res/normalization-1-OWL-control.xml");
    }
    protected Set<OWLAxiom> getNormalizedAxioms(String resourceName) throws Exception {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology ontology = getOWLOntologyFromResource(resourceName);
        OwlNormalization normalization = new OwlNormalization(factory);
        normalization.processOntology(ontology);
        for (OWLDescription[] inclusion : normalization.getConceptInclusions()) {
            OWLDescription superDescription;
            if (inclusion.length == 1) {
                superDescription = inclusion[0];
            } else {
                superDescription = factory.getOWLObjectUnionOf(inclusion);
            }
            axioms.add(factory.getOWLSubClassAxiom(factory.getOWLThing(), superDescription));
        }
        for (OWLObjectPropertyExpression[] inclusion : normalization.getObjectPropertyInclusions())
            axioms.add(factory.getOWLSubObjectPropertyAxiom(inclusion[0],inclusion[1]));
        axioms.addAll(normalization.getFacts());
        return axioms;
    }
    protected void assertNormalization(String inputResourceName,String controlResourceName) throws Exception {
        Set<OWLAxiom> normlizedAxioms=getNormalizedAxioms(inputResourceName);
        assertEquals(normlizedAxioms,controlResourceName);
    }
}
