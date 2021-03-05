package rationals;

import java.util.*;

/**
 * This class is used by Automaton objects to create new states on A user can
 * implement its own version of StateFactory by providing an implementation for
 * createState
 * 
 * @author Arnaud.Bailly - bailly@lifl.fr
 * @version Thu Apr 25 2002
 */
public class DefaultStateFactory implements StateFactory, Cloneable {

    class DefaultState implements State {

        public final int i;

        boolean initial;

        boolean terminal;

        final Automaton a;

        DefaultState(int i, boolean initial, boolean terminal) {
            this.i = i;
            this.a = automaton;
            this.initial = initial;
            this.terminal = terminal;
        }

        @Override
        public boolean isInitial() {
            return this.initial;
        }

        @Override
        public boolean isTerminal() {
            return this.terminal;
        }

        @Override
        public String toString() {
            return Integer.toString(i);
        }

        @Override
        public boolean equals(Object o) {
            if(this==o) {
                return true;
            }
            if(!(o instanceof DefaultState)) {
                return false;
            }
                DefaultState ds = (DefaultState) o;
                return (ds.i == i) && (a == ds.a);
        }

        @Override
        public int hashCode() {
            return i;
        }
    }

    class DefaultStateSet implements Set<State> {

        private final DefaultStateFactory df;

        /**
         * @param df df
         */
        public DefaultStateSet(DefaultStateFactory df) {
            this.df = df;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof DefaultStateSet)) {
                return false;
            }
            DefaultStateSet dss = (DefaultStateSet) obj;
            return dss.bits.equals(bits) && dss.df == df;
        }

        @Override
        public int hashCode() {
            return bits.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            String b = bits.toString();
            sb.append(b.substring(1, b.length() - 1));
            sb.append(']');
            return sb.toString();
        }

        int modcount = 0;

        int mods = 0;

        int bit = -1;

        final BitSet bits = new BitSet();

        final Iterator<State> it = new Iterator<State>() {

            @Override
            public void remove() {
                if (bit > 0)
                    bits.clear(bit);
            }

            @Override
            public boolean hasNext() {
                return bits.nextSetBit(bit) > -1;
            }

            @Override
            public State next() {
                bit = bits.nextSetBit(bit);
                if (bit == -1)
                    throw new NoSuchElementException();
                DefaultState ds = new DefaultState(bit, false, false);
                ds.initial = automaton.initials().contains(ds);
                ds.terminal = automaton.terminals().contains(ds);
                mods++;
                modcount++;
                if (mods != modcount)
                    throw new ConcurrentModificationException();
                /* advance iterator */
                bit++;
                return ds;
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
            Iterator<State> l = iterator();
            int i = 0;
            while (l.hasNext()) {
                ret[i++] = l.next();
            }
            return ret;
        }

        @Override
        public boolean add(State o) {
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
        public boolean addAll(Collection<? extends State> c) {
            DefaultStateSet dss = (DefaultStateSet) c;
            bits.or(dss.bits);
            modcount++;
            return true;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            DefaultStateSet dss = (DefaultStateSet) c;
            BitSet bs = new BitSet();
            bs.or(bits);
            bs.and(dss.bits);
            modcount++;
            return bs.equals(dss.bits);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            DefaultStateSet dss = (DefaultStateSet) c;
            bits.andNot(dss.bits);
            modcount++;
            return true;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            DefaultStateSet dss = (DefaultStateSet) c;
            bits.and(dss.bits);
            modcount++;
            return true;
        }

        @Override
        public Iterator<State> iterator() {
            /* reset iterator */
            bit = modcount = mods = 0;
            return it;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            Iterator<State> iterator = iterator();
            List<T> l = new ArrayList<>();
            while (iterator.hasNext()) {
                l.add((T) iterator.next());
            }
            return l.toArray(a);
        }

    }

    // //////////////////////////////////////////////////////
    // FIELDS
    // /////////////////////////////////////////////////////

    protected int id = 0;

    Automaton automaton;

    // //////////////////////////////////////////////////////
    // PUBLIC METHODS
    // /////////////////////////////////////////////////////

    DefaultStateFactory(Automaton a) {
        this.automaton = a;
    }

    @Override
    public State create(boolean initial, boolean terminal) {
        return new DefaultState(id++, initial, terminal);
    }

    @Override
    public Set<State> stateSet() {
        return new DefaultStateSet(this);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultStateFactory cl = (DefaultStateFactory) super.clone();
        cl.id = 0;
        return cl;
    }
}
