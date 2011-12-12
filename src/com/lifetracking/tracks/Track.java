package com.lifetracking.tracks;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Random;
import com.lifetracking.MyLife;
import com.lifetracking.SaveLoad;
import com.lifetracking.graphing.GraphingOptions;
import com.lifetracking.graphing.TrackGraph;
import android.app.Dialog;

//Track is a list of data points created by the user.
public abstract class Track {
	
	//============================================ STATIC ================================================
	
	//Store all user tracks. DO NOT insert tracks manually.
	public static ArrayList<Track> m_tracks;//public: READ-ONLY
	public static String m_tracksPrefix = "Track";
	static {
		m_tracks = new ArrayList<Track>();
	}
	
	public static void addExampleTrack(){
		Track t = new NumberTrack("Example");
		m_tracks.add(t);
		
		Random r = new Random();
		long offset = 0;
		float value = (int)(r.nextFloat() * 300.0f) / 10.0f;
		for(int n = 0; n < 50; n++){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(MyLife.m_originDate.getTimeInMillis() + offset);
			value += (int)(r.nextFloat() * 10.0f - 5.0f) / 10.0f;
			t.addTrackValue(value, calendar).m_note = "Once upon a time there was a boy whose name was Alexei!!!";
			offset += 400000000L * (0.1f * r.nextFloat() + 0.9f);
		}
		
		t = new NumberTrack("Example2");
		m_tracks.add(t);
		
		offset = 1600000000L;
		value = (int)(r.nextFloat() * 300.0f) / 10.0f;
		for(int n = 0; n < 60; n++){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(MyLife.m_originDate.getTimeInMillis() + offset);
			value += (int)(r.nextFloat() * 10.0f) / 10.0f;
			t.addTrackValue(value, calendar).m_note = "2twoosies";
			offset += 400000000L * (0.1f * r.nextFloat() + 0.9f);
		}
	}

	//Track types.
	public static enum TrackType {Number};
	public static Track createNewTrack(TrackType trackType) {
		switch(trackType){
			case Number: return new NumberTrack("null");
			default: assert(false);
		}
		return null;
	}
	
	//Add the given track to the global list of tracks.
	//return - the given track.
	public static Track addTrack(Track track){
		m_tracks.add(track);
		SaveLoad.saveTrack(track);
		return track;
	}
	
	//Add a new track with the given name to the global list of tracks, but only if there is no track with the same name.
	//return - track with the given name (newly created if not found).
	public static Track addTrack(String trackName, boolean onlyIfUniqueName){
		if(onlyIfUniqueName){
			for(Track t : m_tracks){
				if(trackName.compareTo(t.m_name) == 0) return t;
			}
		}
		return addTrack(new NumberTrack(trackName));
	}
	
	//Remove the track at the given index from the list of tracks and delete corresponding file.
	public static void removeTrack(int index){
		Track track = m_tracks.get(index);
		m_tracks.remove(index);
		SaveLoad.deleteTrack(track);
	}
	
	//Remove the track from the list of tracks and delete corresponding file.
	public static void removeTrack(Track track){
		m_tracks.remove(track);
		SaveLoad.deleteTrack(track);
	}
	
	//======================================== END STATIC ================================================
	
	//Track instance variables.
	public long m_id;//acts as a unique id for the file
	public String m_name;
	public TrackType m_type;
	public ArrayList<TrackValue> m_values;//public: READ-ONLY
	public TrackValue m_lastAddedTrackValue;
	public TrackGraph m_graph;
	public GraphingOptions m_graphingOptions;

	//Create new Track with the given name and type.
	public Track(TrackType type, String name){
		m_id = new Random().nextLong();
		m_name = name;
		m_type = type;
		m_values = new ArrayList<TrackValue>();
		m_lastAddedTrackValue = null;
		m_graph = new TrackGraph(this);
		m_graphingOptions = new GraphingOptions();
	}
	
	//return - true if this track's graph is assigned to a GraphView.
	public boolean isGraphed(){
		return m_graph.isGraphed();
	}
	
	//Add a new value to this track.
	public TrackValue addTrackValue(float value){
		return addTrackValue(new TrackValue(value));
	}
	
	//Add a new value to this track, but with a preset date.
	public TrackValue addTrackValue(float value, Calendar date){
		return addTrackValue(new TrackValue(value, date));
	}
	
	//Add track value to this track.
	public TrackValue addTrackValue(TrackValue value){
		m_lastAddedTrackValue = value;
		//Insert the value into a sorted order by date.
		for(int n = 0; n < m_values.size(); n++){
			if(m_values.get(n).m_dateOffset > value.m_dateOffset){
				m_values.add(n, value);
				return value;
			}
		}
		m_values.add(m_values.size(), value);
		return value;
	}
	
	//Remove track value at the given index.
	public void removeTrackValue(int index){
		TrackValue value = m_values.remove(index);
		if(m_lastAddedTrackValue == value) m_lastAddedTrackValue = null;
	}
	
	public abstract Dialog showAddTrackValueDialog();
	public abstract Dialog showEditTrackValueDialog(TrackValue value);
}
