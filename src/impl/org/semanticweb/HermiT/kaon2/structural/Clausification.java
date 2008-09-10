// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.kaon2.structural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.AtLeastAbstractRoleConcept;
import org.semanticweb.HermiT.model.AtMostAbstractRoleGuard;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.NodeIDLessThan;
import org.semanticweb.kaon2.api.Fact;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Namespaces;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.logic.Literal;
import org.semanticweb.kaon2.api.logic.Predicate;
import org.semanticweb.kaon2.api.logic.Rule;
import org.semanticweb.kaon2.api.owl.axioms.ClassMember;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.DifferentIndividuals;
import org.semanticweb.kaon2.api.owl.axioms.InverseObjectProperties;
import org.semanticweb.kaon2.api.owl.axioms.NegativeDataPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.NegativeObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyAttribute;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.SameIndividual;
import org.semanticweb.kaon2.api.owl.elements.DataAll;
import org.semanticweb.kaon2.api.owl.elements.DataCardinality;
import org.semanticweb.kaon2.api.owl.elements.DataHasValue;
import org.semanticweb.kaon2.api.owl.elements.DataNot;
import org.semanticweb.kaon2.api.owl.elements.DataOneOf;
import org.semanticweb.kaon2.api.owl.elements.DataPropertyExpression;
import org.semanticweb.kaon2.api.owl.elements.DataSome;
import org.semanticweb.kaon2.api.owl.elements.DatatypeRestriction;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.InverseObjectProperty;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectAll;
import org.semanticweb.kaon2.api.owl.elements.ObjectAnd;
import org.semanticweb.kaon2.api.owl.elements.ObjectCardinality;
import org.semanticweb.kaon2.api.owl.elements.ObjectExistsSelf;
import org.semanticweb.kaon2.api.owl.elements.ObjectHasValue;
import org.semanticweb.kaon2.api.owl.elements.ObjectNot;
import org.semanticweb.kaon2.api.owl.elements.ObjectOneOf;
import org.semanticweb.kaon2.api.owl.elements.ObjectOr;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;
import org.semanticweb.kaon2.api.owl.elements.ObjectPropertyExpression;
import org.semanticweb.kaon2.api.owl.elements.ObjectSome;

/**
 * This class implements the clausification part of the HermiT algorithm;
 */
public class Clausification {
    protected static final org.semanticweb.HermiT.model.Variable X=org.semanticweb.HermiT.model.Variable.create("X");
    protected static final org.semanticweb.HermiT.model.Variable Y=org.semanticweb.HermiT.model.Variable.create("Y");

