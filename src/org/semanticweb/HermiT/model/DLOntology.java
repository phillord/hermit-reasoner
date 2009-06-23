// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;

import rationals.Automaton;

/**
 * Represents a DL ontology as a set of rules.
 */
public class DLOntology implements Serializable {
    private static final long serialVersionUID=3189937959595369812L;
    
    protected static final String CRLF=System.getProperty("line.separator");
    protected static final int CONTAINS_NO_ROLES=0;
    protected static final int CONTAINS_ONLY_GRAPH_ROLES=1;
    protected static final int CONTAINS_ONLY_TREE_ROLES=2;
    protected static final int CONTAINS_GRAPH_AND_TREE_ROLES=3;

    protected final String m_ontologyIRI;
    protected final Set<DLClause> m_dlClauses;
    protected final Set<Atom> m_positiveFacts;
    protected final Set<Atom> m_negativeFacts;
    protected final boolean m_hasInverseRoles;
    protected final boolean m_hasAtMostRestrictions;
    protected final boolean m_hasNominals;
    protected final boolean m_hasDatatypes;
    protected final boolean m_isHorn;
    protected final Set<AtomicConcept> m_allAtomicConcepts;
    protected final int m_numberOfExternalConcepts;
    protected final Set<ComplexObjectRoleInclusion> m_allComplexObjectRoleInclusions;
    protected final Set<AtomicRole> m_allAtomicObjectRoles;
    protected final Set<AtomicRole> m_allAtomicDataRoles;
    protected final Set<Individual> m_allIndividuals;
    protected final Set<DescriptionGraph> m_allDescriptionGraphs;
    /**
     * gstoil
     */
    protected final Map<OWLObjectPropertyExpression, Automaton> m_automataOfComplexObjectProperties;
    protected final Map<AtomicConcept,Set<Set<Concept>>> m_unaryValidBlockConditions;
    protected final Map<Set<AtomicConcept>,Set<Set<Concept>>> m_nAryValidBlockConditions;
    
