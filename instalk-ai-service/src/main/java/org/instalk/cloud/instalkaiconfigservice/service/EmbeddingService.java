package org.instalk.cloud.instalkaiconfigservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class EmbeddingService {

    @Autowired
    @Qualifier("embeddingWebClient")
    private WebClient embeddingWebClient;

    @Value("${ai.embedding-model}")
    private String embeddingModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Double> embed(String input) {
        if (input == null || input.trim().isEmpty()) {
            return List.of();
        }
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", embeddingModel);
            requestBody.put("input", input.trim());

            String response = embeddingWebClient.post()
                    .bodyValue(objectMapper.writeValueAsString(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode dataNode = root.path("data");
            if (!dataNode.isArray() || dataNode.isEmpty()) {
                return List.of();
            }

            JsonNode embeddingNode = dataNode.get(0).path("embedding");
            if (!embeddingNode.isArray()) {
                return List.of();
            }

            return objectMapper.convertValue(embeddingNode, new TypeReference<List<Double>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