    public DLOntology clausify(boolean prepareForNIRule,Ontology ontology,Collection<DescriptionGraph> descriptionGraphs) throws KAON2Exception {
        Normalization normalization=new Normalization();
        normalization.processOntology(ontology);
        return clausify(prepareForNIRule,
        		        ontology.getOntologyURI(),
        		        normalization.getConceptInclusions(),
        		        normalization.getNormalObjectPropertyInclusions(),
        		        normalization.getInverseObjectPropertyInclusions(),
        		        normalization.getAsymmetricObjectProperties(), 
        		        normalization.getNormalDataPropertyInclusios(),
        		        normalization.getFacts(),descriptionGraphs,
        		        normalization.getRules());
    }
    public DLOntology clausify(boolean prepareForNIRule,
    		                   String ontologyURI,
    		                   Collection<Description[]> conceptInclusions,
    		                   Collection<ObjectPropertyExpression[]> normalObjectPropertyInclusions,
    		                   Collection<ObjectPropertyExpression[]> inverseObjectPropertyInclusions,
    		                   Collection<ObjectPropertyExpression> antisymmetricObjectProperties, 
    		                   Collection<DataPropertyExpression[]> inverseDataPropertyInclusions,
    		                   Collection<Fact> facts,Collection<DescriptionGraph> descriptionGraphs,
    		                   Collection<Rule> additionalRules) throws KAON2Exception {
        DetermineExpressivity determineExpressivity=new DetermineExpressivity();
        for (Description[] inclusion : conceptInclusions)
            for (Description description : inclusion)
                description.accept(determineExpressivity);
        for (ObjectPropertyExpression[] inclusion : normalObjectPropertyInclusions) {
            boolean isInverse0=(inclusion[0] instanceof InverseObjectProperty);
            boolean isInverse1=(inclusion[1] instanceof InverseObjectProperty);
            if ((!isInverse0 && isInverse1) || (isInverse0 && !isInverse1))
                determineExpressivity.m_hasInverseRoles=true;
        }
        for (ObjectPropertyExpression[] inclusion : inverseObjectPropertyInclusions) {
            boolean isInverse0=(inclusion[0] instanceof InverseObjectProperty);
            boolean isInverse1=(inclusion[1] instanceof InverseObjectProperty);
            if ((isInverse0 && isInverse1) || (!isInverse0 && !isInverse1))
                determineExpressivity.m_hasInverseRoles=true;
        }
        if (inverseDataPropertyInclusions.size()>0)
            throw new IllegalArgumentException("Data properties are not supported yet.");
        Set<DLClause> dlClauses=new LinkedHashSet<DLClause>();
        Set<Atom> positiveFacts=new HashSet<Atom>();
        Set<Atom> negativeFacts=new HashSet<Atom>();
        for (ObjectPropertyExpression[] inclusion : normalObjectPropertyInclusions) {
            Atom subRoleAtom=getRoleAtom(inclusion[0],X,Y);
            Atom superRoleAtom=getRoleAtom(inclusion[1],X,Y);
            DLClause dlClause=DLClause.create(new Atom[] { superRoleAtom },new Atom[] { subRoleAtom });
            dlClauses.add(dlClause);
        }
        for (ObjectPropertyExpression[] inclusion : inverseObjectPropertyInclusions) {
            Atom subRoleAtom=getRoleAtom(inclusion[0],X,Y);
            Atom superRoleAtom=getRoleAtom(inclusion[1],Y,X);
            DLClause dlClause=DLClause.create(new Atom[] { superRoleAtom },new Atom[] { subRoleAtom });
            dlClauses.add(dlClause);
        }
        for (ObjectPropertyExpression axiom : antisymmetricObjectProperties) {
        	Atom roleAtom=getRoleAtom(axiom,X,Y);
            Atom inverseRoleAtom=getRoleAtom(axiom,Y,X);
        	DLClause dlClause = DLClause.create(new Atom[] { roleAtom, inverseRoleAtom }, new Atom[] { });
        	dlClauses.add(dlClause);
        }
        boolean shouldUseNIRule=determineExpressivity.m_hasAtMostRestrictions && determineExpressivity.m_hasInverseRoles && (determineExpressivity.m_hasNominals || prepareForNIRule);
        Clausifier clausifier=new Clausifier(positiveFacts,shouldUseNIRule);
        for (Description[] inclusion : conceptInclusions) {
            for (Description description : inclusion)
                description.accept(clausifier);
            DLClause dlClause=clausifier.getDLClause();
            dlClauses.add(dlClause.getSafeVersion());
        }
        clausifier.clausifyAtMostStuff(dlClauses);
        FactClausifier factClausifier=new FactClausifier(positiveFacts,negativeFacts);
        for (Fact fact : facts)
            fact.accept(factClausifier);
        for (DescriptionGraph descriptionGraph : descriptionGraphs)
            descriptionGraph.produceStartDLClauses(dlClauses);
        for (Rule rule : additionalRules)
            convertRule(rule,dlClauses);
        return new DLOntology(ontologyURI,
        		              dlClauses,
        		              positiveFacts,
        		              negativeFacts,
        		              determineExpressivity.m_hasInverseRoles,
        		              determineExpressivity.m_hasAtMostRestrictions,
        		              determineExpressivity.m_hasNominals,
        		              shouldUseNIRule,
        		              determineExpressivity.m_hasReflexivity);
    }
    protected static Atom getRoleAtom(ObjectPropertyExpression objectProperty,org.semanticweb.HermiT.model.Term first,org.semanticweb.HermiT.model.Term second) {
        objectProperty=objectProperty.getSimplified();
        if (objectProperty instanceof ObjectProperty) {
            AtomicRole role=AtomicRole.createObjectRole(((ObjectProperty)objectProperty).getURI());
            return Atom.create(role,new org.semanticweb.HermiT.model.Term[] { first,second });
        }
        else if (objectProperty instanceof InverseObjectProperty) {
            ObjectProperty internalObjectProperty=(ObjectProperty)((InverseObjectProperty)objectProperty).getObjectProperty();
            AtomicRole role=AtomicRole.createObjectRole(internalObjectProperty.getURI());
            return Atom.create(role,new org.semanticweb.HermiT.model.Term[] { second,first });
        }
        else
            throw new IllegalStateException("Internal error: unsupported type of object property!");
    }
    protected static LiteralConcept getLiteralConcept(Description description) {
        if (description instanceof OWLClass) {
            return AtomicConcept.create(((OWLClass)description).getURI());
        }
        else if (description instanceof ObjectNot) {
            Description internal=((ObjectNot)description).getDescription();
            if (!(internal instanceof OWLClass))
                throw new IllegalStateException("Internal error: invalid normal form.");
            return AtomicNegationConcept.create(AtomicConcept.create(((OWLClass)internal).getURI()));
        }
        else
            throw new IllegalStateException("Internal error: invalid normal form.");
    }
    protected static Role getRole(ObjectPropertyExpression objectProperty) {
        objectProperty=objectProperty.getSimplified();
        if (objectProperty instanceof ObjectProperty)
            return AtomicRole.createObjectRole(((ObjectProperty)objectProperty).getURI());
        else if (objectProperty instanceof InverseObjectProperty) {
            ObjectPropertyExpression internal=((InverseObjectProperty)objectProperty).getObjectProperty();
            if (!(internal instanceof ObjectProperty))
                throw new IllegalStateException("Internal error: invalid normal form.");
            return InverseRole.create(AtomicRole.createObjectRole(((ObjectProperty)internal).getURI()));
        }
        else
            throw new IllegalStateException("Internal error: invalid normal form.");
    }
    protected static org.semanticweb.HermiT.model.Individual getIndividual(org.semanticweb.kaon2.api.owl.elements.Individual individual) {
        return org.semanticweb.HermiT.model.Individual.create(individual.getURI());
    }
    protected static void convertRule(Rule rule,Set<DLClause> dlClauses) {
        Atom[] body=new Atom[rule.getBodyLength()];
        for (int index=0;index<rule.getBodyLength();index++)
            body[index]=convertLiteral(rule.getBodyLiteral(index));
        if (rule.isHeadConjunctive()) {
            for (int index=0;index<rule.getHeadLength();index++) {
                Atom[] head=new Atom[1];
                head[0]=convertLiteral(rule.getHeadLiteral(index));
                dlClauses.add(DLClause.create(head,body));
            }
        }
        else {
            Atom[] head=new Atom[rule.getHeadLength()];
            for (int index=0;index<rule.getHeadLength();index++)
                head[index]=convertLiteral(rule.getHeadLiteral(index));
            dlClauses.add(DLClause.create(head,body));
        }
    }
    protected static Atom convertLiteral(Literal literal) {
        DLPredicate dlPredicate=convertPredicate(literal.getPredicate());
        org.semanticweb.HermiT.model.Term[] arguments=new org.semanticweb.HermiT.model.Term[literal.getArity()];
        for (int index=0;index<literal.getArity();index++) {
            org.semanticweb.kaon2.api.logic.Term term=literal.getArgument(index);
            if (!(term instanceof org.semanticweb.kaon2.api.logic.Variable))
                throw new IllegalArgumentException("Invalid argument term.");
            arguments[index]=org.semanticweb.HermiT.model.Variable.create(term.toString());
        }
        return Atom.create(dlPredicate,arguments);
    }
    protected static DLPredicate convertPredicate(Predicate predicate) {
        if (predicate instanceof OWLClass)
            return AtomicConcept.create(((OWLClass)predicate).getURI());
        else if (predicate instanceof ObjectProperty)
            return AtomicRole.createObjectRole(((ObjectProperty)predicate).getURI());
        else if (KAON2Manager.factory().predicateSymbol(Namespaces.OWL_NS+"sameAs",2).equals(predicate))
            return Equality.INSTANCE;
        else if (KAON2Manager.factory().predicateSymbol(Namespaces.OWL_NS+"differentFrom",2).equals(predicate))
            return Inequality.INSTANCE;
        else
            throw new IllegalArgumentException("Unsupported predicate.");
    }

