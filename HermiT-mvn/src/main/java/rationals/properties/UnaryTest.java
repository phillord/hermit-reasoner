package rationals.properties;

import rationals.Automaton;

/**
 * Unary test.
 */
public interface UnaryTest {
    /**
     * @param a a
     * @return true if test passes
     */
    boolean test(Automaton a) ;
}