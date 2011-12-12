package com.lifetracking.graphing;

import java.util.Calendar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import com.lifetracking.R;
import com.lifetracking.SaveLoad;
import com.lifetracking.events.EventType;
import com.lifetracking.intervals.IntervalType;
import com.lifetracking.tracks.Track;
import com.lifetracking.ui.MainActivity;

//GraphActivity shows a graph for the given Track.
public class GraphActivity extends LifeTracking.LifeActivity {

	public static Graph m_graph;//graph to add to the GraphView when the menu is created; this is used as an onCreate parameter

	private GraphView m_graphView;

	//Called when the activity is first created.
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(m_graph == null) return;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Create the graph view.
		m_graphView = new GraphView(this, m_graph);
		m_graphView.setOnTouchListener(m_graphView);
		setContentView(m_graphView);
	}
	
	//Called when the activity is destroyed.
	@Override public void onDestroy(){
		super.onDestroy();
		if(m_graphView == null) return;
		m_graphView.clearAllGraphs();
		m_graphView = null;
	}

	//Create options menu (called once to create).
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		LifeTracking.g_menuInflater.inflate(R.layout.graph_menu_options_menu, menu);
		return true;
	}
	
	//Prepare the options menu (called every time right before it's displayed).
	@Override public boolean onPrepareOptionsMenu(Menu menu){
		super.onPrepareOptionsMenu(menu);
		//Let GraphView determine everything.
		m_graphView.prepareOptionsMenu(menu);
		return true;
	}

	//Process option item selection event.
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		DialogInterface.OnMultiChoiceClickListener emptyListener = new DialogInterface.OnMultiChoiceClickListener(){ public void onClick(DialogInterface dialog, int which, boolean isChecked) {}};
		switch (item.getItemId()) {
		case R.id.GraphMenu_Options_Tracks:{
			//Create a list of names of all the Tracks and if they are being graphed or not.
			final int tracksCount = Track.m_tracks.size();
			final boolean[] trackSelected = new boolean[tracksCount];
			CharSequence[] trackNames = new CharSequence[tracksCount];
			for(int n = 0; n < tracksCount; n++){
				Track track = Track.m_tracks.get(n);
				trackNames[n] = track.m_name;
				trackSelected[n] = track.isGraphed();
			}
			
			//Create dialog with the list of all tracks.
			AlertDialog.Builder builder = new AlertDialog.Builder(GraphActivity.this);
			builder.setMultiChoiceItems(trackNames, trackSelected, emptyListener)
				.setPositiveButton("Update", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
					for(int n = 0; n < tracksCount; n++){
						Track track = Track.m_tracks.get(n);
						if(!trackSelected[n] && track.isGraphed()){
							m_graphView.removeGraph(track.m_graph);
						} else if(trackSelected[n] && !track.isGraphed() && track.m_values.size() >= 2){
							m_graphView.addGraph(track.m_graph);
						}
					}
					m_graphView.needsRecalculation();
					if(m_graphView.m_graphs.size() == 0){
						startActivity(new Intent(GraphActivity.this, MainActivity.class));
					}
				}})
				.setNegativeButton("Cancel", null)
				.show();
		}break;
		case R.id.GraphMenu_Options_Intervals:{
			//Create a list of names of all the IntervalTypes and if they are being graphed or not.
			final int intervalTypesCount = IntervalType.m_intervalTypes.size();
			final boolean[] intervalTypesSelected = new boolean[intervalTypesCount];
			CharSequence[] intervalTypeNames = new CharSequence[intervalTypesCount];
			for(int n = 0; n < intervalTypesCount; n++){
				IntervalType intervalType = IntervalType.m_intervalTypes.get(n);
				intervalTypeNames[n] = intervalType.m_name;
				intervalTypesSelected[n] = intervalType.isGraphed();
			}
			
			//Create dialog with the list of all interval types.
			AlertDialog.Builder builder = new AlertDialog.Builder(GraphActivity.this);
			builder.setMultiChoiceItems(intervalTypeNames, intervalTypesSelected, emptyListener)
				.setPositiveButton("Update", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
					for(int n = 0; n < intervalTypesCount; n++){
						IntervalType intervalType = IntervalType.m_intervalTypes.get(n);
						if(!intervalTypesSelected[n] && intervalType.isGraphed()){
							m_graphView.removeGraph(intervalType.m_graph);
						} else if(intervalTypesSelected[n] && !intervalType.isGraphed() && intervalType.canGraph()){
							m_graphView.addGraph(intervalType.m_graph);
						}
					}
					m_graphView.needsRecalculation();
					if(m_graphView.m_graphs.size() == 0){
						startActivity(new Intent(GraphActivity.this, MainActivity.class));
					}
				}})
				.setNegativeButton("Cancel", null)
				.show();
		}break;
		case R.id.GraphMenu_Options_Events:{
			//Create a list of names of all the EventTypes and if they are being graphed or not.
			final int eventTypesCount = EventType.m_eventTypes.size();
			final boolean[] eventTypesSelected = new boolean[eventTypesCount];
			CharSequence[] eventTypeNames = new CharSequence[eventTypesCount];
			for(int n = 0; n < eventTypesCount; n++){
				EventType eventType = EventType.m_eventTypes.get(n);
				eventTypeNames[n] = eventType.m_name;
				eventTypesSelected[n] = eventType.isGraphed();
			}
			
			//Create dialog with the list of all event types.
			AlertDialog.Builder builder = new AlertDialog.Builder(GraphActivity.this);
			builder.setMultiChoiceItems(eventTypeNames, eventTypesSelected, emptyListener)
				.setPositiveButton("Update", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
					for(int n = 0; n < eventTypesCount; n++){
						EventType eventType = EventType.m_eventTypes.get(n);
						if(!eventTypesSelected[n] && eventType.isGraphed()){
							m_graphView.removeGraph(eventType.m_graph);
						} else if(eventTypesSelected[n] && !eventType.isGraphed() && eventType.canGraph()){
							m_graphView.addGraph(eventType.m_graph);
						}
					}
					m_graphView.needsRecalculation();
					if(m_graphView.m_graphs.size() == 0){
						startActivity(new Intent(GraphActivity.this, MainActivity.class));
					}
				}})
				.setNegativeButton("Cancel", null)
				.show();
		}break;
		case R.id.GraphMenu_Options_ViewOptions:{
			//Present user with view options.
			View layout = LifeTracking.g_inflater.inflate(R.layout.graph_menu_view_options_dialog, null, false);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			
			//Some options will be invisible if there is no selected graph.
			Graph graph = m_graphView.m_selectedGraph;
			if(graph == null && m_graphView.m_graphs.size() == 1){
				graph = m_graphView.m_graphs.get(0);
			}
			final GraphingOptions options = graph == null ? null : graph.getGraphingOptions();

			//Setup all check boxes.
			CheckBox autoAdjustY = (CheckBox) layout.findViewById(R.id.GraphMenu_ViewOptions_AutoAdjustYCheckBox);
			if(options != null) autoAdjustY.setChecked(options.m_autoAdjustY);
			else autoAdjustY.setVisibility(View.GONE);
			autoAdjustY.setOnClickListener(new CheckBox.OnClickListener() { public void onClick(View v) {
				options.m_autoAdjustY = ((CheckBox)v).isChecked();
			}});
			CheckBox includeYZero = (CheckBox) layout.findViewById(R.id.GraphMenu_ViewOptions_IncludeYZeroCheckBox);
			if(options != null) includeYZero.setChecked(options.m_includeYZero);
			else includeYZero.setVisibility(View.GONE);
			includeYZero.setOnClickListener(new CheckBox.OnClickListener() { public void onClick(View v) {
				options.m_includeYZero = ((CheckBox)v).isChecked();
			}});
			CheckBox userPercentScale = (CheckBox) layout.findViewById(R.id.GraphMenu_ViewOptions_UsePercentScaleCheckBox);
			userPercentScale.setChecked(MyLife.m_usePercentScale);
			userPercentScale.setOnClickListener(new CheckBox.OnClickListener() { public void onClick(View v) {
				MyLife.m_usePercentScale = ((CheckBox)v).isChecked();
				SaveLoad.saveMyLife();
			}});
			CheckBox bigFont = (CheckBox) layout.findViewById(R.id.GraphMenu_ViewOptions_BigFontCheckBox);
			bigFont.setChecked(MyLife.m_useBigFont);
			bigFont.setOnClickListener(new CheckBox.OnClickListener() { public void onClick(View v) {
				MyLife.m_useBigFont = ((CheckBox)v).isChecked();
				SaveLoad.saveMyLife();
				m_graphView.createPaints();
			}});
			
			//Create dialog.
			builder.setNeutralButton("Done", null).show().setOnDismissListener(new Dialog.OnDismissListener(){ public void onDismiss(DialogInterface dialog) {
				m_graphView.needsRecalculation();
			}});;
		}break;
		case R.id.GraphMenu_Options_Selection:{
			//Present user with the option to edit the current selection.
			View layout = LifeTracking.g_inflater.inflate(R.layout.graph_menu_edit_value_dialog, null, false);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			
			//Create dialog now so we can dismiss it in event handlers.
			final Dialog editPointDialog = builder.create();
			//"Edit Value" button.
			final Button editValueButton = (Button) layout.findViewById(R.id.GraphMenu_EditValue_EditButton);
			editValueButton.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) {
				editPointDialog.dismiss();
				m_graphView.editSelectedValue();
			}});
			
			//"Delete Value" button.
			final Button deleteValueButton = (Button) layout.findViewById(R.id.GraphMenu_EditValue_DeleteButton);
			deleteValueButton.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) {
				editPointDialog.dismiss();
				//Check if the user is sure about deleting the point...
				AlertDialog.Builder builder = new AlertDialog.Builder(GraphActivity.this);
	        	builder.setMessage("Delete this value?")
	        		.setPositiveButton("Yes", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
	        			if(!m_graphView.deleteSelectedValue()){
	        				startActivity(new Intent(GraphActivity.this, MainActivity.class));
	        			}
	        		}})
	        		.setNegativeButton("No", null)
	        		.show();
			}});
			editPointDialog.show();
		}break;
		case R.id.GraphMenu_Options_StartEndDates:{
			//Present user with option of setting the start and end date for the window.
			AlertDialog.Builder builder = new AlertDialog.Builder(GraphActivity.this);
			CharSequence menuItems[] = new CharSequence[]{"Last week", "Last month", "Last 3 months", "Last 6 months", "Last year", "All"};
			builder.setItems(menuItems, new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
	    		//Compute start date based on item selected.	
				Calendar startDate = Calendar.getInstance();
    			if(id == 0) startDate.add(Calendar.DAY_OF_YEAR, -7);
    	    	else if(id == 1) startDate.add(Calendar.MONTH, -1);
    	    	else if(id == 2) startDate.add(Calendar.MONTH, -3);
    	    	else if(id == 3) startDate.add(Calendar.MONTH, -6);
    	    	else if(id == 4) startDate.add(Calendar.YEAR, -1);
    	    	else if(id == 5) startDate = MyLife.getCalendarFromOffset(m_graphView.m_absoluteMinX);
    			
    			m_graphView.m_minX = MyLife.getOffsetFromCalendar(startDate);
    			m_graphView.m_maxX = MyLife.getOffsetFromCalendar(Calendar.getInstance());
    			m_graphView.needsRecalculation();
    		}})
    		.show();
		}break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	//Process key down event.
	@Override public boolean onKeyDown(int keyCode, KeyEvent event) {		 
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			m_graphView.zoom(-1);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			m_graphView.zoom(1);
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			m_graphView.slide(-1);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			m_graphView.slide(1);
			break;
		default:
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}
}
