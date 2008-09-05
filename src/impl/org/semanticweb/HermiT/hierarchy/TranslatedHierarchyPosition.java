// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.util.Set;
import org.semanticweb.HermiT.util.Translator;
import org.semanticweb.HermiT.util.TranslatedSet;

public class TranslatedHierarchyPosition<T, U>
    implements HierarchyPosition<U> {
    public HierarchyPosition<T> position;
    public Translator<T, U> translator;
    PositionTranslator<T, U> positionTranslator;

    public TranslatedHierarchyPosition(HierarchyPosition<T> inPos,
                                        Translator<T, U> inTranslator) {
        position = inPos;
        translator = inTranslator;
        positionTranslator = new PositionTranslator<T, U>(translator);
    }
    
    public Set<U> getEquivalents() {
        return new TranslatedSet<T, U>(position.getEquivalents(), translator);
    }
    
    public Set<U> getAncestors() {
        return new TranslatedSet<T, U>(position.getAncestors(), translator);
    }
    
    public Set<U> getDescendants() {
        return new TranslatedSet<T, U>(position.getDescendants(), translator);
    }
    
    public Set<HierarchyPosition<U>> getParentPositions() {
        return new TranslatedSet<HierarchyPosition<T>, HierarchyPosition<U>>
            (position.getParentPositions(), positionTranslator);
    }
    
    public Set<HierarchyPosition<U>> getChildPositions() {
        return new TranslatedSet<HierarchyPosition<T>, HierarchyPosition<U>>
            (position.getChildPositions(), positionTranslator);
    }
    
    public Set<HierarchyPosition<U>> getAncestorPositions() {
        return new TranslatedSet<HierarchyPosition<T>, HierarchyPosition<U>>
            (position.getAncestorPositions(), positionTranslator);
    }
    
    public Set<HierarchyPosition<U>> getDescendantPositions() {
        return new TranslatedSet<HierarchyPosition<T>, HierarchyPosition<U>>
            (position.getDescendantPositions(), positionTranslator);
    }
    
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj instanceof TranslatedHierarchyPosition) {
            TranslatedHierarchyPosition<T, U> other =
             (TranslatedHierarchyPosition<T, U>) obj;
            if (other.position.equals(position) &&
                other.translator.equals(translator)) {
                return true;
            }
        }
        return false;
    }
    
    public int hashCode() {
        return position.hashCode();
    }
    
}
