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

    private Object one;

    private Object zero;

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
    class TransitionSet implements Set {

        private BitSet from;

        private BitSet[][] trans;

        private BitSet bits;
        
        /**
         * @param fromSet
         *            the set of states indices to take into account
         * @param trans the set of transitions
         */
        public TransitionSet(BitSet fromSet,BitSet[][] trans) {
            this.from = fromSet;
            this.trans = trans;
        }


        public boolean equals(Object obj) {
            TransitionSet ts = (TransitionSet) obj;
            return (ts == null) ? false
                    : (ts.from.equals(from) && ts.trans == trans);
        }

        public int hashCode() {
            return from.hashCode() << 9 ^trans.hashCode() ;
        }

        public String toString() {
            return super.toString();
        }

        private int modcount = 0;

        private int mods = 0;

        private int frombit = -1;
        private int tobit = -1;
        private int lblbit = -1;

        private Iterator it = new Iterator() {

            /*
             *  (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                // NOT IMPLEMENTED
            }

            public boolean hasNext() {
                return from.nextSetBit(frombit) > -1 ;
            }

            public Object next() {
                frombit = from.nextSetBit(frombit);
                if (frombit == -1)
                    throw new NoSuchElementException();
                mods++;
                modcount++;
                if (mods != modcount)
                    throw new ConcurrentModificationException();
                // construct transition
                DefaultStateFactory.DefaultState from = null;/*getStateFactory().new DefaultStateFactory.DefaultState(frombit,false,false);*/
                from.initial = BinaryAutomaton.this.initials().contains(from);
                from.terminal = BinaryAutomaton.this.terminals().contains(from);
                
                DefaultStateFactory.DefaultState to = null; /*new DefaultStateFactory.DefaultState(tobit,false,false);*/
                to.initial = BinaryAutomaton.this.initials().contains(to);
                to.terminal = BinaryAutomaton.this.terminals().contains(to);
                
                Transition tr = new Transition(from,lblbit == 1 ? one : zero,to);
                /* advance iterator */
                //bit++;
                return tr;
            }
        };

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#size()
         */
        public int size() {
            return bits.cardinality();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#clear()
         */
        public void clear() {
            modcount++;
            bits.clear();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#isEmpty()
         */
        public boolean isEmpty() {
            return bits.isEmpty();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#toArray()
         */
        public Object[] toArray() {
            Object[] ret = new Object[size()];
            Iterator it = iterator();
            int i = 0;
            while (it.hasNext()) {
                ret[i++] = it.next();
            }
            return ret;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#add(java.lang.Object)
         */
        public boolean add(Object o) {
            DefaultState ds = (DefaultState) o;
            if (bits.get(ds.i))
                return false;
            bits.set(ds.i);
            modcount++;
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#contains(java.lang.Object)
         */
        public boolean contains(Object o) {
            DefaultState ds = (DefaultState) o;
            return bits.get(ds.i);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#remove(java.lang.Object)
         */
        public boolean remove(Object o) {
            DefaultState ds = (DefaultState) o;
            if (!bits.get(ds.i))
                return false;
            bits.clear(ds.i);
            modcount++;
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#addAll(java.util.Collection)
         */
        public boolean addAll(Collection c) {
        return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#containsAll(java.util.Collection)
         */
        public boolean containsAll(Collection c) {
 return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#removeAll(java.util.Collection)
         */
        public boolean removeAll(Collection c) {
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#retainAll(java.util.Collection)
         */
        public boolean retainAll(Collection c) {
        return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#iterator()
         */
        public Iterator iterator() {
            /* reset iterator */
            frombit = modcount = mods = 0;
            return it;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Set#toArray(java.lang.Object[])
         */
        public Object[] toArray(Object[] a) {
            Object[] ret;
            if (a.length == size())
                ret = a;
            else { /* create array dynamically */
                ret = (Object[]) Array.newInstance(a.getClass()
                        .getComponentType(), size());
            }
            Iterator it = iterator();
            int i = 0;
            while (it.hasNext()) {
                DefaultState ds = (DefaultState) it.next();
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

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#accessibleAndCoAccessibleStates()
     */
    public Set accessibleAndCoAccessibleStates() {
        // TODO Auto-generated method stub
        return super.accessibleAndCoAccessibleStates();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#accessibleStates()
     */
    public Set accessibleStates() {
        // TODO Auto-generated method stub
        return super.accessibleStates();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#accessibleStates(java.util.Set)
     */
    public Set accessibleStates(Set states) {
        // TODO Auto-generated method stub
        return super.accessibleStates(states);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#accessibleStates(rationals.State)
     */
    public Set accessibleStates(State state) {
        // TODO Auto-generated method stub
        return super.accessibleStates(state);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#addState(boolean, boolean)
     */
    public State addState(boolean initial, boolean terminal) {
        DefaultStateFactory.DefaultState st = (DefaultStateFactory.DefaultState) super
                .addState(initial, terminal);
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

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#addTransition(rationals.Transition)
     */
    public void addTransition(Transition transition)
            throws NoSuchStateException {
        // extract states' indices
        DefaultStateFactory.DefaultState from = (DefaultStateFactory.DefaultState) transition
                .start();
        DefaultStateFactory.DefaultState to = (DefaultStateFactory.DefaultState) transition
                .end();
        int i = transition.label() == one ? 1
                : (transition.label() == zero) ? 0 : -1;
        if (i == -1)
            throw new IllegalArgumentException(
                    "Bad transition label for binary automaton");
        // update transition matrix
        try {
            trans[from.i][i].set(to.i);
            reverse[to.i][i].set(from.i);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NoSuchStateException("Invalid from or to state in "
                    + transition);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        // TODO Auto-generated method stub
        return super.clone();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#delta()
     */
    public Set delta() {
        // TODO Auto-generated method stub
        return super.delta();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.StateMachine#delta(java.util.Set)
     */
    public Set delta(Set s) {
        // TODO Auto-generated method stub
        return super.delta(s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#delta(rationals.State, java.lang.Object)
     */
    public Set delta(State state, Object label) {
        // TODO Auto-generated method stub
        return super.delta(state, label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#delta(rationals.State)
     */
    public Set delta(State state) {
        // TODO Auto-generated method stub
        return super.delta(state);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#deltaFrom(rationals.State, rationals.State)
     */
    public Set deltaFrom(State from, State to) {
        // TODO Auto-generated method stub
        return super.deltaFrom(from, to);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#deltaMinusOne(rationals.State, java.lang.Object)
     */
    public Set deltaMinusOne(State state, Object label) {
        // TODO Auto-generated method stub
        return super.deltaMinusOne(state, label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.Rational#deltaMinusOne(rationals.State)
     */
    public Set deltaMinusOne(State st) {
        // TODO Auto-generated method stub
        return super.deltaMinusOne(st);
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
