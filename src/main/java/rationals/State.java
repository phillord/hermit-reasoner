package rationals;

/**
 * Interface for State objects
 * 
 * This class defines the notion of state of an automaton.
 * 
 * @author yroos@lifl.fr
 * @version 1.0
 * @see Automaton
 * @see StateFactory
 */
public interface State {

    /**
     * Determines if this state is initial.
     * 
     * @return true iff this state is initial.
     */
    boolean isInitial();

    /**
     * Determines if this state is terminal.
     * 
     * @return true iff this state is terminal.
     */
    boolean isTerminal();
}
