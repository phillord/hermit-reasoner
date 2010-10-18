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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeRegistry;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.AtomicDataRange;
import org.semanticweb.HermiT.model.AtomicNegationDataRange;
import org.semanticweb.HermiT.model.ConstantEnumeration;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InternalDatatype;
import org.semanticweb.HermiT.model.LiteralDataRange;
import org.semanticweb.HermiT.monitor.TableauMonitor;

public final class DatatypeManager implements Serializable {
    private static final long serialVersionUID=-5304869484553471737L;

    protected final InterruptFlag m_interruptFlag;
    protected final TableauMonitor m_tableauMonitor;
    protected final ExtensionManager m_extensionManager;
    protected final ExtensionTable.Retrieval m_assertionsDeltaOldRetrieval;
    protected final ExtensionTable.Retrieval m_inequalityDeltaOldRetrieval;
    protected final ExtensionTable.Retrieval m_inequality01Retrieval;
    protected final ExtensionTable.Retrieval m_inequality02Retrieval;
    protected final ExtensionTable.Retrieval m_assertions0Retrieval;
    protected final ExtensionTable.Retrieval m_assertions1Retrieval;
    protected final DConjunction m_conjunction;
    protected final List<DVariable> m_auxiliaryVariableList;
    protected final UnionDependencySet m_unionDependencySet;
    protected final boolean[] m_newVariableAdded;
    protected final Set<DatatypeRestriction> m_unknownDatatypeRestrictionsPermanent;
    protected Set<DatatypeRestriction> m_unknownDatatypeRestrictionsAdditional;

