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
package com.htmlhifive.pitalium.explorer.response;

public class ResultDirectory {
	private Integer id;

	private String name;

	private long timestamp;

	private String dirTimestamp;

	private Integer numberOfResults;

	private Integer numberOfScreenshots;

	private Integer numberOfBrowsers;

	public ResultDirectory(){
		this(0, "", 0, "", 0, 0, 0);
	}

	public ResultDirectory(Integer id, String name, long timestamp, String dirTimestamp, Integer numberOfResults, Integer numberOfScreenshots, Integer numberOfBrowsers) {
		this.id = id;
		this.name = name;
		this.timestamp = timestamp;
		this.dirTimestamp = dirTimestamp;
		this.numberOfResults = numberOfResults;
		this.numberOfScreenshots = numberOfScreenshots;
		this.numberOfBrowsers = numberOfBrowsers;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getDirTimestamp() {
		return dirTimestamp;
	}

	public void setDirTimestamp(String dirTimestamp){
		this.dirTimestamp = dirTimestamp;
	}

	public Integer getNumberOfResults() {
		return numberOfResults;
	}

	public void setNumberOfResults(Integer numberOfResults) {
		this.numberOfResults = numberOfResults;
	}

	public Integer getNumberOfScreenshots() {
		return numberOfScreenshots;
	}

	public void setNumberOfScreenshots(Integer numberOfScreenshots) {
		this.numberOfScreenshots = numberOfScreenshots;
	}

	public Integer getNumberOfBrowsers() {
		return numberOfBrowsers;
	}

	public void setNumberOfBrowsers(Integer numberOfBrowsers) {
		this.numberOfBrowsers = numberOfBrowsers;
	}
}
