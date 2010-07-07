package org.semanticweb.HermiT.tableau;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Term;

public class ReasoningTaskDescription {
    public static enum StandardTestType {
        CONCEPT_SATISFIABILITY("satisfiability of concept '{0}'"),
        CONSISTENCY("ABox satisfiability"),
        CONCEPT_SUBSUMPTION("concept subsumption '{0}' => '{1}'"),
        OBJECT_ROLE_SATISFIABILITY("satisfiability of object role '{0}'"),
        DATA_ROLE_SATISFIABILITY("satisfiability of data role '{0}'"),
        OBJECT_ROLE_SUBSUMPTION("object role subsumption '{0}' => '{1}'"),
        DATA_ROLE_SUBSUMPTION("data role subsumption '{0}' => '{1}'"),
        INSTANCE_OF("class instance '{0}'('{1}')"),
        OBJECT_ROLE_INSTANCE_OF("object role instance '{0}'('{1}', '{2}')"),
        DATA_ROLE_INSTANCE_OF("data role instance '{0}'('{1}', '{2}')"),
        ENTAILMENT("entailment of '{0}'"),
        DOMAIN("check if {0} is domain of {1}"),
        RANGE("check if {0} is range of {1}");
        
        public final String messagePattern; 
        StandardTestType(String messagePattern) {
            this.messagePattern=messagePattern;
        } 
    }
    protected final boolean m_flipSatisfiabilityResult;
    protected final String m_messagePattern;
    protected final Object[] m_arguments;

    public ReasoningTaskDescription(boolean flipSatisfiabilityResult,StandardTestType testType,Object... arguments) {
        this(flipSatisfiabilityResult,testType.messagePattern,arguments);
    }
    public ReasoningTaskDescription(boolean flipSatisfiabilityResult,String message,Object... arguments) {
        m_flipSatisfiabilityResult=flipSatisfiabilityResult;
        m_messagePattern=message;
        m_arguments=arguments;
    }
    public boolean flipSatisfiabilityResult() {
        return m_flipSatisfiabilityResult;
    }
    public String getTaskDescription(Prefixes prefixes) {
        String result=m_messagePattern;
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
    public String getMessagePattern() {
        return m_messagePattern;
    }
    public String toString() {
        return getTaskDescription(Prefixes.STANDARD_PREFIXES);
    }
    
    public static ReasoningTaskDescription isABoxSatisfiable() {
        return new ReasoningTaskDescription(false,StandardTestType.CONSISTENCY);
    }
    public static ReasoningTaskDescription isConceptSatisfiable(Object atomicConcept) {
        return new ReasoningTaskDescription(false,StandardTestType.CONCEPT_SATISFIABILITY,atomicConcept);
    }
    public static ReasoningTaskDescription isConceptSubsumedBy(Object atomicSubconcept,Object atomicSuperconcept) {
        return new ReasoningTaskDescription(true,StandardTestType.CONCEPT_SUBSUMPTION,atomicSubconcept,atomicSuperconcept);
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
    public static ReasoningTaskDescription isRoleSubsumedByList(Object subrole,Object... superroles) {
        StringBuffer message=new StringBuffer();
        message.append("satisiability of role '{0}' ");
        for (int index=0;index<superroles.length;index++) {
            message.append(" and not({");
            message.append(index+1);
            message.append("})");
        }
        Object[] arguments=new Object[superroles.length+1];
        arguments[0]=subrole;
        System.arraycopy(superroles,0,arguments,1,superroles.length);
        return new ReasoningTaskDescription(false,message.toString(),arguments);
    }
    public static ReasoningTaskDescription isRoleSatisfiable(Object role,boolean isObjectRole) {
        return new ReasoningTaskDescription(false,(isObjectRole ? StandardTestType.OBJECT_ROLE_SATISFIABILITY : StandardTestType.DATA_ROLE_SATISFIABILITY),role);
    }
    public static ReasoningTaskDescription isRoleSubsumedBy(Object subrole,Object superrole,boolean isObjectRole) {
        return new ReasoningTaskDescription(true,(isObjectRole ? StandardTestType.OBJECT_ROLE_SUBSUMPTION : StandardTestType.DATA_ROLE_SUBSUMPTION),subrole,superrole);
    }
    public static ReasoningTaskDescription isInstanceOf(Object atomicConcept,Object individual) {
        return new ReasoningTaskDescription(true,StandardTestType.INSTANCE_OF,atomicConcept,individual);
    }
    public static ReasoningTaskDescription isObjectRoleInstanceOf(Object atomicRole,Object individual1,Object individual2) {
        return new ReasoningTaskDescription(true,StandardTestType.OBJECT_ROLE_INSTANCE_OF,atomicRole,individual1,individual2);
    }
    public static ReasoningTaskDescription isDataRoleInstanceOf(Object atomicRole,Object individual1,Object individual2) {
        return new ReasoningTaskDescription(true,StandardTestType.DATA_ROLE_INSTANCE_OF,atomicRole,individual1,individual2);
    }
    public static ReasoningTaskDescription isAxiomEntailed(Object axiom) {
        return new ReasoningTaskDescription(true,StandardTestType.ENTAILMENT,axiom);
    }
    public static ReasoningTaskDescription isDomainOf(Object domain,Object role) {
        return new ReasoningTaskDescription(true,StandardTestType.DOMAIN,domain,role);
    }
    public static ReasoningTaskDescription isRangeOf(Object range,Object role) {
        return new ReasoningTaskDescription(true,StandardTestType.RANGE,range,role);
    }
}
