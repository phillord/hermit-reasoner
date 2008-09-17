// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This class is responsible for mapping between URIs and "identifiers", which
 * can be given in any of three different formats:
 *  1) &lt;uri&gt;
 *  2) prefix:localpart
 *  3) localpart
 * Forms 2 and 3 are dependent upon a set of namespace declarations which
 * associates namespaces with prefices. An identifier encoded in form 2 which
 * uses an unregistered prefix is invalid---expanding it will result in an
 * exception. Form 3 can only be used if a "default namespace" (a namespace
 * associated with an empty prefix) has been declared.
 * Neither prefices nor localparts may contain colon characters.
 */
public class Namespaces implements Serializable {
    private static final long serialVersionUID=-158185482289831766L;
    
    /** The map of prefixes to the corresponding URI. */
    private Map<String, String> namespaceByPrefix;
    
    /** The map of URIs to prefixes. */
    private Map<String, String> prefixByNamespace;
    
    // // Little optimization to make abbreviating fast(er):
    private Pattern abbrPattern;
    
    /**
     * Create a new Namespaces object from a set of prefix declarations and
     * a sequence of other Namespaces objects to copy.
     * 
     * If multiple prefices are associated with the same namespace name in
     * `declarations`, then one of the prefices will be chosen arbitrarily
     * as the canonical abbreviation. To bind multiple prefices to the same
     * namespace name but use the specific prefix `p` as the canonical
     * abbreviation, first create a Namespaces object `n` defining all the
     * prefices and then derive a new Namespaces object from `n` and
     * declarations for the canonical prefices.
     *
     * @param prefixDeclarations    map from prefices to namespace names
     * @param sources   other declarations to include, in order of precedence
     */
    public Namespaces(Map<String, String> prefixDeclarations,
                    Namespaces... sources) {
        init(prefixDeclarations, sources);
    }
    
    /**
     * Create a new Namespaces object with declarations copied from
     * `sources` (given in order of precedence) and using `defaultNamespace`
     * as (surprisingly enough) a default namespace.
     */
    public Namespaces(String defaultNamespace, Namespaces... sources) {
        Map<String, String> decl = new TreeMap<String, String>();
        decl.put("", defaultNamespace);
        init(decl, sources);
    }
    
    private void init(Map<String, String> prefixDeclarations,
                        Namespaces... sources) {
        namespaceByPrefix = new TreeMap<String,String>();
        prefixByNamespace = new TreeMap<String,String>();

        // Copy backwards to preserve precedence:
        for (int i = sources.length - 1; i >= 0; --i) {
            namespaceByPrefix.putAll(sources[i].namespaceByPrefix);
            prefixByNamespace.putAll(sources[i].prefixByNamespace);
        }
        // highest precedence are the new declarations:
        for (Map.Entry<String, String> e : prefixDeclarations.entrySet()) {
            namespaceByPrefix.put(e.getKey(), e.getValue());
            prefixByNamespace.put(e.getValue(), e.getKey());
        }
        
        // Build the regex to identify namespaces in URLs:
        List<String> list = new ArrayList<String>(prefixByNamespace.keySet());
        // Sort the namespaces longest-first:
        java.util.Collections.sort(list, new Comparator<String>() {
            public int compare(String lhs, String rhs) {
                return rhs.length() - lhs.length();
            }
        });
        StringBuilder pat = new StringBuilder("^(");
        boolean didOne = false;
        for (String s : list) {
            if (didOne) {
                pat.append("|(");
            } else {
                pat.append("(");
                didOne = true;
            }
            pat.append(Pattern.quote(s));
            pat.append(")");
        }
        pat.append(")");
        if (didOne) {
            abbrPattern = Pattern.compile(pat.toString());
        } else {
            abbrPattern = null;
        }
    }
    
    /**
     * Return an unmodifiable map from prefices to their expansions.
     * Note that there is not (currently) an API to determine the "canonical"
     * prefix for a particular namespace name which is associated with
     * multiple prefices. (Having multiple such prefices is probably
     * bad practice, anyway.)
     */
    public Map<String, String> getDeclarations() {
        return java.util.Collections.unmodifiableMap(namespaceByPrefix);
    }
     
    /**
     * Abbreviate the given string, which must be a full URI.
     */
    public String idFromUri(String uri) {
        if (abbrPattern != null) {
            Matcher m = abbrPattern.matcher(uri);
            if (m.find()) {
                assert prefixByNamespace.containsKey(m.group(1));
                String prefix = prefixByNamespace.get(m.group(1));
                String localPart = uri.substring(m.end());
                if (prefix == null || prefix.length() == 0) {
                    return localPart;
                } else {
                    return prefix + ":" + localPart;
                }
            }
        }
        return "<" + uri + ">";
    }

    /**
     * Expand a full URI from `id`, which is of one of the following forms:
     * `name`, where `name` is an identifier containing no colon characters,
     * `prefix:name`, where `prefix` is a registered namespace prefix, or 
     * `&lt;uri&gt;`, where `uri` is a URI.
     */
    public String uriFromId(String id) {
        if (id.length() > 0 && id.charAt(0) == '<') {
            if (id.charAt(id.length() - 1) != '>') {
                throw new IllegalArgumentException("The string `" + id
                    + "` is not a valid identifier; URIs must be enclosed in"
                    + " '<' and '>'.");
            }
            return id.substring(1, id.length() - 1);
        } else {
            int pos = id.indexOf(':');
            if (pos != -1) {
                String prefix = id.substring(0, pos);
                String ns = namespaceByPrefix.get(prefix);
                if (ns == null) {
                    if (prefix == "http") {
                        throw new IllegalArgumentException(
                            "The URI `" + id + "` must be enclosed in "
                            + "'<' and '>' to be used as an identifier.");
                    }
                    throw new IllegalArgumentException("The string `" + prefix
                        + "` is not a registered namespace prefix.");
                }
                return ns + id.substring(pos + 1);
            } else { // use the default namespace
                String ns = namespaceByPrefix.get(null);
                if (ns == null) {
                    ns = namespaceByPrefix.get("");
                }
                if (ns == null) {
                    throw new IllegalArgumentException("The identifier `" + id
                        + "` contains only a local name, but no default "
                        + "namespace has been registered.");
                }
                return ns + id;
            }
        }
    }

    /**
     * Commonly-used semantic web namespaces (OWL, RDF, SWRL, etc.)
     */
    public static final Namespaces semanticWebNamespaces;
    static {
        Map<String, String> decl = new TreeMap<String, String>();
        decl.put("owl", "http://www.w3.org/2002/07/owl#");
        decl.put("owlx", "http://www.w3.org/2003/05/owl-xml#");
        decl.put("owl11xml", "http://www.w3.org/2006/12/owl11-xml#");
        decl.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        decl.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        decl.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        decl.put("swrl", "http://www.w3.org/2003/11/swrl#");
        decl.put("swrlb", "http://www.w3.org/2003/11/swrlb#");
        decl.put("swrlx", "http://www.w3.org/2003/11/swrlx#");
        decl.put("ruleml", "http://www.w3.org/2003/11/ruleml#");
        decl.put("kaon2", "http://kaon2.semanticweb.org/internal#");
        semanticWebNamespaces = new Namespaces(decl);
    }
    public static final Namespaces none;
    static {
        none = new Namespaces(new TreeMap<String, String>());
    }

}
