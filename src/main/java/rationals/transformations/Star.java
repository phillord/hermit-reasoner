package rationals.transformations;

import java.util.HashMap;
import java.util.Map;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * Compute the kleene-star closure of an automaton.
 * 
 * @author nono
 * @version $Id: Star.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Star implements UnaryTransformation {
    @Override
    public Automaton transform(Automaton a) {
        if (a.delta().size() == 0)
            return Automaton.epsilonAutomaton();
        Automaton b = new Automaton();
        State ni = b.addState(true, true);
        State nt = b.addState(true, true);
        Map<State, State> map = new HashMap<>();
        for (State i : a.states()) {
            map.put(i, b.addState(false, false));
        }
        for(    Transition t :a.delta()) {
                b.addTransition(new Transition(map.get(t.start()), t
                        .label(), map.get(t.end())),null);
            if (t.start().isInitial() && t.end().isTerminal()) {
                    b.addTransition(new Transition(ni, t.label(), nt),null);
                    b.addTransition(new Transition(nt, t.label(), ni),null);
            } else if (t.start().isInitial()) {
                    b.addTransition(new Transition(ni, t.label(), map
                            .get(t.end())),null);
                    b.addTransition(new Transition(nt, t.label(), map
                            .get(t.end())),null);
            } else if (t.end().isTerminal()) {
                    b.addTransition(new Transition(map.get(t.start()),
                            t.label(), nt),null);
                    b.addTransition(new Transition(map.get(t.start()),
                            t.label(), ni),null);
            }
        }
        return b;
    }
}