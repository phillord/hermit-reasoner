// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.owlapi.structural;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.hierarchy.HierarchyPosition;
import org.semanticweb.HermiT.hierarchy.NaiveHierarchyPosition;
import org.semanticweb.HermiT.model.AtLeastAbstractRoleConcept;
import org.semanticweb.HermiT.model.AtLeastConcreteRoleConcept;
import org.semanticweb.HermiT.model.AtMostAbstractRoleGuard;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.KeyClause;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.NodeIDLessThan;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.dataranges.BigRational;
import org.semanticweb.HermiT.model.dataranges.DataConstant;
import org.semanticweb.HermiT.model.dataranges.DataRange;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionBoolean;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionDateTime;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionDouble;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionFloat;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionInteger;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionLiteral;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionOWLRealPlus;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionRational;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionString;
import org.semanticweb.HermiT.model.dataranges.EnumeratedDataRange;
import org.semanticweb.HermiT.model.dataranges.DataConstant.Impl;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets;
import org.semanticweb.HermiT.util.GraphUtils;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataComplementOf;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataRangeFacetRestriction;
import org.semanticweb.owl.model.OWLDataRangeRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDataVisitor;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitor;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.model.OWLUntypedConstant;
import org.semanticweb.owl.vocab.OWLRestrictedDataRangeFacetVocabulary;

import dk.brics.automaton.Datatypes;

public class OwlClausification implements Serializable {
    private static final long serialVersionUID = 1909494208824352106L;
    protected static final org.semanticweb.HermiT.model.Variable X = org.semanticweb.HermiT.model.Variable.create("X");
    protected static final org.semanticweb.HermiT.model.Variable Y = org.semanticweb.HermiT.model.Variable.create("Y");
    protected static final org.semanticweb.HermiT.model.Variable Z = org.semanticweb.HermiT.model.Variable.create("Z");
    protected final Reasoner.Configuration config;
    private int amqOffset; // the number of negative at-most replacements already performed
    
    public OwlClausification(Reasoner.Configuration config) {
        amqOffset = 0;
        this.config = config;
    }
    
    public DLOntology clausify(
            OWLOntologyManager ontologyManager,
            OWLOntology ontology,
            Collection<DescriptionGraph> descriptionGraphs) throws OWLException {
        Set<OWLHasKeyDummy> noKeys = Collections.emptySet();
        return clausifyWithKeys(ontologyManager, ontology, descriptionGraphs, noKeys);
    }

    public DLOntology clausifyOntologiesDisregardImports(OWLOntologyManager ontologyManager,Collection<OWLOntology> ontologies,String resultingDLOntologyURI)  {
        OWLAxioms axioms=new OWLAxioms();
        OwlNormalization normalization = new OwlNormalization(ontologyManager.getOWLDataFactory(),axioms);
        Set<OWLClass> allReferencedClasses=new HashSet<OWLClass>();
        Set<OWLIndividual> allReferencedIndividuals=new HashSet<OWLIndividual>();
        Set<OWLDataProperty> allReferencedDataProperties=new HashSet<OWLDataProperty>();
        Set<OWLObjectProperty> allReferencedObjectProperties=new HashSet<OWLObjectProperty>();
        for (OWLOntology ontology : ontologies) {
            normalization.processOntology(config, ontology);
            allReferencedClasses.addAll(ontology.getReferencedClasses());
            allReferencedIndividuals.addAll(ontology.getReferencedIndividuals());
            allReferencedDataProperties.addAll(ontology.getReferencedDataProperties());
            allReferencedObjectProperties.addAll(ontology.getReferencedObjectProperties());
        }
        TransitivityManager transitivityManager=new TransitivityManager(ontologyManager.getOWLDataFactory());
        transitivityManager.rewriteConceptInclusions(axioms);
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        return clausify(
                ontologyManager.getOWLDataFactory(),
                resultingDLOntologyURI,
                axioms.m_conceptInclusions,
                axioms.m_objectPropertyInclusions,
                axioms.m_dataPropertyInclusions,
                axioms.m_asymmetricObjectProperties,
                axioms.m_reflexiveObjectProperties,
                axioms.m_irreflexiveObjectProperties,
                axioms.m_disjointObjectProperties,
                axioms.m_disjointDataProperties,
                axioms.m_hasKeys,
                axioms.m_facts,
                noDescriptionGraphs,
                allReferencedClasses,
                allReferencedIndividuals,
                allReferencedDataProperties,
                allReferencedObjectProperties);
    }

    /**
     * Dummy function to sneak in keys. ONLY TO BE USED FOR TESTING PURPOSES, 
     * while waiting for official OWL API support for keys. 
     * @param ontology an OWL API loaded ontology
     * @param descriptionGraphs some description graphs
     * @param keys some keys that are to be used during the reasoning (only 
     *             atomic concepts are supported)
     * @return an ontology in HermiT internal format
     * @throws OWLException
     */
    public DLOntology clausifyWithKeys(
            OWLOntologyManager ontologyManager,
            OWLOntology ontology,
            Collection<DescriptionGraph> descriptionGraphs, 
            Set<OWLHasKeyDummy> keys) {

        OWLAxioms axioms=new OWLAxioms();
        OwlNormalization normalization = new OwlNormalization(ontologyManager.getOWLDataFactory(),axioms);
        
        Set<OWLClass> allReferencedClasses=new HashSet<OWLClass>();
        Set<OWLIndividual> allReferencedIndividuals=new HashSet<OWLIndividual>();
        Set<OWLDataProperty> allReferencedDataProperties=new HashSet<OWLDataProperty>();
        Set<OWLObjectProperty> allReferencedObjectProperties=new HashSet<OWLObjectProperty>();

        Set<OWLOntology> processedOntologies = new HashSet<OWLOntology>();
        List<OWLOntology> toProcess = new ArrayList<OWLOntology>();
        toProcess.add(ontology);
        while (!toProcess.isEmpty()) {
            OWLOntology anOntology = toProcess.remove(toProcess.size() - 1);
            if (!processedOntologies.contains(anOntology)) {
                toProcess.addAll(anOntology.getImports(ontologyManager));
                normalization.processOntology(config, anOntology);
                allReferencedClasses.addAll(anOntology.getReferencedClasses());
                allReferencedIndividuals.addAll(anOntology.getReferencedIndividuals());
                allReferencedDataProperties.addAll(anOntology.getReferencedDataProperties());
                allReferencedObjectProperties.addAll(anOntology.getReferencedObjectProperties());
                processedOntologies.add(anOntology);
            }
        }
        normalization.processKeys(config, keys);

        TransitivityManager transitivityManager=new TransitivityManager(ontologyManager.getOWLDataFactory());
        transitivityManager.rewriteConceptInclusions(axioms);

        return clausify(
                ontologyManager.getOWLDataFactory(),
                ontology.getURI().toString(),
                axioms.m_conceptInclusions,
                axioms.m_objectPropertyInclusions,
                axioms.m_dataPropertyInclusions,
                axioms.m_asymmetricObjectProperties,
                axioms.m_reflexiveObjectProperties,
                axioms.m_irreflexiveObjectProperties,
                axioms.m_disjointObjectProperties,
                axioms.m_disjointDataProperties,
                axioms.m_hasKeys,
                axioms.m_facts,
                descriptionGraphs,
                allReferencedClasses,
                allReferencedIndividuals,
                allReferencedDataProperties,
                allReferencedObjectProperties);
    }
    
    static protected Map<AtomicRole, HierarchyPosition<AtomicRole>>
        buildRoleHierarchy(
            Collection<OWLObjectPropertyExpression[]> objInclusions,
            Collection<OWLDataPropertyExpression[]> dataInclusions,
            Set<AtomicRole> objectRoles, Set<AtomicRole> dataRoles) {
        final Map<Role, Set<Role>> subRoles = new HashMap<Role, Set<Role>>();
        for (OWLObjectPropertyExpression[] inclusion : objInclusions) {
            Role sub = getRole(inclusion[0]);
            Role sup = getRole(inclusion[1]);
            Set<Role> subs = subRoles.get(sup);
            if (subs == null) {
                subs = new HashSet<Role>();
                subRoles.put(sup, subs);
            }
            subs.add(sub);
        }
        for (OWLDataPropertyExpression[] inclusion : dataInclusions) {
            Role sub = getRole(inclusion[0]);
            Role sup = getRole(inclusion[1]);
            Set<Role> subs = subRoles.get(sup);
            if (subs == null) {
                subs = new HashSet<Role>();
                subRoles.put(sup, subs);
            }
            subs.add(sub);
        }
        
        GraphUtils.transitivelyClose(subRoles);
        
        NaiveHierarchyPosition.Ordering<AtomicRole> ordering =
            new NaiveHierarchyPosition.Ordering<AtomicRole>() {
                public boolean less(AtomicRole sub, AtomicRole sup) {
                    if (sup == AtomicRole.TOP_DATA_ROLE ||
                        sup == AtomicRole.TOP_OBJECT_ROLE ||
                        sub == AtomicRole.BOTTOM_DATA_ROLE ||
                        sub == AtomicRole.BOTTOM_OBJECT_ROLE) {
                        return true;
                    }
                    Set<Role> subs = subRoles.get(sup);
                    if (subs == null) {
                        return false;
                    }
                    return subs.contains(sub);
                }
            };
        Map<AtomicRole, HierarchyPosition<AtomicRole>> hierarchy =
            NaiveHierarchyPosition.buildHierarchy(
                AtomicRole.TOP_OBJECT_ROLE,
                AtomicRole.BOTTOM_OBJECT_ROLE,
                objectRoles,
                ordering);
        
        hierarchy.putAll(NaiveHierarchyPosition.buildHierarchy(
                            AtomicRole.TOP_DATA_ROLE,
                            AtomicRole.BOTTOM_DATA_ROLE,
                            dataRoles,
                            ordering));
        return hierarchy;
    }


