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

public class ScreenshotFile {
	private Integer id;

	private String name;

	private long timestamp;
	private String platform;
	private String browser;
	private String version;

	private Double size;
	private Integer width;
	private Integer height;

	public ScreenshotFile(){
		this(0,"", 0,"","","",0d,0,0);
	}

	public ScreenshotFile(Integer id, String name,
						  long timestamp, String platform, String browser, String version,
						  Double size, Integer width, Integer height){
		this.id = id;
		this.name = name;
		this.timestamp = timestamp;
		this.platform = platform;
		this.browser = browser;
		this.version = version;
		this.size = size;
		this.width = width;
		this.height = height;
	}

	public Integer getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}

	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}

	public long getTimestamp(){
		return timestamp;
	}
	public void setTimestamp(long timestamp){
		this.timestamp = timestamp;
	}

	public String getPlatform(){
		return platform;
	}
	public void setPlatform(String platform){
		this.platform = platform;
	}

	public String getBrowser(){
		return browser;
	}
	public void setBrowser(String browser){
		this.browser = browser;
	}

	public String getVersion(){
		return version;
	}
	public void setVersion(String version){
		this.version = version;
	}

	public Double getSize(){
		return size;
	}
	public void setSize(double size){
		this.size = size;
	}

	public Integer getWidth(){
		return width;
	}
	public void setWidth(int width){
		this.width = width;
	}

	public Integer getHeight(){
		return height;
	}
	public void setHeight(int height){
		this.height = height;
	}
}
