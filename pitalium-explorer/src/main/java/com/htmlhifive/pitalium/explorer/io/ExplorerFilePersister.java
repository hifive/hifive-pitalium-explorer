/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.htmlhifive.pitalium.common.util.JSONUtils;
import com.htmlhifive.pitalium.core.config.FilePersisterConfig;
import com.htmlhifive.pitalium.core.io.FilePersister;
import com.htmlhifive.pitalium.core.io.PersistMetadata;
import com.htmlhifive.pitalium.core.model.ExecResult;
import com.htmlhifive.pitalium.core.model.IndexDomSelector;
import com.htmlhifive.pitalium.core.model.ScreenAreaResult;
import com.htmlhifive.pitalium.core.model.ScreenshotResult;
import com.htmlhifive.pitalium.core.model.SelectorType;
import com.htmlhifive.pitalium.core.model.TargetResult;
import com.htmlhifive.pitalium.core.model.TestResult;
import com.htmlhifive.pitalium.core.selenium.PtlCapabilities;
import com.htmlhifive.pitalium.explorer.entity.Area;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.Target;
import com.htmlhifive.pitalium.explorer.entity.TestEnvironment;
import com.htmlhifive.pitalium.explorer.entity.TestExecution;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionAndEnvironment;
import com.htmlhifive.pitalium.explorer.image.ComparedRectangle;
import com.htmlhifive.pitalium.explorer.image.ImagePair;
import com.htmlhifive.pitalium.explorer.image.SimilarityUnit;
import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;
import com.htmlhifive.pitalium.explorer.response.Result;
import com.htmlhifive.pitalium.explorer.response.ResultDirectory;
import com.htmlhifive.pitalium.explorer.response.ResultListOfExpected;
import com.htmlhifive.pitalium.explorer.response.ScreenshotFile;
import com.htmlhifive.pitalium.explorer.service.ScreenshotIdService;
import com.htmlhifive.pitalium.image.model.RectangleArea;

public class ExplorerFilePersister extends FilePersister implements ExplorerPersister {

	private static Logger log = LoggerFactory.getLogger(ExplorerFilePersister.class);

	private ScreenshotIdService screenshotIdService;

	private Map<Integer, Screenshot> screenshotMap;
	private Map<Integer, List<Screenshot>> screenshotListMap;
	private Map<Integer, Target> targetMap;

	public ExplorerFilePersister() {
		// FIXME 独自のConfigに差し替える必要があるかも
		super();
	}

	public ExplorerFilePersister(FilePersisterConfig config) {
		// FIXME 独自のConfigに差し替える必要があるかも
		super(config);
	}

	@Override
	public void setScreenshotIdService(ScreenshotIdService screenshotIdService) {
		this.screenshotIdService = screenshotIdService;
	}

	@Override
	public Page<ResultDirectory> findResultDirectory(String searchMethod, String searchTestScreen, int page, int pageSize, boolean refresh){
		File root = super.getResultDirectoryFile();
		if (!root.exists() || !root.isDirectory()) {
			log.error("Directory(" + root.getAbsolutePath() + ") Not Found.");
			return new PageImpl<>(new ArrayList<ResultDirectory>());
		}

		LinkedList<ResultDirectory> resultDirectoriesList = new LinkedList<ResultDirectory>();

		File resultDirectoryJson = new File(root, "resultDirectory.json");
		if (!refresh){
			if(!resultDirectoryJson.exists()){
				return new PageImpl<>(new ArrayList<ResultDirectory>());
			}else{
				resultDirectoriesList = JSONUtils.readValue(resultDirectoryJson, new TypeReference<LinkedList<ResultDirectory>>(){});
			}
		}else{
			File[] subDirectories = root.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name){
					return new File(dir, name).isDirectory();
				}
			});

			int id = 0;
			for(int i=0; i<subDirectories.length; i++){
				File timeDirectory = subDirectories[i];
				String timestampString = timeDirectory.getName();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
				long timestamp;
				try {
					timestamp = sdf.parse(timestampString.replace("_", "")).getTime();
				} catch (ParseException e) {
					log.error("timestamp parsing error: " + timestampString);
					timestamp = 0;
				}
				
				File[] methodDirectories = timeDirectory.listFiles(new FileFilter(){
					@Override
					public boolean accept(File f){
						return f.isDirectory();
					}
				});
				
				for(int j=0; j<methodDirectories.length; j++){
					File directory = methodDirectories[j];
					String methodName = directory.getName();
					
					File resultListOfExpectedJson = new File(new File(directory, "comparisonResults"), "resultList.json");
					List<ResultListOfExpected> resultListOfExpectedList = null;
					if (resultListOfExpectedJson.exists()){
						resultListOfExpectedList = JSONUtils.readValue(resultListOfExpectedJson, new TypeReference<ArrayList<ResultListOfExpected>>(){});
					}
					int numberOfResults = 0;
					if (resultListOfExpectedList != null){
						numberOfResults = resultListOfExpectedList.size();
					}

					
//					File[] results = new File(directory, "comparisonResults").listFiles(new FilenameFilter(){
//						@Override
//						public boolean accept(File dir, String name) {
//							return name.toLowerCase().endsWith(".json");
//						}
//					});
//					int numberOfResults = 0;
//					if(results != null){
//						numberOfResults = results.length;
//					}
					
					File[] screenshots = directory.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							for(String extension : new String[]{".png", "jpg", ".jpeg"}){
								if(name.toLowerCase().endsWith(extension)) return true;
							}
							return false;
						}
					});
					int numberOfScreenshots = 0;
					if(screenshots != null){
						numberOfScreenshots = screenshots.length;
					}

					HashSet<String> browsers = new HashSet<String>();
					for(File screenshot : screenshots){
						if(screenshot.getName().toLowerCase().contains("chrome")) browsers.add("chrome");
						else if(screenshot.getName().toLowerCase().contains("safari")) browsers.add("safari");
						else if(screenshot.getName().toLowerCase().contains("firefox")) browsers.add("firefox");
						else if(screenshot.getName().toLowerCase().contains("IE")) browsers.add("IE");
						else browsers.add("unknown");
					}
					int numberOfBrowsers = browsers.size();
					
					ResultDirectory resultDirectory = new ResultDirectory(++id, methodName, timestamp, timestampString,
							numberOfResults, numberOfScreenshots, numberOfBrowsers);
					resultDirectoriesList.add(resultDirectory);
				}

