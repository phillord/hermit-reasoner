package org.semanticweb.HermiT.datalog;

import org.semanticweb.HermiT.model.Term;

/**
 * Query result collector.
 */
public interface QueryResultCollector {
    /**
     * @param conjunctiveQuery conjunctiveQuery
     * @param result result
     */
    void processResult(ConjunctiveQuery conjunctiveQuery,Term[] result);
}
