// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import org.semanticweb.HermiT.model.AbstractRole;
import org.semanticweb.HermiT.model.AtomicAbstractRole;
import org.semanticweb.HermiT.model.InverseAbstractRole;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;

public class RoleBox {
    static public class NotRegularException extends IllegalArgumentException {
        NotRegularException() {
            super("The given role axioms are not regular; no suitable ordering is possible.");
        }
    }
    
    protected Map<AbstractRole, AbstractRole> m_canonicalNames;
    protected Set<AbstractRole> m_symmetricRoles;
    
    protected Map<Integer, Map<Integer, Set<AbstractRole>>> m_transitions;
    protected Map<AbstractRole, Integer> m_initialStates;
    protected Set<Integer> m_acceptingStates;
    
    protected AbstractRole canonical(AbstractRole r) {
        AbstractRole out = m_canonicalNames.get(r);
        return (out == null ? r : out);
    }
    public boolean isComplex(AbstractRole r) {
        return m_initialStates.containsKey(canonical(r));
    }
    
    /** Constructs a RoleBox from a collection of role axioms.
    *
    *   @param definitions  a map from each role to role sequences which imply that role
    *                       i.e. if R is transitive then [R, R] would be a member of the
    *                       collection associated with R.
    */
    public RoleBox(Map<AbstractRole, Set<List<AbstractRole>>> definitions) {
        Set<AbstractRole> complexRoles = new HashSet<AbstractRole>();
        m_canonicalNames = new HashMap<AbstractRole, AbstractRole>();
        m_symmetricRoles = new HashSet<AbstractRole>();
        
        // Separate simple inclusions and identify obvious complex roles:
        Map<AbstractRole, Set<AbstractRole>> superRoles = new HashMap<AbstractRole, Set<AbstractRole>>();
        for (AbstractRole r : definitions.keySet()) {
            for (List<AbstractRole> rolePath : definitions.get(r)) {
                if (rolePath != null) {
                    // TODO: role paths of length 0 are reflexivity?
                    if (rolePath.size() > 1) complexRoles.add(r);
                    else if (rolePath.size() == 1) {
                        for (AbstractRole s : rolePath) {
                            Set<AbstractRole> s_supers = superRoles.get(s);
                            if (s_supers == null) s_supers = superRoles.put(s, new HashSet<AbstractRole>());
                            s_supers.add(r);
                        }
                    }
                }
            }
        }
        // Transitively close supers:
        Queue<AbstractRole> q;
        for (Map.Entry<AbstractRole, Set<AbstractRole>> r : superRoles.entrySet()) {
            q = new LinkedList<AbstractRole>(r.getValue());
            while (!q.isEmpty()) {
                AbstractRole s = q.remove();
                Set<AbstractRole> s_supers = superRoles.get(s);
                if (s_supers != null) for (AbstractRole t : s_supers) {
                    if (r.getValue().add(t)) q.add(t);
                }
            }
        }
        // Identify role equivalence classes:
        Map<AbstractRole, Set<AbstractRole>> equivalencies = new HashMap<AbstractRole, Set<AbstractRole>>();
        for (Map.Entry<AbstractRole, Set<AbstractRole>> r : superRoles.entrySet()) {
            Set<AbstractRole> r_equiv = equivalencies.get(r.getKey());
            if (r_equiv == null) r_equiv = equivalencies.put(r.getKey(), new HashSet<AbstractRole>());
            r_equiv.add(r.getKey());
            for (AbstractRole s : r.getValue()) {
                Set<AbstractRole> s_supers = superRoles.get(s);
                if (s == r.getKey().getInverseRole() ||
                    (s_supers != null && s_supers.contains(r.getKey()))) {
                    Set<AbstractRole> s_equiv = equivalencies.get(s);
                    if (s_equiv == null) s_equiv = equivalencies.put(s, r_equiv);
                    else {
                        s_equiv.addAll(r_equiv);
                        r_equiv = equivalencies.put(r.getKey(), s_equiv);
                    }
                    r_equiv.add(s);
                }
            }
        }
        // Pick canonical names and identify symmetric roles:
        for (Map.Entry<AbstractRole, Set<AbstractRole>> e : equivalencies.entrySet()) {
            // We'll end up doing this several times for each equivalence class;
            // the last key in map order will end up as the canonical name:
            for (AbstractRole r : e.getValue()) {
                m_canonicalNames.put(r, e.getKey());
                m_canonicalNames.put(r.getInverseRole(), e.getKey().getInverseRole());
                if (e.getValue().contains(r.getInverseRole())) m_symmetricRoles.add(e.getKey());
            }
        }
        
        // Identify complex roles:
        q = new LinkedList<AbstractRole>(complexRoles);
        complexRoles.clear();
        while (!q.isEmpty()) {
            AbstractRole r = q.remove();
            if (complexRoles.add(r)) {
                // All super-roles are also complex (this will also get equivalents):
                Set<AbstractRole> supers = superRoles.get(r);
                if (supers != null) {
                    for (AbstractRole s : supers) q.add(s);
                }
                // A complex role's inverse is complex:
                q.add(r.getInverseRole());
            }
        }

        // Build a "sanitized" set of RIAs using canonical names for all roles:
        Map<AbstractRole, Set<List<AbstractRole>>> sanitized_definitions
             = new HashMap<AbstractRole, Set<List<AbstractRole>>>();
        for (AbstractRole r : definitions.keySet()) {
            // Only bother building automata for complex roles:
            if (complexRoles.contains(r)) {
                AbstractRole canonical_r = canonical(r);
                boolean isInverse = (canonical_r instanceof InverseAbstractRole);
                if (isInverse) canonical_r = canonical_r.getInverseRole();
                assert canonical_r instanceof AtomicAbstractRole;
                assert canonical_r == canonical(canonical_r);
                Set<List<AbstractRole>> newDefs = sanitized_definitions.get(canonical_r);
                if (newDefs == null) newDefs = sanitized_definitions.put(canonical_r, new HashSet<List<AbstractRole>>());
                for (List<AbstractRole> oldPath : definitions.get(r)) {
                    LinkedList<AbstractRole> newPath = new LinkedList<AbstractRole>();
                    for (AbstractRole s : oldPath) {
                        if (isInverse) newPath.addFirst(canonical(s.getInverseRole()));
                        else newPath.addLast(canonical(s));
                    }
                    // Only add non-tautological definitions:
                    if (!(newPath.size() == 1 && newPath.contains(canonical_r)) &&
                        // ignore symmetries as well. (we pass them explicitly)
                        !(newPath.size() == 1 && newPath.contains(canonical_r.getInverseRole()))) {
                        newDefs.add(newPath);
                    }
                }
            }
        }
        
        // Build the automata and convert them to a convenient format for future use:
        RoleAutomaton a =
            RoleAutomaton.combineAutomata(RoleAutomaton.makeAutomata(sanitized_definitions, m_symmetricRoles));
        for (RoleAutomaton.Transition t : a.m_transitions) {
            Map<Integer, Set<AbstractRole>> m = m_transitions.get(t.x.i.n);
            if (m == null) m = m_transitions.put(t.x.i.n, new HashMap<Integer, Set<AbstractRole>>());
            Set<AbstractRole> s = m.get(t.x.f.n);
            if (s == null) s = m.put(t.x.f.n, new HashSet<AbstractRole>());
            s.add(t.r);
        }
        m_initialStates = new HashMap<AbstractRole, Integer>();
        for (Map.Entry<Integer, Set<AbstractRole>> e : m_transitions.get(new Integer(0)).entrySet()) {
            for (AbstractRole r : e.getValue()) {
                assert !m_initialStates.containsKey(r);
                m_initialStates.put(r, e.getKey());
            }
        }
        for (AbstractRole r : complexRoles) assert m_initialStates.containsKey(canonical(r));
        m_acceptingStates = new HashSet<Integer>();
        m_acceptingStates.add(a.finalState().n);
    }