//				File[] results = directory.listFiles(new FilenameFilter() {
//					@Override
//					public boolean accept(File dir, String name) {
//						return name.toLowerCase().endsWith(".json");
//					}
//				});
//				int numberOfResults = results.length;
//
//				File[] screenshots = directory.listFiles(new FilenameFilter() {
//					@Override
//					public boolean accept(File dir, String name) {
//						for(String extension : new String[]{".png", "jpg", ".jpeg"}){
//							if(name.toLowerCase().endsWith(extension)) return true;
//						}
//						return false;
//					}
//				});
//				int numberOfScreenshots = screenshots.length;

//				HashSet<String> browsers = new HashSet<String>();
//				for(File screenshot : screenshots){
//					if(screenshot.getName().toLowerCase().contains("chrome")) browsers.add("chrome");
//					else if(screenshot.getName().toLowerCase().contains("safari")) browsers.add("safari");
//					else if(screenshot.getName().toLowerCase().contains("firefox")) browsers.add("firefox");
//					else if(screenshot.getName().toLowerCase().contains("IE")) browsers.add("IE");
//					else browsers.add("unknown");
//				}
//				int numberOfBrowsers = browsers.size();


			}

			if(resultDirectoryJson.exists()) resultDirectoryJson.delete();
			try {
				FileWriter fw = new FileWriter(resultDirectoryJson.getPath());
				fw.write(JSONUtils.toString(resultDirectoriesList));
				fw.close();
			} catch (Exception e) {
				log.error("file write error: can not write " + resultDirectoryJson.getPath());
			}
		}

		int size = resultDirectoriesList.size();
		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			pageSize = (int) Math.min(size, Integer.MAX_VALUE);
		}

		resultDirectoriesList.subList((page-1)*pageSize, Math.min(page*pageSize, size));

		PageRequest pageable = new PageRequest(page - 1, pageSize);
		return new PageImpl<ResultDirectory>(resultDirectoriesList, pageable, size);
	}
	
	@Override
	public Map<String, List> findScreenshotFiles(String path, boolean refresh){
		File root = super.getResultDirectoryFile();
		if (!root.exists() || !root.isDirectory()) {
			log.error("Directory(" + root.getAbsolutePath() + ") Not Found.");
			return new HashMap<String, List>();
		}

		File directory = new File(root, path);
		File[] files = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				for(String extension : new String[]{".png", "jpg", ".jpeg"}){
					if(name.toLowerCase().endsWith(extension)) return true;
				}
				return false;
			}
		});

		List<ScreenshotFile> screenshotFileList = new LinkedList<ScreenshotFile>();

		File screenshotFileListJson= new File(directory, "screenshotFileList.json");
		if (!refresh){
			if(!screenshotFileListJson.exists()){
				return new HashMap<String, List>();
			}else{
				screenshotFileList = JSONUtils.readValue(screenshotFileListJson, new TypeReference<LinkedList<ScreenshotFile>>(){});
			}
		}else{
			for(int i=0; i<files.length; i++){
				File file = files[i];
				String name = file.getName();
				Date date = new Date(file.lastModified());
				long timestamp = date.getTime();

				String platform = "unknown";
				if(name.toLowerCase().contains("window")) platform = "window";
				else if(name.toLowerCase().contains("osx")) platform = "osx";
				else if(name.toLowerCase().contains("linux")) platform = "linux";
				else if(name.toLowerCase().contains("android")) platform = "android";
				else if(name.toLowerCase().contains("ios")) platform = "ios";

				String browser = "unknown";
				if(name.toLowerCase().contains("chrome")) browser = "chrome";
				else if(name.toLowerCase().contains("safari")) browser = "safari";
				else if(name.toLowerCase().contains("firefox")) browser = "firefox";
				else if(name.toLowerCase().contains("IE")) browser = "IE";

				String version = "";
				// this regex has something problem. should be fixed later
				Matcher m = Pattern.compile("v.?(\\d+(\\d|\\.)*+)").matcher(name);
				if(m.find()){
					version = m.group(1);
				}

				double size = ((double) file.length())/(1024*1024);
				size = Double.valueOf(new DecimalFormat("#.##").format(size));

				BufferedImage bimg;
				int width = 0;
				int height = 0;
				try {
					bimg = ImageIO.read(file);
					width = bimg.getWidth();
					height = bimg.getHeight();
				} catch (IOException e) {
					e.printStackTrace();
				}

				ScreenshotFile screenshotFile = new ScreenshotFile(i+1, name, timestamp,
						platform, browser, version,
						size, width, height);
				screenshotFileList.add(screenshotFile);
			}

			if(screenshotFileListJson.exists()) screenshotFileListJson.delete();
			try {
				FileWriter fw = new FileWriter(screenshotFileListJson.getPath());
				fw.write(JSONUtils.toString(screenshotFileList));
				fw.close();
			} catch (Exception e) {
				log.error("file write error: can not write " + screenshotFileListJson.getPath());
			}
		}
		File resultListJson = new File(directory, "comparisonResults/resultList.json");
		List<ResultListOfExpected> resultList;
		if(resultListJson.exists()){
			resultList = JSONUtils.readValue(resultListJson, new TypeReference<LinkedList<ResultListOfExpected>>(){});
		} else{
			resultList = new LinkedList<ResultListOfExpected>();
		}

		Map<String, List> ret = new HashMap<String, List>();
		ret.put("screenshotFileList", screenshotFileList);
		ret.put("resultList", resultList);
		return ret;
	}
	
	@Override
	public ResultListOfExpected executeComparing(String path, String expectedFilename, String[] targetFilenames) {
		File root = super.getResultDirectoryFile();
		if (!root.exists() || !root.isDirectory()) {
			log.error("Directory(" + root.getAbsolutePath() + ") Not Found.");
//			return new ArrayList<Result>();
			return new ResultListOfExpected();
		}

		File directory = new File(root, path);
		if(!directory.exists() || !directory.isDirectory()){
			log.error("Directory(" + directory.getAbsolutePath() + ") Not Found.");
//			return new ArrayList<Result>();
			return new ResultListOfExpected();
		}

		File comparisonResultsDir = new File(directory, "comparisonResults");
		if(!comparisonResultsDir.exists() || !comparisonResultsDir.isDirectory()){
			comparisonResultsDir.mkdir();
		}
				
		File expectedFile = new File(directory, expectedFilename);
		if(!expectedFile.exists()){ 
			log.error("Directory(" + expectedFile.getAbsolutePath() + ") Not Found.");
//			return new ArrayList<Result>();
			return new ResultListOfExpected();
		}
		BufferedImage expectedImage;
		try {
			expectedImage = ImageIO.read(expectedFile);
		} catch (IOException e) {
			log.error("get buffered image error:: " + expectedFile.getAbsolutePath());
//			return new ArrayList<Result>();
			return new ResultListOfExpected();
		}

		File resultListJson = new File(comparisonResultsDir, "resultList.json");
		if(!resultListJson.exists()){
			try {
				resultListJson.createNewFile();
			} catch (IOException e) {
				log.error("Can not create " + resultListJson.getAbsolutePath());
			}
		}
		List<ResultListOfExpected> resultList;
		try {
			resultList = JSONUtils.readValue(resultListJson, new TypeReference<LinkedList<ResultListOfExpected>>(){});
		} catch (Exception e){
			resultList = new LinkedList<ResultListOfExpected>();
		}
		int id = 1;
		if(resultList.size() != 0){
			id = resultList.get(resultList.size()-1).getId()+1;
		}
		
		List<Result> pairResultList= new LinkedList<Result>();
		for(int i=0; i<targetFilenames.length; i++){
			String targetFilename = targetFilenames[i];
			List<ComparedRectangle> comparedRectangles;
			
			String filenamePair = expectedFilename+"__"+targetFilename+ "__" + Integer.toString(id) + ".json";
			File filenamePairJson = new File(comparisonResultsDir, filenamePair);
//			if(filenamePairJson.exists() && !refresh){
//				log.error("alread exists " + filenamePair);
//				continue;
//			}

			File targetFile = new File(directory, targetFilename);
			if(!targetFile.exists()){ 
				log.error("Directory(" + targetFile.getAbsolutePath() + ") Not Found.");
				continue;
			}

			BufferedImage targetImage;
			try {
				targetImage = ImageIO.read(targetFile);
			} catch (IOException e) {
				log.error("get buffered image error:: " + targetFile.getAbsolutePath());
				continue;
			}
			
			ImagePair imagePair = new ImagePair(expectedImage, targetImage);
			comparedRectangles = imagePair.getComparedRectangles();

			double entireSimilarity = imagePair.getEntireSimilarity();
//			Result result = new Result(expectedFilename, targetFilename, entireSimilarity, comparedRectangles.size());
			Result result = new Result(i+1, targetFilename, entireSimilarity, comparedRectangles.size());
			pairResultList.add(result);
			
			try {
				FileWriter fw = new FileWriter(filenamePairJson.getPath());
				fw.write(JSONUtils.toString(comparedRectangles));
				fw.close();
			} catch (Exception e) {
				log.error("file write error: can not write " + filenamePairJson.getPath());
			}
		}
		ResultListOfExpected resultListOfExpected = new ResultListOfExpected(id, expectedFilename, pairResultList, System.currentTimeMillis());
		
		resultList.add(resultListOfExpected);

		try {
			FileWriter fw = new FileWriter(resultListJson.getPath());
			fw.write(JSONUtils.toString(resultList));
			fw.close();
		} catch (Exception e){
			log.error("file write error: can not write " + resultListJson.getPath());
		}

		return resultListOfExpected;
	}

	public Map<String, byte[]> getImages(String path, String expectedFilename, String targetFilename){
		File root = super.getResultDirectoryFile();
		if (!root.exists() || !root.isDirectory()) {
			log.error("Directory(" + root.getAbsolutePath() + ") Not Found.");
			return new HashMap<String, byte[]>();
		}

		File directory = new File(root, path);
		if(!directory.exists() || !directory.isDirectory()){
			log.error("Directory(" + directory.getAbsolutePath() + ") Not Found.");
			return new HashMap<String, byte[]>();
		}

		File comparisonResultsDir = new File(directory, "comparisonResults");
		if(!comparisonResultsDir.exists() || !comparisonResultsDir.isDirectory()){
			log.error("Directory(" + comparisonResultsDir.getAbsolutePath() + ") Not Found.");
			return new HashMap<String, byte[]>();
		}

		File expectedFile = new File(directory, expectedFilename);
		if(!expectedFile.exists()){ 
			log.error("Directory(" + expectedFile.getAbsolutePath() + ") Not Found.");
			return new HashMap<String, byte[]>();
		}

		BufferedImage expectedImage;
		try {
			expectedImage = ImageIO.read(expectedFile);
		} catch (IOException e) {
			log.error("get buffered image error:: " + expectedFile.getAbsolutePath());
			return new HashMap<String, byte[]>();
		}

		File targetFile = new File(directory, targetFilename);
		if(!targetFile.exists()){ 
			log.error("Directory(" + targetFile.getAbsolutePath() + ") Not Found.");
			return new HashMap<String, byte[]>();
		}

		BufferedImage targetImage;
		try {
			targetImage = ImageIO.read(targetFile);
		} catch (IOException e) {
			log.error("get buffered image error:: " + targetFile.getAbsolutePath());
			return new HashMap<String, byte[]>();
		}
		

		ByteArrayOutputStream expectedBao= new ByteArrayOutputStream();
		ByteArrayOutputStream targetBao= new ByteArrayOutputStream();
		try {
			ImageIO.write(expectedImage, "png", expectedBao);
			ImageIO.write(targetImage, "png", targetBao);
		} catch (IOException e) {
			log.error("Change to bytearray Error");
			return new HashMap<String, byte[]>();
		}
		
		Map<String, byte[]> imageMap= new HashMap<String, byte[]>();
		imageMap.put("expectedImage", expectedBao.toByteArray());
		imageMap.put("targetImage", targetBao.toByteArray());
		
		return imageMap;
	}
	
