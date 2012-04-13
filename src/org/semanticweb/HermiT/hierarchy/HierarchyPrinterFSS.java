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
package org.semanticweb.HermiT.hierarchy;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Role;

public class HierarchyPrinterFSS {
    protected final PrintWriter m_out;
    protected final String m_defaultPrefixIRI;
    protected final Set<String> m_prefixIRIs;
    protected Prefixes m_prefixes;

    public HierarchyPrinterFSS(PrintWriter out,String defaultPrefixIRI) {
        m_out=out;
        m_defaultPrefixIRI=defaultPrefixIRI;
        m_prefixIRIs=new TreeSet<String>();
        m_prefixIRIs.add(defaultPrefixIRI);
        m_prefixIRIs.add(Prefixes.s_semanticWebPrefixes.get("owl:"));
    }
    public void loadAtomicConceptPrefixIRIs(Collection<AtomicConcept> atomicConcepts) {
        for (AtomicConcept atomicConcept : atomicConcepts) {
            String uri=atomicConcept.getIRI();
            int hashIndex=uri.indexOf('#');
            if (hashIndex!=-1) {
                String prefixIRI=uri.substring(0,hashIndex+1);
                String localName=uri.substring(hashIndex+1);
                if (Prefixes.isValidLocalName(localName))
                    m_prefixIRIs.add(prefixIRI);
            }
        }
    }
    public void loadAtomicRolePrefixIRIs(Collection<AtomicRole> atomicRoles) {
        for (AtomicRole atomicRole : atomicRoles) {
            String uri=atomicRole.getIRI();
            int hashIndex=uri.indexOf('#');
            if (hashIndex!=-1) {
                String prefixIRI=uri.substring(0,hashIndex+1);
                String localName=uri.substring(hashIndex+1);
                if (Prefixes.isValidLocalName(localName))
                    m_prefixIRIs.add(prefixIRI);
            }
        }
    }
    public void startPrinting() {
        String owlPrefixIRI=Prefixes.s_semanticWebPrefixes.get("owl:");
        m_prefixes=new Prefixes();
        m_prefixes.declareDefaultPrefix(m_defaultPrefixIRI);
        m_prefixes.declarePrefix("owl:",owlPrefixIRI);
        int index=1;
        for (String prefixIRI : m_prefixIRIs)
            if (!m_defaultPrefixIRI.equals(prefixIRI) && !owlPrefixIRI.equals(prefixIRI)) {
                String prefixName="a"+(index++)+":";
                m_prefixes.declarePrefix(prefixName,prefixIRI);
            }
        for (Map.Entry<String,String> entry : m_prefixes.getPrefixIRIsByPrefixName().entrySet())
            if (!"owl:".equals(entry.getKey()))
                m_out.println("Prefix("+entry.getKey()+"=<"+entry.getValue()+">)");
        m_out.println();
        m_out.println("Ontology(<"+m_prefixes.getPrefixIRIsByPrefixName().get(":")+">");
        m_out.println();
    }
    public void printAtomicConceptHierarchy(Hierarchy<AtomicConcept> atomicConceptHierarchy) {
        Hierarchy<AtomicConcept> sortedAtomicConceptHierarchy=atomicConceptHierarchy.transform(new IdentityTransformer<AtomicConcept>(),AtomicConceptComparator.INSTANCE);
        AtomicConceptPrinter atomicConceptPrinter=new AtomicConceptPrinter(sortedAtomicConceptHierarchy.getBottomNode());
        sortedAtomicConceptHierarchy.traverseDepthFirst(atomicConceptPrinter);
        atomicConceptPrinter.printNode(0,sortedAtomicConceptHierarchy.getBottomNode(),null,true);
    }
    public void printRoleHierarchy(Hierarchy<? extends Role> roleHierarchy,boolean objectProperties) {
        Hierarchy<Role> sortedRoleHierarchy=roleHierarchy.transform(new IdentityTransformer<Role>(),RoleComparator.INSTANCE);
        RolePrinter rolePrinter=new RolePrinter(sortedRoleHierarchy,objectProperties);
        sortedRoleHierarchy.traverseDepthFirst(rolePrinter);
        rolePrinter.printNode(0,sortedRoleHierarchy.getBottomNode(),null,true);
    }
    public void endPrinting() {
        m_out.println();
        m_out.println(")");
        m_out.flush();
    }

    protected class AtomicConceptPrinter implements Hierarchy.HierarchyNodeVisitor<AtomicConcept> {
        protected final HierarchyNode<AtomicConcept> m_bottomNode;

        public AtomicConceptPrinter(HierarchyNode<AtomicConcept> bottomNode) {
            m_bottomNode=bottomNode;
        }
        public boolean redirect(HierarchyNode<AtomicConcept>[] nodes) {
            return true;
        }
        public void visit(int level,HierarchyNode<AtomicConcept> node,HierarchyNode<AtomicConcept> parentNode,boolean firstVisit) {
            if (!node.equals(m_bottomNode))
                printNode(level,node,parentNode,firstVisit);
        }
        public void printNode(int level,HierarchyNode<AtomicConcept> node,HierarchyNode<AtomicConcept> parentNode,boolean firstVisit) {
            Set<AtomicConcept> equivalences=node.getEquivalentElements();
            boolean printSubClasOf=(parentNode!=null);
            boolean printEquivalences=firstVisit && equivalences.size()>1;
            boolean printDeclarations=false;
            if (firstVisit)
                for (AtomicConcept atomicConcept : equivalences)
                    if (needsDeclaration(atomicConcept)) {
                        printDeclarations=true;
                        break;
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
                if (printDeclarations)
                    for (AtomicConcept atomicConcept : equivalences)
                        if (needsDeclaration(atomicConcept)) {
                            if (!afterWS)
                                m_out.print(' ');
                            m_out.print("Declaration( Class( ");
                            print(atomicConcept);
                            m_out.print(" ) )");
                            afterWS=false;
                        }
                m_out.println();
            }
        }
        protected void print(AtomicConcept atomicConcept) {
            m_out.print(m_prefixes.abbreviateIRI(atomicConcept.getIRI()));
        }
        protected boolean needsDeclaration(AtomicConcept atomicConcept) {
            return !AtomicConcept.NOTHING.equals(atomicConcept) && !AtomicConcept.THING.equals(atomicConcept);
        }
    }

