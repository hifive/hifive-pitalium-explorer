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

import java.awt.print.Pageable;
import java.util.List;

public interface ScreenshotRepository extends JpaRepository<Screenshot, Integer> {

	public List<Screenshot> findByTestExecutionIdAndTestMethodContainingAndTestScreenContaining(Integer testExecutionId,
			String testMethod, String testScreen);

	@Query("SELECT s " + "FROM Screenshot AS s " + "WHERE s.id > :from " + "AND 3 > (" + "SELECT COUNT(*) "
			+ "FROM ProcessedImage AS p " + "WHERE s.id = p.screenshotId " + "AND ( " + "p.algorithm = 'edge' "
			+ "OR p.algorithm = 'edge_0' " + "OR p.algorithm = 'edge_1' )) "
			+ "ORDER BY s.id desc" /*
									 * from recent to older
									 */
	)
	public List<Screenshot> findNotProcessedEdge(@Param("from") Integer from);

	public Page<Screenshot> findByTestExecutionIdAndTestEnvironmentId(@Param("testExecutionId") Integer testExecutionId,
			@Param("testEnvironmentId") Integer testEnvironmentId, Pageable page);

	@Query("select distinct new com.htmlhifive.pitalium.explorer.entity.TestExecutionAndEnvironment( "
			+ "exe.id, exe.time, env.id, env.platform, env.platformVersion, env.deviceName, "
			+ "env.browserName, env.browserVersion ) from Screenshot as s, TestExecution as exe, TestEnvironment as env "
			+ "where s.testExecution = exe.id and s.testEnvironment = env.id " + "order by exe.time desc, env.id asc")
	public List<TestExecutionAndEnvironment> findTestExecutionAndEnvironment();

}
