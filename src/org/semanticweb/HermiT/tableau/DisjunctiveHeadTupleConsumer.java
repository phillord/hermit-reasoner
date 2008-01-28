package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;

public class DisjunctiveHeadTupleConsumer implements TupleConsumer,Serializable {
    private static final long serialVersionUID=-7169241066922398377L;

    protected final Tableau m_tableau;
    protected final DLPredicate[] m_headDLPredicates;
    protected final int[] m_disjunctStart;
    protected final int[] m_copyTupleToArguments;

    public DisjunctiveHeadTupleConsumer(Tableau tableau,DLPredicate[] headDLPredicates,int[][] copyTupleToHeads) {
        m_tableau=tableau;
        m_headDLPredicates=headDLPredicates;
        m_disjunctStart=new int[m_headDLPredicates.length];
        int argumentsSize=0;
        for (int disjunctIndex=0;disjunctIndex<m_headDLPredicates.length;disjunctIndex++) {
            m_disjunctStart[disjunctIndex]=argumentsSize;
            argumentsSize+=m_headDLPredicates[disjunctIndex].getArity();
        }
        m_copyTupleToArguments=new int[argumentsSize];
        int number=0;
        for (int i=0;i<m_headDLPredicates.length;i++) {
            int[] indices=copyTupleToHeads[i];
            for (int j=0;j<indices.length;j++)
                m_copyTupleToArguments[number++]=indices[j];
        }
    }
    public void consumeTuple(Object[] tuple,DependencySet dependencySet) {
        Node[] arguments=new Node[m_copyTupleToArguments.length];
        for (int argumentIndex=m_copyTupleToArguments.length-1;argumentIndex>=0;--argumentIndex)
            arguments[argumentIndex]=(Node)tuple[m_copyTupleToArguments[argumentIndex]];
        GroundDisjunction groundDisjunction=new GroundDisjunction(m_tableau,m_headDLPredicates,m_disjunctStart,arguments,m_tableau.m_dependencySetFactory.getPermanent(dependencySet));
        if (!groundDisjunction.isSatisfied(m_tableau))
            m_tableau.addGroundDisjunction(groundDisjunction);
    }
}