    protected DLOntology clausify(
            OWLDataFactory factory,
            String ontologyURI,
            Collection<OWLDescription[]> conceptInclusions,
            Collection<OWLObjectPropertyExpression[]> objectPropertyInclusions,
            Collection<OWLDataPropertyExpression[]> dataPropertyInclusions,
            Set<OWLObjectPropertyExpression> asymmetricObjectProperties,
            Set<OWLObjectPropertyExpression> reflexiveObjectProperties,
            Set<OWLObjectPropertyExpression> irreflexiveObjectProperties,
            Collection<OWLObjectPropertyExpression[]> disjointObjectProperties,
            Collection<OWLDataPropertyExpression[]> disjointDataProperties,
            Set<OWLHasKeyDummy> hasKeys, 
            Collection<OWLIndividualAxiom> facts,
            Collection<DescriptionGraph> descriptionGraphs,
            Set<OWLClass> classes,
            Set<OWLIndividual> individuals,
            Set<OWLDataProperty> dataProperties,
            Set<OWLObjectProperty> objectProperties) {
        DetermineExpressivity determineExpressivity = new DetermineExpressivity();
        for (OWLDescription[] inclusion : conceptInclusions)
            for (OWLDescription description : inclusion)
                description.accept(determineExpressivity);
        for (OWLObjectPropertyExpression[] inclusion : objectPropertyInclusions) {
            boolean isInverse0 = (inclusion[0] instanceof OWLObjectPropertyInverse);
            boolean isInverse1 = (inclusion[1] instanceof OWLObjectPropertyInverse);
            if ((!isInverse0 && isInverse1) || (isInverse0 && !isInverse1))
                determineExpressivity.m_hasInverseRoles = true;
        }
        if (reflexiveObjectProperties.size() > 0)
            determineExpressivity.m_hasReflexivity = true;
        if (dataPropertyInclusions.size() > 0) {
            determineExpressivity.m_hasDatatypes = true;
        }
        if (hasKeys.size() > 0) 
            determineExpressivity.m_hasKeys = true;
        Set<DLClause> dlClauses = new LinkedHashSet<DLClause>();
        Set<Atom> positiveFacts = new HashSet<Atom>();
        Set<Atom> negativeFacts = new HashSet<Atom>();
        for (OWLObjectPropertyExpression[] inclusion : objectPropertyInclusions) {
            Atom subRoleAtom = getRoleAtom(inclusion[0], X, Y);
            Atom superRoleAtom = getRoleAtom(inclusion[1], X, Y);
            DLClause dlClause = DLClause.create(new Atom[] { superRoleAtom },
                    new Atom[] { subRoleAtom });
            dlClauses.add(dlClause);
        }
        for (OWLDataPropertyExpression[] inclusion : dataPropertyInclusions) {
            Atom subProp = getDataPropertyAtom(inclusion[0], X, Y);
            Atom superProp = getDataPropertyAtom(inclusion[1], X, Y);
            DLClause dlClause = DLClause.create(new Atom[] { superProp },
                    new Atom[] { subProp });
            dlClauses.add(dlClause);
        }
        for (OWLObjectPropertyExpression axiom : asymmetricObjectProperties) {
            Atom roleAtom = getRoleAtom(axiom, X, Y);
            Atom inverseRoleAtom = getRoleAtom(axiom, Y, X);
            DLClause dlClause = DLClause.create(new Atom[] {}, new Atom[] {
                    roleAtom, inverseRoleAtom });
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression axiom : reflexiveObjectProperties) {
            Atom roleAtom = getRoleAtom(axiom, X, X);
            DLClause dlClause = DLClause.create(new Atom[] { roleAtom },
                    new Atom[] {});
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression axiom : irreflexiveObjectProperties) {
            Atom roleAtom = getRoleAtom(axiom, X, X);
            DLClause dlClause = DLClause.create(new Atom[] {},
                    new Atom[] { roleAtom });
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression[] properties : disjointObjectProperties) {
            for (int i = 0; i < properties.length; i++) {
                for (int j = i + 1; j < properties.length; j++) {
                    Atom atom_i = getRoleAtom(properties[i], X, Y);
                    Atom atom_j = getRoleAtom(properties[j], X, Y);
                    DLClause dlClause = DLClause.create(new Atom[] {},
                            new Atom[] { atom_i, atom_j });
                    dlClauses.add(dlClause.getSafeVersion());
                }
            }
        }
        { // make bottom roles unsatisfiable:
            Atom bodyAtom = Atom.create(AtomicRole.BOTTOM_OBJECT_ROLE,
                new org.semanticweb.HermiT.model.Term[] { X, Y });
            dlClauses.add(DLClause.create(
                new Atom[] {}, new Atom[] { bodyAtom }).getSafeVersion());
            bodyAtom = Atom.create(AtomicRole.BOTTOM_DATA_ROLE,
                new org.semanticweb.HermiT.model.Term[] { X, Y });
            dlClauses.add(DLClause.create(
                new Atom[] {}, new Atom[] { bodyAtom }).getSafeVersion());
        }
        for (OWLDataPropertyExpression[] properties : disjointDataProperties) {
            for (int i = 0; i < properties.length; i++) {
                for (int j = i + 1; j < properties.length; j++) {
                    Atom atom_i = getDataPropertyAtom(properties[i], X, Y);
                    Atom atom_j = getDataPropertyAtom(properties[j], X, Z);
                    Atom atom_ij = Atom.create(Inequality.create(),
                            new org.semanticweb.HermiT.model.Term[] { Y, Z });
                    DLClause dlClause = DLClause.create(new Atom[] { atom_ij },
                            new Atom[] { atom_i, atom_j });
                    dlClauses.add(dlClause.getSafeVersion());
                }
            }
        }
        boolean shouldUseNIRule = determineExpressivity.m_hasAtMostRestrictions
                && determineExpressivity.m_hasInverseRoles
                && (determineExpressivity.m_hasNominals ||
                    config.existentialStrategyType ==
                        Reasoner.ExistentialStrategyType.INDIVIDUAL_REUSE);
        if (config.prepareForExpressiveQueries) {
            shouldUseNIRule = true;
        }
        Clausifier clausifier = new Clausifier(positiveFacts, shouldUseNIRule,
                factory, amqOffset, config.ignoreUnsupportedDatatypes);
        for (OWLDescription[] inclusion : conceptInclusions) {
            for (OWLDescription description : inclusion)
                description.accept(clausifier);
            DLClause dlClause = clausifier.getDLClause();
            dlClauses.add(dlClause.getSafeVersion());
        }
        amqOffset += clausifier.clausifyAtMostStuff(dlClauses);
        for (OWLHasKeyDummy hk : hasKeys) {
            dlClauses.add(clausifyKey(hk).getSafeVersion());
        }
        FactClausifier factClausifier = new FactClausifier(positiveFacts,
                negativeFacts);
        for (OWLIndividualAxiom fact : facts)
            fact.accept(factClausifier);
        for (DescriptionGraph descriptionGraph : descriptionGraphs) {
            descriptionGraph.produceStartDLClauses(dlClauses);
        }
        Set<AtomicConcept> atomicConcepts = new HashSet<AtomicConcept>();
        for (OWLClass c : classes) {
            atomicConcepts.add(AtomicConcept.create(c.getURI().toString()));
        }
        Set<Individual> hermitIndividuals = new HashSet<Individual>();
        for (OWLIndividual i : individuals) {
            Individual ind = Individual.create(i.getURI().toString());
            hermitIndividuals.add(ind);
            // all named individuals are tagged with a concept, so that keys are 
            // only applied to them
            if (determineExpressivity.m_hasKeys) {
                positiveFacts.add(Atom.create(AtomicConcept.INTERNAL_NAMED,
                    new org.semanticweb.HermiT.model.Term[] { ind }));
            }
        }
        Set<AtomicRole> objectRoles = new HashSet<AtomicRole>();
        for (OWLObjectProperty p : objectProperties) {
            objectRoles.add(AtomicRole.createObjectRole(p.getURI().toString()));
        }
        Set<AtomicRole> dataRoles = new HashSet<AtomicRole>();
        for (OWLDataProperty p : dataProperties) {
            dataRoles.add(AtomicRole.createDataRole(p.getURI().toString()));
        }
        return new DLOntology(ontologyURI,
                dlClauses, positiveFacts, negativeFacts,
                atomicConcepts, hermitIndividuals,
                buildRoleHierarchy(objectPropertyInclusions,
                                    dataPropertyInclusions,
                                    objectRoles, dataRoles),
                determineExpressivity.m_hasInverseRoles,
                determineExpressivity.m_hasAtMostRestrictions,
                determineExpressivity.m_hasNominals, shouldUseNIRule,
                determineExpressivity.m_hasReflexivity, 
                determineExpressivity.m_hasDatatypes);
    }
    
    public DLClause clausifyKey(OWLHasKeyDummy object) {
        List<Atom> headAtoms = new ArrayList<Atom>();
        List<Atom> bodyAtoms = new ArrayList<Atom>();

        // we have two named individuals (corresponding to X1 and X2) that 
        // might have to be equated
        org.semanticweb.HermiT.model.Variable X2;
        X2 = org.semanticweb.HermiT.model.Variable.create("X2");
        headAtoms.add(Atom.create(Equality.INSTANCE, X, X2));
        
        // keys only work on datatypes and named individuals
        bodyAtoms.add(Atom.create(AtomicConcept.INTERNAL_NAMED,
                new org.semanticweb.HermiT.model.Term[] { X }));
        bodyAtoms.add(Atom.create(AtomicConcept.INTERNAL_NAMED,
                new org.semanticweb.HermiT.model.Term[] { X2 }));
        
        // the concept expression of a hasKey statement is either a concept 
        // name or a negated concept name after normalization
        OWLDescription description = object.getClassExpression();
        if (description instanceof OWLClass) {
            OWLClass owlClass = (OWLClass) description;
            if (!owlClass.isOWLThing()) {
                bodyAtoms.add(Atom.create(
                        AtomicConcept.create(owlClass.getURI().toString()),
                        new org.semanticweb.HermiT.model.Term[] { X }));
                bodyAtoms.add(Atom.create(
                        AtomicConcept.create(owlClass.getURI().toString()),
                        new org.semanticweb.HermiT.model.Term[] { X2 }));
            }
        } else if (description instanceof OWLObjectComplementOf) {
            OWLDescription internal = ((OWLObjectComplementOf) description).getOperand();
            if (internal instanceof OWLClass) {
                OWLClass owlClass = (OWLClass) internal;
                bodyAtoms.add(Atom.create(
                        AtomicConcept.create(owlClass.getURI().toString()),
                        new org.semanticweb.HermiT.model.Term[] { X }));
                bodyAtoms.add(Atom.create(
                        AtomicConcept.create(owlClass.getURI().toString()),
                        new org.semanticweb.HermiT.model.Term[] { X2 }));
            } else {
                throw new IllegalStateException(
                        "Internal error: invalid normal form.");
            }
        } else {
            throw new IllegalStateException(
                    "Internal error: invalid normal form.");
        }
        
        int y_ind = 0;
        // object properties always go to the body
        for (OWLObjectPropertyExpression p : object.getObjectProperties()) {
            org.semanticweb.HermiT.model.Variable y;
            y = org.semanticweb.HermiT.model.Variable.create("Y" + y_ind);
            y_ind++;
            bodyAtoms.add(getRoleAtom(p, X, y));
            bodyAtoms.add(getRoleAtom(p, X2, y));
            // also the key criteria are named in case of object properties
            bodyAtoms.add(Atom.create(AtomicConcept.INTERNAL_NAMED,
                    new org.semanticweb.HermiT.model.Term[] { y }));
        }
        
        // data properties go to the body, but with different variables 
        // the head gets an atom asserting inequality between that data values
        for (OWLDataPropertyExpression d : object.getDataProperties()) {
            org.semanticweb.HermiT.model.Variable y;
            y = org.semanticweb.HermiT.model.Variable.create("Y" + y_ind);
            y_ind++;
            bodyAtoms.add(getDataPropertyAtom(d, X, y));
            org.semanticweb.HermiT.model.Variable y2;
            y2 = org.semanticweb.HermiT.model.Variable.create("Y" + y_ind);
            y_ind++;
            bodyAtoms.add(getDataPropertyAtom(d, X2, y2));
            headAtoms.add(Atom.create(Inequality.INSTANCE, y, y2));
        }
        
        Atom[] hAtoms = new Atom[headAtoms.size()];
        headAtoms.toArray(hAtoms);
        Atom[] bAtoms = new Atom[bodyAtoms.size()];
        bodyAtoms.toArray(bAtoms);
        DLClause clause = KeyClause.create(hAtoms, bAtoms);
        return clause;
    }
    
