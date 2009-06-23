// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.owl.model.OWLClassExpression;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLObjectAllValuesFrom;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

public class ObjectPropertyInclusionManager {
    protected final OWLDataFactory m_factory;
    protected final Graph<OWLObjectPropertyExpression> m_subObjectProperties;
    protected final Set<OWLObjectPropertyExpression> m_transitiveObjectProperties;
    protected final Map<OWLObjectAllValuesFrom,OWLClassExpression> m_replacedDescriptions;
    /**
     * gstoil additions
     */
    protected final Map<OWLObjectPropertyExpression,Automaton> m_automataForComplexRoles;
    protected final Set<OWLObjectPropertyExpression> m_nonSimpleRoles;                                                                                                                                          

    public ObjectPropertyInclusionManager(OWLDataFactory factory) {
        m_factory=factory;
        m_subObjectProperties=new Graph<OWLObjectPropertyExpression>();
        m_transitiveObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_replacedDescriptions=new HashMap<OWLObjectAllValuesFrom,OWLClassExpression>();

        /**
         * gstoil additions
         */
        m_automataForComplexRoles = new HashMap<OWLObjectPropertyExpression,Automaton>();
        m_nonSimpleRoles = new HashSet<OWLObjectPropertyExpression>();

    }
    
    public void prepareTransformation(OWLAxioms axioms) {
        for (OWLObjectPropertyExpression[] inclusion : axioms.m_simpleObjectPropertyInclusions)
            addInclusion(inclusion[0],inclusion[1]);
        /*
         * Previous HermiT code
        */ 
//        for (OWLAxioms.ComplexObjectPropertyInclusion inclusion : axioms.m_complexObjectPropertyInclusions)
//              addInclusion(inclusion.m_subObjectProperties,inclusion.m_superObjectProperties);
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
    public void addInclusion(OWLObjectPropertyExpression[] subObjectProperties,OWLObjectPropertyExpression superObjectProperty) {
        if (subObjectProperties.length==1)
            addInclusion(subObjectProperties[0],superObjectProperty);
        else if (subObjectProperties.length==2 && subObjectProperties[0].equals(superObjectProperty) && subObjectProperties[1].equals(superObjectProperty)) {
            m_transitiveObjectProperties.add(superObjectProperty.getSimplified());
            m_transitiveObjectProperties.add(superObjectProperty.getInverseProperty().getSimplified());
        }
        else
            throw new IllegalArgumentException("Object property chains not supported yet.");
    }
    public Map<OWLObjectPropertyExpression,Automaton> rewriteAxioms(OWLAxioms axioms) {
        /**
         * gstoil additions modifications
         */
//        m_subObjectProperties.transitivelyClose();
        for (OWLClassExpression[] inclusion : axioms.m_conceptInclusions)
            for (int index=0;index<inclusion.length;index++){
            	if(inclusion[index] instanceof OWLObjectCardinalityRestriction)
                    if( m_nonSimpleRoles.contains( ((OWLObjectCardinalityRestriction)inclusion[index]).getProperty() ) )
                        throw new IllegalArgumentException( "Non simple role '" + (OWLObjectCardinalityRestriction)inclusion[index] + "' or its inverse appears in asymmetricity axiom");
            	inclusion[index]=replaceDescriptionIfNecessary(inclusion[index]);
            }

        for (Map.Entry<OWLObjectAllValuesFrom,OWLClassExpression> mapping : m_replacedDescriptions.entrySet()) {
        	
            OWLObjectAllValuesFrom replacedAllRestriction=mapping.getKey();
            String indexOfInitialConcept = mapping.getValue().asOWLClass().getURI().getFragment(); 
            OWLObjectPropertyExpression objectProperty = replacedAllRestriction.getProperty();
            OWLClassExpression owlConcept = replacedAllRestriction.getFiller();
            Automaton automatonOfRole = m_automataForComplexRoles.get( objectProperty );
            String initialState = automatonOfRole.initials().toArray()[0].toString();
            boolean isOfNegativePolarity = false;
            OWLClassExpression conceptForInitialState = m_factory.getOWLClass(URI.create("internal:all#"+indexOfInitialConcept+initialState));            
            if (replacedAllRestriction.getFiller() instanceof OWLObjectComplementOf || replacedAllRestriction.getFiller().equals(m_factory.getOWLNothing())){
                conceptForInitialState = m_factory.getOWLClass(URI.create("internal:all#"+indexOfInitialConcept+initialState)).getComplementNNF();
                isOfNegativePolarity = true;
            }
            Map<State,OWLClassExpression> mapOfNewConceptNames = new HashMap<State,OWLClassExpression>();
            Object[] states = automatonOfRole.states().toArray();
            for( int i=0 ; i<states.length ; i++ ){
                State state = (State)states[i];
                if( state.isInitial() )
                	continue;
                else
                	mapOfNewConceptNames.put( state, m_factory.getOWLClass(URI.create("internal:all#"+indexOfInitialConcept+state)) );
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
                for( int i=0 ; i<finalStates.length ; i++ )
                        axioms.m_conceptInclusions.add(new OWLClassExpression[] { mapOfNewConceptNames.get( (State)finalStates[i] ).getComplementNNF(), owlConcept });
        }
        m_replacedDescriptions.clear();
        
        return m_automataForComplexRoles;
//        m_automataForComplexRoles.clear();
//        m_nonSimpleRoles.clear();
        /**
         * @previous code simulating an automaton for transitive roles and sub-roles
         */
//      for (Map.Entry<OWLObjectAllValuesFrom,OWLClassExpression> mapping : m_replacedDescriptions.entrySet()) {
//      OWLObjectAllValuesFrom replacedConcept=mapping.getKey();
//      OWLClassExpression replacement=mapping.getValue();
//              axioms.m_conceptInclusions.add(new OWLClassExpression[] { replacement.getComplementNNF(),replacedConcept });
//            for (OWLObjectPropertyExpression transitiveSubObjectProperty : getTransitiveSubObjectProperties(replacedConcept.getProperty())) {
//                OWLObjectAllValuesFrom consequentAll=m_factory.getOWLObjectAllValuesFrom(transitiveSubObjectProperty,replacedConcept.getFiller());
//                OWLClassExpression consequentReplacement=m_replacedDescriptions.get(consequentAll);
//                OWLObjectAllValuesFrom allConsequentReplacement=m_factory.getOWLObjectAllValuesFrom(transitiveSubObjectProperty,consequentReplacement);
//                axioms.m_conceptInclusions.add(new OWLClassExpression[] { replacement.getComplementNNF(),allConsequentReplacement });
//            }
//        }
//        m_replacedDescriptions.clear();
    }
    protected OWLClassExpression replaceDescriptionIfNecessary(OWLClassExpression desc) {
        /**
         * gstoil modifications/additions
         */
        if (desc instanceof OWLObjectAllValuesFrom) {
            OWLObjectAllValuesFrom objectAll=(OWLObjectAllValuesFrom)desc;
            OWLObjectPropertyExpression objectProperty=objectAll.getProperty();
//            Set<OWLObjectPropertyExpression> transitiveSubObjectProperties=getTransitiveSubObjectProperties(objectProperty);
//            if (!transitiveSubObjectProperties.isEmpty()) {
            if( m_automataForComplexRoles.containsKey( objectProperty ) ){
                OWLClassExpression replacedConcept=getReplacementFor(objectAll);
                String initialState = m_automataForComplexRoles.get( objectProperty ).initials().toArray()[0].toString();
                String indexOfReplacedConcept = replacedConcept.asOWLClass().getURI().getFragment();
                OWLClassExpression replacement = m_factory.getOWLClass(URI.create("internal:all#"+indexOfReplacedConcept+initialState));
                if (objectAll.getFiller() instanceof OWLObjectComplementOf || objectAll.getFiller().equals(m_factory.getOWLNothing()))
                	replacement = m_factory.getOWLClass(URI.create("internal:all#"+indexOfReplacedConcept+initialState)).getComplementNNF();
//                for (OWLObjectPropertyExpression transitiveSubObjectProperty : transitiveSubObjectProperties) {
//                    OWLObjectAllValuesFrom subObjectAll=m_factory.getOWLObjectAllValuesFrom(transitiveSubObjectProperty,objectAll.getFiller());
//                    getReplacementFor(subObjectAll);
//                }
                return replacement;
            }
        }
        return desc;
    }
    protected OWLClassExpression getReplacementFor(OWLObjectAllValuesFrom objectAll) {
        OWLClassExpression replacement=m_replacedDescriptions.get(objectAll);
        if (replacement==null) {
            replacement=m_factory.getOWLClass(URI.create("internal:all#"+m_replacedDescriptions.size()));
//            if (objectAll.getFiller() instanceof OWLObjectComplementOf || objectAll.getFiller().equals(m_factory.getOWLNothing()))
//                replacement=replacement.getComplementNNF();
            m_replacedDescriptions.put(objectAll,replacement);
        }
        return replacement;
    }
    /**
     * gstoil This is not used anymore
     */
    protected Set<OWLObjectPropertyExpression> getTransitiveSubObjectProperties(OWLObjectPropertyExpression objectProperty) {
        Set<OWLObjectPropertyExpression> result=new HashSet<OWLObjectPropertyExpression>();
        if (m_transitiveObjectProperties.contains(objectProperty))
            result.add(objectProperty);
        Set<OWLObjectPropertyExpression> subObjectProperties=m_subObjectProperties.getSuccessors(objectProperty);
        for (OWLObjectPropertyExpression subObjectProperty : subObjectProperties)
            if (m_transitiveObjectProperties.contains(subObjectProperty))
                result.add(subObjectProperty);
        return result;
    }

    public void rewriteAxioms(OWLAxioms axioms, Map<OWLObjectPropertyExpression, Automaton> automataOfComplexObjectProperties) {
            m_automataForComplexRoles.putAll( automataOfComplexObjectProperties );
            rewriteAxioms( axioms );
    }
}
