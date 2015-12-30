/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.semanticweb.HermiT.structural;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.UnsupportedDatatypeException;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.owlapi.model.*;
/**ReducedABoxOnlyClausification.*/
public class ReducedABoxOnlyClausification implements OWLAxiomVisitor {

    protected final Configuration.WarningMonitor m_warningMonitor;
    protected final boolean m_ignoreUnsupportedDatatypes;
    protected final Set<AtomicConcept> m_allAtomicConcepts;
    protected final Set<AtomicRole> m_allAtomicObjectRoles;
    protected final Set<AtomicRole> m_allAtomicDataRoles;
    protected final Set<Atom> m_positiveFacts;
    protected final Set<Atom> m_negativeFacts;
    protected final Set<Individual> m_allIndividuals;

    /**
     * @param configuration configuration
     * @param allAtomicConcepts allAtomicConcepts
     * @param allAtomicObjectRoles allAtomicObjectRoles
     * @param allAtomicDataRoles allAtomicDataRoles
     */
    public ReducedABoxOnlyClausification(Configuration configuration, Set<AtomicConcept> allAtomicConcepts, Set<AtomicRole> allAtomicObjectRoles, Set<AtomicRole> allAtomicDataRoles) {
        m_warningMonitor=configuration.warningMonitor;
        m_ignoreUnsupportedDatatypes=configuration.ignoreUnsupportedDatatypes;
        m_allAtomicConcepts=allAtomicConcepts;
        m_allAtomicObjectRoles=allAtomicObjectRoles;
        m_allAtomicDataRoles=allAtomicDataRoles;
        m_positiveFacts=new HashSet<>();
        m_negativeFacts=new HashSet<>();
        m_allIndividuals=new HashSet<>();
    }
    /**
     * @param axioms axioms
     */
    public void clausify(OWLIndividualAxiom... axioms) {
        m_positiveFacts.clear();
        m_negativeFacts.clear();
        for (OWLIndividualAxiom fact : axioms)
            fact.accept(this);
    }
    /**
     * @return positive facts
     */
    public Set<Atom> getPositiveFacts() {
        return m_positiveFacts;
    }
    /**
     * @return negative facts
     */
    public Set<Atom> getNegativeFacts() {
        return m_negativeFacts;
    }
    /**
     * @return all individuals
     */
    public Set<Individual> getAllIndividuals() {
        return m_allIndividuals;
    }
    protected Atom getConceptAtom(OWLClass cls, Term term) {
        AtomicConcept atomicConcept=AtomicConcept.create(cls.getIRI().toString());
        if (m_allAtomicConcepts.contains(atomicConcept))
            return Atom.create(atomicConcept,term);
        else
            throw new IllegalArgumentException("Internal error: fresh classes in class assertions are not compatible with incremental ABox loading!");
    }
    protected Atom getRoleAtom(OWLObjectPropertyExpression objectProperty,Term first,Term second) {
        AtomicRole atomicRole;
        if (objectProperty.isAnonymous()) {
            OWLObjectProperty internalObjectProperty=objectProperty.getNamedProperty();
            atomicRole=AtomicRole.create(internalObjectProperty.getIRI().toString());
            Term tmp=first;
            first=second;
            second=tmp;
        }
        else
            atomicRole=AtomicRole.create(objectProperty.asOWLObjectProperty().getIRI().toString());
        if (m_allAtomicObjectRoles.contains(atomicRole))
            return Atom.create(atomicRole,first,second);
        else
            throw new IllegalArgumentException("Internal error: fresh properties in property assertions are not compatible with incremental ABox loading!");
    }
    protected Atom getRoleAtom(OWLDataPropertyExpression dataProperty,Term first,Term second) {
        AtomicRole atomicRole;
        if (dataProperty instanceof OWLDataProperty)
            atomicRole=AtomicRole.create(((OWLDataProperty)dataProperty).getIRI().toString());
        else
            throw new IllegalStateException("Internal error: unsupported type of data property!");
        if (m_allAtomicDataRoles.contains(atomicRole))
            return Atom.create(atomicRole,first,second);
        else
            throw new IllegalArgumentException("Internal error: fresh properties in property assertions are not compatible with incremental ABox loading!");
    }
    protected Individual getIndividual(OWLIndividual individual) {
        Individual ind;
        if (individual.isAnonymous())
            ind=Individual.createAnonymous(individual.asOWLAnonymousIndividual().getID().toString());
        else
            ind=Individual.create(individual.asOWLNamedIndividual().getIRI().toString());
        m_allIndividuals.add(ind);
        return ind;
    }
    @Override
    public void visit(OWLSameIndividualAxiom object) {
        List<OWLIndividual> individuals=asList(object.individuals());
        for (int i=0;i<individuals.size()-1;i++)
            m_positiveFacts.add(Atom.create(Equality.create(),getIndividual(individuals.get(i)),getIndividual(individuals.get(i+1))));
    }
    @Override
    public void visit(OWLDifferentIndividualsAxiom object) {
        List<OWLIndividual> individuals=asList(object.individuals());
        for (int i=0;i<individuals.size()-1;i++)
            for (int j=i+1;j<individuals.size();j++)
                m_positiveFacts.add(Atom.create(Inequality.create(),getIndividual(individuals.get(i)),getIndividual(individuals.get(j))));
    }
    @Override
    public void visit(OWLClassAssertionAxiom object) {
        // we can handle everything that results in positive or negative facts
        // (C a) with C a named class, HasSelf, HasValue, negated named class, negated HasSelf, negatedHasValue
        // all used names must already exist in the loaded ontology
        OWLClassExpression description=object.getClassExpression();
        if (description instanceof OWLClass) {
            m_positiveFacts.add(getConceptAtom((OWLClass)description,getIndividual(object.getIndividual())));
        } else if (description instanceof OWLObjectHasSelf) {
            m_positiveFacts.add(getRoleAtom(((OWLObjectHasSelf)description).getProperty().getNamedProperty(),getIndividual(object.getIndividual()),getIndividual(object.getIndividual())));
        } else if (description instanceof OWLObjectHasValue) {
            OWLObjectHasValue hasValue=(OWLObjectHasValue)description;
            OWLObjectPropertyExpression role=hasValue.getProperty();
            OWLIndividual filler=hasValue.getFiller();
            m_positiveFacts.add(getRoleAtom(role,getIndividual(object.getIndividual()),getIndividual(filler)));
        } else if (description instanceof OWLObjectComplementOf) {
            OWLClassExpression negated=((OWLObjectComplementOf)description).getOperand();
            if (negated instanceof OWLClass) {
                m_negativeFacts.add(getConceptAtom((OWLClass)negated,getIndividual(object.getIndividual())));
            } else if (negated instanceof OWLObjectHasSelf) {
                m_negativeFacts.add(getRoleAtom(((OWLObjectHasSelf)negated).getProperty().getNamedProperty(),getIndividual(object.getIndividual()),getIndividual(object.getIndividual())));
            } else if (negated instanceof OWLObjectHasValue) {
                OWLObjectHasValue hasValue=(OWLObjectHasValue)negated;
                OWLObjectPropertyExpression role=hasValue.getProperty();
                OWLIndividual filler=hasValue.getFiller();
                m_negativeFacts.add(getRoleAtom(role,getIndividual(object.getIndividual()),getIndividual(filler)));
            } else {
                throw new IllegalArgumentException("Internal error: invalid normal form for ABox updates (class assertion with negated class).");
            }
        } else
            throw new IllegalArgumentException("Internal error: invalid normal form for ABox updates.");
    }
    @Override
    public void visit(OWLObjectPropertyAssertionAxiom object) {
        m_positiveFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),getIndividual(object.getObject())));
    }
    @Override
    public void visit(OWLNegativeObjectPropertyAssertionAxiom object) {
        m_negativeFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),getIndividual(object.getObject())));
    }
    @Override
    public void visit(OWLDataPropertyAssertionAxiom object) {
        Constant targetValue=getConstant(object.getObject());
        m_positiveFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),targetValue));
    }
    @Override
    public void visit(OWLNegativeDataPropertyAssertionAxiom object) {
        Constant targetValue=getConstant(object.getObject());
        m_negativeFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),targetValue));
    }
    protected Constant getConstant(OWLLiteral literal) {
        try {
            if (literal.isRDFPlainLiteral()) {
                if (literal.hasLang())
                    return Constant.create(literal.getLiteral()+"@"+literal.getLang(),Prefixes.s_semanticWebPrefixes.get("rdf:")+"PlainLiteral");
                else
                    return Constant.create(literal.getLiteral()+"@",Prefixes.s_semanticWebPrefixes.get("rdf:")+"PlainLiteral");
            }
            else
                return Constant.create(literal.getLiteral(),literal.getDatatype().getIRI().toString());
        }
        catch (UnsupportedDatatypeException e) {
            if (m_ignoreUnsupportedDatatypes) {
                if (m_warningMonitor!=null)
                    m_warningMonitor.warning("Ignoring unsupported datatype '"+literal.toString()+"'.");
                return Constant.createAnonymous(literal.getLiteral());
            }
            else
                throw e;
        }
    }
}
