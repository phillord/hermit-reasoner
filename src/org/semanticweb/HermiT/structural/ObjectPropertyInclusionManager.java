// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.owl.model.OWLCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLPropertyExpression;

import org.semanticweb.HermiT.graph.Graph;

import rationals.Automaton;
import rationals.Transition;
import rationals.transformations.EpsilonTransitionRemover;
import rationals.transformations.Reducer;
import rationals.transformations.ToDFA;

public class ObjectPropertyInclusionManager {
    protected final OWLDataFactory m_factory;
    protected final Graph<OWLObjectPropertyExpression> m_subObjectProperties;
    protected final Set<OWLObjectPropertyExpression> m_transitiveObjectProperties;
    protected final Map<OWLObjectAllRestriction,OWLDescription> m_replacedDescriptions;
    /**
     * @gstoil additions
     */
    protected final Map<OWLObjectPropertyExpression,Automaton> m_automataForComplexRoles;
    protected final Set<OWLObjectPropertyExpression> m_nonSimpleRoles;     																	

    public ObjectPropertyInclusionManager(OWLDataFactory factory) {
        m_factory=factory;
        m_subObjectProperties=new Graph<OWLObjectPropertyExpression>();
        m_transitiveObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_replacedDescriptions=new HashMap<OWLObjectAllRestriction,OWLDescription>();

        /**
         * @gstoil additions
         */
        m_automataForComplexRoles = new HashMap<OWLObjectPropertyExpression,Automaton>();
        m_nonSimpleRoles = new HashSet<OWLObjectPropertyExpression>();
    }
    
    public void prepareTransformation(OWLAxioms axioms) {
        for (OWLObjectPropertyExpression[] inclusion : axioms.m_simpleObjectPropertyInclusions)
            addInclusion(inclusion[0],inclusion[1]);

//        /*
//         * Previous HermiT code follows 
//        */ 
//        for (OWLAxioms.ComplexObjectPropertyInclusion inclusion : axioms.m_complexObjectPropertyInclusions)
//           	addInclusion(inclusion.m_subObjectProperties,inclusion.m_superObjectProperties);

        AutomataConstructionManager automataBuilder = new AutomataConstructionManager();
        m_automataForComplexRoles.putAll( automataBuilder.createAutomata( axioms.m_simpleObjectPropertyInclusions, axioms.m_complexObjectPropertyInclusions ) );
        m_nonSimpleRoles.addAll( automataBuilder.getM_nonSimpleRoles() );

        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_asymmetricObjectProperties)
        	if( m_nonSimpleRoles.contains( objectPropertyExpression ) )
                throw new IllegalArgumentException( "Non simple role '" + objectPropertyExpression + "' appears in asymmetricity axiom");

        for (OWLObjectPropertyExpression objectPropertyExpression : axioms.m_irreflexiveObjectProperties)
        	if( m_nonSimpleRoles.contains( objectPropertyExpression ) )
                throw new IllegalArgumentException( "Non simple role '" + objectPropertyExpression + "' appears in asymmetricity axiom");

