// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.net.URI;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.HermiT.hierarchy.SubsumptionHierarchy;
import org.semanticweb.HermiT.hierarchy.SubsumptionHierarchyNode;
import org.semanticweb.HermiT.hierarchy.HierarchyPosition;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologySetProvider;

public class HermitReasoner implements OWLReasoner {
    static final String mUriBase = "urn:hermit:kb";

    Reasoner hermit;
    java.util.Set<OWLOntology> ontologies;
    OWLOntology ontology;
    OWLOntologyManager manager;
    OWLDataFactory factory;
    int nextKbId;
    
    HermitReasoner(OWLOntologyManager inManager) {
        manager = inManager;
        factory = manager.getOWLDataFactory();
        nextKbId = 1;
        clearOntologies();
    }
    
    // ReasonerBase implementation:
    public void classify() {
            System.out.println("Seeding subsumption cache...");
        hermit.seedSubsumptionCache();
            System.out.println("...done");
    }

    public void clearOntologies() {
        hermit = null;
        ontologies = new java.util.HashSet<OWLOntology>();
        ontology = null;
    }

    public void dispose() {
        clearOntologies();
    }

    public java.util.Set<OWLOntology> getLoadedOntologies() {
        return ontologies;
    }

    public boolean isClassified() {
		return hermit != null && hermit.isSubsumptionCacheSeeded();
	}

    public boolean isDefined(OWLClass c) {
		return ontology.containsClassReference(c.getURI());
	}
    
	public boolean isDefined(OWLIndividual i) {
		return ontology.containsIndividualReference(i.getURI());
	}

    public boolean isDefined(OWLObjectProperty p) {
        return ontology.containsObjectPropertyReference(p.getURI());
    }

    public boolean isDefined(OWLDataProperty p) {
        return ontology.containsDataPropertyReference(p.getURI());
    }
    
    public boolean isRealised() {
        return hermit != null && hermit.isRealizationCached();
    }
    
    public void loadOntologies(java.util.Set<OWLOntology> inOntologies) {
        try {
            ontologies = inOntologies;
            URI theUri = null;
            for (OWLOntology i : ontologies) {
                theUri = i.getURI();
                break;
            }
            ontology =
                new org.semanticweb.owl.util.OWLOntologyMerger(
                    new SetProviderFromSet(ontologies)
                ).createMergedOntology(manager,theUri);// URI.create(mUriBase + String.valueOf(mNextKbId++)));
            Reasoner.Configuration config = new Reasoner.Configuration();
            config.subsumptionCacheStrategyType = Reasoner.SubsumptionCacheStrategyType.JUST_IN_TIME;
            System.out.println("Loading ontology into HermiT...");
            hermit = new Reasoner(ontology, config);
            System.out.println("...done");
        } catch (OWLException e) {
            throw new RuntimeException("Failed to merge ontologies.", e);
        }
    }
    
    public void realise() {
        hermit.cacheRealization();
    }
    public void unloadOntologies(java.util.Set<OWLOntology> inOntologies) {
        ontologies.removeAll(ontologies);
        loadOntologies(inOntologies);
    }

    class SetProviderFromSet implements OWLOntologySetProvider {
        // Because the OWL API can be a little retarded at times.
        java.util.Set<OWLOntology> mS;
        public SetProviderFromSet(java.util.Set<OWLOntology> s) { mS = s; }
        public java.util.Set<OWLOntology> getOntologies() { return mS; }
    }
    
    // SatisfiabilityChecker implementation:
    public boolean isSatisfiable(OWLDescription d) {
        return hermit.isClassSatisfiable(d);
    }
    
    protected Set<Set<OWLClass>>
        classSets(Set<HierarchyPosition<OWLClass>> positions) {
        java.util.Set<java.util.Set<OWLClass>> r
            = new java.util.HashSet<java.util.Set<OWLClass>>();
        for (HierarchyPosition<OWLClass> pos : positions) {
            r.add(pos.getEquivalents());
        }
        return r;        
    }

    // ClassReasoner implementation:
    public java.util.Set<java.util.Set<OWLClass>>
        getDescendantClasses(OWLDescription d) {
        return classSets(hermit.getPosition(d).getDescendantPositions());
    }

    public java.util.Set<java.util.Set<OWLClass>>
        getAncestorClasses(OWLDescription d) {
        return classSets(hermit.getPosition(d).getAncestorPositions());
    }

    public java.util.Set<OWLClass> getEquivalentClasses(OWLDescription d) {
        return hermit.getPosition(d).getEquivalents();
    }
    
    public java.util.Set<OWLClass> getInconsistentClasses() {
        return getEquivalentClasses(
            factory.getOWLClass(
                URI.create("http://www.w3.org/2002/07/owl#Nothing")));
    }
    
    public java.util.Set<java.util.Set<OWLClass>> getSubClasses(OWLDescription d) {
        return classSets(hermit.getPosition(d).getChildPositions());
    }
    
    public java.util.Set<java.util.Set<OWLClass>> getSuperClasses(OWLDescription d) {
        return classSets(hermit.getPosition(d).getParentPositions());
    }
    

    public boolean isEquivalentClass(OWLDescription c, OWLDescription d) {
        return isSubClassOf(c, d) && isSubClassOf(d, c);
    }
    public boolean isSubClassOf(OWLDescription subclass, OWLDescription superclass) {
        return hermit.isSubsumedBy(subclass, superclass);
    }
    
    // ConsistencyChecker implementation:
    public boolean isConsistent(OWLOntology ignored) {
        return hermit.isConsistent();
    }
    
