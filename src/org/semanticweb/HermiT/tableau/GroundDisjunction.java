// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;

public class GroundDisjunction implements Serializable {
    private static final long serialVersionUID=6245673952732442673L;

    protected final DLPredicate[] m_dlPredicates;
    protected final int[] m_disjunctStart;
    protected final Node[] m_arguments;
    protected final boolean[] m_isCore;
    protected PermanentDependencySet m_dependencySet;
    protected GroundDisjunction m_previousGroundDisjunction;
    protected GroundDisjunction m_nextGroundDisjunction;

    public GroundDisjunction(Tableau tableau,DLPredicate[] dlPredicates,int[] disjunctStart,Node[] arguments,boolean[] isCore,DependencySet dependencySet) {
        m_dlPredicates=dlPredicates;
        m_disjunctStart=disjunctStart;
        m_arguments=arguments;
        m_isCore=isCore;
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
    public boolean isCore(int disjunctIndex) {
        return m_isCore[disjunctIndex];
    }
    public DependencySet getDependencySet() {
        return m_dependencySet;
    }
    public boolean isPruned() {
        for (int argumentIndex=m_arguments.length-1;argumentIndex>=0;--argumentIndex)
            if (m_arguments[argumentIndex].isPruned())
                return true;
        return false;
    }
    
    @SuppressWarnings("fallthrough")
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
            case 3:
                if (dlPredicate instanceof AnnotatedEquality) {
                    if (extensionManager.containsAnnotatedEquality((AnnotatedEquality)dlPredicate,getArgument(disjunctIndex,0).getCanonicalNode(),getArgument(disjunctIndex,1).getCanonicalNode(),getArgument(disjunctIndex,2).getCanonicalNode()))
                        return true;
                    break;
                }
                // fall through!
            default:
                throw new IllegalStateException("Invalid arity of DL-predicate.");
            }
        }
        return false;
    }
    @SuppressWarnings("fallthrough")
    public boolean addDisjunctToTableau(Tableau tableau,int disjunctIndex,DependencySet dependencySet) {
        DLPredicate dlPredicate=getDLPredicate(disjunctIndex);
        switch (dlPredicate.getArity()) {
        case 1:
            dependencySet=getArgument(disjunctIndex,0).addCacnonicalNodeDependencySet(dependencySet);
            return tableau.getExtensionManager().addAssertion(dlPredicate,getArgument(disjunctIndex,0).getCanonicalNode(),dependencySet,isCore(disjunctIndex));
        case 2:
            dependencySet=getArgument(disjunctIndex,0).addCacnonicalNodeDependencySet(dependencySet);
            dependencySet=getArgument(disjunctIndex,1).addCacnonicalNodeDependencySet(dependencySet);
            return tableau.getExtensionManager().addAssertion(dlPredicate,getArgument(disjunctIndex,0).getCanonicalNode(),getArgument(disjunctIndex,1).getCanonicalNode(),dependencySet,isCore(disjunctIndex));
        case 3:
            if (dlPredicate instanceof AnnotatedEquality) {
                dependencySet=getArgument(disjunctIndex,0).addCacnonicalNodeDependencySet(dependencySet);
                dependencySet=getArgument(disjunctIndex,1).addCacnonicalNodeDependencySet(dependencySet);
                dependencySet=getArgument(disjunctIndex,2).addCacnonicalNodeDependencySet(dependencySet);
                return tableau.getExtensionManager().addAnnotatedEquality((AnnotatedEquality)dlPredicate,getArgument(disjunctIndex,0).getCanonicalNode(),getArgument(disjunctIndex,1).getCanonicalNode(),getArgument(disjunctIndex,2).getCanonicalNode(),dependencySet);
            }
            // fall through!
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
            else if (dlPredicate instanceof AnnotatedEquality) {
                AnnotatedEquality annotatedEquality=(AnnotatedEquality)dlPredicate;
                buffer.append('[');
                buffer.append(getArgument(disjunctIndex,0).getNodeID());
                buffer.append(" == ");
                buffer.append(getArgument(disjunctIndex,1).getNodeID());
                buffer.append("]@atMost(");
                buffer.append(annotatedEquality.getCaridnality());
                buffer.append(' ');
                buffer.append(annotatedEquality.getOnRole().toString(prefixes));
                buffer.append(' ');
                buffer.append(annotatedEquality.getToConcept().toString(prefixes));
                buffer.append(")(");
                buffer.append(getArgument(disjunctIndex,2).getNodeID());
                buffer.append(')');
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
