package rationals.properties;

import java.util.Iterator;
import java.util.Set;

import rationals.Automaton;
import rationals.State;
import rationals.transformations.TransformationsToolBox;

/**
 * Checks whether an automaton recognizes the empty word. This test assumes that
 * the tested automaton does not contain epsilon (ie. <code>null</code>)
 * transitions.
 * 
 * @author nono
 * @version $Id: ContainsEpsilon.java 2 2006-08-24 14:41:48Z oqube $
 */
public class ContainsEpsilon implements UnaryTest {

    public boolean test(Automaton a) {
        Iterator i = a.initials().iterator();
        Set s = a.getStateFactory().stateSet();
        while (i.hasNext()) {
            State st = (State) i.next();
            if (st.isTerminal())
                return true;
            s.add(st);
            /* compute epsilon closure */
            Set cl = TransformationsToolBox.epsilonClosure(s,a);
            if(TransformationsToolBox.containsATerminalState(cl))
                return true;
        }
        return false;
    }
}
