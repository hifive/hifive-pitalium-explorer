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

/**
 * スクリーンショットに割り当てるシーケンシャルIDを管理するサービス
 */
public interface ScreenshotIdService {

	/**
	 * スクリーンショット種別
	 */
	enum ScreenshotType {
		/**
		 * Pitaliumが保存したスクリーンショットファイル
		 */
		PITALIUM_FILE, /**
		 * ファイルアップロード等から保存した一時ファイル
		 */
		TEMPORARY_FILE
	}

	/**
	 * 次のスクリーンショット識別IDを採番します。
	 *
	 * @param type スクリーンショット種別
	 * @return スクリーンショット識別ID
	 */
	int nextId(ScreenshotType type);

	/**
	 * スクリーンショット識別IDに対応する種別を取得します。
	 * @param id スクリーンショット識別ID
	 * @return IDに対応するスクリーンショット種別
	 * @throws IllegalArgumentException IDに対応する種別が存在しない場合
	 */
	ScreenshotType getScreenshotType(int id);

	void clearTypes();

}
