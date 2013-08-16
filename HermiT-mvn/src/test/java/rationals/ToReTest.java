/*______________________________________________________________________________
*
* Copyright 2004 Arnaud Bailly - NORSYS/LIFL
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
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
* OF THE POSSIBILITY OF SUCH DAMAGE.
*______________________________________________________________________________
*
* Created on Sep 13, 2004
* 
*/
package rationals;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;
import rationals.converters.ConverterException;
import rationals.converters.Expression;
import rationals.converters.ToRExpression;

/**
 * @author nono
 * @version $Id: ToReTest.java 2 2006-08-24 14:41:48Z oqube $
 */
public class ToReTest extends TestCase {

  public ToReTest(String n) {
    super(n);
  }
  
  public void testRE1() throws ConverterException {
      String re = "ab*c";
      Automaton a = new Expression().fromString(re);
      String er = new ToRExpression().toString(a);
      System.err.println(er);
      a = new Expression().fromString(er);
      assertTrue(a.accept(Arrays.asList(new Object[]{"a","b","b","c"})));    
      assertTrue(!a.accept(Arrays.asList(new Object[]{"a","b","b"})));  
    }
    
  public void testRESingleton() throws ConverterException {
      String re = "a";
      Automaton a = new Expression().fromString(re);
      String er = new ToRExpression().toString(a);
      System.err.println(er);
      a = new Expression().fromString(er);
      assertTrue(a.accept(Arrays.asList(new Object[]{"a"})));   
    }
    
  public void testREEpsilon() throws ConverterException {
    String re = "(ab*c)*";
    Automaton a = new Expression().fromString(re);
    String er = new ToRExpression().toString(a);
    System.err.println(er);
    a = new Expression().fromString(er);
    assertTrue(a.accept(new ArrayList()));    
    assertTrue(a.accept(Arrays.asList(new Object[]{"a","b","b","c"})));    
    assertTrue(!a.accept(Arrays.asList(new Object[]{"a","b","b"})));  
  }
  
  
}

/* 
 * $Log: ToReTest.java,v $
 * Revision 1.1  2004/09/21 11:50:28  bailly
 * added interface BinaryTest
 * added class for testing automaton equivalence (isomorphism of normalized automata)
 * added computation of RE from Automaton
 *
*/