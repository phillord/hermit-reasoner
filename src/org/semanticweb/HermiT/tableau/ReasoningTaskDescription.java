package org.semanticweb.HermiT.tableau;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Term;

public class ReasoningTaskDescription {
    protected final boolean m_flipSatisfiabilityResult;
    protected final String m_message;
    protected final Object[] m_arguments;

    public ReasoningTaskDescription(boolean flipSatisfiabilityResult,String message,Object... arguments) {
        m_flipSatisfiabilityResult=flipSatisfiabilityResult;
        m_message=message;
        m_arguments=arguments;
    }
    public boolean flipSatisfiabilityResult() {
        return m_flipSatisfiabilityResult;
    }
    public String getTaskDescription(Prefixes prefixes) {
        String result=m_message;
        for (int argumentIndex=0;argumentIndex<m_arguments.length;argumentIndex++) {
            Object argument=m_arguments[argumentIndex];
            String argumentString;
            if (argument instanceof DLPredicate)
                argumentString=((DLPredicate)argument).toString(prefixes);
            else if (argument instanceof Role)
                argumentString=((Role)argument).toString(prefixes);
            else if (argument instanceof Concept)
                argumentString=((Concept)argument).toString(prefixes);
            else if (argument instanceof Term)
                argumentString=((Term)argument).toString(prefixes);
            else
                argumentString=argument.toString();
            result=result.replace("{"+argumentIndex+"}",argumentString);
        }
        return result;
    }
    public static ReasoningTaskDescription isABoxSatisfiable() {
        return new ReasoningTaskDescription(false,"ABox satisfiability");
    }
    public static ReasoningTaskDescription isConceptSatisfiable(Object atomicConcept) {
        return new ReasoningTaskDescription(false,"satisfiability of concept '{0}'",atomicConcept);
    }
    public static ReasoningTaskDescription isConceptSubsumedBy(Object atomicSubconcept,Object atomicSuperconcept) {
        return new ReasoningTaskDescription(true,"concept subsumption '{0}' => '{1}'",atomicSubconcept,atomicSuperconcept);
    }
    public static ReasoningTaskDescription isConceptSubsumedByList(Object atomicSubconcept,Object... atomicSuperconcepts) {
        StringBuffer message=new StringBuffer();
        message.append("satisiability of concept '{0}' ");
        for (int index=0;index<atomicSuperconcepts.length;index++) {
            message.append(" and not({");
            message.append(index+1);
            message.append("})");
        }
        Object[] arguments=new Object[atomicSuperconcepts.length+1];
        arguments[0]=atomicSubconcept;
        System.arraycopy(atomicSuperconcepts,0,arguments,1,atomicSuperconcepts.length);
        return new ReasoningTaskDescription(false,message.toString(),arguments);
    }
    public static ReasoningTaskDescription isRoleSatisfiable(Object role,boolean isObjectRole) {
        return new ReasoningTaskDescription(false,"satisfiability of "+(isObjectRole ? "object" : "data")+" role '{0}'",role);
    }
    public static ReasoningTaskDescription isRoleSubsumedBy(Object subrole,Object superrole,boolean isObjectRole) {
        return new ReasoningTaskDescription(true,(isObjectRole ? "object" : "data")+" role subsumptioin '{0}' => '{1}'",subrole,superrole);
    }
    public static ReasoningTaskDescription isInstanceOf(Object individual,Object atomicConcept) {
        return new ReasoningTaskDescription(true,"instantiation '{0}' => '{1}'",individual,atomicConcept);
    }
    public static ReasoningTaskDescription isAxiomEntailed(Object axiom) {
        return new ReasoningTaskDescription(true,"entailment of '{0}'",axiom);
    }
    public static ReasoningTaskDescription isSameAs(Object individual1,Object individual2) {
        return new ReasoningTaskDescription(true,"is {0} same as {1}",individual1,individual2);
    }
}
