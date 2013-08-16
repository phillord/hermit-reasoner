package rationals.transformations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

/**
 * Compute the kleene-star closure of an automaton.
 * 
 * @author nono
 * @version $Id: Star.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Star implements UnaryTransformation {
    public Automaton transform(Automaton a) {
        if (a.delta().size() == 0)
            return Automaton.epsilonAutomaton();
        Automaton b = new Automaton();
        State ni = b.addState(true, true);
        State nt = b.addState(true, true);
        Map map = new HashMap();
        Iterator i = a.states().iterator();
        while (i.hasNext()) {
            map.put(i.next(), b.addState(false, false));
        }
        i = a.delta().iterator();
        while (i.hasNext()) {
            Transition t = (Transition) i.next();
            try {
                b.addTransition(new Transition((State) map.get(t.start()), t
                        .label(), (State) map.get(t.end())));
            } catch (NoSuchStateException x) {
            }
            if (t.start().isInitial() && t.end().isTerminal()) {
                try {
                    b.addTransition(new Transition(ni, t.label(), nt));
                    b.addTransition(new Transition(nt, t.label(), ni));
                } catch (NoSuchStateException x) {
                }
            } else if (t.start().isInitial()) {
                try {
                    b.addTransition(new Transition(ni, t.label(), (State) map
                            .get(t.end())));
                    b.addTransition(new Transition(nt, t.label(), (State) map
                            .get(t.end())));
                } catch (NoSuchStateException x) {
                }
            } else if (t.end().isTerminal()) {
                try {
                    b.addTransition(new Transition((State) map.get(t.start()),
                            t.label(), nt));
                    b.addTransition(new Transition((State) map.get(t.start()),
                            t.label(), ni));
                } catch (NoSuchStateException x) {
                }
            }
        }
        return b;
    }
}