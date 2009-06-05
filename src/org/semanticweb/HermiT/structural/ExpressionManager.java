package org.semanticweb.HermiT.structural;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassExpression;
import org.semanticweb.owl.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owl.model.OWLDataAllValuesFrom;
import org.semanticweb.owl.model.OWLDataComplementOf;
import org.semanticweb.owl.model.OWLDataExactCardinality;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataHasValue;
import org.semanticweb.owl.model.OWLDataIntersectionOf;
import org.semanticweb.owl.model.OWLDataMaxCardinality;
import org.semanticweb.owl.model.OWLDataMinCardinality;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataSomeValuesFrom;
import org.semanticweb.owl.model.OWLDataUnionOf;
import org.semanticweb.owl.model.OWLDataVisitorEx;
import org.semanticweb.owl.model.OWLDatatype;
import org.semanticweb.owl.model.OWLDatatypeRestriction;
import org.semanticweb.owl.model.OWLFacetRestriction;
import org.semanticweb.owl.model.OWLObjectAllValuesFrom;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinality;
import org.semanticweb.owl.model.OWLObjectHasSelf;
import org.semanticweb.owl.model.OWLObjectHasValue;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinality;
import org.semanticweb.owl.model.OWLObjectMinCardinality;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLStringLiteral;
import org.semanticweb.owl.model.OWLTypedLiteral;

public class ExpressionManager {
    protected final OWLDataFactory m_factory;
    protected final DescriptionNNFVisitor m_descriptionNNFVisitor;
    protected final DataRangeNNFVisitor m_dataRangeNNFVisitor;
    protected final DescriptionComplementNNFVisitor m_descriptionComplementNNFVisitor;
    protected final DataRangeComplementNNFVisitor m_dataRangeComplementNNFVisitor;
    protected final DescriptionSimplificationVisitor m_descriptionSimplificationVisitor;
    protected final DataRangeSimplificationVisitor m_dataRangeSimplificationVisitor;
    
    public ExpressionManager(OWLDataFactory factory) {
        m_factory=factory;
        m_descriptionNNFVisitor=new DescriptionNNFVisitor();
        m_dataRangeNNFVisitor=new DataRangeNNFVisitor();
        m_descriptionComplementNNFVisitor=new DescriptionComplementNNFVisitor();
        m_dataRangeComplementNNFVisitor=new DataRangeComplementNNFVisitor();
        m_descriptionSimplificationVisitor=new DescriptionSimplificationVisitor();
        m_dataRangeSimplificationVisitor=new DataRangeSimplificationVisitor();
    }
    public OWLClassExpression getNNF(OWLClassExpression description) {
        return description.accept(m_descriptionNNFVisitor);
    }
    public OWLDataRange getNNF(OWLDataRange dataRange) {
        return dataRange.accept(m_dataRangeNNFVisitor);
    }
    public OWLClassExpression getComplementNNF(OWLClassExpression description) {
        return description.accept(m_descriptionComplementNNFVisitor);
    }
    public OWLDataRange getComplementNNF(OWLDataRange dataRange) {
        return dataRange.accept(m_dataRangeComplementNNFVisitor);
    }
    public OWLClassExpression getSimplified(OWLClassExpression description) {
        return description.accept(m_descriptionSimplificationVisitor);
    }
    public OWLDataRange getSimplified(OWLDataRange dataRange) {
        return dataRange.accept(m_dataRangeSimplificationVisitor);
    }

    // -----------------------------------------------------------------------------------
    // NNF
    // -----------------------------------------------------------------------------------
    