    public DatatypeManager(Tableau tableau) {
        m_interruptFlag=tableau.m_interruptFlag;
        m_tableauMonitor=tableau.m_tableauMonitor;
        m_extensionManager=tableau.m_extensionManager;
        m_assertionsDeltaOldRetrieval=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,false },ExtensionTable.View.DELTA_OLD);
        // The following retrieval should actually be created such that it has Inequality.INSTANCE as the first binding.
        // We don't do this because of the implementation of the ExtensionTable. Namely, each ExtensionTable keeps
        // only one index for all tuples, and then filters out the assertions that don't fit into the index. As a
        // side effect of such an implementation, this means that each time we go through the entire index for inequalities,
        // which can take up a lot of time. The current solution is more efficient: it goes through the actual delta-old
        // and then filters out inequalities "manually".
        m_inequalityDeltaOldRetrieval=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { false,false,false },ExtensionTable.View.DELTA_OLD);
        m_inequality01Retrieval=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.EXTENSION_THIS);
        m_inequality02Retrieval=m_extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.EXTENSION_THIS);
        m_assertions0Retrieval=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { true,false },ExtensionTable.View.EXTENSION_THIS);
        m_assertions1Retrieval=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.EXTENSION_THIS);
        m_conjunction=new DConjunction();
        m_auxiliaryVariableList=new ArrayList<DVariable>();
        m_unionDependencySet=new UnionDependencySet(16);
        m_newVariableAdded=new boolean[1];
        m_unknownDatatypeRestrictionsPermanent=tableau.m_permanentDLOntology.getAllUnknownDatatypeRestrictions();
        if (tableau.m_additionalDLOntology!=null)
            additionalDLOntologySet(tableau.m_additionalDLOntology);
    }
    public void additionalDLOntologySet(DLOntology additionalDLOntology) {
        m_unknownDatatypeRestrictionsAdditional=additionalDLOntology.getAllUnknownDatatypeRestrictions();
    }
    public void additionalDLOntologyCleared() {
        m_unknownDatatypeRestrictionsAdditional=null;
    }
    public void clear() {
        m_assertionsDeltaOldRetrieval.clear();
        m_inequalityDeltaOldRetrieval.clear();
        m_inequality01Retrieval.clear();
        m_inequality02Retrieval.clear();
        m_assertions0Retrieval.clear();
        m_assertions1Retrieval.clear();
        m_conjunction.clear();
        m_auxiliaryVariableList.clear();
        m_unionDependencySet.clearConstituents();
    }
    public void applyUnknownDatatypeRestrictionSemantics() {
        Object[] tupleBuffer=m_assertionsDeltaOldRetrieval.getTupleBuffer();
        m_assertionsDeltaOldRetrieval.open();
        while (!m_extensionManager.containsClash() && !m_assertionsDeltaOldRetrieval.afterLast()) {
            Object dataRangeObject=tupleBuffer[0];
            if (dataRangeObject instanceof DatatypeRestriction) {
                DatatypeRestriction datatypeRestriction=(DatatypeRestriction)dataRangeObject;
                if (m_unknownDatatypeRestrictionsPermanent.contains(datatypeRestriction) || (m_unknownDatatypeRestrictionsAdditional!=null && m_unknownDatatypeRestrictionsAdditional.contains(datatypeRestriction)))
                    generateInequalitiesFor(datatypeRestriction,(Node)tupleBuffer[1],m_assertionsDeltaOldRetrieval.getDependencySet(),AtomicNegationDataRange.create(datatypeRestriction));
            }
            else if (dataRangeObject instanceof AtomicNegationDataRange) {
                AtomicNegationDataRange negationDataRange=(AtomicNegationDataRange)dataRangeObject;
                DataRange negatedDataRange=negationDataRange.getNegatedDataRange();
                if (negatedDataRange instanceof DatatypeRestriction) {
                    DatatypeRestriction datatypeRestriction=(DatatypeRestriction)negatedDataRange;
                    if (m_unknownDatatypeRestrictionsPermanent.contains(datatypeRestriction) || (m_unknownDatatypeRestrictionsAdditional!=null && m_unknownDatatypeRestrictionsAdditional.contains(datatypeRestriction)))
                        generateInequalitiesFor(negationDataRange,(Node)tupleBuffer[1],m_assertionsDeltaOldRetrieval.getDependencySet(),datatypeRestriction);
                }
            }
            m_assertionsDeltaOldRetrieval.next();
        }
    }
    protected void generateInequalitiesFor(DataRange dataRange1,Node node1,DependencySet dependencySet1,DataRange dataRange2) {
        m_unionDependencySet.clearConstituents();
        m_unionDependencySet.addConstituent(dependencySet1);
        m_unionDependencySet.addConstituent(null);
        m_assertions0Retrieval.getBindingsBuffer()[0]=dataRange2;
        Object[] tupleBuffer=m_assertions0Retrieval.getTupleBuffer();
        m_assertions0Retrieval.open();
        while (!m_assertions0Retrieval.afterLast()) {
            Node node2=(Node)tupleBuffer[1];
            m_unionDependencySet.m_dependencySets[1]=m_assertions0Retrieval.getDependencySet();
            if (m_tableauMonitor!=null)
                m_tableauMonitor.unknownDatatypeRestrictionDetectionStarted(dataRange1,node1,dataRange2,node2);
            m_extensionManager.addAssertion(Inequality.INSTANCE,node1,node2,m_unionDependencySet,false);
            if (m_tableauMonitor!=null)
                m_tableauMonitor.unknownDatatypeRestrictionDetectionFinished(dataRange1,node1,dataRange2,node2);
            m_assertions0Retrieval.next();
        }
    }
    public void checkDatatypeConstraints() {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.datatypeCheckingStarted();
        m_conjunction.clear();
        Object[] tupleBuffer=m_assertionsDeltaOldRetrieval.getTupleBuffer();
        m_assertionsDeltaOldRetrieval.open();
        while (!m_extensionManager.containsClash() && !m_assertionsDeltaOldRetrieval.afterLast()) {
            if (tupleBuffer[0] instanceof DataRange) {
                // A data range was added in the last saturation step, so we check the D-conjunction hanging off of its node.
                Node node=(Node)tupleBuffer[1];
                DVariable variable=getAndInitializeVariableFor(node,m_newVariableAdded);
                // m_newVariableAdded[0]==false means that 'variable' has already been checked in this iteration.
                if (m_newVariableAdded[0]) {
                    m_conjunction.clearActiveVariables();
                    loadConjunctionFrom(variable);
                    checkConjunctionSatisfiability();
                }
            }
            m_assertionsDeltaOldRetrieval.next();
        }
        tupleBuffer=m_inequalityDeltaOldRetrieval.getTupleBuffer();
        m_inequalityDeltaOldRetrieval.open();
        while (!m_extensionManager.containsClash() && !m_inequalityDeltaOldRetrieval.afterLast()) {
            // This is a part of the hack described in the constructor: m_inequalityDeltaOldRetrieval
            // iterates through the entire extension (for efficiency) and then we need to filter out
            // inequalities ourselves.
            if (Inequality.INSTANCE.equals(tupleBuffer[0])) {
                Node node1=(Node)tupleBuffer[1];
                Node node2=(Node)tupleBuffer[2];
                if (!node1.getNodeType().isAbstract() && !node2.getNodeType().isAbstract()) {
                    m_conjunction.clearActiveVariables();
                    // An inequality between concrete was added in the last saturation step, so we check the D-conjunction hanging off of its node.
                    DVariable variable1=getAndInitializeVariableFor(node1,m_newVariableAdded);
                    // m_newVariableAdded[0]==false means that 'variable1' has already been checked in this iteration.
                    if (m_newVariableAdded[0])
                        loadConjunctionFrom(variable1);
                    DVariable variable2=getAndInitializeVariableFor(node2,m_newVariableAdded);
                    // m_newVariableAdded[0]==false means that 'variable1' has already been checked in this iteration.
                    if (m_newVariableAdded[0])
                        loadConjunctionFrom(variable2);
                    m_conjunction.addInequality(variable1,variable2);
                    checkConjunctionSatisfiability();
                }
            }
            m_inequalityDeltaOldRetrieval.next();
        }
        if (m_tableauMonitor!=null)
            m_tableauMonitor.datatypeCheckingFinished(!m_extensionManager.containsClash());
        m_unionDependencySet.clearConstituents();
        m_conjunction.clear();
        m_auxiliaryVariableList.clear();
    }
    protected void loadConjunctionFrom(DVariable startVariable) {
        m_auxiliaryVariableList.clear();
        m_auxiliaryVariableList.add(startVariable);
        while (!m_extensionManager.containsClash() && !m_auxiliaryVariableList.isEmpty()) {
            DVariable reachedVariable=m_auxiliaryVariableList.remove(m_auxiliaryVariableList.size()-1);
            if (!m_conjunction.m_activeVariables.contains(reachedVariable)) {
                m_conjunction.m_activeVariables.add(reachedVariable);
                // Concrete root nodes are assigned a particular value, so they act as "breakers" in the conjunction:
                // the nodes that are unequal to them can be analyzed independently.
                if (reachedVariable.m_node.getNodeType()!=NodeType.ROOT_CONSTANT_NODE) {
                    // Look for all inequalities where reachedNode occurs in the first position.
                    m_inequality01Retrieval.getBindingsBuffer()[0]=Inequality.INSTANCE;
                    m_inequality01Retrieval.getBindingsBuffer()[1]=reachedVariable.m_node;
                    m_inequality01Retrieval.open();
                    Object[] tupleBuffer=m_inequality01Retrieval.getTupleBuffer();
                    while (!m_extensionManager.containsClash() && !m_inequality01Retrieval.afterLast()) {
                        Node newNode=(Node)tupleBuffer[2];
                        DVariable newVariable=getAndInitializeVariableFor(newNode,m_newVariableAdded);
                        m_auxiliaryVariableList.add(newVariable);
                        m_conjunction.addInequality(reachedVariable,newVariable);
                        m_inequality01Retrieval.next();
                        m_interruptFlag.checkInterrupt();
                    }
                    // Look for all inequalities where reachedNode occurs in the second position.
                    m_inequality02Retrieval.getBindingsBuffer()[0]=Inequality.INSTANCE;
                    m_inequality02Retrieval.getBindingsBuffer()[2]=reachedVariable.m_node;
                    m_inequality02Retrieval.open();
                    tupleBuffer=m_inequality02Retrieval.getTupleBuffer();
                    while (!m_extensionManager.containsClash() && !m_inequality02Retrieval.afterLast()) {
                        Node newNode=(Node)tupleBuffer[1];
                        DVariable newVariable=getAndInitializeVariableFor(newNode,m_newVariableAdded);
                        m_auxiliaryVariableList.add(newVariable);
                        m_conjunction.addInequality(newVariable,reachedVariable);
                        m_inequality02Retrieval.next();
                        m_interruptFlag.checkInterrupt();
                    }
                }
            }
        }
    }
    protected DVariable getAndInitializeVariableFor(Node node,boolean[] newVariableAdded) {
        DVariable variable=m_conjunction.getVariableForEx(node,newVariableAdded);
        if (m_newVariableAdded[0]) {
            m_assertions1Retrieval.getBindingsBuffer()[1]=variable.m_node;
            m_assertions1Retrieval.open();
            Object[] tupleBuffer=m_assertions1Retrieval.getTupleBuffer();
            while (!m_extensionManager.containsClash() && !m_assertions1Retrieval.afterLast()) {
                Object potentialDataRange=tupleBuffer[0];
                if (potentialDataRange instanceof DataRange)
                    addDataRange(variable,(DataRange)potentialDataRange);
                m_assertions1Retrieval.next();
                m_interruptFlag.checkInterrupt();
            }
            if (!m_extensionManager.containsClash())
                normalize(variable);
        }
        return variable;
    }
    protected void addDataRange(DVariable variable,DataRange dataRange) {
        if (dataRange instanceof InternalDatatype) {
            // Internal datatypes are skipped, as they do not contribute to datatype checking.
            // These are used to encode rdfs:Literal and datatype definitions, and to rename complex data ranges.
        }
        else if (dataRange instanceof DatatypeRestriction) {
            DatatypeRestriction datatypeRestriction=(DatatypeRestriction)dataRange;
            if (!m_unknownDatatypeRestrictionsPermanent.contains(datatypeRestriction) && (m_unknownDatatypeRestrictionsAdditional==null || !m_unknownDatatypeRestrictionsAdditional.contains(datatypeRestriction))) {
                variable.m_positiveDatatypeRestrictions.add(datatypeRestriction);
                if (variable.m_mostSpecificRestriction==null)
                    variable.m_mostSpecificRestriction=datatypeRestriction;
                else if (DatatypeRegistry.isDisjointWith(variable.m_mostSpecificRestriction.getDatatypeURI(),datatypeRestriction.getDatatypeURI())) {
                    m_unionDependencySet.clearConstituents();
                    m_unionDependencySet.addConstituent(m_extensionManager.getAssertionDependencySet(variable.m_mostSpecificRestriction,variable.m_node));
                    m_unionDependencySet.addConstituent(m_extensionManager.getAssertionDependencySet(datatypeRestriction,variable.m_node));
                    Object[] tuple1;
                    Object[] tuple2;
                    if (m_tableauMonitor!=null) {
                        tuple1=new Object[] { variable.m_mostSpecificRestriction,variable.m_node };
                        tuple2=new Object[] { datatypeRestriction,variable.m_node };
                        m_tableauMonitor.clashDetectionStarted(tuple1,tuple2);
                    }
                    m_extensionManager.setClash(m_unionDependencySet);
                    if (m_tableauMonitor!=null) {
                        tuple1=new Object[] { variable.m_mostSpecificRestriction,variable.m_node };
                        tuple2=new Object[] { datatypeRestriction,variable.m_node };
                        m_tableauMonitor.clashDetectionFinished(tuple1,tuple2);
                    }
                }
                else if (DatatypeRegistry.isSubsetOf(datatypeRestriction.getDatatypeURI(),variable.m_mostSpecificRestriction.getDatatypeURI()))
                    variable.m_mostSpecificRestriction=datatypeRestriction;
            }
        }
        else if (dataRange instanceof ConstantEnumeration)
            variable.m_positiveConstantEnumerations.add((ConstantEnumeration)dataRange);
        else if (dataRange instanceof AtomicNegationDataRange) {
            DataRange negatedDataRange=((AtomicNegationDataRange)dataRange).getNegatedDataRange();
            if (negatedDataRange instanceof InternalDatatype) {
                // Skip for the same reasons as above.
            }
            else if (negatedDataRange instanceof DatatypeRestriction) {
                DatatypeRestriction datatypeRestriction=(DatatypeRestriction)negatedDataRange;
                if (!m_unknownDatatypeRestrictionsPermanent.contains(datatypeRestriction) && (m_unknownDatatypeRestrictionsAdditional==null || !m_unknownDatatypeRestrictionsAdditional.contains(datatypeRestriction)))
                    variable.m_negativeDatatypeRestrictions.add(datatypeRestriction);
            }
            else if (negatedDataRange instanceof ConstantEnumeration) {
                ConstantEnumeration negatedConstantEnumeration=(ConstantEnumeration)negatedDataRange;
                variable.m_negativeConstantEnumerations.add(negatedConstantEnumeration);
                for (int index=negatedConstantEnumeration.getNumberOfConstants()-1;index>=0;--index)
                    variable.addForbiddenDataValue(negatedConstantEnumeration.getConstant(index).getDataValue());
            }
            else
                throw new IllegalStateException("Internal error: invalid data range.");
        }
        else
            throw new IllegalStateException("Internal error: invalid data range.");
    }
    protected void checkConjunctionSatisfiability() {
        if (!m_extensionManager.containsClash() && !m_conjunction.m_activeVariables.isEmpty()) {
            if (m_tableauMonitor!=null)
                m_tableauMonitor.datatypeConjunctionCheckingStarted(m_conjunction);
            if (m_conjunction.isSymmetricClique()) {
                DVariable representative=m_conjunction.m_activeVariables.get(0);
                if (!m_extensionManager.containsClash() && !representative.hasCardinalityAtLeast(m_conjunction.m_activeVariables.size()))
                    setClashFor(m_conjunction.m_activeVariables);
            }
            else if (!m_extensionManager.containsClash()) {
                eliminateTrivialInequalities();
                eliminateTriviallySatisfiableNodes();
                enumerateValueSpaceSubsets();
                if (!m_extensionManager.containsClash()) {
                    eliminateTriviallySatisfiableNodes();
                    checkAssignments();
                }
            }
            if (m_tableauMonitor!=null)
                m_tableauMonitor.datatypeConjunctionCheckingFinished(m_conjunction,!m_extensionManager.containsClash());
        }
    }
    protected void normalize(DVariable variable) {
        if (!variable.m_positiveConstantEnumerations.isEmpty())
            normalizeAsEnumeration(variable);
        else if (!variable.m_positiveDatatypeRestrictions.isEmpty())
            normalizeAsValueSpaceSubset(variable);
    }
    protected void normalizeAsEnumeration(DVariable variable) {
        variable.m_hasExplicitDataValues=true;
        List<Object> explicitDataValues=variable.m_explicitDataValues;
        List<ConstantEnumeration> positiveConstantEnumerations=variable.m_positiveConstantEnumerations;
        ConstantEnumeration firstDataValueEnumeration=positiveConstantEnumerations.get(0);
        nextValue: for (int index=firstDataValueEnumeration.getNumberOfConstants()-1;index>=0;--index) {
            Object dataValue=firstDataValueEnumeration.getConstant(index).getDataValue();
            if (!explicitDataValues.contains(dataValue) && !variable.m_forbiddenDataValues.contains(dataValue)) {
                for (int enumerationIndex=positiveConstantEnumerations.size()-1;enumerationIndex>=1;--enumerationIndex)
                    if (!containsDataValue(positiveConstantEnumerations.get(enumerationIndex),dataValue))
                        continue nextValue;
                explicitDataValues.add(dataValue);
            }
        }
        variable.m_forbiddenDataValues.clear();
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
    protected boolean containsDataValue(ConstantEnumeration constantEnumeration,Object dataValue) {
        for (int index=constantEnumeration.getNumberOfConstants()-1;index>=0;--index)
            if (constantEnumeration.getConstant(index).getDataValue().equals(dataValue))
                return true;
        return false;
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
            if (!DatatypeRegistry.isDisjointWith(mostSpecificDatatypeURI,datatypeRestrictionDatatypeURI))
                variable.m_valueSpaceSubset=DatatypeRegistry.conjoinWithDRNegation(variable.m_valueSpaceSubset,datatypeRestriction);
        }
        if (!variable.m_valueSpaceSubset.hasCardinalityAtLeast(1)) {
            variable.m_forbiddenDataValues.clear();
            setClashFor(variable);
        }
        else {
            for (int valueIndex=variable.m_forbiddenDataValues.size()-1;valueIndex>=0;--valueIndex) {
                Object forbiddenValue=variable.m_forbiddenDataValues.get(valueIndex);
                if (!variable.m_valueSpaceSubset.containsDataValue(forbiddenValue))
                    variable.m_forbiddenDataValues.remove(valueIndex);
            }
        }
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
            if (variable.hasCardinalityAtLeast(variable.m_unequalTo.size()+1)) {
                for (int index=variable.m_unequalTo.size()-1;index>=0;--index) {
                    DVariable neighborVariable=variable.m_unequalTo.get(index);
                    neighborVariable.m_unequalTo.remove(variable);
                    neighborVariable.m_unequalToDirect.remove(variable);
                    if (!m_auxiliaryVariableList.contains(neighborVariable))
                        m_auxiliaryVariableList.add(neighborVariable);
                }
                variable.clearEqualities();
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
                variable.m_forbiddenDataValues.clear();
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
                m_interruptFlag.checkInterrupt();
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
    }
    protected void loadAssertionDependencySets(DVariable variable) {
        Node node=variable.m_node;
        for (int index=variable.m_positiveDatatypeRestrictions.size()-1;index>=0;--index) {
            AtomicDataRange dataRange=variable.m_positiveDatatypeRestrictions.get(index);
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
        for (int index=variable.m_negativeDatatypeRestrictions.size()-1;index>=0;--index) {
            LiteralDataRange dataRange=variable.m_negativeDatatypeRestrictions.get(index).getNegation();
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
        for (int index=variable.m_positiveConstantEnumerations.size()-1;index>=0;--index) {
            AtomicDataRange dataRange=variable.m_positiveConstantEnumerations.get(index);
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
        for (int index=variable.m_negativeConstantEnumerations.size()-1;index>=0;--index) {
            LiteralDataRange dataRange=variable.m_negativeConstantEnumerations.get(index).getNegation();
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
    }

    public static class DConjunction implements Serializable {
        private static final long serialVersionUID = 3597740301361593691L;
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
            for (int index=m_activeVariables.size()-1;index>=0;--index)
                m_activeVariables.get(index).clearEqualities();
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
        protected DVariable getVariableForEx(Node node,boolean[] newVariableAdded) {
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
            // Inequalities between nodes in the tableau are detected by the ExtensionManager.
            // Consequently, the DConjunction should not contain inequalities between DVariables.
            assert node1!=node2;
            if (!node1.m_unequalTo.contains(node2)) {
                node1.m_unequalTo.add(node2);
                node2.m_unequalTo.add(node1);
                node1.m_unequalToDirect.add(node2);
            }
        }
        public boolean isSymmetricClique() {
            // This method depends on the fact that there are no self-links.
            int numberOfVariables=m_activeVariables.size();
            if (numberOfVariables>0) {
                DVariable first=m_activeVariables.get(0);
                for (int variableIndex=numberOfVariables-1;variableIndex>=0;--variableIndex) {
                    DVariable variable=m_activeVariables.get(variableIndex);
                    if (variable.m_unequalTo.size()+1!=numberOfVariables || !first.hasSameRestrictions(variable))
                        return false;
                }
            }
            return true;
        }
        public String toString() {
            return toString(Prefixes.STANDARD_PREFIXES);
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
                buffer.append(variableIndex);
                buffer.append(')');
                for (int neighborIndex=0;neighborIndex<variable.m_unequalToDirect.size();neighborIndex++) {
                    buffer.append(" & ");
                    buffer.append(variableIndex);
                    buffer.append(" != ");
                    buffer.append(m_activeVariables.indexOf(variable.m_unequalToDirect.get(neighborIndex)));
                }
            }
            return buffer.toString();
        }
    }

    public static class DVariable implements Serializable {
        private static final long serialVersionUID = -2490195841140286089L;
        protected final List<ConstantEnumeration> m_positiveConstantEnumerations;
        protected final List<ConstantEnumeration> m_negativeConstantEnumerations;
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
            m_positiveConstantEnumerations=new ArrayList<ConstantEnumeration>();
            m_negativeConstantEnumerations=new ArrayList<ConstantEnumeration>();
            m_positiveDatatypeRestrictions=new ArrayList<DatatypeRestriction>();
            m_negativeDatatypeRestrictions=new ArrayList<DatatypeRestriction>();
            m_unequalTo=new ArrayList<DVariable>();
            m_unequalToDirect=new ArrayList<DVariable>();
            m_forbiddenDataValues=new ArrayList<Object>();
            m_explicitDataValues=new ArrayList<Object>();
        }
        protected void dispose() {
            m_positiveConstantEnumerations.clear();
            m_negativeConstantEnumerations.clear();
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
        protected void clearEqualities() {
            m_unequalTo.clear();
            m_unequalToDirect.clear();
        }
        protected void addForbiddenDataValue(Object forbiddenDataValue) {
            if (!m_forbiddenDataValues.contains(forbiddenDataValue))
                m_forbiddenDataValues.add(forbiddenDataValue);
        }
        public boolean hasCardinalityAtLeast(int number) {
            if (m_hasExplicitDataValues)
                return m_explicitDataValues.size()>=number;
            else if (m_valueSpaceSubset!=null)
                return m_valueSpaceSubset.hasCardinalityAtLeast(number+m_forbiddenDataValues.size());
            else
                return true;
        }
        public Node getNode() {
            return m_node;
        }
        public List<ConstantEnumeration> getPositiveDataValueEnumerations() {
            return Collections.unmodifiableList(m_positiveConstantEnumerations);
        }
        public List<ConstantEnumeration> getNegativeDataValueEnumerations() {
            return Collections.unmodifiableList(m_negativeConstantEnumerations);
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
        public boolean hasSameRestrictions(DVariable that) {
            return this==that || (
                equals(m_positiveConstantEnumerations,that.m_positiveConstantEnumerations) &&
                equals(m_negativeConstantEnumerations,that.m_negativeConstantEnumerations) &&
                equals(m_positiveDatatypeRestrictions,that.m_positiveDatatypeRestrictions) &&
                equals(m_negativeDatatypeRestrictions,that.m_negativeDatatypeRestrictions)
            );
        }
        protected static <T> boolean equals(List<T> first,List<T> second) {
            if (first.size()!=second.size())
                return false;
            for (int index=first.size()-1;index>=0;--index) {
                T object=first.get(index);
                if (!second.contains(object))
                    return false;
            }
            return true;
        }
        public String toString() {
            return toString(Prefixes.STANDARD_PREFIXES);
        }
        public String toString(Prefixes prefixes) {
            StringBuffer buffer=new StringBuffer();
            boolean first=true;
            buffer.append('[');
            for (int index=0;index<m_positiveConstantEnumerations.size();index++) {
                if (first)
                    first=false;
                else
                    buffer.append(", ");
                buffer.append(m_positiveConstantEnumerations.get(index).toString(prefixes));
            }
            for (int index=0;index<m_negativeConstantEnumerations.size();index++) {
                if (first)
                    first=false;
                else
                    buffer.append(", ");
                buffer.append(m_negativeConstantEnumerations.get(index).getNegation().toString(prefixes));
            }
            for (int index=0;index<m_positiveDatatypeRestrictions.size();index++) {
                if (first)
                    first=false;
                else
                    buffer.append(", ");
                buffer.append(m_positiveDatatypeRestrictions.get(index).toString(prefixes));
            }
            for (int index=0;index<m_negativeDatatypeRestrictions.size();index++) {
                if (first)
                    first=false;
                else
                    buffer.append(", ");
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

    protected static class SmallestEnumerationFirst implements Comparator<DVariable>, Serializable {
        private static final long serialVersionUID = 8838838641444833249L;
        public static final Comparator<DVariable> INSTANCE=new SmallestEnumerationFirst();

        public int compare(DVariable o1,DVariable o2) {
            return o1.m_explicitDataValues.size()-o2.m_explicitDataValues.size();
        }

    }
}
