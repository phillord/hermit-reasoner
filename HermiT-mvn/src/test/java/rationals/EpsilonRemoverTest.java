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
 * Created on 22 mars 2005
 *
 */
package rationals;

import java.util.Set;

import junit.framework.TestCase;
import rationals.transformations.EpsilonTransitionRemover;
import rationals.transformations.Reducer;

/**
 * @author nono
 * @version $Id: EpsilonRemoverTest.java 2 2006-08-24 14:41:48Z oqube $
 */
@SuppressWarnings("javadoc")
public class EpsilonRemoverTest extends TestCase {

    /**
     * Constructor for EspilonRemoverTest.
     * 
     * @param arg0
     */
    public EpsilonRemoverTest(String arg0) {
        super(arg0);
    }

    public void testEpsilon() {
        Automaton a = new Automaton();
        State s1 = a.addState(true, false);
        State s2 = a.addState(false, false);
        State s3 = a.addState(false, true);
        a.addTransition(new Transition(s1, null, s1), null);
        a.addTransition(new Transition(s1, "a", s2), null);
        a.addTransition(new Transition(s2, null, s3), null);
        a.addTransition(new Transition(s3, null, s1), null);
        a.addTransition(new Transition(s3, "b", s3), null);
        Automaton b = new EpsilonTransitionRemover().transform(a);
        assertTrue(!b.alphabet().contains(null));
        /* check there is no transition with null labels */
        Set<Transition> s = b.delta();
        assertNoEpsilon(s);

        b = new Reducer().transform(b);
    }

    public void testEpsilon2() {
        Automaton a = new Automaton();
        State s1 = a.addState(true, false);
        State s2 = a.addState(false, false);
        State s3 = a.addState(false, true);
        a.addTransition(new Transition(s1, null, s2), null);
        a.addTransition(new Transition(s1, "a", s2), null);
        a.addTransition(new Transition(s2, "a", s3), null);
        a.addTransition(new Transition(s3, null, s2), null);
        Automaton b = new EpsilonTransitionRemover().transform(a);
        assertTrue(!b.alphabet().contains(null));
        /* check there is no transition with null labels */
        Set<Transition> s = b.delta();
        assertNoEpsilon(s);

        b = new Reducer().transform(b);
    }

    public void testEpsilon3() {
        Automaton a = new Automaton();
        State s1 = a.addState(true, false);
        State s2 = a.addState(false, false);
        State s3 = a.addState(false, true);
        a.addTransition(new Transition(s1, "a", s2), null);
        a.addTransition(new Transition(s2, null, s3), null);
        Automaton b = new EpsilonTransitionRemover().transform(a);
        assertTrue(!b.alphabet().contains(null));
        /* check there is no transition with null labels */
        Set<Transition> s = b.delta();
        assertNoEpsilon(s);
        b = new Reducer().transform(b);
    }

    /**
     * @param s
     */
    private static void assertNoEpsilon(Set<Transition> s) {
        for (Transition tr : s) {
            assertNotNull("Transition " + tr + " labelled with epsilon", tr.label());
        }
    }

}
