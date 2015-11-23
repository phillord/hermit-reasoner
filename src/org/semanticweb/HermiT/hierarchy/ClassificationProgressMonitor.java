/**
 * 
 */
package org.semanticweb.HermiT.hierarchy;

import org.semanticweb.HermiT.model.AtomicConcept;
/**ClassificationProgressMonitor.*/
public interface ClassificationProgressMonitor {
    /**
     * @param element element
     */
    void elementClassified(AtomicConcept element);
}