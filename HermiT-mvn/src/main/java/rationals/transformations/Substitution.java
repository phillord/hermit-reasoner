/**
 * 
 */
package rationals.transformations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

/**
 * A (rational) substitution is a morphism that maps letters to languages. A
 * rational substitution is constructed like a {@link Morphism} with an instance
 * of {@link Map} that has Object as keys and either Object or Automaton
 * instances as values. If the value of a key is an object, then it is
 * considered a letter and this class acts like a morphism. If the value of the
 * key is an Automaton, then the complete transition is replaced by the language
 * denoted by the automaton.
 * 
 * @author nono
 * @see J.Berstel, "Transductions and context-free languages"
 */
public class Substitution implements UnaryTransformation {

    private Map<Object, Object> morph;

    public Substitution(Map<Object, Object> m) {
        this.morph = m;
    }

    @Override
    public Automaton transform(Automaton a) {
        Automaton b = new Automaton();
        /* state map */
        Map<State, State> stm = new HashMap<>();
        for (Iterator<Transition> i = a.delta().iterator(); i.hasNext();) {
            Transition tr = i.next();
            State ns = tr.start();
            State nss = stm.get(ns);
            if (nss == null) {
                nss = b.addState(ns.isInitial(), ns.isTerminal());
                stm.put(ns, nss);
            }
            State ne = tr.end();
            State nse = stm.get(ne);
            if (nse == null) {
                nse = b.addState(ne.isInitial(), ne.isTerminal());
                stm.put(ne, nse);
            }
            Object lbl = tr.label();
            if (!morph.containsKey(lbl))
                b.addTransition(new Transition(nss, lbl, nse), null);
            else {
                /* is value an automaton ? */
                Object o = morph.get(lbl);
                if (o instanceof Automaton)
                    insert(nss, nse, b, (Automaton) o);
                else
                    b.addTransition(new Transition(nss, morph.get(lbl), nse), null);
            }
        }
        return b;
    }

    /**
     * Insert <code>automaton</code> between states <code>nss</code> and
     * <code>nse</code> in automaton <code>b</code>. This method add epsilon
     * transitions from <code>nss</code> to each starting state of automaton and
     * from each ending state to <code>nse</code>.
     * 
     * @param nss
     * @param nse
     * @param b
     * @param automaton
     */
    private void insert(State nss, State nse, Automaton b, Automaton automaton) {
        /* map states */
        Map<State, State> map = new HashMap<>();
        for (Iterator<State> i = automaton.states().iterator(); i.hasNext();) {
            State e = i.next();
            State n = b.addState(false, false);
            map.put(e, n);
            if (e.isInitial())
                b.addTransition(new Transition(nss, null, n), null);
            if (e.isTerminal())
                b.addTransition(new Transition(n, null, nse), null);

        }
        for (Iterator<Transition> i = automaton.delta().iterator(); i.hasNext();) {
            Transition t = i.next();
            b.addTransition(new Transition(map.get(t.start()), t.label(), map.get(t.end())), null);
        }

    }

}