    protected class DescriptionNNFVisitor implements OWLClassExpressionVisitorEx<OWLClassExpression> {
        public OWLClassExpression visit(OWLClass d) {
            return d;
        }
        public OWLClassExpression visit(OWLObjectIntersectionOf d) {
            Set<OWLClassExpression> newConjuncts=new HashSet<OWLClassExpression>();
            for (OWLClassExpression description : d.getOperands()) {
                OWLClassExpression descriptionNNF=getNNF(description);
                newConjuncts.add(descriptionNNF);
            }
            return m_factory.getOWLObjectIntersectionOf(newConjuncts);
        }
        public OWLClassExpression visit(OWLObjectUnionOf d) {
            Set<OWLClassExpression> newDisjuncts=new HashSet<OWLClassExpression>();
            for (OWLClassExpression description : d.getOperands()) {
                OWLClassExpression descriptionNNF=getNNF(description);
                newDisjuncts.add(descriptionNNF);
            }
            return m_factory.getOWLObjectUnionOf(newDisjuncts);
        }
        public OWLClassExpression visit(OWLObjectComplementOf d) {
            return getComplementNNF(d.getOperand());
        }
        public OWLClassExpression visit(OWLObjectOneOf d) {
            return d;
        }
        public OWLClassExpression visit(OWLObjectSomeValuesFrom d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectSomeValuesFrom(d.getProperty().getSimplified(),filler);
        }
        public OWLClassExpression visit(OWLObjectAllValuesFrom d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectAllValuesFrom(d.getProperty().getSimplified(),filler);
        }
        public OWLClassExpression visit(OWLObjectHasValue d) {
            return m_factory.getOWLObjectHasValue(d.getProperty().getSimplified(),d.getValue());
        }
        public OWLClassExpression visit(OWLObjectHasSelf d) {
            return m_factory.getOWLObjectHasSelf(d.getProperty().getSimplified());
        }
        public OWLClassExpression visit(OWLObjectMinCardinality d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectMinCardinality(d.getProperty().getSimplified(),d.getCardinality(),filler);
        }
        public OWLClassExpression visit(OWLObjectMaxCardinality d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectMaxCardinality(d.getProperty().getSimplified(),d.getCardinality(),filler);
        }
        public OWLClassExpression visit(OWLObjectExactCardinality d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectExactCardinality(d.getProperty().getSimplified(),d.getCardinality(),filler);
        }
        public OWLClassExpression visit(OWLDataSomeValuesFrom d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataSomeValuesFrom(d.getProperty(),filler);
        }
        public OWLClassExpression visit(OWLDataAllValuesFrom d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataAllValuesFrom(d.getProperty(),filler);
        }
        public OWLClassExpression visit(OWLDataHasValue d) {
            return d;
        }
        public OWLClassExpression visit(OWLDataMinCardinality d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataMinCardinality(d.getProperty(),d.getCardinality(),filler);
        }
        public OWLClassExpression visit(OWLDataMaxCardinality d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataMaxCardinality(d.getProperty(),d.getCardinality(),filler);
        }
        public OWLClassExpression visit(OWLDataExactCardinality d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataExactCardinality(d.getProperty(),d.getCardinality(),filler);
        }
    }
    
    protected class DataRangeNNFVisitor implements OWLDataVisitorEx<OWLDataRange> {
        public OWLDataRange visit(OWLDatatype o) {
            return o;
        }
        public OWLDataRange visit(OWLDataComplementOf o) {
            return getComplementNNF(o.getDataRange());
        }
        public OWLDataRange visit(OWLDataOneOf o) {
            return o;
        }
        public OWLDataRange visit(OWLDataRange o) {
            return o;
        }
        public OWLDataRange visit(OWLDatatypeRestriction o) {
            return o;
        }
        public OWLDataRange visit(OWLFacetRestriction node) {
            return null;
        }
        public OWLDataRange visit(OWLTypedLiteral o) {
            return null;
        }
        public OWLDataRange visit(OWLStringLiteral o) {
            return null;
        }
        public OWLDataRange visit(OWLDataIntersectionOf node) {
            // TODO Auto-generated method stub
            return null;
        }
        public OWLDataRange visit(OWLDataUnionOf node) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    // -----------------------------------------------------------------------------------
    // Complement NNF
    // -----------------------------------------------------------------------------------
    
