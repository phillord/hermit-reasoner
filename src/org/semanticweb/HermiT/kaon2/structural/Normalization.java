package org.semanticweb.HermiT.kaon2.structural;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.kaon2.api.*;
import org.semanticweb.kaon2.api.owl.elements.*;
import org.semanticweb.kaon2.api.owl.axioms.*;
import org.semanticweb.kaon2.api.logic.*;

/**
 * This class implements the structural transformation from our new tableau paper. This transformation departs in the following way from the paper:
 * it keeps the concepts of the form \exists R.{ a_1, ..., a_n }, \forall R.{ a_1, ..., a_n }, and \forall R.\neg { a } intact.
 * These concepts are then clausified in a more efficient way.
 */
public class Normalization2 {
    protected final Map<Description,Description> m_definitions;
    protected final Map<ObjectOneOf,OWLClass> m_definitionsForNegativeNominals;
    protected final Collection<Description[]> m_conceptInclusions;
    protected final Collection<ObjectPropertyExpression[]> m_normalObjectPropertyInclusions;
    protected final Collection<ObjectPropertyExpression[]> m_inverseObjectPropertyInclusions;
    protected final Collection<DataPropertyExpression[]> m_normalDataPropertyInclusions;
    protected final Collection<Fact> m_facts;
    protected final Collection<Rule> m_rules;
    