    /**
     * Creates an atom in the Hermit internal format such that the variables
     * automatically reflect whether the role was an inverse role or not.
     */
    protected static Atom getRoleAtom(
            OWLObjectPropertyExpression objectProperty,
            org.semanticweb.HermiT.model.Term first,
            org.semanticweb.HermiT.model.Term second) {
        objectProperty = objectProperty.getSimplified();
        if (objectProperty instanceof OWLObjectProperty) {
            AtomicRole role = AtomicRole.createObjectRole(((OWLObjectProperty) objectProperty).getURI().toString());
            return Atom.create(role, new org.semanticweb.HermiT.model.Term[] {
                    first, second });
        } else if (objectProperty instanceof OWLObjectPropertyInverse) {
            OWLObjectProperty internalObjectProperty = (OWLObjectProperty) ((OWLObjectPropertyInverse) objectProperty).getInverse();
            AtomicRole role = AtomicRole.createObjectRole(internalObjectProperty.getURI().toString());
            return Atom.create(role, new org.semanticweb.HermiT.model.Term[] {
                    second, first });
        } else
            throw new IllegalStateException(
                    "Internal error: unsupported type of object property!");
    }

    /**
     * Creates a concrete role (data property) atom in the Hermit internal 
     * format.
     * @param dataProperty the data property/concrete role
     * @param first its term
     * @param second its second term
     * @return an atom
     */
    protected static Atom getDataPropertyAtom(
            OWLDataPropertyExpression dataProperty,
            org.semanticweb.HermiT.model.Term first,
            org.semanticweb.HermiT.model.Term second) {
        if (dataProperty instanceof OWLDataProperty) {
            AtomicRole property = AtomicRole.createDataRole(((OWLDataProperty) dataProperty).getURI().toString());
            return Atom.create(property,
                    new org.semanticweb.HermiT.model.Term[] { first, second });
        } else
            throw new IllegalStateException(
                    "Internal error: unsupported type of data property!");
    }

    /**
     * Returns an atomic concept or a negated atomic concept in the Hermit
     * internal format, which are both LiteralConcept objects.
     */
    protected static LiteralConcept getLiteralConcept(OWLDescription description) {
        if (description instanceof OWLClass) {
            return AtomicConcept.create(((OWLClass) description).getURI().toString());
        } else if (description instanceof OWLObjectComplementOf) {
            OWLDescription internal = ((OWLObjectComplementOf) description).getOperand();
            if (!(internal instanceof OWLClass))
                throw new IllegalStateException(
                        "Internal error: invalid normal form.");
            return AtomicNegationConcept.create(AtomicConcept.create(((OWLClass) internal).getURI().toString()));
        } else
            throw new IllegalStateException(
                    "Internal error: invalid normal form.");
    }

    /**
     * Creates an abstract role (object property) in the Hermit internal format.
     * @param objectProperty the object property/abstract role
     * @return an Abstract Role
     */
    protected static Role getRole(
            OWLObjectPropertyExpression objectProperty) {
        objectProperty = objectProperty.getSimplified();
        if (objectProperty instanceof OWLObjectProperty)
            return AtomicRole.createObjectRole(((OWLObjectProperty) objectProperty).getURI().toString());
        else if (objectProperty instanceof OWLObjectPropertyInverse) {
            OWLObjectPropertyExpression internal = ((OWLObjectPropertyInverse) objectProperty).getInverse();
            if (!(internal instanceof OWLObjectProperty)) {
                throw new IllegalStateException(
                        "Internal error: invalid normal form.");
            }
            return InverseRole.create(AtomicRole.createObjectRole(((OWLObjectProperty) internal).getURI().toString()));
        } else
            throw new IllegalStateException(
                    "Internal error: invalid normal form.");
    }

    protected static Role getRole(OWLDataPropertyExpression dataProperty) {
        return AtomicRole.createDataRole(dataProperty.asOWLDataProperty().getURI().toString());
    }

    protected static org.semanticweb.HermiT.model.Individual getIndividual(
            OWLIndividual individual) {
        return org.semanticweb.HermiT.model.Individual.create(individual.getURI().toString());
    }

    protected static class Clausifier implements OWLDescriptionVisitor {
        protected final Map<AtomicConcept, AtomicConcept> m_negativeAtMostReplacements;
        private final int amqOffset; // the number of "negativeAtMostReplacements" which have already been clausified
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;
        protected final Set<AtMostAbstractRoleGuard> m_atMostRoleGuards;
        protected final Set<Atom> m_positiveFacts;
        protected final boolean m_renameAtMost;
        protected int m_yIndex;
        protected final OWLDataFactory m_factory;
        protected final boolean ignoreUnsupportedDatatypes;

        public Clausifier(Set<Atom> positiveFacts, boolean renameAtMost,
                OWLDataFactory factory, int amqOffset, boolean ignoreUnsupportedDatatypes) {
            m_negativeAtMostReplacements = new HashMap<AtomicConcept, AtomicConcept>();
            this.amqOffset = amqOffset;
            m_headAtoms = new ArrayList<Atom>();
            m_bodyAtoms = new ArrayList<Atom>();
            m_atMostRoleGuards = new HashSet<AtMostAbstractRoleGuard>();
            m_positiveFacts = positiveFacts;
            m_renameAtMost = renameAtMost;
            m_factory = factory;
            this.ignoreUnsupportedDatatypes = ignoreUnsupportedDatatypes;
        }

        public DLClause getDLClause() {
            Atom[] headAtoms = new Atom[m_headAtoms.size()];
            m_headAtoms.toArray(headAtoms);
            Arrays.sort(headAtoms, HeadComparator.INSTANCE);
            Atom[] bodyAtoms = new Atom[m_bodyAtoms.size()];
            m_bodyAtoms.toArray(bodyAtoms);
            DLClause dlClause = DLClause.create(headAtoms, bodyAtoms);
            m_headAtoms.clear();
            m_bodyAtoms.clear();
            m_yIndex = 0;
            return dlClause;
        }

        protected void ensureYNotZero() {
            if (m_yIndex == 0)
                m_yIndex++;
        }

        protected org.semanticweb.HermiT.model.Variable nextY() {
            org.semanticweb.HermiT.model.Variable result;
            if (m_yIndex == 0)
                result = Y;
            else
                result = org.semanticweb.HermiT.model.Variable.create("Y"
                        + m_yIndex);
            m_yIndex++;
            return result;
        }

        protected AtomicConcept getConceptForNominal(OWLIndividual individual) {
            AtomicConcept result = AtomicConcept.create("internal:nom$"
                    + individual.getURI().toString());
            m_positiveFacts.add(Atom.create(result, getIndividual(individual)));
            return result;
        }

        public void visit(OWLClass object) {
            m_headAtoms.add(Atom.create(
                    AtomicConcept.create(object.getURI().toString()),
                    new org.semanticweb.HermiT.model.Term[] { X }));
        }

        public void visit(OWLDataAllRestriction desc) {
            org.semanticweb.HermiT.model.Variable y = nextY();
            DataVisitor dataVisitor = new DataVisitor(ignoreUnsupportedDatatypes);
            desc.getFiller().accept(dataVisitor);
            DataRange d = new DatatypeRestrictionLiteral(DT.LITERAL);
            if (dataVisitor.getDataRange() != null){
                m_bodyAtoms.add(getDataPropertyAtom(desc.getProperty(), X, y));
                d = dataVisitor.getDataRange();
                if (!d.isBottom()) {
                    m_headAtoms.add(Atom.create(d,
                            new org.semanticweb.HermiT.model.Term[] { y }));
                }
            } else {
                m_bodyAtoms.add(getDataPropertyAtom(desc.getProperty(), X, y));
                m_headAtoms.add(Atom.create(d,
                        new org.semanticweb.HermiT.model.Term[] { y }));
            }
        }

        public void visit(OWLDataSomeRestriction desc) {
            OWLDataProperty dp = (OWLDataProperty) desc.getProperty();
            AtomicRole property = AtomicRole.createDataRole(dp.getURI().toString());
            DataVisitor dataVisitor = new DataVisitor(ignoreUnsupportedDatatypes);
            desc.getFiller().accept(dataVisitor);
            DataRange d = new DatatypeRestrictionLiteral(DT.LITERAL);
            if (dataVisitor.getDataRange() != null) {
                d = dataVisitor.getDataRange();
            }
            m_headAtoms.add(Atom
                    .create(AtLeastConcreteRoleConcept.create(1, property, d),
                            new org.semanticweb.HermiT.model.Term[] { X }));
        }

        public void visit(OWLDataExactCardinalityRestriction desc) {
            throw new IllegalStateException(
                    "Internal error: invalid normal form.");
        }

