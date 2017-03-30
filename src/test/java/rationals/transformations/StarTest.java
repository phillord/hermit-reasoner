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

import junit.framework.TestCase;
import rationals.Automaton;
import rationals.State;
import rationals.Transition;
import rationals.properties.ContainsEpsilon;

/**
 * @author nono
 * @version $Id: StarTest.java 2 2006-08-24 14:41:48Z oqube $
 */
@SuppressWarnings("javadoc")
public class StarTest extends TestCase {

    private Automaton automaton;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        automaton = new Automaton();
        State s1 = automaton.addState(true, false);
        State s2 = automaton.addState(false, false);
        State s3 = automaton.addState(false, true);
        automaton.addTransition(new Transition(s1, "a", s2), null);
        automaton.addTransition(new Transition(s2, "b", s3), null);
    }

    /**
     * Constructor for StarTest.
     * 
     * @param arg0
     */
    public StarTest(String arg0) {
        super(arg0);
    }

    public void test() {
        Star star = new Star();
        Automaton b = star.transform(automaton);
        assertTrue(new ContainsEpsilon().test(b));
    }

}
