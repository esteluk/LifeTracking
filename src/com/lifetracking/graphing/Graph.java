package com.lifetracking.graphing;

import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;

//Graph is an abstract class for an entity that can be graphed in GraphView.
public abstract class Graph {
	
	public GraphView m_view;
	public int m_firstIndex, m_lastIndex;//roughly, the indices for the visible values (usually including at least on just outside the screen)

	//Called when the graph is added/removed from the GraphView.
	public void addedToView(GraphView view){
		m_view = view;
	}
	
	//Called when the graph is added/removed from the GraphView.
	public void removedFromView(){
		m_view = null;
	}
	
	//return - true if this graph is assigned to a GraphView.
	public boolean isGraphed(){
		return m_view != null;
	}
	
	//return - true if this graph is OK to graph.
	public boolean canBeGraphed(){
		return getValuesSize() >= 2;
	}
	
	//Update the indices that are active for this graph.
	//return - true if the graph should be rendered.
	public boolean updateIndices(){
		m_firstIndex = m_lastIndex = -1;
		int valuesSize = getValuesSize();
		for(int i = 0; i < valuesSize; i++){
			long start = getX(i);
			long end = hasTwoXValues() ? getX2(i) : start;
			if(end >= m_view.m_minX && start <= m_view.m_maxX){
				if(m_firstIndex == -1) m_firstIndex = i;
				m_lastIndex = i;
			}
		}
		if(m_firstIndex == -1 || m_lastIndex == -1) return false;
		//try to include track values just outside the window range
		if(m_firstIndex > 1) m_firstIndex--;
		if(m_lastIndex < valuesSize - 1) m_lastIndex++;
		return true;
	}

	//return - number of values in this graph.
	public abstract int getValuesSize();
	
	//return - true if the graph values of this graph have two X values.
	public abstract boolean hasTwoXValues();
	
	//return - true if this graph has Y values.
	public abstract boolean hasYValues();
	
	//return - x/x2/y coordinate for the graph value at the given index. (x2 and y are optional.) (x1<=x2)
	public abstract long getX(int index);
	public abstract long getX2(int index);
	public abstract double getY(int index);
	
	//return - distance^2 from point (x,y) (in pixel coordinates) to the graph value at the given index. Double.MAX_VALUE if value isn't in selection range.
	public abstract double trySelect(int index, int x, int y);
	
	//return - visible dialog for editing the graph value at the given index.
	public abstract Dialog showEditValueDialog(int index);
	
	//Remove the graph value at the given index.
	public abstract void deleteValue(int index);
	
	//return - full string representation of the graph value at the given index.
	public abstract String getValueString(int index);
	
	//return - note for the graph value at the given index.
	public abstract String getNote(int index);
	
	//return - name of this graph.
	public abstract String getName();
	
	//return - graphing optiosn for this graph.
	public abstract GraphingOptions getGraphingOptions();
	
	//Render this graph's values.
	public abstract void renderValues(Canvas canvas, int graphColor);
	
	//Render the graph value at the given index as a selected value.
	public abstract void renderSelectedValue(Canvas canvas, int index, float xAxisY, float yAxisX);
	
	//Render the x/y axis label for selected graph value at the given index.
	//return - rectangle bounding the rendered text. Null if none rendered.
	public abstract Rect renderXAxisLabel(Canvas canvas, int index, float positionY, TextPaint textPaint);
	public abstract Rect renderYAxisLabel(Canvas canvas, int index, float positionX, TextPaint textPaint);
}
