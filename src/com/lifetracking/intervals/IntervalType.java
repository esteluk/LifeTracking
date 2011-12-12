package com.lifetracking.intervals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import com.lifetracking.R;
import com.lifetracking.SaveLoad;
import com.lifetracking.graphing.GraphingOptions;
import com.lifetracking.graphing.IntervalGraph;
import com.lifetracking.ui.GuiHelper;

//IntervalType is a collection of Intervals.
public class IntervalType {
	
	//============================================ STATIC ================================================
	
	//Store all user intervals. DO NOT insert intervals manually.
	public static ArrayList<IntervalType> m_intervalTypes;//public: READ-ONLY
	public static String m_intervalTypesPrefix = "IntervalType";
	static {
		m_intervalTypes = new ArrayList<IntervalType>();
	}
	
	//Add various fake interval types with data for debug/test purposes.
	public static void addExampleIntervalTypes(){
		IntervalType t = new IntervalType("Interval");
		m_intervalTypes.add(t);
		
		Random r = new Random();
		long offset = 0;
		for(int n = 0; n < 50; n++){
			Calendar calendarStart = Calendar.getInstance(), calendarEnd = Calendar.getInstance();
			long length = (long)(400000000L * (0.4 * r.nextDouble() + 0.6));
			calendarStart.setTimeInMillis(MyLife.m_originDate.getTimeInMillis() + offset);
			calendarEnd.setTimeInMillis(MyLife.m_originDate.getTimeInMillis() + offset + length);
			t.addInterval(calendarStart, calendarEnd).m_note = "An interval of sorts...";
			offset += length + 400000000L * (0.4 * r.nextDouble() + 0.6);
		}
		Calendar temp = Calendar.getInstance(); temp.setTimeInMillis(MyLife.m_originDate.getTimeInMillis() + offset);
		t.addInterval(temp).m_note = "This is the end!!!";
	}
	
	//Add the given interval type to the global list of interval types.
	//return - the given interval type.
	public static IntervalType addIntervalType(IntervalType intervalType){
		m_intervalTypes.add(intervalType);
		SaveLoad.saveIntervalType(intervalType);
		return intervalType;
	}
	
	//Add a new interval type with the given name, but only if there is no interval type with the same name.
	//return - interval type with the given name (newly created if not found).
	public static IntervalType addIntervalType(String intervalTypeName, boolean onlyIfUniqueName){
		if(onlyIfUniqueName){
			for(IntervalType t : m_intervalTypes){
				if(intervalTypeName.compareTo(t.m_name) == 0) return t;
			}
		}
		return addIntervalType(new IntervalType(intervalTypeName));
	}
	
	//Remove the interval type at the given index from the list of interval types and delete corresponding file.
	public static void removeIntervalType(int index){
		removeIntervalType(m_intervalTypes.get(index));
	}
	
	//Remove the interval type from the list of interval types and delete corresponding file.
	public static void removeIntervalType(IntervalType intervalType){
		m_intervalTypes.remove(intervalType);
		SaveLoad.deleteIntervalType(intervalType);
	}
	
	//======================================== END STATIC ================================================
	
	public long m_id;//acts as a unique id for the type
	public String m_name;
	public Interval m_lastAddedInterval;
	public ArrayList<Interval> m_intervals;//public: READ-ONLY
	public IntervalGraph m_graph;
	public GraphingOptions m_graphingOptions;
	
	//Create new IntervalType with the given name.
	public IntervalType(String name){
		m_id = new Random().nextLong();
		m_name = name;
		m_intervals = new ArrayList<Interval>();
		m_graph = new IntervalGraph(this);
		m_graphingOptions = new GraphingOptions();
	}
	
	//return - true if this track's graph is assigned to a GraphView.
	public boolean isGraphed(){
		return m_graph.isGraphed();
	}
	
	//Add a new interval with the given start date.
	//return - new interval. Null if there is already an interval in progress.
	public Interval addInterval(Calendar startDate){
		return addInterval(new Interval(this, startDate));
	}
	
	//Add a new interval with the given start and end dates.
	//return - new interval.
	public Interval addInterval(Calendar startDate, Calendar endDate){
		Interval interval = new Interval(this, startDate);
		interval.setEndDate(endDate);
		return addInterval(interval);
	}
	
