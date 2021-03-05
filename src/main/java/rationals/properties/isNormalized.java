package rationals.properties;

import rationals.Automaton;
import rationals.State;

/**
 * Tests if an automaton is normalized.
 * 
 * @see rationals.transformations.Normalizer
 * @author nono
 * @version $Id: isNormalized.java 2 2006-08-24 14:41:48Z oqube $
 */
public class isNormalized implements UnaryTest {
    @Override
    public boolean test(Automaton a) {
        if (a.initials().size() != 1)
            return false;
        if (a.terminals().size() != 1)
            return false;
        State e = a.initials().iterator().next();
        if (!a.deltaMinusOne(e).isEmpty())
            return false;
        e = a.terminals().iterator().next();
        return a.delta(e).isEmpty();
    }
}