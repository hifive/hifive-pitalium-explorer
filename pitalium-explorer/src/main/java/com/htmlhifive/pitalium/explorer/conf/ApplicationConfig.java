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

import antlr.StringUtils;

@Configuration()
public class ApplicationConfig {

	@Value("${diffImageCache:}")
	private String diffImageCache;

	@Value("${uploadPath:C:/temp}")
	private String uploadPath;

	/**
	 * Diff画像のキャッシュ機能を有効にするか判定する。
	 *
	 * @return 有効にする場合は、true。
	 */
	public Boolean isDiffImageCacheOn() {
		return StringUtils.equals(diffImageCache, "on");
	}

	/**
	 * アップロードされたファイルの格納先を取得する。
	 *
	 * @return ファイルの格納先
	 */
	public String getUploadPath() {
		return uploadPath;
	}
}
