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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.structural.OWLAxioms.ComplexObjectPropertyInclusion;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

public class ObjectPropertyInclusionManager {
    protected final Map<OWLObjectPropertyExpression,Automaton> m_automataByProperty;

    public ObjectPropertyInclusionManager(OWLAxioms axioms) {
        m_automataByProperty=new HashMap<OWLObjectPropertyExpression,Automaton>();
        createAutomata(m_automataByProperty,axioms.m_complexObjectPropertyExpressions,axioms.m_simpleObjectPropertyInclusions,axioms.m_complexObjectPropertyInclusions);
    }
    public int rewriteNegativeObjectPropertyAssertions(OWLDataFactory factory,OWLAxioms axioms,int replacementIndex) {
        // now object property inclusion manager added all non-simple properties to axioms.m_complexObjectPropertyExpressions
        // now that we know which roles are non-simple, we can decide which negative object property assertions have to be
        // expressed as concept assertions so that transitivity rewriting applies properly. All new concepts for the concept
        // assertions must be normalised, because we are done with the normal normalisation phase.
        Set<OWLIndividualAxiom> redundantFacts=new HashSet<OWLIndividualAxiom>();
        Set<OWLIndividualAxiom> additionalFacts=new HashSet<OWLIndividualAxiom>();
        for (OWLIndividualAxiom axiom : axioms.m_facts) {
            if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiom) {
                OWLNegativeObjectPropertyAssertionAxiom negAssertion=(OWLNegativeObjectPropertyAssertionAxiom)axiom;
                OWLObjectPropertyExpression prop=negAssertion.getProperty().getSimplified();
                if (axioms.m_complexObjectPropertyExpressions.contains(prop)) {
                    // turn not op(a b) into
                    // C(a) and not C or forall op not{b}
                    OWLIndividual individual=negAssertion.getObject();
                    // neg. op assertions cannot contain anonymous individuals
                    OWLClass individualConcept=factory.getOWLClass(IRI.create("internal:nom#"+individual.asOWLNamedIndividual().getIRI().toString()));
                    OWLClassExpression notIndividualConcept=factory.getOWLObjectComplementOf(individualConcept);
                    OWLClassExpression allNotIndividualConcept=factory.getOWLObjectAllValuesFrom(prop,notIndividualConcept);
                    OWLClassExpression definition=factory.getOWLClass(IRI.create("internal:def#"+(replacementIndex++)));
                    axioms.m_conceptInclusions.add(new OWLClassExpression[] { factory.getOWLObjectComplementOf(definition), allNotIndividualConcept });
                    additionalFacts.add(factory.getOWLClassAssertionAxiom(definition,negAssertion.getSubject()));
                    additionalFacts.add(factory.getOWLClassAssertionAxiom(individualConcept,individual));
                    redundantFacts.add(negAssertion);
                }
            }
        }
        axioms.m_facts.addAll(additionalFacts);
        axioms.m_facts.removeAll(redundantFacts);
        return replacementIndex;
    }
    public void rewriteAxioms(OWLDataFactory dataFactory,OWLAxioms axioms,int firstReplacementIndex) {
        // Check the asymmetric object properties for simplicity
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_asymmetricObjectProperties)
            if (axioms.m_complexObjectPropertyExpressions.contains(objectPropertyExpression))
                throw new IllegalArgumentException("Non-simple property '"+objectPropertyExpression+"' or its inverse appears in asymmetric object property axiom.");
        // Check the irreflexive object properties for simplicity
        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_irreflexiveObjectProperties)
            if (axioms.m_complexObjectPropertyExpressions.contains(objectPropertyExpression))
                throw new IllegalArgumentException("Non-simple property '"+objectPropertyExpression+"' or its inverse appears in irreflexive object property axiom.");
        // Check the disjoint object properties for simplicity
        for (OWLObjectPropertyExpression[] properties : axioms.m_disjointObjectProperties)
            for (int i=0;i<properties.length;i++)
                if (axioms.m_complexObjectPropertyExpressions.contains(properties[i]))
                    throw new IllegalArgumentException("Non-simple property '"+properties[i]+"' or its inverse appears in disjoint properties axiom.");
        // Check simple properties in the number restrictions and replace universals
        Map<OWLObjectAllValuesFrom,OWLClassExpression> replacedDescriptions=new HashMap<OWLObjectAllValuesFrom,OWLClassExpression>();
        for (OWLClassExpression[] inclusion : axioms.m_conceptInclusions) {
            for (int index=0;index<inclusion.length;index++) {
                OWLClassExpression classExpression=inclusion[index];
                if (classExpression instanceof OWLObjectCardinalityRestriction) {
                    OWLObjectCardinalityRestriction objectCardinalityRestriction=(OWLObjectCardinalityRestriction)classExpression;
                    OWLObjectPropertyExpression objectPropertyExpression=objectCardinalityRestriction.getProperty();
                    if (axioms.m_complexObjectPropertyExpressions.contains(objectPropertyExpression))
                        throw new IllegalArgumentException("Non-simple property '"+objectPropertyExpression+"' or its inverse appears in the cardinality restriction '"+objectCardinalityRestriction+"'.");
                }
                else if (classExpression instanceof OWLObjectHasSelf) {
                	OWLObjectHasSelf objectSelfRestriction=(OWLObjectHasSelf)classExpression;
                	if (axioms.m_complexObjectPropertyExpressions.contains(objectSelfRestriction.getProperty()))
                        throw new IllegalArgumentException("Non-simple property '"+objectSelfRestriction.getProperty()+"' or its inverse appears in the Self restriction '"+objectSelfRestriction+"'.");
                }
                if (classExpression instanceof OWLObjectAllValuesFrom) {
                    OWLObjectAllValuesFrom objectAll=(OWLObjectAllValuesFrom)classExpression;
                    if (!objectAll.getFiller().equals(dataFactory.getOWLThing())) {
                        OWLObjectPropertyExpression objectProperty=objectAll.getProperty();
                        if (m_automataByProperty.containsKey(objectProperty)) {
                            OWLClassExpression replacement=replacedDescriptions.get(objectAll);
                            if (replacement==null) {
                                replacement=dataFactory.getOWLClass(IRI.create("internal:all#"+(firstReplacementIndex++)));
                                if (objectAll.getFiller() instanceof OWLObjectComplementOf || objectAll.getFiller().equals(dataFactory.getOWLNothing()))
                                    replacement=replacement.getComplementNNF();
                                replacedDescriptions.put(objectAll,replacement);
                            }
                            inclusion[index]=replacement;
                        }
                    }
                }
            }
        }
        // Generate the automaton for each replacement
        for (Map.Entry<OWLObjectAllValuesFrom,OWLClassExpression> replacement : replacedDescriptions.entrySet()) {
            Automaton automaton=m_automataByProperty.get(replacement.getKey().getProperty());
            boolean isOfNegativePolarity=(replacement.getValue() instanceof OWLObjectComplementOf);
            // Generate states of the automaton
            Map<State,OWLClassExpression> statesToConcepts=new HashMap<State,OWLClassExpression>();
            for (Object stateObject : automaton.states()) {
                State state=(State)stateObject;
                if (state.isInitial())
                    statesToConcepts.put(state,replacement.getValue());
                else {
                    OWLClassExpression stateConcept=dataFactory.getOWLClass(IRI.create("internal:all#"+(firstReplacementIndex++)));
                    if (isOfNegativePolarity)
                        stateConcept=stateConcept.getComplementNNF();
                    statesToConcepts.put(state,stateConcept);
                }
            }
            // Generate the transitions
            for (Object transitionObject : automaton.delta()) {
                Transition transition=(Transition)transitionObject;
                OWLClassExpression fromStateConcept=statesToConcepts.get(transition.start()).getComplementNNF();
                OWLClassExpression toStateConcept=statesToConcepts.get(transition.end());
                if (transition.label()==null)
                    axioms.m_conceptInclusions.add(new OWLClassExpression[] { fromStateConcept,toStateConcept });
                else {
                    OWLObjectAllValuesFrom consequentAll=dataFactory.getOWLObjectAllValuesFrom((OWLObjectPropertyExpression)transition.label(),toStateConcept);
                    axioms.m_conceptInclusions.add(new OWLClassExpression[] { fromStateConcept,consequentAll });
                }
            }
            // Generate the final states
            OWLClassExpression filler=replacement.getKey().getFiller();
            for (Object finalStateObject : automaton.terminals()) {
                OWLClassExpression finalStateConceptComplement=statesToConcepts.get(finalStateObject).getComplementNNF();
                if (filler.isOWLNothing())
                    axioms.m_conceptInclusions.add(new OWLClassExpression[] { finalStateConceptComplement });
                else
                    axioms.m_conceptInclusions.add(new OWLClassExpression[] { finalStateConceptComplement,filler });
            }
        }
    }
    protected void createAutomata(Map<OWLObjectPropertyExpression,Automaton> automataByProperty,Set<OWLObjectPropertyExpression> complexObjectPropertyExpressions,Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions,Collection<ComplexObjectPropertyInclusion> complexObjectPropertyInclusions) {
        Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> equivalentPropertiesMap=findEquivalentProperties(simpleObjectPropertyInclusions);
        Set<OWLObjectPropertyExpression> symmetricObjectProperties=findSymmetricProperties(simpleObjectPropertyInclusions);
        Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> inversePropertiesMap=buildInversePropertiesMap(simpleObjectPropertyInclusions);
        Graph<OWLObjectPropertyExpression> propertyDependencyGraph=buildPropertyOrdering(simpleObjectPropertyInclusions,complexObjectPropertyInclusions,equivalentPropertiesMap);
        checkForRegularity(propertyDependencyGraph,equivalentPropertiesMap);

        Graph<OWLObjectPropertyExpression> complexPropertiesDependencyGraph=propertyDependencyGraph.clone();
        Set<OWLObjectPropertyExpression> transitiveProperties=new HashSet<OWLObjectPropertyExpression>();
        Map<OWLObjectPropertyExpression,Automaton> individualAutomata=buildIndividualAutomata(complexPropertiesDependencyGraph,simpleObjectPropertyInclusions,complexObjectPropertyInclusions,equivalentPropertiesMap,transitiveProperties);
        Set<OWLObjectPropertyExpression> simpleProperties=findSimpleProperties(complexPropertiesDependencyGraph,individualAutomata);

        propertyDependencyGraph.removeElements(simpleProperties);

        complexPropertiesDependencyGraph.removeElements(simpleProperties);
        complexObjectPropertyExpressions.addAll(complexPropertiesDependencyGraph.getElements());
       	for (OWLObjectPropertyExpression[] inclusion : simpleObjectPropertyInclusions) {
       		if (complexObjectPropertyExpressions.contains(inclusion[0]) && individualAutomata.containsKey(inclusion[1])) {
       			Automaton auto = individualAutomata.get(inclusion[1]);
       			try {
       				auto.addTransition(new Transition((State)auto.initials().iterator().next(),inclusion[0],(State)auto.terminals().iterator().next()));
       				individualAutomata.put(inclusion[1], auto);
       			} catch (NoSuchStateException e) {
       				throw new IllegalArgumentException("Could not create automaton for property at the bottom of hierarchy (simple property).");
       			}
       		}
       	}
        Set<OWLObjectPropertyExpression> inverseOfComplexProperties = new HashSet<OWLObjectPropertyExpression>();
        for (OWLObjectPropertyExpression complexProp : complexObjectPropertyExpressions)
        	inverseOfComplexProperties.add(complexProp.getInverseProperty().getSimplified());
        complexObjectPropertyExpressions.addAll(inverseOfComplexProperties);

        connectAllAutomata(automataByProperty,propertyDependencyGraph,inversePropertiesMap,individualAutomata,simpleObjectPropertyInclusions,symmetricObjectProperties,transitiveProperties);
        Map<OWLObjectPropertyExpression,Automaton> individualAutomataForEquivRoles=new HashMap<OWLObjectPropertyExpression,Automaton>();
        for (OWLObjectPropertyExpression propExprWithAutomaton : automataByProperty.keySet())
        	if (equivalentPropertiesMap.get(propExprWithAutomaton)!=null) {
        		Automaton autoOfPropExpr = automataByProperty.get(propExprWithAutomaton);
	        	for (OWLObjectPropertyExpression equivProp : equivalentPropertiesMap.get(propExprWithAutomaton)) {
	        		if (!equivProp.equals(propExprWithAutomaton) && !automataByProperty.containsKey(equivProp)) {
	        			Automaton automatonOfEquivalent=(Automaton)autoOfPropExpr.clone();
						individualAutomataForEquivRoles.put(equivProp, automatonOfEquivalent);
						simpleProperties.remove(equivProp);
				        complexObjectPropertyExpressions.add(equivProp);
	        		}
	        		OWLObjectPropertyExpression inverseEquivProp = equivProp.getInverseProperty().getSimplified();
	        		if (!inverseEquivProp.equals(propExprWithAutomaton) && !automataByProperty.containsKey(inverseEquivProp)) {
	        			Automaton automatonOfEquivalent=(Automaton)autoOfPropExpr.clone();
						individualAutomataForEquivRoles.put(inverseEquivProp, getMirroredCopy(automatonOfEquivalent));
						simpleProperties.remove(inverseEquivProp);
				        complexObjectPropertyExpressions.add(inverseEquivProp);
	        		}
	        	}
        	}
        automataByProperty.putAll(individualAutomataForEquivRoles);
    }
    private Set<OWLObjectPropertyExpression> findSymmetricProperties(Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions) {
    	Set<OWLObjectPropertyExpression> symmetricProperties = new HashSet<OWLObjectPropertyExpression>();
    	for (OWLObjectPropertyExpression[] inclusion : simpleObjectPropertyInclusions)
    		if (inclusion[1].getInverseProperty().getSimplified().equals(inclusion[0]) || inclusion[1].equals(inclusion[0].getInverseProperty().getSimplified())){
    			symmetricProperties.add(inclusion[0]);
    			symmetricProperties.add(inclusion[0].getInverseProperty().getSimplified());
    		}
		return symmetricProperties;
	}
	protected Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> buildInversePropertiesMap(Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions) {
        Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> inversePropertiesMap=new HashMap<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>>();
        for (OWLObjectPropertyExpression[] inclusion : simpleObjectPropertyInclusions)
            if (inclusion[1] instanceof OWLObjectInverseOf) {
                Set<OWLObjectPropertyExpression> inverseProperties=inversePropertiesMap.get(inclusion[0]);
                if (inverseProperties==null)
                    inverseProperties=new HashSet<OWLObjectPropertyExpression>();
                inverseProperties.add(inclusion[1].getInverseProperty().getSimplified());
                inversePropertiesMap.put(inclusion[0],inverseProperties);
                
                inverseProperties=inversePropertiesMap.get(inclusion[1].getInverseProperty().getSimplified());
                if (inverseProperties==null)
                    inverseProperties=new HashSet<OWLObjectPropertyExpression>();
                inverseProperties.add(inclusion[0]);
                inversePropertiesMap.put(inclusion[1].getInverseProperty().getSimplified(),inverseProperties);
                
            }
            else if (inclusion[0] instanceof OWLObjectInverseOf) {
                Set<OWLObjectPropertyExpression> inverseProperties=inversePropertiesMap.get(inclusion[1]);
                if (inverseProperties==null)
                    inverseProperties=new HashSet<OWLObjectPropertyExpression>();
                inverseProperties.add(inclusion[0].getInverseProperty().getSimplified());
                inversePropertiesMap.put(inclusion[1],inverseProperties);
                
                inverseProperties=inversePropertiesMap.get(inclusion[0].getInverseProperty().getSimplified());
                if (inverseProperties==null)
                    inverseProperties=new HashSet<OWLObjectPropertyExpression>();
                inverseProperties.add(inclusion[1]);
                inversePropertiesMap.put(inclusion[0].getInverseProperty().getSimplified(),inverseProperties);
            }
        return inversePropertiesMap;
    }
    protected Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> findEquivalentProperties(Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions) {
        Graph<OWLObjectPropertyExpression> propertyDependencyGraph=new Graph<OWLObjectPropertyExpression>();
        Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> equivalentObjectPropertiesMapping=new HashMap<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>>();
        for (OWLObjectPropertyExpression[] inclusion : simpleObjectPropertyInclusions)
            if (!inclusion[0].equals(inclusion[1]) && !inclusion[0].equals(inclusion[1].getInverseProperty().getSimplified()))
                propertyDependencyGraph.addEdge(inclusion[0],inclusion[1]);
        propertyDependencyGraph.transitivelyClose();
        for (OWLObjectPropertyExpression objExpr : propertyDependencyGraph.getElements()) {
            if (propertyDependencyGraph.getSuccessors(objExpr).contains(objExpr) || propertyDependencyGraph.getSuccessors(objExpr).contains(objExpr.getInverseProperty().getSimplified())) {
                Set<OWLObjectPropertyExpression> equivPropertiesSet=new HashSet<OWLObjectPropertyExpression>();
                for (OWLObjectPropertyExpression succ : propertyDependencyGraph.getSuccessors(objExpr)) {
                    if (!succ.equals(objExpr) && (propertyDependencyGraph.getSuccessors(succ).contains(objExpr) || propertyDependencyGraph.getSuccessors(succ).contains(objExpr.getInverseProperty().getSimplified())))
                        equivPropertiesSet.add(succ);
                }
                equivalentObjectPropertiesMapping.put(objExpr,equivPropertiesSet);
            }
        }
        return equivalentObjectPropertiesMapping;
    }
    protected Set<OWLObjectPropertyExpression> findSimpleProperties(Graph<OWLObjectPropertyExpression> complexPropertiesDependencyGraph,Map<OWLObjectPropertyExpression,Automaton> individualAutomata) {
        Set<OWLObjectPropertyExpression> simpleProperties=new HashSet<OWLObjectPropertyExpression>();

        Graph<OWLObjectPropertyExpression> complexPropertiesDependencyGraphWithInverses=complexPropertiesDependencyGraph.clone();

        for (OWLObjectPropertyExpression complexProperty1 : complexPropertiesDependencyGraph.getElements())
            for (OWLObjectPropertyExpression complexProperty2 : complexPropertiesDependencyGraph.getSuccessors(complexProperty1))
                complexPropertiesDependencyGraphWithInverses.addEdge(complexProperty1.getInverseProperty().getSimplified(),complexProperty2.getInverseProperty().getSimplified());

        Graph<OWLObjectPropertyExpression> invertedGraph=complexPropertiesDependencyGraphWithInverses.getInverse();
        invertedGraph.transitivelyClose();

        for (OWLObjectPropertyExpression properties : invertedGraph.getElements()) {
            boolean hasComplexSubproperty=false;
            for (OWLObjectPropertyExpression subDependingProperties : invertedGraph.getSuccessors(properties)) {
                if (individualAutomata.containsKey(subDependingProperties) || individualAutomata.containsKey(subDependingProperties.getInverseProperty().getSimplified())) {
                    hasComplexSubproperty=true;
                    break;
                }
            }
            if (!hasComplexSubproperty && !individualAutomata.containsKey(properties) && !individualAutomata.containsKey(properties.getInverseProperty().getSimplified()))
                simpleProperties.add(properties);
        }
        return simpleProperties;
    }
    protected void connectAllAutomata(Map<OWLObjectPropertyExpression,Automaton> completeAutomata,Graph<OWLObjectPropertyExpression> propertyDependencyGraph,Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> inversePropertiesMap,Map<OWLObjectPropertyExpression,Automaton> individualAutomata,Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions, Set<OWLObjectPropertyExpression> symmetricObjectProperties, Set<OWLObjectPropertyExpression> transitiveProperties) {
        Graph<OWLObjectPropertyExpression> transClosedGraph=propertyDependencyGraph.clone();
        transClosedGraph.transitivelyClose();

        Set<OWLObjectPropertyExpression> propertiesToStartRecursion=new HashSet<OWLObjectPropertyExpression>();
        for (OWLObjectPropertyExpression owlProp : transClosedGraph.getElements())
            if (transClosedGraph.getSuccessors(owlProp).isEmpty())
                propertiesToStartRecursion.add(owlProp);

        Graph<OWLObjectPropertyExpression> inversePropertyDependencyGraph=propertyDependencyGraph.getInverse();

        for (OWLObjectPropertyExpression superproperty : propertiesToStartRecursion)
            buildCompleteAutomataForProperties(superproperty,inversePropertiesMap,individualAutomata,completeAutomata,inversePropertyDependencyGraph,symmetricObjectProperties,transitiveProperties);

        for (OWLObjectPropertyExpression property : individualAutomata.keySet())
            if (!completeAutomata.containsKey(property)) {
                Automaton propertyAutomaton=individualAutomata.get(property);
                if ((completeAutomata.containsKey(property.getInverseProperty().getSimplified()) && inversePropertyDependencyGraph.getElements().contains(property.getInverseProperty().getSimplified())) || individualAutomata.containsKey(property.getInverseProperty().getSimplified())) {
                    Automaton inversePropertyAutomaton=completeAutomata.get(property.getInverseProperty().getSimplified());
                    if (inversePropertyAutomaton==null)
                        inversePropertyAutomaton=individualAutomata.get(property.getInverseProperty().getSimplified());
                    increaseAutomatonWithInversePropertyAutomaton(propertyAutomaton,inversePropertyAutomaton);
                }
                completeAutomata.put(property,propertyAutomaton);
            }

        Map<OWLObjectPropertyExpression,Automaton> extraCompleteAutomataForInverseProperties=new HashMap<OWLObjectPropertyExpression,Automaton>();
        for (OWLObjectPropertyExpression property : completeAutomata.keySet())
            if (!completeAutomata.containsKey(property.getInverseProperty().getSimplified()))
                extraCompleteAutomataForInverseProperties.put(property.getInverseProperty().getSimplified(),getMirroredCopy(completeAutomata.get(property)));

        completeAutomata.putAll(extraCompleteAutomataForInverseProperties);
        extraCompleteAutomataForInverseProperties.clear();

        for (OWLObjectPropertyExpression property : completeAutomata.keySet())
            if (completeAutomata.containsKey(property) && !completeAutomata.containsKey(property.getInverseProperty().getSimplified()))
                extraCompleteAutomataForInverseProperties.put(property.getInverseProperty().getSimplified(),getMirroredCopy(completeAutomata.get(property)));

        completeAutomata.putAll(extraCompleteAutomataForInverseProperties);
        extraCompleteAutomataForInverseProperties.clear();
        
        for (OWLObjectPropertyExpression propExprWithAutomaton : completeAutomata.keySet())
        	if (inversePropertiesMap.get(propExprWithAutomaton)!=null) {
        		Automaton autoOfPropExpr = completeAutomata.get(propExprWithAutomaton);
	        	for (OWLObjectPropertyExpression inverseProp : inversePropertiesMap.get(propExprWithAutomaton)) {
	        		Automaton automatonOfInverse=completeAutomata.get(inverseProp);
	        		if (automatonOfInverse!=null) {
	        			increaseAutomatonWithInversePropertyAutomaton(autoOfPropExpr,automatonOfInverse);
	        			extraCompleteAutomataForInverseProperties.put(propExprWithAutomaton,autoOfPropExpr);
	        		}
	        		else {
	        			automatonOfInverse=getMirroredCopy(autoOfPropExpr);
	        			extraCompleteAutomataForInverseProperties.put(inverseProp,automatonOfInverse);
	        		}
	        	}
	        }
        completeAutomata.putAll(extraCompleteAutomataForInverseProperties);
        
    }
    protected void increaseAutomatonWithInversePropertyAutomaton(Automaton propertyAutomaton,Automaton inversePropertyAutomaton) {
        State initialState=(State)propertyAutomaton.initials().iterator().next();
        State finalState=(State)propertyAutomaton.terminals().iterator().next();
        Transition transition=(Transition)propertyAutomaton.deltaFrom(initialState,finalState).iterator().next();
        automataConnector(propertyAutomaton,getMirroredCopy(inversePropertyAutomaton),transition);
    }
    protected Automaton buildCompleteAutomataForProperties(OWLObjectPropertyExpression propertyToBuildAutomatonFor,Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> inversePropertiesMap,Map<OWLObjectPropertyExpression,Automaton> individualAutomata,Map<OWLObjectPropertyExpression,Automaton> completeAutomata,Graph<OWLObjectPropertyExpression> inversedPropertyDependencyGraph, Set<OWLObjectPropertyExpression> symmetricObjectProperties, Set<OWLObjectPropertyExpression> transitiveProperties) {
        if (completeAutomata.containsKey(propertyToBuildAutomatonFor))
            return completeAutomata.get(propertyToBuildAutomatonFor);
        else if (completeAutomata.containsKey(propertyToBuildAutomatonFor.getInverseProperty().getSimplified()) && !individualAutomata.containsKey(propertyToBuildAutomatonFor)) {
            Automaton mirroredCopy=getMirroredCopy(completeAutomata.get(propertyToBuildAutomatonFor.getInverseProperty().getSimplified()));
            completeAutomata.put(propertyToBuildAutomatonFor,mirroredCopy);
            return mirroredCopy;
        }
        //if the role has no (inv) sub-role which is complex and we need to completely construct its automaton
        if (inversedPropertyDependencyGraph.getSuccessors(propertyToBuildAutomatonFor).isEmpty() && inversedPropertyDependencyGraph.getSuccessors(propertyToBuildAutomatonFor.getInverseProperty().getSimplified()).isEmpty()) {
            Automaton automatonForLeafProperty=individualAutomata.get(propertyToBuildAutomatonFor);
            //if the individual automaton for the role is empty
            if (automatonForLeafProperty==null) {
                Set<OWLObjectPropertyExpression> inverses=inversePropertiesMap.get(propertyToBuildAutomatonFor);
                boolean noInversePropertyWithAutomaton=true;
                //if it has declared inverse roles
                if (inverses!=null) {
                    for (OWLObjectPropertyExpression inverse : inverses)
                        if (individualAutomata.containsKey(inverse) && !inverse.equals(propertyToBuildAutomatonFor)) {
                            automatonForLeafProperty=getMirroredCopy(buildCompleteAutomataForProperties(inverse,inversePropertiesMap,individualAutomata,completeAutomata,inversedPropertyDependencyGraph, symmetricObjectProperties,transitiveProperties));
                            automatonForLeafProperty=minimizeAndNormalizeAutomaton(automatonForLeafProperty);
                            completeAutomata.put(propertyToBuildAutomatonFor,automatonForLeafProperty);
                            noInversePropertyWithAutomaton=false;
                            break;
                        }
                }
                //else if Inv(R) has an automaton
                else if (individualAutomata.containsKey(propertyToBuildAutomatonFor.getInverseProperty().getSimplified())) {
                    automatonForLeafProperty=getMirroredCopy(buildCompleteAutomataForProperties(propertyToBuildAutomatonFor.getInverseProperty().getSimplified(),inversePropertiesMap,individualAutomata,completeAutomata,inversedPropertyDependencyGraph,symmetricObjectProperties,transitiveProperties));
                    if (!completeAutomata.containsKey(propertyToBuildAutomatonFor)) {
                        automatonForLeafProperty=minimizeAndNormalizeAutomaton(automatonForLeafProperty);
                        completeAutomata.put(propertyToBuildAutomatonFor,automatonForLeafProperty);
                    }
                    else
                        automatonForLeafProperty=completeAutomata.get(propertyToBuildAutomatonFor);
                    noInversePropertyWithAutomaton=false;
                }
                //if no inverse (either declared or Inv(R)) has an automaton
                if (noInversePropertyWithAutomaton) {
                    automatonForLeafProperty=new Automaton();
                    State initial=automatonForLeafProperty.addState(true,false);
                    State accepting=automatonForLeafProperty.addState(false,true);
                    try {
                        automatonForLeafProperty.addTransition(new Transition(initial,propertyToBuildAutomatonFor,accepting));
                    }
                    catch (NoSuchStateException e) {
                        throw new IllegalArgumentException("Could not create automaton for property at the bottom of hierarchy (simple property).");
                    }
                	finalizeConstruction(completeAutomata,propertyToBuildAutomatonFor,automatonForLeafProperty,symmetricObjectProperties,transitiveProperties);
                }
            }
            else {
                if (propertyToBuildAutomatonFor.getInverseProperty().getSimplified().isAnonymous() && individualAutomata.containsKey(propertyToBuildAutomatonFor.getInverseProperty().getSimplified())) {
                    Automaton inversePropertyAutomaton=buildCompleteAutomataForProperties(propertyToBuildAutomatonFor.getInverseProperty().getSimplified(),inversePropertiesMap,individualAutomata,completeAutomata,inversedPropertyDependencyGraph,symmetricObjectProperties,transitiveProperties);
                    increaseAutomatonWithInversePropertyAutomaton(automatonForLeafProperty,getMirroredCopy(inversePropertyAutomaton));
                    if (!completeAutomata.containsKey(propertyToBuildAutomatonFor))
                    	finalizeConstruction(completeAutomata,propertyToBuildAutomatonFor,automatonForLeafProperty,symmetricObjectProperties,transitiveProperties);
                    else
                        automatonForLeafProperty=completeAutomata.get(propertyToBuildAutomatonFor);
                }
                else {
                    increaseWithDefinedInverseIfNecessary(propertyToBuildAutomatonFor,automatonForLeafProperty,inversePropertiesMap,individualAutomata);
                	finalizeConstruction(completeAutomata,propertyToBuildAutomatonFor,automatonForLeafProperty,symmetricObjectProperties,transitiveProperties);
                }
            }
            return automatonForLeafProperty;
        }
        else {
            Automaton biggerPropertyAutomaton=individualAutomata.get(propertyToBuildAutomatonFor);
            if (biggerPropertyAutomaton==null) {
                biggerPropertyAutomaton=new Automaton();
                State initialState=biggerPropertyAutomaton.addState(true,false);
                State finalState=biggerPropertyAutomaton.addState(false,true);
                Transition transition=new Transition(initialState,propertyToBuildAutomatonFor,finalState);
                try {
                    biggerPropertyAutomaton.addTransition(transition);
                }
                catch (NoSuchStateException e) {
                    throw new IllegalArgumentException("Could not create automaton");
                }
                for (OWLObjectPropertyExpression smallerProperty : inversedPropertyDependencyGraph.getSuccessors(propertyToBuildAutomatonFor)) {
                    Automaton smallerPropertyAutomaton=buildCompleteAutomataForProperties(smallerProperty,inversePropertiesMap,individualAutomata,completeAutomata,inversedPropertyDependencyGraph,symmetricObjectProperties,transitiveProperties);
                    automataConnector(biggerPropertyAutomaton,smallerPropertyAutomaton,transition);
                    try {
                        biggerPropertyAutomaton.addTransition(new Transition(initialState,smallerProperty,finalState));
                    }
                    catch (NoSuchStateException e) {
                        throw new IllegalArgumentException("Could not create automaton");
                    }
                }
                if (propertyToBuildAutomatonFor.getInverseProperty().getSimplified().isAnonymous() && individualAutomata.containsKey(propertyToBuildAutomatonFor.getInverseProperty().getSimplified())) {
                    Automaton inversePropertyAutomaton=buildCompleteAutomataForProperties(propertyToBuildAutomatonFor.getInverseProperty().getSimplified(),inversePropertiesMap,individualAutomata,completeAutomata,inversedPropertyDependencyGraph,symmetricObjectProperties,transitiveProperties);
                    increaseAutomatonWithInversePropertyAutomaton(biggerPropertyAutomaton,getMirroredCopy(inversePropertyAutomaton));
                    if (!completeAutomata.containsKey(propertyToBuildAutomatonFor))
                    	finalizeConstruction(completeAutomata,propertyToBuildAutomatonFor,biggerPropertyAutomaton,symmetricObjectProperties,transitiveProperties);
                    else
                        biggerPropertyAutomaton=completeAutomata.get(propertyToBuildAutomatonFor);
                }
                else {
                    increaseWithDefinedInverseIfNecessary(propertyToBuildAutomatonFor,biggerPropertyAutomaton,inversePropertiesMap,individualAutomata);
                    if (!completeAutomata.containsKey(propertyToBuildAutomatonFor))
                    	finalizeConstruction(completeAutomata,propertyToBuildAutomatonFor,biggerPropertyAutomaton,symmetricObjectProperties,transitiveProperties);
                    else
                        biggerPropertyAutomaton=completeAutomata.get(propertyToBuildAutomatonFor);
                }
            }
            else {
                for (OWLObjectPropertyExpression smallerProperty : inversedPropertyDependencyGraph.getSuccessors(propertyToBuildAutomatonFor)) {
                    boolean someInternalTransitionMatched=false;
                    for (Object transitionObject : biggerPropertyAutomaton.delta()) {
                        Transition transition=(Transition)transitionObject;
                        if (transition.label()!=null && transition.label().equals(smallerProperty)) {
                            Automaton smallerPropertyAutomaton=buildCompleteAutomataForProperties(smallerProperty,inversePropertiesMap,individualAutomata,completeAutomata,inversedPropertyDependencyGraph,symmetricObjectProperties,transitiveProperties);
                            if (smallerPropertyAutomaton.delta().size()!=1)
                                automataConnector(biggerPropertyAutomaton,smallerPropertyAutomaton,transition);
                            someInternalTransitionMatched=true;
                        }
                    }
                    if (!someInternalTransitionMatched) {
                        Automaton smallerPropertyAutomaton=buildCompleteAutomataForProperties(smallerProperty,inversePropertiesMap,individualAutomata,completeAutomata,inversedPropertyDependencyGraph,symmetricObjectProperties,transitiveProperties);
                        Transition initial2TerminalTransition=(Transition)biggerPropertyAutomaton.deltaFrom((State)biggerPropertyAutomaton.initials().iterator().next(),(State)biggerPropertyAutomaton.terminals().iterator().next()).iterator().next();
                        automataConnector(biggerPropertyAutomaton,smallerPropertyAutomaton,initial2TerminalTransition);
                    }
                }
            }
            if (propertyToBuildAutomatonFor.getInverseProperty().getSimplified().isAnonymous() && individualAutomata.containsKey(propertyToBuildAutomatonFor.getInverseProperty().getSimplified())) {
                Automaton inversePropertyAutomaton=buildCompleteAutomataForProperties(propertyToBuildAutomatonFor.getInverseProperty().getSimplified(),inversePropertiesMap,individualAutomata,completeAutomata,inversedPropertyDependencyGraph,symmetricObjectProperties,transitiveProperties);
                increaseAutomatonWithInversePropertyAutomaton(biggerPropertyAutomaton,getMirroredCopy(inversePropertyAutomaton));
                if (!completeAutomata.containsKey(propertyToBuildAutomatonFor))
                	finalizeConstruction(completeAutomata,propertyToBuildAutomatonFor,biggerPropertyAutomaton,symmetricObjectProperties,transitiveProperties);
                else
                    biggerPropertyAutomaton=completeAutomata.get(propertyToBuildAutomatonFor);
            }
            else {
                increaseWithDefinedInverseIfNecessary(propertyToBuildAutomatonFor,biggerPropertyAutomaton,inversePropertiesMap,individualAutomata);
                if (!completeAutomata.containsKey(propertyToBuildAutomatonFor))
                	finalizeConstruction(completeAutomata,propertyToBuildAutomatonFor,biggerPropertyAutomaton,symmetricObjectProperties,transitiveProperties);
                else
                    biggerPropertyAutomaton=completeAutomata.get(propertyToBuildAutomatonFor);
            }
            return biggerPropertyAutomaton;
        }
    }
    private void finalizeConstruction(Map<OWLObjectPropertyExpression,Automaton> completeAutomata,OWLObjectPropertyExpression propertyToBuildAutomatonFor,Automaton biggerPropertyAutomaton,Set<OWLObjectPropertyExpression> symmetricObjectProperties,Set<OWLObjectPropertyExpression> transitiveProperties) {
        try {
        	if (transitiveProperties.contains(propertyToBuildAutomatonFor.getInverseProperty().getSimplified()))
            	biggerPropertyAutomaton.addTransition(new Transition((State)biggerPropertyAutomaton.terminals().iterator().next(),null,(State)biggerPropertyAutomaton.initials().iterator().next()));
        }
        catch (NoSuchStateException e) {
            throw new IllegalArgumentException("Could not create automaton for symmetric property: "+propertyToBuildAutomatonFor);
        }

    	if (symmetricObjectProperties.contains (propertyToBuildAutomatonFor)) {
	        Transition basicTransition=new Transition((State)biggerPropertyAutomaton.initials().iterator().next(),propertyToBuildAutomatonFor.getInverseProperty().getSimplified(),(State)biggerPropertyAutomaton.terminals().iterator().next());
	        automataConnector(biggerPropertyAutomaton,getMirroredCopy(biggerPropertyAutomaton),basicTransition);
    	}
    	biggerPropertyAutomaton=minimizeAndNormalizeAutomaton(biggerPropertyAutomaton);
        completeAutomata.put(propertyToBuildAutomatonFor,biggerPropertyAutomaton);
        completeAutomata.put(propertyToBuildAutomatonFor.getInverseProperty().getSimplified(),getMirroredCopy(biggerPropertyAutomaton));
	}
	protected void increaseWithDefinedInverseIfNecessary(OWLObjectPropertyExpression propertyToBuildAutomatonFor,Automaton leafPropertyAutomaton,Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> inversePropertiesMap,Map<OWLObjectPropertyExpression,Automaton> individualAutomata) {
        Set<OWLObjectPropertyExpression> inverses=inversePropertiesMap.get(propertyToBuildAutomatonFor);
        if (inverses!=null) {
            Automaton inversePropertyAutomaton=null;
            for (OWLObjectPropertyExpression inverse : inverses) {
                if (individualAutomata.containsKey(inverse) && !inverse.equals(propertyToBuildAutomatonFor)) {
                    inversePropertyAutomaton=individualAutomata.get(inverse);
                    increaseAutomatonWithInversePropertyAutomaton(leafPropertyAutomaton,inversePropertyAutomaton);
                }
            }
        }
        else if (individualAutomata.containsKey(propertyToBuildAutomatonFor.getInverseProperty().getSimplified())) {
        	Automaton autoOfInv_Role = individualAutomata.get(propertyToBuildAutomatonFor.getInverseProperty().getSimplified());
        	increaseAutomatonWithInversePropertyAutomaton(leafPropertyAutomaton,autoOfInv_Role);
        }
    }
    protected Automaton minimizeAndNormalizeAutomaton(Automaton automaton) {
    	//This part of the code seemed to have a bug in an ontology given by Birte. The ontology created very large automata and was
    	//extremely difficult to see where the bug was exactly. Either the ToDFA class has a bug or due to state renaming that ToDFA does
    	//state names got mixed up later (a similar thing has happened before) however I could not detect something like that happening now.
    	//Without this code the automata are about double in size than with the code which can cause performance issues in ontologies with
    	//large and complex RIAs, which fortunately does not happen.
//        Reducer minimizerDeterminizer=new Reducer();
//        //if the automaton has more than 350-400 transitions it seems that the determiniser is very slow. In general this code does help to reduce the number of clauses produced.
//        if( automaton.delta().size() > 300 )
//        	return automaton;
//        Normalizer normalizer=new Normalizer();
//        Automaton tempMinimizedAuto=minimizerDeterminizer.transform(automaton);
//        if (tempMinimizedAuto.delta().size()>=automaton.delta().size())
//            return automaton;
//        if (tempMinimizedAuto.initials().size()!=1 || tempMinimizedAuto.terminals().size()!=1)
//            tempMinimizedAuto=normalizer.transform(tempMinimizedAuto);
//        if (tempMinimizedAuto.delta().size()>automaton.delta().size())
            return automaton;
//        return tempMinimizedAuto;
    }
    protected void useStandardAutomataConnector(Automaton biggerPropertyAutomaton,Automaton smallerPropertyAutomaton,Transition transition) {
        Map<State,State> stateMapper=getDisjointUnion(biggerPropertyAutomaton,smallerPropertyAutomaton);

        State initialState=transition.start();
	    State finalState=transition.end();

	    State oldStartOfSmaller=stateMapper.get(smallerPropertyAutomaton.initials().iterator().next());
	    State oldFinalOfSmaller=stateMapper.get(smallerPropertyAutomaton.terminals().iterator().next());

	    try {
	    	biggerPropertyAutomaton.addTransition(new Transition(initialState,null,oldStartOfSmaller));
	        biggerPropertyAutomaton.addTransition(new Transition(oldFinalOfSmaller,null,finalState));
	    }
	    catch (NoSuchStateException e) {
	    	throw new IllegalArgumentException("Could not build the Complete Automata of non-Simple Properties");
	    }
    }
    protected void automataConnector(Automaton biggerPropertyAutomaton,Automaton smallerPropertyAutomaton,Transition transition) {
    	useStandardAutomataConnector(biggerPropertyAutomaton,smallerPropertyAutomaton,transition);
    }
	protected Set<Transition> deltaToState(Automaton smallerPropertyAutomaton,State state) {
        Set<Transition> incommingTrans=new HashSet<Transition>();
        for (Object transitionObject : smallerPropertyAutomaton.delta()) {
            Transition transition=(Transition)transitionObject;
            if (transition.end().equals(state))
                incommingTrans.add(transition);
        }
        return incommingTrans;
    }
    protected Graph<OWLObjectPropertyExpression> buildPropertyOrdering(Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions,Collection<ComplexObjectPropertyInclusion> complexObjectPropertyInclusions,Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> equivalentPropertiesMap) {
        Graph<OWLObjectPropertyExpression> propertyDependencyGraph=new Graph<OWLObjectPropertyExpression>();
        for (OWLObjectPropertyExpression[] inclusion : simpleObjectPropertyInclusions)
            if (!inclusion[0].equals(inclusion[1]) && !inclusion[0].equals(inclusion[1].getInverseProperty().getSimplified()) && (equivalentPropertiesMap.get(inclusion[0])==null || !equivalentPropertiesMap.get(inclusion[0]).contains(inclusion[1])))
                propertyDependencyGraph.addEdge(inclusion[0],inclusion[1]);
        for (OWLAxioms.ComplexObjectPropertyInclusion inclusion : complexObjectPropertyInclusions) {
            OWLObjectPropertyExpression owlSuperProperty=inclusion.m_superObjectProperty;
            OWLObjectPropertyExpression owlSubPropertyInChain=null;
            OWLObjectPropertyExpression[] owlSubProperties=inclusion.m_subObjectProperties;
            if (owlSubProperties.length!=2 && owlSuperProperty.equals(owlSubProperties[0]) && owlSuperProperty.equals(owlSubProperties[owlSubProperties.length-1]))
                throw new IllegalArgumentException("The given property hierarchy is not regular.");

            for (int i=0;i<owlSubProperties.length;i++) {
                owlSubPropertyInChain=owlSubProperties[i];

                if (owlSubProperties.length!=2 && i>0 && i<owlSubProperties.length-1 && (owlSubPropertyInChain.equals(owlSuperProperty) || (equivalentPropertiesMap.containsKey(owlSuperProperty) && equivalentPropertiesMap.get(owlSuperProperty).contains(owlSubPropertyInChain))))
                    throw new IllegalArgumentException("The given property hierarchy is not regular.");
                else if (owlSubPropertyInChain.getInverseProperty().getSimplified().equals(owlSuperProperty))
                    throw new IllegalArgumentException("The given property hierarchy is not regular.");
                else if (!owlSubPropertyInChain.equals(owlSuperProperty))
                    propertyDependencyGraph.addEdge(owlSubPropertyInChain,owlSuperProperty);
            }
        }
        return propertyDependencyGraph;
    }
    protected void checkForRegularity(Graph<OWLObjectPropertyExpression> propertyDependencyGraph,Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> equivalentPropertiesMap) {
        Graph<OWLObjectPropertyExpression> regularityCheckGraph=propertyDependencyGraph.clone();
        Graph<OWLObjectPropertyExpression> regularityCheckGraphTemp;

        boolean trimmed=false;
        do {
            trimmed=false;
            regularityCheckGraphTemp=regularityCheckGraph.clone();
            for (OWLObjectPropertyExpression prop : regularityCheckGraphTemp.getElements()) {
                for (OWLObjectPropertyExpression succProp : regularityCheckGraphTemp.getSuccessors(prop)) {
                    if (equivalentPropertiesMap.containsKey(prop) && equivalentPropertiesMap.get(prop).contains(succProp)) {
                        for (OWLObjectPropertyExpression succPropSucc : regularityCheckGraphTemp.getSuccessors(succProp)) {
                            if (!prop.equals(succPropSucc))
                                regularityCheckGraph.addEdge(prop,succPropSucc);
                        }
                        trimmed=true;
                        regularityCheckGraph.getSuccessors(prop).remove(succProp);
                    }
                }
            }
        } while (trimmed);

        regularityCheckGraph.transitivelyClose();

        for (OWLObjectPropertyExpression prop : regularityCheckGraph.getElements()) {
            Set<OWLObjectPropertyExpression> successors=regularityCheckGraph.getSuccessors(prop);
            if (successors.contains(prop) || successors.contains(prop.getInverseProperty().getSimplified()))
                throw new IllegalArgumentException("The given property hierarchy is not regular.\nThere is a cyclic dependency involving property "+prop);
        }
    }
    protected Map<OWLObjectPropertyExpression,Automaton> buildIndividualAutomata(Graph<OWLObjectPropertyExpression> complexPropertiesDependencyGraph,Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions,Collection<ComplexObjectPropertyInclusion> complexObjectPropertyInclusions,Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> equivalentPropertiesMap,Set<OWLObjectPropertyExpression> transitiveProperties) {
        Map<OWLObjectPropertyExpression,Automaton> automataMap=new HashMap<OWLObjectPropertyExpression,Automaton>();
        for (OWLAxioms.ComplexObjectPropertyInclusion inclusion : complexObjectPropertyInclusions) {
            OWLObjectPropertyExpression[] subObjectProperties=inclusion.m_subObjectProperties;
            OWLObjectPropertyExpression superObjectProperty=inclusion.m_superObjectProperty;
            Automaton automaton=null;
            State initialState=null;
            State finalState=null;
            if (!automataMap.containsKey(superObjectProperty)) {
                automaton=new Automaton();
                initialState=automaton.addState(true,false);
                finalState=automaton.addState(false,true);
                try {
                    automaton.addTransition(new Transition(initialState,superObjectProperty,finalState));
                }
                catch (NoSuchStateException e) {
                    throw new IllegalArgumentException("Could not create automaton");
                }
            }
            else {
                automaton=automataMap.get(superObjectProperty);
                initialState=(State)automaton.initials().iterator().next();
                finalState=(State)automaton.terminals().iterator().next();
            }
            // RR->R
            if (subObjectProperties.length==2 && subObjectProperties[0].equals(superObjectProperty) && subObjectProperties[1].equals(superObjectProperty)) {
                try {
                    automaton.addTransition(new Transition(finalState,null,initialState));
                    transitiveProperties.add(superObjectProperty);
                }
                catch (NoSuchStateException e) {
                    throw new IllegalArgumentException("Could not create automaton");
                }
            }
            // R S2...Sn->R
            else if (subObjectProperties[0].equals(superObjectProperty)) {
                State fromState=finalState;
                OWLObjectPropertyExpression transitionLabel;
                for (int i=1;i<subObjectProperties.length-1;i++) {
                    transitionLabel=subObjectProperties[i];
                    if (equivalentPropertiesMap.containsKey(superObjectProperty) && equivalentPropertiesMap.get(superObjectProperty).contains(transitionLabel))
                        transitionLabel=superObjectProperty;
                    try {
                        fromState=addNewTransition(automaton,fromState,transitionLabel);
                    }
                    catch (NoSuchStateException e) {
                        throw new IllegalArgumentException("Could not create automaton");
                    }
                }
                try {
                    transitionLabel=subObjectProperties[subObjectProperties.length-1];
                    if (equivalentPropertiesMap.containsKey(superObjectProperty) && equivalentPropertiesMap.get(superObjectProperty).contains(transitionLabel))
                        transitionLabel=superObjectProperty;
                    automaton.addTransition(new Transition(fromState,transitionLabel,finalState));
                }
                catch (NoSuchStateException e) {
                    throw new IllegalArgumentException("Could not create automaton");
                }
            }
            // S1...Sn-1 R->R
            else if (subObjectProperties[subObjectProperties.length-1].equals(superObjectProperty)) {
                State fromState=initialState;
                OWLObjectPropertyExpression transitionLabel;
                for (int i=0;i<subObjectProperties.length-2;i++) {
                    transitionLabel=subObjectProperties[i];
                    if (equivalentPropertiesMap.containsKey(superObjectProperty) && equivalentPropertiesMap.get(superObjectProperty).contains(transitionLabel))
                        transitionLabel=superObjectProperty;
                    try {
                        fromState=addNewTransition(automaton,fromState,transitionLabel);
                    }
                    catch (NoSuchStateException e) {
                        throw new IllegalArgumentException("Could not create automaton");
                    }
                }
                try {
                    transitionLabel=subObjectProperties[subObjectProperties.length-2];
                    if (equivalentPropertiesMap.containsKey(superObjectProperty) && equivalentPropertiesMap.get(superObjectProperty).contains(transitionLabel))
                        transitionLabel=superObjectProperty;
                    automaton.addTransition(new Transition(fromState,transitionLabel,initialState));
                }
                catch (NoSuchStateException e) {
                    throw new IllegalArgumentException("Could not create automaton");
                }
            }
            // S1...Sn->R
            else {
                State fromState=initialState;
                OWLObjectPropertyExpression transitionLabel;
                for (int i=0;i<subObjectProperties.length-1;i++) {
                    transitionLabel=subObjectProperties[i];
                    if (equivalentPropertiesMap.containsKey(superObjectProperty) && equivalentPropertiesMap.get(superObjectProperty).contains(transitionLabel))
                        transitionLabel=superObjectProperty;
                    try {
                        fromState=addNewTransition(automaton,fromState,transitionLabel);
                    }
                    catch (NoSuchStateException e) {
                        throw new IllegalArgumentException("Could not create automaton");
                    }
                }
                try {
                    transitionLabel=subObjectProperties[subObjectProperties.length-1];
                    if (equivalentPropertiesMap.containsKey(superObjectProperty) && equivalentPropertiesMap.get(superObjectProperty).contains(transitionLabel))
                        transitionLabel=superObjectProperty;
                    automaton.addTransition(new Transition(fromState,transitionLabel,finalState));
                }
                catch (NoSuchStateException e) {
                    throw new IllegalArgumentException("Could not create automaton");
                }
            }
            automataMap.put(superObjectProperty,automaton);
        }
        // For those transitive properties that other properties do not depend on other properties the automaton is complete.
        // So we also need to build the auto for the inverse of R unless Inv(R) has its own.
        for (ComplexObjectPropertyInclusion inclusion : complexObjectPropertyInclusions) {
            OWLObjectPropertyExpression superpropertyExpression=inclusion.m_superObjectProperty;
            OWLObjectPropertyExpression[] subpropertyExpression=inclusion.m_subObjectProperties;
            if (subpropertyExpression.length==2 && subpropertyExpression[0].equals(superpropertyExpression) && subpropertyExpression[1].equals(superpropertyExpression))
                if (!complexPropertiesDependencyGraph.getElements().contains(superpropertyExpression) && !automataMap.containsKey(superpropertyExpression.getInverseProperty().getSimplified())) {
                    complexPropertiesDependencyGraph.addEdge(superpropertyExpression,superpropertyExpression);
                    Automaton propertyAutomaton=automataMap.get(superpropertyExpression);
                    automataMap.put(superpropertyExpression.getInverseProperty().getSimplified(),getMirroredCopy(propertyAutomaton));
                }
        }
        // we always want to construct an automaton for the top object property since it might occur in queries
        // the axiomatisation at query time fails if we don't have the automaton        
        OWLDataFactory df=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        OWLObjectProperty topOP=df.getOWLTopObjectProperty();
        if (!automataMap.keySet().contains(topOP)) {
            try {
                Automaton automaton=new Automaton();
                State initialState=automaton.addState(true,false);
                State finalState=automaton.addState(false,true);
                automaton.addTransition(new Transition(initialState,topOP,finalState));
                automaton.addTransition(new Transition(finalState,null,initialState)); // transitivity
                automataMap.put(topOP, automaton);
            }
            catch (NoSuchStateException e) {
                throw new IllegalArgumentException("Could not create automaton");
            }
        }        
        return automataMap;
    }
    protected Map<State,State> getDisjointUnion(Automaton automaton1,Automaton automaton2) {
        Map<State,State> stateMapperUnionInverse=new HashMap<State,State>();
        for (Object stateObject : automaton2.states())
            stateMapperUnionInverse.put((State)stateObject,automaton1.addState(false,false));

        for (Object transitionObject : automaton2.delta()) {
            Transition transition=(Transition)transitionObject;
            try {
                automaton1.addTransition(new Transition(stateMapperUnionInverse.get(transition.start()),transition.label(),stateMapperUnionInverse.get(transition.end())));
            }
            catch (NoSuchStateException x) {
                throw new IllegalArgumentException("Could not create disjoint union of automata");
            }
        }
        return stateMapperUnionInverse;
    }
    protected Automaton getMirroredCopy(Automaton automaton) {
        Automaton mirroredCopy=new Automaton();
        Map<State,State> map=new HashMap<State,State>();
        for (Object stateObject : automaton.states()) {
            State state=(State)stateObject;
            map.put(state,mirroredCopy.addState(state.isTerminal(),state.isInitial()));
        }
        for (Object transitionObject : automaton.delta()) {
            Transition transition=(Transition)transitionObject;
            try {
                if (transition.label() instanceof OWLObjectPropertyExpression)
                    mirroredCopy.addTransition(new Transition(map.get(transition.end()),((OWLObjectPropertyExpression)transition.label()).getInverseProperty().getSimplified(),map.get(transition.start())));
                else
                    mirroredCopy.addTransition(new Transition(map.get(transition.end()),transition.label(),map.get(transition.start())));
            }
            catch (NoSuchStateException x) {
            }
        }
        return mirroredCopy;
    }
    protected State addNewTransition(Automaton automaton,State fromState,OWLObjectPropertyExpression objectPropertyExpression) throws NoSuchStateException {
        OWLObjectPropertyExpression propertyOfChain=objectPropertyExpression;
        State toState=automaton.addState(false,false);
        automaton.addTransition(new Transition(fromState,propertyOfChain,toState));
        return toState;
    }
}
