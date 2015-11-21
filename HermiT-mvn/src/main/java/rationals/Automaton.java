package rationals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rationals.transformations.TransformationsToolBox;

/**
 * A class defining Automaton objects
 * 
 * This class defines the notion of automaton. Following notations are used to
 * describe this class.
 * <p>
 * An automaton is a 5-uple <em>A = (X , Q , I , T , D)</em> where
 * <ul>
 * <li><em>X</em> is a finite set of labels named alphabet ,
 * <li><em>Q</em> is a finite set of states,
 * <li><em>I</em>, included in <em>Q</em>, is the set of initial states,
 * <li><em>T</em>, included in <em>Q</em>, is the set of terminal states
 * <li>and <em>D</em> is the set of transitions, which is included in
 * <em>Q times X times Q</em> (transitions are triples <em>(q , l , q')</em>
 * where <em>q, q'</em> are states and <em>l</em> a label).
 * </ul>
 * The empty word, usually denoted by <em>epsilon</em> will be denoted here by
 * the symbol <em>@</em>.
 * <p>
 * In this implementation of automaton, any object may be a label, states are
 * instance of class <tt>State</tt> and transitions are intances of class
 * <tt>Transition</tt>. Only automata should create instances of states through
 * <tt>Automaton</tt> method <tt>newState</tt>.
 * 
 * @author yroos@lifl.fr
 * @author bailly@lifl.fr
 * @version $Id: Automaton.java 15 2008-09-20 13:29:35Z oqube $
 * @see Transition State
 */
public class Automaton implements Acceptor, StateMachine, Rational, Cloneable {
    /* the identification of this automaton */
    private Object id;

    protected Builder<?> builder;

    /**
     * @return Returns the id.
     */
    @Override
    public Object getId() {
        return id == null ? "automaton" : id;
    }

    /**
     * @param id
     *            The id to set.
     */
    @Override
    public void setId(Object id) {
        this.id = id;
    }

    // The set of all objects which are labels of
    // transitions of this automaton.
    protected Set<Object> alphabet;

    // The set of all states of this automaton.
    private Set<State> states;

    // the set of initial states
    private Set<State> initials;

    // the set of terminale states
    private Set<State> terminals;

    // Allows acces to transitions of this automaton
    // starting from a given state and labelled by
    // a given object. The keys of this map are instances
    // of class Key and
    // values are sets of transitions.
    private Map<Key, Set<Transition>> transitions;

    // Allows acces to transitions of this automaton
    // arriving to a given state and labelled by
    // a given object. The keys of this map are instances
    // of class Key and
    // values are sets of transitions.
    private Map<Key, Set<Transition>> reverse;

    // bonte
    private StateFactory stateFactory = new DefaultStateFactory(this);

    private Map<Object, State> labels = new HashMap<>();

    @Override
    public StateFactory getStateFactory() {
        return this.stateFactory;
    }

    @Override
    public void setStateFactory(StateFactory factory) {
        this.stateFactory = factory;
        factory.setAutomaton(this);
    }

    /**
     * Returns an automaton which recognizes the regular language associated
     * with the regular expression <em>@</em>, where <em>@</em> denotes the
     * empty word.
     * 
     * @return an automaton which recognizes <em>@</em>
     */
    public static Automaton epsilonAutomaton() {
        Automaton v = new Automaton();
        v.addState(true, true);
        return v;
    }

    /**
     * Returns an automaton which recognizes the regular language associated
     * with the regular expression <em>l</em>, where <em>l</em> is a given
     * label.
     * 
     * @param label
     *            any object that will be used as a label.
     * @return an automaton which recognizes <em>label</em>
     */
    public static Automaton labelAutomaton(Object label) {
        Automaton v = new Automaton();
        State start = v.addState(true, false);
        State end = v.addState(false, true);
        v.addTransition(new Transition(start, label, end), null);
        return v;
    }

