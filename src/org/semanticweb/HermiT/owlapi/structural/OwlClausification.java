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

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.AtLeastAbstractRoleConcept;
import org.semanticweb.HermiT.model.AtLeastConcreteRoleConcept;
import org.semanticweb.HermiT.model.AtMostAbstractRoleGuard;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.DatatypeRestrictionBoolean;
import org.semanticweb.HermiT.model.DatatypeRestrictionInteger;
import org.semanticweb.HermiT.model.DatatypeRestrictionString;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.NodeIDLessThan;
import org.semanticweb.HermiT.model.Role;
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
import org.semanticweb.owl.vocab.XSDVocabulary;

public class OwlClausification {
    protected static final org.semanticweb.HermiT.model.Variable X = org.semanticweb.HermiT.model.Variable.create("X");
    protected static final org.semanticweb.HermiT.model.Variable Y = org.semanticweb.HermiT.model.Variable.create("Y");
    protected static final org.semanticweb.HermiT.model.Variable Z = org.semanticweb.HermiT.model.Variable.create("Z");

    public DLOntology clausify(Reasoner.Configuration config, OWLOntology ontology,
            OWLDataFactory factory,
            Collection<DescriptionGraph> descriptionGraphs) throws OWLException {
        OwlNormalization normalization = new OwlNormalization(factory);
        normalization.processOntology(ontology);
        return clausify(config, ontology.getURI().toString(),
                normalization.getConceptInclusions(),
                normalization.getObjectPropertyInclusions(),
                normalization.getDataPropertyInclusions(),
                normalization.getAsymmetricObjectProperties(),
                normalization.getReflexiveObjectProperties(),
                normalization.getIrreflexiveObjectProperties(),
                normalization.getTransitiveObjectProperties(),
                normalization.getDisjointObjectProperties(),
                normalization.getDisjointDataProperties(),
                normalization.getFacts(), descriptionGraphs, factory);
    }

