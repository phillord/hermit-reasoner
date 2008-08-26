// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.util;

import java.util.Map;
import java.util.Set;
import java.util.AbstractMap;
import java.util.Iterator;

public class TranslatedMap<KeyT, KeyU, ValT, ValU>
    extends AbstractMap<KeyU, ValU> {
    public Map<KeyT, ValT> map;
    public Translator<KeyT, KeyU> keyTranslator;
    public Translator<Object, KeyT> keyRevTranslator;
    public Translator<ValT, ValU> valTranslator;
    
    public TranslatedMap(Map<KeyT, ValT> inMap,
                         Translator<KeyT, KeyU> inKeyTranslator,
                         Translator<Object, KeyT> inKeyReverseTranslator,
                         Translator<ValT, ValU> inValTranslator) {
        map = inMap;
        keyTranslator = inKeyTranslator;
        keyRevTranslator = inKeyReverseTranslator;
        valTranslator = inValTranslator;
    }
    
    class EntryTranslator implements
        Translator<Map.Entry<KeyT, ValT>, Map.Entry<KeyU, ValU>> {
        class Entry implements Map.Entry<KeyU, ValU> {
            Map.Entry<KeyT, ValT> entry;
            public Entry(Map.Entry<KeyT, ValT> inEntry) {
                entry = inEntry;
            }
            public KeyU getKey() {
                return keyTranslator.translate(entry.getKey());
            }
            public ValU getValue() {
                return valTranslator.translate(entry.getValue());
            }
            public ValU setValue(ValU val) {
                throw new UnsupportedOperationException();
            }
            public boolean equals(Object o) {
                if (o instanceof Map.Entry) {
                    Map.Entry other = (Map.Entry) o;
                    if ((getKey() == null ? other.getKey() == null
                                          : getKey().equals(other.getKey())) &&
                        (getValue()==null ? other.getValue()==null
                                          : getValue().equals(other.getValue()))) {
                        return true;
                    }
                }
                return false;
            }
            public int hashCode() {
                return entry.hashCode();
            }
        }
        
        public Map.Entry<KeyU, ValU> translate(Map.Entry<KeyT, ValT> inEntry) {
            return new Entry(inEntry);
        }
    }

    public Set<Map.Entry<KeyU, ValU>> entrySet() {
        return new TranslatedSet<Map.Entry<KeyT, ValT>, Map.Entry<KeyU, ValU>>
            (map.entrySet(), new EntryTranslator());
    }
    public boolean containsKey(Object key) {
        try {
            KeyT newKey = keyRevTranslator.translate(key);
            return map.containsKey(newKey);
        } catch (ClassCastException e) {
            return false;
        }
    }
    public ValU get(Object key) {
        try {
            KeyT newKey = keyRevTranslator.translate(key);
            return valTranslator.translate(map.get(newKey));
        } catch (ClassCastException e) {
            return null;
        }
    }
}
