// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.structural.OWLAxioms.ComplexObjectPropertyInclusion;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;
import rationals.transformations.Normalizer;
import rationals.transformations.Reducer;

public class AutomataConstructionManager {
	
	protected final Set<OWLObjectPropertyExpression> m_nonSimpleRoles;
	
	public AutomataConstructionManager(){
		m_nonSimpleRoles = new HashSet<OWLObjectPropertyExpression>();
	}

	public Map<OWLObjectPropertyExpression,Automaton> createAutomata(Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions,Collection<ComplexObjectPropertyInclusion> complexObjectPropertyInclusions) {
		Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> equivalentPropertiesMap = findEquivalentRoles( simpleObjectPropertyInclusions );

		Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> inverseRolesMap = buildInverseRolesMap( simpleObjectPropertyInclusions );
        Graph<OWLObjectPropertyExpression> propertyDependencyGraph = buildPropertyOrdering( simpleObjectPropertyInclusions, complexObjectPropertyInclusions, equivalentPropertiesMap );
        checkForRegularity( propertyDependencyGraph, equivalentPropertiesMap );

        Graph<OWLObjectPropertyExpression> complexRolesDependencyGraph = propertyDependencyGraph.clone();  
        Map<OWLObjectPropertyExpression,Automaton> individualAutomata = buildIndividualAutomata( complexRolesDependencyGraph, simpleObjectPropertyInclusions, complexObjectPropertyInclusions, equivalentPropertiesMap );
        Set<OWLObjectPropertyExpression> simpleRoles = findSimpleRoles( complexRolesDependencyGraph, individualAutomata );

        propertyDependencyGraph.removeElements( simpleRoles );
        complexRolesDependencyGraph.removeElements( simpleRoles );
		m_nonSimpleRoles.addAll( complexRolesDependencyGraph.getElements() );

		Map<OWLObjectPropertyExpression,Automaton> connectedAutomata = connectAllAutomata(propertyDependencyGraph,inverseRolesMap,individualAutomata,simpleObjectPropertyInclusions);

		for( OWLObjectPropertyExpression owlPropExpr : connectedAutomata.keySet() )
			connectedAutomata.put( owlPropExpr, minimizeAndNormalizeAutomaton( connectedAutomata.get( owlPropExpr ) ));

		return connectedAutomata;
	}
	private Map<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>> buildInverseRolesMap(
			Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions) {
		Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> inverseRolesMap = new HashMap<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>>(); 
		
		for (OWLObjectPropertyExpression[] inclusion : simpleObjectPropertyInclusions)
			if( inclusion[1] instanceof OWLObjectInverseOf ){
				Set<OWLObjectPropertyExpression> inverseRoles = inverseRolesMap.get( inclusion[0] );
				if( inverseRoles == null )
					inverseRoles = new HashSet<OWLObjectPropertyExpression>();
				inverseRoles.add( inclusion[1].getInverseProperty().getSimplified() );
				inverseRolesMap.put(inclusion[0], inverseRoles);
			}
		return inverseRolesMap;
	}

