package com.htmlhifive.testexplorer.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;

import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.response.TestExecutionResult;
import com.htmlhifive.testlib.core.io.Persister;

public interface ExplorerPersister extends Persister {

	int defaultPageSize = 20;

	Page<TestExecutionResult> findTestExecution(String searchTestMethod, String searchTestScreen, int page, int pageSize);

	List<Screenshot> findScreenshot(Integer testExecutionId, String searchTestMethod, String searchTestScreen);
	
	Screenshot getScreenshot(Integer screenshotid);

	File getImage(Integer id) throws IOException;

	File searchProcessedImageFile(Integer screenshotId, String algorithm);
	
	List<Screenshot> findNotProcessedEdge();
	
	boolean exsitsProcessedImage(Integer screenshotId, String algorithm);
	
	String getEdgeFileName(Integer screenshotId, String algorithm);
	
	void saveProcessedImage(Integer screenshotId, String algorithm, String edgeFileName);
}
