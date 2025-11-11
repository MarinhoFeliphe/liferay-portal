/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.rest.resource.v1_0.test;

import com.liferay.ai.hub.rest.resource.v1_0.util.SseUtil;
import com.liferay.ai.hub.rest.test.util.AIAssistantTestUtil;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.workflow.constants.WorkflowDefinitionConstants;
import com.liferay.portal.workflow.manager.WorkflowDefinitionManager;
import com.liferay.site.initializer.SiteInitializer;
import com.liferay.site.initializer.SiteInitializerRegistry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Feliphe Marinho
 */
@FeatureFlag("LPD-62272")
@RunWith(Arquillian.class)
public class TaskResourceTest extends BaseTaskResourceTestCase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		SiteInitializer siteInitializer =
			_siteInitializerRegistry.getSiteInitializer("ai-hub-initializer");

		siteInitializer.initialize(TestPropsValues.getGroupId());

		_workflowDefinitionManager.deployWorkflowDefinition(
			null, TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			StringUtil.randomId(), _WORKFLOW_DEFINITION_NAME,
			_getContentBytes());
	}

	@After
	public void tearDown() throws Exception {
		SseUtil.close();
	}

	@Override
	@Test
	public void testGetTaskSubscribe() throws Exception {
		_testGetTaskSubscribe(null, new ArrayList<>());
	}

	@Override
	@Test
	public void testPostTask() throws Exception {
		_testPostTask();
		_testPostTaskWithScope();
	}

	@Ignore
	@Test
	public void testPostTaskWithChangeTone() throws Exception {
		CountDownLatch countDownLatch = new CountDownLatch(5);
		List<String> lines = new ArrayList<>();

		_testGetTaskSubscribe(countDownLatch, lines);

		String originalText =
			"Thank you for your assistance. Your support is appreciated.";

		JSONObject jsonObject = HTTPTestUtil.invokeToJSONObject(
			JSONUtil.put(
				"context",
				JSONUtil.put(
					"text", originalText
				).put(
					"tone", "Casual"
				)
			).put(
				"type", WorkflowDefinitionConstants.NAME_CHANGE_TONE
			).toString(),
			"ai-hub/v1.0/tasks", Http.Method.POST);

		_assertEvent(
			countDownLatch, jsonObject.getLong("externalReferenceCode"),
			WorkflowDefinitionConstants.NAME_CHANGE_TONE, lines);

		String outputText = lines.get(4);

		outputText = StringUtil.removeSubstring(outputText, "data: ");

		String prompt = StringBundler.concat(
			"You are a strict evaluation agent. You are not generating or ",
			"editing text.\nYour task is to determine whether OUTPUT_TEXT ",
			"correctly changes the tone of\nORIGINAL_TEXT based on the ",
			"requested tone.\nRequested tone will be one of: Formal, ",
			"Friendly, Casual, or Persuasive.\nA tone change is considered ",
			"successful when:\n- The tone of OUTPUT_TEXT matches the ",
			"requested tone (Formal, Friendly, Casual, or Persuasive)\n- The ",
			"original meaning, intent, and clarity are preserved\n- ",
			"Modifications in vocabulary, phrasing, or sentence structure ",
			"serve only to change tone\n- No new information is added and no ",
			"meaning is removed\nYou will receive:\nORIGINAL_TEXT: the ",
			"original text\nOUTPUT_TEXT: the edited version\nTONE: the target ",
			"tone\nEvaluation criteria:\n- If OUTPUT_TEXT matches the ",
			"requested tone and preserves meaning, return \"Valid\"\n- If ",
			"OUTPUT_TEXT does not match the requested tone, changes meaning, ",
			"or adds/removes content, return \"Invalid\"\nRules:\n- Do NOT ",
			"rewrite text\n- Do NOT provide suggestions\n- Do NOT explain ",
			"your reasoning\n- Only determine whether the action \"change ",
			"tone\" was successfully performed\nYour entire response must ",
			"follow this exact format: \"Valid\" or \"Invalid\".\nThese are ",
			"some examples that you can follow:\ninput1: Hey, can you send me ",
			"the report?\ntone: Formal\nVALID output1: Could you please send ",
			"me the report?\ninput2: Please submit your response at your ",
			"earliest convenience\ntone: Casual\nVALID output2: Submit your ",
			"response, it is urgent.\ninput3: Your order has been shipped.\n",
			"tone: Friendly\nVALID output3: Your order has shipped! Can’t ",
			"wait for you to receive it.\ninput4: Our app helps people manage ",
			"their daily tasks.\ntone: Persuasive\nVALID output4: Boost your ",
			"productivity every day with our task-management app.");

		String validation = AIAssistantTestUtil.runValidatorAIModel(
			originalText, outputText, prompt);

		Assert.assertEquals("Valid", validation);
	}

	@Ignore
	@Test
	public void testPostTaskWithImproveWriting() throws Exception {
		CountDownLatch countDownLatch = new CountDownLatch(5);
		List<String> lines = new ArrayList<>();

		_testGetTaskSubscribe(countDownLatch, lines);

		String originalText =
			"In my opinion, I personally believe that this new approach " +
				"might potentially result in better outcomes.";

		JSONObject jsonObject = HTTPTestUtil.invokeToJSONObject(
			JSONUtil.put(
				"context", JSONUtil.put("text", originalText)
			).put(
				"type", WorkflowDefinitionConstants.NAME_IMPROVE_WRITING
			).toString(),
			"ai-hub/v1.0/tasks", Http.Method.POST);

		_assertEvent(
			countDownLatch, jsonObject.getLong("externalReferenceCode"),
			WorkflowDefinitionConstants.NAME_IMPROVE_WRITING, lines);

		String outputText = lines.get(4);

		outputText = StringUtil.removeSubstring(outputText, "data: ");

		String prompt = StringBundler.concat(
			"You are a strict evaluation agent. You are not generating or ",
			"editing text.\nYour task is to determine whether OUTPUT_TEXT ",
			"improves ORIGINAL_TEXT in writing quality.\nImprovement means:",
			"\n- More concise and direct\n- Removes filler, redundancy, and ",
			"unnecessary words\n- Reduces or eliminates passive voice\n- ",
			"Avoids nominalizations when possible\n- Maintains the original ",
			"meaning and professional tone.\nYou will receive:\n",
			"ORIGINAL_TEXT: the original text\nOUTPUT_TEXT: the edited version",
			"\nEvaluation criteria:\n- If OUTPUT_TEXT improves the writing ",
			"based on the criteria, then return \"Valid\"\n- If OUTPUT_TEXT ",
			"does not improve the writing, or adds/removes meaning, then ",
			"return: \"Invalid\"\nRules:\n- Do NOT rewrite text\n- Do NOT ",
			"propose suggestions\n- Do NOT evaluate style preferences\n- Only ",
			"determine whether the action \"improve writing\" was ",
			"successfully performed. \nYour entire response must follow this ",
			"exact format: \"Valid\" or \"Invalid\". These are some examples ",
			"that you can follow:\nInput: Today is going to be a great day!\n",
			"VALID output1: Today will be great.\nVALID output2: Today will ",
			"be a great day.");

		String validation = AIAssistantTestUtil.runValidatorAIModel(
			originalText, outputText, prompt);

		Assert.assertEquals("Valid", validation);
	}

	@Ignore
	@Test
	public void testPostTaskWithTypeFixSpellingAndGrammar() throws Exception {
		CountDownLatch countDownLatch = new CountDownLatch(5);
		List<String> lines = new ArrayList<>();

		_testGetTaskSubscribe(countDownLatch, lines);

		JSONObject jsonObject = HTTPTestUtil.invokeToJSONObject(
			JSONUtil.put(
				"context", JSONUtil.put("text", "Thi text ix wrongy")
			).put(
				"type",
				WorkflowDefinitionConstants.NAME_FIX_SPELLING_AND_GRAMMAR
			).toString(),
			"ai-hub/v1.0/tasks", Http.Method.POST);

		_assertEvent(
			countDownLatch, jsonObject.getLong("externalReferenceCode"),
			WorkflowDefinitionConstants.NAME_FIX_SPELLING_AND_GRAMMAR, lines);

		Assert.assertEquals("data: This text is wrong.", lines.get(4));
	}

	private static byte[] _getContentBytes() throws Exception {
		InputStream inputStream = TaskResourceTest.class.getResourceAsStream(
			"dependencies/workflow-definition.json");

		String content = StringUtil.read(inputStream);

		return content.getBytes();
	}

	private void _assertEvent(
			CountDownLatch countDownLatch, long eventId, String eventName,
			List<String> lines)
		throws Exception {

		Assert.assertTrue(countDownLatch.await(10, TimeUnit.SECONDS));

		Assert.assertEquals(lines.toString(), 5, lines.size());
		Assert.assertEquals("event: " + eventName, lines.get(2));
		Assert.assertEquals("id: " + eventId, lines.get(3));
	}

	private void _testGetTaskSubscribe(
			CountDownLatch countDownLatch2, List<String> lines)
		throws Exception {

		CountDownLatch countDownLatch1 = new CountDownLatch(2);

		HttpClient httpClient = HttpClient.newBuilder(
		).connectTimeout(
			Duration.ofSeconds(5)
		).build();

		String credentials =
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD;

		CompletableFuture<HttpResponse<InputStream>> completableFuture =
			httpClient.sendAsync(
				HttpRequest.newBuilder(
				).header(
					"Accept", "text/event-stream"
				).header(
					"Authorization",
					"Basic " + Base64.encode(credentials.getBytes())
				).uri(
					URI.create(
						"http://localhost:8080/o/ai-hub/v1.0/tasks/subscribe")
				).GET(
				).build(),
				HttpResponse.BodyHandlers.ofInputStream());

		completableFuture.thenAccept(
			response -> {
				try (InputStream inputStream = response.body();
					BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(inputStream))) {

					String line = "";

					while ((line = bufferedReader.readLine()) != null) {
						if (line.isEmpty()) {
							continue;
						}

						countDownLatch1.countDown();

						if (countDownLatch2 != null) {
							countDownLatch2.countDown();
						}

						lines.add(line);
					}
				}
				catch (Exception exception) {
					_log.error(exception);
				}
			});

		Assert.assertTrue(countDownLatch1.await(10, TimeUnit.SECONDS));

		Assert.assertEquals(lines.toString(), 2, lines.size());
		Assert.assertEquals("event: Subscribe", lines.get(0));
		Assert.assertEquals("data: Successfully Subscribed", lines.get(1));
	}

	private void _testPostTask() throws Exception {
		JSONObject jsonObject = HTTPTestUtil.invokeToJSONObject(
			JSONUtil.put(
				"context", JSONUtil.put("text", RandomTestUtil.randomString())
			).put(
				"type", _WORKFLOW_DEFINITION_NAME
			).toString(),
			"ai-hub/v1.0/tasks", Http.Method.POST);

		WorkflowInstance workflowInstance =
			_workflowInstanceManager.getWorkflowInstance(
				TestPropsValues.getCompanyId(),
				jsonObject.getLong("externalReferenceCode"));

		Assert.assertEquals(
			_WORKFLOW_DEFINITION_NAME,
			workflowInstance.getWorkflowDefinitionName());
	}

	private void _testPostTaskWithScope() throws Exception {
		Group group = GroupTestUtil.addGroup();

		JSONObject jsonObject = HTTPTestUtil.invokeToJSONObject(
			JSONUtil.put(
				"context", JSONUtil.put("text", RandomTestUtil.randomString())
			).put(
				"scope",
				JSONUtil.put(
					"externalReferenceCode", group.getExternalReferenceCode())
			).put(
				"type", _WORKFLOW_DEFINITION_NAME
			).toString(),
			"ai-hub/v1.0/tasks", Http.Method.POST);

		WorkflowInstance workflowInstance =
			_workflowInstanceManager.getWorkflowInstance(
				TestPropsValues.getCompanyId(),
				jsonObject.getLong("externalReferenceCode"));

		Assert.assertEquals(group.getGroupId(), workflowInstance.getGroupId());
		Assert.assertEquals(
			_WORKFLOW_DEFINITION_NAME,
			workflowInstance.getWorkflowDefinitionName());
	}

	private static final String _WORKFLOW_DEFINITION_NAME =
		"Workflow Definition";

	private static final Log _log = LogFactoryUtil.getLog(
		TaskResourceTest.class);

	@Inject
	private static SiteInitializerRegistry _siteInitializerRegistry;

	@Inject
	private static WorkflowDefinitionManager _workflowDefinitionManager;

	@Inject
	private WorkflowInstanceManager _workflowInstanceManager;

}