	private Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> findEquivalentRoles(Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions ){
		Graph<OWLObjectPropertyExpression> propertyDependencyGraph = new Graph<OWLObjectPropertyExpression>();
		Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> equivalentObjectPropertiesMapping = new HashMap<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>>();
	  	for (OWLObjectPropertyExpression[] inclusion : simpleObjectPropertyInclusions){
    		if( !inclusion[0].equals( inclusion[1] ) && !inclusion[0].equals( inclusion[1].getInverseProperty().getSimplified() )){
    			propertyDependencyGraph.addEdge( inclusion[0], inclusion[1] );
    		}
	  	}
	  	propertyDependencyGraph.transitivelyClose();
	  	for(OWLObjectPropertyExpression objExpr : propertyDependencyGraph.getElements()){
	  		if( propertyDependencyGraph.getSuccessors( objExpr ).contains( objExpr ) || 
	  			propertyDependencyGraph.getSuccessors( objExpr ).contains( objExpr.getInverseProperty().getSimplified() )){
	  			Set<OWLObjectPropertyExpression> equivPropertiesSet = new HashSet<OWLObjectPropertyExpression>();
	  			for( OWLObjectPropertyExpression succ : propertyDependencyGraph.getSuccessors( objExpr ) ){
	  				if( !succ.equals( objExpr ) && (propertyDependencyGraph.getSuccessors( succ ).contains( objExpr ) || 
	  												propertyDependencyGraph.getSuccessors( succ ).contains( objExpr.getInverseProperty().getSimplified() )) )
	  					equivPropertiesSet.add( succ );
	  			}
	  			equivalentObjectPropertiesMapping.put( objExpr, equivPropertiesSet);
	  		}
	  	}
		return equivalentObjectPropertiesMapping;
	}
	private Set<OWLObjectPropertyExpression> findSimpleRoles(Graph<OWLObjectPropertyExpression> complexRolesDependencyGraph,Map<OWLObjectPropertyExpression, Automaton> individualAutomata) {
		Set<OWLObjectPropertyExpression> simpleRoles = new HashSet<OWLObjectPropertyExpression>();
		
		Graph<OWLObjectPropertyExpression> complexRolesDependencyGraphWithInverses = complexRolesDependencyGraph.clone(); 
		
		for( OWLObjectPropertyExpression complexOWLProp1 : complexRolesDependencyGraph.getElements() )
			for( OWLObjectPropertyExpression complexOWLProp2 : complexRolesDependencyGraph.getSuccessors( complexOWLProp1 )) 
				complexRolesDependencyGraphWithInverses.addEdge( complexOWLProp1.getInverseProperty().getSimplified(), complexOWLProp2.getInverseProperty().getSimplified() );
		
		Graph<OWLObjectPropertyExpression> invertedGraph = complexRolesDependencyGraphWithInverses.getInverse();
		invertedGraph.transitivelyClose();

		for(OWLObjectPropertyExpression owlProp : invertedGraph.getElements() ){
			boolean someComplexSubRole = false;
			for( OWLObjectPropertyExpression subDependingProperties : invertedGraph.getSuccessors( owlProp )){
				if( individualAutomata.containsKey( subDependingProperties ) || 
					individualAutomata.containsKey( subDependingProperties.getInverseProperty().getSimplified() ) ){
					someComplexSubRole = true;
					break;
				}
			}
			if( !someComplexSubRole && !individualAutomata.containsKey( owlProp ) && !individualAutomata.containsKey( owlProp.getInverseProperty().getSimplified() ))
				simpleRoles.add( owlProp );
		}
		return simpleRoles;
	}
	private Map<OWLObjectPropertyExpression, Automaton> connectAllAutomata(Graph<OWLObjectPropertyExpression> propertyDependencyGraph , Map<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>> inverseRolesMap, Map<OWLObjectPropertyExpression, Automaton> individualAutomata,Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions) {
    	Graph<OWLObjectPropertyExpression> transClosedGraph = propertyDependencyGraph.clone();
    	transClosedGraph.transitivelyClose();
    	
    	Set<OWLObjectPropertyExpression> rolesToStartRecursion = new HashSet<OWLObjectPropertyExpression>();
    	for( OWLObjectPropertyExpression owlProp : transClosedGraph.getElements() )
    		if( transClosedGraph.getSuccessors( owlProp ).isEmpty() )
    			rolesToStartRecursion.add( owlProp );

    	Graph<OWLObjectPropertyExpression> inversePropertyDependencyGraph = propertyDependencyGraph.getInverse();

    	Map<OWLObjectPropertyExpression,Automaton> completeAutomata = new HashMap<OWLObjectPropertyExpression,Automaton>();
    	for( OWLObjectPropertyExpression superRole : rolesToStartRecursion )
    		buildCompleteAutomataForRoles( superRole, inverseRolesMap, individualAutomata, completeAutomata, inversePropertyDependencyGraph );

    	for( OWLObjectPropertyExpression owlProp : individualAutomata.keySet() )
    		if( !completeAutomata.containsKey( owlProp ) ){
    			Automaton autoOfRole = individualAutomata.get( owlProp );
    			if( (completeAutomata.containsKey( owlProp.getInverseProperty().getSimplified() ) &&  
    	    			inversePropertyDependencyGraph.getElements().contains( owlProp.getInverseProperty().getSimplified() ) )
    	    			|| individualAutomata.containsKey( owlProp.getInverseProperty().getSimplified() ) ){
    	    			Automaton autoOfInverseRole = completeAutomata.get( owlProp.getInverseProperty().getSimplified() );
    	    			if( autoOfInverseRole == null)
    	    				autoOfInverseRole = individualAutomata.get( owlProp.getInverseProperty().getSimplified() );
    	    			increaseAutoWithAutoOfInverseRole( autoOfRole, autoOfInverseRole );
    			}
    			completeAutomata.put( owlProp, autoOfRole );
    		}

    	Map<OWLObjectPropertyExpression,Automaton> extraCompleteAutomataForInverseRoles = new HashMap<OWLObjectPropertyExpression,Automaton>();
    	for( OWLObjectPropertyExpression owlProp : completeAutomata.keySet() )
    		if( !completeAutomata.containsKey( owlProp.getInverseProperty().getSimplified() ) )
    			extraCompleteAutomataForInverseRoles.put( owlProp.getInverseProperty().getSimplified(), getMirroredCopy( completeAutomata.get( owlProp ) ));
    	completeAutomata.putAll( extraCompleteAutomataForInverseRoles );
    	extraCompleteAutomataForInverseRoles.clear();
    	
    	for( OWLObjectPropertyExpression owlProp : completeAutomata.keySet() ){
    		Automaton autoOfRole = completeAutomata.get( owlProp );
    		//The role has an inverse that has an automaton. Then the automata need to be joined
    		if( (completeAutomata.containsKey( owlProp.getInverseProperty().getSimplified() ))) {
    			Automaton autoOfInverseRole = completeAutomata.get( owlProp.getInverseProperty().getSimplified() );
//    			increaseAutoWithAutoOfInverseRole( autoOfRole, autoOfInverseRole );
    	     	//The role is also a symmetric one.
    			for( OWLObjectPropertyExpression[] inclusion : simpleObjectPropertyInclusions )
    				if( inclusion[0].equals( owlProp ) && inclusion[1].getInverseProperty().getSimplified().equals( owlProp ) || 
    					inclusion[0].getInverseProperty().getSimplified().equals( owlProp ) && inclusion[1].equals( owlProp )){
						Transition basicTransition = new Transition((State)autoOfRole.initials().toArray()[0], owlProp.getInverseProperty().getSimplified(), (State)autoOfRole.terminals().toArray()[0]);
						try {
							autoOfRole.addTransition( basicTransition );
							connectAutomata(autoOfRole, autoOfInverseRole, basicTransition);
							completeAutomata.put( owlProp , autoOfRole );
						} catch (NoSuchStateException e) {
							throw new IllegalArgumentException( "Could not create automaton for symmetric role: " + owlProp + " from already existing auto of its inverse");
						}
    				}
    		}
    	}
    	for( OWLObjectPropertyExpression owlProp : completeAutomata.keySet() )
    		if( completeAutomata.containsKey( owlProp ) && !completeAutomata.containsKey( owlProp.getInverseProperty().getSimplified() ) )
    			extraCompleteAutomataForInverseRoles.put( owlProp.getInverseProperty().getSimplified(), getMirroredCopy( completeAutomata.get( owlProp ) ));

    	completeAutomata.putAll( extraCompleteAutomataForInverseRoles );
    	extraCompleteAutomataForInverseRoles.clear();

    	return completeAutomata;
	}
	private void increaseAutoWithAutoOfInverseRole(Automaton autoOfRole,Automaton automatonOfInverse) {
		State initialState = (State)autoOfRole.initials().toArray()[0];
		State finalState = (State)autoOfRole.terminals().toArray()[0];
		Transition tr = (Transition)autoOfRole.deltaFrom(initialState, finalState).toArray()[0];
		connectAutomata(autoOfRole, getMirroredCopy( automatonOfInverse ), tr);
	}

