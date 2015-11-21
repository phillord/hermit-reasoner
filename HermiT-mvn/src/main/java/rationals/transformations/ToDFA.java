package rationals.transformations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * Determinization of an automaton.
 * 
 * @author yroos
 * @version $Id: ToDFA.java 7 2006-08-31 23:01:30Z oqube $
 */
public class ToDFA implements UnaryTransformation {
  
  @Override
public Automaton transform(Automaton a) {
    Automaton ret = new Automaton();
    Map<Set<State>, State> bmap = new HashMap<>();
    List<Set<State>> todo = new ArrayList<>();
    Set<Set<State>> done = new HashSet<>();
    Set<State> as = TransformationsToolBox.epsilonClosure(a.initials(), a);
    State from = ret.addState(true, TransformationsToolBox
                  .containsATerminalState(as));
    bmap.put(as, from);
    todo.add(as);
    do {
      Set<State> sts = todo.remove(0);
      from = bmap.get(sts);
      if (done.contains(sts))
    continue;
      done.add(sts);
      /* get transition sets */
      Map<Object, Set<State>> tam = TransformationsToolBox.mapAlphabet(a.delta(sts), a);
      /* unsynchronizable transitions in A */
      for (Iterator<Map.Entry<Object, Set<State>>> i = tam.entrySet().iterator(); i.hasNext();) {
    Map.Entry<Object, Set<State>> me = i.next();
    Object l = me.getKey();
    as = me.getValue();
    Set<State> asc = TransformationsToolBox.epsilonClosure(as, a);
    State to = bmap.get(asc);
    if (to == null) {
      to = ret.addState(false, TransformationsToolBox
                .containsATerminalState(asc));
      bmap.put(asc, to);
    }
    todo.add(asc);
      boolean valid=ret.addTransition(new Transition(from, l, to),null);
      assert valid;
      }
    } while (!todo.isEmpty());
    return ret;
  }
}
