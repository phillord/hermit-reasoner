package org.semanticweb.HermiT.kaon2.structural;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.semanticweb.kaon2.api.*;
import org.semanticweb.kaon2.api.owl.elements.*;
import org.semanticweb.kaon2.api.owl.axioms.*;
import org.semanticweb.kaon2.api.logic.*;

import org.semanticweb.HermiT.model.*;

/**
 * This class implements the clausification part of the HermiT algorithm;
 */
public class Clausification {
    protected static final org.semanticweb.HermiT.model.Variable X=org.semanticweb.HermiT.model.Variable.create("X");
    protected static final org.semanticweb.HermiT.model.Variable Y=org.semanticweb.HermiT.model.Variable.create("Y");

    public DLOntology clausify(Ontology ontology,boolean processTransitivity,Collection<DescriptionGraph> descriptionGraphs) throws KAON2Exception {
        Normalization normalization=new Normalization(processTransitivity);
        normalization.processOntology(ontology);
        return clausify(ontology.getOntologyURI(),normalization.getConceptInclusions(),normalization.getNormalObjectPropertyInclusions(),normalization.getInverseObjectPropertyInclusions(),normalization.getFacts(),descriptionGraphs,normalization.getRules());
    }
    public DLOntology clausify(String ontologyURI,Collection<Description[]> conceptInclusions,Collection<ObjectPropertyExpression[]> normalObjectPropertyInclusions,Collection<ObjectPropertyExpression[]> inverseObjectPropertyInclusions,Collection<Fact> facts,Collection<DescriptionGraph> descriptionGraphs,Collection<Rule> additionalRules) throws KAON2Exception {
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
        Set<DLClause> dlClauses=new HashSet<DLClause>();
        Set<Atom> positiveFacts=new HashSet<Atom>();
        Set<Atom> negativeFacts=new HashSet<Atom>();
        for (ObjectPropertyExpression[] inclusion : normalObjectPropertyInclusions) {
            Atom subRoleAtom=getAbstractRoleAtom(inclusion[0],X,Y);
            Atom superRoleAtom=getAbstractRoleAtom(inclusion[1],X,Y);
            DLClause dlClause=DLClause.create(new Atom[][] { { superRoleAtom } },new Atom[] { subRoleAtom });
            dlClauses.add(dlClause);
        }
        for (ObjectPropertyExpression[] inclusion : inverseObjectPropertyInclusions) {
            Atom subRoleAtom=getAbstractRoleAtom(inclusion[0],X,Y);
            Atom superRoleAtom=getAbstractRoleAtom(inclusion[1],Y,X);
            DLClause dlClause=DLClause.create(new Atom[][] { { superRoleAtom } },new Atom[] { subRoleAtom });
            dlClauses.add(dlClause);
        }
        Clausifier clausifier=new Clausifier(positiveFacts,determineExpressivity.m_hasAtMostRestrictions && determineExpressivity.m_hasInverseRoles && determineExpressivity.m_hasNominals);
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
            dlClauses.add(convertRule(rule));
        return new DLOntology(ontologyURI,dlClauses,positiveFacts,negativeFacts,determineExpressivity.m_hasInverseRoles,determineExpressivity.m_hasAtMostRestrictions,determineExpressivity.m_hasNominals);
    }
    protected static Atom getAbstractRoleAtom(ObjectPropertyExpression objectProperty,org.semanticweb.HermiT.model.Term first,org.semanticweb.HermiT.model.Term second) {
        objectProperty=objectProperty.getSimplified();
        if (objectProperty instanceof ObjectProperty) {
            AtomicAbstractRole role=AtomicAbstractRole.create(((ObjectProperty)objectProperty).getURI());
            return Atom.create(role,new org.semanticweb.HermiT.model.Term[] { first,second });
        }
        else if (objectProperty instanceof InverseObjectProperty) {
            ObjectProperty internalObjectProperty=(ObjectProperty)((InverseObjectProperty)objectProperty).getObjectProperty();
            AtomicAbstractRole role=AtomicAbstractRole.create(internalObjectProperty.getURI());
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
    protected static AbstractRole getAbstractRole(ObjectPropertyExpression objectProperty) {
        objectProperty=objectProperty.getSimplified();
        if (objectProperty instanceof ObjectProperty)
            return AtomicAbstractRole.create(((ObjectProperty)objectProperty).getURI());
        else if (objectProperty instanceof InverseObjectProperty) {
            ObjectPropertyExpression internal=((InverseObjectProperty)objectProperty).getObjectProperty();
            if (!(internal instanceof ObjectProperty))
                throw new IllegalStateException("Internal error: invalid normal form.");
            return InverseAbstractRole.create(AtomicAbstractRole.create(((ObjectProperty)internal).getURI()));
        }
        else
            throw new IllegalStateException("Internal error: invalid normal form.");
    }
    protected static org.semanticweb.HermiT.model.Individual getIndividual(org.semanticweb.kaon2.api.owl.elements.Individual individual) {
        return org.semanticweb.HermiT.model.Individual.create(individual.getURI());
    }
    protected static DLClause convertRule(Rule rule) {
        Atom[][] head;
        if (rule.isHeadConjunctive()) {
            head=new Atom[1][rule.getHeadLength()];
            for (int index=0;index<rule.getHeadLength();index++)
                head[0][index]=convertLiteral(rule.getHeadLiteral(index));
        }
        else {
            head=new Atom[rule.getHeadLength()][1];
            for (int index=0;index<rule.getHeadLength();index++)
                head[index][0]=convertLiteral(rule.getHeadLiteral(index));
        }
        Atom[] body=new Atom[rule.getBodyLength()];
        for (int index=0;index<rule.getBodyLength();index++)
            body[index]=convertLiteral(rule.getBodyLiteral(index));
        return DLClause.create(head,body);
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
            return AtomicAbstractRole.create(((ObjectProperty)predicate).getURI());
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
        protected final Set<AtMostAbstractRoleGuard> m_atMostAbstractRoleGuards;
        protected final Set<Atom> m_positiveFacts;
        protected final boolean m_renameAtMost;
        protected int m_yIndex;
        
        public Clausifier(Set<Atom> positiveFacts,boolean renameAtMost) {
            m_negativeAtMostReplacements=new HashMap<AtomicConcept,AtomicConcept>();
            m_headAtoms=new ArrayList<Atom>();
            m_bodyAtoms=new ArrayList<Atom>();
            m_atMostAbstractRoleGuards=new HashSet<AtMostAbstractRoleGuard>();
            m_positiveFacts=positiveFacts;
            m_renameAtMost=renameAtMost;
        }
        public DLClause getDLClause() {
            Atom[][] headAtoms=new Atom[m_headAtoms.size()][1];
            for (int index=0;index<m_headAtoms.size();index++)
                headAtoms[index][0]=m_headAtoms.get(index);
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
            m_bodyAtoms.add(getAbstractRoleAtom(object.getObjectProperty(),X,y));
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
                    m_headAtoms.add(getAbstractRoleAtom(objectProperty,X,y));
                }
            }
            else {
                LiteralConcept toConcept=getLiteralConcept(description);
                AbstractRole onAbstractRole=getAbstractRole(objectProperty);
                m_headAtoms.add(Atom.create(AtLeastAbstractRoleConcept.create(1,onAbstractRole,toConcept),new org.semanticweb.HermiT.model.Term[] { X }));
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
                    AbstractRole onAbstractRole=getAbstractRole(object.getObjectProperty());
                    m_headAtoms.add(Atom.create(AtLeastAbstractRoleConcept.create(object.getCardinality(),onAbstractRole,toConcept),new org.semanticweb.HermiT.model.Term[] { X }));
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
                    AbstractRole onAbstractRole;
                    if (object.getObjectProperty() instanceof ObjectProperty)
                        onAbstractRole=AtomicAbstractRole.create(((ObjectProperty)object.getObjectProperty()).getURI());
                    else {
                        ObjectProperty internalObjectProperty=(ObjectProperty)((InverseObjectProperty)object.getObjectProperty()).getObjectProperty();
                        onAbstractRole=InverseAbstractRole.create(AtomicAbstractRole.create(internalObjectProperty.getURI()));
                    }
                    AtMostAbstractRoleGuard atMostAbstractRole=AtMostAbstractRoleGuard.create(object.getCardinality(),onAbstractRole,toAtomicConcept);
                    m_atMostAbstractRoleGuards.add(atMostAbstractRole);
                    m_headAtoms.add(Atom.create(atMostAbstractRole,new org.semanticweb.HermiT.model.Term[] { X }));
                    // This is an optimization that is described in the SHOIQ paper right after the clausification section.
                    // In order to prevent the application of the rule to the entire universe in some cases, R(x,y) \wedge C(y) to the body of the rule
                    org.semanticweb.HermiT.model.Variable Y=nextY();
                    m_bodyAtoms.add(getAbstractRoleAtom(object.getObjectProperty(),X,Y));
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
            for (AtMostAbstractRoleGuard atMostAbstractRole : m_atMostAbstractRoleGuards) {
                m_bodyAtoms.add(Atom.create(atMostAbstractRole,new org.semanticweb.HermiT.model.Term[] { X }));
                AbstractRole onAbstractRole=atMostAbstractRole.getOnAbstractRole();
                ObjectPropertyExpression onObjectProperty;
                if (onAbstractRole instanceof AtomicAbstractRole)
                    onObjectProperty=KAON2Manager.factory().objectProperty(((AtomicAbstractRole)onAbstractRole).getURI());
                else {
                    AtomicAbstractRole innerAbstractRole=((InverseAbstractRole)onAbstractRole).getInverseOf();
                    onObjectProperty=KAON2Manager.factory().inverseObjectProperty(KAON2Manager.factory().objectProperty(innerAbstractRole.getURI()));
                }
                addAtMostAtoms(atMostAbstractRole.getCaridnality(),onObjectProperty,KAON2Manager.factory().owlClass(atMostAbstractRole.getToAtomicConcept().getURI()));
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
                m_bodyAtoms.add(getAbstractRoleAtom(onObjectProperty,X,yVars[i]));
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
            m_positiveFacts.add(getAbstractRoleAtom(object.getObjectProperty(),getIndividual(object.getSourceIndividual()),getIndividual(object.getTargetIndividual())));
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
}
