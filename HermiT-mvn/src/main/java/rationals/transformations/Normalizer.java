package rationals.transformations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import rationals.Automaton;
import rationals.NoSuchStateException;
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

    public Automaton transform(Automaton a) {
        Automaton b = new Automaton();
        State ni = b.addState(true, false);
        State nt = b.addState(false, true);
        Map map = new HashMap();
        Iterator i = a.states().iterator();
        while (i.hasNext()) {
            State st = (State) i.next();
            map.put(st, b.addState(false, false));
        }
        /* add epsilon transition if contains epsilon */
        if (new ContainsEpsilon().test(a))
            try {
                b.addTransition(new Transition(ni, null, nt));
            } catch (NoSuchStateException e) {
            }
        i = a.delta().iterator();
        while (i.hasNext()) {
            Transition t = (Transition) i.next();
            if (t.start().isInitial() && t.end().isTerminal()) {
                try {
                    b.addTransition(new Transition(ni, t.label(), nt));
                } catch (NoSuchStateException x) {
                }
            }
            if (t.start().isInitial()) {
                try {
                    b.addTransition(new Transition(ni, t.label(), (State) map
                            .get(t.end())));
                } catch (NoSuchStateException x) {
                }
            }

            if (t.end().isTerminal())
                try {
                    b.addTransition(new Transition((State) map.get(t.start()),
                            t.label(), nt));
                } catch (NoSuchStateException x) {
                }

            try {
                b.addTransition(new Transition((State) map.get(t.start()), t
                        .label(), (State) map.get(t.end())));
            } catch (NoSuchStateException x) {
            }

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