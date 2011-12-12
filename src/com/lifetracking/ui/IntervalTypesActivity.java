package com.lifetracking.ui;

import java.util.Calendar;
import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import com.lifetracking.R;
import com.lifetracking.SaveLoad;
import com.lifetracking.analysis.IntervalAnalysisActivity;
import com.lifetracking.graphing.GraphActivity;
import com.lifetracking.intervals.Interval;
import com.lifetracking.intervals.IntervalType;
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

//IntervalTypesActivity allows the user to manage IntervalTypes.
public class IntervalTypesActivity extends LifeTracking.LifeActivity {
	
	private IntervalType m_selectedIntervalType = null;
	
	//Called when the activity is first created.
    @Override public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.interval_types_menu);
		
		//"Add New Interval Type" button.
		View addNewIntervalTypeButton = findViewById(R.id.IntervalTypesMenu_AddNewIntervalTypeButton);
		addNewIntervalTypeButton.setOnClickListener(new OnClickListener() { public void onClick(View v) {
        	//Show the dialog for renaming.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.name_dialog, null, false);
    		
    		final EditText nameText = (EditText) layout.findViewById(R.id.NameDialog_Name);
    		nameText.setHint("Interval type name");
 
            AlertDialog.Builder builder = new AlertDialog.Builder(IntervalTypesActivity.this);
            builder.setView(layout)
            	.setMessage("Create new interval type")
            	.setPositiveButton("Create", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
    				IntervalType.addIntervalType(new IntervalType(nameText.getText().toString()));
    				onStart();
            	}})
            	.setNegativeButton("Cancel", null)
            	.show();
		}});
		
		addNewIntervalTypeButton.setOnLongClickListener(new OnLongClickListener() { public boolean onLongClick(View v) {
    		//Show the CSV Import dialog.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.csv_import_dialog, null, false);
    		final EditText filenameText = (EditText) layout.findViewById(R.id.CsvImport_Filename);
    		final CheckBox usesCommas = (CheckBox) layout.findViewById(R.id.CsvImport_UsesCommas);
    		final CheckBox hasHeader = (CheckBox) layout.findViewById(R.id.CsvImport_HasHeader);
        	AlertDialog.Builder builder = new AlertDialog.Builder(IntervalTypesActivity.this);
        	builder.setView(layout)
        		.setMessage("Import CSV as new interval type?")
        		.setPositiveButton("Import", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
        			IntervalType intervalType = SaveLoad.importIntervalTypeFromCsv(filenameText.getText().toString(), usesCommas.isChecked(), hasHeader.isChecked());
        			if(intervalType != null){
        				IntervalType.m_intervalTypes.add(intervalType);
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

    	ViewGroup layout = (ViewGroup)findViewById(R.id.IntervalTypesMenu_IntervalsLayout);
    	layout.removeAllViews();

		for(int n = 0; n < IntervalType.m_intervalTypes.size(); n++){
			final IntervalType intervalType = IntervalType.m_intervalTypes.get(n);
			
			//Add row.
			View row = LifeTracking.g_inflater.inflate(R.layout.two_button_row, null, false);
			layout.addView(row);
			
			//Setup the primary "Interval Type Name" button for the row. (Caution: order of setting events is important here.)
			Button button = (Button)row.findViewById(R.id.TwoButtonRow_FirstButton);
			button.setText(intervalType.m_name);
			button.setOnLongClickListener(new OnLongClickListener() { public boolean onLongClick(View v) {
				m_selectedIntervalType = intervalType;
				return false;//don't consume click
			}});
			registerForContextMenu(button);//apparently this overwrites onClickListener
			button.setOnClickListener(new OnClickListener() { public void onClick(View v) {
				if(intervalType.canGraph()){
					GraphActivity.m_graph = intervalType.m_graph;
					startActivity(new Intent(IntervalTypesActivity.this, GraphActivity.class));
				} else {
					Toast.makeText(IntervalTypesActivity.this, "Not enough data values to graph.", Toast.LENGTH_SHORT).show();
				}
			}});
			
			//Setup the "Start/End Interval" button. (Caution: order of setting events is important here.)
			final ImageButton imageButton = (ImageButton)row.findViewById(R.id.TwoButtonRow_SecondButton);
			imageButton.setImageResource(intervalType.isInProgress() ? R.drawable.interval_end : R.drawable.interval_start);
			imageButton.setOnLongClickListener(new OnLongClickListener() { public boolean onLongClick(View v) {
        		View layout = LifeTracking.g_inflater.inflate(R.layout.add_interval_dialog, null, false);

        		final Calendar date = Calendar.getInstance();
        		final EditText noteText = (EditText) layout.findViewById(R.id.AddIntervalDialog_NoteText);
        		boolean inProgress = intervalType.isInProgress();
        		if(inProgress){
        			noteText.setText(intervalType.m_lastAddedInterval.m_note);
        		}
        		
    			GuiHelper.setupDateTimeButtons(layout, R.id.AddIntervalDialog_StartTimeButton, R.id.AddIntervalDialog_StartDateButton, date);
        		layout.findViewById(R.id.AddIntervalDialog_EndTimeButton).setVisibility(View.GONE);
        		layout.findViewById(R.id.AddIntervalDialog_EndDateButton).setVisibility(View.GONE);
    			layout.findViewById(R.id.AddIntervalDialog_StartLabel).setVisibility(View.GONE);
    			layout.findViewById(R.id.AddIntervalDialog_EndLabel).setVisibility(View.GONE);
    			
    			final TextView lastValueText = (TextView) layout.findViewById(R.id.AddIntervalDialog_LastValueText);
    			if(intervalType.m_intervals.size() > 0){
    				Interval lastValue = intervalType.m_intervals.get(intervalType.m_intervals.size() - 1);
    				if(lastValue.isInProgress()){
    					lastValueText.setText("This interval started on " + lastValue.getStartTimeDateString());
    				} else {
    					lastValueText.setText("Last interval ended on " + lastValue.getEndTimeDateString());
    				}
    			} else {
    				lastValueText.setVisibility(View.GONE);
    			}

        		//Create the "Start/End Interval" dialog.
        		AlertDialog.Builder builder = new AlertDialog.Builder(IntervalTypesActivity.this);
        		builder.setView(layout)
        			.setMessage(inProgress ? "End interval" : "Start interval")
        			.setPositiveButton(inProgress ? "End" : "Start", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
    					if(!intervalType.isInProgress()){
    						Interval interval = intervalType.addInterval(date);
    						interval.m_note = noteText.getText().toString();
    						intervalType.m_lastAddedInterval = interval;
    						SaveLoad.saveIntervalType(intervalType);
    						imageButton.setImageResource(R.drawable.interval_end);
    						Toast.makeText(IntervalTypesActivity.this, "\"" + intervalType.m_name + "\" started at " + interval.getStartTimeString() + ", " + interval.getStartDateString(), Toast.LENGTH_LONG).show();
    					} else {
    						Interval interval = intervalType.m_lastAddedInterval;
    						if(date.after(MyLife.getCalendarFromOffset(interval.m_start))){
	    						interval.setEndDate(date);
	    						interval.m_note = noteText.getText().toString();
	    						intervalType.m_lastAddedInterval = interval;
	    						SaveLoad.saveIntervalType(intervalType);
	    						imageButton.setImageResource(R.drawable.interval_start);
	    						Toast.makeText(IntervalTypesActivity.this, "\"" + intervalType.m_name + "\" ended at " + interval.getEndTimeString() + ", " + interval.getEndDateString(), Toast.LENGTH_LONG).show();
    						} else {
    							Toast.makeText(LifeTracking.g_globalContext, "Error: End date can't be before start date.", Toast.LENGTH_LONG).show();
    						}
    					}
        			}})
        			.setNegativeButton("Cancel", null)
        			.show();
				return true;//consume click
			}});
			imageButton.setOnClickListener(new OnClickListener() { public void onClick(View v) {
				Interval interval = intervalType.m_lastAddedInterval;
				if(!intervalType.isInProgress()){
					interval = intervalType.addInterval(Calendar.getInstance());
					intervalType.m_lastAddedInterval = interval;
					SaveLoad.saveIntervalType(intervalType);
					imageButton.setImageResource(R.drawable.interval_end);
					Toast.makeText(IntervalTypesActivity.this, "\"" + intervalType.m_name + "\" started at " + interval.getStartTimeDateString(), Toast.LENGTH_LONG).show();
				} else {
					Calendar now = Calendar.getInstance();
					if(now.after(MyLife.getCalendarFromOffset(interval.m_start))){
						interval.setEndDate(now);
						intervalType.m_lastAddedInterval = interval;
						SaveLoad.saveIntervalType(intervalType);
						imageButton.setImageResource(R.drawable.interval_start);
						Toast.makeText(IntervalTypesActivity.this, "\"" + intervalType.m_name + "\" ended at " + interval.getEndTimeDateString(), Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(LifeTracking.g_globalContext, "Error: End date can't be before start date.", Toast.LENGTH_LONG).show();
					}
				}
			}});
		}
    }
    
	//Called when context menu should be created.
    @Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
    	super.onCreateContextMenu(menu, v, menuInfo);
    	LifeTracking.g_menuInflater.inflate(R.layout.interval_context_menu, menu);
    	if(m_selectedIntervalType.m_lastAddedInterval == null){
    		menu.findItem(R.id.IntervalContextMenu_UndoLastButton).setEnabled(false);
    	}
    }
    
	//Called when context menu item is selected.
    @Override public boolean onContextItemSelected(MenuItem item){
    	if(item.getItemId() == R.id.IntervalContextMenu_AnalyzeButton){
    		if(m_selectedIntervalType.m_intervals.size() >= 2){
    			IntervalAnalysisActivity.m_intervalType = m_selectedIntervalType;
    			startActivity(new Intent(IntervalTypesActivity.this, IntervalAnalysisActivity.class));
    		} else {
    			Toast.makeText(IntervalTypesActivity.this, "Not enough intervals to analyze.", Toast.LENGTH_SHORT).show();
    		}
    	} else if(item.getItemId() == R.id.IntervalContextMenu_UndoLastButton){
    		//Check if the user is sure about removing the last value.
    		final Interval lastAddedInterval = m_selectedIntervalType.m_lastAddedInterval;
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		if(lastAddedInterval.isInProgress()){
    			builder.setMessage("Undo the start on " + lastAddedInterval.getStartTimeString() + ", " + lastAddedInterval.getStartDateString() + ")")
        		.setPositiveButton("Undo", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
        			m_selectedIntervalType.removeInterval(lastAddedInterval);
        			SaveLoad.saveIntervalType(m_selectedIntervalType);
        			onStart();
        		}})
        		.setNegativeButton("Cancel", null)
        		.show();
    		} else {
    			builder.setMessage("Undo the end date on " + lastAddedInterval.getEndTimeDateString() + ")")
        		.setPositiveButton("Undo", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
        			lastAddedInterval.setEndDate(null);
    				m_selectedIntervalType.m_lastAddedInterval = null;
    				SaveLoad.saveIntervalType(m_selectedIntervalType);
    				onStart();
        		}})
        		.setNegativeButton("Cancel", null)
        		.show();
    		}
    	} else if(item.getItemId() == R.id.IntervalContextMenu_RenameButton){
    		//Show the dialog for renaming.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.name_dialog, null, false);
    		final EditText intervalTypeNameText = (EditText) layout.findViewById(R.id.NameDialog_Name);
    		intervalTypeNameText.setHint(m_selectedIntervalType.m_name);
    		intervalTypeNameText.setText(m_selectedIntervalType.m_name);
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setView(layout)
        		.setMessage("Rename \"" + m_selectedIntervalType.m_name + "\" interval type?")
        		.setPositiveButton("Rename", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
        			m_selectedIntervalType.m_name = intervalTypeNameText.getText().toString();
        			SaveLoad.saveIntervalType(m_selectedIntervalType);
        			onStart();
        		}})
        		.setNegativeButton("Cancel", null)
        		.show();
    	} else if(item.getItemId() == R.id.IntervalContextMenu_ExportCsvButton){
    		//Show the Export CSV dialog.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.csv_export_dialog, null, false);
    		final EditText filenameText = (EditText) layout.findViewById(R.id.CsvExport_Filename);
    		final CheckBox useCommas = (CheckBox) layout.findViewById(R.id.CsvExport_UseCommas);
    		filenameText.setText("LifeTracking_" + m_selectedIntervalType.m_name);
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setView(layout)
        		.setMessage("Export \"" + m_selectedIntervalType.m_name + "\" as CSV?")
	    		.setPositiveButton("Export", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
	    			SaveLoad.exportIntervalTypeToCsv(m_selectedIntervalType, filenameText.getText().toString(), useCommas.isChecked());
	    		}})
	    		.setNegativeButton("Cancel", null)
	    		.show();
    	} else if(item.getItemId() == R.id.IntervalContextMenu_DeleteButton){
    		//Check if the user is sure about deleting the interval type.
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("Delete \"" + m_selectedIntervalType.m_name + "\" interval type?")
	    		.setPositiveButton("Delete", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
	    			//Delete the corresponding interval type.
	    			IntervalType.removeIntervalType(m_selectedIntervalType);
	    			onStart();
	    		}})
	    		.setNegativeButton("Cancel", null)
	    		.show();
        }
    	return true;
    }
}
