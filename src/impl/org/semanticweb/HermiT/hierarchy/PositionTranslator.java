// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import org.semanticweb.HermiT.util.Translator;

public class PositionTranslator<T, U>
    implements Translator<HierarchyPosition<T>, HierarchyPosition<U>> {
    Translator<T, U> translator;
    public PositionTranslator(Translator<T, U> inTranslator) {
        translator = inTranslator;
    }
    public HierarchyPosition<U> translate(HierarchyPosition<T> inPos) {
        return new TranslatedHierarchyPosition<T, U>(inPos, translator);
    }
    public boolean equals(Object obj) {
        if (obj instanceof PositionTranslator
            && ((PositionTranslator) obj).translator.equals(translator)) {
            return true;
        } else {
            return false;
        }
    }
    public int hashCode() {
        return translator.hashCode();
    }
}
