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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import rationals.Automaton;
import rationals.Couple;
import rationals.State;
import rationals.Transition;

/**
 * This method computes the (strong) bisimulation relation between two states.
 * 
 * @author nono
 * @version $Id: Bisimulation.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Bisimulation implements Relation {

    private Automaton a1;

    private Automaton a2;

    private Set<Couple> exp;

    /**
     * Constructor with two automataon.
     * This constructor effectively calls {@link #setAutomata(Automaton,Automaton)}.
     * 
     * @param automaton automaton
     * @param automaton2 automaton2
     */
    public Bisimulation(Automaton automaton, Automaton automaton2) {
        setAutomata(automaton,automaton2);
    }

    /**
     * Argument-less constructor.
     * Note that this implies the method {@link #setAutomata(Automaton,Automaton)} 
     * <strong>must</strong> be called before using this relation.
     */
    public Bisimulation() {
    }

    @Override
    public void setAutomata(Automaton a1, Automaton a2) {
        this.a1 = a1;
        this.a2 = a2;
        this.exp = new HashSet<>();
    }

    @Override
    public boolean equivalence(State q0a, State q0b) {
        Couple cpl = new Couple(q0a, q0b);
        /* check states are unknown */
        if (exp.contains(cpl))
            return true;
        exp.add(cpl);
        /* iterate over all transitions */
        Set<Transition> tas = a1.delta(q0a);
        Set<Transition> tbs = a2.delta(q0b);
        Iterator<Transition> it = tas.iterator();
        while (it.hasNext()) {
            Transition tr = it.next();
            State ea = tr.end();
            /* check transition exists in b */
            Set<Transition> tbsl = a2.delta(q0b, tr.label());
            if (tbsl.isEmpty())
                return false;
            Iterator<Transition> trb = tbsl.iterator();
            while (trb.hasNext()) {
                Transition tb = trb.next();
                /* mark transition as visited */
                tbs.remove(tb);
                State eb = tb.end();
                if (!equivalence(ea, eb) && !trb.hasNext())
                    return false;
            }
        }
        /* checks all transitions from b has been visited */
        if (!tbs.isEmpty()) {
            exp.remove(cpl);
            return false;
        }
        /* OK */
        return true;
    }

    @Override
    public boolean equivalence(Set<State> nsa, Set<State> nsb) {
       for(Iterator<State> i = nsa.iterator();i.hasNext();) {
           State sa = i.next();
           for(Iterator<State> j = nsb.iterator();j.hasNext();) {
               State sb = j.next();
               if(!equivalence(sa,sb))
                   return false;
           }
       }
       return true;
    }
}