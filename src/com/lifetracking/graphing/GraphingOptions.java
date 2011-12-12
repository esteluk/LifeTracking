package com.lifetracking.graphing;

//GraphingOptions contain different options for drawing a graph.
public class GraphingOptions {

	public boolean m_autoAdjustY;
	public boolean m_includeYZero;
	
	//Create new GraphingOptions.
	public GraphingOptions(){
		m_autoAdjustY = false;
		m_includeYZero = false;
	}
}
