package com.htmlhifive.testexplorer.cache;

public class ProcessedImageUtility {
	public static String getAlgorithmNameForEdge(int colorIndex)
	{
		if (colorIndex >= 0 && colorIndex <= 1)
			return "edge_" + Integer.toString(colorIndex);
		return "edge";
	}
}
