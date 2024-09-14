package org.springframework.samples.petclinic.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * This class configures essential components for chat system.
 */
@Configuration
public class AgentConfig {

	/**
	 * Configure a bean of type ChatClient
	 */
	@Bean
	public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
		return chatClientBuilder.build();
	}

	/**
	 * Configure a bean of type ChatClientCustomizer
	 * @param vectorStore: configured below
	 * @param model: auto-configured in application properties
	 */
	@Bean
	public ChatClientCustomizer chatClientCustomizer(VectorStore vectorStore, ChatModel model) {
		// use a in-memory storage to track context and chat history between user interactions
		ChatMemory chatMemory = new InMemoryChatMemory();
		// use PromptChatMemoryAdvisor to access the stored conversation memory
		// use ModeledQuestionAnswerAdvisor to process user queries before retrieving relevant documents
		return b -> b.defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory),
				new ModeledQuestionAnswerAdvisor(vectorStore, SearchRequest.defaults(), model));
	}

	/**
	 * Configure a bean of type VectorStore, which is used to store the semantic vectors (embeddings).
	 * @param embeddingModel: auto-configured in application properties
	 */
	@Bean
	public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
		Resource resource = new DefaultResourceLoader().getResource("classpath:petclinic-terms-of-use.txt");
		TextReader textReader = new TextReader(resource);
		List<Document> documents = new TokenTextSplitter().apply(textReader.get());
		VectorStore store = new SimpleVectorStore(embeddingModel);
		store.add(documents);
		return store;
	}

}
