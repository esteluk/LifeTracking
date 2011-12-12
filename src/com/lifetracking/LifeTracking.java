package com.lifetracking;

import com.lifetracking.events.EventType;
import com.lifetracking.intervals.IntervalType;
import com.lifetracking.tracks.Track;
import com.lifetracking.ui.MainActivity;
import com.lifetracking.ui.WelcomeActivity;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuInflater;

//This is the primary activity that gets launched.
public class LifeTracking extends Activity {

	public static Context g_globalContext;//each activity, when launched, sets this context to itself
	public static LayoutInflater g_inflater;
	public static MenuInflater g_menuInflater;

	//Called when the activity is first created.
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActivity(this);
		
		//Run loading thread to load track files.
		final Dialog dialog = ProgressDialog.show(this, "", "Loading data. Please wait...", true);
		LoadingThread.m_threadEndHandler = new Handler(){ public void handleMessage(Message msg) {
			dialog.dismiss();
			startActivity(new Intent(LifeTracking.this, MyLife.m_showWelcomeScreen ? WelcomeActivity.class : MainActivity.class));
		}};
		LoadingThread thread = new LoadingThread();
		thread.start();
	}
	
	//Called when the activity is started.
	@Override public void onStart(){
		super.onStart();
		setActivity(this);
	}
	
	//Called when the activity is resumed.
	@Override public void onResume(){
		super.onResume();
		setActivity(this);
	}
	
	//Set current global activity.
	public static void setActivity(Activity activity){
		g_globalContext = activity;
		g_inflater = activity.getLayoutInflater();
		g_menuInflater = activity.getMenuInflater();
	}
	
	//Check if the data is loaded. This fixes the problem when Advanced Task Killer kills the app, user relaunches it, but the data isn't loaded.
	public static void checkIfLoaded(Activity activity){
		if(!MyLife.m_loadedData) {
			activity.startActivity(new Intent(activity, LifeTracking.class));
			activity.finish();
		}
	}
	
	//LifeActivity is an Activity that sets some global variables when it becomes active in any way.
	public static class LifeActivity extends Activity {
		
		//Called when the activity is created.
		@Override public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			setActivity(this);
			checkIfLoaded(this);
		}
		
		//Called when the activity is started.
		@Override public void onStart(){
			super.onStart();
			setActivity(this);
			checkIfLoaded(this);
		}
		
		//Called when the activity is resumed.
		@Override public void onResume(){
			super.onResume();
			setActivity(this);
			checkIfLoaded(this);
		}
	}
	
	//LoadingThread is used to asynchronously load all data.
	public static class LoadingThread extends Thread {
		
		public static Handler m_threadEndHandler = null;
		
		//Run this thread.
		public void run() {
			boolean debug = false;
			SaveLoad.loadMyLife();
			
			Track.m_tracks.clear();
			IntervalType.m_intervalTypes.clear();
			EventType.m_eventTypes.clear();
			if(debug) Track.addExampleTrack();
			if(debug) IntervalType.addExampleIntervalTypes();
			if(debug) EventType.addExampleEventTypes();
			
			String files[] = g_globalContext.fileList();
			for (String fileName : files) {
				if(debug){
					g_globalContext.deleteFile(fileName);
				} else if(fileName.startsWith(Track.m_tracksPrefix)){
					Track track = SaveLoad.loadTrack(fileName);
					if(track != null) Track.m_tracks.add(track);
					else LifeTracking.g_globalContext.deleteFile(fileName);
				} else if(fileName.startsWith(IntervalType.m_intervalTypesPrefix)){
					IntervalType intervalType = SaveLoad.loadIntervalType(fileName);
					if(intervalType != null) IntervalType.m_intervalTypes.add(intervalType);
					else LifeTracking.g_globalContext.deleteFile(fileName);
				} else if(fileName.startsWith(EventType.m_eventTypesPrefix)){
					EventType eventType = SaveLoad.loadEventType(fileName);
					if(eventType != null) EventType.m_eventTypes.add(eventType);
					else LifeTracking.g_globalContext.deleteFile(fileName);
				}
			}
			
			MyLife.m_loadedData = true;
			if(m_threadEndHandler != null){
				m_threadEndHandler.dispatchMessage(m_threadEndHandler.obtainMessage());
				m_threadEndHandler = null;
			}
		}
	}
}