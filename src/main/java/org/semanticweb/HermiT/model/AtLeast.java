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
package org.semanticweb.HermiT.model;



/**
 * Represents at-least concepts, either for data ranges or concepts.
 */
public abstract class AtLeast extends ExistentialConcept implements DLPredicate {
    private static final long serialVersionUID = -5450065396132818872L;
    
    protected final int m_number;
    protected final Role m_onRole;
    
    protected AtLeast(int number,Role onRole) {
        m_number=number;
        m_onRole=onRole;
    }
    public int getNumber() {
        return m_number;
    }
    public Role getOnRole() {
        return m_onRole;
    }
    public int getArity() {
        return 1;
    }
    public boolean isAlwaysTrue() {
        return false;
    }
}
