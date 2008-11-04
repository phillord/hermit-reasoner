// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.net.URI;
import java.util.Set;

import org.semanticweb.HermiT.hierarchy.HierarchyPosition;
import org.semanticweb.HermiT.monitor.PluginMonitor;
import org.semanticweb.owl.inference.MonitorableOWLReasoner;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologySetProvider;
import org.semanticweb.owl.util.ProgressMonitor;

public class HermitReasoner implements MonitorableOWLReasoner {
    static final String mUriBase = "urn:hermit:kb";

    Reasoner hermit;
    PluginMonitor monitor;
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
        monitor = new PluginMonitor();
    }
    
    public OWLEntity getCurrentEntity() {
        String entUrl = monitor.curConcept();
        if (entUrl == null) {
            return null;
        } else {
            return factory.getOWLClass(URI.create(entUrl));
        }
    }
    
    public void setProgressMonitor(ProgressMonitor m) {
        monitor.setMonitor(m);
    }

    // ReasonerBase implementation:
    public void classify() {
        try {
            monitor.beginTask("Classifying...", hermit.numConceptNames());
            // System.out.println("Seeding subsumption cache...");
            hermit.seedSubsumptionCache();
            // System.out.println("...done");
            } catch (PluginMonitor.Cancelled e) {
                // ignore; if we pass it on the user gets a dialog
            } finally {
            monitor.endTask();
        }
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
            config.monitor = monitor;
            try {
                monitor.beginTask("Loading...");
                // System.out.println("Loading ontology into HermiT...");
                hermit = new Reasoner(ontology, config);
                // System.out.println("...done");
            } catch (PluginMonitor.Cancelled e) {
                // ignore; if we pass it on the user gets a dialog
            } finally {
                monitor.endTask();
            }
        } catch (OWLException e) {
            throw new RuntimeException("Failed to merge ontologies.", e);
        }
    }
    
    public void realise() {
        if (hermit != null) hermit.cacheRealization();
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
        if (hermit == null) return true;
        //System.out.println("Checking satisfiability...");
        return hermit.isClassSatisfiable(d);
    }
    
    protected <T> Set<Set<T>>
        posToSets(Set<HierarchyPosition<T>> positions) {
        java.util.Set<java.util.Set<T>> r
            = new java.util.HashSet<java.util.Set<T>>();
        for (HierarchyPosition<T> pos : positions) {
            r.add(pos.getEquivalents());
        }
        return r;        
    }

    // ClassReasoner implementation:
    public java.util.Set<java.util.Set<OWLClass>>
        getDescendantClasses(OWLDescription d) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLClass>>();
        }
        return posToSets(hermit.getPosition(d).getDescendantPositions());
    }

    public java.util.Set<java.util.Set<OWLClass>>
        getAncestorClasses(OWLDescription d) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLClass>>();
        }
        return posToSets(hermit.getPosition(d).getAncestorPositions());
    }

    public java.util.Set<OWLClass> getEquivalentClasses(OWLDescription d) {
        if (hermit == null) {
            return new java.util.HashSet<OWLClass>();
        }
        return hermit.getPosition(d).getEquivalents();
    }
    
    public java.util.Set<OWLClass> getInconsistentClasses() {
        return getEquivalentClasses(
            factory.getOWLClass(
                URI.create("http://www.w3.org/2002/07/owl#Nothing")));
    }
    
    public java.util.Set<java.util.Set<OWLClass>> getSubClasses(OWLDescription d) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLClass>>();
        }
        return posToSets(hermit.getPosition(d).getChildPositions());
    }
    
    public java.util.Set<java.util.Set<OWLClass>> getSuperClasses(OWLDescription d) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLClass>>();
        }
        return posToSets(hermit.getPosition(d).getParentPositions());
    }
    

    public boolean isEquivalentClass(OWLDescription c, OWLDescription d) {
        if (hermit == null) return false;
        return isSubClassOf(c, d) && isSubClassOf(d, c);
    }
    public boolean isSubClassOf(OWLDescription subclass, OWLDescription superclass) {
        if (hermit == null) return false;
        return hermit.isSubsumedBy(subclass, superclass);
    }
    
    // ConsistencyChecker implementation:
    public boolean isConsistent(OWLOntology ignored) {
        if (hermit == null) return true;
        return hermit.isConsistent();
    }
    
    // IndividualReasoner stubs:
    public java.util.Map<OWLDataProperty,java.util.Set<OWLConstant>> getDataPropertyRelationships(OWLIndividual individual) {
        // TODO: implement (somehow)
        return new java.util.HashMap<OWLDataProperty,java.util.Set<OWLConstant>>();
    }
    
    public java.util.Set<OWLIndividual> getIndividuals(OWLDescription d, boolean direct) {
        if (hermit == null) {
            return new java.util.HashSet<OWLIndividual>();
        }
        if (direct) {
            return hermit.getDirectMembers(d);
        } else {
            return hermit.getMembers(d);
        }
    }

    public java.util.Map<OWLObjectProperty,java.util.Set<OWLIndividual>> getObjectPropertyRelationships(OWLIndividual individual) {
        // TODO: implement (can be done now, but a pain in the ass)
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
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLClass>>();
        }
        if (direct) {
            return posToSets(hermit.getMemberships(individual).getParentPositions());
        } else {
            return posToSets(hermit.getMemberships(individual).getAncestorPositions());
        }
    }

    public boolean hasDataPropertyRelationship(OWLIndividual subject, OWLDataPropertyExpression property, OWLConstant object) {
        return isSubClassOf(
            factory.getOWLObjectOneOf(subject),
            factory.getOWLDataValueRestriction(property, object));
    }
    
    public boolean hasObjectPropertyRelationship(OWLIndividual subject, OWLObjectPropertyExpression property, OWLIndividual object) {
        return isSubClassOf(
            factory.getOWLObjectOneOf(subject),
            factory.getOWLObjectSomeRestriction(property, factory.getOWLObjectOneOf(object)));
    }

    public boolean hasType(OWLIndividual individual, OWLDescription type, boolean direct) {
        if (type instanceof OWLClass && direct) {
            for (HierarchyPosition<OWLClass> pos :
                    hermit.getPosition(factory.getOWLObjectOneOf(individual)).getParentPositions()) {
                if (pos.getEquivalents().contains((OWLClass) type)) {
                    return true;
                }
            }
            return false;
        } else {
            return isSubClassOf(factory.getOWLObjectOneOf(individual), type);
        }
    }
    
    // PropertyReasoner interface:
    public java.util.Set<java.util.Set<OWLDataProperty>>	getAncestorProperties(OWLDataProperty property) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLDataProperty>>();
        }
        return posToSets(hermit.getPosition(property).getAncestorPositions());
    }
    
    public java.util.Set<java.util.Set<OWLObjectProperty>>	getAncestorProperties(OWLObjectProperty property) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLObjectProperty>>();
        }
        return posToSets(hermit.getPosition(property).getAncestorPositions());
    }
    
    public java.util.Set<java.util.Set<OWLDataProperty>>	getDescendantProperties(OWLDataProperty property) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLDataProperty>>();
        }
        return posToSets(hermit.getPosition(property).getDescendantPositions());
    }
    
    public java.util.Set<java.util.Set<OWLObjectProperty>>	getDescendantProperties(OWLObjectProperty property) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLObjectProperty>>();
        }
        return posToSets(hermit.getPosition(property).getDescendantPositions());
    }
    
    public java.util.Set<java.util.Set<OWLDescription>>	getDomains(OWLDataProperty property) {
        java.util.Set<java.util.Set<OWLDescription>> out = new java.util.HashSet<java.util.Set<OWLDescription>>();
        for (java.util.Set<OWLClass> classSet :
            getAncestorClasses(factory.getOWLDataMinCardinalityRestriction(property, 1))) {
            java.util.Set<OWLDescription> newSet = new java.util.HashSet<OWLDescription>();
            for (OWLClass c : classSet) newSet.add(c);
            out.add(newSet);
        }
        return out;
    }
    
    public java.util.Set<java.util.Set<OWLDescription>>	getDomains(OWLObjectProperty property) {
        java.util.Set<java.util.Set<OWLDescription>> out = new java.util.HashSet<java.util.Set<OWLDescription>>();
        for (java.util.Set<OWLClass> classSet :
            getAncestorClasses(factory.getOWLObjectMinCardinalityRestriction(property, 1))) {
            java.util.Set<OWLDescription> newSet = new java.util.HashSet<OWLDescription>();
            for (OWLClass c : classSet) newSet.add(c);
            out.add(newSet);
        }
        return out;
        //return getAncestorClasses(factory.getOWLObjectMinCardinalityRestriction(property, 1));
    }
    
    public java.util.Set<OWLDataProperty>	getEquivalentProperties(OWLDataProperty property) {
        if (hermit == null) {
            return new java.util.HashSet<OWLDataProperty>();
        }
        return hermit.getPosition(property).getEquivalents();
    }
    
    public java.util.Set<OWLObjectProperty>	getEquivalentProperties(OWLObjectProperty property) {
        if (hermit == null) {
            return new java.util.HashSet<OWLObjectProperty>();
        }
        return hermit.getPosition(property).getEquivalents();
    }
    
    public java.util.Set<java.util.Set<OWLObjectProperty>>	getInverseProperties(OWLObjectProperty property) {
        // TODO: implement this (requires property expression classification)
        return new java.util.HashSet<java.util.Set<OWLObjectProperty>>();
    }
    
    public java.util.Set<OWLDataRange>	getRanges(OWLDataProperty property) {
        // TODO: implement this (somehow)
        return new java.util.HashSet<OWLDataRange>();
    }
    
    public java.util.Set<OWLDescription>	getRanges(OWLObjectProperty property) {
        java.util.Set<OWLDescription> newSet = new java.util.HashSet<OWLDescription>();
        for (java.util.Set<OWLClass> classSet :
            getAncestorClasses(factory.getOWLObjectMinCardinalityRestriction(property.getInverseProperty(), 1))) {
            for (OWLClass c : classSet) newSet.add(c);
        }
        return newSet;
        //return getAncestorClasses(factory.getOWLObjectMinCardinalityRestriction(property.getInverseProperty(), 1));
    }
    
    public java.util.Set<java.util.Set<OWLDataProperty>>	getSubProperties(OWLDataProperty property) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLDataProperty>>();
        }
        return posToSets(hermit.getPosition(property).getChildPositions());
    }
    
    public java.util.Set<java.util.Set<OWLObjectProperty>>	getSubProperties(OWLObjectProperty property) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLObjectProperty>>();
        }
        return posToSets(hermit.getPosition(property).getChildPositions());
    }
    
    public java.util.Set<java.util.Set<OWLDataProperty>>	getSuperProperties(OWLDataProperty property) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLDataProperty>>();
        }
        return posToSets(hermit.getPosition(property).getParentPositions());
    }
    
    public java.util.Set<java.util.Set<OWLObjectProperty>>	getSuperProperties(OWLObjectProperty property) {
        if (hermit == null) {
            return new java.util.HashSet<java.util.Set<OWLObjectProperty>>();
        }
        return posToSets(hermit.getPosition(property).getParentPositions());
    }

    public boolean	isFunctional(OWLDataProperty property) {
        return !isSatisfiable(factory.getOWLDataMinCardinalityRestriction(property, 2));
    }
    
    public boolean	isFunctional(OWLObjectProperty property) {
        return !isSatisfiable(factory.getOWLObjectMinCardinalityRestriction(property, 2));
    }
    
    public boolean	isInverseFunctional(OWLObjectProperty property) {
        return !isSatisfiable(factory.getOWLObjectMinCardinalityRestriction(property.getInverseProperty(), 2));
    }
    
    public boolean	isIrreflexive(OWLObjectProperty property) {
        return !isSatisfiable(factory.getOWLObjectSelfRestriction(property));
    }

    public boolean	isReflexive(OWLObjectProperty property) {
        return !isSatisfiable(factory.getOWLObjectComplementOf(factory.getOWLObjectSelfRestriction(property)));
    }

    public boolean	isAntiSymmetric(OWLObjectProperty property) {
        // this function is mis-named: check Asymmetry, not Antisymmetry:
        if (hermit == null) return false;
        return hermit.isAsymmetric(property);
    }

    // TODO: get negative property assertions working in HermiT so that we can implement these
    public boolean	isSymmetric(OWLObjectProperty property) { return false; }
    public boolean	isTransitive(OWLObjectProperty property) { return false; }
}