    /** The callback interface for the rewriteRole method. */
    public interface RoleRewriter {
        public void addTransition(Integer curState, AbstractRole role, Integer destState);
        public void finalState(Integer state);
    }
    /** Uses the provided rewriter to describe the role paths which induce a given role.
    *   The description is in the form of a "state machine" with integers representing states.
    *   It is possible that the same integer is used in callbacks generated by different
    *   calls, with different role arguments, to this method; such an integer refers to the
    *   same state in all cases. (The automata for different roles may intersect.)
    *
    *   @return     The initial state of the automaton for the given role.
    */
    public Integer rewriteRole(AbstractRole role, RoleRewriter rewriter, Set<Integer> visited) {
        assert isComplex(role);
        if (visited == null) visited = new HashSet<Integer>();
        Integer i = m_initialStates.get(role);
        Queue<Integer> q = new LinkedList<Integer>();
        q.add(i);
        while (!q.isEmpty()) {
            Integer curState = q.remove();
            if (!visited.contains(curState)) {
                for (Map.Entry<Integer, Set<AbstractRole>> e
                        : m_transitions.get(curState).entrySet()) {
                    q.add(e.getKey());
                    for (AbstractRole r : e.getValue()) {
                        rewriter.addTransition(curState, r, e.getKey());
                    }
                    if (m_acceptingStates.contains(e.getKey())) {
                        rewriter.finalState(e.getKey());
                    }
                }
                visited.add(curState);
            }
        }
        return i;
    }
}