        public void visit(OWLDataMaxCardinalityRestriction desc) {
            int number = desc.getCardinality();
            OWLDataProperty dp = (OWLDataProperty) desc.getProperty();
            DataVisitor dataVisitor = new DataVisitor(ignoreUnsupportedDatatypes);
            dataVisitor.negate();
            desc.getFiller().accept(dataVisitor);
            // null means we parsed an unsupported datatype and the flag to 
            // ignore them is set, so do not add any constraints for the concept
            if (dataVisitor.getDataRange() != null) {
                ensureYNotZero();
                org.semanticweb.HermiT.model.Variable[] yVars = new org.semanticweb.HermiT.model.Variable[number + 1];
                for (int i = 0; i < yVars.length; i++) {
                    yVars[i] = nextY();
                    m_bodyAtoms.add(getDataPropertyAtom(dp, X, yVars[i]));
                    if (!dataVisitor.getDataRange().isBottom()) {
                        m_headAtoms.add(Atom.create(dataVisitor.getDataRange(),
                                new org.semanticweb.HermiT.model.Term[] { yVars[i] }));
                    }
                }
                for (int i = 0; i < yVars.length; i++) {
                    for (int j = i + 1; j < yVars.length; j++) {
                        m_headAtoms.add(Atom.create(Equality.INSTANCE,
                                new org.semanticweb.HermiT.model.Term[] { yVars[i],
                                        yVars[j] }));
                    }
                }
            }
        }

        public void visit(OWLDataMinCardinalityRestriction desc) {
            int number = desc.getCardinality();
            OWLDataProperty dp = (OWLDataProperty) desc.getProperty();
            AtomicRole property = AtomicRole.createDataRole(dp.getURI().toString());
            DataVisitor dataVisitor = new DataVisitor(ignoreUnsupportedDatatypes);
            desc.getFiller().accept(dataVisitor);
            DataRange d;
            if (dataVisitor.getDataRange() == null) {
                d = new DatatypeRestrictionLiteral(DT.LITERAL);
            } else {
                d = dataVisitor.getDataRange();
            }
            m_headAtoms.add(Atom.create(
                    AtLeastConcreteRoleConcept.create(
                            number, 
                            property, 
                            d),
                        new org.semanticweb.HermiT.model.Term[] { X }));
        }

        public void visit(OWLDataValueRestriction desc) {
            throw new RuntimeException("Invalid normal form.");
        }

        public void visit(OWLObjectAllRestriction object) {
            org.semanticweb.HermiT.model.Variable y = nextY();
            m_bodyAtoms.add(getRoleAtom(object.getProperty(), X, y));
            OWLDescription description = object.getFiller();
            if (description instanceof OWLClass) {
                OWLClass owlClass = (OWLClass) description;
                if (!owlClass.isOWLNothing()) {
                    m_headAtoms.add(Atom.create(
                            AtomicConcept.create(owlClass.getURI().toString()),
                            new org.semanticweb.HermiT.model.Term[] { y }));
                }
            } else if (description instanceof OWLObjectOneOf) {
                OWLObjectOneOf objectOneOf = (OWLObjectOneOf) description;
                for (OWLIndividual individual : objectOneOf.getIndividuals()) {
                    org.semanticweb.HermiT.model.Variable yInd = nextY();
                    m_bodyAtoms.add(Atom.create(
                            getConceptForNominal(individual), yInd));
                    m_headAtoms.add(Atom.create(Equality.INSTANCE, y, yInd));
                }
            } else if (description instanceof OWLObjectComplementOf) {
                OWLDescription internal = ((OWLObjectComplementOf) description).getOperand();
                if (internal instanceof OWLClass) {
                    OWLClass owlClass = (OWLClass) internal;
                    m_bodyAtoms.add(Atom.create(
                            AtomicConcept.create(owlClass.getURI().toString()),
                            new org.semanticweb.HermiT.model.Term[] { y }));
                } else if (internal instanceof OWLObjectOneOf
                        && ((OWLObjectOneOf) internal).getIndividuals().size() == 1) {
                    OWLObjectOneOf objectOneOf = (OWLObjectOneOf) internal;
                    OWLIndividual individual = objectOneOf.getIndividuals().iterator().next();
                    m_bodyAtoms.add(Atom.create(
                            getConceptForNominal(individual), y));
                } else
                    throw new IllegalStateException(
                            "Internal error: invalid normal form.");
            } else
                throw new IllegalStateException(
                        "Internal error: invalid normal form.");
        }

        public void visit(OWLObjectSomeRestriction object) {
            OWLObjectPropertyExpression objectProperty = object.getProperty();
            OWLDescription description = object.getFiller();
            if (description instanceof OWLObjectOneOf) {
                OWLObjectOneOf objectOneOf = (OWLObjectOneOf) description;
                for (OWLIndividual individual : objectOneOf.getIndividuals()) {
                    org.semanticweb.HermiT.model.Variable y = nextY();
                    m_bodyAtoms.add(Atom.create(
                            getConceptForNominal(individual), y));
                    m_headAtoms.add(getRoleAtom(objectProperty, X, y));
                }
            } else {
                LiteralConcept toConcept = getLiteralConcept(description);
                Role onRole = getRole(objectProperty);
                m_headAtoms.add(Atom.create(AtLeastAbstractRoleConcept.create(
                        1, onRole, toConcept),
                        new org.semanticweb.HermiT.model.Term[] { X }));
            }
        }

        public void visit(OWLObjectSelfRestriction object) {
            OWLObjectPropertyExpression objectProperty = object.getProperty();
            Atom roleAtom = getRoleAtom(objectProperty, X, X);
            m_headAtoms.add(roleAtom);
        }

        public void visit(OWLObjectMinCardinalityRestriction object) {
            LiteralConcept toConcept = getLiteralConcept(object.getFiller());
            Role onRole = getRole(object.getProperty());
            m_headAtoms.add(Atom.create(AtLeastAbstractRoleConcept.create(
                    object.getCardinality(), onRole, toConcept),
                    new org.semanticweb.HermiT.model.Term[] { X }));
        }

        public void visit(OWLObjectMaxCardinalityRestriction object) {
            if (m_renameAtMost) {
                AtomicConcept toAtomicConcept;
                if (object.getFiller() instanceof OWLClass)
                    toAtomicConcept = AtomicConcept.create(((OWLClass) object.getFiller()).getURI().toString());
                else if (object.getFiller() instanceof OWLObjectComplementOf
                        && ((OWLObjectComplementOf) object.getFiller()).getOperand() instanceof OWLClass) {
                    AtomicConcept originalAtomicConcept = AtomicConcept.create(((OWLClass) ((OWLObjectComplementOf) object.getFiller()).getOperand()).getURI().toString());
                    toAtomicConcept = m_negativeAtMostReplacements.get(originalAtomicConcept);
                    if (toAtomicConcept == null) {
                        toAtomicConcept = AtomicConcept.create("internal:amq#"
                                + m_negativeAtMostReplacements.size() + amqOffset);
                        m_negativeAtMostReplacements.put(originalAtomicConcept,
                                toAtomicConcept);
                    }
                } else
                    throw new IllegalStateException("invalid normal form.");
                Role onRole;
                if (object.getProperty() instanceof OWLObjectProperty) {
                    onRole = AtomicRole.createObjectRole(((OWLObjectProperty) object.getProperty()).getURI().toString());
                } else {
                    OWLObjectProperty internalObjectProperty = (OWLObjectProperty) ((OWLObjectPropertyInverse) object.getProperty()).getInverse();
                    onRole = InverseRole.create(AtomicRole.createObjectRole(internalObjectProperty.getURI().toString()));
                }
                AtMostAbstractRoleGuard atMostRole = AtMostAbstractRoleGuard.create(
                        object.getCardinality(), onRole,
                        toAtomicConcept);
                m_atMostRoleGuards.add(atMostRole);
                m_headAtoms.add(Atom.create(atMostRole,
                        new org.semanticweb.HermiT.model.Term[] { X }));
                // This is an optimization that is described in the SHOIQ paper
                // right after the clausification section.
                // In order to prevent the application of the rule to the entire
                // universe in some cases, R(x,y) \wedge C(y) to the body of the
                // rule
                org.semanticweb.HermiT.model.Variable Y = nextY();
                m_bodyAtoms.add(getRoleAtom(object.getProperty(), X, Y));
                if (!AtomicConcept.THING.equals(toAtomicConcept))
                    m_bodyAtoms.add(Atom.create(toAtomicConcept, Y));
            } else
                addAtMostAtoms(object.getCardinality(), object.getProperty(),
                        object.getFiller());
        }

        public void visit(OWLObjectExactCardinalityRestriction object) {
            throw new IllegalStateException(
                    "Internal error: invalid normal form.");
        }

        public void visit(OWLObjectOneOf object) {
            for (OWLIndividual individual : object.getIndividuals()) {
                org.semanticweb.HermiT.model.Variable Y = nextY();
                AtomicConcept conceptForNominal = getConceptForNominal(individual);
                m_headAtoms.add(Atom.create(Equality.INSTANCE, X, Y));
                m_bodyAtoms.add(Atom.create(conceptForNominal, Y));
            }
        }

        public void visit(OWLObjectValueRestriction object) {
            throw new IllegalStateException(
                    "Internal error: invalid normal form.");
        }

        public void visit(OWLObjectComplementOf object) {
            OWLDescription description = object.getOperand();
            if (!(description instanceof OWLClass)) {
                if (description instanceof OWLObjectSelfRestriction) {
                    OWLObjectPropertyExpression objectProperty = ((OWLObjectSelfRestriction) description).getProperty();
                    Atom roleAtom = getRoleAtom(objectProperty, X, X);
                    m_bodyAtoms.add(roleAtom);
                } else
                    throw new IllegalStateException(
                            "Internal error: invalid normal form.");
            }
            m_bodyAtoms.add(Atom.create(
                    AtomicConcept.create(((OWLClass) description).getURI().toString()),
                    new org.semanticweb.HermiT.model.Term[] { X }));
        }

        public void visit(OWLObjectUnionOf object) {
            throw new IllegalStateException(
                    "Internal error: invalid normal form.");
        }

        public void visit(OWLObjectIntersectionOf object) {
            throw new IllegalStateException(
                    "Internal error: invalid normal form.");
        }

