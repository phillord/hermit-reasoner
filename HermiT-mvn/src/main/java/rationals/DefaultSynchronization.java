/*______________________________________________________________________________
 * 
 * Copyright 2005 Arnaud Bailly - NORSYS/LIFL
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * (3) The name of the author may not be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on 1 avr. 2005
 *
 */
package rationals;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Default synchronization scheme for standard automaton. This class
 * synchronizes the labels of two transitions if they are equal.
 * 
 * @author nono
 * @version $Id: DefaultSynchronization.java 2 2006-08-24 14:41:48Z oqube $
 */
public class DefaultSynchronization implements Synchronization {

    @Override
    public Object synchronize(Object t1, Object t2) {
        return t1 == null ? null : (t1.equals(t2) ? t1 : null);
    }

    @Override
    public <T> Set<T> synchronizable(Set<T> a, Set<T> b) {
        Set<T> r = new HashSet<>(a);
        r.retainAll(b);
        return r;
    }

    @Override
    public <T> Set<T> synchronizable(Collection<Set<T>> alphl) {
        Set<T> niou = new HashSet<>();
        /*
         * synchronization set is the union of pairwise intersection of the sets
         * in alphl
         */
        for (Set<T> s : alphl) {
            for (Set<T> b : alphl) {
                niou.addAll(synchronizable(s, b));
            }
        }
        return niou;
    }

    @Override
    public boolean synchronizeWith(Object object, Set<Object> alph) {
        return alph.contains(object);
    }

}