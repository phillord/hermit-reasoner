// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import org.semanticweb.owl.inference.*;
import org.semanticweb.owl.model.*;
import org.semanticweb.HermiT.hierarchy.*;
import org.semanticweb.HermiT.model.*;
import java.net.URI;

public class HermitReasoner implements OWLReasoner {
    static final String mUriBase = "urn:hermit:kb";

    HermiT mHermitButUseTheAccessFunction;
    java.util.Set<OWLOntology> mOntologies;
    OWLOntology mOntology;
    OWLOntologyManager mManager;
    OWLDataFactory mFactory;
    int mNextKbId;
    
    HermitReasoner(OWLOntologyManager manager) {
        mManager = manager;
        mFactory = mManager.getOWLDataFactory();
        mNextKbId = 1;
        clearOntologies();
    }
    
    protected HermiT getHermit() {
        try {
            if (mHermitButUseTheAccessFunction == null) {
                mHermitButUseTheAccessFunction = new HermiT();
                mHermitButUseTheAccessFunction.loadOwlOntology(mOntology,mFactory,null);
            }
            return mHermitButUseTheAccessFunction;
        } catch (OWLException e) {
            throw new RuntimeException("Error classifying ontology.", e);
        }
    }
    
    // ReasonerBase implementation:
    public void classify() {
        getHermit();
    }
    public void clearOntologies() {
        mHermitButUseTheAccessFunction = null;
        mOntologies = new java.util.HashSet<OWLOntology>();
        mOntology = null;
    }
    public void dispose() {
        clearOntologies();
    }
    public java.util.Set<OWLOntology> getLoadedOntologies() {
        return mOntologies;
    }
    public boolean isClassified() { return mHermitButUseTheAccessFunction != null; }
    public boolean isDefined(OWLClass c)
        { return mOntology.containsClassReference(c.getURI()); }
    public boolean isDefined(OWLIndividual i)
        { return mOntology.containsIndividualReference(i.getURI()); }
    public boolean isDefined(OWLObjectProperty p)
        { return mOntology.containsObjectPropertyReference(p.getURI()); }
    public boolean isDefined(OWLDataProperty p)
        { return mOntology.containsDataPropertyReference(p.getURI()); }
    public boolean isRealised() { return false; }
    public void loadOntologies(java.util.Set<OWLOntology> ontologies) {
        try {
            mOntologies = ontologies;
            URI theUri = null;
            for (OWLOntology i : ontologies) {
                theUri = i.getURI();
                break;
            }
            mOntology =
                new org.semanticweb.owl.util.OWLOntologyMerger(
                    new SetProviderFromSet(mOntologies)
                ).createMergedOntology(mManager,theUri);// URI.create(mUriBase + String.valueOf(mNextKbId++)));
            mHermitButUseTheAccessFunction = null;
        } catch (OWLException e) {
            throw new RuntimeException("Failed to merge ontologies.", e);
        }
    }
    public void realise() {}
    public void unloadOntologies(java.util.Set<OWLOntology> ontologies) {
        mOntologies.removeAll(ontologies);
        loadOntologies(mOntologies);
    }

    class SetProviderFromSet implements OWLOntologySetProvider {
        // Because the OWL API can be a little retarded at times.
        java.util.Set<OWLOntology> mS;
        public SetProviderFromSet(java.util.Set<OWLOntology> s) { mS = s; }
        public java.util.Set<OWLOntology> getOntologies() { return mS; }
    }
    
