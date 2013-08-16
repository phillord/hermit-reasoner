package rationals.transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rationals.Automaton;
import rationals.DefaultSynchronization;
import rationals.NoSuchStateException;
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

    private Synchronization synchronization;

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

    /*
     *  (non-Javadoc)
     * @see rationals.transformations.BinaryTransformation#transform(rationals.Automaton, rationals.Automaton)
     */
    public Automaton transform(Automaton a, Automaton b) {
        Automaton ret = new Automaton();
        Set alph = synchronization.synchronizable(a.alphabet(), b.alphabet());
        /* check alphabets */
        Map amap = new HashMap();
        Map bmap = new HashMap();
        List /* < StatesCouple > */todo = new ArrayList();
        Set /* < StatesCouple > */done = new HashSet();
        Set as = TransformationsToolBox.epsilonClosure(a.initials(), a);
        Set bs = TransformationsToolBox.epsilonClosure(b.initials(), b);
        State from = ret.addState(true, TransformationsToolBox
                .containsATerminalState(as)
                && TransformationsToolBox.containsATerminalState(bs));
        StatesCouple sc = new StatesCouple(as, bs);
        amap.put(sc, from);
        todo.add(sc);
        do {
            StatesCouple couple = (StatesCouple) todo.remove(0);
            from = (State) amap.get(couple);
            if (done.contains(couple))
                continue;
            done.add(couple);
            /* get transition sets */
            Map tam = TransformationsToolBox.mapAlphabet(a.delta(couple.sa), a);
            Map tbm = TransformationsToolBox.mapAlphabet(b.delta(couple.sb), b);
            /* create label map for synchronized trans */
            Map /* < Object, StatesCouple > */tcm = new HashMap();
            /* unsynchronizable transitions in A */
            for (Iterator i = tam.entrySet().iterator(); i.hasNext();) {
                Map.Entry me = (Map.Entry) i.next();
                Object l = me.getKey();
                as = (Set) me.getValue();
                if (!alph.contains(l)) {
                    Set asc = TransformationsToolBox.epsilonClosure(as, a);
                    tcm.put(l, sc = new StatesCouple(asc, couple.sb));
                    State to = (State) amap.get(sc);
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
            for (Iterator i = tbm.entrySet().iterator(); i.hasNext();) {
                Map.Entry me = (Map.Entry) i.next();
                Object l = me.getKey();
                bs = (Set) me.getValue();
                if (!alph.contains(l)) {
                    Set bsc = TransformationsToolBox.epsilonClosure(bs, b);
                    tcm.put(l, sc = new StatesCouple(couple.sa, bsc));
                    State to = (State) amap.get(sc);
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
            for (Iterator i = tam.entrySet().iterator(); i.hasNext();) {
                Map.Entry me = (Map.Entry) i.next();
                Object l = me.getKey();
                as = (Set) me.getValue();
                for (Iterator j = tbm.entrySet().iterator(); j.hasNext();) {
                    Map.Entry mbe = (Map.Entry) j.next();
                    Object k = mbe.getKey();
                    bs = (Set) mbe.getValue();
                    Object sy = synchronization.synchronize(l, k);
                    if (sy != null) {
                        Set asc = TransformationsToolBox.epsilonClosure(as, a);
                        Set bsc = TransformationsToolBox.epsilonClosure(bs, b);
                        tcm.put(sy, sc = new StatesCouple(asc, bsc));
                        State to = (State) amap.get(sc);
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
            for (Iterator i = tcm.entrySet().iterator(); i.hasNext();) {
                Map.Entry me = (Map.Entry) i.next();
                Object l = me.getKey();
                sc = (StatesCouple) me.getValue();
                State to = (State) amap.get(sc);
                if (to == null) {
                    to = ret.addState(false, TransformationsToolBox
                            .containsATerminalState(sc.sa)
                            && TransformationsToolBox
                                    .containsATerminalState(sc.sb));
                    amap.put(sc, to);
                }
                try {
                    ret.addTransition(new Transition(from, l, to));
                } catch (NoSuchStateException e) {
                }
            }
        } while (!todo.isEmpty());
        return ret;
    }


}