/* Copyright 2009 by the Oxford University Computing Laboratory
   
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

package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.Prefixes;


/**
 * Represents a data range in a DL clause.
 */
public abstract class DataRange implements Serializable {
    private static final long serialVersionUID=352467050584766830L;

    public abstract boolean isAlwaysTrue();
    public abstract boolean isAlwaysFalse();
    public int getArity() {
        return 1;
    }
    public abstract String toString(Prefixes prefixes);
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
}
