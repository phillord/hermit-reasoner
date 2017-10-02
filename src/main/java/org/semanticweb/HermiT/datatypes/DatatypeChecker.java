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
package org.semanticweb.HermiT.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.AtomicNegationDataRange;
import org.semanticweb.HermiT.model.ConstantEnumeration;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.InternalDatatype;

public abstract class DatatypeChecker<NT> implements Serializable {
	private static final long serialVersionUID = -2651936436581879066L;

	protected final SmallestEnumerationFirst<NT> m_smallestEnumerationFirst;
    protected final List<DVariable<NT>> m_unusedVariables;
    protected final List<DVariable<NT>> m_usedVariables;
    protected final List<DVariable<NT>> m_activeVariables;
    protected final List<DVariable<NT>> m_auxiliaryVariableList;
    protected DVariable<NT>[] m_buckets;
    protected int m_numberOfEntries;
    protected int m_resizeThreshold;

	@SuppressWarnings("unchecked")
	public DatatypeChecker() {
		m_smallestEnumerationFirst= new SmallestEnumerationFirst<>();
        m_unusedVariables= new ArrayList<>();
        m_usedVariables= new ArrayList<>();
        m_activeVariables= new ArrayList<>();
        m_auxiliaryVariableList= new ArrayList<>();
        m_buckets=new DVariable[16];
        m_resizeThreshold=(int)(m_buckets.length*0.75);
        m_numberOfEntries=0;
    }
    public void clear() {
        for (int index=m_usedVariables.size()-1;index>=0;--index) {
            DVariable<NT> variable=m_usedVariables.get(index);
            variable.dispose();
            m_unusedVariables.add(variable);
        }
        m_usedVariables.clear();
        m_activeVariables.clear();
        m_auxiliaryVariableList.clear();
        Arrays.fill(m_buckets,null);
        m_numberOfEntries=0;
    }
    public String toString() {
        return toString(Prefixes.ImmutablePrefixes.getStandartPrefixes());
    }
    public String toString(Prefixes prefixes) {
        StringBuffer buffer=new StringBuffer();
        boolean first=true;
        for (int variableIndex=0;variableIndex<m_activeVariables.size();variableIndex++) {
            if (first)
                first=false;
            else
                buffer.append(" & ");
            DVariable<NT> variable=m_activeVariables.get(variableIndex);
            buffer.append(variable.toString(prefixes));
            buffer.append('(');
            buffer.append(variableIndex);
            buffer.append(')');
            for (int neighborIndex=0;neighborIndex<variable.m_unequalToDirected.size();neighborIndex++) {
                buffer.append(" & ");
                buffer.append(variableIndex);
                buffer.append(" != ");
                buffer.append(m_activeVariables.indexOf(variable.m_unequalToDirected.get(neighborIndex)));
            }
        }
        return buffer.toString();
    }
    public DVariable<NT> getVariableFor(NT node) {
        int index=getIndexFor(node.hashCode(),m_buckets.length);
        DVariable<NT> entry=m_buckets[index];
        while (entry!=null) {
            if (entry.m_node==node)
                return entry;
            entry=entry.m_nextEntry;
        }
        return null;
    }
    public DVariable<NT> getVariableForEx(NT node,boolean[] newVariableAdded) {
        int index=getIndexFor(node.hashCode(),m_buckets.length);
        DVariable<NT> entry=m_buckets[index];
        while (entry!=null) {
            if (entry.m_node==node) {
                newVariableAdded[0]=false;
                return entry;
            }
            entry=entry.m_nextEntry;
        }
        DVariable<NT> newVariable;
        if (m_unusedVariables.isEmpty())
            newVariable= new DVariable<>();
        else
            newVariable=m_unusedVariables.remove(m_unusedVariables.size()-1);
        newVariable.m_node=node;
        newVariable.m_nextEntry=m_buckets[index];
        m_buckets[index]=newVariable;
        m_numberOfEntries++;
        if (m_numberOfEntries>=m_resizeThreshold)
            resize(m_buckets.length*2);
        newVariableAdded[0]=true;
        if (!newVariable.m_isUsed) {
        	m_usedVariables.add(newVariable);
        	newVariable.m_isUsed=true;
        }
        return newVariable;
    }
    protected void resize(int newCapacity) {
        @SuppressWarnings("unchecked")
		DVariable<NT>[] newBuckets=new DVariable[newCapacity];
        for (DVariable<NT> m_bucket : m_buckets) {
            DVariable<NT> entry = m_bucket;
            while (entry != null) {
                DVariable<NT> nextEntry = entry.m_nextEntry;
                int newIndex = getIndexFor(entry.m_node.hashCode(), newCapacity);
                entry.m_nextEntry = newBuckets[newIndex];
                newBuckets[newIndex] = entry;
                entry = nextEntry;
            }
        }
        m_buckets=newBuckets;
        m_resizeThreshold=(int)(newCapacity*0.75);
    }
    public void clearActiveVariables() {
        m_activeVariables.clear();
    }
    public List<DVariable<NT>> getActiveVariables() {
        return m_activeVariables;
    }
    public void addInequality(DVariable<NT> variable1,DVariable<NT> variable2) {
        // Inequalities between nodes in the tableau are detected by the ExtensionManager.
        // Consequently, the conjunction to be checked should not contain inequalities between the same variables.
        assert variable1!=variable2;
        if (!variable1.m_unequalTo.contains(variable2)) {
            variable1.m_unequalTo.add(variable2);
            variable2.m_unequalTo.add(variable1);
            variable1.m_unequalToDirected.add(variable2);
        }
    }
    public Object getUnsatisfiabilityCauseOrCauses() {
        if (!m_activeVariables.isEmpty()) {
	        if (isSymmetricClique()) {
                DVariable<NT> representative=m_activeVariables.get(0);
                if (!representative.hasCardinalityAtLeast(m_activeVariables.size()))
                    return m_activeVariables;
	        }
	        else {
	            eliminateTrivialInequalities();
	            eliminateTriviallySatisfiableVariables();
	            DVariable<NT> emptyVariable=enumerateValueSpaceSubsets();
	            if (emptyVariable!=null)
	            	return emptyVariable;
                eliminateTriviallySatisfiableVariables();
                if (!checkAssignments())
                	return m_activeVariables;
	        }
        }
        return null;
    }
    protected void eliminateTrivialInequalities() {
        for (int index1=m_activeVariables.size()-1;index1>=0;--index1) {
            DVariable<NT> variable1=m_activeVariables.get(index1);
            if (variable1.m_mostSpecificRestriction!=null) {
                String datatypeURI1=variable1.m_mostSpecificRestriction.getDatatypeURI();
                for (int index2=variable1.m_unequalToDirected.size()-1;index2>=0;--index2) {
                    DVariable<NT> variable2=variable1.m_unequalToDirected.get(index2);
                    if (variable2.m_mostSpecificRestriction!=null && DatatypeRegistry.isDisjointWith(datatypeURI1,variable2.m_mostSpecificRestriction.getDatatypeURI())) {
                        variable1.m_unequalTo.remove(variable2);
                        variable1.m_unequalToDirected.remove(variable2);
                        variable2.m_unequalTo.remove(variable1);
                        variable2.m_unequalToDirected.remove(variable1);
                    }
                }
            }
        }
    }
    protected void eliminateTriviallySatisfiableVariables() {
        m_auxiliaryVariableList.clear();
        for (int index=m_activeVariables.size()-1;index>=0;--index)
            m_auxiliaryVariableList.add(m_activeVariables.get(index));
        while (!m_auxiliaryVariableList.isEmpty()) {
            DVariable<NT> variable=m_auxiliaryVariableList.remove(m_auxiliaryVariableList.size()-1);
            if (variable.hasCardinalityAtLeast(variable.m_unequalTo.size()+1)) {
                for (int index=variable.m_unequalTo.size()-1;index>=0;--index) {
                    DVariable<NT> neighborVariable=variable.m_unequalTo.get(index);
                    neighborVariable.m_unequalTo.remove(variable);
                    neighborVariable.m_unequalToDirected.remove(variable);
                    if (!m_auxiliaryVariableList.contains(neighborVariable))
                        m_auxiliaryVariableList.add(neighborVariable);
                }
                m_activeVariables.remove(variable);
            }
        }
    }
    protected DVariable<NT> enumerateValueSpaceSubsets() {
        for (int index=m_activeVariables.size()-1;index>=0;--index) {
            DVariable<NT> variable=m_activeVariables.get(index);
            if (!variable.enumerateValueSpaceSubset())
            	return variable;
        }
        return null;
    }
    protected boolean checkAssignments() {
        // This method could be further optimized to check each clique of inequalities separately.
        // It is not expected that this is an important optimization, so we don't to it for the moment.
        // The nodes are sorted so that we get a kind of 'join order' optimization.
        Collections.sort(m_activeVariables,m_smallestEnumerationFirst);
        return findAssignment(0);
    }
    protected boolean findAssignment(int nodeIndex) {
        if (nodeIndex==m_activeVariables.size())
            return true;
        else {
            DVariable<NT> variable=m_activeVariables.get(nodeIndex);
            for (int valueIndex=variable.m_explicitDataValues.size()-1;valueIndex>=0;--valueIndex) {
                Object dataValue=variable.m_explicitDataValues.get(valueIndex);
                if (variable.satisfiesNeighbors(dataValue)) {
                    variable.m_dataValue=dataValue;
                    if (findAssignment(nodeIndex+1))
                        return true;
                }
                checkInterrupt();
            }
            variable.m_dataValue=null;
            return false;
        }
    }
    protected boolean isSymmetricClique() {
        // This method depends on the fact that there are no self-links.
        int numberOfVariables=m_activeVariables.size();
        if (numberOfVariables>0) {
            DVariable<NT> first=m_activeVariables.get(0);
            for (int variableIndex=numberOfVariables-1;variableIndex>=0;--variableIndex) {
                DVariable<NT> variable=m_activeVariables.get(variableIndex);
                if (variable.m_unequalTo.size()+1!=numberOfVariables || !first.hasSameRestrictions(variable))
                    return false;
            }
        }
        return true;
    }
    protected static int getIndexFor(int hashCode,int tableLength) {
        hashCode+=~(hashCode << 9);
        hashCode^=(hashCode >>> 14);
        hashCode+=(hashCode << 4);
        hashCode^=(hashCode >>> 10);
        return hashCode & (tableLength-1);
    }
    protected static boolean containsDataValue(ConstantEnumeration constantEnumeration,Object dataValue) {
        for (int index=constantEnumeration.getNumberOfConstants()-1;index>=0;--index)
            if (constantEnumeration.getConstant(index).getDataValue().equals(dataValue))
                return true;
        return false;
    }
    protected abstract void checkInterrupt();

