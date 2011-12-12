package com.lifetracking.ui;

import com.lifetracking.*;
import com.lifetracking.analysis.TrackAnalysisActivity;
import com.lifetracking.graphing.GraphActivity;
import com.lifetracking.tracks.Track;
import com.lifetracking.tracks.TrackValue;
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
import android.widget.Toast;

//TracksActivity allows the user to manage Tracks.
public class TracksActivity extends LifeTracking.LifeActivity {
	
	private Track m_selectedTrack;//this is the currently selected track in main menu
	
	//Called when the activity is first created.
    @Override public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.tracks_screen);
		
		//"Add New Track" button.
		final Button addTrackButton = (Button) findViewById(R.id.MainMenu_AddNewTrackButton);

        addTrackButton.setOnClickListener(new OnClickListener() { public void onClick(View v) {
        	//Show the dialog for renaming a track.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.name_dialog, null, false);
    		final EditText trackNameText = (EditText) layout.findViewById(R.id.NameDialog_Name);
    		trackNameText.setHint("Track name");
        	AlertDialog.Builder builder = new AlertDialog.Builder(TracksActivity.this);
        	builder.setView(layout)
        		.setMessage("Add new track")
        		.setPositiveButton("Add", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
        			Track track = Track.createNewTrack(Track.TrackType.Number);
                	track.m_name = trackNameText.getText().toString();
                	Track.addTrack(track);
    				onStart();
        		}})
        		.setNegativeButton("Cancel", null)
        		.show();
        }});
        
        addTrackButton.setOnLongClickListener(new OnLongClickListener() { public boolean onLongClick(View v) {
    		//Show the CSV Import dialog.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.csv_import_dialog, null, false);
    		final EditText filenameText = (EditText) layout.findViewById(R.id.CsvImport_Filename);
    		final CheckBox usesCommas = (CheckBox) layout.findViewById(R.id.CsvImport_UsesCommas);
    		final CheckBox hasHeader = (CheckBox) layout.findViewById(R.id.CsvImport_HasHeader);
        	AlertDialog.Builder builder = new AlertDialog.Builder(TracksActivity.this);
        	builder.setView(layout)
        		.setMessage("Import CSV as new track?")
        		.setPositiveButton("Import", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
        			Track track = SaveLoad.importTrackFromCsv(filenameText.getText().toString(), usesCommas.isChecked(), hasHeader.isChecked());
        			if(track != null){
        				Track.m_tracks.add(track);
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
    	
    	//Add track rows to the table.
    	ViewGroup layout = (ViewGroup)findViewById(R.id.MainMenu_TracksLayout);
    	layout.removeAllViews();

		for(int n = 0; n < Track.m_tracks.size(); n++){
			//Add row.
			View row = LifeTracking.g_inflater.inflate(R.layout.two_button_row, null, false);
			layout.addView(row);
			final Track track = Track.m_tracks.get(n);
			
			//Setup the primary "Track Name" button for the row. (Caution: order of setting events is important here.)
			Button button = (Button)row.findViewById(R.id.TwoButtonRow_FirstButton);
			button.setText(Track.m_tracks.get(n).m_name);
			button.setOnLongClickListener(new OnLongClickListener() { public boolean onLongClick(View v) {
				m_selectedTrack = track;
				return false;//don't consume click
			}});
			registerForContextMenu(button);//apparently this overwrites onClickListener
			button.setOnClickListener(new OnClickListener() { public void onClick(View v) {
				showGraphMenu(track);
			}});
			
			//Setup the "Add Track Value" button.
			ImageButton imageButton = (ImageButton)row.findViewById(R.id.TwoButtonRow_SecondButton);
			imageButton.setOnClickListener(new OnClickListener() { public void onClick(View v) {
				track.showAddTrackValueDialog();
			}});
			imageButton.setOnLongClickListener(new OnLongClickListener() { public boolean onLongClick(View v) {
				track.showAddTrackValueDialog();
				return true;
			}});
		}
    }
    
    //Called when context menu should be created.
    @Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
    	super.onCreateContextMenu(menu, v, menuInfo);
    	LifeTracking.g_menuInflater.inflate(R.layout.track_context_menu, menu);
    	menu.findItem(R.id.TrackContextMenu_UndoLastButton).setEnabled(m_selectedTrack.m_lastAddedTrackValue != null);
    }
    
    //Called when context menu item is selected.
    @Override public boolean onContextItemSelected(MenuItem item){
    	if(item.getItemId() == R.id.TrackContextMenu_AnalyzeButton){
    		if(m_selectedTrack.m_values.size() >= 2){
    			TrackAnalysisActivity.m_track = m_selectedTrack;
        		startActivity(new Intent(TracksActivity.this, TrackAnalysisActivity.class));
    		} else {
    			Toast.makeText(TracksActivity.this, "Not enough values to analyze.", Toast.LENGTH_SHORT).show();
    		}
    	} else if(item.getItemId() == R.id.TrackContextMenu_UndoLastButton){
    		//Check if the user is sure about removing the last value.
    		final TrackValue lastTrackValue = m_selectedTrack.m_lastAddedTrackValue;
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("Delete the last added value of " + UtilHelper.valueToString(lastTrackValue.m_value) + "?\n" + "(Added on " + lastTrackValue.getTimeString() + ", " + lastTrackValue.getDateString() + ")")
        		.setPositiveButton("Delete", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
        			m_selectedTrack.m_values.remove(lastTrackValue);
        			m_selectedTrack.m_lastAddedTrackValue = null;
    				SaveLoad.saveTrack(m_selectedTrack);
        		}})
        		.setNegativeButton("Cancel", null)
        		.show();
    	} else if(item.getItemId() == R.id.TrackContextMenu_RenameTrackButton){
    		//Show the dialog for renaming a track.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.name_dialog, null, false);
    		final EditText trackNameText = (EditText) layout.findViewById(R.id.NameDialog_Name);
    		trackNameText.setHint(m_selectedTrack.m_name);
    		trackNameText.setText(m_selectedTrack.m_name);
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setView(layout)
        		.setMessage("Rename \"" + m_selectedTrack.m_name + "\" track?")
        		.setPositiveButton("Rename", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
        			m_selectedTrack.m_name = trackNameText.getText().toString();
    				SaveLoad.saveTrack(m_selectedTrack);
    				onStart();
        		}})
        		.setNegativeButton("Cancel", null)
        		.show();
    	} else if(item.getItemId() == R.id.TrackContextMenu_ExportCsvButton){
    		//Show the Export CSV dialog.
    		View layout = LifeTracking.g_inflater.inflate(R.layout.csv_export_dialog, null, false);
    		final EditText filenameText = (EditText) layout.findViewById(R.id.CsvExport_Filename);
    		final CheckBox useCommas = (CheckBox) layout.findViewById(R.id.CsvExport_UseCommas);
    		filenameText.setText("LifeTracking_" + m_selectedTrack.m_name);
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setView(layout)
        		.setMessage("Export \"" + m_selectedTrack.m_name + "\" as CSV?")
	    		.setPositiveButton("Export", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
	    			SaveLoad.exportTrackToCsv(m_selectedTrack, filenameText.getText().toString(), useCommas.isChecked());
	    		}})
	    		.setNegativeButton("Cancel", null)
	    		.show();
    	} else if(item.getItemId() == R.id.TrackContextMenu_DeleteTrackButton){
    		//Check if the user is sure about deleting the track.
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("Delete \"" + m_selectedTrack.m_name + "\" track?")
        		.setPositiveButton("Delete", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
    				//Delete the corresponding track.
    				Track.removeTrack(m_selectedTrack);
    				onStart();
        		}})
        		.setNegativeButton("Cancel", null)
	        	.show();
        }
    	return true;
    }
    
    //Show the GraphMenu, using the given track.
    private void showGraphMenu(Track track){
    	if(track.m_values.size() >= 2){
			GraphActivity.m_graph = track.m_graph;
			startActivity(new Intent(TracksActivity.this, GraphActivity.class));
		} else {
			Toast.makeText(TracksActivity.this, "Not enough data points to graph.", Toast.LENGTH_SHORT).show();
		}
    }
}