	private Automaton buildCompleteAutomataForRoles(OWLObjectPropertyExpression roleToBuildAutomaton, Map<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>> inverseRolesMap, Map<OWLObjectPropertyExpression, Automaton> individualAutomata, Map<OWLObjectPropertyExpression, Automaton> completeAutomata, Graph<OWLObjectPropertyExpression> inversedPropertyDependencyGraph) {

		if( completeAutomata.containsKey( roleToBuildAutomaton ) )
			return completeAutomata.get( roleToBuildAutomaton );
		else if( completeAutomata.containsKey( roleToBuildAutomaton.getInverseProperty().getSimplified() ) && 
				 !individualAutomata.containsKey( roleToBuildAutomaton )){
			Automaton mirroredCopy = getMirroredCopy( completeAutomata.get( roleToBuildAutomaton.getInverseProperty().getSimplified() ));
			completeAutomata.put( roleToBuildAutomaton, mirroredCopy );

			return mirroredCopy;
		}
		if( inversedPropertyDependencyGraph.getSuccessors( roleToBuildAutomaton ).isEmpty() ){
			Automaton autoForLeafProperty = individualAutomata.get( roleToBuildAutomaton );
			if( autoForLeafProperty == null ){
				Set<OWLObjectPropertyExpression> inverses = inverseRolesMap.get( roleToBuildAutomaton );
				boolean noInverseRoleWithAutomaton = true;
				if( inverses != null){
					for( OWLObjectPropertyExpression inverse : inverses )
						if( individualAutomata.containsKey( inverse ) ){
							autoForLeafProperty = getMirroredCopy( buildCompleteAutomataForRoles( inverse, inverseRolesMap, individualAutomata, completeAutomata, inversedPropertyDependencyGraph ) );
							completeAutomata.put( roleToBuildAutomaton , autoForLeafProperty );
							noInverseRoleWithAutomaton = false;
							break;
						}
				}
				else if( individualAutomata.containsKey( roleToBuildAutomaton.getInverseProperty().getSimplified() ) ){
					autoForLeafProperty = getMirroredCopy( buildCompleteAutomataForRoles( roleToBuildAutomaton.getInverseProperty().getSimplified(), inverseRolesMap, individualAutomata, completeAutomata, inversedPropertyDependencyGraph ) );
					completeAutomata.put( roleToBuildAutomaton , autoForLeafProperty );
					noInverseRoleWithAutomaton = false;
				}
				if( noInverseRoleWithAutomaton ){
					autoForLeafProperty = new Automaton();
					State initial = autoForLeafProperty.addState(true , false );
					State accepting = autoForLeafProperty.addState(false , true );
					try {
						autoForLeafProperty.addTransition( new Transition(initial, roleToBuildAutomaton, accepting ) );
					} catch (NoSuchStateException e) {
						throw new IllegalArgumentException( "Could not create automaton for role at the bottom of hierarchy (simple role)");
					}
					Automaton autoOfInverseRole = null;
					if( roleToBuildAutomaton.getInverseProperty().getSimplified().isAnonymous() && individualAutomata.containsKey( roleToBuildAutomaton.getInverseProperty().getSimplified() ) ){
						autoOfInverseRole = buildCompleteAutomataForRoles( roleToBuildAutomaton.getInverseProperty().getSimplified(), inverseRolesMap, individualAutomata, completeAutomata, inversedPropertyDependencyGraph );
						increaseAutoWithAutoOfInverseRole( autoForLeafProperty, getMirroredCopy( autoOfInverseRole ) );
						completeAutomata.put( roleToBuildAutomaton , autoForLeafProperty );
						completeAutomata.put( roleToBuildAutomaton.getInverseProperty().getSimplified() , getMirroredCopy( autoForLeafProperty ) );					
					}
					else{
						completeAutomata.put( roleToBuildAutomaton , autoForLeafProperty );
					}
				}
			}
			else{
				Automaton autoOfInverseRole = null;
				if( roleToBuildAutomaton.getInverseProperty().getSimplified().isAnonymous() && individualAutomata.containsKey( roleToBuildAutomaton.getInverseProperty().getSimplified() ) ){
					autoOfInverseRole = buildCompleteAutomataForRoles( roleToBuildAutomaton.getInverseProperty().getSimplified(), inverseRolesMap, individualAutomata, completeAutomata, inversedPropertyDependencyGraph );
					increaseAutoWithAutoOfInverseRole( autoForLeafProperty, getMirroredCopy( autoOfInverseRole ) );
					completeAutomata.put( roleToBuildAutomaton , autoForLeafProperty );
					completeAutomata.put( roleToBuildAutomaton.getInverseProperty().getSimplified() , getMirroredCopy( autoForLeafProperty ) );					
				}
				else{
		    		Set<OWLObjectPropertyExpression> inverses = inverseRolesMap.get( roleToBuildAutomaton );
		    		if( inverses != null){
		    			autoOfInverseRole = null;
		    			for( OWLObjectPropertyExpression inverse : inverses ){
		    				if( individualAutomata.containsKey( inverse ) ){
		    					autoOfInverseRole = individualAutomata.get( inverse );
		    					increaseAutoWithAutoOfInverseRole( autoForLeafProperty, autoOfInverseRole );
		    				}
		    			}
		    		}
					completeAutomata.put( roleToBuildAutomaton , autoForLeafProperty );
				}
			}
			return autoForLeafProperty;
		}
		else{
			Automaton autoOfBiggerRole = individualAutomata.get( roleToBuildAutomaton );
			if( autoOfBiggerRole == null ){
				autoOfBiggerRole = new Automaton();
				State initialState = autoOfBiggerRole.addState( true , false );
				State finalState = autoOfBiggerRole.addState( false , true);
       			Transition trans = new Transition(initialState, roleToBuildAutomaton , finalState);
        		try {
        			autoOfBiggerRole.addTransition( trans );
        		} catch (NoSuchStateException e) {
					throw new IllegalArgumentException("Could not create automaton");
				}
        		for( OWLObjectPropertyExpression smallerRole : inversedPropertyDependencyGraph.getSuccessors( roleToBuildAutomaton ) ){
	        		Automaton autoOfSmallerRole = buildCompleteAutomataForRoles( smallerRole, inverseRolesMap, individualAutomata, completeAutomata, inversedPropertyDependencyGraph );
	        		connectAutomata( autoOfBiggerRole, autoOfSmallerRole, trans );
		        }
				Automaton autoOfInverseRole = null;
				if( roleToBuildAutomaton.getInverseProperty().getSimplified().isAnonymous() && individualAutomata.containsKey( roleToBuildAutomaton.getInverseProperty().getSimplified() ) ){
					autoOfInverseRole = buildCompleteAutomataForRoles( roleToBuildAutomaton.getInverseProperty().getSimplified(), inverseRolesMap, individualAutomata, completeAutomata, inversedPropertyDependencyGraph );
					increaseAutoWithAutoOfInverseRole( autoOfBiggerRole, getMirroredCopy( autoOfInverseRole ) );
					completeAutomata.put( roleToBuildAutomaton , autoOfBiggerRole );
					completeAutomata.put( roleToBuildAutomaton.getInverseProperty().getSimplified() , getMirroredCopy( autoOfBiggerRole ) );					
				}
				else{
					completeAutomata.put( roleToBuildAutomaton , autoOfBiggerRole );
				}
			}
			else{
				Object[] transitionsIterator = autoOfBiggerRole.delta().toArray() ;
	        	for( OWLObjectPropertyExpression smallerRole : inversedPropertyDependencyGraph.getSuccessors( roleToBuildAutomaton ) ){
	        		boolean someInternalTransitionMatched = false;
	        		for( int i=0 ; i<transitionsIterator.length ; i++ ) {
	        			Transition trans = (Transition) transitionsIterator[i];
	        			
		        		if( trans.label() != null && trans.label().equals( smallerRole ) ){
		        			Automaton autoOfSmallerRole = buildCompleteAutomataForRoles( smallerRole, inverseRolesMap, individualAutomata, completeAutomata, inversedPropertyDependencyGraph );
		        			connectAutomata( autoOfBiggerRole, autoOfSmallerRole, trans );
		        			someInternalTransitionMatched = true;
		        		}
	        		}
		        	if( !someInternalTransitionMatched ){
	        			Automaton autoOfSmallerRole = buildCompleteAutomataForRoles( smallerRole, inverseRolesMap, individualAutomata, completeAutomata, inversedPropertyDependencyGraph );
	        			Transition initial2TerminalTransition = (Transition)autoOfBiggerRole.deltaFrom( (State)autoOfBiggerRole.initials().toArray()[0], (State)autoOfBiggerRole.terminals().toArray()[0]).toArray()[0];
	        			connectAutomata( autoOfBiggerRole, autoOfSmallerRole, initial2TerminalTransition );
		        	}
	        	}
			}
			Automaton autoOfInverseRole = null;
			if( roleToBuildAutomaton.getInverseProperty().getSimplified().isAnonymous() && individualAutomata.containsKey( roleToBuildAutomaton.getInverseProperty().getSimplified() ) ){
				autoOfInverseRole = buildCompleteAutomataForRoles( roleToBuildAutomaton.getInverseProperty().getSimplified(), inverseRolesMap, individualAutomata, completeAutomata, inversedPropertyDependencyGraph );
				increaseAutoWithAutoOfInverseRole( autoOfBiggerRole, getMirroredCopy( autoOfInverseRole ) );
				completeAutomata.put( roleToBuildAutomaton , autoOfBiggerRole );
				completeAutomata.put( roleToBuildAutomaton.getInverseProperty().getSimplified() , getMirroredCopy( autoOfBiggerRole ) );					
			}
			else{
				completeAutomata.put( roleToBuildAutomaton , autoOfBiggerRole );
			}
	        return autoOfBiggerRole;
		}
	}
	private Automaton minimizeAndNormalizeAutomaton(Automaton automaton) {

		Reducer minimizerDeterminizer = new Reducer();
		Normalizer normalizer = new Normalizer();
		
		automaton = minimizerDeterminizer.transform( automaton );
		if( automaton.initials().size() != 1 )
			automaton = normalizer.transform( automaton );
		
		return automaton;
	}