    public DLOntology(String ontologyIRI,Set<DLClause> dlClauses,Set<Atom> positiveFacts,Set<Atom> negativeFacts,
            Set<AtomicConcept> atomicConcepts,Set<ComplexObjectRoleInclusion> allComplexObjectRoleInclusions,
            Set<AtomicRole> atomicObjectRoles,Set<AtomicRole> atomicDataRoles,Set<Individual> individuals,
            boolean hasInverseRoles,boolean hasAtMostRestrictions,boolean hasNominals,boolean hasDatatypes) {
        this(ontologyIRI, dlClauses, positiveFacts, negativeFacts, atomicConcepts, allComplexObjectRoleInclusions, 
                atomicObjectRoles, atomicDataRoles, individuals, hasInverseRoles, hasAtMostRestrictions, hasNominals, 
                hasDatatypes, null, null);
    }
    public DLOntology(String ontologyIRI,Set<DLClause> dlClauses,Set<Atom> positiveFacts,Set<Atom> negativeFacts,
            Set<AtomicConcept> atomicConcepts,Set<ComplexObjectRoleInclusion> allComplexObjectRoleInclusions,
            Set<AtomicRole> atomicObjectRoles,Set<AtomicRole> atomicDataRoles,Set<Individual> individuals,
            boolean hasInverseRoles,boolean hasAtMostRestrictions,boolean hasNominals,boolean hasDatatypes,
            Map<AtomicConcept, Set<Set<Concept>>> unaryValidBlockConditions,Map<Set<AtomicConcept>, 
            Set<Set<Concept>>> nAryValidBlockConditions) {
        m_ontologyIRI=ontologyIRI;
        m_dlClauses=dlClauses;
        m_positiveFacts=positiveFacts;
        m_negativeFacts=negativeFacts;
        m_hasInverseRoles=hasInverseRoles;
        m_hasAtMostRestrictions=hasAtMostRestrictions;
        m_hasNominals=hasNominals;
        m_hasDatatypes=hasDatatypes;
        if (atomicConcepts==null)
            m_allAtomicConcepts=new TreeSet<AtomicConcept>(AtomicConceptComparator.INSTANCE);
        else
            m_allAtomicConcepts=atomicConcepts;
        int numberOfExternalConcepts=0;
        for (AtomicConcept c : m_allAtomicConcepts)
            if (!Prefixes.isInternalIRI(c.getIRI()))
                numberOfExternalConcepts++;
        m_numberOfExternalConcepts=numberOfExternalConcepts;
        if (allComplexObjectRoleInclusions==null)
            m_allComplexObjectRoleInclusions=new HashSet<ComplexObjectRoleInclusion>();
        else
            m_allComplexObjectRoleInclusions=allComplexObjectRoleInclusions;
        m_automataOfComplexObjectProperties = new HashMap<OWLObjectPropertyExpression, Automaton>();
        if (atomicObjectRoles==null)
            m_allAtomicObjectRoles=new TreeSet<AtomicRole>(AtomicRoleComparator.INSTANCE);
        else
            m_allAtomicObjectRoles=atomicObjectRoles;
        if (atomicDataRoles==null)
            m_allAtomicDataRoles=new TreeSet<AtomicRole>(AtomicRoleComparator.INSTANCE);
        else
            m_allAtomicDataRoles=atomicDataRoles;
        if (individuals==null)
            m_allIndividuals=new TreeSet<Individual>(IndividualComparator.INSTANCE);
        else
            m_allIndividuals=individuals;
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
        m_isHorn=isHorn;
        for (Atom atom : m_positiveFacts) {
            addDLPredicate(atom.getDLPredicate());
            for (int i=0;i<atom.getArity();++i) {
                Term argument=atom.getArgument(i);
                if (argument instanceof Individual)
                    m_allIndividuals.add((Individual)argument);
            }
        }
        for (Atom atom : m_negativeFacts) {
            addDLPredicate(atom.getDLPredicate());
            for (int i=0;i<atom.getArity();++i) {
                Term argument=atom.getArgument(i);
                if (argument instanceof Individual)
                    m_allIndividuals.add((Individual)argument);
            }
        }
        if (unaryValidBlockConditions==null) {
            m_unaryValidBlockConditions=new HashMap<AtomicConcept,Set<Set<Concept>>>();
        } else {
            m_unaryValidBlockConditions=unaryValidBlockConditions;
        }
        if (nAryValidBlockConditions==null) {
            m_nAryValidBlockConditions=new HashMap<Set<AtomicConcept>,Set<Set<Concept>>>();   
        } else {
            m_nAryValidBlockConditions=nAryValidBlockConditions;
        }
    }
    protected void addDLPredicate(DLPredicate dlPredicate) {
        if (dlPredicate instanceof AtomicConcept)
            m_allAtomicConcepts.add((AtomicConcept)dlPredicate);
        else if (dlPredicate instanceof AtLeastConcept) {
            LiteralConcept literalConcept=((AtLeastConcept)dlPredicate).getToConcept();
            if (literalConcept instanceof AtomicConcept)
                m_allAtomicConcepts.add((AtomicConcept)literalConcept);
        }
        else if (dlPredicate instanceof DescriptionGraph)
            m_allDescriptionGraphs.add((DescriptionGraph)dlPredicate);
        else if (dlPredicate instanceof ExistsDescriptionGraph)
            m_allDescriptionGraphs.add(((ExistsDescriptionGraph)dlPredicate).getDescriptionGraph());
    }

    public String getOntologyIRI() {
        return m_ontologyIRI;
    }

    public Set<AtomicConcept> getAllAtomicConcepts() {
        return m_allAtomicConcepts;
    }

    public int getNumberOfExternalConcepts() {
        return m_numberOfExternalConcepts;
    }

    public Set<ComplexObjectRoleInclusion> getAllComplexObjectRoleInclusions() {
        return m_allComplexObjectRoleInclusions;
    }

    public Set<AtomicRole> getAllAtomicObjectRoles() {
        return m_allAtomicObjectRoles;
    }

    public Set<AtomicRole> getAllAtomicDataRoles() {
        return m_allAtomicDataRoles;
    }

