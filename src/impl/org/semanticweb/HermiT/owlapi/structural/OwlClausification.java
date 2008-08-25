// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.owlapi.structural;

import java.net.URI;
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

import org.semanticweb.HermiT.model.AbstractRole;
import org.semanticweb.HermiT.model.AtLeastAbstractRoleConcept;
import org.semanticweb.HermiT.model.AtMostAbstractRoleGuard;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicAbstractRole;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseAbstractRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.NodeIDLessThan;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
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
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;

public class OwlClausification {
    protected static final org.semanticweb.HermiT.model.Variable X = org.semanticweb.HermiT.model.Variable.create("X");
    protected static final org.semanticweb.HermiT.model.Variable Y = org.semanticweb.HermiT.model.Variable.create("Y");

    public DLOntology clausify(boolean prepareForNIRule, OWLOntology ontology,
            OWLDataFactory factory,
            Collection<DescriptionGraph> descriptionGraphs) throws OWLException {
        OwlNormalization normalization = new OwlNormalization(factory);
        normalization.processOntology(ontology);
        return clausify(prepareForNIRule, ontology.getURI().toString(),
                normalization.getConceptInclusions(),
                normalization.getObjectPropertyInclusions(),
                normalization.getDataPropertyInclusions(),
                normalization.getAsymmetricObjectProperties(),
                normalization.getReflexiveObjectProperties(),
                normalization.getIrreflexiveObjectProperties(),
                normalization.getDisjointObjectProperties(),
                normalization.getDisjointDataProperties(),
                normalization.getFacts(), descriptionGraphs, factory);
    }

    public DLOntology clausify(boolean prepareForNIRule, String ontologyURI,
            Collection<OWLDescription[]> conceptInclusions,
            Collection<OWLObjectPropertyExpression[]> objectPropertyInclusions,
            Collection<OWLDataPropertyExpression[]> dataPropertyInclusions,
            Set<OWLObjectPropertyExpression> asymmetricObjectProperties,
            Set<OWLObjectPropertyExpression> reflexiveObjectProperties,
            Set<OWLObjectPropertyExpression> irreflexiveObjectProperties,
            Set<OWLObjectPropertyExpression[]> disjointObjectProperties,
            Set<OWLDataPropertyExpression[]> disjointDataProperties,
            Collection<OWLIndividualAxiom> facts,
            Collection<DescriptionGraph> descriptionGraphs,
            OWLDataFactory factory) throws OWLException {
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
        if (dataPropertyInclusions.size() > 0)
            throw new IllegalArgumentException(
                    "Data properties are not supported yet.");
        Set<DLClause> dlClauses = new LinkedHashSet<DLClause>();
        Set<Atom> positiveFacts = new HashSet<Atom>();
        Set<Atom> negativeFacts = new HashSet<Atom>();
        for (OWLObjectPropertyExpression[] inclusion : objectPropertyInclusions) {
            Atom subRoleAtom = getAbstractRoleAtom(inclusion[0], X, Y);
            Atom superRoleAtom = getAbstractRoleAtom(inclusion[1], X, Y);
            DLClause dlClause = DLClause.create(new Atom[] { superRoleAtom },
                    new Atom[] { subRoleAtom });
            dlClauses.add(dlClause);
        }
        for (OWLObjectPropertyExpression axiom : asymmetricObjectProperties) {
            Atom roleAtom = getAbstractRoleAtom(axiom, X, Y);
            Atom inverseRoleAtom = getAbstractRoleAtom(axiom, Y, X);
            DLClause dlClause = DLClause.create(new Atom[] {}, new Atom[] {
                    roleAtom, inverseRoleAtom });
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression axiom : reflexiveObjectProperties) {
            Atom roleAtom = getAbstractRoleAtom(axiom, X, X);
            DLClause dlClause = DLClause.create(new Atom[] { roleAtom },
                    new Atom[] {});
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression axiom : irreflexiveObjectProperties) {
            Atom roleAtom = getAbstractRoleAtom(axiom, X, X);
            DLClause dlClause = DLClause.create(new Atom[] {},
                    new Atom[] { roleAtom });
            dlClauses.add(dlClause.getSafeVersion());
        }
        for (OWLObjectPropertyExpression[] properties : disjointObjectProperties) {
            for (int i = 0; i < properties.length; i++) {
                for (int j = i + 1; j < properties.length; j++) {
                    Atom atom_i = getAbstractRoleAtom(properties[i], X, Y);
                    Atom atom_j = getAbstractRoleAtom(properties[j], X, Y);
                    DLClause dlClause = DLClause.create(new Atom[] {},
                            new Atom[] { atom_i, atom_j });
                    dlClauses.add(dlClause.getSafeVersion());
                }
            }
        }
        if (disjointDataProperties.size() > 0)
            throw new IllegalArgumentException(
                    "Data properties are not supported yet.");
        boolean shouldUseNIRule = determineExpressivity.m_hasAtMostRestrictions
                && determineExpressivity.m_hasInverseRoles
                && (determineExpressivity.m_hasNominals || prepareForNIRule);
        Clausifier clausifier = new Clausifier(positiveFacts, shouldUseNIRule,
                factory);
        for (OWLDescription[] inclusion : conceptInclusions) {
            for (OWLDescription description : inclusion)
                description.accept(clausifier);
            DLClause dlClause = clausifier.getDLClause();
            dlClauses.add(dlClause.getSafeVersion());
        }
        clausifier.clausifyAtMostStuff(dlClauses);
        FactClausifier factClausifier = new FactClausifier(positiveFacts,
                negativeFacts);
        for (OWLIndividualAxiom fact : facts)
            fact.accept(factClausifier);
        for (DescriptionGraph descriptionGraph : descriptionGraphs) {
            descriptionGraph.produceStartDLClauses(dlClauses);
        }
        return new DLOntology(ontologyURI, dlClauses, positiveFacts,
                negativeFacts, determineExpressivity.m_hasInverseRoles,
                determineExpressivity.m_hasAtMostRestrictions,
                determineExpressivity.m_hasNominals, shouldUseNIRule,
                determineExpressivity.m_hasReflexivity);
    }

