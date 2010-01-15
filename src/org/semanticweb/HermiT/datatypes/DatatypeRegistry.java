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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.anyuri.AnyURIDatatypeHandler;
import org.semanticweb.HermiT.datatypes.binarydata.BinaryDataDatatypeHandler;
import org.semanticweb.HermiT.datatypes.bool.BooleanDatatypeHandler;
import org.semanticweb.HermiT.datatypes.datetime.DateTimeDatatypeHandler;
import org.semanticweb.HermiT.datatypes.doublenum.DoubleDatatypeHandler;
import org.semanticweb.HermiT.datatypes.floatnum.FloatDatatypeHandler;
import org.semanticweb.HermiT.datatypes.owlreal.OWLRealDatatypeHandler;
import org.semanticweb.HermiT.datatypes.rdfplainliteral.RDFPlainLiteralDatatypeHandler;
import org.semanticweb.HermiT.datatypes.xmlliteral.XMLLiteralDatatypeHandler;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DatatypeRestriction;

/**
 * A registry for all available datatype handlers.
 */
public class DatatypeRegistry {
    protected static final Map<String,DatatypeHandler> s_handlersByDatatypeURI=new HashMap<String,DatatypeHandler>();
    protected static final Map<Class<?>,DatatypeHandler> s_handlersByDataValueClass=new HashMap<Class<?>,DatatypeHandler>();
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
    
    public static void registerDatatypeHandler(DatatypeHandler datatypeHandler) {
        synchronized (s_handlersByDatatypeURI) {
            for (String datatypeURI : datatypeHandler.getManagedDatatypeURIs())
                if (s_handlersByDatatypeURI.containsKey(datatypeURI))
                    throw new IllegalArgumentException("Datatype handler for datatype '"+datatypeURI+"' has already been registed.");
            for (Class<?> dataValueClass: datatypeHandler.getManagedDataValueClasses())
                if (s_handlersByDataValueClass.containsKey(dataValueClass))
                    throw new IllegalArgumentException("Datatype handler for data value class '"+dataValueClass+"' has already been registed.");
            for (String datatypeURI : datatypeHandler.getManagedDatatypeURIs())
                s_handlersByDatatypeURI.put(datatypeURI,datatypeHandler);
            for (Class<?> dataValueClass: datatypeHandler.getManagedDataValueClasses())
                s_handlersByDataValueClass.put(dataValueClass,datatypeHandler);
        }
    }
    protected static DatatypeHandler getDatatypeHandlerFor(String datatypeURI) throws UnsupportedDatatypeException {
        DatatypeHandler datatypeHandler;
        synchronized (s_handlersByDatatypeURI) {
            datatypeHandler=s_handlersByDatatypeURI.get(datatypeURI);
        }
        if (datatypeHandler==null)
            throw new UnsupportedDatatypeException("Datatype handler for datatype '"+datatypeURI+"' has not been registed.");
        else
            return datatypeHandler;
    }
    protected static DatatypeHandler getDatatypeHandlerFor(DatatypeRestriction datatypeRestriction) throws UnsupportedDatatypeException {
        return getDatatypeHandlerFor(datatypeRestriction.getDatatypeURI());
    }
    public static String toString(Prefixes prefixes,Object dataValue) {
        DatatypeHandler datatypeHandler;
        Class<?> dataValueClass=dataValue.getClass();
        synchronized (s_handlersByDatatypeURI) {
            datatypeHandler=s_handlersByDataValueClass.get(dataValueClass);
        }
        if (datatypeHandler==null)
            throw new IllegalArgumentException("Datatype handler for data value of class '"+dataValueClass+"' has not been registed.");
        else
            return datatypeHandler.toString(prefixes,dataValue);
    }
    public static Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException,UnsupportedDatatypeException {
        return getDatatypeHandlerFor(datatypeURI).parseLiteral(lexicalForm,datatypeURI);
    }
    public static void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedDatatypeException,UnsupportedFacetException {
        getDatatypeHandlerFor(datatypeRestriction).validateDatatypeRestriction(datatypeRestriction);
    }
    public static ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        return getDatatypeHandlerFor(datatypeRestriction).createValueSpaceSubset(datatypeRestriction);
    }
    public static ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        return getDatatypeHandlerFor(datatypeRestriction).conjoinWithDR(valueSpaceSubset,datatypeRestriction);
    }
    public static ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        return getDatatypeHandlerFor(datatypeRestriction).conjoinWithDRNegation(valueSpaceSubset,datatypeRestriction);
    }
    public static boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        DatatypeHandler datatypeHandler=getDatatypeHandlerFor(subsetDatatypeURI);
        if (datatypeHandler.getManagedDatatypeURIs().contains(supersetDatatypeURI))
            return datatypeHandler.isSubsetOf(subsetDatatypeURI,supersetDatatypeURI);
        else
            return false;
    }
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
        protected final static Set<Class<?>> s_managedDataValueClasses=new HashSet<Class<?>>();
        static {
            s_managedDataValueClasses.add(Constant.AnonymousConstantValue.class);
        }

        public Set<String> getManagedDatatypeURIs() {
            return s_managedDatatypeURIs;
        }
        public Set<Class<?>> getManagedDataValueClasses() {
            return s_managedDataValueClasses;
        }
        public String toString(Prefixes prefixes,Object dataValue) {
            Constant.AnonymousConstantValue value=(Constant.AnonymousConstantValue)dataValue;
            return '\"'+value.getName()+"\"^^"+prefixes.abbreviateIRI(ANONYMOUS_CONSTANTS);
        }
        public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
            assert ANONYMOUS_CONSTANTS.equals(datatypeURI);
            return new Constant.AnonymousConstantValue(lexicalForm.trim());
        }
        public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
        public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
        public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
        public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
        public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
        public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
            throw new IllegalStateException("Internal error: anonymous constants datatype should not occur in datatype restrictions.");
        }
    }
}
