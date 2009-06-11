package org.semanticweb.HermiT.reasoner;

public class RIARegularityTest extends AbstractReasonerTest {
	
	 public RIARegularityTest(String name) {
	        super(name);
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
//	 The following is in dissagreement with FaCT++
	 public void testRIARegularity3() throws Exception{
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
}
