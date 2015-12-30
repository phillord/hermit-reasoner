package rationals.transformations;

import java.util.HashMap;
import java.util.Map;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * Removes states that neither accessible nor coaccessible.
 * 
 * @author nono
 * @version $Id: Pruner.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Pruner implements UnaryTransformation {

  @Override
public Automaton transform(Automaton a) {
    Map<State, State> conversion = new HashMap<>() ;
    Automaton b = new Automaton() ;
    for (State e : a.accessibleAndCoAccessibleStates()) {
      conversion.put(e , b.addState(e.isInitial() , e.isTerminal())) ;
    }
    for (Transition t :a.delta()){
      State bs = conversion.get(t.start()) ;
      State be = conversion.get(t.end()) ;
      if(bs == null || be == null)
          continue;
        b.addTransition(new Transition(
          bs,
          t.label() ,
          be),null);
    }
    return b ;
  }
}
  
