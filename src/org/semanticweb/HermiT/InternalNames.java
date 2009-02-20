// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.util.Map;

public class InternalNames {
    public static boolean isInternalURI(String uri) {
        return uri.startsWith("internal:");
    }
    
    /**
     * Return a set of namespaces with both the contents of `n` and
     * prefices for all internal concepts, including shorthands for
     * internal nominal concepts based on the declarations from 'n'.
     */
    static public Namespaces withInternalNamespaces(Namespaces n) {
        Map<String, String> decl
            = new java.util.HashMap<String, String>();
        decl.put("q", "internal:q#");
        decl.put("nnq", "internal:nnq#");
        decl.put("amq", "internal:amq#");
        decl.put("all", "internal:all#");
        for (Map.Entry<String, String> e : n.getDeclarations().entrySet()) {
            String prefix = "nom";
            if (e.getKey() != null && e.getKey().length() > 0) {
                prefix = prefix + "-" + e.getKey();
            }
            decl.put(prefix, "internal:nom$" + e.getValue());
        }
        return new Namespaces(decl, n);
    }
}
