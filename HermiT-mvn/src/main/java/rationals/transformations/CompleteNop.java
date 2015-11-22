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
package rationals.transformations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * This methods completes the transitions in an Automaton w.r.t. to an
 * arbitrary alphabet.
 * <p>
 * That is, for each state <code>q</code> and for each letter <code>l</code>
 * in <code>ioa</code>'s alphabet, if there is no transition labelled with
 * <code>l</code> starting from <code>q</code>, it adds a transition
 * <code>(q,l,q)</code> to this automaton.
 * <p>
 * The semantic of this completion scheme should be compared with
 * {@link rationals.transformations.CompleteSink}which completes an automaton
 * by adding a sink state.
 * <p>
 * 
 * @author nono
 * @version $Id: CompleteNop.java 6 2006-08-30 08:56:44Z oqube $
 */
public class CompleteNop implements UnaryTransformation {

    private final Set<Object> alphabet;

    public CompleteNop(Set<Object> alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public Automaton transform(Automaton a) {
        Automaton b = new Automaton();
        Map<State, State> qm = new HashMap<>();
        for (Iterator<State> i = a.states().iterator(); i.hasNext();) {
            State q = i.next();
            State p = b.addState(q.isInitial(), q.isTerminal());
            qm.put(q, p);
        }
        Set<Object> alph = new HashSet<>();
        for (Iterator<State> it = a.states().iterator(); it.hasNext();) {
            State q = it.next();
            alph.addAll(alphabet);
            for (Iterator<Transition> i2 = a.delta(q).iterator(); i2.hasNext();) {
                Transition tr = i2.next();
                    b.addTransition(new Transition(qm.get(tr.start()),
                            tr.label(), qm.get(tr.end())),null);
                alph.remove(tr.label());
            }
            for (Iterator<Object> i2 = alph.iterator(); i2.hasNext();) {
                    b.addTransition(new Transition(q, i2.next(), q),null);
            }
            alph.clear();
        }
        return b;
    }

}
