package org.semanticweb.HermiT.reasoner;

public class RIATest extends AbstractReasonerTest {

	 public RIATest(String name) {
	        super(name);
	 }

	 public void testInverseAndChain() throws Exception{
	     String axioms = "ObjectPropertyAssertion(:hasFemalePartner :marriage_of_david_and_margaret :margaret) " +
	                     "ObjectPropertyAssertion(:hasHusband :marriage_of_david_and_margaret :david) " +
                         "SubObjectPropertyOf(ObjectInverseOf(:hasWife) :isWifeOf) " +
//                         "SubObjectPropertyOf(:isWifeOf ObjectInverseOf(:hasWife)) " +
                         "SubObjectPropertyOf(ObjectInverseOf(:isWifeOf) :hasWife) " +
                         "SubObjectPropertyOf(ObjectInverseOf(:hasHusband) :isHusbandOf) " +
                         "SubObjectPropertyOf(ObjectPropertyChain(:isHusbandOf :hasFemalePartner) :hasWife) " +
                         "NegativeObjectPropertyAssertion(:isWifeOf :margaret :david) ";
	     loadReasonerWithAxioms(axioms);
	     assertFalse(m_reasoner.isConsistent());
	}
	 
    public void testRIARegularity0() throws Exception{
        // This is not really regular according to the OWL 2 specification due to the first axiom;
        // however, we simply don't care about it!
        String axioms = "SubObjectPropertyOf(:loves owl:topObjectProperty) " +
                        "SubObjectPropertyOf(ObjectPropertyChain(:pHuman owl:topObjectProperty :pCat) :loves) ";
        assertRegular(axioms,true);
     }
	 public void testRIARegularity1() throws Exception{
		 String axioms = "SubObjectPropertyOf(:A :B) " +
		 				 "SubObjectPropertyOf(:B :C) " +
		 				 "SubObjectPropertyOf(:C :D) " +
		 				 "SubObjectPropertyOf(:D :A) ";
	     assertRegular(axioms,true);
	 }
	 public void testRIARegularity2() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R :Q) :P) " +
		 				 "InverseObjectProperties(:P :Q) ";
	     assertRegular(axioms,false);
	 }
	 public void testRIARegularity3() throws Exception{
	     // The following is in disagreement with FaCT++
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R ObjectInverseOf(:Q)) :P) " +
		 				 "InverseObjectProperties(:P :Q) ";
	     assertRegular(axioms,false);
	 }
	 public void testRIARegularity4() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R :Q :P) :P) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:P :S) :Q) " +
		 				 "SubObjectPropertyOf(:Q :R) ";
	     assertRegular(axioms,false);
	 }
	 public void testRIARegularity5() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R :Q :P) :P) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:P :S) :L) " +
		 				 "SubObjectPropertyOf(:L :R) " +
		 				 "SubObjectPropertyOf(:R :L) ";
	     assertRegular(axioms,true);
	 }
	 public void testRIARegularity6() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:P ObjectInverseOf(:P) :P) :P) ";
	     assertRegular(axioms,false);
	 }
	 public void testRIARegularity7() throws Exception{
		 String axioms = "InverseObjectProperties(:P :P-) "+
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:L :P-) :L) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:R :L) :P) ";
	     assertRegular(axioms,false);
	 }
	 public void testRIARegularity8() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R4 :R1) :R1) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:R1 :R2) :R2) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:R2 :R3) :R3) " +
		 				 "SubObjectPropertyOf(ObjectPropertyChain(:R3 :R4) :R4) " +
		 				 "EquivalentObjectProperties( :R1 :R2 )" +
		 				 "EquivalentObjectProperties( :R2 :R3 )" +
		 				 "EquivalentObjectProperties( :R3 :R4 )" +
		 				 "EquivalentObjectProperties( :R4 :R1 )";
	     assertRegular(axioms,true);
	 }
	 public void testRIARegularity9() throws Exception{
		 String axioms = 	"SubObjectPropertyOf(ObjectPropertyChain(:R1 :R2 :R3) :R) " +
		 					"EquivalentObjectProperties( :R2 :R )";
	     assertRegular(axioms,false);
	 }
}