    public Set<Individual> getAllIndividuals() {
        return m_allIndividuals;
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

    public boolean hasDatatypes() {
        return m_hasDatatypes;
    }
    public Map<AtomicConcept, Set<Set<Concept>>> getUnaryValidBlockConditions() {
        return m_unaryValidBlockConditions;
    }
    public Map<Set<AtomicConcept>, Set<Set<Concept>>> getNAryValidBlockConditions() {
        return m_nAryValidBlockConditions;
    }
    public boolean isHorn() {
        return m_isHorn;
    }

    public Collection<DLClause> getNonadmissibleDLClauses() {
        Set<AtomicConcept> bodyOnlyAtomicConcepts=getBodyOnlyAtomicConcepts();
        Collection<DLClause> nonadmissibleDLClauses=new HashSet<DLClause>();
        Set<AtomicRole> graphAtomicRoles=computeGraphAtomicRoles();
        for (DLClause dlClause : m_dlClauses) {
            // key clauses (from HasKey axioms) are not admissible according to
            // the standard HT admissibility rules
            if (!dlClause.isKnownToBeAdmissible()) {
                int usedRoleTypes=getUsedRoleTypes(dlClause,graphAtomicRoles);
                switch (usedRoleTypes) {
                case CONTAINS_NO_ROLES:
                case CONTAINS_ONLY_TREE_ROLES:
                    if (!isTreeDLClause(dlClause,graphAtomicRoles,bodyOnlyAtomicConcepts))
                        nonadmissibleDLClauses.add(dlClause);
                    break;
                case CONTAINS_ONLY_GRAPH_ROLES:
                    if (!isGraphDLClause(dlClause))
                        nonadmissibleDLClauses.add(dlClause);
                    break;
                case CONTAINS_GRAPH_AND_TREE_ROLES:
                    nonadmissibleDLClauses.add(dlClause);
                    break;
                }
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
                if (dlPredicate instanceof AtLeastConcept)
                    bodyOnlyAtomicConcepts.remove(((AtLeastConcept)dlPredicate).getToConcept());
            }
        return bodyOnlyAtomicConcepts;
    }

    protected Set<AtomicRole> computeGraphAtomicRoles() {
        Set<AtomicRole> graphAtomicRoles=new HashSet<AtomicRole>();
        for (DescriptionGraph descriptionGraph : m_allDescriptionGraphs)
            for (int edgeIndex=0;edgeIndex<descriptionGraph.getNumberOfEdges();edgeIndex++) {
                DescriptionGraph.Edge edge=descriptionGraph.getEdge(edgeIndex);
                graphAtomicRoles.add(edge.getAtomicRole());
            }
        boolean change=true;
        while (change) {
            change=false;
            for (DLClause dlClause : m_dlClauses)
                if (containsAtomicRoles(dlClause,graphAtomicRoles))
                    if (addAtomicRoles(dlClause,graphAtomicRoles))
                        change=true;
        }
        return graphAtomicRoles;
    }

    protected boolean containsAtomicRoles(DLClause dlClause,Set<AtomicRole> roles) {
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getBodyAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicRole && roles.contains(dlPredicate))
                return true;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getHeadAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicRole && roles.contains(dlPredicate))
                return true;
        }
        return false;
    }

    protected boolean addAtomicRoles(DLClause dlClause,Set<AtomicRole> roles) {
        boolean change=false;
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getBodyAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicRole)
                if (roles.add((AtomicRole)dlPredicate))
                    change=true;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getHeadAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicRole)
                if (roles.add((AtomicRole)dlPredicate))
                    change=true;
        }
        return change;
    }

    /**
     * Takes the set of roles that are for use in Description Graphs and detects whether clause contains no roles, only roles from the given set, only roles not from the given set or both types of roles.
     */
    protected int getUsedRoleTypes(DLClause dlClause,Set<AtomicRole> graphAtomicRoles) {
        int usedRoleTypes=CONTAINS_NO_ROLES;
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getBodyAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicRole) {
                if (usedRoleTypes==CONTAINS_NO_ROLES)
                    usedRoleTypes=(graphAtomicRoles.contains(dlPredicate) ? CONTAINS_ONLY_GRAPH_ROLES : CONTAINS_ONLY_TREE_ROLES);
                else {
                    if (usedRoleTypes==CONTAINS_ONLY_GRAPH_ROLES) {
                        if (!graphAtomicRoles.contains(dlPredicate))
                            return CONTAINS_GRAPH_AND_TREE_ROLES;
                    }
                    else {
                        if (graphAtomicRoles.contains(dlPredicate))
                            return CONTAINS_GRAPH_AND_TREE_ROLES;
                    }
                }
            }
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getHeadAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicRole) {
                if (usedRoleTypes==CONTAINS_NO_ROLES)
                    usedRoleTypes=(graphAtomicRoles.contains(dlPredicate) ? CONTAINS_ONLY_GRAPH_ROLES : CONTAINS_ONLY_TREE_ROLES);
                else {
                    if (usedRoleTypes==CONTAINS_ONLY_GRAPH_ROLES) {
                        if (!graphAtomicRoles.contains(dlPredicate))
                            return CONTAINS_GRAPH_AND_TREE_ROLES;
                    }
                    else {
                        if (graphAtomicRoles.contains(dlPredicate))
                            return CONTAINS_GRAPH_AND_TREE_ROLES;
                    }
                }
            }
        }
        return usedRoleTypes;
    }

    /**
     * Tests whether the clause conforms to the properties of HT clauses, i.e., the variables can be split into a center variable x, a set of branch variables y_i, and a set of nominal variables z_j such that certain conditions hold.
     */
    protected boolean isTreeDLClause(DLClause dlClause,Set<AtomicRole> graphAtomicRoles,Set<AtomicConcept> bodyOnlyAtomicConcepts) {
        if (dlClause.getHeadLength()==0 && dlClause.getBodyLength()==0)
            return true;
        Set<Variable> variables=new HashSet<Variable>();
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            Atom atom=dlClause.getBodyAtom(atomIndex);
            atom.getVariables(variables);
            DLPredicate dlPredicate=atom.getDLPredicate();
            if (!(dlPredicate instanceof AtomicRole) && !(dlPredicate instanceof AtomicConcept) && !dlPredicate.equals(NodeIDLessEqualThan.INSTANCE) && !(dlPredicate instanceof NodeIDsAscendingOrEqual))
                return false;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            Atom atom=dlClause.getHeadAtom(atomIndex);
            atom.getVariables(variables);
            DLPredicate dlPredicate=atom.getDLPredicate();
            if (!(dlPredicate instanceof AtomicRole) && !(dlPredicate instanceof AtomicConcept) && !(dlPredicate instanceof DataRange) && !(dlPredicate instanceof ExistentialConcept) && !Equality.INSTANCE.equals(dlPredicate) && !Inequality.INSTANCE.equals(dlPredicate) && !(dlPredicate instanceof AnnotatedEquality))
                return false;
            else if (dlPredicate instanceof AtLeastConcept) {
                AtLeastConcept atLeastConcept=(AtLeastConcept)dlPredicate;
                if (graphAtomicRoles.contains(atLeastConcept.getOnRole()))
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

    /**
     * Tests whether the given center variable is suitable.
     */
    protected boolean isTreeWithCenterVariable(DLClause dlClause,Variable centerVariable,Set<AtomicConcept> bodyOnlyAtomicConcepts) {
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            Atom atom=dlClause.getBodyAtom(atomIndex);
            if (atom.getDLPredicate() instanceof AtomicRole && !atom.containsVariable(centerVariable))
                return false;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            Atom atom=dlClause.getHeadAtom(atomIndex);
            if (atom.getDLPredicate() instanceof AtomicRole && !atom.containsVariable(centerVariable))
                return false;
            if (Equality.INSTANCE.equals(atom.getDLPredicate()) || (atom.getDLPredicate() instanceof AnnotatedEquality)) {
                if ((atom.getDLPredicate() instanceof AnnotatedEquality) && !centerVariable.equals(atom.getArgumentVariable(2)))
                    return false;
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

    protected boolean isGraphDLClause(DLClause dlClause) {
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getBodyAtom(atomIndex).getDLPredicate();
            if (!(dlPredicate instanceof AtomicRole) && !(dlPredicate instanceof AtomicConcept))
                return false;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getHeadAtom(atomIndex).getDLPredicate();
            if (!(dlPredicate instanceof AtomicRole) && !(dlPredicate instanceof AtomicConcept) && !Equality.INSTANCE.equals(dlPredicate))
                return false;
        }
        return true;
    }

    public String toString(Prefixes prefixes) {
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("Prefixes: [");
        stringBuffer.append(CRLF);
        for (Map.Entry<String,String> entry : prefixes.getPrefixIRIsByPrefixName().entrySet()) {
            stringBuffer.append("  ");
            stringBuffer.append(entry.getKey());
            stringBuffer.append(": = <");
            stringBuffer.append(entry.getValue());
            stringBuffer.append('>');
            stringBuffer.append(CRLF);
        }
        stringBuffer.append("]");
        stringBuffer.append(CRLF);
        stringBuffer.append("Deterministic DL-clauses: [");
        stringBuffer.append(CRLF);
        for (DLClause dlClause : m_dlClauses)
            if (dlClause.getHeadLength()<=1) {
                stringBuffer.append("  ");
                stringBuffer.append(dlClause.toString(prefixes));
                stringBuffer.append(CRLF);
            }
        stringBuffer.append("]");
        stringBuffer.append(CRLF);
        stringBuffer.append("Disjunctive DL-clauses: [");
        stringBuffer.append(CRLF);
        for (DLClause dlClause : m_dlClauses)
            if (dlClause.getHeadLength()>1) {
                stringBuffer.append("  ");
                stringBuffer.append(dlClause.toString(prefixes));
                stringBuffer.append(CRLF);
            }
        stringBuffer.append("]");
        stringBuffer.append(CRLF);
        stringBuffer.append("ABox: [");
        stringBuffer.append(CRLF);
        for (Atom atom : m_positiveFacts) {
            stringBuffer.append("  ");
            stringBuffer.append(atom.toString(prefixes));
            stringBuffer.append(CRLF);
        }
        for (Atom atom : m_negativeFacts) {
            stringBuffer.append("  !");
            stringBuffer.append(atom.toString(prefixes));
            stringBuffer.append(CRLF);
        }
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
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

    public static class ComplexObjectRoleInclusion implements Serializable {
        private static final long serialVersionUID=-8373563413008795874L;

        protected final Role[] m_subRoles;
        protected final Role m_superRole;
        
        public ComplexObjectRoleInclusion(Role[] subRoles,Role superRole) {
            m_subRoles=subRoles;
            m_superRole=superRole;
        }
        public int getNumberOfSubRoles() {
            return m_subRoles.length;
        }
        public Role getSubRole(int roleIndex) {
            return m_subRoles[roleIndex];
        }
        public Role getSuperRole() {
            return m_superRole;
        }
    }
    
    public static class AtomicConceptComparator implements Serializable,Comparator<AtomicConcept> {
        private static final long serialVersionUID=2386841732225838685L;
        public static final Comparator<AtomicConcept> INSTANCE=new AtomicConceptComparator();

        public int compare(AtomicConcept o1,AtomicConcept o2) {
            return o1.getIRI().compareTo(o2.getIRI());
        }

        protected Object readResolve() {
            return INSTANCE;
        }
    }

    public static class AtomicRoleComparator implements Serializable,Comparator<AtomicRole> {
        private static final long serialVersionUID=3483541702854959793L;
        public static final Comparator<AtomicRole> INSTANCE=new AtomicRoleComparator();

        public int compare(AtomicRole o1,AtomicRole o2) {
            return o1.getIRI().compareTo(o2.getIRI());
        }

        protected Object readResolve() {
            return INSTANCE;
        }
    }

    public static class IndividualComparator implements Serializable,Comparator<Individual> {
        private static final long serialVersionUID=2386841732225838685L;
        public static final Comparator<Individual> INSTANCE=new IndividualComparator();

        public int compare(Individual o1,Individual o2) {
            return o1.getIRI().compareTo(o2.getIRI());
        }

        protected Object readResolve() {
            return INSTANCE;
        }
    }

	public void setAutomata(
			Map<OWLObjectPropertyExpression, Automaton> automataOfComplexRoles) {
		m_automataOfComplexObjectProperties.putAll(automataOfComplexRoles);
		
	}
    public Map<OWLObjectPropertyExpression, Automaton> getAutomataOfComplexObjectProperties() {
		return m_automataOfComplexObjectProperties;
	}

}
