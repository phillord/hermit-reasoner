package rationals.transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * A set of utility methods used in transformations of automaton.
 * 
 * @author nono
 * @version $Id: TransformationsToolBox.java 10 2007-05-30 17:25:00Z oqube $
 */
public class TransformationsToolBox {

  public static boolean containsATerminalState(Set s) {
    Iterator i = s.iterator() ;
    while(i.hasNext()) {
      try {
        State e = (State) i.next() ;
        if (e.isTerminal()) return true ;
      } catch(ClassCastException x) {}
    }
    return false ;
  } 

  public static boolean containsAnInitialState(Set s) {
    Iterator i = s.iterator() ;
    while(i.hasNext()) {
      try {
        State e = (State) i.next() ;
        if (e.isInitial()) return true ;
      } catch(ClassCastException x) {}
    }
    return false ;
  } 
  
  /**
   * Compute the set of states that are reachable ina given automanton
   * from a set of states using epsilon moves.
   * An epsilon transition is a transition which is labelled <code>null</code>.
   * 
   * @param s the set of starting states 
   * @param a the automaton 
   * @return a - possibly empty - set of states reachable from <code>s</code> through
   * epsilon transitions. 
   */
  public static Set<State> epsilonClosure(Set<State> s, Automaton a) {
      Set<State> exp = a.getStateFactory().stateSet();
      exp.addAll(s); /* set of states to visit */
      Set<State> view = a.getStateFactory().stateSet(); /* set of states visited */
      Set<State> arr = a.getStateFactory().stateSet(); /* the set of arrival states */
      arr.addAll(s);
      do {
          Set<State> ns = a.getStateFactory().stateSet();
          ns.addAll(exp); /* arrival states */
          Iterator it = ns.iterator();
          while (it.hasNext()) {
              State st = (State) it.next();
              Iterator it2 = a.delta(st).iterator();
              while (it2.hasNext()) {
                  Transition tr = (Transition) it2.next();
                  if (tr.label() == null && !view.contains(tr.end())
                          && !tr.end().equals(st)) {
                      /* compute closure of epsilon transitions */
                      exp.add(tr.end());
                      arr.add(tr.end());
                  }
              }
              exp.remove(st);
              view.add(st);
          }
      } while (!exp.isEmpty());
      return arr;
  }

  
  /**
   * Compute a map from letters to set of states given 
   * a set of transitions.
   * This method computes the arrival set of states for each letter
   * occuring in a given set of transitions. epsilon transitions 
   * are not taken into account.
   *  
   * @param ts a Set of Transition objects.
   * @return a Map from Object - transition labels - to Set of State objects. 
   */
  public static Map mapAlphabet(Set ts,Automaton a) {
      Map am = new HashMap();
      List tas =new ArrayList(ts);
      /* compute set of states for each letter */
      while (!tas.isEmpty()) {
          Transition tr = (Transition) tas.remove(0);
          Object l = tr.label();
          if (l == null)
              continue;
          Set as = (Set) am.get(l);
          if (as == null) {
              as = a.getStateFactory().stateSet();
              am.put(l, as);
          }
          as.add(tr.end());
      }
      return am;
  }

}
