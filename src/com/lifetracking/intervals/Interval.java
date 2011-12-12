package com.lifetracking.intervals;

import java.util.Calendar;
import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import com.lifetracking.graphing.TimeAxis;

import android.text.format.DateFormat;

//Interval is a single value for an IntervalType. It's defined by start and end dates.
public class Interval {
	
	public IntervalType m_intervalType;
	public String m_note;
	public long m_start, m_end;//in seconds (offsets from MyLife.m_originDate) (end=-1 if it's not defined, i.e. the interval is "in progress")

	//Create empty new Interval.
	public Interval(){}
	
	//Create new Interval with a given name, starting at the given date.
	public Interval(IntervalType intervalType, Calendar start) {
		m_intervalType = intervalType;
		m_note = "";
		m_start = MyLife.getOffsetFromCalendar(start);
		m_end = -1;
	}
	
	//Set the start date for this Interval.
	public void setStartDate(Calendar start){
		m_start = MyLife.getOffsetFromCalendar(start);
	}
	
	//Set the end date for this Interval. If null is passed, then the end is erased and the interval is "in progress".
	public void setEndDate(Calendar end){
		m_end = end == null ? -1 : MyLife.getOffsetFromCalendar(end);
		if(m_end > 0 && m_end < m_start) m_end = m_start + 1;
	}
	
	//return - end offset, unless this interval is in progress, in which case return the offset of Now.
	public long getEndOrCurrent(){
		return isInProgress() ? MyLife.getOffsetFromCalendar(Calendar.getInstance()) : m_end;
	}

	//return - true if this interval is in progress.
	public boolean isInProgress(){
		return m_end < 0;
	}
	
	//return - length in seconds of this interval. If the interval is "in progress", then return start to current time.
	public long getDuration(){
		return getEndOrCurrent() - m_start;
	}
	
	//return - date when this interval started.
	public Calendar getStartDate(){
		return MyLife.getCalendarFromOffset(m_start);
	}
	
	//return - date when this interval ended.
	public Calendar getEndDate(){
		return MyLife.getCalendarFromOffset(m_end);
	}
	
	//return - date when this interval started formated according to the given format.
	public String formatStartDate(String format){
		return DateFormat.format(format, getStartDate().getTime()).toString();
	}
	
	//return - date when this interval ended formated according to the given format.
	public String formatEndDate(String format){
		return DateFormat.format(format, getEndDate().getTime()).toString();
	}
	
	//return - string corresponding to the date when this interval started.
	public String getStartDateString(){
		return DateFormat.getDateFormat(LifeTracking.g_globalContext).format(getStartDate().getTime());
	}
	
	//return - string corresponding to the time when this interval started.
	public String getStartTimeString(){
		return DateFormat.getTimeFormat(LifeTracking.g_globalContext).format(getStartDate().getTime());
	}
	
	//return - string corresponding to the date when this interval ended.
	public String getEndDateString(){
		return DateFormat.getDateFormat(LifeTracking.g_globalContext).format(getEndDate().getTime());
	}
	
	//return - string corresponding to the time when this interval ended.
	public String getEndTimeString(){
		return DateFormat.getTimeFormat(LifeTracking.g_globalContext).format(getEndDate().getTime());
	}
	
	//return - string corresponding to the time and date when this interval started.
	public String getStartTimeDateString(){
		return getStartTimeString() + ", " + getStartDateString();
	}
	
	//return - string corresponding to the time and date when this interval ended.
	public String getEndTimeDateString(){
		return getEndTimeString() + ", " + getEndDateString();
	}
	
	//return - a nice string representation of this interval.
	public String toString(){
		String note = m_note.length() > 0 ? (" Note: \"" + m_note + "\".") : "";
		if(isInProgress()){
			return m_intervalType.m_name + " interval started on " + getStartTimeDateString() + "." + note;
		} else {
			return m_intervalType.m_name + " interval is from " + getStartTimeDateString() + " to " + getEndTimeDateString() + ". (" + TimeAxis.secondsToString(getDuration()) + ")" + note;
		}
	}
}
