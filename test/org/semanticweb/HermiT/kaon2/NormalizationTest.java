package org.semanticweb.HermiT.kaon2;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.kaon2.structural.NormalizationCrappy;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.ObjectPropertyExpression;

public class NormalizationTest extends AbstractHermiTTest {

    public NormalizationTest(String name) {
        super(name);
    }

    public void testFirst() throws Exception {
        assertNormalization("../res/normalization-1-input.xml",
                "../res/normalization-1-control.xml");
    }

    protected Set<Axiom> getNormalizedAxioms(String resourceName)
            throws Exception {
        Set<Axiom> axioms = new HashSet<Axiom>();
        Ontology ontology = getOntologyFromResource(resourceName);
        NormalizationCrappy normalization = new NormalizationCrappy();
        normalization.processOntology(ontology);
        for (Description[] inclusion : normalization.getConceptInclusions()) {
            Description superDescription;
            if (inclusion.length == 1)
                superDescription = inclusion[0];
            else
                superDescription = KAON2Manager.factory().objectOr(inclusion);
            axioms.add(KAON2Manager.factory().subClassOf(
                    KAON2Manager.factory().thing(), superDescription));
        }
        for (ObjectPropertyExpression[] inclusion : normalization.getNormalObjectPropertyInclusions())
            axioms.add(KAON2Manager.factory().subObjectPropertyOf(inclusion[0],
                    inclusion[1]));
        for (ObjectPropertyExpression[] inclusion : normalization.getInverseObjectPropertyInclusions())
            axioms.add(KAON2Manager.factory().subObjectPropertyOf(inclusion[0],
                    inclusion[1].getInverseObjectProperty()));
        axioms.addAll(normalization.getFacts());
        return axioms;
    }

    protected void assertNormalization(String inputResourceName,
            String controlResourceName) throws Exception {
        Set<Axiom> normlizedAxioms = getNormalizedAxioms(inputResourceName);
        assertEquals(normlizedAxioms, controlResourceName);
    }
}
