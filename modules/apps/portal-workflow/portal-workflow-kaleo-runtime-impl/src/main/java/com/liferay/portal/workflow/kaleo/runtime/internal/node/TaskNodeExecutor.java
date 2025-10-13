/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.runtime.internal.node;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.workflow.kaleo.definition.DelayDuration;
import com.liferay.portal.workflow.kaleo.definition.DurationScale;
import com.liferay.portal.workflow.kaleo.definition.ExecutionType;
import com.liferay.portal.workflow.kaleo.definition.NodeType;
import com.liferay.portal.workflow.kaleo.definition.exception.KaleoDefinitionValidationException;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.model.KaleoTask;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoTimer;
import com.liferay.portal.workflow.kaleo.model.KaleoTransition;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.assignment.AggregateKaleoTaskAssignmentSelector;
import com.liferay.portal.workflow.kaleo.runtime.calendar.DueDateCalculator;
import com.liferay.portal.workflow.kaleo.runtime.graph.PathElement;
import com.liferay.portal.workflow.kaleo.runtime.node.BaseNodeExecutor;
import com.liferay.portal.workflow.kaleo.runtime.node.NodeExecutor;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoLogLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskAssignmentInstanceLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskLocalService;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author Michael C. Han
 */
@Component(service = NodeExecutor.class)
public class TaskNodeExecutor extends BaseNodeExecutor {

	@Override
	public NodeType getNodeType() {
		return NodeType.TASK;
	}

	@Override
	protected boolean doEnter(
			KaleoNode currentKaleoNode, ExecutionContext executionContext)
		throws PortalException {

		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();
		ServiceContext serviceContext = executionContext.getServiceContext();

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		KaleoTask kaleoTask = _kaleoTaskLocalService.getKaleoNodeKaleoTask(
			currentKaleoNode.getKaleoNodeId());

		Date dueDate = _calculateDueDate(kaleoTask);

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			_createTaskInstanceToken(
				executionContext, workflowContext, serviceContext,
				kaleoInstanceToken, kaleoTask, dueDate);

		executionContext.setKaleoTaskInstanceToken(kaleoTaskInstanceToken);

		kaleoActionExecutor.executeKaleoActions(
			KaleoNode.class.getName(), currentKaleoNode.getKaleoNodeId(),
			ExecutionType.ON_ASSIGNMENT, executionContext);

		notificationHelper.sendKaleoNotifications(
			KaleoNode.class.getName(), currentKaleoNode.getKaleoNodeId(),
			ExecutionType.ON_ASSIGNMENT, executionContext);

		kaleoTimerInstanceTokenLocalService.addKaleoTimerInstanceTokens(
			executionContext.getKaleoInstanceToken(),
			executionContext.getKaleoTaskInstanceToken(),
			kaleoTimerLocalService.getKaleoTimers(
				KaleoNode.class.getName(), currentKaleoNode.getKaleoNodeId()),
			executionContext.getWorkflowContext(),
			executionContext.getServiceContext());

		_kaleoLogLocalService.addTaskAssignmentKaleoLogs(
			null, kaleoTaskInstanceToken, "assigned-initial-task",
			workflowContext, serviceContext);

		return true;
	}

	@Override
	protected void doExecute(
		KaleoNode currentKaleoNode, ExecutionContext executionContext,
		List<PathElement> remainingPathElements) {

		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();

		VertexAiGeminiStreamingChatModel model = VertexAiGeminiStreamingChatModel.builder()
			.project("upgrades-accelerator-liferay")
			.location("us-central1")
			.modelName("gemini-2.5-flash-lite")
			.build();

			model.chat("Based in the content bellow, retrieve me a summary of it, also I'd like that this content is " +
					   "categorized, please retrieve this data in a json format but do not include the json delimiters, " +
					   "like this: {\"summary\":\"content summary\", \"category\":\"content category\"} content: " +
					   "The Butterfly's Journey: A Simple Life Cycle\n" +
					   "The life of a butterfly is a fascinating transformation that happens in four main stages. This process is called complete metamorphosis.\n" +
					   "\n" +
					   "Stage 1: The Egg\n" +
					   "A butterfly's life begins when an adult female lays a tiny egg, usually on a specific plant that the future caterpillar will eat. The egg stage is the shortest of the four.\n" +
					   "\n" +
					   "Stage 2: The Larva (Caterpillar)\n" +
					   "Next, the egg hatches into a larva, which we commonly call a caterpillar. A caterpillar's only job is to eat, eat, and eat! It grows rapidly, shedding its skin many times (a process called molting) to accommodate its increasing size.\n" +
					   "\n" +
					   "Stage 3: The Pupa (Chrysalis)\n" +
					   "Once the caterpillar is fully grown, it enters the pupa stage. For butterflies, the pupa is known as a chrysalis. During this seemingly resting stage, a massive change is taking place inside the protective casing. The caterpillar's body is entirely reorganized into the shape of an adult butterfly.\n" +
					   "\n" +
					   "Stage 4: The Adult Butterfly\n" +
					   "Finally, the adult butterfly emerges from the chrysalis. The adult's main job is to reproduce (mate and lay eggs) and feed on nectar from flowers, starting the entire cycle over again.",
				new StreamingChatResponseHandler() {

				@Override
				public void onPartialResponse(String partialResponse) {
				}

				@Override
				public void onCompleteResponse(ChatResponse completeResponse) {
					try {
						JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
							completeResponse.aiMessage().text());

						workflowContext.put(
							"category", jsonObject.getString("category"));
						workflowContext.put(
							"summary", jsonObject.getString("summary"));

						_kaleoInstanceLocalService.updateKaleoInstance(
							executionContext.getKaleoInstanceToken().getKaleoInstanceId(),
							workflowContext);

						System.out.println(completeResponse);
					}
					catch (JSONException e) {
						throw new RuntimeException(e);
					}
					catch (PortalException e) {
						throw new RuntimeException(e);
					}
					finally {
						model.close();
					}
				}

				@Override
				public void onError(Throwable error) {
					System.out.println(error);
					model.close();
				}
			});

