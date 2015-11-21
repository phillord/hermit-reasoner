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
 * Created on 22 juin 2005
 *
 */
package rationals;

import java.lang.reflect.Array;
import java.util.BitSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import rationals.DefaultStateFactory.DefaultState;

/**
 * A specialization of Automaton for handling two letters alphabets.
 * <p>
 * A binary automaton is just an ordinary finite states automaton with an
 * alphabet of two elements. A binary automaton may not contain
 * <code>epsilon</code> transitions but may be non-deterministic.
 * <p />
 * This implementation uses bitset matrices for handling transitions with an
 * expected increase in efficiency and decrease in size of automata.
 * 
 * @author nono
 * @version $Id: BinaryAutomaton.java 2 2006-08-24 14:41:48Z oqube $
 */
public class BinaryAutomaton extends Automaton {

    protected Object one;

    protected Object zero;

    // array of transitions
    private BitSet[][] trans;

    private BitSet[][] reverse;

    // next index to use in trans
    private int idx;

    /**
     * This class implements Set for Transition objects which are computed on
     * the fly from an array of bitsets.
     * 
     * @author nono
     * @version $Id: BinaryAutomaton.java 2 2006-08-24 14:41:48Z oqube $
     */
    @SuppressWarnings("rawtypes")
    class TransitionSet implements Set {

        protected BitSet from;

        private BitSet[][] transactions;

        private BitSet bits;

        /**
         * @param fromSet
         *            the set of states indices to take into account
         * @param trans
         *            the set of transitions
         */
        public TransitionSet(BitSet fromSet, BitSet[][] trans) {
            this.from = fromSet;
            this.transactions = trans;
        }

        @Override
        public boolean equals(Object obj) {
            TransitionSet ts = (TransitionSet) obj;
            return (ts == null) ? false : (ts.from.equals(from) && ts.transactions == transactions);
        }

        @Override
        public int hashCode() {
            return from.hashCode() << 9 ^ transactions.hashCode();
        }

        protected int modcount = 0;

        protected int mods = 0;

        protected int frombit = -1;
        protected int lblbit = -1;

        private Iterator<Transition> it = new Iterator<Transition>() {

            @Override
            public void remove() {
                // NOT IMPLEMENTED
            }

            @Override
            public boolean hasNext() {
                return from.nextSetBit(frombit) > -1;
            }

            @Override
            public Transition next() {
                frombit = from.nextSetBit(frombit);
                if (frombit == -1)
                    throw new NoSuchElementException();
                mods++;
                modcount++;
                if (mods != modcount)
                    throw new ConcurrentModificationException();
                // construct transition
                DefaultStateFactory.DefaultState fromState = null;/*
                                                                   * getStateFactory
                                                                   * ().new
                                                                   * DefaultStateFactory
                                                                   * .
                                                                   * DefaultState
                                                                   * (frombit,
                                                                   * false,false
                                                                   * );
                                                                   */
                fromState.initial = BinaryAutomaton.this.initials().contains(fromState);
                fromState.terminal = BinaryAutomaton.this.terminals().contains(fromState);

                DefaultStateFactory.DefaultState toState = null; /*
                                                                  * new
                                                                  * DefaultStateFactory
                                                                  * .
                                                                  * DefaultState
                                                                  * (tobit,false
                                                                  * ,false);
                                                                  */
                toState.initial = BinaryAutomaton.this.initials().contains(toState);
                toState.terminal = BinaryAutomaton.this.terminals().contains(toState);

                Transition tr = new Transition(fromState, lblbit == 1 ? one : zero, toState);
                /* advance iterator */
                // bit++;
                return tr;
            }
        };

        @Override
        public int size() {
            return bits.cardinality();
        }

        @Override
        public void clear() {
            modcount++;
            bits.clear();
        }

        @Override
        public boolean isEmpty() {
            return bits.isEmpty();
        }

        @Override
        public Object[] toArray() {
            Object[] ret = new Object[size()];
            Iterator<Transition> iterator = iterator();
            int i = 0;
            while (iterator.hasNext()) {
                ret[i++] = iterator.next();
            }
            return ret;
        }

        @Override
        public boolean add(Object o) {
            DefaultState ds = (DefaultState) o;
            if (bits.get(ds.i))
                return false;
            bits.set(ds.i);
            modcount++;
            return true;
        }

