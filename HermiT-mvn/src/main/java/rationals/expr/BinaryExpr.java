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

/**
 * @author nono
 * @version $Id: BinaryExpr.java 2 2006-08-24 14:41:48Z oqube $
 */
public abstract class BinaryExpr extends RationalExpr {
    
    private RationalExpr left;
    private RationalExpr right;

    /**
     * Construct a binary expression from two sub-expressions.
     * 
     * @param e e
     * @param f f
     */
    public BinaryExpr(RationalExpr e,RationalExpr f) {
        this.left = e ;
        this.right = f;
    }
    
    /**
     * @return Returns the left.
     */
    public RationalExpr getLeft() {
        return left;
    }
    /**
     * @param left The left to set.
     */
    public void setLeft(RationalExpr left) {
        this.left = left;
    }
    /**
     * @return Returns the right.
     */
    public RationalExpr getRight() {
        return right;
    }
    /**
     * @param right The right to set.
     */
    public void setRight(RationalExpr right) {
        this.right = right;
    }
    
}
