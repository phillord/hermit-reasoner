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
package rationals.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import rationals.Automaton;
import rationals.State;
import rationals.Transition;
import rationals.transformations.StatesCouple;
import rationals.transformations.TransformationsToolBox;

/**
 * A class that compute trace equivalence relation between two states. This
 * class checks whether two states from two automata are trace equivalent, which
 * simply means they recognize the same prefix of languages.
 * <p>
 * This class effectively computes the deterministic form of the two given
 * automata.
 * 
 * @author nono
 * @version $Id: TraceEquivalence.java 2 2006-08-24 14:41:48Z oqube $
 */
public class TraceEquivalence implements Relation {

    private Automaton a1;

    private Automaton a2;

    private List errorTrace;

    /*
     * (non-Javadoc)
     * 
     * @see rationals.tests.Relation#setAutomata(rationals.Automaton,
     *      rationals.Automaton)
     */
    public void setAutomata(Automaton a1, Automaton a2) {
        this.a1 = a1;
        this.a2 = a2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.tests.Relation#equivalence(rationals.State,
     *      rationals.State)
     */
    public boolean equivalence(State q0a, State q0b) {
        /* compute epsilon closures on states */
        Set nsa = a1.getStateFactory().stateSet();
        Set nsb = a2.getStateFactory().stateSet();
        nsa.add(q0a);
        nsb.add(q0b);
        /* check equivalence on sets */
        return equivalence(nsa, nsb);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.properties.Relation#equivalence(java.util.Set,
     *      java.util.Set)
     */
    public boolean equivalence(Set nsa, Set nsb) {
        /* sets of explored states */
        Stack todo /* < StatesCouple > */= new Stack();
        /* current traces for failure */
        Stack labels = new Stack();
        List trace = new ArrayList();
        Set /* < StatesCouple > */done = new HashSet();
        todo.push(new StatesCouple(nsa, nsb));
        labels.push("");
        do {
            StatesCouple cpl = (StatesCouple) todo.pop();
            Object lbl = labels.pop();
            Set sa = TransformationsToolBox.epsilonClosure(cpl.sa, a1);
            Set sb = TransformationsToolBox.epsilonClosure(cpl.sb, a2);
            if (done.contains(cpl)) {
                trace.remove(trace.size() - 1);
                continue;
            }else
                trace.add(lbl);
            done.add(cpl);
            /* compute set of transitions */
            List /* < Transition > */tas = new ArrayList(a1.delta(sa));
            List /* < Transition > */tbs = new ArrayList(a2.delta(sb));
            /* map from letters to set of states */
            Map /* < Object, State > */am = new HashMap();
            Map /* < Object, State > */bm = new HashMap();
            /* compute set of states reached for each letter */
            mapAlphabet(tas, am, a1);
            mapAlphabet(tbs, bm, a2);
            Iterator it2 = am.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry me = (Map.Entry) it2.next();
                Object l = me.getKey();
                Set as = (Set) me.getValue();
                Set bs = (Set) bm.remove(l);
                if (bs == null) {
                    this.errorTrace = trace;
                    this.errorTrace.add(l);
                    return false;
                }
                StatesCouple sc = new StatesCouple(as, bs);
                todo.push(sc);
                labels.push(l);
            }
            if (!bm.isEmpty()) {
                this.errorTrace = trace;
                this.errorTrace.add(bm.keySet());
                return false;
            }
        } while (!todo.isEmpty());
        return true;
    }

    /*
     * @param tas @param am
     */
    public void mapAlphabet(List tas, Map am, Automaton a) {
        /* compute set of states for each letter */
        while (!tas.isEmpty()) {
            Transition tr = (Transition) tas.remove(0);
            Object l = tr.label();
            if (l == null)
                continue;
            Set as = (Set) am.get(l);
            if (as == null) {
                as = a.getStateFactory().stateSet();
                am.put(l, as);
            }
            as.add(tr.end());
        }
    }

    /**
     * @return Returns the errorTrace.
     */
    public List getErrorTrace() {
        return errorTrace;
    }
}