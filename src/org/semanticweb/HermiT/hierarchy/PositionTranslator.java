// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.io.Serializable;

import org.semanticweb.HermiT.util.Translator;

public class PositionTranslator<T, U>
    implements Serializable, Translator<HierarchyPosition<T>, HierarchyPosition<U>> {
    private static final long serialVersionUID = -7002747276282495243L;
    Translator<T, U> translator;
    public PositionTranslator(Translator<T, U> inTranslator) {
        translator = inTranslator;
    }
    public HierarchyPosition<U> translate(HierarchyPosition<T> inPos) {
        return new TranslatedHierarchyPosition<T, U>(inPos, translator);
    }
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj instanceof PositionTranslator
            && ((PositionTranslator<T, U>) obj).translator.equals(translator)) {
            return true;
        } else {
            return false;
        }
    }
    public int hashCode() {
        return translator.hashCode();
    }
}
