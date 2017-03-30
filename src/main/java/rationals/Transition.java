package rationals;

/**
 * Defines a Transition (an edge from a state to a state) in an Automaton
 * 
 * This class defines the notion of transition of an automaton. a transition is
 * a triple <em>(q , l , q')</em> where <em>q, q'</em> are states and <em>l</em>
 * a label. States <em>q</em> and <em>q'</em> must belong to the same automaton
 * <em>A</em> and the transition may only be used with this automaton <em>A</em>
 * .
 * 
 * @author yroos@lifl.fr
 * @version 1.0
 * @see Automaton
 */
public class Transition {

    private int hash = Integer.MIN_VALUE;

    private final State start;

    private Object label;

    private final State end;

    /**
     * Creates a new transition <em>(q , l , q')</em>.
     * 
     * @param start
     *            the state <em>q</em> for this transition <em>(q , l , q')</em>
     *            .
     * @param label
     *            the label <em>l</em>
     * @param end
     *            the state <em>q'</em> for this transition
     *            <em>(q , l , q')</em>.
     */
    public Transition(State start, Object label, State end) {
        this.start = start;
        this.label = label;
        this.end = end;
    }

    /**
     * Returns the starting state of this transition.
     * 
     * @return the starting state of this transition, that is the state
     *         <em>q</em> for this transition <em>(q , l , q')</em>.
     */
    public State start() {
        return start;
    }

    /**
     * Returns the label this transition.
     * 
     * @return the label state of this transition, that is the object <em>l</em>
     *         for this transition <em>(q , l , q')</em>.
     */
    public Object label() {
        return label;
    }

    /**
     * Returns the ending state of this transition.
     * 
     * @return the ending state of this transition, that is the state
     *         <em>q'</em> for this transition <em>(q , l , q')</em>.
     */
    public State end() {
        return end;
    }

    /**
     * returns a textual representation of this transition.
     * 
     * @return a textual representation of this transition based
     */
    @Override
    public String toString() {
        if (label == null) {
            return "(" + start + " , 1 , " + end + ")";
        } else {
            return "(" + start + " , " + label + " , " + end + ")";
        }
    }

    /**
     * Determines if this transition is equal to the parameter.
     * 
     * @param o
     *            any object.
     * @return true iff this transition is equal to the parameter. That is if
     *         <tt>o</tt> is a transition which is composed same states and
     *         label (in the sense of method <tt>equals</tt>).
     */
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o) {
            return true;
        }
        if (o instanceof Transition) {
            Transition t = (Transition) o;
            if (label != t.label) {
                if (label == null || t.label == null)
                    return false;
                if (!t.label.equals(label))
                    return false;
            }
            return (start == t.start()) && (end == t.end());
        }
        return false;
    }

    @Override
    public int hashCode() {
        /* store computed value */
        if (hash != Integer.MIN_VALUE)
            return hash;
        int x, y, z;
        if (start == null)
            x = 0;
        else
            x = start.hashCode();
        if (end == null)
            y = 0;
        else
            y = end.hashCode();
        if (label == null)
            z = 0;
        else
            z = label.hashCode();
        int t = new java.awt.Point(x, y).hashCode();
        return hash = new java.awt.Point(t, z).hashCode();
    }
}
