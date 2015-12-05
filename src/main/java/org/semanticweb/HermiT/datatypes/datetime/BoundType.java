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
package org.semanticweb.HermiT.datatypes.datetime;

public enum BoundType {
    INCLUSIVE,EXCLUSIVE;

    public BoundType getComplement() {
        return values()[1-ordinal()];
    }
    public static BoundType getMoreRestrictive(BoundType boundType1,BoundType boundType2) {
        int maxOrdinal=Math.max(boundType1.ordinal(),boundType1.ordinal());
        return values()[maxOrdinal];
    }
}