package com.lifetracking.ui;

import com.lifetracking.*;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

//MainActivity has the main menu, which is a collection of tabs.
public class MainActivity extends TabActivity {

	//Called when the activity is first created.
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LifeTracking.setActivity(this);
		LifeTracking.checkIfLoaded(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_screen);

		Resources res = getResources();
		TabHost tabHost = getTabHost();

		//Create the tabs.
		Intent intent = new Intent(this, IntervalTypesActivity.class);
		TabHost.TabSpec spec = tabHost.newTabSpec("interval types").setIndicator("Intervals", res.getDrawable(R.drawable.tab_interval)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent(this, TracksActivity.class);
		spec = tabHost.newTabSpec("tracks").setIndicator("Tracks", res.getDrawable(R.drawable.tab_track)).setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent(this, EventTypesActivity.class);
		spec = tabHost.newTabSpec("event types").setIndicator("Events", res.getDrawable(R.drawable.tab_event)).setContent(intent);
		tabHost.addTab(spec);
	}
	
	//Called when the activity is started.
	@Override public void onStart(){
		super.onStart();
		LifeTracking.setActivity(this);
		LifeTracking.checkIfLoaded(this);
	}
	
	//Called when the activity is resumed.
	@Override public void onResume(){
		super.onResume();
		LifeTracking.setActivity(this);
		LifeTracking.checkIfLoaded(this);
	}
}