    /**
     * Returns an automaton which recognizes the regular language associated
     * with the regular expression <em>u</em>, where <em>u</em> is a given word.
     * 
     * @param word
     *            a List of Object interpreted as a word
     * @return an automaton which recognizes <em>label</em>
     */
    public static Automaton labelAutomaton(List<Object> word) {
        Automaton v = new Automaton();
        State start = null;
        if (word.isEmpty()) {
            v.addState(true, true);
            return v;
        } else
            start = v.addState(true, false);
        State end = null;
        for (Iterator<Object> i = word.iterator(); i.hasNext();) {
            Object o = i.next();
            end = v.addState(false, !i.hasNext());
            v.addTransition(new Transition(start, o, end), null);
            start = end;
        }
        return v;
    }

    /**
     * Creates a new empty automaton which contains no state and no transition.
     * An empty automaton recognizes the empty language.
     */
    public Automaton() {
        this(null);
    }

    /**
     * Create a new empty automaton with given state factory.
     * 
     * @param sf
     *            the StateFactory object to use for creating new states. May be
     *            null.
     */
    public Automaton(StateFactory sf) {
        this.stateFactory = sf == null ? new DefaultStateFactory(this) : sf;
        alphabet = new HashSet<>();
        states = stateFactory.stateSet();
        initials = stateFactory.stateSet();
        terminals = stateFactory.stateSet();
        transitions = new HashMap<>();
        reverse = new HashMap<>();
    }

    @Override
    public State addState(boolean initial, boolean terminal) {

        State state = stateFactory.create(initial, terminal);
        if (initial)
            initials.add(state);
        if (terminal)
            terminals.add(state);
        states.add(state);
        return state;
    }

    @Override
    public Set<Object> alphabet() {
        return alphabet;
    }

    @Override
    public Set<State> states() {
        return states;
    }

    @Override
    public Set<State> initials() {
        return initials;
    }

    @Override
    public Set<State> terminals() {
        return terminals;
    }

    // Computes and return the set of all accessible states, starting
    // from a given set of states and using transitions
    // contained in a given Map
    protected Set<State> access(Set<State> start, Map<Key, Set<Transition>> map) {
        Set<State> current = start;
        Set<State> old;
        do {
            old = current;
            current = stateFactory.stateSet();
            Iterator<State> i = old.iterator();
            while (i.hasNext()) {
                State e = i.next();
                current.add(e);
                Iterator<Object> j = alphabet.iterator();
                while (j.hasNext()) {
                    Iterator<Transition> k = find(map, e, j.next()).iterator();
                    while (k.hasNext()) {
                        current.add(k.next().end());
                    }
                }
            }
        } while (current.size() != old.size());
        return current;
    }

    @Override
    public Set<State> accessibleStates() {
        return access(initials, transitions);
    }

    @Override
    public Set<State> accessibleStates(Set<State> s) {
        return access(s, transitions);
    }

    @Override
    public Set<State> accessibleStates(State state) {
        Set<State> s = stateFactory.stateSet();
        s.add(state);
        return access(s, transitions);
    }

    @Override
    public Set<State> coAccessibleStates(Set<State> s) {
        return access(s, reverse);
    }

    @Override
    public Set<State> coAccessibleStates() {
        return access(terminals, reverse);
    }

    @Override
    public Set<State> accessibleAndCoAccessibleStates() {
        Set<State> ac = accessibleStates();
        ac.retainAll(coAccessibleStates());
        return ac;
    }

    // Computes and return the set of all transitions, starting
    // from a given state and labelled by a given label
    // contained in a given Map
    protected Set<Transition> find(Map<Key, Set<Transition>> m, State e, Object l) {
        Key n = new Key(e, l);
        if (!m.containsKey(n))
            return new HashSet<>();
        return m.get(n);
    }

    // add a given transition in a given Map
    protected void add(Map<Key, Set<Transition>> m, Transition t) {
        Key n = new Key(t.start(), t.label());
        Set<Transition> s;
        if (!m.containsKey(n)) {
            s = new HashSet<>();
            m.put(n, s);
        } else
            s = m.get(n);
        s.add(t);
    }

    @Override
    public Set<Transition> delta() {
        Set<Transition> s = new HashSet<>();
        for (Set<Transition> tr : transitions.values())
            s.addAll(tr);
        return s;
    }

    @Override
    public Set<Transition> delta(State state, Object label) {
        return find(transitions, state, label);
    }

