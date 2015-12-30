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
 * Created on 5 avr. 2005
 *
 */
package rationals.transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;

/**
 * A transformation that computes the complement of an automaton.
 * <p>
 * This transformation computes the complement of an automaton: Terminal states
 * are inverted and missing transitions are added.
 * 
 * @author nono
 * @version $Id: Complement.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Complement implements UnaryTransformation {

    @Override
    public Automaton transform(Automaton a) {
        Automaton ret = new Automaton();
        List<State> todo = new ArrayList<>();
        Map<State, State> sm = new HashMap<>();
        Set<State> done = new HashSet<>();
        Set<State> s = a.initials();
        todo.addAll(s);
        while (!todo.isEmpty()) {
            State st = todo.remove(0);
            State ns = sm.get(st);
            if (ns == null) {
                ns = ret.addState(st.isInitial(), !st.isTerminal());
                sm.put(st, ns);
            }
            done.add(st);
            for (Object l : a.alphabet()) {
                Set<Transition> ends = a.delta(st, l);
                if (ends.isEmpty())
                        ret.addTransition(new Transition(ns, l, ns),null);
                else {
                    for (Iterator<Transition> i = ends.iterator(); i.hasNext();) {
                        State end = i.next().end();
                        State ne = sm.get(end);
                        if (ne == null) {
                            ne = ret.addState(end.isInitial(), !end
                                    .isTerminal());
                            sm.put(end, ne);
                            todo.add(end);
                        }
                            ret.addTransition(new Transition(ns, l, ne),null);
                    }
                }

            }
        }
        return ret;
    }

}