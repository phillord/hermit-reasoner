// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.owlapi.structural;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectAllRestriction;

import org.semanticweb.HermiT.util.GraphUtils;

public class TransitivityManager {
    private static final long serialVersionUID=-3267310119489682236L;

    protected final OWLDataFactory m_factory;
    protected final Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> m_subObjectProperties;
    protected final Set<OWLObjectPropertyExpression> m_transitiveObjectProperties;
    protected final Map<OWLObjectAllRestriction,OWLDescription> m_replacedDescriptions;

    public TransitivityManager(OWLDataFactory factory) {
        m_factory=factory;
        m_subObjectProperties=new HashMap<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>>();
        m_transitiveObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_replacedDescriptions=new HashMap<OWLObjectAllRestriction,OWLDescription>();
    }

    public void prepareTransformation(OWLAxioms axioms) {
        for (OWLObjectPropertyExpression transitiveProperty : axioms.m_transitiveObjectProperties)
            makeTransitive(transitiveProperty);
        for (OWLObjectPropertyExpression[] inclusion : axioms.m_objectPropertyInclusions)
            addInclusion(inclusion[0],inclusion[1]);
    }
    
    public void addInclusion(OWLObjectPropertyExpression subObjectProperty,OWLObjectPropertyExpression superObjectProperty) {
        subObjectProperty=subObjectProperty.getSimplified();
        superObjectProperty=superObjectProperty.getSimplified();
        addInclusionEx(subObjectProperty,superObjectProperty);
        addInclusionEx(subObjectProperty.getInverseProperty().getSimplified(),superObjectProperty.getInverseProperty().getSimplified());
    }

    public void addInclusion(List<OWLObjectPropertyExpression> chain,OWLObjectPropertyExpression implied) {
        if (chain.size()==1)
            addInclusion(chain.get(0),implied);
        else if (chain.size()==2 && chain.get(0).equals(implied) && chain.get(1).equals(implied))
            makeTransitive(implied);
        else
            throw new IllegalArgumentException("Object property chains not supported by this role manager.");
    }

    public void makeTransitive(OWLObjectPropertyExpression objectProperty) {
        m_transitiveObjectProperties.add(objectProperty.getSimplified());
        m_transitiveObjectProperties.add(objectProperty.getInverseProperty().getSimplified());
    }

    public void rewriteConceptInclusions(OWLAxioms axioms) {
        GraphUtils.transitivelyClose(m_subObjectProperties);
        for (OWLDescription[] inclusion : axioms.m_conceptInclusions)
            for (int index=0;index<inclusion.length;index++)
                inclusion[index]=replaceDescriptionIfNecessary(inclusion[index]);
        for (Map.Entry<OWLObjectAllRestriction,OWLDescription> mapping : m_replacedDescriptions.entrySet()) {
            OWLObjectAllRestriction replacedConcept=mapping.getKey();
            OWLDescription replacement=mapping.getValue();
            axioms.m_conceptInclusions.add(new OWLDescription[] { replacement.getComplementNNF(),replacedConcept });
            for (OWLObjectPropertyExpression transitiveSubObjectProperty : getTransitiveSubObjectProperties(replacedConcept.getProperty())) {
                OWLObjectAllRestriction consequentAll=m_factory.getOWLObjectAllRestriction(transitiveSubObjectProperty,replacedConcept.getFiller());
                OWLDescription consequentReplacement=m_replacedDescriptions.get(consequentAll);
                OWLObjectAllRestriction allConsequentReplacement=m_factory.getOWLObjectAllRestriction(transitiveSubObjectProperty,consequentReplacement);
                axioms.m_conceptInclusions.add(new OWLDescription[] { replacement.getComplementNNF(),allConsequentReplacement });
            }
        }
        m_replacedDescriptions.clear();
    }

    protected void addInclusionEx(OWLObjectPropertyExpression subObjectProperty,OWLObjectPropertyExpression superObjectProperty) {
        Set<OWLObjectPropertyExpression> subObjectProperties=m_subObjectProperties.get(superObjectProperty);
        if (subObjectProperties==null) {
            subObjectProperties=new HashSet<OWLObjectPropertyExpression>();
            m_subObjectProperties.put(superObjectProperty,subObjectProperties);
        }
        subObjectProperties.add(subObjectProperty);
    }

    protected OWLDescription replaceDescriptionIfNecessary(OWLDescription desc) {
        if (desc instanceof OWLObjectAllRestriction) {
            OWLObjectAllRestriction objectAll=(OWLObjectAllRestriction)desc;
            OWLObjectPropertyExpression objectProperty=objectAll.getProperty();
            Set<OWLObjectPropertyExpression> transitiveSubObjectProperties=getTransitiveSubObjectProperties(objectProperty);
            if (!transitiveSubObjectProperties.isEmpty()) {
                OWLDescription replacement=getReplacementFor(objectAll);
                for (OWLObjectPropertyExpression transitiveSubObjectProperty : transitiveSubObjectProperties) {
                    OWLObjectAllRestriction subObjectAll=m_factory.getOWLObjectAllRestriction(transitiveSubObjectProperty,objectAll.getFiller());
                    getReplacementFor(subObjectAll);
                }
                return replacement;
            }
        }
        return desc;
    }

    protected OWLDescription getReplacementFor(OWLObjectAllRestriction objectAll) {
        OWLDescription replacement=m_replacedDescriptions.get(objectAll);
        if (replacement==null) {
            replacement=m_factory.getOWLClass(URI.create("internal:all#"+m_replacedDescriptions.size()));
            if (objectAll.getFiller() instanceof OWLObjectComplementOf || objectAll.getFiller().equals(m_factory.getOWLNothing()))
                replacement=replacement.getComplementNNF();
            m_replacedDescriptions.put(objectAll,replacement);
        }
        return replacement;
    }

    protected Set<OWLObjectPropertyExpression> getTransitiveSubObjectProperties(OWLObjectPropertyExpression objectProperty) {
        Set<OWLObjectPropertyExpression> result=new HashSet<OWLObjectPropertyExpression>();
        if (m_transitiveObjectProperties.contains(objectProperty))
            result.add(objectProperty);
        Set<OWLObjectPropertyExpression> subObjectProperties=m_subObjectProperties.get(objectProperty);
        if (subObjectProperties!=null)
            for (OWLObjectPropertyExpression subObjectProperty : subObjectProperties)
                if (m_transitiveObjectProperties.contains(subObjectProperty))
                    result.add(subObjectProperty);
        return result;
    }
}
