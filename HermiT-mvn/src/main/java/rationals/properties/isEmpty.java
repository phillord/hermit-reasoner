package rationals.properties;

import java.util.Iterator;

import rationals.Automaton;
import rationals.State;


public class isEmpty implements UnaryTest {
  public boolean test(Automaton a) {
    Iterator i = a.accessibleStates().iterator() ;
    while (i.hasNext()) {
      if (((State) i.next()).isTerminal()) return false ;
    }
    return true ;
  }
}
