/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

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
package org.semanticweb.HermiT.structural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeRegistry;
import org.semanticweb.HermiT.datatypes.UnsupportedDatatypeException;
import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtLeastDataRange;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.ConstantEnumeration;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InternalDatatype;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.LiteralDataRange;
import org.semanticweb.HermiT.model.NodeIDLessEqualThan;
import org.semanticweb.HermiT.model.NodeIDsAscendingOrEqual;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.model.DLClause.ClauseType;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDataVisitor;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

public class OWLClausification {
    protected static final Variable X=Variable.create("X");
    protected static final Variable Y=Variable.create("Y");
    protected static final Variable Z=Variable.create("Z");

    protected final Configuration m_configuration;

    public OWLClausification(Configuration configuration) {
        m_configuration=configuration;
    }
    public Object[] preprocessAndClausify(OWLOntology rootOntology,Collection<DescriptionGraph> descriptionGraphs) {
        OWLDataFactory factory=rootOntology.getOWLOntologyManager().getOWLDataFactory();
        String ontologyIRI=rootOntology.getOntologyID().getDefaultDocumentIRI()==null ? "urn:hermit:kb" : rootOntology.getOntologyID().getDefaultDocumentIRI().toString();
        Collection<OWLOntology> importClosure=rootOntology.getImportsClosure();
        OWLAxioms axioms=new OWLAxioms();
        OWLNormalization normalization=new OWLNormalization(factory,axioms,0);
        for (OWLOntology ontology : importClosure)
            normalization.processOntology(ontology);
        BuiltInPropertyManager builtInPropertyManager=new BuiltInPropertyManager(factory);
        builtInPropertyManager.axiomatizeBuiltInPropertiesAsNeeded(axioms);
        ObjectPropertyInclusionManager objectPropertyInclusionManager=new ObjectPropertyInclusionManager(axioms);
        // now object property inclusion manager added all non-simple properties to axioms.m_complexObjectPropertyExpressions
        // now that we know which roles are non-simple, we can decide which negative object property assertions have to be
        // expressed as concept assertions so that transitivity rewriting applies properly.
        objectPropertyInclusionManager.rewriteNegativeObjectPropertyAssertions(factory, axioms, normalization.m_definitions.size());
        objectPropertyInclusionManager.rewriteAxioms(factory,axioms,0);
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        OWLAxiomsExpressivity axiomsExpressivity=new OWLAxiomsExpressivity(axioms);
        DLOntology dlOntology=clausify(factory,ontologyIRI,axioms,axiomsExpressivity,descriptionGraphs);
        return new Object[] { objectPropertyInclusionManager,dlOntology };
    }
    public DLOntology clausify(OWLDataFactory factory,String ontologyIRI,OWLAxioms axioms,OWLAxiomsExpressivity axiomsExpressivity,Collection<DescriptionGraph> descriptionGraphs) {
        Set<DLClause> dlClauses=new LinkedHashSet<DLClause>();
        Set<Atom> positiveFacts=new HashSet<Atom>();
        Set<Atom> negativeFacts=new HashSet<Atom>();
        Set<DatatypeRestriction> allUnknownDatatypeRestrictions=new HashSet<DatatypeRestriction>();
        for (OWLObjectPropertyExpression[] inclusion : axioms.m_simpleObjectPropertyInclusions) {
            OWLObjectPropertyExpression sub=inclusion[0];
            OWLObjectPropertyExpression sup=inclusion[1];
            ClauseType clauseType;
            if (sub.isAnonymous()!=sup.isAnonymous())
                clauseType=ClauseType.INVERSE_OBJECT_PROPERTY_INCLUSION;
            else
                clauseType=ClauseType.OBJECT_PROPERTY_INCLUSION;
            Atom subRoleAtom=getRoleAtom(inclusion[0],X,Y);
            Atom superRoleAtom=getRoleAtom(inclusion[1],X,Y);
            DLClause dlClause=DLClause.create(new Atom[] { superRoleAtom },new Atom[] { subRoleAtom },clauseType);
            dlClauses.add(dlClause);
        }
        for (OWLDataPropertyExpression[] inclusion : axioms.m_dataPropertyInclusions) {
            Atom subProp=getRoleAtom(inclusion[0],X,Y);
            Atom superProp=getRoleAtom(inclusion[1],X,Y);
            DLClause dlClause=DLClause.create(new Atom[] { superProp },new Atom[] { subProp },ClauseType.DATA_PROPERTY_INCLUSION);
            dlClauses.add(dlClause);
        }
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_asymmetricObjectProperties) {
            Atom roleAtom=getRoleAtom(objectPropertyExpression,X,Y);
            Atom inverseRoleAtom=getRoleAtom(objectPropertyExpression,Y,X);
            DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { roleAtom,inverseRoleAtom },ClauseType.ASYMMETRY);
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_reflexiveObjectProperties) {
            Atom roleAtom=getRoleAtom(objectPropertyExpression,X,X);
            DLClause dlClause=DLClause.create(new Atom[] { roleAtom },new Atom[] {},ClauseType.REFLEXIVITY);
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_irreflexiveObjectProperties) {
            Atom roleAtom=getRoleAtom(objectPropertyExpression,X,X);
            DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { roleAtom },ClauseType.IRREFLEXIVITY);
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression[] properties : axioms.m_disjointObjectProperties)
            for (int i=0;i<properties.length;i++)
                for (int j=i+1;j<properties.length;j++) {
                    Atom atom_i=getRoleAtom(properties[i],X,Y);
                    Atom atom_j=getRoleAtom(properties[j],X,Y);
                    DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { atom_i,atom_j },ClauseType.DISJOINT_OBJECT_PROPERTIES);
                    dlClauses.add(dlClause.getSafeVersion());
                }
        if (axioms.m_dataPropertyInclusions.contains(factory.getOWLDataProperty(IRI.create(AtomicRole.BOTTOM_DATA_ROLE.getIRI())))) {
            Atom bodyAtom=Atom.create(AtomicRole.BOTTOM_DATA_ROLE,X,Y);
            dlClauses.add(DLClause.create(new Atom[] {},new Atom[] { bodyAtom },ClauseType.OTHER).getSafeVersion());
        }
        for (OWLDataPropertyExpression[] properties : axioms.m_disjointDataProperties)
            for (int i=0;i<properties.length;i++)
                for (int j=i+1;j<properties.length;j++) {
                    Atom atom_i=getRoleAtom(properties[i],X,Y);
                    Atom atom_j=getRoleAtom(properties[j],X,Z);
                    Atom atom_ij=Atom.create(Inequality.create(),Y,Z);
                    DLClause dlClause=DLClause.create(new Atom[] { atom_ij },new Atom[] { atom_i,atom_j },ClauseType.DISJOINT_DATA_PROPERTIES);
                    dlClauses.add(dlClause.getSafeVersion());
                }
        DataRangeConverter dataRangeConverter=new DataRangeConverter(m_configuration.warningMonitor,axioms.m_definedDatatypesIRIs,allUnknownDatatypeRestrictions,m_configuration.ignoreUnsupportedDatatypes);
        NormalizedAxiomClausifier clausifier=new NormalizedAxiomClausifier(dataRangeConverter,positiveFacts,factory,axioms.m_dps2ranges);
        for (OWLClassExpression[] inclusion : axioms.m_conceptInclusions) {
            for (OWLClassExpression description : inclusion)
                description.accept(clausifier);
            DLClause dlClause=clausifier.getDLClause();
            dlClauses.add(dlClause.getSafeVersion());
        }
        NormalizedDataRangeAxiomClausifier normalizedDataRangeAxiomClausifier=new NormalizedDataRangeAxiomClausifier(dataRangeConverter,factory,axioms.m_definedDatatypesIRIs);
        for (OWLDataRange[] inclusion : axioms.m_dataRangeInclusions) {
            for (OWLDataRange description : inclusion)
                description.accept(normalizedDataRangeAxiomClausifier);
            DLClause dlClause=normalizedDataRangeAxiomClausifier.getDLClause();
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLHasKeyAxiom hasKey : axioms.m_hasKeys)
            dlClauses.add(clausifyKey(hasKey).getSafeVersion());
        FactClausifier factClausifier=new FactClausifier(dataRangeConverter,positiveFacts,negativeFacts);
        for (OWLIndividualAxiom fact : axioms.m_facts)
            fact.accept(factClausifier);
        for (DescriptionGraph descriptionGraph : descriptionGraphs)
            descriptionGraph.produceStartDLClauses(dlClauses);
        Set<AtomicConcept> atomicConcepts=new HashSet<AtomicConcept>();
        Set<AtomicRole> atomicObjectRoles=new HashSet<AtomicRole>();
        Set<Role> complexObjectRoles=new HashSet<Role>();
        Set<AtomicRole> atomicDataRoles=new HashSet<AtomicRole>();
        for (OWLClass owlClass : axioms.m_classes)
            atomicConcepts.add(AtomicConcept.create(owlClass.getIRI().toString()));
        Set<Individual> individuals=new HashSet<Individual>();
        for (OWLNamedIndividual owlIndividual : axioms.m_namedIndividuals) {
            Individual individual=Individual.create(owlIndividual.getIRI().toString());
            individuals.add(individual);
            // all named individuals are tagged with a concept, so that keys/rules are
            // only applied to them
            if (!axioms.m_hasKeys.isEmpty() || !axioms.m_rules.isEmpty())
                positiveFacts.add(Atom.create(AtomicConcept.INTERNAL_NAMED,individual));
        }
        for (OWLObjectProperty objectProperty : axioms.m_objectProperties)
            atomicObjectRoles.add(AtomicRole.create(objectProperty.getIRI().toString()));
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_complexObjectPropertyExpressions)
            complexObjectRoles.add(getRole(objectPropertyExpression));
        for (OWLDataProperty dataProperty : axioms.m_dataProperties)
            atomicDataRoles.add(AtomicRole.create(dataProperty.getIRI().toString()));
        // Clausify SWRL rules
        if (!axioms.m_rules.isEmpty()) {
            m_configuration.checkClauses=false;
            NormalizedRuleClausifier normalizedRuleClausifier=new NormalizedRuleClausifier(positiveFacts,negativeFacts,descriptionGraphs,axioms.m_objectPropertiesUsedInAxioms,dataRangeConverter);
            for (SWRLRule rule : axioms.m_rules)
                rule.accept(normalizedRuleClausifier);
            Set<Atom> safenessAtoms=new HashSet<Atom>();
            boolean isGraphRule=false;
            for (int clauseIndex=0;clauseIndex<normalizedRuleClausifier.m_bodies.size();clauseIndex++) {
                List<Atom> bodies=normalizedRuleClausifier.m_bodies.get(clauseIndex);
                List<Atom> heads=normalizedRuleClausifier.m_heads.get(clauseIndex);
                for (Atom at : bodies) {
                    if (at.getDLPredicate() instanceof AtomicRole && normalizedRuleClausifier.m_graphRoles.contains(factory.getOWLObjectProperty(IRI.create(((AtomicRole)at.getDLPredicate()).getIRI()))))
                        isGraphRule=true;
                    if (isGraphRule)
                        break;
                }
                if (!isGraphRule)
                    for (Atom at : heads)
                        if (at.getDLPredicate() instanceof AtomicRole && normalizedRuleClausifier.m_graphRoles.contains(factory.getOWLObjectProperty(IRI.create(((AtomicRole)at.getDLPredicate()).getIRI()))))
                            isGraphRule=true;
                // We will always apply the DL-safe restriction to rules such as A(x) and B(y) -> C(x)
                // because if the rule does not contain roles we don't know whether it is a graph rule or not...
                if (!isGraphRule) {
                    // apply rules only to named individuals
                    for (Variable var : normalizedRuleClausifier.m_DLSafeVarSets.get(clauseIndex)) {
                        bodies.add(Atom.create(AtomicConcept.INTERNAL_NAMED,var));
                    }
                }
                Atom[] headAtoms=new Atom[heads.size()];
                heads.toArray(headAtoms);
                Atom[] bodyAtoms=new Atom[bodies.size()];
                bodies.toArray(bodyAtoms);
                dlClauses.add(DLClause.create(headAtoms,bodyAtoms,isGraphRule ? ClauseType.GRAPH_RULE : ClauseType.SWRL_RULE));
                isGraphRule=false;
                safenessAtoms.clear();
            }
        }
        // Create the DL ontology
        return new DLOntology(ontologyIRI,dlClauses,positiveFacts,negativeFacts,atomicConcepts,atomicObjectRoles,complexObjectRoles,atomicDataRoles,allUnknownDatatypeRestrictions,axioms.m_definedDatatypesIRIs,individuals,axiomsExpressivity.m_hasInverseRoles,axiomsExpressivity.m_hasAtMostRestrictions,axiomsExpressivity.m_hasNominals,axiomsExpressivity.m_hasDatatypes);
    }
    protected DLClause clausifyKey(OWLHasKeyAxiom object) {
        List<Atom> headAtoms=new ArrayList<Atom>();
        List<Atom> bodyAtoms=new ArrayList<Atom>();
        // we have two named individuals (corresponding to X1 and X2) that
        // might have to be equated
        Variable X2=Variable.create("X2");
        Variable X1=Variable.create("X1");
        headAtoms.add(Atom.create(Equality.INSTANCE,X1,X2));
        // keys only work on datatypes and named individuals
        bodyAtoms.add(Atom.create(AtomicConcept.INTERNAL_NAMED,X1));
        bodyAtoms.add(Atom.create(AtomicConcept.INTERNAL_NAMED,X2));
        // the concept expression of a hasKey statement is either a concept
        // name or a negated concept name after normalization
        OWLClassExpression description=object.getClassExpression();
        if (description instanceof OWLClass) {
            OWLClass owlClass=(OWLClass)description;
            if (!owlClass.isOWLThing()) {
                bodyAtoms.add(Atom.create(AtomicConcept.create(owlClass.getIRI().toString()),X1));
                bodyAtoms.add(Atom.create(AtomicConcept.create(owlClass.getIRI().toString()),X2));
            }
        }
        else if (description instanceof OWLObjectComplementOf) {
            OWLClassExpression internal=((OWLObjectComplementOf)description).getOperand();
            if (internal instanceof OWLClass) {
                OWLClass owlClass=(OWLClass)internal;
                bodyAtoms.add(Atom.create(AtomicConcept.create(owlClass.getIRI().toString()),X1));
                bodyAtoms.add(Atom.create(AtomicConcept.create(owlClass.getIRI().toString()),X2));
            }
            else {
                throw new IllegalStateException("Internal error: invalid normal form.");
            }
        }
        else {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        int y_ind=1;
        // object properties always go to the body
        for (OWLObjectPropertyExpression p : object.getObjectPropertyExpressions()) {
            Variable y;
            y=Variable.create("Y"+y_ind);
            y_ind++;
            bodyAtoms.add(getRoleAtom(p,X1,y));
            bodyAtoms.add(getRoleAtom(p,X2,y));
            // also the key criteria are named in case of object properties
            bodyAtoms.add(Atom.create(AtomicConcept.INTERNAL_NAMED,y));
        }
        // data properties go to the body, but with different variables
        // the head gets an atom asserting inequality between that data values
        for (OWLDataPropertyExpression d : object.getDataPropertyExpressions()) {
            Variable y;
            y=Variable.create("Y"+y_ind);
            y_ind++;
            bodyAtoms.add(getRoleAtom(d,X1,y));
            Variable y2;
            y2=Variable.create("Y"+y_ind);
            y_ind++;
            bodyAtoms.add(getRoleAtom(d,X2,y2));
            headAtoms.add(Atom.create(Inequality.INSTANCE,y,y2));
        }
        Atom[] hAtoms=new Atom[headAtoms.size()];
        headAtoms.toArray(hAtoms);
        Atom[] bAtoms=new Atom[bodyAtoms.size()];
        bodyAtoms.toArray(bAtoms);
        DLClause clause=DLClause.createEx(true,hAtoms,bAtoms,ClauseType.HAS_KEY);
        return clause;
    }
    protected static LiteralConcept getLiteralConcept(OWLClassExpression description) {
        if (description instanceof OWLClass) {
            return AtomicConcept.create(((OWLClass)description).getIRI().toString());
        }
        else if (description instanceof OWLObjectComplementOf) {
            OWLClassExpression internal=((OWLObjectComplementOf)description).getOperand();
            if (!(internal instanceof OWLClass))
                throw new IllegalStateException("Internal error: invalid normal form.");
            return AtomicConcept.create(((OWLClass)internal).getIRI().toString()).getNegation();
        }
        else
            throw new IllegalStateException("Internal error: invalid normal form.");
    }
    protected static Role getRole(OWLObjectPropertyExpression objectPropertyExpression) {
        objectPropertyExpression=objectPropertyExpression.getSimplified();
        if (objectPropertyExpression instanceof OWLObjectProperty)
            return AtomicRole.create(((OWLObjectProperty)objectPropertyExpression).getIRI().toString());
        else if (objectPropertyExpression instanceof OWLObjectInverseOf) {
            OWLObjectPropertyExpression internal=((OWLObjectInverseOf)objectPropertyExpression).getInverse();
            if (!(internal instanceof OWLObjectProperty))
                throw new IllegalStateException("Internal error: invalid normal form.");
            return AtomicRole.create(((OWLObjectProperty)internal).getIRI().toString()).getInverse();
        }
        else
            throw new IllegalStateException("Internal error: invalid normal form.");
    }
    protected static AtomicRole getAtomicRole(OWLDataPropertyExpression dataPropertyExpression) {
        return AtomicRole.create(((OWLDataProperty)dataPropertyExpression).getIRI().toString());
    }
    protected static Atom getRoleAtom(OWLObjectPropertyExpression objectProperty,Term first,Term second) {
        objectProperty=objectProperty.getSimplified();
        if (!objectProperty.isAnonymous()) {
            AtomicRole role=AtomicRole.create(objectProperty.asOWLObjectProperty().getIRI().toString());
            return Atom.create(role,first,second);
        }
        else if (objectProperty.isAnonymous()) {
            OWLObjectProperty internalObjectProperty=objectProperty.getNamedProperty();
            AtomicRole role=AtomicRole.create(internalObjectProperty.getIRI().toString());
            return Atom.create(role,second,first);
        }
        else
            throw new IllegalStateException("Internal error: unsupported type of object property!");
    }
    protected static Atom getRoleAtom(OWLDataPropertyExpression dataProperty,Term first,Term second) {
        if (dataProperty instanceof OWLDataProperty) {
            AtomicRole property=AtomicRole.create(((OWLDataProperty)dataProperty).getIRI().toString());
            return Atom.create(property,first,second);
        }
        else
            throw new IllegalStateException("Internal error: unsupported type of data property!");
    }
    protected static Individual getIndividual(OWLIndividual individual) {
        if (individual.isAnonymous())
            return Individual.createAnonymous(individual.asOWLAnonymousIndividual().getID().toString());
        else
            return Individual.create(individual.asOWLNamedIndividual().getIRI().toString());
    }

    protected static class NormalizedAxiomClausifier implements OWLClassExpressionVisitor {
        public static Map<String,Long> dt2maxRangeCardinality;
        protected final DataRangeConverter m_dataRangeConverter;
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;
        protected final Set<Atom> m_positiveFacts;
        protected final OWLDataFactory m_factory;
        protected final Map<OWLDataProperty,OWLDatatype> m_dps2ranges;
        protected int m_yIndex;
        protected int m_zIndex;
        static {
            dt2maxRangeCardinality=new HashMap<String,Long>();
            dt2maxRangeCardinality.put(Prefixes.s_semanticWebPrefixes.get("xsd")+"int",((long)(Integer.MAX_VALUE)+(long)(Integer.MAX_VALUE)+1l));
            dt2maxRangeCardinality.put(Prefixes.s_semanticWebPrefixes.get("xsd")+"short",((long)(Short.MAX_VALUE)+(long)(Short.MAX_VALUE)+1l));
            dt2maxRangeCardinality.put(Prefixes.s_semanticWebPrefixes.get("xsd")+"byte",((long)(Byte.MAX_VALUE)+(long)(Byte.MAX_VALUE)+2l));
            dt2maxRangeCardinality.put(Prefixes.s_semanticWebPrefixes.get("xsd")+"unsignedInt",new Long("4294967296"));
            dt2maxRangeCardinality.put(Prefixes.s_semanticWebPrefixes.get("xsd")+"unsignedShort",new Long("65536"));
            dt2maxRangeCardinality.put(Prefixes.s_semanticWebPrefixes.get("xsd")+"unsignedByte",new Long("256"));
            // long >= Long.MIN_VALUE - <= Long.MAX_VALUE
            // int >= Integer.MIN_VALUE - <= Integer.MAX_VALUE
            // short >= Short.MIN_VALUE - <= Short.MAX_VALUE
            // byte >= Byte.MIN_VALUE - <= Byte.MAX_VALUE
            // unsignedLong >= 0 - <= new BigInteger("18446744073709551615")
            // unsignedInt >= 0 - <= 4294967295L
            // unsignedShort >= 0 - <= 65535
            // unsignedByte >= 0 - <= 255
        }
        public NormalizedAxiomClausifier(DataRangeConverter dataRangeConverter,Set<Atom> positiveFacts,OWLDataFactory factory,Map<OWLDataProperty,OWLDatatype> dps2ranges) {
            m_dataRangeConverter=dataRangeConverter;
            m_headAtoms=new ArrayList<Atom>();
            m_bodyAtoms=new ArrayList<Atom>();
            m_positiveFacts=positiveFacts;
            m_dps2ranges=dps2ranges;
            m_factory=factory;
        }
        protected DLClause getDLClause() {
            Atom[] headAtoms=new Atom[m_headAtoms.size()];
            m_headAtoms.toArray(headAtoms);
            Atom[] bodyAtoms=new Atom[m_bodyAtoms.size()];
            m_bodyAtoms.toArray(bodyAtoms);
            DLClause dlClause=DLClause.create(headAtoms,bodyAtoms,ClauseType.CONCEPT_INCLUSION);
            m_headAtoms.clear();
            m_bodyAtoms.clear();
            m_yIndex=0;
            m_zIndex=0;
            return dlClause;
        }
        protected void ensureYNotZero() {
            if (m_yIndex==0)
                m_yIndex++;
        }
        protected Variable nextY() {
            Variable result;
            if (m_yIndex==0)
                result=Y;
            else
                result=Variable.create("Y"+m_yIndex);
            m_yIndex++;
            return result;
        }
        protected Variable nextZ() {
            Variable result;
            if (m_zIndex==0)
                result=Z;
            else
                result=Variable.create("Z"+m_zIndex);
            m_zIndex++;
            return result;
        }
        protected AtomicConcept getConceptForNominal(OWLIndividual individual) {
            AtomicConcept result;
            if (individual.isAnonymous()) {
                result=AtomicConcept.create("internal:anon#"+individual.asOWLAnonymousIndividual().getID().toString());
            }
            else {
                result=AtomicConcept.create("internal:nom#"+individual.asOWLNamedIndividual().getIRI().toString());
            }
            m_positiveFacts.add(Atom.create(result,getIndividual(individual)));
            return result;
        }

        // Various types of descriptions

        public void visit(OWLClass object) {
            m_headAtoms.add(Atom.create(AtomicConcept.create(object.getIRI().toString()),X));
        }
        public void visit(OWLObjectIntersectionOf object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLObjectUnionOf object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLObjectComplementOf object) {
            OWLClassExpression description=object.getOperand();
            if (description instanceof OWLObjectHasSelf) {
                OWLObjectPropertyExpression objectProperty=((OWLObjectHasSelf)description).getProperty();
                Atom roleAtom=getRoleAtom(objectProperty,X,X);
                m_bodyAtoms.add(roleAtom);
            }
            /**
             * gstoil modification. This situation is now possible with automata. Since the translation says that for each final state f add inclusion f -> C. If C is a nominal and comes from a concept with negative polarity then subsumption is actually f-> \neg {o} which was not a valid HermiT normal form.
             */
            else if (description instanceof OWLObjectOneOf && ((OWLObjectOneOf)description).getIndividuals().size()==1) {
                OWLIndividual individual=((OWLObjectOneOf)description).getIndividuals().iterator().next();
                m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),X));
            }
            else if (!(description instanceof OWLClass))
                throw new IllegalStateException("Internal error: invalid normal form.");
            else
                m_bodyAtoms.add(Atom.create(AtomicConcept.create(((OWLClass)description).getIRI().toString()),X));
        }
        public void visit(OWLObjectOneOf object) {
            for (OWLIndividual individual : object.getIndividuals()) {
                Variable z=nextZ();
                AtomicConcept conceptForNominal=getConceptForNominal(individual);
                m_headAtoms.add(Atom.create(Equality.INSTANCE,X,z));
                m_bodyAtoms.add(Atom.create(conceptForNominal,z));
            }
        }
        public void visit(OWLObjectSomeValuesFrom object) {
            OWLClassExpression filler=object.getFiller();
            if (filler instanceof OWLObjectOneOf) {
                for (OWLIndividual individual : ((OWLObjectOneOf)filler).getIndividuals()) {
                    Variable z=nextZ();
                    m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),z));
                    m_headAtoms.add(getRoleAtom(object.getProperty(),X,z));
                }
            }
            else {
                LiteralConcept toConcept=getLiteralConcept(filler);
                Role onRole=getRole(object.getProperty());
                AtLeastConcept atLeastConcept=AtLeastConcept.create(1,onRole,toConcept);
                if (!atLeastConcept.isAlwaysFalse())
                    m_headAtoms.add(Atom.create(atLeastConcept,X));
            }
        }
        public void visit(OWLObjectAllValuesFrom object) {
            Variable y=nextY();
            m_bodyAtoms.add(getRoleAtom(object.getProperty(),X,y));
            OWLClassExpression filler=object.getFiller();
            if (filler instanceof OWLClass) {
                AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)filler).getIRI().toString());
                if (!atomicConcept.isAlwaysFalse())
                    m_headAtoms.add(Atom.create(atomicConcept,y));
            }
            else if (filler instanceof OWLObjectOneOf) {
                for (OWLIndividual individual : ((OWLObjectOneOf)filler).getIndividuals()) {
                    Variable zInd=nextZ();
                    m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),zInd));
                    m_headAtoms.add(Atom.create(Equality.INSTANCE,y,zInd));
                }
            }
            else if (filler instanceof OWLObjectComplementOf) {
                OWLClassExpression operand=((OWLObjectComplementOf)filler).getOperand();
                if (operand instanceof OWLClass) {
                    AtomicConcept internalAtomicConcept=AtomicConcept.create(((OWLClass)operand).getIRI().toString());
                    if (!internalAtomicConcept.isAlwaysTrue())
                        m_bodyAtoms.add(Atom.create(internalAtomicConcept,y));
                }
                else if (operand instanceof OWLObjectOneOf && ((OWLObjectOneOf)operand).getIndividuals().size()==1) {
                    OWLIndividual individual=((OWLObjectOneOf)operand).getIndividuals().iterator().next();
                    m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),y));
                }
                else
                    throw new IllegalStateException("Internal error: invalid normal form.");
            }
            else
                throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLObjectHasValue object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLObjectHasSelf object) {
            OWLObjectPropertyExpression objectProperty=object.getProperty();
            Atom roleAtom=getRoleAtom(objectProperty,X,X);
            m_headAtoms.add(roleAtom);
        }
        public void visit(OWLObjectMinCardinality object) {
            LiteralConcept toConcept=getLiteralConcept(object.getFiller());
            Role onRole=getRole(object.getProperty());
            AtLeastConcept atLeastConcept=AtLeastConcept.create(object.getCardinality(),onRole,toConcept);
            if (!atLeastConcept.isAlwaysFalse())
                m_headAtoms.add(Atom.create(atLeastConcept,X));
        }
        public void visit(OWLObjectMaxCardinality object) {
            int cardinality=object.getCardinality();
            OWLObjectPropertyExpression onObjectProperty=object.getProperty();
            OWLClassExpression filler=object.getFiller();
            ensureYNotZero();
            boolean isPositive;
            AtomicConcept atomicConcept;
            if (filler instanceof OWLClass) {
                isPositive=true;
                atomicConcept=AtomicConcept.create(((OWLClass)filler).getIRI().toString());
                if (atomicConcept.isAlwaysTrue())
                    atomicConcept=null;
            }
            else if (filler instanceof OWLObjectComplementOf) {
                OWLClassExpression internal=((OWLObjectComplementOf)filler).getOperand();
                if (!(internal instanceof OWLClass))
                    throw new IllegalStateException("Internal error: Invalid ontology normal form.");
                isPositive=false;
                atomicConcept=AtomicConcept.create(((OWLClass)internal).getIRI().toString());
                if (atomicConcept.isAlwaysFalse())
                    atomicConcept=null;
            }
            else
                throw new IllegalStateException("Internal error: Invalid ontology normal form.");
            Role onRole=getRole(onObjectProperty);
            LiteralConcept toConcept=getLiteralConcept(filler);
            AnnotatedEquality annotatedEquality=AnnotatedEquality.create(cardinality,onRole,toConcept);
            Variable[] yVars=new Variable[cardinality+1];
            for (int i=0;i<yVars.length;i++) {
                yVars[i]=nextY();
                m_bodyAtoms.add(getRoleAtom(onObjectProperty,X,yVars[i]));
                if (atomicConcept!=null) {
                    Atom atom=Atom.create(atomicConcept,yVars[i]);
                    if (isPositive)
                        m_bodyAtoms.add(atom);
                    else
                        m_headAtoms.add(atom);
                }
            }
            // Node ID comparisons are not needed in case of functionality axioms,
            // as the effect of these is simulated by the way in which the rules are applied.
            if (yVars.length>2) {
                for (int i=0;i<yVars.length-1;i++)
                    m_bodyAtoms.add(Atom.create(NodeIDLessEqualThan.INSTANCE,yVars[i],yVars[i+1]));
                m_bodyAtoms.add(Atom.create(NodeIDsAscendingOrEqual.create(yVars.length),yVars));
            }
            for (int i=0;i<yVars.length;i++)
                for (int j=i+1;j<yVars.length;j++)
                    m_headAtoms.add(Atom.create(annotatedEquality,yVars[i],yVars[j],X));
        }
        public void visit(OWLObjectExactCardinality object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLDataSomeValuesFrom object) {
            if (!object.getProperty().isOWLBottomDataProperty()) {
                AtomicRole atomicRole=getAtomicRole(object.getProperty());
                LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(object.getFiller());
                AtLeastDataRange atLeastDataRange=AtLeastDataRange.create(1,atomicRole,literalRange);
                if (!atLeastDataRange.isAlwaysFalse())
                    m_headAtoms.add(Atom.create(atLeastDataRange,X));
            }
        }
        public void visit(OWLDataAllValuesFrom object) {
            LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(object.getFiller());
            if (object.getProperty().isOWLTopDataProperty()) {
                if (literalRange.isAlwaysFalse())
                    return; // bottom
            }
            Variable y=nextY();
            m_bodyAtoms.add(getRoleAtom(object.getProperty(),X,y));
            if (literalRange.isNegatedInternalDatatype()) {
                InternalDatatype negatedRange=(InternalDatatype)literalRange.getNegation();
                if (!negatedRange.isAlwaysTrue())
                    m_bodyAtoms.add(Atom.create(negatedRange,y));
            }
            else {
                if (!literalRange.isAlwaysFalse())
                    m_headAtoms.add(Atom.create((DLPredicate)literalRange,y));
            }
        }
        public void visit(OWLDataHasValue object) {
            throw new IllegalStateException("Internal error: Invalid normal form.");
        }
        public void visit(OWLDataMinCardinality object) {
            if (!object.getProperty().isOWLBottomDataProperty() || object.getCardinality()==0) {
                OWLDataProperty dp=object.getProperty().asOWLDataProperty();
                OWLDataRange dr=object.getFiller();
                int n=object.getCardinality();

                if (m_dps2ranges.containsKey(dp)) {
                    String dt=m_dps2ranges.get(dp).getIRI().toString();
                    if (dt2maxRangeCardinality.containsKey(dt) && n>dt2maxRangeCardinality.get(dt))
                        return;
                }
                if (dr.isDatatype()) {
                    String dt=dr.asOWLDatatype().getIRI().toString();
                    if (dt2maxRangeCardinality.containsKey(dt) && n>dt2maxRangeCardinality.get(dt))
                        return;
                }
                AtomicRole atomicRole=getAtomicRole(object.getProperty());
                LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(object.getFiller());
                AtLeastDataRange atLeast=AtLeastDataRange.create(object.getCardinality(),atomicRole,literalRange);
                if (!atLeast.isAlwaysFalse())
                    m_headAtoms.add(Atom.create(atLeast,X));
            }
        }
        public void visit(OWLDataMaxCardinality object) {
            int number=object.getCardinality();
            LiteralDataRange negatedDataRange=m_dataRangeConverter.convertDataRange(object.getFiller()).getNegation();
            ensureYNotZero();
            Variable[] yVars=new Variable[number+1];
            for (int i=0;i<yVars.length;i++) {
                yVars[i]=nextY();
                m_bodyAtoms.add(getRoleAtom(object.getProperty(),X,yVars[i]));
                if (negatedDataRange.isNegatedInternalDatatype()) {
                    InternalDatatype negated=(InternalDatatype)negatedDataRange.getNegation();
                    if (!negated.isAlwaysTrue())
                        m_bodyAtoms.add(Atom.create(negated,yVars[i]));
                }
                else {
                    if (!negatedDataRange.isAlwaysFalse())
                        m_headAtoms.add(Atom.create((DLPredicate)negatedDataRange,yVars[i]));
                }
            }
            for (int i=0;i<yVars.length;i++)
                for (int j=i+1;j<yVars.length;j++)
                    m_headAtoms.add(Atom.create(Equality.INSTANCE,yVars[i],yVars[j]));
        }
        public void visit(OWLDataExactCardinality object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
    }

    protected static class NormalizedDataRangeAxiomClausifier implements OWLDataVisitor {
        protected final DataRangeConverter m_dataRangeConverter;
        protected final Set<String> m_definedDatatypeIRIs;
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;
        protected final OWLDataFactory m_factory;
        protected int m_yIndex;

        public NormalizedDataRangeAxiomClausifier(DataRangeConverter dataRangeConverter,OWLDataFactory factory,Set<String> definedDatatypeIRIs) {
            m_dataRangeConverter=dataRangeConverter;
            m_definedDatatypeIRIs=definedDatatypeIRIs;
            m_headAtoms=new ArrayList<Atom>();
            m_bodyAtoms=new ArrayList<Atom>();
            m_factory=factory;
        }
        protected DLClause getDLClause() {
            Atom[] headAtoms=new Atom[m_headAtoms.size()];
            m_headAtoms.toArray(headAtoms);
            Atom[] bodyAtoms=new Atom[m_bodyAtoms.size()];
            m_bodyAtoms.toArray(bodyAtoms);
            DLClause dlClause=DLClause.create(headAtoms,bodyAtoms,ClauseType.DATA_RANGE_INCLUSION);
            m_headAtoms.clear();
            m_bodyAtoms.clear();
            m_yIndex=0;
            return dlClause;
        }
        protected void ensureYNotZero() {
            if (m_yIndex==0)
                m_yIndex++;
        }
        protected Variable nextY() {
            Variable result;
            if (m_yIndex==0)
                result=Y;
            else
                result=Variable.create("Y"+m_yIndex);
            m_yIndex++;
            return result;
        }

        // Various types of descriptions

        public void visit(OWLDatatype dt) {
            LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(dt);
//            if (!literalConcept.isAlwaysFalse())
            m_headAtoms.add(Atom.create((DLPredicate)literalRange,X));
        }
        public void visit(OWLDataIntersectionOf dr) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLDataUnionOf dr) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLDataComplementOf dr) {
            OWLDataRange description=dr.getDataRange();
            if (description.isDatatype() && (Prefixes.isInternalIRI(description.asOWLDatatype().getIRI().toString()) || m_definedDatatypeIRIs.contains(description.asOWLDatatype()))) {
                m_bodyAtoms.add(Atom.create(InternalDatatype.create(description.asOWLDatatype().getIRI().toString()),X));
            }
            else {
                LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(dr);
                if (literalRange.isNegatedInternalDatatype()) {
                    InternalDatatype negatedDatatype=(InternalDatatype)literalRange.getNegation();
                    if (!negatedDatatype.isAlwaysTrue())
                        m_bodyAtoms.add(Atom.create(negatedDatatype,X));
                }
                else {
                    if (!literalRange.isAlwaysFalse())
                        m_headAtoms.add(Atom.create((DLPredicate)literalRange,X));
                }
            }
        }
        public void visit(OWLDataOneOf object) {
            LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(object);
//            if (literalConcept instanceof AtomicNegationConcept) {
//                AtomicConcept negatedConcept=((AtomicNegationConcept)literalConcept).getNegatedAtomicConcept();
//                if (!negatedConcept.isAlwaysTrue())
//                    m_bodyAtoms.add(Atom.create(negatedConcept,X));
//            }
//            else {
//                if (!literalConcept.isAlwaysFalse())
              m_headAtoms.add(Atom.create((DLPredicate)literalRange,X));
//            }
        }
        public void visit(OWLFacetRestriction node) {
            throw new IllegalStateException("Internal error: Invalid normal form. ");
        }
        public void visit(OWLDatatypeRestriction node) {
            LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(node);
//            if (literalConcept instanceof AtomicNegationConcept) {
//                AtomicConcept negatedConcept=((AtomicNegationConcept)literalConcept).getNegatedAtomicConcept();
//                if (!negatedConcept.isAlwaysTrue())
//                    m_bodyAtoms.add(Atom.create(negatedConcept,X));
//            }
//            else {
//                if (!literalConcept.isAlwaysFalse())
                    m_headAtoms.add(Atom.create((DLPredicate)literalRange,X));
//            }
        }
        public void visit(OWLLiteral node) {
            throw new IllegalStateException("Internal error: Invalid normal form. ");
        }
    }

    protected static class DataRangeConverter implements OWLDataVisitorEx<Object> {
        protected final Configuration.WarningMonitor m_warningMonitor;
        protected final boolean m_ignoreUnsupportedDatatypes;
        protected final Set<String> m_definedDatatypeIRIs; // contains custom datatypes from DatatypeDefinition axioms
        protected final Set<DatatypeRestriction> m_allUnknownDatatypeRestrictions;

        public DataRangeConverter(Configuration.WarningMonitor warningMonitor,Set<String> definedDatatypeIRIs,Set<DatatypeRestriction> allUnknownDatatypeRestrictions,boolean ignoreUnsupportedDatatypes) {
            m_warningMonitor=warningMonitor;
            m_definedDatatypeIRIs=definedDatatypeIRIs;
            m_ignoreUnsupportedDatatypes=ignoreUnsupportedDatatypes;
            m_allUnknownDatatypeRestrictions=allUnknownDatatypeRestrictions;
        }
        public LiteralDataRange convertDataRange(OWLDataRange dataRange) {
            return (LiteralDataRange)dataRange.accept(this);
        }
        public Object visit(OWLDatatype object) {
            String datatypeURI=object.getIRI().toString();
            if (InternalDatatype.RDFS_LITERAL.getIRI().equals(datatypeURI))
                return InternalDatatype.RDFS_LITERAL;
            if (datatypeURI.startsWith("internal:defdata#") || m_definedDatatypeIRIs.contains(object.getIRI().toString()))
                return InternalDatatype.create(datatypeURI);
            DatatypeRestriction datatype=DatatypeRestriction.create(datatypeURI,DatatypeRestriction.NO_FACET_URIs,DatatypeRestriction.NO_FACET_VALUES);
            if (datatypeURI.startsWith("internal:unknown-datatype#"))
                m_allUnknownDatatypeRestrictions.add(datatype);
            else {
                try {
                    DatatypeRegistry.validateDatatypeRestriction(datatype);
                }
                catch (UnsupportedDatatypeException e) {
                    if (m_ignoreUnsupportedDatatypes) {
                        if (m_warningMonitor!=null)
                            m_warningMonitor.warning("Ignoring unsupported datatype '"+object.getIRI().toString()+"'.");
                        m_allUnknownDatatypeRestrictions.add(datatype);
                    }
                    else
                        throw e;
                }
            }
            return datatype;
        }
        public Object visit(OWLDataComplementOf object) {
            return convertDataRange(object.getDataRange()).getNegation();
        }
        public Object visit(OWLDataOneOf object) {
            Set<Constant> constants=new HashSet<Constant>();
            for (OWLLiteral literal : object.getValues())
                constants.add((Constant)literal.accept(this));
            Constant[] constantsArray=new Constant[constants.size()];
            constants.toArray(constantsArray);
            return ConstantEnumeration.create(constantsArray);
        }
        public Object visit(OWLDatatypeRestriction object) {
            if (!(object.getDatatype().isOWLDatatype()))
                throw new IllegalArgumentException("Datatype restrictions are supported only on OWL datatypes.");
            String datatypeURI=object.getDatatype().getIRI().toString();
            if (InternalDatatype.RDFS_LITERAL.getIRI().equals(datatypeURI)) {
                if (!object.getFacetRestrictions().isEmpty())
                    throw new IllegalArgumentException("rdfs:Literal does not support any facets.");
                return InternalDatatype.RDFS_LITERAL;
            }
            String[] facetURIs=new String[object.getFacetRestrictions().size()];
            Constant[] facetValues=new Constant[object.getFacetRestrictions().size()];
            int index=0;
            for (OWLFacetRestriction facet : object.getFacetRestrictions()) {
                facetURIs[index]=facet.getFacet().getIRI().toURI().toString();
                facetValues[index]=(Constant)facet.getFacetValue().accept(this);
                index++;
            }
            DatatypeRestriction datatype=DatatypeRestriction.create(datatypeURI,facetURIs,facetValues);
            DatatypeRegistry.validateDatatypeRestriction(datatype);
            return datatype;
        }
        public Object visit(OWLFacetRestriction object) {
            throw new IllegalStateException("Internal error: should not get in here.");
        }
        public Object visit(OWLLiteral object) {
            try {
                if (object.isRDFPlainLiteral()) {
                    if (object.hasLang()) {
                        return Constant.create(object.getLiteral()+"@"+object.getLang(),Prefixes.s_semanticWebPrefixes.get("rdf")+"PlainLiteral");
                    } else {
                        return Constant.create(object.getLiteral()+"@",Prefixes.s_semanticWebPrefixes.get("rdf")+"PlainLiteral");
                    }
                } else {
                    return Constant.create(object.getLiteral(),object.getDatatype().getIRI().toString());
                }
            }
            catch (UnsupportedDatatypeException e) {
                if (m_ignoreUnsupportedDatatypes) {
                    if (m_warningMonitor!=null)
                        m_warningMonitor.warning("Ignoring unsupported datatype '"+object.toString()+"'.");
                    return Constant.createAnonymous(object.getLiteral());
                }
                else
                    throw e;
            }
        }
        public Object visit(OWLDataIntersectionOf node) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public Object visit(OWLDataUnionOf node) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
    }

    protected static class FactClausifier extends OWLAxiomVisitorAdapter {
        protected final DataRangeConverter m_dataRangeConverter;
        protected final Set<Atom> m_positiveFacts;
        protected final Set<Atom> m_negativeFacts;

        public FactClausifier(DataRangeConverter dataRangeConverter,Set<Atom> positiveFacts,Set<Atom> negativeFacts) {
            m_dataRangeConverter=dataRangeConverter;
            m_positiveFacts=positiveFacts;
            m_negativeFacts=negativeFacts;
        }
        public void visit(OWLSameIndividualAxiom object) {
            OWLIndividual[] individuals=new OWLIndividual[object.getIndividuals().size()];
            object.getIndividuals().toArray(individuals);
            for (int i=0;i<individuals.length-1;i++)
                m_positiveFacts.add(Atom.create(Equality.create(),getIndividual(individuals[i]),getIndividual(individuals[i+1])));
        }
        public void visit(OWLDifferentIndividualsAxiom object) {
            OWLIndividual[] individuals=new OWLIndividual[object.getIndividuals().size()];
            object.getIndividuals().toArray(individuals);
            for (int i=0;i<individuals.length;i++)
                for (int j=i+1;j<individuals.length;j++)
                    m_positiveFacts.add(Atom.create(Inequality.create(),getIndividual(individuals[i]),getIndividual(individuals[j])));
        }
        public void visit(OWLClassAssertionAxiom object) {
            OWLClassExpression description=object.getClassExpression();
            if (description instanceof OWLClass) {
                AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)description).getIRI().toString());
                m_positiveFacts.add(Atom.create(atomicConcept,getIndividual(object.getIndividual())));
            }
            else if (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLClass) {
                AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)((OWLObjectComplementOf)description).getOperand()).getIRI().toString());
                m_negativeFacts.add(Atom.create(atomicConcept,getIndividual(object.getIndividual())));
            }
            else if (description instanceof OWLObjectHasSelf) {
                OWLObjectHasSelf self=(OWLObjectHasSelf)description;
                m_positiveFacts.add(getRoleAtom(self.getProperty(),getIndividual(object.getIndividual()),getIndividual(object.getIndividual())));
            }
            else if (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLObjectHasSelf) {
                OWLObjectHasSelf self=(OWLObjectHasSelf)(((OWLObjectComplementOf)description).getOperand());
                m_negativeFacts.add(getRoleAtom(self.getProperty(),getIndividual(object.getIndividual()),getIndividual(object.getIndividual())));
            }
            else
                throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLObjectPropertyAssertionAxiom object) {
            m_positiveFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),getIndividual(object.getObject())));
        }
        public void visit(OWLNegativeObjectPropertyAssertionAxiom object) {
            m_negativeFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),getIndividual(object.getObject())));
        }
        public void visit(OWLDataPropertyAssertionAxiom object) {
            Constant targetValue=(Constant)object.getObject().accept(m_dataRangeConverter);
            m_positiveFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),targetValue));
        }
        public void visit(OWLNegativeDataPropertyAssertionAxiom object) {
            Constant targetValue=(Constant)object.getObject().accept(m_dataRangeConverter);
            m_negativeFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),targetValue));
        }
    }

    protected class NormalizedRuleClausifier implements SWRLObjectVisitor {
        protected final DataRangeConverter m_dataRangeConverter;
        protected final Set<OWLObjectProperty> m_objectPropertiesUsedInAxioms;
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;
        protected final Set<Variable> m_DLSafeVars;
        public final List<List<Atom>> m_heads=new ArrayList<List<Atom>>(); // contains the clause atoms for all heads after clausification
        public final List<List<Atom>> m_bodies=new ArrayList<List<Atom>>(); // contains the clause atoms for all bodies after clausification
        public final List<Set<Variable>> m_DLSafeVarSets=new ArrayList<Set<Variable>>(); // contains the variables to which the DL Safe restriction can be applied (no literal variables) after clausification
        protected final Set<Atom> m_positiveFacts;
        protected final Set<Atom> m_negativeFacts;
        protected Variable m_lastVariable;
        protected Atom m_lastAtom;
        protected final Set<SWRLVariable> m_headVars=new HashSet<SWRLVariable>();
        protected final Set<SWRLVariable> m_bodyVars=new HashSet<SWRLVariable>();
        protected final Set<SWRLVariable> m_varsOfLastAtom=new HashSet<SWRLVariable>();
        protected boolean m_containsIndividuals=false;
        protected boolean m_containsVariables=false;
        public final Set<OWLObjectProperty> m_graphRoles=new HashSet<OWLObjectProperty>();
        protected final Map<OWLObjectProperty,Set<SWRLRule>> unknownRoleInRule=new HashMap<OWLObjectProperty,Set<SWRLRule>>();
        protected int freshVarIndex=0;
        protected final Map<OWLNamedIndividual,Variable> constantToVar=new HashMap<OWLNamedIndividual,Variable>();

        public NormalizedRuleClausifier(Set<Atom> positiveFacts,Set<Atom> negativeFacts,Collection<DescriptionGraph> dGraphs,Set<OWLObjectProperty> objectPropertiesUsedInAxioms,DataRangeConverter dataRangeConverter) {
            m_dataRangeConverter=dataRangeConverter;
            m_headAtoms=new ArrayList<Atom>();
            m_bodyAtoms=new ArrayList<Atom>();
            m_DLSafeVars=new HashSet<Variable>();
            m_positiveFacts=positiveFacts;
            m_negativeFacts=negativeFacts;
            m_objectPropertiesUsedInAxioms=objectPropertiesUsedInAxioms;
            OWLDataFactory df=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            for (DescriptionGraph dGraph : dGraphs) {
                for (int i=0;i<dGraph.getNumberOfEdges();i++) {
                    m_graphRoles.add(df.getOWLObjectProperty(IRI.create(dGraph.getEdge(i).getAtomicRole().getIRI())));
                }
            }
        }

        protected void checkStrictSeparation(SWRLRule rule) {
            boolean hasGraphRoles=false;
            boolean hasNonGraphRoles=false;
            Set<OWLObjectProperty> unknownRoles=new HashSet<OWLObjectProperty>();
            for (OWLObjectProperty op : rule.getObjectPropertiesInSignature()) {
                if (m_objectPropertiesUsedInAxioms.contains(op))
                    hasNonGraphRoles=true;
                if (m_graphRoles.contains(op))
                    hasGraphRoles=true;
                if (!m_objectPropertiesUsedInAxioms.contains(op) && !m_graphRoles.contains(op))
                    unknownRoles.add(op);
            }
            if (hasGraphRoles && hasNonGraphRoles) {
                throw new IllegalArgumentException("Internal error: It seems rules mix graph and non-graph rules, which violates the strict separation condition. ");
            }
            if (hasGraphRoles) {
                // if there is one graph role, then also all unknown roles have to be graph roles
                m_graphRoles.addAll(rule.getObjectPropertiesInSignature());
                for (OWLObjectProperty op : unknownRoles) {
                    if (unknownRoleInRule.containsKey(op)) {
                        for (SWRLRule r : unknownRoleInRule.get(op)) {
                            checkStrictSeparation(r);
                        }
                    }
                }
                unknownRoles.removeAll(rule.getObjectPropertiesInSignature());
            }
            else if (!hasGraphRoles && !unknownRoles.isEmpty()) {
                for (OWLObjectProperty op : unknownRoles) {
                    if (unknownRoleInRule.containsKey(op)) {
                        Set<SWRLRule> rules=unknownRoleInRule.get(op);
                        rules.add(rule);
                    }
                    else {
                        Set<SWRLRule> rules=new HashSet<SWRLRule>();
                        rules.add(rule);
                        unknownRoleInRule.put(op,rules);
                    }
                }
            }
        }

        public void visit(SWRLRule rule) {
            checkStrictSeparation(rule);
            freshVarIndex=0;
            for (SWRLAtom atom : rule.getBody()) {
                atom.accept(this);
                m_bodyAtoms.add(m_lastAtom);
                m_bodyVars.addAll(m_varsOfLastAtom);
                m_varsOfLastAtom.clear();
            }
            for (SWRLAtom atom : rule.getHead()) {
                atom.accept(this);
                m_headAtoms.add(m_lastAtom);
                m_headVars.addAll(m_varsOfLastAtom);
                m_varsOfLastAtom.clear();
            }
            // Only variables that occur in the antecedent of a rule may occur in the consequent (safety condition)
            if (!m_bodyVars.containsAll(m_headVars)) {
                throw new IllegalArgumentException("Error: The rule "+rule+" contains head variables that do not occur in the body, which violates the safety restrictions. ");
            }
            else if (m_headAtoms.isEmpty() && !m_containsVariables) {
                m_negativeFacts.add(m_bodyAtoms.get(0));
            }
            else {
                m_heads.add(new ArrayList<Atom>(m_headAtoms));
                m_bodies.add(new ArrayList<Atom>(m_bodyAtoms));
                m_DLSafeVarSets.add(new HashSet<Variable>(m_DLSafeVars));
                m_headAtoms.clear();
                m_bodyAtoms.clear();
                m_DLSafeVars.clear();
                constantToVar.clear();
            }
        }

        public void visit(SWRLClassAtom atom) {
            if (atom.getPredicate().isAnonymous()) {
                throw new IllegalStateException("Internal error: SWRL rule class atoms should be normalized to contain only named classes, but this class atom has a complex concept: "+atom.getPredicate());
            }
            atom.getArgument().accept(this);
            Variable var=m_lastVariable;
            m_DLSafeVars.add(var);
            m_lastAtom=Atom.create(AtomicConcept.create(atom.getPredicate().asOWLClass().getIRI().toString()),m_lastVariable);
        }
        public void visit(SWRLDataRangeAtom atom) {
            if (!(atom.getPredicate() instanceof OWLDatatype)) {
                throw new IllegalStateException("Internal error: SWRL rule data range atoms should be normalized to contain only datatypes, but this atom has a (complex) data range: "+atom.getPredicate());
            }
            LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(atom.getPredicate().asOWLDatatype());
//            OWLDatatype dt=atom.getPredicate().asOWLDatatype();
            atom.getArgument().accept(this); 
            m_lastAtom=Atom.create((DLPredicate)literalRange,m_lastVariable);
        }
        public void visit(SWRLObjectPropertyAtom atom) {
            if (atom.getPredicate().isAnonymous()) {
                throw new IllegalStateException("Internal error: object properties in SWRL rule object property atoms should be normalized to contain only named properties, but this atom has an (anonymous) object property expression: "+atom.getPredicate());
            }
            atom.getFirstArgument().accept(this);
            Variable var1=m_lastVariable;
            m_DLSafeVars.add(var1);
            atom.getSecondArgument().accept(this);
            Variable var2=m_lastVariable;
            m_DLSafeVars.add(var2);
            m_lastAtom=getRoleAtom(atom.getPredicate().asOWLObjectProperty(),var1,var2);
        }
        public void visit(SWRLDataPropertyAtom atom) {
            atom.getFirstArgument().accept(this);
            Variable var1=m_lastVariable;
            m_DLSafeVars.add(var1);
            atom.getSecondArgument().accept(this);
            Variable var2=m_lastVariable;
            m_lastAtom=getRoleAtom(atom.getPredicate().asOWLDataProperty(),var1,var2);
        }
        public void visit(SWRLBuiltInAtom node) {
            throw new UnsupportedOperationException("Rules with SWRL built-in atoms are not yet supported. ");
        }
        public void visit(SWRLVariable node) {
            m_lastVariable=Variable.create(node.getIRI().toString());
            m_varsOfLastAtom.add(node);
            m_containsVariables=true;
        }
        public void visit(SWRLIndividualArgument atom) {
            throw new IllegalStateException("Internal error: individual arguments in rules should have been normalized away.");
        }
        public void visit(SWRLLiteralArgument arg) {
            throw new IllegalStateException("Internal error: Data constants in rules should have been normalized away. ");
        }
        public void visit(SWRLSameIndividualAtom atom) {
            atom.getFirstArgument().accept(this);
            Variable var1=m_lastVariable;
            atom.getSecondArgument().accept(this);
            Variable var2=m_lastVariable;
            m_lastAtom=Atom.create(Equality.INSTANCE,var1,var2);
        }
        public void visit(SWRLDifferentIndividualsAtom atom) {
            atom.getFirstArgument().accept(this);
            Variable var1=m_lastVariable;
            atom.getSecondArgument().accept(this);
            Variable var2=m_lastVariable;
            m_lastAtom=Atom.create(Inequality.INSTANCE,var1,var2);
        }
    }
}
