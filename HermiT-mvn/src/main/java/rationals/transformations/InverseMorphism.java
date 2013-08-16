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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

/**
 * A general  class for applying inverse morphism on rational sets.
 * <p>
 * A morphism is constructed from a {@see java.util.Map} from letters to
 * letters (ie. from Object to Object). An inverse morphism is then computed 
 * by inversing the given map. A morphism is usually surjective, which is
 * not the case of an inverse morphism, unless of course it is also 
 * injective. This means that the image of a single letter may be a
 * set of letters. 
 * </p>
 * <p>
 * 
 * </p>
 *  
 * @author nono
 * @version $Id: InverseMorphism.java 2 2006-08-24 14:41:48Z oqube $
 * @see rationals.transformations.Morphism
 */
public class InverseMorphism implements UnaryTransformation {

    private Map morph;

    public InverseMorphism(Map m) {
        this.morph = inverse(m);
    }
    
    /*
     * create inverse mapping from given map.
     * The key are letters (Object) and the values 
     * are sets of letters (Set).
     */
    private Map inverse(Map m) {
    	Map inv = new HashMap();
    	for(Iterator i = m.entrySet().iterator();i.hasNext();) {
    		Map.Entry e  = (Map.Entry)i.next();
    		Object v = e.getValue();
    		Object k = e.getKey();
    		Set s = (Set)inv.get(v);
    		if(s == null) {
    			s = new HashSet();
    			inv.put(v,s);
    		}
    		s.add(k);
    	}
    	return inv;
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
            Set s = (Set)morph.get(lbl);
            if(s == null)
                try {
                    b.addTransition(new Transition(nss,lbl,nse));
                } catch (NoSuchStateException e) {
                }
            else
                try {
                	for(Iterator j = s.iterator();j.hasNext();)
                		b.addTransition(new Transition(nss,j.next(),nse));
                } catch (NoSuchStateException e1) {
                }
        }
        // handle epsilon's image
        Set s = (Set)morph.get(null);
        if(s != null) {
        	// append auto transition to each state
        	for(Iterator i = b.states().iterator();i.hasNext();){
        		State st = (State)i.next();
        		for(Iterator j = s.iterator();j.hasNext();) {
        			Object o = j.next();
        			try {
						if(o != null)
							b.addTransition(new Transition(st,o,st));
					} catch (NoSuchStateException e) {
					}
        		}
        	}
        }
        return b;
    }

}
