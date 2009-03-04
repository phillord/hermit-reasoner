// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.owlapi.reasoner;

import java.net.URI;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.hierarchy.HierarchyPosition;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.owl.inference.MonitorableOWLReasoner;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.ProgressMonitor;

public class HermiTReasoner implements MonitorableOWLReasoner {
    protected final OWLOntologyManager manager;
    protected final boolean tolerateUnknownDatatypes;
    protected final boolean onlyAncestorBlocking;
    protected final PluginMonitor monitor;
    protected final Set<OWLOntology> ontologies;
    protected final Set<OWLClass> allReferencedClasses;
    protected final Set<OWLIndividual> allReferencedIndividuals;
    protected final Set<OWLDataProperty> allReferencedDataProperties;
    protected final Set<OWLObjectProperty> allReferencedObjectProperties;
    protected Reasoner hermit;

    public HermiTReasoner(OWLOntologyManager inManager) {
        this(inManager,false,false);
    }

    public HermiTReasoner(OWLOntologyManager inManager,boolean tolerateUnknownDatatypes) {
        this(inManager,tolerateUnknownDatatypes,false);
    }

    public HermiTReasoner(OWLOntologyManager inManager,boolean tolerateUnknownDatatypes,boolean onlyAncestorBlocking) {
        monitor=new PluginMonitor();
        manager=inManager;
        ontologies=new HashSet<OWLOntology>();
        allReferencedClasses=new HashSet<OWLClass>();
        allReferencedIndividuals=new HashSet<OWLIndividual>();
        allReferencedDataProperties=new HashSet<OWLDataProperty>();
        allReferencedObjectProperties=new HashSet<OWLObjectProperty>();
        this.tolerateUnknownDatatypes=tolerateUnknownDatatypes;
        this.onlyAncestorBlocking=onlyAncestorBlocking;
    }

    public OWLEntity getCurrentEntity() {
        AtomicConcept currentConcept=monitor.currentlyClassifiedConcept();
        if (currentConcept==null)
            return null;
        else
            return manager.getOWLDataFactory().getOWLClass(URI.create(currentConcept.getURI()));
    }

    public void setProgressMonitor(ProgressMonitor m) {
        monitor.setMonitor(m);
    }

    public void classify() {
        try {
            monitor.beginTask("Classifying...",hermit.getDLOntology().getNumberOfExternalConcepts());
            hermit.computeClassHierarchy();
        }
        catch (PluginMonitor.Cancelled e) {
            // ignore; if we pass it on the user gets a dialog
        }
        finally {
            monitor.endTask();
        }
    }

    public void clearOntologies() {
        hermit=null;
        ontologies.clear();
        allReferencedClasses.clear();
        allReferencedIndividuals.clear();
        allReferencedDataProperties.clear();
        allReferencedObjectProperties.clear();
    }

    public void dispose() {
        clearOntologies();
    }

    public Set<OWLOntology> getLoadedOntologies() {
        return new HashSet<OWLOntology>(ontologies);
    }

    public boolean isClassified() {
        return hermit!=null && hermit.isClassHierarchyComputed();
    }

    public boolean isDefined(OWLClass c) {
        return allReferencedClasses.contains(c);
    }

    public boolean isDefined(OWLIndividual i) {
        return allReferencedIndividuals.contains(i);
    }

    public boolean isDefined(OWLObjectProperty p) {
        return allReferencedObjectProperties.contains(p);
    }

    public boolean isDefined(OWLDataProperty p) {
        return allReferencedDataProperties.contains(p);
    }

    public boolean isRealised() {
        return hermit!=null && hermit.isRealizationComputed();
    }

