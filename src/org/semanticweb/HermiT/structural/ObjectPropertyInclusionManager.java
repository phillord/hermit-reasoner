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

import org.semanticweb.HermiT.structural.OWLAxioms.ComplexObjectPropertyInclusion;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

public class ObjectPropertyInclusionManager {
    protected final OWLDataFactory m_factory;

    public ObjectPropertyInclusionManager(OWLDataFactory factory) {
        m_factory=factory;
    }
    public void rewriteAxioms(OWLAxioms axioms,Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions,Collection<ComplexObjectPropertyInclusion> complexObjectPropertyInclusions) {
        Map<OWLObjectPropertyExpression,Automaton> automataForComplexRoles=new HashMap<OWLObjectPropertyExpression,Automaton>();
        Set<OWLObjectPropertyExpression> nonSimpleRoles=new HashSet<OWLObjectPropertyExpression>();
        AutomataConstructionManager automataBuilder=new AutomataConstructionManager();
        automataBuilder.createAutomata(automataForComplexRoles,nonSimpleRoles,simpleObjectPropertyInclusions,complexObjectPropertyInclusions);

        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_asymmetricObjectProperties)
            if (nonSimpleRoles.contains(objectPropertyExpression))
                throw new IllegalArgumentException("Non-simple property '"+objectPropertyExpression+"' or its inverse appears in asymmetric object property axiom.");

        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_irreflexiveObjectProperties)
            if (nonSimpleRoles.contains(objectPropertyExpression))
                throw new IllegalArgumentException("Non-simple property '"+objectPropertyExpression+"' or its inverse appears in irreflexive object property axiom.");

        for (OWLObjectPropertyExpression[] properties : axioms.m_disjointObjectProperties)
            for (int i=0;i<properties.length;i++)
                if (nonSimpleRoles.contains(properties[i]))
                    throw new IllegalArgumentException("Non-simple property '"+properties[i]+"' or its inverse appears in disjoint properties axiom.");

        Map<OWLObjectAllValuesFrom,OWLClassExpression> replacedDescriptions=new HashMap<OWLObjectAllValuesFrom,OWLClassExpression>();
        for (OWLClassExpression[] inclusion : axioms.m_conceptInclusions)
            for (int index=0;index<inclusion.length;index++) {
                OWLClassExpression classExpression=inclusion[index];
                if (classExpression instanceof OWLObjectCardinalityRestriction) {
                    OWLObjectCardinalityRestriction objectCardinalityRestriction=(OWLObjectCardinalityRestriction)inclusion[index];
                    OWLObjectPropertyExpression objectPropertyExpression=objectCardinalityRestriction.getProperty();
                    if (nonSimpleRoles.contains(objectPropertyExpression))
                        throw new IllegalArgumentException("Non-simple property '"+objectPropertyExpression+"' or its inverse appears in a number restriction '"+objectCardinalityRestriction+"'.");
                }
                if (classExpression instanceof OWLObjectAllValuesFrom) {
                    OWLObjectAllValuesFrom objectAll=(OWLObjectAllValuesFrom)classExpression;
                    OWLObjectPropertyExpression objectProperty=objectAll.getProperty();
                    if (automataForComplexRoles.containsKey(objectProperty)) {
                        OWLClassExpression replacedConcept=getReplacementFor(replacedDescriptions,objectAll);
                        String initialState=automataForComplexRoles.get(objectProperty).initials().toArray()[0].toString();
                        String indexOfReplacedConcept=replacedConcept.asOWLClass().getIRI().getFragment()+"_";
                        OWLClassExpression replacement=m_factory.getOWLClass(IRI.create("internal:all#"+indexOfReplacedConcept+objectProperty.getNamedProperty().getIRI().getFragment()+initialState));
                        if (objectAll.getFiller() instanceof OWLObjectComplementOf || objectAll.getFiller().equals(m_factory.getOWLNothing()))
                            replacement=m_factory.getOWLClass(IRI.create("internal:all#"+indexOfReplacedConcept+objectProperty.getNamedProperty().getIRI().getFragment()+initialState)).getComplementNNF();
                        inclusion[index]=replacement;
                    }
                }
            }

        for (Map.Entry<OWLObjectAllValuesFrom,OWLClassExpression> mapping : replacedDescriptions.entrySet()) {
            OWLObjectAllValuesFrom replacedAllRestriction=mapping.getKey();
            OWLObjectPropertyExpression objectProperty=replacedAllRestriction.getProperty();
            String objectPropertyName=objectProperty.getNamedProperty().getIRI().getFragment();
            OWLClassExpression owlConcept=replacedAllRestriction.getFiller();
            String indexOfInitialConcept=mapping.getValue().asOWLClass().getIRI().getFragment()+"_";

            Automaton automatonOfRole=automataForComplexRoles.get(objectProperty);
            String initialState=automatonOfRole.initials().toArray()[0].toString();
            boolean isOfNegativePolarity=false;
            OWLClassExpression conceptForInitialState=m_factory.getOWLClass(IRI.create("internal:all#"+indexOfInitialConcept+objectPropertyName+initialState));
            if (replacedAllRestriction.getFiller() instanceof OWLObjectComplementOf || replacedAllRestriction.getFiller().equals(m_factory.getOWLNothing())) {
                conceptForInitialState=m_factory.getOWLClass(IRI.create("internal:all#"+indexOfInitialConcept+objectPropertyName+initialState)).getComplementNNF();
                isOfNegativePolarity=true;
            }
            Map<State,OWLClassExpression> mapOfNewConceptNames=new HashMap<State,OWLClassExpression>();
            Object[] states=automatonOfRole.states().toArray();
            for (int i=0;i<states.length;i++) {
                State state=(State)states[i];
                if (state.isInitial())
                    continue;
                else
                    mapOfNewConceptNames.put(state,m_factory.getOWLClass(IRI.create("internal:all#"+indexOfInitialConcept+objectPropertyName+state)));
            }
            if (isOfNegativePolarity)
                for (State state : mapOfNewConceptNames.keySet())
                    mapOfNewConceptNames.put(state,mapOfNewConceptNames.get(state).getComplementNNF());

            mapOfNewConceptNames.put((State)automatonOfRole.initials().toArray()[0],conceptForInitialState);

            Object[] transitionsIterator=automatonOfRole.delta().toArray();
            OWLClassExpression fromStateConcept=null;
            OWLClassExpression toStateConcept=null;

            for (int i=0;i<transitionsIterator.length;i++) {
                Transition transition=(Transition)transitionsIterator[i];
                fromStateConcept=mapOfNewConceptNames.get(transition.start()).getComplementNNF();
                toStateConcept=mapOfNewConceptNames.get(transition.end());

                if (transition.label()==null)
                    axioms.m_conceptInclusions.add(new OWLClassExpression[] { fromStateConcept,toStateConcept });
                else {
                    OWLObjectAllValuesFrom consequentAll=m_factory.getOWLObjectAllValuesFrom((OWLObjectPropertyExpression)transition.label(),toStateConcept);
                    axioms.m_conceptInclusions.add(new OWLClassExpression[] { fromStateConcept,consequentAll });
                }
            }
            Object[] finalStates=automatonOfRole.terminals().toArray();
            for (int i=0;i<finalStates.length;i++) {
                OWLClassExpression[] classExpressions;
                if (owlConcept.isOWLNothing())
                    classExpressions=new OWLClassExpression[] { mapOfNewConceptNames.get(finalStates[i]).getComplementNNF() };
                else if (owlConcept.isOWLThing())
                    classExpressions=new OWLClassExpression[] { owlConcept };
                else
                    classExpressions=new OWLClassExpression[] { mapOfNewConceptNames.get(finalStates[i]).getComplementNNF(),owlConcept };

                axioms.m_conceptInclusions.add(classExpressions);
            }
        }
    }
    protected OWLClassExpression getReplacementFor(Map<OWLObjectAllValuesFrom,OWLClassExpression> replacedDescriptions,OWLObjectAllValuesFrom objectAll) {
        OWLClassExpression replacement=replacedDescriptions.get(objectAll);
        if (replacement==null) {
            replacement=m_factory.getOWLClass(IRI.create("internal:all#"+replacedDescriptions.size()));
            replacedDescriptions.put(objectAll,replacement);
        }
        return replacement;
    }
}
