// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
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

    public static final Map<String,String> s_semanticWebNamespaces;
    static {
        s_semanticWebNamespaces=new HashMap<String,String>();
        s_semanticWebNamespaces.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        s_semanticWebNamespaces.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
        s_semanticWebNamespaces.put("owl","http://www.w3.org/2002/07/owl#");
        s_semanticWebNamespaces.put("xsd","http://www.w3.org/2001/XMLSchema#");
        s_semanticWebNamespaces.put("swrl","http://www.w3.org/2003/11/swrl#");
        s_semanticWebNamespaces.put("swrlb","http://www.w3.org/2003/11/swrlb#");
        s_semanticWebNamespaces.put("swrlx","http://www.w3.org/2003/11/swrlx#");
        s_semanticWebNamespaces.put("ruleml","http://www.w3.org/2003/11/ruleml#");
    }
    @SuppressWarnings("serial")
    public static final Namespaces EMPTY=new Namespaces() {
        protected boolean registerNamespaceRaw(String prefix,String namespace) {
            throw new UnsupportedOperationException("The well-known empty namespace cannot be modified.");
        }
    };

    protected final Map<String,String> m_namespaceByPrefix;
    protected final Map<String,String> m_prefixByNamespace;
    protected Pattern m_namespaceMathingPattern;

    public Namespaces() {
        m_namespaceByPrefix=new TreeMap<String,String>();
        m_prefixByNamespace=new TreeMap<String,String>();
        buildNamespaceMatchingPattern();
    }
    protected void buildNamespaceMatchingPattern() {
        List<String> list=new ArrayList<String>(m_prefixByNamespace.keySet());
        // Sort the namespaces, longest first
        Collections.sort(list,new Comparator<String>() {
            public int compare(String lhs,String rhs) {
                return rhs.length()-lhs.length();
            }
        });
        StringBuilder pattern=new StringBuilder("^(");
        boolean didOne=false;
        for (String namespace : list) {
            if (didOne)
                pattern.append("|(");
            else {
                pattern.append("(");
                didOne=true;
            }
            pattern.append(Pattern.quote(namespace));
            pattern.append(")");
        }
        pattern.append(")");
        if (didOne)
            m_namespaceMathingPattern=Pattern.compile(pattern.toString());
        else
            m_namespaceMathingPattern=null;
    }
    public String abbreviateURI(String uri) {
        if (m_namespaceMathingPattern!=null) {
            Matcher matcher=m_namespaceMathingPattern.matcher(uri);
            if (matcher.find()) {
                String prefix=m_prefixByNamespace.get(matcher.group(1));
                String localName=uri.substring(matcher.end());
                if (prefix==null || prefix.length()==0)
                    return localName;
                else
                    return prefix+":"+localName;
            }
        }
        return "<"+uri+">";
    }
    /**
     * Abbreviates the given URI in a safe way. The local name must match the 'localNameChecker' pattern,
     * and the abbreviation must not be in the set of prohibited abbreviations.
     */
    public String abbreviateURISafe(String uri,Pattern localNameChecker,Set<String> prohibitedAbbreviations) {
        if (m_namespaceMathingPattern!=null) {
            Matcher matcher=m_namespaceMathingPattern.matcher(uri);
            if (matcher.find()) {
                String prefix=m_prefixByNamespace.get(matcher.group(1));
                String localName=uri.substring(matcher.end());
                String abbreviation;
                if (prefix==null || prefix.length()==0)
                    abbreviation=localName;
                else
                    abbreviation=prefix+":"+localName;
                if (localNameChecker.matcher(localName).matches() && !prohibitedAbbreviations.contains(abbreviation))
                    return abbreviation;
            }
        }
        return "<"+uri+">";
    }
    /**
     * Expands a full URI from the abbreviated one, which is of one of the following forms:
     * 'name', where 'name' is an identifier containing no colon characters,
     * 'prefix:name', where 'prefix' is a registered namespace prefix, or
     * '&lt;uri&gt;', where 'uri' is a URI.
     */
    public String expandAbbreviatedURI(String abbreviation) {
        if (abbreviation.length()>0 && abbreviation.charAt(0)=='<') {
            if (abbreviation.charAt(abbreviation.length()-1)!='>')
                throw new IllegalArgumentException("The string '"+abbreviation+"' is not a valid abbreviation: URIs must be enclosed in '<' and '>'.");
            return abbreviation.substring(1,abbreviation.length()-1);
        }
        else {
            int pos=abbreviation.indexOf(':');
            if (pos!=-1) {
                String prefix=abbreviation.substring(0,pos);
                String ns=m_namespaceByPrefix.get(prefix);
                if (ns==null) {
                    // Catch the common error of not quoting URIs starting with http:
                    if (prefix=="http")
                        throw new IllegalArgumentException("The URI '"+abbreviation+"' must be enclosed in '<' and '>' to be used as an abbreviation.");
                    throw new IllegalArgumentException("The string '"+prefix+"' is not a registered namespace prefix.");
                }
                return ns+abbreviation.substring(pos+1);
            }
            else {
                String ns=m_namespaceByPrefix.get("");
                if (ns==null)
                    throw new IllegalArgumentException("The abbreviation '"+abbreviation+"' contains only a local name, but no default namespace has been registered.");
                return ns+abbreviation;
            }
        }
    }
    public boolean registerNamespace(String prefix,String namespace) {
        boolean containsPrefix=registerNamespaceRaw(prefix,namespace);
        buildNamespaceMatchingPattern();
        return containsPrefix;
    }
    protected boolean registerNamespaceRaw(String prefix,String namespace) {
        String existingPrefix=m_prefixByNamespace.get(namespace);
        if (existingPrefix!=null && !prefix.equals(existingPrefix))
            throw new IllegalArgumentException("The suppllied namespace has already been registered with prefix '"+existingPrefix+"'.");
        m_prefixByNamespace.put(namespace,prefix);
        return m_namespaceByPrefix.put(prefix,namespace)==null;
    }
    public boolean registerDefaultNamespace(String namespace) {
        return registerNamespace("",namespace);
    }
    public Map<String,String> getNamespacesByPrefix() {
        return java.util.Collections.unmodifiableMap(m_namespaceByPrefix);
    }
    public String getNamespace(String prefix) {
        return m_namespaceByPrefix.get(prefix);
    }
    public String getPrefix(String namespace) {
        return m_prefixByNamespace.get(namespace);
    }
    /**
     * Registers HermiT's internal prefixes with this object.
     * 
     * @param individualURIs    the collection of URIs used in individuals (used for registering nominal prefixes)
     * @return                  'true' if this namespace object already contained one of the internal prefixes
     */
    public boolean registerInternalNamespaces(Collection<String> individualURIs) {
        boolean containsPrefix=false;
        if (registerNamespaceRaw("def","internal:def#"))
            containsPrefix=true;
        if (registerNamespaceRaw("nnq","internal:nnq#"))
            containsPrefix=true;
        if (registerNamespaceRaw("amq","internal:amq#"))
            containsPrefix=true;
        if (registerNamespaceRaw("all","internal:all#"))
            containsPrefix=true;
        int individualURIsIndex=1;
        for (String uri : individualURIs) {
            if (registerNamespaceRaw("nom"+(individualURIsIndex==1 ? "" : String.valueOf(individualURIsIndex)),"internal:nom#"+uri))
                containsPrefix=true;
            individualURIsIndex++;
        }
        if (registerNamespaceRaw("nam","internal:nam#"))
            containsPrefix=true;
        if (registerNamespaceRaw("grd","internal:grd#"))
            containsPrefix=true;
        buildNamespaceMatchingPattern();
        return containsPrefix;
    }
    /**
     * Registers the well-known Semantic Web namespaces.
     * 
     * @return                  'true' if this namespace object already contained one of the well-known prefixes
     */
    public boolean reegisterSemanticWebPrefixes() {
        boolean containsPrefix=false;
        for (Map.Entry<String,String> entry : s_semanticWebNamespaces.entrySet())
            if (registerNamespaceRaw(entry.getKey(),entry.getValue()))
                containsPrefix=true;
        buildNamespaceMatchingPattern();
        return containsPrefix;
    }
    /**
     * Registers all the prefixes from the supplied namespaces object.
     * 
     * @param namespaces        the object from which the prefixes are taken
     * @return                  'true' if this namespace object already contained one of the prefixes from the supplied object
     */
    public boolean addPrefixes(Namespaces namespaces) {
        boolean containsPrefix=false;
        for (Map.Entry<String,String> entry : namespaces.m_namespaceByPrefix.entrySet())
            if (registerNamespaceRaw(entry.getKey(),entry.getValue()))
                containsPrefix=true;
        buildNamespaceMatchingPattern();
        return containsPrefix;
    }
    /**
     * Determines whether the supplied URI is in the HermiT's internal namespace.
     */
    public static boolean isInternalURI(String uri) {
        return uri.startsWith("internal:");
    }
}
