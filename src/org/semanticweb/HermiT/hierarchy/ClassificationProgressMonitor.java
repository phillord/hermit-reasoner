/**
 * 
 */
package org.semanticweb.HermiT.hierarchy;

import org.semanticweb.HermiT.model.AtomicConcept;

public interface ClassificationProgressMonitor {
    void elementClassified(AtomicConcept element);
}