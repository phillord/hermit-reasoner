package org.semanticweb.HermiT.blocking.core;

import java.util.Iterator;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.InterningManager;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.Role;

public class AtMostConjunctionConcept extends Concept implements DLPredicate {
    private static final long serialVersionUID = 1125030135583676765L;
    protected final Role m_onRole;
    protected final Set<AtomicNegationConcept> m_toConcepts;
    
    protected AtMostConjunctionConcept(Role onRole,Set<AtomicNegationConcept> toConcepts) {
        m_onRole=onRole;
        m_toConcepts=toConcepts;
    }
    public Role getOnRole() {
        return m_onRole;
    }
    public Set<AtomicNegationConcept> getToConcept() {
        return m_toConcepts;
    }
    public int getArity() {
        return 1;
    }
    public boolean isAlwaysTrue() {
        return false;
    }
    public boolean isAlwaysFalse() {
        for (LiteralConcept c : m_toConcepts) {
            if (!c.isAlwaysFalse()) return false;
        }
        return true;
    }
    public String toString(Prefixes prefixes) {
        String s="atMost0 ("+m_onRole.toString(prefixes)+' ';
        boolean first=true; 
        for (LiteralConcept c : m_toConcepts) {
            if (!first) s+=" /\\ ";
            s+=c.toString(prefixes);
            first=false;
        }
        s+=')';
        return s;
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtMostConjunctionConcept> s_interningManager=new InterningManager<AtMostConjunctionConcept>() {
        protected boolean equal(AtMostConjunctionConcept object1,AtMostConjunctionConcept object2) {
            if (object1.m_onRole!=object2.m_onRole || object1.m_toConcepts.size()!=object2.m_toConcepts.size()) return false;
            if (object1.m_toConcepts.size()==1 && object2.m_toConcepts.size()==1) 
                return object1.m_toConcepts.iterator().next()==object2.m_toConcepts.iterator().next();
            Iterator<AtomicNegationConcept> i1=object1.m_toConcepts.iterator();
            while(i1.hasNext()) {
                if (!object2.m_toConcepts.contains(i1.next())) return false;
            }
            return true;
        }
        protected int getHashCode(AtMostConjunctionConcept object) {
            return (object.m_onRole.hashCode())*13+object.m_toConcepts.hashCode();
        }
    };
    
    public static AtMostConjunctionConcept create(Role onRole,Set<AtomicNegationConcept> toConcepts) {
        return s_interningManager.intern(new AtMostConjunctionConcept(onRole,toConcepts));
    }
}