	//Add the given interval.
	//return - added interval. Null if there is already an interval in progress, and the given interval is in progress too.
	public Interval addInterval(Interval interval){
		if(isInProgress() && interval.isInProgress()) return null;
		//Insert the value into a sorted order by start date.
		for(int n = 0; n < m_intervals.size(); n++){
			if(m_intervals.get(n).m_start > interval.m_start){
				m_intervals.add(n, interval);
				if(interval.isInProgress()) m_lastAddedInterval = interval;
				return interval;
			}
		}
		m_intervals.add(m_intervals.size(), interval);
		if(interval.isInProgress()) m_lastAddedInterval = interval;
		return interval;
	}
	
	//Remove interval at the given index.
	public void removeInterval(int index){
		Interval interval = m_intervals.remove(index);
		if(interval == m_lastAddedInterval) m_lastAddedInterval = null;
	}
	
	//Remove the given interval.
	public void removeInterval(Interval interval){
		if(interval == m_lastAddedInterval) m_lastAddedInterval = null;
		m_intervals.remove(interval);
	}
	
	//return - last interval (by time). Null if no intervals.
	public Interval getLastInterval(){
		return m_intervals.size() > 0 ? m_intervals.get(m_intervals.size() - 1) : null;
	}
	
	//return - true if the last interval of this type is in progress.
	public boolean isInProgress(){
		return (m_lastAddedInterval != null && m_lastAddedInterval.isInProgress());
	}
	
	//return - true if this can be graphed.
	public boolean canGraph(){
		return m_intervals.size() >= 2 || (m_intervals.size() == 1 && !m_intervals.get(0).isInProgress());
	}
	
	//Create a Dialog for editing an interval.
	public Dialog showEditIntervalDialog(final Interval interval){
		final IntervalType intervalType = this;
		final Calendar newStartDate = interval.getStartDate();
		final Calendar newEndDate = interval.getEndDate();
		final boolean isInProgress = interval.isInProgress();
		
		View layout = LifeTracking.g_inflater.inflate(R.layout.add_interval_dialog, null, false);

		final EditText noteText = (EditText) layout.findViewById(R.id.AddIntervalDialog_NoteText);
		noteText.setText(interval.m_note);
		
		GuiHelper.setupDateTimeButtons(layout, R.id.AddIntervalDialog_StartTimeButton, R.id.AddIntervalDialog_StartDateButton, newStartDate);
		
		Button endTimeButton = (Button) layout.findViewById(R.id.AddIntervalDialog_EndTimeButton);
		Button endDateButton = (Button) layout.findViewById(R.id.AddIntervalDialog_EndDateButton);
		if(isInProgress){
			endTimeButton.setVisibility(View.GONE);
			endDateButton.setVisibility(View.GONE);
			layout.findViewById(R.id.AddIntervalDialog_EndLabel).setVisibility(View.GONE);
		} else {
			GuiHelper.setupDateTimeButtons(layout, R.id.AddIntervalDialog_EndTimeButton, R.id.AddIntervalDialog_EndDateButton, newEndDate);
		}
		layout.findViewById(R.id.AddIntervalDialog_LastValueText).setVisibility(View.GONE);

		//Create the "Start/End Interval" dialog.
		AlertDialog.Builder builder = new AlertDialog.Builder(LifeTracking.g_globalContext);
		builder.setView(layout)
			.setMessage("Edit interval")
			.setPositiveButton("Edit", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
				boolean isInputOk = isInProgress || newEndDate.after(newStartDate);
				if(isInputOk){
					intervalType.removeInterval(interval);
					interval.setStartDate(newStartDate);
					if(!isInProgress) interval.setEndDate(newEndDate);
					interval.m_note = noteText.getText().toString();
					intervalType.addInterval(interval);
					SaveLoad.saveIntervalType(intervalType);
					Toast.makeText(LifeTracking.g_globalContext, "Interval edited", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(LifeTracking.g_globalContext, "Error: End date can't be before start date.", Toast.LENGTH_LONG).show();
				}
			}})
			.setNegativeButton("Cancel", null);
		return builder.show();
	}
}
