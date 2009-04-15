package org.semanticweb.HermiT.structural;

import java.util.Set;
import java.util.HashSet;

import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescriptionVisitorEx;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataVisitorEx;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataComplementOf;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLDataRangeRestriction;
import org.semanticweb.owl.model.OWLDataRangeFacetRestriction;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.model.OWLUntypedConstant;

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
    public OWLDescription getNNF(OWLDescription description) {
        return description.accept(m_descriptionNNFVisitor);
    }
    public OWLDataRange getNNF(OWLDataRange dataRange) {
        return dataRange.accept(m_dataRangeNNFVisitor);
    }
    public OWLDescription getComplementNNF(OWLDescription description) {
        return description.accept(m_descriptionComplementNNFVisitor);
    }
    public OWLDataRange getComplementNNF(OWLDataRange dataRange) {
        return dataRange.accept(m_dataRangeComplementNNFVisitor);
    }
    public OWLDescription getSimplified(OWLDescription description) {
        return description.accept(m_descriptionSimplificationVisitor);
    }
    public OWLDataRange getSimplified(OWLDataRange dataRange) {
        return dataRange.accept(m_dataRangeSimplificationVisitor);
    }

    // -----------------------------------------------------------------------------------
    // NNF
    // -----------------------------------------------------------------------------------
    
    protected class DescriptionNNFVisitor implements OWLDescriptionVisitorEx<OWLDescription> {
        public OWLDescription visit(OWLClass d) {
            return d;
        }
        public OWLDescription visit(OWLObjectIntersectionOf d) {
            Set<OWLDescription> newConjuncts=new HashSet<OWLDescription>();
            for (OWLDescription description : d.getOperands()) {
                OWLDescription descriptionNNF=getNNF(description);
                newConjuncts.add(descriptionNNF);
            }
            return m_factory.getOWLObjectIntersectionOf(newConjuncts);
        }
        public OWLDescription visit(OWLObjectUnionOf d) {
            Set<OWLDescription> newDisjuncts=new HashSet<OWLDescription>();
            for (OWLDescription description : d.getOperands()) {
                OWLDescription descriptionNNF=getNNF(description);
                newDisjuncts.add(descriptionNNF);
            }
            return m_factory.getOWLObjectUnionOf(newDisjuncts);
        }
        public OWLDescription visit(OWLObjectComplementOf d) {
            return getComplementNNF(d.getOperand());
        }
        public OWLDescription visit(OWLObjectOneOf d) {
            return d;
        }
        public OWLDescription visit(OWLObjectSomeRestriction d) {
            OWLDescription filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectSomeRestriction(d.getProperty().getSimplified(),filler);
        }
        public OWLDescription visit(OWLObjectAllRestriction d) {
            OWLDescription filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectAllRestriction(d.getProperty().getSimplified(),filler);
        }
        public OWLDescription visit(OWLObjectValueRestriction d) {
            return m_factory.getOWLObjectValueRestriction(d.getProperty().getSimplified(),d.getValue());
        }
        public OWLDescription visit(OWLObjectSelfRestriction d) {
            return m_factory.getOWLObjectSelfRestriction(d.getProperty().getSimplified());
        }
        public OWLDescription visit(OWLObjectMinCardinalityRestriction d) {
            OWLDescription filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectMinCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality(),filler);
        }
        public OWLDescription visit(OWLObjectMaxCardinalityRestriction d) {
            OWLDescription filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectMaxCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality(),filler);
        }
        public OWLDescription visit(OWLObjectExactCardinalityRestriction d) {
            OWLDescription filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectExactCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality(),filler);
        }
        public OWLDescription visit(OWLDataSomeRestriction d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataSomeRestriction(d.getProperty(),filler);
        }
        public OWLDescription visit(OWLDataAllRestriction d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataAllRestriction(d.getProperty(),filler);
        }
        public OWLDescription visit(OWLDataValueRestriction d) {
            return d;
        }
        public OWLDescription visit(OWLDataMinCardinalityRestriction d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataMinCardinalityRestriction(d.getProperty(),d.getCardinality(),filler);
        }
        public OWLDescription visit(OWLDataMaxCardinalityRestriction d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataMaxCardinalityRestriction(d.getProperty(),d.getCardinality(),filler);
        }
        public OWLDescription visit(OWLDataExactCardinalityRestriction d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataExactCardinalityRestriction(d.getProperty(),d.getCardinality(),filler);
        }
    }
    
    protected class DataRangeNNFVisitor implements OWLDataVisitorEx<OWLDataRange> {
        public OWLDataRange visit(OWLDataType o) {
            return o;
        }
        public OWLDataRange visit(OWLDataComplementOf o) {
            return getComplementNNF(o.getDataRange());
        }
        public OWLDataRange visit(OWLDataOneOf o) {
            return o;
        }
        public OWLDataRange visit(OWLDataRangeRestriction o) {
            return o;
        }
        public OWLDataRange visit(OWLDataRangeFacetRestriction o) {
            return null;
        }
        public OWLDataRange visit(OWLTypedConstant o) {
            return null;
        }
        public OWLDataRange visit(OWLUntypedConstant o) {
            return null;
        }
    }

    // -----------------------------------------------------------------------------------
    // Complement NNF
    // -----------------------------------------------------------------------------------
    
    protected class DescriptionComplementNNFVisitor implements OWLDescriptionVisitorEx<OWLDescription> {
        public OWLDescription visit(OWLClass d) {
            if (d.isOWLThing())
                return m_factory.getOWLNothing();
            if (d.isOWLNothing())
                return m_factory.getOWLThing();
            return m_factory.getOWLObjectComplementOf(d);
        }
        public OWLDescription visit(OWLObjectIntersectionOf d) {
            Set<OWLDescription> newDisjuncts=new HashSet<OWLDescription>();
            for (OWLDescription description : d.getOperands())
                newDisjuncts.add(getComplementNNF(description));
            return m_factory.getOWLObjectUnionOf(newDisjuncts);
        }
        public OWLDescription visit(OWLObjectUnionOf d) {
            Set<OWLDescription> newConjuncts=new HashSet<OWLDescription>();
            for (OWLDescription description : d.getOperands())
                newConjuncts.add(getComplementNNF(description));
            return m_factory.getOWLObjectIntersectionOf(newConjuncts);
        }
        public OWLDescription visit(OWLObjectComplementOf d) {
            return getNNF(d.getOperand());
        }
        public OWLDescription visit(OWLObjectOneOf d) {
            return m_factory.getOWLObjectComplementOf(d);
        }
        public OWLDescription visit(OWLObjectSomeRestriction d) {
            OWLDescription filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLObjectAllRestriction(d.getProperty().getSimplified(),filler);
        }
        public OWLDescription visit(OWLObjectAllRestriction d) {
            OWLDescription filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLObjectSomeRestriction(d.getProperty().getSimplified(),filler);
        }
        public OWLDescription visit(OWLObjectValueRestriction d) {
            return m_factory.getOWLObjectComplementOf(getNNF(d));
        }
        public OWLDescription visit(OWLObjectSelfRestriction d) {
            return m_factory.getOWLObjectComplementOf(getNNF(d));
        }
        public OWLDescription visit(OWLObjectMinCardinalityRestriction d) {
            if (d.getCardinality()==0)
                return m_factory.getOWLNothing();
            else {
                OWLDescription filler=getNNF(d.getFiller());
                return m_factory.getOWLObjectMaxCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality()-1,filler);
            }
        }
        public OWLDescription visit(OWLObjectMaxCardinalityRestriction d) {
            OWLDescription filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectMinCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality()+1,filler);
        }
        public OWLDescription visit(OWLObjectExactCardinalityRestriction d) {
            OWLDescription filler=getNNF(d.getFiller());
            if (d.getCardinality()==0)
                return m_factory.getOWLObjectMinCardinalityRestriction(d.getProperty().getSimplified(),1,filler);
            else {
                Set<OWLDescription> disjuncts=new HashSet<OWLDescription>();
                disjuncts.add(m_factory.getOWLObjectMaxCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality()-1,filler));
                disjuncts.add(m_factory.getOWLObjectMinCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality()+1,filler));
                return m_factory.getOWLObjectUnionOf(disjuncts);
            }
        }
        public OWLDescription visit(OWLDataSomeRestriction d) {
            OWLDataRange filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLDataAllRestriction(d.getProperty(),filler);
        }
        public OWLDescription visit(OWLDataAllRestriction d) {
            OWLDataRange filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLDataSomeRestriction(d.getProperty(),filler);
        }
        public OWLDescription visit(OWLDataValueRestriction d) {
            return m_factory.getOWLObjectComplementOf(d);
        }
        public OWLDescription visit(OWLDataMinCardinalityRestriction d) {
            if (d.getCardinality()==0)
                return m_factory.getOWLNothing();
            else {
                OWLDataRange filler=getNNF(d.getFiller());
                return m_factory.getOWLDataMaxCardinalityRestriction(d.getProperty(),d.getCardinality()-1,filler);
            }
        }
        public OWLDescription visit(OWLDataMaxCardinalityRestriction d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataMinCardinalityRestriction(d.getProperty(),d.getCardinality()+1,filler);
        }
        public OWLDescription visit(OWLDataExactCardinalityRestriction d) {
            OWLDataRange filler=getNNF(d.getFiller());
            if (d.getCardinality()==0)
                return m_factory.getOWLDataMinCardinalityRestriction(d.getProperty(),1,filler);
            else {
                Set<OWLDescription> disjuncts=new HashSet<OWLDescription>();
                disjuncts.add(m_factory.getOWLDataMaxCardinalityRestriction(d.getProperty(),d.getCardinality()-1,filler));
                disjuncts.add(m_factory.getOWLDataMinCardinalityRestriction(d.getProperty(),d.getCardinality()+1,filler));
                return m_factory.getOWLObjectUnionOf(disjuncts);
            }
        }
    }
    
    protected class DataRangeComplementNNFVisitor implements OWLDataVisitorEx<OWLDataRange> {
        public OWLDataRange visit(OWLDataType o) {
            return m_factory.getOWLDataComplementOf(o);
        }
        public OWLDataRange visit(OWLDataComplementOf o) {
            return getNNF(o.getDataRange());
        }
        public OWLDataRange visit(OWLDataOneOf o) {
            return m_factory.getOWLDataComplementOf(o);
        }
        public OWLDataRange visit(OWLDataRangeRestriction o) {
            return m_factory.getOWLDataComplementOf(o);
        }
        public OWLDataRange visit(OWLDataRangeFacetRestriction o) {
            return null;
        }
        public OWLDataRange visit(OWLTypedConstant o) {
            return null;
        }
        public OWLDataRange visit(OWLUntypedConstant o) {
            return null;
        }
    }

    // -----------------------------------------------------------------------------------
    // Simplification
    // -----------------------------------------------------------------------------------
    
    protected class DescriptionSimplificationVisitor implements OWLDescriptionVisitorEx<OWLDescription> {
        public OWLDescription visit(OWLClass d) {
            return d;
        }
        public OWLDescription visit(OWLObjectIntersectionOf d) {
            Set<OWLDescription> newConjuncts=new HashSet<OWLDescription>();
            for (OWLDescription description : d.getOperands()) {
                OWLDescription descriptionSimplified=getSimplified(description);
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
        public OWLDescription visit(OWLObjectUnionOf d) {
            Set<OWLDescription> newDisjuncts=new HashSet<OWLDescription>();
            for (OWLDescription description : d.getOperands()) {
                OWLDescription descriptionSimplified=getSimplified(description);
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
        public OWLDescription visit(OWLObjectComplementOf d) {
            OWLDescription operandSimplified=getSimplified(d.getOperand());
            if (operandSimplified.isOWLThing())
                return m_factory.getOWLNothing();
            else if (operandSimplified.isOWLNothing())
                return m_factory.getOWLThing();
            else if (operandSimplified instanceof OWLObjectComplementOf)
                return ((OWLObjectComplementOf)operandSimplified).getOperand();
            else
                return m_factory.getOWLObjectComplementOf(operandSimplified);
        }
        public OWLDescription visit(OWLObjectOneOf d) {
            return d;
        }
        public OWLDescription visit(OWLObjectSomeRestriction d) {
            OWLDescription filler=getSimplified(d.getFiller());
            if (filler.isOWLNothing())
                return m_factory.getOWLNothing();
            else
                return m_factory.getOWLObjectSomeRestriction(d.getProperty().getSimplified(),filler);
        }
        public OWLDescription visit(OWLObjectAllRestriction d) {
            OWLDescription filler=getSimplified(d.getFiller());
            if (filler.isOWLThing())
                return m_factory.getOWLThing();
            else
                return m_factory.getOWLObjectAllRestriction(d.getProperty().getSimplified(),filler);
        }
        public OWLDescription visit(OWLObjectValueRestriction d) {
            OWLObjectOneOf nominal=m_factory.getOWLObjectOneOf(d.getValue());
            return m_factory.getOWLObjectSomeRestriction(d.getProperty().getSimplified(),nominal);
        }
        public OWLDescription visit(OWLObjectSelfRestriction d) {
            return m_factory.getOWLObjectSelfRestriction(d.getProperty().getSimplified());
        }
        public OWLDescription visit(OWLObjectMinCardinalityRestriction d) {
            OWLDescription filler=getSimplified(d.getFiller());
            if (d.getCardinality()<=0)
                return m_factory.getOWLThing();
            else if (filler.isOWLNothing())
                return m_factory.getOWLNothing();
            else if (d.getCardinality()==1)
                return m_factory.getOWLObjectSomeRestriction(d.getProperty().getSimplified(),filler);
            else
                return m_factory.getOWLObjectMinCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality(),filler);
        }
        public OWLDescription visit(OWLObjectMaxCardinalityRestriction d) {
            OWLDescription filler=getSimplified(d.getFiller());
            if (filler.isOWLNothing())
                return m_factory.getOWLThing();
            else if (d.getCardinality()<=0)
                return m_factory.getOWLObjectAllRestriction(d.getProperty().getSimplified(),m_factory.getOWLObjectComplementOf(filler));
            else
                return m_factory.getOWLObjectMaxCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality(),filler);
        }
        public OWLDescription visit(OWLObjectExactCardinalityRestriction d) {
            OWLDescription filler=getSimplified(d.getFiller());
            if (d.getCardinality()<0)
                return m_factory.getOWLNothing();
            else if (d.getCardinality()==0)
                return m_factory.getOWLObjectAllRestriction(d.getProperty().getSimplified(),m_factory.getOWLObjectComplementOf(filler));
            else if (filler.isOWLNothing())
                return m_factory.getOWLNothing();
            else {
                OWLObjectMinCardinalityRestriction minCardinality=m_factory.getOWLObjectMinCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality(),filler);
                OWLObjectMaxCardinalityRestriction maxCardinality=m_factory.getOWLObjectMaxCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality(),filler);
                return m_factory.getOWLObjectIntersectionOf(minCardinality,maxCardinality);
            }
        }
        public OWLDescription visit(OWLDataSomeRestriction d) {
            OWLDataRange filler=getSimplified(d.getFiller());
            if (isBottomDataRange(filler))
                return m_factory.getOWLNothing();
            else
                return m_factory.getOWLDataSomeRestriction(d.getProperty(),filler);
        }
        public OWLDescription visit(OWLDataAllRestriction d) {
            OWLDataRange filler=getSimplified(d.getFiller());
            if (filler.isTopDataType())
                return m_factory.getOWLThing();
            else
                return m_factory.getOWLDataAllRestriction(d.getProperty(),filler);
        }
        public OWLDescription visit(OWLDataValueRestriction d) {
            OWLDataOneOf nominal=m_factory.getOWLDataOneOf(d.getValue());
            return m_factory.getOWLDataSomeRestriction(d.getProperty(),nominal);
        }
        public OWLDescription visit(OWLDataMinCardinalityRestriction d) {
            OWLDataRange filler=getSimplified(d.getFiller());
            if (d.getCardinality()<=0)
                return m_factory.getOWLThing();
            else if (isBottomDataRange(filler))
                return m_factory.getOWLNothing();
            else if (d.getCardinality()==1)
                return m_factory.getOWLDataSomeRestriction(d.getProperty(),filler);
            else
                return m_factory.getOWLDataMinCardinalityRestriction(d.getProperty(),d.getCardinality(),filler);
        }
        public OWLDescription visit(OWLDataMaxCardinalityRestriction d) {
            OWLDataRange filler=getSimplified(d.getFiller());
            if (isBottomDataRange(filler))
                return m_factory.getOWLThing();
            else if (d.getCardinality()<=0)
                return m_factory.getOWLDataAllRestriction(d.getProperty(),m_factory.getOWLDataComplementOf(filler));
            else
                return m_factory.getOWLDataMaxCardinalityRestriction(d.getProperty(),d.getCardinality(),filler);
        }
        public OWLDescription visit(OWLDataExactCardinalityRestriction d) {
            OWLDataRange filler=getSimplified(d.getFiller());
            if (d.getCardinality()<0)
                return m_factory.getOWLNothing();
            else if (d.getCardinality()==0)
                return m_factory.getOWLDataAllRestriction(d.getProperty(),m_factory.getOWLDataComplementOf(filler));
            else if (isBottomDataRange(filler))
                return m_factory.getOWLNothing();
            else {
                OWLDataMinCardinalityRestriction minCardinality=m_factory.getOWLDataMinCardinalityRestriction(d.getProperty(),d.getCardinality(),filler);
                OWLDataMaxCardinalityRestriction maxCardinality=m_factory.getOWLDataMaxCardinalityRestriction(d.getProperty(),d.getCardinality(),filler);
                return m_factory.getOWLObjectIntersectionOf(minCardinality,maxCardinality);
            }
        }
        protected boolean isBottomDataRange(OWLDataRange dataRange) {
            return dataRange instanceof OWLDataComplementOf && ((OWLDataComplementOf)dataRange).getDataRange().isTopDataType();
        }
    }
    
    protected class DataRangeSimplificationVisitor implements OWLDataVisitorEx<OWLDataRange> {
        public OWLDataRange visit(OWLDataType o) {
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
        public OWLDataRange visit(OWLDataRangeRestriction o) {
            return o;
        }
        public OWLDataRange visit(OWLDataRangeFacetRestriction o) {
            return null;
        }
        public OWLDataRange visit(OWLTypedConstant o) {
            return null;
        }
        public OWLDataRange visit(OWLUntypedConstant o) {
            return null;
        }
    }
}