        /**
         * @return the number of new "negativeAtMostReplacements" introduced
         */
        public int clausifyAtMostStuff(Collection<DLClause> dlClauses) {
            for (AtMostAbstractRoleGuard atMostRole : m_atMostRoleGuards) {
                m_bodyAtoms.add(Atom.create(atMostRole,
                        new org.semanticweb.HermiT.model.Term[] { X }));
                Role onRole = atMostRole.getOnRole();
                OWLObjectPropertyExpression onObjectProperty;
                if (onRole instanceof AtomicRole) {
                    onObjectProperty = m_factory.getOWLObjectProperty(URI.create(((AtomicRole) onRole).getURI().toString()));
                } else {
                    AtomicRole innerRole = ((InverseRole) onRole).getInverseOf();
                    onObjectProperty = m_factory.getOWLObjectPropertyInverse(m_factory.getOWLObjectProperty(URI.create(innerRole.getURI().toString())));
                }
                addAtMostAtoms(
                        atMostRole.getCaridnality(),
                        onObjectProperty,
                        m_factory.getOWLClass(URI.create(atMostRole.getToAtomicConcept().getURI().toString())));
                DLClause dlClause = getDLClause();
                dlClauses.add(dlClause);
            }
            for (Map.Entry<AtomicConcept, AtomicConcept> entry : m_negativeAtMostReplacements.entrySet()) {
                m_headAtoms.add(Atom.create(entry.getKey(), X));
                m_headAtoms.add(Atom.create(entry.getValue(), X));
                DLClause dlClause = getDLClause();
                dlClauses.add(dlClause);
            }
            return m_negativeAtMostReplacements.size();
        }

        protected void addAtMostAtoms(int number,
                OWLObjectPropertyExpression onObjectProperty,
                OWLDescription toDescription) {
            ensureYNotZero();
            boolean isPositive;
            AtomicConcept atomicConcept;
            if (toDescription instanceof OWLClass) {
                isPositive = true;
                atomicConcept = AtomicConcept.create(((OWLClass) toDescription).getURI().toString());
                if (AtomicConcept.THING.equals(atomicConcept))
                    atomicConcept = null;
            } else if (toDescription instanceof OWLObjectComplementOf) {
                OWLDescription internal = ((OWLObjectComplementOf) toDescription).getOperand();
                if (!(internal instanceof OWLClass))
                    throw new IllegalStateException(
                            "Invalid ontology normal form.");
                isPositive = false;
                atomicConcept = AtomicConcept.create(((OWLClass) internal).getURI().toString());
                if (AtomicConcept.NOTHING.equals(atomicConcept))
                    atomicConcept = null;
            } else
                throw new IllegalStateException("Invalid ontology normal form.");
            org.semanticweb.HermiT.model.Variable[] yVars = new org.semanticweb.HermiT.model.Variable[number + 1];
            for (int i = 0; i < yVars.length; i++) {
                yVars[i] = nextY();
                m_bodyAtoms.add(getRoleAtom(onObjectProperty, X,
                        yVars[i]));
                if (atomicConcept != null) {
                    Atom atom = Atom.create(
                            atomicConcept,
                            new org.semanticweb.HermiT.model.Term[] { yVars[i] });
                    if (isPositive)
                        m_bodyAtoms.add(atom);
                    else
                        m_headAtoms.add(atom);
                }
            }
            if (yVars.length > 2) // For functionality this is subsumed by the
                // way in which the rules are applied
                for (int i = 0; i < yVars.length - 1; i++)
                    m_bodyAtoms.add(Atom.create(NodeIDLessThan.INSTANCE,
                            new org.semanticweb.HermiT.model.Term[] { yVars[i],
                                    yVars[i + 1] }));
            for (int i = 0; i < yVars.length; i++)
                for (int j = i + 1; j < yVars.length; j++)
                    m_headAtoms.add(Atom.create(Equality.INSTANCE,
                            new org.semanticweb.HermiT.model.Term[] { yVars[i],
                                    yVars[j] }));
        }
    }

    // protected static class DataTypeVisitor extends OWLDa
    protected static class DataVisitor implements OWLDataVisitor {
        protected static OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        protected static OWLDataFactory factory = man.getOWLDataFactory();
        protected boolean isNegated = false;
        protected DataRange currentDataRange;
        protected DataConstant currentConstant;
        protected boolean ignoreUnsupportedDatatypes;
        
        public DataVisitor(boolean ignoreUnsupportedDatatypes) {
            this.ignoreUnsupportedDatatypes = ignoreUnsupportedDatatypes;
        }
        
        public void visit(OWLDataComplementOf dataComplementOf) {
            OWLDataRange range = dataComplementOf.getDataRange();
            isNegated = !isNegated;
            range.accept(this);
        }

        public void visit(OWLDataOneOf dataOneOf) {
            currentDataRange = new EnumeratedDataRange();
            if (isNegated) currentDataRange.negate();
            for (OWLConstant constant : dataOneOf.getValues()) {
                constant.accept(this);
                if (currentConstant != null) {
                    currentDataRange.addOneOf(currentConstant);
                }
            }
        }
        
