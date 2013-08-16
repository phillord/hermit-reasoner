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

import java.util.List;
import java.util.Set;

import rationals.Automaton;
import rationals.State;
import rationals.transformations.EpsilonTransitionRemover;

/**
 * This method computes the weak bisimulation relation between two states.
 * The weak bisimulation is computed as (strong) bisimulation between the two 
 * given automata where all epsilon transitions have been removed.
 * 
 * @author nono
 * @version $Id: WeakBisimulation.java 2 2006-08-24 14:41:48Z oqube $
 */
public class WeakBisimulation implements Relation {

    private Automaton a1;

    private Automaton a2;

    private Set exp;

    private Bisimulation bisim;

    /*
     * (non-Javadoc)
     * 
     * @see rationals.tests.Relation#setAutomata(rationals.Automaton,
     *      rationals.Automaton)
     */
    public void setAutomata(Automaton a1, Automaton a2) {
        EpsilonTransitionRemover er = new EpsilonTransitionRemover();
        this.bisim = new Bisimulation( er.transform(a1), er.transform(a2));
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.tests.Relation#equivalence(rationals.State,
     *      rationals.State)
     */
    public boolean equivalence(State q0a, State q0b) {
        return bisim.equivalence(q0a,q0b);
    }

    public boolean equivalence(Set nsa, Set nsb) {
        return bisim.equivalence(nsa,nsb);
    }
    /* (non-Javadoc)
     * @see rationals.properties.Relation#getErrorTrace()
     */
    public List getErrorTrace() {
        // TODO Auto-generated method stub
        return null;
    }
}