        @Override
        public boolean contains(Object o) {
            DefaultState ds = (DefaultState) o;
            return bits.get(ds.i);
        }

        @Override
        public boolean remove(Object o) {
            DefaultState ds = (DefaultState) o;
            if (!bits.get(ds.i))
                return false;
            bits.clear(ds.i);
            modcount++;
            return true;
        }

        @Override
        public boolean addAll(Collection c) {
            return false;
        }

        @Override
        public boolean containsAll(Collection c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection c) {
            return false;
        }

        @Override
        public Iterator<Transition> iterator() {
            /* reset iterator */
            frombit = modcount = mods = 0;
            return it;
        }

        @Override
        public Object[] toArray(Object[] a) {
            Object[] ret;
            if (a.length == size())
                ret = a;
            else { /* create array dynamically */
                ret = (Object[]) Array.newInstance(a.getClass().getComponentType(), size());
            }
            Iterator iterator = iterator();
            while (iterator.hasNext()) {
                // XXX this code is never used. Transitions are returned from
                // the iterator, but cast to states here
                DefaultState ds = (DefaultState) iterator.next();
                ret[ds.i] = ds;
            }
            return ret;
        }

    }

    /**
     * Construct a binary automaton with given objects as zero and one.
     * <p>
     * This objects may then be passed in Transition objects labels. Note that
     * equality is tested using <code>==</code>, not method
     * <code>equals()</code>. This means that a reference must be kept by the
     * caller or methods {@see #zero()}and {@see #one()}may later be called.
     * This also means that one of the object may be <code>null</code>.
     * <p>
     * The two parameters must be different in the sense of == operator.
     * 
     * @param one
     *            the Object in transitions labels denoting one
     * @param zero
     *            the Object in transitions labels denoting zero
     */
    public BinaryAutomaton(Object one, Object zero) {
        if (one == zero)
            throw new IllegalArgumentException("Labels may not be identical");
        this.one = one;
        this.zero = zero;
        // ensure factory is default state factory
        setStateFactory(new DefaultStateFactory(this));
        // make initial trans array
        trans = new BitSet[0][0];
        reverse = new BitSet[0][0];
        alphabet.add(one);
        alphabet.add(zero);
        idx = 0;
    }

    public BinaryAutomaton() {
        this(null, new Object());
    }

    @Override
    public State addState(boolean initial, boolean terminal) {
        DefaultStateFactory.DefaultState st = (DefaultStateFactory.DefaultState) super.addState(initial, terminal);
        // add a new row to the matrix
        idx = st.i;
        BitSet[][] ntr = new BitSet[idx + 1][];
        System.arraycopy(trans, 0, ntr, 0, idx);
        ntr[idx] = new BitSet[2];
        ntr[idx][0] = new BitSet();
        ntr[idx][1] = new BitSet();
        trans = ntr;
        // do the same for reverse
        ntr = new BitSet[idx + 1][];
        System.arraycopy(reverse, 0, ntr, 0, idx);
        ntr[idx] = new BitSet[2];
        ntr[idx][0] = new BitSet();
        ntr[idx][1] = new BitSet();
        reverse = ntr;
        return st;
    }

    @Override
    public boolean addTransition(Transition transition) {
        int i = transition.label() == one ? 1 : 0;
        // extract states' indices
        int from = ((DefaultStateFactory.DefaultState) transition.start()).i;
        int to = ((DefaultStateFactory.DefaultState) transition.end()).i;
        // update transition matrix
        trans[from][i].set(to);
        reverse[to][i].set(from);
        return true;
    }

    @Override
    public boolean validTransition(Transition transition) {
        int i = transition.label() == one ? 1 : (transition.label() == zero) ? 0 : -1;
        if (i == -1)
            return false;
        if (trans.length == 0 || trans[0].length < 2 || reverse.length == 0 || reverse[0].length < 2) {
            return false;
        }

        int from = ((DefaultStateFactory.DefaultState) transition.start()).i;
        int to = ((DefaultStateFactory.DefaultState) transition.end()).i;
        return from < trans[0].length && to < reverse[0].length;
    }

    // ACCESSORS

    public Object getOne() {
        return one;
    }

    public void setOne(Object one) {
        this.one = one;
    }

    public Object getZero() {
        return zero;
    }

    public void setZero(Object zero) {
        this.zero = zero;
    }
}
