/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.workflow.metrics.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.search.test.util.SearchTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.workflow.metrics.model.Assignment;
import com.liferay.portal.workflow.metrics.model.RoleAssignment;
import com.liferay.portal.workflow.metrics.model.UserAssignment;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.Instance;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.Process;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.Task;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.TaskBulkSelection;
import com.liferay.portal.workflow.metrics.rest.client.pagination.Page;
import com.liferay.portal.workflow.metrics.rest.client.pagination.Pagination;
import com.liferay.portal.workflow.metrics.rest.resource.v1_0.test.helper.WorkflowMetricsRESTTestHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Rafael Praxedes
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class TaskResourceTest extends BaseTaskResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_process = _workflowMetricsRESTTestHelper.addProcess(
			testGroup.getCompanyId());

		_instance = _workflowMetricsRESTTestHelper.addInstance(
			testGroup.getCompanyId(), false, _process.getId());
	}

	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();

		if (_instance != null) {
			_workflowMetricsRESTTestHelper.deleteInstance(
				testGroup.getCompanyId(), _instance);
		}

		if (_process != null) {
			_workflowMetricsRESTTestHelper.deleteProcess(
				testGroup.getCompanyId(), _process);
		}
	}

	@Override
	@Test
	public void testPostTasksPage() throws Exception {
		Page<Task> page = taskResource.postTasksPage(
			Pagination.of(1, 10),
			new TaskBulkSelection() {
				{
					processId = _process.getId();
				}
			});

		Assert.assertEquals(0, page.getTotalCount());

		Task task1 = testGetProcessTask_addTask();

		Task task2 = testGetProcessTask_addTask();

		User user = TestPropsValues.getUser();

		List<Assignment> assignments = new ArrayList<>();

		for (long roleId : user.getRoleIds()) {
			assignments.add(
				new RoleAssignment(roleId, Collections.emptyList()));
		}

		Task task3 = _workflowMetricsRESTTestHelper.addTask(
			assignments, testGroup.getCompanyId(), _instance);

		page = taskResource.postTasksPage(
			Pagination.of(1, 10),
			new TaskBulkSelection() {
				{
					assigneeIds = new Long[] {TestPropsValues.getUserId()};
					processId = _process.getId();
				}
			});

		Assert.assertEquals(2, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task1, task2), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(1, 10),
			new TaskBulkSelection() {
				{
					assigneeIds = new Long[] {-1L};
					processId = _process.getId();
				}
			});

		Assert.assertEquals(1, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task3), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(1, 10),
			new TaskBulkSelection() {
				{
					instanceIds = new Long[] {_instance.getId()};
				}
			});

		Assert.assertEquals(3, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task1, task2, task3), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(1, 10),
			new TaskBulkSelection() {
				{
					processId = _process.getId();
				}
			});

		Assert.assertEquals(3, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task1, task2, task3), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(1, 10),
			new TaskBulkSelection() {
				{
					taskNames = new String[] {task1.getName()};
				}
			});

		Assert.assertEquals(1, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task1), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(1, 10),
			new TaskBulkSelection() {
				{
					taskNames = new String[] {task2.getName()};
				}
			});

		Assert.assertEquals(1, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task2), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(1, 10),
			new TaskBulkSelection() {
				{
					processId = _process.getId();
					taskNames = new String[] {task2.getName()};
				}
			});

		Assert.assertEquals(1, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task2), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(0, 0),
			new TaskBulkSelection() {
				{
					assigneeIds = new Long[] {TestPropsValues.getUserId()};
					processId = _process.getId();
				}
			});

		Assert.assertEquals(2, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task1, task2), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(0, 0),
			new TaskBulkSelection() {
				{
					assigneeIds = new Long[] {-1L};
					processId = _process.getId();
				}
			});

		Assert.assertEquals(1, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task3), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(0, 0),
			new TaskBulkSelection() {
				{
					instanceIds = new Long[] {_instance.getId()};
				}
			});

		Assert.assertEquals(3, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task1, task2, task3), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(0, 0),
			new TaskBulkSelection() {
				{
					processId = _process.getId();
				}
			});

		Assert.assertEquals(3, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task1, task2, task3), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(0, 0),
			new TaskBulkSelection() {
				{
					taskNames = new String[] {task1.getName()};
				}
			});

		Assert.assertEquals(1, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task1), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(0, 0),
			new TaskBulkSelection() {
				{
					taskNames = new String[] {task2.getName()};
				}
			});

		Assert.assertEquals(1, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task2), (List<Task>)page.getItems());

		page = taskResource.postTasksPage(
			Pagination.of(0, 0),
			new TaskBulkSelection() {
				{
					processId = _process.getId();
					taskNames = new String[] {task2.getName()};
				}
			});

		Assert.assertEquals(1, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(task2), (List<Task>)page.getItems());
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"name"};
	}

	@Override
	protected Task randomTask() throws Exception {
		Task task = super.randomTask();

		task.setInstanceId(_instance.getId());
		task.setProcessId(_process.getId());
		task.setProcessVersion(_process.getVersion());

		return task;
	}

	@Override
	protected Task testDeleteProcessTask_addTask() throws Exception {
		return testGetProcessTask_addTask();
	}

	@Override
	protected Task testGetProcessTask_addTask() throws Exception {
		User user = TestPropsValues.getUser();

		return _workflowMetricsRESTTestHelper.addTask(
			Arrays.asList(
				new UserAssignment(user.getUserId(), user.getFullName())),
			testGroup.getCompanyId(), _instance);
	}

	@Override
	protected Long testGetProcessTasksPage_getProcessId() throws Exception {
		return _process.getId();
	}

	@Override
	protected Task testGraphQLTask_addTask() throws Exception {
		return testGetProcessTask_addTask();
	}

	@Override
	protected Task testPatchProcessTask_addTask() throws Exception {
		return testGetProcessTask_addTask();
	}

	@Override
	protected Task testPatchProcessTaskComplete_addTask() throws Exception {
		User user = TestPropsValues.getUser();

		Task task = randomPatchTask();

		return _workflowMetricsRESTTestHelper.addTask(
			Arrays.asList(
				new UserAssignment(user.getUserId(), user.getFullName())),
			testGroup.getCompanyId(), RandomTestUtil.randomLong(), _instance,
			task.getName(), task.getNodeId(), task.getProcessId(),
			task.getProcessVersion(), task.getId());
	}

	private Instance _instance;
	private Process _process;

	@Inject
	private WorkflowMetricsRESTTestHelper _workflowMetricsRESTTestHelper;

}