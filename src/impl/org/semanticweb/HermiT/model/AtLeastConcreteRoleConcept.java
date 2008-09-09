// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Namespaces;

/**
 * Represents an at-least data property concept.
 */
public class AtLeastConcreteRoleConcept extends ExistentialConcept implements
        DLPredicate {

    private static final long serialVersionUID = -9188905009088347624L;

    protected final int number;
    protected final AtomicConcreteRole onAtomicConcreteRole;
    protected final DataRange toDataRange;

    protected AtLeastConcreteRoleConcept(int number,
            AtomicConcreteRole onAtomicConcreteRole, DataRange toDataRange) {
        this.number = number;
        this.onAtomicConcreteRole = onAtomicConcreteRole;
        this.toDataRange = toDataRange;
    }

    public int getNumber() {
        return number;
    }

    public AtomicConcreteRole getOnAtomicConcreteRole() {
        return onAtomicConcreteRole;
    }

    public DataRange getToDataRange() {
        return toDataRange;
    }

    public int getArity() {
        return 1;
    }

    public String toString(Namespaces namespaces) {
        return "atLeast(" + number + ' ' + onAtomicConcreteRole.toString(namespaces)
                + ' ' + toDataRange.toString(namespaces) + ')';
    }

    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtLeastConcreteRoleConcept> s_interningManager = new InterningManager<AtLeastConcreteRoleConcept>() {
        protected boolean equal(AtLeastConcreteRoleConcept object1,
                AtLeastConcreteRoleConcept object2) {
            return object1.getNumber() == object2.getNumber()
                    && object1.getOnAtomicConcreteRole() == object2.getOnAtomicConcreteRole()
                    && object1.getToDataRange() == object2.getToDataRange();
        }

        protected int getHashCode(AtLeastConcreteRoleConcept object) {
            return (object.getNumber() * 7 + object.getOnAtomicConcreteRole().hashCode())
                    * 7 + object.getToDataRange().hashCode();
        }
    };

    public static AtLeastConcreteRoleConcept create(int number,
            AtomicConcreteRole onAbstractDataProperty, DataRange toDataRange) {
        return s_interningManager.intern(new AtLeastConcreteRoleConcept(number,
                onAbstractDataProperty, toDataRange));
    }
}
