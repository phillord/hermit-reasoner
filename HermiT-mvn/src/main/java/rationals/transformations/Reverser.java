package rationals.transformations;


import java.util.HashMap;
import java.util.Iterator;
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

  public Automaton transform(Automaton a) {
    Automaton b = new Automaton() ;
    Map map = new HashMap() ;
    Iterator i = a.states().iterator() ;
    while(i.hasNext()) {
      State e = (State) i.next() ;
      map.put(e , b.addState(e.isTerminal() , e.isInitial())) ;
    }
    i = a.delta().iterator() ;
    while(i.hasNext()) {
      Transition t = (Transition) i.next() ;
      try {
        b.addTransition(new Transition(
          (State) map.get(t.end()) ,
          t.label() ,
          (State) map.get(t.start()))) ;
      } catch(NoSuchStateException x) {}
    }
    return b ;
  }
}
