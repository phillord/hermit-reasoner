package org.semanticweb.HermiT.hierarchy;

import java.util.Set;

public interface ClassificationManager<E> {
    boolean isSatisfiable(E element);
    boolean isSubsumedBy(E subelement,E superelement);
    Hierarchy<E> classify(ProgressMonitor<E> progressMonitor,E topElement,E bottomElement,Set<E> elements);

    interface ProgressMonitor<E> {
        void elementClassified(E element);
    }
}
