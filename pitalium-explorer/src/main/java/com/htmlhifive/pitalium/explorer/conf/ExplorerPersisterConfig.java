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
package com.htmlhifive.pitalium.explorer.conf;

import java.util.Map;

import com.htmlhifive.pitalium.core.config.FilePersisterConfig;
import com.htmlhifive.pitalium.core.config.PersisterConfig;
import com.htmlhifive.pitalium.core.config.PtlConfiguration;

@PtlConfiguration
public class ExplorerPersisterConfig extends PersisterConfig {

	private Map<String, FilePersisterConfig> files;

	private String defaultResultKey;

	public Map<String, FilePersisterConfig> getFiles() {
		return files;
	}

	public void setFiles(Map<String, FilePersisterConfig> files) {
		this.files = files;
	}

	public String getDefaultResultKey() {
		return defaultResultKey;
	}

	public void setDefaultResultKey(String key) {
		this.defaultResultKey = key;
	}

	@Override
	public FilePersisterConfig getFile() {
		if (files == null || files.isEmpty()) {
			return super.getFile();
		}

		if (defaultResultKey != null) {
			return files.get(defaultResultKey);
		}

		return (FilePersisterConfig) files.values().toArray()[0];
	}


}
