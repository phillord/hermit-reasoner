package org.semanticweb.HermiT.model;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypeRestrictionDateTime extends DatatypeRestrictionLiteral {
    
    public static DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    protected Date minInclusive = null;
    protected Date maxInclusive = null;

    public DatatypeRestrictionDateTime() {
        this.datatypeURI = XSDVocabulary.DATE_TIME.getURI();
        this.supportedFacets = new HashSet<Facets>(
                Arrays.asList(new Facets[] {
                        Facets.MIN_INCLUSIVE, Facets.MIN_EXCLUSIVE, Facets.MAX_INCLUSIVE, Facets.MAX_EXCLUSIVE
                })
        );
    }
    
    public DataRange getNewInstance() {
        return new DatatypeRestrictionDateTime();
    }
    
    public boolean isFinite() {
        return ((minInclusive != null && maxInclusive != null) 
                || !oneOf.isEmpty());
    }
    public boolean hasMinCardinality(int n) {
        if (n == 0) return true;
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return (oneOf.size() >= n);
            }
            BigInteger nBig = new BigInteger("" + n);
            int substract = 0;
            for (String constant : notOneOf) {
                DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                try {
                    Date not = dfm.parse(constant);
                    if ((not.compareTo(maxInclusive) <= 0)
                            && (not.compareTo(minInclusive) >= 0)) {
                        substract++;
                    } else {
                        removeOneOf(constant);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            BigInteger bigMinInclusive = new BigInteger("" + minInclusive.getTime());
            BigInteger bigMaxInclusive = new BigInteger("" + maxInclusive.getTime());
            BigInteger actualSize = bigMaxInclusive.subtract(bigMinInclusive);
            actualSize = actualSize.add(BigInteger.ONE);
            actualSize = actualSize.subtract(new BigInteger("" + substract));
            return (actualSize.compareTo(nBig) >= 0);
        }
        return true;
    }
    public Set<String> getEnumeration() {
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return oneOf;
            }
            BigInteger bigMinInclusive = new BigInteger("" + minInclusive.getTime());
            BigInteger bigMaxInclusive = new BigInteger("" + maxInclusive.getTime());
            BigInteger actualSize = bigMaxInclusive.subtract(bigMinInclusive);
            if (actualSize.compareTo(new BigInteger("" + Integer.MAX_VALUE)) > 0) {
                System.err.println("Datatype checking produced a set with more than " + Integer.MAX_VALUE + "entries!");
                System.err.println("I give up!");
                return null;
            } else {
                int bound = actualSize.intValue();
                if (bound > 1000) {
                    System.err.println("Datatype checking produces set with more than 1000 entries!");
                    System.err.println("I'll try to handle that!");
                }
                Set<String> result = new HashSet<String>();
                long lower = minInclusive.getTime();
                long upper = maxInclusive.getTime();
                for (long i = lower; i <= upper; i++) {
                    result.add("" + dfm.format(new Date(i)));
                }
                return result;
            }
        } 
        return null;
    }
    public String getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            SortedSet<String> sortedOneOfs = new TreeSet<String>(oneOf);
            for (String constant : sortedOneOfs) {
                if (!notOneOf.contains(constant)) return sortedOneOfs.first();
            }
            return null;
        }
        BigInteger bigMinInclusive = new BigInteger("" + minInclusive.getTime());
        BigInteger bigMaxInclusive = new BigInteger("" + maxInclusive.getTime());
        BigInteger actualSize = bigMaxInclusive.subtract(bigMinInclusive);
        if (actualSize.compareTo(BigInteger.ZERO) >= 0) {
            BigInteger constant = bigMinInclusive;
            while (constant.compareTo(bigMaxInclusive) <= 0) {
                if (!notOneOf.contains(constant.toString())) return constant.toString();
                constant = constant.add(BigInteger.ONE);
            }
            return null;
        }
        return null;
    }
    public boolean accepts(String constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        boolean accepted = true;
        try {
            Date dateValue = dfm.parse(constant);
            if (minInclusive != null) {
                accepted = accepted && (minInclusive.compareTo(dateValue) >= 0);
            }
            if (maxInclusive != null) {
                accepted = accepted && (maxInclusive.compareTo(dateValue) <= 0);
            }
            accepted = accepted && !notOneOf.contains(constant);
            return accepted; 
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean isTop() {
        return false; 
    }
    public boolean isBottom() {
        return !hasMinCardinality(1);
    }
    public boolean addOneOf(String constant) {
        boolean result = false;
        try {
            dfm.parse(constant);
            return oneOf.add(constant);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
    public void conjoinFacetsFrom(DataRange range) {
        if (!(range instanceof DatatypeRestrictionDateTime)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionInteger. It is " +
                    "only allowed to add facets from other integer " +
                    "datatype restrictions. ");
        }
        DatatypeRestrictionDateTime restr = (DatatypeRestrictionDateTime) range;
        if (isNegated || restr.isNegated()) {
            throw new RuntimeException("Cannot add facets to or from negated " +
            "data ranges!");
        }
        if (restr.getMinInclusive() != null) {
            addFacet(Facets.MIN_INCLUSIVE, dfm.format(restr.getMinInclusive())); 
        }
        if (restr.getMaxInclusive() != null) {
            addFacet(Facets.MAX_INCLUSIVE, dfm.format(restr.getMaxInclusive()));
        }
    }
    public void addFacet(Facets facet, String value) {
        if (isNegated) {
            throw new RuntimeException("Cannot add facets to negated data ranges!");
        }
        Date valueDate = null;
        try {
            valueDate = dfm.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        switch (facet) {
        case MIN_INCLUSIVE: {
            if (minInclusive != null) {
                if (valueDate.compareTo(minInclusive) < 0) {
                    minInclusive = valueDate;
                }
            } else {
                minInclusive = valueDate;
            }
        } break;
        case MIN_EXCLUSIVE: {
            if (valueDate.getTime() != Long.MAX_VALUE) {
                valueDate.setTime(valueDate.getTime() + 1);
                if (minInclusive != null) {
                    if (valueDate.compareTo(minInclusive) < 0) {
                        minInclusive = valueDate;
                    }
                } else {
                    minInclusive = valueDate;
                }
            } else {
                System.err.println("The date " + value + " is out of the " +
                		"supported range and will be ignored. ");
            }

        } break;
        case MAX_INCLUSIVE: {
            if (maxInclusive != null) {
                if (valueDate.compareTo(maxInclusive) > 0) {
                    maxInclusive = valueDate;
                }
            } else {
                maxInclusive = valueDate;
            }
        } break;
        case MAX_EXCLUSIVE:  {
            if (valueDate.getTime() != Long.MIN_VALUE) {
                valueDate.setTime(valueDate.getTime() - 1);
                if (maxInclusive != null) {
                    if (valueDate.compareTo(maxInclusive) > 0) {
                        maxInclusive = valueDate;
                    }
                } else {
                    maxInclusive = valueDate;
                }
            }
        } break;
        default:
            throw new IllegalArgumentException("Unsupported facet.");
        }
    }
    public Date getMinInclusive() {
        return minInclusive;
    }
    public Date getMaxInclusive() {
        return maxInclusive;
    }
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        if (isNegated) buffer.append("(not"); 
        buffer.append("(");
        buffer.append(namespaces.idFromUri(datatypeURI.toString()));
        for (String value : oneOf) {
            buffer.append(" " + value);
        }
        for (String value : notOneOf) {
            buffer.append(" not " + value);
        }
        if (minInclusive != null) {
            buffer.append(" >= " + minInclusive);
        }
        if (maxInclusive != null) {
            buffer.append(" <= " + maxInclusive);
        }
        buffer.append(")");
        if (isNegated) buffer.append(")");
        return buffer.toString();        
    }
}
