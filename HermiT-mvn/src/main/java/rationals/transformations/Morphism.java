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
 * Created on 6 mai 2005
 *
 */
package rationals.transformations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

/**
 * A general  class for alphabetic morphism over automaton.
 * <p>
 * A morphism is constructed from a {@see java.util.Map} from letters to
 * letters (ie. from Object to Object). To distinguish between explicit 
 * mapping to <code>null</code> and implicit identity, if a letter is mapped 
 * as is, then it should not be included as a key.
 *  
 * @author nono
 * @version $Id: Morphism.java 2 2006-08-24 14:41:48Z oqube $
 */
public class Morphism implements UnaryTransformation {

    private Map morph;

    public Morphism(Map m) {
        this.morph = m;
    }
    
    /* (non-Javadoc)
     * @see rationals.transformations.UnaryTransformation#transform(rationals.Automaton)
     */
    public Automaton transform(Automaton a) {
        Automaton b = new Automaton();
        /* state map */
        Map stm = new HashMap();
        for(Iterator i = a.delta().iterator();i.hasNext();) {
            Transition tr = (Transition)i.next();
            State ns = tr.start();
            State nss = (State)stm.get(ns);
            if(nss == null) {
                nss = b.addState(ns.isInitial(),ns.isTerminal());
                stm.put(ns,nss);
            }
            State ne = tr.end();
            State nse = (State)stm.get(ne);
            if(nse == null) {
                nse = b.addState(ne.isInitial(),ne.isTerminal());
                stm.put(ne,nse);
            }
            Object lbl = tr.label();
            if(!morph.containsKey(lbl))
                try {
                    b.addTransition(new Transition(nss,lbl,nse));
                } catch (NoSuchStateException e) {
                }
            else
                try {
                    b.addTransition(new Transition(nss,morph.get(lbl),nse));
                } catch (NoSuchStateException e1) {
                }
        }
        return b;
    }

}
