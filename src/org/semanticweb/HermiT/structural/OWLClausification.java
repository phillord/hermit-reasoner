// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DataValueEnumeration;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.NodeIDLessEqualThan;
import org.semanticweb.HermiT.model.NodeIDsAscendingOrEqual;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLClassExpression;
import org.semanticweb.owl.model.OWLClassExpressionVisitor;
import org.semanticweb.owl.model.OWLDataAllValuesFrom;
import org.semanticweb.owl.model.OWLDataComplementOf;
import org.semanticweb.owl.model.OWLDataExactCardinality;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataHasValue;
import org.semanticweb.owl.model.OWLDataIntersectionOf;
import org.semanticweb.owl.model.OWLDataMaxCardinality;
import org.semanticweb.owl.model.OWLDataMinCardinality;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataSomeValuesFrom;
import org.semanticweb.owl.model.OWLDataUnionOf;
import org.semanticweb.owl.model.OWLDataVisitor;
import org.semanticweb.owl.model.OWLDataVisitorEx;
import org.semanticweb.owl.model.OWLDatatype;
import org.semanticweb.owl.model.OWLDatatypeRestriction;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLFacetRestriction;
import org.semanticweb.owl.model.OWLHasKeyAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLLiteral;
import org.semanticweb.owl.model.OWLNamedIndividual;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectAllValuesFrom;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinality;
import org.semanticweb.owl.model.OWLObjectHasSelf;
import org.semanticweb.owl.model.OWLObjectHasValue;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinality;
import org.semanticweb.owl.model.OWLObjectMinCardinality;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSameIndividualAxiom;
import org.semanticweb.owl.model.OWLStringLiteral;
import org.semanticweb.owl.model.OWLTypedLiteral;
import org.semanticweb.owl.util.OWLAxiomVisitorAdapter;

import rationals.Automaton;

public class OWLClausification {
    protected static final Variable X=Variable.create("X");
    protected static final Variable Y=Variable.create("Y");
    protected static final Variable Z=Variable.create("Z");

    protected final Configuration m_configuration;
    