    public Normalization2() {
        m_definitions=new HashMap<Description,Description>();
        m_definitionsForNegativeNominals=new HashMap<ObjectOneOf,OWLClass>();
        m_conceptInclusions=new ArrayList<Description[]>();
        m_normalObjectPropertyInclusions=new ArrayList<ObjectPropertyExpression[]>(); 
        m_inverseObjectPropertyInclusions=new ArrayList<ObjectPropertyExpression[]>();
        m_normalDataPropertyInclusions=new ArrayList<DataPropertyExpression[]>();
        m_facts=new HashSet<Fact>();
        m_rules=new HashSet<Rule>();
    }
    public Collection<Description[]> getConceptInclusions() {
        return m_conceptInclusions;
    }
    public Collection<ObjectPropertyExpression[]> getNormalObjectPropertyInclusions() {
        return m_normalObjectPropertyInclusions;
    }
    public Collection<ObjectPropertyExpression[]> getInverseObjectPropertyInclusions() {
        return m_inverseObjectPropertyInclusions;
    }
    public Collection<DataPropertyExpression[]> getNormalDataPropertyInclusios() {
        return m_normalDataPropertyInclusions;
    }
    public Collection<Fact> getFacts() {
        return m_facts;
    }
    public Collection<Rule> getRules() {
        return m_rules;
    }
    public void processOntology(Ontology ontology) throws KAON2Exception {
        TransitivityManager transitivityManager=new TransitivityManager();
        for (InverseObjectProperties axiom : ontology.createAxiomRequest(InverseObjectProperties.class).getAll()) {
            ObjectPropertyExpression first=axiom.getFirst().getSimplified();
            ObjectPropertyExpression second=axiom.getSecond().getSimplified();
            transitivityManager.addInclusion(first,second.getInverseObjectProperty().getSimplified());
            transitivityManager.addInclusion(second,first.getInverseObjectProperty().getSimplified());
            m_inverseObjectPropertyInclusions.add(new ObjectPropertyExpression[] { first,second  });
            m_inverseObjectPropertyInclusions.add(new ObjectPropertyExpression[] { second,first  });
        }
        for (SubObjectPropertyOf axiom : ontology.createAxiomRequest(SubObjectPropertyOf.class).getAll()) {
            if (axiom.getSubObjectProperties().size()!=1)
                throw new KAON2Exception("General SubObjectProperty axioms are not supported.");
            ObjectPropertyExpression subObjectProperty=axiom.getSubObjectProperties().get(0).getSimplified();
            ObjectPropertyExpression superObjectProperty=axiom.getSuperObjectProperty().getSimplified();
            transitivityManager.addInclusion(subObjectProperty,superObjectProperty);
            m_normalObjectPropertyInclusions.add(new ObjectPropertyExpression[] { subObjectProperty,superObjectProperty });
        }
        for (EquivalentObjectProperties axiom : ontology.createAxiomRequest(EquivalentObjectProperties.class).getAll()) {
            ObjectPropertyExpression[] objectProperties=new ObjectPropertyExpression[axiom.getObjectProperties().size()];
            axiom.getObjectProperties().toArray(objectProperties);
            for (int i=0;i<objectProperties.length;i++)
                objectProperties[i]=objectProperties[i].getSimplified();
            for (int i=0;i<objectProperties.length-1;i++) {
                transitivityManager.addInclusion(objectProperties[i],objectProperties[i+1]);
                transitivityManager.addInclusion(objectProperties[i+1],objectProperties[i]);
                m_normalObjectPropertyInclusions.add(new ObjectPropertyExpression[] { objectProperties[i],objectProperties[i+1] });
                m_normalObjectPropertyInclusions.add(new ObjectPropertyExpression[] { objectProperties[i+1],objectProperties[i] });
            }
        }
        for (SubDataPropertyOf axiom : ontology.createAxiomRequest(SubDataPropertyOf.class).getAll()) {
            DataPropertyExpression subDataProperty=axiom.getSubDataProperty();
            DataPropertyExpression superDataProperty=axiom.getSuperDataProperty();
            m_normalDataPropertyInclusions.add(new DataPropertyExpression[] { subDataProperty,superDataProperty });
        }
        for (EquivalentDataProperties axiom : ontology.createAxiomRequest(EquivalentDataProperties.class).getAll()) {
            DataPropertyExpression[] dataProperties=new DataPropertyExpression[axiom.getDataProperties().size()];
            axiom.getDataProperties().toArray(dataProperties);
            for (int i=0;i<dataProperties.length-1;i++) {
                m_normalDataPropertyInclusions.add(new DataPropertyExpression[] { dataProperties[i],dataProperties[i+1] });
                m_normalDataPropertyInclusions.add(new DataPropertyExpression[] { dataProperties[i+1],dataProperties[i] });
            }
        }
        List<Description[]> inclusions=new ArrayList<Description[]>();
        for (SubClassOf axiom : ontology.createAxiomRequest(SubClassOf.class).getAll()) { 
            inclusions.add(new Description[] { axiom.getSubDescription().getComplementNNF(),axiom.getSuperDescription().getNNF() });
        }
        for (EquivalentClasses axiom : ontology.createAxiomRequest(EquivalentClasses.class).getAll()) {
            Description[] descriptions=new Description[axiom.getDescriptions().size()];
            axiom.getDescriptions().toArray(descriptions);
            for (int i=0;i<descriptions.length-1;i++) {
                inclusions.add(new Description[] { descriptions[i].getComplementNNF(),descriptions[i+1].getNNF() });
                inclusions.add(new Description[] { descriptions[i+1].getComplementNNF(),descriptions[i].getNNF() });
            }
        }
        for (DisjointClasses axiom : ontology.createAxiomRequest(DisjointClasses.class).getAll()) {
            Description[] descriptions=new Description[axiom.getDescriptions().size()];
            axiom.getDescriptions().toArray(descriptions);
            for (int i=0;i<descriptions.length;i++)
                descriptions[i]=descriptions[i].getComplementNNF();
            for (int i=0;i<descriptions.length;i++)
                for (int j=i+1;j<descriptions.length;j++)
                    inclusions.add(new Description[] { descriptions[i],descriptions[j] });
        }
        for (ObjectPropertyAttribute axiom : ontology.createAxiomRequest(ObjectPropertyAttribute.class).getAll()) {
            switch (axiom.getAttribute()) {
            case ObjectPropertyAttribute.OBJECT_PROPERTY_FUNCTIONAL:
                inclusions.add(new Description[] { KAON2Manager.factory().objectCardinality(ObjectCardinality.MAXIMUM,1,axiom.getObjectProperty().getSimplified(),KAON2Manager.factory().thing()) });
                break;
            case ObjectPropertyAttribute.OBJECT_PROPERTY_INVERSE_FUNCTIONAL:
                inclusions.add(new Description[] { KAON2Manager.factory().objectCardinality(ObjectCardinality.MAXIMUM,1,axiom.getObjectProperty().getSimplified().getInverseObjectProperty(),KAON2Manager.factory().thing()) });
                break;
            case ObjectPropertyAttribute.OBJECT_PROPERTY_SYMMETRIC:
                {
                    ObjectPropertyExpression objectProperty=axiom.getObjectProperty().getSimplified();
                    m_inverseObjectPropertyInclusions.add(new ObjectPropertyExpression[] { objectProperty,objectProperty });
                    transitivityManager.addInclusion(objectProperty,objectProperty.getInverseObjectProperty().getSimplified());
                }
                break;
            case ObjectPropertyAttribute.OBJECT_PROPERTY_TRANSITIVE:
                transitivityManager.makeTransitive(axiom.getObjectProperty().getSimplified());
                break;
            }
        }
        for (DataPropertyAttribute axiom : ontology.createAxiomRequest(DataPropertyAttribute.class).getAll()) {
            switch (axiom.getAttribute()) {
            case DataPropertyAttribute.DATA_PROPERTY_FUNCTIONAL:
                inclusions.add(new Description[] { KAON2Manager.factory().dataCardinality(DataCardinality.MAXIMUM,1,axiom.getDataProperty(),KAON2Manager.factory().rdfsLiteral()) });
                break;
            }
        }
        for (ObjectPropertyDomain axiom : ontology.createAxiomRequest(ObjectPropertyDomain.class).getAll()) {
            ObjectAll allPropertyNohting=KAON2Manager.factory().objectAll(axiom.getObjectProperty().getSimplified(),KAON2Manager.factory().nothing());
            inclusions.add(new Description[] { axiom.getDomain(),allPropertyNohting });
        }
        for (ObjectPropertyRange axiom : ontology.createAxiomRequest(ObjectPropertyRange.class).getAll()) {
            ObjectAll allPropertyRange=KAON2Manager.factory().objectAll(axiom.getObjectProperty().getSimplified(),axiom.getRange().getNNF());
            inclusions.add(new Description[] { allPropertyRange });
        }
        for (DataPropertyDomain axiom : ontology.createAxiomRequest(DataPropertyDomain.class).getAll()) {
            DataAll allPropertyNohting=KAON2Manager.factory().dataAll(KAON2Manager.factory().dataNot(KAON2Manager.factory().rdfsLiteral()),axiom.getDataProperty());
            inclusions.add(new Description[] { axiom.getDomain(),allPropertyNohting });
        }
        for (DataPropertyRange axiom : ontology.createAxiomRequest(DataPropertyRange.class).getAll()) {
            DataAll allPropertyRange=KAON2Manager.factory().dataAll(axiom.getRange().getNNF(),axiom.getDataProperty());
            inclusions.add(new Description[] { allPropertyRange });
        }
        KAON2Visitor normalizer=new NormalizationVisitor(inclusions);
        boolean[] alreadyExists=new boolean[1];
        for (Fact fact : ontology.createAxiomRequest(Fact.class).getAll()) {
            if (fact instanceof ClassMember) {
                ClassMember classMember=(ClassMember)fact;
                Description description=classMember.getDescription().getNNF().getSimplified();
                if (!isSimple(description)) {
                    Description definition=getDefinitionFor(description,alreadyExists);
                    if (!alreadyExists[0])
                        inclusions.add(new Description[] { definition.getComplementNNF(),description });
                    description=definition;
                }
                if (description==classMember.getDescription())
                    m_facts.add(fact);
                else
                    m_facts.add(KAON2Manager.factory().classMember(description,classMember.getIndividual()));
            }
            else if (fact instanceof ObjectPropertyMember) {
                ObjectPropertyMember objectPropertyMember=(ObjectPropertyMember)fact;
                m_facts.add(KAON2Manager.factory().objectPropertyMember(objectPropertyMember.getObjectProperty().getSimplified(),objectPropertyMember.getSourceIndividual(),objectPropertyMember.getTargetIndividual()));
            }
            else if (fact instanceof SameIndividual || fact instanceof DifferentIndividuals) {
                m_facts.add(fact);
            }
            else if (!(fact instanceof EntityAnnotation))
                throw new KAON2Exception("Unsupported type of fact encountered.");
        }
        nomalizeInclusions(inclusions,normalizer);
        // process transitivity
        transitivityManager.transitivelyClose();
        for (Description[] inclusion : m_conceptInclusions) {
            for (int index=0;index<inclusion.length;index++)
                inclusion[index]=transitivityManager.replaceDescriptionIfNecessary(inclusion[index]);
        }
        transitivityManager.generateTransitivityAxioms();
        // add the rules
        m_rules.addAll(ontology.createAxiomRequest(Rule.class).getAll());
    }
    protected void nomalizeInclusions(List<Description[]> inclusions,KAON2Visitor normalizer) throws KAON2Exception {
        while (!inclusions.isEmpty()) {
            Description simplifiedDescription=KAON2Manager.factory().objectOr(inclusions.remove(inclusions.size()-1)).getSimplified();
            if (!KAON2Manager.factory().thing().equals(simplifiedDescription)) {
                if (simplifiedDescription instanceof ObjectOr) {
                    ObjectOr objectOr=(ObjectOr)simplifiedDescription;
                    Description[] descriptions=new Description[objectOr.getDescriptions().size()];
                    objectOr.getDescriptions().toArray(descriptions);
                    if (!distributeUnionOverAnd(descriptions,inclusions) && !optimizedNegativeOneOfTranslation(descriptions,inclusions)) {
                        for (int index=0;index<descriptions.length;index++)
                            descriptions[index]=(Description)descriptions[index].accept(normalizer);
                        m_conceptInclusions.add(descriptions);
                    }
                }
                else if (simplifiedDescription instanceof ObjectAnd) {
                    ObjectAnd objectAnd=(ObjectAnd)simplifiedDescription;
                    for (Description conjunct : objectAnd.getDescriptions())
                        inclusions.add(new Description[] { conjunct });
                }
                else {
                    Description normalized=(Description)simplifiedDescription.accept(normalizer);
                    m_conceptInclusions.add(new Description[] { normalized });
                }
            }
        }
        
    }
    protected boolean optimizedNegativeOneOfTranslation(Description[] descriptions,List<Description[]> inclusions) {
        if (descriptions.length==2) {
            ObjectOneOf objectOneOf=null;
            Description other=null;
            if (descriptions[0] instanceof ObjectNot && ((ObjectNot)descriptions[0]).getDescription() instanceof ObjectOneOf) {
                objectOneOf=(ObjectOneOf)((ObjectNot)descriptions[0]).getDescription();
                other=descriptions[1];
            }
            else if (descriptions[1] instanceof ObjectNot && ((ObjectNot)descriptions[1]).getDescription() instanceof ObjectOneOf) {
                other=descriptions[0];
                objectOneOf=(ObjectOneOf)((ObjectNot)descriptions[1]).getDescription();
            }
            if (objectOneOf!=null && (other instanceof OWLClass || (other instanceof ObjectNot && ((ObjectNot)other).getDescription() instanceof OWLClass))) {
                for (Individual individual : objectOneOf.getIndividuals())
                    m_facts.add(KAON2Manager.factory().classMember(other,individual));
                return true;
            }
        }
        return false;
    }
    protected boolean distributeUnionOverAnd(Description[] descriptions,List<Description[]> inclusions) {
        int andIndex=-1;
        for (int index=0;index<descriptions.length;index++) {
            Description description=descriptions[index];
            if (!isSimple(description))
                if (description instanceof ObjectAnd) {
                    if (andIndex==-1)
                        andIndex=index;
                    else
                        return false;
                }
                else
                    return false;
        }
        if (andIndex==-1)
            return false;
        ObjectAnd objectAnd=(ObjectAnd)descriptions[andIndex];
        for (Description description : objectAnd.getDescriptions()) {
            Description[] newDescriptions=descriptions.clone();
            newDescriptions[andIndex]=description;
            inclusions.add(newDescriptions);
        }
        return true;
    }
    protected Description getDefinitionFor(Description description,boolean[] alreadyExists) {
        Description definition=m_definitions.get(description);
        if (definition==null) {
            definition=KAON2Manager.factory().owlClass("internal:q#"+m_definitions.size());
            if (!(Boolean)description.accept(PLVisitor.INSTANCE))
                definition=KAON2Manager.factory().objectNot(definition);
            m_definitions.put(description,definition);
            alreadyExists[0]=false;
        }
        else
            alreadyExists[0]=true;
        return definition;
    }
    protected OWLClass getDefinitionForNegativeNominal(ObjectOneOf objectOneOf,boolean[] alreadyExists) {
        OWLClass definition=m_definitionsForNegativeNominals.get(objectOneOf);
        if (definition==null) {
            definition=KAON2Manager.factory().owlClass("internal:nnq#"+m_definitionsForNegativeNominals.size());
            m_definitionsForNegativeNominals.put(objectOneOf,definition);
            alreadyExists[0]=false;
        }
        else
            alreadyExists[0]=true;
        return definition;
    }
    protected boolean isSimple(Description description) {
        return description instanceof OWLClass || (description instanceof ObjectNot && ((ObjectNot)description).getDescription() instanceof OWLClass);
    }

