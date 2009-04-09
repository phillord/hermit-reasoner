package org.semanticweb.HermiT.datatypes.datetime;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DateTime {
    public static final int NO_TIMEZONE=Integer.MAX_VALUE;
    public static final long MAX_TIME_ZONE_CORRECTION=14L*60L*60L*1000L;
    public static final DateTime MINUS_INFINITY=new DateTime(Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE);
    public static final DateTime PLUS_INFINITY=new DateTime(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE);
    
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
    
    protected final int m_year;
    protected final int m_month;
    protected final int m_day;
    protected final int m_hour;
    protected final int m_minute;
    protected final int m_second;
    protected final int m_millisecond;
    protected final int m_timeZoneOffset;
    protected final int m_hashCode;
    
    public DateTime(int year,int month,int day,int hour,int minute,int second,int millisecond,int timeZoneOffset) {
        m_year=year;
        m_month=month;
        m_day=day;
        m_hour=hour;
        m_minute=minute;
        m_second=second;
        m_millisecond=millisecond;
        m_timeZoneOffset=timeZoneOffset;
        m_hashCode=m_year+m_month*3+m_day*5+m_hour*7+m_minute*11+m_second*13+m_millisecond*17+m_timeZoneOffset*19;
    }
    public String toString() {
        if (this==MINUS_INFINITY)
            return "-INF";
        else if (this==PLUS_INFINITY)
            return "+INF";
        else {
            StringBuffer buffer=new StringBuffer();
            appendPadded(buffer,m_year,4);
            buffer.append('-');
            appendPadded(buffer,m_month,2);
            buffer.append('-');
            appendPadded(buffer,m_day,2);
            buffer.append('T');
            appendPadded(buffer,m_hour,2);
            buffer.append(':');
            appendPadded(buffer,m_minute,2);
            buffer.append(':');
            appendPadded(buffer,m_second,2);
            if (m_millisecond>0) {
                buffer.append('.');
                appendPadded(buffer,m_millisecond,3);
            }
            if (m_timeZoneOffset!=NO_TIMEZONE)
                if (m_timeZoneOffset==0)
                    buffer.append('Z');
                else {
                    int timeZoneHour=m_timeZoneOffset/60;
                    int timeZoneMinute=m_timeZoneOffset % 60;
                    appendPadded(buffer,timeZoneHour,2);
                    buffer.append(':');
                    appendPadded(buffer,timeZoneMinute,2);
                }
            return buffer.toString();
        }
    }
    public int getYear() {
        return m_year;
    }
    public int getMonth() {
        return m_month;
    }
    public int getDay() {
        return m_day;
    }
    public int getHour() {
        return m_hour;
    }
    public int getMinute() {
        return m_minute;
    }
    public int getSecond() {
        return m_second;
    }
    public int getMillisecond() {
        return m_millisecond;
    }
    public boolean hasTimeZoneOffset() {
        return m_timeZoneOffset!=NO_TIMEZONE;
    }
    public int getTimeZoneOffset() {
        return m_timeZoneOffset;
    }
    protected void appendPadded(StringBuffer buffer,int value,int digits) {
        if (value<0) {
            buffer.append('-');
            value=-value;
        }
        String stringValue=String.valueOf(value);
        for (int i=digits-stringValue.length();i>0;--i)
            buffer.append('0');
        buffer.append(stringValue);
    }
    public boolean isSmallerThan(DateTime that) {
        int comparison=getType()-that.getType();
        if (comparison!=0)
            return comparison<0;
        else if (this==MINUS_INFINITY || this==PLUS_INFINITY)
            return false;
        else {
            long thisTimeOnTimeline=getTimeOnTimeline();
            long thatTimeOnTimeline=that.getTimeOnTimeline();
            if (hasTimeZoneOffset()!=that.hasTimeZoneOffset()) {
                if (!hasTimeZoneOffset())
                    thisTimeOnTimeline+=MAX_TIME_ZONE_CORRECTION;
                if (!that.hasTimeZoneOffset())
                    thatTimeOnTimeline-=MAX_TIME_ZONE_CORRECTION;
            }
            return thisTimeOnTimeline<thatTimeOnTimeline;
        }
    }
    public boolean isSmallerThanOrEqualTo(DateTime that) {
        int comparison=getType()-that.getType();
        if (comparison!=0)
            return comparison<0;
        else if (this==MINUS_INFINITY || this==PLUS_INFINITY)
            return true;
        else {
            long thisTimeOnTimeline=getTimeOnTimeline();
            long thatTimeOnTimeline=that.getTimeOnTimeline();
            if (hasTimeZoneOffset()!=that.hasTimeZoneOffset()) {
                if (!hasTimeZoneOffset())
                    thisTimeOnTimeline+=MAX_TIME_ZONE_CORRECTION;
                if (!that.hasTimeZoneOffset())
                    thatTimeOnTimeline-=MAX_TIME_ZONE_CORRECTION;
                // A time instant that has a time zone offset is never equal
                // to a time instant that does not have a time zone offset.
                // Because of that, the following comparison user < rather than <=.
                return thisTimeOnTimeline<thatTimeOnTimeline;
            }
            return thisTimeOnTimeline<=thatTimeOnTimeline;
        }
    }
    public boolean isGreaterThan(DateTime that) {
        int comparison=getType()-that.getType();
        if (comparison!=0)
            return comparison>0;
        else if (this==MINUS_INFINITY || this==PLUS_INFINITY)
            return false;
        else {
            long thisTimeOnTimeline=getTimeOnTimeline();
            long thatTimeOnTimeline=that.getTimeOnTimeline();
            if (hasTimeZoneOffset()!=that.hasTimeZoneOffset()) {
                if (!hasTimeZoneOffset())
                    thisTimeOnTimeline-=MAX_TIME_ZONE_CORRECTION;
                if (!that.hasTimeZoneOffset())
                    thatTimeOnTimeline+=MAX_TIME_ZONE_CORRECTION;
            }
            return thisTimeOnTimeline>thatTimeOnTimeline;
        }
    }
    public boolean isGreaterThanOrEqualTo(DateTime that) {
        int comparison=getType()-that.getType();
        if (comparison!=0)
            return comparison>0;
        else if (this==MINUS_INFINITY || this==PLUS_INFINITY)
            return true;
        else {
            long thisTimeOnTimeline=getTimeOnTimeline();
            long thatTimeOnTimeline=that.getTimeOnTimeline();
            if (hasTimeZoneOffset()!=that.hasTimeZoneOffset()) {
                if (!hasTimeZoneOffset())
                    thisTimeOnTimeline-=MAX_TIME_ZONE_CORRECTION;
                if (!that.hasTimeZoneOffset())
                    thatTimeOnTimeline+=MAX_TIME_ZONE_CORRECTION;
                // A time instant that has a time zone offset is never equal
                // to a time instant that does not have a time zone offset.
                // Because of that, the following comparison user > rather than >=.
                return thisTimeOnTimeline>thatTimeOnTimeline;
            }
            return thisTimeOnTimeline>=thatTimeOnTimeline;
        }
    }
    protected int getType() {
        if (this==MINUS_INFINITY)
            return 0;
        else if (this==PLUS_INFINITY)
            return 2;
        else
            return 1;
    }
    public long getTimeOnTimeline() {
        long yearMinusOne=m_year-1;
        long timeZoneOffset=(m_timeZoneOffset==NO_TIMEZONE ? 0 : m_timeZoneOffset);
        long timeOnTimeline=31536000L*yearMinusOne+86400L*(yearMinusOne/400-yearMinusOne/100+yearMinusOne/4);
        for (int month=1;month<m_month;month++)
            timeOnTimeline+=86400L*daysInMonth(m_year,month);
        timeOnTimeline=(timeOnTimeline+86400L*(m_day-1)+3600L*m_hour+60L*(m_minute-timeZoneOffset)+m_second)*1000L+m_millisecond;
        return timeOnTimeline;
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
            int millisecond=Integer.parseInt(matcher.group(SECOND_FRACTION_GROUP));
            if (year<=-10000 || year>=10000 ||
                month<=0 || month>12 ||
                day<=0 || day>daysInMonth(year,month) ||
                minute<0 || minute>=60 ||
                second<0 || second>=60 ||
                hour<0 || hour>24 || (hour==24 && (minute!=0 || second!=0 || millisecond!=0)) ||
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
        else if (this==MINUS_INFINITY || this==MINUS_INFINITY || !(that instanceof DateTime))
            return false;
        DateTime thatObject=(DateTime)that;
        return
            m_year==thatObject.m_year &&
            m_month==thatObject.m_month &&
            m_day==thatObject.m_day &&
            m_hour==thatObject.m_hour &&
            m_minute==thatObject.m_minute &&
            m_second==thatObject.m_second &&
            m_millisecond==thatObject.m_millisecond &&
            m_timeZoneOffset==thatObject.m_timeZoneOffset;
    }
    public int hashCode() {
        return m_hashCode;
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
