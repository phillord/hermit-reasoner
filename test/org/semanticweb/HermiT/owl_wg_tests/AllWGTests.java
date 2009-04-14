package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;

public class AllWGTests {
    public static Test suite() throws Exception {
        return WGTestRegistry.createSuite(false);
    }
}