    protected static Atom getAbstractRoleAtom(
            OWLObjectPropertyExpression objectProperty,
            org.semanticweb.HermiT.model.Term first,
            org.semanticweb.HermiT.model.Term second) {
        objectProperty = objectProperty.getSimplified();
        if (objectProperty instanceof OWLObjectProperty) {
            AtomicAbstractRole role = AtomicAbstractRole.create(((OWLObjectProperty) objectProperty).getURI().toString());
            return Atom.create(role, new org.semanticweb.HermiT.model.Term[] {
                    first, second });
        } else if (objectProperty instanceof OWLObjectPropertyInverse) {
            OWLObjectProperty internalObjectProperty = (OWLObjectProperty) ((OWLObjectPropertyInverse) objectProperty).getInverse();
            AtomicAbstractRole role = AtomicAbstractRole.create(internalObjectProperty.getURI().toString());
            return Atom.create(role, new org.semanticweb.HermiT.model.Term[] {
                    second, first });
        } else
            throw new IllegalStateException(
                    "Internal error: unsupported type of object property!");
    }

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

    protected static AbstractRole getAbstractRole(
            OWLObjectPropertyExpression objectProperty) {
        objectProperty = objectProperty.getSimplified();
        if (objectProperty instanceof OWLObjectProperty)
            return AtomicAbstractRole.create(((OWLObjectProperty) objectProperty).getURI().toString());
        else if (objectProperty instanceof OWLObjectPropertyInverse) {
            OWLObjectPropertyExpression internal = ((OWLObjectPropertyInverse) objectProperty).getInverse();
            if (!(internal instanceof OWLObjectProperty)) {
                throw new IllegalStateException(
                        "Internal error: invalid normal form.");
            }
            return InverseAbstractRole.create(AtomicAbstractRole.create(((OWLObjectProperty) internal).getURI().toString()));
        } else
            throw new IllegalStateException(
                    "Internal error: invalid normal form.");
    }

    protected static org.semanticweb.HermiT.model.Individual getIndividual(
            OWLIndividual individual) {
        return org.semanticweb.HermiT.model.Individual.create(individual.getURI().toString());
    }

