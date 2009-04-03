// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.model.*;

public class GroundDisjunction implements Serializable {
    private static final long serialVersionUID=6245673952732442673L;

    protected final DLPredicate[] m_dlPredicates;
    protected final int[] m_disjunctStart;
    protected final Node[] m_arguments;
    protected PermanentDependencySet m_dependencySet;
    protected GroundDisjunction m_previousGroundDisjunction;
    protected GroundDisjunction m_nextGroundDisjunction;

    public GroundDisjunction(Tableau tableau,DLPredicate[] dlPredicates,int[] disjunctStart,Node[] arguments,DependencySet dependencySet) {
        m_dlPredicates=dlPredicates;
        m_disjunctStart=disjunctStart;
        m_arguments=arguments;
        m_dependencySet=tableau.m_dependencySetFactory.getPermanent(dependencySet);
        tableau.m_dependencySetFactory.addUsage(m_dependencySet);
    }
    public GroundDisjunction getPreviousGroundDisjunction() {
        return m_previousGroundDisjunction;
    }
    public GroundDisjunction getNextGroundDisjunction() {
        return m_nextGroundDisjunction;
    }
    public void destroy(Tableau tableau) {
        tableau.m_dependencySetFactory.removeUsage(m_dependencySet);
        m_dependencySet=null;
    }
    public int getNumberOfDisjuncts() {
        return m_dlPredicates.length;
    }
    public DLPredicate getDLPredicate(int disjunctIndex) {
        return m_dlPredicates[disjunctIndex];
    }
    public Node getArgument(int disjunctIndex,int argumentIndex) {
        return m_arguments[m_disjunctStart[disjunctIndex]+argumentIndex];
    }
    public DependencySet getDependencySet() {
        return m_dependencySet;
    }
    public boolean isSatisfied(Tableau tableau) {
        ExtensionManager extensionManager=tableau.getExtensionManager();
        for (int disjunctIndex=0;disjunctIndex<getNumberOfDisjuncts();disjunctIndex++) {
            DLPredicate dlPredicate=getDLPredicate(disjunctIndex);
            switch (dlPredicate.getArity()) {
            case 1:
                if (extensionManager.containsAssertion(dlPredicate,getArgument(disjunctIndex,0).getCanonicalNode()))
                    return true;
                break;
            case 2:
                if (extensionManager.containsAssertion(dlPredicate,getArgument(disjunctIndex,0).getCanonicalNode(),getArgument(disjunctIndex,1).getCanonicalNode()))
                    return true;
                break;
            default:
                throw new IllegalStateException("Invalid arity of DL-predicate.");
            }
        }
        return false;
    }
    public boolean addDisjunctToTableau(Tableau tableau,int disjunctIndex,DependencySet choicePointDependencySet) {
        DependencySet dependencySet=choicePointDependencySet;
        DLPredicate dlPredicate=getDLPredicate(disjunctIndex);
        switch (dlPredicate.getArity()) {
        case 1:
            dependencySet=getArgument(disjunctIndex,0).addCacnonicalNodeDependencySet(dependencySet);
            return tableau.getExtensionManager().addAssertion(dlPredicate,getArgument(disjunctIndex,0).getCanonicalNode(),dependencySet);
        case 2:
            dependencySet=getArgument(disjunctIndex,0).addCacnonicalNodeDependencySet(dependencySet);
            dependencySet=getArgument(disjunctIndex,1).addCacnonicalNodeDependencySet(dependencySet);
            return tableau.getExtensionManager().addAssertion(dlPredicate,getArgument(disjunctIndex,0).getCanonicalNode(),getArgument(disjunctIndex,1).getCanonicalNode(),dependencySet);
        default:
            throw new IllegalStateException("Unsupported predicate arity.");
        }
    }
    public String toString(Prefixes prefixes) {
        StringBuffer buffer=new StringBuffer();
        for (int disjunctIndex=0;disjunctIndex<getNumberOfDisjuncts();disjunctIndex++) {
            if (disjunctIndex!=0)
                buffer.append(" v ");
            DLPredicate dlPredicate=getDLPredicate(disjunctIndex);
            if (Equality.INSTANCE.equals(dlPredicate)) {
                buffer.append(getArgument(disjunctIndex,0).getNodeID());
                buffer.append(" == ");
                buffer.append(getArgument(disjunctIndex,1).getNodeID());
            }
            else {
                buffer.append(dlPredicate.toString(prefixes));
                buffer.append('(');
                for (int argumentIndex=0;argumentIndex<dlPredicate.getArity();argumentIndex++) {
                    if (argumentIndex!=0)
                        buffer.append(',');
                    buffer.append(getArgument(disjunctIndex,argumentIndex).getNodeID());
                }
                buffer.append(')');
            }
        }
        return buffer.toString();
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
}
