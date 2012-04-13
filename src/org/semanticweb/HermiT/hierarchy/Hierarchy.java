/* Copyright 2009 by the Oxford University Computing Laboratory

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
package org.semanticweb.HermiT.hierarchy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
    public boolean isEmpty() {
        return m_nodesByElements.size()==2 && m_topNode.m_equivalentElements.size()==1 && m_bottomNode.m_equivalentElements.size()==1;
    }
    public HierarchyNode<E> getNodeForElement(E element) {
        return m_nodesByElements.get(element);
    }
    public Collection<HierarchyNode<E>> getAllNodes() {
        return Collections.unmodifiableCollection(m_nodesByElements.values());
    }
    public Set<HierarchyNode<E>> getAllNodesSet() {
        return Collections.unmodifiableSet(new HashSet<HierarchyNode<E>>(m_nodesByElements.values()));
    }
    public Set<E> getAllElements() {
        return Collections.unmodifiableSet(m_nodesByElements.keySet());
    }
    public int getDepth() {
        HierarchyDepthFinder<E> depthFinder=new HierarchyDepthFinder<E>(m_bottomNode);
        traverseDepthFirst(depthFinder);
        return depthFinder.depth;
    }
    protected final class HierarchyDepthFinder<T> implements Hierarchy.HierarchyNodeVisitor<T> {
        protected final HierarchyNode<T> m_bottomNode;
        protected int depth=0;

        public HierarchyDepthFinder(HierarchyNode<T> bottomNode) {
            m_bottomNode=bottomNode;
        }
        public boolean redirect(HierarchyNode<T>[] nodes) {
            return true;
        }
        public void visit(int level,HierarchyNode<T> node,HierarchyNode<T> parentNode,boolean firstVisit) {
            if (node.equals(m_bottomNode)&&level>depth)
                depth=level;
        }
    }
    public <T> Hierarchy<T> transform(Transformer<? super E,T> transformer,Comparator<T> comparator) {
        HierarchyNodeComparator<T> newNodeComparator=new HierarchyNodeComparator<T>(comparator);
        Map<HierarchyNode<E>,HierarchyNode<T>> oldToNew=new HashMap<HierarchyNode<E>,HierarchyNode<T>>();
        for (HierarchyNode<E> oldNode : m_nodesByElements.values()) {
            Set<T> newEquivalentElements;
            Set<HierarchyNode<T>> newParentNodes;
            Set<HierarchyNode<T>> newChildNodes;
            if (comparator==null) {
                newEquivalentElements=new HashSet<T>();
                newParentNodes=new HashSet<HierarchyNode<T>>();
                newChildNodes=new HashSet<HierarchyNode<T>>();
            }
            else {
                newEquivalentElements=new TreeSet<T>(comparator);
                newParentNodes=new TreeSet<HierarchyNode<T>>(newNodeComparator);
                newChildNodes=new TreeSet<HierarchyNode<T>>(newNodeComparator);
            }
            for (E oldElement : oldNode.m_equivalentElements) {
                T newElement=transformer.transform(oldElement);
                newEquivalentElements.add(newElement);
            }
            T newRepresentative=transformer.determineRepresentative(oldNode.m_representative,newEquivalentElements);
            HierarchyNode<T> newNode=new HierarchyNode<T>(newRepresentative,newEquivalentElements,newParentNodes,newChildNodes);
            oldToNew.put(oldNode,newNode);
        }
        for (HierarchyNode<E> oldParentNode : m_nodesByElements.values()) {
            HierarchyNode<T> newParentNode=oldToNew.get(oldParentNode);
            for (HierarchyNode<E> oldChildNode : oldParentNode.m_childNodes) {
                HierarchyNode<T> newChildNode=oldToNew.get(oldChildNode);
                newParentNode.m_childNodes.add(newChildNode);
                newChildNode.m_parentNodes.add(newParentNode);
            }
        }
        HierarchyNode<T> newTopNode=oldToNew.get(m_topNode);
        HierarchyNode<T> newBottomNode=oldToNew.get(m_bottomNode);
        Hierarchy<T> newHierarchy=new Hierarchy<T>(newTopNode,newBottomNode);
        for (HierarchyNode<T> newNode : oldToNew.values())
            for (T newElement : newNode.m_equivalentElements)
                newHierarchy.m_nodesByElements.put(newElement,newNode);
        return newHierarchy;
    }
    @SuppressWarnings("unchecked")
    public void traverseDepthFirst(HierarchyNodeVisitor<E> visitor) {
        HierarchyNode<E>[] redirectBuffer=new HierarchyNode[2];
        Set<HierarchyNode<E>> visited=new HashSet<HierarchyNode<E>>();
        traverseDepthFirst(visitor,0,m_topNode,null,visited,redirectBuffer);
    }
    protected void traverseDepthFirst(HierarchyNodeVisitor<E> visitor,int level,HierarchyNode<E> node,HierarchyNode<E> parentNode,Set<HierarchyNode<E>> visited,HierarchyNode<E>[] redirectBuffer) {
        redirectBuffer[0]=node;
        redirectBuffer[1]=parentNode;
        if (visitor.redirect(redirectBuffer)) {
            node=redirectBuffer[0];
            parentNode=redirectBuffer[1];
            boolean firstVisit=visited.add(node);
            visitor.visit(level,node,parentNode,firstVisit);
            if (firstVisit)
                for (HierarchyNode<E> childNode : node.m_childNodes)
                    traverseDepthFirst(visitor,level+1,childNode,node,visited,redirectBuffer);
        }
    }
    public String toString() {
        StringWriter buffer=new StringWriter();
        final PrintWriter output=new PrintWriter(buffer);
        traverseDepthFirst(new HierarchyNodeVisitor<E>() {
            public boolean redirect(HierarchyNode<E>[] nodes) {
                return true;
            }
            public void visit(int level,HierarchyNode<E> node,HierarchyNode<E> parentNode,boolean firstVisit) {
                if (!node.equals(m_bottomNode))
                    printNode(level,node,parentNode,firstVisit);
            }
            public void printNode(int level,HierarchyNode<E> node,HierarchyNode<E> parentNode,boolean firstVisit) {
                Set<E> equivalences=node.getEquivalentElements();
                boolean printSubClasOf=(parentNode!=null);
                boolean printEquivalences=firstVisit && equivalences.size()>1;
                if (printSubClasOf || printEquivalences) {
                    for (int i=4*level;i>0;--i)
                        output.print(' ');
                    output.print(node.getRepresentative().toString());
                    if (printEquivalences) {
                        output.print('[');
                        boolean first=true;
                        for (E element : equivalences) {
                            if (!node.getRepresentative().equals(element)) {
                                if (first)
                                    first=false;
                                else
                                    output.print(' ');
                                output.print(element);
                            }
                        }
                        output.print(']');
                    }
                    if (printSubClasOf) {
                        output.print(" -> ");
                        output.print(parentNode.getRepresentative().toString());
                    }
                    output.println();
                }
            }
        });
        output.flush();
        return buffer.toString();
    }
    public static <T> Hierarchy<T> emptyHierarchy(Collection<T> elements,T topElement,T bottomElement) {
        HierarchyNode<T> topBottomNode=new HierarchyNode<T>(topElement);
        topBottomNode.m_equivalentElements.add(topElement);
        topBottomNode.m_equivalentElements.add(bottomElement);
        topBottomNode.m_equivalentElements.addAll(elements);
        return new Hierarchy<T>(topBottomNode,topBottomNode);
    }
    public static <T> Hierarchy<T> trivialHierarchy(T topElement,T bottomElement) {
        HierarchyNode<T> topNode=new HierarchyNode<T>(topElement);
        topNode.m_equivalentElements.add(topElement);
        HierarchyNode<T> bottomNode=new HierarchyNode<T>(bottomElement);
        bottomNode.m_equivalentElements.add(bottomElement);
        topNode.m_childNodes.add(bottomNode);
        bottomNode.m_parentNodes.add(topNode);
        return new Hierarchy<T>(topNode,bottomNode);
    }
    protected static interface HierarchyNodeVisitor<E> {
        boolean redirect(HierarchyNode<E>[] nodes);
        void visit(int level,HierarchyNode<E> node,HierarchyNode<E> parentNode,boolean firstVisit);
    }

    public static interface Transformer<E,T> {
        T transform(E element);
        T determineRepresentative(E oldRepresentative,Set<T> newEquivalentElements);
    }

    protected static class HierarchyNodeComparator<E> implements Comparator<HierarchyNode<E>> {
        protected final Comparator<E> m_elementComparator;

        public HierarchyNodeComparator(Comparator<E> elementComparator) {
            m_elementComparator=elementComparator;
        }
        public int compare(HierarchyNode<E> n1,HierarchyNode<E> n2) {
            return m_elementComparator.compare(n1.m_representative,n2.m_representative);
        }

    }
}
