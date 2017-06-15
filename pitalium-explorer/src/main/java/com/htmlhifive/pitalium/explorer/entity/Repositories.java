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
package com.htmlhifive.pitalium.explorer.entity;

/**
 * immutable structure of all repositories
 */
public class Repositories {
	private final ConfigRepository configRepository;
	private final ProcessedImageRepository processedImageRepository;
	private final ScreenshotRepository screenshotRepository;
	private final TestExecutionRepository testExecutionRepository;

	/**
	 * Constructor. Must provide all repositories to create.
	 *
	 * @param configRepository configRepository
	 * @param processedImageRepository processedImageRepository
	 * @param screenshotRepository screenshotRepository
	 * @param testExecutionRepository testExecutionRepository
	 */
	public Repositories(ConfigRepository configRepository, ProcessedImageRepository processedImageRepository,
			ScreenshotRepository screenshotRepository, TestExecutionRepository testExecutionRepository) {
		this.configRepository = configRepository;
		this.processedImageRepository = processedImageRepository;
		this.screenshotRepository = screenshotRepository;
		this.testExecutionRepository = testExecutionRepository;
	}

	/**
	 * @return the configRepository
	 */
	public ConfigRepository getConfigRepository() {
		return configRepository;
	}

	/**
	 * @return the processedImageRepository
	 */
	public ProcessedImageRepository getProcessedImageRepository() {
		return processedImageRepository;
	}

	/**
	 * @return the screenshotRepository
	 */
	public ScreenshotRepository getScreenshotRepository() {
		return screenshotRepository;
	}

	/**
	 * @return the testExecutionRepository
	 */
	public TestExecutionRepository getTestExecutionRepository() {
		return testExecutionRepository;
	}
}
