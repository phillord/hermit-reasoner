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
package org.semanticweb.HermiT.datatypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.datatypes.anyuri.AnyURIDatatypeHandler;
import org.semanticweb.HermiT.datatypes.binarydata.BinaryDataDatatypeHandler;
import org.semanticweb.HermiT.datatypes.bool.BooleanDatatypeHandler;
import org.semanticweb.HermiT.datatypes.datetime.DateTimeDatatypeHandler;
import org.semanticweb.HermiT.datatypes.doublenum.DoubleDatatypeHandler;
import org.semanticweb.HermiT.datatypes.floatnum.FloatDatatypeHandler;
import org.semanticweb.HermiT.datatypes.owlreal.OWLRealDatatypeHandler;
import org.semanticweb.HermiT.datatypes.rdfplainliteral.RDFPlainLiteralDatatypeHandler;
import org.semanticweb.HermiT.datatypes.xmlliteral.XMLLiteralDatatypeHandler;
import org.semanticweb.HermiT.model.DatatypeRestriction;

/**
 * A registry for all available datatype handlers.
 */
public class DatatypeRegistry {
    protected static final Map<String,DatatypeHandler> s_handlersByDatatypeURI=new HashMap<>();
    static {
        registerDatatypeHandler(new AnonymousConstantsDatatypeHandler());
        registerDatatypeHandler(new BooleanDatatypeHandler());
        registerDatatypeHandler(new RDFPlainLiteralDatatypeHandler());
        registerDatatypeHandler(new OWLRealDatatypeHandler());
        registerDatatypeHandler(new DoubleDatatypeHandler());
        registerDatatypeHandler(new FloatDatatypeHandler());
        registerDatatypeHandler(new DateTimeDatatypeHandler());
        registerDatatypeHandler(new BinaryDataDatatypeHandler());
        registerDatatypeHandler(new AnyURIDatatypeHandler());
        registerDatatypeHandler(new XMLLiteralDatatypeHandler());
    }

