package org.semanticweb.HermiT.reasoner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.semanticweb.HermiT.datatypes.binarydata.BinaryData;
import org.semanticweb.HermiT.datatypes.binarydata.BinaryDataLengthInterval;
import org.semanticweb.HermiT.datatypes.binarydata.BinaryDataType;

public class BinaryDataTest extends AbstractReasonerTest {

    public BinaryDataTest(String name) {
        super(name);
    }
    public void testParsing_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:hexBinary"),
            OO(HEXB("0AFF"))
        );
    }
    public void testParsing_2() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:base64Binary"),
            OO(HEXB("0AFF"))
        );
    }
    public void testLength_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:hexBinary","xsd:length",INT("2")),
            OO(HEXB("0AFF"))
        );
    }
    public void testLength_2() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:hexBinary","xsd:length",INT("3")),
            OO(HEXB("0AFF"))
        );
    }
    public void testLength_3() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:hexBinary","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:hexBinary","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(HEXB("0AFF"))
        );
    }
    public void testLength_4() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:hexBinary","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:hexBinary","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(HEXB("0AFF0AFF0AFF"))
        );
    }
    public void testLength_5() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:hexBinary","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:hexBinary","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(HEXB("0AFF0AFF0A"))
        );
    }
    public void testSize_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:hexBinary","xsd:length",INT("0"))
        );
    }
    public void testSize_2() throws Exception {
        assertDRSatisfiable(false,2,
            DR("xsd:hexBinary","xsd:length",INT("0"))
        );
    }
    public void testSize_3() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:hexBinary","xsd:length",INT("0")),
            NOT(OO(HEXB("")))
        );
    }
    public void testIntersection_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:hexBinary","xsd:minLength",INT("0")),
            NOT(DR("xsd:hexBinary","xsd:minLength",INT("1")))
        );
    }
    public void testIntersection_2() throws Exception {
        assertDRSatisfiable(false,2,
            DR("xsd:hexBinary","xsd:minLength",INT("0")),
            NOT(DR("xsd:hexBinary","xsd:minLength",INT("1")))
        );
    }
    public void testExplicitSize() throws Exception {
        BinaryDataLengthInterval imax2=interval(BinaryDataType.HEX_BINARY,0,2);
        assertEquals(10000000,imax2.subtractSizeFrom(10000000+1+256+256*256));

        BinaryDataLengthInterval imin1max2=interval(BinaryDataType.HEX_BINARY,1,2);
        assertEquals(10000000,imin1max2.subtractSizeFrom(10000000+256+256*256));
    }
    public void testEnumerate1() throws Exception {
        BinaryDataLengthInterval imax1=interval(BinaryDataType.HEX_BINARY,0,1);
        List<Object> values=new ArrayList<Object>();
        imax1.enumerateValues(values);
        List<Object> control=new ArrayList<Object>();
        addAllOfLength(control,BinaryDataType.HEX_BINARY,new byte[0],0);
        addAllOfLength(control,BinaryDataType.HEX_BINARY,new byte[1],0);
        assertContainsAll(values,control.toArray());
    }
    public void testEnumerate2() throws Exception {
        BinaryDataLengthInterval iexact1=interval(BinaryDataType.HEX_BINARY,1,1);
        List<Object> values=new ArrayList<Object>();
        iexact1.enumerateValues(values);
        List<Object> control=new ArrayList<Object>();
        addAllOfLength(control,BinaryDataType.HEX_BINARY,new byte[1],0);
        assertContainsAll(values,control.toArray());
    }
    protected void addAllOfLength(Collection<Object> result,BinaryDataType type,byte[] buffer,int position) {
        if (position==buffer.length) {
            BinaryData binaryData=new BinaryData(type,buffer.clone());
            result.add(binaryData);
        }
        else {
            for (int b=0;b<=255;b++) {
                buffer[position]=(byte)b;
                addAllOfLength(result,type,buffer,position+1);
            }
        }
    }
    protected static BinaryDataLengthInterval interval(BinaryDataType type,int minLength,int maxLength) {
        return new BinaryDataLengthInterval(type,minLength,maxLength);
    }
    public void testBase64Parsing() {
        BinaryData data1=BinaryData.parseBase64Binary("ZXdyZA==");
        assertBinaryData(data1,"ewrd");
        BinaryData data2=BinaryData.parseBase64Binary(" Z  X d y Z A ==  ");
        assertBinaryData(data2,"ewrd");
    }
    protected static void assertBinaryData(BinaryData data,String string) {
        assertEquals(string.length(),data.getNumberOfBytes());
        for (int i=0;i<string.length();i++)
            assertEquals(string.charAt(i),data.getByte(i));
    }
}
