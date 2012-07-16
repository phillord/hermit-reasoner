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
// An update for the tests (all.rdf) should regularly be downloaded to the ontologies folder from http://wiki.webont.org/exports/
package org.semanticweb.HermiT.owl_wg_tests;

import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
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
    protected static final IRI TEST_CASE_IRI=IRI.create(WGTestRegistry.URI_BASE+"TestCase");

    protected enum Status {
        APPROVED("Approved"),REJECTED("Rejected"),PROPOSED("Proposed"),EXTRACREDIT("Extracredit");

        public final IRI uri;

        private Status(String uriSuffix) {
            uri=IRI.create(WGTestRegistry.URI_BASE+uriSuffix);
        }
    }

    protected enum Semantics {
        DIRECT("DIRECT"),RDF_BASED("RDF-BASED");

        public final IRI uri;

        private Semantics(String uriSuffix) {
            uri=IRI.create(WGTestRegistry.URI_BASE+uriSuffix);
        }
    }

    protected enum Species {
        DL("DL"),FULL("FULL");

        public final IRI uri;

        private Species(String uriSuffix) {
            uri=IRI.create(WGTestRegistry.URI_BASE+uriSuffix);
        }
    }

    public enum TestType {
        CONSISTENCY("ConsistencyTest"),INCONSISTENCY("InconsistencyTest"),POSITIVE_ENTAILMENT("PositiveEntailmentTest"),NEGATIVE_ENTAILMENT("NegativeEntailmentTest"),PROFILE_IDENTIFICATION("ProfileIdentificationTest");

        public final IRI uri;

        private TestType(String uriSuffix) {
            uri=IRI.create(WGTestRegistry.URI_BASE+uriSuffix);
        }
    }

    public enum SerializationFormat {
        FUNCTIONAL("FUNCTIONAL","fsPremiseOntology","fsConclusionOntology","fsNonConclusionOntology"),OWLXML("OWLXML","owlXmlPremiseOntology","owlXmlConclusionOntology","owlXmlNonConclusionOntology"),RDFXML("RDFXML","rdfXmlPremiseOntology","rdfXmlConclusionOntology","rdfXmlNonConclusionOntology");

        public final OWLDataProperty premise;
        public final OWLDataProperty conclusion;
        public final OWLDataProperty nonconclusion;

        private SerializationFormat(String indIRI,String premiseIRI,String conclusionIRI,String nonconclusionIRI) {
            OWLDataFactory df=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            premise=df.getOWLDataProperty(IRI.create(WGTestRegistry.URI_BASE+premiseIRI));
            conclusion=df.getOWLDataProperty(IRI.create(WGTestRegistry.URI_BASE+conclusionIRI));
            nonconclusion=df.getOWLDataProperty(IRI.create(WGTestRegistry.URI_BASE+nonconclusionIRI));
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
        if (i.isAnonymous())
            throw new InvalidWGTestException("Invalid test error: Test individuals must be named. ");
        String testIndividualIRI=testIndividual.asOWLNamedIndividual().getIRI().toString();
        testID=testIndividualIRI.substring(WGTestRegistry.TEST_ID_PREFIX.length());

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
        Set<OWLLiteral> identifiers=dps.get(df.getOWLDataProperty(IRI.create(WGTestRegistry.URI_BASE+"identifier")));
        if (identifiers==null || identifiers.isEmpty())
            throw new InvalidWGTestException("Test does not have an identifier.");
        if (identifiers.size()!=1)
            throw new InvalidWGTestException("Test has more than one identifier.");
        return identifiers.iterator().next().getLiteral();
    }

    protected Status getStatus(Map<OWLObjectPropertyExpression,Set<OWLIndividual>> ops,OWLDataFactory df) throws InvalidWGTestException {
        Set<OWLIndividual> statuses=ops.get(df.getOWLObjectProperty(IRI.create(WGTestRegistry.URI_BASE+"status")));
        if (statuses==null || statuses.isEmpty())
            return null;
        else if (statuses.size()>1)
            throw new InvalidWGTestException("The test "+testID+" has more than one status.");
        else {
            OWLIndividual i=statuses.iterator().next();
            if (i.isAnonymous())
                throw new InvalidWGTestException("Invalid test error: Test individuals must be named. ");
            IRI statusIRI=i.asOWLNamedIndividual().getIRI();
            for (Status status : Status.values())
                if (statusIRI.equals(status.uri))
                    return status;
            throw new InvalidWGTestException("The test "+testID+"has an invalid status of "+statusIRI.toString()+".");
        }
    }

    protected EnumSet<TestType> getTestType() throws InvalidWGTestException {
        EnumSet<TestType> testTypes=EnumSet.noneOf(TestType.class);
        Set<OWLClassExpression> types=testIndividual.getTypes(testContainer);
        nextItem: for (OWLClassExpression type : types) {
            if (type instanceof OWLClass) {
                IRI testTypeIRI=((OWLClass)type).getIRI();
                for (TestType testType : TestType.values()) {
                    if (testTypeIRI.equals(testType.uri)) {
                        testTypes.add(testType);
                        continue nextItem;
                    }
                }
                if (!TEST_CASE_IRI.equals(testTypeIRI))
                    throw new InvalidWGTestException("The test "+testID+" has an invalid test type "+testTypeIRI.toString()+".");
            }
        }
        return testTypes;
    }

    protected EnumSet<Species> getSpecies(Map<OWLObjectPropertyExpression,Set<OWLIndividual>> ops,OWLDataFactory df) throws InvalidWGTestException {
        EnumSet<Species> species=EnumSet.noneOf(Species.class);
        Set<OWLIndividual> specs=ops.get(df.getOWLObjectProperty(IRI.create(WGTestRegistry.URI_BASE+"species")));
        if (specs!=null) {
            nextItem: for (OWLIndividual s : specs) {
                if (s.isAnonymous()) {
                    throw new InvalidWGTestException("Invalid test error: Test individuals must be named. ");
                }
                IRI speciesIRI=s.asOWLNamedIndividual().getIRI();
                for (Species spc : Species.values()) {
                    if (speciesIRI.equals(spc.uri)) {
                        species.add(spc);
                        continue nextItem;
                    }
                }
                throw new InvalidWGTestException("The test "+testID+" has an invalid species "+speciesIRI.toString()+".");
            }
        }
        return species;
    }

    protected EnumSet<Semantics> getSemantics(Map<OWLObjectPropertyExpression,Set<OWLIndividual>> ops,OWLDataFactory df) throws InvalidWGTestException {
        EnumSet<Semantics> semantics=EnumSet.noneOf(Semantics.class);
        Set<OWLIndividual> sems=ops.get(df.getOWLObjectProperty(IRI.create(WGTestRegistry.URI_BASE+"semantics")));
        if (sems!=null) {
            nextItem: for (OWLIndividual s : sems) {
                if (s.isAnonymous())
                    throw new InvalidWGTestException("Invalid test error: Test individuals must be named. ");
                IRI semanticsIRI=s.asOWLNamedIndividual().getIRI();
                for (Semantics sem : Semantics.values()) {
                    if (semanticsIRI.equals(sem.uri)) {
                        semantics.add(sem);
                        continue nextItem;
                    }
                }
                throw new InvalidWGTestException("The test "+testID+" has an invalid semantics "+semanticsIRI.toString()+".");
            }
        }
        return semantics;
    }

    protected EnumSet<Semantics> getNotSemantics(Map<OWLObjectPropertyExpression,Set<OWLIndividual>> nops,OWLDataFactory df) throws InvalidWGTestException {
        EnumSet<Semantics> notSemantics=EnumSet.noneOf(Semantics.class);
        Set<OWLIndividual> nsems=nops.get(df.getOWLObjectProperty(IRI.create(WGTestRegistry.URI_BASE+"semantics")));
        if (nsems!=null) {
            nextItem: for (OWLIndividual s : nsems) {
                if (s.isAnonymous())
                    throw new InvalidWGTestException("Invalid test error: Test individuals must be named. ");
                IRI semanticsIRI=s.asOWLNamedIndividual().getIRI();
                for (Semantics sem : Semantics.values()) {
                    if (semanticsIRI.equals(sem.uri)) {
                        notSemantics.add(sem);
                        continue nextItem;
                    }
                }
                throw new InvalidWGTestException("The test "+testID+" has an invalid not semantics "+semanticsIRI.toString()+".");
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
                StringDocumentSource source=new StringDocumentSource(premises.iterator().next().getLiteral());
                try {
                    return manager.loadOntologyFromOntologyDocument(source);
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
                StringDocumentSource source=new StringDocumentSource(conclusions.iterator().next().getLiteral());
                try {
                    OWLOntology concl=manager.loadOntologyFromOntologyDocument(source);
                    return concl;
                }
                catch (OWLOntologyCreationException e) {
                    throw new InvalidWGTestException("Invalid conclusion ontology.",e);
                }
            }
        }
        throw new InvalidWGTestException("Test "+testID+" has no conclusion ontology in a parsable format.");
    }

    public void addTestsToSuite(TestSuite suite) {
        addTestsToSuite(suite,true);
    }
    public void addTestsToSuite(TestSuite suite,boolean useDisjunctionLearning) {
        for (TestType testType : TestType.values()) {
            Test test=getTest(testType,null,useDisjunctionLearning);
            if (test!=null)
                suite.addTest(test);
        }
    }

    public Test getTest(TestType testType,File dumpTestDataDirectory,boolean useDisjunctionLearning) {
        if (testTypes.contains(testType) && isDLTest()) {
            switch (testType) {
            case CONSISTENCY:
                return new ConsistencyTest(this,true,dumpTestDataDirectory,useDisjunctionLearning);
            case INCONSISTENCY:
                return new ConsistencyTest(this,false,dumpTestDataDirectory,useDisjunctionLearning);
            case POSITIVE_ENTAILMENT:
                return new EntailmentTest(this,true,dumpTestDataDirectory,useDisjunctionLearning);
            case NEGATIVE_ENTAILMENT:
                return new EntailmentTest(this,false,dumpTestDataDirectory,useDisjunctionLearning);
            case PROFILE_IDENTIFICATION:
                break;
            default:
                break;
            }
        }
        return null;
    }

    public String toString() {
        return "Test: "+identifier+", status "+status+", test types: "+testTypes+", species: "+species+", semantics: "+semantics+", not semantics: "+notsemantics;
    }
}
