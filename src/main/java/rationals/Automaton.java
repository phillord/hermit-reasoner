package rationals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

    // The set of all objects which are labels of
    // transitions of this automaton.
    protected final Set<Object> alphabet;

    // The set of all states of this automaton.
    private final Set<State> states;

    // the set of initial states
    private final Set<State> initials;

    // the set of terminale states
    private final Set<State> terminals;

    // Allows acces to transitions of this automaton
    // starting from a given state and labelled by
    // a given object. The keys of this map are instances
    // of class Key and
    // values are sets of transitions.
    private final Map<Key, Set<Transition>> transitions;

    // Allows acces to transitions of this automaton
    // arriving to a given state and labelled by
    // a given object. The keys of this map are instances
    // of class Key and
    // values are sets of transitions.
    private final Map<Key, Set<Transition>> reverse;

    // bonte
    private final StateFactory stateFactory;

    private final Map<Object, State> labels = new HashMap<>();

    @Override
    public StateFactory getStateFactory() {
        return this.stateFactory;
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
        Automaton b = new Automaton();
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
            if (o == this) {
                return true;
            }
            if (o == null || !(o instanceof Key))
                return false;
            Key t = (Key) o;
            return (l == null ? t.l == null : l.equals(t.l)) && (s == null ? t.s == null : s.equals(t.s));
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
}
