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
 * Created on 11 avr. 2005
 *
 */
package rationals;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author nono
 * @version $Id: AutomatonTest.java 2 2006-08-24 14:41:48Z oqube $
 */
@SuppressWarnings("javadoc")
public class AutomatonTest extends TestCase {

    private Automaton automaton;
    private State[] ss;

    /**
     * Constructor for AutomatonTest.
     * 
     * @param arg0
     */
    public AutomatonTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        automaton = new Automaton();
        /* states */
        ss = new State[5];
        ss[0] = automaton.addState(true, false);
        ss[1] = automaton.addState(false, false);
        ss[2] = automaton.addState(false, false);
        ss[3] = automaton.addState(false, false);
        ss[4] = automaton.addState(false, true);
        /* transition */
        automaton.addTransition(new Transition(ss[0], "a", ss[0]), null);
        automaton.addTransition(new Transition(ss[0], "b", ss[1]), null);
        automaton.addTransition(new Transition(ss[1], "b", ss[0]), null);
        automaton.addTransition(new Transition(ss[1], "a", ss[2]), null);
        automaton.addTransition(new Transition(ss[2], "b", ss[3]), null);
        automaton.addTransition(new Transition(ss[2], "a", ss[1]), null);
        automaton.addTransition(new Transition(ss[3], "a", ss[2]), null);
        automaton.addTransition(new Transition(ss[3], "b", ss[4]), null);
        automaton.addTransition(new Transition(ss[4], "b", ss[0]), null);
        automaton.addTransition(new Transition(ss[4], "a", ss[4]), null);
    }

    public void testAddState() {
        State s = automaton.addState(false, false);
        assertTrue(automaton.states().contains(s));
        assertTrue(!automaton.initials().contains(s));
        assertTrue(!automaton.terminals().contains(s));
    }

    public void testAlphabet() {
        Set<String> alph = new HashSet<>();
        alph.add("a");
        alph.add("b");
        alph.add("c");
        automaton.addTransition(new Transition(ss[0], "c", ss[3]), null);
        assertTrue(automaton.alphabet().equals(alph));
    }

    public void testStates() {
        State s = automaton.addState(false, false);
        assertTrue(automaton.states().contains(s) && automaton.states().size() == 6);
    }

    public void testInitials() {
        State s = automaton.addState(true, false);
        assertTrue(automaton.states().contains(s));
        assertTrue(automaton.initials().contains(s));
        assertTrue(!automaton.terminals().contains(s));
    }

    public void testTerminals() {
        State s = automaton.addState(false, true);
        assertTrue(automaton.states().contains(s));
        assertTrue(!automaton.initials().contains(s));
        assertTrue(automaton.terminals().contains(s));
    }

    /*
     * Class under test for Set accessibleStates()
     */
    public void testAccessibleStates() {
        State s5 = automaton.addState(false, false);
        State s6 = automaton.addState(false, false);
        automaton.addTransition(new Transition(ss[0], "c", s5), null);
        automaton.addTransition(new Transition(s5, "c", s6), null);
        automaton.addTransition(new Transition(s6, "a", s5), null);
        Set<State> acc = automaton.accessibleStates();
        assertTrue(acc.contains(s5) && acc.contains(s6));
    }

    /*
     * Class under test for Set coAccessibleStates()
     */
    public void testCoAccessibleStates() {
        State s5 = automaton.addState(false, false);
        State s6 = automaton.addState(false, false);
        automaton.addTransition(new Transition(s5, "c", ss[4]), null);
        automaton.addTransition(new Transition(s6, "c", s5), null);
        automaton.addTransition(new Transition(s5, "a", s6), null);
        Set<State> acc = automaton.coAccessibleStates();
        assertTrue(acc.contains(s5) && acc.contains(s6));
    }

    public void testAccessibleAndCoAccessibleStates() {
        Set<State> acc = automaton.accessibleAndCoAccessibleStates();
        State s5 = automaton.addState(false, false);
        State s6 = automaton.addState(false, false);
        automaton.addTransition(new Transition(ss[0], "c", s5), null);
        automaton.addTransition(new Transition(s5, "c", s6), null);
        automaton.addTransition(new Transition(s6, "a", s5), null);
        assertTrue(automaton.states().containsAll(acc));
        assertTrue(!acc.contains(s5) && !acc.contains(s6));
    }
}