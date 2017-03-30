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
package org.semanticweb.HermiT.blocking;

import org.semanticweb.HermiT.tableau.Node;

/**
 * Blocking signature.
 */
public abstract class BlockingSignature {
    protected BlockingSignature m_nextEntry=null;

    /**
     * @return next entry
     */
    public final BlockingSignature getNextEntry() {
        return m_nextEntry;
    }
    /**
     * @param nextEntry next entry
     */
    public void setNextEntry(BlockingSignature nextEntry) {
        m_nextEntry=nextEntry;
    }
    /**
     * @param node node
     * @return true if block
     */
    public abstract boolean blocksNode(Node node);
}
