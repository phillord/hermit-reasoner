package org.semanticweb.HermiT.owl_wg_tests;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;


// silly name because otherwise maven tries to run this as a test. Which fails
public class TstDescriptorForMaven{

    public static Collection<Object[]> getEntailmentTestParams() throws Exception{ 
        // PWL code
        List<Object[]> params = new ArrayList<Object[]>();


        // from ALLNonRejectedExtracreditWGTests
        WGTestRegistry wgTestRegistry = new WGTestRegistry();
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors()){
            if (wgTestDescriptor.isDLTest()
                &&(wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED || wgTestDescriptor.status==WGTestDescriptor.Status.PROPOSED)
                ){
                
                boolean useDisjunctionLearning = 
                    !(wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-209") || 
                      wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-208"));
                
                //WGTestDescriptor code
                for( WGTestDescriptor.TestType testType: WGTestDescriptor.TestType.values() ){

                    if( wgTestDescriptor.testTypes.contains( testType ) && 
                        wgTestDescriptor.isDLTest() ){
                        if( testType == WGTestDescriptor.TestType.POSITIVE_ENTAILMENT ){
                            Object[] param = {wgTestDescriptor,true,null,useDisjunctionLearning};
                            params.add( param );
                            continue;
                        }
                        if( testType == WGTestDescriptor.TestType.NEGATIVE_ENTAILMENT ){
                            Object[] param = {wgTestDescriptor,false,null,useDisjunctionLearning};
                            params.add( param );
                            continue;
                        }
                    }
                }
            }
        }

        return params;
        
    }

    // this is largely the same as above...
    public static Collection<Object[]> getConsistencyTestParams() throws Exception{
        // PWL code
        List<Object[]> params = new ArrayList<Object[]>();


        // from ALLNonRejectedExtracreditWGTests
        WGTestRegistry wgTestRegistry = new WGTestRegistry();
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors()){
            if (wgTestDescriptor.isDLTest()
                &&(wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED || wgTestDescriptor.status==WGTestDescriptor.Status.PROPOSED)
                ){
                
                boolean useDisjunctionLearning = 
                    !(wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-209") || 
                      wgTestDescriptor.identifier.startsWith("WebOnt-description-logic-208"));
                
                //WGTestDescriptor code
                for( WGTestDescriptor.TestType testType: WGTestDescriptor.TestType.values() ){

                    if( wgTestDescriptor.testTypes.contains( testType ) && 
                        wgTestDescriptor.isDLTest() ){
                        if( testType == WGTestDescriptor.TestType.CONSISTENCY ){
                            Object[] param = {wgTestDescriptor,true,null,useDisjunctionLearning};
                            params.add( param );
                            continue;
                        }
                        if( testType == WGTestDescriptor.TestType.INCONSISTENCY ){
                            Object[] param = {wgTestDescriptor,false,null,useDisjunctionLearning};
                            params.add( param );
                            continue;
                        }
                    }
                }
            }
        }

        return params;
    }
        

}
