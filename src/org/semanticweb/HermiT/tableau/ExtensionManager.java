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
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.InternalDatatype;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.monitor.TableauMonitor;
/**ExtensionManager.*/
public final class ExtensionManager implements Serializable {
    private static final long serialVersionUID=5900300914631070591L;

    protected final Tableau m_tableau;
    protected final TableauMonitor m_tableauMonitor;
    protected final DependencySetFactory m_dependencySetFactory;
    protected final Map<Integer,ExtensionTable> m_extensionTablesByArity;
    protected final ExtensionTable[] m_allExtensionTablesArray;
    protected final ExtensionTable m_binaryExtensionTable;
    protected final ExtensionTable m_ternaryExtensionTable;
    protected final Object[] m_binaryAuxiliaryTupleContains;
    protected final Object[] m_binaryAuxiliaryTupleAdd;
    protected final Object[] m_ternaryAuxiliaryTupleContains;
    protected final Object[] m_ternaryAuxiliaryTupleAdd;
    protected final Object[] m_fouraryAuxiliaryTupleContains;
    protected final Object[] m_fouraryAuxiliaryTupleAdd;
    protected PermanentDependencySet m_clashDependencySet;
    protected boolean m_addActive;

    /**
     * @param tableau tableau
     */
    public ExtensionManager(Tableau tableau) {
        m_tableau=tableau;
        m_tableauMonitor=m_tableau.m_tableauMonitor;
        m_dependencySetFactory=m_tableau.m_dependencySetFactory;
        m_extensionTablesByArity=new HashMap<>();
        m_binaryExtensionTable=
            new ExtensionTableWithTupleIndexes(m_tableau,2,!m_tableau.isDeterministic(),
                new TupleIndex[] {
                    new TupleIndex(new int[] { 1,0 }),
                    new TupleIndex(new int[] { 0,1 })
                }
            ) {
                private static final long serialVersionUID=1462821385000191875L;

                @Override
                public boolean isTupleActive(Object[] tuple) {
                    return ((Node)tuple[1]).isActive();
                }
                @Override
                public boolean isTupleActive(int tupleIndex) {
                    return ((Node)m_tupleTable.getTupleObject(tupleIndex,1)).isActive();
                }
            };
        m_extensionTablesByArity.put(new Integer(2),m_binaryExtensionTable);
        m_ternaryExtensionTable=
            new ExtensionTableWithTupleIndexes(m_tableau,3,!m_tableau.isDeterministic(),
                new TupleIndex[] {
                    new TupleIndex(new int[] { 0,1,2 }),
                    new TupleIndex(new int[] { 1,2,0 }),
                    new TupleIndex(new int[] { 2,0,1 })
                }
            ) {
                private static final long serialVersionUID=-731201626401421877L;

                @Override
                public boolean isTupleActive(Object[] tuple) {
                    return ((Node)tuple[1]).isActive() && ((Node)tuple[2]).isActive();
                }
                @Override
                public boolean isTupleActive(int tupleIndex) {
                    return ((Node)m_tupleTable.getTupleObject(tupleIndex,1)).isActive()
                        && ((Node)m_tupleTable.getTupleObject(tupleIndex,2)).isActive();
                }
            };
        m_extensionTablesByArity.put(new Integer(3),m_ternaryExtensionTable);
        for (DescriptionGraph descriptionGraph : m_tableau.m_permanentDLOntology.getAllDescriptionGraphs()) {
            Integer arityInteger=Integer.valueOf(descriptionGraph.getNumberOfVertices()+1);
            if (!m_extensionTablesByArity.containsKey(arityInteger))
                m_extensionTablesByArity.put(arityInteger,new ExtensionTableWithFullIndex(m_tableau,descriptionGraph.getNumberOfVertices()+1,!m_tableau.isDeterministic()));
        }
        m_allExtensionTablesArray=new ExtensionTable[m_extensionTablesByArity.size()];
        m_extensionTablesByArity.values().toArray(m_allExtensionTablesArray);
        m_binaryAuxiliaryTupleContains=new Object[2];
        m_binaryAuxiliaryTupleAdd=new Object[2];
        m_ternaryAuxiliaryTupleContains=new Object[3];
        m_ternaryAuxiliaryTupleAdd=new Object[3];
        m_fouraryAuxiliaryTupleContains=new Object[4];
        m_fouraryAuxiliaryTupleAdd=new Object[4];
    }
    /**
     * Clear.
     */
    public void clear() {
        for (int index=m_allExtensionTablesArray.length-1;index>=0;--index)
            m_allExtensionTablesArray[index].clear();
        m_clashDependencySet=null;
        m_binaryAuxiliaryTupleContains[0]=null;
        m_binaryAuxiliaryTupleContains[1]=null;
        m_binaryAuxiliaryTupleAdd[0]=null;
        m_binaryAuxiliaryTupleAdd[1]=null;
        m_ternaryAuxiliaryTupleContains[0]=null;
        m_ternaryAuxiliaryTupleContains[1]=null;
        m_ternaryAuxiliaryTupleContains[2]=null;
        m_ternaryAuxiliaryTupleAdd[0]=null;
        m_ternaryAuxiliaryTupleAdd[1]=null;
        m_ternaryAuxiliaryTupleAdd[2]=null;
        m_fouraryAuxiliaryTupleContains[0]=null;
        m_fouraryAuxiliaryTupleContains[1]=null;
        m_fouraryAuxiliaryTupleContains[2]=null;
        m_fouraryAuxiliaryTupleContains[3]=null;
        m_fouraryAuxiliaryTupleAdd[0]=null;
        m_fouraryAuxiliaryTupleAdd[1]=null;
        m_fouraryAuxiliaryTupleAdd[2]=null;
        m_fouraryAuxiliaryTupleAdd[3]=null;

    }
    /**
     * Branching point pushed.
     */
    public void branchingPointPushed() {
        for (int index=m_allExtensionTablesArray.length-1;index>=0;--index)
            m_allExtensionTablesArray[index].branchingPointPushed();
    }
    /**
     * Backtrack.
     */
    public void backtrack() {
        for (int index=m_allExtensionTablesArray.length-1;index>=0;--index)
            m_allExtensionTablesArray[index].backtrack();
    }
    /**
     * @return binary extension table
     */
    public ExtensionTable getBinaryExtensionTable() {
        return m_binaryExtensionTable;
    }
    /**
     * @return ternary extension table
     */
    public ExtensionTable getTernaryExtensionTable() {
        return m_ternaryExtensionTable;
    }
    /**
     * @param arity arity
     * @return extension table
     */
    public ExtensionTable getExtensionTable(int arity) {
        switch (arity) {
        case 2:
            return m_binaryExtensionTable;
        case 3:
            return m_ternaryExtensionTable;
        default:
            return m_extensionTablesByArity.get(arity);
        }
    }
    /**
     * @return extension tables
     */
    public Collection<ExtensionTable> getExtensionTables() {
        return m_extensionTablesByArity.values();
    }
    /**
     * @return true if changes are created
     */
    public boolean propagateDeltaNew() {
        boolean hasChange=false;
        for (int index=0;index<m_allExtensionTablesArray.length;index++)
            if (m_allExtensionTablesArray[index].propagateDeltaNew())
                hasChange=true;
        return hasChange;
    }
    /**
     * Clear clash.
     */
    public void clearClash() {
        if (m_clashDependencySet!=null) {
            m_dependencySetFactory.removeUsage(m_clashDependencySet);
            m_clashDependencySet=null;
        }
    }
    /**
     * @param clashDependencySet clashDependencySet
     */
    public void setClash(DependencySet clashDependencySet) {
        if (m_clashDependencySet!=null)
            m_dependencySetFactory.removeUsage(m_clashDependencySet);
        m_clashDependencySet=m_dependencySetFactory.getPermanent(clashDependencySet);
        if (m_clashDependencySet!=null)
            m_dependencySetFactory.addUsage(m_clashDependencySet);
        if (m_tableauMonitor!=null)
            m_tableauMonitor.clashDetected();
    }
    /**
     * @return clash set
     */
    public DependencySet getClashDependencySet() {
        return m_clashDependencySet;
    }
    /**
     * @return true if clash contained
     */
    public boolean containsClash() {
        return m_clashDependencySet!=null;
    }
    /**
     * @param concept concept
     * @param node node
     * @return true if assertion contained
     */
    public boolean containsConceptAssertion(Concept concept,Node node) {
        if (node.getNodeType().isAbstract() && AtomicConcept.THING.equals(concept))
            return true;
        else {
            m_binaryAuxiliaryTupleContains[0]=concept;
            m_binaryAuxiliaryTupleContains[1]=node;
            return m_binaryExtensionTable.containsTuple(m_binaryAuxiliaryTupleContains);
        }
    }
    /**
     * @param range range
     * @param node node
     * @return true if assertion contained
     */
    public boolean containsDataRangeAssertion(DataRange range,Node node) {
        if (!node.getNodeType().isAbstract() && InternalDatatype.RDFS_LITERAL.equals(range))
            return true;
        else {
            m_binaryAuxiliaryTupleContains[0]=range;
            m_binaryAuxiliaryTupleContains[1]=node;
            return m_binaryExtensionTable.containsTuple(m_binaryAuxiliaryTupleContains);
        }
    }
    /**
     * @param role role
     * @param nodeFrom nodeFrom
     * @param nodeTo nodeTo
     * @return true if assertion contained
     */
    public boolean containsRoleAssertion(Role role,Node nodeFrom,Node nodeTo) {
        if (role instanceof AtomicRole) {
            m_ternaryAuxiliaryTupleContains[0]=role;
            m_ternaryAuxiliaryTupleContains[1]=nodeFrom;
            m_ternaryAuxiliaryTupleContains[2]=nodeTo;
        }
        else {
            m_ternaryAuxiliaryTupleContains[0]=((InverseRole)role).getInverseOf();
            m_ternaryAuxiliaryTupleContains[1]=nodeTo;
            m_ternaryAuxiliaryTupleContains[2]=nodeFrom;
        }
        return m_ternaryExtensionTable.containsTuple(m_ternaryAuxiliaryTupleContains);
    }
    /**
     * @param dlPredicate dlPredicate
     * @param node node
     * @return true if assertion contained
     */
    public boolean containsAssertion(DLPredicate dlPredicate,Node node) {
        if (AtomicConcept.THING.equals(dlPredicate))
            return true;
        else {
            m_binaryAuxiliaryTupleContains[0]=dlPredicate;
            m_binaryAuxiliaryTupleContains[1]=node;
            return m_binaryExtensionTable.containsTuple(m_binaryAuxiliaryTupleContains);
        }
    }
    /**
     * @param dlPredicate dlPredicate
     * @param node0 node0
     * @param node1 node1
     * @return true if assertion contained
     */
    public boolean containsAssertion(DLPredicate dlPredicate,Node node0,Node node1) {
        if (Equality.INSTANCE.equals(dlPredicate))
            return node0==node1;
        else {
            m_ternaryAuxiliaryTupleContains[0]=dlPredicate;
            m_ternaryAuxiliaryTupleContains[1]=node0;
            m_ternaryAuxiliaryTupleContains[2]=node1;
            return m_ternaryExtensionTable.containsTuple(m_ternaryAuxiliaryTupleContains);
        }
    }
    /**
     * @param node0 node0
     * @param node1 node1
     * @param node2 node2
     * @return true if equality included
     */
    public static boolean containsAnnotatedEquality(Node node0,Node node1,Node node2) {
        return NominalIntroductionManager.canForgetAnnotation(node0,node1,node2) && node0==node1;
    }
    /**
     * @param tuple tuple
     * @return true if tuple contained
     */
    public boolean containsTuple(Object[] tuple) {
        if (tuple.length==0)
            return containsClash();
        else if (AtomicConcept.THING.equals(tuple[0]))
            return true;
        else if (Equality.INSTANCE.equals(tuple[0]))
            return tuple[1]==tuple[2];
        else if (tuple[0] instanceof AnnotatedEquality)
            return NominalIntroductionManager.canForgetAnnotation((Node)tuple[1],(Node)tuple[2],(Node)tuple[3]) && tuple[1]==tuple[2];
        else
            return getExtensionTable(tuple.length).containsTuple(tuple);
    }
    /**
     * @param concept concept
     * @param node node
     * @return dependency set
     */
    public DependencySet getConceptAssertionDependencySet(Concept concept,Node node) {
        if (AtomicConcept.THING.equals(concept))
            return m_dependencySetFactory.emptySet();
        else {
            m_binaryAuxiliaryTupleContains[0]=concept;
            m_binaryAuxiliaryTupleContains[1]=node;
            return m_binaryExtensionTable.getDependencySet(m_binaryAuxiliaryTupleContains);
        }
    }
    /**
     * @param dlPredicate dlPredicate
     * @param node node
     * @return dependency set
     */
    public DependencySet getAssertionDependencySet(DLPredicate dlPredicate,Node node) {
        m_binaryAuxiliaryTupleContains[0]=dlPredicate;
        m_binaryAuxiliaryTupleContains[1]=node;
        return m_binaryExtensionTable.getDependencySet(m_binaryAuxiliaryTupleContains);
    }
    /**
     * @param dlPredicate dlPredicate
     * @param node0 node0
     * @param node1 node1
     * @return dependency set
     */
    public DependencySet getAssertionDependencySet(DLPredicate dlPredicate,Node node0,Node node1) {
        if (Equality.INSTANCE.equals(dlPredicate))
            return node0==node1 ? m_dependencySetFactory.emptySet() : null;
        else {
            m_ternaryAuxiliaryTupleContains[0]=dlPredicate;
            m_ternaryAuxiliaryTupleContains[1]=node0;
            m_ternaryAuxiliaryTupleContains[2]=node1;
            return m_ternaryExtensionTable.getDependencySet(m_ternaryAuxiliaryTupleContains);
        }
    }
    /**
     * @param concept concept
     * @param node node
     * @param dependencySet dependencySet
     * @param isCore isCore
     * @return true if assertion added
     */
    public boolean addConceptAssertion(Concept concept,Node node,DependencySet dependencySet,boolean isCore) {
        if (m_addActive)
            throw new IllegalStateException("ExtensionManager is not reentrant.");
        m_addActive=true;
        try {
            m_binaryAuxiliaryTupleAdd[0]=concept;
            m_binaryAuxiliaryTupleAdd[1]=node;
            return m_binaryExtensionTable.addTuple(m_binaryAuxiliaryTupleAdd,dependencySet,isCore);
        }
        finally {
            m_addActive=false;
        }
    }
    /**
     * @param dataRange dataRange
     * @param node node
     * @param dependencySet dependencySet
     * @param isCore isCore
     * @return true if assertion added
     */
    public boolean addDataRangeAssertion(DataRange dataRange,Node node,DependencySet dependencySet,boolean isCore) {
        if (m_addActive)
            throw new IllegalStateException("ExtensionManager is not reentrant.");
        m_addActive=true;
        try {
            m_binaryAuxiliaryTupleAdd[0]=dataRange;
            m_binaryAuxiliaryTupleAdd[1]=node;
            return m_binaryExtensionTable.addTuple(m_binaryAuxiliaryTupleAdd,dependencySet,isCore);
        }
        finally {
            m_addActive=false;
        }
    }
    /**
     * @param role role
     * @param nodeFrom nodeFrom
     * @param nodeTo nodeTo
     * @param dependencySet dependencySet
     * @param isCore isCore
     * @return true if role assertion added
     */
    public boolean addRoleAssertion(Role role,Node nodeFrom,Node nodeTo,DependencySet dependencySet,boolean isCore) {
        if (role instanceof AtomicRole)
            return addAssertion((AtomicRole)role,nodeFrom,nodeTo,dependencySet,isCore);
        else
            return addAssertion(((InverseRole)role).getInverseOf(),nodeTo,nodeFrom,dependencySet,isCore);
    }
    /**
     * @param dlPredicate dlPredicate
     * @param node node
     * @param dependencySet dependencySet
     * @param isCore isCore
     * @return true if assertion added
     */
    public boolean addAssertion(DLPredicate dlPredicate,Node node,DependencySet dependencySet,boolean isCore) {
        if (m_addActive)
            throw new IllegalStateException("ExtensionManager is not reentrant.");
        m_addActive=true;
        try {
            m_binaryAuxiliaryTupleAdd[0]=dlPredicate;
            m_binaryAuxiliaryTupleAdd[1]=node;
            return m_binaryExtensionTable.addTuple(m_binaryAuxiliaryTupleAdd,dependencySet,isCore);
        }
        finally {
            m_addActive=false;
        }
    }
    /**
     * @param dlPredicate dlPredicate
     * @param node0 node0
     * @param node1 node1
     * @param dependencySet dependencySet
     * @param isCore isCore
     * @return true if assertion added
     */
    public boolean addAssertion(DLPredicate dlPredicate,Node node0,Node node1,DependencySet dependencySet,boolean isCore) {
        if (Equality.INSTANCE.equals(dlPredicate))
            return m_tableau.m_mergingManager.mergeNodes(node0,node1,dependencySet);
        else {
            if (m_addActive)
                throw new IllegalStateException("ExtensionManager is not reentrant.");
            m_addActive=true;
            try {
                m_ternaryAuxiliaryTupleAdd[0]=dlPredicate;
                m_ternaryAuxiliaryTupleAdd[1]=node0;
                m_ternaryAuxiliaryTupleAdd[2]=node1;
                return m_ternaryExtensionTable.addTuple(m_ternaryAuxiliaryTupleAdd,dependencySet,isCore);
            }
            finally {
                m_addActive=false;
            }
        }
    }
    /**
     * @param dlPredicate dlPredicate
     * @param node0 node0
     * @param node1 node1
     * @param node2 node2
     * @param dependencySet dependencySet
     * @param isCore isCore
     * @return true if assertion added
     */
    public boolean addAssertion(DLPredicate dlPredicate,Node node0,Node node1,Node node2,DependencySet dependencySet,boolean isCore) {
        if (m_addActive)
            throw new IllegalStateException("ExtensionManager is not reentrant.");
        m_fouraryAuxiliaryTupleAdd[0]=dlPredicate;
        m_fouraryAuxiliaryTupleAdd[1]=node0;
        m_fouraryAuxiliaryTupleAdd[2]=node1;
        m_fouraryAuxiliaryTupleAdd[3]=node2;
        return addTuple(m_fouraryAuxiliaryTupleAdd,dependencySet,isCore);
    }
    /**
     * @param annotatedEquality annotatedEquality
     * @param node0 node0
     * @param node1 node1
     * @param node2 node2
     * @param dependencySet dependencySet
     * @return true if annotation added
     */
    public boolean addAnnotatedEquality(AnnotatedEquality annotatedEquality,Node node0,Node node1,Node node2,DependencySet dependencySet) {
        return m_tableau.m_nominalIntroductionManager.addAnnotatedEquality(annotatedEquality,node0,node1,node2,dependencySet);
    }
    /**
     * @param tuple tuple
     * @param dependencySet dependencySet
     * @param isCore isCore
     * @return true if tuple added
     */
    public boolean addTuple(Object[] tuple,DependencySet dependencySet,boolean isCore) {
        if (tuple.length==0) {
            boolean result=(m_clashDependencySet==null);
            setClash(dependencySet);
            return result;
        }
        else if (Equality.INSTANCE.equals(tuple[0]))
            return m_tableau.m_mergingManager.mergeNodes((Node)tuple[1],(Node)tuple[2],dependencySet);
        else if (tuple[0] instanceof AnnotatedEquality)
            return m_tableau.m_nominalIntroductionManager.addAnnotatedEquality((AnnotatedEquality)tuple[0],(Node)tuple[1],(Node)tuple[2],(Node)tuple[3],dependencySet);
        else {
            if (m_addActive)
                throw new IllegalStateException("ExtensionManager is not reentrant.");
            m_addActive=true;
            try {
                return getExtensionTable(tuple.length).addTuple(tuple,dependencySet,isCore);
            }
            finally {
                m_addActive=false;
            }
        }
    }
}
