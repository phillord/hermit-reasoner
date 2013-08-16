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
package rationals.transformations;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import rationals.Automaton;
import rationals.converters.ConverterException;
import rationals.converters.Expression;
import rationals.converters.ToRExpression;
import rationals.properties.isEmpty;

/**
 * @author nono
 * @version $Id: MixTest.java 10 2007-05-30 17:25:00Z oqube $
 */
public class MixTest extends TestCase {

    /**
     * Constructor for MixTest.
     * 
     * @param arg0
     */
    public MixTest(String arg0) {
        super(arg0);
    }

    public void testMix1() throws ConverterException {
        Automaton a = new Expression().fromString("ab*cd");
        Automaton b = new Pruner().transform(new Expression()
                .fromString("a*ebc"));
        Automaton c = new Mix().transform(a, b);
        String re = new ToRExpression().toString(c);
        System.out.println(re);
        assertEquals("aebcd", re);
    }

    public void testMix2() throws ConverterException {
        Automaton a = new Pruner().transform(new Expression()
                .fromString("a(bb)*e"));
        Automaton b = new Pruner().transform(new Expression()
                .fromString("a(bbb)*e"));
        Automaton c = new Reducer().transform(new Mix().transform(a, b));
        System.out.println(new ToRExpression().toString(c));
        assertTrue("automata should accept word", c.accept(makeList("abbbbbbbbbbbbe")));
        assertTrue("automata should accept word", c.accept(makeList("ae")));
        assertTrue("automata should not accept word", !c.accept(makeList("abbe")));
    }

    private List<Object> makeList(String string) {
      List<Object> l = new ArrayList<Object>();
      for(int i=0;i<string.length();i++)
        l.add(string.charAt(i)+"");
      return l;
    }

    public void testMix4() throws ConverterException {
        Automaton a = new Expression().fromString("a(b+c)(ab)*");
        Automaton b = new Expression().fromString("(a+b)*c");
        Automaton c = new Reducer().transform(new Mix().transform(a, b));
        String re = new ToRExpression().toString(c);
        System.out.println(re);
        assertEquals("ac", re);
    }

    public void testMixCommute() throws ConverterException {
        Automaton a = new Expression().fromString("ab*cd");
        Automaton b = new Pruner().transform(new Expression()
                .fromString("a*ebc"));
        Automaton c = new Mix().transform(a, b);
        Automaton d = new Mix().transform(b, a);
        String rec = new ToRExpression().toString(c);
        System.err.println("a m b =" +rec);
        String red = new ToRExpression().toString(d);
        System.err.println("b m a =" +red);
        assertEquals(rec,red);
    }

    public void testMixEmpty() throws ConverterException {
        Automaton a = new Expression().fromString("abc");
        Automaton b = new Expression()
                .fromString("acb");
        Automaton c = new Mix().transform(a, b);
        assertTrue(new isEmpty().test(c));
    }

}
