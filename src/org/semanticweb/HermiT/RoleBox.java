// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.util.GraphUtils;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;

public class RoleBox {
    @SuppressWarnings("serial")
    static public class NotRegularException extends IllegalArgumentException {
        NotRegularException() {
            super("The given role axioms are not regular; no suitable ordering is possible.");
        }
    }

    private Map<Role,Role> m_canonicalNames;
    private Set<Role> m_symmetricRoles;

    private Map<Integer,Map<Integer,Set<Role>>> m_transitions;
    private Map<Role,Integer> m_initialStates;
    private Set<Integer> m_acceptingStates;

    protected Role canonical(Role r) {
        Role out=m_canonicalNames.get(r);
        return (out==null ? r : out);
    }

    public boolean isComplex(Role r) {
        return m_initialStates.containsKey(canonical(r));
    }

    /**
     * Constructs a RoleBox from a collection of role axioms.
     * 
     * @param definitions
     *            a map from each role to role sequences which imply that role i.e. if R is transitive then [R, R] would be a member of the collection associated with R.
     */
    public RoleBox(Map<Role,Set<List<Role>>> definitions) {
        Set<Role> complexRoles=new HashSet<Role>();
        m_canonicalNames=new HashMap<Role,Role>();
        m_symmetricRoles=new HashSet<Role>();

        // Separate simple inclusions and identify obvious complex roles:
        Map<Role,Set<Role>> superRoles=new HashMap<Role,Set<Role>>();
        for (Role r : definitions.keySet()) {
            for (List<Role> rolePath : definitions.get(r)) {
                if (rolePath!=null) {
                    // TODO: role paths of length 0 are reflexivity?
                    if (rolePath.size()>1)
                        complexRoles.add(r);
                    else if (rolePath.size()==1) {
                        for (Role s : rolePath) {
                            Set<Role> s_supers=superRoles.get(s);
                            if (s_supers==null) {
                                s_supers=new HashSet<Role>();
                                superRoles.put(s,s_supers);
                            }
                            else
                                s_supers.add(r);
                        }
                    }
                }
            }
        }
        // Transitively close supers:
        // Queue<Role> q;
        // for (Map.Entry<Role, Set<Role>> r : superRoles.entrySet()) {
        // q = new LinkedList<Role>(r.getValue());
        // while (!q.isEmpty()) {
        // Role s = q.remove();
        // Set<Role> s_supers = superRoles.get(s);
        // if (s_supers != null) for (Role t : s_supers) {
        // if (r.getValue().add(t)) q.add(t);
        // }
        // }
        // }
        GraphUtils.transitivelyClose(superRoles);

        // Identify role equivalence classes:
        Map<Role,Set<Role>> equivalencies=new HashMap<Role,Set<Role>>();
        for (Map.Entry<Role,Set<Role>> r : superRoles.entrySet()) {
            Set<Role> r_equiv=equivalencies.get(r.getKey());
            if (r_equiv==null) {
                r_equiv=new HashSet<Role>();
                equivalencies.put(r.getKey(),r_equiv);
            }
            else
                r_equiv.add(r.getKey());
            for (Role s : r.getValue()) {
                Set<Role> s_supers=superRoles.get(s);
                if (s==r.getKey().getInverse()||(s_supers!=null&&s_supers.contains(r.getKey()))) {
                    Set<Role> s_equiv=equivalencies.get(s);
                    if (s_equiv==null) {
                        s_equiv=r_equiv;
                        equivalencies.put(s,s_equiv);
                    }
                    else {
                        s_equiv.addAll(r_equiv);
                        r_equiv=s_equiv;
                        equivalencies.put(r.getKey(),s_equiv);
                    }
                    r_equiv.add(s);
                }
            }
        }
        // Pick canonical names and identify symmetric roles:
        for (Map.Entry<Role,Set<Role>> e : equivalencies.entrySet()) {
            // We'll end up doing this several times for each equivalence class;
            // the last key in map order will end up as the canonical name:
            for (Role r : e.getValue()) {
                m_canonicalNames.put(r,e.getKey());
                m_canonicalNames.put(r.getInverse(),e.getKey().getInverse());
                if (e.getValue().contains(r.getInverse()))
                    m_symmetricRoles.add(e.getKey());
            }
        }

        // Identify complex roles:
        Queue<Role> q=new LinkedList<Role>(complexRoles);
        complexRoles.clear();
        while (!q.isEmpty()) {
            Role r=q.remove();
            if (complexRoles.add(r)) {
                // All super-roles are also complex (this will also get equivalents):
                Set<Role> supers=superRoles.get(r);
                if (supers!=null) {
                    for (Role s : supers)
                        q.add(s);
                }
                // A complex role's inverse is complex:
                q.add(r.getInverse());
            }
        }

        // Build a "sanitized" set of RIAs using canonical names for all roles:
        Map<Role,Set<List<Role>>> sanitized_definitions=new HashMap<Role,Set<List<Role>>>();
        for (Role r : definitions.keySet()) {
            // Only bother building automata for complex roles:
            if (complexRoles.contains(r)) {
                Role canonical_r=canonical(r);
                boolean isInverse=(canonical_r instanceof InverseRole);
                if (isInverse)
                    canonical_r=canonical_r.getInverse();
                assert canonical_r instanceof AtomicRole;
                assert canonical_r==canonical(canonical_r);
                Set<List<Role>> newDefs=sanitized_definitions.get(canonical_r);
                if (newDefs==null) {
                    newDefs=new HashSet<List<Role>>();
                    sanitized_definitions.put(canonical_r,newDefs);
                }
                for (List<Role> oldPath : definitions.get(r)) {
                    LinkedList<Role> newPath=new LinkedList<Role>();
                    for (Role s : oldPath) {
                        if (isInverse)
                            newPath.addFirst(canonical(s.getInverse()));
                        else
                            newPath.addLast(canonical(s));
                    }
                    // Only add non-tautological definitions:
                    if (!(newPath.size()==1&&newPath.contains(canonical_r))&&
                    // ignore symmetries as well. (we pass them explicitly)
                            !(newPath.size()==1&&newPath.contains(canonical_r.getInverse()))) {
                        newDefs.add(newPath);
                    }
                }
            }
        }

        // Build the automata and convert them to a convenient format for future use:
        RoleAutomaton a=RoleAutomaton.combineAutomata(RoleAutomaton.makeAutomata(sanitized_definitions,m_symmetricRoles));
        for (RoleAutomaton.Transition t : a.m_transitions) {
            Map<Integer,Set<Role>> m=m_transitions.get(t.x.i.n);
            if (m==null) {
                m=new HashMap<Integer,Set<Role>>();
                m_transitions.put(t.x.i.n,m);
            }
            Set<Role> s=m.get(t.x.f.n);
            if (s==null) {
                s=new HashSet<Role>();
                m.put(t.x.f.n,s);
            }
            s.add(t.r);
        }
        m_initialStates=new HashMap<Role,Integer>();
        for (Map.Entry<Integer,Set<Role>> e : m_transitions.get(new Integer(0)).entrySet()) {
            for (Role r : e.getValue()) {
                assert !m_initialStates.containsKey(r);
                m_initialStates.put(r,e.getKey());
            }
        }
        for (Role r : complexRoles)
            assert m_initialStates.containsKey(canonical(r));
        m_acceptingStates=new HashSet<Integer>();
        m_acceptingStates.add(a.finalState().n);
    }

