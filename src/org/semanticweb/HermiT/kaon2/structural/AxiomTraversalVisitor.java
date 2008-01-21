package org.semanticweb.HermiT.kaon2.structural;

import org.semanticweb.kaon2.api.*;
import org.semanticweb.kaon2.api.flogic.*;
import org.semanticweb.kaon2.api.logic.*;
import org.semanticweb.kaon2.api.owl.axioms.*;
import org.semanticweb.kaon2.api.owl.elements.*;

/**
 * This is the default visitor which traverses all axioms.
 */
public class AxiomTraversalVisitor extends KAON2VisitorAdapter {

    public Object visit(InverseObjectProperty object) {
        object.getObjectProperty().accept(this);
        return null;
    }
    public Object visit(QueryDefinition object) {
        for (Variable variable : object.getDistinguishedVariables())
            variable.accept(this);
        object.getFormula().accept(this);
        return null;
    }
    public Object visit(FunctionalTerm object) {
        for (int i=0;i<object.getArity();i++)
            object.getArgument(i).accept(this);
        return null;
    }
    public Object visit(Literal object) {
        object.getPredicate().accept(this);
        for (int i=0;i<object.getArity();i++)
            object.getArgument(i).accept(this);
        return null;
    }
    public Object visit(Rule object) {
        for (int i=0;i<object.getHeadLength();i++)
            object.getHeadFormula(i).accept(this);
        for (int i=0;i<object.getBodyLength();i++)
            object.getBodyFormula(i).accept(this);
        return null;
    }
    public Object visit(Conjunction object) {
        for (Formula conjunct : object.getConjuncts())
            conjunct.accept(this);
        return null;
    }
    public Object visit(Disjunction object) {
        for (Formula disjunct : object.getDisjuncts())
            disjunct.accept(this);
        return null;
    }
    public Object visit(ClassicalNegation object) {
        object.getFormula().accept(this);
        return null;
    }
    public Object visit(DefaultNegation object) {
        object.getFormula().accept(this);
        return null;
    }
    public Object visit(Forall object) {
        for (Variable variable : object.getBoundVariables())
            variable.accept(this);
        object.getFormula().accept(this);
        return null;
    }
    public Object visit(Exists object) {
        for (Variable variable : object.getBoundVariables())
            variable.accept(this);
        object.getFormula().accept(this);
        return null;
    }
    public Object visit(Implication object) {
        object.getAntecedent().accept(this);
        object.getConsequent().accept(this);
        return null;
    }
    public Object visit(Equivalence object) {
        object.getFormula1().accept(this);
        object.getFormula2().accept(this);
        return null;
    }
    public Object visit(FMolecule object) {
        object.getObject().accept(this);
        if (object.getSubClassOf()!=null)
            object.getSubClassOf().accept(this);
        if (object.getInstanceOf()!=null)
            object.getInstanceOf().accept(this);
        for (int methodCallIndex=0;methodCallIndex<object.getNumberOfMethodCalls();methodCallIndex++)
            object.getMethodCall(methodCallIndex).accept(this);
        return null;
    }
    public Object visit(FMethodCall object) {
        object.getMethodName().accept(this);
        for (int argumentIndex=0;argumentIndex<object.getNumberOfArguments();argumentIndex++)
            object.getArgument(argumentIndex).accept(this);
        for (int resultIndex=0;resultIndex<object.getNumberOfResults();resultIndex++) {
            FMolecule resultFMolecule=object.getResultFMolecule(resultIndex);
            if (resultFMolecule!=null)
                resultFMolecule.accept(this);
            else
                object.getResultTerm(resultIndex);
        }
        return null;
    }
    public Object visit(DataNot object) {
        object.getDataRange().accept(this);
        return null;
    }
    public Object visit(DataOneOf object) {
        for (Constant literalValue : object.getLiteralValues())
            literalValue.accept(this);
        return null;
    }
    public Object visit(DataAll object) {
        for (DataPropertyExpression dataProperty : object.getDataProperties())
            dataProperty.accept(this);
        object.getDataRange().accept(this);
        return null;
    }
    public Object visit(DataSome object) {
        for (DataPropertyExpression dataProperty : object.getDataProperties())
            dataProperty.accept(this);
        object.getDataRange().accept(this);
        return null;
    }
    public Object visit(DataCardinality object) {
        object.getDataProperty().accept(this);
        return null;
    }
    public Object visit(DataHasValue object) {
        object.getDataProperty().accept(this);
        object.getLiteralValue().accept(this);
        return null;
    }
    public Object visit(DatatypeRestriction object) {
        object.getDataRange().accept(this);
        return null;
    }
    public Object visit(ObjectAll object) {
        object.getObjectProperty().accept(this);
        object.getDescription().accept(this);
        return null;
    }
    public Object visit(ObjectSome object) {
        object.getObjectProperty().accept(this);
        object.getDescription().accept(this);
        return null;
    }
    public Object visit(ObjectExistsSelf object) {
        object.getObjectProperty().accept(this);
        return null;
    }
    public Object visit(ObjectCardinality object) {
        object.getObjectProperty().accept(this);
        object.getDescription().accept(this);
        return null;
    }
    public Object visit(ObjectOneOf object) {
        for (Individual individual : object.getIndividuals())
            individual.accept(this);
        return null;
    }
    public Object visit(ObjectHasValue object) {
        object.getObjectProperty().accept(this);
        object.getIndividual().accept(this);
        return null;
    }
    public Object visit(ObjectNot object) {
        object.getDescription().accept(this);
        return null;
    }
    public Object visit(ObjectOr object) {
        for (Description description : object.getDescriptions())
            description.accept(this);
        return null;
    }
    public Object visit(ObjectAnd object) {
        for (Description description : object.getDescriptions())
            description.accept(this);
        return null;
    }
    public Object visit(SubClassOf object) {
        visitAxiomAnnotations(object);
        object.getSubDescription().accept(this);
        object.getSuperDescription().accept(this);
        return null;
    }
    public Object visit(EquivalentClasses object) {
        visitAxiomAnnotations(object);
        for (Description description : object.getDescriptions())
            description.accept(this);
        return null;
    }
    public Object visit(DisjointClasses object) {
        visitAxiomAnnotations(object);
        for (Description description : object.getDescriptions())
            description.accept(this);
        return null;
    }
    public Object visit(DisjointUnion object) {
        visitAxiomAnnotations(object);
        object.getOWLClass().accept(this);
        for (Description description : object.getDescriptions())
            description.accept(this);
        return null;
    }
    public Object visit(DataPropertyAttribute object) {
        visitAxiomAnnotations(object);
        object.getDataProperty().accept(this);
        return null;
    }
    public Object visit(DataPropertyDomain object) {
        visitAxiomAnnotations(object);
        object.getDataProperty().accept(this);
        object.getDomain().accept(this);
        return null;
    }
    public Object visit(DataPropertyRange object) {
        visitAxiomAnnotations(object);
        object.getDataProperty().accept(this);
        object.getRange().accept(this);
        return null;
    }
    public Object visit(SubDataPropertyOf object) {
        visitAxiomAnnotations(object);
        object.getSubDataProperty().accept(this);
        object.getSuperDataProperty().accept(this);
        return null;
    }
    public Object visit(EquivalentDataProperties object) {
        visitAxiomAnnotations(object);
        for (DataPropertyExpression dataProperty : object.getDataProperties())
            dataProperty.accept(this);
        return null;
    }
    public Object visit(DisjointDataProperties object) {
        visitAxiomAnnotations(object);
        for (DataPropertyExpression dataProperty : object.getDataProperties())
            dataProperty.accept(this);
        return null;
    }
    public Object visit(ObjectPropertyAttribute object) {
        visitAxiomAnnotations(object);
        object.getObjectProperty().accept(this);
        return null;
    }
    public Object visit(ObjectPropertyDomain object) {
        visitAxiomAnnotations(object);
        object.getObjectProperty().accept(this);
        object.getDomain().accept(this);
        return null;
    }
    public Object visit(ObjectPropertyRange object) {
        visitAxiomAnnotations(object);
        object.getObjectProperty().accept(this);
        object.getRange().accept(this);
        return null;
    }
    public Object visit(SubObjectPropertyOf object) {
        visitAxiomAnnotations(object);
        for (ObjectPropertyExpression subObjectProperty : object.getSubObjectProperties())
            subObjectProperty.accept(this);
        object.getSuperObjectProperty().accept(this);
        return null;
    }
    public Object visit(EquivalentObjectProperties object) {
        visitAxiomAnnotations(object);
        for (ObjectPropertyExpression objectProperty : object.getObjectProperties())
            objectProperty.accept(this);
        return null;
    }
    public Object visit(DisjointObjectProperties object) {
        visitAxiomAnnotations(object);
        for (ObjectPropertyExpression objectProperty : object.getObjectProperties())
            objectProperty.accept(this);
        return null;
    }
    public Object visit(InverseObjectProperties object) {
        visitAxiomAnnotations(object);
        object.getFirst().accept(this);
        object.getSecond().accept(this);
        return null;
    }
    public Object visit(SameIndividual object) {
        visitAxiomAnnotations(object);
        for (Individual individual : object.getIndividuals())
            individual.accept(this);
        return null;
    }
    public Object visit(DifferentIndividuals object) {
        visitAxiomAnnotations(object);
        for (Individual individual : object.getIndividuals())
            individual.accept(this);
        return null;
    }
    public Object visit(DataPropertyMember object) {
        visitAxiomAnnotations(object);
        object.getDataProperty().accept(this);
        object.getSourceIndividual().accept(this);
        object.getTargetValue().accept(this);
        return null;
    }
    public Object visit(NegativeDataPropertyMember object) {
        visitAxiomAnnotations(object);
        object.getDataProperty().accept(this);
        object.getSourceIndividual().accept(this);
        object.getTargetValue().accept(this);
        return null;
    }
    public Object visit(ObjectPropertyMember object) {
        visitAxiomAnnotations(object);
        object.getObjectProperty().accept(this);
        object.getSourceIndividual().accept(this);
        object.getTargetIndividual().accept(this);
        return null;
    }
    public Object visit(NegativeObjectPropertyMember object) {
        visitAxiomAnnotations(object);
        object.getObjectProperty().accept(this);
        object.getSourceIndividual().accept(this);
        object.getTargetIndividual().accept(this);
        return null;
    }
    public Object visit(ClassMember object) {
        visitAxiomAnnotations(object);
        object.getDescription().accept(this);
        object.getIndividual().accept(this);
        return null;
    }
    public Object visit(AnnotationByConstant object) {
        object.getAnnotationProperty().accept(this);
        object.getAnnotationValue().accept(this);
        return null;
    }
    public Object visit(AnnotationByIndividual object) {
        object.getAnnotationProperty().accept(this);
        object.getAnnotationValue().accept(this);
        return null;
    }
    public Object visit(EntityAnnotation object) {
        visitAxiomAnnotations(object);
        object.getEntity().accept(this);
        object.getAnnotationProperty().accept(this);
        if (object.getAnnotationValue() instanceof Entity)
            ((Entity)object.getAnnotationValue()).accept(this);
        else if (object.getAnnotationValue() instanceof Constant)
            object.getAnnotationValue().accept(this);
        else
            object.getAnnotationValue().accept(this);
        return null;
    }
    public Object visit(Declaration object) {
        visitAxiomAnnotations(object);
        object.getEntity().accept(this);
        return null;
    }
    protected void visitAxiomAnnotations(OWLAxiom axiom) {
        if (!axiom.getAnnotations().isEmpty())
            for (Annotation annotation : axiom.getAnnotations())
                annotation.accept(this);
    }
}
