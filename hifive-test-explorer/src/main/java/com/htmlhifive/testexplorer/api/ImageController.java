/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.api;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.htmlhifive.testexplorer.model.Capability;
import com.htmlhifive.testexplorer.model.ScreenShot;
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
		Map<String, ScreenShot> screenShotMap = (Map<String, ScreenShot>) request.getSession(false).getAttribute(
				KEY_INDEX_MAP);

		// Validate Parameters.
		ScreenShot screenShot = screenShotMap.get(id);
		if (screenShot == null) {
			log.error("id(" + id + ") is invalid parameter.");
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return;
		}

		File pngFile;
		try {
			pngFile = findPngFile(screenShot);
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return;
		}

		// Send png file.
		try {
			writeFileToResponse(pngFile, response);
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
	public void getDiffImage(@RequestParam String sourceId, @RequestParam String targetId,
			HttpServletResponse response) {

		@SuppressWarnings("unchecked")
		Map<String, ScreenShot> screenShotMap = (Map<String, ScreenShot>) request.getSession(false).getAttribute(
				KEY_INDEX_MAP);

		// Validate Parameters.
		ScreenShot sourceScreenShot = screenShotMap.get(sourceId);
		if (sourceScreenShot == null) {
			log.error("id(" + sourceId + ") is invalid parameter.");
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return;
		}
		ScreenShot targetScreenShot = screenShotMap.get(targetId);
		if (targetScreenShot == null) {
			log.error("id(" + targetId + ") is invalid parameter.");
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return;
		}

		File sourcePngFile;
		File targetPngFile;

		try {
			sourcePngFile = findPngFile(sourceScreenShot);
			targetPngFile = findPngFile(targetScreenShot);
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return;
		}

		// Create a partial image.
		BufferedImage actualImage;
		BufferedImage expectedImage;
		try {
			actualImage = createImage(sourcePngFile);
			expectedImage = createImage(targetPngFile);
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return;
		}

		// Compare.
		List<Point> diffPoints = ImageUtility.compareImages(expectedImage, null, actualImage, null, null, null);
		BufferedImage image = !diffPoints.isEmpty() ? ImageUtility.getMarkedImage(actualImage, diffPoints)
				: actualImage;
		// Send png file.
		try {
			writeImageToResponse(image, response);
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	private void writeFileToResponse(File file, HttpServletResponse response) throws IOException {
		BufferedImage image = null;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		writeImageToResponse(image, response);
	}

	private void writeImageToResponse(BufferedImage image, HttpServletResponse response) throws IOException {
		// Send png file.
		response.setContentType("image/png");
		try {
			ImageIO.write(image, "png", response.getOutputStream());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw e;
		} catch (IndexOutOfBoundsException e2) {
			// Even if an exception occurs, no problem in the subsequent processing.
			log.warn(e2.getMessage(), e2);
		}
	}

	private File getBaseDirectory(ScreenShot screenShot) throws NoSuchFileException {
		Capability capability = screenShot.getCapability();
		String directoryName = screenShot.getResultFile().getExecuteTime() + File.separatorChar
				+ capability.getTestClass();
		File base = new File(apiConfig.getProperty(RESULTS_DIR), directoryName);
		if (!base.exists() || !base.isDirectory()) {
			log.error("Directory(" + base.getAbsolutePath() + ") Not Found.");
			throw new NoSuchFileException(base.getAbsolutePath());
		}
		return base;
	}

	private File findPngFile(ScreenShot screenShot) throws IOException {
		return findFile(screenShot, ".png");
	}

	private File findFile(ScreenShot screenShot, String extension) throws IOException {
		File root = getBaseDirectory(screenShot);

		String fileName = screenShot.getFileName() + extension;
		File file = new File(root, fileName);
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException(file.getAbsolutePath() + " Not Found.");
		}
		return file;
	}

	private BufferedImage createImage(File pngFile) throws IOException {
		try {
			return ImageIO.read(pngFile);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
}