		System.out.println("MAIN THREAD");
	}

	@Override
	protected void doExit(
			KaleoNode currentKaleoNode, ExecutionContext executionContext,
			List<PathElement> remainingPathElements)
		throws PortalException {

		String transitionName = executionContext.getTransitionName();

		KaleoTransition kaleoTransition = null;

		if (Validator.isNull(transitionName)) {
			kaleoTransition = currentKaleoNode.getDefaultKaleoTransition();
		}
		else {
			kaleoTransition = currentKaleoNode.getKaleoTransition(
				transitionName);
		}

		ExecutionContext newExecutionContext = new ExecutionContext(
			executionContext.getKaleoInstanceToken(),
			executionContext.getKaleoTaskInstanceToken(),
			executionContext.getWorkflowContext(),
			executionContext.getServiceContext());

		PathElement pathElement = new PathElement(
			null, kaleoTransition.getTargetKaleoNode(), newExecutionContext);

		remainingPathElements.add(pathElement);
	}

	private Date _calculateDueDate(KaleoTask kaleoTask)
		throws KaleoDefinitionValidationException {

		List<KaleoTimer> kaleoTimers = kaleoTimerLocalService.getKaleoTimers(
			KaleoNode.class.getName(), kaleoTask.getKaleoNodeId());

		if (kaleoTimers.isEmpty()) {
			return null;
		}

		TreeSet<Date> sortedDueDates = new TreeSet<>();

		for (KaleoTimer kaleoTimer : kaleoTimers) {
			DelayDuration delayDuration = new DelayDuration(
				kaleoTimer.getDuration(),
				DurationScale.parse(kaleoTimer.getScale()));

			sortedDueDates.add(
				_dueDateCalculator.getDueDate(new Date(), delayDuration));
		}

		return sortedDueDates.first();
	}

	private KaleoTaskInstanceToken _createTaskInstanceToken(
			ExecutionContext executionContext,
			Map<String, Serializable> workflowContext,
			ServiceContext serviceContext,
			KaleoInstanceToken kaleoInstanceToken, KaleoTask kaleoTask,
			Date dueDate)
		throws PortalException {

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			_kaleoTaskInstanceTokenLocalService.addKaleoTaskInstanceToken(
				kaleoInstanceToken.getKaleoInstanceTokenId(),
				kaleoTask.getKaleoTaskId(), kaleoTask.getName(),
				Collections.emptyList(), dueDate, workflowContext,
				serviceContext);

		executionContext.setKaleoTaskInstanceToken(kaleoTaskInstanceToken);

		_kaleoTaskAssignmentInstanceLocalService.addTaskAssignmentInstances(
			kaleoTaskInstanceToken,
			_aggregateKaleoTaskAssignmentSelector.getKaleoTaskAssignments(
				kaleoTask.getKaleoTaskAssignments(), executionContext),
			workflowContext, serviceContext);

		return _kaleoTaskInstanceTokenLocalService.getKaleoTaskInstanceToken(
			kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId());
	}

	@Reference
	private AggregateKaleoTaskAssignmentSelector
		_aggregateKaleoTaskAssignmentSelector;

	@Reference
	private DueDateCalculator _dueDateCalculator;

	@Reference
	private KaleoLogLocalService _kaleoLogLocalService;

	@Reference
	private KaleoTaskAssignmentInstanceLocalService
		_kaleoTaskAssignmentInstanceLocalService;

	@Reference
	private KaleoTaskInstanceTokenLocalService
		_kaleoTaskInstanceTokenLocalService;

	@Reference
	private KaleoTaskLocalService _kaleoTaskLocalService;

	@Reference
	private KaleoInstanceLocalService _kaleoInstanceLocalService;

}