    @Override
    public Set<Transition> deltaFrom(State from, State to) {
        Set<Transition> t = delta(from);
        for (Iterator<Transition> i = t.iterator(); i.hasNext();) {
            Transition tr = i.next();
            if (!to.equals(tr.end()))
                i.remove();
        }
        return t;
    }

    @Override
    public Set<Transition> delta(State state) {
        Set<Transition> s = new HashSet<>();
        for (Object lt : alphabet)
            s.addAll(delta(state, lt));
        return s;
    }

    @Override
    public Set<Transition> delta(Set<State> s) {
        Set<Transition> ds = new HashSet<>();
        for (State st : s)
            ds.addAll(delta(st));
        return ds;
    }

    /**
     * Return a mapping from couples (q,q') of states to all (q,l,q')
     * transitions from q to q'
     * 
     * @return a Map
     */
    public Map<Couple, Set<Transition>> couples() {
        // loop on transition map keys
        Iterator<Map.Entry<Key, Set<Transition>>> it = transitions.entrySet().iterator();
        Map<Couple, Set<Transition>> ret = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry<Key, Set<Transition>> e = it.next();
            // get start and end state
            State st = e.getKey().s;
            Iterator<Transition> trans = e.getValue().iterator();
            while (trans.hasNext()) {
                Transition tr = trans.next();
                State nd = tr.end();
                Couple cpl = new Couple(st, nd);
                Set<Transition> s = ret.get(cpl);
                if (s == null)
                    s = new HashSet<>();
                s.add(tr);
                ret.put(cpl, s);
            }
        }
        return ret;
    }

    /**
     * Returns the set of all transitions of the reverse of this automaton
     * 
     * @return the set of all transitions of the reverse of this automaton. A
     *         reverse of an automaton <em>A = (X , Q , I , T , D)</em> is the
     *         automaton <em>A' = (X , Q , T , I , D')</em> where <em>D'</em> is
     *         the set <em>{ (q , l , q') | (q' , l , q) in D}</em>. Objects
     *         which are contained in this set are instances of class
     *         <tt>Transition</tt>.
     * @see Transition
     */
    @Override
    public Set<Transition> deltaMinusOne(State state, Object label) {
        return find(reverse, state, label);
    }

    @Override
    public boolean addTransition(Transition transition) {
        if (!alphabet.contains(transition.label())) {
            alphabet.add(transition.label());
        }
        add(transitions, transition);
        add(reverse, new Transition(transition.end(), transition.label(), transition.start()));
        return true;
    }

    @Override
    public boolean validTransition(Transition transition) {
        return transition == null || states.contains(transition.start()) && states.contains(transition.end());
    }

    @Override
    public boolean addTransition(Transition transition, String ifInvalid) {
        if (validTransition(transition)) {
            return addTransition(transition);
        }
        if (ifInvalid != null) {
            throw new IllegalArgumentException(ifInvalid);
        }
        return false;
    }

    /**
     * the project method keeps from the Automaton only the transitions labelled
     * with the letters contained in the set alph, effectively computing a
     * projection on this alphabet.
     * 
     * @param alph
     *            the alphabet to project on
     */
    public void projectOn(Set<Object> alph) {
        // remove unwanted transitions from ret
        Iterator<Map.Entry<Key, Set<Transition>>> trans = transitions.entrySet().iterator();
        Set<Transition> newtrans = new HashSet<>();
        while (trans.hasNext()) {
            Map.Entry<Key, Set<Transition>> entry = trans.next();
            Key k = entry.getKey();
            Iterator<Transition> tit = entry.getValue().iterator();
            while (tit.hasNext()) {
                Transition tr = tit.next();
                if (!alph.contains(k.l)) {
                    // create epsilon transition
                    newtrans.add(new Transition(k.s, null, tr.end()));
                    // remove transtion
                    tit.remove();
                }
            }
        }
        // add newly created transitions
        if (!newtrans.isEmpty()) {
            for (Transition tr : newtrans) {
                add(transitions, tr);
                add(reverse, new Transition(tr.end(), tr.label(), tr.start()));
            }
        }
        // remove alphabet
        alphabet.retainAll(alph);
    }

    /**
     * returns a textual representation of this automaton.
     * 
     * @return a textual representation of this automaton based on the converter
     *         <tt>toAscii</tt>.
     * @see rationals.converters.toAscii
     */
    @Override
    public String toString() {
        return new rationals.converters.toAscii().toString(this);
    }

    /**
     * returns a copy of this automaton.
     * 
     * @return a copy of this automaton with new instances of states and
     *         transitions.
     */
    @Override
    public Object clone() {
        Automaton b;
        b = new Automaton();
        Map<State, State> map = new HashMap<>();
        for (State e : states)
            map.put(e, b.addState(e.isInitial(), e.isTerminal()));
        for (Transition t : delta()) {
            b.addTransition(new Transition(map.get(t.start()), t.label(), map.get(t.end())), null);
        }
        return b;
    }

    private class Key {
        State s;

        Object l;

        protected Key(State s, Object l) {
            this.s = s;
            this.l = l;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            try {
                Key t = (Key) o;
                boolean ret = (l == null ? t.l == null : l.equals(t.l)) && (s == null ? t.s == null : s.equals(t.s));
                return ret;
            } catch (ClassCastException x) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int x, y;
            if (s == null)
                x = 0;
            else
                x = s.hashCode();
            if (l == null)
                y = 0;
            else
                y = l.hashCode();
            return y << 16 | x;
        }
    }

    /**
     * Returns true if this automaton accepts given word -- ie. sequence of
     * letters. Note that this method accepts words with letters not in this
     * automaton's alphabet, effectively recognizing all words from any alphabet
     * projected to this alphabet.
     * <p>
     * If you need standard recognition, use
     * 
     * @param word
     * @see{accept(java.util.List)}. @param word
     * @return
     */
    public boolean prefixProjection(List<Object> word) {
        Set<State> s = stepsProject(word);
        return !s.isEmpty();
    }

    /**
     * Return the set of steps this automaton will be in after reading word.
     * Note this method skips letters not in alphabet instead of rejecting them.
     * 
     * @param word
     * @return
     */
    public Set<State> stepsProject(List<Object> word) {
        Set<State> s = initials();
        Iterator<Object> it = word.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (!alphabet.contains(o))
                continue;
            s = step(s, o);
            if (s.isEmpty())
                return s;
        }
        return s;
    }

    @Override
    public boolean accept(List<?> word) {
        Set<State> s = TransformationsToolBox.epsilonClosure(steps(word), this);
        s.retainAll(terminals());
        return !s.isEmpty();
    }

    /**
     * Return true if this automaton can accept the given word starting from
     * given set. <em>Note</em> The ending state(s) need not be terminal for
     * this method to return true.
     * 
     * @param state
     *            a starting state
     * @param word
     *            a List of objects in this automaton's alphabet
     * @return true if there exists a path labelled by word from s to at least
     *         one other state in this automaton.
     */
    public boolean accept(State state, List<Object> word) {
        Set<State> s = stateFactory.stateSet();
        s.add(state);
        return !steps(s, word).isEmpty();
    }

    @Override
    public Set<State> steps(List<?> word) {
        Set<State> s = TransformationsToolBox.epsilonClosure(initials(), this);
        return steps(s, word);
    }

    @Override
    public Set<State> steps(Set<State> s, List<?> word) {
        Iterator<?> it = word.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            s = step(s, o);
            if (s.isEmpty())
                return s;
        }
        return s;
    }

    @Override
    public Set<State> steps(State st, List<?> word) {
        Set<State> s = stateFactory.stateSet();
        s.add(st);
        Iterator<?> it = word.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            s = step(s, o);
            if (s.isEmpty())
                return s;
        }
        return s;
    }

    @Override
    public List<Set<State>> traceStates(List<?> word, State start) {
        List<Set<State>> ret = new ArrayList<>();
        Set<State> s = null;
        if (start != null) {
            s = stateFactory.stateSet();
            s.add(start);
        } else {
            s = initials();
        }
        Iterator<?> it = word.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (!alphabet.contains(o))
                continue;
            s = step(s, o);
            ret.add(s);
            if (s.isEmpty())
                return null;
        }
        return ret;
    }

    /**
     * Returns the size of the longest word recognized by this automaton where
     * letters not belonging to its alphabet are ignored.
     * 
     * 
     * @param word
     * @return
     */
    public int longestPrefixWithProjection(List<?> word) {
        int lret = 0;
        Set<State> s = initials();
        for (Object o : word) {
            if ((o == null) || !alphabet.contains(o)) {
                lret++;
                continue;
            }
            s = step(s, o);
            if (s.isEmpty())
                break;
            lret++;
        }
        return lret;
    }

    @Override
    public Set<State> step(Set<State> s, Object o) {
        Set<State> ns = stateFactory.stateSet();
        Set<State> ec = TransformationsToolBox.epsilonClosure(s, this);
        Iterator<State> it = ec.iterator();
        while (it.hasNext()) {
            State st = it.next();
            Iterator<Transition> it2 = delta(st).iterator();
            while (it2.hasNext()) {
                Transition tr = it2.next();
                if (tr.label() != null && tr.label().equals(o))
                    ns.add(tr.end());
            }
        }
        return ns;
    }

    /**
     * @param tr
     * @param msg
     */
    public void updateTransitionWith(Transition tr, Object msg) {
        Object lbl = tr.label();
        alphabet.remove(lbl);
        alphabet.add(msg);
        /* update transition map */
        Key k = new Key(tr.start(), lbl);
        Set<Transition> s = transitions.remove(k);
        if (s != null)
            transitions.put(new Key(tr.start(), msg), s);
        /* update reverse map */
        k = new Key(tr.end(), lbl);
        s = reverse.remove(k);
        if (s != null)
            reverse.put(new Key(tr.end(), msg), s);
        tr.setLabel(msg);
    }

    @Override
    public Set<Transition> deltaMinusOne(State st) {
        Set<Transition> s = new HashSet<>();
        Iterator<?> alphit = alphabet().iterator();
        while (alphit.hasNext()) {
            s.addAll(deltaMinusOne(st, alphit.next()));
        }
        return s;
    }

    /**
     * Enumerate all prefix of words of length lower or equal than i in this
     * automaton. This method takes exponential time and space to execute: <em>
     * use with care !</em>.
     * 
     * @param ln
     *            maximal length of words.
     * @return a Set of List of Object
     */
    public Set<List<Object>> enumerate(int ln) {
        Set<List<Object>> ret = new HashSet<>();
        class EnumState {
            public EnumState(State s, List<Object> list) {
                st = s;
                word = new ArrayList<>(list);
            }

            State st;

            List<Object> word;
        }

        LinkedList<EnumState> ll = new LinkedList<>();
        List<Object> cur = new ArrayList<>();
        for (Iterator<State> i = initials.iterator(); i.hasNext();) {
            State s = i.next();
            if (s.isTerminal())
                ret.add(new ArrayList<>());
            ll.add(new EnumState(s, cur));
        }

        do {
            EnumState st = ll.removeFirst();
            Set<Transition> trs = delta(st.st);
            List<Object> word = st.word;
            for (Iterator<Transition> k = trs.iterator(); k.hasNext();) {
                Transition tr = k.next();
                word.add(tr.label());
                if (word.size() <= ln) {
                    EnumState en = new EnumState(tr.end(), word);
                    ll.add(en);
                    ret.add(en.word);
                }
                word.remove(word.size() - 1);
            }
        } while (!ll.isEmpty());
        return ret;
    }

    /**
     * Create a new state with given label. The state is created with as neither
     * initial nor terminal.
     * 
     * @param label
     *            the state's label. May not be null.
     * @return the newly created state.
     */
    public State state(Object label) {
        State s = labels.get(label);
        if (s == null) {
            s = stateFactory.create(false, false);
            states.add(s);
            labels.put(label, s);
        }
        return s;
    }

    /**
     * Starts creation of a new transition from the given state. Note that the
     * state is created with given label if it does not exists.
     * 
     * @param o
     *            the label of state to create transition from. may not be null.
     * @return a TransitionBuilder that can be used to create a new transition.
     */
    @SuppressWarnings("unchecked")
    public <T extends Builder<T>> T from(Object o) {
        return (T) builder.build(state(o), this);
    }

    public <T extends Builder<T>> void setBuilder(T t) {
        this.builder = t;
    }
}
