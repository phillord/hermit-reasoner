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
import java.util.Set;

/**
 * An interface for defining various synchronization schemes. This interface allows various
 * strategies of synchronization between transitions of two automata.
 * 
 * @author nono
 * @version $Id: Synchronization.java 2 2006-08-24 14:41:48Z oqube $
 */
public interface Synchronization {

    /**
     * Synchronize two transitions. This method should return a letter denoting
     * the result of synchronizing the two transitions' labels. If the result is
     * <code>null</code>, then no synchronization occurs.
     * 
     * @param t1
     *            first label to synchronize
     * @param t2
     *            second label to synchronize
     * @return a non null Object if the two transitions can be synchronized.
     */
    Object synchronize(Object t1, Object t2);

    /**
     * Compute the synchronizable letters from two alphabets. This method
     * returns the set of letters from a and b that can be synchronized. In the
     * default case, this method simply computes the intersection of the two
     * sets.
     * 
     * @param a
     *            an alphabet
     * @param b
     *            another alphabet
     * @param <T> type
     * @return a new Set of letters (may be empty) from a and b that can be
     *         synchronized.
     */
    <T> Set<T> synchronizable(Set<T> a, Set<T> b);

    /**
     * Construct the synchronization alphabet from a collection of alphabets.
     * 
     * @param alphl alphl
     * @param <T> type
     * @return a Set implementation containing all letters of all alphabets in
     *         <code>alphl</code> that could be synchronized.
     */
    <T> Set<T> synchronizable(Collection<Set<T>> alphl);

    /**
     * Checks whether or not the given letter is synchronizable in the given
     * automaton's alphabet. This method checks in a synchronization dependant
     * way that the given letter pertains to the synchronization set.
     * 
     * @param object
     *            the letter to check
     * @param alph
     *            the alphabet
     * @return true if object is synchronizable with some letter in
     *         <code>alph</code>, false otherwise.
     */
    boolean synchronizeWith(Object object, Set<Object> alph);

}
