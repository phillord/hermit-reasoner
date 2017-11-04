/* Copyright 2009 by the Oxford University Computing Laboratory
   
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

/**
 * Expression manager.
 */
public class ExpressionManager {
    protected final OWLDataFactory m_factory;
    protected final DescriptionNNFVisitor m_descriptionNNFVisitor;
    protected final DataRangeNNFVisitor m_dataRangeNNFVisitor;
    protected final DescriptionComplementNNFVisitor m_descriptionComplementNNFVisitor;
    protected final DataRangeComplementNNFVisitor m_dataRangeComplementNNFVisitor;
    protected final DescriptionSimplificationVisitor m_descriptionSimplificationVisitor;
    protected final DataRangeSimplificationVisitor m_dataRangeSimplificationVisitor;
    
    /**
     * @param factory factory
     */
    public ExpressionManager(OWLDataFactory factory) {
        m_factory=factory;
        m_descriptionNNFVisitor=new DescriptionNNFVisitor();
        m_dataRangeNNFVisitor=new DataRangeNNFVisitor();
        m_descriptionComplementNNFVisitor=new DescriptionComplementNNFVisitor();
        m_dataRangeComplementNNFVisitor=new DataRangeComplementNNFVisitor();
        m_descriptionSimplificationVisitor=new DescriptionSimplificationVisitor();
        m_dataRangeSimplificationVisitor=new DataRangeSimplificationVisitor();
    }
    <T> List<T> transform(List<T> t, Function<T, T> f){
        List<T> list=new ArrayList<>(t);
        for(int i=0;i<list.size();i++) {
            list.set(i, f.apply(list.get(i)));
        }
        return list;
    }
    /**
     * @param description description
     * @return normalised form
     */
    public OWLClassExpression getNNF(OWLClassExpression description) {
        return description.accept(m_descriptionNNFVisitor);
    }
    /**
     * @param dataRange description
     * @return normalised form
     */
    public OWLDataRange getNNF(OWLDataRange dataRange) {
        return dataRange.accept(m_dataRangeNNFVisitor);
    }
    /**
     * @param description description
     * @return normalised form
     */
    public OWLClassExpression getComplementNNF(OWLClassExpression description) {
        return description.accept(m_descriptionComplementNNFVisitor);
    }
    /**
     * @param dataRange description
     * @return normalised form
     */
    public OWLDataRange getComplementNNF(OWLDataRange dataRange) {
        return dataRange.accept(m_dataRangeComplementNNFVisitor);
    }
    /**
     * @param description description
     * @return normalised form
     */
    public OWLClassExpression getSimplified(OWLClassExpression description) {
        return description.accept(m_descriptionSimplificationVisitor);
    }
    /**
     * @param dataRange description
     * @return normalised form
     */
    public OWLDataRange getSimplified(OWLDataRange dataRange) {
        return dataRange.accept(m_dataRangeSimplificationVisitor);
    }

    // -----------------------------------------------------------------------------------
    // NNF
    // -----------------------------------------------------------------------------------
    
