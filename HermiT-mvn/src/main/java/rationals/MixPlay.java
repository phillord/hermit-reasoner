package rationals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * This class implements an algorithm for finding a synchronizing word given a
 * target letter.
 * 
 * @author bailly
 * @version $Id: MixPlay.java 2 2006-08-24 14:41:48Z oqube $
 */
public class MixPlay implements AutomatonRunner {

    class MixException extends Exception {

        List<Object> word;

        List<StatesTuple> states;

        MixException(List<Object> w, List<StatesTuple> st) {
            this.word = w;
            this.states = st;
        }
    }

    private Set<StatesTuple> explored;

    private final static Random random = new Random();

    private final static Transition[] trmodel = new Transition[0];

    private Object target;

    private List<Automaton> autos;

    private Synchronization sync;

    private Set<?> syncAlphabet;

    private Set<AutomatonRunListener> listeners = new HashSet<>();

    /*
     * current set of states
     */
    private StatesTuple current;

    /**
     * Construct a mix with the given list of automata.
     * 
     * @param autos
     *            a List of automaton objects
     */
    public MixPlay(List<Automaton> autos) {
        this.autos = autos;
        this.sync = new DefaultSynchronization();
    }

    /**
     * Construct an empty mix.
     *
     */
    public MixPlay() {
        this.autos = new ArrayList<>();
        this.sync = new DefaultSynchronization();
    }

    /**
     * Adds a new automaton to this mix.
     * 
     * @param a
     */
    public void addAutomaton(Automaton a) {
        this.autos.add(a);
    }

    /**
     * Reset the state of this mix. The current state is set to the start states
     * of the mixed automata.
     */
    public void reset() {
        this.explored = new HashSet<>();
        this.target = null;
        List<Set<State>> states = new ArrayList<>();
        List<Set<Object>> alphl = new ArrayList<>();
        for (Iterator<Automaton> it = autos.iterator(); it.hasNext();) {
            Automaton a = it.next();
            states.add(a.initials());
            Set<Object> alph = a.alphabet();
            alphl.add(alph);
        }
        /* make synalph */
        this.syncAlphabet = sync.synchronizable(alphl);
        this.current = new StatesTuple(states);
    }

    /**
     * Try to play for given target with given start states in each automaton.
     * 
     * @param t
     *            the targeted letter
     * @return a list of letters ending in <code>target</code>
     */
    public List<Object> play(Object t) {
        this.target = t;
        List<Object> word = new ArrayList<>();
        List<StatesTuple> tuples = new ArrayList<>();
        /* initial states */
        try {
            doPlay(word, tuples, current);
        } catch (MixException mex) {
            /* notify listeners of synchronization */
            notify(mex.word, mex.states);
            return mex.word;
        }
        return new ArrayList<>();
    }

    /**
     * Notify each listener of the fired transitions when a word is found.
     * 
     * @param word
     * @param states
     */
    private void notify(List<Object> word, List<StatesTuple> states) {
        if (listeners.isEmpty() || word.isEmpty() || states.isEmpty())
            return;
        Iterator<Object> wit = word.iterator();
        Iterator<StatesTuple> sit = states.iterator();
        for (; sit.hasNext();) {
            StatesTuple tup = sit.next();
            Object lt = wit.next();
            int ln = tup.sets.size();
            /* fire event */
            for (int i = 0; i < ln; i++) {
                Automaton a = autos.get(i);
                Set<Transition> trans = new HashSet<>();
                for (Iterator<?> stit = tup.sets.get(i).iterator(); stit.hasNext();)
                    trans.addAll(a.delta((State) stit.next(), lt));
                for (Iterator<AutomatonRunListener> lit = listeners.iterator(); lit.hasNext();)
                    lit.next().fire(a, trans, lt);
            }
        }
    }

