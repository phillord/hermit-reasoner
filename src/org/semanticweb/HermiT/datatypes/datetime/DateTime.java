// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.datetime;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DateTime {
    public static final int NO_TIMEZONE=Integer.MAX_VALUE;
    public static final long MAX_TIME_ZONE_CORRECTION=14L*60L*60L*1000L;
    
    protected static final Pattern s_dateTimePattern=Pattern.compile(
        "(-?[0-9]{4,})"+
        "-"+
        "([0-9]{2})"+
        "-"+
        "([0-9]{2})"+
        "T"+
        "([0-9]{2})"+
        ":"+
        "([0-9]{2})"+
        ":"+
        "([0-9]{2})([.]([0-9]{1,3}))?"+
        "((Z)|(([+]|-)([0-9]{2}):([0-9]{2})))?"
    );
    protected static final int YEAR_GROUP=1;
    protected static final int MONTH_GROUP=2;
    protected static final int DAY_GROUP=3;
    protected static final int HOUR_GROUP=4;
    protected static final int MINUTE_GROUP=5;
    protected static final int SECOND_WHOLE_GROUP=6;
    protected static final int SECOND_FRACTION_GROUP=8;
    protected static final int TZ_OFFSET_GROUP=9;
    protected static final int TZ_OFFSET_Z_GROUP=10;
    protected static final int TZ_OFFSET_SIGN_GROUP=12;
    protected static final int TZ_OFFSET_HOUR_GROUP=13;
    protected static final int TZ_OFFSET_MINUTE_GROUP=14;

    protected final long m_timeOnTimeline;
    protected final int m_timeZoneOffset;
    
    public DateTime(int year,int month,int day,int hour,int minute,int second,int millisecond,int timeZoneOffset) {
        m_timeOnTimeline=getTimeOnTimelineRaw(year,month,day,hour,minute,second,millisecond)-(timeZoneOffset==NO_TIMEZONE ? 0 : timeZoneOffset*60L*1000L);
        m_timeZoneOffset=timeZoneOffset;
    }
    public DateTime(long timeOnTimeline,int timeZoneOffset) {
        m_timeOnTimeline=timeOnTimeline;
        m_timeZoneOffset=timeZoneOffset;
    }
    public String toString() {
        long timeOnTimeline=m_timeOnTimeline;
        if (m_timeZoneOffset!=NO_TIMEZONE)
            timeOnTimeline+=m_timeZoneOffset*60L*1000L;
        int millisecond=(int)(timeOnTimeline % 1000L);
        timeOnTimeline=timeOnTimeline/1000L;
        int second=(int)(timeOnTimeline % 60L);
        timeOnTimeline=timeOnTimeline/60L;
        int minute=(int)(timeOnTimeline % 60L);
        timeOnTimeline=timeOnTimeline/60L;
        int hour=(int)(timeOnTimeline % 24L);
        long days=timeOnTimeline/24L;
        int year=(int)(days/366L);
        days-=daysToYearStart(year);
        int daysInYear=daysInYear(year);
        while (days>daysInYear) {
            days-=daysInYear;
            year++;
            daysInYear=daysInYear(year);
        }
        int month=1;
        int daysInMonth=daysInMonth(year,month);
        while (days>daysInMonth) {
            days-=daysInMonth;
            month++;
            daysInMonth=daysInMonth(year,month);
        }
        int day=((int)days)+1;
        if (day>daysInMonth(year,month)) {
            day=1;
            month++;
        }
        if (month>12) {
            month=1;
            year++;
        }
        StringBuffer buffer=new StringBuffer();
        appendPadded(buffer,year,4);
        buffer.append('-');
        appendPadded(buffer,month,2);
        buffer.append('-');
        appendPadded(buffer,day,2);
        buffer.append('T');
        appendPadded(buffer,hour,2);
        buffer.append(':');
        appendPadded(buffer,minute,2);
        buffer.append(':');
        appendPadded(buffer,second,2);
        if (millisecond>0) {
            buffer.append('.');
            appendPadded(buffer,millisecond,3);
        }
        if (m_timeZoneOffset!=NO_TIMEZONE)
            if (m_timeZoneOffset==0)
                buffer.append('Z');
            else {
                int absTimeZoneOffset;
                if (m_timeZoneOffset>0) {
                    buffer.append('+');
                    absTimeZoneOffset=m_timeZoneOffset;
                }
                else {
                    buffer.append('-');
                    absTimeZoneOffset=-m_timeZoneOffset;
                }
                int timeZoneHour=absTimeZoneOffset/60;
                int timeZoneMinute=absTimeZoneOffset % 60;
                appendPadded(buffer,timeZoneHour,2);
                buffer.append(':');
                appendPadded(buffer,timeZoneMinute,2);
            }
        return buffer.toString();
    }
    protected static long daysToYearStart(int year) {
        long yearMinusOne=year-1;
        return 365*yearMinusOne+(yearMinusOne/400)-(yearMinusOne/100)+(yearMinusOne/4);
    }
    protected static int daysInYear(int year) {
        return 365+((year % 4)!=0 || ((year % 100)==0 && (year % 400)!=0) ? 0 : 1);
    }
    public long getTimeOnTimeline() {
        return m_timeOnTimeline;
    }
    public boolean hasTimeZoneOffset() {
        return m_timeZoneOffset!=NO_TIMEZONE;
    }
    public int getTimeZoneOffset() {
        return m_timeZoneOffset;
    }
    protected void appendPadded(StringBuffer buffer,int value,int digits) {
        String stringValue=String.valueOf(value);
        for (int i=digits-stringValue.length();i>0;--i)
            buffer.append('0');
        buffer.append(stringValue);
    }
    public static DateTime parse(String lexicalForm) {
        Matcher matcher=s_dateTimePattern.matcher(lexicalForm);
        if (!matcher.matches())
            return null;
        try {
            int year=Integer.parseInt(matcher.group(YEAR_GROUP));
            int month=Integer.parseInt(matcher.group(MONTH_GROUP));
            int day=Integer.parseInt(matcher.group(DAY_GROUP));
            int hour=Integer.parseInt(matcher.group(HOUR_GROUP));
            int minute=Integer.parseInt(matcher.group(MINUTE_GROUP));
            int second=Integer.parseInt(matcher.group(SECOND_WHOLE_GROUP));
            // Milliseconds must be padded to exactly three characters so
            // that they can be parsed correctly!
            String millisecondString=matcher.group(SECOND_FRACTION_GROUP);
            int millisecond;
            if (millisecondString!=null) {
                while (millisecondString.length()<3)
                    millisecondString+='0';
                millisecond=Integer.parseInt(millisecondString);
            }
            else
                millisecond=0;
            if (year<-9999 || year>9999 ||
                month<=0 || month>12 ||
                day<=0 || day>daysInMonth(year,month) ||
                hour<0 || hour>24 || (hour==24 && (minute!=0 || second!=0 || millisecond!=0)) ||
                minute<0 || minute>=60 ||
                second<0 || second>=60 ||
                millisecond<0 || millisecond>=1000)
                return null;
            int timeZoneOffset;
            if (matcher.group(TZ_OFFSET_GROUP)==null)
                timeZoneOffset=NO_TIMEZONE;
            else {
                if (matcher.group(TZ_OFFSET_Z_GROUP)!=null)
                    timeZoneOffset=0;
                else {
                    int sign=("-".equals(matcher.group(TZ_OFFSET_SIGN_GROUP)) ? -1 : 1);
                    int timeZoneOffsetHour=Integer.parseInt(matcher.group(TZ_OFFSET_HOUR_GROUP));
                    int timeZoneOffsetMinute=Integer.parseInt(matcher.group(TZ_OFFSET_MINUTE_GROUP));
                    if (timeZoneOffsetHour<0 || timeZoneOffsetHour>14 || (timeZoneOffsetHour==14 && timeZoneOffsetMinute!=0) || 
                        timeZoneOffsetMinute<0 || timeZoneOffsetMinute>=60)
                        return null;
                    else
                        timeZoneOffset=sign*(timeZoneOffsetHour*60+timeZoneOffsetMinute);
                }
            }
            return new DateTime(year,month,day,hour,minute,second,millisecond,timeZoneOffset);
        }
        catch (NumberFormatException nfe) {
            return null;
        }
    }
    public boolean equals(Object that) {
        if (this==that)
            return true;
        DateTime thatObject=(DateTime)that;
        return m_timeOnTimeline==thatObject.m_timeOnTimeline && m_timeZoneOffset==thatObject.m_timeZoneOffset;
    }
    public int hashCode() {
        return (int)(m_timeOnTimeline+m_timeZoneOffset);
    }
    protected long getTimeOnTimelineRaw(int year,int month,int day,int hour,int minute,int second,int millisecond) {
        long yearMinusOne=year-1;
        long timeOnTimeline=31536000L*yearMinusOne;
        timeOnTimeline+=86400L*(yearMinusOne/400-yearMinusOne/100+yearMinusOne/4);
        for (int monthIndex=1;monthIndex<month;monthIndex++)
            timeOnTimeline+=86400L*daysInMonth(year,monthIndex);
        timeOnTimeline+=86400L*(day-1);
        timeOnTimeline+=3600L*hour+60L*minute+second;
        timeOnTimeline=timeOnTimeline*1000L+millisecond;
        return timeOnTimeline;
    }
    protected static int daysInMonth(int year,int month) {
        if (month==2) {
            if ((year % 4)!=0 || ((year % 100)==0 && (year % 400)!=0))
                return 28;
            else
                return 29;
        }
        else if (month==4 || month==6 || month==9 || month==11)
            return 30;
        else
            return 31;
    }
}
