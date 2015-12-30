package rationals.transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * This class allows to remove epsilon transitions in an automaton. Epsilon
 * transition are transitions (q , l , q') where l is null.
 * 
 * @author Yves Roos
 * @version 22032002
 */
public class EpsilonTransitionRemover implements UnaryTransformation {

    @Override
    public Automaton transform(Automaton a) {
        Automaton ret = new Automaton(); /* resulting automaton */
        Map<HashValue, State> sm = new HashMap<>();
        Set<HashValue> done = new HashSet<>();
        List<HashValue> todo = new ArrayList<>(); /* set of states to explore */
        Set<State> cur = TransformationsToolBox.epsilonClosure(a.initials(), a);
        /* add cur as initial state of ret */
        State is = ret.addState(true,TransformationsToolBox.containsATerminalState(cur));
        HashValue hv = new HashValue(cur);
        sm.put(hv,is);
        todo.add(hv);
        do {
            HashValue s = todo.remove(0);
            State ns =  sm.get(s);
            if(ns == null) {
                ns = ret.addState(false,TransformationsToolBox.containsATerminalState(s.s));
                sm.put(s,ns);
            }
            /* set s as explored */
            done.add(s);
            /* look for all transitions in s */
            Map <Object, Set<State>> trm = instructions(a.delta(s.s),a);
            for (Map.Entry<Object, Set<State>> e :trm.entrySet()){
                Object o = e.getKey();
                Set<State> ar = e.getValue();
                /* compute closure of arrival set */
                ar = TransformationsToolBox.epsilonClosure(ar, a);
                hv = new HashValue(ar);
                /* retrieve state in new automaton from hashvalue */
                State ne = sm.get(hv);
                if(ne == null) {
                    ne = ret.addState(false,TransformationsToolBox.containsATerminalState(ar));
                    sm.put(hv,ne);
                }
                    /* create transition */
                    ret.addTransition(new Transition(ns,o,ne),null);
                /* explore new state */
                if(!done.contains(hv))
                    todo.add(hv);
            }
        } while (!todo.isEmpty());
        return ret;
    }

    private static Map<Object, Set<State>> instructions(Set<Transition> s,Automaton a) {
        Map<Object, Set<State>> m = new HashMap<>();
        Iterator<Transition> it = s.iterator();
        while (it.hasNext()) {
            Transition tr = it.next();
            Object l = tr.label();
            if (l != null) {
                Set<State> st = m.get(l);
                if (st == null) {
                    st = a.getStateFactory().stateSet();
                    m.put(l,st);
                }
                /* add arrival state */
                st.add(tr.end());
            }
        }
        return m;
    }

}

