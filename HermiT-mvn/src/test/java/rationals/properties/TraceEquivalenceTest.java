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
 * Created on 27 mars 2005
 *
 */
package rationals.properties;

import junit.framework.TestCase;
import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * @author nono
 * @version $Id: TraceEquivalenceTest.java 2 2006-08-24 14:41:48Z oqube $
 */
public class TraceEquivalenceTest extends TestCase {

    /**
     * Constructor for TraceEquivalenceTest.
     * 
     * @param arg0
     */
    public TraceEquivalenceTest(String arg0) {
        super(arg0);
    }

    /*
     * simple trace equivalence test
     */
    public void testTraceEq() throws Throwable {
        Automaton a = new Automaton();
        State a1 = a.addState(true, false);
        State a2 = a.addState(false, true);
        State a3 = a.addState(false, true);
        a.addTransition(new Transition(a1, "a", a2));
        a.addTransition(new Transition(a1, "a", a3));
        a.addTransition(new Transition(a2, "b", a3));
        a.addTransition(new Transition(a3, "b", a2));
        Automaton b = new Automaton();
        State b1 = b.addState(true, false);
        State b2 = b.addState(false, true);
        b.addTransition(new Transition(b1, "a", b2));
        b.addTransition(new Transition(b2, "b", b2));
        TraceEquivalence equiv = new TraceEquivalence();
        AreEquivalent eq = new AreEquivalent(equiv);
        assertTrue(eq.test(a, b));
    }

}
