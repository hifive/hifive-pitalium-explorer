<<<<<<< HEAD
/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Config, String> {

	public static String ABSOLUTE_PATH_KEY = "absolutePath";

}
=======
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
package com.htmlhifive.pitalium.explorer.entity;


public interface ConfigRepository extends JpaRepository<Config, String> {

	public static String ABSOLUTE_PATH_KEY = "absolutePath";

}
>>>>>>> refs/remotes/origin/dev-snu
