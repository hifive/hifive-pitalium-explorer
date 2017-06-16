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
package com.htmlhifive.pitalium.explorer.changelog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.htmlhifive.pitalium.core.model.IndexDomSelector;

/**
 * 対象領域の実行結果の変更された箇所
 *
 * @author sasaki
 */
public class TargetResultChangePoint {
	/** スクリーンショットの実行結果の変更された箇所 */
	private final ScreenshotResultChangePoint screenshot;

	/** 対象領域となるDOM要素 */
	private final IndexDomSelector target;

	/**
	 * コンストラクタ
	 *
	 * @param screenshot スクリーンショットの実行結果の変更された箇所
	 * @param target 対象領域となるDOM要素
	 */
	@JsonCreator
	public TargetResultChangePoint(@JsonProperty("screenshot") ScreenshotResultChangePoint screenshot,
			@JsonProperty("target") IndexDomSelector target) {
		this.screenshot = screenshot;
		this.target = target;
	}

	/**
	 * スクリーンショットの実行結果の変更された箇所を取得する。
	 *
	 * @return スクリーンショットの実行結果の変更された箇所
	 */
	public ScreenshotResultChangePoint getScreenshot() {
		return screenshot;
	}

	/**
	 * 対象領域となるDOM要素を取得する。
	 *
	 * @return 対象領域となるDOM要素
	 */
	public IndexDomSelector getTarget() {
		return target;
	}
}
