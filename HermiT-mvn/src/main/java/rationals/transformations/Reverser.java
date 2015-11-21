package rationals.transformations;


import java.util.HashMap;
import java.util.Map;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

/**
 * Computes the reversal of an Automaton.
 * <p /> 
 * <ul>
 * <li>C = A-</li>
 * <li>S(C) = S(A)</li>
 * <li>S0(C) = T(A)</li>
 * <li>T(C) = S0(A)</li>
 * <li>D(C) = { (s1,a,s2) | exists (s2,a,s1) in D(A)  }</li>
 * </ul>

 * @author nono
 * @version $Id: Reverser.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Reverser implements UnaryTransformation {

  @Override
public Automaton transform(Automaton a) {
    Automaton b = new Automaton() ;
    Map<State, State> map = new HashMap<>() ;
    for(State e : a.states()) {
      map.put(e , b.addState(e.isTerminal() , e.isInitial())) ;
    }
    for(Transition t : a.delta()) {
        b.addTransition(new Transition(
          map.get(t.end()) ,
          t.label() ,
          map.get(t.start())),null) ;
    }
    return b ;
  }
}
