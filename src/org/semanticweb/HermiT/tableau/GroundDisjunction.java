package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;

public class GroundDisjunction implements Serializable {
    private static final long serialVersionUID=6245673952732442673L;

    protected final DLPredicate[] m_dlPredicates;
    protected final int[] m_disjunctStart;
    protected final Node[] m_arguments;
    protected final DependencySet m_dependencySet;
    protected GroundDisjunction m_previousUnprocessedGroundDisjunction;
    protected GroundDisjunction m_nextUnprocessedGroundDisjunction;
    protected GroundDisjunction m_nextProcessedGroundDisjunction;

    public GroundDisjunction(Tableau tableau,DLPredicate[] dlPredicates,int[] disjunctStart,Node[] arguments,DependencySet dependencySet) {
        m_dlPredicates=dlPredicates;
        m_disjunctStart=disjunctStart;
        m_arguments=arguments;
        m_dependencySet=dependencySet;
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
                if (extensionManager.containsConceptAssertion((Concept)dlPredicate,getArgument(disjunctIndex,0).getCanonicalNode()))
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
        DLPredicate dlPredicate=getDLPredicate(disjunctIndex);
        switch (dlPredicate.getArity()) {
        case 1:
            return tableau.getExtensionManager().addConceptAssertion((Concept)dlPredicate,getArgument(disjunctIndex,0).getCanonicalNode(),m_dependencySet,choicePointDependencySet);
        case 2:
            return tableau.getExtensionManager().addAssertion(dlPredicate,getArgument(disjunctIndex,0).getCanonicalNode(),getArgument(disjunctIndex,1).getCanonicalNode(),m_dependencySet,choicePointDependencySet);
        default:
            throw new IllegalStateException("Unsupported predicate arity.");
        }
    }
    public String toString() {
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
                buffer.append(dlPredicate.toString());
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
}