    protected class DescriptionNNFVisitor implements OWLClassExpressionVisitorEx<OWLClassExpression> {
        @Override
        public OWLClassExpression visit(OWLClass d) {
            return d;
        }
        @Override
        public OWLClassExpression visit(OWLObjectIntersectionOf d) {
            List<OWLClassExpression> list= transform(d.getOperandsAsList(), ExpressionManager.this::getNNF);
            return m_factory.getOWLObjectIntersectionOf(list);
        }
        @Override
        public OWLClassExpression visit(OWLObjectUnionOf d) {
            List<OWLClassExpression> list=transform(d.getOperandsAsList(), ExpressionManager.this::getNNF);
            return m_factory.getOWLObjectUnionOf(list);
        }
        @Override
        public OWLClassExpression visit(OWLObjectComplementOf d) {
            return getComplementNNF(d.getOperand());
        }
        @Override
        public OWLClassExpression visit(OWLObjectOneOf d) {
            return d;
        }
        @Override
        public OWLClassExpression visit(OWLObjectSomeValuesFrom d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectSomeValuesFrom(d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLObjectAllValuesFrom d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectAllValuesFrom(d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLObjectHasValue d) {
            return m_factory.getOWLObjectHasValue(d.getProperty(),d.getFiller());
        }
        @Override
        public OWLClassExpression visit(OWLObjectHasSelf d) {
            return m_factory.getOWLObjectHasSelf(d.getProperty());
        }
        @Override
        public OWLClassExpression visit(OWLObjectMinCardinality d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectMinCardinality(d.getCardinality(),d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLObjectMaxCardinality d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectMaxCardinality(d.getCardinality(),d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLObjectExactCardinality d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectExactCardinality(d.getCardinality(),d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLDataSomeValuesFrom d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataSomeValuesFrom(d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLDataAllValuesFrom d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataAllValuesFrom(d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLDataHasValue d) {
            return d;
        }
        @Override
        public OWLClassExpression visit(OWLDataMinCardinality d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataMinCardinality(d.getCardinality(),d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLDataMaxCardinality d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataMaxCardinality(d.getCardinality(),d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLDataExactCardinality d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataExactCardinality(d.getCardinality(),d.getProperty(),filler);
        }
    }
    
    protected class DataRangeNNFVisitor implements OWLDataVisitorEx<OWLDataRange> {
        @Override
        public OWLDataRange visit(OWLDatatype o) {
            return o;
        }
        @Override
        public OWLDataRange visit(OWLDataComplementOf o) {
            return getComplementNNF(o.getDataRange());
        }
        @Override
        public OWLDataRange visit(OWLDataOneOf o) {
            return o;
        }
        public OWLDataRange visit(OWLDataRange o) {
            return o;
        }
        @Override
        public OWLDataRange visit(OWLDatatypeRestriction o) {
            return o;
        }
        @SuppressWarnings("null")
        @Override
        public OWLDataRange visit(OWLFacetRestriction node) {
            return null;
        }
        @SuppressWarnings("null")
        @Override
        public OWLDataRange visit(OWLLiteral o) {
            return null;
        }
        @Override
        public OWLDataRange visit(OWLDataIntersectionOf range) {
            return m_factory.getOWLDataIntersectionOf(range.operands().map(dr->getNNF(dr)));
        }
        @Override
        public OWLDataRange visit(OWLDataUnionOf range) {
            return m_factory.getOWLDataUnionOf(range.operands().map(dr->getNNF(dr)));
         }
    }

    // -----------------------------------------------------------------------------------
    // Complement NNF
    // -----------------------------------------------------------------------------------
    
    protected class DescriptionComplementNNFVisitor implements OWLClassExpressionVisitorEx<OWLClassExpression> {
        @Override
        public OWLClassExpression visit(OWLClass d) {
            if (d.isOWLThing())
                return m_factory.getOWLNothing();
            if (d.isOWLNothing())
                return m_factory.getOWLThing();
            return m_factory.getOWLObjectComplementOf(d);
        }
        @Override
        public OWLClassExpression visit(OWLObjectIntersectionOf d) {
            List<OWLClassExpression> list=transform(d.getOperandsAsList(), ExpressionManager.this::getComplementNNF);
            return m_factory.getOWLObjectUnionOf(list);
        }
        @Override
        public OWLClassExpression visit(OWLObjectUnionOf d) {
            List<OWLClassExpression> list=transform(d.getOperandsAsList(), ExpressionManager.this::getComplementNNF);
            return m_factory.getOWLObjectIntersectionOf(list);
        }
        @Override
        public OWLClassExpression visit(OWLObjectComplementOf d) {
            return getNNF(d.getOperand());
        }
        @Override
        public OWLClassExpression visit(OWLObjectOneOf d) {
            return m_factory.getOWLObjectComplementOf(d);
        }
        @Override
        public OWLClassExpression visit(OWLObjectSomeValuesFrom d) {
            OWLClassExpression filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLObjectAllValuesFrom(d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLObjectAllValuesFrom d) {
            OWLClassExpression filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLObjectSomeValuesFrom(d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLObjectHasValue d) {
            return m_factory.getOWLObjectComplementOf(getNNF(d));
        }
        @Override
        public OWLClassExpression visit(OWLObjectHasSelf d) {
            return m_factory.getOWLObjectComplementOf(getNNF(d));
        }
        @Override
        public OWLClassExpression visit(OWLObjectMinCardinality d) {
            if (d.getCardinality()==0)
                return m_factory.getOWLNothing();
            else {
                OWLClassExpression filler=getNNF(d.getFiller());
                return m_factory.getOWLObjectMaxCardinality(d.getCardinality()-1,d.getProperty(),filler);
            }
        }
        @Override
        public OWLClassExpression visit(OWLObjectMaxCardinality d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            return m_factory.getOWLObjectMinCardinality(d.getCardinality()+1,d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLObjectExactCardinality d) {
            OWLClassExpression filler=getNNF(d.getFiller());
            if (d.getCardinality()==0)
                return m_factory.getOWLObjectMinCardinality(1,d.getProperty(),filler);
            else {
                return m_factory.getOWLObjectUnionOf(Stream.of(
                        m_factory.getOWLObjectMaxCardinality(d.getCardinality()-1,d.getProperty(),filler),
                        m_factory.getOWLObjectMinCardinality(d.getCardinality()+1,d.getProperty(),filler)));
            }
        }
        @Override
        public OWLClassExpression visit(OWLDataSomeValuesFrom d) {
            OWLDataRange filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLDataAllValuesFrom(d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLDataAllValuesFrom d) {
            OWLDataRange filler=getComplementNNF(d.getFiller());
            return m_factory.getOWLDataSomeValuesFrom(d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLDataHasValue d) {
            return m_factory.getOWLObjectComplementOf(d);
        }
        @Override
        public OWLClassExpression visit(OWLDataMinCardinality d) {
            if (d.getCardinality()==0)
                return m_factory.getOWLNothing();
            else {
                OWLDataRange filler=getNNF(d.getFiller());
                return m_factory.getOWLDataMaxCardinality(d.getCardinality()-1,d.getProperty(),filler);
            }
        }
        @Override
        public OWLClassExpression visit(OWLDataMaxCardinality d) {
            OWLDataRange filler=getNNF(d.getFiller());
            return m_factory.getOWLDataMinCardinality(d.getCardinality()+1,d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLDataExactCardinality d) {
            OWLDataRange filler=getNNF(d.getFiller());
            if (d.getCardinality()==0)
                return m_factory.getOWLDataMinCardinality(1,d.getProperty(),filler);
            else {
                return m_factory.getOWLObjectUnionOf(Stream.of(
                        m_factory.getOWLDataMaxCardinality(d.getCardinality()-1,d.getProperty(),filler),
                        m_factory.getOWLDataMinCardinality(d.getCardinality()+1,d.getProperty(),filler)));
            }
        }
    }
    
    protected class DataRangeComplementNNFVisitor implements OWLDataVisitorEx<OWLDataRange> {
        @Override
        public OWLDataRange visit(OWLDatatype o) {
            return m_factory.getOWLDataComplementOf(o);
        }
        @Override
        public OWLDataRange visit(OWLDataComplementOf o) {
            return getNNF(o.getDataRange());
        }
        @Override
        public OWLDataRange visit(OWLDataOneOf o) {
            return m_factory.getOWLDataComplementOf(o);
        }
        @Override
        public OWLDataRange visit(OWLDatatypeRestriction o) {
            return m_factory.getOWLDataComplementOf(o);
        }
        @SuppressWarnings("null")
        @Override
        public OWLDataRange visit(OWLFacetRestriction o) {
            return null;
        }
        @SuppressWarnings("null")
        @Override
        public OWLDataRange visit(OWLLiteral o) {
            return null;
        }
        @Override
        public OWLDataRange visit(OWLDataIntersectionOf range) {
            return m_factory.getOWLDataUnionOf(range.operands().map(dr->getComplementNNF(dr)));
        }
        @Override
        public OWLDataRange visit(OWLDataUnionOf range) {
            return m_factory.getOWLDataIntersectionOf(range.operands().map(dr->getComplementNNF(dr)));
         }
    }

    // -----------------------------------------------------------------------------------
    // Simplification
    // -----------------------------------------------------------------------------------
    
    protected class DescriptionSimplificationVisitor implements OWLClassExpressionVisitorEx<OWLClassExpression> {
        @Override
        public OWLClassExpression visit(OWLClass d) {
            return d;
        }
        @Override
        public OWLClassExpression visit(OWLObjectIntersectionOf d) {
            List<OWLClassExpression> newConjuncts=new ArrayList<>();
            for (OWLClassExpression description : d.getOperandsAsList()) {
                OWLClassExpression descriptionSimplified=getSimplified(description);
                if (descriptionSimplified.isOWLThing())
                    continue;
                else if (descriptionSimplified.isOWLNothing())
                    return m_factory.getOWLNothing();
                else if (descriptionSimplified instanceof OWLObjectIntersectionOf) {
                    newConjuncts.addAll(((OWLObjectIntersectionOf)descriptionSimplified).getOperandsAsList());
                }
                else
                    newConjuncts.add(descriptionSimplified);
            }
            return d;
        }
        @Override
        public OWLClassExpression visit(OWLObjectUnionOf d) {
            List<OWLClassExpression> newDisjuncts=new ArrayList<>();
            for (OWLClassExpression description : d.getOperandsAsList()) {
                OWLClassExpression descriptionSimplified=getSimplified(description);
                if (descriptionSimplified.isOWLThing())
                    return m_factory.getOWLThing();
                else if (descriptionSimplified.isOWLNothing())
                    continue;
                else if (descriptionSimplified instanceof OWLObjectUnionOf)
                    newDisjuncts.addAll(((OWLObjectUnionOf)descriptionSimplified).getOperandsAsList());
                else
                    newDisjuncts.add(descriptionSimplified);
            }
            return m_factory.getOWLObjectUnionOf(newDisjuncts);
        }
        @Override
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
        @Override
        public OWLClassExpression visit(OWLObjectOneOf d) {
            return d;
        }
        @Override
        public OWLClassExpression visit(OWLObjectSomeValuesFrom d) {
            OWLClassExpression filler=getSimplified(d.getFiller());
            if (filler.isOWLNothing())
                return m_factory.getOWLNothing();
            else
                return m_factory.getOWLObjectSomeValuesFrom(d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLObjectAllValuesFrom d) {
            OWLClassExpression filler=getSimplified(d.getFiller());
            if (filler.isOWLThing())
                return m_factory.getOWLThing();
            else
                return m_factory.getOWLObjectAllValuesFrom(d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLObjectHasValue d) {
            OWLObjectOneOf nominal=m_factory.getOWLObjectOneOf(d.getFiller());
            return m_factory.getOWLObjectSomeValuesFrom(d.getProperty(),nominal);
        }
        @Override
        public OWLClassExpression visit(OWLObjectHasSelf d) {
            return m_factory.getOWLObjectHasSelf(d.getProperty());
        }
        @Override
        public OWLClassExpression visit(OWLObjectMinCardinality d) {
            OWLClassExpression filler=getSimplified(d.getFiller());
            if (d.getCardinality()<=0)
                return m_factory.getOWLThing();
            else if (filler.isOWLNothing())
                return m_factory.getOWLNothing();
            else if (d.getCardinality()==1)
                return m_factory.getOWLObjectSomeValuesFrom(d.getProperty(),filler);
            else
                return m_factory.getOWLObjectMinCardinality(d.getCardinality(),d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLObjectMaxCardinality d) {
            OWLClassExpression filler=getSimplified(d.getFiller());
            if (filler.isOWLNothing())
                return m_factory.getOWLThing();
            else if (d.getCardinality()<=0)
                return m_factory.getOWLObjectAllValuesFrom(d.getProperty(),m_factory.getOWLObjectComplementOf(filler));
            else
                return m_factory.getOWLObjectMaxCardinality(d.getCardinality(),d.getProperty(),filler);
        }
        @Override
        public OWLClassExpression visit(OWLObjectExactCardinality d) {
            OWLClassExpression filler=getSimplified(d.getFiller());
            if (d.getCardinality()<0)
                return m_factory.getOWLNothing();
            else if (d.getCardinality()==0)
                return m_factory.getOWLObjectAllValuesFrom(d.getProperty(),m_factory.getOWLObjectComplementOf(filler));
            else if (filler.isOWLNothing())
                return m_factory.getOWLNothing();
            else {
                OWLObjectMinCardinality minCardinality=m_factory.getOWLObjectMinCardinality(d.getCardinality(),d.getProperty(),filler);
                OWLObjectMaxCardinality maxCardinality=m_factory.getOWLObjectMaxCardinality(d.getCardinality(),d.getProperty(),filler);
                return m_factory.getOWLObjectIntersectionOf(minCardinality,maxCardinality);
            }
        }
        @Override
        public OWLClassExpression visit(OWLDataSomeValuesFrom d) {
            if (isBottomDataRange(d.getFiller())) {
                return m_factory.getOWLNothing();
            }
            if (isBottomDataRange(getSimplified(d.getFiller())))
                return m_factory.getOWLNothing();
            else
                return m_factory.getOWLDataSomeValuesFrom(d.getProperty(),getSimplified(d.getFiller()));
        }
        @Override
        public OWLClassExpression visit(OWLDataAllValuesFrom d) {
            if (d.getFiller().isTopDatatype()) {
                return m_factory.getOWLThing();
            }
            if (getSimplified(d.getFiller()).isTopDatatype())
                return m_factory.getOWLThing();
            else
                return m_factory.getOWLDataAllValuesFrom(d.getProperty(),getSimplified(d.getFiller()));
        }
        @Override
        public OWLClassExpression visit(OWLDataHasValue d) {
            OWLDataOneOf nominal=m_factory.getOWLDataOneOf(d.getFiller());
            return m_factory.getOWLDataSomeValuesFrom(d.getProperty(),nominal);
        }
        @Override
        public OWLClassExpression visit(OWLDataMinCardinality d) {
            if (d.getCardinality()<=0)
                return m_factory.getOWLThing();
            else if (isBottomDataRange(getSimplified(d.getFiller())))
                return m_factory.getOWLNothing();
            else if (d.getCardinality()==1)
                return m_factory.getOWLDataSomeValuesFrom(d.getProperty(),getSimplified(d.getFiller()));
            else
                return m_factory.getOWLDataMinCardinality(d.getCardinality(),d.getProperty(),getSimplified(d.getFiller()));
        }
        @Override
        public OWLClassExpression visit(OWLDataMaxCardinality d) {
            if (isBottomDataRange(d.getFiller())) {
                return m_factory.getOWLThing();
            }
            if (isBottomDataRange(getSimplified(d.getFiller())))
                return m_factory.getOWLThing();
            else if (d.getCardinality()<=0)
                return m_factory.getOWLDataAllValuesFrom(d.getProperty(),m_factory.getOWLDataComplementOf(getSimplified(d.getFiller())));
            else
                return m_factory.getOWLDataMaxCardinality(d.getCardinality(),d.getProperty(),getSimplified(d.getFiller()));
        }
        @Override
        public OWLClassExpression visit(OWLDataExactCardinality d) {
            if (d.getCardinality()<0)
                return m_factory.getOWLNothing();
            OWLDataRange filler=getSimplified(d.getFiller());
             if (d.getCardinality()==0)
                return m_factory.getOWLDataAllValuesFrom(d.getProperty(),m_factory.getOWLDataComplementOf(filler));
            if (isBottomDataRange(filler))
                return m_factory.getOWLNothing();
            else {
                OWLDataMinCardinality minCardinality=m_factory.getOWLDataMinCardinality(d.getCardinality(),d.getProperty(),filler);
                OWLDataMaxCardinality maxCardinality=m_factory.getOWLDataMaxCardinality(d.getCardinality(),d.getProperty(),filler);
                return m_factory.getOWLObjectIntersectionOf(minCardinality,maxCardinality);
            }
        }
        protected boolean isBottomDataRange(OWLDataRange dataRange) {
            return dataRange instanceof OWLDataComplementOf && ((OWLDataComplementOf)dataRange).getDataRange().isTopDatatype();
        }
    }
    
    protected class DataRangeSimplificationVisitor implements OWLDataVisitorEx<OWLDataRange> {
        @Override
        public OWLDataRange visit(OWLDatatype o) {
            return o;
        }
        @Override
        public OWLDataRange visit(OWLDataComplementOf o) {
            OWLDataRange dataRangeSimplified=getSimplified(o.getDataRange());
            if (dataRangeSimplified instanceof OWLDataComplementOf)
                return ((OWLDataComplementOf)dataRangeSimplified).getDataRange();
            else
                return m_factory.getOWLDataComplementOf(dataRangeSimplified);
        }
        @Override
        public OWLDataRange visit(OWLDataOneOf o) {
            return o;
        }
        @Override
        public OWLDataRange visit(OWLDatatypeRestriction o) {
            return o;
        }
        @SuppressWarnings("null")
        @Override
        public OWLDataRange visit(OWLFacetRestriction o) {
            return null;
        }
        @SuppressWarnings("null")
        @Override
        public OWLDataRange visit(OWLLiteral o) {
            return null;
        }
        @Override
        public OWLDataRange visit(OWLDataIntersectionOf range) {
            List<OWLDataRange> newConjuncts=new ArrayList<>();
            for (OWLDataRange dr : range.getOperandsAsList()) {
                if (dr.isTopDatatype())
                    continue;
                OWLDataRange drSimplified=getSimplified(dr);
                if (drSimplified.isTopDatatype())
                    continue;
                else if (drSimplified instanceof OWLDataIntersectionOf)
                    newConjuncts.addAll(((OWLDataIntersectionOf)drSimplified).getOperandsAsList());
                else
                    newConjuncts.add(drSimplified);
            }
            return m_factory.getOWLDataIntersectionOf(newConjuncts);
        }
        @Override
        public OWLDataRange visit(OWLDataUnionOf range) {
            List<OWLDataRange> newDisjuncts=new ArrayList<>();
            for (OWLDataRange dr : range.getOperandsAsList()) {
                if (dr.isTopDatatype())
                    return m_factory.getTopDatatype();
                OWLDataRange drSimplified=getSimplified(dr);
                if (drSimplified.isTopDatatype())
                    return m_factory.getTopDatatype();
                else if (drSimplified instanceof OWLDataUnionOf)
                    newDisjuncts.addAll(((OWLDataUnionOf)drSimplified).getOperandsAsList());
                else
                    newDisjuncts.add(drSimplified);
            }
            return m_factory.getOWLDataUnionOf(newDisjuncts);
        }
    }
}
