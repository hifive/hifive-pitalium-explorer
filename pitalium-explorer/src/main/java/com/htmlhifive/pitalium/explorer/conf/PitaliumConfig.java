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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.htmlhifive.pitalium.core.result.TestResultManager;
import com.htmlhifive.pitalium.explorer.io.ExplorerPersister;

@Configuration
public class PitaliumConfig {

	@Bean(name = "configExplorerPersister")
	public ExplorerPersister explorerPersister() {
		return (ExplorerPersister) TestResultManager.getInstance().getPersister();
	}

}