    /**
     * Recursive play function
     * 
     * @param word
     *            current accumulated word
     * @param tuples
     *            current accumulated list of states tuples
     * @param states
     *            current states tuple
     */
    private void doPlay(List<Object> word, List<StatesTuple> tuples, StatesTuple states) throws MixException {
        /* set current states */
        current = states;
        if (!word.isEmpty() && word.get(word.size() - 1).equals(target))
            throw new MixException(word, tuples);
        /* stop exploring on loop */
        if (explored.contains(states))
            return;
        else
            explored.add(states);
        /* contains already tested transitions */
        Set<Transition> s = new HashSet<>();
        /* list of transitions */
        for (int i = 0; i < states.sets.size(); i++) {
            Transition[] trs = autos.get(i).delta(states.sets.get(i)).toArray(trmodel);
            int ln = trs.length;
            int k = random.nextInt(ln);
            for (int j = 0; j < ln; j++) {
                Transition tr = trs[(k + j) % ln];
                if (s.contains(tr))
                    continue;
                s.add(tr);
                /* check synchronization */
                if (!checkSynchronizableWith(tr.label(), states))
                    continue;
                /* check early rejection */
                if (!checkAccessibleWith(tr.label(), states))
                    continue;
                /* ok - try this transition */
                StatesTuple tup = advanceWith(tr.label(), states);
                /* recurse - an exception is thrown if a match is found */
                word.add(tr.label());
                tuples.add(states);
                // System.err.println("Trying " + word);
                doPlay(word, tuples, tup);
                // System.err.println("No way for " + word);
                word.remove(word.size() - 1);
                tuples.remove(tuples.size() - 1);
            }
        }
    }

    /**
     * Checks synchronization of automaton on this letter
     * 
     * @param object
     * @param states
     * @return
     */
    private boolean checkSynchronizableWith(Object object, StatesTuple states) {
        if (!syncAlphabet.contains(object))
            return true;
        for (int i = 0; i < states.sets.size(); i++) {
            Automaton auto = autos.get(i);
            if (!sync.synchronizeWith(object, auto.alphabet()))
                continue;
            /*
             * compute synchronizable transitions
             */
            Set<?> s = auto.delta(states.sets.get(i));
            Set<State> adv = auto.getStateFactory().stateSet();
            for (Iterator<?> j = s.iterator(); j.hasNext();) {
                Transition tr = (Transition) j.next();
                Object lbl = tr.label();
                if (sync.synchronize(lbl, object) != null)
                    adv.add(tr.end());
            }
            if (adv.isEmpty())
                return false;
        }
        return true;
    }

    /**
     * Checks that, if object is in the alphabet of an automaton, firing of
     * transation does not preclude access of target
     * 
     * @param object
     * @param states
     * @return
     */
    @SuppressWarnings("unused")
    private static boolean checkAccessibleWith(Object object, StatesTuple states) {
        return true;
    }

    /**
     * @param object
     * @param states
     * @return
     */
    private StatesTuple advanceWith(Object object, StatesTuple states) {
        List<Set<State>> nstates = new ArrayList<>();
        for (int i = 0; i < states.sets.size(); i++) {
            Automaton auto = autos.get(i);
            /*
             * compute synchronizable transitions
             */
            Set<?> s = auto.delta(states.sets.get(i));
            Set<State> adv = auto.getStateFactory().stateSet();
            for (Iterator<?> j = s.iterator(); j.hasNext();) {
                Transition tr = (Transition) j.next();
                Object lbl = tr.label();
                if (sync.synchronize(lbl, object) != null)
                    adv.add(tr.end());
            }
            nstates.add(adv.isEmpty() ? states.sets.get(i) : adv);
        }
        return new StatesTuple(nstates);
    }

    @Override
    public void addRunListener(AutomatonRunListener l) {
        listeners.add(l);
    }

    @Override
    public void removeRunListener(AutomatonRunListener l) {
        listeners.remove(l);
    }

    /**
     * 
     * @return
     */
    public Synchronization getSynchronization() {
        return sync;
    }

    /**
     * 
     * @param sync
     */
    public void setSynchronization(Synchronization sync) {
        this.sync = sync;
    }
}
/*
 * Created on Apr 9, 2004
 * 
 * $Log: MixPlay.java,v $ Revision 1.7 2004/08/31 14:16:22 bailly *** empty log
 * message ***
 * 
 * Revision 1.6 2004/04/15 11:51:00 bailly added randomization of MixPlay TODO:
 * check accessibility of synchronization letter
 * 
 * Revision 1.5 2004/04/14 10:02:14 bailly *** empty log message ***
 * 
 * Revision 1.4 2004/04/14 07:33:43 bailly correct version of synchronization on
 * the fly
 * 
 * Revision 1.3 2004/04/13 07:08:38 bailly *** empty log message ***
 * 
 * Revision 1.2 2004/04/12 16:37:59 bailly worked on synchronization algorithm :
 * begins to work but there are still problems with proper implementation of
 * backtracking
 * 
 * Revision 1.1 2004/04/09 15:51:50 bailly Added algorithm for computing a mixed
 * word from several automata (to be verified)
 * 
 */
