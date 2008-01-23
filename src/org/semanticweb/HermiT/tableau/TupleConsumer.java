package org.semanticweb.HermiT.tableau;

public interface TupleConsumer {
    void consumeTuple(Object[] tuple,DependencySet[] dependencySets);
}
