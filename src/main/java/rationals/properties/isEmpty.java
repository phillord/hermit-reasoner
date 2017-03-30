package rationals.properties;

import java.util.Iterator;

import rationals.Automaton;
import rationals.State;

/**
 * Empty test.
 */
public class isEmpty implements UnaryTest {
    @Override
    public boolean test(Automaton a) {
        Iterator<State> i = a.accessibleStates().iterator();
        while (i.hasNext()) {
            if (i.next().isTerminal())
                return false;
        }
        return true;
    }
}
