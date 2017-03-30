package rationals.transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import rationals.Automaton;
import rationals.DefaultSynchronization;
import rationals.State;
import rationals.Synchronization;
import rationals.Transition;

/**
 * This class implements the mix - ie: synchronization product - operator
 * between two automatas.
 * <ul>
 * <li>C = A mix B</li>
 * <li>S(C) = { (a,b) | a in S(A) and b in S(B) }</li>
 * <li>S0(C) = (S0(A),SO(B))</li>
 * <li>T(C) = { (a,b) | a in T(A) and b in T(B) }</li>
 * <li>D(C) = { ((s1a,s1b),a,(s2a,s2b)) | exists (s1a,a,s2a) in D(A) and exists
 * (s1b,a,s2b) in D(b) } U { ((s1a,s1b),a,(s1a,s2b)) | a not in S(A) and exists
 * (s1b,a,s2b) in D(b) } U { ((s1a,s1b),a,(s2a,s1b)) | a not in S(B) and exists
 * (s1a,a,s2a) in D(a) }</li>
 * </ul>
 * 
 * @author Arnaud Bailly
 * @version 22032002
 */
public class Mix implements BinaryTransformation {

    private final Synchronization synchronization;

    /**
     * Compute mix of two automata using default synchronization scheme which is
     * the equality of labels.
     * 
     * @see rationals.DefaultSynchronization
     * @see rationals.Synchronization
     */
    public Mix() {
        this.synchronization = new DefaultSynchronization();
    }

    /**
     * Compute mix of two automata using given synchronization scheme.
     * 
     * @param synch
     *            a Synchronization object. Must not be null.
     */
    public Mix(Synchronization synch) {
        this.synchronization = synch;
    }

    @Override
    public Automaton transform(Automaton a, Automaton b) {
        Automaton ret = new Automaton();
        Set<Object> alph = synchronization.synchronizable(a.alphabet(), b.alphabet());
        /* check alphabets */
        Map<StatesCouple, State> amap = new HashMap<>();
        List<StatesCouple> todo = new ArrayList<>();
        Set<StatesCouple> done = new HashSet<>();
        Set<State> as = TransformationsToolBox.epsilonClosure(a.initials(), a);
        Set<State> bs = TransformationsToolBox.epsilonClosure(b.initials(), b);
        State from = ret.addState(true, TransformationsToolBox
                .containsATerminalState(as)
                && TransformationsToolBox.containsATerminalState(bs));
        StatesCouple sc = new StatesCouple(as, bs);
        amap.put(sc, from);
        todo.add(sc);
        do {
            StatesCouple couple = todo.remove(0);
            from = amap.get(couple);
            if (done.contains(couple))
                continue;
            done.add(couple);
            /* get transition sets */
            Map<Object, Set<State>> tam = TransformationsToolBox.mapAlphabet(a.delta(couple.sa), a);
            Map<Object, Set<State>> tbm = TransformationsToolBox.mapAlphabet(b.delta(couple.sb), b);
            /* create label map for synchronized trans */
            Map<Object, StatesCouple> tcm = new HashMap<>();
            /* unsynchronizable transitions in A */
            for (Iterator<Map.Entry<Object, Set<State>>> i = tam.entrySet().iterator(); i.hasNext();) {
                Map.Entry<Object, Set<State>> me = i.next();
                Object l = me.getKey();
                as = me.getValue();
                if (!alph.contains(l)) {
                    Set<State> asc = TransformationsToolBox.epsilonClosure(as, a);
                    tcm.put(l, sc = new StatesCouple(asc, couple.sb));
                    State to = amap.get(sc);
                    if (to == null) {
                        to = ret.addState(false, TransformationsToolBox
                                .containsATerminalState(sc.sa)
                                && TransformationsToolBox
                                        .containsATerminalState(sc.sb));
                        amap.put(sc, to);
                    }
                    todo.add(sc);
                    i.remove();
                }
            }
            /* unsynchronizable transition(s) in B */
            for (Iterator<Map.Entry<Object, Set<State>>> i = tbm.entrySet().iterator(); i.hasNext();) {
                Map.Entry<Object, Set<State>> me =  i.next();
                Object l = me.getKey();
                bs = me.getValue();
                if (!alph.contains(l)) {
                    Set<State> bsc = TransformationsToolBox.epsilonClosure(bs, b);
                    tcm.put(l, sc = new StatesCouple(couple.sa, bsc));
                    State to = amap.get(sc);
                    if (to == null) {
                        to = ret.addState(false, TransformationsToolBox
                                .containsATerminalState(sc.sa)
                                && TransformationsToolBox
                                        .containsATerminalState(sc.sb));
                        amap.put(sc, to);
                    }
                    todo.add(sc);
                    i.remove();
                }
            }
            /*
             * there remains in tam and tbm only possibly synchronizable
             * transitions
             */
            for (Iterator<Entry<Object, Set<State>>> i = tam.entrySet().iterator(); i.hasNext();) {
                Map.Entry<Object, Set<State>> me = i.next();
                Object l = me.getKey();
                as = me.getValue();
                for (Iterator<Entry<Object, Set<State>>> j = tbm.entrySet().iterator(); j.hasNext();) {
                    Map.Entry<Object, Set<State>> mbe = j.next();
                    Object k = mbe.getKey();
                    bs = mbe.getValue();
                    Object sy = synchronization.synchronize(l, k);
                    if (sy != null) {
                        Set<State> asc = TransformationsToolBox.epsilonClosure(as, a);
                        Set<State> bsc = TransformationsToolBox.epsilonClosure(bs, b);
                        tcm.put(sy, sc = new StatesCouple(asc, bsc));
                        State to = amap.get(sc);
                        if (to == null) {
                            to = ret.addState(false, TransformationsToolBox
                                    .containsATerminalState(sc.sa)
                                    && TransformationsToolBox
                                            .containsATerminalState(sc.sb));
                            amap.put(sc, to);
                        }
                        todo.add(sc);
                    }
                }
            }
            /*
             * 
             * create new transitions in return automaton, update maps
             */
            for (Iterator<Entry<Object, StatesCouple>> i = tcm.entrySet().iterator(); i.hasNext();) {
                Entry<Object, StatesCouple> me = i.next();
                Object l = me.getKey();
                sc = me.getValue();
                State to = amap.get(sc);
                if (to == null) {
                    to = ret.addState(false, TransformationsToolBox
                            .containsATerminalState(sc.sa)
                            && TransformationsToolBox
                                    .containsATerminalState(sc.sb));
                    amap.put(sc, to);
                }
                    ret.addTransition(new Transition(from, l, to),null);
            }
        } while (!todo.isEmpty());
        return ret;
    }


}