    // protected static void convertRule(Rule rule,Set<DLClause> dlClauses) {
    // Atom[] body=new Atom[rule.getBodyLength()];
    // for (int index=0;index<rule.getBodyLength();index++)
    // body[index]=convertLiteral(rule.getBodyLiteral(index));
    // if (rule.isHeadConjunctive()) {
    // for (int index=0;index<rule.getHeadLength();index++) {
    // Atom[] head=new Atom[1];
    // head[0]=convertLiteral(rule.getHeadLiteral(index));
    // dlClauses.add(DLClause.create(head,body));
    // }
    // }
    // else {
    // Atom[] head=new Atom[rule.getHeadLength()];
    // for (int index=0;index<rule.getHeadLength();index++)
    // head[index]=convertLiteral(rule.getHeadLiteral(index));
    // dlClauses.add(DLClause.create(head,body));
    // }
    // }
    // protected static Atom convertLiteral(Literal literal) {
    // DLPredicate dlPredicate=convertPredicate(literal.getPredicate());
    // org.semanticweb.HermiT.model.Term[] arguments=new
    // org.semanticweb.HermiT.model.Term[literal.getArity()];
    // for (int index=0;index<literal.getArity();index++) {
    // org.semanticweb.kaon2.api.logic.Term term=literal.getArgument(index);
    // if (!(term instanceof org.semanticweb.kaon2.api.logic.Variable))
    // throw new IllegalArgumentException("Invalid argument term.");
    //arguments[index]=org.semanticweb.HermiT.model.Variable.create(term.toString
    // ());
    // }
    // return Atom.create(dlPredicate,arguments);
    // }
    // protected static DLPredicate convertPredicate(Predicate predicate) {
    // if (predicate instanceof OWLClass)
    // return AtomicConcept.create(((OWLClass)predicate).getURI());
    // else if (predicate instanceof ObjectProperty)
    // return AtomicAbstractRole.create(((ObjectProperty)predicate).getURI());
    // else if
    // (KAON2Manager.factory().predicateSymbol(Namespaces.OWL_NS+"sameAs"
    // ,2).equals(predicate))
    // return Equality.INSTANCE;
    // else if
    // (KAON2Manager.factory().predicateSymbol(Namespaces.OWL_NS+"differentFrom"
    // ,2).equals(predicate))
    // return Inequality.INSTANCE;
    // else
    // throw new IllegalArgumentException("Unsupported predicate.");
    // }

    protected static class Clausifier implements OWLDescriptionVisitor {
        protected final Map<AtomicConcept, AtomicConcept> m_negativeAtMostReplacements;
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;
        protected final Set<AtMostAbstractRoleGuard> m_atMostAbstractRoleGuards;
        protected final Set<Atom> m_positiveFacts;
        protected final boolean m_renameAtMost;
        protected int m_yIndex;
        protected final OWLDataFactory m_factory;

        public Clausifier(Set<Atom> positiveFacts, boolean renameAtMost,
                OWLDataFactory factory) {
            m_negativeAtMostReplacements = new HashMap<AtomicConcept, AtomicConcept>();
            m_headAtoms = new ArrayList<Atom>();
            m_bodyAtoms = new ArrayList<Atom>();
            m_atMostAbstractRoleGuards = new HashSet<AtMostAbstractRoleGuard>();
            m_positiveFacts = positiveFacts;
            m_renameAtMost = renameAtMost;
            m_factory = factory;
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
            throw new RuntimeException("Datatypes are not supported yet.");
        }

        public void visit(OWLDataExactCardinalityRestriction desc) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }

