package rationals;

import java.lang.reflect.Array;
import java.util.BitSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class is used by Automaton objects to create new states on A user can
 * implement its own version of StateFactory by providing an implementation for
 * createState
 * 
 * @author Arnaud.Bailly - bailly@lifl.fr
 * @version Thu Apr 25 2002
 */
public class DefaultStateFactory implements StateFactory, Cloneable {

  public class DefaultState implements State {

    public final int i;

    boolean initial;

    boolean terminal;

    Automaton a;

    DefaultState(int i, boolean initial, boolean terminal) {
      this.i = i;
      this.a = automaton;
      this.initial = initial;
      this.terminal = terminal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see salvo.jesus.graph.Vertex#getObject()
     */
    public Object getObject() {
      return new Integer(i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see salvo.jesus.graph.Vertex#setObject(java.lang.Object)
     */
    public void setObject(Object object) {
      /* NOOP */
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.State#setInitial(boolean)
     */
    public State setInitial(boolean initial) {
      this.initial = initial;
      if(initial)
        a.initials().add(this);
      else
        a.initials().remove(this);
      return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.State#setTerminal(boolean)
     */
    public State setTerminal(boolean terminal) {
      this.terminal = terminal;
      if(terminal)
        a.terminals().add(this);
      else
        a.terminals().remove(this);
      return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.State#isInitial()
     */
    public boolean isInitial() {
      return this.initial;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.State#isTerminal()
     */
    public boolean isTerminal() {
      return this.terminal;
    }

    public String toString() {
      return Integer.toString(i);
    }

    public boolean equals(Object o) {
      try {
        DefaultState ds = (DefaultState) o;
        return (ds.i == i) && (a == ds.a);
      } catch (ClassCastException e) {
        return false;
      }
    }

    public int hashCode() {
      return i;
    }
  }

  class DefaultStateSet implements Set {

    private DefaultStateFactory df;

    /**
     * @param set
     */
    public DefaultStateSet(DefaultStateSet set, DefaultStateFactory df) {
      this.bits = (BitSet) set.bits.clone();
      this.df = df;
    }

    /**
     * 
     */
    public DefaultStateSet(DefaultStateFactory df) {
      this.df = df;
    }

    public boolean equals(Object obj) {
      DefaultStateSet dss = (DefaultStateSet) obj;
      return (dss == null) ? false : (dss.bits.equals(bits) && dss.df == df);
    }

    public int hashCode() {
      return bits.hashCode();
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append('[');
      String b = bits.toString();
      sb.append(b.substring(1, b.length() - 1));
      sb.append(']');
      return sb.toString();
    }

    int modcount = 0;

    int mods = 0;

    int bit = -1;

    BitSet bits = new BitSet();

    Iterator it = new Iterator() {

      public void remove() {
        if (bit > 0)
          bits.clear(bit);
      }

      public boolean hasNext() {
        return bits.nextSetBit(bit) > -1;
      }

      public Object next() {
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
      DefaultStateSet dss = (DefaultStateSet) c;
      bits.or(dss.bits);
      modcount++;
      return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c) {
      DefaultStateSet dss = (DefaultStateSet) c;
      BitSet bs = new BitSet();
      bs.or(bits);
      bs.and(dss.bits);
      modcount++;
      return bs.equals(dss.bits);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Set#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c) {
      DefaultStateSet dss = (DefaultStateSet) c;
      bits.andNot(dss.bits);
      modcount++;
      return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Set#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection c) {
      DefaultStateSet dss = (DefaultStateSet) c;
      bits.and(dss.bits);
      modcount++;
      return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Set#iterator()
     */
    public Iterator iterator() {
      /* reset iterator */
      bit = modcount = mods = 0;
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
        ret = (Object[]) Array.newInstance(a.getClass().getComponentType(),
            size());
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

  /**
   * Creates a new state which is initial and terminal or not, depending on the
   * value of parameters.
   * 
   * @param initial
   *          if true, this state will be initial; otherwise this state will be
   *          non initial.
   * @param terminal
   *          if true, this state will be terminal; otherwise this state will be
   *          non terminal.
   */
  public State create(boolean initial, boolean terminal) {
    return new DefaultState(id++, initial, terminal);
  }

  /*
   * (non-Javadoc)
   * 
   * @see rationals.StateFactory#stateSet()
   */
  public Set stateSet() {
    return new DefaultStateSet(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see rationals.StateFactory#stateSet(java.util.Set)
   */
  public Set stateSet(Set s) {
    return new DefaultStateSet((DefaultStateSet) s, this);
  }

  public Object clone() {
    DefaultStateFactory cl;
    try {
      cl = (DefaultStateFactory) super.clone();
    } catch (CloneNotSupportedException e) {
      cl = null;
    }
    cl.id = 0;
    return cl;
  }

  /*
   * (non-Javadoc)
   * 
   * @see rationals.StateFactory#setAutomaton(rationals.Automaton)
   */
  public void setAutomaton(Automaton automaton) {
    this.automaton = automaton;
  }
}
