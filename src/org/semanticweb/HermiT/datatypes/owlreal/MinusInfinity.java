/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory
   
   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.datatypes.owlreal;

public final class MinusInfinity extends Number {
    private static final long serialVersionUID=-205551124673073593L;
    public static final MinusInfinity INSTANCE=new MinusInfinity();

    private MinusInfinity() {
    }
    public boolean equals(Object that) {
        return this==that;
    }
    public String toString() {
        return "-INF";
    }
    public double doubleValue() {
        throw new UnsupportedOperationException();
    }
    public float floatValue() {
        throw new UnsupportedOperationException();
    }
    public int intValue() {
        throw new UnsupportedOperationException();
    }
    public long longValue() {
        throw new UnsupportedOperationException();
    }
    protected Object readResolve() {
        return INSTANCE;
    }
}
