/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.cache;

public class ProcessedImageUtility {
	public static String getAlgorithmNameForEdge(int colorIndex) {
		if (colorIndex >= 0 && colorIndex <= 1)
			return "edge_" + Integer.toString(colorIndex);
		return "edge";
	}
}
