package org.semanticweb.HermiT.reasoner;

import static org.junit.Assert.assertEquals;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class InverseAnonymousTest {
    String input = "Prefix: : <http://example.org/>\n" + "        Ontology: <http://asdf>\n"
            + "        ObjectProperty: hasBigPart\n" + "            SubPropertyOf: hasPart, inverse (partOf)\n"
            + "        ObjectProperty: hasPart\n" + "            InverseOf:  partOf\n"
            + "        ObjectProperty: partOf\n" + "            InverseOf: hasPart";
    OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    OWLDataFactory df = man.getOWLDataFactory();
    OWLObjectProperty bigPart = df.getOWLObjectProperty(IRI.create("http://example.org/hasBigPart"));
    OWLObjectProperty partOf = df.getOWLObjectProperty(IRI.create("http://example.org/partOf"));
    OWLObjectProperty hasPart = df.getOWLObjectProperty(IRI.create("http://example.org/hasPart"));
    OWLObjectPropertyExpression invPartOf = df.getOWLObjectInverseOf(partOf);
    OWLObjectPropertyExpression invHasPart = df.getOWLObjectInverseOf(hasPart);
    Set<OWLObjectPropertyExpression> expectedHasPart = new HashSet<>(Arrays.asList(invPartOf, hasPart));
    Set<OWLObjectPropertyExpression> expectedPartOf = new HashSet<>(Arrays.asList(invHasPart, partOf));
    Set<OWLObjectPropertyExpression> expectedBigPart = Collections.singleton(bigPart);

    protected void assertExpectedEquivalencies(OWLReasoner reasoner) {
        assertEquals(expectedBigPart, asSet(reasoner.getEquivalentObjectProperties(bigPart).entities()));
        assertEquals(expectedHasPart, asSet(reasoner.getEquivalentObjectProperties(hasPart).entities()));
        assertEquals(expectedPartOf, asSet(reasoner.getEquivalentObjectProperties(partOf).entities()));
    }

    protected OWLReasoner reason(OWLOntology ont) {
        OWLReasoner reasoner = new ReasonerFactory().createReasoner(ont);
        return reasoner;
    }

    @Test
    public void shouldFindBigPartSingletonWithoutTransitiveCharacteristic() throws OWLOntologyCreationException {
        OWLOntology ont = man.loadOntologyFromOntologyDocument(new StringDocumentSource(input));
        OWLReasoner reasoner = reason(ont);
        assertExpectedEquivalencies(reasoner);
    }

    @Test
    public void shouldFindBigPartSingletonWithTransitiveCharacteristic() throws OWLOntologyCreationException {
        String in = input + "\n            Characteristics: Transitive";
        OWLOntology ont = man.loadOntologyFromOntologyDocument(new StringDocumentSource(in));
        OWLReasoner reasoner = new ReasonerFactory().createReasoner(ont);
        assertExpectedEquivalencies(reasoner);
    }
}