    public DLOntology clausify(Reasoner.Configuration config, String ontologyURI,
            Collection<OWLDescription[]> conceptInclusions,
            Collection<OWLObjectPropertyExpression[]> objectPropertyInclusions,
            Collection<OWLDataPropertyExpression[]> dataPropertyInclusions,
            Set<OWLObjectPropertyExpression> asymmetricObjectProperties,
            Set<OWLObjectPropertyExpression> reflexiveObjectProperties,
            Set<OWLObjectPropertyExpression> irreflexiveObjectProperties,
            Set<OWLObjectPropertyExpression> transitiveObjectProperties,
            Set<OWLObjectPropertyExpression[]> disjointObjectProperties,
            Set<OWLDataPropertyExpression[]> disjointDataProperties,
            Collection<OWLIndividualAxiom> facts,
            Collection<DescriptionGraph> descriptionGraphs,
            OWLDataFactory factory) {
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
        if (config.clausifyTransitivity) {
            for (OWLObjectPropertyExpression prop : transitiveObjectProperties) {
                DLClause dlClause = DLClause.create(
                    new Atom[] { getRoleAtom(prop, Y, Z) },
                    new Atom[] { getRoleAtom(prop, Y, X),
                                 getRoleAtom(prop, X, Z) });
                dlClauses.add(dlClause);
            }
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
        if (disjointDataProperties.size() > 0)
            throw new IllegalArgumentException("Disjoint data properties are not supported yet.");
        boolean shouldUseNIRule = determineExpressivity.m_hasAtMostRestrictions
                && determineExpressivity.m_hasInverseRoles
                && (determineExpressivity.m_hasNominals ||
                    config.existentialStrategyType ==
                        Reasoner.ExistentialStrategyType.INDIVIDUAL_REUSE);
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
    // return AtomicRole.create(((ObjectProperty)predicate).getURI());
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
        protected final Set<AtMostAbstractRoleGuard> m_atMostRoleGuards;
        protected final Set<Atom> m_positiveFacts;
        protected final boolean m_renameAtMost;
        protected int m_yIndex;
        protected final OWLDataFactory m_factory;

        public Clausifier(Set<Atom> positiveFacts, boolean renameAtMost,
                OWLDataFactory factory) {
            m_negativeAtMostReplacements = new HashMap<AtomicConcept, AtomicConcept>();
            m_headAtoms = new ArrayList<Atom>();
            m_bodyAtoms = new ArrayList<Atom>();
            m_atMostRoleGuards = new HashSet<AtMostAbstractRoleGuard>();
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
            org.semanticweb.HermiT.model.Variable y = nextY();
            m_bodyAtoms.add(getDataPropertyAtom(desc.getProperty(), X, y));
            DataVisitor dataVisitor = new DataVisitor();
            desc.getFiller().accept(dataVisitor);
            for (DataRange dataRange : dataVisitor.getDataRanges()) {
                if (!dataRange.isBottom()) {
                    m_headAtoms.add(Atom.create(dataRange,
                            new org.semanticweb.HermiT.model.Term[] { y }));
                }
            }
        }

        public void visit(OWLDataSomeRestriction desc) {
            OWLDataProperty dp = (OWLDataProperty) desc.getProperty();
            AtomicRole property = AtomicRole.createDataRole(dp.getURI().toString());
            DataVisitor dataVisitor = new DataVisitor();
            desc.getFiller().accept(dataVisitor);
            for (DataRange dataRange : dataVisitor.getDataRanges()) {
                m_headAtoms.add(Atom.create(AtLeastConcreteRoleConcept.create(1, property,
                        dataRange),
                        new org.semanticweb.HermiT.model.Term[] { X }));
            }
        }

        public void visit(OWLDataExactCardinalityRestriction desc) {
            throw new IllegalStateException(
                    "Internal error: invalid normal form.");
        }

        public void visit(OWLDataMaxCardinalityRestriction desc) {
            //throw new RuntimeException("Cardinality restrictions are not yet supported for datatypes. ");
            int number = desc.getCardinality();
            OWLDataProperty dp = (OWLDataProperty) desc.getProperty();
            DataVisitor dataVisitor = new DataVisitor();
            desc.getFiller().accept(dataVisitor);
            ensureYNotZero();
            org.semanticweb.HermiT.model.Variable[] yVars = new org.semanticweb.HermiT.model.Variable[number + 1];
            for (int i = 0; i < yVars.length; i++) {
                yVars[i] = nextY();
                m_bodyAtoms.add(getDataPropertyAtom(dp, X, yVars[i]));
                for (DataRange dataRange : dataVisitor.getDataRanges()) {
                    if (i == 0) dataRange.negate();
                    if (!dataRange.isBottom()) {
                        m_headAtoms.add(Atom.create(dataRange,
                                new org.semanticweb.HermiT.model.Term[] { yVars[i] }));
                    }
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

        public void visit(OWLDataMinCardinalityRestriction desc) {
            int number = desc.getCardinality();
            OWLDataProperty dp = (OWLDataProperty) desc.getProperty();
            AtomicRole property = AtomicRole.createDataRole(dp.getURI().toString());
            DataVisitor dataVisitor = new DataVisitor();
            desc.getFiller().accept(dataVisitor);
            for (DataRange dataRange : dataVisitor.getDataRanges()) {
                m_headAtoms.add(Atom.create(AtLeastConcreteRoleConcept.create(number,
                        property, dataRange),
                        new org.semanticweb.HermiT.model.Term[] { X }));
            }
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
                                + m_negativeAtMostReplacements.size());
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

        public void clausifyAtMostStuff(Collection<DLClause> dlClauses) {
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
        protected static OWLDataType integerDataType = factory.getOWLDataType(XSDVocabulary.INTEGER.getURI());
        protected static OWLDataType stringDataType = factory.getOWLDataType(XSDVocabulary.STRING.getURI());
        protected static OWLDataType booleanDataType = factory.getOWLDataType(XSDVocabulary.BOOLEAN.getURI());
        protected static OWLDataType literalDataType = factory.getOWLDataType(URI.create(
            "http://www.w3.org/2000/01/rdf-schema#Literal"));

        protected boolean isNegated = false;
        protected List<DataRange> dataRanges;
        protected DataRange currentDataRange;
        
        public DataVisitor() {
            dataRanges = new ArrayList<DataRange>();
        }

        public void visit(OWLDataType dataType) {
            if (integerDataType.equals(dataType)) {
                currentDataRange =  new DatatypeRestrictionInteger(); 
            } else if (stringDataType.equals(dataType)) {
                currentDataRange =  new DatatypeRestrictionString();
            } else if (literalDataType.equals(dataType)) {
                currentDataRange =  new DataRange();
            } else if (booleanDataType.equals(dataType)) {
                currentDataRange =  new DatatypeRestrictionBoolean();
            } else {
                throw new RuntimeException("Unsupported datatype.");
            }
            dataRanges.add(currentDataRange);
        }

        public void visit(OWLDataComplementOf dataComplementOf) {
            OWLDataRange range = dataComplementOf.getDataRange();
            range.accept(this);
            for (DataRange negate : dataRanges) {
                negate.negate();
            }
        }

        public void visit(OWLDataOneOf dataOneOf) {
            Set<OWLConstant> constants = dataOneOf.getValues();
            for (OWLConstant constant : constants) {
                if (constant.isTyped()) {
                    constant.asOWLTypedConstant().accept(this);
                }  else {
                    throw new RuntimeException("Untyped datatype found " + constant);
                }
            }
        }

        public void visit(OWLDataRangeRestriction rangeRestriction) {
            OWLDataRange range = rangeRestriction.getDataRange();
            range.accept(this);
            for (OWLDataRangeFacetRestriction facetRestriction : rangeRestriction.getFacetRestrictions()) {
                OWLRestrictedDataRangeFacetVocabulary facetOWL = facetRestriction.getFacet();
                OWLTypedConstant constant = facetRestriction.getFacetValue();
                DataRange.Facets facet = null;
                switch (facetOWL) {
                case LENGTH: {
                    facet = DataRange.Facets.LENGTH;
                } break;
                case MIN_INCLUSIVE: {
                    facet = DataRange.Facets.MIN_INCLUSIVE;
                } break;
                case MIN_EXCLUSIVE: {
                    facet = DataRange.Facets.MIN_EXCLUSIVE;
                } break;
                case MAX_INCLUSIVE: {
                    facet = DataRange.Facets.MAX_INCLUSIVE;
                } break;
                case MAX_EXCLUSIVE: {
                    facet = DataRange.Facets.MAX_EXCLUSIVE;
                } break;
                case FRACTION_DIGITS: {
                    facet = DataRange.Facets.FRACTION_DIGITS;
                } break;
                case MAX_LENGTH: {
                    facet = DataRange.Facets.MAX_LENGTH;
                } break;
                case MIN_LENGTH: {
                    facet = DataRange.Facets.MIN_LENGTH;
                } break;
                case PATTERN: {
                    facet = DataRange.Facets.PATTERN;
                } break;
                case TOTAL_DIGITS: {
                    facet = DataRange.Facets.TOTAL_DIGITS;
                } break;
                default:
                    throw new IllegalArgumentException("Unsupported facet.");
                }
                currentDataRange.addFacet(facet, constant.getLiteral());
            }
        }

        public void visit(OWLTypedConstant typedConstant) {
            DataRange dataRange = null;
            if (integerDataType.equals(typedConstant.getDataType())) {
                dataRange = new DatatypeRestrictionInteger();
            } else if (stringDataType.equals(typedConstant.getDataType())) {
                dataRange = new DatatypeRestrictionString();
            } else if (booleanDataType.equals(typedConstant.getDataType())) {
                dataRange = new DatatypeRestrictionBoolean();
            } else {
                throw new RuntimeException("Parsed typed constant of an unsupported data type " + typedConstant);
            }
            dataRange.addEqualsValue(typedConstant.getLiteral());
            dataRanges.add(dataRange);
        }

        public void visit(OWLUntypedConstant untypedConstant) {
            throw new RuntimeException("Parsed untyped constant " + untypedConstant);
        }

        public void visit(OWLDataRangeFacetRestriction facetRestriction) {
            throw new RuntimeException("Data range facet restrictions are not yet supported. ");
        }

        public List<DataRange> getDataRanges() {
            return dataRanges;
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
