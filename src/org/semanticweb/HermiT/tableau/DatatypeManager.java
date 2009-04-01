package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.datatypes.DatatypeRegistry;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.NegationDataRange;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.DataValueEnumeration;
import org.semanticweb.HermiT.monitor.TableauMonitor;

public class DatatypeManager implements Serializable {
    private static final long serialVersionUID=-5304869484553471737L;

    protected final TableauMonitor m_tableauMonitor;
    protected final ExtensionManager m_extensionManager;
    protected final ExtensionTable.Retrieval m_assertionsDeltaOldRetrieval;
    protected final ExtensionTable.Retrieval m_inequalityDeltaOldRetrieval;
    protected final ExtensionTable.Retrieval m_inequality01Retrieval;
    protected final ExtensionTable.Retrieval m_inequality02Retrieval;
    protected final ExtensionTable.Retrieval m_assertions1Retrieval;
    protected final DConjunction m_conjunction;
    protected final List<Node> m_auxiliaryNodeList;
    protected final List<DVariable> m_auxiliaryVariableList;
    protected final UnionDependencySet m_unionDependencySet;
    protected final boolean[] m_newVariableAdded;

    public DatatypeManager(Tableau tableau) {
        m_tableauMonitor=tableau.m_tableauMonitor;
        m_extensionManager=tableau.m_extensionManager;
        m_assertionsDeltaOldRetrieval=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,false },ExtensionTable.View.DELTA_OLD);
        m_inequalityDeltaOldRetrieval=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,false },ExtensionTable.View.DELTA_OLD);
        m_inequalityDeltaOldRetrieval.getBindingsBuffer()[0]=Inequality.INSTANCE;
        m_inequality01Retrieval=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.EXTENSION_THIS);
        m_inequality01Retrieval.getBindingsBuffer()[0]=Inequality.INSTANCE;
        m_inequality02Retrieval=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.EXTENSION_THIS);
        m_inequality02Retrieval.getBindingsBuffer()[0]=Inequality.INSTANCE;
        m_assertions1Retrieval=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.EXTENSION_THIS);
        m_conjunction=new DConjunction();
        m_auxiliaryNodeList=new ArrayList<Node>();
        m_auxiliaryVariableList=new ArrayList<DVariable>();
        m_unionDependencySet=new UnionDependencySet(16);
        m_newVariableAdded=new boolean[1];
    }
    public boolean checkDatatypeConstraints() {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.datatypeCheckingStarted();
        m_conjunction.clear();
        Object[] tupleBuffer=m_assertionsDeltaOldRetrieval.getTupleBuffer();
        m_assertionsDeltaOldRetrieval.open();
        while (!m_extensionManager.containsClash() && !m_assertionsDeltaOldRetrieval.afterLast()) {
            if (tupleBuffer[0] instanceof DataRange) {
                // A data range was added in the last saturation step, so we check the D-conjunction hanging off of its node.
                m_conjunction.clearActiveVariables();
                Node node=(Node)tupleBuffer[1];
                DVariable variable=m_conjunction.getVariableFor(node);
                // If variable==null, this means that 'variable' has not been checked in this iteration.
                if (variable==null) {
                    loadNodesReachableByInequality(node);
                    loadDataRanges();
                    checkConjunctionSatisfiability();
                }
            }
            m_assertionsDeltaOldRetrieval.next();
        }
        tupleBuffer=m_inequalityDeltaOldRetrieval.getTupleBuffer();
        m_inequalityDeltaOldRetrieval.open();
        while (!m_extensionManager.containsClash() && !m_inequalityDeltaOldRetrieval.afterLast()) {
            Node node1=(Node)tupleBuffer[1];
            Node node2=(Node)tupleBuffer[2];
            if (node1.getNodeType()==NodeType.CONCRETE_NODE && node2.getNodeType()==NodeType.CONCRETE_NODE) {
                // An inequality between concrete was added in the last saturation step, so we check the D-conjunction hanging off of its node.
                DVariable variable1=m_conjunction.getVariableFor(node1);
                DVariable variable2=m_conjunction.getVariableFor(node2);
                // If variable1==null, this means that 'node1' has not been checked in this iteration,
                // and similarly for variable2==null.
                if (variable1==null && variable2==null) {
                    // It suffices to start the inequality from one of the two nodes:
                    // loadNodesReachableByInequality(node1) will load all reachable nodes and this will include node2.
                    loadNodesReachableByInequality(node1);
                    loadDataRanges();
                    checkConjunctionSatisfiability();
                }
            }
            m_inequalityDeltaOldRetrieval.next();
        }
        if (m_tableauMonitor!=null)
            m_tableauMonitor.datatypeCheckingFinished(!m_extensionManager.containsClash());
        m_unionDependencySet.clearConstituents();
        m_conjunction.clear();
        m_auxiliaryNodeList.clear();
        m_auxiliaryVariableList.clear();
        return true;
    }
    protected void loadNodesReachableByInequality(Node node) {
        m_auxiliaryNodeList.clear();
        m_auxiliaryNodeList.add(node);
        while (!m_auxiliaryNodeList.isEmpty()) {
            Node reachedNode=m_auxiliaryNodeList.remove(m_auxiliaryNodeList.size()-1);
            DVariable reachedVariable=m_conjunction.activateVariable(reachedNode,m_newVariableAdded);
            // Look for all inequalities where reachedNode occurs in the first position.
            m_inequality01Retrieval.getBindingsBuffer()[1]=reachedNode;
            m_inequality01Retrieval.open();
            Object[] tupleBuffer=m_inequality01Retrieval.getTupleBuffer();
            while (!m_inequality01Retrieval.afterLast()) {
                Node newNode=(Node)tupleBuffer[2];
                DVariable newVariable=m_conjunction.activateVariable(newNode,m_newVariableAdded);
                if (m_newVariableAdded[0])
                    m_auxiliaryNodeList.add(newNode);
                m_conjunction.addInequality(reachedVariable,newVariable);
                m_inequality01Retrieval.next();
            }
            // Look for all inequalities where reachedNode occurs in the second position.
            m_inequality02Retrieval.getBindingsBuffer()[2]=reachedNode;
            m_inequality02Retrieval.open();
            tupleBuffer=m_inequality02Retrieval.getTupleBuffer();
            while (!m_inequality02Retrieval.afterLast()) {
                Node newNode=(Node)tupleBuffer[1];
                DVariable newVariable=m_conjunction.activateVariable(newNode,m_newVariableAdded);
                if (m_newVariableAdded[0])
                    m_auxiliaryNodeList.add(newNode);
                m_conjunction.addInequality(newVariable,reachedVariable);
                m_inequality02Retrieval.next();
            }
        }
    }
    protected void loadDataRanges() {
        for (int index=m_conjunction.m_activeVariables.size()-1;index>=0;--index) {
            DVariable variable=m_conjunction.m_activeVariables.get(index);
            m_assertions1Retrieval.getBindingsBuffer()[1]=variable.m_node;
            m_assertions1Retrieval.open();
            Object[] tupleBuffer=m_assertions1Retrieval.getTupleBuffer();
            while (!m_assertions1Retrieval.afterLast()) {
                Object potentialDataRange=tupleBuffer[0];
                if (potentialDataRange instanceof DataRange)
                    addDataRange(variable,(DataRange)potentialDataRange);
                m_assertions1Retrieval.next();
            }
        }
    }
    public void addDataRange(DVariable variable,DataRange dataRange) {
        if (dataRange instanceof DatatypeRestriction) {
            DatatypeRestriction datatypeRestriction=(DatatypeRestriction)dataRange;
            variable.m_positiveDatatypeRestrictions.add(datatypeRestriction);
            if (variable.m_mostSpecificRestriction==null)
                variable.m_mostSpecificRestriction=datatypeRestriction;
            else if (DatatypeRegistry.isDisjointWith(variable.m_mostSpecificRestriction.getDatatypeURI(),datatypeRestriction.getDatatypeURI())) {
                m_unionDependencySet.clearConstituents();
                m_unionDependencySet.addConstituent(m_extensionManager.getAssertionDependencySet(variable.m_mostSpecificRestriction,variable.m_node));
                m_unionDependencySet.addConstituent(m_extensionManager.getAssertionDependencySet(datatypeRestriction,variable.m_node));
                m_extensionManager.setClash(m_unionDependencySet);
                if (m_tableauMonitor!=null) {
                    Object[] tuple1=new Object[] { variable.m_mostSpecificRestriction,variable.m_node };
                    Object[] tuple2=new Object[] { datatypeRestriction,variable.m_node };
                    m_tableauMonitor.clashDetected(tuple1,tuple2);
                }
            }
            else if (DatatypeRegistry.isSubsetOf(datatypeRestriction.getDatatypeURI(),variable.m_mostSpecificRestriction.getDatatypeURI()))
                variable.m_mostSpecificRestriction=datatypeRestriction;
        }
        else if (dataRange instanceof DataValueEnumeration)
            variable.m_positiveDataValueEnumerations.add((DataValueEnumeration)dataRange);
        else if (dataRange instanceof NegationDataRange) {
            DataRange negatedDataRange=((NegationDataRange)dataRange).getNegatedDataRange();
            if (negatedDataRange instanceof DatatypeRestriction)
                variable.m_negativeDatatypeRestrictions.add((DatatypeRestriction)negatedDataRange);
            else if (negatedDataRange instanceof DataValueEnumeration) {
                DataValueEnumeration negatedDataValueEnumeration=(DataValueEnumeration)negatedDataRange;
                variable.m_negativeDataValueEnumerations.add(negatedDataValueEnumeration);
                for (int index=negatedDataValueEnumeration.getNumberOfDataValues()-1;index>=0;--index)
                    variable.addForbiddenDataValue(negatedDataValueEnumeration.getDataValue(index));
            }
            else
                throw new IllegalStateException("Internal error: invalid data range.");
        }
        else
            throw new IllegalStateException("Internal error: invalid data range.");
    }
    protected void checkConjunctionSatisfiability() {
        List<DVariable> activeNodes=m_conjunction.m_activeVariables;
        for (int index=activeNodes.size()-1;!m_extensionManager.containsClash() && index>=0;--index) {
            DVariable variable=activeNodes.get(index);
            if (!variable.m_positiveDataValueEnumerations.isEmpty())
                normalizeAsEnumeration(variable);
            else if (!variable.m_positiveDatatypeRestrictions.isEmpty())
                normalizeAsValueSpaceSubset(variable);
        }
        if (!m_extensionManager.containsClash()) {
            eliminateTrivialInequalities();
            eliminateTriviallySatisfiableNodes();
            enumerateValueSpaceSubsets();
            if (!m_extensionManager.containsClash()) {
                eliminateTriviallySatisfiableNodes();
                checkAssignments();
            }
        }
    }
    protected void normalizeAsEnumeration(DVariable variable) {
        variable.m_hasExplicitDataValues=true;
        List<Object> explicitDataValues=variable.m_explicitDataValues;
        List<DataValueEnumeration> positiveDataValueEnumerations=variable.m_positiveDataValueEnumerations;
        DataValueEnumeration firstDataValueEnumeration=positiveDataValueEnumerations.get(0);
        nextValue: for (int index=firstDataValueEnumeration.getNumberOfDataValues()-1;index>=0;--index) {
            Object dataValue=firstDataValueEnumeration.getDataValue(index);
            if (!variable.m_forbiddenDataValues.contains(dataValue)) {
                for (int enumerationIndex=positiveDataValueEnumerations.size()-1;enumerationIndex>=1;--enumerationIndex)
                    if (!positiveDataValueEnumerations.get(enumerationIndex).containsDataValue(dataValue))
                        continue nextValue;
                explicitDataValues.add(dataValue);
            }
        }
        List<DatatypeRestriction> positiveDatatypeRestrictions=variable.m_positiveDatatypeRestrictions;
        for (int index=positiveDatatypeRestrictions.size()-1;!explicitDataValues.isEmpty() && index>=0;--index) {
            DatatypeRestriction positiveDatatypeRestriction=positiveDatatypeRestrictions.get(index);
            ValueSpaceSubset valueSpaceSubset=DatatypeRegistry.createValueSpaceSubset(positiveDatatypeRestriction);
            eliminateDataValuesUsingValueSpaceSubset(valueSpaceSubset,explicitDataValues,false);
        }
        List<DatatypeRestriction> negativeDatatypeRestrictions=variable.m_negativeDatatypeRestrictions;
        for (int index=negativeDatatypeRestrictions.size()-1;!explicitDataValues.isEmpty() && index>=0;--index) {
            DatatypeRestriction negativeDatatypeRestriction=negativeDatatypeRestrictions.get(index);
            ValueSpaceSubset valueSpaceSubset=DatatypeRegistry.createValueSpaceSubset(negativeDatatypeRestriction);
            eliminateDataValuesUsingValueSpaceSubset(valueSpaceSubset,explicitDataValues,true);
        }
        if (explicitDataValues.isEmpty())
            setClashFor(variable);
    }
    protected void eliminateDataValuesUsingValueSpaceSubset(ValueSpaceSubset valueSpaceSubset,List<Object> explicitDataValues,boolean eliminateWhenValue) {
        for (int valueIndex=explicitDataValues.size()-1;valueIndex>=0;--valueIndex) {
            Object dataValue=explicitDataValues.get(valueIndex);
            if (valueSpaceSubset.containsDataValue(dataValue)==eliminateWhenValue)
                explicitDataValues.remove(valueIndex);
        }
    }
    protected void normalizeAsValueSpaceSubset(DVariable variable) {
        String mostSpecificDatatypeURI=variable.m_mostSpecificRestriction.getDatatypeURI();
        variable.m_valueSpaceSubset=DatatypeRegistry.createValueSpaceSubset(variable.m_mostSpecificRestriction);
        List<DatatypeRestriction> positiveDatatypeRestrictions=variable.m_positiveDatatypeRestrictions;
        for (int index=positiveDatatypeRestrictions.size()-1;index>=0;--index) {
            DatatypeRestriction datatypeRestriction=positiveDatatypeRestrictions.get(index);
            if (datatypeRestriction!=variable.m_mostSpecificRestriction)
                variable.m_valueSpaceSubset=DatatypeRegistry.conjoinWithDR(variable.m_valueSpaceSubset,datatypeRestriction);
        }
        List<DatatypeRestriction> negativeDatatypeRestrictions=variable.m_negativeDatatypeRestrictions;
        for (int index=negativeDatatypeRestrictions.size()-1;index>=0;--index) {
            DatatypeRestriction datatypeRestriction=negativeDatatypeRestrictions.get(index);
            String datatypeRestrictionDatatypeURI=datatypeRestriction.getDatatypeURI();
            if (!DatatypeRegistry.isDisjointWith(mostSpecificDatatypeURI,datatypeRestrictionDatatypeURI) && !DatatypeRegistry.isSubsetOf(mostSpecificDatatypeURI,datatypeRestrictionDatatypeURI))
                variable.m_valueSpaceSubset=DatatypeRegistry.conjoinWithDRNegation(variable.m_valueSpaceSubset,datatypeRestriction);
        }
        if (!variable.m_valueSpaceSubset.hasCardinalityAtLeast(1))
            setClashFor(variable);
    }
    protected void eliminateTrivialInequalities() {
        for (int index1=m_conjunction.m_activeVariables.size()-1;index1>=0;--index1) {
            DVariable variable1=m_conjunction.m_activeVariables.get(index1);
            if (variable1.m_mostSpecificRestriction!=null) {
                String datatypeURI1=variable1.m_mostSpecificRestriction.getDatatypeURI();
                for (int index2=variable1.m_unequalToDirect.size()-1;index2>=0;--index2) {
                    DVariable variable2=variable1.m_unequalToDirect.get(index2);
                    if (variable2.m_mostSpecificRestriction!=null && DatatypeRegistry.isDisjointWith(datatypeURI1,variable2.m_mostSpecificRestriction.getDatatypeURI())) {
                        variable1.m_unequalTo.remove(variable2);
                        variable1.m_unequalToDirect.remove(variable2);
                        variable2.m_unequalTo.remove(variable1);
                        variable2.m_unequalToDirect.remove(variable1);
                    }
                }
            }
        }
    }
    protected void eliminateTriviallySatisfiableNodes() {
        m_auxiliaryVariableList.clear();
        for (int index=m_conjunction.m_activeVariables.size()-1;index>=0;--index)
            m_auxiliaryVariableList.add(m_conjunction.m_activeVariables.get(index));
        while (!m_auxiliaryVariableList.isEmpty()) {
            DVariable variable=m_auxiliaryVariableList.remove(m_auxiliaryVariableList.size()-1);
            if (variable.isObviouslySatisfiable()) {
                for (int index=variable.m_unequalTo.size()-1;index>=0;--index) {
                    DVariable neighborVariable=variable.m_unequalTo.get(index);
                    neighborVariable.m_unequalTo.remove(variable);
                    neighborVariable.m_unequalToDirect.remove(variable);
                    if (!m_auxiliaryVariableList.contains(neighborVariable))
                        m_auxiliaryVariableList.add(neighborVariable);
                }
                variable.m_unequalTo.clear();
                variable.m_unequalToDirect.clear();
                m_conjunction.m_activeVariables.remove(variable);
            }
        }
    }
    protected void enumerateValueSpaceSubsets() {
        for (int index=m_conjunction.m_activeVariables.size()-1;!m_extensionManager.containsClash() && index>=0;--index) {
            DVariable variable=m_conjunction.m_activeVariables.get(index);
            if (variable.m_valueSpaceSubset!=null) {
                variable.m_hasExplicitDataValues=true;
                variable.m_valueSpaceSubset.enumerateDataValues(variable.m_explicitDataValues);
                if (!variable.m_forbiddenDataValues.isEmpty()) {
                    for (int valueIndex=variable.m_explicitDataValues.size()-1;valueIndex>=0;--valueIndex) {
                        Object dataValue=variable.m_explicitDataValues.get(valueIndex);
                        if (variable.m_forbiddenDataValues.contains(dataValue))
                            variable.m_explicitDataValues.remove(valueIndex);
                    }
                }
                variable.m_valueSpaceSubset=null;
                if (variable.m_explicitDataValues.isEmpty())
                    setClashFor(variable);
            }
        }
    }
    protected void checkAssignments() {
        // This method could be further optimized to check each clique of inequalities separately.
        // It is not expected that this is an important optimization, so we don't to it for the moment.
        // The nodes are sorted so that we get a kind of 'join order' optimization.
        Collections.sort(m_conjunction.m_activeVariables,SmallestEnumerationFirst.INSTANCE);
        if (!findAssignment(0))
            setClashFor(m_conjunction.m_activeVariables);
    }
    protected boolean findAssignment(int nodeIndex) {
        if (nodeIndex==m_conjunction.m_activeVariables.size())
            return true;
        else {
            DVariable variable=m_conjunction.m_activeVariables.get(nodeIndex);
            for (int valueIndex=variable.m_explicitDataValues.size()-1;valueIndex>=0;--valueIndex) {
                Object dataValue=variable.m_explicitDataValues.get(valueIndex);
                if (satisfiesNeighbors(variable,dataValue)) {
                    variable.m_dataValue=dataValue;
                    if (findAssignment(nodeIndex+1))
                        return true;
                }
            }
            variable.m_dataValue=null;
            return false;
        }
    }
    protected boolean satisfiesNeighbors(DVariable variable,Object dataValue) {
        for (int neighborIndex=variable.m_unequalTo.size()-1;neighborIndex>=0;--neighborIndex) {
            Object neighborDataValue=variable.m_unequalTo.get(neighborIndex).m_dataValue;
            if (neighborDataValue!=null && neighborDataValue.equals(dataValue))
                return false;
        }
        return true;
    }
    protected void setClashFor(DVariable variable) {
        m_unionDependencySet.clearConstituents();
        loadAssertionDependencySets(variable);
        m_extensionManager.setClash(m_unionDependencySet);
        if (m_tableauMonitor!=null)
            m_tableauMonitor.clashDetected();
    }
    protected void setClashFor(List<DVariable> variables) {
        m_unionDependencySet.clearConstituents();
        for (int nodeIndex=variables.size()-1;nodeIndex>=0;--nodeIndex) {
            DVariable variable=variables.get(nodeIndex);
            loadAssertionDependencySets(variable);
            for (int neighborIndex=variable.m_unequalToDirect.size()-1;neighborIndex>=0;--neighborIndex) {
                DVariable neighborVariable=variable.m_unequalToDirect.get(neighborIndex);
                DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(Inequality.INSTANCE,variable.m_node,neighborVariable.m_node);
                m_unionDependencySet.addConstituent(dependencySet);
            }
        }
        m_extensionManager.setClash(m_unionDependencySet);
        if (m_tableauMonitor!=null)
            m_tableauMonitor.clashDetected();
    }
    protected void loadAssertionDependencySets(DVariable variable) {
        Node node=variable.m_node;
        for (int index=variable.m_positiveDatatypeRestrictions.size()-1;index>=0;--index) {
            DataRange dataRange=variable.m_positiveDatatypeRestrictions.get(index);
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
        for (int index=variable.m_negativeDatatypeRestrictions.size()-1;index>=0;--index) {
            DataRange dataRange=(DataRange)variable.m_negativeDatatypeRestrictions.get(index).getNegation();
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
        for (int index=variable.m_positiveDataValueEnumerations.size()-1;index>=0;--index) {
            DataRange dataRange=variable.m_positiveDataValueEnumerations.get(index);
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
        for (int index=variable.m_negativeDataValueEnumerations.size()-1;index>=0;--index) {
            DataRange dataRange=(DataRange)variable.m_negativeDataValueEnumerations.get(index).getNegation();
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
    }
    
    public static class DConjunction {
        protected final List<DVariable> m_unusedVariables;
        protected final List<DVariable> m_usedVariables;
        protected final List<DVariable> m_activeVariables;
        protected DVariable[] m_buckets;
        protected int m_numberOfEntries;
        protected int m_resizeThreshold;
        
        public DConjunction() {
            m_unusedVariables=new ArrayList<DVariable>();
            m_usedVariables=new ArrayList<DVariable>();
            m_activeVariables=new ArrayList<DVariable>();
            m_buckets=new DVariable[16];
            m_resizeThreshold=(int)(m_buckets.length*0.75);
            m_numberOfEntries=0;
        }
        protected void clear() {
            for (int index=m_usedVariables.size()-1;index>=0;--index) {
                DVariable variable=m_usedVariables.get(index);
                variable.dispose();
                m_unusedVariables.add(variable);
            }
            m_usedVariables.clear();
            m_activeVariables.clear();
            Arrays.fill(m_buckets,null);
            m_numberOfEntries=0;
        }
        protected void clearActiveVariables() {
            m_activeVariables.clear();
        }
        public List<DVariable> getActiveVariables() {
            return Collections.unmodifiableList(m_activeVariables);
        }
        public DVariable getVariableFor(Node node) {
            int index=getIndexFor(node.hashCode(),m_buckets.length);
            DVariable entry=m_buckets[index];
            while (entry!=null) {
                if (entry.m_node==node)
                    return entry;
                entry=entry.m_nextEntry;
            }
            return null;
        }
        protected DVariable activateVariable(Node node,boolean[] newVariableAdded) {
            int index=getIndexFor(node.hashCode(),m_buckets.length);
            DVariable entry=m_buckets[index];
            while (entry!=null) {
                if (entry.m_node==node) {
                    newVariableAdded[0]=false;
                    return entry;
                }
                entry=entry.m_nextEntry;
            }
            DVariable newVariable;
            if (m_unusedVariables.isEmpty())
                newVariable=new DVariable();
            else
                newVariable=m_unusedVariables.remove(m_unusedVariables.size()-1);
            newVariable.m_node=node;
            newVariable.m_nextEntry=m_buckets[index];
            m_buckets[index]=newVariable;
            m_numberOfEntries++;
            if (m_numberOfEntries>=m_resizeThreshold)
                resize(m_buckets.length*2);
            newVariableAdded[0]=true;
            m_usedVariables.add(newVariable);
            m_activeVariables.add(newVariable);
            return newVariable;
        }
        protected void resize(int newCapacity) {
            DVariable[] newBuckets=new DVariable[newCapacity];
            for (int i=0;i<m_buckets.length;i++) {
                DVariable entry=m_buckets[i];
                while (entry!=null) {
                    DVariable nextEntry=entry.m_nextEntry;
                    int newIndex=getIndexFor(entry.m_node.hashCode(),newCapacity);
                    entry.m_nextEntry=newBuckets[newIndex];
                    newBuckets[newIndex]=entry;
                    entry=nextEntry;
                }
            }
            m_buckets=newBuckets;
            m_resizeThreshold=(int)(newCapacity*0.75);
        }
        protected void addInequality(DVariable node1,DVariable node2) {
            if (!node1.m_unequalTo.contains(node2)) {
                node1.m_unequalTo.add(node2);
                node2.m_unequalTo.add(node1);
                node1.m_unequalToDirect.add(node2);
            }
        }
        public String toString() {
            return toString(Prefixes.EMPTY);
        }
        public String toString(Prefixes prefixes) {
            StringBuffer buffer=new StringBuffer();
            boolean first=true;
            for (int variableIndex=0;variableIndex<m_activeVariables.size();variableIndex++) {
                if (first)
                    first=false;
                else
                    buffer.append(" & ");
                DVariable variable=m_activeVariables.get(variableIndex);
                buffer.append(variable.toString(prefixes));
                buffer.append('(');
                buffer.append(')');
                for (int neighborIndex=0;neighborIndex<variable.m_unequalToDirect.size();neighborIndex++) {
                    buffer.append(' ');
                    buffer.append(variableIndex);
                    buffer.append(" != ");
                    buffer.append(m_activeVariables.indexOf(variable.m_unequalToDirect.get(neighborIndex)));
                }
            }
            return buffer.toString();
        }
    }
    
    public static class DVariable {
        protected final List<DataValueEnumeration> m_positiveDataValueEnumerations;
        protected final List<DataValueEnumeration> m_negativeDataValueEnumerations;
        protected final List<DatatypeRestriction> m_positiveDatatypeRestrictions;
        protected final List<DatatypeRestriction> m_negativeDatatypeRestrictions;
        protected final List<DVariable> m_unequalTo;
        protected final List<DVariable> m_unequalToDirect;
        protected final List<Object> m_forbiddenDataValues;
        protected final List<Object> m_explicitDataValues;
        protected boolean m_hasExplicitDataValues;
        protected DatatypeRestriction m_mostSpecificRestriction;
        protected Node m_node;
        protected DVariable m_nextEntry;
        protected ValueSpaceSubset m_valueSpaceSubset;
        protected Object m_dataValue;
        
        protected DVariable() {
            m_positiveDataValueEnumerations=new ArrayList<DataValueEnumeration>();
            m_negativeDataValueEnumerations=new ArrayList<DataValueEnumeration>();
            m_positiveDatatypeRestrictions=new ArrayList<DatatypeRestriction>();
            m_negativeDatatypeRestrictions=new ArrayList<DatatypeRestriction>();
            m_unequalTo=new ArrayList<DVariable>();
            m_unequalToDirect=new ArrayList<DVariable>();
            m_forbiddenDataValues=new ArrayList<Object>();
            m_explicitDataValues=new ArrayList<Object>();
        }
        protected void dispose() {
            m_positiveDataValueEnumerations.clear();
            m_negativeDataValueEnumerations.clear();
            m_positiveDatatypeRestrictions.clear();
            m_negativeDatatypeRestrictions.clear();
            m_unequalTo.clear();
            m_unequalToDirect.clear();
            m_forbiddenDataValues.clear();
            m_explicitDataValues.clear();
            m_hasExplicitDataValues=false;
            m_mostSpecificRestriction=null;
            m_node=null;
            m_nextEntry=null;
            m_valueSpaceSubset=null;
            m_dataValue=null;
        }
        protected void addForbiddenDataValue(Object forbiddenDataValue) {
            if (!m_forbiddenDataValues.contains(forbiddenDataValue))
                m_forbiddenDataValues.add(forbiddenDataValue);
        }
        protected boolean isObviouslySatisfiable() {
            if (m_hasExplicitDataValues)
                return m_explicitDataValues.size()>m_unequalTo.size();
            else if (m_valueSpaceSubset!=null) {
                int neighborCount=m_unequalTo.size()+m_forbiddenDataValues.size();
                return m_valueSpaceSubset.hasCardinalityAtLeast(neighborCount+1);
            }
            else
                return true;
        }
        public List<DataValueEnumeration> getPositiveDataValueEnumerations() {
            return Collections.unmodifiableList(m_positiveDataValueEnumerations);
        }
        public List<DataValueEnumeration> getNegativeDataValueEnumerations() {
            return Collections.unmodifiableList(m_negativeDataValueEnumerations);
        }
        public List<DatatypeRestriction> getPositiveDatatypeRestrictions() {
            return Collections.unmodifiableList(m_positiveDatatypeRestrictions);
        }
        public List<DatatypeRestriction> getNegativeDatatypeRestrictions() {
            return Collections.unmodifiableList(m_negativeDatatypeRestrictions);
        }
        public List<DVariable> getUnequalToDirect() {
            return Collections.unmodifiableList(m_unequalToDirect);
        }
        public String toString() {
            return toString(Prefixes.EMPTY);
        }
        public String toString(Prefixes prefixes) {
            StringBuffer buffer=new StringBuffer();
            boolean first=true;
            buffer.append('[');
            for (int index=0;index<m_positiveDataValueEnumerations.size();index++) {
                if (first)
                    first=false;
                else
                    buffer.append(',');
                buffer.append(m_positiveDataValueEnumerations.get(index).toString(prefixes));
            }
            for (int index=0;index<m_negativeDataValueEnumerations.size();index++) {
                if (first)
                    first=false;
                else
                    buffer.append(',');
                buffer.append(m_negativeDataValueEnumerations.get(index).getNegation().toString(prefixes));
            }
            for (int index=0;index<m_positiveDatatypeRestrictions.size();index++) {
                if (first)
                    first=false;
                else
                    buffer.append(',');
                buffer.append(m_positiveDatatypeRestrictions.get(index).toString(prefixes));
            }
            for (int index=0;index<m_negativeDatatypeRestrictions.size();index++) {
                if (first)
                    first=false;
                else
                    buffer.append(',');
                buffer.append(m_negativeDatatypeRestrictions.get(index).getNegation().toString(prefixes));
            }
            buffer.append(']');
            return buffer.toString();
        }
    }

    protected static int getIndexFor(int hashCode,int tableLength) {
        hashCode+=~(hashCode << 9);
        hashCode^=(hashCode >>> 14);
        hashCode+=(hashCode << 4);
        hashCode^=(hashCode >>> 10);
        return hashCode & (tableLength-1);
    }
    
    protected static class SmallestEnumerationFirst implements Comparator<DVariable> {
        public static final Comparator<DVariable> INSTANCE=new SmallestEnumerationFirst();

        public int compare(DVariable o1,DVariable o2) {
            return o1.m_explicitDataValues.size()-o2.m_explicitDataValues.size();
        }
        
    }
}