//	public List<ComparedRectangle> getComparedResult(String directoryName, String expectedFilename, String targetFilename){
	public List<ComparedRectangle> getComparedResult(String path, int resultListId, int targetResultId){
		File root = super.getResultDirectoryFile();
		if (!root.exists() || !root.isDirectory()) {
			log.error("Directory(" + root.getAbsolutePath() + ") Not Found.");
			return new ArrayList<ComparedRectangle>();
		}

		File directory = new File(root, path);
		if(!directory.exists() || !directory.isDirectory()){
			log.error("Directory(" + directory.getAbsolutePath() + ") Not Found.");
			return new ArrayList<ComparedRectangle>();
		}

		File comparisonResultsDir = new File(directory, "comparisonResults");
		if(!comparisonResultsDir.exists() || !comparisonResultsDir.isDirectory()){
			log.error("Directory(" + comparisonResultsDir.getAbsolutePath() + ") Not Found.");
			return new ArrayList<ComparedRectangle>();
		}
		
		File resultListJson = new File(comparisonResultsDir, "resultList.json");
		if(!resultListJson.exists()){
			log.error("Directory(" + resultListJson.getAbsolutePath() + ") Not Found.");
			return new ArrayList<ComparedRectangle>();
		}
		
		
		List<ResultListOfExpected> resultList;
		try{
			resultList = JSONUtils.readValue(resultListJson, new TypeReference<List<ResultListOfExpected>>(){});
		}catch(Exception e){
			log.error("json read value error: " + resultListJson.getAbsolutePath());
			return new ArrayList<ComparedRectangle>();
		}
		
		String expectedFilename = "";
		String targetFilename = "";
		for(ResultListOfExpected resultListOfExpected: resultList){
			if(resultListOfExpected.getId() == resultListId){
				expectedFilename = resultListOfExpected.getExpectedFilename();
				for(Result targetResult: resultListOfExpected.getResultList()){
					if(targetResult.getId() == targetResultId){
						targetFilename = targetResult.getTargetFilename();
					}
				}
			}
		}
		
		String filenamePair = expectedFilename + "__" + targetFilename + "__" + Integer.toString(resultListId) + ".json";
		File filenamePairJson = new File(comparisonResultsDir, filenamePair);
		if(!filenamePairJson.exists()){ 
			log.error("Directory(" + filenamePairJson.getAbsolutePath() + ") Not Found.");
			return new ArrayList<ComparedRectangle>();
		}
		List<ComparedRectangle> comparedRectangleList;
		try{
			comparedRectangleList = JSONUtils.readValue(filenamePairJson, new TypeReference<ArrayList<ComparedRectangle>>(){});
		} catch(Exception e){
			log.error("Json to Object error: " + filenamePairJson.getAbsolutePath());
			e.printStackTrace();
			comparedRectangleList = new ArrayList<ComparedRectangle>();
		}
		return comparedRectangleList;
	}


	@Override
	public Page<TestExecutionResult> findTestExecution(String searchTestMethod, String searchTestScreen, int page,
			int pageSize) {
		File root = super.getResultDirectoryFile();
		if (!root.exists() || !root.isDirectory()) {
			log.error("Directory(" + root.getAbsolutePath() + ") Not Found.");
			return new PageImpl<>(new ArrayList<TestExecutionResult>());
		}

		IOFileFilter filter = FileFilterUtils.nameFileFilter(getTestResultFileName());
		Collection<File> collection = FileUtils.listFiles(root, filter, TrueFileFilter.INSTANCE);

		screenshotMap = new HashMap<>();
		screenshotListMap = new HashMap<>();
		targetMap = new HashMap<>();

		// 後続のページング処理用
		List<TestExecution> testExecutionList = new ArrayList<>();
		// Screenshotの関連を貼るための処理用
		Map<String, List<Screenshot>> workScreenshotListMap = new HashMap<>();
		Map<ScreenshotResult, Screenshot> workScreenshotMap = new HashMap<>();

		List<TestEnvironment> workEnvList = new ArrayList<>();

		int executionId = 0;
		int targetId = 0;
		int areaId = 0;
		int environmentId = 0;

		File[] files = collection.toArray(new File[collection.size()]);
		for (int i = 0, len = files.length; i < len; i++) {
			String executionDate = files[i].getParentFile().getParentFile().getName();
			PersistMetadata metadata = new PersistMetadata(executionDate, files[i].getParentFile().getName());
			TestResult testResult = super.loadTestResult(metadata);

			TestExecution testExecution = new TestExecution();
			DateTime dateTime = DateTimeFormat.forPattern("yyyy_MM_dd_HH_mm_ss").parseDateTime(executionDate);
			testExecution.setTime(new Timestamp(dateTime.getMillis()));

			// 重複チェック
			boolean exists = false;
			for (TestExecution exec : testExecutionList) {
				if (StringUtils.equals(exec.getTimeString(), testExecution.getTimeString())) {
					exists = true;
					testExecution = exec;
					break;
				}
			}

			if (!exists) {
				testExecutionList.add(testExecution);
				testExecution.setId(executionId);
				executionId++;
			}

			ExecResult result = testResult.getResult();
			if (result != null) {
				if (result == ExecResult.FAILURE || ExecResult.FAILURE.name().equals(testExecution.getExecResult())) {
					testExecution.setExecResult(ExecResult.FAILURE.name());
				} else {
					testExecution.setExecResult(ExecResult.SUCCESS.name());
				}
			}

			List<Screenshot> screenshotList = new ArrayList<>();
			for (ScreenshotResult screenshotResult : testResult.getScreenshotResults()) {
				int screenshotId = screenshotIdService.nextId(ScreenshotIdService.ScreenshotType.PITALIUM_FILE);
				Screenshot screenshot = createScreenshot(screenshotId, screenshotResult);
				screenshot.setTestExecution(testExecution);

				// Capability
				Map<String, ?> capabilities = screenshotResult.getCapabilities();
				TestEnvironment testEnvironment = createTestEnvironment(capabilities);
				// リストの何番目に一致するデータがあるか探す。
				int index = indexOf(workEnvList, testEnvironment);
				if (index == -1) {
					workEnvList.add(testEnvironment);
					testEnvironment.setId(environmentId);
					environmentId++;
				} else {
					testEnvironment = workEnvList.get(index);
				}
				screenshot.setTestEnvironment(testEnvironment);

				// Target
				List<TargetResult> targetResultList = screenshotResult.getTargetResults();
				List<Target> targetList = new ArrayList<>();
				for (TargetResult targetResult : targetResultList) {
					Target target = createTarget(targetId, screenshotId, targetResult, screenshotResult);
					targetList.add(target);

					// 比較対象の情報
					ScreenAreaResult screenAreaResult = targetResult.getTarget();
					Area area = createArea(areaId, targetId, screenAreaResult, false);
					areaId++;
					target.setArea(area);

					// 比較除外対象の情報
					List<ScreenAreaResult> screenAreaResultList = targetResult.getExcludes();
					List<Area> exculdeAreaList = new ArrayList<>();
					for (ScreenAreaResult excludeScreenAreaResult : screenAreaResultList) {
						Area excludeArea = createArea(areaId, targetId, excludeScreenAreaResult, true);
						exculdeAreaList.add(excludeArea);
						areaId++;
					}
					target.setExcludeAreas(exculdeAreaList);
					targetMap.put(targetId, target);
					targetId++;
				}
				screenshot.setTargets(targetList);

				screenshotMap.put(screenshotId, screenshot);
				screenshotList.add(screenshot);

				workScreenshotMap.put(screenshotResult, screenshot);
			}
			// TestExecution.id をキーとして格納する。
			List<Screenshot> list = screenshotListMap.get(testExecution.getId());
			if (list == null) {
				list = new ArrayList<>();
				screenshotListMap.put(testExecution.getId(), list);
			}
			list.addAll(screenshotList);
			// 実行日時をキーとして格納する。
			workScreenshotListMap.put(testExecution.getTimeString(), list);
		}

		// Screenshot同士の関連付けを行う。
		for (ScreenshotResult result : workScreenshotMap.keySet()) {
			String expectedId = result.getExpectedId();
			if (expectedId == null) {
				continue;
			}
			Screenshot screenshot = workScreenshotMap.get(result);

			// 期待値の実行日時と一致するScreenshotのリストを取得し、関連付けを行う。
			List<Screenshot> screenshotList = workScreenshotListMap.get(expectedId);
			for (Screenshot expectedScreenshot : screenshotList) {
				if (StringUtils.equals(expectedScreenshot.getTestClass(), screenshot.getTestClass())
						&& StringUtils.equals(expectedScreenshot.getFileName(), screenshot.getFileName())) {
					screenshot.setExpectedScreenshotId(expectedScreenshot.getId());
					break;
				}
			}
		}

		// 検索条件に一致するtestExecution.Idを取得する。
		List<Integer> extractExecutionIdList = extractTestExecutionId(searchTestMethod, searchTestScreen);

		// 検索条件に一致したTestExecutionのみに絞り込み、かつ最新から並ぶようにソートする。
		List<TestExecution> tempTestExecutionList = new ArrayList<>();
		for (int i = testExecutionList.size() - 1; i >= 0; i--) {
			TestExecution testExecution = testExecutionList.get(i);
			if (extractExecutionIdList.contains(testExecution.getId())) {
				tempTestExecutionList.add(testExecution);
			}
		}
		testExecutionList = tempTestExecutionList;

		int size = testExecutionList.size();

		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			pageSize = (int) Math.min(size, Integer.MAX_VALUE);
		}

		// 表示ページ番号、ページ表示数に合わせてリストを作成する。
		List<TestExecutionResult> resultList = new ArrayList<TestExecutionResult>();
		for (int i = (page - 1) * pageSize; i < Math.min(page * pageSize, size); i++) {
			TestExecution execution = testExecutionList.get(i);
			List<Screenshot> list = findScreenshot(execution.getId(), searchTestMethod, searchTestScreen);

			int passedCount = 0;
			int totalCount = 0;
			for (Screenshot s : list) {
				totalCount++;
				if (s.getComparisonResult() == null || s.getComparisonResult().booleanValue()) {
					// 成功時
					passedCount++;
				}
			}
			TestExecutionResult testExecutionResult = new TestExecutionResult(execution, Long.valueOf(passedCount),
					Long.valueOf(totalCount));
			resultList.add(testExecutionResult);
		}

		PageRequest pageable = new PageRequest(page - 1, pageSize);
		return new PageImpl<TestExecutionResult>(resultList, pageable, size);
	}

	private Screenshot createScreenshot(Integer screenshotId, ScreenshotResult screenshotResult) {
		Screenshot screenshot = new Screenshot();
		screenshot.setId(screenshotId);
		screenshot.setScreenshotName(screenshotResult.getScreenshotId());
		screenshot.setComparisonResult(
				screenshotResult.getResult() != null ? screenshotResult.getResult().isSuccess() : null);
		screenshot.setTestClass(screenshotResult.getTestClass());
		screenshot.setTestMethod(screenshotResult.getTestMethod());
		screenshot.setTestScreen(screenshotResult.getScreenshotId());

		PersistMetadata screenshotMetadata = new PersistMetadata(screenshotResult.getExpectedId(),
				screenshotResult.getTestClass(), screenshotResult.getTestMethod(), screenshotResult.getScreenshotId(),
				new PtlCapabilities(screenshotResult.getCapabilities()));
		screenshot.setFileName(getScreenshotImageFileName(screenshotMetadata));
		return screenshot;
	}

	private TestEnvironment createTestEnvironment(Map<String, ?> capabilities) {
		TestEnvironment testEnvironment = new TestEnvironment();
		testEnvironment.setBrowserName((String) capabilities.get("browserName"));
		testEnvironment.setBrowserVersion((String) capabilities.get("version"));
		testEnvironment.setDeviceName((String) capabilities.get("deviceName"));
		testEnvironment.setId(null);
		testEnvironment.setLabel(null);
		testEnvironment.setPlatform((String) capabilities.get("platform"));
		testEnvironment.setPlatformVersion((String) capabilities.get("platformVersion"));
		return testEnvironment;
	}

	private int indexOf(List<TestEnvironment> environmentList, TestEnvironment environment) {
		// idの値を除いて一致するデータを探す。
		for (int i = 0, size = environmentList.size(); i < size; i++) {
			TestEnvironment env = environmentList.get(i);
			if (StringUtils.equals(env.getPlatform(), environment.getPlatform())
					&& StringUtils.equals(env.getPlatformVersion(), environment.getPlatformVersion())
					&& StringUtils.equals(env.getDeviceName(), environment.getDeviceName())
					&& StringUtils.equals(env.getBrowserName(), environment.getBrowserName())
					&& StringUtils.equals(env.getBrowserVersion(), environment.getBrowserVersion())) {
				return i;
			}
		}
		return -1;
	}

	private Target createTarget(Integer targetId, Integer screenshotId, TargetResult targetResult,
			ScreenshotResult screenshotResult) {
		Target target = new Target();
		target.setTargetId(targetId);
		target.setScreenshotId(screenshotId);
		target.setComparisonResult(targetResult.getResult() != null ? targetResult.getResult().isSuccess() : null);

		// 比較対象の情報
		ScreenAreaResult screenAreaResult = targetResult.getTarget();

		PersistMetadata targetMetadata = new PersistMetadata(screenshotResult.getExpectedId(),
				screenshotResult.getTestClass(), screenshotResult.getTestMethod(), screenshotResult.getScreenshotId(),
				screenAreaResult.getSelector(), screenAreaResult.getRectangle(),
				new PtlCapabilities(screenshotResult.getCapabilities()));
		target.setFileName(getScreenshotImageFileName(targetMetadata));
		return target;
	}

	private Area createArea(Integer areaId, Integer targetId, ScreenAreaResult screenAreaResult, boolean excluded) {
		Area area = new Area();
		area.setAreaId(areaId);
		area.setTargetId(targetId);
		area.setSelectorType(screenAreaResult.getSelector().getType().name());
		area.setSelectorValue(screenAreaResult.getSelector().getValue());
		area.setSelectorIndex(screenAreaResult.getSelector().getIndex());
		area.setX(screenAreaResult.getRectangle().getX());
		area.setY(screenAreaResult.getRectangle().getY());
		area.setWidth(screenAreaResult.getRectangle().getWidth());
		area.setHeight(screenAreaResult.getRectangle().getHeight());
		area.setExcluded(excluded);
		return area;
	}

	private List<Integer> extractTestExecutionId(String searchTestMethod, String searchTestScreen) {
		boolean existsSearchTestMethod = searchTestMethod != null && !searchTestMethod.trim().isEmpty();
		boolean existsSearchTestScreen = searchTestScreen != null && !searchTestScreen.trim().isEmpty();

		List<Integer> extractExecutionIdList = new ArrayList<>();
		for (Entry<Integer, List<Screenshot>> entry : screenshotListMap.entrySet()) {
			List<Screenshot> screenshotList = entry.getValue();
			for (Screenshot screenshot : screenshotList) {
				if (existsSearchTestScreen) {
					if (!screenshot.getTestScreen().contains(searchTestScreen)) {
						continue;
					}
				}
				if (existsSearchTestMethod) {
					if (!screenshot.getTestMethod().contains(searchTestMethod)) {
						continue;
					}
				}
				extractExecutionIdList.add(entry.getKey());
				break;
			}
		}
		return extractExecutionIdList;
	}

	@Override
	public List<Screenshot> findScreenshot(Integer testExecutionId, String searchTestMethod, String searchTestScreen) {
		if (screenshotListMap == null) {
			return new ArrayList<Screenshot>();
		}

		List<Screenshot> screenshotList = screenshotListMap.get(testExecutionId);

		// 検索条件に一致するScreenshotを抽出する
		boolean existsSearchTestMethod = searchTestMethod != null && !searchTestMethod.trim().isEmpty();
		boolean existsSearchTestScreen = searchTestScreen != null && !searchTestScreen.trim().isEmpty();

		List<Screenshot> extractScreenshotList = new ArrayList<>();
		for (Screenshot screenshot : screenshotList) {
			if (existsSearchTestScreen) {
				if (!screenshot.getTestScreen().contains(searchTestScreen)) {
					continue;
				}
			}
			if (existsSearchTestMethod) {
				if (!screenshot.getTestMethod().contains(searchTestMethod)) {
					continue;
				}
			}
			extractScreenshotList.add(screenshot);
		}
		return extractScreenshotList;
	}

	@Override
	public Screenshot getScreenshot(Integer screenshotId) {
		return screenshotMap != null ? screenshotMap.get(screenshotId) : null;
	}

	@Override
	public Target getTarget(Integer screenshotId, Integer targetId) {
		Screenshot screenshot = getScreenshot(screenshotId);

		if (screenshot == null) {
			return null;
		}

		Target target = null;
		for (Target t : screenshot.getTargets()) {
			if (t.getTargetId().intValue() == targetId.intValue()) {
				target = t;
				break;
			}
		}

		// targetIdはシーケンシャルにふっているため、
		// 引数でわたってきたtargetIdと期待値となる画像のScreenshotクラスから取得したTargetクラスのIDは一致しない。
		// そのために以下の処理を必要とする。
		if (target == null) {
			Area area = targetMap.get(targetId).getArea();
			for (Target t : screenshot.getTargets()) {
				if (StringUtils.equals(t.getArea().getSelectorType(), area.getSelectorType())
						&& StringUtils.equals(t.getArea().getSelectorValue(), area.getSelectorValue())
						&& t.getArea().getSelectorIndex() == area.getSelectorIndex()) {
					target = t;
					break;
				}
			}
		}
		return target;
	}

	@Override
	public File getImage(Integer screenshotId, Integer targetId) throws IOException {
		Screenshot screenshot = getScreenshot(screenshotId);

		if (screenshot == null) {
			return null;
		}

		Target target = getTarget(screenshotId, targetId);
		Area area = target.getArea();
		IndexDomSelector selector = new IndexDomSelector(SelectorType.valueOf(area.getSelectorType()),
				area.getSelectorValue(), area.getSelectorIndex());
		RectangleArea rectangleArea = new RectangleArea(area.getX(), area.getY(), area.getWidth(), area.getHeight());
		Map<String, String> map = new HashMap<>();
		TestEnvironment env = screenshot.getTestEnvironment();
		map.put("browserName", env.getBrowserName());
		map.put("version", env.getBrowserVersion());
		map.put("deviceName", env.getDeviceName());
		map.put("platform", env.getPlatform());
		map.put("platformVersion", env.getPlatformVersion());

		PersistMetadata metadata = new PersistMetadata(screenshot.getTestExecution().getTimeString(),
				screenshot.getTestClass(), screenshot.getTestMethod(), screenshot.getTestScreen(), selector,
				rectangleArea, new PtlCapabilities(map));
		// Send PNG image
		return super.getScreenshotImageFile(metadata);
	}

	@Override
	public Page<Screenshot> findScreenshot(Integer testExecutionId, Integer testEnvironmentId, int page, int pageSize) {
		if (screenshotListMap == null) {
			return new PageImpl<Screenshot>(new ArrayList<Screenshot>());
		}

		List<Screenshot> screenshotList = screenshotListMap.get(testExecutionId);

		List<Screenshot> extractScreenshotList = new ArrayList<>();
		for (Screenshot screenshot : screenshotList) {
			if (screenshot.getTestEnvironment().getId() == testEnvironmentId) {
				extractScreenshotList.add(screenshot);
			}
		}

		int size = extractScreenshotList.size();

		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			pageSize = Integer.MAX_VALUE;
		}

		// 表示ページ番号、ページ表示数に合わせてリストを作成する。
		List<Screenshot> resultList = new ArrayList<>();
		for (int i = (page - 1) * pageSize; i < Math.min(page * pageSize, size); i++) {
			resultList.add(extractScreenshotList.get(i));
		}

		PageRequest pageable = new PageRequest(page - 1, pageSize);
		return new PageImpl<Screenshot>(resultList, pageable, size);
	}

	@Override
	public Page<TestExecutionAndEnvironment> findTestExecutionAndEnvironment(int page, int pageSize) {
		if (screenshotListMap == null) {
			return new PageImpl<TestExecutionAndEnvironment>(new ArrayList<TestExecutionAndEnvironment>());
		}

		List<TestExecutionAndEnvironment> extractList = new ArrayList<>();
		for (Entry<Integer, List<Screenshot>> entry : screenshotListMap.entrySet()) {
			for (Screenshot screenshot : entry.getValue()) {
				TestExecutionAndEnvironment testEE = new TestExecutionAndEnvironment();

				TestExecution testExec = screenshot.getTestExecution();
				testEE.setExecutionId(testExec.getId());
				testEE.setExecutionTime(testExec.getTimeString());

				TestEnvironment testEnv = screenshot.getTestEnvironment();
				testEE.setEnvironmentId(testEnv.getId());
				testEE.setBrowserName(testEnv.getBrowserName());
				testEE.setBrowserVersion(testEnv.getBrowserVersion());
				testEE.setPlatform(testEnv.getPlatform());
				testEE.setPlatformVersion(testEnv.getPlatformVersion());
				testEE.setDeviceName(testEnv.getDeviceName());

				if (!extractList.contains(testEE)) {
					extractList.add(testEE);
				}
			}
		}

		// 実行日時の降順にソート
		List<TestExecutionAndEnvironment> tempTestExecutionAndEnvironmentList = new ArrayList<>();
		for (int i = extractList.size() - 1; i >= 0; i--) {
			TestExecutionAndEnvironment testEE = extractList.get(i);
			tempTestExecutionAndEnvironmentList.add(testEE);
		}
		extractList = tempTestExecutionAndEnvironmentList;

		int size = extractList.size();

		if (pageSize == 0) {
			pageSize = defaultPageSize;
		} else if (pageSize == -1) {
			pageSize = Integer.MAX_VALUE;
		}

		// 表示ページ番号、ページ表示数に合わせてリストを作成する。
		List<TestExecutionAndEnvironment> resultList = new ArrayList<>();
		for (int i = (page - 1) * pageSize; i < Math.min(page * pageSize, size); i++) {
			resultList.add(extractList.get(i));
		}

		PageRequest pageable = new PageRequest(page - 1, pageSize);
		return new PageImpl<TestExecutionAndEnvironment>(resultList, pageable, size);
	}

	@Override
	public File searchProcessedImageFile(Integer screenshotId, String algorithm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Screenshot> findNotProcessedEdge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exsitsProcessedImage(Integer screenshotId, String algorithm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getEdgeFileName(Integer screenshotId, String algorithm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveProcessedImage(Integer screenshotId, String algorithm, String edgeFileName) {
		throw new UnsupportedOperationException();
	}

}