    public OWLClausification(Configuration configuration) {
        m_configuration=configuration;
    }
    public DLOntology clausify(OWLOntologyManager ontologyManager,OWLOntology ontology,Collection<DescriptionGraph> descriptionGraphs) {
        Set<OWLOntology> importClosure=new HashSet<OWLOntology>();
        List<OWLOntology> toProcess=new ArrayList<OWLOntology>();
        toProcess.add(ontology);
        while (!toProcess.isEmpty()) {
            OWLOntology anOntology=toProcess.remove(toProcess.size()-1);
            if (importClosure.add(anOntology))
                toProcess.addAll(anOntology.getImports(ontologyManager));
        }
        return clausifyImportClosure(ontologyManager.getOWLDataFactory(),ontology.getOntologyID().getDefaultDocumentIRI() == null ? "urn:hermit:kb" : ontology.getOntologyID().getDefaultDocumentIRI().toString(),importClosure,descriptionGraphs);
    }
    public DLOntology clausifyImportClosure(OWLDataFactory factory,String ontologyURI,Collection<OWLOntology> importClosure,Collection<DescriptionGraph> descriptionGraphs) {
        OWLAxioms axioms=new OWLAxioms();
        OWLNormalization normalization=new OWLNormalization(factory,axioms);
        for (OWLOntology ontology : importClosure)
            normalization.processOntology(m_configuration,ontology);
        BuiltInPropertyManager builtInPropertyManager=new BuiltInPropertyManager(factory);
        builtInPropertyManager.axiomatizeBuiltInPropertiesAsNeeded(axioms);
        ObjectPropertyInclusionManager objectPropertyInclusionManager=new ObjectPropertyInclusionManager(factory);
        objectPropertyInclusionManager.prepareTransformation(axioms);
        Map<OWLObjectPropertyExpression, Automaton> automataOfComplexRoles = objectPropertyInclusionManager.rewriteAxioms(axioms);
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        /**
         * @gstoil
         */
        DLOntology dlOntology = clausify(factory,ontologyURI,axioms,descriptionGraphs);
        dlOntology.setAutomata( automataOfComplexRoles );
        return dlOntology;
    }
    public DLOntology clausify(OWLDataFactory factory,String ontologyURI,OWLAxioms axioms,Collection<DescriptionGraph> descriptionGraphs) {
        OWLAxiomsExpressivity axiomsExpressivity=new OWLAxiomsExpressivity(axioms);
        return clausify(factory,ontologyURI,axioms,axiomsExpressivity,descriptionGraphs);
    }
    public DLOntology clausify(OWLDataFactory factory,String ontologyURI,OWLAxioms axioms,OWLAxiomsExpressivity axiomsExpressivity,Collection<DescriptionGraph> descriptionGraphs) {
        Set<DLClause> dlClauses=new LinkedHashSet<DLClause>();
        Set<Atom> positiveFacts=new HashSet<Atom>();
        Set<Atom> negativeFacts=new HashSet<Atom>();
        for (OWLObjectPropertyExpression[] inclusion : axioms.m_simpleObjectPropertyInclusions) {
            Atom subRoleAtom=getRoleAtom(inclusion[0],X,Y);
            Atom superRoleAtom=getRoleAtom(inclusion[1],X,Y);
            DLClause dlClause=DLClause.create(new Atom[] { superRoleAtom },new Atom[] { subRoleAtom });
            dlClauses.add(dlClause);
        }
        for (OWLDataPropertyExpression[] inclusion : axioms.m_dataPropertyInclusions) {
            Atom subProp=getRoleAtom(inclusion[0],X,Y);
            Atom superProp=getRoleAtom(inclusion[1],X,Y);
            DLClause dlClause=DLClause.create(new Atom[] { superProp },new Atom[] { subProp });
            dlClauses.add(dlClause);
        }
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_asymmetricObjectProperties) {
            Atom roleAtom=getRoleAtom(objectPropertyExpression,X,Y);
            Atom inverseRoleAtom=getRoleAtom(objectPropertyExpression,Y,X);
            DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { roleAtom,inverseRoleAtom });
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_reflexiveObjectProperties) {
            Atom roleAtom=getRoleAtom(objectPropertyExpression,X,X);
            DLClause dlClause=DLClause.create(new Atom[] { roleAtom },new Atom[] {});
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_irreflexiveObjectProperties) {
            Atom roleAtom=getRoleAtom(objectPropertyExpression,X,X);
            DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { roleAtom });
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression[] properties : axioms.m_disjointObjectProperties)
            for (int i=0;i<properties.length;i++)
                for (int j=i+1;j<properties.length;j++) {
                    Atom atom_i=getRoleAtom(properties[i],X,Y);
                    Atom atom_j=getRoleAtom(properties[j],X,Y);
                    DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { atom_i,atom_j });
                    dlClauses.add(dlClause.getSafeVersion());
                }
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_unsatisfiableObjectProperties) {
            Atom roleAtom=getRoleAtom(objectPropertyExpression,X,Y);
            dlClauses.add(DLClause.create(new Atom[] {},new Atom[] { roleAtom }).getSafeVersion());
        }
        if (axioms.m_dataPropertyInclusions.contains(factory.getOWLDataProperty(URI.create(AtomicRole.BOTTOM_DATA_ROLE.getIRI())))) {
            Atom bodyAtom=Atom.create(AtomicRole.BOTTOM_DATA_ROLE,X,Y);
            dlClauses.add(DLClause.create(new Atom[] {},new Atom[] { bodyAtom }).getSafeVersion());
        }
        for (OWLDataPropertyExpression[] properties : axioms.m_disjointDataProperties)
            for (int i=0;i<properties.length;i++)
                for (int j=i+1;j<properties.length;j++) {
                    Atom atom_i=getRoleAtom(properties[i],X,Y);
                    Atom atom_j=getRoleAtom(properties[j],X,Z);
                    Atom atom_ij=Atom.create(Inequality.create(),Y,Z);
                    DLClause dlClause=DLClause.create(new Atom[] { atom_ij },new Atom[] { atom_i,atom_j });
                    dlClauses.add(dlClause.getSafeVersion());
                }
        for (OWLDataPropertyExpression dataPropertyExpression : axioms.m_unsatisfiableDataProperties) {
            Atom roleAtom=getRoleAtom(dataPropertyExpression,X,Y);
            dlClauses.add(DLClause.create(new Atom[] {},new Atom[] { roleAtom }).getSafeVersion());
        }
        DataRangeConverter dataRangeConverter=new DataRangeConverter(m_configuration.warningMonitor,axioms.m_dataRangeDefinitions,m_configuration.ignoreUnsupportedDatatypes);
        NormalizedAxiomClausifier clausifier=new NormalizedAxiomClausifier(dataRangeConverter,positiveFacts,factory);
        for (OWLClassExpression[] inclusion : axioms.m_conceptInclusions) {
            for (OWLClassExpression description : inclusion)
                description.accept(clausifier);
            DLClause dlClause=clausifier.getDLClause();
            dlClauses.add(dlClause.getSafeVersion());
        }
        NormalizedDataRangeAxiomClausifier drClausifier=new NormalizedDataRangeAxiomClausifier(dataRangeConverter,factory,axioms.m_dataRangeDefinitions);
        for (OWLDataRange[] inclusion : axioms.m_dataRangeInclusions) {
            for (OWLDataRange description : inclusion)
                description.accept(drClausifier);
            DLClause dlClause=drClausifier.getDLClause();
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
        Set<DLOntology.ComplexObjectRoleInclusion> complexObjectRoleInclusions=new HashSet<DLOntology.ComplexObjectRoleInclusion>();
        Set<AtomicRole> objectRoles=new HashSet<AtomicRole>();
        Set<AtomicRole> dataRoles=new HashSet<AtomicRole>();
        for (OWLClass c : axioms.m_classes)
            atomicConcepts.add(AtomicConcept.create(c.getURI().toString()));
        Set<Individual> hermitIndividuals=new HashSet<Individual>();
        for (OWLNamedIndividual i : axioms.m_individuals) {
            Individual ind=Individual.create(i.getURI().toString());
            hermitIndividuals.add(ind);
            // all named individuals are tagged with a concept, so that keys are
            // only applied to them
            if (!axioms.m_hasKeys.isEmpty())
                positiveFacts.add(Atom.create(AtomicConcept.INTERNAL_NAMED,ind));
        }
        for (OWLObjectProperty objectProperty : axioms.m_objectProperties)
            objectRoles.add(AtomicRole.create(objectProperty.getURI().toString()));
        for (OWLAxioms.ComplexObjectPropertyInclusion inclusion : axioms.m_complexObjectPropertyInclusions) {
            Role[] subRoles=new Role[inclusion.m_subObjectProperties.length];
            for (int index=inclusion.m_subObjectProperties.length-1;index>=0;--index)
                subRoles[index]=getRole(inclusion.m_subObjectProperties[index]);
            Role superRole=getRole(inclusion.m_superObjectProperties);
            complexObjectRoleInclusions.add(new DLOntology.ComplexObjectRoleInclusion(subRoles,superRole));
        }
        for (OWLDataProperty objectProperty : axioms.m_dataProperties)
            dataRoles.add(AtomicRole.create(objectProperty.getURI().toString()));
        return new DLOntology(ontologyURI,dlClauses,positiveFacts,negativeFacts,atomicConcepts,complexObjectRoleInclusions,objectRoles,dataRoles,hermitIndividuals,axiomsExpressivity.m_hasInverseRoles,axiomsExpressivity.m_hasAtMostRestrictions,axiomsExpressivity.m_hasNominals,axiomsExpressivity.m_hasDatatypes);
    }
    protected DLClause clausifyKey(OWLHasKeyAxiom object) {
        List<Atom> headAtoms=new ArrayList<Atom>();
        List<Atom> bodyAtoms=new ArrayList<Atom>();
        // we have two named individuals (corresponding to X1 and X2) that
        // might have to be equated
        Variable X2=Variable.create("X2");
        headAtoms.add(Atom.create(Equality.INSTANCE,X,X2));
        // keys only work on datatypes and named individuals
        bodyAtoms.add(Atom.create(AtomicConcept.INTERNAL_NAMED,X));
        bodyAtoms.add(Atom.create(AtomicConcept.INTERNAL_NAMED,X2));
        // the concept expression of a hasKey statement is either a concept
        // name or a negated concept name after normalization
        OWLClassExpression description=object.getClassExpression();
        if (description instanceof OWLClass) {
            OWLClass owlClass=(OWLClass)description;
            if (!owlClass.isOWLThing()) {
                bodyAtoms.add(Atom.create(AtomicConcept.create(owlClass.getURI().toString()),X));
                bodyAtoms.add(Atom.create(AtomicConcept.create(owlClass.getURI().toString()),X2));
            }
        } else if (description instanceof OWLObjectComplementOf) {
            OWLClassExpression internal=((OWLObjectComplementOf)description).getOperand();
            if (internal instanceof OWLClass) {
                OWLClass owlClass=(OWLClass)internal;
                bodyAtoms.add(Atom.create(AtomicConcept.create(owlClass.getURI().toString()),X));
                bodyAtoms.add(Atom.create(AtomicConcept.create(owlClass.getURI().toString()),X2));
            } else {
                throw new IllegalStateException("Internal error: invalid normal form.");
            }
        } else {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        int y_ind=0;
        // object properties always go to the body
        for (OWLObjectPropertyExpression p : object.getObjectPropertyExpressions()) {
            Variable y;
            y=Variable.create("Y"+y_ind);
            y_ind++;
            bodyAtoms.add(getRoleAtom(p,X,y));
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
            bodyAtoms.add(getRoleAtom(d,X,y));
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
        DLClause clause=DLClause.createEx(true,hAtoms,bAtoms);
        return clause;
    }
    protected static LiteralConcept getLiteralConcept(OWLClassExpression description) {
        if (description instanceof OWLClass) {
            return AtomicConcept.create(((OWLClass)description).getURI().toString());
        }
        else if (description instanceof OWLObjectComplementOf) {
            OWLClassExpression internal=((OWLObjectComplementOf)description).getOperand();
            if (!(internal instanceof OWLClass))
                throw new IllegalStateException("Internal error: invalid normal form.");
            return AtomicConcept.create(((OWLClass)internal).getURI().toString()).getNegation();
        }
        else
            throw new IllegalStateException("Internal error: invalid normal form.");
    }
    protected static Role getRole(OWLObjectPropertyExpression objectPropertyExpression) {
        objectPropertyExpression=objectPropertyExpression.getSimplified();
        if (objectPropertyExpression instanceof OWLObjectProperty)
            return AtomicRole.create(((OWLObjectProperty)objectPropertyExpression).getURI().toString());
        else if (objectPropertyExpression instanceof OWLObjectPropertyInverse) {
            OWLObjectPropertyExpression internal=((OWLObjectPropertyInverse)objectPropertyExpression).getInverse();
            if (!(internal instanceof OWLObjectProperty))
                throw new IllegalStateException("Internal error: invalid normal form.");
            return InverseRole.create(AtomicRole.create(((OWLObjectProperty)internal).getURI().toString()));
        }
        else
            throw new IllegalStateException("Internal error: invalid normal form.");
    }
    protected static AtomicRole getAtomicRole(OWLDataPropertyExpression dataPropertyExpression) {
        return AtomicRole.create(((OWLDataProperty)dataPropertyExpression).getURI().toString());
    }
    protected static Atom getRoleAtom(OWLObjectPropertyExpression objectProperty,Term first,Term second) {
        objectProperty=objectProperty.getSimplified();
        if (objectProperty instanceof OWLObjectProperty) {
            AtomicRole role=AtomicRole.create(((OWLObjectProperty)objectProperty).getURI().toString());
            return Atom.create(role,first,second);
        }
        else if (objectProperty instanceof OWLObjectPropertyInverse) {
            OWLObjectProperty internalObjectProperty=(OWLObjectProperty)((OWLObjectPropertyInverse)objectProperty).getInverse();
            AtomicRole role=AtomicRole.create(internalObjectProperty.getURI().toString());
            return Atom.create(role,second,first);
        }
        else
            throw new IllegalStateException("Internal error: unsupported type of object property!");
    }
    protected static Atom getRoleAtom(OWLDataPropertyExpression dataProperty,Term first,Term second) {
        if (dataProperty instanceof OWLDataProperty) {
            AtomicRole property=AtomicRole.create(((OWLDataProperty)dataProperty).getURI().toString());
            return Atom.create(property,first,second);
        }
        else
            throw new IllegalStateException("Internal error: unsupported type of data property!");
    }
    protected static Individual getIndividual(OWLIndividual individual) {
        if (individual.isAnonymous()) {
            return Individual.create("internal:anonymousIndividual#" + individual.asAnonymousIndividual().getID().toString());
        } else {
            return Individual.create(individual.asNamedIndividual().getURI().toString());
        }
    }

    protected static class NormalizedAxiomClausifier implements OWLClassExpressionVisitor {
        protected final DataRangeConverter m_dataRangeConverter;
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;
        protected final Set<Atom> m_positiveFacts;
        protected final OWLDataFactory m_factory;
        protected int m_yIndex;

        public NormalizedAxiomClausifier(DataRangeConverter dataRangeConverter,Set<Atom> positiveFacts,OWLDataFactory factory) {
            m_dataRangeConverter=dataRangeConverter;
            m_headAtoms=new ArrayList<Atom>();
            m_bodyAtoms=new ArrayList<Atom>();
            m_positiveFacts=positiveFacts;
            m_factory=factory;
        }
        protected DLClause getDLClause() {
            Atom[] headAtoms=new Atom[m_headAtoms.size()];
            m_headAtoms.toArray(headAtoms);
            Arrays.sort(headAtoms,HeadComparator.INSTANCE);
            Atom[] bodyAtoms=new Atom[m_bodyAtoms.size()];
            m_bodyAtoms.toArray(bodyAtoms);
            DLClause dlClause=DLClause.create(headAtoms,bodyAtoms);
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
        protected AtomicConcept getConceptForNominal(OWLIndividual individual) {
            if (individual.isAnonymous()) {
                throw new IllegalStateException("Nominals must be named individuals and cannot be anonymous. ");
            }
            AtomicConcept result=AtomicConcept.create("internal:nom#"+individual.asNamedIndividual().getURI().toString());
            m_positiveFacts.add(Atom.create(result,getIndividual(individual)));
            return result;
        }
        
        // Various types of descriptions
        
        public void visit(OWLClass object) {
            m_headAtoms.add(Atom.create(AtomicConcept.create(object.getURI().toString()),X));
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
             * @gstoil modification. This situation is now possible with automata. Since the translation says that
             * for each final state f add inclusion f -> C. If C is a nominal and comes from a concept with negative
             * polarity then subsumption is actually f-> \neg {o} which was not a valid HermiT normal form. 
             */
            else if (description instanceof OWLObjectOneOf && ((OWLObjectOneOf)description).getIndividuals().size()==1) {
                OWLIndividual individual=((OWLObjectOneOf)description).getIndividuals().iterator().next();
                m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),X));
            }
            else if (!(description instanceof OWLClass))
                throw new IllegalStateException("Internal error: invalid normal form.");
            else
                m_bodyAtoms.add(Atom.create(AtomicConcept.create(((OWLClass)description).getURI().toString()),X));
        }
        public void visit(OWLObjectOneOf object) {
            for (OWLIndividual individual : object.getIndividuals()) {
                Variable y=nextY();
                AtomicConcept conceptForNominal=getConceptForNominal(individual);
                m_headAtoms.add(Atom.create(Equality.INSTANCE,X,y));
                m_bodyAtoms.add(Atom.create(conceptForNominal,y));
            }
        }
        public void visit(OWLObjectSomeValuesFrom object) {
            OWLClassExpression filler=object.getFiller();
            if (filler instanceof OWLObjectOneOf) {
                for (OWLIndividual individual : ((OWLObjectOneOf)filler).getIndividuals()) {
                    Variable y=nextY();
                    m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),y));
                    m_headAtoms.add(getRoleAtom(object.getProperty(),X,y));
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
                AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)filler).getURI().toString());
                if (!atomicConcept.isAlwaysFalse())
                    m_headAtoms.add(Atom.create(atomicConcept,y));
            }
            else if (filler instanceof OWLObjectOneOf) {
                for (OWLIndividual individual : ((OWLObjectOneOf)filler).getIndividuals()) {
                    Variable yInd=nextY();
                    m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),yInd));
                    m_headAtoms.add(Atom.create(Equality.INSTANCE,y,yInd));
                }
            }
            else if (filler instanceof OWLObjectComplementOf) {
                OWLClassExpression operand=((OWLObjectComplementOf)filler).getOperand();
                if (operand instanceof OWLClass) {
                    AtomicConcept internalAtomicConcept=AtomicConcept.create(((OWLClass)operand).getURI().toString());
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
                atomicConcept=AtomicConcept.create(((OWLClass)filler).getURI().toString());
                if (atomicConcept.isAlwaysTrue())
                    atomicConcept=null;
            }
            else if (filler instanceof OWLObjectComplementOf) {
                OWLClassExpression internal=((OWLObjectComplementOf)filler).getOperand();
                if (!(internal instanceof OWLClass))
                    throw new IllegalStateException("Internal error: Invalid ontology normal form.");
                isPositive=false;
                atomicConcept=AtomicConcept.create(((OWLClass)internal).getURI().toString());
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
            AtomicRole atomicRole=getAtomicRole(object.getProperty());
            LiteralConcept literalConcept=m_dataRangeConverter.convertDataRange(object.getFiller());
            AtLeastConcept atLeastConcept=AtLeastConcept.create(1,atomicRole,literalConcept);
            if (!atLeastConcept.isAlwaysFalse())
                m_headAtoms.add(Atom.create(atLeastConcept,X));
        }
        public void visit(OWLDataAllValuesFrom object) {
            Variable y=nextY();
            m_bodyAtoms.add(getRoleAtom(object.getProperty(),X,y));
            LiteralConcept literalConcept=m_dataRangeConverter.convertDataRange(object.getFiller());
            if (literalConcept instanceof AtomicNegationConcept) {
                AtomicConcept negatedConcept=((AtomicNegationConcept)literalConcept).getNegatedAtomicConcept();
                if (!negatedConcept.isAlwaysTrue())
                    m_bodyAtoms.add(Atom.create(negatedConcept,y));
            }
            else {
                if (!literalConcept.isAlwaysFalse())
                    m_headAtoms.add(Atom.create((DLPredicate)literalConcept,y));
            }
        }
        public void visit(OWLDataHasValue object) {
            throw new IllegalStateException("Internal error: Invalid normal form.");
        }
        public void visit(OWLDataMinCardinality object) {
            AtomicRole atomicRole=getAtomicRole(object.getProperty());
            LiteralConcept literalConcept=m_dataRangeConverter.convertDataRange(object.getFiller());
            AtLeastConcept atLeastConcept=AtLeastConcept.create(object.getCardinality(),atomicRole,literalConcept);
            if (!atLeastConcept.isAlwaysFalse())
                m_headAtoms.add(Atom.create(atLeastConcept,X));
        }
        public void visit(OWLDataMaxCardinality object) {
            int number=object.getCardinality();
            LiteralConcept negatedDataRange=m_dataRangeConverter.convertDataRange(object.getFiller()).getNegation();
            ensureYNotZero();
            Variable[] yVars=new Variable[number+1];
            for (int i=0;i<yVars.length;i++) {
                yVars[i]=nextY();
                m_bodyAtoms.add(getRoleAtom(object.getProperty(),X,yVars[i]));
                if (negatedDataRange instanceof AtomicNegationConcept) {
                    AtomicConcept negatedConcept=((AtomicNegationConcept)negatedDataRange).getNegatedAtomicConcept();
                    if (!negatedConcept.isAlwaysTrue())
                        m_bodyAtoms.add(Atom.create(negatedConcept,yVars[i]));
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
        protected final Map<OWLDataRange,OWLDatatype> m_dataRangeDefs;
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;
        protected final OWLDataFactory m_factory;
        protected int m_yIndex;

        public NormalizedDataRangeAxiomClausifier(DataRangeConverter dataRangeConverter,OWLDataFactory factory,Map<OWLDataRange,OWLDatatype> dataRangeDefinitions) {
            m_dataRangeConverter=dataRangeConverter;
            m_dataRangeDefs=dataRangeDefinitions;
            m_headAtoms=new ArrayList<Atom>();
            m_bodyAtoms=new ArrayList<Atom>();
            m_factory=factory;
        }
        protected DLClause getDLClause() {
            Atom[] headAtoms=new Atom[m_headAtoms.size()];
            m_headAtoms.toArray(headAtoms);
            Arrays.sort(headAtoms,HeadComparator.INSTANCE);
            Atom[] bodyAtoms=new Atom[m_bodyAtoms.size()];
            m_bodyAtoms.toArray(bodyAtoms);
            DLClause dlClause=DLClause.create(headAtoms,bodyAtoms);
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
            LiteralConcept literalConcept=m_dataRangeConverter.convertDataRange(dt);
            if (!literalConcept.isAlwaysFalse())
                m_headAtoms.add(Atom.create((DLPredicate)literalConcept,X));
        }
        public void visit(OWLDataIntersectionOf dr) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLDataUnionOf dr) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLDataComplementOf dr) {
            OWLDataRange description=dr.getDataRange();
            if (description instanceof OWLDatatype && m_dataRangeDefs.containsValue((OWLDatatype)description)) {
                m_bodyAtoms.add(Atom.create(AtomicConcept.create(((OWLDatatype)description).getURI().toString()),X));
            } else {
                LiteralConcept literalConcept=m_dataRangeConverter.convertDataRange(dr);
                if (literalConcept instanceof AtomicNegationConcept) {
                    AtomicConcept negatedConcept=((AtomicNegationConcept)literalConcept).getNegatedAtomicConcept();
                    if (!negatedConcept.isAlwaysTrue())
                        m_bodyAtoms.add(Atom.create(negatedConcept,X));
                } else {
                    if (!literalConcept.isAlwaysFalse())
                        m_headAtoms.add(Atom.create((DLPredicate)literalConcept,X));
                }
            } 
        }
        public void visit(OWLDataOneOf object) {
            LiteralConcept literalConcept=m_dataRangeConverter.convertDataRange(object);
            if (literalConcept instanceof AtomicNegationConcept) {
                AtomicConcept negatedConcept=((AtomicNegationConcept)literalConcept).getNegatedAtomicConcept();
                if (!negatedConcept.isAlwaysTrue())
                    m_bodyAtoms.add(Atom.create(negatedConcept,X));
            } else {
                if (!literalConcept.isAlwaysFalse())
                    m_headAtoms.add(Atom.create((DLPredicate)literalConcept,X));
            }
        }
        public void visit(OWLTypedLiteral node) {
            throw new IllegalStateException("Internal error: Invalid normal form. ");
        }
        public void visit(OWLStringLiteral node) {
            throw new IllegalStateException("Internal error: Invalid normal form. ");
        }
        public void visit(OWLFacetRestriction node) {
            throw new IllegalStateException("Internal error: Invalid normal form. ");
        }
        public void visit(OWLDatatypeRestriction node) {
            LiteralConcept literalConcept=m_dataRangeConverter.convertDataRange(node);
            if (literalConcept instanceof AtomicNegationConcept) {
                AtomicConcept negatedConcept=((AtomicNegationConcept)literalConcept).getNegatedAtomicConcept();
                if (!negatedConcept.isAlwaysTrue())
                    m_bodyAtoms.add(Atom.create(negatedConcept,X));
            } else {
                if (!literalConcept.isAlwaysFalse())
                    m_headAtoms.add(Atom.create((DLPredicate)literalConcept,X));
            }
        }
    }

    protected static class DataRangeConverter implements OWLDataVisitorEx<Object> {
        protected final Configuration.WarningMonitor m_warningMonitor;
        protected final boolean m_ignoreUnsupportedDatatypes;
        protected final Map<OWLDataRange,OWLDatatype> m_datatypeDefs; // newly introduced definitions for conjunctions or disjunctions of data ranges

        public DataRangeConverter(Configuration.WarningMonitor warningMonitor,Map<OWLDataRange,OWLDatatype> datatypeDefs,boolean ignoreUnsupportedDatatypes) {
            m_warningMonitor=warningMonitor;
            m_datatypeDefs=datatypeDefs;
            m_ignoreUnsupportedDatatypes=ignoreUnsupportedDatatypes;
        }
        public LiteralConcept convertDataRange(OWLDataRange dataRange) {
            return (LiteralConcept)dataRange.accept(this);
        }
        public Object visit(OWLDatatype object) {
            String datatypeURI=object.getURI().toString();
            if (AtomicConcept.RDFS_LITERAL.getIRI().equals(datatypeURI))
                return AtomicConcept.RDFS_LITERAL;
            DatatypeRestriction datatype=DatatypeRestriction.create(datatypeURI,DatatypeRestriction.NO_FACET_URIs,DatatypeRestriction.NO_FACET_VALUES);
            try {
                DatatypeRegistry.validateDatatypeRestriction(datatype);
                return datatype;
            } catch (UnsupportedDatatypeException e) {
                // maybe it is a newly introduced datatype
                if (m_datatypeDefs.containsValue(object)) {
                    return AtomicConcept.create(object.getURI().toString());
                } else if (m_ignoreUnsupportedDatatypes) {
                    if (m_warningMonitor!=null)
                        m_warningMonitor.warning("Ignoring unsupprted datatype '"+object.getURI().toString()+"'.");
                    return AtomicConcept.create(object.getURI().toString());
                } else {
                    throw new IllegalArgumentException("A definition is missing for the custom datatype " + object.toString());
                }
            }
        }
        public Object visit(OWLDataComplementOf object) {
            return convertDataRange(object.getDataRange()).getNegation();
        }
        public Object visit(OWLDataOneOf object) {
            Set<Object> dataValues=new HashSet<Object>();
            for (OWLLiteral constant : object.getValues())
                dataValues.add(constant.accept(this));
            return DataValueEnumeration.create(dataValues.toArray());
        }
        public Object visit(OWLDatatypeRestriction object) {
            if (!(object.getDatatype().isOWLDatatype()))
                throw new IllegalArgumentException("Datatype restrictions are supported only on OWL datatypes.");
            String datatypeURI=object.getDatatype().getURI().toString();
            if (AtomicConcept.RDFS_LITERAL.getIRI().equals(datatypeURI)) {
                if (!object.getFacetRestrictions().isEmpty())
                    throw new IllegalArgumentException("rdfs:Literal does not support any facets.");
                return AtomicConcept.RDFS_LITERAL;
            }
            String[] facetURIs=new String[object.getFacetRestrictions().size()];
            Object[] facetValues=new Object[object.getFacetRestrictions().size()];
            int index=0;
            for (OWLFacetRestriction facet : object.getFacetRestrictions()) {
                facetURIs[index]=facet.getFacet().getIRI().toURI().toString();
                facetValues[index]=facet.getFacetValue().accept(this);
                index++;
            }
            DatatypeRestriction datatype=DatatypeRestriction.create(datatypeURI,facetURIs,facetValues);
            DatatypeRegistry.validateDatatypeRestriction(datatype);
            return datatype;
        }
        public Object visit(OWLFacetRestriction object) {
            throw new IllegalStateException("Internal error: should not get in here.");
        }
        public Object visit(OWLTypedLiteral object) {
            return DatatypeRegistry.parseLiteral(object.getLiteral(),object.getDatatype().getURI().toString());
        }
        public Object visit(OWLStringLiteral object) {
            if (object.getLang()==null)
                return DatatypeRegistry.parseLiteral(object.getLiteral(),Prefixes.s_semanticWebPrefixes.get("xsd")+"string");
            else
                return DatatypeRegistry.parseLiteral(object.getLiteral()+"@"+object.getLang(),Prefixes.s_semanticWebPrefixes.get("rdf")+"PlainLiteral");
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
                AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)description).getURI().toString());
                m_positiveFacts.add(Atom.create(atomicConcept,getIndividual(object.getIndividual())));
            }
            else if (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLClass) {
                AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)((OWLObjectComplementOf)description).getOperand()).getURI().toString());
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
            Constant targetValue=Constant.create(object.getObject().accept(m_dataRangeConverter));
            m_positiveFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),targetValue));
        }
        public void visit(OWLNegativeDataPropertyAssertionAxiom object) {
            Constant targetValue=Constant.create(object.getObject().accept(m_dataRangeConverter));
            m_negativeFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),targetValue));
        }
    }

    protected static class HeadComparator implements Comparator<Atom> {
        public static final HeadComparator INSTANCE=new HeadComparator();

        public int compare(Atom o1,Atom o2) {
            int type1;
            if (o1.getDLPredicate() instanceof AtLeastConcept)
                type1=2;
            else
                type1=1;
            int type2;
            if (o2.getDLPredicate() instanceof AtLeastConcept)
                type2=2;
            else
                type2=1;
            return type1-type2;
        }
    }
}
