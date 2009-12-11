package org.semanticweb.HermiT.hierarchy;

import java.util.Set;

public interface SubsumptionCache<E> {
    Set<E> getAllKnownSubsumers(E element);
    boolean isSatisfiable(E element);
    boolean isSubsumedBy(E subelement,E superelement);
}
