// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.util;

import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;

public class TranslatedSet<T,U> extends AbstractSet<U> {
    public Set<T> set;
    public Translator<T,U> translator;
    
    public TranslatedSet(Set<T> inSet, Translator<T,U> inT) {
        set = inSet;
        translator = inT;
    }

    public class TranslatedIterator implements Iterator<U> {
        public Iterator<T> i;
        public TranslatedIterator(Iterator<T> inI) {
            i = inI;
        }
        
        public boolean hasNext() {
            return i.hasNext();
        }
        
        public U next() {
            return translator.translate(i.next());
        }
        
        public void remove() {
            i.remove();
        }
    }
    public TranslatedIterator iterator() {
        return new TranslatedIterator(set.iterator());
    }
    public int size() {
        return set.size();
    }
}
