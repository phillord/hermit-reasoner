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
package rationals.expr;

import rationals.algebra.SemiRing;

/**
 * @author nono
 * @version $Id: RationalExpr.java 2 2006-08-24 14:41:48Z oqube $
 */
public abstract class RationalExpr implements SemiRing {

    static final RationalExpr zero = new RationalExpr() {
        
        @Override
        public SemiRing mult(SemiRing s1) {
            return zero;
        }

        @Override
        public SemiRing plus(SemiRing s1) {
            return s1;
        }
        
        @Override
        public boolean equals(Object o) {
            return this == o;
        }
        
        @Override
        public int hashCode() {
            return -1;
        }
        
        @Override
        public String toString() {
            return "0";
        }
    };

    static final RationalExpr epsilon = new RationalExpr() {

        @Override
        public boolean equals(Object o) {
            return o == epsilon;
        }
        

        @Override
        public SemiRing mult(SemiRing s2) {
            return s2;
        }
        
        @Override
        public String toString() {
            return "1";
        }

        @Override
        public int hashCode() {
            return 0;
        }
    };

    static final RationalExpr one = RationalExpr.epsilon;
    
    @Override
    public final SemiRing one() {
        return one;
    }
    
    @Override
    public final SemiRing zero() {
        return zero;
    }

    @Override
    public SemiRing mult(SemiRing s2) {
        if(s2 == zero)
            return zero;
        if(s2 == RationalExpr.epsilon)
            return this;
        RationalExpr re = (RationalExpr)s2;
        return new Product(this,re);
    }
    
    @Override
    public SemiRing plus(SemiRing s2) {
        if(s2 == zero)
            return this;
        return new Plus(this,(RationalExpr)s2);
    }

}
