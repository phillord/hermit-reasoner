package org.semanticweb.HermiT.blocking;

import org.semanticweb.HermiT.tableau.Node;

/**
 * Checks whether the rules from some set are applicable given the current state of the extensions.
 */
public interface BlockingValidator {
    public boolean isBlockValid(Node blocked);
    public void blockerChanged(Node node);
}
