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
 * Created on 18 mars 2005
 *
 */
package rationals;

import java.util.List;
import java.util.Set;

/**
 * @author nono
 * @version $Id: Acceptor.java 10 2007-05-30 17:25:00Z oqube $
 */
public interface Acceptor {
    /**
     * Checks this automaton accepts the given "word".
     * A word is a list of objects. This method checks that reading <code>word</code>
     * starting from initials state leads to at least one terminal state.
     * 
     * @param word
     * @return
     */
    boolean accept(List<Object> word);

    /**
     * Return a trace of states reading word from start state. 
     * If start state is null, assume reading from
     * initials().
     * This method returns a List of Set objects showing all the states 
     * reached by this run while reading <code>word</code> starting from <code>start</code>.
     * 
     * @param word a List of objects in this automaton's alphabet
     * @param start a starting State. Maybe null
     * @return a List of Set of State objects
     */
    List<Set<State>> traceStates(List<Object> word, State start);
    
    /*
     *  (non-Javadoc)
     * @see rationals.Acceptor#steps(java.util.List)
     */
    Set<State> steps(List<Object> word);

}