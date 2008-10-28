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
    
    public enum Impl {
        IInteger (111), 
        IDouble (11),
        IDecimal (1),
        IDateTime (2),
        IString (3),
        IBoolean (4), 
        ILiteral (5), 
        IAnyURI (6);
        
        private final int position;
        
        Impl(int position) {
            this.position = position;
        }
        
        public int getPosition() { 
            return position; 
        }
    }
    public enum DT {
        
        OWLREALPLUS (Impl.IDecimal, "1", (uris.get("owl") + "realPlus")),
        OWLREAL (Impl.IDecimal, "11", (uris.get("owl") + "real")),
        DECIMAL (Impl.IDecimal, "111", (uris.get("xsd") + "decimal")),
        DOUBLE (Impl.IDouble, "1111", (uris.get("xsd") + "double")),
        FLOAT (Impl.IDouble, "11111", (uris.get("xsd") + "float")),
        INTEGER (Impl.IInteger, "111111", (uris.get("xsd") + "integer")),
        NONNEGATIVEINTEGER (Impl.IInteger, "1111111", (uris.get("xsd") + "nonNegativeInteger")),
        NONPOSITIVEINTEGER (Impl.IInteger, "1111112", (uris.get("xsd") + "nonPositiveInteger")),
        POSITIVEINTEGER (Impl.IInteger, "11111111", (uris.get("xsd") + "positiveInteger")),
        NEGATIVEINTEGER (Impl.IInteger, "11111121", (uris.get("xsd") + "negativeInteger")),
        LONG (Impl.IInteger, "1111113", (uris.get("xsd") + "long")),
        INT (Impl.IInteger, "11111131", (uris.get("xsd") + "int")),
        SHORT (Impl.IInteger, "111111311", (uris.get("xsd") + "short")),
        BYTE (Impl.IInteger, "1111113111", (uris.get("xsd") + "byte")),
        UNSIGNEDLONG (Impl.IInteger, "1111114", (uris.get("xsd") + "unsignedLong")),
        UNSIGNEDINT (Impl.IInteger, "11111141", (uris.get("xsd") + "unsignedInt")),
        UNSIGNEDSHORT (Impl.IInteger, "111111411", (uris.get("xsd") + "unsignedShort")),
        UNSIGNEDBYTE (Impl.IInteger, "1111114111", (uris.get("xsd") + "unsignedByte")),
        OWLDATETIME  (Impl.IDateTime, "2", (uris.get("owl") + "dateTime")), 
        DATETIME (Impl.IDateTime, "21", (uris.get("xsd") + "dateTime")),
        RDFTEXT (Impl.IString, "3", (uris.get("rdf") + "text")),
        STRING (Impl.IString, "31", (uris.get("xsd") + "string")),
        BOOLEAN (Impl.IBoolean, "4", (uris.get("xsd") + "boolean")), 
        LITERAL (Impl.ILiteral, "5", (uris.get("rdfs") + "Literal")),
        ANYURI (Impl.IAnyURI, "6", (uris.get("xsd") + "anyURI"));        

        private final Impl impl;
        private final String position;   // in a tree that indicates subsumption 
        // relationships between datatypes
        private final String uri;
        
        DT(Impl impl, String position, String uri) {
            this.impl = impl;
            this.position = position;
            this.uri = uri;
        }
        
        public int getImpl() { 
            return impl.getPosition(); 
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
