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
 * Created on 20 fevr. 2005
 *
 */
package rationals.transformations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

/**
 * This class computes the projection of an Automaton on given alphabet. The
 * projection alphabet is set by the class's constructor.
 * <p />
 * The algorithm is verys simple: All transitions which are not labelled
 * with letters from the projection alphabet are transformed into 
 * <code>null</code> transitions. The resulting automaton is obviously no 
 * more deterministic if the automaton <code>a</code> was.
 * 
 * @author nono
 * @version $Id: Projection.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Projection implements UnaryTransformation {

    private Set alphabet;

    public Projection(Set alphabet) {
        this.alphabet = alphabet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.transformations.UnaryTransformation#transform(rationals.Automaton)
     */
    public Automaton transform(Automaton a) {
        Automaton b = new Automaton();
        Map smap = new HashMap();
        Iterator it = a.delta().iterator();
        while (it.hasNext()) {
            Transition tr = (Transition) it.next();
            State os = tr.start();
            State oe = tr.end();
            Object l = tr.label();
            /* check states exist */
            State ns = (State) smap.get(os);
            State ne = (State) smap.get(oe);
            if (ns == null)
                smap.put(os, ns = b.addState(os.isInitial(), os.isTerminal()));
            if (ne == null)
                smap.put(oe, ne = b.addState(oe.isInitial(), oe.isTerminal()));
            /* check label is in alphabet */
            if (alphabet.contains(l))
                try {
                    b.addTransition(new Transition(ns, l, ne));
                } catch (NoSuchStateException e) {
                }
            else
                try {
                    b.addTransition(new Transition(ns, null, ne));
                } catch (NoSuchStateException e1) {
                }
        }
        return b;
    }
}