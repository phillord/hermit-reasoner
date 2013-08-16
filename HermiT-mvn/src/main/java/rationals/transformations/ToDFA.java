package rationals.transformations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

/**
 * Determinization of an automaton.
 * 
 * @author yroos
 * @version $Id: ToDFA.java 7 2006-08-31 23:01:30Z oqube $
 */
public class ToDFA implements UnaryTransformation {
  
  public Automaton transform(Automaton a) {
    Automaton ret = new Automaton();
    Map bmap = new HashMap();
    List /* < Set > */todo = new ArrayList();
    Set /* < Set > */done = new HashSet();
    Set as = TransformationsToolBox.epsilonClosure(a.initials(), a);
    State from = ret.addState(true, TransformationsToolBox
			      .containsATerminalState(as));
    bmap.put(as, from);
    todo.add(as);
    do {
      Set sts = (Set) todo.remove(0);
      from = (State) bmap.get(sts);
      if (done.contains(sts))
	continue;
      done.add(sts);
      /* get transition sets */
      Map tam = TransformationsToolBox.mapAlphabet(a.delta(sts), a);
      /* unsynchronizable transitions in A */
      for (Iterator i = tam.entrySet().iterator(); i.hasNext();) {
	Map.Entry me = (Map.Entry) i.next();
	Object l = me.getKey();
	as = (Set) me.getValue();
	Set asc = TransformationsToolBox.epsilonClosure(as, a);
	State to = (State) bmap.get(asc);
	if (to == null) {
	  to = ret.addState(false, TransformationsToolBox
			    .containsATerminalState(asc));
	  bmap.put(asc, to);
	}
	todo.add(asc);
	try {
	  ret.addTransition(new Transition(from, l, to));
	} catch (NoSuchStateException e) {
	  assert false;
	}
      }
    } while (!todo.isEmpty());
    return ret;
  }

  /*
    public Automaton transform(Automaton a) {
        a = new EpsilonTransitionRemover().transform(a);
        Automaton b = new Automaton();
        Map map = new HashMap();
        LinkedList l = new LinkedList();
        Set done = new HashSet();
        Set e = a.initials();
        boolean t = TransformationsToolBox.containsATerminalState(e);
        map.put(e, b.addState(true, t));
        l.add(e);
        while (!l.isEmpty()) {
            Set e1 = (Set) l.removeFirst();
            done.add(e1);
            State ep1 = (State) map.get(e1);
            Iterator j = a.alphabet().iterator();
            Object label = null;
            while (j.hasNext()) {
                label = j.next();
                Iterator i = e1.iterator();
                Set e2 = a.getStateFactory().stateSet();
                while (i.hasNext()) {
                    Iterator k = a.delta((State) i.next(), label).iterator();
                    while (k.hasNext()) {
                        e2.add(((Transition) k.next()).end());
                    }
                }
                State ep2;
                if (!e2.isEmpty()) {
                    if (!map.containsKey(e2)) {
                        t = TransformationsToolBox.containsATerminalState(e2);
                        map.put(e2, b.addState(false, t));
                    }
                    ep2 = (State) map.get(e2);
                    try {
                        b.addTransition(new Transition(ep1, label, ep2));
                    } catch (NoSuchStateException x) {
                    }
                    if (!done.contains(e2))
                        l.add(e2);
                }
            }
        }
        return b;
    }
  */
}
