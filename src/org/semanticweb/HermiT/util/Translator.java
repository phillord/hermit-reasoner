// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.util;

public interface Translator<T,U> {
    U translate(T t);
}
