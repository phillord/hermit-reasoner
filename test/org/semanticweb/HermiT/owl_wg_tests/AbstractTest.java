package org.semanticweb.HermiT.owl_wg_tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public abstract class AbstractTest extends junit.framework.TestCase {

    protected enum Status {APPROVED, REJECTED, PROPOSED}
    protected enum Semantics {DL, FULL}
    public enum TestType {CONSISTENCY, INCONSISTENCY, POSITIVE_ENTAILMENT, NEGATIVE_ENTAILMENT}
    public enum SerializationFormat {
        FUNCTIONAL("FUNCTIONAL", "fsPremiseOntology", "fsConclusionOntology", "fsNonConclusionOntology"),
        OWLXML("OWLXML","owlXmlPremiseOntology", "owlXmlConclusionOntology", "owlXmlNonConclusionOntology"), 
        RDFXML("RDFXML", "rdfXmlPremiseOntology", "rdfXmlConclusionOntology", "rdfXmlNonConclusionOntology");

        protected final OWLIndividual ind;
        protected final OWLDataProperty premise;
        protected final OWLDataProperty conclusion;
        protected final OWLDataProperty nonconclusion;

        private SerializationFormat(String indURI, String premiseURI, String conclusionURI, String nonconclusionURI) {
            OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();    
            this.ind = df.getOWLIndividual(URI.create(WGJunitTests.URI_BASE + indURI));
            this.premise = df.getOWLDataProperty(URI.create(WGJunitTests.URI_BASE + premiseURI));
            this.conclusion = df.getOWLDataProperty(URI.create(WGJunitTests.URI_BASE + conclusionURI));
            this.nonconclusion = df.getOWLDataProperty(URI.create(WGJunitTests.URI_BASE + nonconclusionURI));
        }
        public OWLIndividual getIndividual() {
            return ind;
        }
        public OWLDataProperty getConclusion() {
            return conclusion;
        }
        public OWLDataProperty getNonConclusion() {
            return nonconclusion;
        }
        public OWLDataProperty getPremise() {
            return premise;
        }
    }
    
    protected final String identifier;
    protected final Status status;
    protected final EnumSet<Semantics> semantics;
    protected final EnumSet<Semantics> notsemantics;
    protected final OWLOntology conclusionOntology;
    protected final OWLOntology premiseOntology;
    protected final OWLOntology nonConclusionOntology;
    
    protected boolean parsingError = false;
    protected final boolean isApplicable;
    
    public AbstractTest(OWLOntologyManager m, OWLOntology o, OWLIndividual i) throws OWLOntologyCreationException, URISyntaxException {
        OWLDataFactory df = m.getOWLDataFactory();
        
        Map<OWLDataPropertyExpression, Set<OWLConstant>> dps = i.getDataPropertyValues(o);
        Map<OWLObjectPropertyExpression, Set<OWLIndividual>> ops = i.getObjectPropertyValues(o);
        Map<OWLObjectPropertyExpression, Set<OWLIndividual>> nops = i.getNegativeObjectPropertyValues(o);

        identifier = retrieveIdentifier(dps, df);
        status = retrieveStatus(ops, df);
        semantics = retrieveSemantics(ops, df);
        notsemantics = retrieveNotSemantics(nops, df);
        premiseOntology = parsePremiseOntology(dps, m, df);
        conclusionOntology = parseConclusionOntology(i.getDataPropertyValues(o), m, true);
        nonConclusionOntology = parseConclusionOntology(i.getDataPropertyValues(o), m, false);
        
        isApplicable = (WGJunitTests.USE_ONLY_APPROVED_TESTS ? status == Status.APPROVED : (status != null && status != Status.REJECTED) && semantics.contains(Semantics.DL) && !notsemantics.contains(Semantics.DL));        
    }
    
    protected String retrieveIdentifier(Map<OWLDataPropertyExpression, Set<OWLConstant>> dps, OWLDataFactory df) {
        Set<OWLConstant> identifiers = dps.get(df.getOWLDataProperty(URI.create(WGJunitTests.URI_BASE + "identifier")));
        if (identifiers == null || identifiers.isEmpty()) {
            WGJunitTests.log.warning("Found a test without identifier. The test will be ignored. ");
            parsingError = true;
            return "";
        }
        if (identifiers.size() != 1) {
            String idents = "";
            for (OWLConstant c : identifiers) {
                idents += c.getLiteral();
            }
            WGJunitTests.log.warning("Found a test with more than one identifier (" + idents + "). The test will be ignored. ");
            parsingError = true;
        }
        return identifiers.iterator().next().getLiteral();
    }
    
    protected Status retrieveStatus(Map<OWLObjectPropertyExpression, Set<OWLIndividual>> ops, OWLDataFactory df) {
        if (parsingError) return null;
        Set<OWLIndividual> statuses = ops.get(df.getOWLObjectProperty(URI.create(WGJunitTests.URI_BASE + "status")));
        if (statuses == null || statuses.isEmpty()) {
            WGJunitTests.log.warning("The test " + identifier + " has no status and will be ignored!");
            parsingError = true;
            return null;
        } else if (statuses.size() > 1) {
            WGJunitTests.log.warning("The test " + identifier + " has more than one status and will be ignored. ");
            parsingError = true;
            return null;
        } else {
            OWLIndividual s = statuses.iterator().next();
            if (s.getURI().equals(URI.create(WGJunitTests.URI_BASE + "Approved"))) return Status.APPROVED;
            else if (s.getURI().equals(URI.create(WGJunitTests.URI_BASE + "Rejected"))) return Status.REJECTED;
            else if (s.getURI().equals(URI.create(WGJunitTests.URI_BASE + "Proposed"))) return Status.PROPOSED;
            else {
                WGJunitTests.log.warning("The test " + identifier + "has an invalid status of " + s.getURI().toASCIIString() + " and will be ignore. ");
                parsingError = true;
                return null;
            }
        }
    }
    
    protected EnumSet<Semantics> retrieveSemantics(Map<OWLObjectPropertyExpression, Set<OWLIndividual>> ops, OWLDataFactory df) {
        EnumSet<Semantics> semantics = EnumSet.noneOf(Semantics.class); // empty set with elements of type Semantics
        if (parsingError) return semantics;
        Set<OWLIndividual> sems = ops.get(df.getOWLObjectProperty(URI.create(WGJunitTests.URI_BASE + "semantics")));
        if (sems != null) {
            for (OWLIndividual s : sems) {
                if (s.getURI().equals(URI.create(WGJunitTests.URI_BASE + "RDF-BASED")) || s.getURI().equals(URI.create(WGJunitTests.URI_BASE + "FULL"))) semantics.add(Semantics.FULL);
                else if (s.getURI().equals(URI.create(WGJunitTests.URI_BASE + "DIRECT")) || s.getURI().equals(URI.create(WGJunitTests.URI_BASE + "DL"))) semantics.add(Semantics.DL);
                else {
                    WGJunitTests.log.warning("The test " + identifier + " has an invalid semantics: " + s.getURI().toASCIIString() + ". Semantics should be FULL or RDF_BASED or DIRECT or DL. Th test will be ignored. ");
                    parsingError = true;
                }
            }
        }
        return semantics;
    }
    
    protected EnumSet<Semantics> retrieveNotSemantics(Map<OWLObjectPropertyExpression, Set<OWLIndividual>> nops, OWLDataFactory df) {
        EnumSet<Semantics> notSemantics = EnumSet.noneOf(Semantics.class); // empty set with elements of type Semantics
        if (parsingError) return notSemantics;
        Set<OWLIndividual> nsems = nops.get(df.getOWLObjectProperty(URI.create(WGJunitTests.URI_BASE + "semantics")));
        if (nsems != null) {
            for (OWLIndividual s : nsems) {
                if (s.getURI().equals(URI.create(WGJunitTests.URI_BASE + "RDF-BASED")) || s.getURI().equals(URI.create(WGJunitTests.URI_BASE + "FULL"))) notSemantics.add(Semantics.FULL);
                else if (s.getURI().equals(URI.create(WGJunitTests.URI_BASE + "DIRECT")) || s.getURI().equals(URI.create(WGJunitTests.URI_BASE + "DL"))) notSemantics.add(Semantics.DL);
                else {
                    WGJunitTests.log.warning("The test " + identifier + " has an invalid not semantics: " + s.getURI().toASCIIString() + ". Semantics should be FULL or RDF_BASED or DIRECT or DL. Th test will be ignored. ");
                    parsingError = true;
                }
            }
        }
        return notSemantics;
    }
    
    protected OWLOntology parsePremiseOntology(Map<OWLDataPropertyExpression, Set<OWLConstant>> dps, OWLOntologyManager manager, OWLDataFactory df) {
        if (parsingError) return null;
        OWLOntology o = null;
        for (SerializationFormat f : SerializationFormat.values()) {
            Set<OWLConstant> premises = dps.get(f.getPremise());
            if (premises != null && premises.isEmpty()) {
                WGJunitTests.log.warning("Test " + identifier + " has no premise and will be ignored!");
                parsingError = true;
                return null;
            }
            if (premises != null && premises.size() != 1) {
                WGJunitTests.log.warning("Test " + identifier + " has more than one premise and will be ignored!");
                parsingError = true;
                return null;
            }
            if (premises != null) {
                StringInputSource source = new StringInputSource(premises.iterator().next().getLiteral());
                try {
                    o = manager.loadOntology(source);
                    return o;
                } catch (OWLOntologyCreationException e) {
                    // keep trying, maybe there is another loadable format
                }
            }
        }
        WGJunitTests.log.warning("Test " + identifier + " has no premise in a parsable format and will be ignored!");
        parsingError = true;
        return null; // could not load any premise
    }
    
    protected OWLOntology parseConclusionOntology(Map<OWLDataPropertyExpression, Set<OWLConstant>> dps, OWLOntologyManager manager, boolean positive) {
        if (parsingError) return null;
        OWLOntology o = null;
        for (SerializationFormat f : SerializationFormat.values()) {
            Set<OWLConstant> conclusions = dps.get(positive ? f.getConclusion() : f.getNonConclusion());
            if (conclusions != null) {
                if (conclusions.size() != 1) {
                    WGJunitTests.log.warning("Test " + identifier + " has more than one " + (positive ? "" : "non") + "conclusion and will be ignored. ");
                    parsingError = true;
                }
                StringInputSource source = new StringInputSource(conclusions.iterator().next().getLiteral());
                try {
                    o = manager.loadOntology(source);
                    return o;
                } catch (OWLOntologyCreationException e) {
                    // keep trying, maybe there is another loadable format
                }
            }
        }
        return null; // could not load any premise
    }
    
    protected abstract class RunnableHermiT extends Thread {
        boolean consistent;
        public boolean getConsistent() {
            return consistent;
        }
    }
}
