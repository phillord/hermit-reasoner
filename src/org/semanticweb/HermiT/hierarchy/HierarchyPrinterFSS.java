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
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.InverseRole;

public class HierarchyPrinterFSS {
    protected static final Pattern s_localNameChecker=Pattern.compile("([a-zA-Z0-9]|-|_)+");
    protected static final Set<String> s_reservedWords=new HashSet<String>();
    
    protected final PrintWriter m_out;
    protected final String m_defaultNamespace;
    protected final Set<String> m_namespaceURIs;
    protected Namespaces m_namespaces;
    
    public HierarchyPrinterFSS(PrintWriter out,String defaultNamespace) {
        m_out=out;
        m_defaultNamespace=defaultNamespace;
        m_namespaceURIs=new TreeSet<String>();
        m_namespaceURIs.add(defaultNamespace);
        m_namespaceURIs.add(Namespaces.s_semanticWebNamespaces.get("owl"));
    }
    public void loadAtomicConceptNamespaces(Collection<AtomicConcept> atomicConcepts) {
        for (AtomicConcept atomicConcept : atomicConcepts) {
            String uri=atomicConcept.getURI();
            int hashIndex=uri.indexOf('#');
            if (hashIndex!=-1) {
                String namespace=uri.substring(0,hashIndex+1);
                String localName=uri.substring(hashIndex+1);
                if (isSafeLocalName(localName))
                    m_namespaceURIs.add(namespace);
            }
        }
    }
    public void loadAtomicRoleNamespaces(Collection<AtomicRole> atomicRoles) {
        for (AtomicRole atomicRole : atomicRoles) {
            String uri=atomicRole.getURI();
            int hashIndex=uri.indexOf('#');
            if (hashIndex!=-1) {
                String namespace=uri.substring(0,hashIndex+1);
                String localName=uri.substring(hashIndex+1);
                if (isSafeLocalName(localName))
                    m_namespaceURIs.add(namespace);
            }
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
        String owlNamespace=Namespaces.s_semanticWebNamespaces.get("owl");
        m_namespaces=new Namespaces();
        m_namespaces.registerDefaultNamespace(m_defaultNamespace);
        m_namespaces.registerNamespace("owl",owlNamespace);
        int index=1;
        for (String namespace : m_namespaceURIs)
            if (!m_defaultNamespace.equals(namespace) && !owlNamespace.equals(namespace)) {
                String prefix="a"+(index++);
                m_namespaces.registerNamespace(prefix,namespace);
            }
        for (Map.Entry<String,String> entry : m_namespaces.getNamespacesByPrefix().entrySet())
            m_out.println("Namespace("+entry.getKey()+"=<"+entry.getValue()+">)");
        m_out.println();
        m_out.println("Ontology(<"+m_namespaces.getNamespacesByPrefix().get("")+">");
        m_out.println();
    }
    public void printAtomicConceptHierarchy(Hierarchy<AtomicConcept> atomicConceptHierarchy) {
        Hierarchy<AtomicConcept> sortedAtomicConceptHierarchy=atomicConceptHierarchy.transform(new IdentityTransformer<AtomicConcept>(),AtomicConceptComparator.INSTANCE);
        AtomicConceptPrinter atomicConceptPrinter=new AtomicConceptPrinter(sortedAtomicConceptHierarchy.getBottomNode());
        sortedAtomicConceptHierarchy.traverseDepthFirst(atomicConceptPrinter);
        atomicConceptPrinter.printNode(0,sortedAtomicConceptHierarchy.getBottomNode(),null,true);
    }
    public void printRoleHierarchy(Hierarchy<? extends Role> roleHierarchy,boolean objectProperties) {
        Hierarchy<PrintableRole> sortedRoleHierarchy=roleHierarchy.transform(new Role2PrintableRoleTransformer(),PrintableRoleComparator.INSTANCE);
        RolePrinter rolePrinter=new RolePrinter(sortedRoleHierarchy.getBottomNode(),objectProperties);
        sortedRoleHierarchy.traverseDepthFirst(rolePrinter);
        rolePrinter.printNode(0,sortedRoleHierarchy.getBottomNode(),null,true);
    }
    public void endPrinting() {
        m_out.println();
        m_out.println(")");
    }

    protected class AtomicConceptPrinter implements Hierarchy.HierarchyNodeVisitor<AtomicConcept> {
        protected final HierarchyNode<AtomicConcept> m_bottomNode;

        public AtomicConceptPrinter(HierarchyNode<AtomicConcept> bottomNode) {
            m_bottomNode=bottomNode;
        }
        public void visit(int level,HierarchyNode<AtomicConcept> node,HierarchyNode<AtomicConcept> parentNode,boolean firstVisit) {
            if (node!=m_bottomNode)
                printNode(level,node,parentNode,firstVisit);
        }
        public void printNode(int level,HierarchyNode<AtomicConcept> node,HierarchyNode<AtomicConcept> parentNode,boolean firstVisit) {
            Set<AtomicConcept> equivalences=node.getEquivalentElements();
            boolean printSubClasOf=(parentNode!=null);
            boolean printEquivalences=firstVisit && equivalences.size()>1;
            boolean printDeclarations=false;
            if (firstVisit) {
                for (AtomicConcept atomicConcept : equivalences)
                    if (needsDeclaration(atomicConcept)) {
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
                    print(node.getRepresentative());
                    m_out.print(' ');
                    print(parentNode.getRepresentative());
                    m_out.print(" )");
                    afterWS=false;
                }
                if (printEquivalences) {
                    if (!afterWS)
                        m_out.print(' ');
                    m_out.print("EquivalentClasses(");
                    for (AtomicConcept atomicConcept : equivalences) {
                        m_out.print(' ');
                        print(atomicConcept);
                    }
                    m_out.print(" )");
                    afterWS=false;
                }
                if (printDeclarations) {
                    for (AtomicConcept atomicConcept : equivalences) {
                        if (needsDeclaration(atomicConcept)) {
                            if (!afterWS)
                                m_out.print(' ');
                            m_out.print("Declaration( Class( ");
                            print(atomicConcept);
                            m_out.print(" ) )");
                            afterWS=false;
                        }
                    }
                }
                m_out.println();
            }
        }
        protected void print(AtomicConcept atomicConcept) {
            m_out.print(m_namespaces.abbreviateURISafe(atomicConcept.getURI(),s_localNameChecker,s_reservedWords));
        }
        protected boolean needsDeclaration(AtomicConcept atomicConcept) {
            return !AtomicConcept.THING.equals(atomicConcept) && !AtomicConcept.NOTHING.equals(atomicConcept);
        }
    }
    
    protected class IdentityTransformer<E> implements Hierarchy.Transformer<E,E> {

        public E transform(E object) {
            return object;
        }
        public E determineRepresentative(E oldRepresentative,Set<E> newEquivalentElements) {
            return ((SortedSet<E>)newEquivalentElements).first();
        }
    }

    protected class RolePrinter implements Hierarchy.HierarchyNodeVisitor<PrintableRole> {
        protected final HierarchyNode<PrintableRole> m_bottomNode;
        protected final boolean m_objectProperties;

        public RolePrinter(HierarchyNode<PrintableRole> bottomNode,boolean objectProperties) {
            m_bottomNode=bottomNode;
            m_objectProperties=objectProperties;
        }
        public void visit(int level,HierarchyNode<PrintableRole> node,HierarchyNode<PrintableRole> parentNode,boolean firstVisit) {
            if (node!=m_bottomNode)
                printNode(level,node,parentNode,firstVisit);
        }
        public void printNode(int level,HierarchyNode<PrintableRole> node,HierarchyNode<PrintableRole> parentNode,boolean firstVisit) {
            Set<PrintableRole> equivalences=node.getEquivalentElements();
            boolean printSubPropertyOf=(parentNode!=null);
            boolean printEquivalences=firstVisit && equivalences.size()>1;
            boolean printDeclarations=false;
            if (firstVisit) {
                for (PrintableRole element : equivalences)
                    if (!element.m_inverse && !element.m_uri.startsWith("owl:")) {
                        printDeclarations=true;
                        break;
                    }
            }
            if (printSubPropertyOf || printEquivalences || printDeclarations) {
                for (int i=2*level;i>0;--i)
                    m_out.print(' ');
                boolean afterWS=true;
                if (printSubPropertyOf) {
                    if (m_objectProperties)
                        m_out.print("SubObjectPropertyOf( ");
                    else
                        m_out.print("SubDataPropertyOf( ");
                    m_out.print(node.getRepresentative());
                    m_out.print(' ');
                    m_out.print(parentNode.getRepresentative());
                    m_out.print(" )");
                    afterWS=false;
                }
                if (printEquivalences) {
                    if (!afterWS)
                        m_out.print(' ');
                    if (m_objectProperties)
                        m_out.print("EquivalentObjectProperties(");
                    else
                        m_out.print("EquivalentDataProperties(");
                    for (PrintableRole element : equivalences) {
                        m_out.print(' ');
                        m_out.print(element);
                    }
                    m_out.print(" )");
                    afterWS=false;
                }
                if (printDeclarations) {
                    for (PrintableRole element : equivalences) {
                        if (!element.m_inverse && !element.m_uri.startsWith("owl:")) {
                            if (!afterWS)
                                m_out.print(' ');
                            m_out.print("Declaration( ");
                            if (m_objectProperties)
                                m_out.print("ObjectProperty( ");
                            else
                                m_out.print("DataProperty( ");
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
    
    protected class Role2PrintableRoleTransformer implements Hierarchy.Transformer<Role,PrintableRole> {

        public PrintableRole transform(Role role) {
            AtomicRole atomicRole;
            boolean inverse;
            if (role instanceof AtomicRole) {
                atomicRole=(AtomicRole)role;
                inverse=false;
            }
            else {
                atomicRole=((InverseRole)role).getInverseOf();
                inverse=true;
            }
            return new PrintableRole(m_namespaces.abbreviateURISafe(atomicRole.getURI(),s_localNameChecker,s_reservedWords),inverse);
        }
        public PrintableRole determineRepresentative(Role oldRepresentative,Set<PrintableRole> newEquivalentElements) {
            return ((SortedSet<PrintableRole>)newEquivalentElements).first();
        }
    }

    protected static class PrintableRole {
        protected final String m_uri;
        protected final boolean m_inverse;
        
        public PrintableRole(String uri,boolean inverse) {
            m_uri=uri;
            m_inverse=inverse;
        }
        public int hashCode() {
            return m_inverse ? m_uri.hashCode() : -m_uri.hashCode();
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof PrintableRole))
                return false;
            PrintableRole thatRole=(PrintableRole)that;
            return thatRole.m_uri.equals(m_uri) && thatRole.m_inverse==m_inverse;
        }
        public String toString() {
            if (m_inverse)
                return "ObjectInverseOf( "+m_uri+" )";
            else
                return m_uri;
        }
    }
    
    protected static class PrintableRoleComparator implements Comparator<PrintableRole> {
        public static final PrintableRoleComparator INSTANCE=new PrintableRoleComparator();

        public int compare(PrintableRole printableRole1,PrintableRole printableRole2) {
            int class1=getPrintableRoleClass(printableRole1);
            int class2=getPrintableRoleClass(printableRole2);
            if (class1!=class2)
                return class1-class2;
            return URIComparator.INSTANCE.compare(printableRole1.m_uri,printableRole2.m_uri);
        }
        protected int getPrintableRoleClass(PrintableRole printableRole) {
            return printableRole.m_inverse ? 1 : 0;
        }
    }
    
    protected static class AtomicConceptComparator implements Comparator<AtomicConcept> {
        public static final AtomicConceptComparator INSTANCE=new AtomicConceptComparator();
    
        public int compare(AtomicConcept atomicConcept1,AtomicConcept atomicConcept2) {
            int class1=getAtomicConceptClass(atomicConcept1);
            int class2=getAtomicConceptClass(atomicConcept2);
            if (class1!=class2)
                return class1-class2;
            return atomicConcept1.getURI().compareTo(atomicConcept2.getURI());
        }
        protected int getAtomicConceptClass(AtomicConcept atomicConcept) {
            if (AtomicConcept.NOTHING.equals(atomicConcept))
                return 0;
            if (AtomicConcept.THING.equals(atomicConcept))
                return 1;
            else
                return 3;
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
            else if ("owl:bottomObjectProperty".equals(uri))
                return 3;
            else if ("owl:topObjectProperty".equals(uri))
                return 4;
            else if ("owl:bottomDataProperty".equals(uri))
                return 5;
            else if ("owl:topDataProperty".equals(uri))
                return 6;
            return 7;
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
