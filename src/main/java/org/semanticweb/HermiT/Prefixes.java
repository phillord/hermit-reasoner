/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for abbreviating IRIs. The resulting IRIs can be either<br>
 * 1) &lt;uri&gt; or<br>
 * 2) prefix-name:local-name where prefix-name can be empty.<br>
 * Forms 1 and 2 are dependent upon a set of prefix declarations that associates prefix names with prefix IRIs.
 * A IRI abbreviated using form 2 that uses an unregistered prefix is invalid---expanding it will result in an exception.
 * Neither prefixes nor local names may contain colon characters. The grammar used for various parts of the IRIs is as follows:<br>
 * PN_CHARS_BASE ::= [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] |
 *                   [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]<br>
 * PN_CHARS      ::= PN_CHARS_BASE | '_' | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]<br>
 * PN_LOCAL      ::= ( PN_CHARS_BASE | '_' | [0-9] ) ( ( PN_CHARS | '.' )* PN_CHARS )?<br>
 * PN_PREFIX     ::= PN_CHARS_BASE ( ( PN_CHARS | '.' )* PN_CHARS )?<br>
 */
public class Prefixes implements Serializable {
    private static final long serialVersionUID=-158185482289831766L;

    protected static final String PN_CHARS_BASE="[A-Za-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD]";
    protected static final String PN_CHARS="[A-Za-z0-9_\\u002D\\u00B7\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0300-\\u036F\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u203F-\\u2040\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD]";
    protected static final Pattern s_localNameChecker=Pattern.compile("("+PN_CHARS_BASE+"|_|[0-9])(("+PN_CHARS+"|[.])*("+PN_CHARS+"))?");
    public static final Map<String,String> s_semanticWebPrefixes;
    static {
        s_semanticWebPrefixes=new HashMap<String,String>();
        s_semanticWebPrefixes.put("rdf:","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        s_semanticWebPrefixes.put("rdfs:","http://www.w3.org/2000/01/rdf-schema#");
        s_semanticWebPrefixes.put("owl:","http://www.w3.org/2002/07/owl#");
        s_semanticWebPrefixes.put("xsd:","http://www.w3.org/2001/XMLSchema#");
        s_semanticWebPrefixes.put("swrl:","http://www.w3.org/2003/11/swrl#");
        s_semanticWebPrefixes.put("swrlb:","http://www.w3.org/2003/11/swrlb#");
        s_semanticWebPrefixes.put("swrlx:","http://www.w3.org/2003/11/swrlx#");
        s_semanticWebPrefixes.put("ruleml:","http://www.w3.org/2003/11/ruleml#");
    }
    public static final Prefixes STANDARD_PREFIXES=new ImmutablePrefixes(s_semanticWebPrefixes);

    protected final Map<String,String> m_prefixIRIsByPrefixName;
    protected final Map<String,String> m_prefixNamesByPrefixIRI;
    protected Pattern m_prefixIRIMatchingPattern;

    public Prefixes() {
        m_prefixIRIsByPrefixName=new TreeMap<String,String>();
        m_prefixNamesByPrefixIRI=new TreeMap<String,String>();
        buildPrefixIRIMatchingPattern();
    }
    protected void buildPrefixIRIMatchingPattern() {
        List<String> list=new ArrayList<String>(m_prefixNamesByPrefixIRI.keySet());
        // Sort the prefix IRIs, longest first
        Collections.sort(list,new Comparator<String>() {
            public int compare(String lhs,String rhs) {
                return rhs.length()-lhs.length();
            }
        });
        StringBuilder pattern=new StringBuilder("^(");
        boolean didOne=false;
        for (String prefixIRI : list) {
            if (didOne)
                pattern.append("|(");
            else {
                pattern.append("(");
                didOne=true;
            }
            pattern.append(Pattern.quote(prefixIRI));
            pattern.append(")");
        }
        pattern.append(")");
        if (didOne)
            m_prefixIRIMatchingPattern=Pattern.compile(pattern.toString());
        else
            m_prefixIRIMatchingPattern=null;
    }
    public String abbreviateIRI(String iri) {
        if (m_prefixIRIMatchingPattern!=null) {
            Matcher matcher=m_prefixIRIMatchingPattern.matcher(iri);
            if (matcher.find()) {
                String localName=iri.substring(matcher.end());
                if (isValidLocalName(localName)) {
                    String prefix=m_prefixNamesByPrefixIRI.get(matcher.group(1));
                    return prefix+localName;
                }
            }
        }
        return "<"+iri+">";
    }
    /**
     * Expands a full IRI from the abbreviated one, which is of one of the following forms:
     * 'prefix:name', where 'prefix' is a registered prefix name (can be empty), or
     * '&lt;iri&gt;', where 'iri' is an IRI.
     */
    public String expandAbbreviatedIRI(String abbreviation) {
        if (abbreviation.length()>0 && abbreviation.charAt(0)=='<') {
            if (abbreviation.charAt(abbreviation.length()-1)!='>')
                throw new IllegalArgumentException("The string '"+abbreviation+"' is not a valid abbreviation: IRIs must be enclosed in '<' and '>'.");
            return abbreviation.substring(1,abbreviation.length()-1);
        }
        else {
            int pos=abbreviation.indexOf(':');
            if (pos!=-1) {
                String prefix=abbreviation.substring(0,pos+1);
                String prefixIRI=m_prefixIRIsByPrefixName.get(prefix);
                if (prefixIRI==null) {
                    // Catch the common error of not quoting IRIs starting with http:
                    if (prefix=="http:")
                        throw new IllegalArgumentException("The IRI '"+abbreviation+"' must be enclosed in '<' and '>' to be used as an abbreviation.");
                    throw new IllegalArgumentException("The string '"+prefix+"' is not a registered prefix name.");
                }
                return prefixIRI+abbreviation.substring(pos+1);
            }
            else
                throw new IllegalArgumentException("The abbreviation '"+abbreviation+"' is not valid (it does not start with a colon).");
        }
    }
    /**
     * Checks whether the given IRI can be expanded
     */
    public boolean canBeExpanded(String iri) {
        if (iri.length()>0 && iri.charAt(0)=='<')
            return false;
        else {
            int pos=iri.indexOf(':');
            if (pos!=-1) {
                String prefix=iri.substring(0,pos+1);
                return m_prefixIRIsByPrefixName.get(prefix)!=null;
            }
            else
                return false;
        }
    }
    public boolean declarePrefix(String prefixName,String prefixIRI) {
        boolean containsPrefix=declarePrefixRaw(prefixName,prefixIRI);
        buildPrefixIRIMatchingPattern();
        return containsPrefix;
    }
    protected boolean declarePrefixRaw(String prefixName,String prefixIRI) {
        if (!prefixName.endsWith(":"))
            throw new IllegalArgumentException("Prefix name '"+prefixName+"' should end with a colon character.");
        String existingPrefixName=m_prefixNamesByPrefixIRI.get(prefixIRI);
        if (existingPrefixName!=null && !prefixName.equals(existingPrefixName))
            throw new IllegalArgumentException("The prefix IRI '"+prefixIRI+"' has already been associated with the prefix name '"+existingPrefixName+"'.");
        m_prefixNamesByPrefixIRI.put(prefixIRI,prefixName);
        return m_prefixIRIsByPrefixName.put(prefixName,prefixIRI)==null;
    }
    public boolean declareDefaultPrefix(String defaultPrefixIRI) {
        return declarePrefix(":",defaultPrefixIRI);
    }
    public Map<String,String> getPrefixIRIsByPrefixName() {
        return java.util.Collections.unmodifiableMap(m_prefixIRIsByPrefixName);
    }
    public String getPrefixIRI(String prefixName) {
        return m_prefixIRIsByPrefixName.get(prefixName);
    }
    public String getPrefixName(String prefixIRI) {
        return m_prefixNamesByPrefixIRI.get(prefixIRI);
    }
    /**
     * Registers HermiT's internal prefixes with this object.
     *
     * @param individualIRIs    the collection of IRIs used in individuals (used for registering nominal prefix names)
     * @return                  'true' if this object already contained one of the internal prefix names
     */
    public boolean declareInternalPrefixes(Collection<String> individualIRIs, Collection<String> anonIndividualIRIs) {
        boolean containsPrefix=false;
        if (declarePrefixRaw("def:","internal:def#"))
            containsPrefix=true;
        if (declarePrefixRaw("defdata:","internal:defdata#"))
            containsPrefix=true;
        if (declarePrefixRaw("nnq:","internal:nnq#"))
            containsPrefix=true;
        if (declarePrefixRaw("all:","internal:all#"))
            containsPrefix=true;
        if (declarePrefixRaw("swrl:","internal:swrl#"))
            containsPrefix=true;
        if (declarePrefixRaw("prop:","internal:prop#"))
            containsPrefix=true;
        int individualIRIsIndex=1;
        for (String iri : individualIRIs) {
            if (declarePrefixRaw("nom"+(individualIRIsIndex==1 ? "" : String.valueOf(individualIRIsIndex))+":","internal:nom#"+iri))
                containsPrefix=true;
            individualIRIsIndex++;
        }
        int anonymousIndividualIRIsIndex=1;
        for (String iri : anonIndividualIRIs) {
            if (declarePrefixRaw("anon"+(anonymousIndividualIRIsIndex==1 ? "" : String.valueOf(anonymousIndividualIRIsIndex))+":","internal:anon#"+iri))
                containsPrefix=true;
            anonymousIndividualIRIsIndex++;
        }
        if (declarePrefixRaw("nam:","internal:nam#"))
            containsPrefix=true;
        buildPrefixIRIMatchingPattern();
        return containsPrefix;
    }
    /**
     * Registers the well-known Semantic Web prefixes.
     *
     * @return                  'true' if this object already contained one of the well-known prefixes
     */
    public boolean declareSemanticWebPrefixes() {
        boolean containsPrefix=false;
        for (Map.Entry<String,String> entry : s_semanticWebPrefixes.entrySet())
            if (declarePrefixRaw(entry.getKey(),entry.getValue()))
                containsPrefix=true;
        buildPrefixIRIMatchingPattern();
        return containsPrefix;
    }
    /**
     * Registers all the prefixes from the supplied object.
     *
     * @param prefixes          the object from which the prefixes are taken
     * @return                  'true' if this object already contained one of the prefixes from the supplied object
     */
    public boolean addPrefixes(Prefixes prefixes) {
        boolean containsPrefix=false;
        for (Map.Entry<String,String> entry : prefixes.m_prefixIRIsByPrefixName.entrySet())
            if (declarePrefixRaw(entry.getKey(),entry.getValue()))
                containsPrefix=true;
        buildPrefixIRIMatchingPattern();
        return containsPrefix;
    }
    /**
     * Converts this object to a string.
     */
    public String toString() {
        return m_prefixIRIsByPrefixName.toString();
    }
    /**
     * Determines whether the supplied IRI is used internally by HermiT.
     */
    public static boolean isInternalIRI(String iri) {
        return iri.startsWith("internal:");
    }
    /**
     * Determines whether the supplied string is a valid local name.
     */
    public static boolean isValidLocalName(String localName) {
        return s_localNameChecker.matcher(localName).matches();
    }

    public static class ImmutablePrefixes extends Prefixes {
        private static final long serialVersionUID=8517988865445255837L;

        public ImmutablePrefixes(Map<String,String> initialPrefixes) {
            for (Map.Entry<String,String> entry : initialPrefixes.entrySet())
                super.declarePrefixRaw(entry.getKey(),entry.getValue());
            buildPrefixIRIMatchingPattern();
        }
        protected boolean declarePrefixRaw(String prefixName,String prefixIRI) {
            throw new UnsupportedOperationException("The well-known standard Prefix instance cannot be modified.");
        }
    }
}