// The remainder of the RBox code reflects the history of the implementation and is not
// intended for public consumption; it is likely to be heavily refactored in the near future.

class RoleAutomaton {
    // probably better to just use integers directly; these states don't hash or compare "correctly"
    static class State { final int n; State(int i) { n = i; } }
    static class StatePair { // mainly written just as a return type for <initial, final> tuples
        final State i; // never null
        final State f; // never null
        StatePair(State inI, State inF) {
            assert inI != null;
            assert inF != null;
            i = inI;
            f = inF;
        }
    }
    static final class Transition { // subclasses would break equals()
        final StatePair x; // never null
        final AbstractRole r; // null is the epsilon transition
        Transition(StatePair p, AbstractRole inR) {
            assert p != null;
            x = p;
            r = inR;
        }
        public int hashCode() {
            return (x.i.n << 16) ^ x.f.n ^ (r != null ? r.hashCode() : 0);
        }
        public boolean equals(Object o) {
            if (o.getClass() != this.getClass()) return false;
            Transition t = (Transition)o;
            return (t.x.i.n == this.x.i.n &&
                    t.x.f.n == this.x.f.n &&
                    t.r == this.r);
        }
    }
    private final StatePair m_initialAndFinal;
    Set<Transition> m_transitions;
    private int m_nextState;
    
    State initialState() { return m_initialAndFinal.i; }
    State finalState() { return m_initialAndFinal.f; }
    void addTransition(State a, State b) { addTransition(a, null, b); }
    void addTransition(State a, AbstractRole r, State b) {
        m_transitions.add(new Transition(new StatePair(a, b), r));
    }
    State newState() { return new State(m_nextState++); }
    StatePair add(RoleAutomaton a) { // performs a disjoint union with (a copy) of argument a
        for (Transition t : a.m_transitions) {
            addTransition(new State(t.x.i.n + m_nextState), t.r, new State(t.x.f.n + m_nextState));
        }
        StatePair out = new StatePair(new State(a.m_initialAndFinal.i.n + m_nextState), new State(a.m_initialAndFinal.f.n + m_nextState));
        m_nextState += a.m_nextState;
        return out;
    }
    StatePair addMirrored(RoleAutomaton a) { // disjoint union with a "mirrored" copy of a
        Set<Transition> transitions = (this == a ? new HashSet<Transition>(a.m_transitions) : a.m_transitions);
        for (Transition t : transitions) {
            addTransition(new State(t.x.f.n + m_nextState), (t.r != null ? t.r.getInverseRole() : null), new State(t.x.i.n + m_nextState));
        }
        StatePair out = new StatePair(new State(a.m_initialAndFinal.f.n + m_nextState), new State(a.m_initialAndFinal.i.n + m_nextState));
        m_nextState += a.m_nextState;
        return out;
    }
    RoleAutomaton mirror() {
        RoleAutomaton out = new RoleAutomaton();
        StatePair s = out.addMirrored(this);
        out.addTransition(out.initialState(), s.i);
        out.addTransition(s.f, out.finalState());
        return out;
    }
    void extendTransitions(AbstractRole r, Map<AbstractRole, RoleAutomaton> automata) {
        // replaces simple transitions for the role R with the entire automaton associated with R
        for (Transition t : new HashSet<Transition>(m_transitions)) {
            if (t.r != null && !t.r.equals(r)) {
                if (t.r.equals(r.getInverseRole())) throw new RuntimeException("Roles defined in terms of their own inverses are not supported.");
                RoleAutomaton a = automata.get(t.r);
                if (a != null) {
                    StatePair p = add(automata.get(t.r));
                    addTransition(t.x.i, p.i);
                    addTransition(p.f, t.x.f);
                }
            }
        }
    }
    
