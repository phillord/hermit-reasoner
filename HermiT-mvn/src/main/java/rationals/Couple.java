/*
 * Created on Jul 13, 2004 By bonte
 *  
 */
package rationals;
/**Couple.*/
public class Couple {

    private final int hash;

    private final State from;

    private final State to;

    /**
     * @param from from
     * @param to to
     */
    public Couple(State from, State to) {
        this.from = from;
        this.to = to;
        this.hash = (from.hashCode() << 16) ^ to.hashCode();
    }

    /**
     * @return from
     */
    public State getFrom() {
        return from;
    }

    /**
     * @return to
     */
    public State getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if ((o != null) && (o instanceof Couple)) {
            Couple c = (Couple) o;
            return from.equals(c.from) && to.equals(c.to);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}

/*
 * $Log: Couple.java,v $ Revision 1.2 2004/09/21 11:50:28 bailly added interface
 * BinaryTest added class for testing automaton equivalence (isomorphism of
 * normalized automata) added computation of RE from Automaton Revision 1.1
 * 2004/07/19 06:39:02 bailly made Automaton, State and Transition subclasses of
 * Graph API modified StateFactory API
 * 
 */