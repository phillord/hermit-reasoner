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
 * Created on 8 avr. 2005
 *
 */
package rationals;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author nono
 * @version $Id: StateFactoryTest.java 2 2006-08-24 14:41:48Z oqube $
 */
@SuppressWarnings("javadoc")
public abstract class StateFactoryTest extends TestCase {

    private final Automaton a;

    /**
     * Constructor for DefaultStateFactoryTest.
     * 
     * @param arg0
     */
    public StateFactoryTest(String arg0, StateFactory sf) {
        super(arg0);
        this.a = new Automaton(sf);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        /* create lots of states */
        for (int i = 0; i < 5000; i++)
            a.addState(i % 500 == 0 ? true : false, i % 600 == 0 ? true : false);
    }

    public void testIterator() {
        Set<State> s = a.initials();
        int i = 0;
        for (Iterator<State> it = s.iterator(); it.hasNext(); i++) {
            State st = it.next();
            assertTrue(st.isInitial());
        }
        assertEquals(10, i);
    }

    public void testIteratorConcur() {
        Set<State> s = a.initials();
        int i = 0;
        for (Iterator<State> it = s.iterator(); it.hasNext(); i++) {
            try {
                it.next();
            } catch (@SuppressWarnings("unused") ConcurrentModificationException ccm) {
                return;
            }
            if (i == 5)
                a.addState(true, false);
        }
        fail("Should have thrown concurrent modification exception");
    }

    public void testIteratorNoSuchElement() {
        Set<State> s = a.initials();
        Iterator<State> it;
        for (it = s.iterator(); it.hasNext();) {
            it.next();
        }
        try {
            it.next();
            fail("Should have thrown no such element exception");
        } catch (@SuppressWarnings("unused") NoSuchElementException nse) {
        }
    }

    public void testSetAdd() {
        Set<State> s = a.getStateFactory().stateSet();
        State st = a.addState(true, true);
        State s2 = a.addState(false, false);
        s.add(st);
        assertTrue(s.contains(st));
        assertTrue(!s.contains(s2));
    }

    public void testSetAddAll() {
        Set<State> s = a.getStateFactory().stateSet();
        Set<State> i = a.initials();
        s.addAll(i);
        for (Iterator<State> it = i.iterator(); it.hasNext();)
            assertTrue(s.contains(it.next()));
    }

    public void testClear() {
        Set<State> s = a.terminals();
        s.clear();
        assertTrue(s.isEmpty());
    }

    public void testContainsAll() {
        Set<State> s = a.getStateFactory().stateSet();
        Set<State> i = a.initials();
        s.addAll(i);
        s.addAll(a.terminals());
        assertTrue(s.containsAll(a.initials()) && s.containsAll(a.terminals()));
    }

    public void testEquals() {
        Set<State> s = a.getStateFactory().stateSet();
        Set<State> i = a.initials();
        s.addAll(i);
        assertEquals(s,i);
    }

    public void testRemove() {
        Set<State> s = a.getStateFactory().stateSet();
        Set<State> i = a.initials();
        s.addAll(i);
        Iterator<State> it = i.iterator();
        State st = it.next();
        s.remove(st);
        while (it.hasNext())
            assertTrue(s.contains(it.next()));
        assertTrue(!s.contains(st));
    }

    public void testRemoveAll() {
        Set<State> s = a.states();
        s.removeAll(a.initials());
        for (Iterator<State> it = s.iterator(); it.hasNext();)
            if (it.next().isInitial())
                fail("Not removed all initial states");
    }

    public void testRetainAll() {
        Set<State> s = a.initials();
        s.retainAll(a.terminals());
        /* should contain state 0 and 3000 */
        assertEquals(2,s.size());
    }

}
