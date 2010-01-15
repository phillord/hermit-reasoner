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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.graph.Graph;
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
    protected final Graph<OWLObjectPropertyExpression> m_subObjectProperties;
    protected final Set<OWLObjectPropertyExpression> m_transitiveObjectProperties;
    protected final Map<OWLObjectAllValuesFrom,OWLClassExpression> m_replacedDescriptions;
    
    protected final Map<OWLObjectPropertyExpression,Automaton> m_automataForComplexRoles;
    protected final Set<OWLObjectPropertyExpression> m_nonSimpleRoles;                                                                                                                                          

    public ObjectPropertyInclusionManager(OWLDataFactory factory) {
        m_factory=factory;
        m_subObjectProperties=new Graph<OWLObjectPropertyExpression>();
        m_transitiveObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_replacedDescriptions=new HashMap<OWLObjectAllValuesFrom,OWLClassExpression>();
 
        m_automataForComplexRoles = new HashMap<OWLObjectPropertyExpression,Automaton>();
        m_nonSimpleRoles = new HashSet<OWLObjectPropertyExpression>();

    }
    
    public void prepareTransformation(OWLAxioms axioms) {
        for (OWLObjectPropertyExpression[] inclusion : axioms.m_simpleObjectPropertyInclusions)
            addInclusion(inclusion[0],inclusion[1]);
 
        AutomataConstructionManager automataBuilder = new AutomataConstructionManager();
        m_automataForComplexRoles.putAll( automataBuilder.createAutomata( axioms.m_simpleObjectPropertyInclusions, axioms.m_complexObjectPropertyInclusions ) );
        m_nonSimpleRoles.addAll( automataBuilder.getM_nonSimpleRoles() );
        

        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_asymmetricObjectProperties)
                if( m_nonSimpleRoles.contains( objectPropertyExpression ) )
                throw new IllegalArgumentException( "Non simple role '" + objectPropertyExpression + "' or its inverse appears in asymmetricity axiom");

        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_irreflexiveObjectProperties)
                if( m_nonSimpleRoles.contains( objectPropertyExpression ) )
                throw new IllegalArgumentException( "Non simple role '" + objectPropertyExpression + "' or its inverse appears in asymmetricity axiom");

        for (OWLObjectPropertyExpression[] properties : axioms.m_disjointObjectProperties)
            for (int i=0;i<properties.length;i++)
                if( m_nonSimpleRoles.contains( properties[i] ) )
                    throw new IllegalArgumentException( "Non simple role '" + properties[i] + "' or its inverse appears in disjoint properties axiom");
    }
        public void addInclusion(OWLObjectPropertyExpression subObjectProperty,OWLObjectPropertyExpression superObjectProperty) {
        subObjectProperty=subObjectProperty.getSimplified();
        superObjectProperty=superObjectProperty.getSimplified();
        m_subObjectProperties.addEdge(superObjectProperty,subObjectProperty);
        m_subObjectProperties.addEdge(superObjectProperty.getInverseProperty().getSimplified(),subObjectProperty.getInverseProperty().getSimplified());
    }
    public Map<OWLObjectPropertyExpression,Automaton> rewriteAxioms(OWLAxioms axioms) {
        for (OWLClassExpression[] inclusion : axioms.m_conceptInclusions)
            for (int index=0;index<inclusion.length;index++){
            	if(inclusion[index] instanceof OWLObjectCardinalityRestriction)
                    if( m_nonSimpleRoles.contains( ((OWLObjectCardinalityRestriction)inclusion[index]).getProperty() ) )
                        throw new IllegalArgumentException( "Non simple role '" + (OWLObjectCardinalityRestriction)inclusion[index] + "' or its inverse appears in asymmetricity axiom");

                    inclusion[index]=replaceDescriptionIfNecessary(inclusion[index]);
            }

        for (Map.Entry<OWLObjectAllValuesFrom,OWLClassExpression> mapping : m_replacedDescriptions.entrySet()) {
        	
            OWLObjectAllValuesFrom replacedAllRestriction=mapping.getKey();
            OWLObjectPropertyExpression objectProperty = replacedAllRestriction.getProperty();
            OWLClassExpression owlConcept = replacedAllRestriction.getFiller();
            String indexOfInitialConcept = mapping.getValue().asOWLClass().getIRI().getFragment() + "_";

            Automaton automatonOfRole = m_automataForComplexRoles.get( objectProperty );
            String initialState = automatonOfRole.initials().toArray()[0].toString();
            boolean isOfNegativePolarity = false;
            OWLClassExpression conceptForInitialState = m_factory.getOWLClass(IRI.create("internal:all#"+indexOfInitialConcept+initialState));            
            if (replacedAllRestriction.getFiller() instanceof OWLObjectComplementOf || replacedAllRestriction.getFiller().equals(m_factory.getOWLNothing())){
                conceptForInitialState = m_factory.getOWLClass(IRI.create("internal:all#"+indexOfInitialConcept+initialState)).getComplementNNF();
                isOfNegativePolarity = true;
            }
            Map<State,OWLClassExpression> mapOfNewConceptNames = new HashMap<State,OWLClassExpression>();
            Object[] states = automatonOfRole.states().toArray();
            for( int i=0 ; i<states.length ; i++ ){
                State state = (State)states[i];
                if( state.isInitial() )
                	continue;
                else
                	mapOfNewConceptNames.put( state, m_factory.getOWLClass(IRI.create("internal:all#"+indexOfInitialConcept+state)) );
            }
            if( isOfNegativePolarity )
                for(State state : mapOfNewConceptNames.keySet())
                	mapOfNewConceptNames.put( state, mapOfNewConceptNames.get( state ).getComplementNNF() );

            mapOfNewConceptNames.put( (State)automatonOfRole.initials().toArray()[0], conceptForInitialState);

            Object[] transitionsIterator = automatonOfRole.delta().toArray();
            OWLClassExpression fromStateConcept = null;
            OWLClassExpression toStateConcept = null;

            for( int i =0 ; i<transitionsIterator.length ; i++ ) {
                Transition trans = (Transition) transitionsIterator[i];
                fromStateConcept = mapOfNewConceptNames.get( trans.start() ).getComplementNNF();
                toStateConcept = mapOfNewConceptNames.get( trans.end() );

                if( trans.label() == null )
                        axioms.m_conceptInclusions.add(new OWLClassExpression[] { fromStateConcept, toStateConcept });
                else{
                        OWLObjectAllValuesFrom consequentAll=m_factory.getOWLObjectAllValuesFrom(
                        					(OWLObjectPropertyExpression)trans.label(), toStateConcept );
                        axioms.m_conceptInclusions.add(new OWLClassExpression[] { fromStateConcept, consequentAll });
                }
            }
            Object[] finalStates = automatonOfRole.terminals().toArray();
            for( int i=0 ; i<finalStates.length ; i++ ){
            	OWLClassExpression[] classExpr; 
            	if( owlConcept.isOWLNothing() )
            		classExpr = new OWLClassExpression[] { mapOfNewConceptNames.get( (State)finalStates[i] ).getComplementNNF() };
            	else if( owlConcept.isOWLThing() )
            		classExpr = new OWLClassExpression[] { owlConcept };
            	else
            		classExpr = new OWLClassExpression[] { mapOfNewConceptNames.get( (State)finalStates[i] ).getComplementNNF(), owlConcept };
                
            	axioms.m_conceptInclusions.add(classExpr);
            }
        }
        m_replacedDescriptions.clear();

        return m_automataForComplexRoles;
    }
    protected OWLClassExpression replaceDescriptionIfNecessary(OWLClassExpression desc) {
        if (desc instanceof OWLObjectAllValuesFrom) {
            OWLObjectAllValuesFrom objectAll=(OWLObjectAllValuesFrom)desc;
            OWLObjectPropertyExpression objectProperty=objectAll.getProperty();
            if( m_automataForComplexRoles.containsKey( objectProperty ) ){
                OWLClassExpression replacedConcept=getReplacementFor(objectAll);
                String initialState = m_automataForComplexRoles.get( objectProperty ).initials().toArray()[0].toString();
                String indexOfReplacedConcept = replacedConcept.asOWLClass().getIRI().getFragment() + "_";
                OWLClassExpression replacement = m_factory.getOWLClass(IRI.create("internal:all#"+indexOfReplacedConcept+initialState));
                if (objectAll.getFiller() instanceof OWLObjectComplementOf || objectAll.getFiller().equals(m_factory.getOWLNothing()))
                	replacement = m_factory.getOWLClass(IRI.create("internal:all#"+indexOfReplacedConcept+initialState)).getComplementNNF();

                return replacement;
            }
        }
        return desc;
    }
    protected OWLClassExpression getReplacementFor(OWLObjectAllValuesFrom objectAll) {
        OWLClassExpression replacement=m_replacedDescriptions.get(objectAll);
        if (replacement==null) {
            replacement=m_factory.getOWLClass(IRI.create("internal:all#"+m_replacedDescriptions.size()));
            m_replacedDescriptions.put(objectAll,replacement);
        }
        return replacement;
    }
    public Map<OWLObjectPropertyExpression,Automaton> rewriteAxioms(OWLAxioms axioms, Map<OWLObjectPropertyExpression, Automaton> automataOfComplexObjectProperties) {
            m_automataForComplexRoles.putAll( automataOfComplexObjectProperties );
            return rewriteAxioms( axioms );
    }
}