    protected class NormalizationVisitor extends KAON2VisitorAdapter {
        protected final Collection<Description[]> m_newInclusions;
        protected final boolean[] m_alreadyExists;

        public NormalizationVisitor(Collection<Description[]> newInclusions) {
            m_newInclusions=newInclusions;
            m_alreadyExists=new boolean[1];
        }
        public Object visit(DataNot object) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }
        public Object visit(DataOneOf object) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }
        public Object visit(DataAll object) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }
        public Object visit(DataSome object) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }
        public Object visit(DataCardinality object) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }
        public Object visit(DataHasValue object) {
            throw new RuntimeException("Datatypes are not supported yet.");
        }
        public Object visit(OWLClass object) {
            return object;
        }
        public Object visit(ObjectAll object) {
            Description description=object.getDescription();
            if ( isSimple(description) ||
                 description instanceof ObjectOneOf ||
                (description instanceof ObjectNot &&
                 ((ObjectNot)description).getDescription() instanceof ObjectOneOf &&
                 ((ObjectOneOf)((ObjectNot)description).getDescription()).getIndividuals().size()==1
                )) // The ObjectOneof cases are optimizations.
                return object;
            else {
                Description definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new Description[] { definition.getComplementNNF(),description });
                return KAON2Manager.factory().objectAll(object.getObjectProperty(),definition);
            }
        }
        public Object visit(ObjectSome object) {
            Description description=object.getDescription();
            if (isSimple(description) || description instanceof ObjectOneOf) // The ObjectOneOf case is an optimization.
                return object;
            else {
                Description definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new Description[] { definition.getComplementNNF(),description });
                return KAON2Manager.factory().objectSome(object.getObjectProperty(),definition);
            }
        }
        public Object visit(ObjectCardinality object) {
            ObjectPropertyExpression objectProperty=object.getObjectProperty().getSimplified();
            Description description=object.getDescription();
            if (object.getCardinalityType()==ObjectCardinality.EXACT) {
                Description definition=getDefinitionFor(object,m_alreadyExists);
                if (!m_alreadyExists[0]) {
                    m_newInclusions.add(new Description[] { definition.getComplementNNF(),KAON2Manager.factory().objectCardinality(ObjectCardinality.MINIMUM,object.getCardinality(),objectProperty,description) });
                    m_newInclusions.add(new Description[] { definition.getComplementNNF(),KAON2Manager.factory().objectCardinality(ObjectCardinality.MAXIMUM,object.getCardinality(),objectProperty,description) });
                }
                return definition;
            }
            else if (object.getCardinalityType()==ObjectCardinality.MINIMUM) {
                if (object.getCardinality()<=0)
                    return KAON2Manager.factory().thing();
                else if (isSimple(description))
                    return object;
                else if (object.getCardinality()==1 && description instanceof ObjectOneOf)  // This is an optimization
                    return KAON2Manager.factory().objectSome(objectProperty,description);
                else {
                    Description definition=getDefinitionFor(description,m_alreadyExists);
                    if (!m_alreadyExists[0])
                        m_newInclusions.add(new Description[] { definition.getComplementNNF(),description });
                    return KAON2Manager.factory().objectCardinality(ObjectCardinality.MINIMUM,object.getCardinality(),objectProperty,definition);
                }
            }
            else if (object.getCardinalityType()==ObjectCardinality.MAXIMUM) {
                if (object.getCardinality()<=0)
                    return KAON2Manager.factory().objectAll(objectProperty,description.getComplementNNF()).accept(this);
                else if (isSimple(description))
                    return object;
                else {
                    Description complementDescription=description.getComplementNNF();
                    Description definition=getDefinitionFor(complementDescription,m_alreadyExists);
                    if (!m_alreadyExists[0])
                        m_newInclusions.add(new Description[] { definition.getComplementNNF(),complementDescription });
                    return KAON2Manager.factory().objectCardinality(ObjectCardinality.MAXIMUM,object.getCardinality(),objectProperty,definition.getComplementNNF());
                }
            }
            else
                throw new IllegalArgumentException("Invalid type of cardinality restriction.");
        }
        public Object visit(ObjectNot object) {
            if (object.getDescription() instanceof ObjectOneOf) {
                ObjectOneOf objectOneOf=(ObjectOneOf)object.getDescription();
                OWLClass definition=getDefinitionForNegativeNominal(objectOneOf,m_alreadyExists);
                if (!m_alreadyExists[0]) {
                    for (Individual individual : objectOneOf.getIndividuals())
                        m_facts.add(KAON2Manager.factory().classMember(definition,individual));
                }
                return KAON2Manager.factory().objectNot(definition);
            }
            else 
                return object;
        }
        public Object visit(ObjectOr object) {
            throw new RuntimeException("OR should be broken down at the outermost level");
        }
        public Object visit(ObjectAnd object) {
            Description definition=getDefinitionFor(object,m_alreadyExists);
            if (!m_alreadyExists[0]) {
                for (Description description : object.getDescriptions())
                    m_newInclusions.add(new Description[] { definition.getComplementNNF(),description });
            }
            return definition;
        }
        public Object visit(ObjectOneOf object) {
            return object;
        }
        public Object visit(ObjectHasValue object) {
            ObjectOneOf objectOneOf=KAON2Manager.factory().objectOneOf(object.getIndividual());
            return KAON2Manager.factory().objectSome(object.getObjectProperty(),objectOneOf);
        }
    }

    protected static class PLVisitor extends KAON2VisitorAdapter {
        protected static final KAON2Visitor INSTANCE=new PLVisitor();

        public Object visit(OWLClass object) {
            if (KAON2Manager.factory().thing().equals(object))
                return Boolean.FALSE;
            else if (KAON2Manager.factory().nothing().equals(object))
                return Boolean.FALSE;
            else
                return Boolean.TRUE;
        }
        public Object visit(ObjectAll object) {
            return object.getDescription().accept(this);
        }
        public Object visit(ObjectSome object) {
            return Boolean.TRUE;
        }
        public Object visit(ObjectCardinality object) {
            if (object.isMinimumCardinality()) {
                if (object.isMaximumCardinality())
                    return object.getCardinality()>0 ? Boolean.TRUE : object.getDescription().getComplementNNF().accept(this);
                else
                    return object.getCardinality()>0 ? Boolean.TRUE : Boolean.FALSE;
            }
            else
                return object.getCardinality()>0 ? Boolean.TRUE : object.getDescription().getComplementNNF().accept(this);
        }
        public Object visit(ObjectNot object) {
            return Boolean.FALSE;
        }
        public Object visit(ObjectOr object) {
            for (Description description : object.getDescriptions())
                if ((Boolean)description.accept(this))
                    return Boolean.TRUE;
            return Boolean.FALSE;
        }
        public Object visit(ObjectAnd object) {
            for (Description description : object.getDescriptions())
                if ((Boolean)description.accept(this))
                    return Boolean.TRUE;
            return Boolean.FALSE;
        }
        public Object visit(ObjectOneOf object) {
            return Boolean.TRUE;
        }
        public Object visit(ObjectHasValue object) {
            return Boolean.TRUE;
        }
    }

    protected static class HasNumberRestrictionsVisitor extends KAON2VisitorAdapter {
        protected static final KAON2Visitor INSTANCE=new HasNumberRestrictionsVisitor();

        public Object visit(DataNot object) {
            return Boolean.FALSE;
        }
        public Object visit(DataOneOf object) {
            return Boolean.FALSE;
        }
        public Object visit(DataAll object) {
            return Boolean.FALSE;
        }
        public Object visit(DataSome object) {
            return Boolean.FALSE;
        }
        public Object visit(DataCardinality object) {
            return Boolean.TRUE;
        }
        public Object visit(DataHasValue object) {
            return Boolean.FALSE;
        }
        public Object visit(OWLClass object) {
            return Boolean.FALSE;
        }
        public Object visit(ObjectAll object) {
            return object.getDescription().accept(this);
        }
        public Object visit(ObjectSome object) {
            return object.getDescription().accept(this);
        }
        public Object visit(ObjectCardinality object) {
            return Boolean.TRUE;
        }
        public Object visit(ObjectNot object) {
            return Boolean.FALSE;
        }
        public Object visit(ObjectOr object) {
            for (Description description : object.getDescriptions())
                if ((Boolean)description.accept(this))
                    return Boolean.TRUE;
            return Boolean.FALSE;
        }
        public Object visit(ObjectAnd object) {
            for (Description description : object.getDescriptions())
                if ((Boolean)description.accept(this))
                    return Boolean.TRUE;
            return Boolean.FALSE;
        }
        public Object visit(ObjectOneOf object) {
            return Boolean.FALSE;
        }
        public Object visit(ObjectHasValue object) {
            return Boolean.FALSE;
        }
    }

    protected class TransitivityManager {
        protected final Map<ObjectPropertyExpression,Set<ObjectPropertyExpression>> m_subObjectProperties;
        protected final Set<ObjectPropertyExpression> m_transitiveObjectProperties;
        protected final Map<ObjectAll,Description> m_replacedDescriptions;

        public TransitivityManager() {
            m_subObjectProperties=new HashMap<ObjectPropertyExpression,Set<ObjectPropertyExpression>>();
            m_transitiveObjectProperties=new HashSet<ObjectPropertyExpression>();
            m_replacedDescriptions=new HashMap<ObjectAll,Description>();
        }
        public void addInclusion(ObjectPropertyExpression subObjectProperty,ObjectPropertyExpression superObjectProperty) {
            addInclusionEx(subObjectProperty.getSimplified(),superObjectProperty.getSimplified());
            addInclusionEx(subObjectProperty.getInverseObjectProperty().getSimplified(),superObjectProperty.getInverseObjectProperty().getSimplified());
        }
        public void makeTransitive(ObjectPropertyExpression objectProperty) {
            m_transitiveObjectProperties.add(objectProperty.getSimplified());
            m_transitiveObjectProperties.add(objectProperty.getInverseObjectProperty().getSimplified());
        }
        protected void addInclusionEx(ObjectPropertyExpression subObjectProperty,ObjectPropertyExpression superObjectProperty) {
            Set<ObjectPropertyExpression> subObjectProperties=m_subObjectProperties.get(superObjectProperty);
            if (subObjectProperties==null) {
                subObjectProperties=new HashSet<ObjectPropertyExpression>();
                m_subObjectProperties.put(superObjectProperty,subObjectProperties);
            }
            subObjectProperties.add(subObjectProperty);
        }
        public Description replaceDescriptionIfNecessary(Description description) {
            if (description instanceof ObjectAll) {
                ObjectAll objectAll=(ObjectAll)description;
                ObjectPropertyExpression objectProperty=((ObjectAll)description).getObjectProperty();
                Set<ObjectPropertyExpression> transitiveSubObjectProperties=getTransitiveSubObjectProperties(objectProperty);
                if (!transitiveSubObjectProperties.isEmpty()) {
                    Description replacement=getReplacementFor(objectAll);
                    for (ObjectPropertyExpression transitiveSubObjectProperty : transitiveSubObjectProperties) {
                        ObjectAll subObjectAll=KAON2Manager.factory().objectAll(transitiveSubObjectProperty,objectAll.getDescription());
                        getReplacementFor(subObjectAll);
                    }
                    return replacement;
                }
            }
            return description;
        }
        protected Description getReplacementFor(ObjectAll objectAll) {
            Description replacement=m_replacedDescriptions.get(objectAll);
            if (replacement==null) {
                replacement=KAON2Manager.factory().owlClass("internal:all#"+m_replacedDescriptions.size());
                if (objectAll.getDescription() instanceof ObjectNot)
                    replacement=replacement.getComplementNNF();
                m_replacedDescriptions.put(objectAll,replacement);
            }
            return replacement;
        }
        public void generateTransitivityAxioms() {
            for (Map.Entry<ObjectAll,Description> replacement : m_replacedDescriptions.entrySet()) {
                m_conceptInclusions.add(new Description[] { replacement.getValue().getComplementNNF(),replacement.getKey() });
                ObjectPropertyExpression objectProperty=replacement.getKey().getObjectProperty();
                for (ObjectPropertyExpression transitiveSubObjectProperty : getTransitiveSubObjectProperties(objectProperty)) {
                    ObjectAll consequentAll=KAON2Manager.factory().objectAll(transitiveSubObjectProperty,replacement.getKey().getDescription());
                    Description consequentReplacement=m_replacedDescriptions.get(consequentAll);
                    assert consequentReplacement!=null;
                    ObjectAll forallConsequentReplacement=KAON2Manager.factory().objectAll(transitiveSubObjectProperty,consequentReplacement);
                    m_conceptInclusions.add(new Description[] { replacement.getValue().getComplementNNF(),forallConsequentReplacement });
                }
            }
            m_replacedDescriptions.clear();
        }
        protected void transitivelyClose() {
            boolean changed=true;
            List<ObjectPropertyExpression> temporary=new ArrayList<ObjectPropertyExpression>();
            while (changed) {
                changed=false;
                for (Map.Entry<ObjectPropertyExpression,Set<ObjectPropertyExpression>> entry : m_subObjectProperties.entrySet()) {
                    temporary.clear();
                    temporary.addAll(entry.getValue());
                    for (int i=temporary.size()-1;i>=0;--i) {
                        Set<ObjectPropertyExpression> subObjectProperties=m_subObjectProperties.get(temporary.get(i));
                        if (subObjectProperties!=null)
                            if (entry.getValue().addAll(subObjectProperties))
                                changed=true;
                    }
                }
            }
        }
        protected Set<ObjectPropertyExpression> getTransitiveSubObjectProperties(ObjectPropertyExpression objectProperty) {
            Set<ObjectPropertyExpression> result=new HashSet<ObjectPropertyExpression>();
            if (m_transitiveObjectProperties.contains(objectProperty))
                result.add(objectProperty);
            Set<ObjectPropertyExpression> subObjectProperties=m_subObjectProperties.get(objectProperty);
            if (subObjectProperties!=null)
                for (ObjectPropertyExpression subObjectProperty : subObjectProperties)
                    if (m_transitiveObjectProperties.contains(subObjectProperty))
                        result.add(subObjectProperty);
            return result;
        }
    }
}
