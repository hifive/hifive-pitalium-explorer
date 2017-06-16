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
package com.htmlhifive.pitalium.explorer.request;

/**
 * 変更要求
 *
 * @author sasaki
 */
public abstract class ChangeRequest {

	/** 結果 */
	private Integer result;

	/** コメント */
	private String comment;

	/**
	 * 結果を取得する。
	 *
	 * @return 結果
	 */
	public Integer getResult() {
		return result;
	}

	/**
	 * 結果を設定する。
	 *
	 * @param result 結果
	 */
	public void setResult(Integer result) {
		this.result = result;
	}

	/**
	 * コメントを取得する。
	 *
	 * @return コメント
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * コメントを設定する。
	 *
	 * @param comment コメント
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

}