    RoleAutomaton() {
        m_nextState = 0;
        State initial = newState(); // for debugging, get initial to always be 0
        m_initialAndFinal = new StatePair(initial, newState());
        m_transitions = new HashSet<Transition>();
    }
    static  Collection<AbstractRole>
        orderRoles(Map<AbstractRole, Set<List<AbstractRole>>> definitions) {
        // Map from a role to the roles less than it:
        Map<AbstractRole,Set<AbstractRole>>
            greaterEdges = new HashMap<AbstractRole,Set<AbstractRole>>();
        if (definitions.isEmpty()) return new LinkedList<AbstractRole>();
        // build partial ordering:
        for (AbstractRole r : definitions.keySet()) {
            greaterEdges.put(r, new HashSet<AbstractRole>());
            for (Collection<AbstractRole> word : definitions.get(r)) {
                assert word != null;
                AbstractRole[] w = {};
                w = word.toArray(w); // not particularly elegant
                if (!(// ignore transitivity:
                      (w.length == 2 &&
                       w[0].equals(r) &&
                       w[1].equals(r)) ||
                      // and symmetry:
                      (w.length == 1 &&
                       w[0].equals(r.getInverseRole())))) {
                    int i = 0;
                    int end = w.length;
                    // Ignore head and tail recursion, but not both:
                    if (w[0].equals(r)) ++i;
                    else if (w[end-1].equals(r)) --end;
                    while (i < end) {
                        greaterEdges.get(r).add(w[i]);
                        greaterEdges.get(r).add(w[i++].getInverseRole());
                    }
                } // end if
            } // end for w
        } // end for r
        // produce a topological sort:
        Map<AbstractRole,Set<AbstractRole>>
            lessEdges = new HashMap<AbstractRole,Set<AbstractRole>>();
        Queue<AbstractRole> q = new LinkedList<AbstractRole>();
        for (Set<AbstractRole> set : greaterEdges.values()) {
            for (AbstractRole r : set) lessEdges.put(r, new HashSet<AbstractRole>());
        }
        for (Map.Entry<AbstractRole, Set<AbstractRole>> e : greaterEdges.entrySet()) {
            for (AbstractRole s : e.getValue()) lessEdges.get(s).add(e.getKey());
            if (e.getValue().isEmpty()) q.add(e.getKey());
        }
        if (q.isEmpty()) throw new RoleBox.NotRegularException();
        LinkedList<AbstractRole> out = new LinkedList<AbstractRole>();
        while (!q.isEmpty()) {
            AbstractRole r = q.remove();
            out.addLast(r);
            for (AbstractRole s : greaterEdges.get(r)) {
                assert lessEdges.get(s).contains(r);
                lessEdges.get(s).remove(r);
            }
            Set<AbstractRole> lessE = lessEdges.get(r);
            if (lessE != null) for (AbstractRole s : lessE) {
                assert greaterEdges.get(s).contains(r);
                greaterEdges.get(s).remove(r);
                if (greaterEdges.get(s).isEmpty()) q.add(s);
            }
            if (out.size() > greaterEdges.size()) throw new RoleBox.NotRegularException();
        }
        assert out.size() == greaterEdges.size();
        return out;
    }

