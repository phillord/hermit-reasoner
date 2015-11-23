/*______________________________________________________________________________
 * 
 * Copyright 2005 Arnaud Bailly - NORSYS/LIFL
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * (3) The name of the author may not be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on 5 avr. 2005
 *
 */
package rationals;

import java.util.List;
import java.util.Set;

/**
 * An interface for abstract state machines.
 * <p>
 * A state machine - or labelled transition system - is defined by a tuple
 * <code>(Q,S,q0,d)</code> where:
 * <ul>
 * <li><code>Q</code> is a set of states ;</li>
 * <li><code>S</code> is a finite set of labels ;</li>
 * <li><code>q0</code> is the initial state of the machine ;</li>
 * <li><code>d</code> is a transition relation in <code>Q x S x Q</code>.
 * </ul>
 * This definition is slightly modified for this interface as the initials is
 * defined as a set instead of a single state.
 * <code>null</code> may be used to denote <em>silent transitions</em>, that is
 * unobservable internal behavior of the machine, which can lead to non
 * determinism.
 * <p>
 * The rationals.Automaton is the main implementation for this interface.
 * 
 * @author nono
 * @version $Id: StateMachine.java 10 2007-05-30 17:25:00Z oqube $
 */
public interface StateMachine {

    /**
     * Returns the alphabet - <code>S</code> - of this state machine.
     * 
     * @return a Set of Object.
     */
    Set<Object> alphabet();

    /**
     * Retrieves the state factory associated to this SM.
     * 
     * @return a StateFactory instance
     */
    StateFactory getStateFactory();

    /**
     * Returns the set of all transitions of this machine starting from a given
     * state and labelled with a given label.
     * 
     * @param state
     *            a state of this SM.
     * @param label
     *            a label used in this SM.
     * @return the set of all transitions of this automaton starting from state
     *         <tt>state</tt> and labelled by <tt>label</tt>. Objects which are
     *         contained in this set are instances of class <tt>Transition</tt>.
     * @see Transition
     */
    Set<Transition> delta(State state, Object label);

    /**
     * Return all transitions from a State.
     * 
     * @param state
     *            start state
     * @return a new Set of transitions (maybe empty)
     */
    Set<Transition> delta(State state);

    /**
     * Returns all transitions from a given set of states.
     * 
     * @param s
     *            a Set of State objects
     * @return a Set of Transition objects
     */
    Set<Transition> delta(Set<State> s);

    /**
     * Return the set of states this SM will be in after reading the word from
     * start states <code>s</code>.
     * 
     * @param s
     *            the set of starting states
     * @param word
     *            the word to read.
     * @return the set of reached states. Maybe empty or <code>null</code>.
     */
    Set<State> steps(Set<State> s, List<?> word);

    /**
     * Return the set of states accessible in one transition from given set of
     * states s and letter o.
     * 
     * @param s
     *            the starting states
     * @param o
     *            the letter
     * @return a set of reachable states. Maybe empty or <code>null</code>.
     */
    Set<State> step(Set<State> s, Object o);

    /**
     * Returns the set of initial states for this machine.
     * 
     * @return a Set of State objects.
     */
    Set<State> initials();

    /**
     * Returns the set of states that can access the given states' set
     * <code>st</code>. This is the inverse relation of <code>d</code>
     * 
     * @param st
     *            end states
     * @return a set of states that can reach <code>st</code>. May be empty or
     *         null.
     */
    Set<Transition> deltaMinusOne(State st);
}