// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.hierarchy.Hierarchy.PrintNode;
import org.semanticweb.HermiT.model.AtomicConcept;

public class Concepts2FSSPrinter implements Hierarchy.Printer<AtomicConcept> {
    protected static final Pattern s_localNameChecker=Pattern.compile("([a-zA-Z0-9]|-|_)+");
    protected static final Set<String> s_reservedWords=new HashSet<String>();
    
    protected final PrintWriter m_out;
    protected final Namespaces m_namespaces;
    
    public Concepts2FSSPrinter(PrintWriter out,String defaultNamespace,Collection<AtomicConcept> atomicConcepts) {
        m_out=out;
        Set<String> namespaces=new TreeSet<String>();
        for (AtomicConcept atomicConcept : atomicConcepts) {
            String uri=atomicConcept.getURI();
            int hashIndex=uri.indexOf('#');
            if (hashIndex!=-1) {
                String namespace=uri.substring(0,hashIndex+1);
                String localName=uri.substring(hashIndex+1);
                if (isSafeLocalName(localName))
                    namespaces.add(namespace);
            }
        }
        namespaces.remove(defaultNamespace);
        namespaces.remove(Namespaces.s_semanticWebNamespaces.get("owl"));
        m_namespaces=new Namespaces();
        m_namespaces.registerDefaultNamespace(defaultNamespace);
        m_namespaces.registerNamespace("owl",Namespaces.s_semanticWebNamespaces.get("owl"));
        int index=1;
        for (String namespace : namespaces) {
            String prefix="a"+(index++);
            m_namespaces.registerNamespace(prefix,namespace);
        }
    }
    protected boolean isSafeLocalName(String localName) {
        return s_localNameChecker.matcher(localName).matches() && !s_reservedWords.contains(localName);
    }
    public String toString(AtomicConcept atomicConcept) {
        String uri=atomicConcept.getURI();
        return m_namespaces.abbreviateURISafe(uri,s_localNameChecker,s_reservedWords);
    }
    public PrintWriter getOut() {
        return m_out;
    }
    public Namespaces getNamespaces() {
        return m_namespaces;
    }
    public Comparator<String> getComparator() {
        return ConceptURIComparator.INSTANCE;
    }
    public void printDeclarations(PrintNode printNode) {
        for (String element : printNode.m_equivalentElements) {
            if (!"owl:Nothing".equals(element) && !"owl:Thing".equals(element)) {
                m_out.print(' ');
                m_out.print("Declaration( Class( ");
                m_out.print(element);
                m_out.print(" ) )");
            }
        }
    }
    public void printEquivalences(PrintNode printNode,boolean precedeWithSpace) {
        if (printNode.m_equivalentElements.size()>1) {
            if (precedeWithSpace)
                m_out.print(' ');
            m_out.print("EquivalentClasses(");
            for (String element : printNode.m_equivalentElements) {
                m_out.print(' ');
                m_out.print(element);
            }
            m_out.print(" )");
        }
    }
    public void printRelation(PrintNode childNode,PrintNode parentNode) {
        m_out.print("SubClassOf( ");
        m_out.print(childNode.m_canonical);
        m_out.print(' ');
        m_out.print(parentNode.m_canonical);
        m_out.print(" )");
    }

    static {
        String[] words=new String[] {
            "Namespace",
            "Ontology",
            "Import",
            "Declaration",
            "Class",
            "Datatype",
            "ObjectProperty",
            "DataProperty",
            "AnnotationProperty",
            "NamedIndividual",
            "Annotation",
            "AnnotationAssertion",
            "SubAnnotationPropertyOf",
            "AnnotationPropertyDomain",
            "AnnotationPropertyRange",
            "ObjectInverseOf",
            "DataIntersectionOf",
            "DataUnionOf",
            "DataComplementOf",
            "DataOneOf",
            "DatatypeRestriction",
            "ObjectIntersectionOf",
            "ObjectUnionOf",
            "ObjectComplementOf",
            "ObjectOneOf",
            "ObjectSomeValuesFrom",
            "ObjectAllValuesFrom",
            "ObjectHasValue",
            "ObjectHasSelf",
            "ObjectMinCardinality",
            "ObjectMaxCardinality",
            "ObjectExactCardinality",
            "DataSomeValuesFrom",
            "DataAllValuesFrom",
            "DataHasValue",
            "DataMinCardinality",
            "DataMaxCardinality",
            "DataExactCardinality",
            "SubClassOf",
            "EquivalentClasses",
            "DisjointClasses",
            "DisjointUnion",
            "SubObjectPropertyOf",
            "ObjectPropertyChain",
            "EquivalentObjectProperties",
            "DisjointObjectProperties",
            "ObjectPropertyDomain",
            "ObjectPropertyRange",
            "InverseObjectProperties",
            "FunctionalObjectProperty",
            "InverseFunctionalObjectProperty",
            "ReflexiveObjectProperty",
            "IrreflexiveObjectProperty",
            "SymmetricObjectProperty",
            "AsymmetricObjectProperty",
            "TransitiveObjectProperty",
            "SubDataPropertyOf",
            "EquivalentDataProperties",
            "DisjointDataProperties",
            "DataPropertyDomain",
            "DataPropertyRange",
            "FunctionalDataProperty",
            "DatatypeDefinition",
            "HasKey",
            "SameIndividual",
            "DifferentIndividuals",
            "ClassAssertion",
            "ObjectPropertyAssertion",
            "NegativeObjectPropertyAssertion",
            "DataPropertyAssertion",
            "NegativeDataPropertyAssertion",

        };
        for (String word : words)
            s_reservedWords.add(word);
    }
}
class ConceptURIComparator implements Comparator<String> {
    public static final ConceptURIComparator INSTANCE=new ConceptURIComparator();

    public int compare(String uri1,String uri2) {
        int class1=getURIClass(uri1);
        int class2=getURIClass(uri2);
        if (class1!=class2)
            return class1-class2;
        return uri1.compareTo(uri2);
    }
    protected int getURIClass(String uri) {
        if ("owl:Nothing".equals(uri))
            return 0;
        else if ("owl:Thing".equals(uri))
            return 1;
        else if (!uri.startsWith("<"))
            return 2;
        else
            return 3;
    }
}
