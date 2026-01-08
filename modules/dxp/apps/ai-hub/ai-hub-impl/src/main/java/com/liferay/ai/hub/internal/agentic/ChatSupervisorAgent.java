/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.agentic;

import com.liferay.ai.hub.agentic.Agent;
import com.liferay.ai.hub.agentic.AgentContext;
import com.liferay.portal.kernel.util.GetterUtil;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.a2a.DefaultA2AClientBuilder;
import dev.langchain4j.agentic.a2a.DefaultA2AService;
import dev.langchain4j.agentic.internal.AgentExecutor;
import dev.langchain4j.agentic.internal.AgentSpecsProvider;
import dev.langchain4j.agentic.internal.AgentUtil;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

/**
 * @author Feliphe Marinho
 */
@Component(service = Agent.class)
public class ChatSupervisorAgent implements Agent {

	@Override
	public void invoke(AgentContext agentContext) {
		ChatModel chatModel = VertexAiGeminiChatModel.builder(
			).project(
				"ai-hub-liferay"
			).location(
				"us-central1"
			).modelName(
				"gemini-2.5-flash-lite"
			).logRequests(
				true
			).logResponses(
				true
			).build();

		/*StyleEditor styleEditor = AgenticServices
			.agentBuilder(StyleEditor.class)
			.chatModel(chatModel)
			.build();
		UntypedAgent untypedAgent = new DefaultA2AService(
		).a2aBuilder(
			"http://localhost:8080", UntypedAgent.class
		).inputKeys(
			"text"
		).build();
		AgentExecutor agentExecutor = AgentUtil.agentToExecutor(
			new ImproveWritingAgent()
		);
		AgenticServices.a2aBuilder(
			"http://localhost:8080"
		).inputKeys(
			"foo"
		).build();*/

		/*AgenticServices.a2aBuilder(
			"http://localhost:8080"
		).inputKeys(
			"foo"
		).build();

		StyleEditor styleEditor = AgenticServices
			.agentBuilder(StyleEditor.class)
			.chatModel(chatModel)
			.build();*/

		HumanInTheLoop humanInTheLoop1 = AgenticServices
			.humanInTheLoopBuilder()
			.description("An agent that improve a given text, making it more concise.")
			.outputKey("text")
			.requestWriter(request -> {
				System.out.println(request);
				System.out.print("> ");
			})
			.responseReader(() -> "This text is incorrect.")
			.build();
		HumanInTheLoop humanInTheLoop2 = AgenticServices
			.humanInTheLoopBuilder()
			.description("An agent that change the tone of a given text to a chosen tone.")
			.outputKey("text")
			.requestWriter(request -> {
				System.out.println(request);
				System.out.print("> ");
			})
			.responseReader(() -> "There appears to be an inaccuracy in this text.")
			.build();

		SupervisorAgent supervisorAgent = AgenticServices
			.supervisorBuilder()
			.chatModel(chatModel)
			.subAgents(
				new FixSpellingAndGrammarAgent(),
				new AgentImpl("An agent that change the tone of a given text to a chosen tone.", "There appears to be an inaccuracy in this text."),
				new AgentImpl("An agent that improve a given text, making it more concise.", "This text is incorrect."))
			.responseStrategy(SupervisorResponseStrategy.SUMMARY)
			.build();

		System.out.println(supervisorAgent.invoke(
			GetterUtil.getString(agentContext.getInput().get("message"))));
	}

	public interface StyleEditor {

		@UserMessage("""
        You are a professional editor.
        Analyze and rewrite the following story to better fit and be more coherent with the {{style}} style.
        Return only the story and nothing else.
        The story is "{{story}}".
        """)
		@dev.langchain4j.agentic.Agent("Edits a story to better fit a given style")
		String editStory(@V("story") String story, @V("style") String style);
	}

	public class ImproveWritingAgent {

		@dev.langchain4j.agentic.Agent(
			description = "An agent that rephrases a given text, making it more concise.",
			name = "ImproveWriting",
			outputKey = "rewrittenText")
		public String improve(Map<String, Object> input) {
			return "This text is incorrect.";
		}
	}

	public class FixSpellingAndGrammarAgent {

		@dev.langchain4j.agentic.Agent(
			description = "An agent that fixes spelling and grammar error of a text",
			name = "fixSpellingAndGrammar",
			outputKey = "rewrittenText")
		public String fix(Map<String, Object> input) {
			return "This text is wrong.";
		}
	}

	/*public interface ChatAgent {

		@UserMessage("Respond the message of the user: {{message}}")
		@dev.langchain4j.agentic.Agent(
			description = "An agent that handles the user request by respond user message if no other agent was able to handle.",
			outputName = "response")
		String chatAgent(@V("message") String message);
	}*/


	public class AgentImpl implements AgentSpecsProvider {

		public AgentImpl(String description, String result) {
			_description = description;
			_result = result;
		}

		private final String _description;
		private final String _result;

		@dev.langchain4j.agentic.Agent
		public String invoke(Map<String, Object> input) {
			return _result;
		}

		@Override
		public String inputKey() {
			return "context";
		}

		@Override
		public String outputKey() {
			return "rewrittenText";
		}

		@Override
		public String description() {
			return _description;
		}

		@Override
		public boolean async() {
			return false;
		}

		@Override
		public AgentListener listener() {
			return null;
		}
	}
}
