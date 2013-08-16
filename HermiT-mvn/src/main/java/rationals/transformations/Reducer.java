package rationals.transformations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

/**
 * Computes the minimal automaton from a deterministic automaton.
 * <p />
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
    private boolean same(State e1, State e2, Automaton a, Map m) {
        if (!m.get(e1).equals(m.get(e2)))
            return false;
        /* iterate over all transitions */
        Set tas = a.delta(e1);
        Set tbs = a.delta(e2);
        Iterator it = tas.iterator();
        while (it.hasNext()) {
            Transition tr = (Transition) it.next();
            State ep1 = tr.end();
            /* check transition exists in b */
            Set tbsl = a.delta(e2, tr.label());
            if (tbsl.isEmpty())
                return false;
            Iterator trb = tbsl.iterator();
            while (trb.hasNext()) {
                Transition tb = (Transition) trb.next();
                /* mark transition as visited */
                tbs.remove(tb);
                State ep2 = tb.end();
                if (!m.get(ep1).equals(m.get(ep2)))
                    return false;
            }
            
        }
        if (!tbs.isEmpty()) {
            return false;
        }
        return true;
    }

    public Automaton transform(Automaton a) {
        Automaton b = new ToDFA().transform(a);
        Map current = new HashMap();
        Set s1 = b.getStateFactory().stateSet();
        Set s2 = b.getStateFactory().stateSet();
        Iterator i = b.states().iterator();
        while (i.hasNext()) {
            State e = (State) i.next();
            if (e.isTerminal()) {
                s1.add(e);
                current.put(e, s1);
            } else {
                s2.add(e);
                current.put(e, s2);
            }
        }
        Map old;
        do {
            old = current;
            current = new HashMap();
            i = old.keySet().iterator();
            while (i.hasNext()) {
                State e1 = (State) i.next();
                Set s = b.getStateFactory().stateSet();
                Iterator j = current.keySet().iterator();
                while (j.hasNext()) {
                    State e2 = (State) j.next();
                    if (same(e1, e2, b, old)) {
                        s = (Set) current.get(e2);
                        break;
                    }
                }
                s.add(e1);
                current.put(e1, s);
            }
        } while (!new HashSet(current.values())
                .equals(new HashSet(old.values())));
        Automaton c = new Automaton();
        Set setSet = new HashSet(current.values());
        Iterator sets = setSet.iterator();
        Map newStates = new HashMap();
        while (sets.hasNext()) {
            Set set = (Set) sets.next();
            boolean term = TransformationsToolBox.containsATerminalState(set);
            boolean init = TransformationsToolBox.containsAnInitialState(set);
            newStates.put(set, c.addState(init, term));
        }
        sets = setSet.iterator();
        while (sets.hasNext()) {
            Set set = (Set) sets.next();
            State r = (State) set.iterator().next();
            State rp = (State) newStates.get(set);
            Iterator k = b.alphabet().iterator();
            while (k.hasNext()) {
                Object l = k.next();
                Set ds = b.delta(r, l);
                if(ds.isEmpty())
                    continue;
                State f = (State) ((Transition) ds.iterator().next())
                        .end();
                State fp = (State) newStates.get(current.get(f));
                try {
                    c.addTransition(new Transition(rp, l, fp));
                } catch (NoSuchStateException x) {
                }
            }
        }
        return c;
    }

}