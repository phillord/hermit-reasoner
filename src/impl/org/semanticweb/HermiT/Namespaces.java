// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;

/**
 * This interface contains some well-known namespaces.
 */
 
// FIXME: this class requires various incantations to avoid surprising
// exceptions---don't use it in new code unless your name is Boris.
// (rob 2008-08-29)
public class Namespaces implements Serializable {
    private static final long serialVersionUID=-158185482289831766L;

    /** The namespace for OWL ontologies. */
    public static final String OWL_NS="http://www.w3.org/2002/07/owl#";
    /** The namespace for OWL XML syntax. */
    public static final String OWLX_NS="http://www.w3.org/2003/05/owl-xml#";
    /** The namespace for OWL 1.1 XML syntax. */
    public static final String OWL_1_1_XML_NS="http://www.w3.org/2006/12/owl11-xml#";
    /** The namespace for XSD datatypes. */
    public static final String XSD_NS="http://www.w3.org/2001/XMLSchema#";
    /** The namespace for RDF elements. */
    public static final String RDF_NS="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    /** The namespace for RDFS elements. */
    public static final String RDFS_NS="http://www.w3.org/2000/01/rdf-schema#";
    /** The namespace for SWRL elements. */
    public static final String SWRL_NS="http://www.w3.org/2003/11/swrl#";
    /** The namespace for SWRL built-ins. */
    public static final String SWRLB_NS="http://www.w3.org/2003/11/swrlb#";
    /** The namespace for SWRL XML syntax elements. */
    public static final String SWRLX_NS="http://www.w3.org/2003/11/swrlx#";
    /** The namespaces for RULE-ML syntax elements. */
    public static final String RULEML_NS="http://www.w3.org/2003/11/ruleml#";
    /** The namespaces for KAON2 elements. */
    public static final String KAON2_NS="http://kaon2.semanticweb.org/internal#";
    /** The map of well-known namespaces and prefixes. */
    protected static final Map<String,String> s_wellKnownNamespaces=new TreeMap<String,String>();
    static {
        s_wellKnownNamespaces.put("owl",OWL_NS);
        s_wellKnownNamespaces.put("owlx",OWLX_NS);
        s_wellKnownNamespaces.put("owl11xml",OWL_1_1_XML_NS);
        s_wellKnownNamespaces.put("xsd",XSD_NS);
        s_wellKnownNamespaces.put("rdf",RDF_NS);
        s_wellKnownNamespaces.put("rdfs",RDFS_NS);
        s_wellKnownNamespaces.put("swrl",SWRL_NS);
        s_wellKnownNamespaces.put("swrlb",SWRLB_NS);
        s_wellKnownNamespaces.put("swrlx",SWRLX_NS);
        s_wellKnownNamespaces.put("ruleml",RULEML_NS);
        s_wellKnownNamespaces.put("kaon2",KAON2_NS);
    }
    /** The set of reserved namespaces and prefixes. */
    protected static final Set<String> s_reservedPrefixes=new HashSet<String>();
    static {
        s_reservedPrefixes.add("lt");
        s_reservedPrefixes.add("gt");
        s_reservedPrefixes.add("amp");
        s_reservedPrefixes.add("apos");
        s_reservedPrefixes.add("quot");
    }

    /** The global static instance. */
    public static final Namespaces INSTANCE;
    static {
        Namespaces namespaces=new Namespaces();
        namespaces.registerStandardPrefixes();
        INSTANCE=getImmutable(namespaces);
    }
    public static final Namespaces EMPTY_INSTANCE=getImmutable(new Namespaces());

    /** The default namespace, of <code>null</code> if none is set. */
    protected String m_defaultNamespace;
    /** The map of prefixes to the corresponding URI. */
    protected final Map<String,String> m_namespaceByPrefix;
    /** The map of URIs to prefixes. */
    protected final Map<String,String> m_prefixByNamespace;
    /** The index of the next automatic prefix. */
    protected int m_nextAutomaticPrefix;

