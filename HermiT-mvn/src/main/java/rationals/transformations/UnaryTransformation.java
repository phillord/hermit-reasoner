package rationals.transformations ;

import rationals.Automaton;

/**
 * Unary transformation.
 */
public interface UnaryTransformation {
    /**
     * @param a a
     * @return automatom
     */
    Automaton transform(Automaton a) ;
}