    // IndividualReasoner stubs:
    public java.util.Map<OWLDataProperty,java.util.Set<OWLConstant>> getDataPropertyRelationships(OWLIndividual individual) {
        // TODO: implement (somehow)
        return new java.util.HashMap<OWLDataProperty,java.util.Set<OWLConstant>>();
    }
    
    public java.util.Set<OWLIndividual> getIndividuals(OWLDescription d, boolean direct) {
        if (direct) {
            return hermit.getDirectMembers(d);
        } else {
            return hermit.getMembers(d);
        }
    }

    public java.util.Map<OWLObjectProperty,java.util.Set<OWLIndividual>> getObjectPropertyRelationships(OWLIndividual individual) {
        return new java.util.HashMap<OWLObjectProperty,java.util.Set<OWLIndividual>>();
    }
    
    public java.util.Set<OWLIndividual> getRelatedIndividuals(OWLIndividual subject, OWLObjectPropertyExpression property) {
        return getIndividuals(
            factory.getOWLObjectSomeRestriction(property.getInverseProperty(),
                                        factory.getOWLObjectOneOf(subject)),
            false);
    }
    
    public java.util.Set<OWLConstant> getRelatedValues(OWLIndividual subject, OWLDataPropertyExpression property) {
        // TODO: implement (somehow)
        return new java.util.HashSet<OWLConstant>();
    }

    public java.util.Set<java.util.Set<OWLClass>> getTypes(OWLIndividual individual, boolean direct) {
        if (direct) {
            return classSets(hermit.getMemberships(individual).getParentPositions());
        } else {
            return classSets(hermit.getMemberships(individual).getAncestorPositions());
        }
    }

    public boolean hasDataPropertyRelationship(OWLIndividual subject, OWLDataPropertyExpression property, OWLConstant object) {
        return false;
    }
    public boolean hasObjectPropertyRelationship(OWLIndividual subject, OWLObjectPropertyExpression property, OWLIndividual object) {
        return false;
    }
    public boolean hasType(OWLIndividual individual, OWLDescription type, boolean direct) {
        return false;
    }
    
    // PropertyReasoner stubs: (not yet implemented)
    public java.util.Set<java.util.Set<OWLDataProperty>>	getAncestorProperties(OWLDataProperty property) {
        return new java.util.HashSet<java.util.Set<OWLDataProperty>>();
    }
    public java.util.Set<java.util.Set<OWLObjectProperty>>	getAncestorProperties(OWLObjectProperty property) {
        return new java.util.HashSet<java.util.Set<OWLObjectProperty>>();
    }
    public java.util.Set<java.util.Set<OWLDataProperty>>	getDescendantProperties(OWLDataProperty property) {
        return new java.util.HashSet<java.util.Set<OWLDataProperty>>();
    }
    public java.util.Set<java.util.Set<OWLObjectProperty>>	getDescendantProperties(OWLObjectProperty property) {
        return new java.util.HashSet<java.util.Set<OWLObjectProperty>>();
    }
    public java.util.Set<java.util.Set<OWLDescription>>	getDomains(OWLDataProperty property) {
        return new java.util.HashSet<java.util.Set<OWLDescription>>();
    }
    public java.util.Set<java.util.Set<OWLDescription>>	getDomains(OWLObjectProperty property) {
        return new java.util.HashSet<java.util.Set<OWLDescription>>();
    }
    public java.util.Set<OWLDataProperty>	getEquivalentProperties(OWLDataProperty property) {
        return new java.util.HashSet<OWLDataProperty>();
    }
    public java.util.Set<OWLObjectProperty>	getEquivalentProperties(OWLObjectProperty property) {
        return new java.util.HashSet<OWLObjectProperty>();
    }
    public java.util.Set<java.util.Set<OWLObjectProperty>>	getInverseProperties(OWLObjectProperty property) {
        return new java.util.HashSet<java.util.Set<OWLObjectProperty>>();
    }
    public java.util.Set<OWLDataRange>	getRanges(OWLDataProperty property) {
        return new java.util.HashSet<OWLDataRange>();
    }
    public java.util.Set<OWLDescription>	getRanges(OWLObjectProperty property) {
        return new java.util.HashSet<OWLDescription>();
    }
    public java.util.Set<java.util.Set<OWLDataProperty>>	getSubProperties(OWLDataProperty property) {
        return new java.util.HashSet<java.util.Set<OWLDataProperty>>();
    }
    public java.util.Set<java.util.Set<OWLObjectProperty>>	getSubProperties(OWLObjectProperty property) {
        return new java.util.HashSet<java.util.Set<OWLObjectProperty>>();
    }
    public java.util.Set<java.util.Set<OWLDataProperty>>	getSuperProperties(OWLDataProperty property) {
        return new java.util.HashSet<java.util.Set<OWLDataProperty>>();
    }
    public java.util.Set<java.util.Set<OWLObjectProperty>>	getSuperProperties(OWLObjectProperty property) {
        return new java.util.HashSet<java.util.Set<OWLObjectProperty>>();
    }      
    public boolean	isAntiSymmetric(OWLObjectProperty property) { return false; }
    public boolean	isFunctional(OWLDataProperty property) { return false; }
    public boolean	isFunctional(OWLObjectProperty property) { return false; }
    public boolean	isInverseFunctional(OWLObjectProperty property) { return false; }
    public boolean	isIrreflexive(OWLObjectProperty property) { return false; }
    public boolean	isReflexive(OWLObjectProperty property) { return false; }
    public boolean	isSymmetric(OWLObjectProperty property) { return false; }
    public boolean	isTransitive(OWLObjectProperty property) { return false; }
}