    public static class DVariable<NT> implements Serializable {
        private static final long serialVersionUID = -2490195841140286089L;
        protected final List<ConstantEnumeration> m_positiveConstantEnumerations;
        protected final List<ConstantEnumeration> m_negativeConstantEnumerations;
        protected final List<DatatypeRestriction> m_positiveDatatypeRestrictions;
        protected final List<DatatypeRestriction> m_negativeDatatypeRestrictions;
        protected final List<DVariable<NT>> m_unequalTo;
        protected final List<DVariable<NT>> m_unequalToDirected;
        protected final List<Object> m_forbiddenDataValues;
        protected final List<Object> m_explicitDataValues;
        protected boolean m_isUsed;
        protected boolean m_hasExplicitDataValues;
        protected DatatypeRestriction m_mostSpecificRestriction;
        protected NT m_node;
        protected DVariable<NT> m_nextEntry;
        protected ValueSpaceSubset m_valueSpaceSubset;
        protected Object m_dataValue;

        protected DVariable() {
            m_positiveConstantEnumerations= new ArrayList<>();
            m_negativeConstantEnumerations= new ArrayList<>();
            m_positiveDatatypeRestrictions= new ArrayList<>();
            m_negativeDatatypeRestrictions= new ArrayList<>();
            m_unequalTo= new ArrayList<>();
            m_unequalToDirected= new ArrayList<>();
            m_forbiddenDataValues= new ArrayList<>();
            m_explicitDataValues= new ArrayList<>();
        }
        public NT getNode() {
            return m_node;
        }
        public List<ConstantEnumeration> getPositiveConstantEnumerations() {
        	return m_positiveConstantEnumerations;
        }
        public List<ConstantEnumeration> getNegativeConstantEnumerations() {
        	return m_negativeConstantEnumerations;
        }
        public List<ConstantEnumeration> getPositiveDataValueEnumerations() {
            return m_positiveConstantEnumerations;
        }
        public List<ConstantEnumeration> getNegativeDataValueEnumerations() {
            return m_negativeConstantEnumerations;
        }
        public List<DatatypeRestriction> getPositiveDatatypeRestrictions() {
            return m_positiveDatatypeRestrictions;
        }
        public List<DatatypeRestriction> getNegativeDatatypeRestrictions() {
            return m_negativeDatatypeRestrictions;
        }
        public List<DVariable<NT>> getUnequalToDirected() {
            return m_unequalToDirected;
        }
        public String toString() {
            return toString(Prefixes.ImmutablePrefixes.getStandartPrefixes());
        }
        public String toString(Prefixes prefixes) {
            StringBuffer buffer=new StringBuffer();
            boolean first=true;
            buffer.append('[');
            for (ConstantEnumeration m_positiveConstantEnumeration : m_positiveConstantEnumerations) {
                if (first)
                    first = false;
                else
                    buffer.append(", ");
                buffer.append(m_positiveConstantEnumeration.toString(prefixes));
            }
            for (ConstantEnumeration m_negativeConstantEnumeration : m_negativeConstantEnumerations) {
                if (first)
                    first = false;
                else
                    buffer.append(", ");
                buffer.append(m_negativeConstantEnumeration.getNegation().toString(prefixes));
            }
            for (DatatypeRestriction m_positiveDatatypeRestriction : m_positiveDatatypeRestrictions) {
                if (first)
                    first = false;
                else
                    buffer.append(", ");
                buffer.append(m_positiveDatatypeRestriction.toString(prefixes));
            }
            for (DatatypeRestriction m_negativeDatatypeRestriction : m_negativeDatatypeRestrictions) {
                if (first)
                    first = false;
                else
                    buffer.append(", ");
                buffer.append(m_negativeDatatypeRestriction.getNegation().toString(prefixes));
            }
            buffer.append(']');
            return buffer.toString();
        }
        public DatatypeRestriction addDataRange(DataRange dataRange,Set<DatatypeRestriction> unknownDatatypeRestrictions1,Set<DatatypeRestriction> unknownDatatypeRestrictions2) {
            if (dataRange instanceof InternalDatatype) {
                // Internal datatypes are skipped, as they do not contribute to datatype checking.
                // These are used to encode rdfs:Literal and datatype definitions, and to rename complex data ranges.
            }
            else if (dataRange instanceof DatatypeRestriction) {
                DatatypeRestriction datatypeRestriction=(DatatypeRestriction)dataRange;
                if ((unknownDatatypeRestrictions1==null || !unknownDatatypeRestrictions1.contains(datatypeRestriction)) && (unknownDatatypeRestrictions2==null || !unknownDatatypeRestrictions2.contains(datatypeRestriction))) {
	                m_positiveDatatypeRestrictions.add(datatypeRestriction);
	                if (m_mostSpecificRestriction==null)
	                    m_mostSpecificRestriction=datatypeRestriction;
	                else if (DatatypeRegistry.isDisjointWith(m_mostSpecificRestriction.getDatatypeURI(),datatypeRestriction.getDatatypeURI()))
	                	return m_mostSpecificRestriction;
	                else if (DatatypeRegistry.isSubsetOf(datatypeRestriction.getDatatypeURI(),m_mostSpecificRestriction.getDatatypeURI()))
	                    m_mostSpecificRestriction=datatypeRestriction;
                }
            }
            else if (dataRange instanceof ConstantEnumeration)
                m_positiveConstantEnumerations.add((ConstantEnumeration)dataRange);
            else if (dataRange instanceof AtomicNegationDataRange) {
                DataRange negatedDataRange=((AtomicNegationDataRange)dataRange).getNegatedDataRange();
                if (negatedDataRange instanceof InternalDatatype) {
                    // Skip for the same reasons as above.
                }
                else if (negatedDataRange instanceof DatatypeRestriction) {
                    DatatypeRestriction datatypeRestriction=(DatatypeRestriction)negatedDataRange;
                    if ((unknownDatatypeRestrictions1==null || !unknownDatatypeRestrictions1.contains(datatypeRestriction)) && (unknownDatatypeRestrictions2==null || !unknownDatatypeRestrictions2.contains(datatypeRestriction)))
                        m_negativeDatatypeRestrictions.add(datatypeRestriction);
                }
                else if (negatedDataRange instanceof ConstantEnumeration) {
                    ConstantEnumeration negatedConstantEnumeration=(ConstantEnumeration)negatedDataRange;
                    m_negativeConstantEnumerations.add(negatedConstantEnumeration);
                    for (int index=negatedConstantEnumeration.getNumberOfConstants()-1;index>=0;--index) {
                        Object forbiddenDataValue = negatedConstantEnumeration.getConstant(index).getDataValue();
                        if (!m_forbiddenDataValues.contains(forbiddenDataValue))
                            m_forbiddenDataValues.add(forbiddenDataValue);
                    }
                }
                else
                    throw new IllegalStateException("Internal error: invalid data range.");
            }
            else
                throw new IllegalStateException("Internal error: invalid data range.");
            return null;
        }
        public boolean prepareForSatisfiabilityChecking() {
            if (!m_positiveConstantEnumerations.isEmpty())
                return prepareAsEnumeration();
            else if (!m_positiveDatatypeRestrictions.isEmpty())
                return prepareAsValueSpaceSubset();
            else
            	return true;
        }
        protected boolean prepareAsEnumeration() {
            m_hasExplicitDataValues=true;
            List<Object> explicitDataValues=m_explicitDataValues;
            List<ConstantEnumeration> positiveConstantEnumerations=m_positiveConstantEnumerations;
            ConstantEnumeration firstDataValueEnumeration=positiveConstantEnumerations.get(0);
            nextValue: for (int index=firstDataValueEnumeration.getNumberOfConstants()-1;index>=0;--index) {
                Object dataValue=firstDataValueEnumeration.getConstant(index).getDataValue();
                if (!explicitDataValues.contains(dataValue) && !m_forbiddenDataValues.contains(dataValue)) {
                    for (int enumerationIndex=positiveConstantEnumerations.size()-1;enumerationIndex>=1;--enumerationIndex)
                        if (!containsDataValue(positiveConstantEnumerations.get(enumerationIndex),dataValue))
                            continue nextValue;
                    explicitDataValues.add(dataValue);
                }
            }
            m_forbiddenDataValues.clear();
            List<DatatypeRestriction> positiveDatatypeRestrictions=m_positiveDatatypeRestrictions;
            for (int index=positiveDatatypeRestrictions.size()-1;!explicitDataValues.isEmpty() && index>=0;--index) {
                DatatypeRestriction positiveDatatypeRestriction=positiveDatatypeRestrictions.get(index);
                ValueSpaceSubset valueSpaceSubset=DatatypeRegistry.createValueSpaceSubset(positiveDatatypeRestriction);
                eliminateDataValuesUsingValueSpaceSubset(valueSpaceSubset,explicitDataValues,false);
            }
            List<DatatypeRestriction> negativeDatatypeRestrictions=m_negativeDatatypeRestrictions;
            for (int index=negativeDatatypeRestrictions.size()-1;!explicitDataValues.isEmpty() && index>=0;--index) {
                DatatypeRestriction negativeDatatypeRestriction=negativeDatatypeRestrictions.get(index);
                ValueSpaceSubset valueSpaceSubset=DatatypeRegistry.createValueSpaceSubset(negativeDatatypeRestriction);
                eliminateDataValuesUsingValueSpaceSubset(valueSpaceSubset,explicitDataValues,true);
            }
            return !explicitDataValues.isEmpty();
        }
        protected void eliminateDataValuesUsingValueSpaceSubset(ValueSpaceSubset valueSpaceSubset,List<Object> explicitDataValues,boolean eliminateWhenValue) {
            for (int valueIndex=explicitDataValues.size()-1;valueIndex>=0;--valueIndex) {
                Object dataValue=explicitDataValues.get(valueIndex);
                if (valueSpaceSubset.containsDataValue(dataValue)==eliminateWhenValue)
                    explicitDataValues.remove(valueIndex);
            }
        }
        protected boolean prepareAsValueSpaceSubset() {
            String mostSpecificDatatypeURI=m_mostSpecificRestriction.getDatatypeURI();
            m_valueSpaceSubset=DatatypeRegistry.createValueSpaceSubset(m_mostSpecificRestriction);
            List<DatatypeRestriction> positiveDatatypeRestrictions=m_positiveDatatypeRestrictions;
            for (int index=positiveDatatypeRestrictions.size()-1;index>=0;--index) {
                DatatypeRestriction datatypeRestriction=positiveDatatypeRestrictions.get(index);
                if (datatypeRestriction!=m_mostSpecificRestriction)
                    m_valueSpaceSubset=DatatypeRegistry.conjoinWithDR(m_valueSpaceSubset,datatypeRestriction);
            }
            List<DatatypeRestriction> negativeDatatypeRestrictions=m_negativeDatatypeRestrictions;
            for (int index=negativeDatatypeRestrictions.size()-1;index>=0;--index) {
                DatatypeRestriction datatypeRestriction=negativeDatatypeRestrictions.get(index);
                String datatypeRestrictionDatatypeURI=datatypeRestriction.getDatatypeURI();
                if (!DatatypeRegistry.isDisjointWith(mostSpecificDatatypeURI,datatypeRestrictionDatatypeURI))
                    m_valueSpaceSubset=DatatypeRegistry.conjoinWithDRNegation(m_valueSpaceSubset,datatypeRestriction);
            }
            if (!m_valueSpaceSubset.hasCardinalityAtLeast(1)) {
                m_forbiddenDataValues.clear();
                return false;
            }
            else {
                for (int valueIndex=m_forbiddenDataValues.size()-1;valueIndex>=0;--valueIndex) {
                    Object forbiddenValue=m_forbiddenDataValues.get(valueIndex);
                    if (!m_valueSpaceSubset.containsDataValue(forbiddenValue))
                        m_forbiddenDataValues.remove(valueIndex);
                }
                return true;
            }
        }
        protected boolean hasCardinalityAtLeast(int number) {
            if (m_hasExplicitDataValues)
                return m_explicitDataValues.size()>=number;
            else if (m_valueSpaceSubset!=null)
                return m_valueSpaceSubset.hasCardinalityAtLeast(number+m_forbiddenDataValues.size());
            else
                return true;
        }
        protected boolean hasSameRestrictions(DVariable<NT> that) {
            return this==that || (
                equals(m_positiveConstantEnumerations,that.m_positiveConstantEnumerations) &&
                equals(m_negativeConstantEnumerations,that.m_negativeConstantEnumerations) &&
                equals(m_positiveDatatypeRestrictions,that.m_positiveDatatypeRestrictions) &&
                equals(m_negativeDatatypeRestrictions,that.m_negativeDatatypeRestrictions)
            );
        }
        protected boolean enumerateValueSpaceSubset() {
            if (m_valueSpaceSubset!=null) {
                m_hasExplicitDataValues=true;
                m_valueSpaceSubset.enumerateDataValues(m_explicitDataValues);
                if (!m_forbiddenDataValues.isEmpty()) {
                    for (int valueIndex=m_explicitDataValues.size()-1;valueIndex>=0;--valueIndex) {
                        Object dataValue=m_explicitDataValues.get(valueIndex);
                        if (m_forbiddenDataValues.contains(dataValue))
                            m_explicitDataValues.remove(valueIndex);
                    }
                }
                m_valueSpaceSubset=null;
                m_forbiddenDataValues.clear();
                return !m_explicitDataValues.isEmpty();
            }
            else
            	return true;
        }
        protected boolean satisfiesNeighbors(Object dataValue) {
            for (int neighborIndex=m_unequalTo.size()-1;neighborIndex>=0;--neighborIndex) {
                Object neighborDataValue=m_unequalTo.get(neighborIndex).m_dataValue;
                if (neighborDataValue!=null && neighborDataValue.equals(dataValue))
                    return false;
            }
            return true;
        }
        protected void dispose() {
            m_positiveConstantEnumerations.clear();
            m_negativeConstantEnumerations.clear();
            m_positiveDatatypeRestrictions.clear();
            m_negativeDatatypeRestrictions.clear();
            m_unequalTo.clear();
            m_unequalToDirected.clear();
            m_forbiddenDataValues.clear();
            m_explicitDataValues.clear();
            m_isUsed=false;
            m_hasExplicitDataValues=false;
            m_mostSpecificRestriction=null;
            m_node=null;
            m_nextEntry=null;
            m_valueSpaceSubset=null;
            m_dataValue=null;
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
    }

    protected static class SmallestEnumerationFirst<NT> implements Comparator<DVariable<NT>>, Serializable {
        private static final long serialVersionUID = 8838838641444833249L;

        public int compare(DVariable<NT> o1,DVariable<NT> o2) {
            return o1.m_explicitDataValues.size()-o2.m_explicitDataValues.size();
        }

    }
}
