package com.liferay.portal.workflow.kaleo.runtime.node;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;

/**
 * @author Feliphe Marinho
 */
@RunWith(Arquillian.class)
public class LLMNodeTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testDoExecute() throws Exception {
		StreamingChatModel model = VertexAiGeminiStreamingChatModel.builder()
			.project("gen-lang-client-0957447944")
			.location("us-central1")
			.modelName("gemini-pro")
			.build();

		model.chat("Tell me a long joke", new StreamingChatResponseHandler() {

			@Override
			public void onPartialResponse(String partialResponse) {
				System.out.print(partialResponse);
			}

			@Override
			public void onCompleteResponse(ChatResponse completeResponse) {
				System.out.print(completeResponse);
			}

			@Override
			public void onError(Throwable error) {
				System.out.print(error);
			}
		});
	}
}
