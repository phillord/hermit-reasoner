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
package rationals.transformations;

import java.util.HashMap;
import java.util.Map;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * Computes the prefix closure of given automaton.
 * <p>
 * The resulting automaton is simply the starting automaton with all states made
 * terminal.
 * <ul>
 * <li>C = Pref(A)</li>
 * <li>S(C) = S(A)</li>
 * <li>S0(C) = S0(A)</li>
 * <li>T(C) = S(A)</li>
 * <li>D(C) = D(A)</li>
 * </ul>
 * 
 * @author nono
 * @version $Id: PrefixClosure.java 2 2006-08-24 14:41:48Z oqube $
 */
public class PrefixClosure implements UnaryTransformation {

    @Override
    public Automaton transform(Automaton a) {
        Automaton ret = new Automaton();
        Map<State, State> sm = new HashMap<>();
        for (State st : a.states()) {
            State sr = ret.addState(st.isInitial(), true);
            sm.put(st, sr);
        }
        /* add all transitions */
        for (Transition tr : a.delta()) {
                ret.addTransition(new Transition(sm.get(tr.start()), tr
                        .label(), sm.get(tr.end())),null);
        }
        return ret;
    }

}

/*
 * $Log: PrefixClosure.java,v $ Revision 1.1 2004/11/15 12:39:14 bailly added
 * PrefixClosure transformation
 *  
 */