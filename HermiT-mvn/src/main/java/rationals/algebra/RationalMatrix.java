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
 * Created on 10 mai 2005
 *
 */
package rationals.algebra;

import java.util.Iterator;
import java.util.Set;

import rationals.Rational;
import rationals.State;
import rationals.Transition;
import rationals.expr.Letter;
import rationals.expr.Plus;
import rationals.expr.RationalExpr;

/**
 * A matrix for representing regular languages.
 * <p>
 * The cell of the matrix are rational expressions made from concatenation,
 * epsilon, letters and union.
 * 
 * @author nono
 * @version $Id: RationalMatrix.java 2 2006-08-24 14:41:48Z oqube $
 * @see rationals.expr
 */
public class RationalMatrix  {

    private Matrix init;
    private Matrix fini;
    private Matrix transitions;
    
    /**
	 * @return Returns the fini.
	 */
	public Matrix getFini() {
		return fini;
	}

	/**
	 * @param fini The fini to set.
	 */
	public void setFini(Matrix fini) {
		this.fini = fini;
	}

	/**
	 * @return Returns the init.
	 */
	public Matrix getInit() {
		return init;
	}

	/**
	 * @param init The init to set.
	 */
	public void setInit(Matrix init) {
		this.init = init;
	}

	/**
	 * @return Returns the transitions.
	 */
	public Matrix getTransitions() {
		return transitions;
	}

	/**
	 * @param transitions The transitions to set.
	 */
	public void setTransitions(Matrix transitions) {
		this.transitions = transitions;
	}

	/**
     * Construct the matrix of a rational language.
     * 
     * @param rat
     *            a Rational language.
     */
    public RationalMatrix(Rational rat) {
        Set st = rat.states();
        int n = st.size();
        init = Matrix.zero(1,n,RationalExpr.zero);
        fini = Matrix.zero(n,1,RationalExpr.zero);
        transitions = Matrix.zero(n,n,RationalExpr.zero);
        State[] sta = (State[]) rat.states().toArray(new State[n]);
        /* fill matrices */
        for (int i = 0; i < sta.length; i++) {
            if (sta[i].isInitial())
                init.matrix[0][i] = Letter.epsilon;
            else 
                init.matrix[0][i] = RationalExpr.zero;
            if (sta[i].isTerminal())
                fini.matrix[i][0] = Letter.epsilon;
            else 
                fini.matrix[i][0] = RationalExpr.zero;
            /* transitions */
            for (int j = 0; j < n; j++) {
                Set trs = rat.deltaFrom(sta[i], (State) sta[j]);
                RationalExpr re = null;
                for (Iterator it = trs.iterator(); it.hasNext();) {
                    Transition tr = (Transition) it.next();
                    Object o = tr.label();
                    Letter l = (o == null) ? Letter.epsilon : new Letter(o);
                    if (re == null)
                        re = l;
                    else
                        re = new Plus(re, l);
                }
                transitions.matrix[i][j] = re == null ? RationalExpr.zero : re;
            }
        }
    }

    /**
     * Compute words from this rational whose length is 
     * n.
     * 
     * @param n
     * @return
     */
    public Matrix nwords(int n) {
        Matrix res = transitions.power(n,Matrix.zero(transitions.getLine(),transitions.getLine(),RationalExpr.zero));
        /* compute product for init and fini */
        Matrix in = (Matrix)init.mult(res);
        return (Matrix)in.mult(fini);
    }

    public String toString() {
        return init.toString() + '\n'+ transitions.toString() + '\n'+fini.toString();
    }
}