    // SatisfiabilityChecker implementation:
    public boolean isSatisfiable(OWLDescription d) {
        if (d.isAnonymous()) {
            throw new UnsupportedOperationException("Testing of anonymous classes is not yet supported.");
        }
        return getHermit().isSatisfiable(d.asOWLClass().getURI().toString());
    }
    // ClassReasoner implementation:
    public java.util.Set<java.util.Set<OWLClass>> getDescendantClasses(OWLDescription d) {
        if (d.isAnonymous()) {
            throw new UnsupportedOperationException("Testing of anonymous classes is not yet supported.");
        }
        SubsumptionHierarchy hier = getHermit().getSubsumptionHierarchy();
        SubsumptionHierarchyNode node = hier.getNodeFor(
            AtomicConcept.create(d.asOWLClass().getURI().toString()));
        if (node == null) node = hier.thingNode();
        
        java.util.Set<SubsumptionHierarchyNode> visited = new java.util.HashSet<SubsumptionHierarchyNode>();
        // The Queue interface doesn't let us enqueue collections all at once, which seems silly:
        java.util.LinkedList<SubsumptionHierarchyNode> q = new java.util.LinkedList<SubsumptionHierarchyNode>();
        java.util.Set<java.util.Set<OWLClass>> r = new java.util.HashSet<java.util.Set<OWLClass>>();
        for (; node != null; node = q.poll()) {
            if (visited.contains(node)) continue;
            q.addAll(node.getChildNodes());
            java.util.Set<OWLClass> equivalents = new java.util.HashSet<OWLClass>();
            for (AtomicConcept c : node.getEquivalentConcepts()) {
                equivalents.add(mFactory.getOWLClass(URI.create(c.getURI())));
            }
            r.add(equivalents);
            visited.add(node);
        }
        return r;
    }
    public java.util.Set<java.util.Set<OWLClass>> getAncestorClasses(OWLDescription d) {
        if (d.isAnonymous()) {
            throw new UnsupportedOperationException("Testing of anonymous classes is not yet supported.");
        }
        SubsumptionHierarchy hier = getHermit().getSubsumptionHierarchy();
        SubsumptionHierarchyNode node = hier.getNodeFor(
            AtomicConcept.create(d.asOWLClass().getURI().toString()));
        if (node == null) node = hier.thingNode();
        
        java.util.Set<SubsumptionHierarchyNode> visited = new java.util.HashSet<SubsumptionHierarchyNode>();
        // The Queue interface doesn't let us enqueue collections all at once, which seems silly:
        java.util.LinkedList<SubsumptionHierarchyNode> q = new java.util.LinkedList<SubsumptionHierarchyNode>();
        java.util.Set<java.util.Set<OWLClass>> r = new java.util.HashSet<java.util.Set<OWLClass>>();
        for (; node != null; node = q.poll()) {
            if (visited.contains(node)) continue;
            q.addAll(node.getParentNodes());
            java.util.Set<OWLClass> equivalents = new java.util.HashSet<OWLClass>();
            for (AtomicConcept c : node.getEquivalentConcepts()) {
                equivalents.add(mFactory.getOWLClass(URI.create(c.getURI())));
            }
            r.add(equivalents);
            visited.add(node);
        }
        return r;
    }
    public java.util.Set<OWLClass> getEquivalentClasses(OWLDescription d) {
        if (d.isAnonymous()) {
            throw new UnsupportedOperationException("Testing of anonymous classes is not yet supported.");
        }
        SubsumptionHierarchy hier = getHermit().getSubsumptionHierarchy();
        SubsumptionHierarchyNode node = hier.getNodeFor(
            AtomicConcept.create(d.asOWLClass().getURI().toString()));
        if (node == null) node = hier.thingNode();
        
        java.util.Set<OWLClass> r = new java.util.HashSet<OWLClass>();
        for (AtomicConcept c : node.getEquivalentConcepts()) {
            r.add(mFactory.getOWLClass(URI.create(c.getURI())));
        }
        return r;
    }
    public java.util.Set<OWLClass> getInconsistentClasses() {
        SubsumptionHierarchyNode node = getHermit().getSubsumptionHierarchy().nothingNode();        
        java.util.Set<OWLClass> r = new java.util.HashSet<OWLClass>();
        for (AtomicConcept c : node.getEquivalentConcepts()) {
            r.add(mFactory.getOWLClass(URI.create(c.getURI())));
        }
        return r;
    }
    public java.util.Set<java.util.Set<OWLClass>> getSubClasses(OWLDescription d) {
        if (d.isAnonymous()) {
            throw new UnsupportedOperationException("Testing of anonymous classes is not yet supported.");
        }
        SubsumptionHierarchy hier = getHermit().getSubsumptionHierarchy();
        SubsumptionHierarchyNode node = hier.getNodeFor(
            AtomicConcept.create(d.asOWLClass().getURI().toString()));
        if (node == null) node = hier.thingNode();
        
        java.util.Set<java.util.Set<OWLClass>> r = new java.util.HashSet<java.util.Set<OWLClass>>();
        for (SubsumptionHierarchyNode i : node.getChildNodes()) {
            java.util.Set<OWLClass> equivalents = new java.util.HashSet<OWLClass>();
            for (AtomicConcept c : i.getEquivalentConcepts()) {
                equivalents.add(mFactory.getOWLClass(URI.create(c.getURI())));
            }
            r.add(equivalents);
        }
        return r;
    }
    public java.util.Set<java.util.Set<OWLClass>> getSuperClasses(OWLDescription d) {
        if (d.isAnonymous()) {
            throw new UnsupportedOperationException("Testing of anonymous classes is not yet supported.");
        }
        SubsumptionHierarchy hier = getHermit().getSubsumptionHierarchy();
        SubsumptionHierarchyNode node = hier.getNodeFor(
            AtomicConcept.create(d.asOWLClass().getURI().toString()));
        if (node == null) node = hier.thingNode();
        
        java.util.Set<java.util.Set<OWLClass>> r = new java.util.HashSet<java.util.Set<OWLClass>>();
        for (SubsumptionHierarchyNode i : node.getParentNodes()) {
            java.util.Set<OWLClass> equivalents = new java.util.HashSet<OWLClass>();
            for (AtomicConcept c : i.getEquivalentConcepts()) {
                equivalents.add(mFactory.getOWLClass(URI.create(c.getURI())));
            }
            r.add(equivalents);
        }
        return r;
    }
    public boolean isEquivalentClass(OWLDescription c, OWLDescription d) {
        return isSubClassOf(c, d) && isSubClassOf(d, c);
    }
    public boolean isSubClassOf(OWLDescription subclass, OWLDescription superclass) {
        return getHermit().isSubsumedBy(AtomicConcept.create(subclass.asOWLClass().getURI().toString()),
                                        AtomicConcept.create(superclass.asOWLClass().getURI().toString()));
    }
    
