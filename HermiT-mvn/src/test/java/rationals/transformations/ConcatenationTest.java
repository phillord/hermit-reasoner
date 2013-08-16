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
package rationals.transformations;

import java.util.Arrays;

import junit.framework.TestCase;
import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * @author nono
 * @version $Id: ConcatenationTest.java 2 2006-08-24 14:41:48Z oqube $
 */
public class ConcatenationTest extends TestCase {

    private Automaton a;
    private Automaton b;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        a = new Automaton();
        State s1 = a.addState(true, false);
        State s2 = a.addState(false, false);
        State s3 = a.addState(false, true);
        a.addTransition(new Transition(s1, "a", s2));
        a.addTransition(new Transition(s2, "b", s3));
        b = new Automaton();
        s1 = b.addState(true, true);
        s2 = b.addState(false, false);
        s3 = b.addState(false, true);
        b.addTransition(new Transition(s1, "c", s1));
        b.addTransition(new Transition(s1, "a", s2));
        b.addTransition(new Transition(s2, "b", s3));
        
    }

    /**
     * Constructor for ConcatenationTest.
     * @param arg0
     */
    public ConcatenationTest(String arg0) {
        super(arg0);
    }

    public void test1() {
        Concatenation conc = new Concatenation();
        Automaton c = conc.transform(a,b);
        Object[] word = new Object[] { "a", "b", "c" , "c" , "a", "b" };
        Object[] word1 = new Object[] { "a", "b", "a", "b" };
        Object[] word2 = new Object[] { "a", "b", "c" , "a"};
        assertTrue(c.accept(Arrays.asList(word)));
        assertTrue(c.accept(Arrays.asList(word1)));
        assertTrue(!c.accept(Arrays.asList(word2)));
    }
}
