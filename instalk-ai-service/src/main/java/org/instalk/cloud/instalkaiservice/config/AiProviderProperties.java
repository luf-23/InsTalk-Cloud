package org.instalk.cloud.instalkaiservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class AiProviderProperties {

    private String key;
    private String url;
    private String embeddingUrl;
    private String embeddingModel;

    /**
     * LangChain4j OpenAI client expects a base like {@code .../v1} and appends {@code /chat/completions}.
     * A config value ending in {@code /v1/chat/completions} must not be shortened with a single
     * path-segment strip (that yields {@code .../v1/chat} and produces a broken double path).
     */
    public String chatBaseUrl() {
        return stripOpenAiChatCompletionsSuffix(url);
    }

    public String embeddingBaseUrl() {
        return stripOpenAiEmbeddingsSuffix(embeddingUrl);
    }

    private static String stripOpenAiChatCompletionsSuffix(String endpointUrl) {
        if (endpointUrl == null || endpointUrl.isBlank()) {
            return endpointUrl;
        }
        String u = endpointUrl.stripTrailing();
        if (u.endsWith("/chat/completions")) {
            return u.substring(0, u.length() - "/chat/completions".length());
        }
        return u;
    }

    private static String stripOpenAiEmbeddingsSuffix(String endpointUrl) {
        if (endpointUrl == null || endpointUrl.isBlank()) {
            return endpointUrl;
        }
        String u = endpointUrl.stripTrailing();
        if (u.endsWith("/embeddings")) {
            return u.substring(0, u.length() - "/embeddings".length());
        }
        return u;
    }
}