    protected class DescriptionComplementNNFVisitor implements OWLClassExpressionVisitorEx<OWLClassExpression> {
        public OWLClassExpression visit(OWLClass d) {
            if (d.isOWLThing())
                return m_factory.getOWLNothing();
            if (d.isOWLNothing())
                return m_factory.getOWLThing();
            return m_factory.getOWLObjectComplementOf(d);
        }
        public OWLClassExpression visit(OWLObjectIntersectionOf d) {
            Set<OWLClassExpression> newDisjuncts=new HashSet<OWLClassExpression>();
            for (OWLClassExpression description : d.getOperands())
                newDisjuncts.add(getComplementNNF(description));
            return m_factory.getOWLObjectUnionOf(newDisjuncts);
        }
        public OWLClassExpression visit(OWLObjectUnionOf d) {
            Set<OWLClassExpression> newConjuncts=new HashSet<OWLClassExpression>();
            for (OWLClassExpression description : d.getOperands())
                newConjuncts.add(getComplementNNF(description));
            return m_factory.getOWLObjectIntersectionOf(newConjuncts);
        }
        public OWLClassExpression visit(OWLObjectComplementOf d) {
            return getNNF(d.getOperand());
        }
        public OWLClassExpression visit(OWLObjectOneOf d) {
            return m_factory.getOWLObjectComplementOf(d);
        }
        public OWLClassExpression visit(OWLObjectSomeValuesFrom d) {
            OWLClassExpression filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLObjectAllValuesFrom(d.getProperty().getSimplified(),filler);
        }
        public OWLClassExpression visit(OWLObjectAllValuesFrom d) {
            OWLClassExpression filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLObjectSomeValuesFrom(d.getProperty().getSimplified(),filler);
        }
        public OWLClassExpression visit(OWLObjectHasValue d) {
            return m_factory.getOWLObjectComplementOf(getNNF(d));
        }
        public OWLClassExpression visit(OWLObjectHasSelf d) {
            return m_factory.getOWLObjectComplementOf(getNNF(d));
        }
        public OWLClassExpression visit(OWLObjectMinCardinality d) {
            if (d.getCardinality()==0)
                return m_factory.getOWLNothing();
            else {
                OWLClassExpression filler=getNNF(d.getFiller());
                return m_factory.getOWLObjectMaxCardinality(d.getProperty().getSimplified(),d.getCardinality()-1,filler);
            }
        }
        public OWLClassExpression visit(OWLObjectMaxCardinality d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectMinCardinality(d.getProperty().getSimplified(),d.getCardinality()+1,filler);
        }
        public OWLClassExpression visit(OWLObjectExactCardinality d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            if (d.getCardinality()==0)
                return m_factory.getOWLObjectMinCardinality(d.getProperty().getSimplified(),1,filler);
            else {
                Set<OWLClassExpression> disjuncts=new HashSet<OWLClassExpression>();
                disjuncts.add(m_factory.getOWLObjectMaxCardinality(d.getProperty().getSimplified(),d.getCardinality()-1,filler));
                disjuncts.add(m_factory.getOWLObjectMinCardinality(d.getProperty().getSimplified(),d.getCardinality()+1,filler));
                return m_factory.getOWLObjectUnionOf(disjuncts);
            }
        }
        public OWLClassExpression visit(OWLDataSomeValuesFrom d) {
            OWLDataRange filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLDataAllValuesFrom(d.getProperty(),filler);
        }
        public OWLClassExpression visit(OWLDataAllValuesFrom d) {
            OWLDataRange filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLDataSomeValuesFrom(d.getProperty(),filler);
        }
        public OWLClassExpression visit(OWLDataHasValue d) {
            return m_factory.getOWLObjectComplementOf(d);
        }
        public OWLClassExpression visit(OWLDataMinCardinality d) {
            if (d.getCardinality()==0)
                return m_factory.getOWLNothing();
            else {
                OWLDataRange filler=getNNF(d.getFiller());
                return m_factory.getOWLDataMaxCardinality(d.getProperty(),d.getCardinality()-1,filler);
            }
        }
        public OWLClassExpression visit(OWLDataMaxCardinality d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataMinCardinality(d.getProperty(),d.getCardinality()+1,filler);
        }
        public OWLClassExpression visit(OWLDataExactCardinality d) {
            OWLDataRange filler=getNNF(d.getFiller());
            if (d.getCardinality()==0)
                return m_factory.getOWLDataMinCardinality(d.getProperty(),1,filler);
            else {
                Set<OWLClassExpression> disjuncts=new HashSet<OWLClassExpression>();
                disjuncts.add(m_factory.getOWLDataMaxCardinality(d.getProperty(),d.getCardinality()-1,filler));
                disjuncts.add(m_factory.getOWLDataMinCardinality(d.getProperty(),d.getCardinality()+1,filler));
                return m_factory.getOWLObjectUnionOf(disjuncts);
            }
        }
    }
    
