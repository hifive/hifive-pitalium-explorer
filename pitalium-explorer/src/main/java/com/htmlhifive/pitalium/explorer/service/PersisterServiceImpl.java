/*
 * Copyright (C) 2015-2017 NS Solutions Corporation
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.htmlhifive.pitalium.explorer.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.htmlhifive.pitalium.core.io.PersistMetadata;
import com.htmlhifive.pitalium.core.io.ResourceUnavailableException;
import com.htmlhifive.pitalium.core.model.TargetResult;
import com.htmlhifive.pitalium.core.model.TestResult;
import com.htmlhifive.pitalium.explorer.changelog.ChangeRecord;
import com.htmlhifive.pitalium.explorer.entity.AreaRepository;
import com.htmlhifive.pitalium.explorer.entity.ConfigRepository;
import com.htmlhifive.pitalium.explorer.entity.ProcessedImageRepository;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.ScreenshotRepository;
import com.htmlhifive.pitalium.explorer.entity.Target;
import com.htmlhifive.pitalium.explorer.entity.TargetRepository;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionAndEnvironment;
import com.htmlhifive.pitalium.explorer.entity.TestExecutionRepository;
import com.htmlhifive.pitalium.explorer.io.ExplorerDBPersister;
import com.htmlhifive.pitalium.explorer.io.ExplorerPersister;
import com.htmlhifive.pitalium.explorer.request.ExecResultChangeRequest;
import com.htmlhifive.pitalium.explorer.request.ScreenshotResultChangeRequest;
import com.htmlhifive.pitalium.explorer.request.TargetResultChangeRequest;
import com.htmlhifive.pitalium.explorer.response.ResultListOfExpected;
import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;
import com.htmlhifive.pitalium.image.model.ComparedRectangleArea;

@Service("persisterService")
public class PersisterServiceImpl implements PersisterService {

	@Autowired
	private TestExecutionRepository testExecutionRepository;
	@Autowired
	private ScreenshotRepository screenshotRepository;
	@Autowired
	private TargetRepository targetRepository;
	@Autowired
	private AreaRepository areaRepository;
	@Autowired
	private ConfigRepository configRepository;
	@Autowired
	private ProcessedImageRepository processedImageRepository;

	@Autowired
	private ScreenshotIdService screenshotIdService;
	@Autowired
	private TemporaryFileService temporaryFileService;

	@Resource(name = "configExplorerPersister")
	private ExplorerPersister persister;

	@PostConstruct
	public void initialize() {
		persister.setScreenshotIdService(screenshotIdService);

		if (persister instanceof ExplorerDBPersister) {
			((ExplorerDBPersister) persister).setTestExecutionRepository(testExecutionRepository);
			((ExplorerDBPersister) persister).setScreenshotRepository(screenshotRepository);
			((ExplorerDBPersister) persister).setTargetRepository(targetRepository);
			((ExplorerDBPersister) persister).setAreaRepository(areaRepository);
			((ExplorerDBPersister) persister).setProcessedImageRepository(processedImageRepository);
			((ExplorerDBPersister) persister).setConfigRepository(configRepository);
		}
	}

	@Override
	public ExplorerPersister getPersister() {
		return persister;
	}

	@Override
	public void setScreenshotIdService(ScreenshotIdService screenshotIdService) {
		persister.setScreenshotIdService(screenshotIdService);
	}

	@Override
	public List<ResultListOfExpected> findScreenshotFiles(String path){
		return persister.findScreenshotFiles(path);
	}
	@Override
	public ResultListOfExpected executeComparing(String expectedFilePath, String[] targetFilePaths) {
		return persister.executeComparing(expectedFilePath, targetFilePaths);
	}
	@Override
	public Map<String, byte[]> getImages(String expectedFilePath, String targetFilePath) {
		return persister.getImages(expectedFilePath, targetFilePath);
	}
	@Override
	public List<ComparedRectangleArea> getComparedResult(String path, int resultListId, int targetResultId){
		return persister.getComparedResult(path, resultListId, targetResultId);
	};
	@Override
	public String deleteResults(String path, int resultListId){
		return persister.deleteResults(path, resultListId);
	}

	@Override
	public Page<TestExecutionResult> findTestExecution(String searchTestMethod, String searchTestScreen, int page,
			int pageSize, String resultDirectoryKey) {
		return persister.findTestExecution(searchTestMethod, searchTestScreen, page, pageSize, resultDirectoryKey);
	}

	@Override
	public List<Screenshot> findScreenshot(Integer testExecutionId, String searchTestMethod, String searchTestScreen) {
		return persister.findScreenshot(testExecutionId, searchTestMethod, searchTestScreen);
	}

	@Override
	public Screenshot getScreenshot(Integer screenshotId) {
		ScreenshotIdService.ScreenshotType type;
		try {
			type = screenshotIdService.getScreenshotType(screenshotId);
		} catch (IllegalArgumentException e) {
			return null;
		}

		if (type == ScreenshotIdService.ScreenshotType.TEMPORARY_FILE) {
			return temporaryFileService.getScreenshot(screenshotId);
		} else {
			return persister.getScreenshot(screenshotId);
		}
	}

	@Override
	public Target getTarget(Integer screenshotId, Integer targetId) {
		ScreenshotIdService.ScreenshotType type;
		try {
			type = screenshotIdService.getScreenshotType(screenshotId);
		} catch (IllegalArgumentException e) {
			return null;
		}

		if (type == ScreenshotIdService.ScreenshotType.TEMPORARY_FILE) {
			return temporaryFileService.getTarget(screenshotId, targetId);
		} else {
			return persister.getTarget(screenshotId, targetId);
		}
	}

	@Override
	public File getImage(Integer screenshotId, Integer targetId) throws IOException {
		ScreenshotIdService.ScreenshotType type;
		try {
			type = screenshotIdService.getScreenshotType(screenshotId);
		} catch (IllegalArgumentException e) {
			return null;
		}

		if (type == ScreenshotIdService.ScreenshotType.TEMPORARY_FILE) {
			return temporaryFileService.getImage(screenshotId, targetId);
		} else {
			return persister.getImage(screenshotId, targetId);
		}
	}

	@Override
	public Page<Screenshot> findScreenshot(Integer testExecutionId, Integer testEnvironmentId, int page, int pageSize) {
		return persister.findScreenshot(testExecutionId, testEnvironmentId, page, pageSize);
	}

	@Override
	public Page<TestExecutionAndEnvironment> findTestExecutionAndEnvironment(int page, int pageSize) {
		return persister.findTestExecutionAndEnvironment(page, pageSize);
	}

	@Override
	public File searchProcessedImageFile(Integer screenshotId, String algorithm) {
		return persister.searchProcessedImageFile(screenshotId, algorithm);
	}

	@Override
	public List<Screenshot> findNotProcessedEdge() {
		return persister.findNotProcessedEdge();
	}

	@Override
	public boolean exsitsProcessedImage(Integer screenshotId, String algorithm) {
		return persister.exsitsProcessedImage(screenshotId, algorithm);
	}

	@Override
	public String getEdgeFileName(Integer screenshotId, String algorithm) {
		return persister.getEdgeFileName(screenshotId, algorithm);
	}

	@Override
	public void saveProcessedImage(Integer screenshotId, String algorithm, String edgeFileName) {
		persister.saveProcessedImage(screenshotId, algorithm, edgeFileName);
	}

	@Override
	public void saveDiffImage(PersistMetadata metadata, BufferedImage image) {
		persister.saveDiffImage(metadata, image);
	}

	@Override
	public BufferedImage loadDiffImage(PersistMetadata metadata) throws ResourceUnavailableException {
		return persister.loadDiffImage(metadata);
	}

	@Override
	public void saveScreenshot(PersistMetadata metadata, BufferedImage image) {
		persister.saveScreenshot(metadata, image);
	}

	@Override
	public InputStream getImageStream(PersistMetadata metadata) throws ResourceUnavailableException {
		return persister.getImageStream(metadata);
	}

	@Override
	public BufferedImage loadScreenshot(PersistMetadata metadata) throws ResourceUnavailableException {
		return persister.loadScreenshot(metadata);
	}

	@Override
	public void saveTargetResults(PersistMetadata metadata, List<TargetResult> results) {
		persister.saveTargetResults(metadata, results);
	}

	@Override
	public List<TargetResult> loadTargetResults(PersistMetadata metadata) throws ResourceUnavailableException {
		return persister.loadTargetResults(metadata);
	}

	@Override
	public void saveTestResult(PersistMetadata metadata, TestResult result) {
		persister.saveTestResult(metadata, result);
	}

	@Override
	public TestResult loadTestResult(PersistMetadata metadata) throws ResourceUnavailableException {
		return persister.loadTestResult(metadata);
	}

	@Override
	public void saveExpectedIds(Map<String, Map<String, String>> expectedIds) {
		persister.saveExpectedIds(expectedIds);
	}

	@Override
	public Map<String, Map<String, String>> loadExpectedIds() throws ResourceUnavailableException {
		return persister.loadExpectedIds();
	}

	@Override
	public List<ChangeRecord> updateExecResult(List<ExecResultChangeRequest> inputModelList) {
		return persister.updateExecResult(inputModelList);
	}

	@Override
	public List<ChangeRecord> updateScreenshotComparisonResult(List<ScreenshotResultChangeRequest> inputModelList) {
		return persister.updateScreenshotComparisonResult(inputModelList);
	}

	@Override
	public List<ChangeRecord> updateTargetComparisonResult(List<TargetResultChangeRequest> inputModelList) {
		return persister.updateTargetComparisonResult(inputModelList);
	}
}
