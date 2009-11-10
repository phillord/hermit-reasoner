package org.semanticweb.HermiT.blocking.core;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.InterningManager;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.Role;

public class AtMostDisjunctionConcept extends Concept implements DLPredicate {
    private static final long serialVersionUID = 1125030135583676765L;
    protected final Role m_onRole;
    protected final AtomicConcept[] m_toConcepts;
    
    protected AtMostDisjunctionConcept(Role onRole,AtomicConcept[] toConcepts) {
        m_onRole=onRole;
        m_toConcepts=toConcepts;
    }
    public Role getOnRole() {
        return m_onRole;
    }
    public AtomicConcept[] getToConcept() {
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
        String s="atMost 0 ("+m_onRole.toString(prefixes)+' ';
        boolean first=true; 
        for (LiteralConcept c : m_toConcepts) {
            if (!first) s+=" \\/ ";
            s+=c.toString(prefixes);
            first=false;
        }
        s+=')';
        return s;
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtMostDisjunctionConcept> s_interningManager=new InterningManager<AtMostDisjunctionConcept>() {
        protected boolean equal(AtMostDisjunctionConcept object1,AtMostDisjunctionConcept object2) {
            if (object1.m_onRole!=object2.m_onRole || object1.m_toConcepts.length!=object2.m_toConcepts.length) return false;
            if (object1.m_toConcepts.length==1 && object2.m_toConcepts.length==1) 
                return object1.m_toConcepts[0]==object2.m_toConcepts[0];
            for (int i=0;i<object1.m_toConcepts.length;i++) {
                boolean contained=false;
                for (int j=0;!contained&&j<object2.m_toConcepts.length;j++)     
                    if (object2.m_toConcepts[j]==object1.m_toConcepts[i]) contained=true;
                if (!contained) return false;
            }
            return true;
        }
        protected int getHashCode(AtMostDisjunctionConcept object) {
            return (object.m_onRole.hashCode())*13+object.m_toConcepts.hashCode();
        }
    };
    
    public static AtMostDisjunctionConcept create(Role onRole,AtomicConcept[] toConcepts) {
        return s_interningManager.intern(new AtMostDisjunctionConcept(onRole,toConcepts));
    }
}