    public void loadOntologies(Set<OWLOntology> inOntologies) {
        // Load the given set of ontologies.
        // The set contains already all ontologies required for the import
        // closure.
        // The manager might at this point contain more ontologies than the ones
        // we have to load.
        try {
            clearOntologies();
            ontologies.addAll(inOntologies);
            for (OWLOntology ontology : ontologies) {
                allReferencedClasses.addAll(ontology.getReferencedClasses());
                allReferencedIndividuals.addAll(ontology.getReferencedIndividuals());
                allReferencedDataProperties.addAll(ontology.getReferencedDataProperties());
                allReferencedObjectProperties.addAll(ontology.getReferencedObjectProperties());
            }
            Configuration config=new Configuration();
            config.monitor=monitor;
            config.ignoreUnsupportedDatatypes=tolerateUnknownDatatypes;
            if (onlyAncestorBlocking) {
                config.blockingStrategyType=Configuration.BlockingStrategyType.ANCESTOR;
            }
            try {
                monitor.beginTask("Loading...");
                hermit=new Reasoner(config,manager,ontologies,"urn:hermit:kb");
            }
            catch (PluginMonitor.Cancelled e) {
                // ignore; if we pass it on the user gets a dialog
            }
            finally {
                monitor.endTask();
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to merge ontologies.",e);
        }
    }

    public void realise() {
        if (hermit!=null)
            hermit.computeRealization();
    }
    public void unloadOntologies(Set<OWLOntology> inOntologies) {
        HashSet<OWLOntology> remainingOntologies=new HashSet<OWLOntology>(ontologies);
        remainingOntologies.removeAll(inOntologies);
        loadOntologies(remainingOntologies);
    }

    public boolean isSatisfiable(OWLDescription d) {
        if (hermit==null)
            return true;
        return hermit.isClassSatisfiable(d);
    }

    protected <T> Set<Set<T>> posToSets(Set<HierarchyPosition<T>> positions) {
        Set<Set<T>> r=new HashSet<Set<T>>();
        for (HierarchyPosition<T> pos : positions) {
            Set<T> equivalents=pos.getEquivalents();
            r.add(new HashSet<T>(equivalents));
        }
        return r;
    }

    public Set<Set<OWLClass>> getDescendantClasses(OWLDescription d) {
        if (hermit==null) {
            return new HashSet<Set<OWLClass>>();
        }
        return posToSets(hermit.getClassHierarchyPosition(d).getDescendantPositions());
    }

    public Set<Set<OWLClass>> getAncestorClasses(OWLDescription d) {
        if (hermit==null) {
            return new HashSet<Set<OWLClass>>();
        }
        return posToSets(hermit.getClassHierarchyPosition(d).getAncestorPositions());
    }

    public Set<OWLClass> getEquivalentClasses(OWLDescription d) {
        if (hermit==null) {
            return new HashSet<OWLClass>();
        }
        return new HashSet<OWLClass>(hermit.getClassHierarchyPosition(d).getEquivalents());
    }

    public Set<OWLClass> getInconsistentClasses() {
        return getEquivalentClasses(manager.getOWLDataFactory().getOWLClass(URI.create("http://www.w3.org/2002/07/owl#Nothing")));
    }

    public Set<Set<OWLClass>> getSubClasses(OWLDescription d) {
        if (hermit==null) {
            return new HashSet<Set<OWLClass>>();
        }
        return posToSets(hermit.getClassHierarchyPosition(d).getChildPositions());
    }

    public Set<Set<OWLClass>> getSuperClasses(OWLDescription d) {
        if (hermit==null) {
            return new HashSet<Set<OWLClass>>();
        }
        return posToSets(hermit.getClassHierarchyPosition(d).getParentPositions());
    }

    public boolean isEquivalentClass(OWLDescription c,OWLDescription d) {
        if (hermit==null)
            return false;
        return isSubClassOf(c,d)&&isSubClassOf(d,c);
    }
    public boolean isSubClassOf(OWLDescription subclass,OWLDescription superclass) {
        if (hermit==null)
            return false;
        return hermit.isClassSubsumedBy(subclass,superclass);
    }

    public boolean isConsistent(OWLOntology ignored) {
        if (hermit==null)
            return true;
        return hermit.isConsistent();
    }

    public Map<OWLDataProperty,Set<OWLConstant>> getDataPropertyRelationships(OWLIndividual individual) {
        return new HashMap<OWLDataProperty,Set<OWLConstant>>();
    }

    public Set<OWLIndividual> getIndividuals(OWLDescription d,boolean direct) {
        if (hermit==null) {
            return new HashSet<OWLIndividual>();
        }
        else if (direct) {
            return new HashSet<OWLIndividual>(hermit.getClassDirectInstances(d));
        }
        else {
            return new HashSet<OWLIndividual>(hermit.getClassInstances(d));
        }
    }

    public Map<OWLObjectProperty,Set<OWLIndividual>> getObjectPropertyRelationships(OWLIndividual individual) {
        // TODO: implement (can be done now, but a pain in the ass)
        return new HashMap<OWLObjectProperty,Set<OWLIndividual>>();
    }

    public Set<OWLIndividual> getRelatedIndividuals(OWLIndividual subject,OWLObjectPropertyExpression property) {
        return getIndividuals(manager.getOWLDataFactory().getOWLObjectSomeRestriction(property.getInverseProperty(),manager.getOWLDataFactory().getOWLObjectOneOf(subject)),false);
    }

    public Set<OWLConstant> getRelatedValues(OWLIndividual subject,OWLDataPropertyExpression property) {
        // TODO: implement (somehow)
        return new HashSet<OWLConstant>();
    }

    public Set<Set<OWLClass>> getTypes(OWLIndividual individual,boolean direct) {
        Set<Set<OWLClass>> result=new HashSet<Set<OWLClass>>();
        if (hermit!=null) {
            Set<HierarchyPosition<OWLClass>> individualTypes=hermit.getIndividualTypes(individual);
            for (HierarchyPosition<OWLClass> individualType : individualTypes) {
                if (direct) {
                    result.addAll(posToSets(individualType.getParentPositions()));
                }
                else {
                    result.addAll(posToSets(individualType.getAncestorPositions()));
                }
            }
        }
        return result;
    }

    public boolean hasDataPropertyRelationship(OWLIndividual subject,OWLDataPropertyExpression property,OWLConstant object) {
        return isSubClassOf(manager.getOWLDataFactory().getOWLObjectOneOf(subject),manager.getOWLDataFactory().getOWLDataValueRestriction(property,object));
    }

    public boolean hasObjectPropertyRelationship(OWLIndividual subject,OWLObjectPropertyExpression property,OWLIndividual object) {
        return isSubClassOf(manager.getOWLDataFactory().getOWLObjectOneOf(subject),manager.getOWLDataFactory().getOWLObjectSomeRestriction(property,manager.getOWLDataFactory().getOWLObjectOneOf(object)));
    }

    public boolean hasType(OWLIndividual individual,OWLDescription type,boolean direct) {
        if (type instanceof OWLClass&&direct) {
            for (HierarchyPosition<OWLClass> pos : hermit.getClassHierarchyPosition(manager.getOWLDataFactory().getOWLObjectOneOf(individual)).getParentPositions()) {
                if (pos.getEquivalents().contains((OWLClass)type)) {
                    return true;
                }
            }
            return false;
        }
        else {
            return isSubClassOf(manager.getOWLDataFactory().getOWLObjectOneOf(individual),type);
        }
    }

    public Set<Set<OWLDataProperty>> getAncestorProperties(OWLDataProperty property) {
        if (hermit==null) {
            return new HashSet<Set<OWLDataProperty>>();
        }
        return posToSets(hermit.getPropertyHierarchyPosition(property).getAncestorPositions());
    }

    public Set<Set<OWLObjectProperty>> getAncestorProperties(OWLObjectProperty property) {
        if (hermit==null) {
            return new HashSet<Set<OWLObjectProperty>>();
        }
        return posToSets(hermit.getPropertyHierarchyPosition(property).getAncestorPositions());
    }

    public Set<Set<OWLDataProperty>> getDescendantProperties(OWLDataProperty property) {
        if (hermit==null) {
            return new HashSet<Set<OWLDataProperty>>();
        }
        return posToSets(hermit.getPropertyHierarchyPosition(property).getDescendantPositions());
    }

    public Set<Set<OWLObjectProperty>> getDescendantProperties(OWLObjectProperty property) {
        if (hermit==null) {
            return new HashSet<Set<OWLObjectProperty>>();
        }
        return posToSets(hermit.getPropertyHierarchyPosition(property).getDescendantPositions());
    }

    public Set<Set<OWLDescription>> getDomains(OWLDataProperty property) {
        Set<Set<OWLDescription>> out=new HashSet<Set<OWLDescription>>();
        for (Set<OWLClass> classSet : getAncestorClasses(manager.getOWLDataFactory().getOWLDataMinCardinalityRestriction(property,1))) {
            Set<OWLDescription> newSet=new HashSet<OWLDescription>(classSet);
            out.add(newSet);
        }
        return out;
    }

    public Set<Set<OWLDescription>> getDomains(OWLObjectProperty property) {
        Set<Set<OWLDescription>> out=new HashSet<Set<OWLDescription>>();
        for (Set<OWLClass> classSet : getAncestorClasses(manager.getOWLDataFactory().getOWLObjectMinCardinalityRestriction(property,1))) {
            Set<OWLDescription> newSet=new HashSet<OWLDescription>(classSet);
            out.add(newSet);
        }
        return out;
    }

    public Set<OWLDataProperty> getEquivalentProperties(OWLDataProperty property) {
        if (hermit==null) {
            return new HashSet<OWLDataProperty>();
        }
        return new HashSet<OWLDataProperty>(hermit.getPropertyHierarchyPosition(property).getEquivalents());
    }

    public Set<OWLObjectProperty> getEquivalentProperties(OWLObjectProperty property) {
        if (hermit==null) {
            return new HashSet<OWLObjectProperty>();
        }
        return new HashSet<OWLObjectProperty>(hermit.getPropertyHierarchyPosition(property).getEquivalents());
    }

    public Set<Set<OWLObjectProperty>> getInverseProperties(OWLObjectProperty property) {
        // TODO: implement this (requires property expression classification)
        return new HashSet<Set<OWLObjectProperty>>();
    }

    public Set<OWLDataRange> getRanges(OWLDataProperty property) {
        // TODO: implement this (somehow)
        return new HashSet<OWLDataRange>();
    }

    public Set<OWLDescription> getRanges(OWLObjectProperty property) {
        Set<OWLDescription> newSet=new HashSet<OWLDescription>();
        for (Set<OWLClass> classSet : getAncestorClasses(manager.getOWLDataFactory().getOWLObjectMinCardinalityRestriction(property.getInverseProperty(),1))) {
            newSet.addAll(classSet);
        }
        return newSet;
    }

    public Set<Set<OWLDataProperty>> getSubProperties(OWLDataProperty property) {
        if (hermit==null) {
            return new HashSet<Set<OWLDataProperty>>();
        }
        return posToSets(hermit.getPropertyHierarchyPosition(property).getChildPositions());
    }

    public Set<Set<OWLObjectProperty>> getSubProperties(OWLObjectProperty property) {
        if (hermit==null) {
            return new HashSet<Set<OWLObjectProperty>>();
        }
        return posToSets(hermit.getPropertyHierarchyPosition(property).getChildPositions());
    }

    public Set<Set<OWLDataProperty>> getSuperProperties(OWLDataProperty property) {
        if (hermit==null) {
            return new HashSet<Set<OWLDataProperty>>();
        }
        return posToSets(hermit.getPropertyHierarchyPosition(property).getParentPositions());
    }

    public Set<Set<OWLObjectProperty>> getSuperProperties(OWLObjectProperty property) {
        if (hermit==null) {
            return new HashSet<Set<OWLObjectProperty>>();
        }
        return posToSets(hermit.getPropertyHierarchyPosition(property).getParentPositions());
    }

    public boolean isFunctional(OWLDataProperty property) {
        return !isSatisfiable(manager.getOWLDataFactory().getOWLDataMinCardinalityRestriction(property,2));
    }

    public boolean isFunctional(OWLObjectProperty property) {
        return !isSatisfiable(manager.getOWLDataFactory().getOWLObjectMinCardinalityRestriction(property,2));
    }

    public boolean isInverseFunctional(OWLObjectProperty property) {
        return !isSatisfiable(manager.getOWLDataFactory().getOWLObjectMinCardinalityRestriction(property.getInverseProperty(),2));
    }

    public boolean isIrreflexive(OWLObjectProperty property) {
        return !isSatisfiable(manager.getOWLDataFactory().getOWLObjectSelfRestriction(property));
    }

    public boolean isReflexive(OWLObjectProperty property) {
        return !isSatisfiable(manager.getOWLDataFactory().getOWLObjectComplementOf(manager.getOWLDataFactory().getOWLObjectSelfRestriction(property)));
    }

    public boolean isAntiSymmetric(OWLObjectProperty property) {
        // this function is mis-named: check Asymmetry, not Antisymmetry:
        if (hermit==null)
            return false;
        return hermit.isAsymmetric(property);
    }

    public boolean isSymmetric(OWLObjectProperty property) {
        // TODO: get negative property assertions working in HermiT so that we can implement this
        return false;
    }
    public boolean isTransitive(OWLObjectProperty property) {
        // TODO: get negative property assertions working in HermiT so that we can implement this
        return false;
    }
}
