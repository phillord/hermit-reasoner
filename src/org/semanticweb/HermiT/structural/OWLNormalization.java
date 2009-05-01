// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owl.model.OWLAntiSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLAxiomVisitor;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitorEx;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointUnionAxiom;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyChainSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.SWRLRule;

/**
 * This class implements the structural transformation from our new tableau paper. This transformation departs in the following way from the paper: it keeps the concepts of the form \exists R.{ a_1, ..., a_n }, \forall R.{ a_1, ..., a_n }, and \forall R.\neg { a } intact. These concepts are then clausified in a more efficient way.
 */
public class OWLNormalization {
    protected final OWLDataFactory m_factory;
    protected final Map<OWLDescription,OWLDescription> m_definitions;
    protected final Map<OWLObjectOneOf,OWLClass> m_definitionsForNegativeNominals;
    protected final OWLAxioms m_axioms;
    protected final ExpressionManager m_expressionManager;
    protected final PLVisitor m_plVisitor;

    public OWLNormalization(OWLDataFactory factory,OWLAxioms axioms) {
        m_factory=factory;
        m_definitions=new HashMap<OWLDescription,OWLDescription>();
        m_definitionsForNegativeNominals=new HashMap<OWLObjectOneOf,OWLClass>();
        m_axioms=axioms;
        m_expressionManager=new ExpressionManager(m_factory);
        m_plVisitor=new PLVisitor();
    }

