// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class ReasoningCache {
    public Map<AtomicConcept, Set<AtomicConcept>>
        knownSubsumers = new HashMap<AtomicConcept, Set<AtomicConcept>>();
        // no entry means none
    public Map<AtomicConcept, Set<AtomicConcept>>
        possibleSubsumers = new HashMap<AtomicConcept, Set<AtomicConcept>>();
        // no entry means all
    
    public boolean allSubsumptionsKnown(Collection<AtomicConcept> concepts) {
        for (AtomicConcept c : concepts) {
            if (!knownSubsumers.containsKey(c)) return false;
            Set<AtomicConcept> poss = possibleSubsumers.get(c);
            if (poss == null || !poss.isEmpty()) return false;
        }
        return true;
    }
    
    public void seed(Collection<AtomicConcept> concepts, Tableau tableau) {
        for (AtomicConcept c : concepts) {
            if (!tableau.isSatisfiable(c)) {
                knownSubsumers.put(c, new HashSet<AtomicConcept>(concepts));
                possibleSubsumers.put(c, new HashSet<AtomicConcept>());
            } else {
                //cache.update(m_tableau.getCheckedNode().getCanonicalNode(), c);
                Node node = tableau.getCheckedNode().getCanonicalNode();
                Set<AtomicConcept> detConcepts = new HashSet<AtomicConcept>();
                Set<AtomicConcept> nondetConcepts = new HashSet<AtomicConcept>();
                { // Retrieve info from tableau:
                    detConcepts.add(AtomicConcept.THING);
                    ExtensionTable.Retrieval retrieval
                        = tableau.getExtensionManager()
                            .getBinaryExtensionTable()
                            .createRetrieval(new boolean[] { false,true },
                                             ExtensionTable.View.TOTAL);
                    retrieval.getBindingsBuffer()[1] = node;
                    for (retrieval.open(); !retrieval.afterLast(); retrieval.next()) {
                        Object obj = retrieval.getTupleBuffer()[0];
                        if (obj instanceof AtomicConcept) {
                            AtomicConcept d = (AtomicConcept) obj;
                            if (!Namespaces.isInternalURI(d.getURI())) {
                                if (retrieval.getDependencySet().isEmpty()) {
                                    detConcepts.add(d);
                                } else {
                                    nondetConcepts.add(d);
                                }
                            }
                        }
                    }
                } // done retrieving info from tableau
                { // update information about c:
                    Set<AtomicConcept> set = knownSubsumers.get(c);
                    if (set == null) {
                        set = new HashSet<AtomicConcept>();
                        knownSubsumers.put(c, set);
                    }
                    set.addAll(detConcepts);
                    set = possibleSubsumers.get(c);
                    if (set == null) {
                        set = new HashSet<AtomicConcept>(nondetConcepts);
                        possibleSubsumers.put(c, set);
                    } else {
                        set.retainAll(nondetConcepts);
                    }
                } // done updating information about c
                nondetConcepts.addAll(detConcepts);
                for (AtomicConcept d : nondetConcepts) {
                    Set<AtomicConcept> set = possibleSubsumers.get(d);
                    if (set == null) {
                        set = new HashSet<AtomicConcept>(nondetConcepts);
                        possibleSubsumers.put(d, set);
                    } else {
                        set.retainAll(nondetConcepts);
                    }
                }
            } // end if isSatisfiable(c)
        } // end for
    } // end function seed
    
}