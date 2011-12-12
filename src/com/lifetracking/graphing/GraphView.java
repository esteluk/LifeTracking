package com.lifetracking.graphing;

import java.util.ArrayList;
import java.util.Calendar;

import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import com.lifetracking.R;
import com.lifetracking.UtilHelper;
import com.lifetracking.graphing.TimeAxis.TimeScale;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Region.Op;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

//GraphView is responsible for managing visible Graphs, window settings, and other things related to graphing.
public class GraphView extends View implements OnTouchListener {
	
	//Read-only please!
	public long m_minX, m_maxX;//date values corresponding to the window's x boundaries
	public double m_minY, m_maxY;//track values corresponding to the window's y boundaries
	
	public ArrayList<Graph> m_graphs;//public: READ-ONLY
	public Graph m_selectedGraph;//null if none selected
	public int m_selectedIndex;//index into the selected graph
	public long m_absoluteMinX, m_absoluteMaxX;//the furthest extents of the window's x boundaries
	private boolean m_needRecalculation;
	
	//Create new GraphView and add the given graph to it.
	public GraphView(Context context, Graph graph) {
		super(context);
		m_graphs = new ArrayList<Graph>();
		createPaints();
		
		final int showMaxValues = 14;
		int valuesSize = graph.getValuesSize();
		m_minX = graph.getX(Math.max(0, valuesSize - showMaxValues));
		m_maxX = graph.hasTwoXValues() ? graph.getX2(valuesSize - 1) : graph.getX(valuesSize - 1);
		if(m_minX > m_maxX){
			long temp = m_minX; m_minX = m_maxX; m_maxX = temp;
		}
		long deltaX = (long)(m_borderFactor * (m_maxX - m_minX));
		m_minX -= deltaX; m_maxX += deltaX;
		
		m_absoluteMinX = Long.MAX_VALUE;
		m_absoluteMaxX = -Long.MAX_VALUE;
		addGraph(graph);
		m_needRecalculation = true;
	}
	
	//Recalculate everything and redraw.
	public void needsRecalculation(){
		m_needRecalculation = true;
		invalidate();
	}
	
	//Add a graph to this view.
	public void addGraph(Graph graph){
		updateAbsolutesX(graph);
		m_graphs.add(graph);
		graph.addedToView(this);
		m_needRecalculation = true;
	}
	
	//Update absoluteMinX/MaxX to include the given graph.
	private void updateAbsolutesX(Graph graph){
		long deltaX = (long)(m_borderFactor * (m_maxX - m_minX));
		m_absoluteMinX = Math.min(m_absoluteMinX, graph.getX(0) + deltaX);
		if(graph.hasTwoXValues()){
			m_absoluteMaxX = Math.max(m_absoluteMaxX, graph.getX2(graph.getValuesSize() - 1) - deltaX);
		} else {
			m_absoluteMaxX = Math.max(m_absoluteMaxX, graph.getX(graph.getValuesSize() - 1) - deltaX);
		}
		if(m_absoluteMinX > m_absoluteMaxX){
			long temp = m_absoluteMinX; m_absoluteMinX = m_absoluteMaxX; m_absoluteMaxX = temp;
		}
	}
	
	//Remove a graph from this view.
	public void removeGraph(Graph graphToRemove){
		if(m_selectedGraph == graphToRemove) m_selectedGraph = null;
		graphToRemove.removedFromView();
		m_graphs.remove(graphToRemove);
		
		m_absoluteMinX = Long.MAX_VALUE;
		m_absoluteMaxX = -Long.MAX_VALUE;
		for(int n = 0; n < m_graphs.size(); n++){
			updateAbsolutesX(m_graphs.get(n));
		}
		if(m_maxX <= m_absoluteMinX){
			long deltaX = m_absoluteMinX - m_minX;
			m_minX += deltaX; m_maxX += deltaX;
		} else if(m_minX >= m_absoluteMaxX) {
			long deltaX = m_absoluteMaxX - m_maxX;
			m_minX += deltaX; m_maxX += deltaX;
		}
		
		m_needRecalculation = true;
	}
	
