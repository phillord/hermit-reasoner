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
 * Created on 30 mars 2005
 *
 */
package rationals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Instances of this class are random automata. A RandomAutomaton is generated
 * according to following parameters :
 * <ul>
 * <li>The number of states in the automaton</li>
 * <li>The number of terminal states</li>
 * <li>The alphabet : if the alphabet contains a <code>null</code> element,
 * it will be used and the resulting automaton will contain epsilon-transitions
 * </li>
 * <li>The mean transition density which is the number of transitions in the
 * automaton divided by the square of the number of states times the size of the
 * alphabet</li>
 * <li>The standard deviation of the transition density</li>
 * <li>A flag indicating if the automaton should be deterministic or not. Note
 * that if the alphabet contains epsilon, then the resulting automaton will most
 * probably be non-deterministic even if this flag is set.</li>
 * </ul>
 * The result is an - non reduced - automaton with a single start state and
 * random transitions following a normal distribution according to preceding
 * parameters over the alphabet.
 * 
 * @author nono
 * @version $Id: RandomAutomaton.java 2 2006-08-24 14:41:48Z oqube $
 */
public class RandomAutomaton extends Automaton {

    private static final Random rand = new Random();

    private int nstate;

    private int fstate;

    private Object[] alph;

    private double density;

    private double deviation;

    /**
     * Construct a RandomAutomaton according to the given parameters.
     * 
     * @param nstate
     *            number of total states
     * @param fstate
     *            number of final states
     * @param alphabet
     *            alphabet
     * @param density
     *            mean transition density
     * @param deviation
     *            transition density standard deviation
     * @param det
     *            is the result deterministic
     */
    public RandomAutomaton(int nstate, int fstate, Object[] alph,
            double density, double deviation, boolean det) {
        this.nstate = nstate;
        this.fstate = fstate;
        this.alph = alph;
        this.density = density;
        this.deviation = deviation;
        if (det)
            makeDFA();
        else
            makeNFA();
    }

    /**
     *  
     */
    private void makeNFA() {
        /* create initial state and other states */
        State init = addState(true, false);
        for (int i = 0; i < fstate; i++)
            addState(false, true);
        for (int i = fstate; i < nstate; i++)
            addState(false, false);
        State[] sts = (State[]) states().toArray(new State[nstate + 1]);
        /* create transitions */
        Iterator it = states().iterator();
        while (it.hasNext()) {
            State from = (State) it.next();
            int c = alph.length * sts.length * sts.length;
            /* number of transitions from this state to other state */
            int nt = (int) (c * (deviation * rand.nextGaussian() + density));
            for (int i = 0; i < nt; i++) {
                State to = sts[rand.nextInt(sts.length)];
                Object lbl = alph[rand.nextInt(alph.length)];
                try {
                    /* create transition */
                    addTransition(new Transition(from, lbl, to));
                } catch (NoSuchStateException e1) {
                }
            }
        }
    }

    /**
     *  
     */
    private void makeDFA() {
        /* create initial state and other states */
        State init = addState(true, false);
        List todo = new ArrayList();
        List done = new ArrayList();
        int fs = fstate;
        int ns = nstate;
        todo.add(init);
        while (ns > 0) {
            /* pop state */
            State from = (State) todo.remove(0);
            done.add(from);
            /* list for alph */
            List l = new ArrayList(Arrays.asList(alph));
            int c = alph.length * nstate;
            /* number of transitions from this state to other state */
            int nt = (int) (deviation * rand.nextGaussian() + density);
            for (int i = 0; i < nt && !l.isEmpty(); i++) {
                /*
                 * select a state : this an already visited state with
                 * probability (done.size() / nstate)
                 */
                State to = null;
                double r = rand.nextDouble() * (nstate - 1);
                if ((int) r < done.size()) {
                    to = (State) done.get((int) r);
                } else {
                    /*
                     * state is final with probability fs / ns
                     */
                    r = rand.nextDouble() * ns;
                    to = addState(false, r < fs);
                    todo.add(to);
                    ns--;
                    if (r < fs)
                        fs--;
                }
                Object lbl = l.remove(rand.nextInt(l.size()));
                try {
                    /* create transition */
                    addTransition(new Transition(from, lbl, to));
                } catch (NoSuchStateException e1) {
                }
            }
        }
    }
}