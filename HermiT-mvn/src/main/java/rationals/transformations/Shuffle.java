package rationals.transformations;

import java.util.Collection;
import java.util.Set;

import rationals.Automaton;
import rationals.Synchronization;
import java.util.Collections;
import java.util.HashSet;

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
 * @see rationals.transformation.Mix
 */
public class Shuffle implements BinaryTransformation {

    public Automaton transform(Automaton a, Automaton b) {

        Mix mix = new Mix(new Synchronization() {
            public Object synchronize(Object t1, Object t2) {
                return null;
            }

            public Set synchronizable(Set a, Set b) {
                return Collections.unmodifiableSet(new HashSet());
            }

            public Set synchronizable(Collection alphl) {
                // TODO Auto-generated method stub
                return null;
            }

            public boolean synchronizeWith(Object object, Set alph) {
                // TODO Auto-generated method stub
                return false;
            }
        });
        return mix.transform(a, b);
    }
}
