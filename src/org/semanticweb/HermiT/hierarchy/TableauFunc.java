// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import org.semanticweb.HermiT.model.AtomicConcept;

public class TableauFunc implements Classifier.Function<AtomicConcept> {
    private TableauSubsumptionChecker checker;
    public TableauFunc(TableauSubsumptionChecker checker) {
        this.checker = checker;
    }
    public boolean doesSubsume(AtomicConcept parent, AtomicConcept child) {
        return checker.isSubsumedBy(child, parent);
    }
}
