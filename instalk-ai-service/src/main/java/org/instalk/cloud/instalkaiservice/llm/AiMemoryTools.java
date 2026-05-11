package org.instalk.cloud.instalkaiservice.llm;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.agent.tool.ToolSpecification;

import java.util.List;

public final class AiMemoryTools {

    private AiMemoryTools() {}

    /**
     * Tool the assistant calls when it needs prior personal context.
     */
    public static final ToolSpecification SEARCH_MEMORIES = ToolSpecification.builder()
            .name("search_memories")
            .description(
                    "Search stored memories for this user with this robot: objective facts (FACT), "
                            + "preferences (PREFERENCE), and notable events (EVENT). "
                            + "Call only when the reply depends on earlier personal context; skip for generic chat.")
            .parameters(JsonObjectSchema.builder()
                    .addStringProperty("query", "Short search query in the conversation language")
                    .addIntegerProperty("limit", "Maximum memories to return (default 6, max 20)")
                    .required("query")
                    .build())
            .build();

    public static List<ToolSpecification> all() {
        return List.of(SEARCH_MEMORIES);
    }
}