        public void visit(OWLTypedConstant typedConstant) {
            OWLDataType dataType = typedConstant.getDataType();
            String lit = typedConstant.getLiteral();
            if (dataType.equals(factory.getOWLDataType(DT.OWLREAL.getURI())) 
                    || dataType.equals(factory.getOWLDataType(DT.OWLREALPLUS.getURI()))) {
                throw new RuntimeException("Parsed the constant " 
                        + typedConstant + " of type owl:real or owl:realPlus, " 
                        + "but the datatypes owl:real and owl:realPlus do not " 
                        + "have any literals. ");
            } else if (dataType.equals(factory.getOWLDataType(DT.RATIONAL.getURI()))) { 
                try {
                    // rational will automatically be reduced
                    BigRational literalAsBR = BigRational.parseRational(lit);
                    // see if it is an integer
                    if (literalAsBR.getDenominator().equals(BigInteger.ONE)) {
                        currentConstant = new DataConstant(
                                Impl.IInteger, DT.RATIONAL, lit);
                    } else {
                        // apparently it is not an integer
                        // see if it is finitely representable as a decimal
                        try {
                            BigDecimal literalAsBD = literalAsBR.bigDecimalValueExact();
                            // ok, it is at least representable as a decimal
                            // let's see if it has also an exact double representation
                            if (literalAsBD.compareTo(new BigDecimal(Double.MAX_VALUE)) <= 0
                                    && literalAsBD.compareTo(new BigDecimal(-Double.MAX_VALUE)) >= 0) {
                                Double literalAsD = new Double(literalAsBD.toString());
                                if (literalAsBD.compareTo(new BigDecimal(literalAsD)) == 0) {
                                    // it is representable as a double without rounding
                                    // see if it is even a float
                                    if (literalAsBD.compareTo(new BigDecimal(Float.MAX_VALUE)) <= 0
                                            && literalAsBD.compareTo(new BigDecimal(-Float.MAX_VALUE)) >= 0) {
                                        Float literalAsF = new Float(literalAsBD.toString());
                                        if (literalAsBD.compareTo(new BigDecimal(literalAsF)) == 0) {
                                            // it is even a float
                                            currentConstant = new DataConstant(
                                                    Impl.IFloat, DT.RATIONAL, literalAsF.toString());
                                        } else {
                                            currentConstant = new DataConstant(
                                                    Impl.IDouble, DT.RATIONAL, literalAsD.toString());
                                        }
                                    } else {
                                        // it is a double
                                        currentConstant = new DataConstant(
                                                Impl.IDouble, DT.RATIONAL, literalAsD.toString());
                                    }
                                } else {
                                    currentConstant = new DataConstant(
                                            Impl.IDecimal, DT.RATIONAL, 
                                            literalAsBD.toString());
                                }
                            }
                        } catch (ArithmeticException e) {
                            // keep it as a rational in its reduced form
                            currentConstant = new DataConstant(
                                    Impl.IRational, DT.RATIONAL, literalAsBR.toString());
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant + " of type owl:rational " 
                            + "cannot be parsed into a rational. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.DECIMAL.getURI()))) { 
                try {
                    BigDecimal literalAsBD = new BigDecimal(lit);
                    // see if it is an integer
                    try {
                        lit = literalAsBD.toBigIntegerExact().toString();
                        currentConstant = new DataConstant(
                                Impl.IInteger, DT.DECIMAL, lit);
                    } catch (ArithmeticException e) {
                        // apperently it is not an integer
                        // see if it fits into the range of doubles
                        if (literalAsBD.compareTo(new BigDecimal(Double.MAX_VALUE)) <= 0
                                && literalAsBD.compareTo(new BigDecimal(-Double.MAX_VALUE)) >= 0) {
                            Double literalAsD = new Double(lit);
                            if (literalAsBD.compareTo(new BigDecimal(literalAsD)) == 0) {
                                // it is representable as a double without rounding
                                // see if it is even a float
                                if (literalAsBD.compareTo(new BigDecimal(Float.MAX_VALUE)) <= 0
                                        && literalAsBD.compareTo(new BigDecimal(-Float.MAX_VALUE)) >= 0) {
                                    Float literalAsF = new Float(lit);
                                    if (literalAsBD.compareTo(new BigDecimal(literalAsF)) == 0) {
                                        // it is even a float
                                        currentConstant = new DataConstant(
                                                Impl.IFloat, DT.DECIMAL, literalAsF.toString());
                                    } else {
                                        currentConstant = new DataConstant(
                                                Impl.IDouble, DT.DECIMAL, literalAsD.toString());
                                    }
                                } else {
                                    // it is a double
                                    currentConstant = new DataConstant(
                                            Impl.IDouble, DT.DECIMAL, literalAsD.toString());
                                }
                            } else {
                                currentConstant = new DataConstant(
                                        Impl.IDecimal, DT.DECIMAL, 
                                        literalAsBD.toString());
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant + " is not numeric. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.DOUBLE.getURI()))) { 
                if (lit.trim().equalsIgnoreCase("INF") 
                        || lit.trim().equalsIgnoreCase("+INF")) {
                    lit = "Infinity";
                }
                if (lit.trim().equalsIgnoreCase("-INF")) {
                    lit = "-Infinity";
                }
                if (lit.equalsIgnoreCase("NaN") 
                        || lit.equalsIgnoreCase("Infinity")
                        || lit.equalsIgnoreCase("-Infinity")) {
                    currentConstant = 
                            new DataConstant(Impl.IDouble, DT.DOUBLE, "" + new Double(lit));
                } else {
                    try {
                        BigDecimal literalAsBD = new BigDecimal(lit);
                        // see if it is in the allowed range
                        if (literalAsBD.compareTo(new BigDecimal(Double.MAX_VALUE)) <= 0
                                && literalAsBD.compareTo(new BigDecimal(-Double.MAX_VALUE)) >= 0) {
                            Double literalAsD = new Double(lit);
                            // see whether we can use the integer implementation
                            try {
                                if (literalAsD.equals(-0.0d)) {
                                    // -0.0 for floats is not the same as the 
                                    // integer value 0, so handle as double in 
                                    // the catch block
                                    throw new ArithmeticException();
                                }
                                lit = literalAsBD.toBigIntegerExact().toString();
                                currentConstant = new DataConstant(
                                        Impl.IInteger, DT.DOUBLE, lit);
                            } catch (ArithmeticException e) {
                                // see if we can use the float implementation
                                if (literalAsD >= (double)-Float.MAX_VALUE 
                                        && literalAsD <= (double)Float.MAX_VALUE) {
                                    Float literalAsF = new Float(lit);
                                    // see if no rounding took place
                                    if ((double)literalAsF == literalAsD) {
                                        currentConstant = new DataConstant(
                                                Impl.IFloat, DT.DOUBLE, literalAsF.toString());
                                    } else {
                                        currentConstant = new DataConstant(
                                                Impl.IDouble, DT.DOUBLE, literalAsD.toString());
                                    }
                                } else {
                                    currentConstant = new DataConstant(
                                            Impl.IDouble, DT.DOUBLE, literalAsD.toString());
                                }
                            }
                        } else {
                            throw new RuntimeException("Parsed constant " 
                                    + typedConstant 
                                    + " is out of the range of double. ");
                        }
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Parsed constant " 
                                + typedConstant 
                                + " is not numeric. ");
                    } 
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.FLOAT.getURI()))) { 
                if (lit.trim().equalsIgnoreCase("INF") 
                        || lit.trim().equalsIgnoreCase("+INF")) {
                    lit = "Infinity";
                }
                if (lit.trim().equalsIgnoreCase("-INF")) {
                    lit = "-Infinity";
                }
                if (lit.equalsIgnoreCase("NaN") 
                        || lit.equalsIgnoreCase("Infinity")
                        || lit.equalsIgnoreCase("-Infinity")) {
                    currentConstant = new DataConstant(
                            Impl.IFloat, DT.FLOAT, "" + new Float(lit));
                } else {
                    try {
                        BigDecimal literalAsBD = new BigDecimal(lit);
                        Float literalAsF = new Float(lit);
                        if (literalAsBD.compareTo(new BigDecimal(Float.MAX_VALUE)) <= 0
                            && literalAsBD.compareTo(new BigDecimal(-Float.MAX_VALUE)) >= 0) {
                            try {
                                // see if we can use the integer implementation
                                if (literalAsF.equals(-0.0f)) {
                                    // -0.0 for floats is not the same as the 
                                    // integer value 0, so handle as float in 
                                    // the catch block
                                    throw new ArithmeticException();
                                }
                                lit = literalAsBD.toBigIntegerExact().toString();
                                currentConstant = new DataConstant(
                                        Impl.IInteger, DT.FLOAT, lit);
                            } catch (ArithmeticException e) {
                                currentConstant = new DataConstant(
                                        Impl.IFloat, DT.FLOAT, literalAsF.toString());
                            }
                        } else {
                            throw new RuntimeException("Parsed constant " 
                                    + typedConstant 
                                    + " is out of the range for floats. ");
                        }
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Parsed constant " 
                                + typedConstant + " is not numeric. ");
                    }
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.INTEGER.getURI()))) {
                try {
                    BigInteger integer = new BigInteger(lit);
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.INTEGER, integer.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not an integer as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.NONNEGATIVEINTEGER.getURI()))) {
                try {
                    BigInteger nonNegative = new BigInteger(lit);
                    if (nonNegative.signum() < 0) {
                        throw new NumberFormatException();
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.NONNEGATIVEINTEGER, nonNegative.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not a non-negative integer as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.NONPOSITIVEINTEGER.getURI()))) {
                try {
                    BigInteger nonPositive = new BigInteger(lit);
                    if (nonPositive.signum() > 0) {
                        throw new NumberFormatException(); // go to catch block
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.NONPOSITIVEINTEGER, nonPositive.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not a non-positive integer as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.POSITIVEINTEGER.getURI()))) {
                try {
                    BigInteger positive = new BigInteger(lit);
                    if (positive.signum() < 1) {
                        throw new NumberFormatException();
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.POSITIVEINTEGER, positive.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not a positive integer as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.NEGATIVEINTEGER.getURI()))) {
                try {
                    BigInteger negative = new BigInteger(lit);
                    if (negative.signum() > -1) {
                        throw new NumberFormatException();
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.NEGATIVEINTEGER, negative.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not a negative integer as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.LONG.getURI()))) {
                try {
                    BigInteger longType = new BigInteger(lit);
                    if (longType.compareTo(new BigInteger("" + Long.MAX_VALUE)) > 0 
                            || longType.compareTo(new BigInteger("" + Long.MIN_VALUE)) < 0) {
                        throw new NumberFormatException();
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.LONG, longType.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not a long as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.INT.getURI()))) {
                try {
                    BigInteger intType = new BigInteger(lit);
                    if (intType.compareTo(new BigInteger("" + Integer.MAX_VALUE)) > 0 
                            || intType.compareTo(new BigInteger("" + Integer.MIN_VALUE)) < 0) {
                        throw new NumberFormatException();
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.INT, intType.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not an int as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.SHORT.getURI()))) {
                try {
                    BigInteger shortType = new BigInteger(lit);
                    if (shortType.compareTo(new BigInteger("" + Short.MAX_VALUE)) > 0 
                            || shortType.compareTo(new BigInteger("" + Short.MIN_VALUE)) < 0) {
                        throw new NumberFormatException();
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.SHORT, shortType.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not a short as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.BYTE.getURI()))) {
                try {
                    BigInteger byteType = new BigInteger(lit);
                    if (byteType.compareTo(new BigInteger("" + Byte.MAX_VALUE)) > 0 
                            || byteType.compareTo(new BigInteger("" + Byte.MIN_VALUE)) < 0) {
                        throw new NumberFormatException();
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.BYTE, byteType.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not a byte as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.UNSIGNEDLONG.getURI()))) {
                try {
                    BigInteger uLongType = new BigInteger(lit);
                    if (uLongType.compareTo(new BigInteger("18446744073709551615")) > 0 
                            || uLongType.compareTo(BigInteger.ZERO) < 0) {
                        throw new NumberFormatException();
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.UNSIGNEDLONG, uLongType.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not an unsigned long as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.UNSIGNEDINT.getURI()))) {
                try {
                    BigInteger uIntType = new BigInteger(lit);
                    if (uIntType.compareTo(new BigInteger("4294967295")) > 0 
                            || uIntType.compareTo(BigInteger.ZERO) < 0) {
                        throw new NumberFormatException();
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.UNSIGNEDINT, uIntType.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not an unsigned int as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.UNSIGNEDSHORT.getURI()))) {
                try {
                    BigInteger uShortType = new BigInteger(lit);
                    if (uShortType.compareTo(new BigInteger("65535")) > 0 
                            || uShortType.compareTo(BigInteger.ZERO) < 0) {
                        throw new NumberFormatException();
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.UNSIGNEDSHORT, uShortType.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not an unsigned short as required. ");
                } 
            } else if (dataType.equals(factory.getOWLDataType(DT.UNSIGNEDBYTE.getURI()))) {
                try {
                    BigInteger uByteType = new BigInteger(lit);
                    if (uByteType.compareTo(new BigInteger("255")) > 0 
                            || uByteType.compareTo(BigInteger.ZERO) < 0) {
                        throw new NumberFormatException();
                    }
                    currentConstant = new DataConstant(
                            Impl.IInteger, DT.UNSIGNEDBYTE, uByteType.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parsed constant " 
                            + typedConstant 
                            + " is not an unsigned byte as required. ");
                } 
            }else if (dataType.equals(factory.getOWLDataType(DT.RDFTEXT.getURI()))) {
                int posAt = lit.lastIndexOf("@");
                if (posAt < 0) {
                    throw new RuntimeException("No @ character found in " 
                            + typedConstant 
                            + " that indicates the start of the required language tag. ");
                }
                String lang = lit.substring(posAt+1);
                String text = lit.substring(0, posAt);
                currentConstant = new DataConstant(
                        Impl.IString, DT.RDFTEXT, text, lang);
            } else if (dataType.equals(factory.getOWLDataType(DT.STRING.getURI()))) {
                currentConstant = new DataConstant(
                        Impl.IString, DT.STRING, lit);
            }  else if (dataType.equals(factory.getOWLDataType(DT.NORMALIZEDSTRING.getURI()))) {
                // no carriage return \r, tab \t, or line feed \n
                if (lit.indexOf("\r") == -1 
                        && lit.indexOf("\t") == -1 
                        && lit.indexOf("\n") == -1) {
                    currentConstant = new DataConstant(
                            Impl.IString, DT.NORMALIZEDSTRING, lit);
                } else {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is not a normalized string. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.TOKEN.getURI()))) {
                // no carriage return \r, line feed \n, tab \t, no leading or  
                // trailing spaces, no internal sequences of two or more space
                if (lit.indexOf("\r") == -1 
                        && lit.indexOf("\t") == -1 
                        && lit.indexOf("\n") == -1
                        && lit.indexOf("  ") == -1
                        && !lit.startsWith(" ")
                        && !lit.endsWith(" ")) {
                    currentConstant = new DataConstant(
                            Impl.IString, DT.TOKEN, lit);
                } else {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is not a token. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.LANGUAGE.getURI()))) {
                if (Datatypes.get("language").run(lit)) {
                    currentConstant = new DataConstant(
                            Impl.IString, DT.LANGUAGE, lit);
                } else {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is not an instance of xsd:language. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.NMTOKEN.getURI()))) {
                if (Datatypes.get("Nmtoken2").run(lit)) {
                    currentConstant = new DataConstant(
                            Impl.IString, DT.NMTOKEN, lit);
                } else {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is not an instance of xsd:NMTOKEN. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.NAME.getURI()))) {
                if (Datatypes.get("Name2").run(lit)) {
                    currentConstant = new DataConstant(
                            Impl.IString, DT.NAME, lit);
                } else {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is not an instance of xsd:NAME. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.NCNAME.getURI()))) {
                if (Datatypes.get("NCName").run(lit)) {
                    currentConstant = new DataConstant(
                            Impl.IString, DT.NCNAME, lit);
                } else {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is not an instance of xsd:NCName. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.IDREF.getURI()))) {
                if (Datatypes.get("NCName").run(lit)) {
                    currentConstant = new DataConstant(
                            Impl.IString, DT.IDREF, lit);
                } else {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is not an instance of xsd:NCName. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.ENTITY.getURI()))) {
                if (Datatypes.get("NCName").run(lit)) {
                    currentConstant = new DataConstant(
                            Impl.IString, DT.ENTITY, lit);
                } else {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is not an instance of xsd:NCName. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.LITERAL.getURI()))) {
                currentConstant = new DataConstant(
                        Impl.ILiteral, DT.LITERAL, lit);
            } else if (dataType.equals(factory.getOWLDataType(DT.BOOLEAN.getURI()))) {
                if (!(lit.equalsIgnoreCase("true") 
                        || lit.equalsIgnoreCase("false") 
                        || lit.equalsIgnoreCase("1")
                        || lit.equalsIgnoreCase("0"))) {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is neither true nor false, but supposed to be " 
                            + " boolean. ");
                } else {
                    if (lit.equalsIgnoreCase("1")) {
                        lit = "true";
                    }
                    if (lit.equalsIgnoreCase("0")) {
                        lit = "false";
                    }
                    currentConstant = new DataConstant(
                            Impl.IBoolean, DT.BOOLEAN, lit);
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.OWLDATETIME.getURI()))) {
                try {
                    DatatypeRestrictionDateTime.dfm.parse(lit);
                    currentConstant = new DataConstant(
                            Impl.IDateTime, DT.OWLDATETIME, lit);
                } catch (ParseException e) {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is supposed to be a dateTime datatype, but " 
                            + "has an invalid format that cannot be parsed. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.DATETIME.getURI()))) {
                try {
                    DatatypeRestrictionDateTime.dfm.parse(lit);
                    currentConstant = new DataConstant(
                            Impl.IDateTime, DT.DATETIME, lit);
                } catch (ParseException e) {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is supposed to be a dateTime datatype, but " 
                            + "has an invalid format that cannot be parsed. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.ANYURI.getURI()))) {
                if (Datatypes.get("URI").run(lit)) {
                    currentConstant = new DataConstant(
                            Impl.IAnyURI, DT.ANYURI, lit);
                } else {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is supposed to be a URI datatype, but has a " 
                            + "format that cannot be parsed. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.BASE64BINARY.getURI()))) {
                // values are limited to the characters a-z, A-Z, 0-9, +, /, 
                // and whitespace (space), \r, \n, \t plus padding =
                // remove whitespace
                lit = lit.replaceAll(" ", "");
                lit = lit.replaceAll("\r", "");
                lit = lit.replaceAll("\n", "");
                lit = lit.replaceAll("\t", "");
                if (Datatypes.get("base64Binary").run(lit)) {
                    currentConstant = new DataConstant(
                            Impl.IBase64Binary, DT.BASE64BINARY, lit);
                } else {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is supposed to be base64binary, but has a" 
                            + " format that cannot be parsed. ");
                }
            } else if (dataType.equals(factory.getOWLDataType(DT.HEXBINARY.getURI()))) {
                // values are limited to the characters a-z, A-Z, 0-9, +, /, 
                // =, (space), \r, \n, \t, 
                if (Datatypes.get("hexBinary").run(lit)) {
                    currentConstant = new DataConstant(
                            Impl.IHexBinary, DT.HEXBINARY, lit);
                } else {
                    throw new RuntimeException("The constant " 
                            + typedConstant 
                            + " is supposed to be hexBinary, but has a" 
                            + " format that cannot be parsed. ");
                }
            } else {
                String message = "The datatype " 
                    + System.getProperty("line.separator") 
                    + "    " + typedConstant.getDataType().getURI() 
                    + System.getProperty("line.separator")
                    + "of the typed constant with literal value "
                    + System.getProperty("line.separator")
                    + "    " + typedConstant.getLiteral()
                    + System.getProperty("line.separator")
                    + "is not supported in HermiT. HermiT should "
                    + "properly support all OWL2 datatypes. If you think "
                    + "this one should be supported, please "
                    + "check that it has the correct prefix or URI "
                    + "and case (xsd:nonNegativeInteger and not "
                    + "xsd:NonnegativeInteger) and if it does please "
                    + "report this error. ";
                if (ignoreUnsupportedDatatypes) {
                    System.err.println(message);
                    System.err.println("The datatype will be ignored. "
                            + System.getProperty("line.separator"));
                    return;
                } else {
                    throw new RuntimeException(message);
                }
//                currentConstant = new DataConstant(
//                        Impl.IUnkown, DT.UNKNOWN, lit);
            }
        }
        
        public void visit(OWLDataType dataType) {
            if (dataType.equals(factory.getOWLDataType(DT.OWLREALPLUS.getURI()))) {
                currentDataRange = new DatatypeRestrictionOWLRealPlus(DT.OWLREALPLUS, true); 
            } else if (dataType.equals(factory.getOWLDataType(DT.OWLREAL.getURI()))) {
                currentDataRange = new DatatypeRestrictionOWLRealPlus(DT.OWLREAL, false); 
            } else if (dataType.equals(factory.getOWLDataType(DT.DECIMAL.getURI()))) {
                currentDataRange = new DatatypeRestrictionOWLRealPlus(DT.DECIMAL, false); 
            } else if (dataType.equals(factory.getOWLDataType(DT.RATIONAL.getURI()))) {
                currentDataRange = new DatatypeRestrictionRational(DT.RATIONAL); 
            } else if (dataType.equals(factory.getOWLDataType(DT.DOUBLE.getURI()))) {
                currentDataRange = new DatatypeRestrictionDouble(DT.DOUBLE); 
            } else if (dataType.equals(factory.getOWLDataType(DT.FLOAT.getURI()))) {
                currentDataRange = new DatatypeRestrictionFloat(DT.FLOAT); 
            } else if (dataType.equals(factory.getOWLDataType(DT.INTEGER.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.INTEGER); 
            } else if (dataType.equals(factory.getOWLDataType(DT.NONNEGATIVEINTEGER.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.NONNEGATIVEINTEGER);
                currentDataRange.addFacet(Facets.MIN_INCLUSIVE, "0");
            } else if (dataType.equals(factory.getOWLDataType(DT.NONPOSITIVEINTEGER.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.NONPOSITIVEINTEGER);
                currentDataRange.addFacet(Facets.MAX_INCLUSIVE, "0");
            } else if (dataType.equals(factory.getOWLDataType(DT.POSITIVEINTEGER.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.POSITIVEINTEGER);
                currentDataRange.addFacet(Facets.MIN_INCLUSIVE, "1");
            } else if (dataType.equals(factory.getOWLDataType(DT.NEGATIVEINTEGER.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.NEGATIVEINTEGER);
                currentDataRange.addFacet(Facets.MAX_INCLUSIVE, "-1");
            } else if (dataType.equals(factory.getOWLDataType(DT.LONG.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.LONG);
                currentDataRange.addFacet(Facets.MAX_INCLUSIVE, "" + Long.MAX_VALUE);
                currentDataRange.addFacet(Facets.MIN_INCLUSIVE, "" + Long.MIN_VALUE); 
            } else if (dataType.equals(factory.getOWLDataType(DT.INT.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.INT);
                currentDataRange.addFacet(Facets.MAX_INCLUSIVE, "" + Integer.MAX_VALUE);
                currentDataRange.addFacet(Facets.MIN_INCLUSIVE, "" + Integer.MIN_VALUE);
            } else if (dataType.equals(factory.getOWLDataType(DT.SHORT.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.SHORT);
                currentDataRange.addFacet(Facets.MAX_INCLUSIVE, "" + Short.MAX_VALUE);
                currentDataRange.addFacet(Facets.MIN_INCLUSIVE, "" + Short.MIN_VALUE);
            } else if (dataType.equals(factory.getOWLDataType(DT.BYTE.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.BYTE);
                currentDataRange.addFacet(Facets.MAX_INCLUSIVE, "" + Byte.MAX_VALUE);
                currentDataRange.addFacet(Facets.MIN_INCLUSIVE, "" + Byte.MIN_VALUE);
            } else if (dataType.equals(factory.getOWLDataType(DT.UNSIGNEDLONG.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.UNSIGNEDLONG);
                currentDataRange.addFacet(Facets.MAX_INCLUSIVE, "" 
                        + (new BigInteger("" + Long.MAX_VALUE)).multiply(new BigInteger("2").add(BigInteger.ONE)));
                currentDataRange.addFacet(Facets.MIN_INCLUSIVE, "0");
            } else if (dataType.equals(factory.getOWLDataType(DT.UNSIGNEDINT.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.UNSIGNEDINT);
                currentDataRange.addFacet(Facets.MAX_INCLUSIVE, "" 
                        + (new BigInteger("" + Integer.MAX_VALUE)).multiply(new BigInteger("2").add(BigInteger.ONE)));
                currentDataRange.addFacet(Facets.MIN_INCLUSIVE, "0");
            } else if (dataType.equals(factory.getOWLDataType(DT.UNSIGNEDSHORT.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.UNSIGNEDSHORT);
                currentDataRange.addFacet(Facets.MAX_INCLUSIVE, "" 
                        + (new BigInteger("" + Short.MAX_VALUE)).multiply(new BigInteger("2").add(BigInteger.ONE)));
                currentDataRange.addFacet(Facets.MIN_INCLUSIVE, "0");
            } else if (dataType.equals(factory.getOWLDataType(DT.UNSIGNEDBYTE.getURI()))) {
                currentDataRange = new DatatypeRestrictionInteger(DT.UNSIGNEDBYTE);
                currentDataRange.addFacet(Facets.MAX_INCLUSIVE, "" 
                        + (new BigInteger("" + Byte.MAX_VALUE)).multiply(new BigInteger("2").add(BigInteger.ONE)));
                currentDataRange.addFacet(Facets.MIN_INCLUSIVE, "0");
            } else if (dataType.equals(factory.getOWLDataType(DT.STRING.getURI()))) {
                currentDataRange = new DatatypeRestrictionString(DT.STRING);
            } else if (dataType.equals(factory.getOWLDataType(DT.LITERAL.getURI()))) {
                currentDataRange = new DatatypeRestrictionLiteral(DT.LITERAL);
            } else if (dataType.equals(factory.getOWLDataType(DT.BOOLEAN.getURI()))) {
                currentDataRange = new DatatypeRestrictionBoolean(DT.BOOLEAN);
            } else if (dataType.equals(factory.getOWLDataType(DT.OWLDATETIME.getURI())) 
                    || dataType.equals(factory.getOWLDataType(DT.DATETIME.getURI()))) {
                currentDataRange = new DatatypeRestrictionDateTime(DT.OWLDATETIME);
            } else if (dataType.equals(factory.getOWLDataType(DT.ANYURI.getURI()))) {
                currentDataRange = new DatatypeRestrictionBoolean(DT.ANYURI);
            } else {
                String message = "The datatype "
                        + System.getProperty("line.separator") + "    "
                        + dataType.getURI().toString() 
                        + System.getProperty("line.separator")
                        + "is not supported in HermiT. HermiT should "
                        + "properly support all OWL2 datatypes. If you think "
                        + "this one should be supported, please "
                        + "check that it has the correct prefix or URI "
                        + "and case (xsd:nonNegativeInteger and not "
                        + "xsd:NonnegativeInteger) and if it does please "
                        + "report this error. ";
                if (ignoreUnsupportedDatatypes) {
                    System.err.println(message);
                    System.err.println("The datatype will be ignored. "
                            + System.getProperty("line.separator"));
                    return;
                } else {
                    throw new RuntimeException(message);
                }
                //currentDataRange = new DatatypeRestrictionUnknown(DT.UNKNOWN);
            }
            if (isNegated) currentDataRange.negate();
        }

        public void visit(OWLDataRangeRestriction rangeRestriction) {
            OWLDataRange range = rangeRestriction.getDataRange();
            range.accept(this);
            if (currentDataRange == null) return;
            for (OWLDataRangeFacetRestriction facetRestriction : rangeRestriction.getFacetRestrictions()) {
                OWLRestrictedDataRangeFacetVocabulary facetOWL = facetRestriction.getFacet();
                facetRestriction.getFacetValue().accept(this);
                String value = currentConstant.getValue();
                switch (facetOWL) {
                case LENGTH: {
                    currentDataRange.addFacet(Facets.LENGTH, value);
                } break;
                case MAX_LENGTH: {
                     currentDataRange.addFacet(Facets.MAX_LENGTH, value);
                } break;
                case MIN_LENGTH: {
                     currentDataRange.addFacet(Facets.MIN_LENGTH, value);
                } break;
                case MIN_INCLUSIVE: {
                    currentDataRange.addFacet(Facets.MIN_INCLUSIVE, value);
                } break;
                case MIN_EXCLUSIVE: {
                    currentDataRange.addFacet(Facets.MIN_EXCLUSIVE, value);
                } break;
                case MAX_INCLUSIVE: {
                   currentDataRange.addFacet(Facets.MAX_INCLUSIVE, value);
                } break;
                case MAX_EXCLUSIVE: {
                    currentDataRange.addFacet(Facets.MAX_EXCLUSIVE, value);
                } break;
//                case FRACTION_DIGITS: {
//                    facet = DatatypeRestrictionLiteral.Facets.FRACTION_DIGITS;
//                } break;
                case PATTERN: {
                    currentDataRange.addFacet(Facets.PATTERN, value);
                } break;
//                case TOTAL_DIGITS: {
//                    facet = DatatypeRestrictionLiteral.Facets.TOTAL_DIGITS;
//                } break;
                default:
                    throw new IllegalArgumentException("Unsupported facet.");
                }
            }
        }

        public void visit(OWLUntypedConstant untypedConstant) {
            String lit = untypedConstant.getLiteral();
            String lang = untypedConstant.getLang();
            if (lang == null) {
                currentConstant = new DataConstant(
                        Impl.IString, DT.STRING, lit);
            } else {
                currentConstant = new DataConstant(
                        Impl.IString, DT.RDFTEXT, lit, lang);
            }
        }

        public void visit(OWLDataRangeFacetRestriction facetRestriction) {
            throw new RuntimeException("OWLDataRangeFacetRestriction were " +
            		"supposed to be handled in OWLDataRangeRestriction. ");
        }

        public DataRange getDataRange() {
            return currentDataRange;
        }
        
        public boolean isNegated() {
            return isNegated;
        }
        public void negate() {
            isNegated = !isNegated;
        }
    }

    protected static class FactClausifier extends
            org.semanticweb.owl.util.OWLAxiomVisitorAdapter {
        protected final Set<Atom> m_positiveFacts;
        protected final Set<Atom> m_negativeFacts;

        public FactClausifier(Set<Atom> positiveFacts, Set<Atom> negativeFacts) {
            m_positiveFacts = positiveFacts;
            m_negativeFacts = negativeFacts;
        }

        public void visit(OWLSameIndividualsAxiom object) {
            OWLIndividual[] individuals = new OWLIndividual[object.getIndividuals().size()];
            object.getIndividuals().toArray(individuals);
            for (int i = 0; i < individuals.length - 1; i++)
                m_positiveFacts.add(Atom.create(Equality.create(),
                        new org.semanticweb.HermiT.model.Term[] {
                                getIndividual(individuals[i]),
                                getIndividual(individuals[i + 1]) }));
        }

        public void visit(OWLDifferentIndividualsAxiom object) {
            OWLIndividual[] individuals = new OWLIndividual[object.getIndividuals().size()];
            object.getIndividuals().toArray(individuals);
            for (int i = 0; i < individuals.length; i++)
                for (int j = i + 1; j < individuals.length; j++)
                    m_positiveFacts.add(Atom.create(Inequality.create(),
                            new org.semanticweb.HermiT.model.Term[] {
                                    getIndividual(individuals[i]),
                                    getIndividual(individuals[j]) }));
        }

        public void visit(OWLDataPropertyAssertionAxiom assertion) {
            throw new IllegalArgumentException(
            "Internal error: data property assertions should have been rewritten into concept assertions.");
        }

        public void visit(OWLNegativeDataPropertyAssertionAxiom object) {
            throw new IllegalArgumentException(
            "Internal error: negative data property assertions should have been rewritten into concept assertions.");
        }

        public void visit(OWLObjectPropertyAssertionAxiom object) {
            m_positiveFacts.add(getRoleAtom(object.getProperty(),
                    getIndividual(object.getSubject()),
                    getIndividual(object.getObject())));
        }

        public void visit(OWLNegativeObjectPropertyAssertionAxiom object) {
            throw new IllegalArgumentException(
                    "Internal error: negative object property assertions should have been rewritten.");
        }

        public void visit(OWLClassAssertionAxiom object) {
            OWLDescription description = object.getDescription();
            if (description instanceof OWLClass) {
                AtomicConcept atomicConcept = AtomicConcept.create(((OWLClass) description).getURI().toString());
                m_positiveFacts.add(Atom.create(
                        atomicConcept,
                        new org.semanticweb.HermiT.model.Term[] { getIndividual(object.getIndividual()) }));
            } else if (description instanceof OWLObjectComplementOf
                    && ((OWLObjectComplementOf) description).getOperand() instanceof OWLClass) {
                AtomicConcept atomicConcept = AtomicConcept.create(((OWLClass) ((OWLObjectComplementOf) description).getOperand()).getURI().toString());
                m_negativeFacts.add(Atom.create(
                        atomicConcept,
                        new org.semanticweb.HermiT.model.Term[] { getIndividual(object.getIndividual()) }));
            } else if (description instanceof OWLObjectSelfRestriction) {
                OWLObjectSelfRestriction selfRestriction = (OWLObjectSelfRestriction) description;
                m_positiveFacts.add(getRoleAtom(
                        selfRestriction.getProperty(),
                        getIndividual(object.getIndividual()),
                        getIndividual(object.getIndividual())));
            } else if (description instanceof OWLObjectComplementOf
                    && ((OWLObjectComplementOf) description).getOperand() instanceof OWLObjectSelfRestriction) {
                OWLObjectSelfRestriction selfRestriction = (OWLObjectSelfRestriction) (((OWLObjectComplementOf) description).getOperand());
                m_negativeFacts.add(getRoleAtom(
                        selfRestriction.getProperty(),
                        getIndividual(object.getIndividual()),
                        getIndividual(object.getIndividual())));
            } else
                throw new IllegalStateException(
                        "Internal error: invalid normal form.");
        }
    }

    protected static class DetermineExpressivity implements
            OWLDescriptionVisitor {
        protected boolean m_hasAtMostRestrictions;
        protected boolean m_hasInverseRoles;
        protected boolean m_hasNominals;
        protected boolean m_hasReflexivity;
        protected boolean m_hasDatatypes;
        protected boolean m_hasKeys;

        protected void checkProperty(OWLObjectPropertyExpression p) {
            if (p instanceof OWLObjectPropertyInverse)
                m_hasInverseRoles = true;
        }

        public void visit(OWLDataAllRestriction desc) {
            m_hasDatatypes = true;
        }

        public void visit(OWLDataExactCardinalityRestriction desc) {
            m_hasDatatypes = true;
        }

        public void visit(OWLDataMaxCardinalityRestriction desc) {
            m_hasDatatypes = true;
        }

        public void visit(OWLDataMinCardinalityRestriction desc) {
            m_hasDatatypes = true;
        }

        public void visit(OWLDataSomeRestriction desc) {
            m_hasDatatypes = true;
        }

        public void visit(OWLDataValueRestriction desc) {
            m_hasDatatypes = true;
        }

        public void visit(OWLClass object) {
        }

        public void visit(OWLObjectAllRestriction object) {
            checkProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLObjectSomeRestriction object) {
            checkProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLObjectExactCardinalityRestriction object) {
            m_hasAtMostRestrictions = true;
            checkProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLObjectMinCardinalityRestriction object) {
            checkProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLObjectMaxCardinalityRestriction object) {
            m_hasAtMostRestrictions = true;
            checkProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLObjectComplementOf object) {
            // WARNING: I think that if this get executed our min/max tests
            // don't work.
            // -rob 2008-06-16
            object.getOperand().accept(this);
        }

        public void visit(OWLObjectUnionOf object) {
            for (OWLDescription d : object.getOperands())
                d.accept(this);
        }

        public void visit(OWLObjectIntersectionOf object) {
            for (OWLDescription d : object.getOperands())
                d.accept(this);
        }

        public void visit(OWLObjectOneOf object) {
            m_hasNominals = true;
        }

        public void visit(OWLObjectValueRestriction object) {
            m_hasNominals = true;
            checkProperty(object.getProperty());
        }

        public void visit(OWLObjectSelfRestriction object) {
            m_hasReflexivity = true;
        }
    }

    protected static class HeadComparator implements Comparator<Atom> {
        public static final HeadComparator INSTANCE = new HeadComparator();

        public int compare(Atom o1, Atom o2) {
            int type1;
            if (o1.getDLPredicate() instanceof AtLeastAbstractRoleConcept)
                type1 = 2;
            else
                type1 = 1;
            int type2;
            if (o2.getDLPredicate() instanceof AtLeastAbstractRoleConcept)
                type2 = 2;
            else
                type2 = 1;
            return type1 - type2;
        }
    }
}
