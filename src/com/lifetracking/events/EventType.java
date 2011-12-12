package com.lifetracking.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import com.lifetracking.R;
import com.lifetracking.SaveLoad;
import com.lifetracking.graphing.EventGraph;
import com.lifetracking.graphing.GraphingOptions;
import com.lifetracking.ui.GuiHelper;

//EventType is a collection of Events.
public class EventType {
	
	//============================================ STATIC ================================================
	
	//Store all user eventTypes. DO NOT insert events manually.
	public static ArrayList<EventType> m_eventTypes;//public: READ-ONLY
	public static String m_eventTypesPrefix = "Event";
	static {
		m_eventTypes = new ArrayList<EventType>();
	}
	
	//Add various fake event types with data for debug/test purposes.
	public static void addExampleEventTypes(){
		Random r = new Random();
		EventType t = new EventType("Example");
		addEventType(t);
		
		long offset = 1600000000L;
		for(int n = 0; n < 60; n++){
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTimeInMillis(MyLife.m_originDate.getTimeInMillis() + offset);
			t.addEvent(calendarStart).m_note = "2twoosies";
			offset += 400000000L * (0.4 * r.nextDouble() + 0.6);
		}
	}
	
	//Add the given event type to the global list of event types.
	//return - the given event type.
	public static EventType addEventType(EventType eventType){
		m_eventTypes.add(eventType);
		SaveLoad.saveEventType(eventType);
		return eventType;
	}
	
	//Add a new event type with the given name, but only if there is no event type with the same name.
	//return - event type with the given name (newly created if not found).
	public static EventType addEventType(String eventTypeName, boolean onlyIfUniqueName){
		if(onlyIfUniqueName){
			for(EventType t : m_eventTypes){
				if(eventTypeName.compareTo(t.m_name) == 0) return t;
			}
		}
		return addEventType(new EventType(eventTypeName));
	}
	
	//Remove the event type at the given index from the list of event types and delete corresponding file.
	public static void removeEventType(int index){
		removeEventType(m_eventTypes.get(index));
	}
	
	//Remove the event type from the list of event types and delete corresponding file.
	public static void removeEventType(EventType eventType){
		m_eventTypes.remove(eventType);
		SaveLoad.deleteEventType(eventType);
	}
	
	//======================================== END STATIC ================================================
	
	public long m_id;//acts as a unique id for the type
	public String m_name;
	public Event m_lastAddedEvent;
	public ArrayList<Event> m_events;//public: READ-ONLY
	public EventGraph m_graph;
	public GraphingOptions m_graphingOptions;
	
	//Create new EventType with the given name.
	public EventType(String name){
		m_id = new Random().nextLong();
		m_name = name;
		m_events = new ArrayList<Event>();
		m_graph = new EventGraph(this);
		m_graphingOptions = new GraphingOptions();
	}
	
	//return - true if this track's graph is assigned to a GraphView.
	public boolean isGraphed(){
		return m_graph.isGraphed();
	}
	
	//Add a new event with the given start date.
	//return - new event.
	public Event addEvent(Calendar startDate){
		return addEvent(new Event(this, startDate));
	}
	
	//Add the given event.
	//return - added event.
	public Event addEvent(Event event){
		//Insert the value into a sorted order by start date.
		for(int n = 0; n < m_events.size(); n++){
			if(m_events.get(n).m_start > event.m_start){
				m_events.add(n, event);
				return event;
			}
		}
		m_events.add(m_events.size(), event);
		return event;
	}
	
	//Remove event at the given index.
	public void removeEvent(int index){
		Event event = m_events.get(index);
		if(event == m_lastAddedEvent) m_lastAddedEvent = null;
		m_events.remove(index);
	}
	
	//Remove the given event.
	public void removeEvent(Event event){
		if(event == m_lastAddedEvent) m_lastAddedEvent = null;
		m_events.remove(event);
	}
	
	//return - true if this can be graphed.
	public boolean canGraph(){
		return m_events.size() >= 2;
	}
	
	//Create a Dialog for editing an event.
	public Dialog showEditEventDialog(final Event event){
		final EventType eventType = this;
		final Calendar newStartDate = event.getStartDate();
		
		//Just reuse add_interval_dialog here.
		View layout = LifeTracking.g_inflater.inflate(R.layout.add_interval_dialog, null, false);
		layout.findViewById(R.id.AddIntervalDialog_EndLabel).setVisibility(View.GONE);
		layout.findViewById(R.id.AddIntervalDialog_EndLayout).setVisibility(View.GONE);

		final EditText noteText = (EditText) layout.findViewById(R.id.AddIntervalDialog_NoteText);
		noteText.setText(event.m_note);
		
		layout.findViewById(R.id.AddIntervalDialog_LastValueText).setVisibility(View.GONE);
		GuiHelper.setupDateTimeButtons(layout, R.id.AddIntervalDialog_StartTimeButton, R.id.AddIntervalDialog_StartDateButton, newStartDate);

		//Create the "Edit Event" dialog.
		AlertDialog.Builder builder = new AlertDialog.Builder(LifeTracking.g_globalContext);
		builder.setView(layout)
			.setMessage("Edit event")
			.setPositiveButton("Edit", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
				eventType.removeEvent(event);
				event.setStartDate(newStartDate);
				event.m_note = noteText.getText().toString();
				eventType.addEvent(event);
				SaveLoad.saveEventType(eventType);
				Toast.makeText(LifeTracking.g_globalContext, "Event edited", Toast.LENGTH_SHORT).show();
			}})
			.setNegativeButton("Cancel", null);
		return builder.show();
	}
}