    // ConsistencyChecker implementation:
    public boolean isConsistent(OWLOntology ignored) {
        return getHermit().isABoxSatisfiable();
    }
    
    // IndividualReasoner stubs: (not yet implemented)
    public java.util.Map<OWLDataProperty,java.util.Set<OWLConstant>> getDataPropertyRelationships(OWLIndividual individual) {
        return new java.util.HashMap<OWLDataProperty,java.util.Set<OWLConstant>>();
    }
    public java.util.Set<OWLIndividual> getIndividuals(OWLDescription clsC, boolean direct) {
        return new java.util.HashSet<OWLIndividual>();
    }
    public java.util.Map<OWLObjectProperty,java.util.Set<OWLIndividual>> getObjectPropertyRelationships(OWLIndividual individual) {
        return new java.util.HashMap<OWLObjectProperty,java.util.Set<OWLIndividual>>();
    }
    public java.util.Set<OWLIndividual> getRelatedIndividuals(OWLIndividual subject, OWLObjectPropertyExpression property) {
        return new java.util.HashSet<OWLIndividual>();
    }
    public java.util.Set<OWLConstant> getRelatedValues(OWLIndividual subject, OWLDataPropertyExpression property) {
        return new java.util.HashSet<OWLConstant>();
    }
    public java.util.Set<java.util.Set<OWLClass>> getTypes(OWLIndividual individual, boolean direct) {
        return new java.util.HashSet<java.util.Set<OWLClass>>();
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