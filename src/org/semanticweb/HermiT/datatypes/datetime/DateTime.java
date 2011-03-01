/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory
   
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
package org.semanticweb.HermiT.datatypes.datetime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    // according to XML Schema 1.1 spec (http://www.w3.org/TR/xmlschema11-2/#dateTime) the reg exp is as follows:
//    -?([1-9][0-9]{3,}|0[0-9]{3})
//            -(0[1-9]|1[0-2])
//            -(0[1-9]|[12][0-9]|3[01])
//            T(([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\.[0-9]+)?|(24:00:00(\.0+)?))
//            (Z|(\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00))?
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
    protected final boolean m_lastDayInstant;
    protected final int m_timeZoneOffset;
    
    public DateTime(int year,int month,int day,int hour,int minute,int second,int millisecond,int timeZoneOffset) {
        m_timeOnTimeline=getTimeOnTimelineRaw(year,month,day,hour,minute,second,millisecond)-(timeZoneOffset==NO_TIMEZONE ? 0 : timeZoneOffset*60L*1000L);
        m_lastDayInstant=(hour==24 && minute==0 && second==0 && millisecond==0);
        m_timeZoneOffset=timeZoneOffset;
    }
    public DateTime(long timeOnTimeline,boolean lastDayInstant,int timeZoneOffset) {
        m_timeOnTimeline=timeOnTimeline;
        m_lastDayInstant=lastDayInstant;
        m_timeZoneOffset=timeZoneOffset;
    }
    public String toString() {
        long timeOnTimeline=m_timeOnTimeline;
        if (m_timeZoneOffset!=NO_TIMEZONE)
            timeOnTimeline+=m_timeZoneOffset*60L*1000L;
        int timePart=(int)(timeOnTimeline % (1000*60*60*24));
        long days=timeOnTimeline/(1000L*60L*60L*24L);
        if (timePart<0) {
            timePart+=1000*60*60*24;
            days--;
            assert timePart>=0;
        }
        int millisecond=timePart % 1000;
        timePart=timePart/1000;
        int second=timePart % 60;
        timePart=timePart/60;
        int minute=(int)(timePart % 60L);
        timePart=timePart/60;
        int hour=(int)(timePart % 24L);
        int year=(int)(days/367L);
        if (year>=0) {
            while (days>=daysToYearStart(year+1))
                year++;
            days-=daysToYearStart(year);
        }
        else {
            while (days<daysToYearStart(year-1))
                year--;
            year--;
            days-=daysToYearStart(year);
        }
        int month=1;
        int daysInMonth=daysInMonth(year,month);
        while (days>daysInMonth) {
            days-=daysInMonth;
            month++;
            daysInMonth=daysInMonth(year,month);
        }
        int day=((int)days)+1;
        if (day==0) {
            month--;
            if (month==0) {
                month=12;
                year--;
            }
            day=daysInMonth(year,month);
        }
        if (m_lastDayInstant) {
            assert hour==0 && minute==0 && second==0 && millisecond==0;
            hour=24;
            day--;
            if (day<=0) {
                month--;
                if (month<=0) {
                    month=12;
                    year--;
                }
                day=daysInMonth(year,month);
            }
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
        if (value<0)
            buffer.append('-');
        String stringAbsValue=String.valueOf(Math.abs(value));
        for (int i=digits-stringAbsValue.length();i>0;--i)
            buffer.append('0');
        buffer.append(stringAbsValue);
    }
    public static DateTime parse(String lexicalForm) {
        Matcher matcher=s_dateTimePattern.matcher(lexicalForm.trim());
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
        if (!(that instanceof DateTime) || that==null)
            return false;
        DateTime thatObject=(DateTime)that;
        return m_timeOnTimeline==thatObject.m_timeOnTimeline && m_lastDayInstant==thatObject.m_lastDayInstant && m_timeZoneOffset==thatObject.m_timeZoneOffset;
    }
    public int hashCode() {
        return (int)(m_timeOnTimeline*3L+m_timeZoneOffset+(m_lastDayInstant ? 117L : 0L));
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
    protected static long daysToYearStart(int year) {
        long yearMinusOne=year-1;
        return 365*yearMinusOne+(yearMinusOne/400)-(yearMinusOne/100)+(yearMinusOne/4);
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
    public static boolean isLastDayInstant(long timeOnTimeline) {
        return (timeOnTimeline % (1000L*60L*60L*24L))==0;
    }
    public static boolean secondsAreZero(long timeOnTimeline) {
        return (timeOnTimeline % (1000L*60L))==0;
    }
    public static int getMinutesInDay(long timeOnTimeline) {
        return (int)((timeOnTimeline/(1000L*60L)) % (24L*60L));
    }
}