	//Remove all graphs.
	public void clearAllGraphs(){
		for(int n = 0; n < m_graphs.size(); n++){
			m_graphs.get(n).removedFromView();
		}
		m_graphs.clear();
	}
	
	//Edit currently selected value.
	public void editSelectedValue(){
		if(m_selectedGraph == null) return;
		Dialog dialog = m_selectedGraph.showEditValueDialog(m_selectedIndex);
		dialog.setOnDismissListener(new Dialog.OnDismissListener(){ public void onDismiss(DialogInterface dialog) {
			m_selectedGraph = null;
			needsRecalculation();
		}});
	}
	
	//Delete currently selected value.
	//return - true if the view is still valid.
	public boolean deleteSelectedValue(){
		if(m_selectedGraph == null) return true;
		m_selectedGraph.deleteValue(m_selectedIndex);
		boolean validView = m_selectedGraph.canBeGraphed() || m_graphs.size() > 1;
		m_selectedGraph = null;
		needsRecalculation();
		return validView;
	}
	
	//Compute the window parameters based on current graphs.
	public void updateWindow(Canvas canvas){
		m_maxX = Math.max(m_maxX, m_absoluteMinX);
		m_minX = Math.min(m_minX, m_absoluteMaxX);
		
		boolean allIncludeYZero = true;
		m_minY = Float.MAX_VALUE; m_maxY = -Float.MAX_VALUE;

		for(int n = 0; n < m_graphs.size(); n++){
			Graph graph = m_graphs.get(n);
			GraphingOptions options = graph.getGraphingOptions();
			if(!graph.updateIndices()) continue;
			if(!graph.hasYValues()) continue;
			int firstIndex = options.m_autoAdjustY ? graph.m_firstIndex : 0;
			int lastIndex = options.m_autoAdjustY ? graph.m_lastIndex : graph.getValuesSize() - 1; 

			//Compute window values.
			for(int i = firstIndex; i <= lastIndex; i++){
				double value = graph.getY(i);
				m_minY = Math.min(m_minY, value);
				m_maxY = Math.max(m_maxY, value);
			}
			if(options.m_includeYZero){
				m_minY = Math.min(m_minY, 0.0);
				m_maxY = Math.max(m_maxY, 0.0);
			} else {
				allIncludeYZero = false;
			}
		}
		if(Math.abs(m_minY - m_maxY) < 0.1 || m_minY == Float.MAX_VALUE){//handle the case where there is only one (or none) unique y value
			if(m_maxY > 0.0f) m_minY = 0.0f;
			else if(m_minY < 0.0f) m_maxY = 0.0f;
			else { m_minY = -1.0f; m_maxY = 1.0f; }
		}
		double deltaY = m_borderFactor * (m_maxY - m_minY);
		if((allIncludeYZero && m_minY >= 0.0) || MyLife.m_usePercentScale) m_minY = 0.0;
		else m_minY -= deltaY;
		if(allIncludeYZero && m_maxY <= 0.0) m_maxY = 0.0f;
		else m_maxY += deltaY;
		m_needRecalculation = false;
	}
	
	//Update this graph given the current canvas.
	//return - true if there is at least one graph with Y values.
	private boolean update(Canvas canvas){
		m_canvasWidth = canvas.getClipBounds().width();
		m_canvasHeight = canvas.getClipBounds().height();
		
		boolean yValuesExist = false;
		m_yAxisX = 0.0f;
		for(int n = 0; n < m_graphs.size(); n++){
			Graph graph = m_graphs.get(n);
			if(!graph.hasYValues()) continue;
			yValuesExist = true;
			for(int i = graph.m_firstIndex; i <= graph.m_lastIndex && i >= 0; i++){
				m_yAxisX = Math.max(m_yAxisX, m_yAxisTextPaint.measureText(UtilHelper.valueToString(graph.getY(i))));
			}
		}
		m_yAxisX += m_axisTextOffset;
		m_xAxisY = m_canvasHeight - 2.0f * m_xAxisTextPaint.getTextSize() - m_axisTextOffset;
		m_scaleX = (m_canvasWidth - m_yAxisX) / (double)(m_maxX - m_minX);
		m_scaleY = m_xAxisY / (double)(m_maxY - m_minY);
		m_scaleY *= -1.0f;//because the phone's coordinate system is upsidedown from the standard one.
		return yValuesExist;
	}
	