    static void registerDatatypeHandler(DatatypeHandler datatypeHandler) {
        synchronized (s_handlersByDatatypeURI) {
            for (String datatypeURI : datatypeHandler.getManagedDatatypeURIs())
                if (s_handlersByDatatypeURI.containsKey(datatypeURI))
                    throw new IllegalArgumentException("Datatype handler for datatype '"+datatypeURI+"' has already been registed.");
            for (String datatypeURI : datatypeHandler.getManagedDatatypeURIs())
                s_handlersByDatatypeURI.put(datatypeURI,datatypeHandler);
        }
    }
    protected static DatatypeHandler getDatatypeHandlerFor(String datatypeURI) throws UnsupportedDatatypeException {
        DatatypeHandler datatypeHandler;
        synchronized (s_handlersByDatatypeURI) {
            datatypeHandler=s_handlersByDatatypeURI.get(datatypeURI);
        }
        if (datatypeHandler==null) {
            String CRLF=System.getProperty("line.separator");
            String message=
                "HermiT supports all and only the datatypes of the OWL 2 datatype map, see "+CRLF+
                "http://www.w3.org/TR/owl2-syntax/#Datatype_Maps. "+CRLF+
                "The datatype '"+datatypeURI+"' is not part of the OWL 2 datatype map and "+CRLF+
                "no custom datatype definition is given; "+CRLF+
                "therefore, HermiT cannot handle this datatype.";
            throw new UnsupportedDatatypeException(message);
        }
        else
            return datatypeHandler;
    }
    protected static DatatypeHandler getDatatypeHandlerFor(DatatypeRestriction datatypeRestriction) throws UnsupportedDatatypeException {
        return getDatatypeHandlerFor(datatypeRestriction.getDatatypeURI());
    }
    /**
     * @param lexicalForm lexicalForm
     * @param datatypeURI datatypeURI
     * @return literal
     * @throws MalformedLiteralException if literal is malformed
     * @throws UnsupportedDatatypeException if literal is unsupported
     */
    public static Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException,UnsupportedDatatypeException {
        DatatypeHandler handler;
        try {
            handler=getDatatypeHandlerFor(datatypeURI);
        }
        catch (UnsupportedDatatypeException e) {
            String CRLF=System.getProperty("line.separator");
            String message=
                "Literals can only use the datatypes from the OWL 2 datatype map, see "+CRLF+
                "http://www.w3.org/TR/owl2-syntax/#Datatype_Maps. "+CRLF+
                "The datatype '"+datatypeURI+"' is not part of the OWL 2 datatype map and "+CRLF+
                "HermiT cannot parse this literal.";
            throw new UnsupportedDatatypeException(message, e);
        }
        return handler.parseLiteral(lexicalForm,datatypeURI);
    }
    /**
     * @param datatypeRestriction datatypeRestriction
     * @throws UnsupportedDatatypeException if datatype is unsupported
     * @throws UnsupportedFacetException if facet is unsupported
     */
    public static void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedDatatypeException,UnsupportedFacetException {
        getDatatypeHandlerFor(datatypeRestriction).validateDatatypeRestriction(datatypeRestriction);
    }
    /**
     * @param datatypeRestriction datatypeRestriction
     * @return subset
     */
    public static ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        return getDatatypeHandlerFor(datatypeRestriction).createValueSpaceSubset(datatypeRestriction);
    }
    /**
     * @param valueSpaceSubset valueSpaceSubset
     * @param datatypeRestriction datatypeRestriction
     * @return conjunction
     */
    public static ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        return getDatatypeHandlerFor(datatypeRestriction).conjoinWithDR(valueSpaceSubset,datatypeRestriction);
    }
    /**
     * @param valueSpaceSubset valueSpaceSubset
     * @param datatypeRestriction datatypeRestriction
     * @return conjunction
     */
    public static ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        return getDatatypeHandlerFor(datatypeRestriction).conjoinWithDRNegation(valueSpaceSubset,datatypeRestriction);
    }
    /**
     * @param subsetDatatypeURI subsetDatatypeURI
     * @param supersetDatatypeURI supersetDatatypeURI
     * @return true if subset
     */
    public static boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        DatatypeHandler datatypeHandler=getDatatypeHandlerFor(subsetDatatypeURI);
        if (datatypeHandler.getManagedDatatypeURIs().contains(supersetDatatypeURI))
            return datatypeHandler.isSubsetOf(subsetDatatypeURI,supersetDatatypeURI);
        else
            return false;
    }
    /**
     * @param datatypeURI1 datatypeURI1
     * @param datatypeURI2 datatypeURI2
     * @return true if disjoint with
     */
    public static boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        DatatypeHandler datatypeHandler=getDatatypeHandlerFor(datatypeURI1);
        if (datatypeHandler.getManagedDatatypeURIs().contains(datatypeURI2))
            return datatypeHandler.isDisjointWith(datatypeURI1,datatypeURI2);
        else
            return true;
    }

    protected static class AnonymousConstantsDatatypeHandler implements DatatypeHandler {
        protected static final String ANONYMOUS_CONSTANTS="internal:anonymous-constants";
        protected final static Set<String> s_managedDatatypeURIs=Collections.singleton(ANONYMOUS_CONSTANTS);

        @Override
        public Set<String> getManagedDatatypeURIs() {
            return s_managedDatatypeURIs;
        }
        @Override
        public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
            assert ANONYMOUS_CONSTANTS.equals(datatypeURI);
            return new AnonymousConstantValue(lexicalForm.trim());
        }
        @Override
        public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
        @Override
        public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
        @Override
        public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
        @Override
        public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
        @Override
        public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
        @Override
        public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
    }

    static class AnonymousConstantValue {
        protected final String m_name;

        public AnonymousConstantValue(String name) {
            m_name=name;
        }
        public String getName() {
            return m_name;
        }
        @Override
        public int hashCode() {
            return m_name.hashCode();
        }
        @Override
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof AnonymousConstantValue))
                return false;
            return ((AnonymousConstantValue)that).m_name.equals(m_name);
        }
    }

}
