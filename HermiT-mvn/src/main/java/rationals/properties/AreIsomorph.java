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
 * Created on 29 mars 2005
 *
 */
package rationals.properties;

import java.util.HashMap;
import java.util.Map;

import rationals.Automaton;

/**
 * Test if two automaton are isomorphic.
 * Two automata are isomorphic iff there exists a bijection
 * between states of each automata that preserve the initial states, the 
 * terminal states and the transition relation.
 *  
 * @author nono
 * @version $Id: AreIsomorph.java 2 2006-08-24 14:41:48Z oqube $
 */
public class AreIsomorph implements BinaryTest {

    /* (non-Javadoc)
     * @see rationals.properties.BinaryTest#test(rationals.Automaton, rationals.Automaton)
     */
    public boolean test(Automaton a, Automaton b) {
        /* basic test */
        if(a.states().size() != b.states().size() || a.initials().size() != b.initials().size() || a.terminals().size() != b.terminals().size())
            return false;
        Map /* < State , State > */ atob = new HashMap();
        
        return false;
    }

}
