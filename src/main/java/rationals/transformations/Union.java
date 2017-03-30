package rationals.transformations;
import java.util.HashMap;
import java.util.Map;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * Compute the union of two automaton.
 * <ul>
 * <li>C = A + B</li>
 * <li>S(C) = S(A) U S(B)</li>
 * <li>S0(C) = S0(A) U SO(B)</li>
 * <li>T(C) = T(A) U T(B)</li>
 * <li>D(C) = D(A) U D(B)</li>
 * </ul>

 * @author nono
 * @version $Id: Union.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Union implements BinaryTransformation {


  @Override
public Automaton transform(Automaton a , Automaton b) {
    Automaton ap = (Automaton) a.clone() ;
    Map<State, State> map = new HashMap<>() ;
    for (State e: b.states()) {
      map.put(e , ap.addState(e.isInitial() , e.isTerminal())) ;
    }
    for (Transition t :b.delta()) {
        ap.addTransition(new Transition(
          map.get(t.start()) ,
          t.label() ,
          map.get(t.end())),null) ;
    }
    return ap ;
  }      
}