    protected static class Clausifier extends KAON2VisitorAdapter {
        protected final Map<AtomicConcept,AtomicConcept> m_negativeAtMostReplacements;
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;
        protected final Set<AtMostAbstractRoleGuard> m_atMostRoleGuards;
        protected final Set<Atom> m_positiveFacts;
        protected final boolean m_renameAtMost;
        protected int m_yIndex;
        
        public Clausifier(Set<Atom> positiveFacts,boolean renameAtMost) {
            m_negativeAtMostReplacements=new HashMap<AtomicConcept,AtomicConcept>();
            m_headAtoms=new ArrayList<Atom>();
            m_bodyAtoms=new ArrayList<Atom>();
            m_atMostRoleGuards=new HashSet<AtMostAbstractRoleGuard>();
            m_positiveFacts=positiveFacts;
            m_renameAtMost=renameAtMost;
        }
        public DLClause getDLClause() {
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
        protected org.semanticweb.HermiT.model.Variable nextY() {
            org.semanticweb.HermiT.model.Variable result;
            if (m_yIndex==0)
                result=Y;
            else
                result=org.semanticweb.HermiT.model.Variable.create("Y"+m_yIndex);
            m_yIndex++;
            return result;
        }
        protected AtomicConcept getConceptForNominal(org.semanticweb.kaon2.api.owl.elements.Individual individual) {
            AtomicConcept result=AtomicConcept.create("internal:nom$"+individual.getURI());
            m_positiveFacts.add(Atom.create(result,getIndividual(individual)));
            return result;
        }
        public Object visit(OWLClass object) {
            m_headAtoms.add(Atom.create(AtomicConcept.create(object.getURI()),new org.semanticweb.HermiT.model.Term[] { X }));
            return null;
        }
        public Object visit(DataNot object) {
            throw new IllegalArgumentException("DataNot is currently not supported.");
        }
        public Object visit(DataOneOf object) {
            throw new IllegalArgumentException("DataOneOf is currently not supported.");
        }
        public Object visit(DataAll object) {
            throw new IllegalArgumentException("DataAll is currently not supported.");
        }
        public Object visit(DataSome object) {
            throw new IllegalArgumentException("DataSome is currently not supported.");
        }
        public Object visit(DatatypeRestriction object) {
            throw new IllegalArgumentException("DatatypeRestriction is currently not supported.");
        }
        public Object visit(DataCardinality object) {
            throw new IllegalArgumentException("DataCardinality is currently not supported.");
        }
        public Object visit(DataHasValue object) {
            throw new IllegalArgumentException("DataHasValue is currently not supported.");
        }
        public Object visit(ObjectAll object) {
            org.semanticweb.HermiT.model.Variable y=nextY();
            m_bodyAtoms.add(getRoleAtom(object.getObjectProperty(),X,y));
            Description description=object.getDescription();
            if (description instanceof OWLClass) {
                OWLClass owlClass=(OWLClass)description;
                if (!KAON2Manager.factory().nothing().equals(owlClass))
                    m_headAtoms.add(Atom.create(AtomicConcept.create(owlClass.getURI()),new org.semanticweb.HermiT.model.Term[] { y }));
            }
            else if (description instanceof ObjectOneOf) {
                ObjectOneOf objectOneOf=(ObjectOneOf)description;
                for (org.semanticweb.kaon2.api.owl.elements.Individual individual : objectOneOf.getIndividuals()) {
                    org.semanticweb.HermiT.model.Variable yInd=nextY();
                    m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),yInd));
                    m_headAtoms.add(Atom.create(Equality.INSTANCE,y,yInd));
                }
            }
            else if (description instanceof ObjectNot) {
                Description internal=((ObjectNot)description).getDescription();
                if (internal instanceof OWLClass) {
                    OWLClass owlClass=(OWLClass)internal;
                    m_bodyAtoms.add(Atom.create(AtomicConcept.create(owlClass.getURI()),new org.semanticweb.HermiT.model.Term[] { y }));
                }
                else if (internal instanceof ObjectOneOf && ((ObjectOneOf)internal).getIndividuals().size()==1) {
                    ObjectOneOf objectOneOf=(ObjectOneOf)internal;
                    org.semanticweb.kaon2.api.owl.elements.Individual individual=objectOneOf.getIndividuals().iterator().next();
                    m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),y));
                }
                else
                    throw new IllegalStateException("Internal error: invalid normal form.");
            }
            else
                throw new IllegalStateException("Internal error: invalid normal form.");
            return null;
        }
        public Object visit(ObjectSome object) {
            ObjectPropertyExpression objectProperty=object.getObjectProperty();
            Description description=object.getDescription();
            if (description instanceof ObjectOneOf) {
                ObjectOneOf objectOneOf=(ObjectOneOf)description;
                for (org.semanticweb.kaon2.api.owl.elements.Individual individual : objectOneOf.getIndividuals()) {
                    org.semanticweb.HermiT.model.Variable y=nextY();
                    m_bodyAtoms.add(Atom.create(getConceptForNominal(individual),y));
                    m_headAtoms.add(getRoleAtom(objectProperty,X,y));
                }
            }
            else {
                LiteralConcept toConcept=getLiteralConcept(description);
                Role onRole=getRole(objectProperty);
                m_headAtoms.add(Atom.create(AtLeastAbstractRoleConcept.create(1,onRole,toConcept),new org.semanticweb.HermiT.model.Term[] { X }));
            }
            return null;
        }
        public Object visit(ObjectExistsSelf object) {
            throw new IllegalArgumentException("ObjectExistsSelf is not supported yet.");
        }
        public Object visit(ObjectCardinality object) {
            switch (object.getCardinalityType()) {
            case ObjectCardinality.MINIMUM:
                {
                    LiteralConcept toConcept=getLiteralConcept(object.getDescription());
                    Role onRole=getRole(object.getObjectProperty());
                    m_headAtoms.add(Atom.create(AtLeastAbstractRoleConcept.create(object.getCardinality(),onRole,toConcept),new org.semanticweb.HermiT.model.Term[] { X }));
                }
                break;
            case ObjectCardinality.MAXIMUM:
                if (m_renameAtMost) {
                    AtomicConcept toAtomicConcept;
                    if (object.getDescription() instanceof OWLClass)
                        toAtomicConcept=AtomicConcept.create(((OWLClass)object.getDescription()).getURI());
                    else if (object.getDescription() instanceof ObjectNot && ((ObjectNot)object.getDescription()).getDescription() instanceof OWLClass) {
                        AtomicConcept originalAtomicConcept=AtomicConcept.create(((OWLClass)((ObjectNot)object.getDescription()).getDescription()).getURI());
                        toAtomicConcept=m_negativeAtMostReplacements.get(originalAtomicConcept);
                        if (toAtomicConcept==null) {
                            toAtomicConcept=AtomicConcept.create("internal:amq#"+m_negativeAtMostReplacements.size());
                            m_negativeAtMostReplacements.put(originalAtomicConcept,toAtomicConcept);
                        }
                    }
                    else
                         throw new IllegalStateException("invalid normal form.");
                    Role onRole;
                    if (object.getObjectProperty() instanceof ObjectProperty)
                        onRole=AtomicRole.createObjectRole(((ObjectProperty)object.getObjectProperty()).getURI());
                    else {
                        ObjectProperty internalObjectProperty=(ObjectProperty)((InverseObjectProperty)object.getObjectProperty()).getObjectProperty();
                        onRole=InverseRole.create(AtomicRole.createObjectRole(internalObjectProperty.getURI()));
                    }
                    AtMostAbstractRoleGuard atMostRole=AtMostAbstractRoleGuard.create(object.getCardinality(),onRole,toAtomicConcept);
                    m_atMostRoleGuards.add(atMostRole);
                    m_headAtoms.add(Atom.create(atMostRole,new org.semanticweb.HermiT.model.Term[] { X }));
                    // This is an optimization that is described in the SHOIQ paper right after the clausification section.
                    // In order to prevent the application of the rule to the entire universe in some cases, R(x,y) \wedge C(y) to the body of the rule
                    org.semanticweb.HermiT.model.Variable Y=nextY();
                    m_bodyAtoms.add(getRoleAtom(object.getObjectProperty(),X,Y));
                    if (!AtomicConcept.THING.equals(toAtomicConcept))
                        m_bodyAtoms.add(Atom.create(toAtomicConcept,Y));
                }
                else
                    addAtMostAtoms(object.getCardinality(),object.getObjectProperty(),object.getDescription());
                break;
            case ObjectCardinality.EXACT:
                throw new IllegalStateException("Internal error: invalid normal form.");
            }
            return null;
        }
        public Object visit(ObjectOneOf object) {
            for (org.semanticweb.kaon2.api.owl.elements.Individual individual : object.getIndividuals()) {
                org.semanticweb.HermiT.model.Variable Y=nextY();
                AtomicConcept conceptForNominal=getConceptForNominal(individual);
                m_headAtoms.add(Atom.create(Equality.INSTANCE,X,Y));
                m_bodyAtoms.add(Atom.create(conceptForNominal,Y));
            }
            return null;
        }
        public Object visit(ObjectHasValue object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public Object visit(ObjectNot object) {
            Description description=object.getDescription();
            if (!(description instanceof OWLClass))
                throw new IllegalStateException("Internal error: invalid normal form.");
            m_bodyAtoms.add(Atom.create(AtomicConcept.create(((OWLClass)description).getURI()),new org.semanticweb.HermiT.model.Term[] { X }));
            return null;
        }
        public Object visit(ObjectOr object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public Object visit(ObjectAnd object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void clausifyAtMostStuff(Collection<DLClause> dlClauses) {
            for (AtMostAbstractRoleGuard atMostRole : m_atMostRoleGuards) {
                m_bodyAtoms.add(Atom.create(atMostRole,new org.semanticweb.HermiT.model.Term[] { X }));
                Role onRole=atMostRole.getOnRole();
                ObjectPropertyExpression onObjectProperty;
                if (onRole instanceof AtomicRole)
                    onObjectProperty=KAON2Manager.factory().objectProperty(((AtomicRole)onRole).getURI());
                else {
                    AtomicRole innerRole=((InverseRole)onRole).getInverseOf();
                    onObjectProperty=KAON2Manager.factory().inverseObjectProperty(KAON2Manager.factory().objectProperty(innerRole.getURI()));
                }
                addAtMostAtoms(atMostRole.getCaridnality(),onObjectProperty,KAON2Manager.factory().owlClass(atMostRole.getToAtomicConcept().getURI()));
                DLClause dlClause=getDLClause();
                dlClauses.add(dlClause);
            }
            for (Map.Entry<AtomicConcept,AtomicConcept> entry : m_negativeAtMostReplacements.entrySet()) {
                m_headAtoms.add(Atom.create(entry.getKey(),X));
                m_headAtoms.add(Atom.create(entry.getValue(),X));
                DLClause dlClause=getDLClause();
                dlClauses.add(dlClause);
            }
        }
        protected void addAtMostAtoms(int number,ObjectPropertyExpression onObjectProperty,Description toDescription) {
            ensureYNotZero();
            boolean isPositive;
            AtomicConcept atomicConcept;
            if (toDescription instanceof OWLClass) {
                isPositive=true;
                atomicConcept=AtomicConcept.create(((OWLClass)toDescription).getURI());
                if (AtomicConcept.THING.equals(atomicConcept))
                    atomicConcept=null;
            }
            else if (toDescription instanceof ObjectNot) {
                Description internal=((ObjectNot)toDescription).getDescription();
                if (!(internal instanceof OWLClass))
                    throw new IllegalStateException("Invalid ontology normal form.");
                isPositive=false;
                atomicConcept=AtomicConcept.create(((OWLClass)internal).getURI());
                if (AtomicConcept.NOTHING.equals(atomicConcept))
                    atomicConcept=null;
            }
            else 
                throw new IllegalStateException("Invalid ontology normal form.");
            org.semanticweb.HermiT.model.Variable[] yVars=new org.semanticweb.HermiT.model.Variable[number+1];
            for (int i=0;i<yVars.length;i++) {
                yVars[i]=nextY();
                m_bodyAtoms.add(getRoleAtom(onObjectProperty,X,yVars[i]));
                if (atomicConcept!=null) {
                    Atom atom=Atom.create(atomicConcept,new org.semanticweb.HermiT.model.Term[] { yVars[i] });
                    if (isPositive)
                        m_bodyAtoms.add(atom);
                    else
                        m_headAtoms.add(atom);
                }
            }
            if (yVars.length>2) // For functionality this is subsumed by the way in which the rules are applied
                for (int i=0;i<yVars.length-1;i++)
                    m_bodyAtoms.add(Atom.create(NodeIDLessThan.INSTANCE,new org.semanticweb.HermiT.model.Term[] { yVars[i],yVars[i+1] }));
            for (int i=0;i<yVars.length;i++)
                for (int j=i+1;j<yVars.length;j++)
                    m_headAtoms.add(Atom.create(Equality.INSTANCE,new org.semanticweb.HermiT.model.Term[] { yVars[i],yVars[j] }));
        }
    }
    
    protected static class FactClausifier extends KAON2VisitorAdapter {
        protected final Set<Atom> m_positiveFacts;
        protected final Set<Atom> m_negativeFacts;

        public FactClausifier(Set<Atom> positiveFacts,Set<Atom> negativeFacts) {
            m_positiveFacts=positiveFacts;
            m_negativeFacts=negativeFacts;
        }
        public Object visit(SameIndividual object) {
            org.semanticweb.kaon2.api.owl.elements.Individual[] individuals=new org.semanticweb.kaon2.api.owl.elements.Individual[object.getIndividuals().size()];
            object.getIndividuals().toArray(individuals);
            for (int i=0;i<individuals.length-1;i++)
                m_positiveFacts.add(Atom.create(Equality.create(),new org.semanticweb.HermiT.model.Term[] { getIndividual(individuals[i]),getIndividual(individuals[i+1]) }));
            return null;
        }
        public Object visit(DifferentIndividuals object) {
            org.semanticweb.kaon2.api.owl.elements.Individual[] individuals=new org.semanticweb.kaon2.api.owl.elements.Individual[object.getIndividuals().size()];
            object.getIndividuals().toArray(individuals);
            for (int i=0;i<individuals.length;i++)
                for (int j=i+1;j<individuals.length;j++)
                    m_positiveFacts.add(Atom.create(Inequality.create(),new org.semanticweb.HermiT.model.Term[] { getIndividual(individuals[i]),getIndividual(individuals[j]) }));
            return null;
        }
        public Object visit(DataPropertyMember object) {
            throw new IllegalArgumentException("DataPropertyMember is not supported yet.");
        }
        public Object visit(NegativeDataPropertyMember object) {
            throw new IllegalArgumentException("DataPropertyMember is not supported yet.");
        }
        public Object visit(ObjectPropertyMember object) {
            m_positiveFacts.add(getRoleAtom(object.getObjectProperty(),getIndividual(object.getSourceIndividual()),getIndividual(object.getTargetIndividual())));
            return null;
        }
        public Object visit(NegativeObjectPropertyMember object) {
            throw new IllegalArgumentException("NegativeObjectPropertyMember is not supported yet.");
        }
        public Object visit(ClassMember object) {
            Description description=object.getDescription();
            if (description instanceof OWLClass) {
                AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)description).getURI());
                m_positiveFacts.add(Atom.create(atomicConcept,new org.semanticweb.HermiT.model.Term[] { getIndividual(object.getIndividual()) }));
            }
            else if (description instanceof ObjectNot && ((ObjectNot)description).getDescription() instanceof OWLClass) {
                AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)((ObjectNot)description).getDescription()).getURI());
                m_negativeFacts.add(Atom.create(atomicConcept,new org.semanticweb.HermiT.model.Term[] { getIndividual(object.getIndividual()) }));
            }
            else
                throw new IllegalStateException("Internal error: invalid normal form.");
            return null;
        }
    }
    
    protected static class DetermineExpressivity extends AxiomTraversalVisitor {
        protected boolean m_hasAtMostRestrictions;
        protected boolean m_hasInverseRoles;
        protected boolean m_hasNominals;
        protected boolean m_hasReflexivity = false;
        
        public Object visit(InverseObjectProperty object) {
            m_hasInverseRoles=true;
            return super.visit(object);
        }
        public Object visit(InverseObjectProperties object) {
            m_hasInverseRoles=true;
            return super.visit(object);
        }
        public Object visit(ObjectOneOf object) {
            m_hasNominals=true;
            return super.visit(object);
        }
        public Object visit(ObjectHasValue object) {
            m_hasNominals=true;
            return super.visit(object);
        }
        public Object visit(ObjectCardinality object) {
            if (object.getCardinalityType()==ObjectCardinality.EXACT || object.getCardinalityType()==ObjectCardinality.MAXIMUM)
                m_hasAtMostRestrictions=true;
            return super.visit(object);
        }
        public Object visit(ObjectPropertyAttribute object) {
            if (object.getAttribute()==ObjectPropertyAttribute.OBJECT_PROPERTY_FUNCTIONAL || object.getAttribute()==ObjectPropertyAttribute.OBJECT_PROPERTY_INVERSE_FUNCTIONAL)
                m_hasAtMostRestrictions=true;
            return super.visit(object);
        }
    }
    
    protected static class HeadComparator implements Comparator<Atom> {
        public static final HeadComparator INSTANCE=new HeadComparator();

        public int compare(Atom o1,Atom o2) {
            int type1;
            if (o1.getDLPredicate() instanceof AtLeastAbstractRoleConcept)
                type1=2;
            else
                type1=1;
            int type2;
            if (o2.getDLPredicate() instanceof AtLeastAbstractRoleConcept)
                type2=2;
            else
                type2=1;
            return type1-type2;
        }
    }
}
