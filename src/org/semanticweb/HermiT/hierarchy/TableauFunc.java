// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.io.Serializable;

import org.semanticweb.HermiT.model.AtomicConcept;

public class TableauFunc implements Serializable, Classifier.Function<AtomicConcept> {
    private static final long serialVersionUID = 3723952999204649625L;
    private TableauSubsumptionChecker checker;
    public TableauFunc(TableauSubsumptionChecker checker) {
        this.checker = checker;
    }
    public boolean doesSubsume(AtomicConcept parent, AtomicConcept child) {
        return checker.isSubsumedBy(child, parent);
    }
}