    static  RoleAutomaton automatonForRole(AbstractRole r, Set<List<AbstractRole>> words, boolean isSymmetric) {
        RoleAutomaton out = new RoleAutomaton();
        out.addTransition(out.initialState(), r, out.finalState());
        for (Collection<AbstractRole> w : words) {
            State a = out.newState();
            java.util.Iterator<AbstractRole> i = w.iterator();
            assert i.hasNext() : "empty role axioms are not allowed";
            AbstractRole s = i.next();
            assert !(r.equals(s.getInverseRole()) && !i.hasNext()) : "symmetries should have already been extracted";
            if (r.equals(s)) {
                out.addTransition(out.finalState(), a);
                assert i.hasNext() : "tautological axioms should have been removed";
                s = i.next();
            } else out.addTransition(out.initialState(), a);
            while (i.hasNext()) {
                assert !(s.equals(r));
                State b = out.newState();
                out.addTransition(a, s, b);
                a = b;
                s = i.next();
            }
            if (r.equals(s)) out.addTransition(a, out.initialState());
            else {
                State b = out.newState();
                out.addTransition(a, s, b);
                out.addTransition(b, out.finalState());
            }
        } // end for w
        if (isSymmetric) {
            StatePair mirror = out.addMirrored(out);
            out.addTransition(out.initialState(), mirror.i);
            out.addTransition(mirror.i, out.initialState());
            out.addTransition(mirror.f, out.finalState());
            out.addTransition(out.finalState(), mirror.f);
        }
        return out;
    }
    static public Map<AbstractRole, RoleAutomaton>
        makeAutomata(Map<AbstractRole, Set<List<AbstractRole>>> definitions,
                     Set<AbstractRole> symmetricRoles) {
        Map<AbstractRole, RoleAutomaton> shallowAutomata = new HashMap<AbstractRole, RoleAutomaton>();
        for (AbstractRole r : definitions.keySet()) {
            shallowAutomata.put(r, automatonForRole(r, definitions.get(r), symmetricRoles.contains(r)));
        }
        Map<AbstractRole, RoleAutomaton> automata = new HashMap<AbstractRole, RoleAutomaton>();
        for (AbstractRole r : orderRoles(definitions)) {
            shallowAutomata.get(r).extendTransitions(r, automata);
            automata.put(r, shallowAutomata.get(r));
            automata.put(r.getInverseRole(), shallowAutomata.get(r).mirror());
        }
        return automata;
    }
    /** Combine the state machines for many roles into a single large state machine.
    *   For i the intitial state of the new combined machine and R a role used as a key
    *   in the map passed as a argument, the machine contains a transition <i, R, j>
    *   where j is the initial state of the sub-machine for R; i.e. using j as the initial
    *   state results in a machine which recognizes exactly the same language as the
    *   automata passed in for R.
    */
    static public  RoleAutomaton combineAutomata(Map<AbstractRole, RoleAutomaton> automata) {
        RoleAutomaton a = new RoleAutomaton();
        int i = 1;
        Map<AbstractRole, Integer> symbols = new HashMap<AbstractRole, Integer>();
        if (automata.isEmpty()) a.addTransition(a.initialState(), a.finalState());
        else for (AbstractRole r : automata.keySet()) {
            symbols.put(r, new Integer(i++));
            StatePair p = a.add(automata.get(r));
            a.addTransition(a.initialState(), r, p.i);
            a.addTransition(p.f, a.finalState());
        }
        // writeSymbols(new java.io.PrintWriter(System.out), symbols);
        // a.writeFsm(new java.io.PrintWriter(System.out), symbols);
        return a;
    }
    
    // Routines for reading from and writing to the text format recognized by the AT&T finite
    // state machine library and command-line tools <http://www.research.att.com/~fsmtools/fsm/>:
    
    RoleAutomaton(java.io.LineNumberReader input, Map<Integer, AbstractRole> symbols) throws java.io.IOException {
        m_nextState = 0;
        m_transitions = new HashSet<Transition>();
        State initial = null;
        while (true) {
            String[] fields = input.readLine().split("\\s+");
            if (fields.length < 3) {
                m_initialAndFinal = new StatePair(initial, new State(java.lang.Integer.parseInt(fields[0])));
                break;
            } else {
                State i = new State(java.lang.Integer.parseInt(fields[0]));
                if (initial == null) initial = i;
                State f = new State(java.lang.Integer.parseInt(fields[1]));
                if (f.n >= m_nextState) m_nextState = f.n + 1;
                AbstractRole r = symbols.get(java.lang.Integer.valueOf(fields[2]));
                addTransition(i, r, f);
            }
        }
    }
    static  Map<Integer, AbstractRole> readSymbols(java.io.LineNumberReader input) throws java.io.IOException {
        Map<Integer, AbstractRole> out = new HashMap<Integer, AbstractRole>();
        for (String line = input.readLine(); line != null; line = input.readLine()) {
            String[] fields = line.split("\\s+");
            out.put(new Integer(fields[1]), AbstractRole.fromString(fields[0]));
        }
        return out;
    }
    static  void writeFsmTransition(RoleAutomaton.Transition t, java.io.PrintWriter output, Map<AbstractRole, Integer> symbols) {
        output.println("" + t.x.i.n + " " + t.x.f.n + " " + (t.r == null ? "0" : symbols.get(t.r).toString()));
    }
    static  void writeSymbols(java.io.PrintWriter output, Map<AbstractRole, Integer> symbols) {
        output.println("Symbols: ");
        for (AbstractRole r : symbols.keySet()) {
            output.println(r.toString() + " " + symbols.get(r).toString());
        }
        output.flush();
    }
    void writeFsm(java.io.PrintWriter output, Map<AbstractRole, Integer> symbols) {
        output.println("Automaton: ");
        // The first transition listed must have the initial state as its source:
        Transition first = null;
        for (Transition t : m_transitions) {
            if (t.x.i.n == initialState().n) first = t;
        }
        writeFsmTransition(first, output, symbols);
        for (Transition t : m_transitions) {
            if (t != first) writeFsmTransition(t, output, symbols);
        }
        output.println("" + finalState().n);
        output.flush();
    }
}
