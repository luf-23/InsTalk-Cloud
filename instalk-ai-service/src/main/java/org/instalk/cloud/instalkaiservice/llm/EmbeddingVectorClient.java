package org.instalk.cloud.instalkaiservice.llm;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingVectorClient {

    private final EmbeddingModel embeddingModel;

    public EmbeddingVectorClient(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public List<Double> embed(String input) {
        if (input == null || input.trim().isEmpty()) {
            return List.of();
        }
        try {
            Response<List<Embedding>> response =
                    embeddingModel.embedAll(List.of(TextSegment.from(input.trim())));
            if (response == null || response.content() == null || response.content().isEmpty()) {
                return List.of();
            }
            float[] vector = response.content().get(0).vector();
            List<Double> list = new ArrayList<>(vector.length);
            for (float v : vector) {
                list.add((double) v);
            }
            return list;
        } catch (Exception e) {
            return List.of();
        }
    }
}