	//Move the window left (direction < 0) or right (direction > 0).
	public void slide(int direction){
		slideX((long)(0.1 * (long)direction * (m_maxX - m_minX)));
		needsRecalculation();
	}
	
	//Move the window left/right by deltaX.
	private void slideX(long deltaX){
		deltaX = Math.min(m_absoluteMaxX, m_minX + deltaX) - m_minX;
		deltaX = Math.max(m_absoluteMinX, m_maxX + deltaX) - m_maxX;
		m_minX += deltaX; m_maxX += deltaX;
	}
	
	//Zoom the window in (zoom < 0) or out (zoom > 0).
	public void zoom(int zoom){
		zoomX((long)(0.1 * (long)zoom * (m_maxX - m_minX)));
		needsRecalculation();
	}
	
	//Zoom the window by delta on both sides.
	private void zoomX(long delta){
		m_minX -= delta; m_maxX += delta;
	}

	//Variables used for onTouch().
	private long m_touchTimeOffset;//time offset value where user pressed down
	private long m_touchDistance = 0;//used for zooming
	private int m_oldFingerX = 0;
	private boolean m_moved = false;//if true, then the user moved the finger when it was down, so it wasn't a select
	
	//Process touch event.
	@Override public boolean onTouch(View v, MotionEvent event){
		if(event.getAction() == MotionEvent.ACTION_UP){
			//Find selected point or deselect all.
			if(event.getPointerCount() == 1) m_touchDistance = 0;
			if(!m_moved){
				int mouseX = (int)event.getX(), mouseY = (int)event.getY();
				Graph oldSelectedGraph = m_selectedGraph;
				int oldSelectedIndex = m_selectedIndex;
				
				//Find closest graph value, starting with graphs that have Y coordinates.
				m_selectedGraph = null;
				double minDistSq = Double.MAX_VALUE;
				boolean hasYCoord = true;
				do {
					for(int n = 0; n < m_graphs.size(); n++){
						Graph graph = m_graphs.get(n);
						if(hasYCoord != graph.hasYValues()) continue;
						for(int i = graph.m_firstIndex; i <= graph.m_lastIndex && i >= 0; i++){
							double distSq = graph.trySelect(i, mouseX, mouseY);
							if(distSq < minDistSq){
								minDistSq = distSq;
								m_selectedGraph = graph;
								m_selectedIndex = i;
							}
						}
					}
					hasYCoord = !hasYCoord;
				} while(!hasYCoord);
				
				//Try to show selected value's full info (if we clicked it again).
				if(m_selectedGraph != null){
					if(oldSelectedGraph == m_selectedGraph && oldSelectedIndex == m_selectedIndex){
						Toast.makeText(LifeTracking.g_globalContext, m_selectedGraph.getValueString(m_selectedIndex), Toast.LENGTH_LONG).show();
					} else if(m_selectedGraph.getNote(m_selectedIndex).length() > 0){//or just show the note
						Toast.makeText(LifeTracking.g_globalContext, m_selectedGraph.getNote(m_selectedIndex), Toast.LENGTH_LONG).show();
					}
				}
			}
		} else if(event.getAction() == MotionEvent.ACTION_DOWN){
			//Start tracking movement or zoom.
			m_moved = event.getPointerCount() != 1;
			m_oldFingerX = (int) event.getX();
			m_touchTimeOffset = convertToGraphX((int)event.getX());
			if(event.getPointerCount() == 2){
				m_touchDistance = Math.abs(convertToGraphX((int)event.getX(0)) - convertToGraphX((int)event.getX(1)));
			}
		} else if(event.getAction() == MotionEvent.ACTION_MOVE){
			if(event.getPointerCount() == 1 && m_touchDistance == 0){
				slideX(m_touchTimeOffset - convertToGraphX((int)event.getX()));
				m_moved |= Math.abs(m_oldFingerX - event.getX()) >= 20.0f;
			} else if(event.getPointerCount() == 2){
				long curDistance = Math.abs(convertToGraphX((int)event.getX(0)) - convertToGraphX((int)event.getX(1)));
				if(m_touchDistance > 0) zoomX((curDistance - m_touchDistance) / 2);
				m_touchDistance = curDistance;
				m_moved = true;
			}
			m_needRecalculation = true;
		}
		
		invalidate();
		return true;
	}
	
