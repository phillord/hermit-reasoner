package rationals.transformations;

import java.util.HashMap;
import java.util.Map;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;
import rationals.properties.ContainsEpsilon;

/**
 * A transformation that normalizes a given Automaton.
 * <p>
 * This transformation produces a new Automaton with the following features :
 * <ol>
 * <li>it has <em>one</em> start state and <em>one</em> end state,</li>
 * <li>there is no incoming (resp. outgoing) transitions to (resp. from) the
 * start (resp. end) state,</li>
 * <li>the resultant automaton is then pruned ({@link Pruner}) to remove
 * inaccessible states.</li>
 * </ol>
 * 
 * @author yroos
 * @version $Id: Normalizer.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Normalizer implements UnaryTransformation {

    @Override
    public Automaton transform(Automaton a) {
        Automaton b = new Automaton();
        State ni = b.addState(true, false);
        State nt = b.addState(false, true);
        Map<State, State> map = new HashMap<>();
        for (State st :a.states()) {
            map.put(st, b.addState(false, false));
        }
        /* add epsilon transition if contains epsilon */
        if (new ContainsEpsilon().test(a))
                b.addTransition(new Transition(ni, null, nt),null);
        for (Transition t :a.delta()) {
            if (t.start().isInitial() && t.end().isTerminal()) {
                    b.addTransition(new Transition(ni, t.label(), nt),null);
            }
            if (t.start().isInitial()) {
                    b.addTransition(new Transition(ni, t.label(), map
                            .get(t.end())),null);
            }

            if (t.end().isTerminal())
                    b.addTransition(new Transition(map.get(t.start()),
                            t.label(), nt),null);

                b.addTransition(new Transition(map.get(t.start()), t
                        .label(), map.get(t.end())),null);

        }
        b = new Pruner().transform(b);
        return b;
    }
}

/*
 * $Log: Normalizer.java,v $ Revision 1.4 2005/02/20 21:14:19 bailly added API
 * for computing equivalence relations on automata
 * 
 * Revision 1.3 2004/09/21 11:50:28 bailly added interface BinaryTest added
 * class for testing automaton equivalence (isomorphism of normalized automata)
 * added computation of RE from Automaton
 *  
 */