    /**
     * Creates an instance of this class not containing any mappings.
     */
    public Namespaces() {
        m_namespaceByPrefix=new TreeMap<String,String>();
        m_prefixByNamespace=new TreeMap<String,String>();
        m_nextAutomaticPrefix=0;
    }
    /**
     * Creates an instance of this class not containing any mappings.
     *
     * @param source                                the namespace object whose mappings are copied
     */
    public Namespaces(Namespaces source) {
        m_namespaceByPrefix=new TreeMap<String,String>(source.m_namespaceByPrefix);
        m_prefixByNamespace=new TreeMap<String,String>(source.m_prefixByNamespace);
        m_nextAutomaticPrefix=0;
    }
    /**
     * Registers internal prefixes "q", "nnq", "nom", and "amq" for the given ontology URI.
     * 
     * @param ontologyURI                           the URI of the ontology
     */
    public synchronized void registerInternalPrefixes(String ontologyURI) {
        registerPrefix("q","internal:q#");
        registerPrefix("nnq","internal:nnq#");
        registerPrefix("nom","internal:nom$"+ontologyURI+"#");
        registerPrefix("amq","internal:amq#");
        registerPrefix("all","internal:all#");
    }
    /**
     * Registers started prefixes to this object.
     */
    public synchronized void registerStandardPrefixes() {
        for (Map.Entry<String,String> entry : s_wellKnownNamespaces.entrySet())
            registerPrefix(entry.getKey(),entry.getValue());
     }
    /**
     * Sets the default namespace. It is used only in abbreviateAsNamespace method.
     *
     * @param defaultNamespace                     the default namespace
     */
    public synchronized void setDefaultNamespace(String defaultNamespace) {
        m_defaultNamespace=defaultNamespace;
    }
    /**
     * Returns the default namespace.
     *
     * @return                                  the default namespace
     */
    public synchronized String getDefaultNamespace() {
        return m_defaultNamespace;
    }
    /**
     * Deregisters a prefix.
     *
     * @param prefix                            the prefix of the URI
     */
    public synchronized void unregisterPrefix(String prefix) {
        String namespace=m_namespaceByPrefix.remove(prefix);
        m_prefixByNamespace.remove(namespace);
    }
    /**
     * Registers a prefix for the URI.
     *
     * @param prefix                            the prefix of the URI
     * @param namespace                         the namespace URI
     */
    public synchronized void registerPrefix(String prefix,String namespace) {
        if (s_reservedPrefixes.contains(prefix))
            throw new IllegalArgumentException("Prefix '"+prefix+"' is reserved in XML.");
        m_namespaceByPrefix.put(prefix,namespace);
        m_prefixByNamespace.put(namespace,prefix);
    }
    /**
     * Returns the namespace URI for the given prefix.
     *
     * @param prefix                            the prefix
     * @return                                  the namespace URI for the prefix (or <code>null</code> if the namespace for the prefix is not registered)
     */
    public synchronized String getNamespaceForPrefix(String prefix) {
        return m_namespaceByPrefix.get(prefix);
    }
    /**
     * Returns the prefix for the given namespace URI.
     *
     * @param namespace                         the namespace URI
     * @return                                  the prefix for the namespace URI (or <code>null</code> if the prefix for the namespace is not registered)
     */
    public synchronized String getPrefixForNamespace(String namespace) {
        return m_prefixByNamespace.get(namespace);
    }
    /**
     * Returns the prefix used to abbreviate the URI.
     *
     * @param uri                               the URI
     * @return                                  the prefix, or <code>null</code> if the URI cannot be abbreviated
     */
    public synchronized String getAbbreviationPrefix(String uri) {
        String namespace=guessNamespace(uri);
        if (namespace==null)
            return null;
        else
            return getPrefixForNamespace(namespace);
    }
    /**
     * Abbreviates given URI into the form prefix:local_name if possible.
     *
     * @param uri                               the URI
     * @return                                  the abbreviated form, or the original URI if abbreviation is not possible
     */
    public synchronized String abbreviateAsNamespace(String uri) {
        int namespaceEnd=guessNamespaceEnd(uri);
        if (namespaceEnd<0)
            return abbreviateAsNamespace(null,uri);
        else
            return abbreviateAsNamespace(uri.substring(0,namespaceEnd+1),uri.substring(namespaceEnd+1));
    }
    /**
     * Abbreviates given namespace URI and local name into the form prefix:local_name if possible.
     *
     * @param namespace                         the namespace (can be <code>null</code>)
     * @param localName                         the local name 
     * @return                                  the abbreviated form, or namespace+localName if abbreviation is not possible
     */
    public synchronized String abbreviateAsNamespace(String namespace,String localName) {
        if (namespace==null)
            return localName;
        if (namespace.equals(m_defaultNamespace))
            return localName;
        String prefix=getPrefixForNamespace(namespace);
        if (prefix==null)
            return namespace+localName;
        else
            return prefix+":"+localName;
    }
    /**
     * Abbreviates given URI into the form prefix:local_name if possible.
     *
     * @param uri                               the URI
     * @return                                  the abbreviated form, or the original URI if abbreviation is not possible
     */
    public synchronized String abbreviateAsNamespaceNoDefault(String uri) {
        int namespaceEnd=guessNamespaceEnd(uri);
        if (namespaceEnd<0)
            return abbreviateAsNamespaceNoDefault(null,uri);
        else
            return abbreviateAsNamespaceNoDefault(uri.substring(0,namespaceEnd+1),uri.substring(namespaceEnd+1));
    }
    /**
     * Abbreviates given URI into the form prefix:local_name if possible.
     *
     * @param namespace                         the namespace (can be <code>null</code>)
     * @param localName                         the local name 
     * @return                                  the abbreviated form, or namespace+localName if abbreviation is not possible
     */
    public synchronized String abbreviateAsNamespaceNoDefault(String namespace,String localName) {
        if (namespace==null)
            return localName;
        String prefix=getPrefixForNamespace(namespace);
        if (prefix==null)
            return namespace+localName;
        else
            return prefix+":"+localName;
    }
    /**
     * Abbreviates given URI into the form &prefix;local_name if possible.
     *
     * @param uri                               the URI
     * @return                                  the abbreviated form, or the original URI if abbreviation is not possible
     */
    public synchronized String abbreviateAsEntity(String uri) {
        int namespaceEnd=guessNamespaceEnd(uri);
        if (namespaceEnd<0)
            return abbreviateAsEntity(null,uri);
        else
            return abbreviateAsEntity(uri.substring(0,namespaceEnd+1),uri.substring(namespaceEnd+1));
    }
    /**
     * Abbreviates given URI into the form &prefix;local_name if possible.
     *
     * @param namespace                         the namespace (can be <code>null</code>)
     * @param localName                         the local name 
     * @return                                  the abbreviated form, or namespace+localName if abbreviation is not possible
     */
    public synchronized String abbreviateAsEntity(String namespace,String localName) {
        if (namespace==null)
            return localName;
        String prefix=getPrefixForNamespace(namespace);
        if (prefix==null)
            return namespace+localName;
        else
            return "&"+prefix+";"+localName;
    }
    /**
     * Attempts to expand given string (either of the form prefix:local_name or of the form &prefix;local_name) into an URI.
     *
     * @param string                            the string
     * @return                                  the expanded URI
     */
    public synchronized String expandString(String string) {
        if (string.length()>0 && string.charAt(0)=='&') {
            int lastSemicolonPosition=string.lastIndexOf(';');
            if (lastSemicolonPosition>=0) {
                String prefix=string.substring(1,lastSemicolonPosition);
                String namespace=getNamespaceForPrefix(prefix);
                if (namespace!=null)
                    return namespace+string.substring(lastSemicolonPosition+1);
            }
        }
        int lastColonPosition=string.lastIndexOf(':');
        if (lastColonPosition>=0) {
            String prefix=string.substring(0,lastColonPosition);
            String namespace=getNamespaceForPrefix(prefix);
            if (namespace!=null)
                return namespace+string.substring(lastColonPosition+1);
        } else {
            return m_defaultNamespace + string;
        }
        return string;
    }
    /**
     * Returns the iterator of all prefixes.
     *
     * @return                                  all prefixes
     */
    public synchronized Iterator<String> prefixes() {
        return Collections.unmodifiableSet(m_namespaceByPrefix.keySet()).iterator();
    }
    /**
     * Makes sure that a prefix for given uri exists. If a prefix for this URI does not exist,
     * a new prefix is generated.
     *
     * @param uri                               the URI
     * @return                                  the prefix (<code>null</code> if the URI does not have a namespace)
     */
    public synchronized String ensureNamespacePrefixExists(String uri) {
        String namespace=guessNamespace(uri);
        String prefix=null;
        if (namespace!=null && namespace.length()!=0) {
            prefix=getPrefixForNamespace(namespace);
            if (prefix==null) {
                for (Map.Entry<String,String> entry : s_wellKnownNamespaces.entrySet())
                    if (entry.getValue().equals(namespace) && getNamespaceForPrefix(entry.getKey())==null) {
                        prefix=entry.getKey();
                        break;
                    }
                if (prefix==null)
                    do {
                        prefix=getNextNamespacePrefix();
                    } while (getNamespaceForPrefix(prefix)!=null || s_wellKnownNamespaces.containsKey(prefix) || s_reservedPrefixes.contains(prefix));
                registerPrefix(prefix,namespace);
            }
        }
        return prefix;
    }
    /**
     * Returns the next new namespace prefix.
     *
     * @return                                  the next new namespace prefix
     */
    protected String getNextNamespacePrefix() {
        StringBuffer buffer=new StringBuffer();
        int index=m_nextAutomaticPrefix++;
        do {
            buffer.append((char)('a'+(index % 26)));
            index=index/26;
        } while (index!=0);
        return buffer.toString();
    }
    /**
     * Returns the index of the last characted of the namespace.
     *
     * @param uri                               the URI of the namespace
     * @return                                  the index of the last characted of the namespace
     */
    public static int guessNamespaceEnd(String uri) {
        for (int i=uri.length()-1;i>=0;i--) {
            char c=uri.charAt(i);
            if (c=='#' || c==':')
                return i;
            if (c=='/') {
                if (i>0 && uri.charAt(i-1)=='/')
                    return -1;
                return i;
            }
        }
        return -1;
    }
    /**
     * Guesses a namespace prefix of a URI.
     *
     * @param uri                               the URI for which the namespace prefix is guessed
     * @return                                  the namespace prefix or <code>null</code> if the prefix cannot be guessed
     */
    public static String guessNamespace(String uri) {
        int index=guessNamespaceEnd(uri);
        return index>=0 ? uri.substring(0,index+1) : null;
    }
    /**
     * Guesses the local name of a URI.
     *
     * @param uri                               the URI for which the local name is guessed
     * @return                                  the local name or the whole URI if the local name cannot be guessed
     */
    public static String guessLocalName(String uri) {
        return uri.substring(guessNamespaceEnd(uri)+1);
    }
    /**
     * Returns the immutable namespaces for the given namespace object.
     * 
     * @param namespaces                        the namespaces object
     * @return                                  the immutable namespaces object
     */
    public static Namespaces getImmutable(Namespaces namespaces) {
        return new ImmutableNamespaces(namespaces);
    }
    
    protected static class ImmutableNamespaces extends Namespaces {
        private static final long serialVersionUID=-6871335627786888403L;

        public ImmutableNamespaces(Namespaces namespaces) {
            super(namespaces);
        }
        public void registerPrefix(String prefix,String namespace) {
            throw new UnsupportedOperationException("The global Namespaces instance cannot be changed.");
        }
        public void unregisterPrefix(String prefix) {
            throw new UnsupportedOperationException("The global Namespaces instance cannot be changed.");
        }
    }
}
