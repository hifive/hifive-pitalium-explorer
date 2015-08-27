/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.service;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.htmlhifive.pitalium.core.result.TestResultManager;
import com.htmlhifive.pitalium.explorer.conf.ApplicationConfig;
import com.htmlhifive.pitalium.explorer.entity.Area;
import com.htmlhifive.pitalium.explorer.entity.AreaRepository;
import com.htmlhifive.pitalium.explorer.entity.ConfigRepository;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImageRepository;
import com.htmlhifive.pitalium.explorer.entity.Repositories;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.ScreenshotRepository;
import com.htmlhifive.pitalium.explorer.entity.Target;
import com.htmlhifive.pitalium.explorer.entity.TargetRepository;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionAndEnvironment;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionRepository;
import com.htmlhifive.pitalium.explorer.image.EdgeDetector;
import com.htmlhifive.pitalium.explorer.io.ExplorerDBPersister;
import com.htmlhifive.pitalium.explorer.io.ExplorerPersister;
import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;
import com.htmlhifive.pitalium.image.model.DiffPoints;
import com.htmlhifive.pitalium.image.util.ImageUtils;

@Service
public class ExplorerService implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7097257097122078409L;

	private static Logger log = LoggerFactory.getLogger(ExplorerService.class);

	@Autowired
	private ApplicationConfig config;
	@Autowired
	private TestExecutionRepository testExecutionRepo;
	@Autowired
	private ScreenshotRepository screenshotRepo;
	@Autowired
	private TargetRepository targetRepo;
	@Autowired
	private AreaRepository areaRepo;
	@Autowired
	private ConfigRepository configRepo;
	@Autowired
	private ProcessedImageRepository processedImageRepo;

	private ExplorerPersister persister;

	public void init() {
		TestResultManager manager = TestResultManager.getInstance();
		persister = (ExplorerPersister) manager.getPersister();

		if (persister instanceof ExplorerDBPersister) {
			((ExplorerDBPersister) persister).setTestExecutionRepository(testExecutionRepo);
			((ExplorerDBPersister) persister).setScreenshotRepository(screenshotRepo);
			((ExplorerDBPersister) persister).setTargetRepository(targetRepo);
			((ExplorerDBPersister) persister).setAreaRepository(areaRepo);
			((ExplorerDBPersister) persister).setProcessedImageRepository(processedImageRepo);
			((ExplorerDBPersister) persister).setConfigRepository(configRepo);
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

	public Page<TestExecutionResult> findTestExecution(String searchTestMethod, String searchTestScreen, int page,
			int pageSize) {
		return persister.findTestExecution(searchTestMethod, searchTestScreen, page, pageSize);
	}

	public List<Screenshot> findScreenshot(Integer testExecutionId, String searchTestMethod, String searchTestScreen) {
		return persister.findScreenshot(testExecutionId, searchTestMethod, searchTestScreen);
	}

	public Screenshot getScreenshot(Integer screenshotId) {
		return persister.getScreenshot(screenshotId);
	}

	public void getImage(Integer screenshotId, Integer targetId, HttpServletResponse response) {
		File file;
		try {
			file = persister.getImage(screenshotId, targetId);
			if (file == null) {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return;
			}

			Target target = persister.getTarget(screenshotId, targetId);
			if (target.getExcludeAreas().isEmpty()) {
				sendFile(file, response);
			} else {
				BufferedImage image = ImageIO.read(file);
				List<Rectangle> excludeRectangleList = createExcludRectangleList(target);
				// permeabilize
				fillRect(image, excludeRectangleList, new Color(0, 0, 0, 150));
				sendImage(image, response);
			}
		} catch (IOException e1) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	public void getEdgeImage(Integer screenshotId, Integer targetId, int colorIndex,
			HttpServletResponse response) {
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
			File imageFile = persister.getImage(screenshotId, targetId);

			if (!imageFile.exists()) {
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

	public void getProcessed(Integer screenshotId, Integer targetId, String algorithm, int colorIndex,
			HttpServletResponse response) {
		switch (algorithm) {
			case "edge":
				getEdgeImage(screenshotId, targetId, colorIndex, response);
				break;
			default:
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				break;
		}
	}

	public Boolean getComparisonResult(Integer sourceScreenshotId, Integer targetScreenshotId, Integer targetId) {
		try {
			File sourceFile = persister.getImage(sourceScreenshotId, targetId);
			File targetFile = persister.getImage(targetScreenshotId, targetId);
			Target target = persister.getTarget(sourceScreenshotId, targetId);

			if (!sourceFile.exists()) {
				log.error("File Not Found. screenshotId = {}.", sourceScreenshotId);
				return null;
			}

			if (!targetFile.exists()) {
				log.error("File Not Found. screenshotId = {}.", targetScreenshotId);
				return null;
			}

			DiffPoints diffPoints = compare(sourceFile, targetFile, target);
			if (diffPoints == null) {
				log.error("DiffPoints Not Get.");
				return null;
			}
			return diffPoints.isSucceeded();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public void getDiffImage(Integer sourceScreenshotId, Integer targetScreenshotId, Integer targetId,
			HttpServletResponse response) {
		try {
			File sourceFile = persister.getImage(sourceScreenshotId, targetId);
			File targetFile = persister.getImage(targetScreenshotId, targetId);
			Target target = persister.getTarget(sourceScreenshotId, targetId);

			if (!sourceFile.exists()|| !targetFile.exists()) {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return;
			}

			DiffPoints diffPoints = compare(sourceFile, targetFile, target);
			if (diffPoints == null) {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return;
			}
			sendMarkedImage(sourceFile, target, diffPoints, response);

		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	private List<Rectangle> createExcludRectangleList(Target target) {
		Area area = target.getArea();
		List<Rectangle> excludeList = new ArrayList<>();
		for (Area excludeArea : target.getExcludeAreas()) {
			// Get the relative coordinates from the starting position of the actualArea.
			// Based on the obtained relative coordinates, to create a rectangle.
			Rectangle rectangle = new Rectangle((int) excludeArea.getX() - (int) area.getX(), (int) excludeArea.getY()
					- (int) area.getY(), (int) excludeArea.getWidth(), (int) excludeArea.getHeight());
			excludeList.add(rectangle);
		}
		return excludeList;
	}

	private void fillRect(BufferedImage image, List<Rectangle> rectangleList, Color color) {
		Graphics graphics = image.getGraphics();
		graphics.setColor(color);

		// Fills the specified rectangle.
		for (Rectangle rect : rectangleList) {
			Point loc = rect.getLocation();
			Dimension size = rect.getSize();
			graphics.fillRect(loc.x, loc.y, size.width, size.height);
		}
		graphics.dispose();
	}

	private DiffPoints compare(File sourceFile, File targetFile, Target target) throws IOException {
		// Create a partial image
		BufferedImage actual = ImageIO.read(sourceFile);
		BufferedImage expected = ImageIO.read(targetFile);

		if (target != null && !target.getExcludeAreas().isEmpty()) {
			List<Rectangle> excludeRectangleList = createExcludRectangleList(target);
			fillRect(actual, excludeRectangleList, Color.BLACK);
			fillRect(expected, excludeRectangleList, Color.BLACK);
		}

		// Compare.
		return ImageUtils.compare(actual, null, expected, null, null);
	}
	
	
	private BufferedImage getMarkedImage(File image, DiffPoints diffPoints) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(image);
		return getMarkedImage(bufferedImage, diffPoints);
	}

	private BufferedImage getMarkedImage(BufferedImage image, DiffPoints diffPoints) throws IOException {
		return ImageUtils.getMarkedImage(image, diffPoints);
	}

	private void sendMarkedImage(File file, Target target, DiffPoints diffPoints, HttpServletResponse response) throws IOException {
		if (target != null && !target.getExcludeAreas().isEmpty()) {
			BufferedImage image = ImageIO.read(file);
			List<Rectangle> excludeRectangleList = createExcludRectangleList(target);
			// permeabilize
			fillRect(image, excludeRectangleList, new Color(0, 0, 0, 150));

			if (!diffPoints.isFailed()) {
				sendImage(image, response);
			} else {
				BufferedImage marked = getMarkedImage(image, diffPoints);
				sendImage(marked, response);
			}
		} else {
			if (!diffPoints.isFailed()) {
				sendFile(file, response);
			} else {
				BufferedImage marked = getMarkedImage(file, diffPoints);
				sendImage(marked, response);
			}
		}
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
	
	public Page<Screenshot> findScreenshot(Integer testExecutionId, Integer testEnvironmentId, 
			int page, int pageSize) {
		return persister.findScreenshot(testExecutionId, testEnvironmentId, page, pageSize);
	}
	
	public Page<TestExecutionAndEnvironment> findTestExecutionAndEnvironment(int page, int pageSize) {
		return persister.findTestExecutionAndEnvironment(page, pageSize);
	}

	public List<String> multipartUpload(List<MultipartFile> files) {
		List<String> list = new ArrayList<>();
		try {
			long l = System.nanoTime();
			for (int i = 0, size = files.size(); i < size; i++) {
				MultipartFile f = files.get(i);
				String fileName = f.getOriginalFilename();
				String extension = FilenameUtils.getExtension(fileName);
				String uniqueFileName = 
						new StringBuilder().append(l).append("_").append(i).append(".").append(extension).toString();
				FileUtils.copyInputStreamToFile(f.getInputStream(), new File(config.getUploadPath(), uniqueFileName));
				list.add(uniqueFileName);
			}
		} catch (IOException e) {
			log.error("Failed to save the files.", e);
			return new ArrayList<>();
		}

		return list;
	}

	public void getDiffImage(String fileName1, String fileName2, HttpServletResponse response) {
		try {
			File file1 = new File(config.getUploadPath(), fileName1);
			File file2 = new File(config.getUploadPath(), fileName2);
	
			if (!file1.exists()|| !file2.exists()) {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return;
			}

			// Compare.
			DiffPoints diffPoints = compare(file1, file2, null);
			if (diffPoints == null) {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return;
			}

			sendMarkedImage(file1, null, diffPoints, response);

		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
}
