package org.semanticweb.HermiT.owl_wg_tests;

import java.util.EnumSet;

import junit.framework.Test;

public class AllWGTestsWithStatus {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        return wgTestRegistry.createSuite(EnumSet.allOf(WGTestDescriptor.Status.class),false);
    }
}
