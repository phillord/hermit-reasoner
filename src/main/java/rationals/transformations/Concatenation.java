package rationals.transformations;

import java.util.HashMap;
import java.util.Map;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;
import rationals.properties.ContainsEpsilon;

/**
 * Compute the concatenation of two automata.
 * <ul>
 * <li>C = A . B</li>
 * <li>S(C) = S(A) U S(B)</li>
 * <li>S0(C) =
 * <ul>
 * <li>S0(A), if not A contains epsilon,</li>
 * <li>S0(A) U SO(B), otherwise</li>
 * </ul>
 * </li>
 * <li>T(C) =
 * <ul>
 * <li>T(B), if not B contains epsilon,</li>
 * <li>T(A) U T(B), otherwise</li>
 * </ul>
 * </li>
 * <li>D(C) = D(A) U D(B) U { (s1,a,s2) | (s,a,s2) in D(B), s in S0(B),s1 in
 * T(A) } - {(s,a,s2) in D(B), s in S0(B) }</li>
 * </ul>
 * 
 * @author nono
 * @version $Id: Concatenation.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Concatenation implements BinaryTransformation {

    @Override
    public Automaton transform(Automaton a, Automaton b) {
        Automaton ap = new Normalizer().transform(a);
        Automaton bp = new Normalizer().transform(b);
        ContainsEpsilon ce = new ContainsEpsilon();
        boolean ace = ce.test(a);
        boolean bce = ce.test(b);
        if (ap.states().isEmpty() && ace)
            return b;
        if (bp.states().isEmpty() && bce)
            return a;
        State junc = null; /* junction state */
        Automaton c = new Automaton();
        Map<State, State> map = new HashMap<>();
        /* add all states from ap */
        for (State e : ap.states()) {
            State n;
            if (e.isInitial()) {
                n = c.addState(true, ace && bce);
            } else if (!e.isTerminal())
                n = c.addState(false, e.isTerminal() && bce);
            else
                continue;
            map.put(e, n);
        }
        /* add states from bp */
        for (State e : bp.states()) {
            State n;
            if (!e.isInitial()) {
                n = c.addState(false, e.isTerminal());
                map.put(e, n);
            }
        }
        /* create junction state */
        junc = c.addState(ace, bce);
        for (Transition t : ap.delta()) {
            if (t.end().isTerminal())
                c.addTransition(new Transition(map.get(t.start()), t.label(), junc), null);
            else
                c.addTransition(new Transition(map.get(t.start()), t.label(), map.get(t.end())), null);

        }
        for (Transition t : bp.delta()) {
            if (t.start().isInitial())
                c.addTransition(new Transition(junc, t.label(), map.get(t.end())), null);
            else
                c.addTransition(new Transition(map.get(t.start()), t.label(), map.get(t.end())), null);
        }
        return c;
    }
}