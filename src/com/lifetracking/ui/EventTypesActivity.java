package com.lifetracking.ui;

import java.util.Calendar;
import com.lifetracking.LifeTracking;
import com.lifetracking.R;
import com.lifetracking.SaveLoad;
import com.lifetracking.analysis.EventAnalysisActivity;
import com.lifetracking.events.Event;
import com.lifetracking.events.EventType;
import com.lifetracking.graphing.GraphActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

//EventTypesActivity allows the user to manage EventTypes.
public class EventTypesActivity extends LifeTracking.LifeActivity {
	
	private EventType m_selectedEventType = null;
	
	//Called when the activity is first created.
    @Override public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.event_types_menu);
		
		//"Add New Event Type" button.
		View addNewEventTypeButton = findViewById(R.id.EventTypesMenu_AddNewEventTypeButton);
		addNewEventTypeButton.setOnClickListener(new OnClickListener() { public void onClick(View v) {
        	//Show the dialog for renaming.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.name_dialog, null, false);
    		
    		final EditText nameText = (EditText) layout.findViewById(R.id.NameDialog_Name);
    		nameText.setHint("Event type name");
 
            AlertDialog.Builder builder = new AlertDialog.Builder(EventTypesActivity.this);
            builder.setView(layout)
            	.setMessage("Create new event type")
            	.setPositiveButton("Create", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
    				EventType.addEventType(new EventType(nameText.getText().toString()));
    				onStart();
            	}})
            	.setNegativeButton("Cancel", null)
            	.show();
		}});
		
		addNewEventTypeButton.setOnLongClickListener(new OnLongClickListener() { public boolean onLongClick(View v) {
    		//Show the CSV Import dialog.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.csv_import_dialog, null, false);
    		final EditText filenameText = (EditText) layout.findViewById(R.id.CsvImport_Filename);
    		final CheckBox usesCommas = (CheckBox) layout.findViewById(R.id.CsvImport_UsesCommas);
    		final CheckBox hasHeader = (CheckBox) layout.findViewById(R.id.CsvImport_HasHeader);
        	AlertDialog.Builder builder = new AlertDialog.Builder(EventTypesActivity.this);
        	builder.setView(layout)
        		.setMessage("Import CSV as new event type?")
        		.setPositiveButton("Import", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
        			EventType eventType = SaveLoad.importEventTypeFromCsv(filenameText.getText().toString(), usesCommas.isChecked(), hasHeader.isChecked());
        			if(eventType != null){
        				EventType.m_eventTypes.add(eventType);
        			}
        		}})
        		.setNegativeButton("Cancel", null)
        		.show();
			return true;
		}});
    }
    
    //Called when the activity is activated.
    @Override public void onStart() {
    	super.onStart();

    	ViewGroup layout = (ViewGroup)findViewById(R.id.EventTypesMenu_EventsLayout);
    	layout.removeAllViews();

		for(int n = 0; n < EventType.m_eventTypes.size(); n++){
			final EventType eventType = EventType.m_eventTypes.get(n);
			
			//Add row.
			View row = LifeTracking.g_inflater.inflate(R.layout.two_button_row, null, false);
			layout.addView(row);
			
			//Setup the primary "Event Type Name" button for the row.
			Button button = (Button)row.findViewById(R.id.TwoButtonRow_FirstButton);
			button.setText(eventType.m_name);
			button.setOnLongClickListener(new OnLongClickListener() { public boolean onLongClick(View v) {
				m_selectedEventType = eventType;
				return false;//don't consume click
			}});
			registerForContextMenu(button);//apparently this overwrites onClickListener
			button.setOnClickListener(new OnClickListener() { public void onClick(View v) {
				if(eventType.canGraph()){
					GraphActivity.m_graph = eventType.m_graph;
					startActivity(new Intent(EventTypesActivity.this, GraphActivity.class));
				} else {
					Toast.makeText(EventTypesActivity.this, "Not enough data values to graph.", Toast.LENGTH_SHORT).show();
				}
			}});
			
			//Setup the "Add Event" button.
			final ImageButton imageButton = (ImageButton)row.findViewById(R.id.TwoButtonRow_SecondButton);
			imageButton.setImageResource(R.drawable.add_event);
			imageButton.setOnLongClickListener(new OnLongClickListener() { public boolean onLongClick(View v) {
				//Just reuse add_interval_dialog here.
        		View layout = LifeTracking.g_inflater.inflate(R.layout.add_interval_dialog, null, false);
        		
        		final Calendar date = Calendar.getInstance();
        		final EditText noteText = (EditText) layout.findViewById(R.id.AddIntervalDialog_NoteText);
        		
    			GuiHelper.setupDateTimeButtons(layout, R.id.AddIntervalDialog_StartTimeButton, R.id.AddIntervalDialog_StartDateButton, date);
    			layout.findViewById(R.id.AddIntervalDialog_EndLabel).setVisibility(View.GONE);
        		layout.findViewById(R.id.AddIntervalDialog_EndLayout).setVisibility(View.GONE);
    			layout.findViewById(R.id.AddIntervalDialog_StartLabel).setVisibility(View.GONE);
    			layout.findViewById(R.id.AddIntervalDialog_EndLabel).setVisibility(View.GONE);
    			
    			final TextView lastValueText = (TextView) layout.findViewById(R.id.AddIntervalDialog_LastValueText);
    			if(eventType.m_events.size() > 0){
    				Event lastValue = eventType.m_events.get(eventType.m_events.size() - 1);
    				lastValueText.setText("Last event added on " + lastValue.getStartTimeDateString());
    			} else {
    				lastValueText.setVisibility(View.GONE);
    			}

        		//Create the "Add Event" dialog.
        		AlertDialog.Builder builder = new AlertDialog.Builder(EventTypesActivity.this);
        		builder.setView(layout)
        			.setMessage("Add event")
        			.setPositiveButton("Add", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
						Event event = eventType.addEvent(date);
						event.m_note = noteText.getText().toString();
						eventType.m_lastAddedEvent = event;
						SaveLoad.saveEventType(eventType);
						Toast.makeText(EventTypesActivity.this, "Added \"" + eventType.m_name + "\" event at " + event.getStartTimeDateString(), Toast.LENGTH_LONG).show();
        			}})
        			.setNegativeButton("Cancel", null)
        			.show();
				return true;//consume click
			}});
			imageButton.setOnClickListener(new OnClickListener() { public void onClick(View v) {
				Event event = eventType.addEvent(Calendar.getInstance());
				eventType.m_lastAddedEvent = event;
				SaveLoad.saveEventType(eventType);
				Toast.makeText(EventTypesActivity.this, "Added \"" + eventType.m_name + "\" event at " + event.getStartTimeDateString(), Toast.LENGTH_LONG).show();
			}});
		}
    }
    
	//Called when context menu should be created.
    @Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
    	super.onCreateContextMenu(menu, v, menuInfo);
    	LifeTracking.g_menuInflater.inflate(R.layout.event_context_menu, menu);
    	if(m_selectedEventType.m_lastAddedEvent == null){
    		menu.findItem(R.id.EventContextMenu_UndoLastButton).setEnabled(false);
    	}
    }
    
	//Called when context menu item is selected.
    @Override public boolean onContextItemSelected(MenuItem item){
    	if(item.getItemId() == R.id.EventContextMenu_AnalyzeButton){
    		if(m_selectedEventType.m_events.size() >= 2){
    			EventAnalysisActivity.m_eventType = m_selectedEventType;
    			startActivity(new Intent(EventTypesActivity.this, EventAnalysisActivity.class));
    		} else {
    			Toast.makeText(EventTypesActivity.this, "Not enough events to analyze.", Toast.LENGTH_SHORT).show();
    		}
    	} else if(item.getItemId() == R.id.EventContextMenu_UndoLastButton){
    		//Check if the user is sure about removing the last value.
    		final Event lastAddedEvent = m_selectedEventType.m_lastAddedEvent;
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Undo the event on " + lastAddedEvent.getStartTimeDateString() + ")")
	    		.setPositiveButton("Undo", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
					m_selectedEventType.removeEvent(lastAddedEvent);
					SaveLoad.saveEventType(m_selectedEventType);
					onStart();
	    		}})
	    		.setNegativeButton("Cancel", null)
	    		.show();
    	} else if(item.getItemId() == R.id.EventContextMenu_RenameButton){
    		//Show the dialog for renaming.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.name_dialog, null, false);
    		final EditText eventTypeNameText = (EditText) layout.findViewById(R.id.NameDialog_Name);
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setView(layout)
        		.setMessage("Rename \"" + m_selectedEventType.m_name + "\" event type?")
        		.setPositiveButton("Rename", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
        			m_selectedEventType.m_name = eventTypeNameText.getText().toString();
        			SaveLoad.saveEventType(m_selectedEventType);
        			onStart();
        		}})
        		.setNegativeButton("Cancel", null)
        		.show();
    	} else if(item.getItemId() == R.id.EventContextMenu_ExportCsvButton){
    		//Show the Export CSV dialog.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.csv_export_dialog, null, false);
    		final EditText filenameText = (EditText) layout.findViewById(R.id.CsvExport_Filename);
    		final CheckBox useCommas = (CheckBox) layout.findViewById(R.id.CsvExport_UseCommas);
    		filenameText.setText("LifeTracking_" + m_selectedEventType.m_name);
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setView(layout)
        		.setMessage("Export \"" + m_selectedEventType.m_name + "\" as CSV?")
	    		.setPositiveButton("Export", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
	    			SaveLoad.exportEventTypeToCsv(m_selectedEventType, filenameText.getText().toString(), useCommas.isChecked());
	    		}})
	    		.setNegativeButton("Cancel", null)
	    		.show();
    	} else if(item.getItemId() == R.id.EventContextMenu_DeleteButton){
    		//Check if the user is sure about deleting the event type.
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("Delete \"" + m_selectedEventType.m_name + "\" event type?")
	    		.setPositiveButton("Delete", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
	    			//Delete the corresponding event type type.
	    			EventType.removeEventType(m_selectedEventType);
	    			onStart();
	    		}})
	    		.setNegativeButton("Cancel", null)
	    		.show();
        }
    	return true;
    }
}
