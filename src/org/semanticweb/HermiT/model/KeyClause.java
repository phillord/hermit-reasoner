package org.semanticweb.HermiT.model;

public class KeyClause extends DLClause {

    protected KeyClause(Atom[] headAtoms, Atom[] bodyAtoms) {
        super(headAtoms, bodyAtoms);
    }

    private static final long serialVersionUID = 5982147362803744560L;

    public boolean isKeyClause() {
        return true;
    }
    
    public static DLClause create(Atom[] headAtoms, Atom[] bodyAtoms) {
        return s_interningManager.intern(new KeyClause(headAtoms, bodyAtoms));
    }
}