	//Prepare the options menu based on currently selected graph.
	public void prepareOptionsMenu(Menu menu){
		menu.findItem(R.id.GraphMenu_Options_Selection).setEnabled(m_selectedGraph != null);
	}
	
	///=================================
	/// Graphing stuff.
	///=================================
	private final double m_borderFactor = 0.05;//subract/add this much border to x/y min/max
	private final float m_hashSize = 3.0f;//half size
	private final float m_axisTextOffset = 4.0f;
	
	private int m_canvasWidth, m_canvasHeight;
	private float m_xAxisY, m_yAxisX;
	private double m_scaleX, m_scaleY;
	
	private TextPaint m_bgGraphNamePaint;
	private Paint m_axisPaint, m_axisHashPaint, m_nowPaint;
	private TextPaint m_xAxisTextPaint, m_yAxisTextPaint, m_xAxisUnitsPaint;
	private static int m_graphColors[];
	
	//Draw event.
	@Override public void onDraw(Canvas canvas) {
		if(m_needRecalculation) updateWindow(canvas);
		boolean yValuesExist = update(canvas);
		
		//Draw graph name if there is only one graph or a selected graph.
		if(m_graphs.size() == 1 || m_selectedGraph != null){
			Graph graph = m_selectedGraph != null ? m_selectedGraph : m_graphs.get(0);
			Rect bgTextRect = new Rect();
			m_bgGraphNamePaint.getTextBounds(graph.getName(), 0, graph.getName().length(), bgTextRect);
			canvas.drawText(graph.getName(), m_canvasWidth, bgTextRect.height(), m_bgGraphNamePaint);
		}
		Rect oldClip = new Rect(canvas.getClipBounds());
		canvas.clipRect(yValuesExist ? m_yAxisX : 0.0f, 0.0f, m_canvasWidth, m_xAxisY);
		
		//Draw all graphs, starting with Intervals.
		boolean drawIntervals = true;
		do {
			for(int n = 0; n < m_graphs.size(); n++){
				Graph graph = m_graphs.get(n);
				if(graph.hasTwoXValues() != drawIntervals) continue;
				//if(!windowUpdated) if(!graph.update(canvas)) continue;
				graph.renderValues(canvas, m_graphColors[n % m_graphColors.length]);
			}
			drawIntervals = !drawIntervals;
		} while(!drawIntervals);
		
		if(m_selectedGraph != null) m_selectedGraph.renderSelectedValue(canvas, m_selectedIndex, m_xAxisY, m_yAxisX);
		canvas.clipRect(oldClip, Op.REPLACE);
		
		Rect xAxisLabelBounds = null, yAxisLabelBounds = null;
		if(m_selectedGraph != null){
			xAxisLabelBounds = m_selectedGraph.renderXAxisLabel(canvas, m_selectedIndex, m_xAxisY + m_axisTextOffset, m_xAxisTextPaint);
			yAxisLabelBounds = m_selectedGraph.renderYAxisLabel(canvas, m_selectedIndex, m_yAxisX - m_axisTextOffset, m_yAxisTextPaint);
		}
		
		int nowX = convertToPixelX(MyLife.getOffsetFromCalendar(Calendar.getInstance()));
		canvas.drawLine(nowX, 0.0f, nowX, m_xAxisY, m_nowPaint);
		drawAxis(canvas, xAxisLabelBounds, yAxisLabelBounds, yValuesExist);
	}
	