    protected class RolePrinter implements Hierarchy.HierarchyNodeVisitor<Role> {
        protected final Hierarchy<Role> m_hierarchy;
        protected final boolean m_objectProperties;

        public RolePrinter(Hierarchy<Role> hierarchy,boolean objectProperties) {
            m_hierarchy=hierarchy;
            m_objectProperties=objectProperties;
        }
        public boolean redirect(HierarchyNode<Role>[] nodes) {
            return true;
        }
        public void visit(int level,HierarchyNode<Role> node,HierarchyNode<Role> parentNode,boolean firstVisit) {
            if (!node.equals(m_hierarchy.getBottomNode()))
                printNode(level,node,parentNode,firstVisit);
        }
        public void printNode(int level,HierarchyNode<Role> node,HierarchyNode<Role> parentNode,boolean firstVisit) {
            Set<Role> equivalences=node.getEquivalentElements();
            boolean printSubPropertyOf=(parentNode!=null);
            boolean printEquivalences=firstVisit && equivalences.size()>1;
            boolean printDeclarations=false;
            if (firstVisit)
                for (Role role : equivalences)
                    if (needsDeclaration(role)) {
                        printDeclarations=true;
                        break;
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
                    print(node.getRepresentative());
                    m_out.print(' ');
                    print(parentNode.getRepresentative());
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
                    for (Role role : equivalences) {
                        m_out.print(' ');
                        print(role);
                    }
                    m_out.print(" )");
                    afterWS=false;
                }
                if (printDeclarations)
                    for (Role role : equivalences)
                        if (needsDeclaration(role)) {
                            if (!afterWS)
                                m_out.print(' ');
                            m_out.print("Declaration( ");
                            if (m_objectProperties)
                                m_out.print("ObjectProperty( ");
                            else
                                m_out.print("DataProperty( ");
                            print(role);
                            m_out.print(" ) )");
                            afterWS=false;
                        }
                m_out.println();
            }
        }
        protected void print(Role role) {
            if (role instanceof AtomicRole)
                m_out.print(m_prefixes.abbreviateIRI(((AtomicRole)role).getIRI()));
            else {
                m_out.print("ObjectInverseOf( ");
                print(((InverseRole)role).getInverseOf());
                m_out.print(" )");
            }
        }
        protected void print(AtomicRole atomicRole) {
            m_out.print(m_prefixes.abbreviateIRI(atomicRole.getIRI()));
        }
        protected boolean needsDeclaration(Role role) {
            return !AtomicRole.BOTTOM_OBJECT_ROLE.equals(role) && !AtomicRole.TOP_OBJECT_ROLE.equals(role) && !AtomicRole.BOTTOM_DATA_ROLE.equals(role) && !AtomicRole.TOP_DATA_ROLE.equals(role) && role instanceof AtomicRole;
        }
    }

    protected static class RoleComparator implements Comparator<Role> {
        public static final RoleComparator INSTANCE=new RoleComparator();

        public int compare(Role role1,Role role2) {
            int comparison=getRoleClass(role1)-getRoleClass(role2);
            if (comparison!=0)
                return comparison;
            comparison=getRoleDirection(role1)-getRoleDirection(role2);
            if (comparison!=0)
                return comparison;
            return getInnerAtomicRole(role1).getIRI().compareTo(getInnerAtomicRole(role2).getIRI());
        }
        protected int getRoleClass(Role role) {
            if (AtomicRole.BOTTOM_OBJECT_ROLE.equals(role))
                return 0;
            else if (AtomicRole.TOP_OBJECT_ROLE.equals(role))
                return 1;
            else if (AtomicRole.BOTTOM_DATA_ROLE.equals(role))
                return 2;
            else if (AtomicRole.TOP_DATA_ROLE.equals(role))
                return 3;
            else
                return 4;
        }
        protected AtomicRole getInnerAtomicRole(Role role) {
            if (role instanceof AtomicRole)
                return (AtomicRole)role;
            else
                return ((InverseRole)role).getInverseOf();
        }
        protected int getRoleDirection(Role role) {
            return role instanceof AtomicRole ? 0 : 1;
        }
    }

    protected static class AtomicConceptComparator implements Comparator<AtomicConcept> {
        public static final AtomicConceptComparator INSTANCE=new AtomicConceptComparator();

        public int compare(AtomicConcept atomicConcept1,AtomicConcept atomicConcept2) {
            int comparison=getAtomicConceptClass(atomicConcept1)-getAtomicConceptClass(atomicConcept2);
            if (comparison!=0)
                return comparison;
            return atomicConcept1.getIRI().compareTo(atomicConcept2.getIRI());
        }
        protected int getAtomicConceptClass(AtomicConcept atomicConcept) {
            if (AtomicConcept.NOTHING.equals(atomicConcept))
                return 0;
            else if (AtomicConcept.THING.equals(atomicConcept))
                return 1;
            else
                return 2;
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
}