        for (OWLObjectPropertyExpression[] properties : axioms.m_disjointObjectProperties)
            for (int i=0;i<properties.length;i++)
            	if( m_nonSimpleRoles.contains( properties[i] ) )
                    throw new IllegalArgumentException( "Non simple role '" + properties[i] + "' appears in disjoint properties axiom");
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
    public void rewriteAxioms(OWLAxioms axioms) {
    	/**
    	 * @gstoil additions modifications
    	 */
//        m_subObjectProperties.transitivelyClose();
        for (OWLDescription[] inclusion : axioms.m_conceptInclusions)
            for (int index=0;index<inclusion.length;index++)
                inclusion[index]=replaceDescriptionIfNecessary(inclusion[index]);

        for (Map.Entry<OWLObjectAllRestriction,OWLDescription> mapping : m_replacedDescriptions.entrySet()) {
            OWLObjectAllRestriction replacedConcept=mapping.getKey();
            OWLDescription conceptForInitialState=mapping.getValue();
            
            OWLObjectPropertyExpression objectProperty = replacedConcept.getProperty();
			OWLDescription owlConcept = replacedConcept.getFiller();
            
            Automaton automatonOfRole = m_automataForComplexRoles.get( objectProperty );
//          EpsilonTransitionRemover eRemover = new EpsilonTransitionRemover();
//          automatonOfRole = eRemover.transform( automatonOfRole );
//          ToDFA determinizer = new ToDFA();
//          automatonOfRole = determinizer.transform( automatonOfRole );

//			Reducer also calles ToDFA.transform() and ToDFA in turn calls EpsilonTransRemover.transform.            
            Reducer minimizer = new Reducer();
            automatonOfRole = minimizer.transform( automatonOfRole );

    		Object[] transitionsIterator = automatonOfRole.delta().toArray();
    		OWLDescription fromStateConcept = null;
			OWLDescription toStateConcept = null;
    		for( int i =0 ; i<transitionsIterator.length ; i++ ) {
    			Transition trans = (Transition) transitionsIterator[i];
	    		if( trans.start().isInitial() )
	    			fromStateConcept = conceptForInitialState.getComplementNNF();
	    		else
	    			fromStateConcept = m_factory.getOWLClass(URI.create("internal:all#" + conceptForInitialState + trans.start().toString() )).getComplementNNF();
	    		
	    		if( trans.end().isInitial() )
	    			toStateConcept = conceptForInitialState;
	    		else
	    			toStateConcept = m_factory.getOWLClass(URI.create("internal:all#" + conceptForInitialState + trans.end().toString() ));
	    		
	    		if( trans.label() == null )
	    			axioms.m_conceptInclusions.add(new OWLDescription[] { fromStateConcept, toStateConcept });

	    		else{
	    			OWLObjectAllRestriction consequentAll=m_factory.getOWLObjectAllRestriction(
	    												(OWLObjectPropertyExpression)trans.label(), toStateConcept );
	    			axioms.m_conceptInclusions.add(new OWLDescription[] { fromStateConcept, consequentAll });
	    		}
    		}
    		fromStateConcept = m_factory.getOWLClass(URI.create("internal:all#" + conceptForInitialState + automatonOfRole.terminals().toArray()[0] )).getComplementNNF();
    		axioms.m_conceptInclusions.add(new OWLDescription[] { fromStateConcept, owlConcept });
        }
        m_automataForComplexRoles.clear();
        m_nonSimpleRoles.clear();
        m_replacedDescriptions.clear();

        /**
         * @previous code simulating an automaton for transitive roles and sub-roles
         */
//            axioms.m_conceptInclusions.add(new OWLDescription[] { replacement.getComplementNNF(),replacedConcept });
//            for (OWLObjectPropertyExpression transitiveSubObjectProperty : getTransitiveSubObjectProperties(replacedConcept.getProperty())) {
//                OWLObjectAllRestriction consequentAll=m_factory.getOWLObjectAllRestriction(transitiveSubObjectProperty,replacedConcept.getFiller());
//                OWLDescription consequentReplacement=m_replacedDescriptions.get(consequentAll);
//                OWLObjectAllRestriction allConsequentReplacement=m_factory.getOWLObjectAllRestriction(transitiveSubObjectProperty,consequentReplacement);
//                axioms.m_conceptInclusions.add(new OWLDescription[] { replacement.getComplementNNF(),allConsequentReplacement });
//            }
//        }
//        m_replacedDescriptions.clear();
    }
    protected OWLDescription replaceDescriptionIfNecessary(OWLDescription desc) {
    	/**
    	 * @gstoil modifications/additions
    	 */
        if (desc instanceof OWLObjectAllRestriction) {
            OWLObjectAllRestriction objectAll=(OWLObjectAllRestriction)desc;
            OWLObjectPropertyExpression objectProperty=objectAll.getProperty();
//            Set<OWLObjectPropertyExpression> transitiveSubObjectProperties=getTransitiveSubObjectProperties(objectProperty);
//            if (!transitiveSubObjectProperties.isEmpty()) {
            if( this.m_automataForComplexRoles.containsKey( objectProperty ) ){
                OWLDescription replacement=getReplacementFor(objectAll);
//                for (OWLObjectPropertyExpression transitiveSubObjectProperty : transitiveSubObjectProperties) {
//                    OWLObjectAllRestriction subObjectAll=m_factory.getOWLObjectAllRestriction(transitiveSubObjectProperty,objectAll.getFiller());
//                    getReplacementFor(subObjectAll);
//                }
                return replacement;
            }
        }
        return desc;
    }
    protected OWLDescription getReplacementFor(OWLObjectAllRestriction objectAll) {
        OWLDescription replacement=m_replacedDescriptions.get(objectAll);
        if (replacement==null) {
            replacement=m_factory.getOWLClass(URI.create("internal:all#"+m_replacedDescriptions.size()));
            if (objectAll.getFiller() instanceof OWLObjectComplementOf || objectAll.getFiller().equals(m_factory.getOWLNothing()))
                replacement=replacement.getComplementNNF();
            m_replacedDescriptions.put(objectAll,replacement);
        }
        return replacement;
    }
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
}
