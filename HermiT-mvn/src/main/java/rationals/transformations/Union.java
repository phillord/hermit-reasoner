package rationals.transformations;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import rationals.Automaton;
import rationals.NoSuchStateException;
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


  public Automaton transform(Automaton a , Automaton b) {
    Automaton ap = (Automaton) a.clone() ;
    Map map = new HashMap() ;
    Iterator i = b.states().iterator() ;
    while(i.hasNext()) {
      State e = (State) i.next() ;
      map.put(e , ap.addState(e.isInitial() , e.isTerminal())) ;
    }
    i = b.delta().iterator() ;
    while(i.hasNext()) {
      Transition t = (Transition) i.next() ;
      try {
        ap.addTransition(new Transition(
          (State) map.get(t.start()) ,
          t.label() ,
          (State) map.get(t.end()))) ;
      } catch(NoSuchStateException x) {}
    }
    return ap ;
  }      
}
