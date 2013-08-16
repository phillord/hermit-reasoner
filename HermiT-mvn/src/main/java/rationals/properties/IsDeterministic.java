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
 * Created on 12 avr. 2005
 *
 */
package rationals.properties;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * A property that checks a given automaton is deterministic. 
 * <p>
 * An automaton <code>(X,Q,I,T,D)</code> is deterministic iff :
 * <ul>
 * <li><code>|I| = 1</code></li>
 * <li><code>D is a function from (Q x X) -> Q</code>
 * <li><code>X</code> does not contains the symbol <code>epsilon</code></li>
 * </ul>
 * @author nono
 * @version $Id: IsDeterministic.java 2 2006-08-24 14:41:48Z oqube $
 */
public class IsDeterministic implements UnaryTest {

    /* (non-Javadoc)
     * @see rationals.properties.UnaryTest#test(rationals.Automaton)
     */
    public boolean test(Automaton a) {
        if(a.alphabet().contains(null))
            return false;
        if(a.initials().size() > 1)
            return false;
        for(Iterator it = a.states().iterator();it.hasNext();) {
            State s = (State)it.next();
            Set tra = new HashSet();
            for(Iterator it2 = a.delta(s).iterator();it2.hasNext();) {
                Transition tr =(Transition)it2.next();
                if(tra.contains(tr.label()))
                    return false;
                else
                    tra.add(tr.label());
            }
        }
        return true;
    }

}
