package org.semanticweb.HermiT.hierarchy;

import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.io.PrintWriter;

import org.semanticweb.HermiT.Namespaces;

public class Hierarchy<E> {
    protected final HierarchyNode<E> m_topNode;
    protected final HierarchyNode<E> m_bottomNode;
    protected final Map<E,HierarchyNode<E>> m_nodesByElements;
    
    public Hierarchy(HierarchyNode<E> topNode,HierarchyNode<E> bottomNode) {
        m_topNode=topNode;
        m_bottomNode=bottomNode;
        m_nodesByElements=new HashMap<E,HierarchyNode<E>>();
        for (E element : m_topNode.m_equivalentElements)
            m_nodesByElements.put(element,m_topNode);
        for (E element : m_bottomNode.m_equivalentElements)
            m_nodesByElements.put(element,m_bottomNode);
    }
    public HierarchyNode<E> getTopNode() {
        return m_topNode;
    }
    public HierarchyNode<E> getBottomNode() {
        return m_bottomNode;
    }
    public HierarchyNode<E> getNodeForElement(E element) {
        return m_nodesByElements.get(element);
    }
    public Collection<HierarchyNode<E>> getAllNodes() {
        return Collections.unmodifiableCollection(m_nodesByElements.values());
    }
    public Set<E> getAllElements() {
        return Collections.unmodifiableSet(m_nodesByElements.keySet());
    }
    public static <T> Hierarchy<T> emptyHierarchy(Collection<T> elements,T topElement,T bottomElement) {
        HierarchyNode<T> topBottomNode=new HierarchyNode<T>(topElement);
        topBottomNode.m_equivalentElements.add(topElement);
        topBottomNode.m_equivalentElements.add(bottomElement);
        topBottomNode.m_equivalentElements.addAll(elements);
        return new Hierarchy<T>(topBottomNode,topBottomNode);
    }
    public void print(Printer<E> printer) {
        PrintNodeComparator printNodeComparator=new PrintNodeComparator(printer.getComparator());
        Map<HierarchyNode<E>,PrintNode> nodeToPrintNode=new HashMap<HierarchyNode<E>,PrintNode>();
        for (HierarchyNode<E> node : m_nodesByElements.values()) {
            PrintNode printNode=new PrintNode(node,printer,printNodeComparator);
            nodeToPrintNode.put(node,printNode);
        }
        for (HierarchyNode<E> node : m_nodesByElements.values()) {
            PrintNode printNode=nodeToPrintNode.get(node);
            for (HierarchyNode<E> childNode : node.m_childNodes) {
                PrintNode childPrintNode=nodeToPrintNode.get(childNode);
                printNode.m_children.add(childPrintNode);
            }
        }
        for (Map.Entry<String,String> entry : printer.getNamespaces().getPrefixDeclarations().entrySet())
            printer.getOut().println("Namespace("+entry.getKey()+"=<"+entry.getValue()+">)");
        printer.getOut().println();
        printer.getOut().println("Ontology(<"+printer.getNamespaces().getPrefixDeclarations().get("")+">");
        printer.getOut().println();
        Set<PrintNode> printed=new HashSet<PrintNode>();
        PrintNode topPrintNode=nodeToPrintNode.get(m_topNode);
        if (topPrintNode.m_equivalentElements.size()>1) {
            printer.printEquivalences(topPrintNode,false);
            printer.printDeclarations(topPrintNode);
            printer.getOut().println();
        }
        for (PrintNode childPrintNode : topPrintNode.m_children)
            print(printer,printed,childPrintNode,topPrintNode,1);
        PrintNode bottomPrintNode=nodeToPrintNode.get(m_bottomNode);
        if (bottomPrintNode.m_equivalentElements.size()>1) {
            printer.printEquivalences(bottomPrintNode,false);
            printer.printDeclarations(bottomPrintNode);
            printer.getOut().println();
        }
        printer.getOut().println();
        printer.getOut().println(")");
    }
    protected void print(Printer<E> printer,Set<PrintNode> printed,PrintNode printNode,PrintNode parentPrintNode,int level) {
        for (int i=2*level;i>0;--i)
            printer.getOut().print(' ');
        printer.printRelation(printNode,parentPrintNode);
        if (printed.add(printNode)) {
            printer.printEquivalences(printNode,true);
            printer.printDeclarations(printNode);
            printer.getOut().println();
            for (PrintNode childPrintNode : printNode.m_children)
                if (childPrintNode.m_node!=m_bottomNode)
                    print(printer,printed,childPrintNode,printNode,level+1);
        }
        else
            printer.getOut().println();
    }
    
    public static interface Printer<E> {
        PrintWriter getOut();
        String toString(E element);
        Namespaces getNamespaces();
        Comparator<String> getComparator();
        void printDeclarations(PrintNode printNode);
        void printEquivalences(PrintNode printNode,boolean precedeWithSpace);
        void printRelation(PrintNode childNode,PrintNode parentNode);
    }
    
    public static class PrintNode {
        protected final HierarchyNode<?> m_node;
        protected final String m_canonical;
        protected final SortedSet<String> m_equivalentElements;
        protected final SortedSet<PrintNode> m_children;
        
        public <E> PrintNode(HierarchyNode<E> node,Printer<E> printer,Comparator<PrintNode> printNodeComparator) {
            m_node=node;
            m_equivalentElements=new TreeSet<String>(printer.getComparator());
            for (E element : node.m_equivalentElements)
                m_equivalentElements.add(printer.toString(element));
            m_canonical=m_equivalentElements.first();
            m_children=new TreeSet<PrintNode>(printNodeComparator);
        }
    }

    protected static class PrintNodeComparator implements Comparator<PrintNode> {
        protected final Comparator<String> m_comparator;

        public PrintNodeComparator(Comparator<String> comparator) {
            m_comparator=comparator;
        }
        public int compare(PrintNode o1,PrintNode o2) {
            return m_comparator.compare(o1.m_canonical,o2.m_canonical);
        }
        
    }
}
