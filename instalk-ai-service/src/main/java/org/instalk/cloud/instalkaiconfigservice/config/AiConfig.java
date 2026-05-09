package org.instalk.cloud.instalkaiconfigservice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiConfig {
    @Value("${ai.key}")
    private String API_KEY;
    @Value("${ai.url}")
    private String API_URL;
    @Value("${ai.embedding-url}")
    private String EMBEDDING_URL;
    @Bean
    public WebClient aiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(API_URL)
                .defaultHeader("Authorization", "Bearer " + API_KEY)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean(name = "embeddingWebClient")
    public WebClient embeddingWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(EMBEDDING_URL)
                .defaultHeader("Authorization", "Bearer " + API_KEY)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
