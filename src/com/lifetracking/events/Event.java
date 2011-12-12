package com.lifetracking.events;

import java.util.Calendar;
import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import android.text.format.DateFormat;

//Event is a single value for an EventType. It's defined by a date.
public class Event {
	
	public EventType m_eventType;
	public String m_note;
	public long m_start;//in seconds (offsets from MyLife.m_originDate)

	//Create empty new Event.
	public Event(){}
	
	//Create new Event with the given date.
	public Event(EventType eventType, Calendar start) {
		m_eventType = eventType;
		m_note = "";
		m_start = MyLife.getOffsetFromCalendar(start);
	}
	
	//Set the start date for this Event.
	public void setStartDate(Calendar start){
		m_start = MyLife.getOffsetFromCalendar(start);
	}
	
	//return - date when this event started.
	public Calendar getStartDate(){
		return MyLife.getCalendarFromOffset(m_start);
	}
	
	//return - date when this event occured formated according to the given format.
	public String formatStartDate(String format){
		return DateFormat.format(format, getStartDate().getTime()).toString();
	}
	
	//return - string corresponding to the date when this event occured.
	public String getStartDateString(){
		return DateFormat.getDateFormat(LifeTracking.g_globalContext).format(getStartDate().getTime());
	}
	
	//return - string corresponding to the time when this event occured.
	public String getStartTimeString(){
		return DateFormat.getTimeFormat(LifeTracking.g_globalContext).format(getStartDate().getTime());
	}
	
	//return - string corresponding to the time and date when this event occured.
	public String getStartTimeDateString(){
		return getStartTimeString() + ", " + getStartDateString();
	}
	
	//return - a nice string representation of this event.
	public String toString(){
		String note = m_note.length() > 0 ? (" Note: \"" + m_note + "\".") : "";
		return m_eventType.m_name + " event added on " + getStartTimeDateString() + "." + note;
	}
}
