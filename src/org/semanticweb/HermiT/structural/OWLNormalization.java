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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
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
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
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
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

/**
 * This class implements the structural transformation from our new tableau paper. This transformation departs in the following way from the paper: it keeps the concepts of the form \exists R.{ a_1, ..., a_n }, \forall R.{ a_1, ..., a_n }, and \forall R.\neg { a } intact. These concepts are then clausified in a more efficient way.
 */
public class OWLNormalization {
    protected final OWLDataFactory m_factory;
    protected final OWLAxioms m_axioms;
    protected final int m_firstReplacementIndex;
    protected final Map<OWLClassExpression,OWLClassExpression> m_definitions;
    protected final Map<OWLObjectOneOf,OWLClass> m_definitionsForNegativeNominals;
    protected final ExpressionManager m_expressionManager;
    protected final PLVisitor m_plVisitor;
    protected final Map<OWLDataRange,OWLDatatype> m_dataRangeDefinitions; // contains custom datatype definitions from DatatypeDefinition axioms

    public OWLNormalization(OWLDataFactory factory,OWLAxioms axioms,int firstReplacementIndex) {
        m_factory=factory;
        m_axioms=axioms;
        m_firstReplacementIndex=firstReplacementIndex;
        m_definitions=new HashMap<OWLClassExpression,OWLClassExpression>();
        m_definitionsForNegativeNominals=new HashMap<OWLObjectOneOf,OWLClass>();
        m_expressionManager=new ExpressionManager(m_factory);
        m_plVisitor=new PLVisitor();
        m_dataRangeDefinitions=new HashMap<OWLDataRange,OWLDatatype>();
    }
    public void processOntology(OWLOntology ontology) {
        // Each entry in the inclusions list represents a disjunction of
        // concepts -- that is, each OWLClassExpression in an entry contributes a
        // disjunct. It is thus not really inclusions, but rather a disjunction
        // of concepts that represents an inclusion axiom.
        m_axioms.m_classes.addAll(ontology.getClassesInSignature(true));
        m_axioms.m_objectProperties.addAll(ontology.getObjectPropertiesInSignature(true));
        m_axioms.m_dataProperties.addAll(ontology.getDataPropertiesInSignature(true));
        m_axioms.m_namedIndividuals.addAll(ontology.getIndividualsInSignature(true));
        processAxioms(ontology.getLogicalAxioms());
    }
    public void processAxioms(Collection<? extends OWLAxiom> axioms) {
        AxiomVisitor axiomVisitor=new AxiomVisitor();
        for (OWLAxiom axiom : axioms)
            axiom.accept(axiomVisitor);
        // now all axioms are in NNF and converted into disjunctions wherever possible
        // exact cardinalities are rewritten into at least and at most cardinalities etc
        // Rules with multiple head atoms are rewritten into several rules (Lloyd-Topor transformation)

        // normalize rules, this might add new concept and data range inclusions
        // in case a rule atom uses a complex concept or data range
        // we keep this inclusions separate because they are only applied to named individuals
        RuleNormalizer ruleNormalizer=new RuleNormalizer(m_axioms.m_rules,axiomVisitor.m_classExpressionInclusionsAsDisjunctions,axiomVisitor.m_dataRangeInclusionsAsDisjunctions);
        for (SWRLRule rule : axiomVisitor.m_rules)
            ruleNormalizer.visit(rule);

        // in normalization, we now simplify the disjuncts where possible (eliminate
        // unnecessary conjuncts/disjuncts) and introduce fresh atomic concepts for complex
        // concepts m_axioms.m_conceptInclusions contains the normalized axioms after the normalization
        normalizeInclusions(axiomVisitor.m_classExpressionInclusionsAsDisjunctions,axiomVisitor.m_dataRangeInclusionsAsDisjunctions);
    }
    protected void addFact(OWLIndividualAxiom axiom) {
        m_axioms.m_facts.add(axiom);
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
    protected static boolean isSimple(OWLClassExpression description) {
        return description instanceof OWLClass || (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLClass);
    }
    protected static boolean isLiteral(OWLDataRange dr) {
        return isAtomic(dr) || isNegatedAtomic(dr);
    }
    protected static boolean isAtomic(OWLDataRange dr) {
        return dr instanceof OWLDatatype || dr instanceof OWLDatatypeRestriction || dr instanceof OWLDataOneOf;
    }
    protected static boolean isNegatedAtomic(OWLDataRange dr) {
        return dr instanceof OWLDataComplementOf && isAtomic(((OWLDataComplementOf)dr).getDataRange());
    }
    protected static boolean isNominal(OWLClassExpression description) {
        return description instanceof OWLObjectOneOf;
    }
    protected static boolean isNegatedOneNominal(OWLClassExpression description) {
        if (!(description instanceof OWLObjectComplementOf))
            return false;
        OWLClassExpression operand=((OWLObjectComplementOf)description).getOperand();
        if (!(operand instanceof OWLObjectOneOf))
            return false;
        return ((OWLObjectOneOf)operand).getIndividuals().size()==1;
    }
    protected void normalizeInclusions(List<OWLClassExpression[]> inclusions,List<OWLDataRange[]> dataRangeInclusions) {
        ClassExpressionNormalizer classExpressionNormalizer=new ClassExpressionNormalizer(inclusions,dataRangeInclusions);
        // normalize all class expression inclusions
        while (!inclusions.isEmpty()) {
            OWLClassExpression simplifiedDescription=m_expressionManager.getNNF(m_expressionManager.getSimplified(m_factory.getOWLObjectUnionOf(inclusions.remove(inclusions.size()-1))));
            if (!simplifiedDescription.isOWLThing()) {
                if (simplifiedDescription instanceof OWLObjectUnionOf) {
                    OWLObjectUnionOf objectOr=(OWLObjectUnionOf)simplifiedDescription;
                    OWLClassExpression[] descriptions=new OWLClassExpression[objectOr.getOperands().size()];
                    objectOr.getOperands().toArray(descriptions);
                    if (!distributeUnionOverAnd(descriptions,inclusions) && !optimizedNegativeOneOfTranslation(descriptions,m_axioms.m_facts)) {
                        for (int index=0;index<descriptions.length;index++)
                            descriptions[index]=descriptions[index].accept(classExpressionNormalizer);
                        m_axioms.m_conceptInclusions.add(descriptions);
                    }
                }
                else if (simplifiedDescription instanceof OWLObjectIntersectionOf) {
                    OWLObjectIntersectionOf objectAnd=(OWLObjectIntersectionOf)simplifiedDescription;
                    for (OWLClassExpression conjunct : objectAnd.getOperands())
                        inclusions.add(new OWLClassExpression[] { conjunct });
                }
                else {
                    OWLClassExpression normalized=simplifiedDescription.accept(classExpressionNormalizer);
                    m_axioms.m_conceptInclusions.add(new OWLClassExpression[] { normalized });
                }
            }
        }
        // normalize data range inclusions
        DataRangeNormalizer dataRangeNormalizer=new DataRangeNormalizer(dataRangeInclusions);
        while (!dataRangeInclusions.isEmpty()) {
            OWLDataRange simplifiedDescription=m_expressionManager.getNNF(m_expressionManager.getSimplified(m_factory.getOWLDataUnionOf(dataRangeInclusions.remove(classExpressionNormalizer.m_newDataRangeInclusions.size()-1))));
            if (!simplifiedDescription.isTopDatatype()) {
                if (simplifiedDescription instanceof OWLDataUnionOf) {
                    OWLDataUnionOf dataOr=(OWLDataUnionOf)simplifiedDescription;
                    OWLDataRange[] descriptions=new OWLDataRange[dataOr.getOperands().size()];
                    dataOr.getOperands().toArray(descriptions);
                    if (!distributeUnionOverAnd(descriptions,dataRangeInclusions)) {
                        for (int index=0;index<descriptions.length;index++)
                            descriptions[index]=descriptions[index].accept(dataRangeNormalizer);
                        m_axioms.m_dataRangeInclusions.add(descriptions);
                    }
                }
                else if (simplifiedDescription instanceof OWLDataIntersectionOf) {
                    OWLDataIntersectionOf dataAnd=(OWLDataIntersectionOf)simplifiedDescription;
                    for (OWLDataRange conjunct : dataAnd.getOperands())
                        dataRangeInclusions.add(new OWLDataRange[] { conjunct });
                }
                else {
                    OWLDataRange normalized=simplifiedDescription.accept(dataRangeNormalizer);
                    dataRangeInclusions.add(new OWLDataRange[] { normalized });
                }
            }
        }
    }
    protected boolean distributeUnionOverAnd(OWLClassExpression[] descriptions,List<OWLClassExpression[]> inclusions) {
        int andIndex=-1;
        for (int index=0;index<descriptions.length;index++) {
            OWLClassExpression description=descriptions[index];
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
        for (OWLClassExpression description : objectAnd.getOperands()) {
            OWLClassExpression[] newDescriptions=descriptions.clone();
            newDescriptions[andIndex]=description;
            inclusions.add(newDescriptions);
        }
        return true;
    }
    protected boolean distributeUnionOverAnd(OWLDataRange[] descriptions,List<OWLDataRange[]> inclusions) {
        int andIndex=-1;
        for (int index=0;index<descriptions.length;index++) {
            OWLDataRange description=descriptions[index];
            if (!isLiteral(description))
                if (description instanceof OWLDataIntersectionOf) {
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
        OWLDataIntersectionOf dataAnd=(OWLDataIntersectionOf)descriptions[andIndex];
        for (OWLDataRange description : dataAnd.getOperands()) {
            OWLDataRange[] newDescriptions=descriptions.clone();
            newDescriptions[andIndex]=description;
            inclusions.add(newDescriptions);
        }
        return true;
    }
    protected boolean optimizedNegativeOneOfTranslation(OWLClassExpression[] descriptions,Collection<OWLIndividualAxiom> facts) {
        if (descriptions.length==2) {
            OWLObjectOneOf nominal=null;
            OWLClassExpression other=null;
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
                    facts.add(m_factory.getOWLClassAssertionAxiom(other,individual));
                return true;
            }
        }
        return false;
    }
    protected OWLClassExpression getDefinitionFor(OWLClassExpression description,boolean[] alreadyExists,boolean forcePositive) {
        OWLClassExpression definition=m_definitions.get(description);
        if (definition==null || (forcePositive && !(definition instanceof OWLClass))) {
            definition=m_factory.getOWLClass(IRI.create("internal:def#"+(m_definitions.size()+m_firstReplacementIndex)));
            if (!forcePositive && !description.accept(m_plVisitor))
                definition=m_factory.getOWLObjectComplementOf(definition);
            m_definitions.put(description,definition);
            alreadyExists[0]=false;
        }
        else
            alreadyExists[0]=true;
        return definition;
    }
    protected OWLDatatype getDefinitionFor(OWLDataRange dr,boolean[] alreadyExists) {
        OWLDatatype definition=m_dataRangeDefinitions.get(dr);
        if (definition==null) {
            definition=m_factory.getOWLDatatype(IRI.create("internal:defdata#"+m_dataRangeDefinitions.size()));
            m_dataRangeDefinitions.put(dr,definition);
            alreadyExists[0]=false;
        }
        else {
            alreadyExists[0]=true;
        }
        return definition;
    }
    protected OWLClassExpression getDefinitionFor(OWLClassExpression description,boolean[] alreadyExists) {
        return getDefinitionFor(description,alreadyExists,false);
    }
    protected OWLClass getClassFor(OWLClassExpression description,boolean[] alreadyExists) {
        return (OWLClass)getDefinitionFor(description,alreadyExists,true);
    }
    protected OWLClass getDefinitionForNegativeNominal(OWLObjectOneOf nominal,boolean[] alreadyExists) {
        OWLClass definition=m_definitionsForNegativeNominals.get(nominal);
        if (definition==null) {
            definition=m_factory.getOWLClass(IRI.create("internal:nnq#"+m_definitionsForNegativeNominals.size()));
            m_definitionsForNegativeNominals.put(nominal,definition);
            alreadyExists[0]=false;
        }
        else
            alreadyExists[0]=true;
        return definition;
    }
    protected OWLClassExpression positive(OWLClassExpression description) {
        return m_expressionManager.getNNF(m_expressionManager.getSimplified(description));
    }
    protected OWLClassExpression negative(OWLClassExpression description) {
        return m_expressionManager.getComplementNNF(m_expressionManager.getSimplified(description));
    }
    protected OWLDataRange positive(OWLDataRange dataRange) {
        return m_expressionManager.getNNF(m_expressionManager.getSimplified(dataRange));
    }
    protected OWLDataRange negative(OWLDataRange dataRange) {
        return m_expressionManager.getComplementNNF(m_expressionManager.getSimplified(dataRange));
    }

    protected class AxiomVisitor implements OWLAxiomVisitor {
        protected final List<OWLClassExpression[]> m_classExpressionInclusionsAsDisjunctions;
        protected final List<OWLDataRange[]> m_dataRangeInclusionsAsDisjunctions;
        protected final Collection<SWRLRule> m_rules;
        protected final boolean[] m_alreadyExists;

        public AxiomVisitor() {
            m_classExpressionInclusionsAsDisjunctions=new ArrayList<OWLClassExpression[]>();
            m_dataRangeInclusionsAsDisjunctions=new ArrayList<OWLDataRange[]>();
            m_rules=new HashSet<SWRLRule>();
            m_alreadyExists=new boolean[1];
        }

        // Semantics-less axioms

        public void visit(OWLImportsDeclaration axiom) {
        }
        public void visit(OWLDeclarationAxiom axiom) {
//            if (axiom.getEntity().isOWLClass()) {
//                OWLClass cls=(OWLClass)axiom.getEntity(); 
//                if (!m_axioms.m_classes.contains(cls)) 
//                    m_axioms.m_classes.add(cls);
//            }
//            if (axiom.getEntity().isOWLObjectProperty()) {
//                OWLObjectProperty op=(OWLObjectProperty)axiom.getEntity(); 
//                if (!m_axioms.m_objectProperties.contains(op)) 
//                    m_axioms.m_objectProperties.add(op);
//            }
//            if (axiom.getEntity().isOWLDataProperty()) {
//                OWLDataProperty dp=(OWLDataProperty)axiom.getEntity(); 
//                if (!m_axioms.m_dataProperties.contains(dp)) 
//                    m_axioms.m_dataProperties.add(dp);
//            }
        }
        public void visit(OWLAnnotationAssertionAxiom axiom) {
        }
        public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        }
        public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        }
        public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        }

        // Class axioms

        public void visit(OWLSubClassOfAxiom axiom) {
            m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { negative(axiom.getSubClass()),positive(axiom.getSuperClass()) });
        }
        public void visit(OWLEquivalentClassesAxiom axiom) {
            if (axiom.getClassExpressions().size()>1) {
                Iterator<OWLClassExpression> iterator=axiom.getClassExpressions().iterator();
                OWLClassExpression first=iterator.next();
                OWLClassExpression last=first;
                while (iterator.hasNext()) {
                    OWLClassExpression next=iterator.next();
                    m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { negative(last),positive(next) });
                    last=next;
                }
                m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { negative(last),positive(first) });
            }
        }
        public void visit(OWLDisjointClassesAxiom axiom) {
            if (axiom.getClassExpressions().size()<=1) {
                throw new IllegalArgumentException("Error: Parsed "+axiom.toString()+". A DisjointClasses axiom in OWL 2 DL must have at least two classes as parameters. ");
            }
            OWLClassExpression[] descriptions=new OWLClassExpression[axiom.getClassExpressions().size()];
            axiom.getClassExpressions().toArray(descriptions);
            for (int i=0;i<descriptions.length;i++)
                descriptions[i]=m_expressionManager.getComplementNNF(descriptions[i]);
            for (int i=0;i<descriptions.length;i++)
                for (int j=i+1;j<descriptions.length;j++)
                    m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { descriptions[i],descriptions[j] });
        }
        public void visit(OWLDisjointUnionAxiom axiom) {
            // DisjointUnion(C CE1 ... CEn)
            // 1. add C implies CE1 or ... or CEn, which is { not C or CE1 or ... or CEn }
            Set<OWLClassExpression> inclusion=new HashSet<OWLClassExpression>(axiom.getClassExpressions());
            inclusion.add(m_expressionManager.getComplementNNF(axiom.getOWLClass()));
            OWLClassExpression[] inclusionArray=new OWLClassExpression[axiom.getClassExpressions().size()+1];
            inclusion.toArray(inclusionArray);
            m_classExpressionInclusionsAsDisjunctions.add(inclusionArray);
            // 2. add CEi implies C, which is { not CEi or C }
            for (OWLClassExpression description : axiom.getClassExpressions())
                m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { negative(description),axiom.getOWLClass() });
            // 3. add CEi and CEj implies bottom (not CEi or not CEj) for 1 <= i < j <= n
            OWLClassExpression[] descriptions=new OWLClassExpression[axiom.getClassExpressions().size()];
            axiom.getClassExpressions().toArray(descriptions);
            for (int i=0;i<descriptions.length;i++)
                descriptions[i]=m_expressionManager.getComplementNNF(descriptions[i]);
            for (int i=0;i<descriptions.length;i++)
                for (int j=i+1;j<descriptions.length;j++)
                    m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { descriptions[i],descriptions[j] });
        }

        // Object property axioms

        public void visit(OWLSubObjectPropertyOfAxiom axiom) {
            if (!axiom.getSubProperty().isOWLBottomObjectProperty() && !axiom.getSuperProperty().isOWLTopObjectProperty())
                addInclusion(axiom.getSubProperty(),axiom.getSuperProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getSubProperty().getNamedProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getSuperProperty().getNamedProperty());
        }
        public void visit(OWLSubPropertyChainOfAxiom axiom) {
            List<OWLObjectPropertyExpression> subPropertyChain=axiom.getPropertyChain();
            if (!containsBottomObjectProperty(subPropertyChain) && !axiom.getSuperProperty().isOWLTopObjectProperty()) {
                OWLObjectPropertyExpression superObjectPropertyExpression=axiom.getSuperProperty();
                if (subPropertyChain.size()==1)
                    addInclusion(subPropertyChain.get(0),superObjectPropertyExpression);
                else if (subPropertyChain.size()==2 && subPropertyChain.get(0).equals(superObjectPropertyExpression) && subPropertyChain.get(1).equals(superObjectPropertyExpression))
                    makeTransitive(axiom.getSuperProperty());
                else if (subPropertyChain.size()==0)
                    throw new IllegalArgumentException("Error: In OWL 2 DL, an empty property chain in property chain axioms is not allowd, but the ontology contains an axiom that the empty chain is a subproperty of "+superObjectPropertyExpression+".");
                else {
                    OWLObjectPropertyExpression[] subObjectProperties=new OWLObjectPropertyExpression[subPropertyChain.size()];
                    subPropertyChain.toArray(subObjectProperties);
                    addInclusion(subObjectProperties,superObjectPropertyExpression);
                }
            }
            for (OWLObjectPropertyExpression objectPropertyExpression : subPropertyChain)
                m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(objectPropertyExpression.getNamedProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getSuperProperty().getNamedProperty());
        }
        protected boolean containsBottomObjectProperty(List<OWLObjectPropertyExpression> properties) {
            for (OWLObjectPropertyExpression property : properties)
                if (property.isOWLBottomObjectProperty())
                    return true;
            return false;
        }
        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            Set<OWLObjectPropertyExpression> objectPropertyExpressions=axiom.getProperties();
            if (objectPropertyExpressions.size()>1) {
                Iterator<OWLObjectPropertyExpression> iterator=objectPropertyExpressions.iterator();
                OWLObjectPropertyExpression first=iterator.next();
                OWLObjectPropertyExpression last=first;
                while (iterator.hasNext()) {
                    OWLObjectPropertyExpression next=iterator.next();
                    addInclusion(last,next);
                    last=next;
                }
                addInclusion(last,first);
            }
            for (OWLObjectPropertyExpression objectPropertyExpression : objectPropertyExpressions)
                m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(objectPropertyExpression.getNamedProperty());
        }
        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
            OWLObjectPropertyExpression[] objectPropertyExpressions=new OWLObjectPropertyExpression[axiom.getProperties().size()];
            axiom.getProperties().toArray(objectPropertyExpressions);
            for (int i=0;i<objectPropertyExpressions.length;i++) {
                objectPropertyExpressions[i]=objectPropertyExpressions[i].getSimplified();
                m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(objectPropertyExpressions[i].getNamedProperty());
            }
            m_axioms.m_disjointObjectProperties.add(objectPropertyExpressions);
        }
        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
            OWLObjectPropertyExpression first=axiom.getFirstProperty();
            OWLObjectPropertyExpression second=axiom.getSecondProperty();
            addInclusion(first,second.getInverseProperty());
            addInclusion(second,first.getInverseProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(first.getNamedProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(second.getNamedProperty());
        }
        public void visit(OWLObjectPropertyDomainAxiom axiom) {
            OWLObjectAllValuesFrom allPropertyNohting=m_factory.getOWLObjectAllValuesFrom(axiom.getProperty().getSimplified(),m_factory.getOWLNothing());
            m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { positive(axiom.getDomain()),allPropertyNohting });
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLObjectPropertyRangeAxiom axiom) {
            OWLObjectAllValuesFrom allPropertyRange=m_factory.getOWLObjectAllValuesFrom(axiom.getProperty().getSimplified(),positive(axiom.getRange()));
            m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { allPropertyRange });
        }
        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
            m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { m_factory.getOWLObjectMaxCardinality(1,axiom.getProperty().getSimplified()) });
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
            m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { m_factory.getOWLObjectMaxCardinality(1,axiom.getProperty().getSimplified().getInverseProperty()) });
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
            makeReflexive(axiom.getProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            makeIrreflexive(axiom.getProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
            OWLObjectPropertyExpression objectProperty=axiom.getProperty();
            addInclusion(objectProperty,objectProperty.getInverseProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
            makeAsymmetric(axiom.getProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
            makeTransitive(axiom.getProperty());
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }

        // Data property axioms

        public void visit(OWLSubDataPropertyOfAxiom axiom) {
            OWLDataPropertyExpression subDataProperty=axiom.getSubProperty();
            checkTopDataPropertyUse(subDataProperty,axiom);
            OWLDataPropertyExpression superDataProperty=axiom.getSuperProperty();
            if (!subDataProperty.isOWLBottomDataProperty() && !superDataProperty.isOWLTopDataProperty())
                addInclusion(subDataProperty,superDataProperty);
        }
        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
            for (OWLDataPropertyExpression dataPropertyExpression : axiom.getProperties())
                checkTopDataPropertyUse(dataPropertyExpression,axiom);
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
            for (OWLDataPropertyExpression dataProperty : dataProperties)
                checkTopDataPropertyUse(dataProperty,axiom);
            m_axioms.m_disjointDataProperties.add(dataProperties);
        }
        public void visit(OWLDataPropertyDomainAxiom axiom) {
            OWLDataPropertyExpression dataProperty=axiom.getProperty();
            checkTopDataPropertyUse(dataProperty,axiom);
            OWLDataRange dataNothing=m_factory.getOWLDataComplementOf(m_factory.getTopDatatype());
            OWLDataAllValuesFrom allPropertyDataNothing=m_factory.getOWLDataAllValuesFrom(dataProperty,dataNothing);
            m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { positive(axiom.getDomain()),allPropertyDataNothing });
        }
        public void visit(OWLDataPropertyRangeAxiom axiom) {
            OWLDataPropertyExpression dataProperty=axiom.getProperty();
            checkTopDataPropertyUse(dataProperty,axiom);
            OWLDataAllValuesFrom allPropertyRange=m_factory.getOWLDataAllValuesFrom(dataProperty,positive(axiom.getRange()));
            m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { allPropertyRange });
        }
        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
            OWLDataPropertyExpression dataProperty=axiom.getProperty();
            checkTopDataPropertyUse(dataProperty,axiom);
            m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { m_factory.getOWLDataMaxCardinality(1,dataProperty) });
        }
        protected void checkTopDataPropertyUse(OWLDataPropertyExpression dataPropertyExpression,OWLAxiom axiom) {
            if (dataPropertyExpression.isOWLTopDataProperty())
                throw new IllegalArgumentException("Error: In OWL 2 DL, owl:topDataProperty is only allowed to occur in the super property position of SubDataPropertyOf axioms, but the ontology contains an axiom "+axiom+" that violates this condition.");
        }

        // Assertions

        public void visit(OWLSameIndividualAxiom axiom) {
            if (axiom.containsAnonymousIndividuals())
                throw new IllegalArgumentException("The axiom "+axiom+" contains anonymous individuals, which is not allowed in OWL 2. ");
            addFact(axiom);
        }
        public void visit(OWLDifferentIndividualsAxiom axiom) {
            if (axiom.containsAnonymousIndividuals())
                throw new IllegalArgumentException("The axiom "+axiom+" contains anonymous individuals, which is not allowed in OWL 2. ");
            addFact(axiom);
        }
        public void visit(OWLClassAssertionAxiom axiom) {
            OWLClassExpression classExpression=axiom.getClassExpression();
            if (classExpression instanceof OWLDataHasValue) {
                OWLDataHasValue hasValue=(OWLDataHasValue)classExpression;
                addFact(m_factory.getOWLDataPropertyAssertionAxiom(hasValue.getProperty(), axiom.getIndividual(), hasValue.getValue()));
                return;
            }
            if (classExpression instanceof OWLDataSomeValuesFrom) {
                OWLDataSomeValuesFrom someValuesFrom=(OWLDataSomeValuesFrom)classExpression;
                OWLDataRange dataRange=someValuesFrom.getFiller();
                if (dataRange instanceof OWLDataOneOf) {
                    OWLDataOneOf oneOf=(OWLDataOneOf)dataRange;
                    if (oneOf.getValues().size()==1) {
                        addFact(m_factory.getOWLDataPropertyAssertionAxiom(someValuesFrom.getProperty(),axiom.getIndividual(),oneOf.getValues().iterator().next()));
                        return;
                    }
                }
            }
            classExpression=positive(classExpression);
            if (!isSimple(classExpression)) {
                OWLClassExpression definition=getDefinitionFor(classExpression,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { negative(definition),classExpression });
                classExpression=definition;
            }
            addFact(m_factory.getOWLClassAssertionAxiom(classExpression,axiom.getIndividual()));
        }
        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            addFact(m_factory.getOWLObjectPropertyAssertionAxiom(axiom.getProperty().getSimplified(),axiom.getSubject(),axiom.getObject()));
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
            if (axiom.containsAnonymousIndividuals())
                throw new IllegalArgumentException("The axiom "+axiom+" contains anonymous individuals, which is not allowed in OWL 2 DL. ");
            addFact(m_factory.getOWLNegativeObjectPropertyAssertionAxiom(axiom.getProperty().getSimplified(),axiom.getSubject(),axiom.getObject()));
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(axiom.getProperty().getNamedProperty());
        }
        public void visit(OWLDataPropertyAssertionAxiom axiom) {
            checkTopDataPropertyUse(axiom.getProperty(),axiom);
            addFact(axiom);
        }
        public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
            checkTopDataPropertyUse(axiom.getProperty(),axiom);
            if (axiom.containsAnonymousIndividuals())
                throw new IllegalArgumentException("The axiom "+axiom+" contains anonymous individuals, which is not allowed in OWL 2 DL. ");
            addFact(axiom);
        }

        // Datatype definitions

        public void visit(OWLDatatypeDefinitionAxiom axiom) {
            m_axioms.m_definedDatatypesIRIs.add(axiom.getDatatype().getIRI().toString());
            m_dataRangeInclusionsAsDisjunctions.add(new OWLDataRange[] { negative(axiom.getDatatype()),positive(axiom.getDataRange()) });
            m_dataRangeInclusionsAsDisjunctions.add(new OWLDataRange[] { negative(axiom.getDataRange()),positive(axiom.getDatatype()) });
        }

        // Keys

        public void visit(OWLHasKeyAxiom axiom) {
            for (OWLDataPropertyExpression dataPropertyExpression : axiom.getDataPropertyExpressions())
                checkTopDataPropertyUse(dataPropertyExpression,axiom);
            OWLClassExpression description=positive(axiom.getClassExpression());
            if (!isSimple(description)) {
                OWLClassExpression definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_classExpressionInclusionsAsDisjunctions.add(new OWLClassExpression[] { negative(definition),description });
                description=definition;
            }
            m_axioms.m_hasKeys.add(m_factory.getOWLHasKeyAxiom(description,axiom.getPropertyExpressions()));
            for (OWLObjectPropertyExpression objectPropertyExpression : axiom.getObjectPropertyExpressions())
                m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(objectPropertyExpression.getNamedProperty());
        }

        // Rules

        public void visit(SWRLRule rule) {
            for (SWRLAtom atom : rule.getBody())
                if (atom instanceof SWRLDataPropertyAtom)
                    checkTopDataPropertyUse(((SWRLDataPropertyAtom)atom).getPredicate(),rule);
            for (SWRLAtom atom : rule.getHead())
                if (atom instanceof SWRLDataPropertyAtom)
                    checkTopDataPropertyUse(((SWRLDataPropertyAtom)atom).getPredicate(),rule);
            if (rule.getBody().isEmpty()) {
                // process as fact
                Rule2FactConverter r2fConverter=new Rule2FactConverter(m_classExpressionInclusionsAsDisjunctions);
                for (SWRLAtom at : rule.getHead())
                    at.accept(r2fConverter);
            }
            else
                m_rules.add(rule);
        }
    }

    protected class ClassExpressionNormalizer implements OWLClassExpressionVisitorEx<OWLClassExpression> {
        protected final Collection<OWLClassExpression[]> m_newInclusions;
        protected final Collection<OWLDataRange[]> m_newDataRangeInclusions;
        protected final boolean[] m_alreadyExists;

        public ClassExpressionNormalizer(Collection<OWLClassExpression[]> newInclusions,Collection<OWLDataRange[]> newDataRangeInclusions) {
            m_newInclusions=newInclusions;
            m_newDataRangeInclusions=newDataRangeInclusions;
            m_alreadyExists=new boolean[1];
        }
        public OWLClassExpression visit(OWLClass object) {
            return object;
        }
        public OWLClassExpression visit(OWLObjectIntersectionOf object) {
            OWLClassExpression definition=getDefinitionFor(object,m_alreadyExists);
            if (!m_alreadyExists[0])
                for (OWLClassExpression description : object.getOperands())
                    m_newInclusions.add(new OWLClassExpression[] { negative(definition),description });
            return definition;
        }
        public OWLClassExpression visit(OWLObjectUnionOf object) {
            throw new IllegalStateException("OR should be broken down at the outermost level");
        }
        public OWLClassExpression visit(OWLObjectComplementOf object) {
            if (isNominal(object.getOperand())) {
                OWLObjectOneOf objectOneOf=(OWLObjectOneOf)object.getOperand();
                OWLClass definition=getDefinitionForNegativeNominal(objectOneOf,m_alreadyExists);
                if (!m_alreadyExists[0])
                    for (OWLIndividual individual : objectOneOf.getIndividuals())
                        addFact(m_factory.getOWLClassAssertionAxiom(definition,individual));
                return m_factory.getOWLObjectComplementOf(definition);
            }
            else
                return object;
        }
        public OWLClassExpression visit(OWLObjectOneOf object) {
            for (OWLIndividual ind : object.getIndividuals())
                if (ind.isAnonymous())
                    throw new IllegalArgumentException("Error: The class expression "+object+" contains anonymous individuals, which is not allowed in OWL 2 (erratum in first OWL 2 spec, to be fixed with next publication of minor corrections). ");
            return object;
        }
        public OWLClassExpression visit(OWLObjectSomeValuesFrom object) {
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(object.getProperty().getNamedProperty());
            OWLClassExpression filler=object.getFiller();
            if (isSimple(filler) || isNominal(filler))
                // The ObjectOneof cases is an optimization.
                return object;
            else {
                OWLClassExpression definition=getDefinitionFor(filler,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLClassExpression[] { negative(definition),filler });
                return m_factory.getOWLObjectSomeValuesFrom(object.getProperty(),definition);
            }
        }
        public OWLClassExpression visit(OWLObjectAllValuesFrom object) {
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(object.getProperty().getNamedProperty());
            OWLClassExpression filler=object.getFiller();
            if (isSimple(filler) || isNominal(filler) || isNegatedOneNominal(filler))
                // The nominal cases are optimizations.
                return object;
            else {
                OWLClassExpression definition=getDefinitionFor(filler,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLClassExpression[] { negative(definition),filler });
                return m_factory.getOWLObjectAllValuesFrom(object.getProperty(),definition);
            }
        }
        public OWLClassExpression visit(OWLObjectHasValue object) {
            throw new IllegalStateException("Internal error: object value restrictions should have been simplified.");
        }
        public OWLClassExpression visit(OWLObjectHasSelf object) {
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(object.getProperty().getNamedProperty());
            return object;
        }
        public OWLClassExpression visit(OWLObjectMinCardinality object) {
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(object.getProperty().getNamedProperty());
            OWLClassExpression filler=object.getFiller();
            if (isSimple(filler))
                return object;
            else {
                OWLClassExpression definition=getDefinitionFor(filler,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLClassExpression[] { negative(definition),filler });
                return m_factory.getOWLObjectMinCardinality(object.getCardinality(),object.getProperty(),definition);
            }
        }
        public OWLClassExpression visit(OWLObjectMaxCardinality object) {
            m_axioms.m_objectPropertiesOccurringInOWLAxioms.add(object.getProperty().getNamedProperty());
            OWLClassExpression filler=object.getFiller();
            if (isSimple(filler))
                return object;
            else {
                OWLClassExpression complementDescription=m_expressionManager.getComplementNNF(filler);
                OWLClassExpression definition=getDefinitionFor(complementDescription,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLClassExpression[] { negative(definition),complementDescription });
                return m_factory.getOWLObjectMaxCardinality(object.getCardinality(),object.getProperty(),m_expressionManager.getComplementNNF(definition));
            }
        }
        public OWLClassExpression visit(OWLObjectExactCardinality object) {
            throw new IllegalStateException("Internal error: exact object cardinality restrictions should have been simplified.");
        }
        public OWLClassExpression visit(OWLDataSomeValuesFrom object) {
            OWLDataRange filler=object.getFiller();
            OWLDataPropertyExpression prop=object.getProperty();
            if (prop.isOWLTopDataProperty())
                throwInvalidTopDPUseError(object);
            if (isLiteral(filler))
                return m_factory.getOWLDataSomeValuesFrom(object.getProperty(),filler);
            else {
                OWLDatatype definition=getDefinitionFor(filler,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newDataRangeInclusions.add(new OWLDataRange[] { negative(definition),filler });
                return m_factory.getOWLDataSomeValuesFrom(object.getProperty(),definition);
            }
        }
        public OWLClassExpression visit(OWLDataAllValuesFrom object) {
            OWLDataRange filler=object.getFiller();
            OWLDataPropertyExpression prop=object.getProperty();
            if (prop.isOWLTopDataProperty())
                throwInvalidTopDPUseError(object);
            if (isLiteral(filler))
                return m_factory.getOWLDataAllValuesFrom(prop,filler);
            else {
                OWLDatatype definition=getDefinitionFor(filler,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newDataRangeInclusions.add(new OWLDataRange[] { negative(definition),filler });
                return m_factory.getOWLDataAllValuesFrom(prop,definition);
            }
        }
        protected void throwInvalidTopDPUseError(OWLClassExpression ex) {
            throw new IllegalArgumentException("Error: In OWL 2 DL, owl:topDataProperty is only allowed to occur in the super property position of SubDataPropertyOf axioms, but the ontology contains an axiom with the class expression "+ex+" that violates this restriction.");
        }
        public OWLClassExpression visit(OWLDataHasValue object) {
            throw new IllegalStateException("Internal error: data value restrictions should have been simplified.");
        }
        public OWLClassExpression visit(OWLDataMinCardinality object) {
            OWLDataRange filler=object.getFiller();
            OWLDataPropertyExpression prop=object.getProperty();
            if (prop.isOWLTopDataProperty())
                throwInvalidTopDPUseError(object);
            if (isLiteral(filler))
                return m_factory.getOWLDataMinCardinality(object.getCardinality(),prop,filler);
            else {
                OWLDatatype definition=getDefinitionFor(filler,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newDataRangeInclusions.add(new OWLDataRange[] { negative(definition),filler });
                return m_factory.getOWLDataMinCardinality(object.getCardinality(),prop,definition);
            }
        }
        public OWLClassExpression visit(OWLDataMaxCardinality object) {
            OWLDataRange filler=object.getFiller();
            OWLDataPropertyExpression prop=object.getProperty();
            if (prop.isOWLTopDataProperty())
                throwInvalidTopDPUseError(object);
            if (isLiteral(filler))
                return m_factory.getOWLDataMaxCardinality(object.getCardinality(),prop,filler);
            else {
                OWLDataRange complementDescription=m_expressionManager.getComplementNNF(filler);
                OWLDatatype definition=getDefinitionFor(complementDescription,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newDataRangeInclusions.add(new OWLDataRange[] { negative(definition),filler });
                return m_factory.getOWLDataMaxCardinality(object.getCardinality(),prop,m_expressionManager.getComplementNNF(definition));
            }
        }
        public OWLClassExpression visit(OWLDataExactCardinality object) {
            throw new IllegalStateException("Internal error: exact data cardinality restrictions should have been simplified.");
        }
    }

    protected class DataRangeNormalizer implements OWLDataVisitorEx<OWLDataRange> {
        protected final Collection<OWLDataRange[]> m_newDataRangeInclusions;
        protected final boolean[] m_alreadyExists;

        public DataRangeNormalizer(Collection<OWLDataRange[]> newDataRangeInclusions) {
            m_newDataRangeInclusions=newDataRangeInclusions;
            m_alreadyExists=new boolean[1];
        }
        public OWLDataRange visit(OWLDatatype node) {
            return node;
        }
        public OWLDataRange visit(OWLDataComplementOf node) {
            return node;
        }
        public OWLDataRange visit(OWLDataOneOf node) {
            return node;
        }
        public OWLDataRange visit(OWLDataIntersectionOf object) {
            OWLDataRange definition=getDefinitionFor(object,m_alreadyExists);
            if (!m_alreadyExists[0])
                for (OWLDataRange description : object.getOperands())
                    m_newDataRangeInclusions.add(new OWLDataRange[] { negative(definition),description });
            return definition;
        }
        public OWLDataRange visit(OWLDataUnionOf node) {
            throw new IllegalStateException("OR should be broken down at the outermost level");
        }
        public OWLDataRange visit(OWLDatatypeRestriction node) {
            return node;
        }
        public OWLDataRange visit(OWLFacetRestriction node) {
            throw new IllegalStateException("Internal error: We shouldn't visit facet restrictions during normalization. ");
        }
        public OWLDataRange visit(OWLLiteral node) {
            throw new IllegalStateException("Internal error: We shouldn't visit typed literals during normalization. ");
        }
    }

    protected class Rule2FactConverter implements SWRLObjectVisitor {

        protected final boolean[] m_alreadyExists;
        protected final Collection<OWLClassExpression[]> m_newInclusions;
        protected int freshDataProperties=0;
        protected int freshIndividuals=0;

        public Rule2FactConverter(Collection<OWLClassExpression[]> newInclusions) {
            m_alreadyExists=new boolean[1];
            m_newInclusions=newInclusions;
        }
        protected OWLNamedIndividual getFreshIndividual() {
            OWLNamedIndividual freshInd=m_factory.getOWLNamedIndividual(IRI.create("internal:nom#swrlfact"+freshIndividuals));
            freshIndividuals++;
            m_axioms.m_namedIndividuals.add(freshInd);
            return freshInd;
        }
        protected OWLDataProperty getFreshDataProperty() {
            freshDataProperties++;
            return m_factory.getOWLDataProperty(IRI.create("internal:freshDP#"+freshDataProperties));
        }
        public void visit(SWRLRule rule) {
        }
        public void visit(SWRLClassAtom atom) {
            if (!(atom.getArgument() instanceof SWRLIndividualArgument))
                throw new IllegalArgumentException("A SWRL rule contains a head atom "+atom+" with a variable that does not occur in the body. ");
            OWLIndividual ind=((SWRLIndividualArgument)atom.getArgument()).getIndividual();
            if (ind.isAnonymous())
                throwAnonIndError(atom);
            if (!isSimple(atom.getPredicate())) {
                OWLClassExpression definition=getDefinitionFor(atom.getPredicate(),m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLClassExpression[] { negative(definition),atom.getPredicate() });
                addFact(m_factory.getOWLClassAssertionAxiom(definition,ind.asOWLNamedIndividual()));
            }
            else
                addFact(m_factory.getOWLClassAssertionAxiom(atom.getPredicate(),ind.asOWLNamedIndividual()));
        }
        public void visit(SWRLDataRangeAtom atom) {
            if (atom.getArgument() instanceof SWRLVariable)
                throwVarError(atom);
            // dr(literal) :-
            // convert to: ClassAssertion(DataSomeValuesFrom(freshDP DataOneOf(literal)) freshIndividual)
            // and top -> \forall freshDP.dr
            OWLLiteral lit=((SWRLLiteralArgument)atom.getArgument()).getLiteral();
            OWLDataRange dr=atom.getPredicate();
            OWLNamedIndividual freshIndividual=getFreshIndividual();
            OWLDataProperty freshDP=getFreshDataProperty();
            OWLDataSomeValuesFrom some=m_factory.getOWLDataSomeValuesFrom(freshDP,m_factory.getOWLDataOneOf(lit));
            OWLClassExpression definition=getDefinitionFor(some,m_alreadyExists);
            if (!m_alreadyExists[0])
                m_newInclusions.add(new OWLClassExpression[] { negative(definition),some });
            addFact(m_factory.getOWLClassAssertionAxiom(definition,freshIndividual));
            m_newInclusions.add(new OWLClassExpression[] { m_factory.getOWLDataAllValuesFrom(freshDP,dr) });
        }

        public void visit(SWRLObjectPropertyAtom atom) {
            if (!(atom.getFirstArgument() instanceof SWRLIndividualArgument) || !(atom.getSecondArgument() instanceof SWRLIndividualArgument))
                throwVarError(atom);
            OWLObjectPropertyExpression ope=atom.getPredicate().getSimplified();
            OWLIndividual first=((SWRLIndividualArgument)atom.getFirstArgument()).getIndividual();
            OWLIndividual second=((SWRLIndividualArgument)atom.getSecondArgument()).getIndividual();
            if (first.isAnonymous() || second.isAnonymous())
                throwAnonIndError(atom);
            if (ope.isAnonymous())
                addFact(m_factory.getOWLObjectPropertyAssertionAxiom(ope.getNamedProperty(),second.asOWLNamedIndividual(),first.asOWLNamedIndividual()));
            else
                addFact(m_factory.getOWLObjectPropertyAssertionAxiom(ope.asOWLObjectProperty(),first.asOWLNamedIndividual(),second.asOWLNamedIndividual()));
        }
        public void visit(SWRLDataPropertyAtom atom) {
            if (!(atom.getSecondArgument() instanceof SWRLLiteralArgument))
                throwVarError(atom);
            if (!(atom.getFirstArgument() instanceof SWRLIndividualArgument))
                throwVarError(atom);
            OWLIndividual ind=((SWRLIndividualArgument)atom.getFirstArgument()).getIndividual();
            if (ind.isAnonymous())
                throwAnonIndError(atom);
            OWLLiteral lit=((SWRLLiteralArgument)atom.getSecondArgument()).getLiteral();
            addFact(m_factory.getOWLDataPropertyAssertionAxiom(atom.getPredicate().asOWLDataProperty(),ind.asOWLNamedIndividual(),lit));
        }
        public void visit(SWRLBuiltInAtom atom) {
            throw new IllegalArgumentException("Error: A rule uses built-in atoms ("+atom+"), but built-in atoms are not supported yet. ");
        }
        public void visit(SWRLSameIndividualAtom atom) {
            Set<OWLNamedIndividual> inds=new HashSet<OWLNamedIndividual>();
            for (SWRLArgument arg : atom.getAllArguments()) {
                if (!(arg instanceof SWRLIndividualArgument))
                    throwVarError(atom);
                OWLIndividual ind=((SWRLIndividualArgument)arg).getIndividual();
                if (ind.isAnonymous())
                    throwAnonIndError(atom);
                inds.add(ind.asOWLNamedIndividual());
            }
            addFact(m_factory.getOWLSameIndividualAxiom(inds));
        }
        public void visit(SWRLDifferentIndividualsAtom atom) {
            Set<OWLNamedIndividual> inds=new HashSet<OWLNamedIndividual>();
            for (SWRLArgument arg : atom.getAllArguments()) {
                if (!(arg instanceof SWRLIndividualArgument))
                    throwVarError(atom);
                OWLIndividual ind=((SWRLIndividualArgument)arg).getIndividual();
                if (ind.isAnonymous())
                    throwAnonIndError(atom);
                inds.add(ind.asOWLNamedIndividual());
            }
            addFact(m_factory.getOWLDifferentIndividualsAxiom(inds));
        }
        public void visit(SWRLVariable variable) {
        }
        public void visit(SWRLIndividualArgument argument) {
        }
        public void visit(SWRLLiteralArgument argument) {
        }
        protected void throwAnonIndError(SWRLAtom atom) {
            throw new IllegalArgumentException("A SWRL rule contains a fact ("+atom+") with an anonymous individual, which is not allowed. ");
        }
        protected void throwVarError(SWRLAtom atom) {
            throw new IllegalArgumentException("A SWRL rule contains a head atom ("+atom+") with a variable that does not occur in the body. ");
        }
    }

    protected final class RuleNormalizer implements SWRLObjectVisitor {
        protected final Collection<OWLAxioms.DisjunctiveRule> m_rules;
        protected final Collection<OWLClassExpression[]> m_classExpressionInclusions;
        protected final Collection<OWLDataRange[]> m_dataRangeInclusions;
        protected final boolean[] m_alreadyExists;
        protected final List<SWRLAtom> m_bodyAtoms=new ArrayList<SWRLAtom>();
        protected final List<SWRLAtom> m_headAtoms=new ArrayList<SWRLAtom>();
        protected final Set<SWRLAtom> m_normalizedBodyAtoms=new HashSet<SWRLAtom>();
        protected final Set<SWRLAtom> m_normalizedHeadAtoms=new HashSet<SWRLAtom>();
        protected final Map<SWRLVariable,SWRLVariable> m_variableRepresentative=new HashMap<SWRLVariable,SWRLVariable>();
        protected final Map<OWLNamedIndividual,SWRLVariable> m_individualsToVariables=new HashMap<OWLNamedIndividual,SWRLVariable>();
        protected final Set<SWRLVariable> m_bodyDataRangeVariables=new HashSet<SWRLVariable>();
        protected final Set<SWRLVariable> m_headDataRangeVariables=new HashSet<SWRLVariable>();
        protected int m_newVariableIndex=0;
        protected boolean m_isPositive;

        public RuleNormalizer(Collection<OWLAxioms.DisjunctiveRule> rules,Collection<OWLClassExpression[]> classExpressionInclusionsFromRules,Collection<OWLDataRange[]> newDataRangeInclusions) {
            m_rules=rules;
            m_classExpressionInclusions=classExpressionInclusionsFromRules;
            m_dataRangeInclusions=newDataRangeInclusions;
            m_alreadyExists=new boolean[1];
        }
        public void visit(SWRLRule rule) {
            // Process head one-by-one and thus break up the conjunction in the head.
            for (SWRLAtom headAtom : rule.getHead()) {
                m_individualsToVariables.clear();
                m_bodyAtoms.clear();
                m_headAtoms.clear();
                m_variableRepresentative.clear();
                m_normalizedBodyAtoms.clear();
                m_normalizedHeadAtoms.clear();
                m_bodyDataRangeVariables.clear();
                m_headDataRangeVariables.clear();

                // Initialize body with all atoms, and initialize head with just the atom we are processing.
                m_bodyAtoms.addAll(rule.getBody());
                m_headAtoms.add(headAtom);

                // First process sameIndividual in the body to set up variable normalizations.
                for (SWRLAtom atom : rule.getBody()) {
                    if (atom instanceof SWRLSameIndividualAtom) {
                        m_bodyAtoms.remove(atom);
                        SWRLSameIndividualAtom sameIndividualAtom=(SWRLSameIndividualAtom)atom;
                        SWRLVariable variable1=getVariableFor(sameIndividualAtom.getFirstArgument());
                        SWRLIArgument argument2=sameIndividualAtom.getSecondArgument();
                        if (argument2 instanceof SWRLVariable)
                            m_variableRepresentative.put((SWRLVariable)argument2,variable1);
                        else {
                            OWLIndividual individual=((SWRLIndividualArgument)argument2).getIndividual();
                            if (individual.isAnonymous())
                                throw new IllegalArgumentException("Internal error: Rules with anonymous individuals are not supported. ");
                            m_individualsToVariables.put(individual.asOWLNamedIndividual(),variable1);
                            m_bodyAtoms.add(m_factory.getSWRLClassAtom(m_factory.getOWLObjectOneOf(individual),variable1));
                        }
                    }
                }

                // Now process head atoms; this might increase the number of body atoms.
                m_isPositive=true;
                while (!m_headAtoms.isEmpty())
                    m_headAtoms.remove(0).accept(this);

                // Now process body atoms.
                m_isPositive=false;
                while (!m_bodyAtoms.isEmpty())
                    m_bodyAtoms.remove(0).accept(this);

                // Do some checking and return the rule.
                if (!m_bodyDataRangeVariables.containsAll(m_headDataRangeVariables))
                    throw new IllegalArgumentException("A SWRL rule contains data range variables in the head, but not in the body, and this is not supported.");
                m_rules.add(new OWLAxioms.DisjunctiveRule(m_normalizedBodyAtoms.toArray(new SWRLAtom[m_normalizedBodyAtoms.size()]),m_normalizedHeadAtoms.toArray(new SWRLAtom[m_normalizedHeadAtoms.size()])));
            }
        }
        public void visit(SWRLClassAtom at) {
            OWLClassExpression c=m_expressionManager.getSimplified(m_expressionManager.getNNF(at.getPredicate()));
            SWRLVariable variable=getVariableFor(at.getArgument());
            if (m_isPositive) {
                // head
                if (c instanceof OWLClass)
                    m_normalizedHeadAtoms.add(m_factory.getSWRLClassAtom(c,variable));
                else {
                    OWLClass definition=getClassFor(at.getPredicate(),m_alreadyExists);
                    if (!m_alreadyExists[0])
                        m_classExpressionInclusions.add(new OWLClassExpression[] { negative(definition),at.getPredicate() });
                    m_normalizedHeadAtoms.add(m_factory.getSWRLClassAtom(definition,variable));
                }
            }
            else {
                // body
                if (c instanceof OWLClass)
                    m_normalizedBodyAtoms.add(m_factory.getSWRLClassAtom(c,variable));
                else {
                    OWLClass definition=getClassFor(at.getPredicate(),m_alreadyExists);
                    if (!m_alreadyExists[0])
                        m_classExpressionInclusions.add(new OWLClassExpression[] { negative(at.getPredicate()),definition });
                    m_normalizedBodyAtoms.add(m_factory.getSWRLClassAtom(definition,variable));
                }
            }
        }
        public void visit(SWRLDataRangeAtom at) {
            OWLDataRange dr=at.getPredicate();
            SWRLDArgument argument=at.getArgument();
            if (!(argument instanceof SWRLVariable))
                throw new IllegalArgumentException("A SWRL rule contains a data range with an argument that is not a literal, and such rules are not supported.");
            if (!m_isPositive)
                dr=m_factory.getOWLDataComplementOf(dr);
            dr=m_expressionManager.getNNF(m_expressionManager.getSimplified(dr));
            if (dr instanceof OWLDataIntersectionOf || dr instanceof OWLDataUnionOf) {
                OWLDatatype definition=getDefinitionFor(dr,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_dataRangeInclusions.add(new OWLDataRange[] { negative(definition),dr });
                dr=definition;
            }
            SWRLAtom atom=m_factory.getSWRLDataRangeAtom(dr,argument);
            m_normalizedHeadAtoms.add(atom);
            m_headDataRangeVariables.add((SWRLVariable)argument);
        }
        public void visit(SWRLObjectPropertyAtom at) {
            OWLObjectPropertyExpression ope=at.getPredicate().getSimplified();
            OWLObjectProperty op=ope.getNamedProperty();
            SWRLVariable variable1;
            SWRLVariable variable2;
            if (ope.isAnonymous()) {
                variable1=getVariableFor(at.getSecondArgument());
                variable2=getVariableFor(at.getFirstArgument());
            }
            else {
                variable1=getVariableFor(at.getFirstArgument());
                variable2=getVariableFor(at.getSecondArgument());

            }
            SWRLAtom newAtom=m_factory.getSWRLObjectPropertyAtom(op,variable1,variable2);
            if (m_isPositive) {
                // head
                m_normalizedHeadAtoms.add(newAtom);
            }
            else {
                // body
                m_normalizedBodyAtoms.add(newAtom);
            }
        }
        public void visit(SWRLDataPropertyAtom at) {
            OWLDataProperty dp=at.getPredicate().asOWLDataProperty();
            SWRLVariable variable1=getVariableFor(at.getFirstArgument());
            SWRLDArgument argument2=at.getSecondArgument();
            if (argument2 instanceof SWRLVariable) {
                SWRLVariable variable2=getVariableFor((SWRLVariable)argument2);
                if (m_isPositive) {
                    m_normalizedHeadAtoms.add(m_factory.getSWRLDataPropertyAtom(dp,variable1,variable2));
                    m_headDataRangeVariables.add(variable2);
                }
                else {
                    if (m_bodyDataRangeVariables.add(variable2))
                        m_normalizedBodyAtoms.add(m_factory.getSWRLDataPropertyAtom(dp,variable1,variable2));
                    else {
                        SWRLVariable variable2Fresh=getFreshVariable();
                        m_normalizedBodyAtoms.add(m_factory.getSWRLDataPropertyAtom(dp,variable1,variable2Fresh));
                        m_normalizedHeadAtoms.add(m_factory.getSWRLDifferentIndividualsAtom(variable2,variable2Fresh));
                    }
                }
            }
            else {
                OWLLiteral literal=((SWRLLiteralArgument)argument2).getLiteral();
                SWRLAtom newAtom=m_factory.getSWRLClassAtom(m_factory.getOWLDataHasValue(dp,literal),variable1);
                if (m_isPositive)
                    m_headAtoms.add(newAtom);
                else
                    m_bodyAtoms.add(newAtom);
            }
        }
        public void visit(SWRLBuiltInAtom at) {
            throw new IllegalArgumentException("A SWRL rule uses a built-in atom, but built-in atoms are not supported yet.");
        }
        public void visit(SWRLSameIndividualAtom at) {
            if (m_isPositive)
                m_normalizedHeadAtoms.add(m_factory.getSWRLSameIndividualAtom(getVariableFor(at.getFirstArgument()),getVariableFor(at.getSecondArgument())));
            else
                throw new IllegalStateException("Internal error: this SWRLSameIndividualAtom should have been processed earlier.");
        }
        public void visit(SWRLDifferentIndividualsAtom at) {
            if (m_isPositive)
                m_normalizedHeadAtoms.add(m_factory.getSWRLDifferentIndividualsAtom(getVariableFor(at.getFirstArgument()),getVariableFor(at.getSecondArgument())));
            else
                m_normalizedHeadAtoms.add(m_factory.getSWRLSameIndividualAtom(getVariableFor(at.getFirstArgument()),getVariableFor(at.getSecondArgument())));
        }
        public void visit(SWRLVariable variable) {
            // nothing to do
        }
        public void visit(SWRLIndividualArgument argument) {
            // nothing to do
        }
        public void visit(SWRLLiteralArgument argument) {
            // nothing to do
        }
        protected SWRLVariable getVariableFor(SWRLIArgument term) {
            SWRLVariable variable;
            if (term instanceof SWRLIndividualArgument) {
                OWLIndividual individual=((SWRLIndividualArgument)term).getIndividual();
                if (individual.isAnonymous())
                    throw new IllegalArgumentException("Internal error: Rules with anonymous individuals are not supported. ");
                variable=m_individualsToVariables.get(individual.asOWLNamedIndividual());
                if (variable==null) {
                    variable=getFreshVariable();
                    m_individualsToVariables.put(individual.asOWLNamedIndividual(),variable);
                    m_bodyAtoms.add(m_factory.getSWRLClassAtom(m_factory.getOWLObjectOneOf(individual),variable));
                }
            }
            else
                variable=(SWRLVariable)term;
            SWRLVariable representative=m_variableRepresentative.get(variable);
            if (representative==null)
                return variable;
            else
                return representative;
        }
        protected SWRLVariable getFreshVariable() {
            SWRLVariable variable=m_factory.getSWRLVariable(IRI.create("internal:swrl#"+m_newVariableIndex));
            m_newVariableIndex++;
            return variable;
        }
    }

    /**
     * checks the polarity
     */
    protected class PLVisitor implements OWLClassExpressionVisitorEx<Boolean> {

        public Boolean visit(OWLClass object) {
            if (object.isOWLThing())
                return Boolean.FALSE;
            else if (object.isOWLNothing())
                return Boolean.FALSE;
            else
                return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectIntersectionOf object) {
            for (OWLClassExpression desc : object.getOperands())
                if (desc.accept(this))
                    return Boolean.TRUE;
            return Boolean.FALSE;
        }
        public Boolean visit(OWLObjectUnionOf object) {
            for (OWLClassExpression desc : object.getOperands())
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
        public Boolean visit(OWLObjectSomeValuesFrom object) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectAllValuesFrom object) {
            return object.getFiller().accept(this);
        }
        public Boolean visit(OWLObjectHasValue object) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectHasSelf object) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectMinCardinality object) {
            return object.getCardinality()>0;
        }
        public Boolean visit(OWLObjectMaxCardinality object) {
            return object.getCardinality()>0 ? Boolean.TRUE : m_expressionManager.getComplementNNF(object.getFiller()).accept(this);
        }
        public Boolean visit(OWLObjectExactCardinality object) {
            return object.getCardinality()>0 ? Boolean.TRUE : m_expressionManager.getComplementNNF(object.getFiller()).accept(this);
        }
        public Boolean visit(OWLDataSomeValuesFrom desc) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLDataAllValuesFrom desc) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLDataHasValue desc) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLDataMinCardinality desc) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLDataMaxCardinality desc) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLDataExactCardinality desc) {
            return Boolean.TRUE;
        }
    }
}
