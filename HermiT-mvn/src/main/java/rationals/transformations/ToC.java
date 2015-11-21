package rationals.transformations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

public class ToC implements UnaryTransformation {

    @Override
    public Automaton transform(Automaton a) {
        Automaton b = new EpsilonTransitionRemover().transform(a);
        Set<State> Ib = b.initials();
        Map<Set<State>, Set<State>> subsets = new HashMap<>();
        Map<Set<State>, Map<Object, Set<State>>> delta = new HashMap<>();
        LinkedList<Set<State>> l = new LinkedList<>();
        Set<Set<State>> done = new HashSet<>();
        l.add(Ib);
        while (!l.isEmpty()) {
            Set<State> e1 = l.removeFirst();
            Set<State> sub = b.getStateFactory().stateSet();
            subsets.put(e1, sub);
            Iterator<Set<State>> j = done.iterator();
            while (j.hasNext()) {
                Set<State> x = j.next();
                if (x.containsAll(e1) && !x.equals(e1))
                    subsets.get(x).addAll(e1);
                if (e1.containsAll(x) && !x.equals(e1))
                    sub.addAll(x);
            }
            done.add(e1);
            delta.put(e1, new HashMap<Object, Set<State>>());
            for (Object label : b.alphabet()) {
                Iterator<State> i = e1.iterator();
                Set<State> e2 = b.getStateFactory().stateSet();
                while (i.hasNext()) {
                    Iterator<Transition> k = b.delta(i.next(), label).iterator();
                    while (k.hasNext()) {
                        e2.add(k.next().end());
                    }
                }
                delta.get(e1).put(label, e2);
                if (!done.contains(e2))
                    l.add(e2);
            }
        }
        Automaton c = new Automaton();
        Map<Set<State>, State> corr = new HashMap<>();
        Iterator<Set<State>> i = done.iterator();
        while (i.hasNext()) {
            Set<State> x = i.next();
            if (!x.isEmpty()) {
                if (!subsets.get(x).containsAll(x)) {
                    boolean ini = Ib.containsAll(x);
                    boolean term = TransformationsToolBox
                            .containsATerminalState(x);
                    corr.put(x, c.addState(ini, term));
                }
            }
        }
        i = corr.keySet().iterator();
        while (i.hasNext()) {
            Set<State> e = i.next();
            Iterator<Object> j = b.alphabet().iterator();
            while (j.hasNext()) {
                Object lab = j.next();
                Set<State> f = delta.get(e).get(lab);
                Iterator<Set<State>> k = corr.keySet().iterator();
                while (k.hasNext()) {
                    Set<State> x = k.next();
                    if (f.containsAll(x)) {
                            c.addTransition(new Transition(corr.get(e),
                                    lab, corr.get(x)),null);
                    }
                }
            }
        }
        return c;
    }
}
