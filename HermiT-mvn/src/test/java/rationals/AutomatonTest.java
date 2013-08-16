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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author nono
 * @version $Id: AutomatonTest.java 2 2006-08-24 14:41:48Z oqube $
 */
public class AutomatonTest extends TestCase {

    private Automaton automaton;
    private State[] ss;

    /**
     * Constructor for AutomatonTest.
     * @param arg0
     */
    public AutomatonTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        automaton = new Automaton();
        /* states */
        ss = new State[5];
        ss[0] = automaton.addState(true,false);
        ss[1] = automaton.addState(false,false);
        ss[2] = automaton.addState(false,false);
        ss[3] = automaton.addState(false,false);
        ss[4] = automaton.addState(false,true);
        /* transition */
        automaton.addTransition(new Transition(ss[0],"a",ss[0]));
        automaton.addTransition(new Transition(ss[0],"b",ss[1]));
        automaton.addTransition(new Transition(ss[1],"b",ss[0]));
        automaton.addTransition(new Transition(ss[1],"a",ss[2]));
        automaton.addTransition(new Transition(ss[2],"b",ss[3]));
        automaton.addTransition(new Transition(ss[2],"a",ss[1]));
        automaton.addTransition(new Transition(ss[3],"a",ss[2]));
        automaton.addTransition(new Transition(ss[3],"b",ss[4]));
        automaton.addTransition(new Transition(ss[4],"b",ss[0]));
        automaton.addTransition(new Transition(ss[4],"a",ss[4]));
    }
    
    public void testAddState() {
        State s = automaton.addState(false,false);
        assertTrue(automaton.states().contains(s));
        assertTrue(!automaton.initials().contains(s));
        assertTrue(!automaton.terminals().contains(s));
    }

    public void testAlphabet() throws NoSuchStateException {
        Set alph = new HashSet();
        alph.add("a");
        alph.add("b");
        alph.add("c");
        automaton.addTransition(new Transition(ss[0],"c",ss[3]));
        assertTrue(automaton.alphabet().equals(alph));        
    }

    public void testStates() {
        State s = automaton.addState(false,false);
        assertTrue(automaton.states().contains(s) && automaton.states().size() == 6);
    }

    public void testInitials() {
        State s = automaton.addState(true,false);
        assertTrue(automaton.states().contains(s));
        assertTrue(automaton.initials().contains(s));
        assertTrue(!automaton.terminals().contains(s));
    }

    public void testTerminals() {
        State s = automaton.addState(false,true);
        assertTrue(automaton.states().contains(s));
        assertTrue(!automaton.initials().contains(s));
        assertTrue(automaton.terminals().contains(s));
    }

    /*
     * Class under test for Set accessibleStates()
     */
    public void testAccessibleStates() throws NoSuchStateException {
        State s5 = automaton.addState(false,false);
        State s6 = automaton.addState(false,false);
        automaton.addTransition(new Transition(ss[0],"c",s5));
        automaton.addTransition(new Transition(s5,"c",s6));
        automaton.addTransition(new Transition(s6,"a",s5));
        Set acc = automaton.accessibleStates();
        assertTrue(acc.contains(s5) && acc.contains(s6));
    }

    /*
     * Class under test for Set coAccessibleStates()
     */
    public void testCoAccessibleStates() throws NoSuchStateException {
        State s5 = automaton.addState(false,false);
        State s6 = automaton.addState(false,false);
        automaton.addTransition(new Transition(s5,"c",ss[4]));
        automaton.addTransition(new Transition(s6,"c",s5));
        automaton.addTransition(new Transition(s5,"a",s6));
        Set acc = automaton.coAccessibleStates();
        assertTrue(acc.contains(s5) && acc.contains(s6));
    }

    public void testAccessibleAndCoAccessibleStates() throws NoSuchStateException {
        Set acc = automaton.accessibleAndCoAccessibleStates();
        State s5 = automaton.addState(false,false);
        State s6 = automaton.addState(false,false);
        automaton.addTransition(new Transition(ss[0],"c",s5));
        automaton.addTransition(new Transition(s5,"c",s6));
        automaton.addTransition(new Transition(s6,"a",s5));
        assertTrue(automaton.states().containsAll(acc));
        assertTrue(!acc.contains(s5) && !acc.contains(s6));
    }

    /*
     * Class under test for Set delta()
     */
    public void testDelta() {
        //TODO Implement delta().
    }

    public void testAddTransition() {
        //TODO Implement addTransition().
    }

    public void testAcceptDFA() throws NoSuchStateException {
		Automaton t = new Automaton();
		State s1 = t.addState(true, true);
		State s2 = t.addState(false, false);
		State s3 = t.addState(false, false);
		t.addTransition(new Transition(s1, "a", s2));
		t.addTransition(new Transition(s2, "b", s3));
		t.addTransition(new Transition(s3, "c", s1));
		// check accept words
		List exp = Arrays.asList(new String[] {  "a", "b","c", "a", "b", "c" });
		assertTrue("Automaton does not accept 'abcabc'",t.accept(exp));
		exp = Arrays.asList(new String[] {  "a", "b","c", "b", "c" });
		assertTrue("Automaton does accept 'abcbc'",!t.accept(exp));
    }

    public void testAcceptNFA1() throws NoSuchStateException {
		Automaton t = new Automaton();
		State s1 = t.addState(true, true);
		State s2 = t.addState(false, false);
		State s3 = t.addState(false, false);
		State s4 = t.addState(false, false);
		t.addTransition(new Transition(s1, "a", s2));
		t.addTransition(new Transition(s2, "b", s3));
		t.addTransition(new Transition(s3, "c", s4));
		t.addTransition(new Transition(s4, null, s1));
		// check accept words
		List exp = Arrays.asList(new String[] {  "a", "b","c", "a", "b", "c" });
		assertTrue("Automaton does not accept 'abcabc'",t.accept(exp));
		exp = Arrays.asList(new String[] {  "a", "b","c", "b", "c" });
		assertTrue("Automaton does accept 'abcbc'",!t.accept(exp));
    }
}
