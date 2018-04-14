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
 * Created on 30 avr. 2005
 *
 */
package rationals.algebra;

import java.util.Arrays;

/**
 * Matrix representation of an automaton.
 * <p>
 * The elements of a the matrix are SemiRing objects.
 * 
 * @author nono
 * @version $Id: Matrix.java 6 2006-08-30 08:56:44Z oqube $
 */
public final class Matrix implements SemiRing {

    /* matrices for transitions, initial and terminal states */
    protected final SemiRing[][] matrix;

    private final int line;
    
    private final int col;
    

    /**
     * @param ns line and column number
     */
    public Matrix(int ns) {
        this.line = this.col = ns;
        this.matrix = new SemiRing[ns][ns];
    }

    /**
     * @param l l
     * @param c c
     */
    public Matrix(int l, int c) {
        this.line = l;
        this.col = c;
        matrix = new SemiRing[l][c];
    }

    /**
     * Returns the n <sup>th </sup> power of this matrix.
     * 
     * @param n
     *            the power. Must be positive or null.
     * @param res
     *            matrix where the result should be stored. Must be same size as
     *            this matrix with all elements initialized with null.
     * @return the result Matrix object with transition matrix equals the n
     *         <sup>th </sup> power of this matrix's transition.
     */
    public Matrix power(int n, Matrix res) {
        int l = line;
        if(line != col)
            throw new IllegalStateException("Cannot compute power of a non square matrix");
        SemiRing[][] tmp = new SemiRing[l][l];
        for (int i = 0; i < l; i++)
            Arrays.fill(tmp[i], matrix[0][0].zero());
        for (int k = 0; k <n; k++) {
            for (int i = 0; i < l; i++) {
                for (int j = 0; j < l; j++) {
                    for (int m = 0; m < l; m++) {
                        if (k==0)
                            tmp[i][j] = tmp[i][j].plus(matrix[i][m]
                                    .mult(matrix[m][j]));
                        else
                            tmp[i][j] = tmp[i][j].plus(res.matrix[i][m]
                                    .mult(matrix[m][j]));
                    }
                }
            }
            /* copy to res */
            for (int i = 0; i < l; i++)
                System.arraycopy(tmp[i],0,res.matrix[i],0,l);
        }
        return res;
    }

    /**
     * @return line number
     */
    public int getLine() {
        return line;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line; i++) {
            sb.append("[ ");
            for (int j = 0; j < col; j++) {
                String s = matrix[i][j].toString();
                sb.append(s).append(' ');
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    @Override
    public SemiRing plus(SemiRing s2) {
        if(s2 == null)
            throw new IllegalArgumentException("Null argument");
        Matrix o = (Matrix)s2; // maybe ClassCastException
        if(col != o.col || line != o.line)
            throw new IllegalArgumentException("Incompatible matrices dimensions : cannot add non square matrices");
        int l = line;
        int c = col;
        Matrix res = Matrix.zero(l,c,matrix[0][0]);
        for(int i=0;i<l;i++) 
            for(int j=0;j<c;j++)
                res.matrix[i][j] = matrix[i][j].plus(o.matrix[i][j]);
        return res;               
    }

    @Override
    public SemiRing mult(SemiRing s2) {
        if(s2 == null)
            throw new IllegalArgumentException("Null argument");
        Matrix o = (Matrix)s2; // maybe ClassCastException
        if(col != o.line)
            throw new IllegalArgumentException("Incompatible matrices dimensions");
        int l = line; // lines
        int c = o.col;  // cols
        int m = col;
        Matrix res = Matrix.zero(l,c,matrix[0][0]);
        for(int i=0;i<l;i++) {
            for(int j=0;j<c;j++)
                for(int k=0;k<m;k++){
                   if(k ==0)
                       res.matrix[i][j] = matrix[i][k].mult(o.matrix[k][j]);
                   else
                       res.matrix[i][j] = res.matrix[i][j].plus(matrix[i][k].mult(o.matrix[k][j]));
                }
        }
        return res;
    }

    @Override
    public SemiRing one() {
        if(line != col)
            throw new IllegalStateException("Cannot get unit matrix on non-square matrices");
        return one(line,matrix[0][0]);
    }

    @Override
    public SemiRing zero() {
        return zero(line,col,matrix[0][0]);
    }
    
    /**
     * @return column number
     */
    public int getCol() {
        return col;
    }
    
    /**
     * Factory method for creating Matrix instances with coefficients
     * in a certain SemiRing.
     * @param line line 
     * @param col col 
     * 
     * @param sr a SemiRing instance. Used to get one and zero.
     * @return a new zero matrix.
     */
    public static Matrix zero(int line,int col,SemiRing sr) {
        Matrix m = new Matrix(line,col);
        for(int i=0;i<line;i++)
            for(int j=0;j<col;j++)
                m.matrix[i][j] = sr.zero();
        return m;
    }
    
    /**
     * Factory method for creating unit Matrix instances with coefficients
     * in a certain SemiRing.
     * @param dim dim 
     * 
     * @param sr a SemiRing instance. Used to get one and zero.
     * @return a new unit square matrix.
     */
    public static Matrix one(int dim,SemiRing sr) {
        Matrix m = new Matrix(dim);
        for(int i=0;i<dim;i++)
            for(int j=0;j<dim;j++)
                m.matrix[i][j] = (i == j) ? sr.one() : sr.zero();
        return m;
    }
    
    
}
