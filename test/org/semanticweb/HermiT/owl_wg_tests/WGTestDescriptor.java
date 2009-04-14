package org.semanticweb.HermiT.owl_wg_tests;

import java.net.URI;
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

public class WGTestDescriptor {

    protected enum Status {
        APPROVED,REJECTED,PROPOSED
    }

    protected enum Semantics {
        DL,FULL
    }

    public enum TestType {
        CONSISTENCY,INCONSISTENCY,POSITIVE_ENTAILMENT,NEGATIVE_ENTAILMENT
    }

    public enum SerializationFormat {
        FUNCTIONAL("FUNCTIONAL","fsPremiseOntology","fsConclusionOntology","fsNonConclusionOntology"),
        OWLXML("OWLXML","owlXmlPremiseOntology","owlXmlConclusionOntology","owlXmlNonConclusionOntology"),
        RDFXML("RDFXML","rdfXmlPremiseOntology","rdfXmlConclusionOntology","rdfXmlNonConclusionOntology");

        protected final OWLDataProperty premise;
        protected final OWLDataProperty conclusion;
        protected final OWLDataProperty nonconclusion;

        private SerializationFormat(String indURI,String premiseURI,String conclusionURI,String nonconclusionURI) {
            OWLDataFactory df=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            premise=df.getOWLDataProperty(URI.create(WGTestRegistry.URI_BASE+premiseURI));
            conclusion=df.getOWLDataProperty(URI.create(WGTestRegistry.URI_BASE+conclusionURI));
            nonconclusion=df.getOWLDataProperty(URI.create(WGTestRegistry.URI_BASE+nonconclusionURI));
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

    protected final OWLOntology testContainer;
    protected final OWLIndividual testIndividual;
    public final String identifier;
    public final Status status;
    public final EnumSet<Semantics> semantics;
    public final EnumSet<Semantics> notsemantics;

    public WGTestDescriptor(OWLOntologyManager m,OWLOntology o,OWLIndividual i) throws InvalidWGTestException {
        testContainer=o;
        testIndividual=i;
        
        OWLDataFactory df=m.getOWLDataFactory();
        Map<OWLDataPropertyExpression,Set<OWLConstant>> dps=i.getDataPropertyValues(o);
        Map<OWLObjectPropertyExpression,Set<OWLIndividual>> ops=i.getObjectPropertyValues(o);
        Map<OWLObjectPropertyExpression,Set<OWLIndividual>> nops=i.getNegativeObjectPropertyValues(o);

        identifier=getIdentifier(dps,df);
        status=getStatus(ops,df);
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
            throw new InvalidWGTestException("The test "+identifier+" has more than one status.");
        else {
            OWLIndividual s=statuses.iterator().next();
            if (s.getURI().equals(URI.create(WGTestRegistry.URI_BASE+"Approved")))
                return Status.APPROVED;
            else if (s.getURI().equals(URI.create(WGTestRegistry.URI_BASE+"Rejected")))
                return Status.REJECTED;
            else if (s.getURI().equals(URI.create(WGTestRegistry.URI_BASE+"Proposed")))
                return Status.PROPOSED;
            else
                throw new InvalidWGTestException("The test "+identifier+"has an invalid status of "+s.getURI().toASCIIString()+".");
        }
    }

    protected EnumSet<Semantics> getSemantics(Map<OWLObjectPropertyExpression,Set<OWLIndividual>> ops,OWLDataFactory df) throws InvalidWGTestException {
        EnumSet<Semantics> semantics=EnumSet.noneOf(Semantics.class); // empty set with elements of type Semantics
        Set<OWLIndividual> sems=ops.get(df.getOWLObjectProperty(URI.create(WGTestRegistry.URI_BASE+"semantics")));
        if (sems!=null) {
            for (OWLIndividual s : sems) {
                if (s.getURI().equals(URI.create(WGTestRegistry.URI_BASE+"RDF-BASED")) || s.getURI().equals(URI.create(WGTestRegistry.URI_BASE+"FULL")))
                    semantics.add(Semantics.FULL);
                else if (s.getURI().equals(URI.create(WGTestRegistry.URI_BASE+"DIRECT")) || s.getURI().equals(URI.create(WGTestRegistry.URI_BASE+"DL")))
                    semantics.add(Semantics.DL);
                else
                    throw new InvalidWGTestException("The test "+identifier+" has an invalid semantics: "+s.getURI().toASCIIString()+". Semantics should be FULL or RDF_BASED or DIRECT or DL.");
            }
        }
        return semantics;
    }

    protected EnumSet<Semantics> getNotSemantics(Map<OWLObjectPropertyExpression,Set<OWLIndividual>> nops,OWLDataFactory df) throws InvalidWGTestException {
        EnumSet<Semantics> notSemantics=EnumSet.noneOf(Semantics.class); // empty set with elements of type Semantics
        Set<OWLIndividual> nsems=nops.get(df.getOWLObjectProperty(URI.create(WGTestRegistry.URI_BASE+"semantics")));
        if (nsems!=null) {
            for (OWLIndividual s : nsems) {
                if (s.getURI().equals(URI.create(WGTestRegistry.URI_BASE+"RDF-BASED")) || s.getURI().equals(URI.create(WGTestRegistry.URI_BASE+"FULL")))
                    notSemantics.add(Semantics.FULL);
                else if (s.getURI().equals(URI.create(WGTestRegistry.URI_BASE+"DIRECT")) || s.getURI().equals(URI.create(WGTestRegistry.URI_BASE+"DL")))
                    notSemantics.add(Semantics.DL);
                else
                    throw new InvalidWGTestException("The test "+identifier+" has an invalid not semantics: "+s.getURI().toASCIIString()+". Semantics should be FULL or RDF_BASED or DIRECT or DL.");
            }
        }
        return notSemantics;
    }

    public OWLOntology getPremiseOntology(OWLOntologyManager manager) throws InvalidWGTestException {
        Map<OWLDataPropertyExpression,Set<OWLConstant>> dps=testIndividual.getDataPropertyValues(testContainer);
        for (SerializationFormat f : SerializationFormat.values()) {
            Set<OWLConstant> premises=dps.get(f.getPremise());
            if (premises!=null) {
                if (premises.size()!=1)
                    throw new InvalidWGTestException("Test "+identifier+" has an incorrect number of premises.");
                StringInputSource source=new StringInputSource(premises.iterator().next().getLiteral());
                try {
                    return manager.loadOntology(source);
                }
                catch (OWLOntologyCreationException e) {
                    // keep trying, maybe there is another loadable format
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

    public OWLOntology getConclusionOntology(OWLOntologyManager manager) throws InvalidWGTestException {
        return getConclusionOntology(manager,true);
    }
    
    public OWLOntology getNonConclusionOntology(OWLOntologyManager manager) throws InvalidWGTestException {
        return getConclusionOntology(manager,false);
    }
    
    protected OWLOntology getConclusionOntology(OWLOntologyManager manager,boolean positive) throws InvalidWGTestException {
        Map<OWLDataPropertyExpression,Set<OWLConstant>> dps=testIndividual.getDataPropertyValues(testContainer);
        for (SerializationFormat f : SerializationFormat.values()) {
            Set<OWLConstant> conclusions=dps.get(positive ? f.getConclusion() : f.getNonConclusion());
            if (conclusions!=null) {
                if (conclusions.size()!=1)
                    throw new InvalidWGTestException("Test "+identifier+" has an incorrect number of "+(positive ? "" : "non")+"conclusions.");
                StringInputSource source=new StringInputSource(conclusions.iterator().next().getLiteral());
                try {
                    return manager.loadOntology(source);
                }
                catch (OWLOntologyCreationException e) {
                    // keep trying, maybe there is another loadable format
                }
            }
        }
        throw new InvalidWGTestException("Test "+identifier+" has no conclusion ontology in a parsable format.");
    }
}
