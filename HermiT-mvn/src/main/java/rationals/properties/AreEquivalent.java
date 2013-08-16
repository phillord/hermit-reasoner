/*______________________________________________________________________________
 *
 * Copyright 2004 Arnaud Bailly - NORSYS/LIFL
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
 * Created on Sep 21, 2004
 * 
 */
package rationals.properties;

import rationals.Automaton;

/**
 * Tests that two automata are equivalent according to some equivalence relation
 * between states.
 * <p>
 * Instances of this class are parameterized by an instance of {@link TransducerRelation}.
 * Given any such instance R, and two automata A1=(Q1,q01,T1,S1,d1) and
 * A2=(Q2,q02,T2,S2,d2), we say that <code>A1 R A2</code> iff
 * <code>q01 R q02</code>.
 * 
 * @author nono
 * @version $Id: AreEquivalent.java 2 2006-08-24 14:41:48Z oqube $
 */
public class AreEquivalent implements BinaryTest {

    private Relation relation;

    public AreEquivalent(Relation r) {
        this.setRelation(r);
    }

    /**
     * Defines the relation to be used for computing equivalence.
     * 
     * @param r
     */
    public void setRelation(Relation r) {
        this.relation = r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rationals.tests.BinaryTest#test(rationals.Automaton,
     *      rationals.Automaton)
     */
    public boolean test(Automaton a, Automaton b) {
        relation.setAutomata(a, b);
        return relation.equivalence(a.initials(), b.initials());
    }

}

/*
 * $Log: AreEquivalent.java,v $ Revision 1.1 2005/03/23 07:22:42 bailly created
 * transductions package corrected EpsilonRemover added some tests removed
 * DirectedGRaph Interface from Automaton
 * 
 * Revision 1.3 2005/02/20 21:14:19 bailly added API for computing equivalence
 * relations on automata
 * 
 * Revision 1.2 2004/11/15 12:45:33 bailly changed equivalence algorithm
 * 
 * Revision 1.1 2004/09/21 11:50:28 bailly added interface BinaryTest added
 * class for testing automaton equivalence (isomorphism of normalized automata)
 * added computation of RE from Automaton
 *  
 */