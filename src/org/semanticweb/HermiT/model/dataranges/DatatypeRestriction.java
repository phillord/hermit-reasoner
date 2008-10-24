package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;


public abstract class DatatypeRestriction implements DataRange, CanonicalDataRange {
    
    protected Set<Facets> supportedFacets = new HashSet<Facets>();
    
    public enum Facets {
        LENGTH, MIN_LENGTH, MAX_LENGTH, 
        PATTERN, 
        MIN_INCLUSIVE, MIN_EXCLUSIVE, MAX_INCLUSIVE, MAX_EXCLUSIVE, 
        TOTAL_DIGITS, FRACTION_DIGITS
    };
    
    protected static final Map<String, String> uris = Namespaces.semanticWebNamespaces.getDeclarations();
    
    public enum DT {
        
        OWLREALPLUS ("1", (uris.get("owl") + "realPlus")),
        OWLREAL ("11", (uris.get("owl") + "real")),
        DECIMAL ("111", (uris.get("xsd") + "decimal")),
        DOUBLE ("1111", (uris.get("xsd") + "double")),
        FLOAT ("11111", (uris.get("xsd") + "float")),
        INTEGER ("111111", (uris.get("xsd") + "integer")),
        NONNEGATIVEINTEGER ("1111111", (uris.get("xsd") + "nonNegativeInteger")),
        NONPOSITIVEINTEGER ("1111112", (uris.get("xsd") + "nonPositiveInteger")),
        POSITIVEINTEGER ("11111111", (uris.get("xsd") + "positiveInteger")),
        NEGATIVEINTEGER ("11111121", (uris.get("xsd") + "negativeInteger")),
        LONG ("1111113", (uris.get("xsd") + "long")),
        INT ("11111131", (uris.get("xsd") + "int")),
        SHORT ("111111311", (uris.get("xsd") + "short")),
        BYTE ("1111113111", (uris.get("xsd") + "byte")),
        UNSIGNEDLONG ("1111114", (uris.get("xsd") + "unsignedLong")),
        UNSIGNEDINT ("11111141", (uris.get("xsd") + "unsignedInt")),
        UNSIGNEDSHORT ("111111411", (uris.get("xsd") + "unsignedShort")),
        UNSIGNEDBYTE ("1111114111", (uris.get("xsd") + "unsignedByte")),
        OWLDATETIME  ("2", (uris.get("owl") + "dateTime")), 
        DATETIME ("21", (uris.get("xsd") + "dateTime")),
        RDFTEXT ("3", (uris.get("rdf") + "text")),
        STRING ("31", (uris.get("xsd") + "string")),
        BOOLEAN ("4", (uris.get("xsd") + "boolean")), 
        LITERAL ("5", (uris.get("rdfs") + "Literal")),
        ANYURI ("6", (uris.get("xsd") + "anyURI"));        

        private final String position;   // in a tree that indicates subsumption 
        // relationships between datatypes
        private final String uri;
        
        DT(String position, String uri) {
            this.position = position;
            this.uri = uri;
        }
        
        public String getPosition() { 
            return position; 
        }
        
        public String getURIAsString() { 
            return uri.toString(); 
        }
        
        public URI getURI() {
            return URI.create(uri);
        }
        
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
    
    public int getArity() {
        return 1;
    }
    
    public DT getDatatype() {
        return datatype;
    }
    
    public URI getDatatypeURI() {
        return datatype != null ? datatype.getURI() : null;
    }
    
    public boolean isNegated() {
        return isNegated;
    }
    
    public void negate() {
        isNegated = !isNegated;
    }
    
    public boolean isBottom() {
        if (!isBottom) {
            if (!hasMinCardinality(BigInteger.ONE)) {
                isBottom = true;
            }
        }
        return isBottom;
    }
    
    public Set<DataConstant> getOneOf() {
        return oneOf;
    }
    
    public void setOneOf(Set<DataConstant> oneOf) {
        this.oneOf = oneOf;
    }
    
    public boolean addOneOf(DataConstant constant) {
        return oneOf.add(constant);
    }
    
    public void setNotOneOf(Set<DataConstant> notOneOf) {
        this.notOneOf = notOneOf;
    }
    
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
    
    public boolean facetsAccept(DataConstant constant) {
        return true;
    }
    
    public boolean supports(Facets facet) {
        return supportedFacets.contains(facet);
    }
    
    public String toString() {
        return toString(Namespaces.none);
    }
    
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        if (datatype != null && datatype.getURI() != null) {
            if (isNegated) buffer.append("not ");
            buffer.append(namespaces.idFromUri(datatype.getURIAsString()));
        }
        buffer.append(printExtraInfo(namespaces));
        boolean firstRun = true;
        if (!oneOf.isEmpty()) {
            if (isNegated) buffer.append("not ");
            buffer.append("oneOf(");
            firstRun = true;
            for (DataConstant constant : oneOf) {
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
            for (DataConstant constant : notOneOf) {
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
