// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Collection;

import org.semanticweb.HermiT.*;

/**
 * Represents a DL ontology as a set of rules.
 */
public class DLOntology implements Serializable {
    private static final long serialVersionUID=3189937959595369812L;
    protected static final String CRLF=System.getProperty("line.separator");

    protected final String m_ontologyURI;
    protected final Set<DLClause> m_dlClauses;
    protected final Set<Atom> m_positiveFacts;
    protected final Set<Atom> m_negativeFacts;
    protected final boolean m_hasInverseRoles;
    protected final boolean m_hasAtMostRestrictions;
    protected final boolean m_hasNominals;
    protected final boolean m_canUseNIRule;
    protected final boolean m_isHorn;
    protected final Set<AtomicConcept> m_allAtomicConcepts;
    protected final Set<DescriptionGraph> m_allDescriptionGraphs;

    public DLOntology(String ontologyURI,Set<DLClause> dlClauses,Set<Atom> positiveFacts,Set<Atom> negativeFacts,boolean hasInverseRoles,boolean hasAtMostRestrictions,boolean hasNominals,boolean canUseNIRule) {
        m_ontologyURI=ontologyURI;
        m_dlClauses=dlClauses;
        m_positiveFacts=positiveFacts;
        m_negativeFacts=negativeFacts;
        m_hasInverseRoles=hasInverseRoles;
        m_hasAtMostRestrictions=hasAtMostRestrictions;
        m_canUseNIRule=canUseNIRule;
        m_hasNominals=hasNominals;
        m_allAtomicConcepts=new TreeSet<AtomicConcept>(AtomicConceptComparator.INSTANCE);
        m_allDescriptionGraphs=new HashSet<DescriptionGraph>();
        boolean isHorn=true;
        for (DLClause dlClause : m_dlClauses) {
            if (dlClause.getHeadLength()>1)
                isHorn=false;
            for (int bodyIndex=dlClause.getBodyLength()-1;bodyIndex>=0;--bodyIndex) {
                DLPredicate dlPredicate=dlClause.getBodyAtom(bodyIndex).getDLPredicate();
                addDLPredicate(dlPredicate);
            }
            for (int headIndex=dlClause.getHeadLength()-1;headIndex>=0;--headIndex) {
                DLPredicate dlPredicate=dlClause.getHeadAtom(headIndex).getDLPredicate();
                addDLPredicate(dlPredicate);
            }
        }
        for (Atom atom : m_positiveFacts)
            addDLPredicate(atom.getDLPredicate());
        for (Atom atom : m_negativeFacts)
            addDLPredicate(atom.getDLPredicate());
        m_isHorn=isHorn;
    }
    protected void addDLPredicate(DLPredicate dlPredicate) {
        if (dlPredicate instanceof AtomicConcept)
            m_allAtomicConcepts.add((AtomicConcept)dlPredicate);
        else if (dlPredicate instanceof AtLeastAbstractRoleConcept) {
            LiteralConcept literalConcept=((AtLeastAbstractRoleConcept)dlPredicate).getToConcept();
            if (literalConcept instanceof AtomicConcept)
                m_allAtomicConcepts.add((AtomicConcept)literalConcept);
        }
        else if (dlPredicate instanceof DescriptionGraph)
            m_allDescriptionGraphs.add((DescriptionGraph)dlPredicate);
        else if (dlPredicate instanceof ExistsDescriptionGraph)
            m_allDescriptionGraphs.add(((ExistsDescriptionGraph)dlPredicate).getDescriptionGraph());
    }
    public String getOntologyURI() {
        return m_ontologyURI;
    }
    public Set<AtomicConcept> getAllAtomicConcepts() {
        return m_allAtomicConcepts;
    }
    public Set<DescriptionGraph> getAllDescriptionGraphs() {
        return m_allDescriptionGraphs;
    }
    public Set<DLClause> getDLClauses() {
        return m_dlClauses;
    }
    public Set<Atom> getPositiveFacts() {
        return m_positiveFacts;
    }
    public Set<Atom> getNegativeFacts() {
        return m_negativeFacts;
    }
    public boolean hasInverseRoles() {
        return m_hasInverseRoles;
    }
    public boolean hasAtMostRestrictions() {
        return m_hasAtMostRestrictions;
    }
    public boolean hasNominals() {
        return m_hasNominals;
    }
    public boolean canUseNIRule() {
        return m_canUseNIRule;
    }
    public boolean isHorn() {
        return m_isHorn;
    }
    public Collection<DLClause> getNonadmissibleDLClauses() {
        Set<AtomicConcept> bodyOnlyAtomicConcepts=getBodyOnlyAtomicConcepts();
        Collection<DLClause> nonadmissibleDLClauses=new HashSet<DLClause>();
        Set<AtomicAbstractRole> graphAtomicRoles=computeGraphAtomicRoles();
        for (DLClause dlClause : m_dlClauses) {
            int usedRoleTypes=getUsedRoleTypes(dlClause,graphAtomicRoles);
            switch (usedRoleTypes) {
            case 0:
            case 2:
                if (!isTreeDLClause(dlClause,graphAtomicRoles,bodyOnlyAtomicConcepts))
                    nonadmissibleDLClauses.add(dlClause);
                break;
            case 1:
                if (!isGraphDLClause(dlClause))
                    nonadmissibleDLClauses.add(dlClause);
                break;
            case 3:
                nonadmissibleDLClauses.add(dlClause);
                break;
            }
        }
        return nonadmissibleDLClauses;
    }
    protected Set<AtomicConcept> getBodyOnlyAtomicConcepts() {
        Set<AtomicConcept> bodyOnlyAtomicConcepts=new HashSet<AtomicConcept>(m_allAtomicConcepts);
        for (DLClause dlClause : m_dlClauses)
            for (int headIndex=0;headIndex<dlClause.getHeadLength();headIndex++) {
                DLPredicate dlPredicate=dlClause.getHeadAtom(headIndex).getDLPredicate();
                bodyOnlyAtomicConcepts.remove(dlPredicate);
                if (dlPredicate instanceof AtLeastAbstractRoleConcept)
                    bodyOnlyAtomicConcepts.remove(((AtLeastAbstractRoleConcept)dlPredicate).getToConcept());
            }
        return bodyOnlyAtomicConcepts;
    }
    protected Set<AtomicAbstractRole> computeGraphAtomicRoles() {
        Set<AtomicAbstractRole> graphAtomicRoles=new HashSet<AtomicAbstractRole>();
        for (DescriptionGraph descriptionGraph : m_allDescriptionGraphs)
            for (int edgeIndex=0;edgeIndex<descriptionGraph.getNumberOfEdges();edgeIndex++) {
                DescriptionGraph.Edge edge=descriptionGraph.getEdge(edgeIndex);
                graphAtomicRoles.add(edge.getAtomicAbstractRole());
            }
        boolean change=true;
        while (change) {
            change=false;
            for (DLClause dlClause : m_dlClauses)
                if (containsAtomicAbstractRoles(dlClause,graphAtomicRoles))
                    if (addAtomicAbstractRoles(dlClause,graphAtomicRoles))
                       change=true;
        }
        return graphAtomicRoles;
    }
    protected boolean containsAtomicAbstractRoles(DLClause dlClause,Set<AtomicAbstractRole> abstractRoles) {
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getBodyAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicAbstractRole && abstractRoles.contains(dlPredicate))
                return true;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getHeadAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicAbstractRole && abstractRoles.contains(dlPredicate))
                return true;
        }
        return false;
    }
    protected boolean addAtomicAbstractRoles(DLClause dlClause,Set<AtomicAbstractRole> abstractRoles) {
        boolean change=false;
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getBodyAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicAbstractRole)
                if (abstractRoles.add((AtomicAbstractRole)dlPredicate))
                    change=true;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getHeadAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicAbstractRole)
                if (abstractRoles.add((AtomicAbstractRole)dlPredicate))
                    change=true;
        }
        return change;
    }
    protected int getUsedRoleTypes(DLClause dlClause,Set<AtomicAbstractRole> graphAtomicRoles) {
        int usedRoleTypes=0; // 0 - DL-clause contains no roles, 1 - DL-clause contains only graph roles, 2 - DL-clause contains only tree roles, 3 - DL-clause contains both graph and tree roles 
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getBodyAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicAbstractRole) {
                if (usedRoleTypes==0)
                    usedRoleTypes=(graphAtomicRoles.contains(dlPredicate) ? 1 : 2);
                else {
                    if (usedRoleTypes==1) {
                        if (!graphAtomicRoles.contains(dlPredicate))
                            return 3;
                    }
                    else {
                        if (graphAtomicRoles.contains(dlPredicate))
                            return 3;
                    }
                }
            }
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getHeadAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicAbstractRole) {
                if (usedRoleTypes==0)
                    usedRoleTypes=(graphAtomicRoles.contains(dlPredicate) ? 1 : 2);
                else {
                    if (usedRoleTypes==1) {
                        if (!graphAtomicRoles.contains(dlPredicate))
                            return 3;
                    }
                    else {
                        if (graphAtomicRoles.contains(dlPredicate))
                            return 3;
                    }
                }
            }
        }
        return usedRoleTypes;
    }
    protected boolean isTreeDLClause(DLClause dlClause,Set<AtomicAbstractRole> graphAtomicRoles,Set<AtomicConcept> bodyOnlyAtomicConcepts) {
        Set<Variable> variables=new HashSet<Variable>();
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            Atom atom=dlClause.getBodyAtom(atomIndex);
            atom.getVariables(variables);
            DLPredicate dlPredicate=atom.getDLPredicate();
            if (!(dlPredicate instanceof AtomicAbstractRole) && !(dlPredicate instanceof AtomicConcept) && !dlPredicate.equals(NodeIDLessThan.INSTANCE))
                return false;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            Atom atom=dlClause.getHeadAtom(atomIndex);
            atom.getVariables(variables);
            DLPredicate dlPredicate=atom.getDLPredicate();
            if (!(dlPredicate instanceof AtomicAbstractRole) && !(dlPredicate instanceof AtomicConcept) && !(dlPredicate instanceof ExistentialConcept) && !Equality.INSTANCE.equals(dlPredicate))
                return false;
            if (dlPredicate instanceof AtLeastAbstractRoleConcept) {
                AtLeastAbstractRoleConcept atLeastAbstractRoleConcept=(AtLeastAbstractRoleConcept)dlPredicate;
                if (graphAtomicRoles.contains(atLeastAbstractRoleConcept.getOnAbstractRole()))
                    return false;
            }
        }
        Variable X=Variable.create("X");
        if (variables.contains(X) && isTreeWithCenterVariable(dlClause,X,bodyOnlyAtomicConcepts))
            return true;
        for (Variable centerVariable : variables)
            if (isTreeWithCenterVariable(dlClause,centerVariable,bodyOnlyAtomicConcepts))
                return true;
        return false;
    }
    protected boolean isTreeWithCenterVariable(DLClause dlClause,Variable centerVariable,Set<AtomicConcept> bodyOnlyAtomicConcepts) {
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            Atom atom=dlClause.getBodyAtom(atomIndex);
            if (atom.getDLPredicate() instanceof AtomicAbstractRole && !isTreeRoleAtom(atom,centerVariable))
                return false;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            Atom atom=dlClause.getHeadAtom(atomIndex);
            if (atom.getDLPredicate() instanceof AtomicAbstractRole && !isTreeRoleAtom(atom,centerVariable))
                return false;
            if (Equality.INSTANCE.equals(atom.getDLPredicate())) {
                Variable otherVariable=null;
                if (centerVariable.equals(atom.getArgument(0))) {
                    otherVariable=atom.getArgumentVariable(1);
                    if (otherVariable==null)
                        return false;
                }
                else if (centerVariable.equals(atom.getArgument(1))) {
                    otherVariable=atom.getArgumentVariable(0);
                    if (otherVariable==null)
                        return false;
                }
                if (otherVariable!=null) {
                    boolean found=false;
                    for (int bodyAtomIndex=0;bodyAtomIndex<dlClause.getBodyLength();bodyAtomIndex++) {
                        Atom bodyAtom=dlClause.getBodyAtom(bodyAtomIndex);
                        if (bodyAtom.getArity()==1 && bodyAtom.getArgument(0).equals(otherVariable) && bodyOnlyAtomicConcepts.contains(bodyAtom.getDLPredicate())) {
                            found=true;
                            break;
                        }
                    }
                    if (!found)
                        return false;
                }
            }
        }
        return true;
    }
    protected boolean isTreeRoleAtom(Atom atom,Variable centerVariable) {
        if (centerVariable.equals(atom.getArgumentVariable(0)) && atom.getArgument(1) instanceof Variable && !centerVariable.equals(atom.getArgument(1)))
            return true;
        if (centerVariable.equals(atom.getArgumentVariable(1)) && atom.getArgument(0) instanceof Variable && !centerVariable.equals(atom.getArgument(0)))
            return true;
        return false;
    }
    protected boolean isGraphDLClause(DLClause dlClause) {
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getBodyAtom(atomIndex).getDLPredicate();
            if (!(dlPredicate instanceof AtomicAbstractRole) && !(dlPredicate instanceof AtomicConcept))
                return false;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getHeadAtom(atomIndex).getDLPredicate();
            if (!(dlPredicate instanceof AtomicAbstractRole) && !(dlPredicate instanceof AtomicConcept) && !Equality.INSTANCE.equals(dlPredicate))
                return false;
        }
        return true;
    }
    public String toString(Namespaces namespaces) {
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("DL-clauses: [");
        stringBuffer.append(CRLF);
        for (DLClause dlClause : m_dlClauses) {
            stringBuffer.append("  ");
            stringBuffer.append(dlClause.toString(namespaces));
            stringBuffer.append(CRLF);
        }
        stringBuffer.append("]");
        stringBuffer.append(CRLF);
        stringBuffer.append("ABox: [");
        stringBuffer.append(CRLF);
        for (Atom atom : m_positiveFacts) {
            stringBuffer.append("  ");
            stringBuffer.append(atom.toString(namespaces));
            stringBuffer.append(CRLF);
        }
        for (Atom atom : m_negativeFacts) {
            stringBuffer.append("  !");
            stringBuffer.append(atom.toString(namespaces));
            stringBuffer.append(CRLF);
        }
        stringBuffer.append("]");
        return stringBuffer.toString();
    }
    public String toString() {
        return toString(Namespaces.INSTANCE);
    }
    public void save(File file) throws IOException {
        OutputStream outputStream=new BufferedOutputStream(new FileOutputStream(file));
        try {
            save(outputStream);
        }
        finally {
            outputStream.close();
        }
    }
    public void save(OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.flush();
    }
    public static DLOntology load(InputStream inputStream) throws IOException {
        try {
            ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
            return (DLOntology)objectInputStream.readObject();
        }
        catch (ClassNotFoundException e) {
            IOException error=new IOException();
            error.initCause(e);
            throw error;
        }
    }
    public static DLOntology load(File file) throws IOException {
        InputStream inputStream=new BufferedInputStream(new FileInputStream(file));
        try {
            return load(inputStream);
        }
        finally {
            inputStream.close();
        }
    }
    
    public static class AtomicConceptComparator implements Serializable,Comparator<AtomicConcept> {
        private static final long serialVersionUID=2386841732225838685L;
        public static final Comparator<AtomicConcept> INSTANCE=new AtomicConceptComparator();

        public int compare(AtomicConcept o1,AtomicConcept o2) {
            return o1.getURI().compareTo(o2.getURI());
        }
        protected Object readResolve() {
            return INSTANCE;
        }
    }
}
