/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.api;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.htmlhifive.testexplorer.image.EdgeDetector;
import com.htmlhifive.testexplorer.model.Screenshot;
import com.htmlhifive.testlib.image.utlity.ImageUtility;

@Controller
@RequestMapping("/image")
public class ImageController {

	// PropertyKey
	private static final String RESULTS_DIR = "resultsDir";

	// SessionKey
	private static final String KEY_INDEX_MAP = "INDEX_MAP";

	@Autowired
	private HttpServletRequest request;
	@Autowired
	private Properties apiConfig;

	private static Logger log = LoggerFactory.getLogger(ImageController.class);

	/**
	 * Get the image from id.
	 *
	 * @param id test execution result id or right image id
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public void getImage(@RequestParam String id, HttpServletResponse response) {
		@SuppressWarnings("unchecked")
		Map<String, Screenshot> screenshotMap = (Map<String, Screenshot>) request.getSession(false).getAttribute(KEY_INDEX_MAP);

		// Parameter validation
		Screenshot screenshot = screenshotMap.get(id);
		if (screenshot == null) {
			log.error("id(" + id + ") is invalid parameter.");
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return;
		}

		// Send PNG image
		try {
			File file = getFile(screenshot);
			sendFile(file, response);
		}  catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	/**
	 * Get edge detection result of an image.
	 *
	 * @param id id of an image to be processed by edge detector.
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/getEdge", method = RequestMethod.GET)
	public void getEdgeImage(@RequestParam String id,
							 @RequestParam(defaultValue = "-1") int colorIndex, HttpServletResponse response)
	{
		@SuppressWarnings("unchecked")
		Map<String, Screenshot> screenshotMap = (Map<String, Screenshot>) request.getSession(false).getAttribute(KEY_INDEX_MAP);

		// Parameter validation
		Screenshot screenshot = screenshotMap.get(id);
		if (screenshot == null) {
			log.error("id(" + id + ") is invalid parameter.");
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return;
		}

		try {
			BufferedImage image = ImageIO.read(getFile(screenshot));
			EdgeDetector edgeDetector = new EdgeDetector(0.5);

			switch (colorIndex)
			{
				case 0:
					edgeDetector.setBackgroundColor(new Color(0, 0, 0, 255));
					edgeDetector.setForegroundColor(new Color(255, 0, 0, 255));
					break;
				case 1:
					edgeDetector.setBackgroundColor(new Color(0, 0, 0, 0));
					edgeDetector.setForegroundColor(new Color(0, 0, 255, 128));
					break;
			}

			BufferedImage edgeImage = edgeDetector.DetectEdge(image);
			sendImage(edgeImage, response);
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	/**
	 * Get the diff image With a marker of comparison result. If there is no difference, return normal image.
	 *
	 * @param sourceId comparison source image id
	 * @param targetId comparison target image id
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/getDiff", method = RequestMethod.GET)
	public void getDiffImage(@RequestParam String sourceId, @RequestParam String targetId, HttpServletResponse response) {
		@SuppressWarnings("unchecked")
		Map<String, Screenshot> screenshotMap = (Map<String, Screenshot>) request.getSession(false).getAttribute(KEY_INDEX_MAP);

		// Validate Parameters.
		Screenshot sourceScreenshot = screenshotMap.get(sourceId);
		if (sourceScreenshot == null) {
			log.error("id(" + sourceId + ") is invalid parameter.");
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return;
		}

		Screenshot targetScreenshot = screenshotMap.get(targetId);
		if (targetScreenshot == null) {
			log.error("id(" + targetId + ") is invalid parameter.");
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return;
		}

		try {
			File source = getFile(sourceScreenshot);
			File target = getFile(targetScreenshot);

			// Create a partial image
			BufferedImage actual = ImageIO.read(source);
			BufferedImage expected = ImageIO.read(target);

			// Compare.
			List<Point> diffPoints = ImageUtility.compareImages(expected, null, actual, null, null, null);
			if (diffPoints.isEmpty()) {
				sendFile(source, response);
			} else {
				BufferedImage marked = ImageUtility.getMarkedImage(actual, diffPoints);
				sendImage(marked, response);
			}
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	private File getFile(Screenshot screenshot) throws FileNotFoundException {
		String path =
				apiConfig.getProperty(RESULTS_DIR) +
				File.separatorChar +
				screenshot.getTestCaseResult().getExecuteTime() +
				File.separatorChar +
				screenshot.getCapability().getTestClass() +
				File.separatorChar +
				screenshot.getFileName() + ".png";

		File file = new File(path);
		if (!file.exists() || !file.isFile()) { throw new FileNotFoundException(path + " Not Found."); }
		return file;
	}

	private void sendFile(File file, HttpServletResponse response) throws IOException {
		response.setContentType("image/png");
		response.flushBuffer();
		IOUtils.copy(new FileInputStream(file), response.getOutputStream());
	}

	private void sendImage(BufferedImage image, HttpServletResponse response) throws IOException {
		response.setContentType("image/png");
		response.flushBuffer();
		ImageIO.write(image, "png", response.getOutputStream());
	}
}
