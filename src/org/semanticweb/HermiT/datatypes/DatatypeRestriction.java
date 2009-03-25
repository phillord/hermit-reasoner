/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.datatypes;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;


/**
 * An abstract superclass of all concrete datatype restrictions. The class 
 * implements the DLPredicate interface and provides several methods for the 
 * DataRange and CanonicalDataRange interfaces that can be overwritten or 
 * extended by the concrete subclasses. 
 * @author BGlimm
 */
public abstract class DatatypeRestriction 
        extends DataRange implements CanonicalDataRange, Serializable {
    
    private static final long serialVersionUID = 524235536504588458L;
    
    protected Set<Facet> supportedFacets = new HashSet<Facet>();
    
    /**
     * A list of facets that are supported by at least one concrete 
     * implementation.
     * @author BGlimm
     */
    public enum Facet {
        LENGTH, MIN_LENGTH, MAX_LENGTH, 
        PATTERN, 
        MIN_INCLUSIVE, MIN_EXCLUSIVE, MAX_INCLUSIVE, MAX_EXCLUSIVE 
    };
    
    protected static final Map<String, String> uris = Namespaces.s_semanticWebNamespaces;
    
    /**
     * The datatypes supported by HermiT together with their URIs and a position 
     * value. The position can be seen as a tree specified as a prefixed closed 
     * subset of strings. This tree is used to find the most specific 
     * restriction in the datatype manager that will then be used for the 
     * canonical datatype and that contains the facets and restrictions of all 
     * data range assertions for a node/variable. E.g., if we have datatype 
     * restrictions of OWLReal, Integer, and Short for a node, then we will use 
     * the implementation of short as a basis for the canonical restriction as 
     * this is the most specific one.
     *  
     * @author BGlimm
     */
    public enum DT {
        OWLREALPLUS ("1", (uris.get("owl") + "realPlus")),
        OWLREAL ("11", (uris.get("owl") + "real")),
        RATIONAL ("111", (uris.get("owl") + "rational")),
        DECIMAL ("1111", (uris.get("xsd") + "decimal")),
        DOUBLE ("11111", (uris.get("xsd") + "double")),
        FLOAT ("111111", (uris.get("xsd") + "float")),
        INTEGER ("1111111", (uris.get("xsd") + "integer")),
        NONNEGATIVEINTEGER ("11111111", (uris.get("xsd") + "nonNegativeInteger")),
        NONPOSITIVEINTEGER ("11111112", (uris.get("xsd") + "nonPositiveInteger")),
        POSITIVEINTEGER ("111111111", (uris.get("xsd") + "positiveInteger")),
        NEGATIVEINTEGER ("111111121", (uris.get("xsd") + "negativeInteger")),
        LONG ("11111113", (uris.get("xsd") + "long")),
        INT ("111111131", (uris.get("xsd") + "int")),
        SHORT ("1111111311", (uris.get("xsd") + "short")),
        BYTE ("11111113111", (uris.get("xsd") + "byte")),
        UNSIGNEDLONG ("11111114", (uris.get("xsd") + "unsignedLong")),
        UNSIGNEDINT ("111111141", (uris.get("xsd") + "unsignedInt")),
        UNSIGNEDSHORT ("1111111411", (uris.get("xsd") + "unsignedShort")),
        UNSIGNEDBYTE ("11111114111", (uris.get("xsd") + "unsignedByte")),
        OWLDATETIME  ("2", (uris.get("owl") + "dateTime")), 
        DATETIME ("21", (uris.get("xsd") + "dateTime")),
        RDFTEXT ("3", (uris.get("rdf") + "text")),
        STRING ("31", (uris.get("xsd") + "string")),
        NORMALIZEDSTRING ("311", (uris.get("xsd") + "normalizedString")),
        TOKEN ("3111", (uris.get("xsd") + "token")),
        LANGUAGE ("31111", (uris.get("xsd") + "language")),
        NAME ("31112", (uris.get("xsd") + "Name")),
        NMTOKEN ("31113", (uris.get("xsd") + "NMTOKEN")),
        NCNAME ("311121", (uris.get("xsd") + "NCName")),
        ID ("3111211", (uris.get("xsd") + "ID")),
        IDREF ("3111212", (uris.get("xsd") + "IDREF")),
        ENTITY ("3111213", (uris.get("xsd") + "ENTITY")),
        BOOLEAN ("4", (uris.get("xsd") + "boolean")), 
        LITERAL ("", (uris.get("rdfs") + "Literal")),
        ANYURI ("5", (uris.get("xsd") + "anyURI")),
        HEXBINARY ("6", (uris.get("xsd") + "hexBinary")),
        BASE64BINARY ("7", (uris.get("xsd") + "base64Binary")),
        UNKNOWN ("8", (uris.get("xsd") + "unknown"));

        /**
         * position in a tree that indicates subsumption relationships between 
         * datatypes
         */
        private final String position;
        private final String uri;
        
        DT(String position, String uri) {
            this.position = position;
            this.uri = uri;
        }
        
        public String getPosition() { 
            return position; 
        }
        
        public String getURIAsString() { 
            return uri; 
        }
        
        public URI getURI() {
            return URI.create(uri);
        }
        
        /**
         * Return all datatypes that are more specific then the given one 
         * according to the tree induced by the position values of the 
         * datatypes.
         * @param datatype a datatype
         * @return a set of more specific datatypes
         */
        public static Set<DT> getSubTreeFor(DT datatype) { 
            Set<DT> subs = new HashSet<DT>();
            String pos = datatype.getPosition();
            if (pos == null) {
                subs.addAll(Arrays.asList(DT.values()));
                return subs;
            }
            for (DT dt : DT.values()) {
                if (dt.getPosition().startsWith(datatype.getPosition())) {
                    subs.add(dt);
                }
            }
            return subs; 
        }
        
        /**
         * Determines whether the second datatype is strictly more specific than 
         * the first one in the hierarchy induced by the position. 
         * @param datatype1 a datatype 
         * @param datatype2 a datatype 
         * @return true if datatype2 is strictly more specific than datattype1 
         * and false otherwise
         */
        public static boolean isSubOf(DT datatype1, DT datatype2) { 
            String pos1 = datatype1.getPosition();
            String pos2 = datatype2.getPosition();
            if (pos1 == null || pos2 == null) return false; 
            return pos1.startsWith(pos2) && pos1 != pos2; 
        }
    }

    protected DT datatype;
    protected Set<DataConstant> oneOf = new HashSet<DataConstant>();
    protected Set<DataConstant> notOneOf = new HashSet<DataConstant>();
    protected boolean isNegated = false;
    protected boolean isBottom = false;
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.DLPredicate#getArity()
     */
    public int getArity() {
        return 1;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getDatatype()
     */
    public DT getDatatype() {
        return datatype;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getDatatypeURI()
     */
    public URI getDatatypeURI() {
        return datatype != null ? datatype.getURI() : null;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#isNegated()
     */
    public boolean isNegated() {
        return isNegated;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#negate()
     */
    public void negate() {
        isNegated = !isNegated;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#isBottom()
     */
    public boolean isBottom() {
        if (!isBottom) {
            if (!hasMinCardinality(BigInteger.ONE)) {
                isBottom = true;
            }
        }
        return isBottom;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getOneOf()
     */
    public Set<DataConstant> getOneOf() {
        return oneOf;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getNotOneOf()
     */
    public Set<DataConstant> getNotOneOf() {
        return notOneOf;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#setOneOf(java.util.Set)
     */
    public void setOneOf(Set<DataConstant> oneOf) {
        this.oneOf = oneOf;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#addOneOf(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean addOneOf(DataConstant constant) {
        return oneOf.add(constant);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#setNotOneOf(java.util.Set)
     */
    public void setNotOneOf(Set<DataConstant> notOneOf) {
        this.notOneOf = notOneOf;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#notOneOf(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean notOneOf(DataConstant constant) {
        boolean result = true;
        if (!oneOf.isEmpty()) {
            result = oneOf.remove(constant);
            if (oneOf.isEmpty()) isBottom = true;
        } else {
            result = notOneOf.add(constant); 
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#supports(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets)
     */
    public boolean supports(Facet facet) {
        return supportedFacets.contains(facet);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString(Namespaces.EMPTY);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.DLPredicate#toString(org.semanticweb.HermiT.Namespaces)
     */
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        if (datatype != null && datatype.getURI() != null) {
            if (isNegated) buffer.append("not ");
            buffer.append(namespaces.abbreviateURI(datatype.getURIAsString()));
        }
        buffer.append(printExtraInfo(namespaces));
        boolean firstRun = true;
        if (!oneOf.isEmpty()) {
            if (isNegated) buffer.append("not ");
            buffer.append("oneOf(");
            firstRun = true;
            SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>();
            sortedOneOfs.addAll(oneOf);
            for (DataConstant constant : sortedOneOfs) {
                if (!firstRun) {
                    buffer.append(isNegated ? " and " : " or ");
                    firstRun = false;
                }
                buffer.append(constant.toString(namespaces));
            }
            buffer.append(")");
        }
        if (!notOneOf.isEmpty()) {
            // only in non-negated canonical ranges
            firstRun = true;
            buffer.append(" (");
            SortedSet<DataConstant> sortedNotOneOfs = new TreeSet<DataConstant>();
            sortedNotOneOfs.addAll(notOneOf);
            for (DataConstant constant : sortedNotOneOfs) {
                if (!firstRun) {
                    buffer.append(" and");
                    firstRun = false;
                }
                buffer.append(" not " + constant.toString(namespaces));
            }
            buffer.append(")");
        }
        buffer.append(")");
        return buffer.toString();        
    }
    
    /**
     * Can be overwritten by the sub-classes, to print something between the 
     * datatype restriction and the list of oneOfs/notOneOfs 
     * @return a string with extra information for the toString method, e.g., 
     * about facet values
     */
    protected String printExtraInfo(Namespaces namespaces) {
        return "";
    }

}