        public void visit(OWLDataMaxCardinalityRestriction desc) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }

        public void visit(OWLDataMinCardinalityRestriction desc) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }

        public void visit(OWLDataSomeRestriction desc) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }

        public void visit(OWLDataValueRestriction desc) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }

        public void visit(OWLObjectAllRestriction object) {
            org.semanticweb.HermiT.model.Variable y = nextY();
            m_bodyAtoms.add(getAbstractRoleAtom(object.getProperty(), X, y));
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
                    m_headAtoms.add(getAbstractRoleAtom(objectProperty, X, y));
                }
            } else {
                LiteralConcept toConcept = getLiteralConcept(description);
                AbstractRole onAbstractRole = getAbstractRole(objectProperty);
                m_headAtoms.add(Atom.create(AtLeastAbstractRoleConcept.create(
                        1, onAbstractRole, toConcept),
                        new org.semanticweb.HermiT.model.Term[] { X }));
            }
        }

        public void visit(OWLObjectSelfRestriction object) {
            OWLObjectPropertyExpression objectProperty = object.getProperty();
            Atom roleAtom = getAbstractRoleAtom(objectProperty, X, X);
            m_headAtoms.add(roleAtom);
        }

        public void visit(OWLObjectMinCardinalityRestriction object) {
            LiteralConcept toConcept = getLiteralConcept(object.getFiller());
            AbstractRole onAbstractRole = getAbstractRole(object.getProperty());
            m_headAtoms.add(Atom.create(AtLeastAbstractRoleConcept.create(
                    object.getCardinality(), onAbstractRole, toConcept),
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
                                + m_negativeAtMostReplacements.size());
                        m_negativeAtMostReplacements.put(originalAtomicConcept,
                                toAtomicConcept);
                    }
                } else
                    throw new IllegalStateException("invalid normal form.");
                AbstractRole onAbstractRole;
                if (object.getProperty() instanceof OWLObjectProperty) {
                    onAbstractRole = AtomicAbstractRole.create(((OWLObjectProperty) object.getProperty()).getURI().toString());
                } else {
                    OWLObjectProperty internalObjectProperty = (OWLObjectProperty) ((OWLObjectPropertyInverse) object.getProperty()).getInverse();
                    onAbstractRole = InverseAbstractRole.create(AtomicAbstractRole.create(internalObjectProperty.getURI().toString()));
                }
                AtMostAbstractRoleGuard atMostAbstractRole = AtMostAbstractRoleGuard.create(
                        object.getCardinality(), onAbstractRole,
                        toAtomicConcept);
                m_atMostAbstractRoleGuards.add(atMostAbstractRole);
                m_headAtoms.add(Atom.create(atMostAbstractRole,
                        new org.semanticweb.HermiT.model.Term[] { X }));
                // This is an optimization that is described in the SHOIQ paper
                // right after the clausification section.
                // In order to prevent the application of the rule to the entire
                // universe in some cases, R(x,y) \wedge C(y) to the body of the
                // rule
                org.semanticweb.HermiT.model.Variable Y = nextY();
                m_bodyAtoms.add(getAbstractRoleAtom(object.getProperty(), X, Y));
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
                    Atom roleAtom = getAbstractRoleAtom(objectProperty, X, X);
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

        public void clausifyAtMostStuff(Collection<DLClause> dlClauses) {
            for (AtMostAbstractRoleGuard atMostAbstractRole : m_atMostAbstractRoleGuards) {
                m_bodyAtoms.add(Atom.create(atMostAbstractRole,
                        new org.semanticweb.HermiT.model.Term[] { X }));
                AbstractRole onAbstractRole = atMostAbstractRole.getOnAbstractRole();
                OWLObjectPropertyExpression onObjectProperty;
                if (onAbstractRole instanceof AtomicAbstractRole) {
                    onObjectProperty = m_factory.getOWLObjectProperty(URI.create(((AtomicAbstractRole) onAbstractRole).getURI().toString()));
                } else {
                    AtomicAbstractRole innerAbstractRole = ((InverseAbstractRole) onAbstractRole).getInverseOf();
                    onObjectProperty = m_factory.getOWLObjectPropertyInverse(m_factory.getOWLObjectProperty(URI.create(innerAbstractRole.getURI().toString())));
                }
                addAtMostAtoms(
                        atMostAbstractRole.getCaridnality(),
                        onObjectProperty,
                        m_factory.getOWLClass(URI.create(atMostAbstractRole.getToAtomicConcept().getURI().toString())));
                DLClause dlClause = getDLClause();
                dlClauses.add(dlClause);
            }
            for (Map.Entry<AtomicConcept, AtomicConcept> entry : m_negativeAtMostReplacements.entrySet()) {
                m_headAtoms.add(Atom.create(entry.getKey(), X));
                m_headAtoms.add(Atom.create(entry.getValue(), X));
                DLClause dlClause = getDLClause();
                dlClauses.add(dlClause);
            }
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
                m_bodyAtoms.add(getAbstractRoleAtom(onObjectProperty, X,
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

        public void visit(OWLDataPropertyAssertionAxiom object) {
            throw new IllegalArgumentException(
                    "DataPropertyMember is not supported yet.");
        }

        public void visit(OWLNegativeDataPropertyAssertionAxiom object) {
            throw new IllegalArgumentException(
                    "DataPropertyMember is not supported yet.");
        }

        public void visit(OWLObjectPropertyAssertionAxiom object) {
            m_positiveFacts.add(getAbstractRoleAtom(object.getProperty(),
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
                m_positiveFacts.add(getAbstractRoleAtom(
                        selfRestriction.getProperty(),
                        getIndividual(object.getIndividual()),
                        getIndividual(object.getIndividual())));
            } else if (description instanceof OWLObjectComplementOf
                    && ((OWLObjectComplementOf) description).getOperand() instanceof OWLObjectSelfRestriction) {
                OWLObjectSelfRestriction selfRestriction = (OWLObjectSelfRestriction) (((OWLObjectComplementOf) description).getOperand());
                m_negativeFacts.add(getAbstractRoleAtom(
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

        protected void checkProperty(OWLObjectPropertyExpression p) {
            if (p instanceof OWLObjectPropertyInverse)
                m_hasInverseRoles = true;
        }

        public void visit(OWLDataAllRestriction desc) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }

        public void visit(OWLDataExactCardinalityRestriction desc) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }

        public void visit(OWLDataMaxCardinalityRestriction desc) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }

        public void visit(OWLDataMinCardinalityRestriction desc) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }

        public void visit(OWLDataSomeRestriction desc) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }

        public void visit(OWLDataValueRestriction desc) {
            throw new RuntimeException("Datatypes are not supported yet.");
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
