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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * スクリーンショットの実行結果の変更された箇所
 *
 * @author sasaki
 */
public class ScreenshotResultChangePoint {
	/** スクリーンショットID */
	private final String screenshotId;

	/** テストクラス名 */
	private final String testClass;

	/** テストメソッド名 */
	private final String testMethod;

	/** 機能 */
	private final Map<String, ?> capabilities;

	/**
	 * コンストラクタ
	 *
	 * @param screenshotId スクリーンショットID
	 * @param testClass テストクラス名
	 * @param testMethod テストメソッド名
	 * @param capabilities 機能
	 */
	@JsonCreator
	public ScreenshotResultChangePoint(@JsonProperty("screenshotId") String screenshotId,
			@JsonProperty("testClass") String testClass, @JsonProperty("testMethod") String testMethod,
			@JsonProperty("capabilities") Map<String, ?> capabilities) {
		this.screenshotId = screenshotId;
		this.testClass = testClass;
		this.testMethod = testMethod;
		this.capabilities = capabilities;
	}

	/**
	 * スクリーンショットIDを取得する。
	 *
	 * @return スクリーンショットID
	 */
	public String getScreenshotId() {
		return screenshotId;
	}

	/**
	 * テストクラス名を取得する。
	 *
	 * @return テストクラス名
	 */
	public String getTestClass() {
		return testClass;
	}

	/**
	 * テストメソッド名を取得する。
	 *
	 * @return テストメソッド名
	 */
	public String getTestMethod() {
		return testMethod;
	}

	/**
	 * 機能を取得する。
	 *
	 * @return 機能
	 */
	public Map<String, ?> getCapabilities() {
		return capabilities;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
		result = prime * result + ((screenshotId == null) ? 0 : screenshotId.hashCode());
		result = prime * result + ((testClass == null) ? 0 : testClass.hashCode());
		result = prime * result + ((testMethod == null) ? 0 : testMethod.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScreenshotResultChangePoint other = (ScreenshotResultChangePoint) obj;
		if (capabilities == null) {
			if (other.capabilities != null)
				return false;
		} else if (!capabilities.equals(other.capabilities))
			return false;
		if (screenshotId == null) {
			if (other.screenshotId != null)
				return false;
		} else if (!screenshotId.equals(other.screenshotId))
			return false;
		if (testClass == null) {
			if (other.testClass != null)
				return false;
		} else if (!testClass.equals(other.testClass))
			return false;
		if (testMethod == null) {
			if (other.testMethod != null)
				return false;
		} else if (!testMethod.equals(other.testMethod))
			return false;
		return true;
	}


}
