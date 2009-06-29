package org.semanticweb.HermiT.owl_wg_tests;

import java.net.URI;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringInputSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class WGTestDescriptor {
    protected static final URI TEST_CASE_URI=URI.create(WGTestRegistry.URI_BASE+"TestCase");

    protected enum Status {
        APPROVED("Approved"),REJECTED("Rejected"),PROPOSED("Proposed");
        
        public final URI uri;
        
        private Status(String uriSuffix) {
            uri=URI.create(WGTestRegistry.URI_BASE+uriSuffix);
        }
    }

    protected enum Semantics {
        DIRECT("DIRECT"),RDF_BASED("RDF-BASED");
        
        public final URI uri;

        private Semantics(String uriSuffix) {
            uri=URI.create(WGTestRegistry.URI_BASE+uriSuffix);
        }
    }

    protected enum Species {
        DL("DL"),FULL("FULL");
        
        public final URI uri;

        private Species(String uriSuffix) {
            uri=URI.create(WGTestRegistry.URI_BASE+uriSuffix);
        }
    }

    public enum TestType {
        CONSISTENCY("ConsistencyTest"),
        INCONSISTENCY("InconsistencyTest"),
        POSITIVE_ENTAILMENT("PositiveEntailmentTest"),
        NEGATIVE_ENTAILMENT("NegativeEntailmentTest"),
        PROFILE_IDENTIFICATION("ProfileIdentificationTest");
        
        public final URI uri;
        
        private TestType(String uriSuffix) {
            uri=URI.create(WGTestRegistry.URI_BASE+uriSuffix);
        }
    }

    public enum SerializationFormat {
        FUNCTIONAL("FUNCTIONAL","fsPremiseOntology","fsConclusionOntology","fsNonConclusionOntology"),
        OWLXML("OWLXML","owlXmlPremiseOntology","owlXmlConclusionOntology","owlXmlNonConclusionOntology"),
        RDFXML("RDFXML","rdfXmlPremiseOntology","rdfXmlConclusionOntology","rdfXmlNonConclusionOntology");

        public final OWLDataProperty premise;
        public final OWLDataProperty conclusion;
        public final OWLDataProperty nonconclusion;

        private SerializationFormat(String indURI,String premiseURI,String conclusionURI,String nonconclusionURI) {
            OWLDataFactory df=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            premise=df.getOWLDataProperty(URI.create(WGTestRegistry.URI_BASE+premiseURI));
            conclusion=df.getOWLDataProperty(URI.create(WGTestRegistry.URI_BASE+conclusionURI));
            nonconclusion=df.getOWLDataProperty(URI.create(WGTestRegistry.URI_BASE+nonconclusionURI));
        }
    }

    protected final OWLOntology testContainer;
    protected final OWLIndividual testIndividual;
    public final String testID;
    public final String identifier;
    public final Status status;
    public final EnumSet<TestType> testTypes;
    public final EnumSet<Species> species;
    public final EnumSet<Semantics> semantics;
    public final EnumSet<Semantics> notsemantics;

    public WGTestDescriptor(OWLOntologyManager m,OWLOntology o,OWLIndividual i) throws InvalidWGTestException {
        testContainer=o;
        testIndividual=i;
        if (i.isAnonymous()) {
            throw new InvalidWGTestException("Invalid test error: Test individuals must be named. ");
        }
        String testIndividualURI=testIndividual.asNamedIndividual().getURI().toString();
        testID=testIndividualURI.substring(WGTestRegistry.TEST_ID_PREFIX.length());
        
        OWLDataFactory df=m.getOWLDataFactory();
        Map<OWLDataPropertyExpression,Set<OWLLiteral>> dps=i.getDataPropertyValues(o);
        Map<OWLObjectPropertyExpression,Set<OWLIndividual>> ops=i.getObjectPropertyValues(o);
        Map<OWLObjectPropertyExpression,Set<OWLIndividual>> nops=i.getNegativeObjectPropertyValues(o);

        identifier=getIdentifier(dps,df);
        status=getStatus(ops,df);
        testTypes=getTestType();
        species=getSpecies(ops,df);
        semantics=getSemantics(ops,df);
        notsemantics=getNotSemantics(nops,df);
    }

    public boolean isDLTest() {
        return semantics.contains(Semantics.DIRECT) && species.contains(Species.DL);
    }
    
    protected String getIdentifier(Map<OWLDataPropertyExpression,Set<OWLLiteral>> dps,OWLDataFactory df) throws InvalidWGTestException {
        Set<OWLLiteral> identifiers=dps.get(df.getOWLDataProperty(URI.create(WGTestRegistry.URI_BASE+"identifier")));
        if (identifiers==null || identifiers.isEmpty())
            throw new InvalidWGTestException("Test does not have an identifier.");
        if (identifiers.size()!=1) {
            String idents="";
            for (OWLLiteral c : identifiers)
                idents+=c.getLiteral();
            throw new InvalidWGTestException("Test has more than one identifier.");
        }
        return identifiers.iterator().next().getLiteral();
    }

    protected Status getStatus(Map<OWLObjectPropertyExpression,Set<OWLIndividual>> ops,OWLDataFactory df) throws InvalidWGTestException {
        Set<OWLIndividual> statuses=ops.get(df.getOWLObjectProperty(URI.create(WGTestRegistry.URI_BASE+"status")));
        if (statuses==null || statuses.isEmpty())
            return null;
        else if (statuses.size()>1)
            throw new InvalidWGTestException("The test "+testID+" has more than one status.");
        else {
            OWLIndividual i = statuses.iterator().next();
            if (i.isAnonymous()) {
                throw new InvalidWGTestException("Invalid test error: Test individuals must be named. ");
            }
            URI statusURI=i.asNamedIndividual().getURI();
            for (Status status : Status.values())
                if (statusURI.equals(status.uri))
                    return status;
            throw new InvalidWGTestException("The test "+testID+"has an invalid status of "+statusURI.toString()+".");
        }
    }

    protected EnumSet<TestType> getTestType() throws InvalidWGTestException {
        EnumSet<TestType> testTypes=EnumSet.noneOf(TestType.class);
        Set<OWLClassExpression> types=testIndividual.getTypes(testContainer);
        nextItem: for (OWLClassExpression type : types) {
            if (type instanceof OWLClass) {
                URI testTypeURI=((OWLClass)type).getURI();
                for (TestType testType : TestType.values()) {
                    if (testTypeURI.equals(testType.uri)) {
                        testTypes.add(testType);
                        continue nextItem;
                    }
                }
                if (!TEST_CASE_URI.equals(testTypeURI))
                    throw new InvalidWGTestException("The test "+testID+" has an invalid test type "+testTypeURI.toString()+".");
            }
        }
        return testTypes;
    }

    protected EnumSet<Species> getSpecies(Map<OWLObjectPropertyExpression,Set<OWLIndividual>> ops,OWLDataFactory df) throws InvalidWGTestException {
        EnumSet<Species> species=EnumSet.noneOf(Species.class);
        Set<OWLIndividual> specs=ops.get(df.getOWLObjectProperty(URI.create(WGTestRegistry.URI_BASE+"species")));
        if (specs!=null) {
            nextItem: for (OWLIndividual s : specs) {
                if (s.isAnonymous()) {
                    throw new InvalidWGTestException("Invalid test error: Test individuals must be named. ");
                }
                URI speciesURI=s.asNamedIndividual().getURI();
                for (Species spc : Species.values()) {
                    if (speciesURI.equals(spc.uri)) {
                        species.add(spc);
                        continue nextItem;
                    }
                }
                throw new InvalidWGTestException("The test "+testID+" has an invalid species "+speciesURI.toString()+".");
            }
        }
        return species;
    }

    protected EnumSet<Semantics> getSemantics(Map<OWLObjectPropertyExpression,Set<OWLIndividual>> ops,OWLDataFactory df) throws InvalidWGTestException {
        EnumSet<Semantics> semantics=EnumSet.noneOf(Semantics.class);
        Set<OWLIndividual> sems=ops.get(df.getOWLObjectProperty(URI.create(WGTestRegistry.URI_BASE+"semantics")));
        if (sems!=null) {
            nextItem: for (OWLIndividual s : sems) {
                if (s.isAnonymous()) {
                    throw new InvalidWGTestException("Invalid test error: Test individuals must be named. ");
                }
                URI semanticsURI=s.asNamedIndividual().getURI();
                for (Semantics sem : Semantics.values()) {
                    if (semanticsURI.equals(sem.uri)) {
                        semantics.add(sem);
                        continue nextItem;
                    }
                }
                throw new InvalidWGTestException("The test "+testID+" has an invalid semantics "+semanticsURI.toString()+".");
            }
        }
        return semantics;
    }

    protected EnumSet<Semantics> getNotSemantics(Map<OWLObjectPropertyExpression,Set<OWLIndividual>> nops,OWLDataFactory df) throws InvalidWGTestException {
        EnumSet<Semantics> notSemantics=EnumSet.noneOf(Semantics.class);
        Set<OWLIndividual> nsems=nops.get(df.getOWLObjectProperty(URI.create(WGTestRegistry.URI_BASE+"semantics")));
        if (nsems!=null) {
            nextItem: for (OWLIndividual s : nsems) {
                if (s.isAnonymous()) {
                    throw new InvalidWGTestException("Invalid test error: Test individuals must be named. ");
                }
                URI semanticsURI=s.asNamedIndividual().getURI();
                for (Semantics sem : Semantics.values()) {
                    if (semanticsURI.equals(sem.uri)) {
                        semantics.add(sem);
                        continue nextItem;
                    }
                }
                throw new InvalidWGTestException("The test "+testID+" has an invalid not semantics "+semanticsURI.toString()+".");
            }
        }
        return notSemantics;
    }

    public OWLOntology getPremiseOntology(OWLOntologyManager manager) throws InvalidWGTestException {
        Map<OWLDataPropertyExpression,Set<OWLLiteral>> dps=testIndividual.getDataPropertyValues(testContainer);
        for (SerializationFormat format : SerializationFormat.values()) {
            Set<OWLLiteral> premises=dps.get(format.premise);
            if (premises!=null) {
                if (premises.size()!=1)
                    throw new InvalidWGTestException("Test "+testID+" has an incorrect number of premises.");
                StringInputSource source=new StringInputSource(premises.iterator().next().getLiteral());
                try {
                    return manager.loadOntology(source);
                }
                catch (OWLOntologyCreationException e) {
                    throw new InvalidWGTestException("Invalid premise ontology.",e);
                }
            }
        }
        // No premise property means that the premise is the empty ontology.
        try {
            return manager.createOntology(IRI.create("uri:urn:opaque"));
        }
        catch (OWLOntologyCreationException e) {
            throw new InvalidWGTestException("Cannot create empty ontology");
        }
    }

    public OWLOntology getConclusionOntology(OWLOntologyManager manager,boolean positive) throws InvalidWGTestException {
        Map<OWLDataPropertyExpression,Set<OWLLiteral>> dps=testIndividual.getDataPropertyValues(testContainer);
        for (SerializationFormat format : SerializationFormat.values()) {
            Set<OWLLiteral> conclusions=dps.get(positive ? format.conclusion : format.nonconclusion);
            if (conclusions!=null) {
                if (conclusions.size()!=1)
                    throw new InvalidWGTestException("Test "+testID+" has an incorrect number of "+(positive ? "" : "non")+"conclusions.");
                StringInputSource source=new StringInputSource(conclusions.iterator().next().getLiteral());
                try {
                    return manager.loadOntology(source);
                }
                catch (OWLOntologyCreationException e) {
                    throw new InvalidWGTestException("Invalid conclusion ontology.",e);
                }
            }
        }
        throw new InvalidWGTestException("Test "+testID+" has no conclusion ontology in a parsable format.");
    }
    
    public void addTestsToSuite(TestSuite suite) {
        for (TestType testType : TestType.values()) {
            Test test=getTest(testType);
            if (test!=null)
                suite.addTest(test);
        }
    }
    
    public Test getTest(TestType testType) {
        if (testTypes.contains(testType) && isDLTest()) {
            switch (testType) {
            case CONSISTENCY:
                return new ConsistencyTest(this,true);
            case INCONSISTENCY:
                return new ConsistencyTest(this,false);
            case POSITIVE_ENTAILMENT:
                return new EntailmentTest(this,true);
            case NEGATIVE_ENTAILMENT:
                return new EntailmentTest(this,false);
            }
        }
        return null;
    }
}
