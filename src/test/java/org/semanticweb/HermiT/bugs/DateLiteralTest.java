package org.semanticweb.HermiT.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.HermiT.datatypes.datetime.DateTime;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class DateLiteralTest {
    @Test
    public void shouldTruncateToMillisecondsButAccebtArbitraryPrecision() throws OWLOntologyCreationException {
        // DateTime uses millisecond precision, regardless of precision of
        // input.
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();
        OWLOntology ont = man.createOntology(IRI.create("urn:test:date"));
        String lexicalValue = "2018-11-01T00:44:58.37";
        ont.add(df.getOWLDataPropertyAssertionAxiom(df.getOWLDataProperty(IRI.create("urn:test:p")),
                df.getOWLNamedIndividual("urn:test:i"),
                df.getOWLLiteral(lexicalValue + "0000", OWL2Datatype.XSD_DATE_TIME)));

        OWLReasoner reasoner = new ReasonerFactory().createReasoner(ont);
        assertTrue(reasoner.isConsistent());

        DateTime dateTime1 = DateTime.parse(lexicalValue);
        DateTime dateTime2 = DateTime.parse(lexicalValue + "0101");
        assertEquals(dateTime1, dateTime2);
        assertEquals("2018-10-32T00:44:58.370", dateTime2.toString());
    }
}
