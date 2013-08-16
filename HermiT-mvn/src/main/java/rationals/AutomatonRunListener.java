/*______________________________________________________________________________
*
* Copyright 2003 Arnaud Bailly - NORSYS/LIFL
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
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
* OF THE POSSIBILITY OF SUCH DAMAGE.
*______________________________________________________________________________
*
* Created on Jul 23, 2004
* 
*/
package rationals;

import java.util.Set;

/**
 * An interface to communicate run events.
 * <p>
 * This interface should be implemented by objects interested in being notified
 * of run events, that is the firing of transitions during a run of an automaton.
 * 
 * @author nono
 * @version $Id: AutomatonRunListener.java 2 2006-08-24 14:41:48Z oqube $
 */
public interface AutomatonRunListener {

    /**
     * Callback method for notification of fire events occuring during the 
     * run of an automaton.
     * 
     * @param automatonutomaton where the event took place
     * @param transitions the set of transitions which have been fired
     * @param o the object effectively "read" for firing transitions 
     */
    public void fire(Automaton automaton,Set transitions,Object o);

}

/* 
 * $Log: AutomatonRunListener.java,v $
 * Revision 1.2  2004/07/23 14:36:34  bailly
 * ajout setTag
 *
 * Revision 1.1  2004/07/23 11:59:17  bailly
 * added listener interfaces
 *
*/