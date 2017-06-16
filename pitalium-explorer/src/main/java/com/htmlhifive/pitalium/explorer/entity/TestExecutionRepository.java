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

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.htmlhifive.pitalium.explorer.response.TestExecutionResult;

public interface TestExecutionRepository extends JpaRepository<TestExecution, Integer> {

	@Query("SELECT NEW com.htmlhifive.pitalium.explorer.response.TestExecutionResult ( " + "te, "
			+ "SUM(CASE WHEN s.comparisonResult = TRUE THEN 1 ELSE 0 END), " + "COUNT(s.id) "
			+ ") FROM TestExecution AS te, Screenshot AS s " + "WHERE te.id = s.testExecution "
			+ "AND s.testMethod LIKE %:testMethod% " + "AND s.testScreen LIKE %:testScreen% " + "GROUP BY te.id ")
	public Page<TestExecutionResult> search(@Param("testMethod") String testMethod,
			@Param("testScreen") String testScreen, Pageable page);

}