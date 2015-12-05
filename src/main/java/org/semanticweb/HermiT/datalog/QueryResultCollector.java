package org.semanticweb.HermiT.datalog;

import org.semanticweb.HermiT.model.Term;

public interface QueryResultCollector {
    void processResult(ConjunctiveQuery conjunctiveQuery,Term[] result);
}