    /** The callback interface for the rewriteRole method. */
    public interface RoleRewriter {
        public void addTransition(Integer curState,Role role,Integer destState);
        public void finalState(Integer state);
    }
    /**
     * Uses the provided rewriter to describe the role paths which induce a given role. The description is in the form of a "state machine" with integers representing states. It is possible that the same integer is used in callbacks generated by different calls, with different role arguments, to this method; such an integer refers to the same state in all cases. (The automata for different roles may intersect.)
     * 
     * @return The initial state of the automaton for the given role.
     */
    public Integer rewriteRole(Role role,RoleRewriter rewriter,Set<Integer> visited) {
        assert isComplex(role);
        if (visited==null)
            visited=new HashSet<Integer>();
        Integer i=m_initialStates.get(role);
        Queue<Integer> q=new LinkedList<Integer>();
        q.add(i);
        while (!q.isEmpty()) {
            Integer curState=q.remove();
            if (!visited.contains(curState)) {
                for (Map.Entry<Integer,Set<Role>> e : m_transitions.get(curState).entrySet()) {
                    q.add(e.getKey());
                    for (Role r : e.getValue()) {
                        rewriter.addTransition(curState,r,e.getKey());
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
    static class State {
        final int n;
        State(int i) {
            n=i;
        }
    }

    static class StatePair { // mainly written just as a return type for <initial, final> tuples
        final State i; // never null
        final State f; // never null
        StatePair(State inI,State inF) {
            assert inI!=null;
            assert inF!=null;
            i=inI;
            f=inF;
        }
    }

    static final class Transition { // subclasses would break equals()
        final StatePair x; // never null
        final Role r; // null is the epsilon transition
        Transition(StatePair p,Role inR) {
            assert p!=null;
            x=p;
            r=inR;
        }
        public int hashCode() {
            return (x.i.n<<16)^x.f.n^(r!=null ? r.hashCode() : 0);
        }
        public boolean equals(Object o) {
            if (o.getClass()!=this.getClass())
                return false;
            Transition t=(Transition)o;
            return (t.x.i.n==this.x.i.n&&t.x.f.n==this.x.f.n&&t.r==this.r);
        }
    }
    private final StatePair m_initialAndFinal;
    Set<Transition> m_transitions;
    private int m_nextState;

    State initialState() {
        return m_initialAndFinal.i;
    }
    State finalState() {
        return m_initialAndFinal.f;
    }
    void addTransition(State a,State b) {
        addTransition(a,null,b);
    }
    void addTransition(State a,Role r,State b) {
        m_transitions.add(new Transition(new StatePair(a,b),r));
    }
    State newState() {
        return new State(m_nextState++);
    }
    StatePair add(RoleAutomaton a) { // performs a disjoint union with (a copy) of argument a
        for (Transition t : a.m_transitions) {
            addTransition(new State(t.x.i.n+m_nextState),t.r,new State(t.x.f.n+m_nextState));
        }
        StatePair out=new StatePair(new State(a.m_initialAndFinal.i.n+m_nextState),new State(a.m_initialAndFinal.f.n+m_nextState));
        m_nextState+=a.m_nextState;
        return out;
    }
    StatePair addMirrored(RoleAutomaton a) { // disjoint union with a "mirrored" copy of a
        Set<Transition> transitions=(this==a ? new HashSet<Transition>(a.m_transitions) : a.m_transitions);
        for (Transition t : transitions) {
            addTransition(new State(t.x.f.n+m_nextState),(t.r!=null ? t.r.getInverse() : null),new State(t.x.i.n+m_nextState));
        }
        StatePair out=new StatePair(new State(a.m_initialAndFinal.f.n+m_nextState),new State(a.m_initialAndFinal.i.n+m_nextState));
        m_nextState+=a.m_nextState;
        return out;
    }
    RoleAutomaton mirror() {
        RoleAutomaton out=new RoleAutomaton();
        StatePair s=out.addMirrored(this);
        out.addTransition(out.initialState(),s.i);
        out.addTransition(s.f,out.finalState());
        return out;
    }
    void extendTransitions(Role r,Map<Role,RoleAutomaton> automata) {
        // replaces simple transitions for the role R with the entire automaton associated with R
        for (Transition t : new HashSet<Transition>(m_transitions)) {
            if (t.r!=null&&!t.r.equals(r)) {
                if (t.r.equals(r.getInverse()))
                    throw new RuntimeException("Roles defined in terms of their own inverses are not supported.");
                RoleAutomaton a=automata.get(t.r);
                if (a!=null) {
                    StatePair p=add(automata.get(t.r));
                    addTransition(t.x.i,p.i);
                    addTransition(p.f,t.x.f);
                }
            }
        }
    }

    RoleAutomaton() {
        m_nextState=0;
        State initial=newState(); // for debugging, get initial to always be 0
        m_initialAndFinal=new StatePair(initial,newState());
        m_transitions=new HashSet<Transition>();
    }
    static Collection<Role> orderRoles(Map<Role,Set<List<Role>>> definitions) {
        // Map from a role to the roles less than it:
        Map<Role,Set<Role>> greaterEdges=new HashMap<Role,Set<Role>>();
        if (definitions.isEmpty())
            return new LinkedList<Role>();
        // build partial ordering:
        for (Role r : definitions.keySet()) {
            greaterEdges.put(r,new HashSet<Role>());
            for (Collection<Role> word : definitions.get(r)) {
                assert word!=null;
                Role[] w= {};
                w=word.toArray(w); // not particularly elegant
                if (!(// ignore transitivity:
                (w.length==2&&w[0].equals(r)&&w[1].equals(r))||
                // and symmetry:
                (w.length==1&&w[0].equals(r.getInverse())))) {
                    int i=0;
                    int end=w.length;
                    // Ignore head and tail recursion, but not both:
                    if (w[0].equals(r))
                        ++i;
                    else if (w[end-1].equals(r))
                        --end;
                    while (i<end) {
                        greaterEdges.get(r).add(w[i]);
                        greaterEdges.get(r).add(w[i++].getInverse());
                    }
                } // end if
            } // end for w
        } // end for r
        // produce a topological sort:
        Map<Role,Set<Role>> lessEdges=new HashMap<Role,Set<Role>>();
        Queue<Role> q=new LinkedList<Role>();
        for (Set<Role> set : greaterEdges.values()) {
            for (Role r : set)
                lessEdges.put(r,new HashSet<Role>());
        }
        for (Map.Entry<Role,Set<Role>> e : greaterEdges.entrySet()) {
            for (Role s : e.getValue())
                lessEdges.get(s).add(e.getKey());
            if (e.getValue().isEmpty())
                q.add(e.getKey());
        }
        if (q.isEmpty())
            throw new RoleBox.NotRegularException();
        LinkedList<Role> out=new LinkedList<Role>();
        while (!q.isEmpty()) {
            Role r=q.remove();
            out.addLast(r);
            for (Role s : greaterEdges.get(r)) {
                assert lessEdges.get(s).contains(r);
                lessEdges.get(s).remove(r);
            }
            Set<Role> lessE=lessEdges.get(r);
            if (lessE!=null)
                for (Role s : lessE) {
                    assert greaterEdges.get(s).contains(r);
                    greaterEdges.get(s).remove(r);
                    if (greaterEdges.get(s).isEmpty())
                        q.add(s);
                }
            if (out.size()>greaterEdges.size())
                throw new RoleBox.NotRegularException();
        }
        assert out.size()==greaterEdges.size();
        return out;
    }

    static RoleAutomaton automatonForRole(Role r,Set<List<Role>> words,boolean isSymmetric) {
        RoleAutomaton out=new RoleAutomaton();
        out.addTransition(out.initialState(),r,out.finalState());
        for (Collection<Role> w : words) {
            State a=out.newState();
            java.util.Iterator<Role> i=w.iterator();
            assert i.hasNext() : "empty role axioms are not allowed";
            Role s=i.next();
            assert !(r.equals(s.getInverse())&&!i.hasNext()) : "symmetries should have already been extracted";
            if (r.equals(s)) {
                out.addTransition(out.finalState(),a);
                assert i.hasNext() : "tautological axioms should have been removed";
                s=i.next();
            }
            else
                out.addTransition(out.initialState(),a);
            while (i.hasNext()) {
                assert !(s.equals(r));
                State b=out.newState();
                out.addTransition(a,s,b);
                a=b;
                s=i.next();
            }
            if (r.equals(s))
                out.addTransition(a,out.initialState());
            else {
                State b=out.newState();
                out.addTransition(a,s,b);
                out.addTransition(b,out.finalState());
            }
        } // end for w
        if (isSymmetric) {
            StatePair mirror=out.addMirrored(out);
            out.addTransition(out.initialState(),mirror.i);
            out.addTransition(mirror.i,out.initialState());
            out.addTransition(mirror.f,out.finalState());
            out.addTransition(out.finalState(),mirror.f);
        }
        return out;
    }
    static public Map<Role,RoleAutomaton> makeAutomata(Map<Role,Set<List<Role>>> definitions,Set<Role> symmetricRoles) {
        Map<Role,RoleAutomaton> shallowAutomata=new HashMap<Role,RoleAutomaton>();
        for (Role r : definitions.keySet()) {
            shallowAutomata.put(r,automatonForRole(r,definitions.get(r),symmetricRoles.contains(r)));
        }
        Map<Role,RoleAutomaton> automata=new HashMap<Role,RoleAutomaton>();
        for (Role r : orderRoles(definitions)) {
            shallowAutomata.get(r).extendTransitions(r,automata);
            automata.put(r,shallowAutomata.get(r));
            automata.put(r.getInverse(),shallowAutomata.get(r).mirror());
        }
        return automata;
    }
    /**
     * Combine the state machines for many roles into a single large state machine. For i the intitial state of the new combined machine and R a role used as a key in the map passed as a argument, the machine contains a transition <i, R, j> where j is the initial state of the sub-machine for R; i.e. using j as the initial state results in a machine which recognizes exactly the same language as the automata passed in for R.
     */
    static public RoleAutomaton combineAutomata(Map<Role,RoleAutomaton> automata) {
        RoleAutomaton a=new RoleAutomaton();
        int i=1;
        Map<Role,Integer> symbols=new HashMap<Role,Integer>();
        if (automata.isEmpty())
            a.addTransition(a.initialState(),a.finalState());
        else
            for (Role r : automata.keySet()) {
                symbols.put(r,new Integer(i++));
                StatePair p=a.add(automata.get(r));
                a.addTransition(a.initialState(),r,p.i);
                a.addTransition(p.f,a.finalState());
            }
        // writeSymbols(new java.io.PrintWriter(System.out), symbols);
        // a.writeFsm(new java.io.PrintWriter(System.out), symbols);
        return a;
    }

    // Routines for reading from and writing to the text format recognized by the AT&T finite
    // state machine library and command-line tools <http://www.research.att.com/~fsmtools/fsm/>:

    RoleAutomaton(java.io.LineNumberReader input,Map<Integer,Role> symbols) throws java.io.IOException {
        m_nextState=0;
        m_transitions=new HashSet<Transition>();
        State initial=null;
        while (true) {
            String[] fields=input.readLine().split("\\s+");
            if (fields.length<3) {
                m_initialAndFinal=new StatePair(initial,new State(java.lang.Integer.parseInt(fields[0])));
                break;
            }
            else {
                State i=new State(java.lang.Integer.parseInt(fields[0]));
                if (initial==null)
                    initial=i;
                State f=new State(java.lang.Integer.parseInt(fields[1]));
                if (f.n>=m_nextState)
                    m_nextState=f.n+1;
                Role r=symbols.get(java.lang.Integer.valueOf(fields[2]));
                addTransition(i,r,f);
            }
        }
    }
    static void writeFsmTransition(RoleAutomaton.Transition t,java.io.PrintWriter output,Map<Role,Integer> symbols) {
        output.println(""+t.x.i.n+" "+t.x.f.n+" "+(t.r==null ? "0" : symbols.get(t.r).toString()));
    }
    static void writeSymbols(java.io.PrintWriter output,Map<Role,Integer> symbols) {
        output.println("Symbols: ");
        for (Role r : symbols.keySet()) {
            output.println(r.toString()+" "+symbols.get(r).toString());
        }
        output.flush();
    }
    void writeFsm(java.io.PrintWriter output,Map<Role,Integer> symbols) {
        output.println("Automaton: ");
        // The first transition listed must have the initial state as its source:
        Transition first=null;
        for (Transition t : m_transitions) {
            if (t.x.i.n==initialState().n)
                first=t;
        }
        writeFsmTransition(first,output,symbols);
        for (Transition t : m_transitions) {
            if (t!=first)
                writeFsmTransition(t,output,symbols);
        }
        output.println(""+finalState().n);
        output.flush();
    }
}
