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
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.datatypes.DatatypeChecker;
import org.semanticweb.HermiT.model.AtomicDataRange;
import org.semanticweb.HermiT.model.AtomicNegationDataRange;
import org.semanticweb.HermiT.model.ConstantEnumeration;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.Inequality;
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
    protected final DatatypeChecker<Node> m_datatypeChecker;
    protected final List<DatatypeChecker.DVariable<Node>> m_auxiliaryVariableList;
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
        m_datatypeChecker=new DatatypeChecker<Node>() {
			private static final long serialVersionUID = -5349765268779083331L;

			protected final void checkInterrupt() {
            	m_interruptFlag.checkInterrupt();
            }
        };
        m_auxiliaryVariableList= new ArrayList<>();
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
        m_datatypeChecker.clear();
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
        m_datatypeChecker.clear();
        Object[] tupleBuffer=m_assertionsDeltaOldRetrieval.getTupleBuffer();
        m_assertionsDeltaOldRetrieval.open();
        while (!m_extensionManager.containsClash() && !m_assertionsDeltaOldRetrieval.afterLast()) {
            if (tupleBuffer[0] instanceof DataRange) {
                // A data range was added in the last saturation step, so we check the D-conjunction hanging off of its node.
                Node node=(Node)tupleBuffer[1];
                DatatypeChecker.DVariable<Node> variable=getAndInitializeVariableFor(node,m_newVariableAdded);
                // m_newVariableAdded[0]==false means that 'variable' has already been checked in this iteration.
                if (m_newVariableAdded[0]) {
                    m_datatypeChecker.clearActiveVariables();
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
                    m_datatypeChecker.clearActiveVariables();
                    // An inequality between concrete was added in the last saturation step, so we check the D-conjunction hanging off of its node.
                    DatatypeChecker.DVariable<Node> variable1=getAndInitializeVariableFor(node1,m_newVariableAdded);
                    // m_newVariableAdded[0]==false means that 'variable1' has already been checked in this iteration.
                    if (m_newVariableAdded[0])
                        loadConjunctionFrom(variable1);
                    DatatypeChecker.DVariable<Node> variable2=getAndInitializeVariableFor(node2,m_newVariableAdded);
                    // m_newVariableAdded[0]==false means that 'variable1' has already been checked in this iteration.
                    if (m_newVariableAdded[0])
                        loadConjunctionFrom(variable2);
                    m_datatypeChecker.addInequality(variable1,variable2);
                    checkConjunctionSatisfiability();
                }
            }
            m_inequalityDeltaOldRetrieval.next();
        }
        if (m_tableauMonitor!=null)
            m_tableauMonitor.datatypeCheckingFinished(!m_extensionManager.containsClash());
        m_unionDependencySet.clearConstituents();
        m_datatypeChecker.clear();
        m_auxiliaryVariableList.clear();
    }
    protected void loadConjunctionFrom(DatatypeChecker.DVariable<Node> startVariable) {
        m_auxiliaryVariableList.clear();
        m_auxiliaryVariableList.add(startVariable);
        List<DatatypeChecker.DVariable<Node>> activeVariables=m_datatypeChecker.getActiveVariables();
        while (!m_extensionManager.containsClash() && !m_auxiliaryVariableList.isEmpty()) {
        	DatatypeChecker.DVariable<Node> reachedVariable=m_auxiliaryVariableList.remove(m_auxiliaryVariableList.size()-1);
            if (!activeVariables.contains(reachedVariable)) {
                activeVariables.add(reachedVariable);
                // Concrete root nodes are assigned a particular value, so they act as "breakers" in the conjunction:
                // the nodes that are unequal to them can be analyzed independently.
                if (reachedVariable.getNode().getNodeType()!=NodeType.ROOT_CONSTANT_NODE) {
                    // Look for all inequalities where reachedNode occurs in the first position.
                    m_inequality01Retrieval.getBindingsBuffer()[0]=Inequality.INSTANCE;
                    m_inequality01Retrieval.getBindingsBuffer()[1]=reachedVariable.getNode();
                    m_inequality01Retrieval.open();
                    Object[] tupleBuffer=m_inequality01Retrieval.getTupleBuffer();
                    while (!m_extensionManager.containsClash() && !m_inequality01Retrieval.afterLast()) {
                        Node newNode=(Node)tupleBuffer[2];
                        DatatypeChecker.DVariable<Node> newVariable=getAndInitializeVariableFor(newNode,m_newVariableAdded);
                        m_auxiliaryVariableList.add(newVariable);
                        m_datatypeChecker.addInequality(reachedVariable,newVariable);
                        m_inequality01Retrieval.next();
                        m_interruptFlag.checkInterrupt();
                    }
                    // Look for all inequalities where reachedNode occurs in the second position.
                    m_inequality02Retrieval.getBindingsBuffer()[0]=Inequality.INSTANCE;
                    m_inequality02Retrieval.getBindingsBuffer()[2]=reachedVariable.getNode();
                    m_inequality02Retrieval.open();
                    tupleBuffer=m_inequality02Retrieval.getTupleBuffer();
                    while (!m_extensionManager.containsClash() && !m_inequality02Retrieval.afterLast()) {
                        Node newNode=(Node)tupleBuffer[1];
                        DatatypeChecker.DVariable<Node> newVariable=getAndInitializeVariableFor(newNode,m_newVariableAdded);
                        m_auxiliaryVariableList.add(newVariable);
                        m_datatypeChecker.addInequality(newVariable,reachedVariable);
                        m_inequality02Retrieval.next();
                        m_interruptFlag.checkInterrupt();
                    }
                }
            }
        }
    }
    protected DatatypeChecker.DVariable<Node> getAndInitializeVariableFor(Node node,boolean[] newVariableAdded) {
    	DatatypeChecker.DVariable<Node> variable=m_datatypeChecker.getVariableForEx(node,newVariableAdded);
        if (m_newVariableAdded[0]) {
            m_assertions1Retrieval.getBindingsBuffer()[1]=variable.getNode();
            m_assertions1Retrieval.open();
            Object[] tupleBuffer=m_assertions1Retrieval.getTupleBuffer();
            while (!m_extensionManager.containsClash() && !m_assertions1Retrieval.afterLast()) {
                Object potentialDataRange=tupleBuffer[0];
                if (potentialDataRange instanceof DataRange) {
                	DatatypeRestriction clashingRestriction=variable.addDataRange((DataRange)potentialDataRange,m_unknownDatatypeRestrictionsPermanent,m_unknownDatatypeRestrictionsAdditional);
                	if (clashingRestriction!=null) {
                        DatatypeRestriction datatypeRestriction=(DatatypeRestriction)potentialDataRange;
                        m_unionDependencySet.clearConstituents();
                        m_unionDependencySet.addConstituent(m_extensionManager.getAssertionDependencySet(clashingRestriction,variable.getNode()));
                        m_unionDependencySet.addConstituent(m_extensionManager.getAssertionDependencySet(datatypeRestriction,variable.getNode()));
                        Object[] tuple1;
                        Object[] tuple2;
                        if (m_tableauMonitor!=null) {
                            tuple1=new Object[] { clashingRestriction,variable.getNode() };
                            tuple2=new Object[] { datatypeRestriction,variable.getNode()};
                            m_tableauMonitor.clashDetectionStarted(tuple1,tuple2);
                        }
                        m_extensionManager.setClash(m_unionDependencySet);
                        if (m_tableauMonitor!=null) {
                            tuple1=new Object[] { clashingRestriction,variable.getNode() };
                            tuple2=new Object[] { datatypeRestriction,variable.getNode() };
                            m_tableauMonitor.clashDetectionFinished(tuple1,tuple2);
                        }
                    }
                }
                m_assertions1Retrieval.next();
                m_interruptFlag.checkInterrupt();
            }
            if (!m_extensionManager.containsClash())
                variable.prepareForSatisfiabilityChecking();
        }
        return variable;
    }
    @SuppressWarnings("unchecked")
	protected void checkConjunctionSatisfiability() {
        if (!m_extensionManager.containsClash() && !m_datatypeChecker.getActiveVariables().isEmpty()) {
            if (m_tableauMonitor!=null)
                m_tableauMonitor.datatypeConjunctionCheckingStarted(m_datatypeChecker);
            Object result=m_datatypeChecker.getUnsatisfiabilityCauseOrCauses();
            if (result!=null) {
            	if (result instanceof DatatypeChecker.DVariable<?>) {
            		DatatypeChecker.DVariable<Node> variable=(DatatypeChecker.DVariable<Node>)result;
                    m_unionDependencySet.clearConstituents();
                    loadAssertionDependencySets(variable);
                    m_extensionManager.setClash(m_unionDependencySet);
            	}
            	else {
            		List<DatatypeChecker.DVariable<Node>> variables=(List<DatatypeChecker.DVariable<Node>>)result;
                    m_unionDependencySet.clearConstituents();
                    for (int nodeIndex=variables.size()-1;nodeIndex>=0;--nodeIndex) {
                    	DatatypeChecker.DVariable<Node> variable=variables.get(nodeIndex);
                        loadAssertionDependencySets(variable);
                        List<DatatypeChecker.DVariable<Node>> unequalToDirected=variable.getUnequalToDirected();
                        for (int neighborIndex=unequalToDirected.size()-1;neighborIndex>=0;--neighborIndex) {
                        	DatatypeChecker.DVariable<Node> neighborVariable=unequalToDirected.get(neighborIndex);
                            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(Inequality.INSTANCE,variable.getNode(),neighborVariable.getNode());
                            m_unionDependencySet.addConstituent(dependencySet);
                        }
                    }
                    m_extensionManager.setClash(m_unionDependencySet);
            	}
            }
            if (m_tableauMonitor!=null)
                m_tableauMonitor.datatypeConjunctionCheckingFinished(m_datatypeChecker,!m_extensionManager.containsClash());
        }
    }
    protected void loadAssertionDependencySets(DatatypeChecker.DVariable<Node> variable) {
        Node node=variable.getNode();
        List<DatatypeRestriction> positiveDatatypeRestrictions=variable.getPositiveDatatypeRestrictions();
        for (int index=positiveDatatypeRestrictions.size()-1;index>=0;--index) {
            AtomicDataRange dataRange=positiveDatatypeRestrictions.get(index);
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
        List<DatatypeRestriction> negativeDatatypeRestrictions=variable.getNegativeDatatypeRestrictions();
        for (int index=negativeDatatypeRestrictions.size()-1;index>=0;--index) {
            LiteralDataRange dataRange=negativeDatatypeRestrictions.get(index).getNegation();
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
        List<ConstantEnumeration> positiveConstantEnumerations=variable.getPositiveConstantEnumerations();
        for (int index=positiveConstantEnumerations.size()-1;index>=0;--index) {
            AtomicDataRange dataRange=positiveConstantEnumerations.get(index);
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
        List<ConstantEnumeration> negativeConstantEnumerations=variable.getNegativeConstantEnumerations();
        for (int index=negativeConstantEnumerations.size()-1;index>=0;--index) {
            LiteralDataRange dataRange=negativeConstantEnumerations.get(index).getNegation();
            DependencySet dependencySet=m_extensionManager.getAssertionDependencySet(dataRange,node);
            m_unionDependencySet.addConstituent(dependencySet);
        }
    }
}
