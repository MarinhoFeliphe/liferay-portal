/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.runtime.internal.node;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlParserUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
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
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.memory.ChatMemoryAccess;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
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

	public interface ContentAnalyzerAssistant {

		@SystemMessage("""
       You are a strict content validation expert. Your task is to analyze and categorize the content and decide the next workflow transition.
       
       Available transitions are: 'reject' and 'approve'.

       Criteria:
       - Content must have a clear title, introduction and conclusion.
       - Content that is complete and well-structured MUST be approved.
       """)
		@UserMessage("""
		Analyze and categorize the content and complete the current node with the decided transition.
		
		This is the content: {{content}}
		""")
		public TokenStream analyze(@V("content") String content, InvocationParameters parameters);
	}

	public interface WritingAssistant {

		@SystemMessage("""
       You are a professional writing editor.
       
       Your sole task is to take the provided text and rewrite it to be significantly more concise, direct, and free of unnecessary filler words,
       nominalizations, and passive voice, while retaining the original meaning and professional tone.
       
       Only output the revised, concise text.
       
       Do not include any explanation, introduction, or conversation.
       """)
		@UserMessage("""
		This is the text to be rewritten : {{text}}
		""")
		public TokenStream refine(@V("text") String text);
	}

	public interface ChatAssistant extends ChatMemoryAccess {

		public TokenStream chat(
			@MemoryId String memoryId, @UserMessage String message,
			InvocationParameters parameters);
	}

	public class AssistantTool {

		@Tool("Call this tool to categorize a content. Input must be the the category name")
		public void categorizeContent(
			@P("Category name") String categoryName,
			InvocationParameters parameters) {

			ExecutionContext executionContext =
				parameters.get("executionContext");

			PermissionThreadLocal.setPermissionChecker(
				parameters.get("permissionChecker"));

			ServiceContext serviceContext =
				executionContext.getServiceContext();

			try (SafeCloseable safeCloseable =
					 CompanyThreadLocal.setCompanyIdWithSafeCloseable(
						 serviceContext.getCompanyId())) {

				Map<String, Serializable> workflowContext =
					executionContext.getWorkflowContext();

				ObjectEntry objectEntry =
					ObjectEntryLocalServiceUtil.getObjectEntry(
						GetterUtil.getLong(
							workflowContext.get("entryClassPK")));

				serviceContext.setAssetTagNames(new String[] {categoryName});

				ObjectEntryLocalServiceUtil.updateObjectEntry(
					objectEntry.getUserId(), objectEntry.getObjectEntryId(),
					objectEntry.getObjectEntryFolderId(),
					objectEntry.getValues(), serviceContext);
			}
			catch (Exception exception) {
				System.out.println(exception.getMessage());
			}
		}

		@Tool("Complete the current node")
		public void completeWorkflowTask(
			@P("Transition name") String transitionName,
			@P("A brief, one-sentence justification for the chosen transition.") String reason,
			InvocationParameters parameters) {

			try {
				ExecutionContext executionContext =
					parameters.get("executionContext");

				PermissionThreadLocal.setPermissionChecker(
					parameters.get("permissionChecker"));

				KaleoTaskInstanceToken kaleoTaskInstanceToken =
					executionContext.getKaleoTaskInstanceToken();

				_workflowTaskManager.assignWorkflowTaskToUser(
					kaleoTaskInstanceToken.getCompanyId(),
					kaleoTaskInstanceToken.getUserId(),
					kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId(),
					kaleoTaskInstanceToken.getUserId(), "", null,
					executionContext.getWorkflowContext());

				_workflowTaskManager.completeWorkflowTask(
					kaleoTaskInstanceToken.getCompanyId(),
					kaleoTaskInstanceToken.getUserId(),
					kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId(),
					transitionName, reason,
					executionContext.getWorkflowContext());
			}
			catch (PortalException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private String _getContent(ExecutionContext executionContext)
		throws PortalException {

		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();

		ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.getObjectEntry(
			GetterUtil.getLong(workflowContext.get("entryClassPK")));

		return HtmlParserUtil.extractText(
			GetterUtil.getString(objectEntry.getValues().get("content")));
	}

	@Override
	protected void doExecute(
		KaleoNode currentKaleoNode, ExecutionContext executionContext,
		List<PathElement> remainingPathElements) {

		/*McpTransport transport = new StreamableHttpMcpTransport.Builder()
			.url("http://localhost:8080/o/mcp/sse")
			.logRequests(true)
			.logResponses(true)
			.build();

		McpClient mcpClient = new DefaultMcpClient.Builder()
			.key("FooMCPClient")
			.transport(transport)
			.build();

		McpToolProvider toolProvider = McpToolProvider.builder()
			.mcpClients(mcpClient)
			.build();*/

		VertexAiGeminiStreamingChatModel model = VertexAiGeminiStreamingChatModel.builder()
			.project("upgrades-accelerator-liferay")
			.location("us-central1")
			.modelName("gemini-2.5-flash-lite")
			.logRequests(true)
			.logResponses(true)
			.build();

		ChatAssistant assistant = AiServices.builder(ChatAssistant.class)
			.streamingChatModel(model)
			.chatMemoryProvider(memoryId ->
				MessageWindowChatMemory
					.builder()
					.chatMemoryStore(_inMemoryChatMemoryStore)
					.maxMessages(10)
					.id(memoryId)
					.build())
			.build();

		System.out.println("Main Thread: " + Thread.currentThread().getId());
	}

	private final InMemoryChatMemoryStore _inMemoryChatMemoryStore = new InMemoryChatMemoryStore();

	private void _updateWorkflowContext(
			ChatResponse chatResponse, ExecutionContext executionContext) {

		try {
			Map<String, Serializable> workflowContext =
				executionContext.getWorkflowContext();

			workflowContext.put(
				"response", chatResponse.aiMessage().text());

			KaleoInstanceToken kaleoInstanceToken =
				executionContext.getKaleoInstanceToken();

			_workflowInstanceManager.updateWorkflowContext(
				kaleoInstanceToken.getCompanyId(),
				kaleoInstanceToken.getKaleoInstanceId(), workflowContext);
		}
		catch (WorkflowException exception) {
			System.out.println(exception.getMessage());
		}
	}

	private void _execute3(ExecutionContext executionContext) {
		VertexAiGeminiStreamingChatModel model = VertexAiGeminiStreamingChatModel.builder()
			.project("upgrades-accelerator-liferay")
			.location("us-central1")
			.modelName("gemini-2.5-flash-lite")
			.logRequests(true)
			.logResponses(true)
			.build();

		ChatAssistant assistant = AiServices.builder(ChatAssistant.class)
			.streamingChatModel(model)
			.chatMemoryProvider(memoryId ->
				MessageWindowChatMemory
					.builder()
					.chatMemoryStore(_inMemoryChatMemoryStore)
					.maxMessages(10)
					.id(memoryId)
					.build())
			.tools(
				new AssistantTool()
			).build();

		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();

		Map<String, Serializable> message =
			(Map<String, Serializable>)workflowContext.get("message");

		try {
			TokenStream tokenStream = assistant.chat(
				GetterUtil.getString(message.get("memoryId")),
				GetterUtil.getString(message.get("content")),
				InvocationParameters.from(
					Map.of(
						"executionContext", executionContext,
						"permissionChecker",
						PermissionThreadLocal.getPermissionChecker())));

			tokenStream
				.onCompleteResponse(
					response -> {
						System.out.println("Thread: " + Thread.currentThread().getId());
						System.out.println(response.aiMessage().text());

						ChatMemory chatMemory = assistant.getChatMemory(
							GetterUtil.getString(message.get("memoryId")));

						chatMemory.messages();

						_updateWorkflowContext(response, executionContext);

						model.close();
					})
				.onError(
					throwable -> {
						System.out.println("Thread: " + Thread.currentThread().getId());
						System.out.println(throwable.getMessage());

						model.close();
					}
				)
				.start();
		}
		catch (Exception exception) {
			System.out.println(exception.getMessage());
		}
	}

	private void _execute2(ExecutionContext executionContext) {
		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();

		Map<String, Serializable> content =
			(Map<String, Serializable>)workflowContext.get("content");

		VertexAiGeminiStreamingChatModel model = VertexAiGeminiStreamingChatModel.builder()
			.project("upgrades-accelerator-liferay")
			.location("us-central1")
			.modelName("gemini-2.5-flash-lite")
			.logRequests(true)
			.logResponses(true)
			.build();

		WritingAssistant assistant = AiServices.builder(
			WritingAssistant.class
		).streamingChatModel(
			model
		).build();

		TokenStream tokenStream = assistant.refine(
			GetterUtil.getString(content.get("value")));

		tokenStream
			.onCompleteResponse(
				response -> {
					System.out.println("Thread: " + Thread.currentThread().getId());
					System.out.println(response.aiMessage().text());

					_updateWorkflowContext(response, executionContext);

					model.close();
				})
			.onError(
				throwable -> {
					System.out.println("Thread: " + Thread.currentThread().getId());
					System.out.println(throwable.getMessage());

					model.close();
				}
			)
			.start();
	}

	private void _execute1(ExecutionContext executionContext) {
		VertexAiGeminiStreamingChatModel model = VertexAiGeminiStreamingChatModel.builder()
			.project("upgrades-accelerator-liferay")
			.location("us-central1")
			.modelName("gemini-2.5-flash-lite")
			.logRequests(true)
			.logResponses(true)
			.build();

		ContentAnalyzerAssistant assistant = AiServices.builder(
			ContentAnalyzerAssistant.class
		).streamingChatModel(
			model
		).tools(
			new AssistantTool()
		).build();

		try {
			TokenStream tokenStream = assistant.analyze(
				_getContent(executionContext),
				InvocationParameters.from(
					Map.of(
						"executionContext", executionContext,
						"permissionChecker",
						PermissionThreadLocal.getPermissionChecker())));

			tokenStream
				.beforeToolExecution(System.out::println)
				.onCompleteResponse(
					response -> {
						System.out.println("Thread: " + Thread.currentThread().getId());
						System.out.println(response.aiMessage().text());

						model.close();
					})
				.onError(
					throwable -> {
						System.out.println("Thread: " + Thread.currentThread().getId());
						System.out.println(throwable.getMessage());

						model.close();
					}
				)
				.start();
		}
		catch (Exception exception) {
			System.out.println(exception.getMessage());
		}

		System.out.println("Main Thread: " + Thread.currentThread().getId());
	}

	@Reference
	private WorkflowTaskManager _workflowTaskManager;

	@Reference
	private WorkflowInstanceManager _workflowInstanceManager;

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