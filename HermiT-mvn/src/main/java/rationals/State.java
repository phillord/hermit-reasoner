package rationals;


/** 
 * Interface for State objects
 * 
 * This class defines the notion of state of an automaton. 
 * @author yroos@lifl.fr
 * @version 1.0
 * @see Automaton
 * @see StateFactory
*/
public interface State {


    /** Determines if this state is initial.
     *  @return true iff this state is initial.
     */
    public boolean isInitial();
    
    /** Determines if this state is terminal.
     *  @return true iff this state is terminal.
     */
    public boolean isTerminal();
    
    /** returns a textual representation of this state.
     *  @return a textual representation of this state.
     */
    public String toString();

    /**
     * Sets the initial status of this state.
     * 
     * @param initial
     * @return
     */
    public State setInitial(boolean initial);

    public State setTerminal(boolean terminal);
}