	private void connectAutomata(Automaton autoOfBiggerRole,Automaton autoOfSmallerRole, Transition tr) {
		Map<State,State> stateMapper = getDisjointUnion( autoOfBiggerRole , autoOfSmallerRole);
		try {
			autoOfBiggerRole.addTransition( new Transition ( tr.start(), null, (State)stateMapper.get(autoOfSmallerRole.initials().toArray()[0])));
			autoOfBiggerRole.addTransition( new Transition ( (State)stateMapper.get( (State)autoOfSmallerRole.terminals().toArray()[0] ), null, tr.end()));
		} catch (NoSuchStateException e) {
			throw new IllegalArgumentException( "Could not build the Complete Automata of non-Simple Roles" );
		}
	}

	private Graph<OWLObjectPropertyExpression> buildPropertyOrdering(Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions,Collection<ComplexObjectPropertyInclusion> complexObjectPropertyInclusions, Map<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>> equivalentPropertiesMap){
    	Graph<OWLObjectPropertyExpression> propertyDependencyGraph = new Graph<OWLObjectPropertyExpression>();

    	for (OWLObjectPropertyExpression[] inclusion : simpleObjectPropertyInclusions){
    		if( !inclusion[0].equals( inclusion[1] ) && 
    			!inclusion[0].equals( inclusion[1].getInverseProperty().getSimplified() ) &&
    			(equivalentPropertiesMap.get( inclusion[0] ) == null ||
    			!equivalentPropertiesMap.get( inclusion[0] ).contains( inclusion[1] ) ) ){
    			propertyDependencyGraph.addEdge( inclusion[0], inclusion[1] );
    		}
    	}
    	for (OWLAxioms.ComplexObjectPropertyInclusion inclusion : complexObjectPropertyInclusions){
    		OWLObjectPropertyExpression owlSuperProperty = inclusion.m_superObjectProperties;
    		OWLObjectPropertyExpression owlSubPropertyInChain = null;
    		OWLObjectPropertyExpression[] owlSubProperties = inclusion.m_subObjectProperties;
    		if( owlSubProperties.length != 2 && owlSuperProperty.equals( owlSubProperties[0] ) && owlSuperProperty.equals( owlSubProperties[owlSubProperties.length-1] ))
    			throw new IllegalArgumentException("The given role hierarchy is not regular.");
    		
    		for( int i=0 ; i<owlSubProperties.length ; i++ ){
    			owlSubPropertyInChain = owlSubProperties[i];

        		if( owlSubProperties.length != 2 && i>0 && i<owlSubProperties.length-1 && 
        			( owlSubPropertyInChain.equals( owlSuperProperty ) || 
        			  (equivalentPropertiesMap.containsKey(owlSuperProperty) && equivalentPropertiesMap.get( owlSuperProperty ).contains( owlSubPropertyInChain ))
        			) )
        			throw new IllegalArgumentException("The given role hierarchy is not regular.");
        		else if( owlSubPropertyInChain.getInverseProperty().getSimplified().equals( owlSuperProperty ) )
        			throw new IllegalArgumentException("The given role hierarchy is not regular.");
        		else if ( !owlSubPropertyInChain.equals( owlSuperProperty ) )
        			propertyDependencyGraph.addEdge( owlSubPropertyInChain, owlSuperProperty );
    		}
    	}
     	return propertyDependencyGraph;
	}
	private void checkForRegularity(Graph<OWLObjectPropertyExpression> propertyDependencyGraph, Map<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>> equivalentPropertiesMap){
	   	Graph<OWLObjectPropertyExpression> regularityCheckGraph = propertyDependencyGraph.clone();
	   	Graph<OWLObjectPropertyExpression> regularityCheckGraphTemp;

	   	boolean trimmed = false;
	   	do{
	   		trimmed = false;
	   		regularityCheckGraphTemp = regularityCheckGraph.clone();
		   	for( OWLObjectPropertyExpression prop : regularityCheckGraphTemp.getElements() ){
		   		for( OWLObjectPropertyExpression succProp : regularityCheckGraphTemp.getSuccessors( prop )){
		   			if( equivalentPropertiesMap.containsKey( prop ) && equivalentPropertiesMap.get( prop ).contains( succProp ) ){
		   				for( OWLObjectPropertyExpression succPropSucc : regularityCheckGraphTemp.getSuccessors( succProp )){
		   					if( !prop.equals( succPropSucc ) )
		   						regularityCheckGraph.addEdge( prop, succPropSucc);
		   				}
		   				trimmed = true;
		   				regularityCheckGraph.getSuccessors( prop ).remove( succProp );
		   			}
		   		}
		   	}
	   	}while( trimmed );

	   	regularityCheckGraph.transitivelyClose();
    	
    	for( OWLObjectPropertyExpression prop : regularityCheckGraph.getElements() ){
    		Set<OWLObjectPropertyExpression> successors = regularityCheckGraph.getSuccessors( prop );
    		if( successors.contains( prop ) || successors.contains( prop.getInverseProperty().getSimplified() ) )
    			throw new IllegalArgumentException("The given role hierarchy is not regular.");
    	}
	}
    private Map<OWLObjectPropertyExpression,Automaton> buildIndividualAutomata(Graph<OWLObjectPropertyExpression> complexRolesDependencyGraph, Collection<OWLObjectPropertyExpression[]> simpleObjectPropertyInclusions, Collection<ComplexObjectPropertyInclusion> complexObjectPropertyInclusions, Map<OWLObjectPropertyExpression, Set<OWLObjectPropertyExpression>> equivalentPropertiesMap){
    	
    	Map<OWLObjectPropertyExpression,Automaton> automataMap = new HashMap<OWLObjectPropertyExpression,Automaton>();
    	Automaton auto = null;
    	State initialState = null;
    	State finalState = null;

     	for( OWLAxioms.ComplexObjectPropertyInclusion inclusion : complexObjectPropertyInclusions ){
    		OWLObjectPropertyExpression[] subObjectProperties = inclusion.m_subObjectProperties;
    		OWLObjectPropertyExpression superObjectProperty = inclusion.m_superObjectProperties;
    		if( !automataMap.containsKey( superObjectProperty )){
    			auto = new Automaton();
        		initialState = auto.addState( true , false );
        		finalState = auto.addState( false , true);
        		try {
					auto.addTransition( new Transition(initialState, superObjectProperty , finalState) );
				} catch (NoSuchStateException e) {
					throw new IllegalArgumentException("Could not create automaton");
				}
    		}
    		else{
    			auto = automataMap.get( superObjectProperty );
    			initialState = (State) auto.initials().toArray()[0];
    			finalState = (State) auto.terminals().toArray()[0];
    		}
    		//RR->R
    		if (subObjectProperties.length==2 && subObjectProperties[0].equals(superObjectProperty) && subObjectProperties[1].equals(superObjectProperty)) {
    			try {
					auto.addTransition( new Transition( finalState, null, initialState ) );
				} catch (NoSuchStateException e) {
					throw new IllegalArgumentException("Could not create automaton");
				}
    		}
    		//R S2...Sn->R
    		else if( subObjectProperties[0].equals(superObjectProperty) ){
    			State fromState = finalState;
    			OWLObjectPropertyExpression transitionLabel;
    			for( int i=1; i<subObjectProperties.length-1 ; i++ ){
    				transitionLabel = subObjectProperties[i];
    				if( equivalentPropertiesMap.containsKey( superObjectProperty ) &&  
    					equivalentPropertiesMap.get( superObjectProperty ).contains( transitionLabel ) )
    					transitionLabel = superObjectProperty;
    				try {
						fromState = addNewTransition( auto, fromState, transitionLabel );
					} catch (NoSuchStateException e) {
						throw new IllegalArgumentException("Could not create automaton");
					}
    			}
    			try {
    				transitionLabel = subObjectProperties[subObjectProperties.length-1];
    				if( equivalentPropertiesMap.containsKey( superObjectProperty ) &&  
    					equivalentPropertiesMap.get( superObjectProperty ).contains( transitionLabel ) )
    					transitionLabel = superObjectProperty;
					auto.addTransition( new Transition( fromState, transitionLabel , finalState) );
				} catch (NoSuchStateException e) {
					throw new IllegalArgumentException("Could not create automaton");
				}
    		}
    		//S1...Sn-1 R->R
    		else if( subObjectProperties[subObjectProperties.length-1].equals(superObjectProperty) ){
    			State fromState = initialState;
    			OWLObjectPropertyExpression transitionLabel;
    			for( int i=0; i<subObjectProperties.length-2 ; i++ ){
    				transitionLabel = subObjectProperties[i];
    				if( equivalentPropertiesMap.containsKey( superObjectProperty ) &&  
    					equivalentPropertiesMap.get( superObjectProperty ).contains( transitionLabel ) )
    					transitionLabel = superObjectProperty;
    				try {
						fromState = addNewTransition( auto, fromState, transitionLabel );
					} catch (NoSuchStateException e) {
						throw new IllegalArgumentException("Could not create automaton");
					}
    			}
    			try {
    				transitionLabel = subObjectProperties[subObjectProperties.length-2];
    				if( equivalentPropertiesMap.containsKey( superObjectProperty ) &&  
    					equivalentPropertiesMap.get( superObjectProperty ).contains( transitionLabel ) )
    					transitionLabel = superObjectProperty;
					auto.addTransition( new Transition( fromState,transitionLabel , initialState) );
				} catch (NoSuchStateException e) {
					throw new IllegalArgumentException("Could not create automaton");
				}
    		}
    		//S1...Sn->R
    		else{
    			State fromState = initialState;
    			OWLObjectPropertyExpression transitionLabel;
    			for( int i=0; i<subObjectProperties.length-1 ; i++ ){
    				transitionLabel = subObjectProperties[i];
    				if( equivalentPropertiesMap.containsKey( superObjectProperty ) &&  
    					equivalentPropertiesMap.get( superObjectProperty ).contains( transitionLabel ) )
    					transitionLabel = superObjectProperty;
    				try {
						fromState = addNewTransition( auto, fromState, transitionLabel );
					} catch (NoSuchStateException e) {
						throw new IllegalArgumentException("Could not create automaton");
					}
    			}
    			try {
    				transitionLabel = subObjectProperties[subObjectProperties.length-1];
    				if( equivalentPropertiesMap.containsKey( superObjectProperty ) &&  
    					equivalentPropertiesMap.get( superObjectProperty ).contains( transitionLabel ) )
    					transitionLabel = superObjectProperty;
					auto.addTransition( new Transition( fromState, transitionLabel, finalState) );
				} catch (NoSuchStateException e) {
					throw new IllegalArgumentException("Could not create automaton");
				}
    		}
    		automataMap.put( superObjectProperty, auto );
		}
     	//For symmetric roles
    	for( OWLObjectPropertyExpression owlProp : automataMap.keySet() )
    		for( OWLObjectPropertyExpression[] inclusion : simpleObjectPropertyInclusions )
    			if( inclusion[0].equals( owlProp ) && inclusion[1].getInverseProperty().getSimplified().equals( owlProp ) || 
    				inclusion[0].getInverseProperty().getSimplified().equals( owlProp ) && inclusion[1].equals( owlProp )){
    				Automaton au = automataMap.get( owlProp );
    				try {
						au.addTransition( new Transition((State)au.initials().toArray()[0], owlProp.getInverseProperty().getSimplified(), (State)au.terminals().toArray()[0]) );
	    				automataMap.put( owlProp , au );
					} catch (NoSuchStateException e) {
						throw new IllegalArgumentException("Could not create automaton for symmetric role:" + owlProp);
					}
    			}
    	//For those transitive roles that other roles do not depend on other roles the automaton is complete. 
    	//So we also need to build the auto for the inverse of R unless Inv(R) has its own.
    	for(ComplexObjectPropertyInclusion inclusion : complexObjectPropertyInclusions){
    		OWLObjectPropertyExpression owlSuperProperty = inclusion.m_superObjectProperties;
    		OWLObjectPropertyExpression[] owlSubPropertyExpression = inclusion.m_subObjectProperties;
    		if( owlSubPropertyExpression.length==2 && owlSubPropertyExpression[0].equals(owlSuperProperty) && owlSubPropertyExpression[1].equals(owlSuperProperty))
    			if( !complexRolesDependencyGraph.getElements().contains( owlSuperProperty ) && 
    				!automataMap.containsKey( owlSuperProperty.getInverseProperty().getSimplified() )){
    				Automaton autoOfRole = automataMap.get( owlSuperProperty );
    				automataMap.put(owlSuperProperty.getInverseProperty().getSimplified(), getMirroredCopy( autoOfRole ) );
    			}
		}
    	return automataMap;
    }
    private Map<State,State> getDisjointUnion( Automaton auto1, Automaton auto2 ){
    	Map<State,State> stateMapperUnionInverse = new HashMap<State,State>();
		Object[] states = auto2.states().toArray();
		for(int i=0 ; i<states.length ; i++ )
			stateMapperUnionInverse.put((State) states[i] , auto1.addState(false , false));

		Object[] transitions = auto2.delta().toArray();
	    for(int i=0 ; i<transitions.length ; i++ ){
	      Transition t = (Transition) transitions[i];
	      try {
	    	  auto1.addTransition(new Transition(	(State) stateMapperUnionInverse.get(t.start()) , 
	    			  									t.label() , 
	    			  								(State) stateMapperUnionInverse.get(t.end())));
	      }catch(NoSuchStateException x){
	    	  throw new IllegalArgumentException("Could not create disjoint union of automata");
	      }
	    }
	    return stateMapperUnionInverse;
    }
    private Automaton getMirroredCopy(Automaton auto){
    	Automaton mirroredCopy = new Automaton() ;
        Map<State,State> map = new HashMap<State,State>() ;
        Object[] objs = auto.states().toArray();
        for( int i =0; i<objs.length ; i++) {
          State e = (State) objs[i] ;
          map.put(e , mirroredCopy.addState(e.isTerminal() , e.isInitial())) ;
        }
        objs = auto.delta().toArray();
        for(int i=0 ; i<objs.length ; i++ ) {
          Transition t = (Transition) objs[i];
          try {
        	  if( t.label() instanceof OWLObjectPropertyExpression)
        		  mirroredCopy.addTransition(new Transition((State) map.get(t.end()),((OWLObjectPropertyExpression) t.label()).getInverseProperty().getSimplified() ,(State) map.get(t.start())));
        	  else
        		  mirroredCopy.addTransition(new Transition((State) map.get(t.end()) , t.label() ,(State) map.get(t.start())));
          } catch(NoSuchStateException x) {}
        }
        return mirroredCopy;
    }
	private State addNewTransition(Automaton auto, State fromState,OWLObjectPropertyExpression objectPropertyExpression) throws NoSuchStateException {
		OWLObjectPropertyExpression propertyOfChain = objectPropertyExpression;
		State toState = auto.addState( false , false );
		auto.addTransition( new Transition( fromState, propertyOfChain, toState) );
		return toState;
	}
	public Set<OWLObjectPropertyExpression> getM_nonSimpleRoles() {
		return m_nonSimpleRoles;
	}
}