// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.model.AtomicConcept;

public class HierarchyPrinterFSS {
    protected static final Pattern s_localNameChecker=Pattern.compile("([a-zA-Z0-9]|-|_)+");
    protected static final Set<String> s_reservedWords=new HashSet<String>();
    
    protected final PrintWriter m_out;
    protected final Namespaces m_namespaces;
    
    public HierarchyPrinterFSS(PrintWriter out,String defaultNamespace,Collection<AtomicConcept> atomicConcepts) {
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
    public PrintWriter getOut() {
        return m_out;
    }
    public Comparator<String> getComparator() {
        return URIComparator.INSTANCE;
    }
    public void startPrinting() {
        for (Map.Entry<String,String> entry : m_namespaces.getPrefixDeclarations().entrySet())
            m_out.println("Namespace("+entry.getKey()+"=<"+entry.getValue()+">)");
        m_out.println();
        m_out.println("Ontology(<"+m_namespaces.getPrefixDeclarations().get("")+">");
        m_out.println();
    }
    public void printAtomicConceptHierarchy(Hierarchy<AtomicConcept> atomicConceptHierarchy) {
        Hierarchy<String> sortedAtomicConceptHierarchy=atomicConceptHierarchy.transform(new AtomicConcept2StringTransformer(),URIComparator.INSTANCE);
        AtomicConceptPrinter atomicConceptPrinter=new AtomicConceptPrinter(sortedAtomicConceptHierarchy.getBottomNode());
        sortedAtomicConceptHierarchy.traverseDepthFirst(atomicConceptPrinter);
        atomicConceptPrinter.printNode(0,sortedAtomicConceptHierarchy.getBottomNode(),null,true);
    }
    public void endPrinting() {
        m_out.println();
        m_out.println(")");
    }

    protected class AtomicConceptPrinter implements Hierarchy.HierarchyNodeVisitor<String> {
        protected final HierarchyNode<String> m_bottomNode;

        public AtomicConceptPrinter(HierarchyNode<String> bottomNode) {
            m_bottomNode=bottomNode;
        }
        public void visit(int level,HierarchyNode<String> node,HierarchyNode<String> parentNode,boolean firstVisit) {
            if (node!=m_bottomNode)
                printNode(level,node,parentNode,firstVisit);
        }
        public void printNode(int level,HierarchyNode<String> node,HierarchyNode<String> parentNode,boolean firstVisit) {
            Set<String> equivalences=node.getEquivalentElements();
            boolean printSubClasOf=(parentNode!=null);
            boolean printEquivalences=firstVisit && equivalences.size()>1;
            boolean printDeclarations=false;
            if (firstVisit) {
                for (String element : equivalences)
                    if (!element.startsWith("owl:")) {
                        printDeclarations=true;
                        break;
                    }
            }
            if (printSubClasOf || printEquivalences || printDeclarations) {
                for (int i=2*level;i>0;--i)
                    m_out.print(' ');
                boolean afterWS=true;
                if (printSubClasOf) {
                    m_out.print("SubClassOf( ");
                    m_out.print(node.getRepresentative());
                    m_out.print(' ');
                    m_out.print(parentNode.getRepresentative());
                    m_out.print(" )");
                    afterWS=false;
                }
                if (printEquivalences) {
                    if (!afterWS)
                        m_out.print(' ');
                    m_out.print("EquivalentClasses(");
                    for (String element : equivalences) {
                        m_out.print(' ');
                        m_out.print(element);
                    }
                    m_out.print(" )");
                    afterWS=false;
                }
                if (printDeclarations) {
                    for (String element : equivalences) {
                        if (!element.startsWith("owl:")) {
                            if (!afterWS)
                                m_out.print(' ');
                            m_out.print("Declaration( Class( ");
                            m_out.print(element);
                            m_out.print(" ) )");
                            afterWS=false;
                        }
                    }
                }
                m_out.println();
            }
        }
    }
    
    protected class AtomicConcept2StringTransformer implements Hierarchy.Transformer<AtomicConcept,String> {

        public String transform(AtomicConcept atomicConcept) {
            return m_namespaces.abbreviateURISafe(atomicConcept.getURI(),s_localNameChecker,s_reservedWords);
        }
        public String determineRepresentative(AtomicConcept oldRepresentative,Set<String> newEquivalentElements) {
            return ((SortedSet<String>)newEquivalentElements).first();
        }
    }

    protected static class URIComparator implements Comparator<String> {
        public static final URIComparator INSTANCE=new URIComparator();
    
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
