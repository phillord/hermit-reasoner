package org.semanticweb.HermiT.reasoner;

public class SimpleRolesTest extends AbstractReasonerTest {
	
	 public SimpleRolesTest(String name) {
	        super(name);
	 }
	 
	 public void testSimpleRoles1() throws Exception{
		 String axioms = "TransitiveObjectProperty(:R) " +
		 				 "SubObjectPropertyOf(:R :P) " +
		 				 "SubClassOf(:C ObjectMinCardinality(2 :P))";
	     assertSimple(axioms,false);
	 }
	 public void testSimpleRoles2() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R :Q) :R) " +
			 			 "SubObjectPropertyOf(:R :P) " +
		 				 "SubClassOf(:C ObjectMaxCardinality(2 :P))";
	     assertSimple(axioms,false);
	 }
	 public void testSimpleRoles3() throws Exception{
		 String axioms = "SubObjectPropertyOf(ObjectPropertyChain(:R :Q) :R) " +
			 			 "SubObjectPropertyOf(:R :S) " +
		 				 "InverseObjectProperties(:S :S-) " +
		 				 "SubClassOf(:C ObjectMaxCardinality(2 :S-))";
	     assertSimple(axioms,false);
	 }
	 public void testSimpleRoles4() throws Exception{
		 String axioms = "TransitiveObjectProperty(:R-) " + 
		 				 "SubObjectPropertyOf(:R :P) " +
			 			 "SubObjectPropertyOf(:P :S) " +
		 				 "InverseObjectProperties(:R :R-) " +
		 				 "InverseObjectProperties(:S :S-) " +
		 				 "SubClassOf(:C ObjectMaxCardinality(2 :S-))";
	     assertSimple(axioms,false);
	 }
}
