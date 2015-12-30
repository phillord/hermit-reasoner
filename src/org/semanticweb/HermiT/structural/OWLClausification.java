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

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

import java.util.*;

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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
/**OWLClausification.*/
public class OWLClausification {
    protected static final Variable X=Variable.create("X");
    protected static final Variable Y=Variable.create("Y");
    protected static final Variable Z=Variable.create("Z");

    protected final Configuration m_configuration;

    /**
     * @param configuration configuration
     */
    public OWLClausification(Configuration configuration) {
        m_configuration=configuration;
    }
    /**
     * @param rootOntology rootOntology
     * @param descriptionGraphs descriptionGraphs
     * @return clausifications
     */
    public Object[] preprocessAndClausify(OWLOntology rootOntology,Collection<DescriptionGraph> descriptionGraphs) {
        OWLDataFactory factory=rootOntology.getOWLOntologyManager().getOWLDataFactory();
        Optional<IRI> defaultDocumentIRI = rootOntology.getOntologyID().getDefaultDocumentIRI();
        String ontologyIRI=defaultDocumentIRI.isPresent()?defaultDocumentIRI.get().toString(): "urn:hermit:kb" ;
        OWLAxioms axioms=new OWLAxioms();
        OWLNormalization normalization=new OWLNormalization(factory,axioms,0);
        rootOntology.importsClosure().forEach(o->normalization.processOntology(o));
        BuiltInPropertyManager builtInPropertyManager=new BuiltInPropertyManager(factory);
        builtInPropertyManager.axiomatizeBuiltInPropertiesAsNeeded(axioms);
        ObjectPropertyInclusionManager objectPropertyInclusionManager=new ObjectPropertyInclusionManager(axioms);
        // now object property inclusion manager added all non-simple properties to axioms.m_complexObjectPropertyExpressions
        // now that we know which roles are non-simple, we can decide which negative object property assertions have to be
        // expressed as concept assertions so that transitivity rewriting applies properly.
        objectPropertyInclusionManager.rewriteNegativeObjectPropertyAssertions(factory,axioms,normalization.m_definitions.size());
        objectPropertyInclusionManager.rewriteAxioms(factory,axioms,0);
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        OWLAxiomsExpressivity axiomsExpressivity=new OWLAxiomsExpressivity(axioms);
        DLOntology dlOntology=clausify(factory,ontologyIRI,axioms,axiomsExpressivity,descriptionGraphs);
        return new Object[] { objectPropertyInclusionManager,dlOntology };
    }
    /**
     * @param factory factory
     * @param ontologyIRI ontologyIRI
     * @param axioms axioms
     * @param axiomsExpressivity axiomsExpressivity
     * @param descriptionGraphs descriptionGraphs
     * @return dl ontology
     */
    public DLOntology clausify(OWLDataFactory factory,String ontologyIRI,OWLAxioms axioms,OWLAxiomsExpressivity axiomsExpressivity,Collection<DescriptionGraph> descriptionGraphs) {
        Set<DLClause> dlClauses=new LinkedHashSet<>();
        Set<Atom> positiveFacts=new HashSet<>();
        Set<Atom> negativeFacts=new HashSet<>();
        Set<DatatypeRestriction> allUnknownDatatypeRestrictions=new HashSet<>();
        for (List<OWLObjectPropertyExpression> inclusion : axioms.m_simpleObjectPropertyInclusions) {
            Atom subRoleAtom=getRoleAtom(inclusion.get(0),X,Y);
            Atom superRoleAtom=getRoleAtom(inclusion.get(1),X,Y);
            DLClause dlClause=DLClause.create(new Atom[] { superRoleAtom },new Atom[] { subRoleAtom });
            dlClauses.add(dlClause);
        }
        for (List<OWLDataPropertyExpression> inclusion : axioms.m_dataPropertyInclusions) {
            Atom subProp=getRoleAtom(inclusion.get(0),X,Y);
            Atom superProp=getRoleAtom(inclusion.get(1),X,Y);
            DLClause dlClause=DLClause.create(new Atom[] { superProp },new Atom[] { subProp });
            dlClauses.add(dlClause);
        }
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_asymmetricObjectProperties) {
            Atom roleAtom=getRoleAtom(objectPropertyExpression,X,Y);
            Atom inverseRoleAtom=getRoleAtom(objectPropertyExpression,Y,X);
            DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { roleAtom,inverseRoleAtom });
            dlClauses.add(dlClause);
        }
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_reflexiveObjectProperties) {
            Atom roleAtom=getRoleAtom(objectPropertyExpression,X,X);
            Atom bodyAtom=Atom.create(AtomicConcept.THING,X);
            DLClause dlClause=DLClause.create(new Atom[] { roleAtom },new Atom[] { bodyAtom });
            dlClauses.add(dlClause);
        }
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_irreflexiveObjectProperties) {
            Atom roleAtom=getRoleAtom(objectPropertyExpression,X,X);
            DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { roleAtom });
            dlClauses.add(dlClause);
        }
        for (List<OWLObjectPropertyExpression> properties : axioms.m_disjointObjectProperties)
            for (int i=0;i<properties.size();i++)
                for (int j=i+1;j<properties.size();j++) {
                    Atom atom_i=getRoleAtom(properties.get(i),X,Y);
                    Atom atom_j=getRoleAtom(properties.get(j),X,Y);
                    DLClause dlClause=DLClause.create(new Atom[] {},new Atom[] { atom_i,atom_j });
                    dlClauses.add(dlClause);
                }
        if (contains(axioms, factory.getOWLBottomDataProperty())) {
            Atom bodyAtom=Atom.create(AtomicRole.BOTTOM_DATA_ROLE,X,Y);
            dlClauses.add(DLClause.create(new Atom[] {},new Atom[] { bodyAtom }));
        }
        for (List<OWLDataPropertyExpression> properties : axioms.m_disjointDataProperties)
            for (int i=0;i<properties.size();i++)
                for (int j=i+1;j<properties.size();j++) {
                    Atom atom_i=getRoleAtom(properties.get(i),X,Y);
                    Atom atom_j=getRoleAtom(properties.get(j),X,Z);
                    Atom atom_ij=Atom.create(Inequality.create(),Y,Z);
                    DLClause dlClause=DLClause.create(new Atom[] { atom_ij },new Atom[] { atom_i,atom_j });
                    dlClauses.add(dlClause);
                }
        DataRangeConverter dataRangeConverter=new DataRangeConverter(m_configuration.warningMonitor,axioms.m_definedDatatypesIRIs,allUnknownDatatypeRestrictions,m_configuration.ignoreUnsupportedDatatypes);
        NormalizedAxiomClausifier clausifier=new NormalizedAxiomClausifier(dataRangeConverter,positiveFacts);
        for (List<OWLClassExpression> inclusion : axioms.m_conceptInclusions) {
            for (OWLClassExpression description : inclusion)
                description.accept(clausifier);
            DLClause dlClause=clausifier.getDLClause();
            dlClauses.add(dlClause.getSafeVersion(AtomicConcept.THING));
        }
        NormalizedDataRangeAxiomClausifier normalizedDataRangeAxiomClausifier=new NormalizedDataRangeAxiomClausifier(dataRangeConverter,axioms.m_definedDatatypesIRIs);
        for (List<OWLDataRange> inclusion : axioms.m_dataRangeInclusions) {
            for (OWLDataRange description : inclusion)
                description.accept(normalizedDataRangeAxiomClausifier);
            DLClause dlClause=normalizedDataRangeAxiomClausifier.getDLClause();
            dlClauses.add(dlClause.getSafeVersion(InternalDatatype.RDFS_LITERAL));
        }
        for (OWLHasKeyAxiom hasKey : axioms.m_hasKeys)
            dlClauses.add(clausifyKey(hasKey));
        FactClausifier factClausifier=new FactClausifier(dataRangeConverter,positiveFacts,negativeFacts);
        for (OWLIndividualAxiom fact : axioms.m_facts)
            fact.accept(factClausifier);
        for (DescriptionGraph descriptionGraph : descriptionGraphs)
            descriptionGraph.produceStartDLClauses(dlClauses);
        Set<AtomicConcept> atomicConcepts=new HashSet<>();
        Set<AtomicRole> atomicObjectRoles=new HashSet<>();
        Set<Role> complexObjectRoles=new HashSet<>();
        Set<AtomicRole> atomicDataRoles=new HashSet<>();
        for (OWLClass owlClass : axioms.m_classes)
            atomicConcepts.add(AtomicConcept.create(owlClass.getIRI().toString()));
        Set<Individual> individuals=new HashSet<>();
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
        if (!axioms.m_rules.isEmpty())
            new NormalizedRuleClausifier(axioms.m_objectPropertiesOccurringInOWLAxioms,descriptionGraphs,dataRangeConverter,dlClauses).processRules(axioms.m_rules);
        // Create the DL ontology
        return new DLOntology(ontologyIRI,dlClauses,positiveFacts,negativeFacts,atomicConcepts,atomicObjectRoles,complexObjectRoles,atomicDataRoles,allUnknownDatatypeRestrictions,axioms.m_definedDatatypesIRIs,individuals,axiomsExpressivity.m_hasInverseRoles,axiomsExpressivity.m_hasAtMostRestrictions,axiomsExpressivity.m_hasNominals,axiomsExpressivity.m_hasDatatypes);
    }
    protected DLClause clausifyKey(OWLHasKeyAxiom object) {
        List<Atom> headAtoms=new ArrayList<>();
        List<Atom> bodyAtoms=new ArrayList<>();
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
                headAtoms.add(Atom.create(AtomicConcept.create(owlClass.getIRI().toString()),X1));
                headAtoms.add(Atom.create(AtomicConcept.create(owlClass.getIRI().toString()),X2));
            }
            else
                throw new IllegalStateException("Internal error: invalid normal form.");
        }
        else
            throw new IllegalStateException("Internal error: invalid normal form.");
        int yIndex=1;
        // object properties always go to the body
        for (OWLObjectPropertyExpression p : asList(object.objectPropertyExpressions())) {
            Variable y;
            y=Variable.create("Y"+yIndex);
            yIndex++;
            bodyAtoms.add(getRoleAtom(p,X1,y));
            bodyAtoms.add(getRoleAtom(p,X2,y));
            // also the key criteria are named in case of object properties
            bodyAtoms.add(Atom.create(AtomicConcept.INTERNAL_NAMED,y));
        }
        // data properties go to the body, but with different variables
        // the head gets an atom asserting inequality between that data values
        for (OWLDataPropertyExpression d : asList(object.dataPropertyExpressions())) {
            Variable y;
            y=Variable.create("Y"+yIndex);
            yIndex++;
            bodyAtoms.add(getRoleAtom(d,X1,y));
            Variable y2;
            y2=Variable.create("Y"+yIndex);
            yIndex++;
            bodyAtoms.add(getRoleAtom(d,X2,y2));
            headAtoms.add(Atom.create(Inequality.INSTANCE,y,y2));
        }
        Atom[] hAtoms=new Atom[headAtoms.size()];
        headAtoms.toArray(hAtoms);
        Atom[] bAtoms=new Atom[bodyAtoms.size()];
        bodyAtoms.toArray(bAtoms);
        return DLClause.create(hAtoms,bAtoms);
    }
    private static boolean contains(OWLAxioms axioms, OWLDataProperty p) {
        for(List<OWLDataPropertyExpression> e: axioms.m_dataPropertyInclusions) {
            for(OWLDataPropertyExpression candidate:e) {
                if(candidate.equals(p)) {
                    return true;
                }
            }
        }
        return false;
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
        protected final DataRangeConverter m_dataRangeConverter;
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;
        protected final Set<Atom> m_positiveFacts;
        protected int m_yIndex;
        protected int m_zIndex;

        public NormalizedAxiomClausifier(DataRangeConverter dataRangeConverter,Set<Atom> positiveFacts) {
            m_dataRangeConverter=dataRangeConverter;
            m_headAtoms=new ArrayList<>();
            m_bodyAtoms=new ArrayList<>();
            m_positiveFacts=positiveFacts;
        }
        protected DLClause getDLClause() {
            Atom[] headAtoms=new Atom[m_headAtoms.size()];
            m_headAtoms.toArray(headAtoms);
            Atom[] bodyAtoms=new Atom[m_bodyAtoms.size()];
            m_bodyAtoms.toArray(bodyAtoms);
            DLClause dlClause=DLClause.create(headAtoms,bodyAtoms);
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

        @Override
        public void visit(OWLClass object) {
            m_headAtoms.add(Atom.create(AtomicConcept.create(object.getIRI().toString()),X));
        }
        @Override
        public void visit(OWLObjectIntersectionOf object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        @Override
        public void visit(OWLObjectUnionOf object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        @Override
        public void visit(OWLObjectComplementOf object) {
            OWLClassExpression description=object.getOperand();
            if (description instanceof OWLObjectHasSelf) {
                OWLObjectPropertyExpression objectProperty=((OWLObjectHasSelf)description).getProperty();
                Atom roleAtom=getRoleAtom(objectProperty,X,X);
                m_bodyAtoms.add(roleAtom);
            }
            else if (description instanceof OWLObjectOneOf && ((OWLObjectOneOf)description).individuals().count()==1) {
                OWLIndividual individual=((OWLObjectOneOf)description).individuals().iterator().next();
                m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),X));
            }
            else if (!(description instanceof OWLClass))
                throw new IllegalStateException("Internal error: invalid normal form.");
            else
                m_bodyAtoms.add(Atom.create(AtomicConcept.create(((OWLClass)description).getIRI().toString()),X));
        }
        @Override
        public void visit(OWLObjectOneOf object) {
            object.individuals().forEach(i-> {
                Variable z=nextZ();
                AtomicConcept conceptForNominal=getConceptForNominal(i);
                m_headAtoms.add(Atom.create(Equality.INSTANCE,X,z));
                m_bodyAtoms.add(Atom.create(conceptForNominal,z));
            });
        }
        @Override
        public void visit(OWLObjectSomeValuesFrom object) {
            OWLClassExpression filler=object.getFiller();
            if (filler instanceof OWLObjectOneOf) {
                ((OWLObjectOneOf)filler).individuals().forEach(i-> {
                    Variable z=nextZ();
                    m_bodyAtoms.add(Atom.create(getConceptForNominal(i),z));
                    m_headAtoms.add(getRoleAtom(object.getProperty(),X,z));
                });
            }
            else {
                LiteralConcept toConcept=getLiteralConcept(filler);
                Role onRole=getRole(object.getProperty());
                AtLeastConcept atLeastConcept=AtLeastConcept.create(1,onRole,toConcept);
                if (!atLeastConcept.isAlwaysFalse())
                    m_headAtoms.add(Atom.create(atLeastConcept,X));
            }
        }
        @Override
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
                ((OWLObjectOneOf)filler).individuals().forEach(i-> {
                    Variable zInd=nextZ();
                    m_bodyAtoms.add(Atom.create(getConceptForNominal(i),zInd));
                    m_headAtoms.add(Atom.create(Equality.INSTANCE,y,zInd));
                });
            }
            else if (filler instanceof OWLObjectComplementOf) {
                OWLClassExpression operand=((OWLObjectComplementOf)filler).getOperand();
                if (operand instanceof OWLClass) {
                    AtomicConcept internalAtomicConcept=AtomicConcept.create(((OWLClass)operand).getIRI().toString());
                    if (!internalAtomicConcept.isAlwaysTrue())
                        m_bodyAtoms.add(Atom.create(internalAtomicConcept,y));
                }
                else if (operand instanceof OWLObjectOneOf && ((OWLObjectOneOf)operand).individuals().count()==1) {
                    OWLIndividual individual=((OWLObjectOneOf)operand).individuals().iterator().next();
                    m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),y));
                }
                else
                    throw new IllegalStateException("Internal error: invalid normal form.");
            }
            else
                throw new IllegalStateException("Internal error: invalid normal form.");
        }
        @Override
        public void visit(OWLObjectHasValue object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        @Override
        public void visit(OWLObjectHasSelf object) {
            OWLObjectPropertyExpression objectProperty=object.getProperty();
            Atom roleAtom=getRoleAtom(objectProperty,X,X);
            m_headAtoms.add(roleAtom);
        }
        @Override
        public void visit(OWLObjectMinCardinality object) {
            LiteralConcept toConcept=getLiteralConcept(object.getFiller());
            Role onRole=getRole(object.getProperty());
            AtLeastConcept atLeastConcept=AtLeastConcept.create(object.getCardinality(),onRole,toConcept);
            if (!atLeastConcept.isAlwaysFalse())
                m_headAtoms.add(Atom.create(atLeastConcept,X));
        }
        @Override
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
        @Override
        public void visit(OWLObjectExactCardinality object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        @Override
        public void visit(OWLDataSomeValuesFrom object) {
            if (!object.getProperty().isOWLBottomDataProperty()) {
                AtomicRole atomicRole=getAtomicRole(object.getProperty());
                LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(object.getFiller());
                AtLeastDataRange atLeastDataRange=AtLeastDataRange.create(1,atomicRole,literalRange);
                if (!atLeastDataRange.isAlwaysFalse())
                    m_headAtoms.add(Atom.create(atLeastDataRange,X));
            }
        }
        @Override
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
        @Override
        public void visit(OWLDataHasValue object) {
            throw new IllegalStateException("Internal error: Invalid normal form.");
        }
        @Override
        public void visit(OWLDataMinCardinality object) {
            if (!object.getProperty().isOWLBottomDataProperty() || object.getCardinality()==0) {
                AtomicRole atomicRole=getAtomicRole(object.getProperty());
                LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(object.getFiller());
                AtLeastDataRange atLeast=AtLeastDataRange.create(object.getCardinality(),atomicRole,literalRange);
                if (!atLeast.isAlwaysFalse())
                    m_headAtoms.add(Atom.create(atLeast,X));
            }
        }
        @Override
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
        @Override
        public void visit(OWLDataExactCardinality object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
    }

    protected static class NormalizedDataRangeAxiomClausifier implements OWLDataVisitor {
        protected final DataRangeConverter m_dataRangeConverter;
        protected final Set<String> m_definedDatatypeIRIs;
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;

        public NormalizedDataRangeAxiomClausifier(DataRangeConverter dataRangeConverter,Set<String> definedDatatypeIRIs) {
            m_dataRangeConverter=dataRangeConverter;
            m_definedDatatypeIRIs=definedDatatypeIRIs;
            m_headAtoms=new ArrayList<>();
            m_bodyAtoms=new ArrayList<>();
        }
        protected DLClause getDLClause() {
            Atom[] headAtoms=new Atom[m_headAtoms.size()];
            m_headAtoms.toArray(headAtoms);
            Atom[] bodyAtoms=new Atom[m_bodyAtoms.size()];
            m_bodyAtoms.toArray(bodyAtoms);
            DLClause dlClause=DLClause.create(headAtoms,bodyAtoms);
            m_headAtoms.clear();
            m_bodyAtoms.clear();
            return dlClause;
        }

        // Various types of descriptions

        @Override
        public void visit(OWLDatatype dt) {
            LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(dt);
            m_headAtoms.add(Atom.create((DLPredicate)literalRange,X));
        }
        @Override
        public void visit(OWLDataIntersectionOf dr) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        @Override
        public void visit(OWLDataUnionOf dr) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        private static String datatypeIRI(OWLDataRange r) {
            if(r.isOWLDatatype()) {
                return r.asOWLDatatype().getIRI().toString();
            }
            return null;
        }
        @Override
        public void visit(OWLDataComplementOf dr) {
            String iri=datatypeIRI(dr.getDataRange());
            if (iri!=null && (Prefixes.isInternalIRI(iri) || m_definedDatatypeIRIs.contains(iri))) {
                m_bodyAtoms.add(Atom.create(InternalDatatype.create(iri),X));
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
        @Override
        public void visit(OWLDataOneOf object) {
            LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(object);
            m_headAtoms.add(Atom.create((DLPredicate)literalRange,X));
        }
        @Override
        public void visit(OWLFacetRestriction node) {
            throw new IllegalStateException("Internal error: Invalid normal form. ");
        }
        @Override
        public void visit(OWLDatatypeRestriction node) {
            LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(node);
            m_headAtoms.add(Atom.create((DLPredicate)literalRange,X));
        }
        @Override
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
        @Override
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
        @Override
        public Object visit(OWLDataComplementOf object) {
            return convertDataRange(object.getDataRange()).getNegation();
        }
        @Override
        public Object visit(OWLDataOneOf object) {
            Set<Constant> constants=new HashSet<>();
            object.values().forEach(l-> constants.add((Constant)l.accept(this)));
            Constant[] constantsArray=new Constant[constants.size()];
            constants.toArray(constantsArray);
            return ConstantEnumeration.create(constantsArray);
        }
        @Override
        public Object visit(OWLDatatypeRestriction object) {
            if (!(object.getDatatype().isOWLDatatype()))
                throw new IllegalArgumentException("Datatype restrictions are supported only on OWL datatypes.");
            String datatypeURI=object.getDatatype().getIRI().toString();
            if (InternalDatatype.RDFS_LITERAL.getIRI().equals(datatypeURI)) {
                if (object.facetRestrictions().count()>0)
                    throw new IllegalArgumentException("rdfs:Literal does not support any facets.");
                return InternalDatatype.RDFS_LITERAL;
            }
            List<OWLFacetRestriction> list=asList(object.facetRestrictions());
            String[] facetURIs=new String[list.size()];
            Constant[] facetValues=new Constant[list.size()];
            int index=0;
            for (OWLFacetRestriction facet : list) {
                facetURIs[index]=facet.getFacet().getIRI().toURI().toString();
                facetValues[index]=(Constant)facet.getFacetValue().accept(this);
                index++;
            }
            DatatypeRestriction datatype=DatatypeRestriction.create(datatypeURI,facetURIs,facetValues);
            DatatypeRegistry.validateDatatypeRestriction(datatype);
            return datatype;
        }
        @Override
        public Object visit(OWLFacetRestriction object) {
            throw new IllegalStateException("Internal error: should not get in here.");
        }
        @Override
        public Object visit(OWLLiteral object) {
            try {
                if (object.isRDFPlainLiteral()||object.getDatatype().getIRI().equals(OWL2Datatype.RDF_LANG_STRING.getIRI())) {
                    if (object.hasLang())
                        return Constant.create(object.getLiteral()+"@"+object.getLang(),Prefixes.s_semanticWebPrefixes.get("rdf:")+"PlainLiteral");
                    else
                        return Constant.create(object.getLiteral()+"@",Prefixes.s_semanticWebPrefixes.get("rdf:")+"PlainLiteral");
                }
                else
                    return Constant.create(object.getLiteral(),object.getDatatype().getIRI().toString());
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
        @Override
        public Object visit(OWLDataIntersectionOf node) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        @Override
        public Object visit(OWLDataUnionOf node) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
    }

    protected static class FactClausifier implements OWLAxiomVisitor {
        protected final DataRangeConverter m_dataRangeConverter;
        protected final Set<Atom> m_positiveFacts;
        protected final Set<Atom> m_negativeFacts;

        public FactClausifier(DataRangeConverter dataRangeConverter,Set<Atom> positiveFacts,Set<Atom> negativeFacts) {
            m_dataRangeConverter=dataRangeConverter;
            m_positiveFacts=positiveFacts;
            m_negativeFacts=negativeFacts;
        }
        @Override
        public void visit(OWLSameIndividualAxiom object) {
            List<OWLIndividual> individuals= asList(object.individuals());
            for (int i=0;i<individuals.size()-1;i++)
                m_positiveFacts.add(Atom.create(Equality.create(),getIndividual(individuals.get(i)),getIndividual(individuals.get(i+1))));
        }
        @Override
        public void visit(OWLDifferentIndividualsAxiom object) {
            List<OWLIndividual> individuals=asList(object.individuals());
            for (int i=0;i<individuals.size()-1;i++)
                for (int j=i+1;j<individuals.size();j++)
                    m_positiveFacts.add(Atom.create(Inequality.create(),getIndividual(individuals.get(i)),getIndividual(individuals.get(j))));
        }
        @Override
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
        @Override
        public void visit(OWLObjectPropertyAssertionAxiom object) {
            m_positiveFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),getIndividual(object.getObject())));
        }
        @Override
        public void visit(OWLNegativeObjectPropertyAssertionAxiom object) {
            m_negativeFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),getIndividual(object.getObject())));
        }
        @Override
        public void visit(OWLDataPropertyAssertionAxiom object) {
            Constant targetValue=(Constant)object.getObject().accept(m_dataRangeConverter);
            m_positiveFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),targetValue));
        }
        @Override
        public void visit(OWLNegativeDataPropertyAssertionAxiom object) {
            Constant targetValue=(Constant)object.getObject().accept(m_dataRangeConverter);
            m_negativeFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),targetValue));
        }
    }

    protected final static class NormalizedRuleClausifier implements SWRLObjectVisitorEx<Atom> {
        protected final Set<OWLObjectProperty> m_objectPropertiesOccurringInOWLAxioms;
        protected final DataRangeConverter m_dataRangeConverter;
        protected final Set<DLClause> m_dlClauses;
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;
        protected final Set<Variable> m_abstractVariables;
        protected final Set<OWLObjectProperty> m_graphObjectProperties=new HashSet<>();
        protected boolean m_containsObjectProperties;
        protected boolean m_containsGraphObjectProperties;
        protected boolean m_containsNonGraphObjectProperties;
        protected boolean m_containsUndeterminedObjectProperties;

        public NormalizedRuleClausifier(Set<OWLObjectProperty> objectPropertiesOccurringInOWLAxioms,Collection<DescriptionGraph> descriptionGraphs,DataRangeConverter dataRangeConverter,Set<DLClause> dlClauses) {
            m_objectPropertiesOccurringInOWLAxioms=objectPropertiesOccurringInOWLAxioms;
            m_dataRangeConverter=dataRangeConverter;
            m_dlClauses=dlClauses;
            m_headAtoms=new ArrayList<>();
            m_bodyAtoms=new ArrayList<>();
            m_abstractVariables=new HashSet<>();
            OWLDataFactory factory=OWLManager.getOWLDataFactory();
            for (DescriptionGraph descriptionGraph : descriptionGraphs)
                for (int i=0;i<descriptionGraph.getNumberOfEdges();i++)
                    m_graphObjectProperties.add(factory.getOWLObjectProperty(IRI.create(descriptionGraph.getEdge(i).getAtomicRole().getIRI())));
            for (OWLObjectProperty objectProperty : m_graphObjectProperties)
                if (objectPropertiesOccurringInOWLAxioms.contains(objectProperty))
                    throw new IllegalArgumentException("Mixing graph and non-graph object properties is not supported.");
        }
        public void processRules(Collection<OWLAxioms.DisjunctiveRule> rules) {
            List<OWLAxioms.DisjunctiveRule> unprocessedRules=new ArrayList<>(rules);
            boolean changed=true;
            while (!unprocessedRules.isEmpty() && changed) {
                changed=false;
                Iterator<OWLAxioms.DisjunctiveRule> iterator=unprocessedRules.iterator();
                while (iterator.hasNext()) {
                    OWLAxioms.DisjunctiveRule rule=iterator.next();
                    determineRuleType(rule);
                    if (m_containsGraphObjectProperties && m_containsNonGraphObjectProperties)
                        throw new IllegalArgumentException("A SWRL rule mixes graph and non-graph object properties, which is not supported.");
                    determineUndeterminedObjectProperties(rule);
                    if (!m_containsUndeterminedObjectProperties) {
                        iterator.remove();
                        clausify(rule,m_containsNonGraphObjectProperties || !m_containsObjectProperties);
                        changed=true;
                    }
                }
            }
            m_containsObjectProperties=false;
            m_containsGraphObjectProperties=false;
            m_containsNonGraphObjectProperties=true;
            m_containsUndeterminedObjectProperties=false;
            for (OWLAxioms.DisjunctiveRule rule : unprocessedRules) {
                determineUndeterminedObjectProperties(rule);
                clausify(rule,true);
            }
        }
        protected void determineRuleType(OWLAxioms.DisjunctiveRule rule) {
            m_containsObjectProperties=false;
            m_containsGraphObjectProperties=false;
            m_containsNonGraphObjectProperties=false;
            m_containsUndeterminedObjectProperties=false;
            for (SWRLAtom atom : rule.m_body)
                checkRuleAtom(atom);
            for (SWRLAtom atom : rule.m_head)
                checkRuleAtom(atom);
        }
        protected void checkRuleAtom(SWRLAtom atom) {
            if (atom instanceof SWRLObjectPropertyAtom) {
                m_containsObjectProperties=true;
                OWLObjectProperty objectProperty=((SWRLObjectPropertyAtom)atom).getPredicate().getNamedProperty();
                boolean isGraphObjectProperty=m_graphObjectProperties.contains(objectProperty);
                boolean isNonGraphObjectProperty=m_objectPropertiesOccurringInOWLAxioms.contains(objectProperty);
                if (isGraphObjectProperty)
                    m_containsGraphObjectProperties=true;
                if (isNonGraphObjectProperty)
                    m_containsNonGraphObjectProperties=true;
                if (!isGraphObjectProperty && !isNonGraphObjectProperty)
                    m_containsUndeterminedObjectProperties=true;
            }
        }
        protected void determineUndeterminedObjectProperties(OWLAxioms.DisjunctiveRule rule) {
            if (m_containsUndeterminedObjectProperties) {
                if (m_containsGraphObjectProperties) {
                    for (SWRLAtom atom : rule.m_body)
                        makeGraphObjectProperty(atom);
                    for (SWRLAtom atom : rule.m_head)
                        makeGraphObjectProperty(atom);
                    m_containsUndeterminedObjectProperties=false;
                }
                else if (m_containsNonGraphObjectProperties) {
                    for (SWRLAtom atom : rule.m_body)
                        makeNonGraphObjectProperty(atom);
                    for (SWRLAtom atom : rule.m_head)
                        makeNonGraphObjectProperty(atom);
                    m_containsUndeterminedObjectProperties=false;
                }
            }
        }
        protected void makeGraphObjectProperty(SWRLAtom atom) {
            if (atom instanceof SWRLObjectPropertyAtom) {
                OWLObjectProperty objectProperty=((SWRLObjectPropertyAtom)atom).getPredicate().getNamedProperty();
                m_graphObjectProperties.add(objectProperty);
            }
        }
        protected void makeNonGraphObjectProperty(SWRLAtom atom) {
            if (atom instanceof SWRLObjectPropertyAtom) {
                OWLObjectProperty objectProperty=((SWRLObjectPropertyAtom)atom).getPredicate().getNamedProperty();
                m_objectPropertiesOccurringInOWLAxioms.add(objectProperty);
            }
        }
        protected void clausify(OWLAxioms.DisjunctiveRule rule,boolean restrictToNamed) {
            m_headAtoms.clear();
            m_bodyAtoms.clear();
            m_abstractVariables.clear();
            for (SWRLAtom atom : rule.m_body)
                m_bodyAtoms.add(atom.accept(this));
            for (SWRLAtom atom : rule.m_head)
                m_headAtoms.add(atom.accept(this));
            if (restrictToNamed) {
                for (Variable variable : m_abstractVariables)
                    m_bodyAtoms.add(Atom.create(AtomicConcept.INTERNAL_NAMED,variable));
            }
            DLClause dlClause=DLClause.create(m_headAtoms.toArray(new Atom[m_headAtoms.size()]),m_bodyAtoms.toArray(new Atom[m_bodyAtoms.size()]));
            m_dlClauses.add(dlClause);
            m_headAtoms.clear();
            m_bodyAtoms.clear();
            m_abstractVariables.clear();
        }
        @Override
        public Atom visit(SWRLClassAtom atom) {
            if (atom.getPredicate().isAnonymous())
                throw new IllegalStateException("Internal error: SWRL rule class atoms should be normalized to contain only named classes, but this class atom has a complex concept: "+atom.getPredicate());
            Variable variable=toVariable(atom.getArgument());
            m_abstractVariables.add(variable);
            return Atom.create(AtomicConcept.create(atom.getPredicate().asOWLClass().getIRI().toString()),variable);
        }
        @Override
        public Atom visit(SWRLDataRangeAtom atom) {
            Variable variable=toVariable(atom.getArgument());
            LiteralDataRange literalRange=m_dataRangeConverter.convertDataRange(atom.getPredicate());
            return Atom.create((DLPredicate)literalRange,variable);
        }
        @Override
        public Atom visit(SWRLObjectPropertyAtom atom) {
            Variable variable1=toVariable(atom.getFirstArgument());
            Variable variable2=toVariable(atom.getSecondArgument());
            m_abstractVariables.add(variable1);
            m_abstractVariables.add(variable2);
            return getRoleAtom(atom.getPredicate().asOWLObjectProperty(),variable1,variable2);
        }
        @Override
        public Atom visit(SWRLDataPropertyAtom atom) {
            Variable variable1=toVariable(atom.getFirstArgument());
            Variable variable2=toVariable(atom.getSecondArgument());
            m_abstractVariables.add(variable1);
            return getRoleAtom(atom.getPredicate().asOWLDataProperty(),variable1,variable2);
        }
        @Override
        public Atom visit(SWRLSameIndividualAtom atom) {
            Variable variable1=toVariable(atom.getFirstArgument());
            Variable variable2=toVariable(atom.getSecondArgument());
            return Atom.create(Equality.INSTANCE,variable1,variable2);
        }
        @Override
        public Atom visit(SWRLDifferentIndividualsAtom atom) {
            Variable variable1=toVariable(atom.getFirstArgument());
            Variable variable2=toVariable(atom.getSecondArgument());
            return Atom.create(Inequality.INSTANCE,variable1,variable2);
        }
        @Override
        public Atom visit(SWRLBuiltInAtom node) {
            throw new UnsupportedOperationException("Rules with SWRL built-in atoms are not yet supported. ");
        }
        @Override
        public Atom visit(SWRLRule rule) {
            throw new IllegalStateException("Internal error: this part of the code is unused.");
        }
        @Override
        public Atom visit(SWRLVariable node) {
            throw new IllegalStateException("Internal error: this part of the code is unused.");
        }
        @Override
        public Atom visit(SWRLIndividualArgument atom) {
            throw new IllegalStateException("Internal error: this part of the code is unused.");
        }
        @Override
        public Atom visit(SWRLLiteralArgument arg) {
            throw new IllegalStateException("Internal error: this part of the code is unused.");
        }
        protected static Variable toVariable(SWRLIArgument argument) {
            if (argument instanceof SWRLVariable)
                return Variable.create(((SWRLVariable)argument).getIRI().toString());
            else
                throw new IllegalStateException("Internal error: all arguments in a SWRL rule should have been normalized to variables.");
        }
        protected static Variable toVariable(SWRLDArgument argument) {
            if (argument instanceof SWRLVariable)
                return Variable.create(((SWRLVariable)argument).getIRI().toString());
            else
                throw new IllegalStateException("Internal error: all arguments in a SWRL rule should have been normalized to variables.");
        }
    }
}
