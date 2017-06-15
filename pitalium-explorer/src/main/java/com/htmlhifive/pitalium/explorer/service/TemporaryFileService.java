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
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.htmlhifive.pitalium.explorer.entity.Screenshot;
import com.htmlhifive.pitalium.explorer.entity.Target;

/**
 * ブラウザからアップロードされた一時ファイルを扱うサービス
 */
public interface TemporaryFileService {

	/**
	 * ブラウザからアップロードされたファイルを受け取って一時領域へ保存し、そのスクリーンショット識別IDを返します。
	 *
	 * @param files ブラウザからアップロードされたファイル一覧
	 * @return アップロードされたファイルに対応するスクリーンショット識別ID一覧
	 */
	List<Integer> upload(List<MultipartFile> files);

	Screenshot getScreenshot(Integer screenshotId);

	Target getTarget(Integer screenshotId, Integer targetId);

	File getImage(Integer screenshotId, Integer targetId);

}