	//Draw the x-y axis for the graph, along with axis hashmarks and labels.
	private void drawAxis(Canvas canvas, Rect xAxisLabelBounds, Rect yAxisLabelBounds, boolean yValuesExist){
		if(yValuesExist) canvas.drawLine(m_yAxisX, 0.0f, m_yAxisX, m_xAxisY, m_axisPaint);
		canvas.drawLine(0.0f, m_xAxisY, m_canvasWidth, m_xAxisY, m_axisPaint);
		
		if(yValuesExist){
			//Render hash marks and hash labels for Y axis.
			Rect underXaxisRect = new Rect(0, (int)m_xAxisY, (int)m_yAxisX, m_canvasHeight);
			Rect oldLabelBounds = null;
			double hashY = Math.pow(10.0, Math.floor(Math.log10(m_maxY - m_minY) - 0.5));
			
			//Go through all hash marks and hash labels to see how many we should skip when rendering labels.
			double y = (int)Math.ceil(m_minY / hashY) * hashY;
			int mostSkipped = 0, curSkipped = 0;
			for(;; y += hashY){
				int pixelY = convertToPixelY(y);
				String valueString = UtilHelper.valueToString(y);
				if(MyLife.m_usePercentScale) valueString += "%";
				canvas.drawLine(m_yAxisX, pixelY, m_yAxisX + m_hashSize, pixelY, m_axisHashPaint);
				Rect labelBounds = UtilHelper.getTextBounds(m_yAxisTextPaint, valueString);
				labelBounds.offset(Math.round(m_yAxisX - m_axisTextOffset), Math.round(pixelY + labelBounds.height() / 2.0f));
				if(labelBounds.bottom < 0) break;
				if(oldLabelBounds != null && Rect.intersects(labelBounds, oldLabelBounds)){
					curSkipped++;
					continue;
				}
				mostSkipped = Math.max(mostSkipped, curSkipped);
				curSkipped = 0;
				oldLabelBounds = labelBounds;
			}
			
			//Render hash marks and hash labels for Y axis.
			hashY *= (long)(mostSkipped + 1);
			y = (int)Math.ceil(m_minY / hashY) * hashY;
			for(;; y += hashY){
				int pixelY = convertToPixelY(y);
				String valueString = UtilHelper.valueToString(y);
				if(MyLife.m_usePercentScale) valueString += "%";
				canvas.drawLine(m_yAxisX - m_hashSize, pixelY, m_yAxisX, pixelY, m_axisHashPaint);
				Rect labelBounds = UtilHelper.getTextBounds(m_yAxisTextPaint, valueString);
				labelBounds.offset(Math.round(m_yAxisX - m_axisTextOffset), Math.round(pixelY + labelBounds.height() / 2.0f));
				if(labelBounds.bottom < 0) break;
				if(yAxisLabelBounds != null && Rect.intersects(labelBounds, yAxisLabelBounds)) continue;
				if(Rect.intersects(labelBounds, underXaxisRect)) continue;
				canvas.drawText(valueString, m_yAxisX - m_axisTextOffset, pixelY + labelBounds.height() / 2.0f, m_yAxisTextPaint);
			}
		}
		
		//Compute values for the x axis.
		TimeScale timeScale = TimeAxis.computeAxisVariables(m_maxX - m_minX);
		long hashX = timeScale.m_hashDist;
		
		//Go through all hash marks and hash labels to see how many we should skip when rendering labels.
		Rect oldLabelBounds = null;
		long x = (m_minX / hashX) * hashX;
		int mostSkipped = 0, curSkipped = 0;
		for(;; x += hashX){
			int pixelX = convertToPixelX(x);
			String valueString = timeScale.formatDate(MyLife.getCalendarFromOffset(x));
			canvas.drawLine(pixelX, m_xAxisY - m_hashSize, pixelX, m_xAxisY, m_axisHashPaint);
			Rect labelBounds = UtilHelper.getTextBounds(m_xAxisTextPaint, valueString + "||");//+"||" for some space
			labelBounds.offset(Math.round(pixelX), Math.round(m_xAxisY + m_axisTextOffset + labelBounds.height()));
			if(labelBounds.left > m_canvasWidth) break;
			if(oldLabelBounds != null && Rect.intersects(labelBounds, oldLabelBounds)) {
				curSkipped++;
				continue;
			}
			mostSkipped = Math.max(mostSkipped, curSkipped);
			curSkipped = 0;
			oldLabelBounds = labelBounds;
		}
		
		//Render hash marks and hash labels for X axis.
		hashX *= (long)(mostSkipped + 1);
		x = (m_minX / hashX) * hashX;
		for(;; x += hashX){
			int pixelX = convertToPixelX(x);
			String valueString = timeScale.formatDate(MyLife.getCalendarFromOffset(x));
			canvas.drawLine(pixelX, m_xAxisY, pixelX, m_xAxisY + m_hashSize, m_axisHashPaint);
			Rect labelBounds = UtilHelper.getTextBounds(m_xAxisTextPaint, valueString + "||");//+"||" for some space
			labelBounds.offset(Math.round(pixelX), Math.round(m_xAxisY + m_axisTextOffset + labelBounds.height()));
			if(labelBounds.left > m_canvasWidth) break;
			if(xAxisLabelBounds != null && Rect.intersects(labelBounds, xAxisLabelBounds)) continue;
			canvas.drawText(valueString, pixelX, m_xAxisY + m_axisTextOffset + labelBounds.height(), m_xAxisTextPaint);
		}

		//Render the missing date part.
		if(m_selectedGraph == null){
			String format = TimeAxis.getMoreFormat(timeScale.m_hashDist);
			String startDate = DateFormat.format(format, MyLife.getCalendarFromOffset(convertToGraphX(0)).getTime()).toString();
			String endDate = DateFormat.format(format, MyLife.getCalendarFromOffset(convertToGraphX(m_canvasWidth)).getTime()).toString();
			String subDate = startDate.compareTo(endDate) == 0 ? startDate : (startDate + " - " + endDate);
			canvas.drawText(subDate, m_canvasWidth / 2, m_canvasHeight, m_xAxisUnitsPaint);
		}
	}
	
