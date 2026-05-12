/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.internal.runtime.integration.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.test.performance.PerformanceTimer;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.AssumeTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.workflow.manager.WorkflowDefinitionManager;

import java.io.Closeable;

import java.util.Properties;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Feliphe Marinho
 */
@RunWith(Arquillian.class)
public class WorkflowInstanceManagerImplPerformanceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new AssumeTestRule("assume"), new LiferayIntegrationTestRule());

	public static void assume() {
		Assume.assumeTrue(Validator.isNull(System.getenv("JENKINS_HOME")));
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		Class<?> clazz = WorkflowInstanceManagerImplPerformanceTest.class;

		_properties = PropertiesUtil.load(
			clazz.getResourceAsStream(
				"dependencies/workflow-instance-performance.properties"),
			"UTF-8");

		_workflowInstancesCount = GetterUtil.getInteger(
			_properties.getProperty("workflow.instances.count"));
	}

	@Test
	public void testStartWorkflowInstance() throws Exception {
		Class<?> clazz = getClass();

		ClassLoader classLoader = clazz.getClassLoader();

		String workflowDefinitionName = RandomTestUtil.randomString();

		WorkflowDefinition workflowDefinition =
			_workflowDefinitionManager.deployWorkflowDefinition(
				FileUtil.getBytes(
					classLoader.getResourceAsStream(
						"com/liferay/portal/workflow/kaleo/dependencies" +
							"/basic-workflow-definition.json")),
				TestPropsValues.getCompanyId(), null, workflowDefinitionName,
				workflowDefinitionName, TestPropsValues.getUserId());

		try (Closeable closeable = new PerformanceTimer(
				GetterUtil.getInteger(
					_properties.getProperty(
						"workflow.instances.start.max.time")),
				StringBundler.concat(
					"Start ", _workflowInstancesCount, " workflow instances"))) {

			for (int i = 0; i < _workflowInstancesCount; i++) {
				_workflowInstanceManager.startWorkflowInstance(
					TestPropsValues.getCompanyId(), 0,
					TestPropsValues.getUserId(), workflowDefinition.getName(),
					workflowDefinition.getVersion(), null, null);
			}
		}
	}

	private static Properties _properties;
	private static int _workflowInstancesCount;

	@Inject
	private WorkflowDefinitionManager _workflowDefinitionManager;

	@Inject
	private WorkflowInstanceManager _workflowInstanceManager;

}