    protected class DataRangeComplementNNFVisitor implements OWLDataVisitorEx<OWLDataRange> {
        public OWLDataRange visit(OWLDatatype o) {
            return m_factory.getOWLDataComplementOf(o);
        }
        public OWLDataRange visit(OWLDataComplementOf o) {
            return getNNF(o.getDataRange());
        }
        public OWLDataRange visit(OWLDataOneOf o) {
            return m_factory.getOWLDataComplementOf(o);
        }
        public OWLDataRange visit(OWLDatatypeRestriction o) {
            return m_factory.getOWLDataComplementOf(o);
        }
        public OWLDataRange visit(OWLFacetRestriction o) {
            return null;
        }
        public OWLDataRange visit(OWLTypedLiteral o) {
            return null;
        }
        public OWLDataRange visit(OWLStringLiteral o) {
            return null;
        }
        public OWLDataRange visit(OWLDataIntersectionOf node) {
            // TODO Auto-generated method stub
            return null;
        }
        public OWLDataRange visit(OWLDataUnionOf node) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    // -----------------------------------------------------------------------------------
    // Simplification
    // -----------------------------------------------------------------------------------
    
    protected class DescriptionSimplificationVisitor implements OWLClassExpressionVisitorEx<OWLClassExpression> {
        public OWLClassExpression visit(OWLClass d) {
            return d;
        }
        public OWLClassExpression visit(OWLObjectIntersectionOf d) {
            Set<OWLClassExpression> newConjuncts=new HashSet<OWLClassExpression>();
            for (OWLClassExpression description : d.getOperands()) {
                OWLClassExpression descriptionSimplified=getSimplified(description);
                if (descriptionSimplified.isOWLThing())
                    continue;
                else if (descriptionSimplified.isOWLNothing())
                    return m_factory.getOWLNothing();
                else if (descriptionSimplified instanceof OWLObjectIntersectionOf)
                    newConjuncts.addAll(((OWLObjectIntersectionOf)descriptionSimplified).getOperands());
                else
                    newConjuncts.add(descriptionSimplified);
            }
            return m_factory.getOWLObjectIntersectionOf(newConjuncts);
        }
        public OWLClassExpression visit(OWLObjectUnionOf d) {
            Set<OWLClassExpression> newDisjuncts=new HashSet<OWLClassExpression>();
            for (OWLClassExpression description : d.getOperands()) {
                OWLClassExpression descriptionSimplified=getSimplified(description);
                if (descriptionSimplified.isOWLThing())
                    return m_factory.getOWLThing();
                else if (descriptionSimplified.isOWLNothing())
                    continue;
                else if (descriptionSimplified instanceof OWLObjectUnionOf)
                    newDisjuncts.addAll(((OWLObjectUnionOf)descriptionSimplified).getOperands());
                else
                    newDisjuncts.add(descriptionSimplified);
            }
            return m_factory.getOWLObjectUnionOf(newDisjuncts);
        }
        public OWLClassExpression visit(OWLObjectComplementOf d) {
            OWLClassExpression operandSimplified=getSimplified(d.getOperand());
            if (operandSimplified.isOWLThing())
                return m_factory.getOWLNothing();
            else if (operandSimplified.isOWLNothing())
                return m_factory.getOWLThing();
            else if (operandSimplified instanceof OWLObjectComplementOf)
                return ((OWLObjectComplementOf)operandSimplified).getOperand();
            else
                return m_factory.getOWLObjectComplementOf(operandSimplified);
        }
        public OWLClassExpression visit(OWLObjectOneOf d) {
            return d;
        }
        public OWLClassExpression visit(OWLObjectSomeValuesFrom d) {
            OWLClassExpression filler=getSimplified(d.getFiller());
            if (filler.isOWLNothing())
                return m_factory.getOWLNothing();
            else
                return m_factory.getOWLObjectSomeValuesFrom(d.getProperty().getSimplified(),filler);
        }
        public OWLClassExpression visit(OWLObjectAllValuesFrom d) {
            OWLClassExpression filler=getSimplified(d.getFiller());
            if (filler.isOWLThing())
                return m_factory.getOWLThing();
            else
                return m_factory.getOWLObjectAllValuesFrom(d.getProperty().getSimplified(),filler);
        }
        public OWLClassExpression visit(OWLObjectHasValue d) {
            OWLObjectOneOf nominal=m_factory.getOWLObjectOneOf(d.getValue());
            return m_factory.getOWLObjectSomeValuesFrom(d.getProperty().getSimplified(),nominal);
        }
        public OWLClassExpression visit(OWLObjectHasSelf d) {
            return m_factory.getOWLObjectHasSelf(d.getProperty().getSimplified());
        }
        public OWLClassExpression visit(OWLObjectMinCardinality d) {
            OWLClassExpression filler=getSimplified(d.getFiller());
            if (d.getCardinality()<=0)
                return m_factory.getOWLThing();
            else if (filler.isOWLNothing())
                return m_factory.getOWLNothing();
            else if (d.getCardinality()==1)
                return m_factory.getOWLObjectSomeValuesFrom(d.getProperty().getSimplified(),filler);
            else
                return m_factory.getOWLObjectMinCardinality(d.getProperty().getSimplified(),d.getCardinality(),filler);
        }
        public OWLClassExpression visit(OWLObjectMaxCardinality d) {
            OWLClassExpression filler=getSimplified(d.getFiller());
            if (filler.isOWLNothing())
                return m_factory.getOWLThing();
            else if (d.getCardinality()<=0)
                return m_factory.getOWLObjectAllValuesFrom(d.getProperty().getSimplified(),m_factory.getOWLObjectComplementOf(filler));
            else
                return m_factory.getOWLObjectMaxCardinality(d.getProperty().getSimplified(),d.getCardinality(),filler);
        }
        public OWLClassExpression visit(OWLObjectExactCardinality d) {
            OWLClassExpression filler=getSimplified(d.getFiller());
            if (d.getCardinality()<0)
                return m_factory.getOWLNothing();
            else if (d.getCardinality()==0)
                return m_factory.getOWLObjectAllValuesFrom(d.getProperty().getSimplified(),m_factory.getOWLObjectComplementOf(filler));
            else if (filler.isOWLNothing())
                return m_factory.getOWLNothing();
            else {
                OWLObjectMinCardinality minCardinality=m_factory.getOWLObjectMinCardinality(d.getProperty().getSimplified(),d.getCardinality(),filler);
                OWLObjectMaxCardinality maxCardinality=m_factory.getOWLObjectMaxCardinality(d.getProperty().getSimplified(),d.getCardinality(),filler);
                return m_factory.getOWLObjectIntersectionOf(minCardinality,maxCardinality);
            }
        }
        public OWLClassExpression visit(OWLDataSomeValuesFrom d) {
            OWLDataRange filler=getSimplified(d.getFiller());
            if (isBottomDataRange(filler))
                return m_factory.getOWLNothing();
            else
                return m_factory.getOWLDataSomeValuesFrom(d.getProperty(),filler);
        }
        public OWLClassExpression visit(OWLDataAllValuesFrom d) {
            OWLDataRange filler=getSimplified(d.getFiller());
            if (filler.isTopDatatype())
                return m_factory.getOWLThing();
            else
                return m_factory.getOWLDataAllValuesFrom(d.getProperty(),filler);
        }
        public OWLClassExpression visit(OWLDataHasValue d) {
            OWLDataOneOf nominal=m_factory.getOWLDataOneOf(d.getValue());
            return m_factory.getOWLDataSomeValuesFrom(d.getProperty(),nominal);
        }
        public OWLClassExpression visit(OWLDataMinCardinality d) {
            OWLDataRange filler=getSimplified(d.getFiller());
            if (d.getCardinality()<=0)
                return m_factory.getOWLThing();
            else if (isBottomDataRange(filler))
                return m_factory.getOWLNothing();
            else if (d.getCardinality()==1)
                return m_factory.getOWLDataSomeValuesFrom(d.getProperty(),filler);
            else
                return m_factory.getOWLDataMinCardinality(d.getProperty(),d.getCardinality(),filler);
        }
        public OWLClassExpression visit(OWLDataMaxCardinality d) {
            OWLDataRange filler=getSimplified(d.getFiller());
            if (isBottomDataRange(filler))
                return m_factory.getOWLThing();
            else if (d.getCardinality()<=0)
                return m_factory.getOWLDataAllValuesFrom(d.getProperty(),m_factory.getOWLDataComplementOf(filler));
            else
                return m_factory.getOWLDataMaxCardinality(d.getProperty(),d.getCardinality(),filler);
        }
        public OWLClassExpression visit(OWLDataExactCardinality d) {
            OWLDataRange filler=getSimplified(d.getFiller());
            if (d.getCardinality()<0)
                return m_factory.getOWLNothing();
            else if (d.getCardinality()==0)
                return m_factory.getOWLDataAllValuesFrom(d.getProperty(),m_factory.getOWLDataComplementOf(filler));
            else if (isBottomDataRange(filler))
                return m_factory.getOWLNothing();
            else {
                OWLDataMinCardinality minCardinality=m_factory.getOWLDataMinCardinality(d.getProperty(),d.getCardinality(),filler);
                OWLDataMaxCardinality maxCardinality=m_factory.getOWLDataMaxCardinality(d.getProperty(),d.getCardinality(),filler);
                return m_factory.getOWLObjectIntersectionOf(minCardinality,maxCardinality);
            }
        }
        protected boolean isBottomDataRange(OWLDataRange dataRange) {
            return dataRange instanceof OWLDataComplementOf && ((OWLDataComplementOf)dataRange).getDataRange().isTopDatatype();
        }
    }
    
    protected class DataRangeSimplificationVisitor implements OWLDataVisitorEx<OWLDataRange> {
        public OWLDataRange visit(OWLDatatype o) {
            return o;
        }
        public OWLDataRange visit(OWLDataComplementOf o) {
            OWLDataRange dataRangeSimplified=getSimplified(o.getDataRange());
            if (dataRangeSimplified instanceof OWLDataComplementOf)
                return ((OWLDataComplementOf)dataRangeSimplified).getDataRange();
            else
                return m_factory.getOWLDataComplementOf(dataRangeSimplified);
        }
        public OWLDataRange visit(OWLDataOneOf o) {
            return o;
        }
        public OWLDataRange visit(OWLDatatypeRestriction o) {
            return o;
        }
        public OWLDataRange visit(OWLFacetRestriction o) {
            return null;
        }
        public OWLDataRange visit(OWLTypedLiteral o) {
            return null;
        }
        public OWLDataRange visit(OWLStringLiteral o) {
            return null;
        }
        public OWLDataRange visit(OWLDataIntersectionOf node) {
            // TODO Auto-generated method stub
            return null;
        }
        public OWLDataRange visit(OWLDataUnionOf node) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
