package rationals.transformations;

import java.util.Collection;
import java.util.Set;

import rationals.Automaton;
import rationals.Synchronization;
import java.util.Collections;

/**
 * This class implements the shuffle operator between two automatas.
 * <ul>
 * <li>C = A shuffle B</li>
 * <li>S(C) = { (a,b) | a in S(A) and b in S(B) }</li>
 * <li>S0(C) = (S0(A),SO(B))</li>
 * <li>T(C) = { (a,b) | a in T(A) and b in T(B) }</li>
 * <li>D(C) = { ((s1a,s1b),a,(s2a,s2b)) | exists (s1a,a,s2a) in D(A) or exists
 * (s1b,a,s2b) in D(b) }</li>
 * </ul>
 * This class uses the Mix operator with an empty alphabet to compute the
 * Shuffle.
 * 
 * @author Arnaud Bailly
 * @version $Id: Shuffle.java 2 2006-08-24 14:41:48Z oqube $
 * @see Mix
 */
public class Shuffle implements BinaryTransformation {

    @Override
    public Automaton transform(Automaton a, Automaton b) {

        Mix mix = new Mix(new Synchronization() {
            @Override
            public Object synchronize(Object t1, Object t2) {
                return null;
            }

            @Override
            public <T> Set<T> synchronizable(Set<T> a1,Set<T> b1) {
                return Collections.<T>emptySet();
            }

            @Override
            public <T> Set<T> synchronizable(Collection<Set<T>> alphl) {
                return Collections.<T>emptySet();
            }

            @Override
            public boolean synchronizeWith(Object object, Set<Object> alph) {
                return false;
            }
        });
        return mix.transform(a, b);
    }
}
