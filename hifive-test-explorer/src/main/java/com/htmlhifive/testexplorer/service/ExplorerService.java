/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.htmlhifive.testexplorer.conf.ApplicationConfig;
import com.htmlhifive.testexplorer.entity.ConfigRepository;
import com.htmlhifive.testexplorer.entity.ProcessedImageRepository;
import com.htmlhifive.testexplorer.entity.Repositories;
import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.ScreenshotRepository;
import com.htmlhifive.testexplorer.entity.TargetRepository;
import com.htmlhifive.testexplorer.entity.TestExecutionRepository;
import com.htmlhifive.testexplorer.file.ImageFileUtility;
import com.htmlhifive.testexplorer.image.EdgeDetector;
import com.htmlhifive.testexplorer.io.ExplorerDBPersister;
import com.htmlhifive.testexplorer.io.ExplorerPersister;
import com.htmlhifive.testexplorer.response.TestExecutionResult;
import com.htmlhifive.testlib.core.result.TestResultManager;
import com.htmlhifive.testlib.image.model.DiffPoints;
import com.htmlhifive.testlib.image.util.ImageUtils;

@Service
public class ExplorerService implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1786687526955533525L;
	@Autowired
	private ApplicationConfig config;
	@Autowired
	private TestExecutionRepository testExecutionRepo;
	@Autowired
	private ScreenshotRepository screenshotRepo;
	@Autowired
	private TargetRepository targetRepo;
	@Autowired
	private ConfigRepository configRepo;
	@Autowired
	private ProcessedImageRepository processedImageRepo;

	private ExplorerPersister persister;
	
	public void init() {
		TestResultManager manager = TestResultManager.getInstance();
		persister = (ExplorerPersister)manager.getPersister();

		if (persister instanceof ExplorerDBPersister) {
			((ExplorerDBPersister)persister).setTestExecutionRepository(testExecutionRepo);
			((ExplorerDBPersister)persister).setScreenshotRepository(screenshotRepo);
			((ExplorerDBPersister)persister).setTargetRepository(targetRepo);
			((ExplorerDBPersister)persister).setProcessedImageRepository(processedImageRepo);
			// FIXME 直したい
			Repositories repositories = new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo);
			((ExplorerDBPersister)persister).setImageFileUtility(new ImageFileUtility(repositories));
		}
	}

	// TODO とりあえず用意。後で消したい。
	public Repositories getRepositories() {
		return new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo);
	}
	// ------------------------------------------------------------

	public ApplicationConfig getApplicationConfig() {
		return config;
	}
	
	public ExplorerPersister getExplorerPersister() {
		return persister;
	}
	
	public Page<TestExecutionResult> findTestExecution(String searchTestMethod, String searchTestScreen, int page, int pageSize) {
		return persister.findTestExecution(searchTestMethod, searchTestScreen, page, pageSize);
	}

	public List<Screenshot> findScreenshot(Integer testExecutionId, String searchTestMethod, String searchTestScreen) {
		return persister.findScreenshot(testExecutionId, searchTestMethod, searchTestScreen);
	}
	
	public Screenshot getScreenshot(Integer screenshotid) {
		return persister.getScreenshot(screenshotid);
	}

	public void getImage(Integer id, HttpServletResponse response) {
		File file;
		try {
			file = persister.getImage(id);
			if (file == null) {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return;
			}
			sendFile(file, response);
		} catch (IOException e1) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	public void getEdgeImage(Integer id, Map<String, String> allparams, HttpServletResponse response) {
		int colorIndex = -1;
		if (allparams.containsKey("colorIndex")) {
			try {
				colorIndex = Integer.parseInt(allparams.get("colorIndex"));
			} catch (NumberFormatException nfe) {
				// ignore
			}
		}

		// FIXME キャッシュ対応後に復活させる
//		File cachedFile = persister.searchProcessedImageFile(id, ProcessedImageUtility.getAlgorithmNameForEdge(colorIndex));
//		if (cachedFile != null) {
//			try {
//				sendFile(cachedFile, response);
//			} catch (IOException e1) {
//				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//			}
//			return;
//		}

		try {
			File imageFile = persister.getImage(id);

			if (imageFile == null) {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return;
			}

			EdgeDetector edgeDetector = new EdgeDetector(0.5);
			
			switch (colorIndex) {
			case 0:
				edgeDetector.setForegroundColor(new Color(255, 0, 0, 255));
				break;
			case 1:
				edgeDetector.setForegroundColor(new Color(0, 0, 255, 255));
				break;
			}

			BufferedImage image = edgeDetector.DetectEdge(ImageIO.read(imageFile));
			if (image == null) {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return;
			}
			sendImage(image, response);
		} catch (IOException e1) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	public void getProcessed(Integer id, String algorithm, Map<String, String> allparams, HttpServletResponse response) {
		switch(algorithm) {
		case "edge":
			getEdgeImage(id, allparams, response);
			break;
		default:
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			break;
		}
	}

	public void getDiffImage(Integer sourceId, Integer targetId, HttpServletResponse response) {
		try {
			DiffPoints diffPoints = compare(sourceId, targetId);
			if (diffPoints == null) {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return;
			}
			File sourceFile = persister.getImage(sourceId);
			if (!diffPoints.isFailed()) {
				sendFile(sourceFile, response);
			} else {
				BufferedImage marked = getMarkedImage(sourceFile, diffPoints);
				sendImage(marked, response);
			}
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	private DiffPoints compare(Integer sourceId, Integer targetId) throws IOException {
		File sourceFile = persister.getImage(sourceId);
		File targetFile = persister.getImage(targetId);
		if (sourceFile == null || targetFile == null) {
			return null;
		}

		// Create a partial image
		BufferedImage actual = ImageIO.read(sourceFile);
		BufferedImage expected = ImageIO.read(targetFile);

		// Compare.
		return ImageUtils.compare(expected, null, actual, null, null);
	}

	private BufferedImage getMarkedImage(File image, DiffPoints diffPoints) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(image);
		return ImageUtils.getMarkedImage(bufferedImage, diffPoints);
	}

	/**
	 * Send a file over http response
	 * 
	 * @param file file to send
	 * @param response response to use
	 * @throws IOException
	 */
	private void sendFile(File file, HttpServletResponse response) throws IOException {
		response.setContentType("image/png");
		response.flushBuffer();
		IOUtils.copy(new FileInputStream(file), response.getOutputStream());
	}

	/**
	 * Send image over response
	 * 
	 * @param image image to send
	 * @param response response to use
	 * @throws IOException
	 */
	private void sendImage(BufferedImage image, HttpServletResponse response) throws IOException {
		response.setContentType("image/png");
		response.flushBuffer();
		ImageIO.write(image, "png", response.getOutputStream());
	}
}
