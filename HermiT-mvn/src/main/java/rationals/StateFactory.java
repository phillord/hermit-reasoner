package rationals;

import java.util.Set;

/**
 * This class is used by Automaton objects to create new states on A user can
 * implement its own version of StateFactory by providing an implementation for
 * createState
 * 
 * @author Arnaud.Bailly - bailly@lifl.fr
 * @version $Id: StateFactory.java 10 2007-05-30 17:25:00Z oqube $
 */
public interface StateFactory {

    /**
     * @param initial initial
     * @param terminal terminal
     * @return state
     */
    State create(boolean initial, boolean terminal);

    /**
     * Return a new empty set that can contains State instances created by this
     * factory. This method is provided for optimisation purposes so that more
     * efficient implementations than plain sets can be used for handling sets
     * of states.
     * 
     * @return an - opaque - implementation of Set.
     */
    Set<State> stateSet();

    /**
     * Returns a new StateFactory object which is the same as this StateFactory.
     * 
     * @return an initialized StateFactory.
     * @throws CloneNotSupportedException if clone not supported
     */
    Object clone() throws CloneNotSupportedException;
}
// /*
// * $Log: StateFactory.java,v $
// * Revision 1.3 2004/07/20 13:21:25 bonte
// * *** empty log message ***
// *
// */=======
// /*
// * $Log: StateFactory.java,v $
// * Revision 1.3 2004/07/20 13:21:25 bonte
// * *** empty log message ***
// *
// * Revision 1.2 2004/07/19 06:39:02 bailly
// * made Automaton, State and Transition subclasses of Graph API
// * modified StateFactory API
// *