    public void processOntology(Configuration config,OWLOntology ontology) {
        // Each entry in the inclusions list represents a disjunction of
        // concepts -- that is, each OWLDescription in an entry contributes a
        // disjunct. It is thus not really inclusions, but rather a disjunction
        // of concepts that represents an inclusion axiom.
        m_axioms.m_classes.addAll(ontology.getReferencedClasses());
        m_axioms.m_objectProperties.addAll(ontology.getReferencedObjectProperties());
        m_axioms.m_dataProperties.addAll(ontology.getReferencedDataProperties());
        m_axioms.m_individuals.addAll(ontology.getReferencedIndividuals());
        AxiomVisitor axiomVisitor=new AxiomVisitor();
        for (OWLAxiom axiom : ontology.getAxioms())
            axiom.accept(axiomVisitor);
        normalizeInclusions(axiomVisitor.m_inclusionsAsDisjunctions,m_axioms.m_conceptInclusions,m_axioms.m_facts);
    }
    public void processKeys(Configuration config,Set<OWLHasKeyDummy> keys) {
        AxiomVisitor axiomVisitor=new AxiomVisitor();
        for (OWLHasKeyDummy key : keys)
            axiomVisitor.visit(key);
    }
    protected void addFact(OWLIndividualAxiom axiom) {
        m_axioms.m_facts.add(axiom);
    }
    protected void addHasKey(OWLHasKeyDummy axiom) {
        m_axioms.m_hasKeys.add(axiom);
    }
    protected void addInclusion(OWLObjectPropertyExpression subObjectPropertyExpression,OWLObjectPropertyExpression superObjectPropertyExpression) {
        m_axioms.m_simpleObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { subObjectPropertyExpression.getSimplified(),superObjectPropertyExpression.getSimplified() });
    }
    protected void addInclusion(OWLObjectPropertyExpression[] subObjectPropertyExpressions,OWLObjectPropertyExpression superObjectPropertyExpression) {
        for (int index=subObjectPropertyExpressions.length-1;index>=0;--index)
            subObjectPropertyExpressions[index]=subObjectPropertyExpressions[index].getSimplified();
        m_axioms.m_complexObjectPropertyInclusions.add(new OWLAxioms.ComplexObjectPropertyInclusion(subObjectPropertyExpressions,superObjectPropertyExpression.getSimplified()));
    }
    protected void addInclusion(OWLDataPropertyExpression subDataPropertyExpression,OWLDataPropertyExpression superDataPropertyExpression) {
        m_axioms.m_dataPropertyInclusions.add(new OWLDataPropertyExpression[] { subDataPropertyExpression,superDataPropertyExpression });
    }
    protected void makeTransitive(OWLObjectPropertyExpression objectPropertyExpression) {
        m_axioms.m_complexObjectPropertyInclusions.add(new OWLAxioms.ComplexObjectPropertyInclusion(objectPropertyExpression.getSimplified()));
    }
    protected void makeReflexive(OWLObjectPropertyExpression objectPropertyExpression) {
        m_axioms.m_reflexiveObjectProperties.add(objectPropertyExpression.getSimplified());
    }
    protected void makeIrreflexive(OWLObjectPropertyExpression objectPropertyExpression) {
        m_axioms.m_irreflexiveObjectProperties.add(objectPropertyExpression.getSimplified());
    }
    protected void makeAsymmetric(OWLObjectPropertyExpression objectPropertyExpression) {
        m_axioms.m_asymmetricObjectProperties.add(objectPropertyExpression.getSimplified());
    }
    protected static boolean isSimple(OWLDescription description) {
        return description instanceof OWLClass || (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLClass);
    }
    protected static boolean isNominal(OWLDescription description) {
        return description instanceof OWLObjectOneOf;
    }
    protected static boolean isNegatedOneNominal(OWLDescription description) {
        if (!(description instanceof OWLObjectComplementOf))
            return false;
        OWLDescription operand=((OWLObjectComplementOf)description).getOperand();
        if (!(operand instanceof OWLObjectOneOf))
            return false;
        return ((OWLObjectOneOf)operand).getIndividuals().size()==1;
    }
    protected void normalizeInclusions(List<OWLDescription[]> inclusions,Collection<OWLDescription[]> normalizedInclusions,Collection<OWLIndividualAxiom> facts) {
        NormalizationVisitor normalizer=new NormalizationVisitor(inclusions);
        while (!inclusions.isEmpty()) {
            OWLDescription simplifiedDescription=m_expressionManager.getSimplified(m_factory.getOWLObjectUnionOf(inclusions.remove(inclusions.size()-1)));
            if (!simplifiedDescription.isOWLThing()) {
                if (simplifiedDescription instanceof OWLObjectUnionOf) {
                    OWLObjectUnionOf objectOr=(OWLObjectUnionOf)simplifiedDescription;
                    OWLDescription[] descriptions=new OWLDescription[objectOr.getOperands().size()];
                    objectOr.getOperands().toArray(descriptions);
                    if (!distributeUnionOverAnd(descriptions,inclusions) && !optimizedNegativeOneOfTranslation(descriptions,facts)) {
                        for (int index=0;index<descriptions.length;index++)
                            descriptions[index]=descriptions[index].accept(normalizer);
                        normalizedInclusions.add(descriptions);
                    }
                }
                else if (simplifiedDescription instanceof OWLObjectIntersectionOf) {
                    OWLObjectIntersectionOf objectAnd=(OWLObjectIntersectionOf)simplifiedDescription;
                    for (OWLDescription conjunct : objectAnd.getOperands())
                        inclusions.add(new OWLDescription[] { conjunct });
                }
                else {
                    OWLDescription normalized=simplifiedDescription.accept(normalizer);
                    normalizedInclusions.add(new OWLDescription[] { normalized });
                }
            }
        }

    }
    protected boolean distributeUnionOverAnd(OWLDescription[] descriptions,List<OWLDescription[]> inclusions) {
        int andIndex=-1;
        for (int index=0;index<descriptions.length;index++) {
            OWLDescription description=descriptions[index];
            if (!isSimple(description))
                if (description instanceof OWLObjectIntersectionOf) {
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
        OWLObjectIntersectionOf objectAnd=(OWLObjectIntersectionOf)descriptions[andIndex];
        for (OWLDescription description : objectAnd.getOperands()) {
            OWLDescription[] newDescriptions=descriptions.clone();
            newDescriptions[andIndex]=description;
            inclusions.add(newDescriptions);
        }
        return true;
    }
    protected boolean optimizedNegativeOneOfTranslation(OWLDescription[] descriptions,Collection<OWLIndividualAxiom> facts) {
        if (descriptions.length==2) {
            OWLObjectOneOf nominal=null;
            OWLDescription other=null;
            if (descriptions[0] instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)descriptions[0]).getOperand() instanceof OWLObjectOneOf) {
                nominal=(OWLObjectOneOf)((OWLObjectComplementOf)descriptions[0]).getOperand();
                other=descriptions[1];
            }
            else if (descriptions[1] instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)descriptions[1]).getOperand() instanceof OWLObjectOneOf) {
                other=descriptions[0];
                nominal=(OWLObjectOneOf)((OWLObjectComplementOf)descriptions[1]).getOperand();
            }
            if (nominal!=null && (other instanceof OWLClass || (other instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)other).getOperand() instanceof OWLClass))) {
                for (OWLIndividual individual : nominal.getIndividuals())
                    facts.add(m_factory.getOWLClassAssertionAxiom(individual,other));
                return true;
            }
        }
        return false;
    }
    protected OWLDescription getDefinitionFor(OWLDescription description,boolean[] alreadyExists,boolean forcePositive) {
        OWLDescription definition=m_definitions.get(description);
        if (definition==null || (forcePositive && !(definition instanceof OWLClass))) {
            definition=m_factory.getOWLClass(URI.create("internal:def#"+m_definitions.size()));
            if (!forcePositive && !description.accept(m_plVisitor))
                definition=m_factory.getOWLObjectComplementOf(definition);
            m_definitions.put(description,definition);
            alreadyExists[0]=false;
        }
        else
            alreadyExists[0]=true;
        return definition;
    }
    protected OWLDescription getDefinitionFor(OWLDescription description,boolean[] alreadyExists) {
        return getDefinitionFor(description,alreadyExists,false);
    }
    protected OWLClass getClassFor(OWLDescription description,boolean[] alreadyExists) {
        return (OWLClass)getDefinitionFor(description,alreadyExists,true);
    }
    protected OWLClass getDefinitionForNegativeNominal(OWLObjectOneOf nominal,boolean[] alreadyExists) {
        OWLClass definition=m_definitionsForNegativeNominals.get(nominal);
        if (definition==null) {
            definition=m_factory.getOWLClass(URI.create("internal:nnq#"+m_definitionsForNegativeNominals.size()));
            m_definitionsForNegativeNominals.put(nominal,definition);
            alreadyExists[0]=false;
        }
        else
            alreadyExists[0]=true;
        return definition;
    }
    protected OWLDescription positive(OWLDescription description) {
        return m_expressionManager.getNNF(m_expressionManager.getSimplified(description));
    }
    protected OWLDescription negative(OWLDescription description) {
        return m_expressionManager.getComplementNNF(m_expressionManager.getSimplified(description));
    }
    protected OWLDataRange positive(OWLDataRange dataRange) {
        return m_expressionManager.getNNF(m_expressionManager.getSimplified(dataRange));
    }

    protected class AxiomVisitor implements OWLAxiomVisitor {
        protected final List<OWLDescription[]> m_inclusionsAsDisjunctions;
        protected final boolean[] m_alreadyExists;

        public AxiomVisitor() {
            m_inclusionsAsDisjunctions=new ArrayList<OWLDescription[]>();
            m_alreadyExists=new boolean[1];
        }

        // Semantic-less axioms
        
        public void visit(OWLImportsDeclaration axiom) {
        }
        public void visit(OWLDeclarationAxiom axiom) {
        }
        public void visit(OWLOntologyAnnotationAxiom axiom) {
        }
        public void visit(OWLEntityAnnotationAxiom axiom) {
        }
        public void visit(OWLAxiomAnnotationAxiom axiom) {
        }

        // Class axioms
        
        public void visit(OWLSubClassAxiom axiom) {
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { negative(axiom.getSubClass()),positive(axiom.getSuperClass()) });
        }
        public void visit(OWLEquivalentClassesAxiom axiom) {
            if (axiom.getDescriptions().size()>1) {
                Iterator<OWLDescription> iterator=axiom.getDescriptions().iterator();
                OWLDescription first=iterator.next();
                OWLDescription last=first;
                while (iterator.hasNext()) {
                    OWLDescription next=iterator.next();
                    m_inclusionsAsDisjunctions.add(new OWLDescription[] {negative(last),positive(next) });
                    last=next;
                }
                m_inclusionsAsDisjunctions.add(new OWLDescription[] {negative(last),positive(first) });
            }
        }
        public void visit(OWLDisjointClassesAxiom axiom) {
            OWLDescription[] descriptions=new OWLDescription[axiom.getDescriptions().size()];
            axiom.getDescriptions().toArray(descriptions);
            for (int i=0;i<descriptions.length;i++)
                descriptions[i]=m_expressionManager.getComplementNNF(descriptions[i]);
            for (int i=0;i<descriptions.length;i++)
                for (int j=i+1;j<descriptions.length;j++)
                    m_inclusionsAsDisjunctions.add(new OWLDescription[] { descriptions[i],descriptions[j] });
        }
        public void visit(OWLDisjointUnionAxiom axiom) {
            // DisjointUnion(C CE1 ... CEn)
            // 1. add C implies CE1 or ... or CEn, which is { not C or CE1 or ... or CEn }
            Set<OWLDescription> inclusion=new HashSet<OWLDescription>(axiom.getDescriptions());
            inclusion.add(m_expressionManager.getComplementNNF(axiom.getOWLClass()));
            OWLDescription[] inclusionArray=new OWLDescription[axiom.getDescriptions().size()+1];
            inclusion.toArray(inclusionArray);
            m_inclusionsAsDisjunctions.add(inclusionArray);
            // 2. add CEi implies CEn implies C, which is { not CEi or C }
            for (OWLDescription description : axiom.getDescriptions())
                m_inclusionsAsDisjunctions.add(new OWLDescription[] {negative(description),axiom.getOWLClass() });
            // 3. add CEi and CEj implies bottom (not CEi or not CEj) for 1 <= i < j <= n
            OWLDescription[] descriptions=new OWLDescription[axiom.getDescriptions().size()];
            axiom.getDescriptions().toArray(descriptions);
            for (int i=0;i<descriptions.length;i++)
                descriptions[i]=m_expressionManager.getComplementNNF(descriptions[i]);
            for (int i=0;i<descriptions.length;i++)
                for (int j=i+1;j<descriptions.length;j++)
                    m_inclusionsAsDisjunctions.add(new OWLDescription[] { descriptions[i],descriptions[j] });
        }

        // Object property axioms
        
        public void visit(OWLObjectSubPropertyAxiom axiom) {
            addInclusion(axiom.getSubProperty(),axiom.getSuperProperty());
        }
        public void visit(OWLObjectPropertyChainSubPropertyAxiom axiom) {
            List<OWLObjectPropertyExpression> subPropertyChain=axiom.getPropertyChain();
            OWLObjectPropertyExpression superObjectPropertyExpression=axiom.getSuperProperty();
            if (subPropertyChain.size()==1)
                addInclusion(subPropertyChain.get(0),superObjectPropertyExpression);
            else if (subPropertyChain.size()==2 && subPropertyChain.get(0).equals(superObjectPropertyExpression) && subPropertyChain.get(1).equals(superObjectPropertyExpression))
                makeTransitive(axiom.getSuperProperty());
            else {
                OWLObjectPropertyExpression[] subObjectProperties=new OWLObjectPropertyExpression[subPropertyChain.size()];
                subPropertyChain.toArray(subObjectProperties);
                addInclusion(subObjectProperties,superObjectPropertyExpression);
            }
        }
        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            if (axiom.getProperties().size()>1) {
                Iterator<OWLObjectPropertyExpression> iterator=axiom.getProperties().iterator();
                OWLObjectPropertyExpression first=iterator.next();
                OWLObjectPropertyExpression last=first;
                while (iterator.hasNext()) {
                    OWLObjectPropertyExpression next=iterator.next();
                    addInclusion(last,next);
                    last=next;
                }
                addInclusion(last,first);
            }
        }
        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
            OWLObjectPropertyExpression[] objectProperties=new OWLObjectPropertyExpression[axiom.getProperties().size()];
            axiom.getProperties().toArray(objectProperties);
            for (int i=0;i<objectProperties.length;i++)
                objectProperties[i]=objectProperties[i].getSimplified();
            m_axioms.m_disjointObjectProperties.add(objectProperties);
        }
        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
            OWLObjectPropertyExpression first=axiom.getFirstProperty();
            OWLObjectPropertyExpression second=axiom.getSecondProperty();
            addInclusion(first,second.getInverseProperty());
            addInclusion(second,first.getInverseProperty());
        }
        public void visit(OWLObjectPropertyDomainAxiom axiom) {
            OWLObjectAllRestriction allPropertyNohting=m_factory.getOWLObjectAllRestriction(axiom.getProperty().getSimplified(),m_factory.getOWLNothing());
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { positive(axiom.getDomain()),allPropertyNohting });
        }
        public void visit(OWLObjectPropertyRangeAxiom axiom) {
            OWLObjectAllRestriction allPropertyRange=m_factory.getOWLObjectAllRestriction(axiom.getProperty().getSimplified(),positive(axiom.getRange()));
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { allPropertyRange });
        }
        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { m_factory.getOWLObjectMaxCardinalityRestriction(axiom.getProperty().getSimplified(),1) });
        }
        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { m_factory.getOWLObjectMaxCardinalityRestriction(axiom.getProperty().getSimplified().getInverseProperty(),1) });
        }
        public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
            makeReflexive(axiom.getProperty());
        }
        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            makeIrreflexive(axiom.getProperty());
        }
        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
            OWLObjectPropertyExpression objectProperty=axiom.getProperty();
            addInclusion(objectProperty,objectProperty.getInverseProperty());
        }
        public void visit(OWLAntiSymmetricObjectPropertyAxiom axiom) {
            makeAsymmetric(axiom.getProperty());
        }
        public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
            makeTransitive(axiom.getProperty());
        }

        // Data property axioms
        
        public void visit(OWLDataSubPropertyAxiom axiom) {
            OWLDataPropertyExpression subDataProperty=axiom.getSubProperty();
            OWLDataPropertyExpression superDataProperty=axiom.getSuperProperty();
            addInclusion(subDataProperty,superDataProperty);
        }
        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
            if (axiom.getProperties().size()>1) {
                Iterator<OWLDataPropertyExpression> iterator=axiom.getProperties().iterator();
                OWLDataPropertyExpression first=iterator.next();
                OWLDataPropertyExpression last=first;
                while (iterator.hasNext()) {
                    OWLDataPropertyExpression next=iterator.next();
                    addInclusion(last,next);
                    last=next;
                }
                addInclusion(last,first);
            }
        }
        public void visit(OWLDisjointDataPropertiesAxiom axiom) {
            OWLDataPropertyExpression[] dataProperties=new OWLDataPropertyExpression[axiom.getProperties().size()];
            axiom.getProperties().toArray(dataProperties);
            m_axioms.m_disjointDataProperties.add(dataProperties);
        }
        public void visit(OWLDataPropertyDomainAxiom axiom) {
            OWLDataRange dataNothing=m_factory.getOWLDataComplementOf(m_factory.getTopDataType());
            OWLDataAllRestriction allPropertyDataNothing=m_factory.getOWLDataAllRestriction(axiom.getProperty(),dataNothing);
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { positive(axiom.getDomain()),allPropertyDataNothing });
        }
        public void visit(OWLDataPropertyRangeAxiom axiom) {
            OWLDataAllRestriction allPropertyRange=m_factory.getOWLDataAllRestriction(axiom.getProperty(),positive(axiom.getRange()));
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { allPropertyRange });
        }
        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { m_factory.getOWLDataMaxCardinalityRestriction(axiom.getProperty(),1) });
        }

        // Keys
        
        public void visit(OWLHasKeyDummy axiom) {
            OWLDescription description=positive(axiom.getClassExpression());
            if (!isSimple(description)) {
                OWLDescription definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_inclusionsAsDisjunctions.add(new OWLDescription[] {negative(definition),description });
                description=definition;
            }
            if (description==axiom.getClassExpression())
                addHasKey(axiom);
            else {
                // Construct a new axiom that uses the concept definition.
                OWLHasKeyDummy newKeyAxiom=new OWLHasKeyDummy(m_factory);
                newKeyAxiom.setClassExpression(description);
                newKeyAxiom.setObjectProperties(axiom.getObjectProperties());
                newKeyAxiom.setDataProperties(axiom.getDataProperties());
                addHasKey(newKeyAxiom);
            }
        }

        // Assertions
        
        public void visit(OWLSameIndividualsAxiom axiom) {
            addFact(axiom);
        }
        public void visit(OWLDifferentIndividualsAxiom axiom) {
            addFact(axiom);
        }
        public void visit(OWLClassAssertionAxiom axiom) {
            OWLDescription description=positive(axiom.getDescription());
            if (!isSimple(description)) {
                OWLDescription definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_inclusionsAsDisjunctions.add(new OWLDescription[] {negative(definition),description });
                description=definition;
            }
            if (description==axiom.getDescription())
                addFact(axiom);
            else
                addFact(m_factory.getOWLClassAssertionAxiom(axiom.getIndividual(),description));
        }
        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            addFact(m_factory.getOWLObjectPropertyAssertionAxiom(axiom.getSubject(),axiom.getProperty().getSimplified(),axiom.getObject()));
        }
        public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
            OWLObjectOneOf nominal=m_factory.getOWLObjectOneOf(axiom.getObject());
            OWLDescription notNominal=m_factory.getOWLObjectComplementOf(nominal);
            OWLDescription allNotNominal=m_factory.getOWLObjectAllRestriction(axiom.getProperty().getSimplified(),notNominal);
            OWLDescription definition=getDefinitionFor(allNotNominal,m_alreadyExists);
            if (!m_alreadyExists[0])
                m_inclusionsAsDisjunctions.add(new OWLDescription[] {negative(definition),allNotNominal });
            addFact(m_factory.getOWLClassAssertionAxiom(axiom.getSubject(),definition));
        }
        public void visit(OWLDataPropertyAssertionAxiom axiom) {
            addFact(axiom);
        }
        public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
            addFact(axiom);
        }

        // Rules
        
        public void visit(SWRLRule rule) {
            throw new IllegalArgumentException("SWRL rules are not supported yet.");
        }
    }

    protected class NormalizationVisitor implements OWLDescriptionVisitorEx<OWLDescription> {
        protected final Collection<OWLDescription[]> m_newInclusions;
        protected final boolean[] m_alreadyExists;

        public NormalizationVisitor(Collection<OWLDescription[]> newInclusions) {
            m_newInclusions=newInclusions;
            m_alreadyExists=new boolean[1];
        }
        public OWLDescription visit(OWLClass object) {
            return object;
        }
        public OWLDescription visit(OWLObjectIntersectionOf object) {
            OWLDescription definition=getDefinitionFor(object,m_alreadyExists);
            if (!m_alreadyExists[0])
                for (OWLDescription description : object.getOperands())
                    m_newInclusions.add(new OWLDescription[] {negative(definition),description });
            return definition;
        }
        public OWLDescription visit(OWLObjectUnionOf object) {
            throw new IllegalStateException("OR should be broken down at the outermost level");
        }
        public OWLDescription visit(OWLObjectComplementOf object) {
            if (isNominal(object.getOperand())) {
                OWLObjectOneOf objectOneOf=(OWLObjectOneOf)object.getOperand();
                OWLClass definition=getDefinitionForNegativeNominal(objectOneOf,m_alreadyExists);
                if (!m_alreadyExists[0])
                    for (OWLIndividual individual : objectOneOf.getIndividuals())
                        addFact(m_factory.getOWLClassAssertionAxiom(individual,definition));
                return m_factory.getOWLObjectComplementOf(definition);
            }
            else
                return object;
        }
        public OWLDescription visit(OWLObjectOneOf object) {
            return object;
        }
        public OWLDescription visit(OWLObjectSomeRestriction object) {
            OWLDescription filler=object.getFiller();
            if (isSimple(filler) || isNominal(filler))
                // The ObjectOneof cases is an optimization.
                return object;
            else {
                OWLDescription definition=getDefinitionFor(filler,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] {negative(definition),filler });
                return m_factory.getOWLObjectSomeRestriction(object.getProperty(),definition);
            }
        }
        public OWLDescription visit(OWLObjectAllRestriction object) {
            OWLDescription filler=object.getFiller();
            if (isSimple(filler) || isNominal(filler) || isNegatedOneNominal(filler))
                // The nominal cases are optimizations.
                return object;
            else {
                OWLDescription definition=getDefinitionFor(filler,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] {negative(definition),filler });
                return m_factory.getOWLObjectAllRestriction(object.getProperty(),definition);
            }
        }
        public OWLDescription visit(OWLObjectValueRestriction object) {
            throw new IllegalStateException("Internal error: object value restrictions should have been simplified.");
        }
        public OWLDescription visit(OWLObjectSelfRestriction object) {
            return object;
        }
        public OWLDescription visit(OWLObjectMinCardinalityRestriction object) {
            OWLDescription filler=object.getFiller();
            if (isSimple(filler))
                return object;
            else {
                OWLDescription definition=getDefinitionFor(filler,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] {negative(definition),filler });
                return m_factory.getOWLObjectMinCardinalityRestriction(object.getProperty(),object.getCardinality(),definition);
            }
        }
        public OWLDescription visit(OWLObjectMaxCardinalityRestriction object) {
            OWLDescription filler=object.getFiller();
            if (isSimple(filler))
                return object;
            else {
                OWLDescription complementDescription=m_expressionManager.getComplementNNF(filler);
                OWLDescription definition=getDefinitionFor(complementDescription,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] {negative(definition),complementDescription });
                return m_factory.getOWLObjectMaxCardinalityRestriction(object.getProperty(),object.getCardinality(),m_expressionManager.getComplementNNF(definition));
            }
        }
        public OWLDescription visit(OWLObjectExactCardinalityRestriction object) {
            throw new IllegalStateException("Internal error: exact object cardinality restrictions should have been simplified.");
        }
        public OWLDescription visit(OWLDataSomeRestriction object) {
            return object;
        }
        public OWLDescription visit(OWLDataAllRestriction object) {
            return object;
        }
        public OWLDescription visit(OWLDataValueRestriction object) {
            throw new IllegalStateException("Internal error: data value restrictions should have been simplified.");
        }
        public OWLDescription visit(OWLDataMinCardinalityRestriction object) {
            return object;
        }
        public OWLDescription visit(OWLDataMaxCardinalityRestriction object) {
            return object;
        }
        public OWLDescription visit(OWLDataExactCardinalityRestriction object) {
            throw new IllegalStateException("Internal error: exact data cardinality restrictions should have been simplified.");
        }
    }

    /**
     * checks the polarity
     */
    protected class PLVisitor implements OWLDescriptionVisitorEx<Boolean> {

        public Boolean visit(OWLClass object) {
            if (object.isOWLThing())
                return Boolean.FALSE;
            else if (object.isOWLNothing())
                return Boolean.FALSE;
            else
                return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectIntersectionOf object) {
            for (OWLDescription desc : object.getOperands())
                if (desc.accept(this))
                    return Boolean.TRUE;
            return Boolean.FALSE;
        }
        public Boolean visit(OWLObjectUnionOf object) {
            for (OWLDescription desc : object.getOperands())
                if (desc.accept(this))
                    return Boolean.TRUE;
            return Boolean.FALSE;
        }
        public Boolean visit(OWLObjectComplementOf object) {
            return Boolean.FALSE;
        }
        public Boolean visit(OWLObjectOneOf object) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectSomeRestriction object) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectAllRestriction object) {
            return object.getFiller().accept(this);
        }
        public Boolean visit(OWLObjectValueRestriction object) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectSelfRestriction object) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectMinCardinalityRestriction object) {
            return object.getCardinality()>0;
        }
        public Boolean visit(OWLObjectMaxCardinalityRestriction object) {
            return object.getCardinality()>0 ? Boolean.TRUE : m_expressionManager.getComplementNNF(object.getFiller()).accept(this);
        }
        public Boolean visit(OWLObjectExactCardinalityRestriction object) {
            return object.getCardinality()>0 ? Boolean.TRUE : m_expressionManager.getComplementNNF(object.getFiller()).accept(this);
        }
        public Boolean visit(OWLDataSomeRestriction desc) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLDataAllRestriction desc) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLDataValueRestriction desc) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLDataMinCardinalityRestriction desc) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLDataMaxCardinalityRestriction desc) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLDataExactCardinalityRestriction desc) {
            return Boolean.TRUE;
        }
    }
}
