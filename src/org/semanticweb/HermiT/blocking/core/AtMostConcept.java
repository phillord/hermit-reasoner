package org.semanticweb.HermiT.blocking.core;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.InterningManager;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.Role;

public class AtMostConcept extends Concept implements DLPredicate {
    private static final long serialVersionUID = 1125030135583676765L;
    protected final int m_number;
    protected final Role m_onRole;
    protected final LiteralConcept m_toConcept;
    
    protected AtMostConcept(int number,Role onRole,LiteralConcept toConcept) {
        m_number=number;
        m_onRole=onRole;
        m_toConcept=toConcept;
    }
    public int getNumber() {
        return m_number;
    }
    public Role getOnRole() {
        return m_onRole;
    }
    public LiteralConcept getToConcept() {
        return m_toConcept;
    }
    public int getArity() {
        return 1;
    }
    public boolean isAlwaysTrue() {
        return false;
    }
    public boolean isAlwaysFalse() {
        return m_toConcept.isAlwaysFalse();
    }
    public String toString(Prefixes prefixes) {
        return "atMost("+m_number+' '+m_onRole.toString(prefixes)+' '+m_toConcept.toString(prefixes)+')';
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtMostConcept> s_interningManager=new InterningManager<AtMostConcept>() {
        protected boolean equal(AtMostConcept object1,AtMostConcept object2) {
            return object1.m_number==object2.m_number && object1.m_onRole==object2.m_onRole && object1.m_toConcept==object2.m_toConcept;
        }
        protected int getHashCode(AtMostConcept object) {
            return (object.m_number*7+object.m_onRole.hashCode())*13+object.m_toConcept.hashCode();
        }
    };
    
    public static AtMostConcept create(int number,Role onRole,LiteralConcept toConcept) {
        return s_interningManager.intern(new AtMostConcept(number,onRole,toConcept));
    }
}