	//return - Graph x coordinate converted to Canvas x coordinate.
	public int convertToPixelX(long x){
		return (int)((x - m_minX) * m_scaleX + m_yAxisX);
	}
	
	//return - Graph y coordinate converted to Canvas y coordinate.
	public int convertToPixelY(double y){
		return (int)((y - m_minY) * m_scaleY + m_xAxisY);
	}
	
	//return - Canvas x coordinate converted to Graph x coordinate.
	public long convertToGraphX(int x){
		return (long)((x - m_yAxisX) / m_scaleX + m_minX);
	}
	
	//return - Canvas y coordinate converted to Graph y coordinate.
	public double convertToGraphY(int y){
		return (double)((y - m_xAxisY) / m_scaleY + m_minY);
	}
	
	//Instantitate necessary Paint objects.
	public void createPaints(){
		final float textSize = MyLife.m_useBigFont ? 24.0f : 14.0f;
		m_axisPaint = new Paint();
		m_axisPaint.setColor(Color.WHITE);
		m_axisPaint.setStrokeWidth(2.0f);
		m_axisHashPaint = new Paint(m_axisPaint);
		m_axisHashPaint.setStrokeWidth(1.0f);
		m_nowPaint = new Paint();
		m_nowPaint.setColor(Color.LTGRAY);
		m_nowPaint.setStrokeWidth(3.0f);
		m_nowPaint.setPathEffect(new DashPathEffect(new float[]{13.0f, 6.0f}, 0));
		
		m_xAxisTextPaint = new TextPaint();
		m_xAxisTextPaint.setColor(Color.WHITE);
		m_xAxisTextPaint.setTextAlign(Align.CENTER);
		m_xAxisTextPaint.setTextSize(textSize);
		m_xAxisTextPaint.setTypeface(Typeface.SERIF);
		m_xAxisTextPaint.setAntiAlias(true);
		m_yAxisTextPaint = new TextPaint(m_xAxisTextPaint);
		m_yAxisTextPaint.setTextAlign(Align.RIGHT);
		m_xAxisUnitsPaint = new TextPaint(m_xAxisTextPaint);
		m_xAxisUnitsPaint.setTextAlign(Align.CENTER);
		
		m_bgGraphNamePaint = new TextPaint();
		m_bgGraphNamePaint.setTextAlign(Align.RIGHT);
		m_bgGraphNamePaint.setTextSize(36.0f);
		m_bgGraphNamePaint.setColor(Color.rgb(10, 10, 10));
		m_bgGraphNamePaint.setAntiAlias(true);
		
		m_graphColors = new int[]{Color.GREEN, Color.RED, Color.CYAN, Color.LTGRAY, Color.MAGENTA, Color.YELLOW};
	}
}
