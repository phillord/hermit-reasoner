package rationals.transformations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * Computes the minimal automaton from a deterministic automaton.
 * <br>
 * This class first determinizes the transformed automaton, then compute
 * states equivalence classes to create new states and transitions.
 * 
 * @author nono
 * @version $Id: Reducer.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Reducer implements UnaryTransformation {

    /*
     * equivalence on DFA
     */
    private static boolean same(State e1, State e2, Automaton a, Map<State, Set<State>> m) {
        if (!m.get(e1).equals(m.get(e2)))
            return false;
        /* iterate over all transitions */
        Set<Transition> tas = a.delta(e1);
        Set<Transition> tbs = a.delta(e2);
        Iterator<Transition> it = tas.iterator();
        while (it.hasNext()) {
            Transition tr = it.next();
            State ep1 = tr.end();
            /* check transition exists in b */
            Set<Transition> tbsl = a.delta(e2, tr.label());
            if (tbsl.isEmpty())
                return false;
            Iterator<Transition> trb = tbsl.iterator();
            while (trb.hasNext()) {
                Transition tb = trb.next();
                /* mark transition as visited */
                tbs.remove(tb);
                State ep2 = tb.end();
                if (!m.get(ep1).equals(m.get(ep2)))
                    return false;
            }
            
        }
        return tbs.isEmpty();
    }

    @Override
    public Automaton transform(Automaton a) {
        Automaton b = new ToDFA().transform(a);
        Map<State, Set<State>> current = new HashMap<>();
        Set<State> s1 = b.getStateFactory().stateSet();
        Set<State> s2 = b.getStateFactory().stateSet();
        Iterator<State> i = b.states().iterator();
        while (i.hasNext()) {
            State e = i.next();
            if (e.isTerminal()) {
                s1.add(e);
                current.put(e, s1);
            } else {
                s2.add(e);
                current.put(e, s2);
            }
        }
        Map<State, Set<State>> old;
        do {
            old = current;
            current = new HashMap<>();
            i = old.keySet().iterator();
            while (i.hasNext()) {
                State e1 = i.next();
                Set<State> s = b.getStateFactory().stateSet();
                Iterator<State> j = current.keySet().iterator();
                while (j.hasNext()) {
                    State e2 = j.next();
                    if (same(e1, e2, b, old)) {
                        s = current.get(e2);
                        break;
                    }
                }
                s.add(e1);
                current.put(e1, s);
            }
        } while (!new HashSet<>(current.values())
                .equals(new HashSet<>(old.values())));
        Automaton c = new Automaton();
        Set<Set<State>> setSet = new HashSet<>(current.values());
        Iterator<Set<State>> sets = setSet.iterator();
        Map<Set<State>, State> newStates = new HashMap<>();
        while (sets.hasNext()) {
            Set<State> set = sets.next();
            boolean term = TransformationsToolBox.containsATerminalState(set);
            boolean init = TransformationsToolBox.containsAnInitialState(set);
            newStates.put(set, c.addState(init, term));
        }
        sets = setSet.iterator();
        while (sets.hasNext()) {
            Set<State> set = sets.next();
            State r = set.iterator().next();
            State rp = newStates.get(set);
            Iterator<Object> k = b.alphabet().iterator();
            while (k.hasNext()) {
                Object l = k.next();
                Set<Transition> ds = b.delta(r, l);
                if(ds.isEmpty())
                    continue;
                State f = ds.iterator().next()
                        .end();
                State fp = newStates.get(current.get(f));
                    c.addTransition(new Transition(rp, l, fp),null);
            }
        }
        return c;
    }

}