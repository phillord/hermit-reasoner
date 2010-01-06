package org.semanticweb.HermiT.reasoner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.HermiT.datatypes.datetime.BoundType;
import org.semanticweb.HermiT.datatypes.datetime.DateTime;
import org.semanticweb.HermiT.datatypes.datetime.DateTimeInterval;
import org.semanticweb.HermiT.datatypes.datetime.IntervalType;

public class DateTimeTest extends AbstractReasonerTest {

    public DateTimeTest(String name) {
        super(name);
    }
    public void testParsing() {
        // Test positive dates
        assertParsing("1926-11-26T16:24:10.200Z");
        assertParsing("1926-11-26T24:00:00Z");
        assertParsing("1926-11-26T16:24:10.200+02:00");
        assertParsing("1926-01-01T16:24:10.200+02:00");
        assertParsing("1926-12-21T16:24:10.200+02:00");
        assertParsing("1926-12-31T16:24:10.200+02:00");
        assertParsing("1926-12-31T00:00:00+02:00");
        // Test year = 0
        assertParsing("0000-01-01T00:00:00Z");
        assertParsing("0000-01-01T00:00:00+14:00");
        assertParsing("0000-01-01T00:00:00-02:00");
        assertParsing("0000-01-01T00:00:00+02:00");
        assertParsing("0000-01-01T24:00:00+02:00");
        // Test year = -1
        assertParsing("-0001-12-31T24:00:00Z");
        assertParsing("-0001-12-31T23:59:59Z");
        assertParsing("-0001-12-31T23:59:59+14:00");
        assertParsing("-0001-12-31T24:00:00+14:00");
        // Test negative dates
        assertParsing("-1926-11-26T16:24:10.200Z");
        assertParsing("-1926-11-26T24:00:00Z");
        assertParsing("-1926-11-26T16:24:10.200+02:00");
        assertParsing("-1926-01-01T16:24:10.200+02:00");
        assertParsing("-1926-12-21T16:24:10.200+02:00");
        assertParsing("-1926-12-31T16:24:10.200+02:00");
        assertParsing("-1926-12-31T00:00:00+02:00");
    }
    public void testExactIntervalsWithoutTZ1() {
        DateTime time1=getTime("1926-11-26T14:00:00");
        DateTime time2=getTime("1926-11-26T14:00:00Z");
        DateTimeInterval interval=wihtoutTZ(time1);
        assertEquals(100000,interval.subtractSizeFrom(100001));
        assertTrue(interval.containsDateTime(time1));
        assertFalse(interval.containsDateTime(time2));
        assertElements(time1,interval,time1);
    }
    public void testExactIntervalsWithoutTZ2() {
        DateTime time1=getTime("1926-11-26T24:00:00");
        DateTime time2=getTime("1926-11-27T00:00:00");
        assertEquals(time1.getTimeOnTimeline(),time2.getTimeOnTimeline());
        DateTime time3=getTime("1926-11-26T24:00:00Z");
        DateTimeInterval interval=wihtoutTZ(time1);
        assertEquals(100000,interval.subtractSizeFrom(100002));
        assertTrue(interval.containsDateTime(time1));
        assertTrue(interval.containsDateTime(time2));
        assertFalse(interval.containsDateTime(time3));
        assertElements(time1,interval,time1,time2);
    }
    public void testExactIntervalsWithTZ1() throws Exception {
        DateTime time1=getTime("1926-11-26T04:00:01Z");
        DateTime time2=getTime("1926-11-26T04:00:01");
        DateTimeInterval interval=wihtTZ(time1);
        assertEquals(100000,interval.subtractSizeFrom(100000+840+840+1));
        assertTrue(interval.containsDateTime(time1));
        assertFalse(interval.containsDateTime(time2));
        assertElements(time1,interval,loadDateTimes("res/datetime-1.txt"));
    }
    public void testExactIntervalsWithTZ2() throws Exception {
        DateTime time1=getTime("1926-11-26T11:00:00Z");
        DateTime time2=getTime("1926-11-26T11:00:00");
        DateTimeInterval interval=wihtTZ(time1);
        assertEquals(100000,interval.subtractSizeFrom(100000+840+840+1+1+1));
        assertTrue(interval.containsDateTime(time1));
        assertFalse(interval.containsDateTime(time2));
        assertElements(time1,interval,loadDateTimes("res/datetime-2.txt"));
    }
    public void testExactIntervalsWithTZ3() throws Exception {
        DateTime time1=getTime("-1926-11-26T11:00:00Z");
        DateTime time2=getTime("-1926-11-26T11:00:00");
        DateTimeInterval interval=wihtTZ(time1);
        assertEquals(100000,interval.subtractSizeFrom(100000+840+840+1+1+1));
        assertTrue(interval.containsDateTime(time1));
        assertFalse(interval.containsDateTime(time2));
        assertElements(time1,interval,loadDateTimes("res/datetime-3.txt"));
    }
    public void testFinite1_1() throws Exception {
        assertDRSatisfiable(true,2,
            DR("xsd:dateTime","xsd:minInclusive",DATE("1965-04-15T00:00:00"),"xsd:maxInclusive",DATE("1965-04-15T00:00:00"))
        );
    }
    public void testFinite1_2() throws Exception {
        assertDRSatisfiable(false,3,
            DR("xsd:dateTime","xsd:minInclusive",DATE("1965-04-15T00:00:00"),"xsd:maxInclusive",DATE("1965-04-15T00:00:00"))
        );
    }
    public void testFinite2_1() throws Exception {
        assertDRSatisfiable(true,4,
            DR("xsd:dateTime","xsd:minInclusive",DATE("1965-04-15T00:00:00"),"xsd:maxInclusive",DATE("1965-05-01T00:00:00")),
            NOT(DR("xsd:dateTime","xsd:minExclusive",DATE("1965-04-15T00:00:00"),"xsd:maxExclusive",DATE("1965-05-01T00:00:00")))
        );
    }
    public void testFinite2_2() throws Exception {
        assertDRSatisfiable(false,5,
            DR("xsd:dateTime","xsd:minInclusive",DATE("1965-04-15T00:00:00"),"xsd:maxInclusive",DATE("1965-05-01T00:00:00")),
            NOT(DR("xsd:dateTime","xsd:minExclusive",DATE("1965-04-15T00:00:00"),"xsd:maxExclusive",DATE("1965-05-01T00:00:00")))
        );
    }
    public void testInfinite_1() throws Exception {
        assertDRSatisfiable(true,100,
            DR("xsd:dateTime","xsd:minInclusive",DATE("1965-04-15T00:00:00"),"xsd:maxInclusive",DATE("1965-05-01T00:00:00.001"))
        );
    }
    public void testInfinite_2() throws Exception {
        assertDRSatisfiable(true,100,
            DR("xsd:dateTime","xsd:minInclusive",DATE("1965-04-15T00:00:00Z"),"xsd:maxInclusive",DATE("1965-05-01T00:00:00.001Z"))
        );
    }
    public void testMizedTZs_1() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:dateTime","xsd:minInclusive",DATE("1965-04-15T00:00:00"),"xsd:maxInclusive",DATE("1965-04-15T00:00:01Z"))
        );
    }
    public void testMizedTZs_2() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:dateTime","xsd:minInclusive",DATE("1965-04-15T00:00:00"),"xsd:maxInclusive",DATE("1965-04-15T00:00:00-14:00"))
        );
    }
    public void testMizedTZs_3() throws Exception {
        assertDRSatisfiable(true,100,
            DR("xsd:dateTime","xsd:minInclusive",DATE("1965-04-15T00:00:00"),"xsd:maxInclusive",DATE("1965-04-15T00:00:00.001-14:00"))
        );
    }
    public void testMember() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:dateTime","xsd:minInclusive",DATE("1965-04-15T00:00:00Z"),"xsd:maxInclusive",DATE("1965-05-01T00:00:00Z")),
            NOT(DR("xsd:dateTime","xsd:minInclusive",DATE("1965-04-17T00:00:00"),"xsd:maxInclusive",DATE("1965-04-18T00:00:00Z"))),
            OO(DATE("1965-04-15T00:00:00Z")),
            NOT(OO(DATE("1965-04-15T00:00:00+00:01")))
        );
    }
    protected static String getTZO(int offset) {
        int offsetAbs=Math.abs(offset);
        int min=offsetAbs % 60;
        int hour=offsetAbs / 60;
        return (offset<0 ? "-" : "+")+padTwo(hour)+":"+padTwo(min);
    }
    protected static void assertParsing(String lexicalForm) {
        DateTime dateTime=getTime(lexicalForm);
        assertEquals(lexicalForm,dateTime.toString());
    }
    protected static DateTime getTime(String lexicalForm) {
        return DateTime.parse(lexicalForm);
    }
    protected static DateTimeInterval wihtoutTZ(DateTime dateTime) {
        return new DateTimeInterval(IntervalType.WITHOUT_TIMEZONE,dateTime.getTimeOnTimeline(),BoundType.INCLUSIVE,dateTime.getTimeOnTimeline(),BoundType.INCLUSIVE);
    }
    protected static DateTimeInterval wihtTZ(DateTime dateTime) {
        return new DateTimeInterval(IntervalType.WITH_TIMEZONE,dateTime.getTimeOnTimeline(),BoundType.INCLUSIVE,dateTime.getTimeOnTimeline(),BoundType.INCLUSIVE);
    }
    protected static void assertElements(DateTime baseDateTime,DateTimeInterval interval,DateTime... expectedDateTimes) {
        List<Object> dateTimes=new ArrayList<Object>();
        interval.enumerateDateTimes(dateTimes);
        assertContainsAll(dateTimes,(Object[])expectedDateTimes);
        for (DateTime dateTime : expectedDateTimes)
            assertEquals(baseDateTime.getTimeOnTimeline(),dateTime.getTimeOnTimeline());
    }
    protected static String padTwo(int value) {
        String string=String.valueOf(value);
        if (string.length()==1)
            string='0'+string;
        return string;
    }
    protected static DateTime[] loadDateTimes(String resourceName) throws Exception {
        List<DateTime> dateTimes=new ArrayList<DateTime>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(DateTimeTest.class.getResourceAsStream(resourceName)));
        String line=reader.readLine();
        while (line!=null) {
            dateTimes.add(DateTime.parse(line));
            line=reader.readLine();
        }
        reader.close();
        DateTime[] dateTimesArray=new DateTime[dateTimes.size()];
        dateTimes.toArray(dateTimesArray);
        return dateTimesArray;
    }
}
