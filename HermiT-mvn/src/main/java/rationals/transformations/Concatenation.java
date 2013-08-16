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

    public Automaton transform(Automaton a, Automaton b) {
        Automaton ap = new Normalizer().transform(a);
        Automaton bp = new Normalizer().transform(b);
        ContainsEpsilon ce = new ContainsEpsilon();
        boolean ace = ce.test(a);
        boolean bce = ce.test(b);
        if (ap.states().size() == 0 && ace)
            return b;
        if (bp.states().size() == 0 && bce)
            return a;
        State junc = null; /* junction state */
        Automaton c = new Automaton();
        Map map = new HashMap();
        /* add all states from ap */
        Iterator i = ap.states().iterator();
        while (i.hasNext()) {
            State e = (State) i.next();
            State n;
            if (e.isInitial()) {
                n = c.addState(true, ace && bce);
            } else if(!e.isTerminal())
                n = c.addState(false, e.isTerminal() && bce);
            else
                continue;
            map.put(e, n);
        }
        /* add states from bp */
        i = bp.states().iterator();
        while (i.hasNext()) {
            State e = (State) i.next();
            State n;
            if (!e.isInitial())  {
                n = c.addState(false, e.isTerminal());
                map.put(e, n);
            }
        }
        /* create junction state */
        junc = c.addState(ace,bce);
        i = ap.delta().iterator();
        while (i.hasNext()) {
            Transition t = (Transition) i.next();
            try {
                if (t.end().isTerminal())
                    c.addTransition(new Transition((State) map.get(t.start()),
                            t.label(), junc));
                else
                    c.addTransition(new Transition((State) map.get(t.start()),
                            t.label(), (State) map.get(t.end())));
            } catch (NoSuchStateException x) {
            }

        }
        i = bp.delta().iterator();
        while (i.hasNext()) {
            Transition t = (Transition) i.next();
            try {
                if (t.start().isInitial())
                    c.addTransition(new Transition(junc, t.label(), (State) map
                            .get(t.end())));
                else
                    c.addTransition(new Transition((State) map.get(t.start()),
                            t.label(), (State) map.get(t.end())));
            } catch (NoSuchStateException x) {
            }
        }
        return c;
    }
}