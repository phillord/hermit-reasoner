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
* Created on Sep 21, 2004
* 
*/
package rationals.properties;

import junit.framework.TestCase;
import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;
import rationals.converters.ConverterException;

/**
 * Test class for equivalence between automata.
 * 
 * @author nono
 * @version $Id: EquivalentTest.java 2 2006-08-24 14:41:48Z oqube $
 */
public class EquivalentTest extends TestCase {

  /**
   * Constructor for EquivalentTest.
   * @param arg0
   */
  public EquivalentTest(String arg0) {
    super(arg0);
  }

  /*
   * Canonical example of NOT a bisimulation
   */
  public void testBisim() throws ConverterException, NoSuchStateException {
    Automaton a = new Automaton();
    State a1 = a.addState(true,false);
    State a2 = a.addState(false,false);
    State a3 = a.addState(false,false);
    State a4 = a.addState(false,false);
    a.addTransition(new Transition(a1,"a",a2));
    a.addTransition(new Transition(a2,"b",a3));
    a.addTransition(new Transition(a2,"c",a4));
    Automaton b = new Automaton();
    State[] sts = new State[5];
    for(int i = 0;i<5;i++)
        sts[i] = b.addState(i == 0,false);
    b.addTransition(new Transition(sts[0],"a",sts[1]));
    b.addTransition(new Transition(sts[0],"a",sts[2]));
    b.addTransition(new Transition(sts[1],"b",sts[3]));
    b.addTransition(new Transition(sts[2],"c",sts[4]));
    AreEquivalent eq = new AreEquivalent(new Bisimulation());
    assertTrue(!eq.test(a,b));
  }
  
  /*
   * Canonical example of NOT a bisimulation
   */
  public void testWeakBisim() throws ConverterException, NoSuchStateException {
    Automaton a = new Automaton();
    State a1 = a.addState(true,false);
    State a2 = a.addState(false,false);
    State a3 = a.addState(false,false);
    State a4 = a.addState(false,false);
    a.addTransition(new Transition(a1,"a",a2));
    a.addTransition(new Transition(a2,null,a2));
    a.addTransition(new Transition(a2,"b",a3));
    a.addTransition(new Transition(a2,"c",a4));
    Automaton b = new Automaton();
    State[] sts = new State[5];
    for(int i = 0;i<5;i++)
        sts[i] = b.addState(i == 0,false);
    b.addTransition(new Transition(sts[0],"a",sts[1]));
    b.addTransition(new Transition(sts[1],null,sts[2]));
    b.addTransition(new Transition(sts[2],"b",sts[3]));
    b.addTransition(new Transition(sts[1],"c",sts[4]));
    AreEquivalent eq = new AreEquivalent(new WeakBisimulation());
    assertTrue(eq.test(a,b));
  }
  
  
}

/* 
 * $Log: EquivalentTest.java,v $
 * Revision 1.3  2005/03/23 07:22:42  bailly
 * created transductions package
 * corrected EpsilonRemover
 * added some tests
 * removed DirectedGRaph Interface from Automaton
 *
 * Revision 1.2  2005/02/20 21:14:19  bailly
 * added API for computing equivalence relations on automata
 *
 * Revision 1.1  2004/09/21 11:50:28  bailly
 * added interface BinaryTest
 * added class for testing automaton equivalence (isomorphism of normalized automata)
 * added computation of RE from Automaton
 *
*/