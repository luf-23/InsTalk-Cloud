package org.instalk.cloud.instalkaiservice.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AiProviderProperties.class)
public class LangChainAiConfig {

    private static final String FALLBACK_CHAT_MODEL = "qwen-plus";

    @Bean
    public StreamingChatModel streamingChatModel(AiProviderProperties props) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(props.chatBaseUrl())
                .apiKey(props.getKey())
                .modelName(FALLBACK_CHAT_MODEL)
                .returnThinking(true)
                .timeout(Duration.ofMinutes(5))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    @Bean
    public ChatModel chatModel(AiProviderProperties props) {
        return OpenAiChatModel.builder()
                .baseUrl(props.chatBaseUrl())
                .apiKey(props.getKey())
                .modelName(FALLBACK_CHAT_MODEL)
                .timeout(Duration.ofMinutes(2))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(AiProviderProperties props) {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(props.embeddingBaseUrl())
                .apiKey(props.getKey())
                .modelName(props.getEmbeddingModel())
                .timeout(Duration.ofSeconds(60))
                .logRequests(false)
                .logResponses(false)
                .build();
    }
}
