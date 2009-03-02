// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for mapping between URIs and "identifiers", which can be given in any of three different formats: 1) &lt;uri&gt; 2) prefix:localpart 3) localpart Forms 2 and 3 are dependent upon a set of namespace declarations which associates namespaces with prefices. An identifier encoded in form 2 which uses an unregistered prefix is invalid---expanding it will result in an exception. Form 3 can only be used if a "default namespace" (a namespace associated with an empty prefix) has been declared. Neither prefices nor localparts may contain colon characters.
 */
public class Namespaces implements Serializable {
    private static final long serialVersionUID=-158185482289831766L;

    /** The map of prefixes to the corresponding URI. */
    protected final Map<String,String> m_namespaceByPrefix;

    /** The map of URIs to prefixes. */
    protected final Map<String,String> m_prefixByNamespace;

    // // Little optimization to make abbreviating fast(er):
    protected Pattern m_prefixMathingPattern;

    /**
     * Create a new Namespaces object from a set of prefix declarations and a sequence of other Namespaces objects to copy.
     * 
     * If multiple prefixes are associated with the same namespace name in `declarations`, then one of the prefices will be chosen arbitrarily as the canonical abbreviation. To bind multiple prefices to the same namespace name but use the specific prefix `p` as the canonical abbreviation, first create a Namespaces object `n` defining all the prefices and then derive a new Namespaces object from `n` and declarations for the canonical prefices.
     * 
     * @param prefixDeclarations
     *            map from prefixes to namespace names
     * @param sources
     *            other declarations to include, in order of precedence
     */
    public Namespaces(Map<String,String> prefixDeclarations,Namespaces... sources) {
        m_namespaceByPrefix=new TreeMap<String,String>();
        m_prefixByNamespace=new TreeMap<String,String>();

        // Copy backwards to preserve precedence:
        for (int i=sources.length-1;i>=0;--i) {
            m_namespaceByPrefix.putAll(sources[i].m_namespaceByPrefix);
            m_prefixByNamespace.putAll(sources[i].m_prefixByNamespace);
        }
        // highest precedence are the new declarations:
        for (Map.Entry<String,String> e : prefixDeclarations.entrySet()) {
            m_namespaceByPrefix.put(e.getKey(),e.getValue());
            m_prefixByNamespace.put(e.getValue(),e.getKey());
        }

        // Build the regex to identify namespaces in URLs:
        List<String> list=new ArrayList<String>(m_prefixByNamespace.keySet());
        // Sort the namespaces longest-first:
        Collections.sort(list,new Comparator<String>() {
            public int compare(String lhs,String rhs) {
                return rhs.length()-lhs.length();
            }
        });
        StringBuilder pattern=new StringBuilder("^(");
        boolean didOne=false;
        for (String prefix : list) {
            if (didOne)
                pattern.append("|(");
            else {
                pattern.append("(");
                didOne=true;
            }
            pattern.append(Pattern.quote(prefix));
            pattern.append(")");
        }
        pattern.append(")");
        if (didOne)
            m_prefixMathingPattern=Pattern.compile(pattern.toString());
        else
            m_prefixMathingPattern=null;
    }

    /**
     * Create a new Namespaces object with declarations copied from `sources` (given in order of precedence) and using `defaultNamespace` as (surprisingly enough) a default namespace.
     */
    public Namespaces(String defaultNamespace,Namespaces... sources) {
        this(createDefaultPrefixDeclarations(defaultNamespace),sources);
    }
    protected static Map<String,String> createDefaultPrefixDeclarations(String defaultNamespace) {
        Map<String,String> result=new TreeMap<String,String>();
        result.put("",defaultNamespace);
        return result;
    }
    
    /**
     * Return an unmodifiable map from prefixes to their expansions. Note that there is not (currently) an API to determine the "canonical" prefix for a particular namespace name which is associated with multiple prefices. (Having multiple such prefices is probably bad practice, anyway.)
     */
    public Map<String,String> getDeclarations() {
        return java.util.Collections.unmodifiableMap(m_namespaceByPrefix);
    }

