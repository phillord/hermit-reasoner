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

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
/**Direct blocking checker.*/
public interface DirectBlockingChecker {
    /**
     * @param tableau tableau
     */
    void initialize(Tableau tableau);
    /**
     * Clear.
     */
    void clear();
    /**
     * @param blocker blocker
     * @param blocked blocked
     * @return true if blocked
     */
    boolean isBlockedBy(Node blocker,Node blocked);
    /**
     * @param node node
     * @return hash code
     */
    int blockingHashCode(Node node);
    /**
     * @param node node
     * @return true if blocker
     */
    boolean canBeBlocker(Node node);
    /**
     * @param node node
     * @return true if node is a tree node
     */
    boolean canBeBlocked(Node node);
    /**
     * @param node node
     * @return true if blocking info changed
     */
    boolean hasBlockingInfoChanged(Node node);
    /**
     * @param node node
     */
    void clearBlockingInfoChanged(Node node);
    /**
     * @param node node
     * @return true if changed since validation
     */
    boolean hasChangedSinceValidation(Node node);
    /**
     * @param node node
     * @param hasChanged hasChanged
     */
    void setHasChangedSinceValidation(Node node, boolean hasChanged);
    /**
     * @param node node
     */
    void nodeInitialized(Node node);
    /**
     * @param node node
     */
    void nodeDestroyed(Node node);
    /**
     * @param concept concept
     * @param node node
     * @param isCore isCore
     * @return assertion added
     */
    Node assertionAdded(Concept concept,Node node,boolean isCore);
    /**
     * @param concept concept
     * @param node node
     * @param isCore isCore
     * @return assertion removed
     */
    Node assertionRemoved(Concept concept,Node node,boolean isCore);
    /**
     * @param range range
     * @param node node
     * @param isCore isCore
     * @return assertion added
     */
    Node assertionAdded(DataRange range,Node node,boolean isCore);
    /**
     * @param range range
     * @param node node
     * @param isCore isCore
     * @return assertion removed
     */
    Node assertionRemoved(DataRange range,Node node,boolean isCore);
    /**
     * @param atomicRole atomicRole
     * @param nodeFrom nodeFrom
     * @param nodeTo nodeTo
     * @param isCore isCore
     * @return assertion added
     */
    Node assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    /**
     * @param atomicRole atomicRole
     * @param nodeFrom nodeFrom
     * @param nodeTo nodeTo
     * @param isCore isCore
     * @return assertion removed
     */
    Node assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore);
    /**
     * @param mergeFrom mergeFrom
     * @param mergeInto mergeInto
     * @return merged node
     */
    Node nodesMerged(Node mergeFrom,Node mergeInto);
    /**
     * @param mergeFrom mergeFrom
     * @param mergeInto mergeInto
     * @return unmerged node
     */
    Node nodesUnmerged(Node mergeFrom,Node mergeInto);
    /**
     * @param node node
     * @return blocking signature
     */
    BlockingSignature getBlockingSignatureFor(Node node);
}
