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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.core.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.htmlhifive.pitalium.explorer.conf.ApplicationConfig;
import com.htmlhifive.pitalium.explorer.entity.Area;
import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.Target;

@Service
public class TemporaryFileServiceImpl implements TemporaryFileService {

	private static final Logger LOG = LoggerFactory.getLogger(TemporaryFileService.class);

	@Autowired
	private ScreenshotIdService screenshotIdService;

	@Autowired
	private ApplicationConfig config;

	private final Map<Integer, String> savedFiles = new HashMap<>();

	@Override
	public synchronized List<Integer> upload(List<MultipartFile> files) {
		List<Integer> list = new ArrayList<>();
		try {
			long l = System.nanoTime();
			for (int i = 0, size = files.size(); i < size; i++) {
				MultipartFile f = files.get(i);
				String fileName = f.getOriginalFilename();
				String extension = FilenameUtils.getExtension(fileName);
				String uniqueFileName = String.valueOf(l) + "_" + i + "." + extension;
				FileUtils.copyInputStreamToFile(f.getInputStream(), new File(config.getUploadPath(), uniqueFileName));

				// ScreenshotId
				int screenshotId = screenshotIdService.nextId(ScreenshotIdService.ScreenshotType.TEMPORARY_FILE);
				savedFiles.put(screenshotId, uniqueFileName);
				list.add(screenshotId);
			}
		} catch (IOException e) {
			LOG.error("Failed to save the files.", e);
			return new ArrayList<>();
		}

		return list;
	}

	@Override
	public synchronized Screenshot getScreenshot(Integer screenshotId) {
		if (!savedFiles.containsKey(screenshotId)) {
			throw new IllegalArgumentException();
		}

		// TODO
		String fileName = savedFiles.get(screenshotId);
		Screenshot screenshot = new Screenshot();
		screenshot.setId(screenshotId);
		screenshot.setScreenshotName("UPLOAD");
		screenshot.setFileName(fileName);

		return screenshot;
	}

	@Override
	public synchronized Target getTarget(Integer screenshotId, Integer targetId) {
		Target target = new Target();
		target.setExcludeAreas(new ArrayList<Area>());
		return target;
	}

	@Override
	public synchronized File getImage(Integer screenshotId, Integer targetId) {
		if (!savedFiles.containsKey(screenshotId)) {
			throw new IllegalArgumentException();
		}

		String fileName = savedFiles.get(screenshotId);
		return new File(config.getUploadPath(), fileName);
	}

}
