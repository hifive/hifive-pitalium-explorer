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

import java.util.ArrayList;
import java.util.List;

@Service
public class ScreenshotIdServiceImpl implements ScreenshotIdService {

	// FIXME 採番方法がかなり適当な実装
	List<ScreenshotType> types = new ArrayList<>();

	@Override
	public synchronized int nextId(ScreenshotType type) {
		int id = types.size();
		types.add(type);
		return id;
	}

	@Override
	public synchronized ScreenshotType getScreenshotType(int id) {
		if (id < 0 || types.size() <= id)
			throw new IllegalArgumentException();

		return types.get(id);
	}

	@Override
	public void clearTypes() {
		types.clear();
	}

}
