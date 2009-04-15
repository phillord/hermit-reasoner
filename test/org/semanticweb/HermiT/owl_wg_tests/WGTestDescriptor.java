package org.semanticweb.HermiT.owl_wg_tests;

import java.net.URI;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

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
        DL("DIRECT","DL"),FULL("RDF-BASED","FULL");
        
        public final URI[] uris;

        private Semantics(String... uriSuffixes) {
            uris=new URI[uriSuffixes.length];
            for (int index=0;index<uriSuffixes.length;index++)
                uris[index]=URI.create(WGTestRegistry.URI_BASE+uriSuffixes[index]);
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
    public final EnumSet<Semantics> semantics;
    public final EnumSet<Semantics> notsemantics;

    public WGTestDescriptor(OWLOntologyManager m,OWLOntology o,OWLIndividual i) throws InvalidWGTestException {
        testContainer=o;
        testIndividual=i;
        String testIndividualURI=testIndividual.getURI().toString();
        testID=testIndividualURI.substring(WGTestRegistry.TEST_ID_PREFIX.length());
        
        OWLDataFactory df=m.getOWLDataFactory();
        Map<OWLDataPropertyExpression,Set<OWLConstant>> dps=i.getDataPropertyValues(o);
        Map<OWLObjectPropertyExpression,Set<OWLIndividual>> ops=i.getObjectPropertyValues(o);
        Map<OWLObjectPropertyExpression,Set<OWLIndividual>> nops=i.getNegativeObjectPropertyValues(o);

        identifier=getIdentifier(dps,df);
        status=getStatus(ops,df);
        testTypes=getTestType();
        semantics=getSemantics(ops,df);
        notsemantics=getNotSemantics(nops,df);
    }

    public boolean isDLTest() {
        return semantics.contains(Semantics.DL);
    }
    
    protected String getIdentifier(Map<OWLDataPropertyExpression,Set<OWLConstant>> dps,OWLDataFactory df) throws InvalidWGTestException {
        Set<OWLConstant> identifiers=dps.get(df.getOWLDataProperty(URI.create(WGTestRegistry.URI_BASE+"identifier")));
        if (identifiers==null || identifiers.isEmpty())
            throw new InvalidWGTestException("Test does not have an identifier.");
        if (identifiers.size()!=1) {
            String idents="";
            for (OWLConstant c : identifiers)
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
            URI statusURI=statuses.iterator().next().getURI();
            for (Status status : Status.values())
                if (statusURI.equals(status.uri))
                    return status;
            throw new InvalidWGTestException("The test "+testID+"has an invalid status of "+statusURI.toString()+".");
        }
    }

    protected EnumSet<TestType> getTestType() throws InvalidWGTestException {
        EnumSet<TestType> testTypes=EnumSet.noneOf(TestType.class);
        Set<OWLDescription> types=testIndividual.getTypes(testContainer);
        nextItem: for (OWLDescription type : types) {
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

    protected EnumSet<Semantics> getSemantics(Map<OWLObjectPropertyExpression,Set<OWLIndividual>> ops,OWLDataFactory df) throws InvalidWGTestException {
        EnumSet<Semantics> semantics=EnumSet.noneOf(Semantics.class);
        Set<OWLIndividual> sems=ops.get(df.getOWLObjectProperty(URI.create(WGTestRegistry.URI_BASE+"semantics")));
        if (sems!=null) {
            nextItem: for (OWLIndividual s : sems) {
                URI semanticsURI=s.getURI();
                for (Semantics sem : Semantics.values()) {
                    for (URI uri : sem.uris)
                        if (semanticsURI.equals(uri)) {
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
                URI semanticsURI=s.getURI();
                for (Semantics sem : Semantics.values()) {
                    for (URI uri : sem.uris)
                        if (semanticsURI.equals(uri)) {
                            notSemantics.add(sem);
                            continue nextItem;
                        }
                }
                throw new InvalidWGTestException("The test "+testID+" has an invalid not semantics "+semanticsURI.toString()+".");
            }
        }
        return notSemantics;
    }

    public OWLOntology getPremiseOntology(OWLOntologyManager manager) throws InvalidWGTestException {
        Map<OWLDataPropertyExpression,Set<OWLConstant>> dps=testIndividual.getDataPropertyValues(testContainer);
        for (SerializationFormat format : SerializationFormat.values()) {
            Set<OWLConstant> premises=dps.get(format.premise);
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
            return manager.createOntology(URI.create("uri:urn:opaque"));
        }
        catch (OWLOntologyCreationException e) {
            throw new InvalidWGTestException("Cannot create empty ontology");
        }
    }

    public OWLOntology getConclusionOntology(OWLOntologyManager manager,boolean positive) throws InvalidWGTestException {
        Map<OWLDataPropertyExpression,Set<OWLConstant>> dps=testIndividual.getDataPropertyValues(testContainer);
        for (SerializationFormat format : SerializationFormat.values()) {
            Set<OWLConstant> conclusions=dps.get(positive ? format.conclusion : format.nonconclusion);
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
        if (testTypes.contains(testType)) {
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