    /**
     * Abbreviate the given string, which must be a full URI.
     */
    public String idFromUri(String uri) {
        if (m_prefixMathingPattern!=null) {
            Matcher matcher=m_prefixMathingPattern.matcher(uri);
            if (matcher.find()) {
                assert m_prefixByNamespace.containsKey(matcher.group(1));
                String prefix=m_prefixByNamespace.get(matcher.group(1));
                String localPart=uri.substring(matcher.end());
                if (prefix==null || prefix.length()==0)
                    return localPart;
                else
                    return prefix+":"+localPart;
            }
        }
        return "<"+uri+">";
    }

    /**
     * Expand a full URI from `id`, which is of one of the following forms: `name`, where `name` is an identifier containing no colon characters, `prefix:name`, where `prefix` is a registered namespace prefix, or `&lt;uri&gt;`, where `uri` is a URI.
     */
    public String uriFromId(String id) {
        if (id.length()>0 && id.charAt(0)=='<') {
            if (id.charAt(id.length()-1)!='>') {
                throw new IllegalArgumentException("The string `"+id+"` is not a valid identifier; URIs must be enclosed in"+" '<' and '>'.");
            }
            return id.substring(1,id.length()-1);
        }
        else {
            int pos=id.indexOf(':');
            if (pos!=-1) {
                String prefix=id.substring(0,pos);
                String ns=m_namespaceByPrefix.get(prefix);
                if (ns==null) {
                    if (prefix=="http") {
                        throw new IllegalArgumentException("The URI `"+id+"` must be enclosed in "+"'<' and '>' to be used as an identifier.");
                    }
                    throw new IllegalArgumentException("The string `"+prefix+"` is not a registered namespace prefix.");
                }
                return ns+id.substring(pos+1);
            }
            else { // use the default namespace
                String ns=m_namespaceByPrefix.get("");
                if (ns==null) {
                    ns=m_namespaceByPrefix.get("");
                }
                if (ns==null) {
                    throw new IllegalArgumentException("The identifier `"+id+"` contains only a local name, but no default "+"namespace has been registered.");
                }
                return ns+id;
            }
        }
    }

    /**
     * Return a set of namespaces with both the contents of `n` and prefixes for all internal concepts, including shorthands for internal nominal concepts based on the declarations from 'n'.
     */
    public static Namespaces withInternalNamespaces(Namespaces n) {
        Map<String,String> decl=new HashMap<String,String>();
        decl.put("q","internal:q#");
        decl.put("nnq","internal:nnq#");
        decl.put("amq","internal:amq#");
        decl.put("all","internal:all#");
        for (Map.Entry<String,String> e : n.getDeclarations().entrySet()) {
            String prefix="nom";
            if (e.getKey()!=null && e.getKey().length()>0) {
                prefix=prefix+"-"+e.getKey();
            }
            decl.put(prefix,"internal:nom$"+e.getValue());
        }
        return new Namespaces(decl,n);
    }

    public static boolean isInternalURI(String uri) {
        return uri.startsWith("internal:");
    }

    /**
     * Commonly-used semantic web namespaces (OWL, RDF, SWRL, etc.)
     */
    public static final Namespaces semanticWebNamespaces;
    static {
        Map<String,String> decl=new TreeMap<String,String>();
        decl.put("owl","http://www.w3.org/2002/07/owl#");
        decl.put("owlx","http://www.w3.org/2003/05/owl-xml#");
        decl.put("owl11xml","http://www.w3.org/2006/12/owl11-xml#");
        decl.put("xsd","http://www.w3.org/2001/XMLSchema#");
        decl.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        decl.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
        decl.put("swrl","http://www.w3.org/2003/11/swrl#");
        decl.put("swrlb","http://www.w3.org/2003/11/swrlb#");
        decl.put("swrlx","http://www.w3.org/2003/11/swrlx#");
        decl.put("ruleml","http://www.w3.org/2003/11/ruleml#");
        decl.put("kaon2","http://kaon2.semanticweb.org/internal#");
        semanticWebNamespaces=new Namespaces(decl);
    }

    public static final Namespaces none=new Namespaces(new TreeMap<String,String>());

}
