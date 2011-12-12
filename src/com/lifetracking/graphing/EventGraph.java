package com.lifetracking.graphing;

import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.text.TextPaint;
import com.lifetracking.SaveLoad;
import com.lifetracking.UtilHelper;
import com.lifetracking.events.Event;
import com.lifetracking.events.EventType;

//IntervalGraph is a Graph for an IntervalType.
public class EventGraph extends Graph {

	private EventType m_eventType;
	
	//Create new TrackGraph.
	public EventGraph(EventType eventType) {
		m_eventType = eventType;
	}
	
	//return - number of values in this graph.
	@Override public int getValuesSize(){
		return m_eventType.m_events.size();
	}
	
	//return - x coordinate for a graph value with the given index.
	@Override public long getX(int index){
		return m_eventType.m_events.get(index).m_start;
	}
	
	//return - 0L. EventGraph values don't have second X coordinate.
	@Override public long getX2(int index){
		return 0L;
	}
	
	//return - 0.0f. EventGraph values don't have Y coordinate.
	@Override public double getY(int index){
		return 0.0f;
	}
	
	//return - distance^2 from point (x,y) (in pixel coordinates) to the graph value at the given index. Double.MAX_VALUE if value isn't in selection range.
	@Override public double trySelect(int index, int x, int y){
		Event value = m_eventType.m_events.get(index);
		int startX = m_view.convertToPixelX(value.m_start);
		double dx = Math.abs(startX - x);
		return dx <= 10.0 ? (dx * dx) : Double.MAX_VALUE;
	}
	
	//return - visible dialog for editing the graph value at the given index.
	@Override public Dialog showEditValueDialog(int index){
		return m_eventType.showEditEventDialog(m_eventType.m_events.get(index));
	}
	
	//Remove the graph value at the given index.
	@Override public void deleteValue(int index){
		m_eventType.removeEvent(index);
		SaveLoad.saveEventType(m_eventType);
	}
	
	//return - full string representation of the graph value at the given index.
	@Override public String getValueString(int index){
		return m_eventType.m_events.get(index).toString();
	}
	
	//return - note for the graph value at the given index.
	@Override public String getNote(int index){
		return m_eventType.m_events.get(index).m_note;
	}
	
	//return - true. IntervalGraph has two X coodinates: start and end.
	@Override public boolean hasTwoXValues(){
		return false;
	}
	
	//return - false. IntervalGraph values don't have Y coordinate.
	@Override public boolean hasYValues(){
		return false;
	}
	
	//return - name of this graph.
	@Override public String getName(){
		return m_eventType.m_name;
	}
	
	//return - graphing optiosn for this graph.
	@Override public GraphingOptions getGraphingOptions(){
		return m_eventType.m_graphingOptions;
	}
	
	///=================================
	/// Graphing functions.
	///=================================
	private final static DashPathEffect m_eventLineDashPathEffect = new DashPathEffect(new float[]{5.0f, 3.0f}, 0);
	private final static DashPathEffect m_noteLineDashPathEffect = new DashPathEffect(new float[]{5.0f, 27.0f}, 0);
	private final static float m_eventLineThickness = 3.0f;
	
	private static Paint m_eventLinePaint, m_noteLinePaint;
	private static int m_normalColor, m_selectionColor;
	
	//Render this graph's values.
	@Override public void renderValues(Canvas canvas, int graphColor) {
		if(m_eventType.m_events.size() < 0) return;
		if(m_firstIndex == -1 || m_lastIndex == -1) return;
		
		m_normalColor = graphColor;
		m_eventLinePaint.setColor(m_normalColor);
		

		Rect clipBounds = canvas.getClipBounds();
		for(int i = m_firstIndex; i <= m_lastIndex; i++){
			drawEvent(canvas, i, clipBounds.top, clipBounds.bottom);
		}
	}
	
	//Draw an event (given its index).
	//top, bottom - Y values for the event line.
	private void drawEvent(Canvas canvas, int index, float top, float bottom){
		Event event = m_eventType.m_events.get(index);
		int x = m_view.convertToPixelX(event.m_start);
		canvas.drawLine(x, top, x, bottom, m_eventLinePaint);
		if(event.m_note.compareTo("") != 0){
			canvas.drawLine(x, top, x, bottom, m_noteLinePaint);
		}
	}
	
	//Render the graph value at the given index as a selected value.
	@Override public void renderSelectedValue(Canvas canvas, int index, float xAxisY, float yAxisX){
		m_eventLinePaint.setColor(m_selectionColor);
		drawEvent(canvas, index, 0.0f, xAxisY);
		m_eventLinePaint.setColor(m_normalColor);
	}
	
	//Render the x axis label for selected graph value at the given index.
	//return - rectangle bounding the rendered text.
	@Override public Rect renderXAxisLabel(Canvas canvas, int index, float positionY, TextPaint textPaint){
		int oldColor = textPaint.getColor();
		textPaint.setColor(m_selectionColor);
		textPaint.setTextAlign(Align.CENTER);
		
		Event event = m_eventType.m_events.get(index);
		int px = m_view.convertToPixelX(event.m_start);
		String date = event.getStartDateString(), time = event.getStartTimeString();
		Rect bounds1 = UtilHelper.getTextBounds(textPaint, time);
		Rect bounds2 = UtilHelper.getTextBounds(textPaint, date);
		float py1 = positionY + bounds1.height();
		float py2 = py1 + bounds2.height();
		canvas.drawText(time, px, py1, textPaint);
		canvas.drawText(date, px, py2, textPaint);
		textPaint.setColor(oldColor);
		bounds1.offset(px, (int)py1);
		bounds2.offset(px, (int)py2);
		bounds1.union(bounds2);
		return bounds1;
	}
	
	//Render the y axis label for selected graph value at the given index.
	//return - null. IntervalGraph values don't have Y coordinate.
	@Override public Rect renderYAxisLabel(Canvas canvas, int index, float positionX, TextPaint textPaint){
		return null;
	}
	
	//Instantitate necessary Paint objects.
	static {
		m_normalColor = Color.GREEN;
		m_selectionColor = Color.rgb(64, 64, 255);
		m_eventLinePaint = new Paint();
		m_eventLinePaint.setStrokeWidth(m_eventLineThickness);
		m_eventLinePaint.setColor(m_normalColor);
		m_eventLinePaint.setPathEffect(m_eventLineDashPathEffect);
		m_noteLinePaint = new Paint(m_eventLinePaint);
		m_noteLinePaint.setColor(Color.WHITE);
		m_noteLinePaint.setPathEffect(m_noteLineDashPathEffect);
	}
}
