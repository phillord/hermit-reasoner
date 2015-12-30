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
// An update for the tests (all.rdf) should regularly be downloaded to the 
// ontologies folder from http://wiki.webont.org/exports/
package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;
@SuppressWarnings("javadoc")
public class FailingWGTestDebug {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry = new WGTestRegistry();
        TestSuite suite = new TestSuite("OWL WG Debugging Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            // if (wgTestDescriptor.isDLTest() &&
            // (wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED ||
            // wgTestDescriptor.status==WGTestDescriptor.Status.PROPOSED ||
            // wgTestDescriptor.status==null)) {
            if (wgTestDescriptor.identifier.startsWith("New-Feature-Keys-004")) {
                wgTestDescriptor.addTestsToSuite(suite);
            }
        // }
        return suite;
    }
}
